# Behavior-Driven Development (BDD) Approach
## English Learning Android App

### 1. BDD Overview

Behavior-Driven Development (BDD) is a software development approach that encourages collaboration between developers, QA, and business stakeholders. BDD focuses on defining the behavior of the application from the user's perspective using natural language scenarios.

**Key Principles:**
- **Ubiquitous Language**: Shared understanding between all stakeholders
- **Outside-In Development**: Start with user behavior, work inward
- **Executable Specifications**: Tests serve as living documentation
- **Collaboration**: Bridge the gap between business and technical teams

### 2. BDD Framework Structure

BDD scenarios follow the **Given-When-Then** format:
- **Given**: Initial context/state (preconditions)
- **When**: The action/event that triggers behavior
- **Then**: The expected outcome/result
- **And**: Additional conditions or outcomes
- **But**: Exceptions or alternative outcomes

### 3. Feature Files and Scenarios

#### 3.1 Feature: Word Search and Definition Selection

```gherkin
Feature: Word Search and Definition Selection
  As an English learner
  I want to search for English words and see Traditional Chinese translations
  So that I can learn new vocabulary effectively

  Background:
    Given the user has opened the English Learning app
    And the user is on the main menu screen

  Scenario: Successful word search with multiple definitions
    Given the user navigates to the Dictionary Search screen
    When the user enters "cook" in the search field
    And the user taps the search button
    Then the app should display multiple definition options
    And each definition should show the Traditional Chinese translation
    And each definition should show the part of speech
    And each definition should show an example sentence
    And the definitions should include "做飯，烹調" for verb usage
    And the definitions should include "廚師" for noun usage

  Scenario: User selects a definition and saves the word
    Given the user has searched for the word "cook"
    And multiple definitions are displayed
    When the user selects the definition "廚師" (noun)
    And the user taps the save button
    Then the word should be saved to the "Not revised" stage
    And a success message should be displayed
    And the user should be able to search for another word

  Scenario: Search with no internet connection
    Given the device has no internet connection
    And the user is on the Dictionary Search screen
    When the user enters "cook" in the search field
    And the user taps the search button
    Then an error message should be displayed
    And the error message should say "No Internet"
    And the user should see an option to retry

  Scenario: Search for a word that doesn't exist
    Given the user is on the Dictionary Search screen
    When the user enters "xyzabc123" in the search field
    And the user taps the search button
    Then an error message should be displayed
    And the error message should say "Word not found"
    And the user should be able to try a different word

  Scenario: Empty search input validation
    Given the user is on the Dictionary Search screen
    When the user leaves the search field empty
    And the user taps the search button
    Then no API call should be made
    And the search field should show a validation message
    And the message should say "Please enter a word to search"

  Scenario: Save button is disabled until definition is selected
    Given the user has searched for a word successfully
    And multiple definitions are displayed
    Then the save button should be disabled
    When the user selects any definition
    Then the save button should become enabled
    When the user deselects the definition
    Then the save button should become disabled again
```

#### 3.2 Feature: Vocabulary Revision System

