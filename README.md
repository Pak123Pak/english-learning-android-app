# English Learning Android App 📚

An Android application for learning English vocabulary with Traditional Chinese translations, featuring spaced repetition and progressive revision stages.

## 📥 Download & Installation

### Option 1: Download Latest Release (Recommended)
1. Go to the [Releases](../../releases) page
2. Download the latest `EnglishLearning-vX.X.X.apk` file
3. On your Android device, open the downloaded APK file
4. If prompted, allow installation from unknown sources:
   - **Settings** → **Security** → **Install unknown apps** → Select your browser → Enable **Allow from this source**
5. Tap **Install** and wait for the installation to complete
6. Open the app and start learning!

### Option 2: Download from GitHub Actions (Latest Build)
1. Go to the [Actions](../../actions) tab
2. Click on the latest successful workflow run
3. Scroll down to **Artifacts** section
4. Download `app-debug` artifact
5. Extract the ZIP file to get the APK
6. Follow steps 3-6 from Option 1 above

### System Requirements
- Android 7.0 (API 24) or higher
- Internet connection for dictionary lookups
- Approximately 20MB storage space

## ✨ Features

### Dictionary Lookup
- Search English words with Traditional Chinese definitions
- Multiple definition support - choose the one that matches your context
- Powered by Cambridge Dictionary API
- Example sentences for each definition
- Word class information (noun, verb, adjective, etc.)

### Revision System
- **6-Stage Spaced Repetition**: "Not revised" → "1st" → "2nd" → "3rd" → "4th" → "5th or above"
- Words progress through stages as you answer correctly
- Incorrect answers keep words in the same stage for more practice
- Progressive hints to help with difficult words
- Fill-in-the-blank exercises with example sentences

### User Experience
- Clean, intuitive interface
- Automatic keyboard dismissal on Enter key
- Stage selection for targeted practice
- Back navigation to main menu from all screens
- Immediate feedback on answers

## 🛠️ Technical Stack

- **Platform**: Android (API 24+)
- **Language**: Kotlin
- **Architecture**: MVVM (Model-View-ViewModel)
- **Database**: Room (SQLite)
- **Networking**: Retrofit + OkHttp
- **UI**: Material Design Components
- **Async Operations**: Kotlin Coroutines + LiveData
- **Navigation**: Android Navigation Component

## 🔒 Privacy & Permissions

- **Permissions**: Internet access only (required for dictionary API)
- **Data Collection**: None - all data stays on your device
- **Analytics**: Not implemented
- **Third-party Services**: Cambridge Dictionary API only

## 🏗️ Building from Source

If you want to build the app yourself:

1. **Prerequisites**
   - Android Studio (latest stable version)
   - JDK 17 or higher
   - Android SDK with API 36

2. **Clone the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/YOUR_REPO_NAME.git
   cd YOUR_REPO_NAME
   ```

3. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

4. **Build the APK**
   - Via Android Studio: **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
   - Via Command Line:
     ```bash
     ./gradlew assembleDebug
     ```
   - The APK will be located at: `app/build/outputs/apk/debug/app-debug.apk`

## 📖 How to Use

1. **Main Menu**: Choose between "Dictionary Lookup" or "Revision"

2. **Dictionary Lookup**:
   - Search for any English word
   - Select the Chinese definition that matches your context
   - Save to start learning

3. **Revision**:
   - Practice words from any stage
   - Type the correct English word based on the Chinese definition
   - Use hints if you get stuck
   - Progress through stages as you improve

## 🤝 Contributing

This is a personal learning project, but suggestions and bug reports are welcome! Please open an issue if you encounter any problems.

## 📝 License

This project is for educational purposes. Cambridge Dictionary API terms of service apply.

## 🙏 Acknowledgments

- Cambridge Dictionary API for word definitions
- Material Design for UI components
- The Android development community

## 📧 Support

If you encounter any issues:
1. Check the [Issues](../../issues) page for existing reports
2. Open a new issue with detailed information about the problem
3. Include your Android version and device model

---

**Note**: This app is in active development. Features may be added or modified based on user feedback.