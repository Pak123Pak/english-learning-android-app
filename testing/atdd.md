# Acceptance Test-Driven Development (ATDD) Approach
## English Learning Android App

### 1. ATDD Overview

Acceptance Test-Driven Development (ATDD) is a collaborative approach where the entire team (business stakeholders, developers, and testers) works together to define acceptance criteria before development begins. These criteria become automated tests that drive the development process.

**Key Principles:**
- **Collaborative Definition**: All stakeholders participate in defining acceptance criteria
- **Test-First Approach**: Write acceptance tests before implementation
- **Executable Specifications**: Acceptance criteria become automated tests
- **Continuous Feedback**: Tests provide immediate feedback on feature completeness

### 2. ATDD Process Flow

```
1. Discuss Feature → 2. Define Acceptance Criteria → 3. Write Acceptance Tests → 4. Implement Feature → 5. Run Tests → 6. Refine
```

### 3. User Stories with Acceptance Criteria

#### 3.1 Epic: English Vocabulary Learning System

**Epic Description**: As an English learner who speaks Traditional Chinese, I want a mobile app that helps me learn English vocabulary through spaced repetition, so that I can improve my English language skills effectively.

#### 3.2 User Story 1: Word Search and Definition Selection

**Story**: As an English learner, I want to search for English words and see their Traditional Chinese translations, so that I can understand their meanings and save them for later study.

**Acceptance Criteria**:

**AC1: Successful Word Search**
```
Given I am on the dictionary search screen
When I enter "cook" in the search field
And I tap the search button
Then I should see multiple definition options
And each definition should display:
  - Traditional Chinese translation
  - Part of speech (noun, verb, etc.)
  - Example sentence in English
And the definitions should include at least:
  - "做飯，烹調" for verb usage
  - "廚師" for noun usage
```

**AC2: Definition Selection**
```
Given I have search results displayed
When I select the "廚師" definition
Then the definition should be highlighted
And the save button should become enabled
And no other definitions should be selected
```

**AC3: Save Word Functionality**
```
Given I have selected a definition
When I tap the save button
Then the word should be saved to the "Not revised" stage
And I should see a success message "Word saved successfully"
And the search field should be cleared
And I should be able to search for another word
```

**AC4: Input Validation**
```
Given I am on the dictionary search screen
When I tap the search button with an empty search field
Then no API call should be made
And I should see the message "Please enter a word to search"
And the search field should be highlighted in red
```

**AC5: Network Error Handling**
```
Given there is no internet connection
When I search for a word
Then I should see the message "No internet connection. Please check your connection and try again."
And I should see a "Retry" button
When I tap "Retry" after connection is restored
Then the search should be performed automatically
```

**AC6: Word Not Found**
```
Given I am connected to the internet
When I search for a non-existent word like "xyzabc123"
Then I should see the message "Word not found. Please try a different word."
And the search field should remain filled with my search term
And I should be able to modify the search and try again
```

#### 3.3 User Story 2: Vocabulary Revision System

**Story**: As an English learner, I want to practice my saved vocabulary words through a spaced repetition system, so that I can improve my retention and gradually master the words.

**Acceptance Criteria**:

**AC1: Revision Stage Selection**
```
Given I have words saved in different stages
When I navigate to the revision screen
Then I should see a dropdown to select revision stages
And the dropdown should contain:
  - "Not revised"
  - "1st"
  - "2nd" 
  - "3rd"
  - "4th"
  - "5th or above"
And "Not revised" should be selected by default
```

**AC2: Word Display for Revision**
```
Given I have selected a revision stage with available words
When the revision starts
Then I should see:
  - The Traditional Chinese translation of the word
  - The part of speech (word class) of the word
  - An example sentence with the English word replaced by "___"
  - An input field to enter the English word
  - A submit button
And the example should be like "She's a wonderful ___." for the word "cook"
```

**AC3: Correct Answer Handling with Continue Button**
```
Given I am reviewing a word from "Not revised" stage
And the correct answer is "cook"
When I enter "cook" in the input field
And I tap submit
Then I should see "Correct!" message
And I should see a "Continue to Next Word" button
And the submit button should be disabled
And the word should move to "1st" stage
When I tap the "Continue to Next Word" button
Then the feedback should be hidden
And the next word from "Not revised" should be displayed
And my progress should update
And the submit button should be re-enabled
```

