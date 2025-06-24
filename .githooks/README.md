# Git Hooks

This directory contains git hooks for the Weather Alert Android project.

## Pre-commit Hook

### What it does

The `pre-commit` hook automatically runs Kotlin code formatting before each commit using the project's configured kotlinter plugin. Here's what happens:

1. **Runs formatting**: Executes `./gradlew formatKotlin` to format all Kotlin code according to the project's style guidelines
2. **Handles changes**: If the formatting task modifies any files, those changes are automatically added to the current commit
3. **Prevents bad commits**: If the formatting task fails (e.g., due to syntax errors), the commit is aborted

### Setup Instructions

To enable the pre-commit hook in your local repository:

1. **Configure git to use the custom hooks directory**:
   ```bash
   git config core.hooksPath .githooks
   ```

2. **Verify the hook is executable** (should already be set):
   ```bash
   chmod +x .githooks/pre-commit
   ```

3. **Test the hook** (optional):
   ```bash
   ./.githooks/pre-commit
   ```

### How it works

When you run `git commit`, the pre-commit hook will:

- ✅ **Success case**: If formatting succeeds and no changes are made, the commit proceeds normally
- 📝 **Auto-fix case**: If formatting succeeds but files are modified, the formatted files are automatically added to your commit
- ❌ **Failure case**: If formatting fails (syntax errors, etc.), the commit is aborted and you need to fix the issues manually

### Example output

```bash
$ git commit -m "Add new feature"
Running Kotlin formatting...
✅ Kotlin formatting completed successfully
📝 Formatting changes detected, adding to commit...
✅ Formatted files added to commit
🚀 Pre-commit hook completed successfully
[main abc1234] Add new feature
 2 files changed, 10 insertions(+), 5 deletions(-)
```

### Benefits

- **Consistent code style**: Ensures all committed code follows the project's formatting standards
- **Automatic fixes**: Minor formatting issues are fixed automatically without manual intervention
- **Early detection**: Catches formatting/syntax issues before they reach the repository
- **Zero configuration**: Works out of the box with the existing kotlinter plugin setup

### Disabling the hook

If you need to temporarily disable the hook (not recommended for regular commits):

```bash
git commit --no-verify -m "Your commit message"
```

### Requirements

- The project must be buildable (all dependencies available)
- The kotlinter Gradle plugin must be properly configured
- Gradle wrapper (`./gradlew`) must be executable

### Troubleshooting

**Hook doesn't run**: Make sure you've run `git config core.hooksPath .githooks`

**Permission denied**: Run `chmod +x .githooks/pre-commit`

**Gradle build fails**: Ensure your development environment is properly set up and the project builds successfully with `./gradlew build`