package com.keystone.kms.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class KeyVersionStatusSpec extends AnyFlatSpec with Matchers:
    "KeyVersionStatus" should "cover all version status exhaustively" in {
        def defineKeyVersionStatus(s: KeyVersionStatus): String = s match
            case KeyVersionStatus.Active => "This version is active."
            case KeyVersionStatus.Inactive => "This version is draining dependencies."
            case KeyVersionStatus.Retired => "All dependencies have been drained."

        defineKeyVersionStatus(KeyVersionStatus.Active) shouldBe "This version is active."
        defineKeyVersionStatus(KeyVersionStatus.Inactive) shouldBe "This version is draining dependencies."
        defineKeyVersionStatus(KeyVersionStatus.Retired) shouldBe "All dependencies have been drained."
    }