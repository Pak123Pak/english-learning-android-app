# Test-Driven Development (TDD) Approach
## English Learning Android App

### 1. TDD Overview

Test-Driven Development (TDD) is a software development approach where tests are written before the actual implementation code. The TDD cycle follows the **Red-Green-Refactor** pattern:

1. **Red**: Write a failing test
2. **Green**: Write minimal code to make the test pass
3. **Refactor**: Improve the code while keeping tests green

### 2. TDD Benefits for Android Development

- **Early Bug Detection**: Catch issues before they reach production
- **Better Code Design**: TDD encourages modular, testable code
- **Documentation**: Tests serve as living documentation
- **Confidence**: Safe refactoring with comprehensive test coverage
- **Reduced Debugging Time**: Issues are caught early in development

### 3. Testing Strategy by Layer

#### 3.1 Unit Tests (70% of total tests)

**Target Components:**
- ViewModels
- Repository classes
- Utility functions
- Data validation logic
- Business logic components

**Testing Framework:**
- **JUnit 5**: Modern testing framework
- **Mockito**: Mocking framework for dependencies
- **Truth**: Fluent assertions library
- **Coroutines Test**: Testing coroutines and LiveData

#### 3.2 Integration Tests (20% of total tests)

**Target Components:**
- Repository + Database interactions
- API service integration
- End-to-end data flow
- ViewModels with real repositories

#### 3.3 UI Tests (10% of total tests)

**Target Components:**
- Critical user flows
- Activity navigation
- Input validation
- Error message display

### 4. Unit Test Examples

#### 4.1 DictionaryViewModel Tests

```kotlin
class DictionaryViewModelTest {
    
    @Mock
    private lateinit var wordRepository: WordRepository
    
    @Mock
    private lateinit var apiRepository: ApiRepository
    
    private lateinit var viewModel: DictionaryViewModel
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = DictionaryViewModel(wordRepository, apiRepository)
    }
    
    @Test
    fun `searchWord with valid input should update search results`() = runTest {
        // Arrange
        val query = "cook"
        val expectedDefinitions = listOf(
            Definition("做飯，烹調", "verb", "She cooks dinner"),
            Definition("廚師", "noun", "She's a wonderful cook")
        )
        val response = WordDefinitionResponse(query, definitions = expectedDefinitions)
        
        whenever(apiRepository.fetchWordDefinition(query))
            .thenReturn(Result.success(response))
        
        // Act
        viewModel.searchWord(query)
        
        // Assert
        val results = viewModel.getSearchResults().getOrAwaitValue()
        Truth.assertThat(results).isEqualTo(expectedDefinitions)
        Truth.assertThat(results).hasSize(2)
    }
    
    @Test
    fun `searchWord with empty input should not trigger API call`() = runTest {
        // Arrange
        val emptyQuery = ""
        
        // Act
        viewModel.searchWord(emptyQuery)
        
        // Assert
        verify(apiRepository, never()).fetchWordDefinition(any())
        val results = viewModel.getSearchResults().getOrAwaitValue()
        Truth.assertThat(results).isEmpty()
    }
    
    @Test
    fun `selectDefinition should update selected definition`() = runTest {
        // Arrange
        val definition = Definition("廚師", "noun", "She's a wonderful cook")
        
        // Act
        viewModel.selectDefinition(definition)
        
        // Assert
        val selected = viewModel.getSelectedDefinition().getOrAwaitValue()
        Truth.assertThat(selected).isEqualTo(definition)
    }
    
    @Test
    fun `saveSelectedWord should save to repository and update state`() = runTest {
        // Arrange
        val definition = Definition("廚師", "noun", "She's a wonderful cook")
        val expectedWord = Word(
            englishWord = "cook",
            chineseTranslation = "廚師",
            partOfSpeech = "noun",
            exampleSentence = "She's a wonderful cook",
            revisionStage = 0
        )
        
        viewModel.selectDefinition(definition)
        whenever(wordRepository.insert(any())).thenReturn(Result.success(1L))
        
        // Act
        viewModel.saveSelectedWord()
        
        // Assert
        verify(wordRepository).insert(expectedWord)
        val saveState = viewModel.getSaveState().getOrAwaitValue()
        Truth.assertThat(saveState).isInstanceOf(SaveWordState.Success::class.java)
    }
    
    @Test
    fun `searchWord with network error should show no internet message`() = runTest {
        // Arrange
        val query = "cook"
        val networkException = ApiException.NetworkException("No internet connection")
        val errorResult = NetworkResult.Error<WordDefinitionResponse>(networkException, "Network error")
        
        whenever(apiRepository.fetchWordDefinition(query))
            .thenReturn(errorResult)
        
        // Act
        viewModel.searchWord(query)
        
        // Assert
        val errorMessage = viewModel.getErrorMessage().getOrAwaitValue()
        Truth.assertThat(errorMessage).isEqualTo("No Internet")
    }
    
    @Test
    fun `searchWord with timeout error should show timeout message`() = runTest {
        // Arrange
        val query = "cook"
        val timeoutException = ApiException.TimeoutException("Request timeout")
        val errorResult = NetworkResult.Error<WordDefinitionResponse>(timeoutException, "Timeout error")
        
        whenever(apiRepository.fetchWordDefinition(query))
            .thenReturn(errorResult)
        
        // Act
        viewModel.searchWord(query)
        
        // Assert
        val errorMessage = viewModel.getErrorMessage().getOrAwaitValue()
        Truth.assertThat(errorMessage).isEqualTo("Request timeout. Please try again.")
    }
}
```

