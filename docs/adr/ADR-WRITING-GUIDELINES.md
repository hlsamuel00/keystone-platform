# ADR Writing Guidelines

## Voice

Write from the perspective of the architecture.

Preferred:

- Keystone Platform requires...
- The architecture adopts...
- The platform treats...

Avoid:

- We decided...
- The architecture team decided...
- The security team determined...

## Decision Language

Use normative language when documenting architecture.

Examples:

- KMS SHALL own all cryptographic keys.
- CSP SHALL NOT persist sensitive material.
- Services MAY request cryptographic operations.

## Alternatives

Every ADR should document alternatives that were considered and rejected.

The rejected alternatives are often more valuable than the selected option.

## Tradeoffs

Document both benefits and costs honestly.

Avoid advocacy.

## Audience

ADRs should be understandable to engineers unfamiliar with Keystone Platform while remaining concise enough for architecture review.

## Goal

ADRs exist to preserve architectural reasoning, not implementation details.