```gherkin
Feature: Vocabulary Revision System
  As an English learner
  I want to practice my saved vocabulary words
  So that I can improve my retention and move words through learning stages

  Background:
    Given the user has saved some words in different revision stages
    And the user is on the main menu screen

  Scenario: Start revision for "Not revised" stage
    Given there are words in the "Not revised" stage
    When the user navigates to the Revision screen
    Then the stage selector should default to "Not revised"
    And the first word from "Not revised" stage should be displayed
    And the Chinese translation should be visible
    And the part of speech should be visible
    And an example sentence with a blank should be shown
    And an input field for the English word should be available

  Scenario: Correct answer shows continue button
    Given the user is on the Revision screen
    And a word from "Not revised" stage is displayed
    And the Chinese translation is "廚師"
    And the example sentence is "She's a wonderful ___."
    When the user enters "cook" in the input field
    And the user taps the submit button
    Then the answer should be marked as correct
    And a "Continue to Next Word" button should appear
    And the submit button should be disabled
    And the word should move to "1st" stage
    When the user taps the "Continue to Next Word" button
    Then the feedback should be hidden
    And the continue button should be hidden
    And the next word from "Not revised" stage should be displayed
    And the submit button should be re-enabled

  Scenario: Incorrect answer shows continue button with correct answer
    Given the user is on the Revision screen
    And a word from "1st" stage is displayed
    And the correct answer is "cook"
    When the user enters "book" in the input field
    And the user taps the submit button
    Then the answer should be marked as incorrect
    And the correct answer "cook" should be displayed
    And a "Continue to Next Word" button should appear
    And the submit button should be disabled
    And the word should remain in "1st" stage
    When the user taps the "Continue to Next Word" button
    Then the feedback should be hidden
    And the continue button should be hidden
    And the next word from "1st" stage should be displayed
    And the submit button should be re-enabled

  Scenario: Continue button not visible initially
    Given the user is on the Revision screen
    And a word is displayed
    Then the "Continue to Next Word" button should not be visible
    And the submit button should be enabled
    And no answer feedback should be shown

  Scenario: Case-insensitive answer validation
    Given the user is on the Revision screen
    And a word is displayed with correct answer "cook"
    When the user enters "COOK" in the input field
    And the user taps the submit button
    Then the answer should be marked as correct
    
  Scenario: Answer with extra spaces is accepted
    Given the user is on the Revision screen
    And a word is displayed with correct answer "cook"
    When the user enters " cook " in the input field
    And the user taps the submit button
    Then the answer should be marked as correct

  Scenario: Word in "5th or above" stage stays in same stage
    Given the user is on the Revision screen
    And a word from "5th or above" stage is displayed
    When the user enters the correct answer
    And the user taps the submit button
    Then the word should remain in "5th or above" stage
    And the next word from "5th or above" stage should be displayed

  Scenario: Switch between revision stages
    Given the user is on the Revision screen
    And currently viewing "Not revised" stage
    When the user selects "2nd" from the stage dropdown
    Then the first word from "2nd" stage should be displayed
    And the stage indicator should show "2nd"

  Scenario: No words available in selected stage
    Given there are no words in the "3rd" stage
    When the user selects "3rd" from the stage dropdown
    Then a message should be displayed saying "No words in this stage"
    And an option to select a different stage should be available
    And an option to add new words should be available

  Scenario: Track progress within a stage
    Given there are 5 words in the "1st" stage
    And the user is on the Revision screen for "1st" stage
    Then a progress indicator should show "1 of 5"
    When the user answers the first word correctly
    And moves to the next word
    Then the progress indicator should show "2 of 5"
```

#### 3.3 Feature: Navigation and User Experience

```gherkin
Feature: Navigation and User Experience
  As an English learner
  I want to navigate easily between app features
  So that I can efficiently use the learning tools

  Scenario: Main menu navigation
    Given the user opens the app
    Then the main menu should display two options
    And one option should be "English-Chinese-Traditional"
    And one option should be "Revision"
    And both options should be clearly labeled and accessible

  Scenario: Navigate to Dictionary Search
    Given the user is on the main menu
    When the user taps "English-Chinese-Traditional"
    Then the Dictionary Search screen should open
    And the screen should have a search input field
    And the screen should have a search button
    And there should be a "Back to Main" button

  Scenario: Navigate to Revision
    Given the user is on the main menu
    When the user taps "Revision"
    Then the Revision screen should open
    And the screen should have a stage selection dropdown
    And there should be a "Back to Main" button

  Scenario: Back navigation from Dictionary Search using back button
    Given the user is on the Dictionary Search screen
    When the user taps the "Back to Main" button
    Then the user should return to the main menu immediately

  Scenario: Back navigation from Revision using back button
    Given the user is on the Revision screen
    When the user taps the "Back to Main" button
    Then the user should return to the main menu immediately

  Scenario: System back navigation
    Given the user is on the Dictionary Search screen
    When the user taps the system back button
    Then the user should return to the main menu

  Scenario: Navigation from Revision to Dictionary and back
    Given the user is on the Revision screen
    And there are no words in the current stage
    When the user taps the "Add New Words" button
    Then the Dictionary Search screen should open
    When the user taps the "Back to Main" button
    Then the user should return to the main menu immediately
    And the user should not be taken back to the Revision screen

  Scenario: App state persistence
    Given the user has searched for a word and selected a definition
    When the user navigates away from the app
    And returns to the app
    Then the selected definition should still be available
    And the user should be able to save the word
```