**AC4: Incorrect Answer Handling with Continue Button**
```
Given I am reviewing a word from "1st" stage
And the correct answer is "cook"
When I enter "book" in the input field
And I tap submit
Then I should see "Incorrect. The correct answer is: cook"
And I should see a "Continue to Next Word" button
And the submit button should be disabled
And the word should remain in "1st" stage but move to the end of the queue
When I tap the "Continue to Next Word" button
Then the feedback should be hidden
And the next word from "1st" stage should be displayed
And the submit button should be re-enabled
```

**AC5: Case Insensitive Validation**
```
Given the correct answer is "cook"
When I enter any of these variations: "COOK", "Cook", "cOOk"
Then my answer should be marked as correct
```

**AC6: Whitespace Handling**
```
Given the correct answer is "cook"
When I enter " cook ", "cook ", or " cook"
Then my answer should be marked as correct
```

**AC7: Stage 5 Behavior**
```
Given I am reviewing a word from "5th or above" stage
When I answer correctly
Then the word should remain in "5th or above" stage
And should move to the end of the "5th or above" queue
```

**AC8: Continue Button Initial State**
```
Given I am on the revision screen with a word displayed
When the word first appears
Then the "Continue to Next Word" button should not be visible
And the submit button should be enabled
And no answer feedback should be shown
```

**AC9: Empty Stage Handling**
```
Given I select a stage that has no words
Then I should see "No words in this stage yet"
And I should see options to:
  - Select a different stage
  - Go to dictionary search to add new words
```

#### 3.4 User Story 3: Navigation and User Experience

**Story**: As an English learner, I want intuitive navigation between app features, so that I can efficiently switch between learning new words and reviewing saved words.

**Acceptance Criteria**:

**AC1: Main Menu Display**
```
Given I open the app
Then I should see the main menu with:
  - App title "English Learning"
  - "English-Chinese-Traditional" button
  - "Revision" button
And both buttons should be clearly visible and tappable
```

**AC2: Navigation to Dictionary**
```
Given I am on the main menu
When I tap "English-Chinese-Traditional"
Then I should navigate to the dictionary search screen
And I should see the search interface
And there should be a back arrow to return to main menu
```

**AC3: Navigation to Revision**
```
Given I am on the main menu
When I tap "Revision"
Then I should navigate to the revision screen
And I should see the stage selection interface
And there should be a back arrow to return to main menu
```

**AC4: Back Navigation with Back Button**
```
Given I am on the dictionary or revision screen
When I tap the "Back to Main" button
Then I should return to the main menu immediately
And any unsaved data should be preserved
```

**AC4b: Back Navigation with System Back**
```
Given I am on any screen other than main menu
When I tap the system back arrow or use device back button
Then I should return to the main menu
And any unsaved data should be preserved
```

**AC4c: Back Navigation from Dictionary when accessed via Revision**
```
Given I am on the Revision screen
And I tap the "Add New Words" button
And I navigate to the Dictionary Search screen
When I tap the "Back to Main" button
Then I should return to the main menu directly
And I should not be taken back to the Revision screen
And the back stack should be properly managed
```

**AC5: Progress Indicators**
```
Given I am on the revision screen with multiple words in a stage
Then I should see progress like "Word 2 of 5"
And the progress should update as I answer questions
```

#### 3.5 User Story 4: Data Management and Word Deletion

**Story**: As an English learner, I want to be able to delete individual words or reset all my progress, so that I can manage my vocabulary list and start fresh when needed.

**Acceptance Criteria**:

**AC1: Delete All Words from Main Menu**
```
Given I am on the main menu screen
And I have words saved in multiple stages
When I tap the "Delete All Words" button
Then I should see a confirmation dialog
And the dialog should display "Are you sure you want to delete all saved words? This action cannot be undone."
When I tap "Delete" to confirm
Then all words should be removed from all stages
And I should see a success message "All words have been deleted successfully"
And I should remain on the main menu screen
```

