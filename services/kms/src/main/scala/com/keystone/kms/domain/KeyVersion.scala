package com.keystone.kms.domain

import java.time.Instant
import java.util.UUID

/** Representative of the versions of a key supported in KMS. A new version is created 
  * when a key is first provisioned and on each subsequent rotation. The version record 
  * is retained for audit purposes, even if the key version is no longer in use. The data of a
  * key version is as follows:
  * - versionId: UUID - the unique identifier referenced internally for the key version
  * - versionNumber: Int - a sequential numerical value for the version number intended
  *   for human readability
  * - createdAt: Instant - a timestamp of when the version was created
  * - status: KeyVersionStatus - the current status of the key version throughout its lifecycle
  */
case class KeyVersion private (
                              versionId: UUID,
                              versionNumber: Int,
                              createdAt: Instant,
                              status: KeyVersionStatus,
                              expirationDate: Option[Instant])

object KeyVersion:
    def createVersion(
                       previousVersion: Option[KeyVersion],
                       createdAt: Instant,
                       status: KeyVersionStatus,
                       expirationDate: Option[Instant]): KeyVersion =

        val nextVersionNumber = previousVersion.map(_.versionNumber + 1).getOrElse(1)
        KeyVersion(
            versionId=UUID.randomUUID(),
            versionNumber=nextVersionNumber,
            createdAt=createdAt,
            status=status,
            expirationDate=expirationDate)

    extension (v: KeyVersion)
        def withStatus(target: KeyVersionStatus): Either[String, KeyVersion] =
            KeyVersionStatusTransition.transition(v.status, target)
              .map(newStatus => v.copy(status=newStatus))