package com.keystone.kms.domain

/** Representative of all the key sizes supported by KMS. This list is not exhaustive
  * of all possible key sizes and is intended to cover the key sizes used currently by
  * Keystone services. Each key size is associated with a specific cryptographic algorithm
  * ensuring that keys are generated with the appropriate level of security for their intended
  * use case. The addition of new key sizes MUST be justified by a specific use case and approved
  * before being added to the codebase.
  */
sealed trait KeySize

object KeySize:
    /** This key size is supported for all encryption and decryption operations and is the default algorithm for
      * encryption/decryption cryptographic operations within the platform.
      */
    case object AES256GCM extends KeySize

    /** This key size is supported for all signing and verification operations and is the preferred algorithm for 
      * signature/verification cryptographic operations within the platform.
      */
    case object Ed25519Fixed extends KeySize

    /** This key size is supported for signing and verification operations and is an accepted alternative when
      * Ed25519Fixed is not available for use for signature/verification cryptographic operations within the platform.
      */
    case object ECP256 extends KeySize 