# JetBrains Junie GitHub Automation

This document outlines the setup, architecture, and usage of the [JetBrains Junie GitHub Action](https://github.com/JetBrains/junie-github-action) in this repository.

## Overview

Junie is JetBrains' AI coding agent for GitHub workflows. In this repository, Junie is integrated with **OpenRouter** using BYOK (Bring Your Own Key) authentication to provide:
1. **Interactive Coding Assistance**: Implement features from GitHub issues, make adjustments on PRs, and resolve merge conflicts when tagged with `@junie-agent`.
2. **Automated Pull Request Code Reviews**: Review PR changes against repository conventions and provide actionable feedback in a single updated comment thread.

Source repository: [JetBrains/junie-github-action](https://github.com/JetBrains/junie-github-action)

---

## Configuration & Authentication

Junie is authenticated via OpenRouter using repository secrets:

| Secret / Variable | Description | Example / Required Format |
|---|---|---|
| `OPENROUTER_API_KEY` *(Secret)* | Your OpenRouter API key | `sk-or-v1-...` |
| `OPENROUTER_MODEL_NAME` *(Secret or Variable)* | Junie model identifier (optional, defaults to `claude-sonnet-5-5`) | `claude-sonnet-5-5` |

> [!IMPORTANT]
> **Model Naming Requirement:**  
> Junie CLI requires model names **without** provider prefixes. Do **not** use `anthropic/claude-sonnet-5.5` or `openai/gpt-4o`. Use Junie's supported identifiers:
> - `claude-sonnet-5-5` (Recommended)
> - `claude-sonnet-5`
> - `claude-opus-5-5`
> - `gemini-3.8-flash`
> - `gpt-5.5`

---

## Active Workflows

### 1. Interactive Agent (`.github/workflows/junie.yml`)

- **Triggers**: Mentioning `@junie-agent` in:
  - Issues (title or description)
  - Issue comments
  - Pull Request review comments
  - Pull Request reviews
- **Permissions**: `contents: write`, `pull-requests: write`, `issues: write`
- **Behavior**:
  - Clones the repository and loads project context from `.junie/guidelines.md`.
  - Implements the requested change, commits to a branch, and opens/updates a PR.

### 2. Automated PR Code Review (`.github/workflows/junie-code-review.yml`)

- **Triggers**: Opened or updated (synchronized) Pull Requests targeting `main` (skips drafts).
- **Permissions**: `contents: read`, `pull-requests: write`, `issues: write`
- **Behavior**:
  - Automatically fetches the PR diff against `main`.
  - Evaluates code against Kotlin/Compose patterns, test coverage, and security.
  - Posts structured review feedback in a single, sticky PR comment.

---

## Usage Guide & Examples

### Implementing Features from Issues
Mention `@junie-agent` in an issue body or comment:
```markdown
@junie-agent Add a toggle in Developer Portal to simulate network timeout
```
Junie will create a branch and open a pull request with the implementation.

### Quick Adjustments on PRs
Request minor changes on an existing PR without re-explaining context:
```markdown
@junie-agent minor-fix rename iconTint to contentColor and update test assertion
```

### Resolving Merge Conflicts
If a PR has merge conflicts with `main`:
```markdown
@junie-agent resolve conflicts
```

### Dynamic Model Override (Per Request)
You can override the model on any prompt using the `junie-args:` syntax:
```markdown
@junie-agent refactor WeatherCheckWorker to use CoroutineScope
junie-args: --model="claude-opus-5-5"
```

---

## Project Guidelines Integration

Junie reads project guidelines from `.junie/guidelines.md`. In this repository, `.junie/guidelines.md` is a relative symlink to [`.github/copilot-instructions.md`](../.github/copilot-instructions.md).

This ensures a **single source of truth** across:
- GitHub Copilot (`.github/copilot-instructions.md`)
- Antigravity / AI Coding Assistants (`AGENTS.md` -> `.github/copilot-instructions.md`)
- JetBrains Junie (`.junie/guidelines.md` -> `.github/copilot-instructions.md`)
