# GitHub Secrets Setup Guide

This guide explains how to configure GitHub Secrets for secure keystore management in CI/CD.

## Required GitHub Secrets

Go to: **Settings → Secrets and variables → Actions → New repository secret**

Add the following secrets:

### 1. `KEYSTORE_BASE64`
**Value:** The base64-encoded keystore file
- **File:** `keystore-base64.txt` (generated in project root)
- **Copy the entire contents** of this file as the secret value

### 2. `KEYSTORE_PASSWORD`
**Value:** `luk510838`
- This is your keystore password

### 3. `KEY_PASSWORD`
**Value:** `luk510838`
- This is your key password

### 4. `KEY_ALIAS`
**Value:** `life-ops`
- This is your key alias

## Steps to Configure

1. **Navigate to GitHub Repository**
   - Go to: https://github.com/LukeBrummett/Life-Ops

2. **Open Settings**
   - Click on "Settings" tab
   - Click on "Secrets and variables" in the left sidebar
   - Click on "Actions"

3. **Add Each Secret**
   - Click "New repository secret"
   - Enter the name (e.g., `KEYSTORE_BASE64`)
   - Paste the value
   - Click "Add secret"
   - Repeat for all 4 secrets

4. **Verify Setup**
   - Once all secrets are added, the GitHub Actions workflow will automatically use them
   - The workflow file is: `.github/workflows/build-release.yml`

## Security Notes

⚠️ **IMPORTANT:**
- Never commit `keystore.properties` or `*.jks` files to git
- The `keystore-base64.txt` file contains sensitive data - **delete it after copying to GitHub Secrets**
- Keep a secure backup of your keystore file (password manager or encrypted storage)
- If the keystore is compromised, you'll need to generate a new one and update all app versions

## Local Development

For local builds, continue using:
- `keystore.properties` (in project root, excluded by .gitignore)
- `life-ops-release-key.jks` (in project root, excluded by .gitignore)

## Testing the Workflow

After setting up secrets:
1. Push changes to the `main` branch, or
2. Manually trigger the workflow from the Actions tab
3. Check the "Build Release APK" workflow for success
4. Download the signed APK from the workflow artifacts

## Clean Up

After copying to GitHub Secrets:
```bash
# Delete the base64 file (contains sensitive data)
del keystore-base64.txt
```
