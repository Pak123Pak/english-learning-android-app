# English Learning Android App

An Android application for learning English vocabulary with Traditional Chinese translations, featuring spaced repetition and progressive revision stages.

## Download & Installation

1. Go to the [Releases](../../releases) page
2. Download the latest `EnglishLearning-vX.X.X.apk` file
3. On your Android device, open the downloaded APK file
4. If prompted, allow installation from unknown sources:
   - **Settings** → **Security** → **Install unknown apps** → Select your browser → Enable **Allow from this source**
5. Tap **Install** and wait for the installation to complete
6. Open the app and start learning!

### System Requirements
- Android 7.0 (API 24) or higher
- Internet connection for dictionary lookups
- Approximately 5-10MB storage space

### Updating the App

When a new version is available:
1. Download the latest APK from [Releases](../../releases)
2. Install directly over your existing app
3. **Your data is safe**: All saved words and progress will be preserved

**Note**: This app uses consistent signing until v1.3. Ppdating to replace version older than v 1.3 will encounter "App not installed" errors.

## How to Use

1. **Main Menu**: Choose between "English - Chinese" or "Revision"

2. **English - Chinese**:
   - Search for any English word
   - Select the Chinese definition that matches your context
   - Save to start learning

3. **Revision**:
   - Practice words from any stage
   - Type the correct English word based on the Chinese definition
   - Use hints if you get stuck
   - Progress through stages as you improve

## Privacy & Security

### Privacy
- **Permissions**: Internet access only (required for dictionary API)
- **Data Collection**: None - all data stays on your device
- **Analytics**: Not implemented - no tracking or profiling
- **Third-party Services**: Cambridge Dictionary API only (no authentication required)
- **Local Storage**: All vocabulary data stored locally using Room database
- **No Cloud Sync**: Your data never leaves your device

### Security Features
- **HTTPS-Only**: All network connections use HTTPS
- **Code Obfuscation**: Release builds use ProGuard/R8 minification
- **No Logging**: Debug logs removed in release builds
- **No Backups**: Backup disabled to prevent data extraction
- **Minimal Permissions**: Only internet access required
- **Open Source**: Code is publicly available for security review

## Support

If you encounter any issues:
1. Check the [Issues](../../issues) page for existing reports
2. Open a new issue with detailed information about the problem
3. Include your Android version and device model

---

**Note**: This app is in active development. Features may be added or modified based on user feedback.