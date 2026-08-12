package com.keystone.kms.domain

/** Representative of the lifecycle relevance of an individual key version,
  * independent of the key's own status. This status exists separate from
  * key-level statuses because a key's material may be rotated without the 
  * key's identity, name, or governance state changing - version status helps
  * provide clarity to whether this specific set of key material is in use; 
  * which is separate from whether the key is approved, compromised, or retired.
  */
sealed trait KeyVersionStatus

object KeyVersionStatus:
    /** As Active, a key version is available for use. This status is
      * representative of where a version will spend most of its lifecycle.
      */
    case object Active extends KeyVersionStatus

    /** As Inactive, a new version has been created and any current dependencies
      * of this version are being actively drained. Once all dependencies have
      * been resolved, the status will be updated accordingly
      */
    case object Inactive extends KeyVersionStatus

    /** As Retired, all dependencies of this version have been drained, either
      * through a traditional lifecycle approach or an accelerated approach in the
      * event of compromise; this version is no longer in use.
      */
    case object Retired extends KeyVersionStatus
