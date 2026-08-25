# AstroVeda Development, Quality & Git Commit Standards

## 1. Zero-Error & Strict Verification Policy
- Every change MUST be thoroughly checked for syntax, compilation, and lint errors before finalizing.
- Run `./gradlew testDebugUnitTest` or relevant test targets to guarantee that existing and new tests pass.
- No untested or broken code should ever be left unverified.

## 2. Commit & Push Protocol
- After verifying changes and passing all tests:
  1. Review `git status` and `git diff` to ensure only intended changes are staged.
  2. Commit with meaningful, conventional commit messages (e.g., `feat:`, `fix:`, `refactor:`, `test:`).
  3. Push cleanly to the remote branch (`git push origin <branch>`).
- Keep commit history clean, descriptive, and atomic.
