package com.keystone.kms.domain

/** Representative of all states in the KMS key lifecycle. 
 * 
 * State transitions are strictly governed. Not all transitions 
 * are permitted. The compiler enforces exhaustive pattern matching
 * across all states. 
 * 
 * Permitted transitions:
 *     PendingApproval -> Active
 *     PendingApproval -> Rejected
 *     Active -> Rotating
 *     Active -> Compromised
 *     Rotating -> Inactive
 *     Compromised -> Inactive
 * 
 * Constraint: No direct transition is allowed from Active to Inactive.
 * A key must first enter the Rotating state to ensure all dependencies are
 * resolved before leaving the service.   
*/

sealed trait KeyState

object KeyState:
    /** In this state, key metadata exists, but awaiting administrator approval.
      * No key material has been generated, and the key is not currently usable.
      */
    case object PendingApproval extends KeyState

    /** In this state, key provisioning has been rejected. This is a terminal state, and they key
      * material will never be generated. The record will be retained for audit purposes.
      */
    case object Rejected extends KeyState

    /** In this state, the key material has been generated, and the key is able to be used for 
      * cryptographic operations. The majority of the key's lifetime is spent in this state. 
      */
    case object Active extends KeyState

    /** In this state, a new key version is Active. This version will remain in service
      * while dependencies are updated to the new version and DEK rewrapping is complete.
      * This is a transitional state and therefore not stable 
    */
    case object Rotating extends KeyState

    /** In this state, a key has been flagged as potentially or confirmed to be compromised. 
      * Immediate rotation is triggered, and the key is removed from service.
      * The record is retained for audit purposes.
      */
    case object Compromised extends KeyState

    /** In this state, the key is no longer operational. All dependencies have been updated
      * and the key material is retained for audit and recovery purposes. This is a terminal
      * operational state.
      */  
    case object Inactive extends KeyState