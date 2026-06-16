package com.keystone.kms.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ServicePermissionsSpec extends AnyFlatSpec with Matchers:
    "ServicePermissions" should "ensure record created has provided values" in {
        val permission = ServicePermissions(
            serviceName="test-service",
            allowedOperations=Set(
                KeyOperation.Encrypt,
                KeyOperation.Decrypt,
            )
        )
        permission.serviceName shouldBe "test-service"
        permission.allowedOperations should have size 2
        permission.allowedOperations should contain (KeyOperation.Encrypt)
        permission.allowedOperations should contain (KeyOperation.Decrypt)
    }

    it should "ensure a record can be created with an empty allowedOperations set" in {
        val emptyPermissions = ServicePermissions(serviceName="test-service")
        emptyPermissions.serviceName shouldBe "test-service"
        emptyPermissions.allowedOperations shouldBe empty
    }