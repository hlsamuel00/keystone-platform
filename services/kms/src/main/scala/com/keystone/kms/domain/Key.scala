package com.keystone.kms.domain

import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

/** Representative of the key identifier; this type provides clarity to nested fields
  * where the java.util.UUID wouldn't provide clarity to what was contained
  */
type KeyId = UUID

/** Representative of the basic structure of a key. Keys diverge into two main
  * types: ManagedKey and RootKeyShare. ManagedKey(s) are every type of key that
  * KMS maintains, whereas RootKeyShare(s) are treated fundamentally different;
  * although they share the same convention of being called a key, they differ greatly
  * in purpose as neither share can be utilized on its own, but still share a core foundation
  * in its lifecycle. The required fields for a Key are fields that are consistent
  * across both RootKeyShare(s) and ManagedKey(s):
  * - keyId: KeyId - the unique identifier for a specific key randomly generated
  * - keyType: KeyType - the unique type of key
  * - exceptionStatus: Option[KeyStatusNotice] - an optional key-level flag indicating any key-level
  * concerns that impact the key as a whole
  * - versions: Versions - all versions of a key, including an audit record indicating any changes made
  * to any versions
  */
sealed trait Key:
    def keyId: KeyId
    def keyType: KeyType
    def exceptionStatus: Option[KeyStatusNotice]
    def versions: Versions

object Key:
    extension (k: Key)
        def currentVersion: Option[KeyVersion] =
            k.versions.entries.headOption

/** Representative of all keys managed by KMS. These keys require additional fields that
  * don't apply to RootKeyShare(s). The unique fields are as follows:
  * - keyId: KeyId - the unique identifier for the key
  * - keyType: KeyType - the specific type of key (KEK, DEK, Transmission, etc.)
  * - exceptionStatus: Option[KeyStatusNotice] - key-level status indicators
  * - versions: Versions - all versions of a key with an audit record
  * - keyName: String - the name of the respective key
  * - permissions: Permissions - the services and their respective permissions on the respective key
  * - dependencies: Set[KeyId] - a set of KeyId(s) who depend on this respective key; a
  * major distinction to be highlighted, this is reversed from typical meaning
  * - owner: String - the service owning the specific key
  * - algorithm: KeyAlgorithm - the specific algorithm the key uses for cryptographic operations
  * - description: String - a brief description of the key's purpose
  * - createdDate: Instant - the date the key was created
  */
case class ManagedKey private (
                              keyId: KeyId,
                              keyType: KeyType,
                              exceptionStatus: Option[KeyStatusNotice],
                              versions: Versions,
                              keyName: String,
                              permissions: Permissions,
                              dependencies: Set[KeyId],
                              owner: String,
                              algorithm: KeyAlgorithm,
                              description: String,
                              createdDate: Instant) extends Key


