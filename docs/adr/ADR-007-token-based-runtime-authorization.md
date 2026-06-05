# ADR-007: Token-Based Runtime Authorization

## Status

Accepted

## Date Accepted

2026-06-04

## Context

Service-to-service communication requires a mechanism for validating both identity and authorization at runtime. As Keystone Platform evolved, it became necessary to determine how services would prove their identity and demonstrate authorization when communicating with other services.

Long-lived credentials provide a simple mechanism for authentication but create significant security concerns. Compromised credentials remain valid for extended periods, increasing blast radius and complicating revocation. Additionally, long-lived credentials provide limited context regarding the specific action being performed or the intended destination.

A second approach is to authenticate service identity while relying on destination services to independently evaluate authorization. While this model proves who a service is, it distributes authorization decisions throughout the platform and creates opportunities for inconsistent enforcement.

The platform therefore required a mechanism that centralizes authorization decisions while providing destination services with verifiable runtime authorization information.

## Alternatives Considered

### Option A: Long-Lived Service Credentials

Services authenticate using long-lived credentials or certificates and communicate directly with one another.

#### Pros

* Simple implementation
* Minimal runtime overhead
* Reduced dependency on token issuance

#### Cons

* Large blast radius when credentials are compromised
* Difficult revocation workflows
* Limited authorization context
* Encourages excessive trust between services

### Option B: Identity-Only Authentication

Services authenticate their identity while destination services independently determine authorization.

#### Pros

* Clear identity verification
* Reduced token complexity
* Flexible service-specific authorization

#### Cons

* Distributed authorization logic
* Inconsistent enforcement
* Increased implementation burden across services
* Difficult auditing and governance

### Option C: Short-Lived Destination-Scoped Authorization Tokens

STS validates service identity, evaluates authorization policy, and issues short-lived tokens containing authorization context.

#### Pros

* Reduced blast radius
* Centralized authorization decisions
* Consistent enforcement model
* Fine-grained authorization
* Simplified auditing

#### Cons

* Increased dependency on STS
* Additional token lifecycle management
* Higher runtime issuance volume

## Decision

Keystone Platform SHALL use short-lived destination-scoped tokens as the standard authorization mechanism for service-to-service communication.

Security Token Service (STS) SHALL issue tokens only after validating service identity and evaluating authorization policy maintained by Service Registry.

Tokens SHALL contain authorization context sufficient for runtime validation, including:

* Issuer (`iss`)
* Subject (`sub`)
* Audience (`aud`)
* Scope (`scope`)
* Risk Tier (`risk_tier`)
* Issued At (`iat`)
* Expiration (`exp`)

Tokens SHALL be destination-scoped and SHALL NOT be reusable across multiple destination services.

Tokens SHALL be short-lived by design.

Authorization decisions SHALL be based on:

* Requesting service identity
* Authorized destination service
* Requested operation scope
* Applicable policy constraints

## Rationale

Authorization is more than proving identity. A service must demonstrate not only who it is, but also where it intends to communicate and what operations it is authorized to perform.

Short-lived destination-scoped tokens allow STS to evaluate authorization centrally and issue a verifiable authorization artifact representing that decision. The token serves as evidence that identity validation, policy evaluation, destination authorization, and scope validation have already occurred.

Including destination and scope information within the token enables fine-grained authorization while reducing reliance on implicit trust relationships between services. A token issued for one destination cannot be reused against another destination, and authorization may be constrained to specific operations rather than broad service access.

Short-lived credentials also reduce exposure windows. Compromised tokens naturally expire, reducing the operational burden associated with credential revocation and limiting the impact of credential theft.

This decision complements ADR-006 by providing the runtime mechanism through which authorization decisions are enforced. Service Registry remains the authoritative source of authorization policy, while STS issues tokens representing the outcome of those policy evaluations.

## Consequences

### Positive

* Reduces blast radius of credential compromise
* Centralizes authorization decisions
* Supports fine-grained authorization
* Enables destination-scoped access control
* Simplifies auditing of authorization activity
* Provides consistent runtime authorization behavior

### Negative

* Increases dependency on STS availability
* Requires token lifecycle management
* Introduces runtime token issuance overhead
* Requires services to validate authorization tokens

### Risks

#### Token Theft

A stolen token may be used until expiration.

Mitigations:

* Short token lifetimes
* Destination-scoped authorization
* Scope-based restrictions
* Audit logging

#### STS Availability Issues

Authorization workflows may be impacted during STS outages.

Mitigations:

* Horizontal scaling
* Token caching strategies
* High-availability deployment architecture

#### Excessively Broad Authorization Scopes

Improper scope definitions may grant more access than intended.

Mitigations:

* Centralized policy governance
* Administrative review processes
* Audit logging
* Principle of least privilege

## Related Decisions

* ADR-003: Platform-Managed Service Identity Keys
* ADR-006: Service Registry as Policy Authority and STS as Enforcement Point
* ADR-008: Event Payload Encryption Beyond TLS
