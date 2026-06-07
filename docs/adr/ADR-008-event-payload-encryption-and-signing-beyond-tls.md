# ADR-008: Event Payload Encryption and Signing Beyond TLS

## Status

Accepted

## Date Accepted

2026-06-05

## Context

Keystone Platform supports asynchronous communication through an event-driven architecture. Events may contain operational metadata, business information, personally identifiable information (PII), or other sensitive data required by downstream services.

Transport Layer Security (TLS) protects data while it is transmitted between systems. However, event-driven systems introduce additional security considerations because event payloads often persist beyond the lifetime of a transport connection. Messages may be queued, retried, replayed, archived, dead-lettered, or stored by intermediary infrastructure before reaching their intended destination.

As a result, protecting only the transport channel does not fully protect the event data itself.

Additionally, encryption alone does not guarantee that an event originated from the expected service or that the payload remained unchanged throughout its lifecycle. Consumers must also be able to verify event authenticity and integrity before processing sensitive information.

The platform therefore required a mechanism that protects sensitive event payloads independently of the transport layer while providing cryptographic guarantees regarding both payload confidentiality and origin authenticity.

## Alternatives Considered

### Option A: TLS-Only Protection

Event payloads rely exclusively on TLS for protection.

#### Pros

* Simple implementation
* Minimal cryptographic overhead
* Widely adopted industry practice

#### Cons

* Payloads are exposed after transport termination
* Sensitive data may be visible in queues or storage systems
* No protection against payload modification after publication
* No proof of event origin
* No end-to-end payload protection

### Option B: Service-Owned Encryption and Signing

Individual services define and implement their own encryption and signing strategies.

#### Pros

* Flexible implementation
* Service-level ownership

#### Cons

* Inconsistent cryptographic standards
* Fragmented key management
* Difficult auditing and governance
* Increased implementation burden
* Higher likelihood of security drift

### Option C: Platform-Managed Encryption and Signing

Sensitive event payloads are encrypted and signed using platform-managed cryptographic controls prior to publication.

#### Pros

* End-to-end payload protection
* Centralized governance
* Consistent cryptographic standards
* Reduced exposure within event infrastructure
* Cryptographic verification of event origin
* Payload integrity protection
* Aligns with KMS ownership model

#### Cons

* Increased implementation complexity
* Additional cryptographic operations
* Key retrieval and verification overhead

## Decision

Keystone Platform SHALL protect sensitive event payloads using both encryption and digital signatures.

TLS SHALL continue to protect data in transit.

Sensitive event payloads SHALL be encrypted prior to publication and decrypted only by authorized destination services.

Sensitive event payloads SHALL be digitally signed prior to publication and verified before processing by consuming services.

Transmission keys SHALL be owned and governed by KMS.

Signing keys SHALL be owned and governed by KMS.

Cryptographic operations associated with event encryption, decryption, signing, and signature verification SHALL be performed through CSP.

Payload protection SHALL support:

* Confidentiality
* Integrity
* Authenticity
* Auditability
* Key rotation

Services SHALL NOT independently manage transmission key or signing key lifecycle operations.

## Rationale

TLS protects communication channels but does not protect event payloads after transport has completed. Event-driven systems introduce storage, replay, retry, archival, and infrastructure concerns that extend beyond the scope of transport-layer protections.

Encrypting event payloads provides confidentiality regardless of where the message resides within the event lifecycle. Queues, dead-letter stores, archives, and intermediary infrastructure may store encrypted data without exposing the underlying payload contents.

However, confidentiality alone is insufficient. Consumers must also verify that an event originated from the expected service and that the payload has not been modified after publication. Digital signatures provide these guarantees by allowing consumers to cryptographically validate both event origin and payload integrity.

By combining encryption with digital signatures, the platform provides defense-in-depth protections for sensitive event data. Encryption protects the payload from unauthorized disclosure, while signing protects against forgery and tampering.

Centralizing ownership of transmission and signing keys within KMS ensures consistent governance, lifecycle management, rotation, and auditing. Performing cryptographic operations through CSP maintains the platform's established separation between cryptographic ownership and cryptographic execution.

This decision aligns with Keystone Platform's broader philosophy of defense in depth. Transport security protects communication channels, payload encryption protects confidentiality, and digital signatures protect integrity and authenticity.

## Consequences

### Positive

* Protects sensitive event data beyond transport boundaries
* Provides cryptographic proof of event origin
* Detects unauthorized payload modification
* Reduces exposure within event infrastructure
* Enables centralized governance of transmission and signing keys
* Supports consistent cryptographic standards
* Provides defense-in-depth protections
* Aligns with existing KMS/CSP architecture

### Negative

* Introduces additional cryptographic overhead
* Requires key retrieval during event processing
* Increases operational complexity
* Requires signature verification by consumers
* Adds signing key lifecycle management requirements

### Risks

#### Transmission Key Compromise

A compromised transmission key may expose encrypted event payloads protected by that key.

Mitigations:

* Centralized lifecycle management
* Key rotation procedures
* Audit logging
* Limited key scope

#### Signing Key Compromise

A compromised signing key may allow unauthorized event generation.

Mitigations:

* Centralized governance through KMS
* Key rotation procedures
* Audit logging
* Service identity validation
* Signature verification controls

#### Decryption or Verification Failures

Consumers may be unable to process events if required cryptographic material is unavailable.

Mitigations:

* High-availability KMS architecture
* Operational recovery procedures
* Retry mechanisms

#### Performance Impact

Encryption, decryption, signing, and verification introduce additional processing overhead.

Mitigations:

* Efficient cryptographic algorithms
* Horizontal scaling
* Performance monitoring

## Related Decisions

* ADR-001: KMS Owns All Cryptographic Keys
* ADR-003: Platform-Managed Service Identity Keys
* ADR-004: Separate KMS and CSP Responsibilities
* ADR-006: Service Registry as Policy Authority and STS as Enforcement Point
* ADR-007: Token-Based Runtime Authorization
