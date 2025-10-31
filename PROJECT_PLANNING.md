# English Learning Android App - Project Planning Document

## Project Overview

This document provides a comprehensive plan for developing an Android application that helps users learn English through a Traditional Chinese interface. The app will feature vocabulary learning with spaced repetition and a word revision system.

### Core Features
- English-Chinese Traditional dictionary lookup
- Vocabulary revision system with 6 stages
- Word search and definition selection
- Spaced repetition learning methodology
- Progress tracking across revision stages

## Pre-Development Preparation

### 1. Development Environment Setup

#### Required Software:
- **Android Studio** (Latest stable version - Arctic Fox or newer)
- **Java Development Kit (JDK)** 17 or higher
- **Android SDK** with API levels 24-34 (Android 7.0 - Android 14)
- **Git** for version control

#### Device Testing:
- **Samsung S24 Ultra** for physical device testing
- Enable Developer Options and USB Debugging
- Install Samsung USB drivers

### 2. API Research and Integration Planning

#### Primary API: Cambridge Dictionary API
- **Repository**: https://github.com/chenelias/cambridge-dictionary-api
- **Focus**: English-Traditional Chinese (en-tw) translations
- **Key Data Points**: 
  - Word definitions array
  - Multiple translation options
  - Example sentences
  - Part of speech information

#### API Integration Requirements:
- Network permission in AndroidManifest.xml
- HTTP client library (Retrofit/Volley/OkHttp)
- JSON parsing library (Gson/Jackson)
- Error handling for network failures
- Caching mechanism for offline access

### 3. Technical Architecture Planning

#### Technology Stack:
- **Language**: Java/Kotlin (Recommend Kotlin for modern Android development)
- **Architecture**: MVVM (Model-View-ViewModel) with LiveData
- **Database**: Room (SQLite wrapper) for local data storage
- **Networking**: Retrofit with Gson converter
- **UI Framework**: Material Design Components
- **Navigation**: Navigation Component
- **Dependency Injection**: Hilt (optional but recommended)

#### Database Schema Planning:
```sql
-- Words table
CREATE TABLE words (
    id INTEGER PRIMARY KEY,
    english_word TEXT NOT NULL,
    selected_definition TEXT NOT NULL,
    chinese_translation TEXT NOT NULL,
    example_sentence TEXT,
    stage TEXT DEFAULT 'Not revised',
    created_at TIMESTAMP,
    last_revised TIMESTAMP
);

-- Revision stages: "Not revised", "1st", "2nd", "3rd", "4th", "5th or above"
```

### 4. User Interface Design

#### Screen Structure:
1. **Main Menu Screen**
   - "English-Chinese-Traditional" button
   - "Revision" button
   - App title and branding

2. **Dictionary Search Screen**
   - Search input field
   - Definition selection interface
   - Save button
   - Back navigation

3. **Revision Screen**
   - Stage selection dropdown
   - Word display area
   - Answer input field
   - Submit button
   - Progress indicator

#### UI/UX Considerations:
- Material Design principles
- Support for Traditional Chinese fonts
- Responsive design for different screen sizes
- Dark/Light theme support
- Accessibility features

### 5. Project Structure Setup

#### Recommended Folder Structure:
```
app/
├── src/
│   ├── main/
│   │   ├── java/com/yourname/englishlearning/
│   │   │   ├── data/
│   │   │   │   ├── database/
│   │   │   │   ├── repository/
│   │   │   │   └── api/
│   │   │   ├── ui/
│   │   │   │   ├── main/
│   │   │   │   ├── dictionary/
│   │   │   │   └── revision/
│   │   │   ├── model/
│   │   │   └── utils/
│   │   ├── res/
│   │   └── AndroidManifest.xml
│   └── test/
└── build.gradle
```

## Development Phase Instructions

### Phase 1: Project Foundation (Before AI Assistance)

#### What YOU should do first:

1. **Create New Android Project**
   - Open Android Studio
   - Create new project with Empty Activity
   - Choose appropriate package name
   - Set minimum SDK to API 24 (Android 7.0)

2. **Set Up Git Repository and GitHub Integration**

3. **Add Basic Dependencies** (in app/build.gradle):
   ```gradle
   dependencies {
       // Room database
       implementation "androidx.room:room-runtime:2.5.0"
       kapt "androidx.room:room-compiler:2.5.0"
       
       // Networking
       implementation 'com.squareup.retrofit2:retrofit:2.9.0'
       implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
       
       // ViewModel and LiveData
       implementation "androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0"
       implementation "androidx.lifecycle:lifecycle-livedata-ktx:2.7.0"
       
       // Navigation
       implementation "androidx.navigation:navigation-fragment-ktx:2.7.4"
       implementation "androidx.navigation:navigation-ui-ktx:2.7.4"
   }
   ```

4. **Create Basic Activity Structure**
   - MainActivity (main menu)
   - DictionaryActivity (word search)
   - RevisionActivity (word testing)

5. **Design Basic XML Layouts**
   - Create placeholder layouts for each activity
   - Set up basic navigation between screens

### Phase 2: AI Assistance Request (What to ask AI to help with)

