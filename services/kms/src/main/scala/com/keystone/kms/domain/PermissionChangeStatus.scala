package com.keystone.kms.domain

/** Representative of the status of each permission change. The permission change status
  * can have either one of two values:
  * - Granted - the permission change request was processed and the requested permission
  *   was applied to the key
  * - Revoked - the permission change request was processed and the requested permission
  *   was removed from the key
  */
sealed trait PermissionChangeStatus

object PermissionChangeStatus:
    case object Granted extends PermissionChangeStatus
    
    case object Revoked extends PermissionChangeStatus