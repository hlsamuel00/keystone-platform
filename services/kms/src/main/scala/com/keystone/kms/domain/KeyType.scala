package com.keystone.kms.domain

/** Representative of all key types supported by KMS. Each type is associated with a
  * specific function of the key and the corresponding cryptographic operations that can
  * be performed using that key. This allows for fine-grained access control and ensures
  * that keys are used in accordance with their intended purpose, reducing risk of misuse
  * and enhancing security.
  */
sealed trait KeyType

object KeyType:
    /** This key type is intended for wrap(encrypting) and unwrap(decrypting) data encryption keys (DEKs). 
      * The purpose of this key is to securely manage DEKs, which are used for encrypting data at rest. By
      * using a key encryption key (KEK) to wrap DEKs, we can ensure that DEKs are protected and can only be
      * accessed by authorized entities with the appropriate permissions.
      */
    case object KEK extends KeyType

    /** This key type is intended for performing encryption and decryption operations directly on data at rest.
      * The purpose of this key is to ensure that data at rest is protected and can only be accessed by authorized
      * entities with the appropriate permissions. This key type is commonly used for encrypting sensitive data stored
      * in databases, file systems, or other storage mediums, providing an additional layer of security for data at rest.
      */
    case object DEK extends KeyType

    /** This key type is intended for performing encryption and decryption operations directly on data in transit.
      * The purpose of this key is to ensure that data in transit is protected and can only be accessed by authorized
      * entities with the appropriate permissions. This key type is commonly used for encrypting sensitive data transmitted
      * over networks, published to topics, or transmitted through other communication channels, providing an additional 
      * layer of security for data in transit.
      */
    case object TransmissionKey extends KeyType

    /** This key type is intended for performing digital signing and signature verification operations. The purpose of this
      * key is to ensure that data being transmitted can be verified for authenticity and integrity, providing assurance to 
      * recipients that the data has not been tampered with and is from a trusted source. This key type is commonly used for
      * signing sensitive data transmitted over networks, published to topics, or transmitted through other communication channels,
      * providing an additional layer of security for data in transit. This key type is asymmetric in nature, as the signing and
      * verification operations typically involve a private key for signing and a public key for verification. This allows for secure
      * digital signature management and verification without exposing the private key to unauthorized entities.
      */
    case object SigningKey extends KeyType

    /** This key type is intended for use only by the Security Token Service (STS) to sign certificates for TLS mutual authentication.
      * The purpose of this key is to ensure that the STS can securely sign certificates used for TLS mutual authentication, providing assurance to
      * services that the certificates they receive for TLS authentication are valid and can be trusted. This key type is asymmetric in nature,
      * as the signing and verification operations typically involve a private key for signing and a public key for verification, allowing
      * for secure certificate signing and verification without exposing the private key to unauthorized entities. This key type is critical 
      * for ensuring the security of TLS communications between services. 
      */
    case object CertificateAuthorityKey extends KeyType

    /** This key type is intended for use solely during service onboarding and is used to identify the service. The purpose
      * of this key is to represent the service's cryptographic identity, allowing the service to attest to its identity, 
      * establishing trust with other services, and enabling permission management based on the service's identity. This key
      * type is critical for ensuring that services can securely identify themselves and establish trust relationships. This key
      * type is asymmetric in nature, as the signing and verification operations typically involve a private key for signing and a
      * public key for verification. This allows for secure identity management and verification without exposing the private key to
      * unauthorized entities. This key type is not intended for general cryptographic operations and should be used exclusively for 
      * service identity management. 
      */
    case object ServiceIdentityKey extends KeyType

    /** This key type is solely intended for one of two independent shares of the platform root key. The purpose of this key is to ensure
      * that the platform root key is securely managed and protected. Neither share of the platform root key is sufficient alone and their sole
      * purpose is to be combined to reconstruct the platform root key. The key shares will never be stored together, and construction
      * will only occur in memory when performing operations and immediately discarded after use. This key type is critical for ensuring the security 
      * of the platform root key, which is the basis for all trust in cryptographic operations within the platform. 
      */
    case object RootKeyShare extends KeyType