After completing Phase 1, you should ask AI to help you with:

#### Core Implementation Tasks:
1. **Database Implementation**
   - Room entities, DAOs, and database setup
   - Migration strategies
   - Data access layer

2. **API Integration**
   - Retrofit service interfaces
   - Response models for Cambridge API
   - Error handling and retry logic
   - Network state management

3. **Business Logic Implementation**
   - Word search and definition selection logic
   - Revision system with stage progression
   - Spaced repetition algorithm
   - Data validation and error handling

4. **UI Implementation**
   - Dynamic UI updates based on data
   - RecyclerView adapters for word lists
   - Custom views for definition selection
   - Input validation and user feedback

5. **Testing Setup**
   - Unit tests for business logic
   - Integration tests for database operations
   - UI tests for critical user flows

#### Specific AI Request Template:
```
"Please help me implement the following components for my English learning Android app:

1. Room database setup with Word entity and WordDao
2. Retrofit API service for Cambridge Dictionary integration
3. Repository pattern for data management
4. ViewModel classes for each screen
5. Complete UI implementation with Material Design
6. Navigation between screens
7. Word search and save functionality
8. Revision system with stage progression
9. Input validation and error handling
10. Basic unit and integration tests

Project requirements: [Include your detailed requirements]
Current project structure: [Include your project structure]
API documentation: [Include Cambridge API details]"
```

### Phase 3: Post-AI Implementation (What to do after)

#### Testing and Validation:
1. **Run All Tests**
   - Execute unit tests
   - Run integration tests
   - Perform manual testing on emulator

2. **Device Testing**
   - Install on Samsung S24 Ultra
   - Test all features on physical device
   - Verify performance and responsiveness

3. **API Testing**
   - Test with real Cambridge Dictionary API
   - Verify Chinese Traditional character rendering
   - Test offline scenarios

#### Code Review and Optimization:
1. **Code Quality**
   - Review AI-generated code for best practices
   - Ensure proper error handling
   - Optimize database queries
   - Add necessary comments and documentation

2. **Performance Optimization**
   - Profile app performance
   - Optimize memory usage
   - Implement proper caching strategies

3. **Security Considerations**
   - API key protection
   - Input sanitization
   - Data encryption if needed

## Post-Development Documentation

### Architecture Documentation (To be created with AI assistance later)

The following documentation will be created in the `architecture/` folder:

1. **PRD (Product Requirements Document)** - `prd.md`
   - Detailed functional requirements
   - User stories and acceptance criteria
   - Technical specifications

2. **Flow Diagram** - `flow.md`
   - User journey flowcharts
   - Data flow diagrams
   - System interaction flows

3. **Class Diagram** - `classes.md`
   - UML class diagrams
   - Relationship mappings
   - System architecture overview

4. **C4 Structure Charts**
   - Context diagrams (.plantuml)
   - Container diagrams (.plantuml)
   - Component diagrams (.plantuml)
   - Code diagrams (.plantuml)

### Testing Documentation (To be created in `testing/` folder)

1. **Test-Driven Development (TDD)** - `tdd.md`
2. **Behaviour-Driven Development (BDD)** - `bdd.md`
3. **Acceptance Test-Driven Development (ATDD)** - `atdd.md`

## Success Criteria

### Technical Milestones:
- [ ] App installs and runs on Samsung S24 Ultra
- [ ] Successful integration with Cambridge Dictionary API
- [ ] Word search returns accurate Traditional Chinese translations
- [ ] Revision system progresses through all 6 stages
- [ ] Data persistence across app sessions
- [ ] Responsive UI with proper Chinese character support

### User Experience Goals:
- [ ] Intuitive navigation between features
- [ ] Fast word lookup (< 2 seconds)
- [ ] Clear definition selection interface
- [ ] Engaging revision experience
- [ ] Progress tracking visibility
- [ ] Offline functionality for saved words

### Quality Assurance:
- [ ] 90%+ test coverage for business logic
- [ ] No critical bugs or crashes
- [ ] Smooth performance on target device
- [ ] Proper error handling and user feedback
- [ ] Accessibility compliance

## Risk Management

### Potential Challenges:
1. **API Limitations**: Cambridge Dictionary API rate limits or availability
2. **Character Encoding**: Traditional Chinese character display issues
3. **Device Compatibility**: Performance on older Android versions
4. **Network Connectivity**: Offline functionality requirements

### Mitigation Strategies:
1. Implement robust caching and offline modes
2. Test character rendering early and frequently
3. Set appropriate minimum SDK requirements
4. Design graceful degradation for network issues

## Timeline Estimation

### Phase 1 (Preparation): 2-3 days
- Environment setup
- Project structure creation
- Basic UI layouts

### Phase 2 (AI-Assisted Development): 5-7 days
- Core feature implementation
- API integration
- Database setup
- Testing framework

### Phase 3 (Testing and Refinement): 3-4 days
- Device testing
- Bug fixes
- Performance optimization
- Documentation completion

**Total Estimated Timeline: 10-14 days**

This planning document serves as your roadmap for successful project completion. Update it as needed throughout the development process.
