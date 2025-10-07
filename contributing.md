# Contributing

Thank you for your interest in contributing to this project! This guide explains the basic workflow and expectations.

## How to contribute

- Fork the repository and create your feature branch from `main`:
  - git checkout -b feat/short-description
- Commit your changes with clear messages:
  - Conventional Commit style is preferred (e.g., feat:, fix:, docs:, test:, chore:)
- Write or update tests when applicable.
- Ensure the project builds and tests pass locally.
- Open a Pull Request (PR) against the `main` branch and fill in the PR template (if available).

## Development setup

- JDK 21+
- Gradle (wrapper provided)
- Optional: Docker Desktop/Compose if you want to run with containers

## Running locally

- Build: `./gradlew build` (or `gradlew.bat build` on Windows)
- Run: `./gradlew bootRun`
- Tests: `./gradlew test`

## Code style

- Follow standard Java conventions.
- Keep classes and methods small and purposeful.
- Prefer constructor injection over field injection for Spring components.

## Commit messages

- Use Conventional Commits where possible:
  - feat: add new user endpoint
  - fix: correct null check in UserService
  - docs: update README with Postman usage
  - test: add tests for login endpoint
  - chore: bump Spring Boot to 3.5.x

## Pull Request checklist

- [ ] Code compiles and tests pass
- [ ] Linting/static analysis shows no new issues (if applicable)
- [ ] Tests added/updated (if applicable)
- [ ] Docs updated (HELP.md, etc.)

## Reporting issues

- Use the issue tracker and provide:
  - Steps to reproduce
  - Expected vs. actual behavior
  - Logs, stack traces, screenshots if relevant

## Security

- Do not include secrets in code or commits.
- Report security issues privately if possible.

## License

- By contributing, you agree that your contributions will be licensed under the same license as the project.


## Branch protection for master and develop

To ensure that master and develop cannot receive direct commits and only accept changes via Pull Requests, please configure the repository settings as follows (requires Maintainer or Admin permissions):

1. Go to Settings → Branches → Branch protection rules.
2. Add a rule for branch name pattern: master
   - Enable "Require a pull request before merging" (set required approvals as needed).
   - Enable "Require status checks to pass before merging" and select the check: "Protect Branches (Disallow Direct Pushes) / Enforce PR-only updates to protected branches".
   - Optionally enable "Restrict who can push to matching branches" and leave it empty to block direct pushes for everyone (or specify allowed actors if needed).
   - Enable "Include administrators" if you want the rules to apply to admins as well.
3. Repeat the same rule for branch name pattern: develop
   - Use the same options as above.

Notes:
- This repository includes a GitHub Actions workflow (.github/workflows/protect-branches.yml) that fails on any direct push to master/develop. When you require this check in the branch protection rules, direct pushes will be effectively blocked and only PR merges will be allowed.
- The workflow allows merges created via PR (merge commits and squash merges) and fails for any other direct commit.
