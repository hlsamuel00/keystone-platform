package com.keystone.kms.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.time.Instant
import java.util.UUID

class VersionsSpec extends AnyFlatSpec with Matchers:
    val v: Versions = Versions.create()

    "Versions" should "create a new object with empty entries and history" in {
        v.entries shouldBe empty
        v.changeHistory shouldBe empty
    }

    it should "approve a legal provisioning" in {
        v.provision(Instant.now(), None) match
            case Right(newV) =>
                newV.entries should have size 1
                newV.entries.head.status shouldBe KeyVersionStatus.Active
                newV.entries.head.versionNumber shouldBe 1
                newV.changeHistory should have size 1
                newV.changeHistory.head.rationale shouldBe KeyVersionRationale.InitialProvisioning
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "reject an illegal provisioning" in {
        v.provision(Instant.now(), None) match
            case Right(newV) =>
                newV.provision(Instant.now(), None) shouldBe a [Left[?, ?]]
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "approve a legal lifecycle rotation" in {
        v.provision(Instant.now(), None) match
            case Right(newV) =>
                newV.lifecycleRotation(Instant.now(), None) match
                    case Right(lifeCycleV) =>
                        lifeCycleV.entries.head.status shouldBe KeyVersionStatus.Active
                        lifeCycleV.entries.tail.head.status shouldBe KeyVersionStatus.Inactive
                        lifeCycleV.changeHistory should have size 3
                        lifeCycleV.changeHistory.head.rationale shouldBe KeyVersionRationale.LifecycleRotation
                        lifeCycleV.changeHistory.tail.head.rationale shouldBe KeyVersionRationale.LifecycleRotation
                    case Left(err) =>
                        fail(s"Expected a legal lifecycle rotation, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "reject an illegal lifecycle rotation" in {
        v.lifecycleRotation(Instant.now(), None) shouldBe a [Left[?, ?]]
    }

    it should "approve a legal lifecycle windDown" in {
        v.provision(Instant.now(), None) match
            case Right(newV) =>
                newV.lifecycleWindDown(Instant.now()) match
                    case Right(windDownV) =>
                        windDownV.entries.head.status shouldBe KeyVersionStatus.Inactive
                        windDownV.entries should have size 1
                        windDownV.changeHistory should have size (newV.changeHistory.size + 1)
                    case Left(err) =>
                        fail(s"Expected a legal lifecycle windDown, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "reject an illegal lifecycle windDown" in {
        v.lifecycleWindDown(Instant.now()) shouldBe a [Left[?, ?]]
    }

    it should "approve a legal compromise rotation" in {
        v.provision(Instant.now(), None) match
            case Right(newV) =>
                newV.compromiseRotation(Instant.now(), None) match
                    case Right(compromiseV) =>
                        compromiseV.entries.head.status shouldBe KeyVersionStatus.Active
                        compromiseV.entries.tail.head.status shouldBe KeyVersionStatus.Retired
                        compromiseV.changeHistory should have size 3
                        compromiseV.changeHistory.head.rationale shouldBe KeyVersionRationale.CompromiseRotation
                        compromiseV.changeHistory.tail.head.rationale shouldBe KeyVersionRationale.CompromiseRotation
                    case Left(err) =>
                        fail(s"Expected a legal compromise rotation, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "reject an illegal compromise rotation" in {
        v.compromiseRotation(Instant.now(), None) shouldBe a [Left[?, ?]]
    }

    it should "approve a legal compromise retiring" in {
        v.provision(Instant.now(), None) match
            case Right(newV) =>
                newV.compromiseRetire(Instant.now()) match
                    case Right(compromiseV) =>
                        compromiseV.entries.head.status shouldBe KeyVersionStatus.Retired
                        compromiseV.entries should have size 1
                        compromiseV.changeHistory should have size (newV.changeHistory.size + 1)
                    case Left(err) =>
                        fail(s"Expected a legal compromise retiring, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "reject an illegal compromise retiring" in {
        v.compromiseRetire(Instant.now()) shouldBe a [Left[?, ?]]
    }

    it should "successfully retire a version given its UUID" in {
        v.provision(Instant.now(), None) match
            case Right(newV) =>
                newV.lifecycleRotation(Instant.now(), None) match
                    case Right(lifeCycleV) =>
                        val versionId = lifeCycleV.entries.tail.head.versionId
                        lifeCycleV.retireVersion(versionId, Instant.now()) match
                            case Right(retiredV) =>
                                retiredV.entries.tail.head.status shouldBe KeyVersionStatus.Retired
                                retiredV.entries.head.status shouldBe lifeCycleV.entries.head.status
                                retiredV.changeHistory should have size (lifeCycleV.changeHistory.size + 1)
                            case Left(err) =>
                                fail(s"Expected legal retiring, but got: $err")
                    case Left(err) =>
                        fail(s"Expected a legal lifecycle rotation, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "reject an invalid retire request" in {
        v.provision(Instant.now(), None) match
            case Right(newV) =>
                newV.retireVersion(UUID.randomUUID(), Instant.now()) shouldBe a [Left[?, ?]]

                val activeId = newV.entries.head.versionId
                newV.retireVersion(activeId, Instant.now()) shouldBe a [Left[?, ?]]

                newV.lifecycleRotation(Instant.now(), None) match
                    case Right(lifeCycleV) =>
                        val retiredId = lifeCycleV.entries.tail.head.versionId
                        lifeCycleV.retireVersion(retiredId, Instant.now()) match
                            case Right(retiredV) =>
                                retiredV.retireVersion(retiredId, Instant.now()) shouldBe a [Left[?, ?]]
                            case Left(err) =>
                                fail(s"Expected legal retiring, but got: $err")
                    case Left(err) =>
                        fail(s"Expected a legal lifecycle rotation, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "successfully retire a version given the version number" in {
        v.provision(Instant.now(), None) match
            case Right(newV) =>
                newV.lifecycleRotation(Instant.now(), None) match
                    case Right(lifeCycleV) =>
                        lifeCycleV.retireVersion(1, Instant.now()) match
                            case Right(retiredV) =>
                                retiredV.entries.tail.head.status shouldBe KeyVersionStatus.Retired
                                retiredV.entries.head.status shouldBe lifeCycleV.entries.head.status
                                retiredV.changeHistory should have size (lifeCycleV.changeHistory.size + 1)
                            case Left(err) =>
                                fail(s"Expected legal retiring, but got: $err")
                    case Left(err) =>
                        fail(s"Expected a legal lifecycle rotation, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "reject a request with an invalid versionNumber" in {
        v.provision(Instant.now(), None) match
            case Right(newV) =>
                newV.lifecycleRotation(Instant.now(), None) match
                    case Right(lifeCycleV) =>
                        lifeCycleV.retireVersion(99, Instant.now()) shouldBe a [Left[?, ?]]
                    case Left(err) =>
                        fail(s"Expected a legal lifecycle rotation, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "successfully continue with new version for valid requests" in {
        v.provision(Instant.now(), None) match
            case Right(newV) =>
                newV.lifecycleWindDown(Instant.now()) match
                    case Right(windDownV) =>
                        windDownV.resumeWithNewVersion(KeyVersionRationale.LifecycleRotation, None) match
                            case Right(rotatedV) =>
                                rotatedV.entries.head.status shouldBe KeyVersionStatus.Active
                                rotatedV.entries should have size 2
                                rotatedV.changeHistory should have size (windDownV.changeHistory.size + 1)
                            case Left(err) =>
                                fail(s"Expected a legal rotation, but got: $err")
                    case Left(err) =>
                        fail(s"Expected a legal lifecycle windDown, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "reject an invalid 'resume with new version' request" in {
        v.resumeWithNewVersion(KeyVersionRationale.InitialProvisioning, None) shouldBe a [Left[?, ?]]

        v.provision(Instant.now(), None) match
            case Right(newV) =>
                newV.resumeWithNewVersion(KeyVersionRationale.LifecycleRotation, None) shouldBe a [Left[?, ?]]

                newV.lifecycleWindDown(Instant.now()) match
                    case Right(windDownV) =>
                        windDownV.resumeWithNewVersion(KeyVersionRationale.CompromiseRotation, None) shouldBe a [Left[?, ?]]
                    case Left(err) =>
                        fail(s"Expected a legal lifecycle windDown, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }
