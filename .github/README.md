# GitHub Actions Workflows

This directory contains automated workflows for the Life-Ops project.

## Available Workflows

### Build Release APK (`build-release.yml`)

Builds a signed release APK using GitHub Secrets for secure keystore management.

**Triggers:**
- Push to `main` branch
- Pull request to `main` branch
- Manual trigger via workflow_dispatch

**Requirements:**
- GitHub Secrets must be configured (see [GITHUB_SECRETS_SETUP.md](../docs/GITHUB_SECRETS_SETUP.md))

**Artifacts:**
- Signed release APK uploaded to workflow artifacts

**Security Features:**
- Keystore file is decoded from base64 in memory
- Keystore properties are created at runtime
- All sensitive files are cleaned up after build
- No secrets are exposed in logs

## Setup Instructions

See [docs/GITHUB_SECRETS_SETUP.md](../docs/GITHUB_SECRETS_SETUP.md) for detailed setup instructions.
