package com.keystone.kms.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class KeyTypeSpec extends AnyFlatSpec with Matchers:
    "KeyType" should "cover all key types exhaustively" in {
        def describeKeyType(kt: KeyType): String = kt match
            case KeyType.KEK => "key encryption key"
            case KeyType.DEK => "data encryption key"
            case KeyType.TransmissionKey => "transmission key"
            case KeyType.SigningKey => "signing key"
            case KeyType.CertificateAuthorityKey => "certificate authority key"
            case KeyType.ServiceIdentityKey => "service identity key"
            case KeyType.RootKeyShare => "root key share"

        describeKeyType(KeyType.KEK) shouldBe "key encryption key"
        describeKeyType(KeyType.DEK) shouldBe "data encryption key"
        describeKeyType(KeyType.TransmissionKey) shouldBe "transmission key"
        describeKeyType(KeyType.SigningKey) shouldBe "signing key"
        describeKeyType(KeyType.CertificateAuthorityKey) shouldBe "certificate authority key"
        describeKeyType(KeyType.ServiceIdentityKey) shouldBe "service identity key"
        describeKeyType(KeyType.RootKeyShare) shouldBe "root key share"
    }