package com.keystone.kms.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class KeySizeSpec extends AnyFlatSpec with Matchers:
    "KeySize" should "cover all key sizes exhaustively" in {
        def describeKeySize(ks: KeySize): String = ks match
            case KeySize.AES256GCM => "key size is 256 bits for AES-GCM"
            case KeySize.Ed25519Fixed => "key size is fixed at 256 bits"
            case KeySize.ECP256 => "key size is 256 bits for Elliptic Curve"

        describeKeySize(KeySize.AES256GCM) shouldBe "key size is 256 bits for AES-GCM"
        describeKeySize(KeySize.Ed25519Fixed) shouldBe "key size is fixed at 256 bits"
        describeKeySize(KeySize.ECP256) shouldBe "key size is 256 bits for Elliptic Curve" 
    }