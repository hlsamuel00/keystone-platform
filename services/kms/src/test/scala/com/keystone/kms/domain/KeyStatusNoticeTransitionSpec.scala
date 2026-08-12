package com.keystone.kms.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class KeyStatusNoticeTransitionSpec extends AnyFlatSpec with Matchers:
    "KeyStatusNoticeTransition" should "ensure valid transitions are permitted" in {
        KeyStatusNoticeTransition.transition(
            current=Some(KeyStatusNotice.PendingApproval),
            target=None
        ) shouldBe Right(None)
        
        KeyStatusNoticeTransition.transition(
            current=Some(KeyStatusNotice.PendingApproval),
            target=Some(KeyStatusNotice.Rejected)
        ) shouldBe Right(Some(KeyStatusNotice.Rejected))
        
        KeyStatusNoticeTransition.transition(
            current=None,
            target=Some(KeyStatusNotice.Compromised)
        ) shouldBe Right(Some(KeyStatusNotice.Compromised))
        
        KeyStatusNoticeTransition.transition(
            current=Some(KeyStatusNotice.Compromised),
            target=None
        ) shouldBe Right(None)
        
        KeyStatusNoticeTransition.transition(
            current=Some(KeyStatusNotice.Compromised),
            target=Some(KeyStatusNotice.Decommissioned)
        ) shouldBe Right(Some(KeyStatusNotice.Decommissioned))
    }
    
    it should "not allow invalid transitions" in {
        KeyStatusNoticeTransition.transition(
            current=Some(KeyStatusNotice.PendingApproval),
            target=Some(KeyStatusNotice.Compromised)
        ) shouldBe Left("Illegal transition: PendingApproval -> Compromised is not a permitted lifecycle transition.")

        KeyStatusNoticeTransition.transition(
            current=Some(KeyStatusNotice.PendingApproval),
            target=Some(KeyStatusNotice.Decommissioned)
        ) shouldBe Left("Illegal transition: PendingApproval -> Decommissioned is not a permitted lifecycle transition.")

        KeyStatusNoticeTransition.transition(
            current=Some(KeyStatusNotice.Rejected),
            target=Some(KeyStatusNotice.Compromised)
        ) shouldBe Left("Illegal transition: Rejected -> Compromised is not a permitted lifecycle transition.")
        
        KeyStatusNoticeTransition.transition(
            current=Some(KeyStatusNotice.Rejected),
            target=Some(KeyStatusNotice.Decommissioned)
        ) shouldBe Left("Illegal transition: Rejected -> Decommissioned is not a permitted lifecycle transition.")

        KeyStatusNoticeTransition.transition(
            current=Some(KeyStatusNotice.Rejected),
            target=None
        ) shouldBe Left("Illegal transition: Rejected -> None is not a permitted lifecycle transition.")

        KeyStatusNoticeTransition.transition(
            current=Some(KeyStatusNotice.Rejected),
            target=Some(KeyStatusNotice.PendingApproval)
        ) shouldBe Left("Illegal transition: Rejected -> PendingApproval is not a permitted lifecycle transition.")

        KeyStatusNoticeTransition.transition(
            current=Some(KeyStatusNotice.Compromised),
            target=Some(KeyStatusNotice.PendingApproval)
        ) shouldBe Left("Illegal transition: Compromised -> PendingApproval is not a permitted lifecycle transition.")

        KeyStatusNoticeTransition.transition(
            current=Some(KeyStatusNotice.Compromised),
            target=Some(KeyStatusNotice.Rejected)
        ) shouldBe Left("Illegal transition: Compromised -> Rejected is not a permitted lifecycle transition.")

        KeyStatusNoticeTransition.transition(
            current=None,
            target=Some(KeyStatusNotice.Decommissioned)
        ) shouldBe Left("Illegal transition: None -> Decommissioned is not a permitted lifecycle transition.")

        KeyStatusNoticeTransition.transition(
            current=None,
            target=Some(KeyStatusNotice.PendingApproval)
        ) shouldBe Left("Illegal transition: None -> PendingApproval is not a permitted lifecycle transition.")

        KeyStatusNoticeTransition.transition(
            current=None,
            target=Some(KeyStatusNotice.Rejected)
        ) shouldBe Left("Illegal transition: None -> Rejected is not a permitted lifecycle transition.")

        KeyStatusNoticeTransition.transition(
            current=Some(KeyStatusNotice.Decommissioned),
            target=None
        ) shouldBe Left("Illegal transition: Decommissioned -> None is not a permitted lifecycle transition.")

        KeyStatusNoticeTransition.transition(
            current=Some(KeyStatusNotice.Decommissioned),
            target=Some(KeyStatusNotice.PendingApproval)
        ) shouldBe Left("Illegal transition: Decommissioned -> PendingApproval is not a permitted lifecycle transition.")

        KeyStatusNoticeTransition.transition(
            current=Some(KeyStatusNotice.Decommissioned),
            target=Some(KeyStatusNotice.Rejected)
        ) shouldBe Left("Illegal transition: Decommissioned -> Rejected is not a permitted lifecycle transition.")

        KeyStatusNoticeTransition.transition(
            current=Some(KeyStatusNotice.Decommissioned),
            target=Some(KeyStatusNotice.Compromised)
        ) shouldBe Left("Illegal transition: Decommissioned -> Compromised is not a permitted lifecycle transition.")
    }
    
    it should "not allow self-transitions" in {
        KeyStatusNoticeTransition.transition(
            current=Some(KeyStatusNotice.PendingApproval),
            target=Some(KeyStatusNotice.PendingApproval)
        ) shouldBe Left("Illegal transition: PendingApproval -> PendingApproval is not a permitted lifecycle transition.")

        KeyStatusNoticeTransition.transition(
            current=Some(KeyStatusNotice.Rejected),
            target=Some(KeyStatusNotice.Rejected)
        ) shouldBe Left("Illegal transition: Rejected -> Rejected is not a permitted lifecycle transition.")

        KeyStatusNoticeTransition.transition(
            current=Some(KeyStatusNotice.Compromised),
            target=Some(KeyStatusNotice.Compromised)
        ) shouldBe Left("Illegal transition: Compromised -> Compromised is not a permitted lifecycle transition.")

        KeyStatusNoticeTransition.transition(
            current=Some(KeyStatusNotice.Decommissioned),
            target=Some(KeyStatusNotice.Decommissioned)
        ) shouldBe Left("Illegal transition: Decommissioned -> Decommissioned is not a permitted lifecycle transition.")

        KeyStatusNoticeTransition.transition(
            current=None,
            target=None
        ) shouldBe Left("Illegal transition: None -> None is not a permitted lifecycle transition.")
    }
