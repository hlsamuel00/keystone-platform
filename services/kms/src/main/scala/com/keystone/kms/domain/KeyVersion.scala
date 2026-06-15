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
  * - keyState: KeyState - the current state of the key throughout its lifecycle
  */
case class KeyVersion private (
                              versionId: UUID,
                              versionNumber: Int,
                              createdAt: Instant,
                              keyState: KeyState)

object KeyVersion:
    def createVersion(
                       previousVersion: Option[KeyVersion],
                       createdAt: Instant,
                       keyState: KeyState): KeyVersion =
        val nextVersionNumber = previousVersion.map(_.versionNumber + 1).getOrElse(1)
        KeyVersion(UUID.randomUUID(), nextVersionNumber, createdAt, keyState)

    extension (v: KeyVersion)
        def withKeyState(state: KeyState): KeyVersion =
            v.copy(keyState=state)