#### 3.4 Feature: Data Persistence and Stage Management

```gherkin
Feature: Data Persistence and Stage Management
  As an English learner
  I want my progress to be saved automatically
  So that I can continue learning where I left off

  Scenario: Word progression through all stages
    Given a new word "cook" is saved to "Not revised"
    When the user answers correctly in revision
    Then the word moves to "1st" stage
    When the user answers correctly again
    Then the word moves to "2nd" stage
    When the user answers correctly again
    Then the word moves to "3rd" stage
    When the user answers correctly again
    Then the word moves to "4th" stage
    When the user answers correctly again
    Then the word moves to "5th or above" stage
    When the user answers correctly again
    Then the word remains in "5th or above" stage

  Scenario: Word regression with incorrect answers
    Given a word is in "3rd" stage
    When the user answers incorrectly
    Then the word should remain in "3rd" stage
    And the word should move to the end of the "3rd" stage queue

  Scenario: Data persistence across app sessions
    Given the user has words in various stages
    When the user closes the app completely
    And reopens the app after some time
    Then all saved words should still be available
    And all words should be in their correct stages
    And the user should be able to continue revision

  Scenario: Multiple words in same stage ordering
    Given there are 3 words in "Not revised" stage
    And they were added in order: "cook", "book", "look"
    When the user starts revision for "Not revised"
    Then the first word shown should be "cook"
    When the user answers correctly
    Then the word "cook" moves to "1st" stage
    And the next word shown should be "book"
```

#### 3.5 Feature: Data Management and Word Deletion

```gherkin
Feature: Data Management and Word Deletion
  As an English learner
  I want to be able to delete words or reset my progress
  So that I can manage my vocabulary list effectively

  Background:
    Given the user has some saved words in different stages
    And the user is on the main menu screen

  Scenario: Delete all saved words from main menu
    Given the user has words saved in multiple stages
    When the user taps the "Delete All Words" button
    Then a confirmation dialog should appear
    And the dialog should show "Are you sure you want to delete all saved words? This action cannot be undone."
    When the user taps "Delete" in the confirmation dialog
    Then all words should be removed from all stages
    And a success message "All words have been deleted successfully" should be displayed
    And the user should remain on the main menu screen

  Scenario: Cancel delete all words operation
    Given the user has words saved in multiple stages
    When the user taps the "Delete All Words" button
    Then a confirmation dialog should appear
    When the user taps "Cancel" in the confirmation dialog
    Then no words should be deleted
    And the user should remain on the main menu screen
    And no success or error message should be displayed

  Scenario: Delete individual word during revision
    Given the user is on the revision screen
    And there is a word "cook" being displayed
    When the user taps the "Delete Word" button
    Then a confirmation dialog should appear
    And the dialog should show "Are you sure you want to delete this word?"
    When the user taps "Delete" in the confirmation dialog
    Then the word "cook" should be removed from the current stage
    And a success message "Word deleted successfully" should be displayed
    And the next word in the stage should be displayed
    
  Scenario: Delete individual word when it's the last word in stage
    Given the user is on the revision screen
    And there is only one word "cook" in the current stage
    When the user taps the "Delete Word" button
    And confirms the deletion
    Then the word "cook" should be removed
    And the empty state should be displayed
    And the message "No words in this stage yet" should be shown

  Scenario: Cancel delete individual word operation
    Given the user is on the revision screen
    And there is a word "cook" being displayed
    When the user taps the "Delete Word" button
    Then a confirmation dialog should appear
    When the user taps "Cancel" in the confirmation dialog
    Then the word "cook" should remain in the stage
    And the word should continue to be displayed
    And no success or error message should be displayed

  Scenario: Error handling during delete all operation
    Given the user taps the "Delete All Words" button
    And confirms the deletion
    When a database error occurs during deletion
    Then an error message "Failed to delete words. Please try again." should be displayed
    And the words should remain in their stages

  Scenario: Error handling during individual word deletion
    Given the user is on the revision screen
    And there is a word being displayed
    When the user taps the "Delete Word" button
    And confirms the deletion
    And a database error occurs
    Then an error message should be displayed
    And the word should remain in the current stage
```

