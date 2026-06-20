package com.keystone.kms.domain

import java.time.Instant
import java.util.UUID

/** Representative of the key identifier; this type provides clarity to nested fields
  * where the java.util.UUID wouldn't provide clarity to what was contained
  */
type KeyId = UUID

/** Representative of the basic structure of a key. Keys diverge into two main
  * types: ManagedKey and RootKeyShare. ManagedKey(s) are every type of key that
  * KMS maintains, whereas RootKeyShare(s) are treated fundamentally different;
  * although they share the same convention of being called a key, they differ greatly
  * in purpose as neither share can be utilized on its own, but still share a core foundation
  * in its lifecycle. The required fields for a Key are fields that are consistent
  * across both RootKeyShare(s) and ManagedKey(s):
  * - keyId: KeyId - the unique identifier for a specific key randomly generated
  * - keyType: KeyType - the unique type of key
  * - keyState: KeyState - the current state of the key starting at PendingApproval
  * - currentVersion: KeyVersion - the current version of the key defaulted to v1
  */
sealed trait Key:
    def keyId: KeyId
    def keyType: KeyType
    def keyState: KeyState
    def currentVersion: KeyVersion

/** Representative of all keys managed by KMS. These keys require additional fields that
  * don't apply to RootKeyShare(s). The unique fields are as follows:
  * - keyId: KeyId - the unique identifier for the key
  * - keyType: KeyType - the specific type of key (KEK, DEK, Transmission, etc.)
  * - keyState: KeyState - the key's current state
  * - currentVersion: KeyVersion - the key's current version (starting with v1)
  * - keyName: String - the name of the respective key
  * - metadata: KeyMetadata - internal details regarding the key
  * - permissions: Permissions - the services and their respective permissions on the respective key
  * - dependencies: Set[KeyId] - a set of KeyId(s) who depend on this respective key; a
  * major distinction to be highlighted, this is reversed from typical meaning
  */
case class ManagedKey private (
                              keyId: KeyId,
                              keyType: KeyType,
                              keyState: KeyState,
                              currentVersion: KeyVersion,
                              keyName: String,
                              metadata: KeyMetadata,
                              permissions: Permissions,
                              dependencies: Set[KeyId]
                              ) extends Key


object ManagedKey:
    /** Creates a ManagedKey derived from Key. The smart constructor requires
      * the following fields:
      * - keyName: String - the name of the respective key following a naming convention of
      *   [owning service]-[key purpose]-[key algorithm]-key (all hyphen delineated), ex:
      *   `payments-service-payment-processing-AES-key`
      * - keyOwner: String - the owning service of the respective key
      * - keyType: KeyType - the type of this respective key
      * - algorithm: KeyAlgorithm - the algorithm this key utilizes
      * - description: String - a brief explanation of the keys purpose,
      *   ex: `encrypting data for accounts-service when payments are processed`
      * - expirationDate: Instant - the date representation of when the key is set to expire
      *
      * Note: keyId, KeyState, currentVersion, permissions, and dependencies are all instantiated
      * internally and are provided by the platform.
      */
    def create(
                keyName: String,
                keyOwner: String,
                keyType: KeyType,
                algorithm: KeyAlgorithm,
                description: String,
                expirationDate: Instant,
              ): ManagedKey =

        val now = Instant.now()
        ManagedKey(
            keyId=UUID.randomUUID(),
            keyType=keyType,
            keyState=KeyState.PendingApproval,
            currentVersion=KeyVersion.createVersion(
                previousVersion=None,
                createdAt=now,
                keyState=KeyState.PendingApproval
            ),
            keyName=keyName,
            metadata=KeyMetadata.createManagedKey(
                owner=keyOwner,
                algorithm=algorithm,
                description=description,
                createdDate=now,
                expirationDate=expirationDate,
            ),
            permissions=Permissions.create(),
            dependencies=Set.empty
        )

    extension(k: ManagedKey)
        def withDependency(dependentKeyId: KeyId): ManagedKey = {
            k.copy(dependencies=k.dependencies + dependentKeyId)
        }

/** Representative of each of the two halves of the RootKeyShare with the following
  * fields:
  * - keyId: KeyId - the unique identifier of the respective key
  * - keyType: KeyType - specifically for RootKeyShare(s) is defaulted
  * to KeyType.RootKeyShare
  * - keyState: KeyState - the current state of the respective key
  * - currentVersion: KeyVersion - the current version of the respective key
  * - metadata: KeyMetadata - specifically constructed for RootKeyShare(s) through
  * KeyMetadata.createRootKeyShare
  *
  * Note: the following fields were omitted from RootKeyShare(s) and their justification:
  * - keyName - removed as keyShare location dictates which share is being accessed
  * - permissions - removed as there is no per-service access allowed; KMS will be the only
  * accessor
  * - dependencies - as the root of trust for KMS, the dependencies set would document every key
  * created, thus losing its semantic meaning
  */
case class RootKeyShare private(
                               keyId: KeyId,
                               keyType: KeyType,
                               keyState: KeyState,
                               currentVersion: KeyVersion,
                               metadata: KeyMetadata,
                               ) extends Key

object RootKeyShare:
    /** Creates a RootKeyShare derived from Key. The smart constructor requires
      * no parameters and the platform generates all required fields */
    def create(): RootKeyShare =

        RootKeyShare(
            keyId=UUID.randomUUID(),
            keyType=KeyType.RootKeyShare,
            keyState=KeyState.PendingApproval,
            currentVersion=KeyVersion.createVersion(
                previousVersion=None,
                createdAt=Instant.now(),
                keyState=KeyState.PendingApproval
            ),
            metadata=KeyMetadata.createRootKeyShare()
        )
