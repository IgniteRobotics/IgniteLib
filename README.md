# IgniteLib
Library of reusable components, utilties, etc

## Building

Requires JDK 17.

```bash
./gradlew build          # compile + run tests
./gradlew spotlessCheck  # check formatting
./gradlew spotlessApply  # fix formatting
```

## Contributing

`main` and `development` are protected. Work on a `feature/<name>` branch and open a pull request into `development`.

Every PR into `main` or `development` gets:

- **Build and Check Code Format**: builds the library and runs the tests. This check must pass before the PR can be merged. Formatting problems don't fail the check; they show up as a PR comment. Run `./gradlew spotlessApply` and push to clear it.
- **AI Code Review**: Gemini posts one review comment and updates it on each push. It is advisory and never blocks a merge. It does not run on PRs from forks, because GitHub doesn't share secrets with them.

Only members of the `code-reviewers` team (and org admins) can merge. Org admins can bypass the required build in an emergency.

## Branch protection

Rulesets live in `.github/rulesets/` and are applied by a repo admin:

```bash
.github/scripts/apply-rulesets.sh --dry-run   # preview
.github/scripts/apply-rulesets.sh             # create or update
```

- `dev-main-pushes`: blocks direct pushes, force-pushes, creation, and deletion of `main`/`development`. Org admins and `code-reviewers` bypass.
- `dev-main-pr-gate`: requires a PR and a passing `Build and Check Code Format` check. Only org admins bypass.

If you rename the build job in `.github/workflows/pr-build-check.yml`, update `dev-main-pr-gate.json` and re-apply, or every merge will be blocked.