### 4. BDD Test Implementation with Espresso

#### 4.1 Step Definitions for Dictionary Search

```kotlin
@RunWith(AndroidJUnit4::class)
class DictionarySearchSteps {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    private lateinit var mockWebServer: MockWebServer
    
    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        // Configure app to use mock server
    }
    
    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
    
    @Test
    fun successful_word_search_with_multiple_definitions() {
        // Given the user has opened the English Learning app
        // And the user is on the main menu screen
        onView(withText("English-Chinese-Traditional"))
            .check(matches(isDisplayed()))
        
        // Given the user navigates to the Dictionary Search screen
        onView(withText("English-Chinese-Traditional"))
            .perform(click())
        
        // Setup mock API response
        val mockResponse = """
            {
                "word": "cook",
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
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
        )
        
        // When the user enters "cook" in the search field
        onView(withId(R.id.searchEditText))
            .perform(typeText("cook"))
        
        // And the user taps the search button
        onView(withId(R.id.searchButton))
            .perform(click())
        
        // Then the app should display multiple definition options
        onView(withId(R.id.definitionsRecyclerView))
            .check(matches(isDisplayed()))
        
        // And each definition should show the Traditional Chinese translation
        onView(withText("做飯，烹調"))
            .check(matches(isDisplayed()))
        onView(withText("廚師"))
            .check(matches(isDisplayed()))
        
        // And each definition should show the part of speech
        onView(withText("verb"))
            .check(matches(isDisplayed()))
        onView(withText("noun"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun user_selects_definition_and_saves_word() {
        // Given the user has searched for the word "cook"
        // And multiple definitions are displayed
        performWordSearch("cook")
        
        // When the user selects the definition "廚師" (noun)
        onView(withText("廚師"))
            .perform(click())
        
        // Then the save button should be enabled
        onView(withId(R.id.saveButton))
            .check(matches(isEnabled()))
        
        // And the user taps the save button
        onView(withId(R.id.saveButton))
            .perform(click())
        
        // Then a success message should be displayed
        onView(withText("Word saved successfully"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun search_with_no_internet_connection() {
        // Given the device has no internet connection
        // Simulate network error
        mockWebServer.enqueue(
            MockResponse()
                .setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
        )
        
        // And the user is on the Dictionary Search screen
        navigateToDictionarySearch()
        
        // When the user enters "cook" in the search field
        onView(withId(R.id.searchEditText))
            .perform(typeText("cook"))
        
        // And the user taps the search button
        onView(withId(R.id.searchButton))
            .perform(click())
        
        // Then an error message should be displayed
        onView(withText("No Internet"))
            .check(matches(isDisplayed()))
        
        // And the user should see an option to retry
        onView(withText("Retry"))
            .check(matches(isDisplayed()))
    }
    
    private fun performWordSearch(word: String) {
        navigateToDictionarySearch()
        setupMockApiResponse(word)
        onView(withId(R.id.searchEditText))
            .perform(typeText(word))
        onView(withId(R.id.searchButton))
            .perform(click())
    }
    
    private fun navigateToDictionarySearch() {
        onView(withText("English-Chinese-Traditional"))
            .perform(click())
    }
    
    private fun setupMockApiResponse(word: String) {
        // Setup appropriate mock response based on word
    }
}
```

