# Contributing to Keystone Platform

Keystone Platform is a security-focused cryptographic platform
built around explicit ownership boundaries, auditability, and
deliberate architecture. These contribution guidelines exist to
ensure that every change to the platform reflects the same
discipline as the platform itself.

---

## Branch Naming

Branches should follow the same type convention as commit
messages. Use the format:

```
<type>/<short-description>
```

Examples:
```
feat/kms-key-state-machine
fix/security-review-workflow
docs/sr-service-runbook
adr/event-bus-selection
ci/add-path-based-triggers
chore/update-gitattributes
refactor/kms-operations-engine
test/kms-lifecycle-unit-tests
```

Branch names are not enforced automatically but should be
consistent with the type of change being made. The PR title
is the enforced convention — see Pull Requests below.

---

## Commit Messages

Keystone Platform uses
[Conventional Commits](https://www.conventionalcommits.org/).
Every commit message must follow this format:

```
<type>(<scope>): <description>
```

Scope is optional but encouraged when the change is specific
to a service or component.

### Valid Types

| Type | When to use |
|---|---|
| `feat` | New feature or capability |
| `fix` | Bug or error correction |
| `docs` | Service documentation, runbooks, README |
| `adr` | Architecture Decision Records |
| `ci` | CI/CD pipeline or workflow changes |
| `chore` | Housekeeping, configuration, tooling |
| `refactor` | Code restructure without behavior change |
| `test` | Test additions or updates |

### Examples

```
feat(kms): add key state machine implementation
fix(ci): update Claude model string in security review workflow
docs(sr): update service runbook with health check procedures
adr(kms): add key lifecycle state machine decision
ci: add PR title convention check
chore: update .gitattributes to enforce LF line endings
refactor(kms): extract policy engine into separate component
test(kms): add unit tests for key state transitions
```

### What Not to Do

```
updated the model string          ← no type prefix
fix/ci update model string        ← wrong format
Fixed things                      ← no type, vague description
WIP                               ← not a meaningful commit
```

---

## Pull Requests

### Scope

PRs should be scoped to a single service and a single
feature or fix. Cross-service changes are rare and should
be discussed before opening a PR.

### Title

PR titles are automatically validated as a required CI check.
Titles must follow the same conventional commit format:

```
<type>(<scope>): <description>
```

Scope is optional. Both of the following are valid:

```
fix: update Claude model string in security review workflow
fix(ci): update Claude model string in security review workflow
```

A PR title that does not match the convention will fail the
`PR Title Check` gate and cannot be merged.

### Description

Use the PR template provided. The security checklist is not
optional — every PR touching platform services must have the
checklist completed before review.

### Review Requirements

All of the following must pass before a PR can be merged:

| Check | Tool | Scope |
|---|---|---|
| PR Title Check | GitHub Actions | Every PR |
| Secret Scanning | TruffleHog + GitGuardian | Every PR |
| Service Tests | GitHub Actions | Changed service only |
| General Code Review | CodeRabbit | Every PR |
| Security Domain Review | Claude (Anthropic API) | Every PR |
| Human Approval | Repository owner | Every PR |

---

## ADR Process

Every significant architectural decision must be captured as
an Architecture Decision Record before implementation begins.

### When to Write an ADR

Write an ADR when:
- A decision has meaningful alternatives that were considered
- The decision has lasting implications on architecture,
  security posture, or operational model
- A future engineer would reasonably ask "why was this done
  this way?"

Do not write an ADR for:
- Implementation details covered by an existing ADR
- Style or formatting preferences
- Decisions that are obviously correct and have no meaningful
  alternatives

### How to Write an ADR

1. Copy `docs/adr/ADR-TEMPLATE.md`
2. Follow `docs/adr/ADR-WRITING-GUIDELINES.md`
3. Number sequentially from the last existing ADR
4. Use normative language — SHALL, SHALL NOT, MAY, SHOULD
5. Document rejected alternatives — they are often more
   valuable than the selected option
6. Open a PR using the `adr:` prefix
7. ADRs are reviewed like code — they must pass all checks
   before merging

### ADR Status Values

| Status | Meaning |
|---|---|
| `Accepted` | Active decision |
| `Superseded by ADR-XXX` | Replaced by a later decision |
| `Deprecated` | No longer applicable |
| `Proposed` | Under discussion |

---

## Code Review

### CodeRabbit

CodeRabbit provides automated general code quality review on
every PR. It will post a summary and inline comments. Address
critical findings before requesting human review.

### Claude Security Review

A custom GitHub Action submits every PR diff to the Anthropic
API for security-domain analysis. The review is structured as:

- **Critical Findings** — must be resolved before merge
- **Warnings** — warrant discussion, may have legitimate context
- **Observations** — positive signals and areas to watch

The security review prompt lives in
`.github/prompts/security-review-prompt.md` and is version
controlled. Changes to the prompt go through the same PR
process as code.

### GitGuardian

GitGuardian scans every PR for leaked secrets and credentials.
Any finding from GitGuardian is a blocking issue regardless
of other review outcomes.

---

## Security Checklist

Every PR touching platform services must verify the following
before opening for review. This checklist is also embedded in
the PR template.

- [ ] No key material, secrets, tokens, or plaintext appears
      in logs or output
- [ ] No hardcoded credentials or sensitive values of any kind
- [ ] Service ownership boundaries are respected — no service
      accesses another service's database or internal state
- [ ] CSP persistence contract is maintained — no sensitive
      material persists beyond operation scope
- [ ] KMS key state transitions follow the defined lifecycle
      if KMS is modified
- [ ] Audit logging captures metadata only — key identifiers,
      service names, operation types, outcomes
- [ ] Envelope encryption hierarchy is preserved — secrets
      encrypted with DEKs, DEKs protected by KEKs