#### 4.2 RevisionViewModel Tests

```kotlin
class RevisionViewModelTest {
    
    @Mock
    private lateinit var wordRepository: WordRepository
    
    private lateinit var viewModel: RevisionViewModel
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        viewModel = RevisionViewModel(wordRepository)
    }
    
    @Test
    fun `loadWordsForStage should update words list`() = runTest {
        // Arrange
        val stage = 1
        val expectedWords = listOf(
            createTestWord("cook", "廚師", stage),
            createTestWord("book", "書", stage)
        )
        val liveData = MutableLiveData(expectedWords)
        
        whenever(wordRepository.getWordsByStage(stage)).thenReturn(liveData)
        
        // Act
        viewModel.loadWordsForStage(stage)
        
        // Assert
        val words = viewModel.getWordsForStage().getOrAwaitValue()
        Truth.assertThat(words).isEqualTo(expectedWords)
        Truth.assertThat(words).hasSize(2)
    }
    
    @Test
    fun `submitAnswer with correct answer should move word to next stage`() = runTest {
        // Arrange
        val currentWord = createTestWord("cook", "廚師", 1)
        val userAnswer = "cook"
        
        whenever(wordRepository.update(any())).thenReturn(Result.success(Unit))
        
        // Act
        val result = viewModel.submitAnswer(currentWord, userAnswer)
        
        // Assert
        Truth.assertThat(result).isTrue()
        val expectedUpdatedWord = currentWord.copy(revisionStage = 2)
        verify(wordRepository).update(expectedUpdatedWord)
    }
    
    @Test
    fun `submitAnswer with incorrect answer should keep word in same stage`() = runTest {
        // Arrange
        val currentWord = createTestWord("cook", "廚師", 1)
        val userAnswer = "wrong"
        
        whenever(wordRepository.update(any())).thenReturn(Result.success(Unit))
        
        // Act
        val result = viewModel.submitAnswer(currentWord, userAnswer)
        
        // Assert
        Truth.assertThat(result).isFalse()
        verify(wordRepository).update(currentWord) // Same stage
    }
    
    @Test
    fun `submitAnswer for stage 5 word should keep in stage 5`() = runTest {
        // Arrange
        val currentWord = createTestWord("cook", "廚師", 5)
        val userAnswer = "cook"
        
        whenever(wordRepository.update(any())).thenReturn(Result.success(Unit))
        
        // Act
        val result = viewModel.submitAnswer(currentWord, userAnswer)
        
        // Assert
        Truth.assertThat(result).isTrue()
        verify(wordRepository).update(currentWord) // Stays at stage 5
    }
    
    @Test
    fun `submitAnswer should show answer result and wait for continue`() = runTest {
        // Arrange
        val currentWord = createTestWord("cook", "廚師", 1)
        val userAnswer = "cook"
        
        whenever(wordRepository.update(any())).thenReturn(Result.success(Unit))
        
        // Act
        viewModel.submitAnswer(userAnswer)
        
        // Assert
        val answerResult = viewModel.answerResult.getOrAwaitValue()
        Truth.assertThat(answerResult).isInstanceOf(AnswerResult.Correct::class.java)
        
        // Verify that the current word hasn't automatically moved yet
        val currentWordAfter = viewModel.currentWord.getOrAwaitValue()
        Truth.assertThat(currentWordAfter).isEqualTo(currentWord)
    }
    
    @Test
    fun `continueToNextWord should move to next word after answer submission`() = runTest {
        // Arrange
        val words = listOf(
            createTestWord("cook", "廚師", 1),
            createTestWord("book", "書", 1)
        )
        setupWordsInStage(words, 1)
        
        // Submit correct answer for first word
        viewModel.submitAnswer("cook")
        
        // Act
        viewModel.continueToNextWord()
        
        // Assert
        val currentWord = viewModel.currentWord.getOrAwaitValue()
        Truth.assertThat(currentWord.englishWord).isEqualTo("book")
        
        val answerResult = viewModel.answerResult.getOrAwaitValue()
        Truth.assertThat(answerResult).isInstanceOf(AnswerResult.Idle::class.java)
    }
    
    @Test
    fun `clearAnswerResult should reset answer feedback state`() = runTest {
        // Arrange
        viewModel.submitAnswer("correct")
        val initialResult = viewModel.answerResult.getOrAwaitValue()
        Truth.assertThat(initialResult).isNotInstanceOf(AnswerResult.Idle::class.java)
        
        // Act
        viewModel.clearAnswerResult()
        
        // Assert
        val clearedResult = viewModel.answerResult.getOrAwaitValue()
        Truth.assertThat(clearedResult).isInstanceOf(AnswerResult.Idle::class.java)
    }
    
    private fun createTestWord(
        english: String, 
        chinese: String, 
        stage: Int
    ) = Word(
        id = 1L,
        englishWord = english,
        chineseTranslation = chinese,
        partOfSpeech = "noun",
        exampleSentence = "Example with $english",
        blankExampleSentence = "Example with ___",
        revisionStage = stage,
        createdAt = System.currentTimeMillis(),
        lastRevisedAt = System.currentTimeMillis()
    )
    @Test
    fun `deleteCurrentWord should delete word from repository and show success message`() = runTest {
        // Arrange
        val wordToDelete = createMockWord("delete", "刪除", 1)
        viewModel.setCurrentStage(1)
        `when`(wordRepository.getWordsByStageSync(1)).thenReturn(listOf(wordToDelete))
        `when`(wordRepository.deleteWord(wordToDelete)).thenReturn(Result.success(Unit))
        
        // Act
        viewModel.deleteCurrentWord()
        
        // Assert
        verify(wordRepository).deleteWord(wordToDelete)
        val successMessage = viewModel.successMessage.getOrAwaitValue()
        Truth.assertThat(successMessage).isEqualTo("Word deleted successfully")
    }
    
    @Test
    fun `deleteCurrentWord should show error message when deletion fails`() = runTest {
        // Arrange
        val wordToDelete = createMockWord("delete", "刪除", 1)
        viewModel.setCurrentStage(1)
        `when`(wordRepository.getWordsByStageSync(1)).thenReturn(listOf(wordToDelete))
        `when`(wordRepository.deleteWord(wordToDelete)).thenReturn(Result.failure(Exception("Delete failed")))
        
        // Act
        viewModel.deleteCurrentWord()
        
        // Assert
        verify(wordRepository).deleteWord(wordToDelete)
        val errorMessage = viewModel.errorMessage.getOrAwaitValue()
        Truth.assertThat(errorMessage).isEqualTo("Failed to delete word")
    }
    
    @Test
    fun `deleteCurrentWord should show error when no current word`() = runTest {
        // Arrange
        viewModel.setCurrentStage(0)
        `when`(wordRepository.getWordsByStageSync(0)).thenReturn(emptyList())
        
        // Act
        viewModel.deleteCurrentWord()
        
        // Assert
        verify(wordRepository, never()).deleteWord(any())
        val errorMessage = viewModel.errorMessage.getOrAwaitValue()
        Truth.assertThat(errorMessage).isEqualTo("No word to delete")
    }
    
    @Test
    fun `clearSuccessMessage should clear success message`() {
        // Arrange
        viewModel.successMessage.value = "Test message"
        
        // Act
        viewModel.clearSuccessMessage()
        
        // Assert
        Truth.assertThat(viewModel.successMessage.value).isNull()
    }
}
```

