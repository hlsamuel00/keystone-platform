package com.keystone.kms.domain

/** Governs the permitted notice transitions a Key can have throughout
  * its lifecycle.
  *
  * Any transition attempts that violate the defined lifecycle return a Left describing
  * the violation. Valid transitions return a Right with the target notice.
  */
object KeyStatusNoticeTransition:
    private def describe(notice: Option[KeyStatusNotice]): String =
        notice.map(_.toString).getOrElse("None")

    def transition(
                    current: Option[KeyStatusNotice],
                    target: Option[KeyStatusNotice]
                  ): Either[String, Option[KeyStatusNotice]] =
        (current, target) match
            case (Some(KeyStatusNotice.PendingApproval), None) =>
                Right(None)
            case (Some(KeyStatusNotice.PendingApproval), Some(KeyStatusNotice.Rejected)) =>
                Right(Some(KeyStatusNotice.Rejected))
            case (None, Some(KeyStatusNotice.Compromised)) =>
                Right(Some(KeyStatusNotice.Compromised))
            case (Some(KeyStatusNotice.Compromised), None) =>
                Right(None)
            case (Some(KeyStatusNotice.Compromised), Some(KeyStatusNotice.Decommissioned)) =>
                Right(Some(KeyStatusNotice.Decommissioned))
            case (from, to) =>
                Left(s"Illegal transition: ${describe(from)} -> ${describe(to)} is not a permitted lifecycle transition.")