package com.keystone.kms.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class KeyVersionRationaleSpec extends AnyFlatSpec with Matchers:
    "KeyVersionRationale" should "cover all rationales exhaustively" in {
        def describeKeyVersionRationale(r: KeyVersionRationale): String = r match 
            case KeyVersionRationale.InitialProvisioning => 
                "Rotation due to initial provisioning."
            case KeyVersionRationale.CompromiseRotation =>
                "Rotation due to compromised key material"
            case KeyVersionRationale.LifecycleRotation =>
                "Rotation due to key life cycle"
        
        
        describeKeyVersionRationale(KeyVersionRationale.InitialProvisioning) shouldBe "Rotation due to initial provisioning."
        describeKeyVersionRationale(KeyVersionRationale.CompromiseRotation) shouldBe "Rotation due to compromised key material"
        describeKeyVersionRationale(KeyVersionRationale.LifecycleRotation) shouldBe "Rotation due to key life cycle"
    }