#### 4.2 Step Definitions for Revision System

```kotlin
@RunWith(AndroidJUnit4::class)
class RevisionSystemSteps {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    private lateinit var database: WordDatabase
    private lateinit var wordDao: WordDao
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            WordDatabase::class.java
        ).allowMainThreadQueries()
        .build()
        
        wordDao = database.wordDao()
        
        // Inject test database into app
        // This would require dependency injection setup
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun correct_answer_advances_word_to_next_stage() = runBlocking {
        // Given there are words in the "Not revised" stage
        val testWord = Word(
            englishWord = "cook",
            chineseTranslation = "廚師",
            partOfSpeech = "noun",
            exampleSentence = "She's a wonderful cook.",
            blankExampleSentence = "She's a wonderful ___.",
            revisionStage = 0
        )
        wordDao.insert(testWord)
        
        // And the user is on the Revision screen
        navigateToRevision()
        
        // Then the Chinese translation should be visible
        onView(withText("廚師"))
            .check(matches(isDisplayed()))
        
        // And an example sentence with a blank should be shown
        onView(withText("She's a wonderful ___."))
            .check(matches(isDisplayed()))
        
        // When the user enters "cook" in the input field
        onView(withId(R.id.answerEditText))
            .perform(typeText("cook"))
        
        // And the user taps the submit button
        onView(withId(R.id.submitButton))
            .perform(click())
        
        // Then the answer should be marked as correct
        onView(withText("Correct!"))
            .check(matches(isDisplayed()))
        
        // And the word should move to "1st" stage
        // Verify through database or UI feedback
        delay(1000) // Wait for UI updates
        
        // Check that the word moved to stage 1
        val updatedWords = wordDao.getWordsByStage(1).value
        assertThat(updatedWords).hasSize(1)
        assertThat(updatedWords?.get(0)?.englishWord).isEqualTo("cook")
    }
    
    @Test
    fun incorrect_answer_keeps_word_in_same_stage() = runBlocking {
        // Given a word in "1st" stage
        val testWord = Word(
            englishWord = "cook",
            chineseTranslation = "廚師",
            partOfSpeech = "noun",
            exampleSentence = "She's a wonderful cook.",
            blankExampleSentence = "She's a wonderful ___.",
            revisionStage = 1
        )
        wordDao.insert(testWord)
        
        // And the user is on the Revision screen
        navigateToRevision()
        selectStage(1)
        
        // When the user enters "book" (incorrect) in the input field
        onView(withId(R.id.answerEditText))
            .perform(typeText("book"))
        
        // And the user taps the submit button
        onView(withId(R.id.submitButton))
            .perform(click())
        
        // Then the answer should be marked as incorrect
        onView(withText("Incorrect"))
            .check(matches(isDisplayed()))
        
        // And the correct answer should be shown briefly
        onView(withText("Correct answer: cook"))
            .check(matches(isDisplayed()))
        
        // And the word should remain in "1st" stage
        delay(1000)
        val wordsInStage = wordDao.getWordsByStage(1).value
        assertThat(wordsInStage).hasSize(1)
        assertThat(wordsInStage?.get(0)?.englishWord).isEqualTo("cook")
    }
    
    @Test
    fun case_insensitive_answer_validation() = runBlocking {
        // Setup test word
        val testWord = createTestWord("cook", 0)
        wordDao.insert(testWord)
        
        navigateToRevision()
        
        // When the user enters "COOK" in uppercase
        onView(withId(R.id.answerEditText))
            .perform(typeText("COOK"))
        
        onView(withId(R.id.submitButton))
            .perform(click())
        
        // Then the answer should be marked as correct
        onView(withText("Correct!"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun switch_between_revision_stages() = runBlocking {
        // Given words in different stages
        wordDao.insert(createTestWord("cook", 0))
        wordDao.insert(createTestWord("book", 2))
        
        navigateToRevision()
        
        // Default should be "Not revised" (stage 0)
        onView(withText("cook"))
            .check(matches(isDisplayed()))
        
        // When the user selects "2nd" from the stage dropdown
        selectStage(2)
        
        // Then the first word from "2nd" stage should be displayed
        onView(withText("book"))
            .check(matches(isDisplayed()))
    }
    
    private fun navigateToRevision() {
        onView(withText("Revision"))
            .perform(click())
    }
    
    private fun selectStage(stage: Int) {
        onView(withId(R.id.stageSpinner))
            .perform(click())
        
        val stageText = when (stage) {
            0 -> "Not revised"
            1 -> "1st"
            2 -> "2nd"
            3 -> "3rd"
            4 -> "4th"
            5 -> "5th or above"
            else -> "Not revised"
        }
        
        onView(withText(stageText))
            .perform(click())
    }
    
    private fun createTestWord(word: String, stage: Int) = Word(
        englishWord = word,
        chineseTranslation = "測試",
        partOfSpeech = "noun",
        exampleSentence = "This is a $word example.",
        blankExampleSentence = "This is a ___ example.",
        revisionStage = stage,
        createdAt = System.currentTimeMillis(),
        lastRevisedAt = System.currentTimeMillis()
    )
}
```

