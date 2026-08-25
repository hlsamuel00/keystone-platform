package com.keystone.kms.domain

/** Governs the permitted status transitions for key versions throughout their
  * lifecycle.
  *
  * Any transition attempts that violate the defined lifecycle return a Left describing
  * the violation. Valid transitions return a Right with the target status.
  */
object KeyVersionStatusTransition:
    def transition(
                    current: KeyVersionStatus,
                    target: KeyVersionStatus): Either[String, KeyVersionStatus] =
        (current, target) match
            case (KeyVersionStatus.Active, KeyVersionStatus.Inactive) =>
                Right(KeyVersionStatus.Inactive)
            // Note: Active -> Retired is intentionally permitted here as a low-level primitive,
            // used by both compromiseRotation and compromiseRetire in Versions. This transition
            // is deliberately NOT gated by compromise authorization at this layer.
            //
            // KeyVersionStatusTransition only ever operates on (KeyVersionStatus, KeyVersionStatus) —
            // it has no access to exceptionStatus, which lives on Key, not Versions. Pushing this
            // gate down here would mean either trusting an unverifiable caller-supplied claim
            // (e.g. a boolean flag), or giving this primitive awareness of key-level governance
            // state it was deliberately designed not to have.
            //
            // The actual enforcement lives in ManagedKey, which holds both exceptionStatus and
            // versions simultaneously and can verify compromise status against ground truth
            // before ever calling this transition. This is not a gap to be closed later — it is
            // the correct layer for this check, consistent with every other cross-field
            // invariant enforced in this domain model (see clearException, decommission).
            //
            // See security review, PR #5, W-1.
            case (KeyVersionStatus.Active, KeyVersionStatus.Retired) =>
                Right(KeyVersionStatus.Retired)
            case (KeyVersionStatus.Inactive, KeyVersionStatus.Retired) =>
                Right(KeyVersionStatus.Retired)
            case (KeyVersionStatus.Inactive, KeyVersionStatus.Active) =>
                Left("Illegal transition: Reactivation of key material is not possible.")
            case (KeyVersionStatus.Retired, KeyVersionStatus.Active) =>
                Left("Illegal transition: Once a version is terminal, reactivation is not possible.")
            case (KeyVersionStatus.Retired, KeyVersionStatus.Inactive) =>
                Left("Illegal transition: Once a version is terminal, reactivation is not possible.")
            case (from, to) =>
                Left(s"Illegal transition: $from -> $to is not a permitted lifecycle transition.")
