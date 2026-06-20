package com.keystone.kms.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class ManagedKeySpec extends AnyFlatSpec with Matchers:

    val testKey: ManagedKey = ManagedKey.create(
        keyName = "payments-service-payment-processing-AES-key",
        keyOwner = "payments-service",
        keyType = KeyType.DEK,
        algorithm = KeyAlgorithm.AES256GCM,
        description = "encrypting payment payloads for payments-service",
        expirationDate = Instant.now().plus(90, ChronoUnit.DAYS)
    )

    "ManagedKey.create" should "initialize lifecycle fields correctly" in {
        testKey.keyState shouldBe KeyState.PendingApproval
        testKey.currentVersion.versionNumber shouldBe 1
        testKey.keyType shouldBe KeyType.DEK
    }

    it should "preserve all caller-provided values in metadata" in {
        testKey.keyName shouldBe "payments-service-payment-processing-AES-key"
        testKey.metadata.owner shouldBe "payments-service"
        testKey.metadata.algorithm shouldBe KeyAlgorithm.AES256GCM
        testKey.metadata.description shouldBe "encrypting payment payloads for payments-service"
        testKey.metadata.expirationDate shouldBe defined
    }

    it should "initialize permissions, dependencies, and rotation history as empty" in {
        testKey.permissions.entries shouldBe empty
        testKey.permissions.changeHistory shouldBe empty
        testKey.dependencies shouldBe empty
        testKey.metadata.lastRotatedDate shouldBe empty
    }

    it should "generate a unique keyId for each created key" in {
        val anotherKey = ManagedKey.create(
            keyName = "audit-service-log-signing-Ed25519-key",
            keyOwner = "audit-service",
            keyType = KeyType.SigningKey,
            algorithm = KeyAlgorithm.Ed25519Fixed,
            description = "signing audit log entries",
            expirationDate = Instant.now().plus(180, ChronoUnit.DAYS)
        )

        testKey.keyId should not equal anotherKey.keyId
    }

    val serviceKeyId: KeyId = UUID.randomUUID()
    val updatedKey: ManagedKey = testKey.withDependency(serviceKeyId)
    
    it should "update dependency set when adding an additional dependent" in {
        updatedKey.dependencies should have size 1
        updatedKey.dependencies should contain (serviceKeyId)
    }
    
    it should "ensure dependencies aren't duplicated" in {
        val duplicated = updatedKey.withDependency(serviceKeyId)
        
        duplicated.dependencies shouldEqual updatedKey.dependencies
    }
    
    it should "be usable polymorphically as a Key" in {
        val asKey: Key = testKey
        asKey.keyType shouldBe KeyType.DEK
        asKey.keyState shouldBe KeyState.PendingApproval
    }