### 5. BDD Test Organization

#### 5.1 Test Structure

```
androidTest/
├── features/
│   ├── DictionarySearchFeature.kt
│   ├── RevisionSystemFeature.kt
│   ├── NavigationFeature.kt
│   └── DataPersistenceFeature.kt
├── steps/
│   ├── DictionarySteps.kt
│   ├── RevisionSteps.kt
│   ├── NavigationSteps.kt
│   └── CommonSteps.kt
├── pageobjects/
│   ├── MainMenuPage.kt
│   ├── DictionarySearchPage.kt
│   └── RevisionPage.kt
└── utils/
    ├── TestDataHelper.kt
    ├── MockServerHelper.kt
    └── DatabaseTestHelper.kt
```

#### 5.2 Page Object Model

```kotlin
class DictionarySearchPage {
    
    fun enterSearchTerm(term: String) {
        onView(withId(R.id.searchEditText))
            .perform(typeText(term))
    }
    
    fun clickSearchButton() {
        onView(withId(R.id.searchButton))
            .perform(click())
    }
    
    fun selectDefinition(translation: String) {
        onView(withText(translation))
            .perform(click())
    }
    
    fun clickSaveButton() {
        onView(withId(R.id.saveButton))
            .perform(click())
    }
    
    fun verifyDefinitionsDisplayed(definitions: List<String>) {
        definitions.forEach { definition ->
            onView(withText(definition))
                .check(matches(isDisplayed()))
        }
    }
    
    fun verifyErrorMessage(message: String) {
        onView(withText(message))
            .check(matches(isDisplayed()))
    }
    
    fun verifySaveButtonState(enabled: Boolean) {
        onView(withId(R.id.saveButton))
            .check(matches(if (enabled) isEnabled() else not(isEnabled())))
    }
}

class RevisionPage {
    
    fun selectStage(stageName: String) {
        onView(withId(R.id.stageSpinner))
            .perform(click())
        onView(withText(stageName))
            .perform(click())
    }
    
    fun enterAnswer(answer: String) {
        onView(withId(R.id.answerEditText))
            .perform(typeText(answer))
    }
    
    fun clickSubmitButton() {
        onView(withId(R.id.submitButton))
            .perform(click())
    }
    
    fun verifyWordDisplayed(chineseTranslation: String) {
        onView(withText(chineseTranslation))
            .check(matches(isDisplayed()))
    }
    
    fun verifyExampleSentence(sentence: String) {
        onView(withText(sentence))
            .check(matches(isDisplayed()))
    }
    
    fun verifyAnswerResult(isCorrect: Boolean) {
        val expectedText = if (isCorrect) "Correct!" else "Incorrect"
        onView(withText(expectedText))
            .check(matches(isDisplayed()))
    }
    
    fun verifyProgressIndicator(current: Int, total: Int) {
        onView(withText("$current of $total"))
            .check(matches(isDisplayed()))
    }
}
```