#### 4.3 MainActivity Tests

```kotlin
class MainActivityTest {
    
    @Mock
    private lateinit var wordRepository: WordRepository
    
    private lateinit var activity: MainActivity
    
    @Test
    fun `deleteAllWords should call repository deleteAllWords and show success message`() = runTest {
        // Arrange
        `when`(wordRepository.deleteAllWords()).thenReturn(Result.success(Unit))
        
        // Act
        activity.deleteAllWords()
        
        // Assert
        verify(wordRepository).deleteAllWords()
        // Verify success toast is shown (would need ActivityScenario for full test)
    }
    
    @Test
    fun `deleteAllWords should show error message when deletion fails`() = runTest {
        // Arrange
        `when`(wordRepository.deleteAllWords()).thenReturn(Result.failure(Exception("Delete failed")))
        
        // Act
        activity.deleteAllWords()
        
        // Assert
        verify(wordRepository).deleteAllWords()
        // Verify error toast is shown (would need ActivityScenario for full test)
    }
}
```

#### 4.4 Repository Tests

```kotlin
class WordRepositoryTest {
    
    @Mock
    private lateinit var wordDao: WordDao
    
    private lateinit var repository: WordRepository
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = WordRepository(wordDao)
    }
    
    @Test
    fun `insert should call dao insert and return id`() = runTest {
        // Arrange
        val word = createTestWord()
        val expectedId = 1L
        
        whenever(wordDao.insert(word)).thenReturn(expectedId)
        
        // Act
        val result = repository.insert(word)
        
        // Assert
        Truth.assertThat(result.isSuccess).isTrue()
        Truth.assertThat(result.getOrNull()).isEqualTo(expectedId)
        verify(wordDao).insert(word)
    }
    
    @Test
    fun `getWordsByStage should return LiveData from dao`() = runTest {
        // Arrange
        val stage = 1
        val expectedWords = listOf(createTestWord())
        val liveData = MutableLiveData(expectedWords)
        
        whenever(wordDao.getWordsByStage(stage)).thenReturn(liveData)
        
        // Act
        val result = repository.getWordsByStage(stage)
        
        // Assert
        Truth.assertThat(result).isEqualTo(liveData)
        verify(wordDao).getWordsByStage(stage)
    }
    
    @Test
    fun `update should call dao update`() = runTest {
        // Arrange
        val word = createTestWord()
        
        // Act
        val result = repository.update(word)
        
        // Assert
        Truth.assertThat(result.isSuccess).isTrue()
        verify(wordDao).update(word)
    }
}
```

