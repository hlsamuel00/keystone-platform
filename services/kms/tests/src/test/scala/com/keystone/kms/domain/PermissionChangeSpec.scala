package com.keystone.kms.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.time.Instant

class PermissionChangeSpec extends AnyFlatSpec with Matchers:
    val now: Instant = Instant.now()

    val approvedChange = PermissionChange(
        serviceName="test service",
        keyName="test-AES-key",
        operation=KeyOperation.Encrypt,
        status=PermissionChangeStatus.Approved,
        changedAt=now
    )

    val rejectedChange = PermissionChange(
        serviceName="test service",
        keyName="test-AES-key",
        operation=KeyOperation.Decrypt,
        status=PermissionChangeStatus.Rejected,
        changedAt=now
    )

    "PermissionChange" should "ensure records are created for both approved and rejected requests" in {
        approvedChange.status shouldBe PermissionChangeStatus.Approved
        rejectedChange.status shouldBe PermissionChangeStatus.Rejected
    }

    it should "ensure the record is timestamped correctly" in {
        approvedChange.changedAt shouldEqual now
        rejectedChange.changedAt shouldEqual now
    }