**AC2: Cancel Delete All Operation**
```
Given I am on the main menu screen
And I have words saved in multiple stages
When I tap the "Delete All Words" button
And I see the confirmation dialog
When I tap "Cancel"
Then no words should be deleted
And I should return to the main menu
And no success or error message should be displayed
```

**AC3: Delete Individual Word During Revision**
```
Given I am on the revision screen
And there is a word "cook" being displayed for review
When I tap the "Delete Word" button
Then I should see a confirmation dialog
And the dialog should display "Are you sure you want to delete this word?"
When I tap "Delete" to confirm
Then the word "cook" should be removed from the current stage
And I should see a success message "Word deleted successfully"
And the next word in the stage should be displayed (if available)
```

**AC4: Delete Last Word in Stage**
```
Given I am on the revision screen
And there is only one word "cook" in the current stage
When I tap the "Delete Word" button
And I confirm the deletion
Then the word "cook" should be removed
And I should see the empty state message "No words in this stage yet"
And I should see the "Add New Words" button
```

**AC5: Cancel Individual Word Deletion**
```
Given I am on the revision screen
And there is a word being displayed
When I tap the "Delete Word" button
And I see the confirmation dialog
When I tap "Cancel"
Then the word should remain in the current stage
And the word should continue to be displayed
And no messages should be displayed
```

**AC6: Error Handling for Delete Operations**
```
Given I am performing any delete operation
When a database error occurs during deletion
Then I should see an appropriate error message
And the data should remain unchanged
And I should be able to retry the operation
```

#### 3.6 User Story 5: App Distribution and Installation

**Story**: As a user, I want to easily download and install the app from GitHub on my Android device, so that I can start learning English without needing Google Play Store access.

**Acceptance Criteria**:

**AC1: Successful APK Download from GitHub Releases**
```
Given I navigate to the GitHub repository releases page
And there is a published release version (e.g., v1.0.0)
When I view the release assets
Then I should see an APK file named "EnglishLearning-v1.0.0.apk"
And the file size should be approximately 5-20 MB
And the release should include auto-generated release notes
And the APK download link should be functional
```

**AC2: Successful APK Installation on Android Device**
```
Given I have downloaded the APK file to my Android device
And my device runs Android 7.0 (API 24) or higher
When I tap on the APK file to install
And I allow installation from unknown sources (if prompted)
And I proceed through the installation prompts
Then the app should install successfully within 30 seconds
And I should see "App installed" confirmation
And the app icon should appear in my app drawer
```

**AC3: App Launch After Installation**
```
Given I have successfully installed the app
When I tap the app icon from the app drawer
Then the app should launch within 2 seconds
And I should see the main menu screen
And all navigation options should be visible and functional
And no crash or error dialogs should appear
```

**AC4: GitHub Actions Build Success**
```
Given code changes are pushed to the main branch
When GitHub Actions workflow is triggered
Then the build job should complete successfully within 10 minutes
And a debug APK artifact should be created
And the artifact should be available for download for 30 days
And the workflow status should show as "passing"
```

**AC5: Automated Release Creation**
```
Given a new version tag is pushed (e.g., v1.0.1)
When the release workflow is triggered
Then a GitHub release should be created automatically
And the release APK should be attached to the release
And the APK should be named with the correct version
And release notes should be auto-generated from commits
And the release should be marked as "Latest"
```

**AC6: No Sensitive Data Exposure**
```
Given the repository is public on GitHub
When reviewing all committed files
Then no API keys should be present in the codebase
And no local.properties file should be committed
And no signing keystore files should be committed
And no personal information should be exposed
And all build artifacts should be gitignored
```

**AC7: README Installation Instructions**
```
Given a new user visits the GitHub repository
When they view the README.md file
Then they should see clear download instructions
And system requirements should be listed
And installation steps should be numbered and detailed
And links to releases and actions should be functional
And screenshots or instructions for enabling unknown sources should be included
```

### 4. Acceptance Test Implementation

#### 4.1 Test Structure Overview

```kotlin
@RunWith(AndroidJUnit4::class)
class AcceptanceTests {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    // Each user story becomes a test class
    // Each acceptance criteria becomes a test method
}
```