object ManagedKey:
    /** Creates a ManagedKey derived from Key. The smart constructor requires
      * the following fields:
      * - keyName: String - the name of the respective key following a naming convention of
      *   [owning service]-[key purpose]-[key algorithm]-key (all hyphen delineated), ex:
      *   `payments-service-payment-processing-AES-key`
      * - keyOwner: String - the owning service of the respective key
      * - keyType: KeyType - the type of this respective key
      * - algorithm: KeyAlgorithm - the algorithm this key utilizes
      * - description: String - a brief explanation of the keys purpose,
      *   ex: `encrypting data for accounts-service when payments are processed`
      *
      * Note: keyId, exceptionStatus, versions, permissions, and dependencies are all instantiated
      * internally and are provided by the platform.
      */
    def create(
                keyName: String,
                keyOwner: String,
                keyType: KeyType,
                algorithm: KeyAlgorithm,
                description: String): ManagedKey =

        val now = Instant.now()
        ManagedKey(
            keyId=UUID.randomUUID(),
            keyType=keyType,
            exceptionStatus=Some(KeyStatusNotice.PendingApproval),
            versions=Versions.create(),
            keyName=keyName,
            owner=keyOwner,
            algorithm=algorithm,
            description=description,
            createdDate=now,
            permissions=Permissions.create(),
            dependencies=Set.empty
        )

    extension (k: ManagedKey)
        /** Adds a dependent key to this key's dependency set. Represents that the
          * dependent key relies on this key's material — used to identify affected
          * keys during rotation events (e.g., DEK rewrapping when a KEK rotates).
          */
        def withDependency(dependentKeyId: KeyId): ManagedKey =
            k.copy(dependencies=k.dependencies + dependentKeyId)

        /** Removes a dependent key from this key's dependency set. Used once a
          * dependent key has been rewrapped or retired and no longer relies on
          * this key's material. Removing a KeyId not present in the set is a
          * no-op — this method never fails.
          */
        def removeDependency(dependentKeyId: KeyId): ManagedKey =
            k.copy(dependencies=k.dependencies - dependentKeyId)

        /** Provisions the first version of key material for this key, transitioning it
          * from PendingApproval to active operation. Must be called after the engine has
          * generated the actual key bytes. Requires exceptionStatus to be PendingApproval —
          * keys in any other state cannot be provisioned.
          */
        def provision(expirationDate: Option[Instant]): Either[String, ManagedKey] =
            k.exceptionStatus match
                case None =>
                    Left("Illegal request: cannot request provisioning key with missing PendingApproval flag.")
                case Some(KeyStatusNotice.PendingApproval) =>
                    k.versions.provision(Instant.now(), expirationDate).map{ updatedVersions =>
                        k.copy(versions=updatedVersions)
                    }
                case Some(KeyStatusNotice.Compromised) =>
                    Left("Illegal request: cannot request provisioning on a compromised key.")
                case Some(KeyStatusNotice.Rejected | KeyStatusNotice.Decommissioned) =>
                    Left("Illegal request: cannot request provisioning on a terminal key.")

        /** Initiates a scheduled lifecycle rotation, placing the current Active version
          * into an Inactive state for dependency draining and creating a new Active version.
          * Requires no active exception status — compromised or pending keys cannot be
          * rotated through the standard lifecycle path.
          */
        def lifecycleRotation(expirationDate: Option[Instant]): Either[String, ManagedKey] =
            k.exceptionStatus match
                case None =>
                    k.versions.lifecycleRotation(Instant.now(), expirationDate).map{ updatedVersions =>
                        k.copy(versions=updatedVersions)
                    }
                case Some(KeyStatusNotice.PendingApproval) =>
                    Left("Illegal request: lifecycle rotation cannot be completed, key must be first provisioned.")
                case Some(KeyStatusNotice.Compromised) =>
                    Left("Illegal request: cannot request lifecycle rotation on a compromised key.")
                case Some(KeyStatusNotice.Rejected | KeyStatusNotice.Decommissioned) =>
                    Left("Illegal request: cannot request lifecycle rotation on a terminal key.")

        /** Initiates a scheduled wind down of a key version, placing the current Active version
          * into an Inactive state for dependency draining. Requires no active exception status -
          * compromised or pending keys cannot be transitioned through the standard lifecycle path.
          *
          * Note: To complete the full rotation lifecycle, resumeWithNewVersion must be called with
          * the same rationale.
          */
        def lifecycleWindDown: Either[String, ManagedKey] =
            k.exceptionStatus match
                case None =>
                    k.versions.lifecycleWindDown(Instant.now()).map{ updatedVersions =>
                        k.copy(versions=updatedVersions)
                    }
                case Some(KeyStatusNotice.PendingApproval) =>
                    Left("Illegal request: lifecycle wind down cannot be completed, key must be first provisioned.")
                case Some(KeyStatusNotice.Compromised) =>
                    Left("Illegal request: cannot request lifecycle wind down on a compromised key.")
                case Some(KeyStatusNotice.Rejected | KeyStatusNotice.Decommissioned) =>
                    Left("Illegal request: cannot request lifecycle wind down on a terminal key.")

        /** Initiates an emergency rotation in response to a key compromise, immediately
          * retiring the current Active version without a draining period and creating a
          * new Active version. Requires exceptionStatus to be Compromised — the flag must
          * be set explicitly before this rotation can be triggered.
          */
        def compromiseRotation(expirationDate: Option[Instant]): Either[String, ManagedKey] =
            k.exceptionStatus match
                case None =>
                    Left("Illegal request: compromised flag must be set before requesting compromise rotation.")
                case Some(KeyStatusNotice.Compromised) =>
                    k.versions.compromiseRotation(Instant.now(), expirationDate).map{ updatedVersions =>
                        k.copy(versions=updatedVersions)
                    }
                case Some(KeyStatusNotice.PendingApproval) =>
                    Left("Illegal request: compromise rotation cannot be completed, key must be first provisioned.")
                case Some(KeyStatusNotice.Rejected | KeyStatusNotice.Decommissioned) =>
                    Left("Illegal request: cannot request compromise rotation on a terminal key.")

        /** Initiates an emergency retiring of a key version in response to a key compromise,
          * immediately retiring the current Active version without a draining period and without
          * creating a new Active version. This is considered a terminal process and prepares the key
          * for decommissioning. Requires exceptionStatus to be Compromised - the flag must be set
          * explicitly before this method can be invoked.
          */
        def compromiseRetire: Either[String, ManagedKey] =
            k.exceptionStatus match
                case None =>
                    Left("Illegal request: compromised flag must be set before requesting compromise retiring.")
                case Some(KeyStatusNotice.Compromised) =>
                    k.versions.compromiseRetire(Instant.now()).map{ updatedVersions =>
                        k.copy(versions=updatedVersions)
                    }
                case Some(KeyStatusNotice.PendingApproval) =>
                    Left("Illegal request: compromise retire cannot be completed, key must be first provisioned.")
                case Some(KeyStatusNotice.Rejected | KeyStatusNotice.Decommissioned) =>
                    Left("Illegal request: cannot request compromise retire on a terminal key.")

        /** Retires a specific key version by its internal UUID, completing its drain cycle
          * by transitioning it from Inactive to Retired. Requires no active exception status —
          * compromised or pending keys cannot have versions retired through this path.
          */
        def retireVersion(versionId: UUID): Either[String, ManagedKey] =
            k.exceptionStatus match
                case None =>
                    k.versions.retireVersion(versionId, Instant.now()).map{ updatedVersions =>
                        k.copy(versions=updatedVersions)
                    }
                case Some(KeyStatusNotice.PendingApproval) =>
                    Left("Illegal request: version retire cannot be completed, key must be first provisioned.")
                case Some(KeyStatusNotice.Compromised) =>
                    Left("Illegal request: version retire request cannot be completed for a compromised key.")
                case Some(KeyStatusNotice.Rejected | KeyStatusNotice.Decommissioned) =>
                    Left("Illegal request: cannot request version retiring on a terminal key.")

        /** Convenience overload of retireVersion that accepts a human-readable version number
          * rather than an internal UUID. Resolves the version number to its corresponding UUID
          * and delegates to the UUID-based implementation — no retirement logic is duplicated here.
          */
        def retireVersion(versionNumber: Int): Either[String, ManagedKey] =
            k.exceptionStatus match
                case None =>
                    k.versions.retireVersion(versionNumber, Instant.now()).map { updatedVersions =>
                        k.copy(versions = updatedVersions)
                    }
                case Some(KeyStatusNotice.PendingApproval) =>
                    Left("Illegal request: version retire cannot be completed, key must be first provisioned.")
                case Some(KeyStatusNotice.Compromised) =>
                    Left("Illegal request: version retire request cannot be completed for a compromised key.")
                case Some(KeyStatusNotice.Rejected | KeyStatusNotice.Decommissioned) =>
                    Left("Illegal request: cannot request version retiring on a terminal key.")

        /** This method allows for the continuation of a rotation to occur on a version that has been
          * successfully wound down and a new Active key version will be created. Requires no active
          * exception status.
          */
        def resumeWithNewVersion(rationale: KeyVersionRationale, expirationDate: Option[Instant]): Either[String, ManagedKey] =
            k.exceptionStatus match
                case None =>
                    k.versions.resumeWithNewVersion(rationale, expirationDate).map{ updatedVersions =>
                        k.copy(versions=updatedVersions)
                    }
                case Some(KeyStatusNotice.PendingApproval) =>
                    Left("Illegal request: cannot complete request; key must be provisioned.")
                case Some(KeyStatusNotice.Compromised) =>
                    Left("Illegal request: resume with new version cannot be completed on a compromised key.")
                case Some(KeyStatusNotice.Rejected | KeyStatusNotice.Decommissioned) =>
                    Left("Illegal request: cannot complete request; new version cannot be requested on a terminal key.")

        /** Sets the key-level exception flag to Compromised, indicating that the current
          * key material has been compromised and a compromise rotation should be initiated.
          * Only valid when no exception status is currently set — keys already in an
          * exceptional state cannot be marked compromised.
          */
        def markCompromised: Either[String, ManagedKey] =
            KeyStatusNoticeTransition
                .transition(k.exceptionStatus, Some(KeyStatusNotice.Compromised))
                .map { statusNotice =>
                    k.copy(exceptionStatus = statusNotice)
                }

        /** Sets the key-level exception flag to Rejected, indicating that the key creation
          * request was denied. Only valid from PendingApproval — this is a terminal state
          * and the key creation request must be resubmitted entirely.
          */
        def markRejected: Either[String, ManagedKey] =
            k.versions.entries.headOption match
                case None =>
                    KeyStatusNoticeTransition
                        .transition(k.exceptionStatus, Some(KeyStatusNotice.Rejected))
                        .map{ statusNotice =>
                            k.copy(exceptionStatus=statusNotice)
                        }
                case Some(_) =>
                    Left("Illegal request: cannot mark key rejected once a key has been provisioned.")

        /** Removes the key-level exception flag when a valid Active version exists.
          * This is called after a new version has been provisioned and the key is
          * ready to return to normal operation. Requires an Active version to be
          * present — keys with no version or a non-Active head version cannot have
          * their exception cleared.
          *
          * Note: intentionally mirrors RootKeyShare.clearException — divergence
          * expected once RootKeyShare approval ceremony is defined (see W-2, issue #8).
          */
        def clearException: Either[String, ManagedKey] =
            k.versions.entries.headOption match
                case None =>
                    Left("Illegal request: cannot remove exception until a new active version is provisioned.")
                case Some(version) if version.status == KeyVersionStatus.Active =>
                    KeyStatusNoticeTransition.transition(k.exceptionStatus, None).map{ keyStatus =>
                        k.copy(exceptionStatus=keyStatus)
                    }
                case Some(version) =>
                    Left("Illegal request: cannot remove exception without an Active version.")

        /** Permanently decommissions a key by setting its exception status to
          * Decommissioned. Requires the current version to be Retired and all
          * dependent keys to be resolved (see removeDependency) — keys with
          * Active or Inactive versions, or unresolved dependencies, cannot be
          * decommissioned. Reachable from exceptionStatus None (standard
          * lifecycle drain) or Some(Compromised) (compromise-driven retirement).
          *
          * Note: PendingApproval and Rejected are not handled in the inner match —
          * both states imply no version material exists, so they can never reach
          * this branch given the outer Retired-version guard. Exhaustivity checking
          * ensures any future KeyStatusNotice addition surfaces as a compiler error
          * here rather than falling through silently.
          *
          * Note: intentionally mirrors RootKeyShare.decommission — divergence
          * expected once RootKeyShare approval ceremony is defined (see W-2, issue #8).
          */
        def decommission: Either[String, ManagedKey] =
            if k.dependencies.nonEmpty then
                Left(s"Illegal request: cannot decommission a key with ${k.dependencies.size} unresolved dependent key(s).")
            else
                k.versions.entries.headOption match
                    case None =>
                        Left("Illegal request: cannot decommission a key with no version record.")
                    case Some(version) if version.status == KeyVersionStatus.Retired =>
                        k.exceptionStatus match
                            case None =>
                                Right(k.copy(exceptionStatus = Some(KeyStatusNotice.Decommissioned)))
                            case Some(KeyStatusNotice.Compromised) =>
                                KeyStatusNoticeTransition.transition(k.exceptionStatus, Some(KeyStatusNotice.Decommissioned)).map { keyStatus =>
                                    k.copy(exceptionStatus = keyStatus)
                                }
                            case Some(KeyStatusNotice.Decommissioned) =>
                                Left("Illegal request: key is already decommissioned.")
                            case Some(other) =>
                                Left(s"Illegal request: cannot decommission a key with exception status $other.")
                    case Some(version) =>
                        Left("Illegal request: cannot decommission a key with an Active or Inactive version.")