### 6. BDD Testing Configuration

#### 6.1 Test Dependencies

```kotlin
// app/build.gradle.kts
androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
androidTestImplementation("androidx.test.uiautomator:uiautomator:2.2.0")
androidTestImplementation("androidx.test:runner:1.5.2")
androidTestImplementation("androidx.test:rules:1.5.0")
androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
```

#### 6.2 Test Runner Configuration

```xml
<!-- AndroidManifest.xml for test -->
<instrumentation
    android:name="androidx.test.runner.AndroidJUnitRunner"
    android:targetPackage="com.example.englishlearningandroidapp"
    android:label="BDD Tests" />
```

### 7. BDD Best Practices

1. **Write User-Centric Scenarios**: Focus on user behavior, not implementation
2. **Use Business Language**: Write scenarios in language stakeholders understand
3. **Keep Scenarios Simple**: One behavior per scenario
4. **Use Background Wisely**: Share common setup, but don't overuse
5. **Maintain Traceability**: Link scenarios to requirements
6. **Regular Review**: Keep scenarios updated with changing requirements
7. **Automate Where Possible**: Convert scenarios to executable tests
8. **Use Page Objects**: Maintain clean, reusable test code
9. **Test Data Management**: Use realistic test data
10. **Continuous Feedback**: Run BDD tests as part of CI/CD

### 8. Collaboration with Stakeholders

#### 8.1 Three Amigos Sessions

**Participants**: Business Analyst, Developer, Tester
**Purpose**: Define and refine scenarios before development
**Frequency**: Before each feature development

**Example Session for Word Search Feature:**
```
Business Analyst: "Users need to search for words and see multiple meanings"
Developer: "How should we handle API failures?"
Tester: "What about edge cases like empty search or special characters?"

Result: Additional scenarios for error handling and validation
```

#### 8.2 Living Documentation

BDD scenarios serve as:
- **Requirements Documentation**: What the system should do
- **Test Cases**: How to verify behavior
- **User Stories**: Why features exist
- **Acceptance Criteria**: When features are done

### 9. BDD Tools Integration

#### 9.1 Cucumber for Android (Optional)

```kotlin
// If using Cucumber-Android
@CucumberOptions(
    features = ["features"],
    glue = ["steps"],
    tags = "@regression"
)
class CucumberTestRunner
```

#### 9.2 Allure Reporting

```kotlin
// Add Allure annotations for better reporting
@Test
@Story("Word Search")
@Severity(SeverityLevel.CRITICAL)
fun `user can search for words and select definitions`() {
    // Test implementation
}
```

### 10. Common BDD Challenges and Solutions

#### 10.1 Challenge: Scenarios Too Technical
**Solution**: Focus on user behavior, not implementation details

**Bad**: "When the API returns HTTP 200"
**Good**: "When the word search is successful"

#### 10.2 Challenge: Scenarios Too Vague
**Solution**: Add specific, measurable outcomes

**Bad**: "Then the user sees results"
**Good**: "Then the user sees 2 definition options with Chinese translations"

#### 10.3 Challenge: Maintenance Overhead
**Solution**: Use Page Object pattern and reusable step definitions

#### 10.4 Challenge: Slow Test Execution
**Solution**: Use mocks for external dependencies, optimize test data setup

---

**Remember**: BDD is about collaboration and shared understanding. The goal is not just to test software, but to ensure that the right software is built to meet user needs.
