package com.keystone.kms.domain

import java.time.Instant
import java.util.UUID
import scala.annotation.tailrec

/** Representative of the versions list and change history audit record.
  * Maintaining this helps provide clarity to which versions were ever created
  * for a specific key as well as their individual statuses. This provides a
  * relatively concrete record of which versions exist, when they were
  * changed/modified, why they were changed/modified, and corresponding timestamps.
  * The structure of Versions is as follows:
  * - entries: List[KeyVersion] - a list of all key versions that exist
  * (Active, Inactive, or Retired) for a specific key.
  * - changeHistory: List[VersionRecord] - a list of all changes made to a specific
  * key's version and a specific rationale attached to each change.
  */
case class Versions private (
                            entries: List[KeyVersion],
                            changeHistory: List[VersionRecord])

object Versions:
    /** Creates an empty Versions instance for a newly created key. This represents
      * a key that has not yet been provisioned with any key material.
      */
    def create(): Versions =
        Versions(
            entries=List.empty,
            changeHistory=List.empty)

    /** Transitions the current head version to a target status, recording the change
      * in changeHistory under the supplied rationale and timestamp. This is a single-concern
      * primitive — it transitions one version's status and nothing else. Public callers
      * never invoke this directly — lifecycleWindDown, compromiseRetire, and rotate
      * each supply the target status and rationale appropriate to their own trigger.
      */
    private def transitionHead(
                              versions: Versions,
                              targetStatus: KeyVersionStatus,
                              rationale: KeyVersionRationale,
                              occurredAt: Instant): Either[String, Versions] =

        versions.entries.headOption match
            case None =>
                Left("Illegal operation: no version present to transition.")
            case Some(current) =>
                current.withStatus(targetStatus).map { transitioned =>
                    val record = VersionRecord(
                        versionId = transitioned.versionId,
                        status = transitioned.status,
                        rationale = rationale,
                        changedAt = occurredAt)

                    versions.copy(
                        entries = transitioned :: versions.entries.tail,
                        changeHistory = record :: versions.changeHistory)
                }

    /** Creates a new Active version of key material and records the event in changeHistory.
      * This is a single-concern primitive — it creates one version and nothing else. Requires
      * that the current head version is not Active (enforcing only one Active version at a time)
      *  and that the supplied rationale matches the most recent changeHistory entry (enforcing
      * that resumeWithNewVersion is called in the correct context following a wind-down).
      * Public callers never invoke this directly — rotate and resumeWithNewVersion delegate
      * here after establishing their own preconditions.
      */
    private def createNewVersion(
                                versions: Versions,
                                expirationDate: Option[Instant],
                                rationale: KeyVersionRationale,
                                occurredAt: Instant): Either[String, Versions] =
        versions.entries.headOption match
            case None =>
                Left("Illegal operation: key material must be first provisioned.")
            case Some(current) if current.status == KeyVersionStatus.Active =>
                Left("Illegal operation: only one version can be active at a time.")
            case Some(current) if versions.changeHistory.headOption.map(_.rationale).contains(rationale) =>
                val replacement = KeyVersion.createVersion(
                    previousVersion=Some(current),
                    createdAt=occurredAt,
                    status=KeyVersionStatus.Active,
                    expirationDate=expirationDate
                )

                val record = VersionRecord(
                    versionId=replacement.versionId,
                    status=replacement.status,
                    rationale=rationale,
                    changedAt=occurredAt
                )

                Right(versions.copy(
                    entries=replacement :: versions.entries,
                    changeHistory=record :: versions.changeHistory
                ))
            case Some(_) if versions.changeHistory.isEmpty =>
                Left("Illegal operation: no change history exists to validate rationale against.")
            case Some(_) =>
                Left("Illegal operation: rationale must match most recent change history entry.")

    /** Shared mechanics for any full rotation: validates the current version's status transition,
      * creates its replacement, and records both halves of the event under one rationale
      * and timestamp. Public callers never invoke this directly — each rotation method
      * supplies the target status and rationale appropriate to its own trigger.
      */
    private def rotate(
                        versions: Versions,
                        targetStatus: KeyVersionStatus,
                        rationale: KeyVersionRationale,
                        occurredAt: Instant,
                        newExpirationDate: Option[Instant]): Either[String, Versions] =
        transitionHead(versions, targetStatus, rationale, occurredAt).flatMap{ transitionedVersion =>
            createNewVersion(transitionedVersion, newExpirationDate, rationale, occurredAt)
        }

    /** Shared mechanics for retiring a specific version: locates the target version
      * by id, validates it is in an Inactive state, and updates its status to a Retired status.
      * Public callers never invoke this directly — retireVersion supplies the target id
      * and delegates here.
      */
    private def retire(
                        entries: List[KeyVersion],
                        targetVersion: UUID): Either[String, List[KeyVersion]] =
        entries match
            case Nil =>
                Left("Illegal operation: target version not found.")
            case head :: rest if head.versionId == targetVersion =>
                head.status match
                    case KeyVersionStatus.Inactive =>
                        head.withStatus(KeyVersionStatus.Retired).map(_ :: rest)
                    case KeyVersionStatus.Retired =>
                        Left("Illegal operation: version is already retired.")
                    case KeyVersionStatus.Active =>
                        Left("Illegal operation: Active version must rotate before retiring.")
            case head :: rest =>
                retire(rest, targetVersion).map(head :: _)

    /** Resolves the human-readable version number to its corresponding internal UUID.
      * Used exclusively by the Int convenience overload of retireVersion to delegate
      * to the UUID-based implementation without duplicating retirement logic.
      */
    @tailrec
    private def getVersionId(entries: List[KeyVersion], versionNumber: Int): Either[String, UUID] =
        entries match
            case Nil =>
                Left("Invalid version number; VersionId not found.")
            case head :: rest if head.versionNumber == versionNumber =>
                Right(head.versionId)
            case _ :: rest =>
                getVersionId(rest, versionNumber)

    extension (v: Versions)
        /** The method for initial provisioning of a key. This creates the initial version
          * of a key and will allow for the KeyStatusNotice.PendingApproval flag to be removed,
          * and should be called sequentially after actual key material has been created.
          */
        def provision(createdAt: Instant, expirationDate: Option[Instant]): Either[String, Versions] =
            if v.entries.nonEmpty then
                Left("Illegal operation: unable to provision a key that already has a version history.")
            else
                val newVersion = KeyVersion.createVersion(
                    previousVersion=None,
                    createdAt=createdAt,
                    status=KeyVersionStatus.Active,
                    expirationDate=expirationDate)

                val record = VersionRecord(
                    versionId=newVersion.versionId,
                    status=newVersion.status,
                    rationale=KeyVersionRationale.InitialProvisioning,
                    changedAt=createdAt)

                Right(
                    v.copy(
                        entries=newVersion :: v.entries,
                        changeHistory=record :: v.changeHistory))

        /** The method for calling a lifecycle wind down on a key. This method will place
          * the current active version into an inactive status (allowing dependency drain).
          *
          * Note: In order to complete the rotation, resumeWithNewVersion must be called with
          * the same rationale.
          */
        def lifecycleWindDown(createdAt:Instant): Either[String, Versions] =
            transitionHead(v, KeyVersionStatus.Inactive, KeyVersionRationale.LifecycleRotation, createdAt)

        /** The method for calling a standard lifecycle rotation on a key. This method
          * will place the previously active version into an inactive status (allowing dependency
          * drain) and creates a new active version.
          */
        def lifecycleRotation(createdAt: Instant, expirationDate: Option[Instant]): Either[String, Versions] =
            rotate(v, KeyVersionStatus.Inactive, KeyVersionRationale.LifecycleRotation, createdAt, expirationDate)

        /** The method for calling a compromise retire on a key version. This method
          * will retire the previously active version of a key, preparing the key
          * for decommissioning.
          *
          * Note: This is a terminal process. A key version marked for retiring without rotation will
          * be decommissioned.
          */
        def compromiseRetire(createdAt: Instant): Either[String, Versions] =
            transitionHead(v, KeyVersionStatus.Retired, KeyVersionRationale.CompromiseRotation, createdAt)

        /** The method for calling a compromised rotation on a key. This method will skip the inactive
          * state and place the previously active version of a key directly into a retired status and generates
          * a new active version for use
          */
        def compromiseRotation(createdAt: Instant, expirationDate: Option[Instant]): Either[String, Versions] =
            rotate(v, KeyVersionStatus.Retired, KeyVersionRationale.CompromiseRotation, createdAt, expirationDate)

        /** Retires a specific key version by its internal identifier. The target version
          * must be in an Inactive state — Active versions must be rotated before retiring,
          * and already-Retired versions cannot be retired again. A corresponding audit
          * record is appended to changeHistory on success.
          *
          * Note: Currently the default rationale is LifecycleRotation as other rationales are
          * added to move a key version from Active to Inactive, the rational will need to be migrated
          * to accept it as a parameter.
          */
        def retireVersion(versionId: UUID, changedAt: Instant): Either[String, Versions] =
            retire(v.entries, versionId).map { entries =>
                val record = VersionRecord(
                    versionId = versionId,
                    status = KeyVersionStatus.Retired,
                    rationale = KeyVersionRationale.LifecycleRotation,
                    changedAt = changedAt
                )

                v.copy(
                    entries = entries,
                    changeHistory = record :: v.changeHistory
                )
            }

        /** Convenience overload of retireVersion that accepts a human-readable version
          * number rather than an internal UUID. Resolves the version number to its
          * corresponding UUID and delegates to the UUID-based implementation — no
          * retirement logic is duplicated here.
          */
        def retireVersion(versionNumber: Int, changedAt: Instant): Either[String, Versions] =
            getVersionId(v.entries, versionNumber).flatMap{ versionId =>
                retireVersion(versionId, changedAt)
            }

        /** The method for completing a full rotation of a key. This method will create a
          * new version of key material for use
          *
          * Note: If the rationale provided does not match the previous rational provided when
          * the active version was transitioned, the method will return a Left.
          *
          * Note: unlike provision/lifecycleRotation/compromiseRotation, this call intentionally
          * reads Instant.now() directly rather than accepting a timestamp parameter — resuming
          * is the immediate act itself, not a step whose timestamp originates from an earlier
          * point in the engine's workflow.
          */
        def resumeWithNewVersion(rationale: KeyVersionRationale, expirationDate: Option[Instant]): Either[String, Versions] =
            createNewVersion(v, expirationDate, rationale, Instant.now())
