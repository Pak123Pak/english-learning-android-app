# Class Diagrams (UML)
## English Learning Android App

### 1. High-Level Architecture Class Diagram

```mermaid
classDiagram
    %% Presentation Layer
    class MainActivity {
        -binding: ActivityMainBinding
        -wordRepository: WordRepository
        +onCreate(Bundle)
        +navigateToDictionary()
        +navigateToRevision()
        +showDeleteAllConfirmationDialog()
        +deleteAllWords()
    }
    
    class DictionaryActivity {
        -binding: ActivityDictionaryBinding
        -viewModel: DictionaryViewModel
        +onCreate(Bundle)
        +searchWord(String)
        +displayDefinitions(List~Definition~)
        +onDefinitionSelected(Definition)
        +saveWord()
        +onBackButtonClick()
    }
    
    class RevisionActivity {
        -binding: ActivityRevisionBinding
        -viewModel: RevisionViewModel
        +onCreate(Bundle)
        +loadWordsForStage(Int)
        +displayCurrentWord(Word)
        +onAnswerSubmitted(String)
        +validateAnswer(String, String)
        +onContinueButtonClick()
        +onBackButtonClick()
        +showAnswerFeedback(String, Boolean)
        +hideAnswerFeedback()
        +showContinueButton()
        +hideContinueButton()
        +showDeleteWordConfirmationDialog()
    }
    
    %% ViewModel Layer
    class DictionaryViewModel {
        -repository: WordRepository
        -apiRepository: ApiRepository
        -searchResults: MutableLiveData~List~Definition~~
        -selectedDefinition: MutableLiveData~Definition~
        -actualWord: MutableLiveData~String~
        +searchWord(String)
        +selectDefinition(Definition)
        +saveWord(Word)
        +getSearchResults(): LiveData~List~Definition~~
        +getActualWord(): LiveData~String~
    }
    
    class RevisionViewModel {
        -repository: WordRepository
        -currentWord: MutableLiveData~Word~
        -currentStage: MutableLiveData~Int~
        -wordsInStage: MutableLiveData~List~Word~~
        -successMessage: MutableLiveData~String~
        +loadWordsForStage(Int)
        +getCurrentWord(): LiveData~Word~
        +submitAnswer(String): Boolean
        +continueToNextWord()
        +clearAnswerResult()
        +loadNextWord()
        +deleteCurrentWord()
        +clearSuccessMessage()
    }
    
    %% Repository Layer
    class WordRepository {
        -wordDao: WordDao
        +insert(Word)
        +update(Word)
        +getWordsByStage(Int): LiveData~List~Word~~
        +getAllWords(): LiveData~List~Word~~
        +deleteWord(Word)
    }
    
    class ApiRepository {
        -apiService: DictionaryApiService
        +fetchWordDefinition(String): Result~WordDefinitionResponse~
        +handleApiError(Throwable): Result~WordDefinitionResponse~
    }
    
    %% Data Layer
    class WordDao {
        +insert(Word)
        +update(Word)
        +delete(Word)
        +getWordsByStage(Int): LiveData~List~Word~~
        +getAllWords(): LiveData~List~Word~~
        +getWordCount(): Int
        +getWordCountByStage(Int): Int
    }
    
    class WordDatabase {
        -INSTANCE: WordDatabase
        +wordDao(): WordDao
        +getDatabase(Context): WordDatabase
    }
    
    %% API Layer
    class DictionaryApiService {
        +getWordDefinition(String): Response~WordDefinitionResponse~
    }
    
    class RetrofitInstance {
        -api: DictionaryApiService
        +getInstance(): DictionaryApiService
    }
    
    %% Data Models
    class Word {
        +id: Long
        +englishWord: String
        +chineseTranslation: String
        +partOfSpeech: String
        +exampleSentence: String
        +blankExampleSentence: String
        +revisionStage: Int
        +createdAt: Long
        +lastRevisedAt: Long
    }
    
    class Definition {
        +translation: String
        +partOfSpeech: String
        +example: String
        +synonyms: List~String~
        +antonyms: List~String~
    }
    
    class WordDefinitionResponse {
        +word: String
        +phonetic: String
        +phonetics: List~Phonetic~
        +definitions: List~Definition~
        +origin: String
    }
    
    class Phonetic {
        +text: String
        +audio: String
    }
    
    %% Relationships
    MainActivity --> DictionaryActivity
    MainActivity --> RevisionActivity
    
    DictionaryActivity --> DictionaryViewModel
    RevisionActivity --> RevisionViewModel
    
    DictionaryViewModel --> WordRepository
    DictionaryViewModel --> ApiRepository
    RevisionViewModel --> WordRepository
    
    WordRepository --> WordDao
    ApiRepository --> DictionaryApiService
    
    WordDao --> WordDatabase
    DictionaryApiService --> RetrofitInstance
    
    WordDao --> Word
    DictionaryViewModel --> Definition
    ApiRepository --> WordDefinitionResponse
    WordDefinitionResponse --> Definition
    WordDefinitionResponse --> Phonetic
```

