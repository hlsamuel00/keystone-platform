package com.keystone.kms.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class KeyStatusNoticeSpec extends AnyFlatSpec with Matchers:
    "KeyStatusNotice" should "cover all status notices exhaustively" in {
        def describeStatusNotice(n: KeyStatusNotice): String = n match
            case KeyStatusNotice.PendingApproval => "Key is currently pending approval."
            case KeyStatusNotice.Rejected => "Key was not created."
            case KeyStatusNotice.Compromised => "Key has been reported as compromised."
            case KeyStatusNotice.Decommissioned => "Key has been decommissioned."

        describeStatusNotice(KeyStatusNotice.PendingApproval) shouldBe "Key is currently pending approval."
        describeStatusNotice(KeyStatusNotice.Rejected) shouldBe "Key was not created."
        describeStatusNotice(KeyStatusNotice.Compromised) shouldBe "Key has been reported as compromised."
        describeStatusNotice(KeyStatusNotice.Decommissioned) shouldBe "Key has been decommissioned."
    }
