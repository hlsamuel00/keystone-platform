package com.keystone.kms.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class KeyStateTransitionSpec extends AnyFlatSpec with Matchers:
    // ----- Permitted Transitions ------------------------------------------------------

    "KeyStateTransition" should
        "permit PendingApproval -> Active" in {
            KeyStateTransition.transition(
                KeyState.PendingApproval,
                KeyState.Active
            ) shouldBe Right(KeyState.Active)
        }
    
    it should "permit PendingApproval -> Rejected" in {
        KeyStateTransition.transition(
            KeyState.PendingApproval,
            KeyState.Rejected
        ) shouldBe Right(KeyState.Rejected)
    }

    it should "permit Active -> Rotating" in {
        KeyStateTransition.transition(
            KeyState.Active,
            KeyState.Rotating
        ) shouldBe Right(KeyState.Rotating)
    }

    it should "permit Active -> Compromised" in {
        KeyStateTransition.transition(
            KeyState.Active,
            KeyState.Compromised
        ) shouldBe Right(KeyState.Compromised)
    }

    it should "permit Rotating -> Inactive" in {
        KeyStateTransition.transition(
            KeyState.Rotating,
            KeyState.Inactive
        ) shouldBe Right(KeyState.Inactive)
    }

    it should "permit Compromised -> Inactive" in {
        KeyStateTransition.transition(
            KeyState.Compromised,
            KeyState.Inactive
        ) shouldBe Right(KeyState.Inactive)
    }

    // ----- Illegal Transitions ------------------------------------------------------

    it should "reject Active -> Inactive" in {
        KeyStateTransition.transition(
            KeyState.Active,
            KeyState.Inactive
        ) shouldBe Left("Illegal transition: Active -> Inactive is not permitted. " +
                         "Key must pass through Rotating to ensure dependencies are resolved.")
    }

    it should "reject Rejected -> Active" in {
        KeyStateTransition.transition(
            KeyState.Rejected,
            KeyState.Active
        ) shouldBe Left("Illegal transition: Rejected -> Active is not a permitted lifecycle transition.")
    }

    it should "reject Inactive -> Active" in {
        KeyStateTransition.transition(
            KeyState.Inactive,
            KeyState.Active
        ) shouldBe Left("Illegal transition: Inactive -> Active is not a permitted lifecycle transition.")
    }

    it should "reject Rotating -> Active" in {
        KeyStateTransition.transition(
            KeyState.Rotating,
            KeyState.Active
        ) shouldBe Left("Illegal transition: Rotating -> Active is not a permitted lifecycle transition.")
    }

    it should "reject PendingApproval -> Inactive" in {
        KeyStateTransition.transition(
            KeyState.PendingApproval,
            KeyState.Inactive
        ) shouldBe Left("Illegal transition: PendingApproval -> Inactive is not a permitted lifecycle transition.")
    }

    it should "reject Compromised -> Active" in {
        KeyStateTransition.transition(
            KeyState.Compromised,
            KeyState.Active
        ) shouldBe Left("Illegal transition: Compromised -> Active is not a permitted lifecycle transition.")
    }

