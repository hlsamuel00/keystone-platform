package com.keystone.kms.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.time.Instant
import java.util.UUID

class VersionRecordSpec extends AnyFlatSpec with Matchers:
    val now: Instant = Instant.now()
    val record: VersionRecord = VersionRecord(
        versionId=UUID.randomUUID(),
        status=KeyVersionStatus.Active,
        rationale=KeyVersionRationale.InitialProvisioning,
        changedAt=now
    )

    "VersionRecord" should "ensure a record is created with the correct fields" in {
        record.versionId shouldBe a [UUID]
        record.status shouldBe KeyVersionStatus.Active
        record.rationale shouldBe KeyVersionRationale.InitialProvisioning
        record.changedAt shouldEqual now
    }

    it should "ensure UUID creation is unique across version records" in {
        val anotherRecord = VersionRecord(
            versionId=UUID.randomUUID(),
            status=KeyVersionStatus.Inactive,
            rationale=KeyVersionRationale.LifecycleRotation,
            changedAt=now
        )

        record.versionId should not equal anotherRecord.versionId
    }