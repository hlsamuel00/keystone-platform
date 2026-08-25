package com.keystone.kms.domain

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID

class ManagedKeySpec extends AnyFlatSpec with Matchers:

    val testKey: ManagedKey = ManagedKey.create(
        keyName = "payments-service-payment-processing-AES-key",
        keyOwner = "payments-service",
        keyType = KeyType.DEK,
        algorithm = KeyAlgorithm.AES256GCM,
        description = "encrypting payment payloads for payments-service"
    )

    "ManagedKey.create" should "initialize lifecycle fields correctly" in {
        testKey.exceptionStatus shouldBe Some(KeyStatusNotice.PendingApproval)
        testKey.currentVersion shouldBe None
        testKey.keyType shouldBe KeyType.DEK
        testKey.versions.entries shouldBe empty
        testKey.versions.changeHistory shouldBe empty
    }

    it should "preserve all caller-provided values in metadata" in {
        testKey.keyName shouldBe "payments-service-payment-processing-AES-key"
        testKey.owner shouldBe "payments-service"
        testKey.algorithm shouldBe KeyAlgorithm.AES256GCM
        testKey.description shouldBe "encrypting payment payloads for payments-service"
    }

    it should "initialize permissions, dependencies, and rotation history as empty" in {
        testKey.permissions.entries shouldBe empty
        testKey.permissions.changeHistory shouldBe empty
        testKey.dependencies shouldBe empty
    }

    it should "generate a unique keyId for each created key" in {
        val anotherKey = ManagedKey.create(
            keyName = "audit-service-log-signing-Ed25519-key",
            keyOwner = "audit-service",
            keyType = KeyType.SigningKey,
            algorithm = KeyAlgorithm.Ed25519Fixed,
            description = "signing audit log entries",
        )

        testKey.keyId should not equal anotherKey.keyId
    }

    val serviceKeyId: KeyId = UUID.randomUUID()
    val updatedKey: ManagedKey = testKey.withDependency(serviceKeyId)
    
    it should "update dependency set when adding an additional dependent and maintain other details" in {
        updatedKey.dependencies should have size 1
        updatedKey.dependencies should contain (serviceKeyId)
        updatedKey.dependencies should not equal testKey.dependencies
        updatedKey.keyType shouldEqual testKey.keyType
        updatedKey.keyId shouldEqual testKey.keyId
        updatedKey.keyName shouldEqual testKey.keyName
        updatedKey.versions shouldEqual testKey.versions
        updatedKey.permissions shouldEqual testKey.permissions
        updatedKey.owner shouldEqual testKey.owner
        updatedKey.algorithm shouldEqual testKey.algorithm
        updatedKey.description shouldEqual testKey.description
    }
    
    it should "ensure dependencies aren't duplicated" in {
        val duplicated = updatedKey.withDependency(serviceKeyId)
        
        duplicated.dependencies shouldEqual updatedKey.dependencies
    }
    it should "update dependency set when removing an additional dependent and maintain other details" in {
        val removedDependency = updatedKey.removeDependency(serviceKeyId)

        removedDependency.dependencies shouldEqual testKey.dependencies
        removedDependency.keyType shouldEqual testKey.keyType
        removedDependency.keyId shouldEqual testKey.keyId
        removedDependency.keyName shouldEqual testKey.keyName
        removedDependency.versions shouldEqual testKey.versions
        removedDependency.permissions shouldEqual testKey.permissions
        removedDependency.owner shouldEqual testKey.owner
        removedDependency.algorithm shouldEqual testKey.algorithm
        removedDependency.description shouldEqual testKey.description
    }

    it should "safely no-op when removing a dependency that was never added" in {
        val neverAdded: KeyId = UUID.randomUUID()
        val result = testKey.removeDependency(neverAdded)

        result.dependencies shouldEqual testKey.dependencies
    }
    
    it should "be usable polymorphically as a Key" in {
        val asKey: Key = testKey
        asKey.keyType shouldBe KeyType.DEK
        asKey.exceptionStatus shouldBe Some(KeyStatusNotice.PendingApproval)
    }

    it should "successfully provision a key and remove the status notice once called after provisioning" in{
        testKey.provision(None) match
            case Right(provisioned) =>
                provisioned.versions.entries should not be empty
                provisioned.exceptionStatus shouldBe Some(KeyStatusNotice.PendingApproval)
                provisioned.clearException match
                    case Right(cleared) =>
                        cleared.exceptionStatus shouldBe None
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "reject an illegal provision request" in {
        testKey.markRejected match
            case Right(rejected) =>
                rejected.provision(None) shouldBe a [Left[?, ?]]
            case Left(err) =>
                fail(s"Expected legal rejected status update, but got: $err")

        testKey.provision(None) match
            case Right(provisioned) =>
                provisioned.provision(None) shouldBe a [Left[?, ?]]
                provisioned.clearException match
                    case Right(cleared) =>
                        cleared.provision(None) shouldBe a [Left[?, ?]]
                        cleared.markCompromised match
                            case Right(compromised) =>
                                compromised.provision(None) shouldBe a [Left[?, ?]]
                            case Left(err) =>
                                fail(s"Expected legal status compromise update, but got: $err")
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "successfully rotate a key through lifecycle rotation" in {
        testKey.provision(None) match
            case Right(provisioned) =>
                provisioned.clearException match
                    case Right(cleared) =>
                        cleared.lifecycleRotation(None) match
                            case Right(rotated) =>
                                rotated.exceptionStatus shouldBe None
                                rotated.versions.entries should have size 2
                                rotated.versions.entries.head.status shouldBe KeyVersionStatus.Active
                                rotated.versions.entries.tail.head.status shouldBe KeyVersionStatus.Inactive
                                rotated.versions.changeHistory should have size (provisioned.versions.changeHistory.size + 2)
                            case Left(err) =>
                                fail(s"Expected legal rotation, but got: $err")
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "reject an invalid lifecycle rotation" in {
        testKey.lifecycleRotation(None) shouldBe a [Left[?, ?]]

        testKey.markRejected match
            case Right(rejected) =>
                rejected.lifecycleRotation(None) shouldBe a [Left[?, ?]]
            case Left(err) =>
                fail(s"Expected legal rejected status update, but got: $err")

        testKey.provision(None) match
            case Right(provisioned) =>
                provisioned.lifecycleRotation(None) shouldBe a [Left[?, ?]]
                provisioned.clearException match
                    case Right(cleared) =>
                        cleared.markCompromised match
                            case Right(compromised) =>
                                compromised.lifecycleRotation(None) shouldBe a [Left[?, ?]]
                            case Left(err) =>
                                fail(s"Expected legal compromise status update, but got: $err")
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "successfully windDown a version for a valid request" in {
        testKey.provision(None) match
            case Right(provisioned) =>
                provisioned.clearException match
                    case Right(cleared) =>
                        cleared.lifecycleWindDown match
                            case Right(woundDown) =>
                                woundDown.exceptionStatus shouldBe None
                                woundDown.versions.entries should have size 1
                                woundDown.versions.entries.head.status shouldBe KeyVersionStatus.Inactive
                                woundDown.versions.changeHistory should have size (provisioned.versions.changeHistory.size + 1)
                            case Left(err) =>
                                fail(s"Expected legal lifecycle wind down, but got: $err")
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "reject an invalid windDown request" in {
        testKey.lifecycleWindDown shouldBe a [Left[?, ?]]

        testKey.markRejected match
            case Right(rejected) =>
                rejected.lifecycleWindDown shouldBe a [Left[?, ?]]
            case Left(err) =>
                fail(s"Expected legal rejected status update, but got: $err")

        testKey.provision(None) match
            case Right(provisioned) =>
                provisioned.clearException match
                    case Right(cleared) =>
                        cleared.markCompromised match
                            case Right(compromised) =>
                                compromised.lifecycleWindDown shouldBe a [Left[?, ?]]
                            case Left(err) =>
                                fail(s"Expected legal compromise status update, but got: $err")
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "successfully resume with new version for valid requests" in {
        testKey.provision(None) match
            case Right(provisioned) =>
                provisioned.clearException match
                    case Right(cleared) =>
                        cleared.lifecycleWindDown match
                            case Right(woundDown) =>
                                woundDown.resumeWithNewVersion(KeyVersionRationale.LifecycleRotation, None) match
                                    case Right(rotated) =>
                                        rotated.exceptionStatus shouldBe None
                                        rotated.versions.entries.head.status shouldBe KeyVersionStatus.Active
                                    case Left(err) =>
                                        fail(s"Expected legal rotation, but got: $err")
                            case Left(err) =>
                                fail(s"Expected legal lifecycle wind down, but got: $err")
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "reject an invalid key version update for invalid requests" in {
        testKey.resumeWithNewVersion(KeyVersionRationale.InitialProvisioning, None) shouldBe a [Left[?, ?]]

        testKey.markRejected match
            case Right(rejected) =>
                rejected.resumeWithNewVersion(KeyVersionRationale.InitialProvisioning, None) shouldBe a [Left[?, ?]]
            case Left(err) =>
                fail(s"Expected legal rejected status update, but got: $err")

        testKey.provision(None) match
            case Right(provisioned) =>
                provisioned.clearException match
                    case Right(cleared) =>
                        cleared.markCompromised match
                            case Right(compromised) =>
                                compromised.resumeWithNewVersion(KeyVersionRationale.CompromiseRotation, None) shouldBe a [Left[?, ?]]
                            case Left(err) =>
                                fail(s"Expected legal compromise status update, but got: $err")
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "successfully rotate a key through compromised rotation" in {
        testKey.provision(None) match
            case Right(provisioned) =>
                provisioned.clearException match
                    case Right(cleared) =>
                        cleared.markCompromised match
                            case Right(marked) =>
                                marked.exceptionStatus shouldBe Some(KeyStatusNotice.Compromised)
                                marked.compromiseRotation(None) match
                                    case Right(compromised) =>
                                        compromised.exceptionStatus shouldEqual marked.exceptionStatus
                                        compromised.versions.entries.head.status shouldBe KeyVersionStatus.Active
                                        compromised.versions.entries.tail.head.status shouldBe KeyVersionStatus.Retired
                                        compromised.versions.changeHistory should have size (provisioned.versions.changeHistory.size + 2)
                                    case Left(err) =>
                                        fail(s"Expected legal rotation, but got: $err")
                            case Left(err) =>
                                fail(s"Expected legal compromise status update, but got: $err")
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "reject an invalid compromise rotation" in {
        testKey.compromiseRotation(None) shouldBe a [Left[?, ?]]

        testKey.markRejected match
            case Right(rejected) =>
                rejected.compromiseRotation(None) shouldBe a [Left[?, ?]]
            case Left(err) =>
                fail(s"Expected legal rejected status update, but got: $err")

        testKey.provision(None) match
            case Right(provisioned) =>
                provisioned.clearException match
                    case Right(cleared) =>
                        cleared.compromiseRotation(None) shouldBe a [Left[?, ?]]
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "successfully retire a version given the UUID or version number" in {
        testKey.provision(None) match
            case Right(provisioned) =>
                val versionId = provisioned.versions.entries.head.versionId
                val versionNumber = provisioned.versions.entries.head.versionNumber
                provisioned.clearException match
                    case Right(cleared) =>
                        cleared.lifecycleRotation(None) match
                            case Right(rotated) =>
                                rotated.retireVersion(versionId) match
                                    case Right(retiredWithId) =>
                                        retiredWithId.versions.entries.tail.head.status shouldBe KeyVersionStatus.Retired
                                        retiredWithId.versions.entries.head.status shouldBe rotated.versions.entries.head.status
                                    case Left(err) =>
                                        fail(s"Expected legal retiring, but got: $err")

                                rotated.retireVersion(versionNumber) match
                                    case Right(retiredWithNumber) =>
                                        retiredWithNumber.versions.entries.tail.head.status shouldBe KeyVersionStatus.Retired
                                        retiredWithNumber.versions.entries.head.status shouldBe rotated.versions.entries.head.status
                                    case Left(err) =>
                                        fail(s"Expected legal version retiring, but got: $err")
                            case Left(err) =>
                                fail(s"Expected legal lifecycle rotation, but got: $err")
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "reject an invalid version retiring request" in {
        testKey.markRejected match
            case Right(rejected) =>
                rejected.retireVersion(UUID.randomUUID()) shouldBe a[Left[?, ?]]
                rejected.retireVersion(1) shouldBe a [Left[?, ?]]
            case Left(err) =>
                fail(s"Expected legal rejected status update, but got: $err")

        testKey.provision(None) match
            case Right(provisioned) =>
                val versionId = provisioned.versions.entries.head.versionId
                val versionNumber = provisioned.versions.entries.head.versionNumber

                provisioned.retireVersion(versionId) shouldBe a[Left[?, ?]]
                provisioned.retireVersion(1) shouldBe a[Left[?, ?]]

                provisioned.clearException match
                    case Right(cleared) =>
                        cleared.retireVersion(versionId) shouldBe a [Left[?, ?]]
                        cleared.retireVersion(versionNumber) shouldBe a [Left[?, ?]]
                        cleared.lifecycleRotation(None) match
                            case Right(rotated) =>
                                rotated.retireVersion(UUID.randomUUID()) shouldBe a [Left[?, ?]]
                                rotated.retireVersion(99) shouldBe a [Left[?, ?]]

                                rotated.retireVersion(versionId) match
                                    case Right(retired) =>
                                        retired.retireVersion(versionId) shouldBe a [Left[?, ?]]
                                    case Left(err) =>
                                        fail(s"Expected legal version retiring, but got: $err")

                                rotated.retireVersion(versionNumber) match
                                    case Right(retired) =>
                                        retired.retireVersion(versionNumber) shouldBe a [Left[?, ?]]
                                    case Left(err) =>
                                        fail(s"Expected legal version retiring, but got: $err")
                            case Left(err) =>
                                fail(s"Expected legal lifecycle rotation, but got: $err")

                        cleared.markCompromised match
                            case Right(compromised) =>
                                compromised.retireVersion(versionId) shouldBe a [Left[?, ?]]
                                compromised.retireVersion(99) shouldBe a [Left[?, ?]]
                            case Left(err) =>
                                fail(s"Expected legal compromise status update, but got: $err")
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "successfully process a valid compromise retire request" in {
        testKey.provision(None) match
            case Right(provisioned) =>
                provisioned.clearException match
                    case Right(cleared) =>
                        cleared.markCompromised match
                            case Right(compromised) =>
                                compromised.compromiseRetire match
                                    case Right(retired) =>
                                        retired.versions.entries.head.status shouldBe KeyVersionStatus.Retired
                                        retired.exceptionStatus shouldBe Some(KeyStatusNotice.Compromised)
                                    case Left(err) =>
                                        fail(s"Expected legal version retiring, but got: $err")
                            case Left(err) =>
                                fail(s"Expected legal compromise status update, but got: $err")
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "reject invalid compromise retire requests" in {
        testKey.compromiseRetire shouldBe a [Left[?, ?]]

        testKey.markRejected match
            case Right(rejected) =>
                rejected.compromiseRetire shouldBe a [Left[?, ?]]
            case Left(err) =>
                fail(s"Expected legal rejected status update, but got: $err")

        testKey.provision(None) match
            case Right(provisioned) =>
                provisioned.clearException match
                    case Right(cleared) =>
                        cleared.compromiseRetire shouldBe a [Left[?, ?]]
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "reject adding a compromised flag for invalid requests" in {
        testKey.markRejected match
            case Right(rejected) =>
                rejected.markCompromised shouldBe a [Left[?, ?]]
            case Left(err) =>
                fail(s"Expected legal rejected status update, but got: $err")

        testKey.markCompromised shouldBe a [Left[?, ?]]

        testKey.provision(None) match
            case Right(provisioned) =>
                provisioned.clearException match
                    case Right(cleared) =>
                        cleared.markCompromised match
                            case Right(compromised) =>
                                compromised.markCompromised shouldBe a [Left[?, ?]]
                            case Left(err) =>
                                fail(s"Expected legal compromise status update, but got: $err")
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "reject adding a rejected flag for invalid requests" in {
        testKey.provision(None) match
            case Right(provisioned) =>
                provisioned.markRejected shouldBe a [Left[?, ?]]

                provisioned.clearException match
                    case Right(cleared) =>
                        cleared.markRejected shouldBe a [Left[?, ?]]

                        cleared.markCompromised match
                            case Right(compromised) =>
                                compromised.markRejected shouldBe a [Left[?, ?]]
                            case Left(err) =>
                                fail(s"Expected legal compromise status update, but got: $err")
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "clear key status notice during full compromise-recovery cycle" in {
        testKey.provision(None) match
            case Right(provisioned) =>
                provisioned.clearException match
                    case Right(cleared) =>
                        cleared.markCompromised match
                            case Right(compromised) =>
                                compromised.compromiseRotation(None) match
                                    case Right(recovered) =>
                                        recovered.clearException match
                                            case Right(recoveredAndRotated) =>
                                                recoveredAndRotated.exceptionStatus shouldBe None
                                                recoveredAndRotated.versions.entries should have size 2
                                                recoveredAndRotated.versions.entries.head.status shouldBe KeyVersionStatus.Active
                                            case Left(err) =>
                                                fail(s"Expected legal compromised exception clearing, but got: $err")
                                    case Left(err) =>
                                        fail(s"Expected legal compromise rotation, but got: $err")
                            case Left(err) =>
                                fail(s"Expected legal compromise status update, but got: $err")
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "reject clearing key-level flag for invalid requests" in {
        testKey.clearException shouldBe a [Left[?, ?]]

        testKey.provision(None) match
            case Right(provisioned) =>
                provisioned.clearException match
                    case Right(cleared) =>
                        cleared.clearException shouldBe a [Left[?, ?]]
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "successfully decommission a retired key for a valid request" in {
        testKey.provision(None) match
            case Right(provisioned) =>
                provisioned.clearException match
                    case Right(cleared) =>
                        cleared.lifecycleWindDown match
                            case Right(woundDown) =>
                                val versionId = cleared.versions.entries.head.versionId
                                woundDown.retireVersion(versionId) match
                                    case Right(retired) =>
                                        retired.decommission match
                                            case Right(decommissioned) =>
                                                decommissioned.exceptionStatus shouldBe Some(KeyStatusNotice.Decommissioned)
                                                decommissioned.versions shouldEqual retired.versions
                                                decommissioned.markCompromised shouldBe a [Left[?, ?]]
                                            case Left(err) =>
                                                fail(s"Expected legal decommissioning, but got: $err")
                                    case Left(err) =>
                                        fail(s"Expected legal version retiring, but got: $err")
                            case Left(err) =>
                                fail(s"Expected legal lifecycle wind down, but got: $err")
                        cleared.markCompromised match
                            case Right(compromised) =>
                                compromised.compromiseRetire match
                                    case Right(retired) =>
                                        retired.decommission match
                                            case Right(decommissioned) =>
                                                decommissioned.exceptionStatus shouldBe Some(KeyStatusNotice.Decommissioned)
                                                decommissioned.versions shouldEqual retired.versions
                                                decommissioned.markCompromised shouldBe a [Left[?, ?]]
                                            case Left(err) =>
                                                fail(s"Expected legal decommissioning, but got: $err")
                                    case Left(err) =>
                                        fail(s"Expected legal compromise rotation, but got: $err")
                            case Left(err) =>
                                fail(s"Expected legal compromise status update, but got: $err")
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "reject an invalid decommission request" in {
        testKey.decommission shouldBe a [Left[?, ?]]

        testKey.provision(None) match
            case Right(provisioned) =>
                provisioned.clearException match
                    case Right(cleared) =>
                        cleared.lifecycleWindDown match
                            case Right(woundDown) =>
                                val versionId = cleared.versions.entries.head.versionId
                                woundDown.retireVersion(versionId) match
                                    case Right(retired) =>
                                        retired.decommission match
                                            case Right(decommissioned) =>
                                                decommissioned.decommission shouldBe a [Left[?, ?]]
                                            case Left(err) =>
                                                fail(s"Expected legal decommissioning, but got $err")
                                    case Left(err) =>
                                        fail(s"Expected legal retiring, but got $err")
                            case Left(err) =>
                                fail(s"Expected legal wind down, but got: $err")
                        cleared.markCompromised match
                            case Right(compromised) =>
                                compromised.decommission shouldBe a [Left[?, ?]]
                            case Left(err) =>
                                fail(s"Expected legal compromise status update, but got: $err")
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }

    it should "reject decommissioning a key with unresolved dependent keys" in {
        val dependentKeyId: KeyId = UUID.randomUUID()

        testKey.provision(None) match
            case Right(provisioned) =>
                provisioned.clearException match
                    case Right(cleared) =>
                        cleared.markCompromised match
                            case Right(compromised) =>
                                compromised.compromiseRetire match
                                    case Right(retired) =>
                                        val retiredWithDependency = retired.withDependency(dependentKeyId)
                                        retiredWithDependency.decommission shouldBe a [Left[?, ?]]
                                    case Left(err) =>
                                        fail(s"Expected legal compromise rotation, but got: $err")
                            case Left(err) =>
                                fail(s"Expected legal compromise status update, but got: $err")
                    case Left(err) =>
                        fail(s"Expected legal exception clearing, but got: $err")
            case Left(err) =>
                fail(s"Expected legal provisioning, but got: $err")
    }