### 2. Data Model Class Diagram

```mermaid
classDiagram
    class Word {
        <<Entity>>
        +id: Long
        +englishWord: String
        +chineseTranslation: String
        +partOfSpeech: String
        +exampleSentence: String
        +blankExampleSentence: String
        +revisionStage: Int
        +createdAt: Long
        +lastRevisedAt: Long
        
        +Word(englishWord: String, chineseTranslation: String, ...)
        +createBlankExample(): String
        +getStageDisplayName(): String
        +isReadyForNextStage(): Boolean
        +incrementStage(): Word
    }
    
    class Definition {
        <<Data Class>>
        +translation: String
        +partOfSpeech: String
        +example: String
        +synonyms: List~String~
        +antonyms: List~String~
        
        +Definition(translation: String, partOfSpeech: String, ...)
        +hasExample(): Boolean
        +createBlankExample(word: String): String
    }
    
    class WordDefinitionResponse {
        <<API Response>>
        +word: String
        +pos: List~String~
        +phonetic: String
        +phonetics: List~Phonetic~
        +pronunciation: List~Pronunciation~
        +verbs: List~VerbForm~
        +definitions: List~Definition~
        +definition: List~CambridgeDefinition~
        +origin: String
        
        +getMainDefinitions(): List~Definition~
        +hasAudio(): Boolean
        +getPrimaryPhonetic(): Phonetic
        +getPrimaryPronunciation(): String
    }
    
    class Phonetic {
        <<Data Class>>
        +text: String
        +audio: String
        
        +hasAudio(): Boolean
        +getAudioUrl(): String
    }
    
    class CambridgeDefinition {
        <<Cambridge API Model>>
        +id: Int
        +pos: String
        +text: String
        +translation: String
        +example: List~CambridgeExample~
    }
    
    class CambridgeExample {
        <<Cambridge API Model>>
        +id: Int
        +text: String
        +translation: String
    }
    
    class Pronunciation {
        <<Cambridge API Model>>
        +pos: String
        +lang: String
        +url: String
        +pron: String
        
        +hasAudio(): Boolean
        +getAudioUrl(): String
    }
    
    class VerbForm {
        <<Cambridge API Model>>
        +id: Int
        +type: String
        +text: String
    }
    
    class RevisionStage {
        <<Enum>>
        NOT_REVISED(0)
        FIRST(1)
        SECOND(2)
        THIRD(3)
        FOURTH(4)
        FIFTH_OR_ABOVE(5)
        
        +getValue(): Int
        +getDisplayName(): String
        +getNext(): RevisionStage
        +fromInt(Int): RevisionStage
    }
    
    %% Relationships
    WordDefinitionResponse --> Definition
    WordDefinitionResponse --> Phonetic
    WordDefinitionResponse --> CambridgeDefinition
    WordDefinitionResponse --> Pronunciation
    WordDefinitionResponse --> VerbForm
    CambridgeDefinition --> CambridgeExample
    Word --> RevisionStage
```

### 3. Repository Pattern Class Diagram

```mermaid
classDiagram
    class BaseRepository {
        <<Abstract>>
        #coroutineScope: CoroutineScope
        #handleError(Throwable): Result~T~
        +cleanup()
    }
    
    class WordRepository {
        -wordDao: WordDao
        -localDataSource: LocalDataSource
        
        +insert(Word): Result~Long~
        +update(Word): Result~Unit~
        +delete(Word): Result~Unit~
        +getWordsByStage(Int): LiveData~List~Word~~
        +getAllWords(): LiveData~List~Word~~
        +getWordCount(): Int
        +getStageStatistics(): Map~Int, Int~
        +searchLocalWords(String): List~Word~
    }
    
    class ApiRepository {
        -apiService: DictionaryApiService
        -networkDataSource: NetworkDataSource
        -cache: Map~String, WordDefinitionResponse~
        
        +fetchWordDefinition(String): Result~WordDefinitionResponse~
        +getCachedDefinition(String): WordDefinitionResponse?
        +clearCache()
        +isNetworkAvailable(): Boolean
    }
    
    class LocalDataSource {
        -dao: WordDao
        
        +insertWord(Word): Long
        +updateWord(Word)
        +deleteWord(Word)
        +getWordsByStage(Int): List~Word~
        +getAllWords(): List~Word~
    }
    
    class NetworkDataSource {
        -apiService: DictionaryApiService
        -client: OkHttpClient
        
        +fetchDefinition(String): WordDefinitionResponse
        +handleNetworkError(Throwable): ApiError
        +isNetworkAvailable(): Boolean
    }
    
    %% Relationships
    WordRepository --|> BaseRepository
    ApiRepository --|> BaseRepository
    
    WordRepository --> LocalDataSource
    ApiRepository --> NetworkDataSource
    
    LocalDataSource --> WordDao
    NetworkDataSource --> DictionaryApiService
```

