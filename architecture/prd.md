# Product Requirements Document (PRD)
## English Learning Android App

### 1. Project Overview

**Product Name**: English Learning App  
**Target Platform**: Android (API 24+)  
**Target Device**: Samsung S24 Ultra (Testing)  
**Development Language**: Kotlin  
**Primary Language Pair**: English ↔ Traditional Chinese  

### 2. Executive Summary

This Android application helps users learn English vocabulary through Traditional Chinese translations with a spaced repetition learning system. The app features word search capabilities using the Cambridge Dictionary API and implements a 6-stage revision system for effective vocabulary retention.

### 3. Business Objectives

- **Primary Goal**: Provide an effective English vocabulary learning tool for Traditional Chinese speakers
- **Secondary Goals**: 
  - Implement scientifically-backed spaced repetition methodology
  - Ensure offline functionality for saved words
  - Create an intuitive user experience for all age groups

### 4. Target Users

**Primary Users**: Traditional Chinese speakers learning English
- Age Range: 13-65 years
- Technical Proficiency: Basic to intermediate Android users
- Learning Context: Self-study, supplementary education tool
- Device: Android smartphones (Samsung S24 Ultra verified)

### 5. Core Features

#### 5.1 Main Navigation
- **Home Screen**: Three primary options
  - "English-Chinese-Traditional" (Dictionary Search)
  - "Revision" (Learning Review)
  - "Delete All Words" (Data Management)

#### 5.2 Dictionary Search Feature
- **Word Search**: Input field for English word queries
- **API Integration**: Cambridge Dictionary API (en-tw endpoint)
- **Definition Display**: Show all available definitions in "easy-to-read" format
- **Definition Selection**: User must select exactly one definition/translation
- **Save Functionality**: Add selected word+definition to "Not revised" stage
- **Navigation**: Back button to return to main menu

**API Response Structure Expected**:
```json
{
  "word": "cook",
  "pos": ["verb", "noun"],
  "pronunciation": [
    {
      "pos": "verb",
      "lang": "uk",
      "url": "https://dictionary.cambridge.org/us/media/english-chinese-traditional/uk_pron/u/ukc/ukcon/ukconve028.mp3",
      "pron": "/kʊk/"
    }
  ],
  "definition": [
    {
      "id": 0,
      "pos": "verb",
      "text": "When you cook food, you prepare it to be eaten by heating it in a particular way",
      "translation": "做飯，烹調;燒，煮",
      "example": [
        {
          "id": 0,
          "text": "I don't cook meat very often.",
          "translation": "我不常煮肉吃。"
        }
      ]
    },
    {
      "id": 1,
      "pos": "noun",
      "text": "someone who prepares and cooks food",
      "translation": "廚師",
      "example": [
        {
          "id": 0,
          "text": "She's a wonderful cook.",
          "translation": "她是位很出色的廚師。"
        }
      ]
    }
  ]
}
```

#### 5.3 Revision System
- **6-Stage Learning Progression**:
  1. "Not revised" (New words)
  2. "1st" (First review)
  3. "2nd" (Second review)
  4. "3rd" (Third review)
  5. "4th" (Fourth review)
  6. "5th or above" (Mastered words)

- **Revision Flow**:
  - Display first word from selected stage
  - Show Chinese translation + example sentence with blank
  - User inputs English word (case-insensitive)
  - Show result feedback (correct/incorrect) and wait for user confirmation
  - User clicks "Continue" button to proceed to next word
  - Correct answer → advance to next stage
  - Incorrect answer → remain in current stage
  - "5th or above" stage: words remain in same stage regardless

- **Stage Selection**: Dropdown/picker to choose which stage to review
- **Individual Word Deletion**: Delete button for current word being reviewed with confirmation
- **Navigation**: Back button to return to main menu

#### 5.4 Data Management
- **Local Database**: Room database for offline storage
- **Word Storage**: English word, selected translation, example sentence, current stage
- **Stage Management**: Track word progression through learning stages
- **Delete All Words**: Complete database reset with confirmation dialog on main screen
- **Delete Individual Word**: Remove specific word from any stage with confirmation
- **Data Safety**: Confirmation dialogs for all destructive operations

### 6. Technical Requirements

#### 6.1 Development Environment
- **Android Studio**: Latest stable version
- **Java Version**: Java 25 (as specified by user)
- **Minimum SDK**: API 24 (Android 7.0)
- **Target SDK**: API 36
- **Architecture**: MVVM with LiveData

#### 6.2 Dependencies
- **Room Database**: 2.6.1 (Local storage)
- **Retrofit**: 2.9.0 (API networking)  
- **Lifecycle**: 2.8.7 (ViewModel, LiveData)
- **Navigation**: 2.8.4 (Screen navigation)
- **Material Design**: UI components

#### 6.3 API Integration
- **Primary API**: Cambridge Dictionary API (GitHub: chenelias/cambridge-dictionary-api)
- **Endpoint Focus**: en-tw (English to Traditional Chinese)
- **Fallback**: Free Dictionary API (if Cambridge unavailable)