#### 4.4 Utility Class Tests

```kotlin
class ValidationUtilsTest {
    
    @Test
    fun `isAnswerCorrect should ignore case`() {
        // Arrange & Act & Assert
        Truth.assertThat(ValidationUtils.isAnswerCorrect("cook", "COOK")).isTrue()
        Truth.assertThat(ValidationUtils.isAnswerCorrect("Cook", "cook")).isTrue()
        Truth.assertThat(ValidationUtils.isAnswerCorrect("COOK", "cook")).isTrue()
    }
    
    @Test
    fun `isAnswerCorrect should trim whitespace`() {
        // Arrange & Act & Assert
        Truth.assertThat(ValidationUtils.isAnswerCorrect("cook", " cook ")).isTrue()
        Truth.assertThat(ValidationUtils.isAnswerCorrect(" cook ", "cook")).isTrue()
    }
    
    @Test
    fun `isAnswerCorrect should return false for different words`() {
        // Arrange & Act & Assert
        Truth.assertThat(ValidationUtils.isAnswerCorrect("cook", "book")).isFalse()
        Truth.assertThat(ValidationUtils.isAnswerCorrect("cook", "")).isFalse()
    }
    
    @Test
    fun `createBlankSentence should replace word with blank`() {
        // Arrange
        val sentence = "She's a wonderful cook."
        val word = "cook"
        
        // Act
        val result = StringUtils.createBlankSentence(sentence, word)
        
        // Assert
        Truth.assertThat(result).isEqualTo("She's a wonderful ___.")
    }
}
```

