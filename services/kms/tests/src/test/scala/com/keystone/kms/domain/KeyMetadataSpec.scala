package com.keystone.kms.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.time.Instant
import java.time.temporal.ChronoUnit

class KeyMetadataSpec extends AnyFlatSpec with Matchers:
    val now = Instant.now()
    val testKeyMetadata = KeyMetadata.createManagedKey(
        owner="test service",
        algorithm=KeyAlgorithm.AES256GCM,
        description="test key metadata for domain model validation",
        createdDate=now,
        expirationDate=now.plus(90, ChronoUnit.DAYS)
    )

    val rootKeyMetadata = KeyMetadata.createRootKeyShare()

    "KeyMetadata" should "set expiration date when creating a managed key" in {
        testKeyMetadata.expirationDate shouldBe defined
    }

    it should "not set expirationdate when creating a root key share" in {
        rootKeyMetadata.expirationDate shouldBe empty
    }

    it should "set the appropriate owner when creating a new key" in {
        testKeyMetadata.owner shouldBe "test service"
        rootKeyMetadata.owner shouldBe "KMS"
    }

    it should "not set lastRotatedDate for newly created keys" in {
        testKeyMetadata.lastRotatedDate shouldBe empty
        rootKeyMetadata.lastRotatedDate shouldBe empty
    }