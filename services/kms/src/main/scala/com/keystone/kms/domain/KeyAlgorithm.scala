package com.keystone.kms.domain

/** Representative of all the key algorithms supported by KMS. This list is not exhaustive
  * of all possible key algorithms and is intended to cover the key algorithms used currently by
  * Keystone services. Each key algorithm is assigned a keySize property that coincide with it's algorithm
  * ensuring that keys are generated with the appropriate level of security for their intended
  * use case. The addition of new key algorithms MUST be justified by a specific use case and approved
  * before being added to the codebase. When new algorithms are added to the code base, ensure that it follows
  * established naming convention [Algorithm Family][Key Size in Bits][Mode of Operation] as outlined below.
  */
sealed trait KeyAlgorithm:
    def keySizeBits: Int

object KeyAlgorithm:
    /** This key algorithm is supported for all encryption and decryption operations and is the default algorithm for
      * encryption/decryption cryptographic operations within the platform.
      */
    case object AES256GCM extends KeyAlgorithm:
        val keySizeBits = 256

    /** This key algorithm is supported for all signing and verification operations and is the preferred algorithm for 
      * signature/verification cryptographic operations within the platform.
      */
    case object Ed25519Fixed extends KeyAlgorithm:
        val keySizeBits = 256

    /** This key algorithm is supported for signing and verification operations and is an accepted alternative when
      * Ed25519Fixed is not available for use for signature/verification cryptographic operations within the platform.
      */
    case object ECP256 extends KeyAlgorithm:
        val keySizeBits = 256