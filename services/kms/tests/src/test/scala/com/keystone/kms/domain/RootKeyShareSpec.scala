package com.keystone.kms.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RootKeyShareSpec extends AnyFlatSpec with Matchers:

    val shareA: RootKeyShare = RootKeyShare.create()
    val shareB: RootKeyShare = RootKeyShare.create()

    "RootKeyShare.create" should "initialize lifecycle fields correctly" in {
        shareA.keyType shouldBe KeyType.RootKeyShare
        shareA.keyState shouldBe KeyState.PendingApproval
        shareA.currentVersion.versionNumber shouldBe 1
    }

    it should "populate metadata via KeyMetadata.createRootKeyShare with no expiration" in {
        shareA.metadata.owner shouldBe "KMS"
        shareA.metadata.expirationDate shouldBe empty
        shareA.metadata.lastRotatedDate shouldBe empty
    }

    it should "generate a unique keyId for each share, even independently created ones" in {
        shareA.keyId should not equal shareB.keyId
    }

    it should "be usable polymorphically as a Key" in {
        val asKey: Key = shareA
        asKey.keyType shouldBe KeyType.RootKeyShare
    }