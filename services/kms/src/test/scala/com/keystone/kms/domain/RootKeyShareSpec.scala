package com.keystone.kms.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RootKeyShareSpec extends AnyFlatSpec with Matchers:

    val testShareA: RootKeyShare = RootKeyShare.create()
    val testShareB: RootKeyShare = RootKeyShare.create()

    "RootKeyShare.create" should "initialize lifecycle fields correctly" in {
        testShareA.keyType shouldBe KeyType.RootKeyShare
        testShareA.exceptionStatus shouldBe Some(KeyStatusNotice.PendingApproval)
        testShareA.currentVersion shouldBe None
    }

    it should "generate a unique keyId for each share, even independently created ones" in {
        testShareA.keyId should not equal testShareB.keyId
    }

    it should "be usable polymorphically as a Key" in {
        val asKey: Key = testShareA
        asKey.keyType shouldBe KeyType.RootKeyShare
    }

    it should "successfully provision the key share for valid requests" in {
        testShareA.provision match
            case Right(provisionedShareA) =>
                provisionedShareA.versions.entries should not be empty
                testShareB.versions.entries shouldBe empty
                provisionedShareA.exceptionStatus shouldBe Some(KeyStatusNotice.PendingApproval)
                provisionedShareA.clearException match
                    case Right(clearedShareA) =>
                        clearedShareA.exceptionStatus shouldBe None
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "reject provisioning for invalid requests" in {
        testShareA.provision match
            case Right(provisionedShareA) =>
                provisionedShareA.provision shouldBe a [Left[?, ?]]
                provisionedShareA.clearException match
                    case Right(clearedShareA) =>
                        clearedShareA.provision shouldBe a [Left[?, ?]]
                        clearedShareA.markCompromised match
                            case Right(compromisedShareA) =>
                                compromisedShareA.provision shouldBe a [Left[?, ?]]
                            case Left(err) =>
                                fail(s"Expected legal status compromise update, but got: $err")
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "successfully perform a lifecycle rotation for valid requests" in {
        testShareA.provision match
            case Right(provisionedShareA) =>
                provisionedShareA.clearException match
                    case Right(clearedShareA) =>
                        clearedShareA.lifecycleRotation match
                            case Right(rotatedShareA) =>
                                rotatedShareA.exceptionStatus shouldBe None
                                rotatedShareA.versions.entries should have size 2
                                rotatedShareA.versions.entries.head.status shouldBe KeyVersionStatus.Active
                                rotatedShareA.versions.entries.tail.head.status shouldBe KeyVersionStatus.Inactive
                                rotatedShareA.versions.changeHistory should have size (provisionedShareA.versions.changeHistory.size + 2)
                            case Left(err) =>
                                fail(s"Expected legal rotation, but got: $err")
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "reject an invalid lifecycle rotation request" in {
        testShareA.lifecycleRotation shouldBe a [Left[?, ?]]

        testShareA.provision match
            case Right(provisionedShareA) =>
                provisionedShareA.lifecycleRotation shouldBe a[Left[?, ?]]
                provisionedShareA.clearException match
                    case Right(clearedShareA) =>
                        clearedShareA.markCompromised match
                            case Right(compromisedShareA) =>
                                compromisedShareA.lifecycleRotation shouldBe a[Left[?, ?]]
                            case Left(err) =>
                                fail(s"Expected legal compromise status update, but got: $err")
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "successfully perform a compromised rotation for valid requests" in {
        testShareA.provision match
            case Right(provisionedShareA) =>
                provisionedShareA.clearException match
                    case Right(clearedShareA) =>
                        clearedShareA.markCompromised match
                            case Right(markedShareA) =>
                                markedShareA.exceptionStatus shouldBe Some(KeyStatusNotice.Compromised)
                                markedShareA.compromiseRotation match
                                    case Right(compromisedShareA) =>
                                        compromisedShareA.exceptionStatus shouldEqual markedShareA.exceptionStatus
                                        compromisedShareA.versions.entries.head.status shouldBe KeyVersionStatus.Active
                                        compromisedShareA.versions.entries.tail.head.status shouldBe KeyVersionStatus.Retired
                                        compromisedShareA.versions.changeHistory should have size (provisionedShareA.versions.changeHistory.size + 2)
                                    case Left(err) =>
                                        fail(s"Expected legal rotation, but got: $err")
                            case Left(err) =>
                                fail(s"Expected legal compromise status update, but got: $err")
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "reject an invalid compromise rotation request" in {
        testShareA.compromiseRotation shouldBe a [Left[?, ?]]

        testShareA.provision match
            case Right(provisionedShareA) =>
                provisionedShareA.clearException match
                    case Right(clearedShareA) =>
                        clearedShareA.compromiseRotation shouldBe a[Left[?, ?]]
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }