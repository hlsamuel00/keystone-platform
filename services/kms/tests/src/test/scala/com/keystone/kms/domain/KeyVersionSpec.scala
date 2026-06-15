package com.keystone.kms.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.time.Instant

class KeyVersionSpec extends AnyFlatSpec with Matchers:
    val v1: KeyVersion = KeyVersion.createVersion(
        previousVersion=None, 
        createdAt=Instant.now(), 
        keyState=KeyState.PendingApproval)
    
    val v2: KeyVersion = KeyVersion.createVersion(
        previousVersion=Some(v1),
        createdAt=Instant.now(),
        keyState=KeyState.PendingApproval)
    
    "KeyVersion" should "ensure versionNumber starts with 1 when no prior version is provided" in {
        v1.versionNumber shouldEqual 1
    }
    
    it should "ensure versionNumber increments by 1 when prior version is provided" in {
        v2.versionNumber shouldEqual 2
    }
    
    it should "ensure versionId values are unique to each version" in {
        v1.versionId should not be v2.versionId
    }
    
    it should "update KeyState and preserve all other fields" in {
        val updated = v1.withKeyState(KeyState.Active)
        updated.versionNumber shouldEqual v1.versionNumber
        updated.versionId shouldEqual v1.versionId
        updated.createdAt shouldEqual v1.createdAt
    }