### 5. Integration Test Examples

#### 5.1 Repository + Database Integration

```kotlin
@RunWith(AndroidJUnit4::class)
class WordRepositoryIntegrationTest {
    
    private lateinit var database: WordDatabase
    private lateinit var wordDao: WordDao
    private lateinit var repository: WordRepository
    
    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            WordDatabase::class.java
        ).build()
        
        wordDao = database.wordDao()
        repository = WordRepository(wordDao)
    }
    
    @After
    fun tearDown() {
        database.close()
    }
    
    @Test
    fun `insert and retrieve word by stage`() = runTest {
        // Arrange
        val word = createTestWord(revisionStage = 1)
        
        // Act
        repository.insert(word)
        val words = repository.getWordsByStage(1).getOrAwaitValue()
        
        // Assert
        Truth.assertThat(words).hasSize(1)
        Truth.assertThat(words[0].englishWord).isEqualTo(word.englishWord)
    }
    
    @Test
    fun `update word stage`() = runTest {
        // Arrange
        val word = createTestWord(revisionStage = 1)
        repository.insert(word)
        
        val insertedWords = repository.getWordsByStage(1).getOrAwaitValue()
        val insertedWord = insertedWords[0]
        val updatedWord = insertedWord.copy(revisionStage = 2)
        
        // Act
        repository.update(updatedWord)
        
        // Assert
        val stage1Words = repository.getWordsByStage(1).getOrAwaitValue()
        val stage2Words = repository.getWordsByStage(2).getOrAwaitValue()
        
        Truth.assertThat(stage1Words).isEmpty()
        Truth.assertThat(stage2Words).hasSize(1)
        Truth.assertThat(stage2Words[0].revisionStage).isEqualTo(2)
    }
}
```

#### 5.2 API Integration Tests

```kotlin
class ApiRepositoryIntegrationTest {
    
    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiRepository: ApiRepository
    
    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        
        val apiService = retrofit.create(DictionaryApiService::class.java)
        apiRepository = ApiRepository(apiService)
    }
    
    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }
    
    @Test
    fun `fetchWordDefinition returns success response`() = runTest {
        // Arrange
        val mockResponse = """
            {
                "word": "cook",
                "pos": ["verb", "noun"],
                "pronunciation": [
                    {
                        "pos": "noun",
                        "lang": "us",
                        "url": "https://dictionary.cambridge.org/us/media/english-chinese-traditional/us_pron/c/coo/cook_/cook.mp3",
                        "pron": "/kʊk/"
                    }
                ],
                "definition": [
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
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json")
        )
        
        // Act
        val result = apiRepository.fetchWordDefinition("cook")
        
        // Assert
        Truth.assertThat(result.isSuccess).isTrue()
        val response = result.getOrNull()
        Truth.assertThat(response?.word).isEqualTo("cook")
        Truth.assertThat(response?.getMainDefinitions()).hasSize(1)
        Truth.assertThat(response?.getMainDefinitions()?.first()?.translation).isEqualTo("廚師")
        Truth.assertThat(response?.pronunciation).hasSize(1)
        Truth.assertThat(response?.getPrimaryPronunciation()).isEqualTo("/kʊk/")
    }
    
    @Test
    fun `adaptCambridgeApiResponse converts to unified format correctly`() = runTest {
        // Arrange
        val cambridgeResponse = """
            {
                "word": "cocky",
                "pos": ["adjective"],
                "pronunciation": [
                    {
                        "pos": "adjective",
                        "lang": "uk",
                        "url": "https://dictionary.cambridge.org/us/media/english-chinese-traditional/uk_pron/u/ukc/ukcon/ukconve028.mp3",
                        "pron": "/ˈkɒki/"
                    }
                ],
                "definition": [
                    {
                        "id": 0,
                        "pos": "adjective",
                        "text": "very confident, usually in a way that is slightly annoying",
                        "translation": "狂妄自負的;趾高氣揚的;驕傲的",
                        "example": [
                            {
                                "id": 0,
                                "text": "Let's not get too cocky—things could still go wrong.",
                                "translation": ""
                            }
                        ]
                    }
                ]
            }
        """.trimIndent()
        
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(cambridgeResponse)
                .addHeader("Content-Type", "application/json")
        )
        
        // Act
        val result = apiRepository.fetchWordDefinition("cocky")
        
        // Assert
        Truth.assertThat(result.isSuccess).isTrue()
        val response = result.getOrNull()
        Truth.assertThat(response?.word).isEqualTo("cocky")
        Truth.assertThat(response?.getMainDefinitions()).hasSize(1)
        
        val definition = response?.getMainDefinitions()?.first()
        Truth.assertThat(definition?.translation).isEqualTo("狂妄自負的;趾高氣揚的;驕傲的")
        Truth.assertThat(definition?.partOfSpeech).isEqualTo("adjective")
        Truth.assertThat(definition?.example).contains("Let's not get too cocky")
    }
    
    @Test
    fun `fetchWordDefinition handles API error`() = runTest {
        // Arrange
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("Word not found")
        )
        
        // Act
        val result = apiRepository.fetchWordDefinition("invalidword")
        
        // Assert
        Truth.assertThat(result.isFailure).isTrue()
        Truth.assertThat(result.exceptionOrNull()).isInstanceOf(HttpException::class.java)
    }
}
```

