package com.keystone.kms.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.Instant
import java.time.temporal.ChronoUnit

class KeyVersionSpec extends AnyFlatSpec with Matchers:
    val v1: KeyVersion = KeyVersion.createVersion(
        previousVersion=None, 
        createdAt=Instant.now(), 
        status=KeyVersionStatus.Active,
        expirationDate=None)
    
    val v2: KeyVersion = KeyVersion.createVersion(
        previousVersion=Some(v1),
        createdAt=Instant.now(),
        status=KeyVersionStatus.Active,
        expirationDate=None)
    
    "KeyVersion" should "ensure versionNumber starts with 1 when no prior version is provided" in {
        v1.versionNumber shouldEqual 1
    }
    
    it should "ensure versionNumber increments by 1 when prior version is provided" in {
        v2.versionNumber shouldEqual 2
    }
    
    it should "ensure versionId values are unique to each version" in {
        v1.versionId should not be v2.versionId
    }

    it should "ensure expiration date is correctly stored" in {
        val now = Instant.now()
        val expiringV = KeyVersion.createVersion(
            previousVersion=None,
            createdAt=now,
            status=KeyVersionStatus.Active,
            expirationDate=Some(now.plus(2, ChronoUnit.DAYS))
        )

        expiringV.expirationDate shouldEqual Some(now.plus(2, ChronoUnit.DAYS))
        v1.expirationDate should not be defined
    }
    
    it should "update status and preserve all other fields on a legal transition" in {
        v1.withStatus(KeyVersionStatus.Inactive) match
            case Right(updated) =>
                updated.status shouldEqual KeyVersionStatus.Inactive
                updated.versionNumber shouldEqual v1.versionNumber
                updated.versionId shouldEqual v1.versionId
                updated.createdAt shouldEqual v1.createdAt
                updated.expirationDate shouldEqual v1.expirationDate
            case Left(err) =>
                fail(s"Expected a legal transition, but got '$err'")
    }

    it should "reject an illegal transition" in {
        v1.withStatus(KeyVersionStatus.Active) shouldBe a [Left[?, ?]]
    }