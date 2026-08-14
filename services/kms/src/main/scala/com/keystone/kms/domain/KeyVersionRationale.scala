package com.keystone.kms.domain

/** Representative of the reasons that the key version has been updated.
  * This provides additional clarity to the version record outlining that the version
  * was changed and why. The options provided are representative of what the platform
  * recognizes currently as reasons the version has changed and is not all-inclusive.
  */
sealed trait KeyVersionRationale

object KeyVersionRationale:
    /** Representative of the initial migration from PendingApproval KeyStatus to
      * a fully usable key. This usually occurs when the engine runs the request to
      * generate the key bytes for a key.
      */
    case object InitialProvisioning extends KeyVersionRationale

    /** Representative of a routine based update where the lifecycle of a key requires
      * rotation.
      */
    case object LifecycleRotation extends KeyVersionRationale

    /** Representative of a rotation that occurred in response to a compromise of key
      * material.  
      */
    case object CompromiseRotation extends KeyVersionRationale

    // /** Representative of a manual rotation that falls outside a scheduled life
    //   * cycle rotation, without an element of compromise.
    //   */
    // case object ManualRotation extends KeyVersionRationale
