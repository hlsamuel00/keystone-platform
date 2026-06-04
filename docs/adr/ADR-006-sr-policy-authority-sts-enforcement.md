# ADR-006: Service Registry as Policy Authority and STS as Enforcement Point

## Status

Accepted

## Date Accepted

2026-06-04

## Context

Keystone Platform requires a centralized mechanism for governing service-to-service authorization. As the platform evolved, services needed a reliable method for determining whether a requesting service was authorized to communicate with a target service while maintaining consistent enforcement across the platform.

A decentralized approach in which individual services maintain their own authorization rules appears attractive due to its simplicity and local ownership. However, this approach creates policy drift, inconsistent authorization decisions, duplicated configuration, and the absence of a single source of truth for service relationships.

Another approach is to combine policy storage and enforcement within STS. While this centralizes authorization decisions, it expands STS responsibilities beyond identity validation and token issuance, creating tighter coupling between policy governance and runtime enforcement.

The architecture therefore required a model that centralizes authorization policy while maintaining clear separation between policy ownership and runtime enforcement.

## Alternatives Considered

### Option A: Service-Owned Authorization Rules

Each service maintains and enforces its own authorization rules and dependency relationships.

#### Pros

* Simple implementation
* Strong local ownership
* Reduced dependency on centralized policy management

#### Cons

* Authorization policy drift
* Inconsistent enforcement across services
* Duplicated configuration
* Difficult auditing and governance
* No authoritative source of service relationships

### Option B: STS Owns and Enforces Authorization Policy

STS stores authorization policy and performs runtime enforcement.

#### Pros

* Centralized authorization decisions
* Simplified runtime architecture
* Reduced dependency on additional services

#### Cons

* Expands STS responsibilities significantly
* Couples policy governance to runtime enforcement
* Reduces separation of concerns
* Creates a larger trust boundary

### Option C: Service Registry Owns Policy, STS Enforces Policy

Service Registry maintains authorization policy and service relationship metadata, while STS performs identity validation and runtime authorization enforcement.

#### Pros

* Centralized policy governance
* Clear separation of responsibilities
* Consistent authorization decisions
* Simplified auditing and policy management
* Supports destination-scoped authorization

#### Cons

* STS depends on Service Registry metadata
* Additional service interaction during authorization workflows
* Authorization accuracy depends on SR data quality

## Decision

Service Registry (SR) SHALL be the authoritative source of truth for service authorization policy and service relationship metadata.

SR SHALL maintain information including:

* Registered service identities
* Authorized service dependencies
* Destination authorization relationships
* Service ownership metadata
* Trust relationship metadata

Security Token Service (STS) SHALL validate service identity and enforce authorization decisions at runtime.

STS SHALL issue short-lived destination-scoped tokens based on authorization metadata maintained within SR.

Application services SHALL NOT communicate directly with SR for authorization decisions.

Application services SHALL interact with STS, which SHALL act as the runtime enforcement interface for authorization workflows.

## Rationale

Separating policy storage from policy enforcement creates clear service boundaries while maintaining centralized governance of authorization rules.

Service Registry serves as the authoritative system of record for service relationships and authorization metadata. This allows authorization policy to be managed in a single location while avoiding duplication across services or runtime components.

STS consumes this metadata to validate identities, evaluate authorization decisions, and issue short-lived destination-scoped tokens. By separating these responsibilities, the platform maintains centralized policy governance while enabling consistent runtime enforcement.

This approach also supports future auditing, dependency analysis, and policy management capabilities because all service relationships originate from a single authoritative source.

The decision aligns with Keystone Platform's broader architectural philosophy of separating ownership from execution. Similar patterns exist elsewhere in the platform, including the KMS/CSP relationship defined in ADR-004, where ownership of cryptographic assets is separated from execution of cryptographic operations.

## Consequences

### Positive

* Establishes a single source of truth for authorization policy
* Provides consistent runtime authorization decisions
* Reduces policy drift across services
* Simplifies auditing and governance
* Enables destination-scoped authorization tokens
* Maintains clear separation of responsibilities

### Negative

* Introduces dependency between STS and SR
* Authorization decisions depend on SR metadata accuracy
* Requires synchronization between policy changes and enforcement

### Risks

#### Stale or Incorrect Policy Data

Incorrect service relationship metadata may result in authorization failures or unintended access.

Mitigations:

* Administrative approval workflows
* Policy validation controls
* Audit logging
* Change review procedures

#### STS Availability Issues

Authorization workflows may be impacted during STS outages.

Mitigations:

* Horizontal scaling
* Short-lived token caching
* Future high-availability architecture

#### Unauthorized Policy Modification

Modification of service relationship metadata may affect authorization outcomes.

Mitigations:

* Controlled administrative access
* Audit logging
* Policy review processes
* Change management controls

## Related Decisions

* ADR-003: Platform-Managed Service Identity Keys
* ADR-004: Separate KMS and CSP Responsibilities
* ADR-007: Token-Based Runtime Authorization
