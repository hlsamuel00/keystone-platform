package com.keystone.kms.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class KeyAlgorithmSpec extends AnyFlatSpec with Matchers:
    "KeyAlgorithm" should "cover all key algorithms exhaustively" in {
        def describeKeyAlgorithm(ka: KeyAlgorithm): String = ka match
            case KeyAlgorithm.AES256GCM => s"Key Algorithm is AES-GCM with a key size of ${KeyAlgorithm.AES256GCM.keySizeBits}"
            case KeyAlgorithm.Ed25519Fixed => s"Key Algorithm is EdDSA using Curve25519 with a key size of ${KeyAlgorithm.Ed25519Fixed.keySizeBits}"
            case KeyAlgorithm.ECP256 => s"Key Algorithm is Elliptic Curve with a key size of ${KeyAlgorithm.ECP256.keySizeBits}"

        describeKeyAlgorithm(KeyAlgorithm.AES256GCM) shouldBe s"Key Algorithm is AES-GCM with a key size of 256"
        describeKeyAlgorithm(KeyAlgorithm.Ed25519Fixed) shouldBe s"Key Algorithm is EdDSA using Curve25519 with a key size of 256"
        describeKeyAlgorithm(KeyAlgorithm.ECP256) shouldBe s"Key Algorithm is Elliptic Curve with a key size of 256" 
    }