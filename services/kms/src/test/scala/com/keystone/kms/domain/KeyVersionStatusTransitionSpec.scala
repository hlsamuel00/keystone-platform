package com.keystone.kms.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class KeyVersionStatusTransitionSpec extends AnyFlatSpec with Matchers:
    "KeyVersionStatusTransition" should "ensure valid transitions are permitted" in {
        KeyVersionStatusTransition.transition(
            current=KeyVersionStatus.Active,
            target=KeyVersionStatus.Inactive
        ) shouldBe Right(KeyVersionStatus.Inactive)

        KeyVersionStatusTransition.transition(
            current=KeyVersionStatus.Inactive,
            target=KeyVersionStatus.Retired
        ) shouldBe Right(KeyVersionStatus.Retired)

        KeyVersionStatusTransition.transition(
            current=KeyVersionStatus.Active,
            target=KeyVersionStatus.Retired
        ) shouldBe Right(KeyVersionStatus.Retired)
    }

    it should "not allow invalid transitions" in {
        KeyVersionStatusTransition.transition(
            current=KeyVersionStatus.Inactive,
            target=KeyVersionStatus.Active
        ) shouldBe Left("Illegal transition: Reactivation of key material is not possible.")

        KeyVersionStatusTransition.transition(
            current=KeyVersionStatus.Retired,
            target=KeyVersionStatus.Active
        ) shouldBe Left("Illegal transition: Once a version is terminal, reactivation is not possible.")

        KeyVersionStatusTransition.transition(
            current=KeyVersionStatus.Retired,
            target=KeyVersionStatus.Inactive
        ) shouldBe Left("Illegal transition: Once a version is terminal, reactivation is not possible.")
    }

    it should "not allow self-transitions" in {
        KeyVersionStatusTransition.transition(
            current=KeyVersionStatus.Active,
            target=KeyVersionStatus.Active
        ) shouldBe Left("Illegal transition: Active -> Active is not a permitted lifecycle transition.")

        KeyVersionStatusTransition.transition(
            current=KeyVersionStatus.Inactive,
            target=KeyVersionStatus.Inactive
        ) shouldBe Left("Illegal transition: Inactive -> Inactive is not a permitted lifecycle transition.")

        KeyVersionStatusTransition.transition(
            current=KeyVersionStatus.Retired,
            target=KeyVersionStatus.Retired
        ) shouldBe Left("Illegal transition: Retired -> Retired is not a permitted lifecycle transition.")
    }