### 6. Test Configuration

#### 6.1 Test Dependencies (app/build.gradle.kts)

```kotlin
dependencies {
    // Unit Testing
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.1.1")
    testImplementation("org.mockito:mockito-kotlin:4.1.0")
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    
    // Integration Testing
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.room:room-testing:2.6.1")
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
}
```

#### 6.2 Test Utils

```kotlin
// TestUtils.kt
object TestUtils {
    
    fun createTestWord(
        id: Long = 1L,
        englishWord: String = "test",
        chineseTranslation: String = "測試",
        partOfSpeech: String = "noun",
        revisionStage: Int = 0
    ) = Word(
        id = id,
        englishWord = englishWord,
        chineseTranslation = chineseTranslation,
        partOfSpeech = partOfSpeech,
        exampleSentence = "This is a $englishWord example.",
        blankExampleSentence = "This is a ___ example.",
        revisionStage = revisionStage,
        createdAt = System.currentTimeMillis(),
        lastRevisedAt = System.currentTimeMillis()
    )
    
    fun createTestDefinition(
        translation: String = "測試",
        partOfSpeech: String = "noun",
        example: String = "This is a test."
    ) = Definition(
        translation = translation,
        partOfSpeech = partOfSpeech,
        example = example,
        synonyms = emptyList(),
        antonyms = emptyList()
    )
}

// LiveDataTestUtils.kt
fun <T> LiveData<T>.getOrAwaitValue(
    time: Long = 2,
    timeUnit: TimeUnit = TimeUnit.SECONDS
): T {
    var data: T? = null
    val latch = CountDownLatch(1)
    
    val observer = object : Observer<T> {
        override fun onChanged(o: T?) {
            data = o
            latch.countDown()
            this@getOrAwaitValue.removeObserver(this)
        }
    }
    
    this.observeForever(observer)
    
    if (!latch.await(time, timeUnit)) {
        throw TimeoutException("LiveData value was never set.")
    }
    
    @Suppress("UNCHECKED_CAST")
    return data as T
}
```

### 6. UI Tests (Espresso)

#### 6.1 Navigation UI Tests

```kotlin
@RunWith(AndroidJUnit4::class)
class NavigationUITest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)
    
    @Test
    fun test_back_button_in_dictionary_activity() {
        // Navigate to Dictionary
        onView(withText("English-Chinese-Traditional"))
            .perform(click())
        
        // Verify we're on Dictionary screen
        onView(withId(R.id.searchEditText))
            .check(matches(isDisplayed()))
        
        // Click back button
        onView(withId(R.id.backButton))
            .perform(click())
        
        // Verify we're back on main menu
        onView(withText("English-Chinese-Traditional"))
            .check(matches(isDisplayed()))
        onView(withText("Revision"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun test_back_button_in_revision_activity() {
        // Navigate to Revision
        onView(withText("Revision"))
            .perform(click())
        
        // Verify we're on Revision screen
        onView(withId(R.id.stageSpinner))
            .check(matches(isDisplayed()))
        
        // Click back button
        onView(withId(R.id.backButton))
            .perform(click())
        
        // Verify we're back on main menu
        onView(withText("English-Chinese-Traditional"))
            .check(matches(isDisplayed()))
        onView(withText("Revision"))
            .check(matches(isDisplayed()))
    }
    
    @Test
    fun test_back_button_visibility_and_styling() {
        // Navigate to Dictionary
        onView(withText("English-Chinese-Traditional"))
            .perform(click())
        
        // Verify back button is visible and has correct text
        onView(withId(R.id.backButton))
            .check(matches(isDisplayed()))
            .check(matches(withText("← Back to Main")))
            .check(matches(isClickable()))
    }
}
```