### 4. ViewModel Architecture Class Diagram

```mermaid
classDiagram
    class BaseViewModel {
        <<Abstract>>
        #viewModelScope: CoroutineScope
        #errorHandler: ErrorHandler
        #loadingState: MutableLiveData~Boolean~
        
        #handleError(Throwable)
        #setLoading(Boolean)
        +getLoadingState(): LiveData~Boolean~
        +cleanup()
    }
    
    class DictionaryViewModel {
        -wordRepository: WordRepository
        -apiRepository: ApiRepository
        -searchResults: MutableLiveData~List~Definition~~
        -selectedDefinition: MutableLiveData~Definition~
        -searchQuery: MutableLiveData~String~
        -actualWord: MutableLiveData~String~
        -saveState: MutableLiveData~SaveWordState~
        
        +searchWord(String)
        +selectDefinition(Definition)
        +saveSelectedWord()
        +clearSearch()
        +getSearchResults(): LiveData~List~Definition~~
        +getSelectedDefinition(): LiveData~Definition~
        +getActualWord(): LiveData~String~
        +getSaveState(): LiveData~SaveWordState~
    }
    
    class RevisionViewModel {
        -wordRepository: WordRepository
        -currentWord: MutableLiveData~Word~
        -currentStage: MutableLiveData~Int~
        -wordsInStage: MutableLiveData~List~Word~~
        -answerResult: MutableLiveData~AnswerResult~
        -stageStatistics: MutableLiveData~Map~Int, Int~~
        
        +setCurrentStage(Int)
        +loadWordsForStage(Int)
        +getCurrentWord(): LiveData~Word~
        +submitAnswer(String)
        +loadNextWord()
        +getStageStatistics(): LiveData~Map~Int, Int~~
        +skipCurrentWord()
    }
    
    class MainViewModel {
        -wordRepository: WordRepository
        -appStatistics: MutableLiveData~AppStatistics~
        -lastUsedFeature: MutableLiveData~String~
        
        +loadAppStatistics()
        +getAppStatistics(): LiveData~AppStatistics~
        +updateLastUsedFeature(String)
    }
    
    %% State Classes
    class SaveWordState {
        <<Sealed Class>>
        +Idle
        +Saving
        +Success(Word)
        +Error(String)
    }
    
    class AnswerResult {
        <<Sealed Class>>
        +Idle
        +Correct(Word, Int)
        +Incorrect(Word, String)
        +Error(String)
    }
    
    class AppStatistics {
        +totalWords: Int
        +wordsPerStage: Map~Int, Int~
        +streakDays: Int
        +lastStudyDate: Long
        
        +getTotalWordCount(): Int
        +getProgress(): Float
    }
    
    %% Relationships
    DictionaryViewModel --|> BaseViewModel
    RevisionViewModel --|> BaseViewModel
    MainViewModel --|> BaseViewModel
    
    DictionaryViewModel --> SaveWordState
    RevisionViewModel --> AnswerResult
    MainViewModel --> AppStatistics
    
    DictionaryViewModel --> WordRepository
    DictionaryViewModel --> ApiRepository
    RevisionViewModel --> WordRepository
    MainViewModel --> WordRepository
```

### 5. Database Layer Class Diagram