#### 4.2 Word Search Acceptance Tests

```kotlin
class WordSearchAcceptanceTests {
    
    private lateinit var mockWebServer: MockWebServer
    private lateinit var dictionaryPage: DictionarySearchPage
    
    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        dictionaryPage = DictionarySearchPage()
        // Configure app to use mock server
    }
    
    @Test
    fun ac1_successful_word_search() {
        // Given I am on the dictionary search screen
        navigateToMainMenu()
        clickDictionaryOption()
        
        // Setup mock API response
        mockWebServer.enqueue(createSuccessResponse("cook"))
        
        // When I enter "cook" in the search field
        dictionaryPage.enterSearchTerm("cook")
        
        // And I tap the search button
        dictionaryPage.clickSearchButton()
        
        // Then I should see multiple definition options
        dictionaryPage.verifyDefinitionsCount(2)
        
        // And each definition should display required information
        dictionaryPage.verifyDefinitionDisplayed("做飯，烹調", "verb", "She cooks dinner every evening.")
        dictionaryPage.verifyDefinitionDisplayed("廚師", "noun", "She's a wonderful cook.")
    }
    
    @Test
    fun ac2_definition_selection() {
        // Given I have search results displayed
        performSuccessfulWordSearch("cook")
        
        // When I select the "廚師" definition
        dictionaryPage.selectDefinition("廚師")
        
        // Then the definition should be highlighted
        dictionaryPage.verifyDefinitionHighlighted("廚師")
        
        // And the save button should become enabled
        dictionaryPage.verifySaveButtonEnabled(true)
        
        // And no other definitions should be selected
        dictionaryPage.verifyDefinitionNotSelected("做飯，烹調")
    }
    
    @Test
    fun ac3_save_word_functionality() {
        // Given I have selected a definition
        performDefinitionSelection("cook", "廚師")
        
        // When I tap the save button
        dictionaryPage.clickSaveButton()
        
        // Then the word should be saved to the "Not revised" stage
        // (This would be verified through database or subsequent revision check)
        
        // And I should see a success message
        dictionaryPage.verifySuccessMessage("Word saved successfully")
        
        // And the search field should be cleared
        dictionaryPage.verifySearchFieldEmpty()
        
        // And I should be able to search for another word
        dictionaryPage.verifySearchFieldEnabled()
    }
    
    @Test
    fun ac4_input_validation() {
        // Given I am on the dictionary search screen
        navigateToMainMenu()
        clickDictionaryOption()
        
        // When I tap the search button with an empty search field
        dictionaryPage.clickSearchButton()
        
        // Then no API call should be made
        verifyNoApiCall()
        
        // And I should see the validation message
        dictionaryPage.verifyValidationMessage("Please enter a word to search")
        
        // And the search field should be highlighted in red
        dictionaryPage.verifySearchFieldHighlighted()
    }
    
    @Test
    fun ac5_network_error_handling() {
        // Given there is no internet connection
        mockWebServer.enqueue(createNetworkErrorResponse())
        
        navigateToMainMenu()
        clickDictionaryOption()
        
        // When I search for a word
        dictionaryPage.enterSearchTerm("cook")
        dictionaryPage.clickSearchButton()
        
        // Then I should see the network error message
        dictionaryPage.verifyErrorMessage("No internet connection. Please check your connection and try again.")
        
        // And I should see a "Retry" button
        dictionaryPage.verifyRetryButtonVisible()
        
        // When I tap "Retry" after connection is restored
        mockWebServer.enqueue(createSuccessResponse("cook"))
        dictionaryPage.clickRetryButton()
        
        // Then the search should be performed automatically
        dictionaryPage.verifyDefinitionsDisplayed()
    }
    
    @Test
    fun ac6_word_not_found() {
        // Given I am connected to the internet
        mockWebServer.enqueue(createNotFoundResponse())
        
        navigateToMainMenu()
        clickDictionaryOption()
        
        // When I search for a non-existent word
        dictionaryPage.enterSearchTerm("xyzabc123")
        dictionaryPage.clickSearchButton()
        
        // Then I should see the not found message
        dictionaryPage.verifyErrorMessage("Word not found. Please try a different word.")
        
        // And the search field should remain filled
        dictionaryPage.verifySearchFieldContains("xyzabc123")
        
        // And I should be able to modify and try again
        dictionaryPage.verifySearchFieldEditable()
    }
    
    private fun createSuccessResponse(word: String): MockResponse {
        val response = """
            {
                "word": "$word",
                "definitions": [
                    {
                        "translation": "做飯，烹調",
                        "partOfSpeech": "verb",
                        "example": "She cooks dinner every evening."
                    },
                    {
                        "translation": "廚師",
                        "partOfSpeech": "noun",
                        "example": "She's a wonderful cook."
                    }
                ]
            }
        """.trimIndent()
        
        return MockResponse()
            .setResponseCode(200)
            .setBody(response)
            .addHeader("Content-Type", "application/json")
    }
    
    private fun createNetworkErrorResponse(): MockResponse {
        return MockResponse()
            .setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
    }
    
    private fun createNotFoundResponse(): MockResponse {
        return MockResponse()
            .setResponseCode(404)
            .setBody("Word not found")
    }
}
```