#### 6.2 Revision Flow UI Tests

```kotlin
@RunWith(AndroidJUnit4::class)
class RevisionFlowUITest {
    
    @get:Rule
    val activityRule = ActivityScenarioRule(RevisionActivity::class.java)
    
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    
    @Inject
    lateinit var wordRepository: WordRepository
    
    @Before
    fun setup() {
        hiltRule.inject()
        setupTestWords()
    }
    
    @Test
    fun test_continue_button_flow_correct_answer() {
        // Select a stage with words
        onView(withId(R.id.stageSpinner))
            .perform(click())
        onData(allOf(instanceOf(String::class.java), `is`("1st")))
            .perform(click())
        
        // Wait for word to load
        onView(withId(R.id.chineseTranslationTextView))
            .check(matches(isDisplayed()))
        
        // Enter correct answer
        onView(withId(R.id.answerEditText))
            .perform(typeText("cook"), closeSoftKeyboard())
        
        // Submit answer
        onView(withId(R.id.submitButton))
            .perform(click())
        
        // Verify feedback is shown
        onView(withId(R.id.answerFeedbackTextView))
            .check(matches(isDisplayed()))
            .check(matches(withText("Correct!")))
        
        // Verify continue button is shown
        onView(withId(R.id.continueButton))
            .check(matches(isDisplayed()))
            .check(matches(withText("Continue to Next Word")))
        
        // Verify submit button is disabled
        onView(withId(R.id.submitButton))
            .check(matches(not(isEnabled())))
        
        // Click continue button
        onView(withId(R.id.continueButton))
            .perform(click())
        
        // Verify continue button is hidden
        onView(withId(R.id.continueButton))
            .check(matches(not(isDisplayed())))
        
        // Verify feedback is hidden
        onView(withId(R.id.answerFeedbackTextView))
            .check(matches(not(isDisplayed())))
        
        // Verify submit button is re-enabled
        onView(withId(R.id.submitButton))
            .check(matches(isEnabled()))
    }
    
    @Test
    fun test_continue_button_flow_incorrect_answer() {
        // Select a stage with words
        onView(withId(R.id.stageSpinner))
            .perform(click())
        onData(allOf(instanceOf(String::class.java), `is`("1st")))
            .perform(click())
        
        // Enter incorrect answer
        onView(withId(R.id.answerEditText))
            .perform(typeText("wrong"), closeSoftKeyboard())
        
        // Submit answer
        onView(withId(R.id.submitButton))
            .perform(click())
        
        // Verify feedback is shown
        onView(withId(R.id.answerFeedbackTextView))
            .check(matches(isDisplayed()))
            .check(matches(withSubstring("Incorrect")))
        
        // Verify continue button is shown
        onView(withId(R.id.continueButton))
            .check(matches(isDisplayed()))
        
        // Click continue button
        onView(withId(R.id.continueButton))
            .perform(click())
        
        // Verify next word is loaded
        onView(withId(R.id.answerEditText))
            .check(matches(withText("")))
    }
    
    @Test
    fun test_continue_button_not_shown_initially() {
        // Select a stage with words
        onView(withId(R.id.stageSpinner))
            .perform(click())
        onData(allOf(instanceOf(String::class.java), `is`("1st")))
            .perform(click())
        
        // Verify continue button is not shown initially
        onView(withId(R.id.continueButton))
            .check(matches(not(isDisplayed())))
        
        // Verify feedback is not shown initially
        onView(withId(R.id.answerFeedbackTextView))
            .check(matches(not(isDisplayed())))
    }
    
    private fun setupTestWords() = runBlocking {
        val testWords = listOf(
            createTestWord("cook", "廚師", 1),
            createTestWord("book", "書", 1),
            createTestWord("good", "好", 2)
        )
        testWords.forEach { word ->
            wordRepository.insert(word)
        }
    }
    
    private fun createTestWord(
        english: String,
        chinese: String,
        stage: Int
    ) = Word(
        englishWord = english,
        chineseTranslation = chinese,
        partOfSpeech = "noun",
        exampleSentence = "This is a $english example.",
        blankExampleSentence = "This is a ___ example.",
        revisionStage = stage,
        createdAt = System.currentTimeMillis(),
        lastRevisedAt = System.currentTimeMillis()
    )
}
```

