## Summary

<!--
Describe what this PR does and why.
Reference any relevant ADRs or prior decisions.
-->

## Type of Change

<!-- Check all that apply -->

- [ ] `feat` — new feature or capability
- [ ] `fix` — bug or error correction
- [ ] `docs` — service documentation, runbooks, README
- [ ] `adr` — architecture decision record
- [ ] `ci` — CI/CD pipeline or workflow change
- [ ] `chore` — housekeeping or configuration
- [ ] `refactor` — restructure without behavior change
- [ ] `test` — test additions or updates

## Service Affected

<!-- Check all that apply -->

- [ ] KMS
- [ ] CSP
- [ ] SR
- [ ] STS
- [ ] SV
- [ ] Platform (cross-cutting)
- [ ] CI/CD
- [ ] Documentation only

## Security Checklist

<!--
Required for all PRs touching platform services.
See CONTRIBUTING.md for full guidance.
-->

- [ ] No key material, secrets, tokens, or plaintext
      appears in logs or output
- [ ] No hardcoded credentials or sensitive values
- [ ] Service ownership boundaries are respected
- [ ] CSP persistence contract is maintained if CSP
      is modified
- [ ] KMS key state transitions follow the defined
      lifecycle if KMS is modified
- [ ] Audit logging captures metadata only
- [ ] Envelope encryption hierarchy is preserved

## Testing

<!--
Describe what was tested and how.
For placeholder test jobs, note what manual
verification was performed instead.
-->

## Related ADRs or Issues

<!--
Link any relevant ADRs or issues.
Example: Implements ADR-009 language selection.
-->
