# Code Owners

This repository can use a CODEOWNERS configuration to automatically request reviews from specific people when files change.

Note: GitHub looks for a file named `CODEOWNERS` (no extension) in the repository. This `codeonwer.md` explains how to set it up. If you want automatic review assignment, create a file named `CODEOWNERS` in the repository root with contents like below and replace placeholders with real GitHub handles or teams.

Example CODEOWNERS file:

```
# Set global owners for everything
*       @your-org/your-team

# Own specific paths
/postman/           @your-handle
/src/main/java/     @backend-team
/src/test/java/     @qa-team

# Own specific files
HELP.md             @docs-team
```

Tips:
- Use team aliases like `@your-org/your-team` for better coverage.
- The last matching pattern in the file takes precedence.
- Patterns follow the same rules as .gitignore.
