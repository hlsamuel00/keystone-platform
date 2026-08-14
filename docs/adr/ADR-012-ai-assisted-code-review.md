# ADR-012: AI-Assisted Code Review

## Status

Accepted

## Date Accepted

2026-06-05

## Context

Keystone Platform is developed as a solo-maintainer project. While this simplifies decision-making and project coordination, it removes one of the most valuable controls present in mature engineering organizations: independent peer review.

Peer review serves multiple purposes. It improves code quality, identifies implementation defects, enforces architectural consistency, and helps detect security issues before they reach production. Without a second engineer reviewing changes, the platform requires alternative mechanisms to provide consistent review coverage and compensate for the absence of independent reviewers.

Traditional automated tooling such as linting, formatting, and static analysis provides valuable validation but lacks sufficient contextual awareness of Keystone's architectural decisions. Such tools can identify syntax errors, style violations, and certain categories of bugs, but they generally cannot detect violations of the platform's trust model, cryptographic boundaries, lifecycle requirements, or ownership principles.

The platform therefore required a review strategy capable of addressing both general software engineering concerns and Keystone-specific security architecture concerns.

## Alternatives Considered

### Option A: CodeRabbit Only

Use CodeRabbit as the sole AI-assisted review mechanism.

#### Pros

* Simple implementation
* Minimal maintenance burden
* Automated PR summaries
* Broad code quality coverage
* No custom review infrastructure required

#### Cons

* Limited awareness of Keystone-specific architecture
* Unable to reliably detect trust model violations
* Unable to validate cryptographic design requirements
* General-purpose review focus

### Option B: Custom Claude Security Review Only

Implement a custom GitHub Action using the Anthropic API and Keystone-specific review prompts.

#### Pros

* Deep awareness of platform architecture
* Focused security-domain analysis
* Detects trust boundary violations
* Detects cryptographic lifecycle issues
* Highly customizable review criteria

#### Cons

* Additional implementation and maintenance effort
* Limited coverage of general code quality concerns
* Requires prompt evolution as the platform grows
* Less effective for broad software engineering review

### Option C: Complementary AI Review Pipeline (Selected)

Use CodeRabbit for general-purpose code review and a custom Claude-based GitHub Action for Keystone-specific security architecture review.

#### Pros

* Broad code quality coverage
* Deep security-domain review
* Layered review strategy
* Strong alignment with platform architecture
* Near-zero operational cost
* Reduces dependence on a single review mechanism

#### Cons

* Additional workflow complexity
* Requires maintenance of custom review prompts
* Multiple review systems to manage
* Does not fully replace independent human review

## Decision

Keystone Platform SHALL implement two independent AI-assisted review controls as required pull request checks.

The first review control SHALL use CodeRabbit to perform:

* General code review
* Bug detection
* Refactoring recommendations
* Pull request summarization
* General software engineering analysis

The second review control SHALL use a custom Claude-powered GitHub Action to perform architecture-aware security review.

The custom security review SHALL evaluate changes against Keystone-specific concerns including:

* Key material exposure
* Cryptographic lifecycle violations
* Ownership boundary violations
* CSP persistence contract violations
* Hardcoded credentials or secrets
* Authorization model violations
* Trust model inconsistencies

Both review controls SHALL be configured as required status checks before merge.

Neither review mechanism SHALL be considered a replacement for human review when independent reviewers become available.

## Rationale

General code review and security architecture review are distinct concerns that require different review strategies.

CodeRabbit provides broad software engineering coverage and can effectively identify common defects, implementation concerns, code quality issues, and maintainability problems. However, it lacks awareness of Keystone's architectural decisions and security-specific design constraints.

Conversely, a custom Claude review action can be explicitly trained to evaluate platform-specific concerns such as trust boundaries, cryptographic lifecycle enforcement, ownership isolation, and service responsibilities. While this enables deeper architectural analysis, it does not provide the same breadth of general-purpose code review.

Breadth and depth are different review problems and should not be solved by the same tool.

Using both review mechanisms creates layered review coverage. Each system contributes capabilities that the other lacks, resulting in broader overall review quality than either tool could provide independently.

This decision also serves as a compensating control for the limitations acknowledged in ADR-011. While a solo-maintainer project cannot fully enforce independent peer review, multiple automated review gates provide meaningful validation beyond simple self-approval. The goal is not to replace human review but to establish consistent and repeatable review standards until additional reviewers become available.

The decision further aligns with Keystone Platform's broader philosophy of defense in depth. Just as the platform layers controls for cryptographic protection, authorization enforcement, and trust management, the development process layers controls for software quality and security review.

## Consequences

### Positive

* Broader review coverage than either tool alone
* Improved detection of architecture-specific security issues
* Improved general code quality review
* Consistent review standards across all pull requests
* Stronger compensating controls for solo development
* Reinforces defense-in-depth principles
* Creates a distinctive security-focused development workflow

### Negative