```mermaid
classDiagram
    class WordDatabase {
        <<Database>>
        -INSTANCE: WordDatabase
        
        +wordDao(): WordDao
        +getDatabase(Context): WordDatabase
        +clearAllTables()
        -buildDatabase(Context): WordDatabase
    }
    
    class WordDao {
        <<DAO>>
        +insert(Word): Long
        +insertAll(List~Word~): List~Long~
        +update(Word)
        +delete(Word)
        +deleteAll()
        +getWordById(Long): Word?
        +getAllWords(): LiveData~List~Word~~
        +getWordsByStage(Int): LiveData~List~Word~~
        +getWordCount(): Int
        +getWordCountByStage(Int): Int
        +searchWords(String): List~Word~
        +getWordsCreatedAfter(Long): List~Word~
        +getWordsNeedingRevision(): List~Word~
    }
    
    class Word {
        <<Entity>>
        +id: Long
        +englishWord: String
        +chineseTranslation: String
        +partOfSpeech: String
        +exampleSentence: String
        +blankExampleSentence: String
        +revisionStage: Int
        +createdAt: Long
        +lastRevisedAt: Long
        
        +toDisplayString(): String
        +isOverdue(): Boolean
        +getNextRevisionDate(): Long
    }
    
    class DatabaseMigrations {
        <<Object>>
        +MIGRATION_1_2: Migration
        +MIGRATION_2_3: Migration
        
        -migrate1to2(SupportSQLiteDatabase)
        -migrate2to3(SupportSQLiteDatabase)
    }
    
    class DatabaseTypeConverters {
        <<TypeConverter>>
        +fromStringList(List~String~): String
        +toStringList(String): List~String~
        +fromTimestamp(Long): Date
        +toTimestamp(Date): Long
    }
    
    %% Relationships
    WordDatabase --> WordDao
    WordDao --> Word
    WordDatabase --> DatabaseMigrations
    WordDatabase --> DatabaseTypeConverters
```

### 6. API Integration Class Diagram

```mermaid
classDiagram
    class RetrofitInstance {
        <<Singleton>>
        -retrofit: Retrofit
        -okHttpClient: OkHttpClient
        -gson: Gson
        
        +getApiService(): DictionaryApiService
        +updateBaseUrl(String)
        -createOkHttpClient(): OkHttpClient
        -createGson(): Gson
    }
    
    class DictionaryApiService {
        <<Interface>>
        +getWordDefinition(String): Response~WordDefinitionResponse~
        +getWordWithPhonetics(String): Response~WordDefinitionResponse~
        +searchSimilarWords(String): Response~List~String~~
    }
    
    class ApiInterceptor {
        +intercept(Chain): Response
        -addHeaders(Request): Request
        -logRequest(Request)
        -logResponse(Response)
    }
    
    class NetworkResult {
        <<Sealed Class>>
        +Success(T)
        +Error(Exception)
        +Loading
        
        +isSuccess(): Boolean
        +isError(): Boolean
        +isLoading(): Boolean
    }
    
    class ApiError {
        +code: Int
        +message: String
        +cause: Throwable?
        
        +isNetworkError(): Boolean
        +isServerError(): Boolean
        +isClientError(): Boolean
    }
    
    class CacheManager {
        -cache: LruCache~String, WordDefinitionResponse~
        -maxSize: Int
        
        +get(String): WordDefinitionResponse?
        +put(String, WordDefinitionResponse)
        +clear()
        +size(): Int
    }
    
    %% Relationships
    RetrofitInstance --> DictionaryApiService
    RetrofitInstance --> ApiInterceptor
    DictionaryApiService --> NetworkResult
    NetworkResult --> ApiError
    ApiRepository --> CacheManager
    ApiRepository --> NetworkResult
```

### 7. UI Component Class Diagram

```mermaid
classDiagram
    class BaseActivity {
        <<Abstract>>
        #binding: ViewBinding
        #loadingDialog: ProgressDialog
        
        #showLoading()
        #hideLoading()
        #showError(String)
        #showSuccess(String)
        +setupToolbar()
    }
    
    class MainActivity {
        -binding: ActivityMainBinding
        -viewModel: MainViewModel
        
        +onCreate(Bundle)
        +onDictionaryClick()
        +onRevisionClick()
        -setupClickListeners()
        -observeViewModel()
    }
    
    class DictionaryActivity {
        -binding: ActivityDictionaryBinding
        -viewModel: DictionaryViewModel
        -definitionsAdapter: DefinitionsAdapter
        
        +onCreate(Bundle)
        +onSearchClick()
        +onDefinitionSelected(Definition)
        +onSaveClick()
        -setupRecyclerView()
        -setupSearchView()
        -observeSearchResults()
    }
    
    class RevisionActivity {
        -binding: ActivityRevisionBinding
        -viewModel: RevisionViewModel
        -currentStage: Int
        
        +onCreate(Bundle)
        +onStageSelected(Int)
        +onAnswerSubmitted()
        +onSkipClick()
        -setupStageSpinner()
        -setupAnswerInput()
        -displayCurrentWord(Word)
        -handleAnswerResult(AnswerResult)
    }
    
    class DefinitionsAdapter {
        -definitions: List~Definition~
        -selectedPosition: Int
        -onDefinitionSelected: (Definition) -> Unit
        
        +onCreateViewHolder(ViewGroup, Int): DefinitionViewHolder
        +onBindViewHolder(DefinitionViewHolder, Int)
        +getItemCount(): Int
        +updateDefinitions(List~Definition~)
        +getSelectedDefinition(): Definition?
    }
    
    class DefinitionViewHolder {
        -binding: ItemDefinitionBinding
        
        +bind(Definition, Boolean, (Definition) -> Unit)
        -setupClickListener()
    }
    
    class CustomViews {
        <<Utility>>
        +createStageSpinner(Context): Spinner
        +createProgressDialog(Context): ProgressDialog
        +setupToolbarWithBack(Activity, String)
    }
    
    %% Relationships
    MainActivity --|> BaseActivity
    DictionaryActivity --|> BaseActivity
    RevisionActivity --|> BaseActivity
    
    DictionaryActivity --> DefinitionsAdapter
    DefinitionsAdapter --> DefinitionViewHolder
    
    MainActivity --> MainViewModel
    DictionaryActivity --> DictionaryViewModel
    RevisionActivity --> RevisionViewModel
```

