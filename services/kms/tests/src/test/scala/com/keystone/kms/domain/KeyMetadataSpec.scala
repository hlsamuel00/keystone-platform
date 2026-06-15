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

    it should "not set expirationDate when creating a root key share" in {
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

    it should "update lastRotatedDate and expirationDate while preserving " +
      "all other fields for rotated managed keys" in {
        val rotated = testKeyMetadata.withRotation(
            Instant.now.plus(1, ChronoUnit.DAYS),
            Instant.now.plus(120, ChronoUnit.DAYS))

        rotated.lastRotatedDate shouldBe defined
        rotated.owner shouldBe testKeyMetadata.owner
        rotated.description shouldBe testKeyMetadata.description
        rotated.algorithm shouldBe testKeyMetadata.algorithm
        (rotated.expirationDate, testKeyMetadata.expirationDate) match
            case (Some(newExp), Some(oldExp)) => newExp.isAfter(oldExp) shouldBe true
            case _ => fail("Expected both expiration dates to be defined")
    }

    it should "update lastRotatedDate and preserve all other fields " +
      "for rotated root key shares" in {
        val rotated = rootKeyMetadata.withRootKeyShareRotation(Instant.now())

        rotated.lastRotatedDate shouldBe defined
        rotated.expirationDate shouldBe empty
        rotated.owner shouldBe rootKeyMetadata.owner
        rotated.description shouldBe rootKeyMetadata.description
        rotated.algorithm shouldBe rootKeyMetadata.algorithm
    }