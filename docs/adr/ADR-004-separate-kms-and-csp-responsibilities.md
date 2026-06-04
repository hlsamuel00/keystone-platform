# ADR-004: Separate KMS and CSP Responsibilities

## Status

Accepted

## Date Accepted

2026-06-04

## Context

As Keystone Platform evolved, KMS emerged as the authoritative owner of all cryptographic assets. This naturally led to a broader architectural question: should KMS also perform cryptographic operations, or should ownership and execution be separated into distinct platform responsibilities?

At first glance, combining these responsibilities appears attractive. KMS already governs keys, enforces policy, manages lifecycle operations, and serves as the authoritative source of truth for cryptographic assets. Extending KMS to perform encryption, decryption, signing, verification, and other cryptographic functions would simplify deployment and reduce the number of platform services.

A common industry approach in which cryptographic operations are performed through shared application libraries was also evaluated. Under this model, services retrieve authorized key material from KMS and perform cryptographic operations locally. This approach provides implementation consistency through shared libraries while avoiding an additional network dependency.

As the architecture evolved, a consistent platform philosophy emerged. Keystone Platform treats security capabilities as platform concerns rather than application concerns. Identity, key management, secret management, and policy enforcement are centralized within dedicated platform services. The remaining question was whether cryptographic execution itself should follow the same model.

## Alternatives Considered

### Option A: KMS Performs Cryptographic Operations

KMS owns cryptographic assets and performs all encryption, decryption, signing, verification, hashing, and related operations.

#### Pros

* Simple deployment model
* Fewer platform services
* Reduced network communication
* Centralized governance

#### Cons

* Expands KMS responsibilities significantly
* Creates a larger trust boundary
* Increases operational complexity within KMS
* Makes independent scaling more difficult
* Risks turning KMS into a platform "god service"

### Option B: Shared Cryptographic Library

Services retrieve authorized key material and perform cryptographic operations locally through a standardized platform library.

#### Pros

* Consistent implementation across services
* Lower runtime latency
* Simplified deployment
* Familiar industry pattern

#### Cons

* Cryptographic execution becomes an application responsibility
* Plaintext key material may exist within many service boundaries
* Increases the number of systems trusted to handle sensitive material
* Requires service owners to maintain library versions and upgrades
* Weakens platform-level ownership of cryptographic execution

### Option C: Dedicated Crypto Service Provider (CSP)

KMS owns cryptographic assets while CSP performs cryptographic operations on behalf of services.

#### Pros

* Clear separation of responsibilities
* Centralized cryptographic execution
* Reduced exposure of key material across the platform
* Independent scaling characteristics
* Consistent implementation and governance
* Aligns with platform-first security architecture

#### Cons

* Additional service dependency
* Additional network hop for cryptographic operations
* Increased operational complexity

## Decision

Keystone Platform SHALL separate cryptographic ownership from cryptographic execution.

KMS SHALL remain the authoritative owner of all cryptographic assets and SHALL be responsible for key generation, storage, lifecycle management, policy enforcement, auditing, rotation, recovery, and governance.

CSP SHALL perform cryptographic operations including encryption, decryption, signing, verification, hashing, and secure random generation on behalf of platform services.

CSP MAY temporarily access plaintext key material and plaintext secret material when required to perform an approved operation.

CSP SHALL NOT persist cryptographic keys, plaintext secrets, ciphertext payloads, tokens, or other sensitive material beyond the scope of the active operation.

Sensitive material SHALL be discarded immediately following operation completion.

## Rationale

The selected architecture reflects Keystone Platform's broader architectural philosophy that security capabilities should be owned and governed by the platform rather than individual applications.

A shared library can provide implementation consistency, but it cannot provide the same architectural boundary as a dedicated service. Under a library-based model, cryptographic execution becomes a responsibility of each consuming service. This increases the number of trust boundaries that handle sensitive material and distributes cryptographic execution throughout the platform.

By centralizing cryptographic execution within CSP, Keystone Platform reduces the number of components that require access to sensitive cryptographic material. Services consume cryptographic capabilities through a well-defined platform interface rather than directly performing cryptographic operations themselves.

Separating ownership from execution also creates clearer service boundaries. KMS governs cryptographic assets while CSP consumes those assets to perform approved operations. This mirrors other architectural patterns within Keystone Platform, where policy storage is separated from policy enforcement and ownership is separated from execution whenever practical.

Although this design introduces an additional service dependency and network hop, the architecture prioritizes governance, trust boundary reduction, auditability, and platform consistency over minimizing operational complexity.

## Consequences

### Positive

* Establishes clear separation between ownership and execution
* Prevents KMS from accumulating excessive responsibilities
* Reduces the number of trust boundaries handling sensitive material
* Centralizes cryptographic execution
* Enables independent scaling of cryptographic workloads
* Creates a consistent platform-wide cryptographic interface
* Aligns with the platform's security-first architecture

### Negative

* Introduces an additional service dependency
* Adds network latency to cryptographic operations
* Increases deployment and operational complexity
* Creates another Tier-1 platform service requiring monitoring and support

### Risks

#### CSP Compromise

An attacker may gain access to sensitive material actively being processed.

Mitigations:

* No persistence of sensitive material
* Ephemeral operation scope
* Comprehensive audit logging
* Strong service authentication and authorization controls

#### CSP Availability Issues

Cryptographic operations may be unavailable during outages.

Mitigations:

* Horizontal scaling
* Future high-availability architecture
* Operational recovery procedures

#### Increased Platform Complexity

Introducing a dedicated cryptographic service increases architectural complexity.

Mitigations:

* Clear ownership boundaries
* Well-defined service contracts
* Independent scalability and operational responsibilities

## Related Decisions

* ADR-001: KMS Owns All Cryptographic Keys
* ADR-002: Split Root Key Trust
* ADR-003: Platform-Managed Service Identity Keys
* ADR-005: Envelope Encryption
* ADR-006: Service Registry as Policy Authority and STS as Enforcement Point
