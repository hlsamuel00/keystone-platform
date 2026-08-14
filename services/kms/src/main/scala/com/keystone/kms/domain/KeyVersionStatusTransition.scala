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
