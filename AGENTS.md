# AGENTS.md

Rules for any AI tool working in this FRC 6829 Ignite Robotics repo. Read every session.
Keep it short; the standards live on the site, not here.

## This repo

<!-- repo-specific:start -->
IgniteLib: a Java 17 library of reusable FRC components and utilities, built with Gradle.

- Build and test: `./gradlew build` (JDK 17; on this machine `JAVA_HOME=~/wpilib/2026/jdk`)
- Format check: `./gradlew spotlessCheck`; fix with `./gradlew spotlessApply`
- PRs go into `development`. The required check is `Build and Check Code Format`.
<!-- repo-specific:end -->

## Who writes the spec

The person who owns the change writes the spec unless they have completed M8 or are a mentor. If you don't know which, ask before drafting.

Mentors and post-M8 students: propose, draft, and plan freely. Everyone else: explain, review, and
answer questions; the student writes the spec.

## Rules

- Every change starts from an issue. Branch `feature/<num>-<name>`; the PR says `Closes #<num>`.
  `openspec archive` is the PR's last commit.
- Once implementation starts, nothing edits files under `src/test/`. Not you, not the student.
  If a test looks wrong, stop and tell a mentor.
- Never run `openspec init` or `openspec update` here. They rewrite committed tool files based on
  the runner's personal settings. Tool files change only through `node .ignite/openspec-regen.mjs`
  (mentors only); CI checks for drift.
- Hooks in `.ignite/hooks/` enforce the test rule. Do not work around a block; report it to a mentor.
- Follow the AI-use tiers in the AI use standard linked below, and fill in the PR's AI disclosure honestly.

## Where the standards live

- [Coding standards](https://igniterobotics.github.io/ignition-labs/programming/coding-standards/)
- [AI use standard](https://igniterobotics.github.io/ignition-labs/programming/ai-use-standard/)
- [Issues and Projects](https://igniterobotics.github.io/ignition-labs/programming/issues-and-projects/)
  for the issue, spec, PR flow
- [The loop](https://igniterobotics.github.io/ignition-labs/programming/ai/the-loop/)
  for how spec-driven work runs