* Additional workflow complexity
* Ongoing maintenance of custom prompts
* Increased CI execution time
* Dependence on external AI review services

### Risks

#### Prompt Drift

The custom security review may become less effective as the platform architecture evolves.

Mitigations:

* Periodic prompt review
* ADR-driven prompt updates
* Alignment with architecture documentation
* Ongoing refinement as services mature

#### False Positives

AI review systems may incorrectly identify issues that are not genuine concerns.

Mitigations:

* Human review of findings
* Prompt tuning
* Iterative workflow improvement

#### False Negatives

Review systems may fail to identify legitimate issues.

Mitigations:

* Multiple review layers
* Secret scanning controls
* CI validation checks
* Future human review participation

#### Single-Maintainer Limitations

AI-assisted review cannot fully replace independent peer review.

Mitigations:

* Multiple review perspectives
* Required status checks
* Documented review standards
* Future organizational review controls when additional contributors exist

## Related Decisions

* ADR-008: Event Payload Encryption and Signing Beyond TLS
* ADR-011: CI/CD Pipeline and Review Controls
* ADR-013: Monorepo Repository Structure

## Amendments

### Amendment 1 — 2026-06-04

#### GitGuardian
GitGuardian activated automatically on the public repository
and was added as a required status check. The platform now
operates three independent secret scanning layers:
TruffleHog, GitGuardian, and the Claude security review.
No configuration was required — GitGuardian activates on
all public repositories automatically.

#### Human Approval Constraint
Required approvals were set to 0 due to GitHub's restriction
that PR authors cannot approve their own PRs on personal
accounts. The AI review gates — CodeRabbit, Claude security
review, GitGuardian, and TruffleHog — serve as the primary
merge controls. A human approval requirement SHALL be
reinstated if additional contributors join the project.

#### TruffleHog Known Limitations
Two known limitations were identified during implementation:

1. The --only-verified flag restricts detection to secrets
   verifiable against live external services. Internal
   platform credentials and rotated keys will not be
   flagged. This gap is accepted at MVP stage and SHALL
   be revisited before production deployment.

2. Force push events and first-push-to-branch scenarios
   may resolve github.event.before to the null SHA
   (0000000000000000000000000000000000000000), potentially
   causing TruffleHog to scan zero commits silently. This
   is a known TruffleHog behavior. Full-history scanning
   as a fallback is deferred to a future iteration.

#### Model Identifier Policy
Pinned, dated model identifiers are preferred for the
Claude security review action to ensure reproducible and
auditable review behavior between PRs. Model identifiers
SHALL be reviewed and updated deliberately rather than
tracking aliases that may resolve differently over time.

### Amendment 2 — 2026-08-14

#### Model String Correction
The security review workflow (`security-review.yml`) referenced
`claude-sonnet-4-6`, a model identifier that does not correspond to
any model Anthropic has published. The workflow SHALL use
`claude-sonnet-5`, Anthropic's current Sonnet-tier model as of this
amendment.

#### Model Identifier Policy — Supersedes Amendment 1
Amendment 1's Model Identifier Policy preferred pinned, dated model
identifiers over aliases, on the basis that dateless identifiers
could silently resolve to a different model over time. This concern
applied to Anthropic's pre-4.6-generation naming convention, where
dateless names (e.g. `claude-sonnet-4-5`) were floating aliases
pointing to the most recent dated snapshot.

Starting with the 4.6 generation, Anthropic's dateless model
identifiers (e.g. `claude-sonnet-5`) are themselves permanently
fixed snapshots — Anthropic does not update the weights or
configuration behind an existing model ID once published. The
original rationale for preferring dated identifiers no longer
applies under this naming convention, since no separate dated
variant exists to prefer. Effective immediately, the current
dateless model ID SHALL be treated as sufficiently pinned for
reproducibility purposes, and the platform's model identifier
SHALL be reviewed and updated deliberately whenever Anthropic
ships a new model generation — consistent with Amendment 1's
underlying intent, though not its literal mechanism.

#### Diff Truncation Limit Increase
`max_diff_chars` was increased from 30,000 to 250,000, after the
KMS domain model refactor PR (#5) produced a diff of approximately
155,000 characters — more than five times the original limit.
Truncation logic was also changed to cut at file boundaries
(`diff --git` markers) rather than mid-file, so any file included
in a truncated review is always reviewed in its entirety rather
than left partially analyzed.

Cost analysis at current Sonnet 5 pricing ($2/$10 per million
input/output tokens) confirms this increase carries negligible
per-run cost — approximately $0.16 for a full review of PR #5's
diff, including the output ceiling increase described below. The
original 30,000-character limit was not a deliberate cost-control
measure and is superseded by this analysis.

#### Output Token Ceiling Increase
`max_tokens` for the security review's Anthropic API call was
increased from 2,048 to 8,192, to prevent the review response
itself from being truncated on large diffs — a distinct concern
from the diff truncation limit above, which governs input rather
than output.