#### 4.3 Revision System Acceptance Tests

```kotlin
class RevisionSystemAcceptanceTests {
    
    private lateinit var database: WordDatabase
    private lateinit var wordDao: WordDao
    private lateinit var revisionPage: RevisionPage
    
    @Before
    fun setup() = runBlocking {
        setupTestDatabase()
        revisionPage = RevisionPage()
        seedTestData()
    }
    
    @Test
    fun ac1_revision_stage_selection() {
        // Given I have words saved in different stages
        verifyTestDataExists()
        
        // When I navigate to the revision screen
        navigateToMainMenu()
        clickRevisionOption()
        
        // Then I should see stage dropdown with all options
        revisionPage.verifyStageDropdownExists()
        revisionPage.verifyStageOptions(listOf(
            "Not revised", "1st", "2nd", "3rd", "4th", "5th or above"
        ))
        
        // And "Not revised" should be selected by default
        revisionPage.verifySelectedStage("Not revised")
    }
    
    @Test
    fun ac2_word_display_for_revision() {
        // Given I have selected a revision stage with available words
        addTestWord("cook", "廚師", "She's a wonderful cook.", 0)
        
        navigateToMainMenu()
        clickRevisionOption()
        
        // When the revision starts
        // Then I should see all required elements
        revisionPage.verifyChineseTranslationDisplayed("廚師")
        revisionPage.verifyPartOfSpeechDisplayed("noun")
        revisionPage.verifyExampleSentenceDisplayed("She's a wonderful ___.")
        revisionPage.verifyAnswerInputExists()
        revisionPage.verifySubmitButtonExists()
    }
    
    @Test
    fun ac3_correct_answer_handling() = runBlocking {
        // Given I am reviewing a word from "Not revised" stage
        val testWord = addTestWord("cook", "廚師", "She's a wonderful cook.", 0)
        
        navigateToMainMenu()
        clickRevisionOption()
        
        // When I enter "cook" and submit
        revisionPage.enterAnswer("cook")
        revisionPage.clickSubmitButton()
        
        // Then I should see correct message
        revisionPage.verifyAnswerFeedback("Correct!")
        
        // And the word should move to "1st" stage
        delay(1000) // Wait for database update
        val updatedWord = wordDao.getWordById(testWord.id)
        assertThat(updatedWord?.revisionStage).isEqualTo(1)
        
        // And next word should be displayed (if available)
        // Progress should update
    }
    
    @Test
    fun ac4_incorrect_answer_handling() = runBlocking {
        // Given I am reviewing a word from "1st" stage
        val testWord = addTestWord("cook", "廚師", "She's a wonderful cook.", 1)
        
        navigateToMainMenu()
        clickRevisionOption()
        revisionPage.selectStage("1st")
        
        // When I enter incorrect answer
        revisionPage.enterAnswer("book")
        revisionPage.clickSubmitButton()
        
        // Then I should see incorrect message with correct answer
        revisionPage.verifyAnswerFeedback("Incorrect. The correct answer is: cook")
        
        // And the word should remain in "1st" stage
        delay(1000)
        val updatedWord = wordDao.getWordById(testWord.id)
        assertThat(updatedWord?.revisionStage).isEqualTo(1)
    }
    
    @Test
    fun ac5_case_insensitive_validation() {
        // Given the correct answer is "cook"
        addTestWord("cook", "廚師", "She's a wonderful cook.", 0)
        
        navigateToMainMenu()
        clickRevisionOption()
        
        // When I enter variations like "COOK", "Cook", "cOOk"
        val variations = listOf("COOK", "Cook", "cOOk", "CooK")
        
        variations.forEach { variation ->
            revisionPage.clearAnswer()
            revisionPage.enterAnswer(variation)
            revisionPage.clickSubmitButton()
            
            // Then each should be marked as correct
            revisionPage.verifyAnswerFeedback("Correct!")
            
            // Reset for next variation
            setupNextWord()
        }
    }
    
    @Test
    fun ac6_whitespace_handling() {
        // Given the correct answer is "cook"
        addTestWord("cook", "廚師", "She's a wonderful cook.", 0)
        
        navigateToMainMenu()
        clickRevisionOption()
        
        // When I enter variations with whitespace
        val variations = listOf(" cook ", "cook ", " cook", "\tcook\n")
        
        variations.forEach { variation ->
            revisionPage.clearAnswer()
            revisionPage.enterAnswer(variation)
            revisionPage.clickSubmitButton()
            
            // Then each should be marked as correct
            revisionPage.verifyAnswerFeedback("Correct!")
            
            setupNextWord()
        }
    }
    
    @Test
    fun ac7_stage_5_behavior() = runBlocking {
        // Given I am reviewing a word from "5th or above" stage
        val testWord = addTestWord("cook", "廚師", "She's a wonderful cook.", 5)
        
        navigateToMainMenu()
        clickRevisionOption()
        revisionPage.selectStage("5th or above")
        
        // When I answer correctly
        revisionPage.enterAnswer("cook")
        revisionPage.clickSubmitButton()
        
        // Then the word should remain in "5th or above" stage
        delay(1000)
        val updatedWord = wordDao.getWordById(testWord.id)
        assertThat(updatedWord?.revisionStage).isEqualTo(5)
    }
    
    @Test
    fun ac8_empty_stage_handling() {
        // Given I select a stage that has no words
        navigateToMainMenu()
        clickRevisionOption()
        revisionPage.selectStage("3rd") // Assuming no words in 3rd stage
        
        // Then I should see appropriate message and options
        revisionPage.verifyEmptyStageMessage("No words in this stage yet")
        revisionPage.verifyOptionToSelectDifferentStage()
        revisionPage.verifyOptionToAddNewWords()
    }
    
    private suspend fun setupTestDatabase() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            WordDatabase::class.java
        ).allowMainThreadQueries().build()
        
        wordDao = database.wordDao()
    }
    
    private suspend fun addTestWord(
        english: String,
        chinese: String, 
        example: String,
        stage: Int
    ): Word {
        val word = Word(
            englishWord = english,
            chineseTranslation = chinese,
            partOfSpeech = "noun",
            exampleSentence = example,
            blankExampleSentence = example.replace(english, "___"),
            revisionStage = stage,
            createdAt = System.currentTimeMillis(),
            lastRevisedAt = System.currentTimeMillis()
        )
        
        val id = wordDao.insert(word)
        return word.copy(id = id)
    }
}
```

