package com.keystone.kms.domain

import com.keystone.kms.domain.KeyState.Rejected
import com.keystone.kms.domain.KeyState.Compromised

/** Governs the permitted state transitions for KMS keys throughout their lifecycle.
  * 
  * Any transition attempts that violate the defined lifecycle return a Left describing
  * the violation. Valid transitions return a Right with the target state.
  * 
  * This enforces the constraint that no direct transitions from Active to Inactive are
  * allowed at the type level.
  */
object KeyStateTransition:
    def transition(current: KeyState, target: KeyState): Either[String, KeyState] =
        (current, target) match
            case (KeyState.PendingApproval, KeyState.Active) => Right(KeyState.Active)
            case (KeyState.PendingApproval, KeyState.Rejected) => Right(KeyState.Rejected)
            case (KeyState.Active, KeyState.Rotating) => Right(KeyState.Rotating)
            case (KeyState.Active, KeyState.Compromised) => Right(KeyState.Compromised)
            case (KeyState.Rotating, KeyState.Inactive) => Right(KeyState.Inactive)
            case (KeyState.Compromised, KeyState.Inactive) => Right(KeyState.Inactive)
            case (KeyState.Active, KeyState.Inactive) => 
                Left("Illegal transition: Active -> Inactive is not permitted. " +
                     "Key must pass through Rotating to ensure dependencies are resolved.")
            case (from, to) =>
                Left(s"Illegal transition: ${from} -> ${to} is not a permitted lifecycle transition.")