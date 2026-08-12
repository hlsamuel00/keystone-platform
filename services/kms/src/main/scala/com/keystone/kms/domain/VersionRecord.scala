package com.keystone.kms.domain

import java.time.Instant
import java.util.UUID

/** Representative of the Version Change record kept for auditing purposes.
  * This record helps keep a full audit of any changes made to a key version,
  * kept independently of the list of versions available for a key, each with
  * the status of the version. The record will capture the following details:
  * - versionId: UUID - the versionID impacted by this recorded change
  * - status: KeyVersionStatus - the status assigned to the version at the time
  * this record was generated
  * - rationale: KeyVersionRationale - the reason for the change to the version
  * - changedAt: Instant - the timestamp for the recorded change
  */
case class VersionRecord(
                        versionId: UUID,
                        status: KeyVersionStatus,
                        rationale: KeyVersionRationale,
                        changedAt: Instant
                        )