### 5. Navigation Acceptance Tests

```kotlin
class NavigationAcceptanceTests {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun ac1_main_menu_display() {
        // Given I open the app
        // Then I should see the main menu elements
        onView(withText("English Learning"))
            .check(matches(isDisplayed()))
        
        onView(withText("English-Chinese-Traditional"))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
        
        onView(withText("Revision"))
            .check(matches(isDisplayed()))
            .check(matches(isClickable()))
    }
    
    @Test
    fun ac2_navigation_to_dictionary() {
        // Given I am on the main menu
        // When I tap "English-Chinese-Traditional"
        onView(withText("English-Chinese-Traditional"))
            .perform(click())
        
        // Then I should navigate to dictionary search screen
        onView(withId(R.id.searchEditText))
            .check(matches(isDisplayed()))
        
        onView(withId(R.id.searchButton))
            .check(matches(isDisplayed()))
        
        // And there should be back navigation
        onView(withContentDescription("Navigate up"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun ac3_navigation_to_revision() {
        // Given I am on the main menu
        // When I tap "Revision"
        onView(withText("Revision"))
            .perform(click())
        
        // Then I should navigate to revision screen
        onView(withId(R.id.stageSpinner))
            .check(matches(isDisplayed()))
        
        // And there should be back navigation
        onView(withContentDescription("Navigate up"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun ac4_back_navigation() {
        // Given I am on dictionary screen
        onView(withText("English-Chinese-Traditional"))
            .perform(click())
        
        // When I tap back arrow
        onView(withContentDescription("Navigate up"))
            .perform(click())
        
        // Then I should return to main menu
        onView(withText("English-Chinese-Traditional"))
            .check(matches(isDisplayed()))
        onView(withText("Revision"))
            .check(matches(isDisplayed()))
        
        // Test with device back button as well
        onView(withText("Revision"))
            .perform(click())
        
        pressBack()
        
        onView(withText("English-Chinese-Traditional"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun ac5_progress_indicators() = runBlocking {
        // Given I have multiple words in a stage
        setupMultipleWordsInStage(stage = 1, count = 5)
        
        // Navigate to revision
        onView(withText("Revision"))
            .perform(click())
        
        onView(withId(R.id.stageSpinner))
            .perform(click())
        onView(withText("1st"))
            .perform(click())
        
        // Then I should see progress
        onView(withText("Word 1 of 5"))
            .check(matches(isDisplayed()))
        
        // Answer correctly and check progress update
        onView(withId(R.id.answerEditText))
            .perform(typeText("testword"))
        onView(withId(R.id.submitButton))
            .perform(click())
        
        // Progress should update
        onView(withText("Word 2 of 5"))
            .check(matches(isDisplayed()))
    }
}
```