### 7. User Interface Requirements

#### 7.1 Design Principles
- **Material Design**: Follow Google's Material Design guidelines
- **Traditional Chinese Support**: Proper font rendering for Traditional Chinese characters
- **Accessibility**: Support for larger text sizes, screen readers
- **Responsive Design**: Optimize for various Android screen sizes

#### 7.2 Screen Specifications

**Main Activity**:
- App title/logo
- Two large, clearly labeled buttons
- Simple, clean layout

**Dictionary Activity**:
- Back button to main menu
- Search input field (prominent)
- Definition list with radio buttons
- Save button (disabled until selection made)
- Standard back navigation

**Revision Activity**:
- Back button to main menu
- Stage selector (dropdown)
- Word display area
- Answer input field
- Submit button
- Answer feedback area
- Continue button (shown after answer submission)
- Progress indicator (current stage, word count)

#### 7.3 User Experience Flow
```
App Launch → Main Menu → [Dictionary Search OR Revision]
Dictionary: Search → Results → Select → Save → Back to Search → Back to Main (optional)
Revision: Select Stage → Show Word → Input Answer → Show Result → Continue → Next Word → Back to Main (optional)
```

### 8. Data Model

#### 8.1 Word Entity
```kotlin
data class Word(
    val id: Long,
    val englishWord: String,
    val chineseTranslation: String,
    val partOfSpeech: String,
    val exampleSentence: String,
    val blankExampleSentence: String, // With word replaced by blank
    val revisionStage: Int, // 0-5 (0=Not revised, 5=5th or above)
    val createdAt: Long,
    val lastRevisedAt: Long
)
```

#### 8.2 Database Schema
- **Table**: words
- **Primary Key**: Auto-generated ID
- **Indexes**: revisionStage (for efficient stage queries)

### 9. Performance Requirements

- **API Response Time**: < 3 seconds for word lookups
- **Local Database Queries**: < 100ms for word retrieval
- **App Launch Time**: < 2 seconds
- **Memory Usage**: < 100MB during normal operation
- **Battery Optimization**: Minimal background processing

### 10. Security & Privacy

- **Data Storage**: All user data stored locally (no cloud sync)
- **API Security**: HTTPS for all network requests
- **Permissions**: Internet access only (no sensitive permissions)
- **Privacy**: No user data collection or analytics

### 11. Testing Requirements

#### 11.1 Testing Methodologies
- **TDD**: Unit tests for business logic
- **BDD**: User behavior scenarios
- **ATDD**: Acceptance criteria validation

#### 11.2 Test Coverage
- **Unit Tests**: Repository, ViewModel, utility classes
- **Integration Tests**: Database operations, API calls
- **UI Tests**: Critical user flows
- **Device Testing**: Samsung S24 Ultra verification

### 12. Success Metrics

#### 12.1 Technical Metrics
- 95%+ uptime for core functionality
- < 1% crash rate
- 100% feature completion as specified
- Successful deployment to Samsung S24 Ultra

#### 12.2 User Experience Metrics
- Intuitive navigation (minimal user confusion)
- Fast word search and retrieval
- Accurate revision system progression
- Proper Traditional Chinese character display

### 13. Project Timeline

**Phase 1: Foundation** (Days 1-2)
- Project setup and dependencies
- Basic activity structure
- Database implementation

**Phase 2: Core Features** (Days 3-5)
- API integration
- Dictionary search functionality
- Revision system implementation

**Phase 3: Polish & Testing** (Days 6-7)
- UI refinement
- Testing and bug fixes
- Samsung S24 Ultra deployment

### 14. Risk Assessment

#### 14.1 Technical Risks
- **API Availability**: Cambridge Dictionary API limitations
- **Character Encoding**: Traditional Chinese rendering issues
- **Device Compatibility**: Performance on Samsung S24 Ultra

#### 14.2 Mitigation Strategies
- **API Fallback**: Implement Free Dictionary API as backup
- **Font Testing**: Early validation of Chinese character support
- **Device Testing**: Regular testing on target device

### 15. Future Enhancements (Out of Scope)

- Multi-language support (other Chinese variants)
- Cloud synchronization
- Advanced analytics and progress tracking
- Social features (sharing, competition)
- Pronunciation features
- Offline dictionary download

### 16. Acceptance Criteria

- [ ] App installs and launches on Samsung S24 Ultra
- [ ] Word search returns accurate Traditional Chinese translations
- [ ] Definition selection saves words to "Not revised" stage
- [ ] Revision system correctly progresses through all 6 stages
- [ ] Case-insensitive answer validation works correctly
- [ ] Stage selection allows switching between revision levels
- [ ] Traditional Chinese characters display properly
- [ ] Navigation between screens works seamlessly
- [ ] Data persists across app sessions
- [ ] No crashes during normal operation

---

**Document Version**: 1.0  
**Last Updated**: September 2025  
**Status**: Approved for Development
