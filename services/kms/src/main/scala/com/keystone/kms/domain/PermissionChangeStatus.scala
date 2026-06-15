package com.keystone.kms.domain

/** Representative of the status of each permission change. The permission change status
  * can have either one of two values:
  * - Approved - the permission change request was authorized and the requested permission 
  *   was applied to the key
  * - Rejected - the permission change request was denied and no modifications were made 
  *   to the key's permissions  
  */
sealed trait PermissionChangeStatus

object PermissionChangeStatus:
    case object Approved extends PermissionChangeStatus
    
    case object Rejected extends PermissionChangeStatus