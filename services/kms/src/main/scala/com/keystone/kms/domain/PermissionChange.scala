package com.keystone.kms.domain

import java.time.Instant

/** Representative of the Permission Change record kept for auditing purposes.
  * The record will capture the following details:
  * - serviceName: String - the name/ID of the service the change is being made for
  * - keyName: String - the name/ID of the key the requested change is being made on
  * - operation: KeyOperation - the requested permission to be added (ex. Encrypt, Decrypt, etc.)
  * - status: PermissionChangeStatus - the outcome of the request
  * - changedAt: Instant - the timestamp the request was processed
  */
case class PermissionChange(
                           serviceName: String,
                           keyName: String,
                           operation: KeyOperation,
                           status: PermissionChangeStatus,
                           changedAt: Instant)