#### 6.3 UI Test Helpers

```kotlin
// Custom Matchers
object CustomMatchers {
    
    fun withBackgroundColor(expectedColorResId: Int): Matcher<View> {
        return object : BoundedMatcher<View, TextView>(TextView::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("has background color: ")
                description.appendValue(expectedColorResId)
            }
            
            override fun matchesSafely(textView: TextView): Boolean {
                val context = textView.context
                val expectedColor = ContextCompat.getColor(context, expectedColorResId)
                val actualColor = (textView.background as? ColorDrawable)?.color
                return actualColor == expectedColor
            }
        }
    }
    
    fun withSubstring(substring: String): Matcher<View> {
        return object : BoundedMatcher<View, TextView>(TextView::class.java) {
            override fun describeTo(description: Description) {
                description.appendText("contains substring: ")
                description.appendValue(substring)
            }
            
            override fun matchesSafely(textView: TextView): Boolean {
                return textView.text.toString().contains(substring)
            }
        }
    }
}

// Test Rule for Database
class DatabaseTestRule : TestWatcher() {
    private lateinit var database: WordDatabase
    
    override fun starting(description: Description?) {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WordDatabase::class.java
        ).allowMainThreadQueries().build()
    }
    
    override fun finished(description: Description?) {
        database.close()
    }
    
    fun getDatabase(): WordDatabase = database
}
```

### 7. Test Coverage Goals

| Component | Target Coverage | Priority |
|-----------|----------------|----------|
| ViewModels | 95% | High |
| Repositories | 90% | High |
| Utility Classes | 95% | High |
| Data Models | 80% | Medium |
| Database Operations | 85% | High |
| API Integration | 80% | Medium |
| UI Navigation | 85% | High |
| User Interactions | 80% | Medium |

### 8. Continuous Integration Setup

#### 8.1 GitHub Actions Workflow

```yaml
name: Android CI

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Run unit tests
      run: ./gradlew test
    
    - name: Run instrumented tests
      uses: reactivecircus/android-emulator-runner@v2
      with:
        api-level: 29
        script: ./gradlew connectedAndroidTest
    
    - name: Generate test report
      run: ./gradlew jacocoTestReport
    
    - name: Upload coverage to Codecov
      uses: codecov/codecov-action@v3
```

### 9. TDD Best Practices

1. **Write Tests First**: Always write the test before the implementation
2. **Keep Tests Simple**: One assertion per test when possible
3. **Use Descriptive Names**: Test names should explain what is being tested
4. **Fast Tests**: Unit tests should run quickly (< 1 second each)
5. **Independent Tests**: Tests should not depend on each other
6. **Mock External Dependencies**: Use mocks for databases, APIs, etc.
7. **Test Edge Cases**: Include boundary conditions and error scenarios
8. **Refactor Regularly**: Improve code quality while keeping tests green
9. **Maintain Test Quality**: Treat test code with the same care as production code
10. **Run Tests Frequently**: Execute tests after every small change

### 10. Common TDD Pitfalls to Avoid

1. **Writing Too Many Tests at Once**: Follow the Red-Green-Refactor cycle
2. **Testing Implementation Details**: Focus on behavior, not implementation
3. **Ignoring Refactor Phase**: Always clean up code after making tests pass
4. **Over-Mocking**: Don't mock everything; use real objects when appropriate
5. **Slow Tests**: Keep unit tests fast by avoiding heavy operations
6. **Complex Tests**: Break down complex scenarios into smaller test cases
7. **Not Testing Error Cases**: Include negative test scenarios
8. **Skipping Integration Tests**: Unit tests alone are not sufficient

---

**Remember**: TDD is not just about testing; it's a design methodology that leads to better, more maintainable code. The tests you write serve as both verification and documentation of your system's behavior.
