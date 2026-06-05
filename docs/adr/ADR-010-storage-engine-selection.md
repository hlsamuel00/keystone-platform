# ADR-010: Storage Engine Selection

## Status

Accepted

## Date Accepted

2026-06-05

## Context

Keystone Platform consists of multiple services with distinct responsibilities, data models, and access patterns. These services require persistent storage for cryptographic metadata, service relationships, authorization policies, secret version histories, certificates, and operational state.

Several storage strategies were considered. One approach was to select purpose-built storage technologies for each service based on its individual requirements. Under this model, Service Registry could utilize a graph database for dependency relationships, Secret Vault could utilize a specialized secret storage solution, and other services could adopt storage engines optimized for their specific workloads.

While technically attractive, this approach introduces significant operational complexity during the MVP phase. Multiple storage engines require separate deployment models, client libraries, backup procedures, recovery strategies, operational tooling, and domain expertise. This complexity does not directly contribute to the platform's primary learning objectives, which focus on cryptography, trust management, authorization, and platform architecture.

The architecture therefore required a storage strategy that balanced service autonomy, operational simplicity, future flexibility, and alignment with the platform's ownership boundaries.

## Alternatives Considered

### Option A: Purpose-Built Storage Per Service

Each service selects the storage engine best suited to its specific requirements.

Examples include graph databases for Service Registry, specialized secret stores for Secret Vault, and alternative storage systems for cryptographic metadata.

#### Pros

* Optimized storage model for each service
* Potential performance benefits for specialized workloads
* Maximum flexibility per service

#### Cons

* Significant operational complexity
* Multiple deployment and backup models
* Additional client libraries and tooling
* Increased cognitive overhead
* Limited value during MVP development

### Option B: Shared PostgreSQL Instance

All services utilize a single PostgreSQL server while maintaining separate databases or schemas.

#### Pros

* Simplified infrastructure footprint
* Centralized database management
* Reduced container count
* Consistent storage technology

#### Cons

* Weakens service ownership boundaries
* Shared administrative surface area
* Shared failure domain
* Shared operational lifecycle
* Reduced infrastructure-level isolation

### Option C: Dedicated PostgreSQL Instance Per Service

Each service owns and operates an independent PostgreSQL instance while standardizing on PostgreSQL as the common storage technology.

#### Pros

* Preserves service ownership boundaries
* Consistent operational model
* Strong infrastructure isolation
* Simplified local development
* Service-specific lifecycle management
* Future storage migrations become service-local decisions

#### Cons

* Increased container count
* Additional resource consumption
* More operational components than a shared database server

## Decision

Keystone Platform SHALL standardize on PostgreSQL as the storage engine for all MVP services.

Each service SHALL own and operate an independent PostgreSQL instance.

Services SHALL NOT share database instances, schemas, or administrative credentials.

The following ownership model is approved:

* Service Registry owns its PostgreSQL instance
* Security Token Service owns its PostgreSQL instance
* Key Management Service owns its PostgreSQL instance
* Secret Vault owns its PostgreSQL instance

PostgreSQL standardization SHALL apply to the storage engine only and SHALL NOT imply shared data ownership or shared datastore infrastructure.

Future storage engine migrations MAY be evaluated on a per-service basis if platform requirements justify specialization.

## Rationale

PostgreSQL provides sufficient flexibility to support the access patterns required by all MVP services.

Relational modeling supports Service Registry's dependency and ownership relationships. JSONB enables flexible metadata storage where rigid schemas provide limited value. Versioned records support secret history and rotation tracking. PostgreSQL's maturity and operational stability make it suitable for both development and production environments.

The primary objective of the MVP is validating the platform's security architecture rather than evaluating database technologies. Standardizing on a single storage engine reduces operational complexity and allows development effort to remain focused on cryptographic workflows, authorization models, trust boundaries, and service interactions.

The platform intentionally standardizes on a storage technology rather than a shared datastore.

A single PostgreSQL server with multiple databases provides logical separation but not infrastructure-level ownership isolation. A dedicated instance per service ensures that no administrative credential, process failure, operational action, or infrastructure compromise can cross service ownership boundaries at the database layer.

This decision aligns with Keystone Platform's broader architectural philosophy of ownership isolation. Just as services own their identities, cryptographic assets, policies, and operational responsibilities, they also own their persistent data stores.

The architecture further benefits from future flexibility. Because each service already owns an isolated datastore, future migration to a specialized storage engine becomes a localized decision rather than a platform-wide migration effort.

## Consequences

### Positive

* Consistent storage technology across services
* Preserves service ownership boundaries
* Strong infrastructure-level isolation
* Simplified operational model
* Reduced cognitive overhead during MVP development
* Supports future service-specific storage migrations
* Aligns with broader platform ownership principles

### Negative

* Increased infrastructure footprint
* Additional containers to manage
* Higher resource utilization than a shared database server
* More operational components during local development

### Risks

#### Service Outgrows PostgreSQL

A service may eventually require a specialized storage engine.

Mitigations:

* Service-level ownership boundaries
* Independent storage instances
* Well-understood migration patterns
* Future architectural review process

#### Operational Overhead

Multiple PostgreSQL instances increase deployment complexity.

Mitigations:

* Docker Compose standardization
* Shared operational tooling
* Common backup and recovery procedures
* Consistent monitoring approach

#### Storage Misconfiguration

Incorrect database configuration may affect service availability or security.

Mitigations:

* Infrastructure as Code
* Configuration review processes
* Standardized deployment patterns
* CI/CD validation controls

## Related Decisions

* ADR-001: KMS Owns All Cryptographic Keys
* ADR-006: Service Registry as Policy Authority and STS as Enforcement Point
* ADR-009: Language Selection
* ADR-011: CI/CD Pipeline and Review Controls
* ADR-013: Monorepo Repository Structure
