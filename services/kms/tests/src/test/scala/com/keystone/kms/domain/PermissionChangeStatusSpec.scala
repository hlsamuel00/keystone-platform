package com.keystone.kms.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class PermissionChangeStatusSpec extends AnyFlatSpec with Matchers:
    "PermissionChangeStatus" should "cover all statuses exhaustively" in {
        def describePermissionChangeStatus(s: PermissionChangeStatus): String = s match
            case PermissionChangeStatus.Approved => "Approved"
            case PermissionChangeStatus.Rejected => "Rejected"
            
        describePermissionChangeStatus(PermissionChangeStatus.Approved) shouldBe "Approved"
        describePermissionChangeStatus(PermissionChangeStatus.Rejected) shouldBe "Rejected"
    }