/** Representative of each of the two halves of the RootKeyShare with the following
  * fields:
  * - keyId: KeyId - the unique identifier of the respective key
  * - keyType: KeyType - specifically for RootKeyShare(s) is defaulted
  * to KeyType.RootKeyShare
  * - exceptionStatus: Option[KeyStatusNotice] - key-level status indicators
  * - versions: Versions - all versions of a key with an audit record
  * - createdDate: Instant - the date the key share was created
  *
  * Note: the following fields were omitted from RootKeyShare(s) and their justification:
  * - keyName - removed as keyShare location dictates which share is being accessed
  * - permissions - removed as there is no per-service access allowed; KMS will be the only
  * accessor
  * - dependencies - as the root of trust for KMS, the dependencies set would document every key
  * created, thus losing its semantic meaning
  * - owner - the owning service of RootKeyShare(s) (both A and B) are KMS
  * - algorithm - the algorithm loses semantic meaning as RootKeyShare is the root of trust of the system
  * - description - the purpose of the RootKeyShare is in what it is, losing its semantic meaning
  *
  * Note: lifecycleWindDown, compromiseRetire, retireVersion, decommission, and resumeWithNewVersion are
  * intentionally absent from RootKeyShare. As a Tier-0 platform object, root key material must
  * always be replaced atomically — wind-down without replacement would leave the platform's
  * root of trust in an unrecoverable degraded state. All rotations use the full
  * atomic lifecycle and compromiseRotation methods exclusively. See W-2, issue #8.
  */