### 8. Utility Classes Diagram

```mermaid
classDiagram
    class ValidationUtils {
        <<Utility>>
        +isValidEnglishWord(String): Boolean
        +normalizeAnswer(String): String
        +isAnswerCorrect(String, String): Boolean
        +validateDefinitionSelection(Definition?): ValidationResult
    }
    
    class StringUtils {
        <<Utility>>
        +createBlankSentence(String, String): String
        -replaceInflectedForms(String, String): String
        +extractWordFromSentence(String): String
        +formatStageDisplay(Int): String
        +truncateText(String, Int): String
        +capitalizeWords(String): String
        +normalizeText(String): String
        +containsChinese(String): Boolean
    }
    
    class DateUtils {
        <<Utility>>
        +getCurrentTimestamp(): Long
        +formatRelativeTime(Long): String
        +getDaysBetween(Long, Long): Int
        +isToday(Long): Boolean
    }
    
    class PreferencesManager {
        -sharedPreferences: SharedPreferences
        
        +setLastUsedStage(Int)
        +getLastUsedStage(): Int
        +setFirstLaunch(Boolean)
        +isFirstLaunch(): Boolean
        +clear()
    }
    
    class NetworkUtils {
        <<Utility>>
        +isNetworkAvailable(Context): Boolean
        +isWifiConnected(Context): Boolean
        +getNetworkType(Context): NetworkType
    }
    
    class ErrorHandler {
        +handleApiError(Throwable): String
        +handleDatabaseError(Throwable): String
        +getErrorMessage(ErrorType): String
        +logError(String, Throwable)
    }
    
    class Constants {
        <<Object>>
        +API_BASE_URL: String
        +DATABASE_NAME: String
        +CACHE_SIZE: Int
        +TIMEOUT_SECONDS: Long
        +STAGE_NAMES: Array~String~
    }
    
    %% Enums
    class NetworkType {
        <<Enumeration>>
        WIFI
        MOBILE
        NONE
        UNKNOWN
    }
    
    class ErrorType {
        <<Enumeration>>
        NETWORK_ERROR
        SERVER_ERROR
        DATABASE_ERROR
        VALIDATION_ERROR
        UNKNOWN_ERROR
    }
    
    class ValidationResult {
        <<Sealed Class>>
        +Valid
        +Invalid(String)
        
        +isValid(): Boolean
        +getErrorMessage(): String?
    }
    
    %% Relationships
    ErrorHandler --> ErrorType
    NetworkUtils --> NetworkType
    ValidationUtils --> ValidationResult
```

---

## Class Diagram Notes

### Design Patterns Used:
1. **MVVM (Model-View-ViewModel)**: Separates UI logic from business logic
2. **Repository Pattern**: Abstracts data sources and provides a clean API
3. **Singleton Pattern**: Database instance, Retrofit instance
4. **Observer Pattern**: LiveData for reactive UI updates
5. **Factory Pattern**: Database creation, ViewModels
6. **Sealed Classes**: Type-safe state management

### Key Relationships:
- **Composition**: Activities contain ViewModels, ViewModels contain Repositories
- **Dependency Injection**: Repositories injected into ViewModels
- **Inheritance**: All Activities extend BaseActivity
- **Association**: Loose coupling between layers

### Threading Considerations:
- Database operations on background threads
- Network calls on IO dispatcher
- UI updates on main thread
- Coroutines for asynchronous operations

### Error Handling Strategy:
- Centralized error handling in BaseViewModel
- Type-safe error states using sealed classes
- User-friendly error messages
- Logging for debugging purposes
