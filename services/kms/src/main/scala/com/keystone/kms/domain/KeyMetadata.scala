package com.keystone.kms.domain

import com.keystone.kms.domain.KeyAlgorithm.AES256GCM

import java.time.Instant
/** Representative of the metadata of keys utilized in KMS. This list is
  * not exhaustive of all relevant metadata for a key, but representative of
  * what is currently needed for keys used in KMS. Additions to key metadata
  * can be processed with a justified need and use case. The metadata for keys is
  * as follows:
  * - owner: String - the client ID owning service of the key.
  * - algorithm: KeyAlgorithm - the algorithm supported by this
  * key which has a derived propery containing the expected key size in bits.
  * - description: String - a description of the keys purpose provided by the provisioner
  * allowing any subsequent reviewers to understand the usage of the key with minimal ambiguity.
  * - createdDate: Instant - a timestamp of when the key was created
  * - expirationDate: Option[Instant] - the timestamp of when the key is set to expire. There are some
  * keys owned by KMS that do not expire making this field optional, but must be completed for keys managed
  * through KMS
  * - lastRotatedDate: Option[Instant] - the timestamp of when a key was last rotated. Newly created keys will
  * not have a date to populate in this field, thus making this field optional.
  */
case class KeyMetadata private (
                      owner: String,
                      algorithm: KeyAlgorithm,
                      description: String,
                      createdDate: Instant,
                      expirationDate: Option[Instant],
                      lastRotatedDate: Option[Instant])

/** Note: direct construction via KeyMetadata(...) is prevented by the private
  * constructor - enforced at compile time, not runtime. Attempting KeyMetadata(...)
  * will not compile; the companion object constructor must be used.
  */
object KeyMetadata:
    def createManagedKey(
                        owner: String,
                        algorithm: KeyAlgorithm,
                        description: String,
                        createdDate: Instant,
                        expirationDate: Instant): KeyMetadata =

        KeyMetadata(
            owner=owner,
            algorithm=algorithm,
            description=description,
            createdDate=createdDate,
            expirationDate=Some(expirationDate),                      // wrapped internally
            lastRotatedDate=None)                                    // new key; no rotation yet

    /** Creates metadata for a root key share. Root key shares are intentionally
      * unnamed — share identity (A or B) is determined by retrieval location.
      * Share A resides in the Operations Engine. Share B resides in the Vault.
      * Neither share is labeled independently to prevent mislabeling and reduce
      * the risk of share misuse.
      */
    def createRootKeyShare(): KeyMetadata =
        KeyMetadata(
            owner="KMS",
            algorithm=AES256GCM,
            description="A share of the rootKey for KMS; unable to be used independently",
            createdDate=Instant.now(),
            expirationDate=None,
            lastRotatedDate=None)

    extension (m: KeyMetadata)
        def withRotation(rotatedDate: Instant, newExpirationDate: Instant): KeyMetadata =
            m.copy(
                lastRotatedDate=Some(rotatedDate),
                expirationDate=Some(newExpirationDate))
        
        def withRootKeyShareRotation(rotatedDate: Instant): KeyMetadata =
            m.copy(lastRotatedDate=Some(rotatedDate))