### 6. Acceptance Test Configuration

#### 6.1 Test Dependencies and Setup

```kotlin
// app/build.gradle.kts
android {
    defaultConfig {
        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments clearPackageData: 'true'
    }
    
    testOptions {
        execution 'ANDROIDX_TEST_ORCHESTRATOR'
    }
}

dependencies {
    // ATDD Testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
    androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
    
    // Database Testing
    androidTestImplementation("androidx.room:room-testing:2.6.1")
    
    // Network Testing
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    
    // Test Orchestrator
    androidTestUtil("androidx.test:orchestrator:1.4.2")
}
```

#### 6.2 Test Suite Organization

```kotlin
@RunWith(Suite::class)
@Suite.SuiteClasses(
    WordSearchAcceptanceTests::class,
    RevisionSystemAcceptanceTests::class,
    NavigationAcceptanceTests::class,
    DataPersistenceAcceptanceTests::class
)
class AcceptanceTestSuite
```

### 7. Traceability Matrix

| User Story | Acceptance Criteria | Test Method | Status |
|------------|-------------------|-------------|--------|
| US1: Word Search | AC1: Successful Search | `ac1_successful_word_search()` | ✅ |
| US1: Word Search | AC2: Definition Selection | `ac2_definition_selection()` | ✅ |
| US1: Word Search | AC3: Save Functionality | `ac3_save_word_functionality()` | ✅ |
| US1: Word Search | AC4: Input Validation | `ac4_input_validation()` | ✅ |
| US1: Word Search | AC5: Network Error | `ac5_network_error_handling()` | ✅ |
| US1: Word Search | AC6: Word Not Found | `ac6_word_not_found()` | ✅ |
| US2: Revision | AC1: Stage Selection | `ac1_revision_stage_selection()` | ✅ |
| US2: Revision | AC2: Word Display | `ac2_word_display_for_revision()` | ✅ |
| US2: Revision | AC3: Correct Answer with Continue | `ac3_correct_answer_with_continue()` | 🔄 |
| US2: Revision | AC4: Incorrect Answer with Continue | `ac4_incorrect_answer_with_continue()` | 🔄 |
| US2: Revision | AC5: Case Insensitive | `ac5_case_insensitive_validation()` | ✅ |
| US2: Revision | AC6: Whitespace | `ac6_whitespace_handling()` | ✅ |
| US2: Revision | AC7: Stage 5 Behavior | `ac7_stage_5_behavior()` | ✅ |
| US2: Revision | AC8: Continue Button Initial | `ac8_continue_button_initial_state()` | 🔄 |
| US2: Revision | AC9: Empty Stage | `ac9_empty_stage_handling()` | ✅ |
| US3: Navigation | AC1: Main Menu | `ac1_main_menu_display()` | ✅ |
| US3: Navigation | AC2: To Dictionary | `ac2_navigation_to_dictionary()` | ✅ |
| US3: Navigation | AC3: To Revision | `ac3_navigation_to_revision()` | ✅ |
| US3: Navigation | AC4: Back Navigation with Button | `ac4_back_navigation_button()` | 🔄 |
| US3: Navigation | AC4b: Back Navigation System | `ac4b_back_navigation_system()` | ✅ |
| US3: Navigation | AC5: Progress Indicators | `ac5_progress_indicators()` | ✅ |

