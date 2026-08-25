package com.keystone.kms.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class KeyOperationSpec extends AnyFlatSpec with Matchers:
    "KeyOperation" should "cover all operations exhaustively" in {
        def describeOperation(op: KeyOperation): String = op match
            case KeyOperation.Encrypt => "encrypt"
            case KeyOperation.Decrypt => "decrypt"
            case KeyOperation.Wrap => "wrap"
            case KeyOperation.Unwrap => "unwrap"
            case KeyOperation.Sign => "sign"
            case KeyOperation.Verify => "verify"

        describeOperation(KeyOperation.Encrypt) shouldBe "encrypt"
        describeOperation(KeyOperation.Decrypt) shouldBe "decrypt"
        describeOperation(KeyOperation.Wrap) shouldBe "wrap"
        describeOperation(KeyOperation.Unwrap) shouldBe "unwrap"
        describeOperation(KeyOperation.Sign) shouldBe "sign"
        describeOperation(KeyOperation.Verify) shouldBe "verify"
    }