case class RootKeyShare private(
                               keyId: KeyId,
                               keyType: KeyType,
                               exceptionStatus: Option[KeyStatusNotice],
                               versions: Versions,
                               createdAt: Instant) extends Key

object RootKeyShare:
    /** The validity period, in days, applied to every RootKeyShare version's
      * expirationDate. Centralized here so provision, lifecycleRotation, and
      * compromiseRotation always compute the same window from a single source
      * of truth, rather than repeating the literal at each call site.
      */
    private val RootKeyShareValidityDays = 1826L

    /** Creates a RootKeyShare derived from Key. The smart constructor requires
      * no parameters and the platform generates all required fields
      */
    def create(): RootKeyShare =

        RootKeyShare(
            keyId=UUID.randomUUID(),
            keyType=KeyType.RootKeyShare,
            exceptionStatus=Some(KeyStatusNotice.PendingApproval),
            versions=Versions.create(),
            createdAt=Instant.now()
        )

    extension (k: RootKeyShare)
        /** Removes the key-level exception flag when a valid Active version exists.
          * Behaves identically to ManagedKey.clearException for now — divergence
          * expected once RootKeyShare multi-party approval ceremony is defined (see W-2, issue #8).
          */
        def clearException: Either[String, RootKeyShare] =
            k.versions.entries.headOption match
                case None =>
                    Left("Illegal request: cannot remove exception until a new active version is provisioned.")
                case Some(version) if version.status == KeyVersionStatus.Active =>
                    KeyStatusNoticeTransition.transition(k.exceptionStatus, None).map { keyStatus =>
                        k.copy(exceptionStatus = keyStatus)
                    }
                case Some(version) =>
                    Left("Illegal request: cannot remove exception without an Active version.")

        /** Provisions the first version of key material for this key, transitioning it
          * from PendingApproval to active operation. Must be called after the engine has
          * generated the actual key bytes. Requires exceptionStatus to be PendingApproval —
          * keys in any other state cannot be provisioned.
          *
          * Note: intentionally mirrors ManagedKey.provision — divergence expected
          * once RootKeyShare multi-party approval ceremony is defined (see W-2, issue #8).
          */
        def provision: Either[String, RootKeyShare] =
            k.exceptionStatus match
                case None =>
                    Left("Illegal request: cannot request provisioning key with missing PendingApproval flag.")
                case Some(KeyStatusNotice.PendingApproval) =>
                    val now = Instant.now()
                    k.versions
                        .provision(now, Some(now.plus(RootKeyShareValidityDays, ChronoUnit.DAYS)))
                        .map { updatedVersions =>
                            k.copy(versions = updatedVersions)
                        }
                case Some(KeyStatusNotice.Compromised) =>
                    Left("Illegal request: cannot request provisioning on a compromised key.")
                case Some(KeyStatusNotice.Rejected | KeyStatusNotice.Decommissioned) =>
                    Left("Illegal request: cannot request provisioning on a terminal key.")

        /** Initiates a scheduled lifecycle rotation, placing the current Active version
          * into an Inactive state for dependency draining and creating a new Active version.
          * Requires no active exception status — compromised or pending keys cannot be
          * rotated through the standard lifecycle path.
          *
          * Note: intentionally mirrors ManagedKey.lifecycleRotation — divergence expected
          * once RootKeyShare multi-party approval ceremony is defined (see W-2, issue #8).
          */
        def lifecycleRotation: Either[String, RootKeyShare] =
            k.exceptionStatus match
                case None =>
                    val now = Instant.now()
                    k.versions
                        .lifecycleRotation(now, Some(now.plus(RootKeyShareValidityDays, ChronoUnit.DAYS)))
                        .map { updatedVersions =>
                            k.copy(versions = updatedVersions)
                        }
                case Some(KeyStatusNotice.PendingApproval) =>
                    Left("Illegal request: lifecycle rotation cannot be completed, key must be first provisioned.")
                case Some(KeyStatusNotice.Compromised) =>
                    Left("Illegal request: cannot request lifecycle rotation on a compromised key.")
                case Some(KeyStatusNotice.Rejected | KeyStatusNotice.Decommissioned) =>
                    Left("Illegal request: cannot request lifecycle rotation on a terminal key.")

        /** Initiates an emergency rotation in response to a key compromise, immediately
          * retiring the current Active version without a draining period and creating a
          * new Active version. Requires exceptionStatus to be Compromised — the flag must
          * be set explicitly before this rotation can be triggered.
          *
          * Note: intentionally mirrors ManagedKey.compromiseRotation — divergence expected
          * once RootKeyShare multi-party approval ceremony is defined (see W-2, issue #8).
          */
        def compromiseRotation: Either[String, RootKeyShare] =
            k.exceptionStatus match
                case None =>
                    Left("Illegal request: compromised flag must be set before requesting compromise rotation.")
                case Some(KeyStatusNotice.Compromised) =>
                    val now = Instant.now()
                    k.versions
                        .compromiseRotation(now, Some(now.plus(RootKeyShareValidityDays, ChronoUnit.DAYS)))
                        .map { updatedVersions =>
                            k.copy(versions = updatedVersions)
                        }
                case Some(KeyStatusNotice.PendingApproval) =>
                    Left("Illegal request: compromise rotation cannot be completed, key must be first provisioned.")
                case Some(KeyStatusNotice.Rejected | KeyStatusNotice.Decommissioned) =>
                    Left("Illegal request: cannot request compromise rotation on a terminal key.")

        /** Sets the key-level exception flag to Compromised, indicating that the current
         * key material has been compromised and a compromise rotation should be initiated.
         * Only valid when no exception status is currently set — keys already in an
         * exceptional state cannot be marked compromised.
         *
         * Note: intentionally mirrors ManagedKey.markCompromised — divergence expected
         * once RootKeyShare multi-party approval ceremony is defined (see W-2, issue #8).
         */
        def markCompromised: Either[String, RootKeyShare] =
            KeyStatusNoticeTransition
                .transition(k.exceptionStatus, Some(KeyStatusNotice.Compromised))
                .map { statusNotice =>
                    k.copy(exceptionStatus = statusNotice)
                }