### 8. ATDD Best Practices

#### 8.1 Collaborative Definition
- Include all stakeholders in acceptance criteria definition
- Use plain language that everyone understands
- Focus on user behavior, not implementation details
- Make criteria specific and measurable

#### 8.2 Test Automation
- Automate acceptance tests as soon as criteria are defined
- Keep tests independent and isolated
- Use meaningful test names that reflect acceptance criteria
- Maintain test data separately from test logic

#### 8.3 Continuous Feedback
- Run acceptance tests as part of CI/CD pipeline
- Provide immediate feedback to development team
- Use test results to validate feature completeness
- Update tests when requirements change

#### 8.4 Test Maintenance
- Keep tests up-to-date with changing requirements
- Refactor tests when application changes
- Use Page Object pattern for maintainable UI tests
- Regular review and cleanup of obsolete tests

### 9. ATDD Success Metrics

#### 9.1 Coverage Metrics
- **Acceptance Criteria Coverage**: 100% of defined criteria have tests
- **User Story Coverage**: All user stories have acceptance tests
- **Feature Coverage**: All features tested end-to-end

#### 9.2 Quality Metrics
- **Pass Rate**: >95% of acceptance tests passing
- **Stability**: <5% flaky test rate
- **Execution Time**: Complete suite runs in <30 minutes
- **Maintenance Effort**: <20% of development time spent on test maintenance

#### 9.3 Collaboration Metrics
- **Stakeholder Participation**: All user stories have stakeholder sign-off
- **Defect Escape Rate**: <2% defects found in production
- **Requirements Clarity**: <10% requirements changes after AC definition

### 10. ATDD Tools and Integration

#### 10.1 Test Reporting
```kotlin
// Generate detailed test reports
@Test
fun ac1_successful_word_search() {
    TestReporter.startTest("AC1: Successful Word Search")
    try {
        // Test implementation
        TestReporter.logStep("Navigate to dictionary search")
        TestReporter.logStep("Enter search term")
        TestReporter.logStep("Verify results")
        TestReporter.passTest()
    } catch (e: Exception) {
        TestReporter.failTest(e.message)
        throw e
    }
}
```

#### 10.2 CI/CD Integration
```yaml
# GitHub Actions workflow for ATDD
name: Acceptance Tests

on:
  pull_request:
    branches: [ main ]

jobs:
  acceptance-tests:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Run Acceptance Tests
      run: ./gradlew connectedAndroidTest
    
    - name: Generate Test Report
      run: ./gradlew testReport
    
    - name: Upload Test Results
      uses: actions/upload-artifact@v3
      with:
        name: acceptance-test-results
        path: app/build/reports/androidTests/
```

---

**Remember**: ATDD is about ensuring that what we build is what the customer actually wants. The acceptance tests serve as both validation and documentation of the system's behavior from the user's perspective.
