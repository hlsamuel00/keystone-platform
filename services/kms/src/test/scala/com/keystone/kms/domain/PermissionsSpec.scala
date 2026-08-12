package com.keystone.kms.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.Inside
import java.time.Instant

class PermissionsSpec extends AnyFlatSpec with Matchers with Inside:
    val v1: Permissions = Permissions.create()
    "Permissions" should "create an empty record when initialized" in {
        v1.entries shouldBe empty
        v1.changeHistory shouldBe empty
    }

    val v2: Permissions = v1.grantOperation(
        serviceName="test-service",
        keyName="test-AES-key",
        operation=KeyOperation.Encrypt,
        rationale="Testing",
        changedAt=Instant.now()
    )
    it should "create an entry for a brand new service" in {
        v2.entries should have size 1
        v2.entries.head.allowedOperations should contain (KeyOperation.Encrypt)
        v2.changeHistory should have size 1
    }

    it should "not duplicate an entry for a service and operation" in {
        val duplicatePerm = v2.grantOperation(
            serviceName="test-service",
            keyName="test-AES-key",
            operation=KeyOperation.Encrypt,
            rationale="Testing",
            changedAt=Instant.now()
        )
        duplicatePerm.entries should have size 1
        duplicatePerm.entries.head.allowedOperations should have size 1
        duplicatePerm.changeHistory should have size 1
    }
    val v3: Permissions = v2.grantOperation(
        serviceName="test-service",
        keyName="test-AES-key",
        operation=KeyOperation.Decrypt,
        rationale="Testing",
        changedAt=Instant.now()
    )

    it should "extend an entry for adding permissions to an existing service" in {
        v3.entries should have size 1
        v3.entries.head.allowedOperations should have size 2
        v3.entries.head.allowedOperations should contain (KeyOperation.Decrypt)
        v3.changeHistory should have size 2
    }

    val v4: Permissions = v3.revokeOperation(
        serviceName="test-service",
        keyName="test-AES-key",
        operation=KeyOperation.Decrypt,
        rationale="Testing",
        changedAt=Instant.now()
    )

    it should "remove an operation when revoked and reflect change in history" in {

        v4.entries.head.allowedOperations should have size 1
        v4.entries.head.allowedOperations should not contain (KeyOperation.Decrypt)
        v4.changeHistory should have size 3
    }

    it should "return the same permissions if a duplicate revocation request is received" in {
        val v5 = v4.revokeOperation(
            serviceName="test-service",
            keyName="test-AES-key",
            operation=KeyOperation.Decrypt,
            rationale="Testing",
            changedAt=Instant.now()
        )
        v5.entries.head.allowedOperations shouldEqual v4.entries.head.allowedOperations
        v5.changeHistory shouldEqual v4.changeHistory
    }