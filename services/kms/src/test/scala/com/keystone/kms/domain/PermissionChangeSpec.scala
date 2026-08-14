package com.keystone.kms.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.time.Instant

class PermissionChangeSpec extends AnyFlatSpec with Matchers:
    val now: Instant = Instant.now()

    val grantedChange = PermissionChange(
        serviceName="test service",
        keyName="test-AES-key",
        operation=KeyOperation.Encrypt,
        status=PermissionChangeStatus.Granted,
        rationale="Access to ENCRYPT with key needed for topic publishing",
        changedAt=now
    )

    val revokedChange = PermissionChange(
        serviceName="test service",
        keyName="test-AES-key",
        operation=KeyOperation.Decrypt,
        status=PermissionChangeStatus.Revoked,
        rationale="Access to DECRYPT with key revoked due to no longer being used",
        changedAt=now
    )

    "PermissionChange" should "ensure records are created for both grant and revoke requests" in {
        grantedChange.status shouldBe PermissionChangeStatus.Granted
        revokedChange.status shouldBe PermissionChangeStatus.Revoked
    }

    it should "ensure the record is timestamped correctly" in {
        grantedChange.changedAt shouldEqual now
        revokedChange.changedAt shouldEqual now
    }
