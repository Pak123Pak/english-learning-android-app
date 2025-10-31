# Debug Logging for Dictionary API

## Overview

This document explains the debug logging feature added to the English Learning Android App for monitoring dictionary API calls and responses in Android Studio's debug terminal.

## Features Added

### 1. Debug Logging in DictionaryViewModel

The `DictionaryViewModel` now includes comprehensive debug logging that shows:
- Search query validation
- API call initiation
- Complete API response details
- Filtered definitions that will be shown to the user
- Definition selection events  
- Word save operations

### 2. Debug Logging in ApiRepository

The `ApiRepository` includes logging for:
- Cache hit/miss statistics
- API call attempts (Cambridge Dictionary API + Free Dictionary fallback)
- API success/failure status
- Cache operations

### 3. Debug Logging in MockApiService

The `MockApiService` (currently used) shows:
- Requested word and normalized form
- Available words in mock database
- Network delay simulation
- Mock API response details
- Found/not found status

## How to View Debug Logs

### Method 1: Android Studio Logcat

1. Open Android Studio
2. Run the app in debug mode
3. Open the **Logcat** window (View → Tool Windows → Logcat)
4. Filter by tags:
   - `DictionaryViewModel` - For ViewModel events
   - `ApiRepository` - For repository operations
   - `MockApiService` - For mock API calls

### Method 2: Android Studio Run Console

1. The logs are also printed to `System.out` for easier viewing
2. Look for logs prefixed with:
   - `[DictionaryViewModel]`
   - `[ApiRepository]`
   - `[MockApiService]`

## Example Debug Output

When searching for the word "cook", you'll see output like:

```
[DictionaryViewModel] === DICTIONARY SEARCH STARTED ===
[DictionaryViewModel] Search Query: 'cook'
[DictionaryViewModel] Validation Passed: Query is valid
[DictionaryViewModel] Making API call to fetch word definition...
[ApiRepository] === API REPOSITORY FETCH STARTED ===
[ApiRepository] Word to fetch: 'cook'
[ApiRepository] Use cache: true
[ApiRepository] Cache MISS for word: 'cook' (will fetch from API)
[ApiRepository] Trying Cambridge Dictionary API for word: 'cook'
[MockApiService] === MOCK API SERVICE CALLED ===
[MockApiService] Requested word: 'cook'
[MockApiService] Normalized word: 'cook'
[MockApiService] Available words: cook, book, hello, learn, english, love, house, water, happy, good
[MockApiService] Simulating network delay (500ms)...
[MockApiService] FOUND definition for 'cook':
[MockApiService]   Word: cook
[MockApiService]   Phonetic: kʊk
[MockApiService]   Definitions count: 2
[MockApiService]     Definition 1:
[MockApiService]       Translation: 做飯，烹調;燒，煮
[MockApiService]       Part of Speech: verb
[MockApiService]       Example: She cooks dinner every evening.
[MockApiService]       Synonyms: prepare, make
[MockApiService]       Antonyms: None
[MockApiService]     Definition 2:
[MockApiService]       Translation: 廚師
[MockApiService]       Part of Speech: noun
[MockApiService]       Example: She's a wonderful cook.
[MockApiService]       Synonyms: chef, culinary artist
[MockApiService]       Antonyms: None
[MockApiService] === MOCK API SUCCESS ===
[ApiRepository] Cambridge API SUCCESS for word: 'cook'
[ApiRepository] Response cached successfully
[ApiRepository] === API REPOSITORY FETCH COMPLETED ===
[DictionaryViewModel] API Call Success!
[DictionaryViewModel] === API RESPONSE DETAILS ===
[DictionaryViewModel] Word: cook
[DictionaryViewModel] Phonetic: kʊk
[DictionaryViewModel] Origin: N/A
[DictionaryViewModel] Total Definitions: 2
[DictionaryViewModel] --- All Definitions ---
[DictionaryViewModel]   Definition 1:
[DictionaryViewModel]     Translation: 做飯，烹調;燒，煮
[DictionaryViewModel]     Part of Speech: verb
[DictionaryViewModel]     Example: She cooks dinner every evening.
[DictionaryViewModel]     Has Example: true
[DictionaryViewModel]   Definition 2:
[DictionaryViewModel]     Translation: 廚師
[DictionaryViewModel]     Part of Speech: noun
[DictionaryViewModel]     Example: She's a wonderful cook.
[DictionaryViewModel]     Has Example: true
[DictionaryViewModel] === END API RESPONSE ===
[DictionaryViewModel] Found 2 definitions
[DictionaryViewModel] === FILTERED DEFINITIONS FOR UI ===
[DictionaryViewModel]   UI Definition 1:
[DictionaryViewModel]     Translation: 做飯，烹調;燒，煮
[DictionaryViewModel]     Part of Speech: verb
[DictionaryViewModel]     Example: She cooks dinner every evening.
[DictionaryViewModel]     Blank Example: She ___s dinner every evening.
[DictionaryViewModel]   UI Definition 2:
[DictionaryViewModel]     Translation: 廚師
[DictionaryViewModel]     Part of Speech: noun
[DictionaryViewModel]     Example: She's a wonderful cook.
[DictionaryViewModel]     Blank Example: She's a wonderful ___.
[DictionaryViewModel] === END FILTERED DEFINITIONS ===
[DictionaryViewModel] === DICTIONARY SEARCH COMPLETED ===
```

## Configuration

### Enabling/Disabling Debug Logs

Debug logging can be controlled by changing the `DEBUG_ENABLED` constant in each class:

```kotlin
// In DictionaryViewModel.kt
companion object {
    private const val DEBUG_ENABLED = true // Set to false to disable
}

// In ApiRepository.kt  
companion object {
    private const val DEBUG_ENABLED = true // Set to false to disable
}

// In MockApiService.kt
companion object {
    private const val DEBUG_ENABLED = true // Set to false to disable
}
```

For production builds, these should be set to `false` to avoid verbose logging.

## Testing the Debug Logging

1. Open the app in Android Studio
2. Navigate to the "English-Chinese-Traditional" page
3. Search for any of these test words:
   - `cook` - Shows multiple definitions
   - `book` - Shows noun and verb forms
   - `hello` - Shows simple definition
   - `love` - Shows multiple definitions
   - `xyz` - Shows "word not found" scenario

4. Watch the Logcat or Run console for detailed debug output

## Benefits

- **Debugging**: Easily track down issues in the dictionary lookup flow
- **Performance Monitoring**: See cache hit rates and API response times
- **Data Verification**: Verify that API responses contain expected data
- **User Experience**: Understand what data is being presented to users

## Notes

- Debug logging only activates when `DEBUG_ENABLED = true`
- Logs are written to both Android's Log system and System.out
- Network delays are simulated in the mock service (500ms)
- Cache statistics are tracked and displayed
