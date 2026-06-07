# Keystone Platform

A security-focused cryptographic platform providing machine
identity, centralized key management, secret management,
cryptographic operations, and secure service-to-service
communication for internal platform services.

Keystone Platform grew from hands-on experience with
production cryptographic infrastructure in financial services
— and a desire to understand the internals deeply rather than
just consume them. The architecture treats key ownership,
lifecycle management, auditability, and blast-radius reduction
as first-class concerns from the ground up.

Keystone Platform is built on the premise that security
infrastructure requires deliberate architecture before
implementation. Every significant decision is documented,
challenged, and traceable — not because the process is the
product, but because the reasoning behind security-critical
systems matters as much as the systems themselves.

---

## Design Principles

As the architecture evolved, a set of principles emerged
that shaped every major decision:

- **Explicit ownership** — every security-critical resource
  has a single authoritative owner. Shared ownership is
  treated as a design smell.
- **Separation of ownership and execution** — services that
  own assets do not perform operations on them, and services
  that perform operations do not own assets.
- **Defense in depth** — no single control is sufficient.
  Controls are layered so that compromise of one component
  does not compromise the platform.
- **Auditability and governance by default** — decisions,
  operations, and lifecycle events are attributable and
  reviewable by design, not as an afterthought.

---

## Platform Services

| Service | Abbreviation | Owns |
|---|---|---|
| Service Registry | SR | Service metadata, namespaces, ownership, risk tiers, allowed dependencies, communication policy |
| Security Token Service | STS | Machine identity, certificate lifecycle, revocation, token issuance, trust enforcement |
| Secret Vault | SV | Secret lifecycle, versioning, leasing, rotation, rollback, retrieval |
| Crypto Service Provider | CSP | Encrypt, decrypt, sign, verify, hash, secure random generation — execution only, no asset ownership |
| Key Management Service | KMS | All key material, key lifecycle, key policies, key versions, key dependencies |

Platform services communicate over TLS 1.3 with
STS-issued destination-scoped tokens. Sensitive event
payloads are signed and encrypted at the application
layer before reaching the Event Bus.

---

## Repository Structure

```
keystone-platform/
├── docs/
│   ├── design/       # Platform architecture document
│   └── adr/          # Architecture Decision Records
├── services/
│   ├── kms/          # Key Management Service (Scala)
│   ├── csp/          # Crypto Service Provider (Scala)
│   ├── sr/           # Service Registry (Python)
│   ├── sts/          # Security Token Service (TBD)
│   └── sv/           # Secret Vault (TBD)
├── docker-compose.yml
├── .github/
│   └── workflows/    # CI/CD and security review pipelines
└── README.md
```

Each service owns its source, tests, docs, and Dockerfile
independently. Per-service documentation lives in
`services/<name>/docs/service.md` and operational
procedures in `services/<name>/docs/runbook.md`.

---

## Architecture and Design Decisions

Architecture docs live in [`docs/design/`](docs/design/).

Every significant decision is captured as an Architecture
Decision Record in [`docs/adr/`](docs/adr/) — including the
problem context, alternatives considered, what was decided,
why, and what tradeoffs were accepted. The rejected
alternatives are often the most useful part.

| ADR | Decision |
|---|---|
| [ADR-001](docs/adr/ADR-001-kms-owns-all-keys.md) | KMS Owns All Cryptographic Keys |
| [ADR-002](docs/adr/ADR-002-split-root-key-trust.md) | Split Root Key Trust |
| [ADR-003](docs/adr/ADR-003-platform-managed-service-identity-keys.md) | Platform-Managed Service Identity Keys |
| [ADR-004](docs/adr/ADR-004-separate-kms-and-csp-responsibilities.md) | Separate KMS and CSP Responsibilities |
| [ADR-005](docs/adr/ADR-005-envelope-encryption-for-secrets.md) | Envelope Encryption for Secrets |
| [ADR-006](docs/adr/ADR-006-sr-policy-authority-sts-enforcement.md) | SR as Policy Authority, STS as Enforcement Point |
| [ADR-007](docs/adr/ADR-007-token-based-runtime-authorization.md) | Token-Based Runtime Authorization |
| [ADR-008](docs/adr/ADR-008-event-payload-encryption-and-signing-beyond-tls.md) | Event Payload Encryption and Signing Beyond TLS |
| [ADR-009](docs/adr/ADR-009-language-selection.md) | Language Selection |
| [ADR-010](docs/adr/ADR-010-storage-engine-selection.md) | Storage Engine Selection |
| [ADR-011](docs/adr/ADR-011-cicd-pipeline-and-review-controls.md) | CI/CD Pipeline and Review Controls |
| [ADR-012](docs/adr/ADR-012-ai-assisted-code-review.md) | AI-Assisted Code Review |
| [ADR-013](docs/adr/ADR-013-monorepo-repository-structure.md) | Monorepo Repository Structure |

---

## Local Development

**Prerequisites**
- Docker and Docker Compose
- Scala / sbt (KMS and CSP)
- Python 3.11+ (SR)

**Running the platform locally**

```bash
docker-compose up
```

> Setup instructions will be fleshed out as services
> are implemented. Per-service setup lives in
> `services/<name>/docs/service.md`.

---

## Project Status

The architecture and all major design decisions are
documented and stable. Implementation is in active planning
with KMS and CSP as the initial targets.

| Component | Status |
|---|---|
| Platform Architecture | Complete |
| Architecture Decision Records | Complete — ADR-001 through ADR-013 |
| KMS | Planned |
| CSP | Planned |
| SR | Planned |
| STS | Planned |
| SV | Planned |
| CI/CD Pipeline | Active |
