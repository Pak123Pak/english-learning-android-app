# Flow Diagrams
## English Learning Android App

### 1. User Flow Diagram

```mermaid
graph TD
    A[App Launch] --> B[Main Menu]
    B --> C[English-Chinese-Traditional]
    B --> D[Revision]
    B --> DA[Delete All Words]
    
    C --> E[Search Word Input]
    E --> F[API Call to Cambridge Dictionary]
    F --> G{API Response Success?}
    G -->|Yes| H[Display Definitions List]
    G -->|No| I[Show Error Message]
    I --> E
    
    H --> J[User Selects One Definition]
    J --> K{Definition Selected?}
    K -->|No| J
    K -->|Yes| L[Save Button Enabled]
    L --> M[Save Word to 'Not Revised' Stage]
    M --> N[Success Message]
    N --> E
    
    C --> CB[Back to Main Button]
    CB --> B
    E --> CB
    
    D --> O[Stage Selection Dropdown]
    O --> P[Select Revision Stage]
    P --> Q[Load First Word from Stage]
    Q --> R{Words Available in Stage?}
    R -->|No| S[Show 'No Words' Message]
    R -->|Yes| T[Display Word Info]
    
    T --> U[Show Chinese Translation]
    U --> V[Show Example with Blank]
    V --> DW[Delete Word Button]
    V --> W[User Input English Word]
    W --> X[Submit Answer]
    X --> Y{Answer Correct?}
    
    Y -->|Yes| YC[Show Correct Feedback]
    Y -->|No| YI[Show Incorrect Feedback]
    
    YC --> ZC[Show Continue Button]
    YI --> ZI[Show Continue Button]
    
    ZC --> UC[User Clicks Continue]
    ZI --> UI[User Clicks Continue]
    
    UC --> Z[Move to Next Stage]
    UI --> AA[Keep in Current Stage]
    
    Z --> BB{Current Stage = '5th or above'?}
    BB -->|Yes| CC[Keep in '5th or above']
    BB -->|No| DD[Move to Next Numbered Stage]
    
    CC --> EE[Load Next Word]
    DD --> EE
    AA --> EE
    EE --> Q
    
    S --> O
    
    D --> DB[Back to Main Button]
    DB --> B
    O --> DB
```

### 2. Data Flow Diagram

```mermaid
graph LR
    A[User Input] --> B[UI Layer]
    B --> C[ViewModel]
    C --> D[Repository]
    
    D --> E[Room Database]
    D --> F[Retrofit API Service]
    
    F --> G[Cambridge Dictionary API]
    G --> H[API Response]
    H --> F
    
    E --> I[Local SQLite Database]
    I --> E
    
    E --> D
    F --> D
    D --> C
    C --> B
    B --> J[UI Update]
```

### 3. Application Architecture Flow

```mermaid
graph TB
    subgraph "Presentation Layer"
        A[MainActivity]
        B[DictionaryActivity]
        C[RevisionActivity]
    end
    
    subgraph "ViewModel Layer"
        D[MainViewModel]
        E[DictionaryViewModel]
        F[RevisionViewModel]
    end
    
    subgraph "Repository Layer"
        G[WordRepository]
        H[ApiRepository]
    end
    
    subgraph "Data Sources"
        I[Room Database]
        J[Retrofit Service]
        K[Cambridge Dictionary API]
    end
    
    A --> D
    B --> E
    C --> F
    
    D --> G
    E --> G
    E --> H
    F --> G
    
    G --> I
    H --> J
    J --> K
```

### 4. Database Operations Flow

```mermaid
graph TD
    A[App Start] --> B[Initialize Room Database]
    B --> C[Create WordDao Instance]
    
    D[Save New Word] --> E[Insert Word with Stage 0]
    E --> F[Update Local Database]
    
    G[Load Revision Words] --> H[Query by Stage]
    H --> I[Return Words List]
    
    J[Update Word Stage] --> K{Answer Correct?}
    K -->|Yes| L[Increment Stage]
    K -->|No| M[Keep Current Stage]
    
    L --> N{Stage >= 5?}
    N -->|Yes| O[Set Stage to 5]
    N -->|No| P[Set Stage to Stage + 1]
    
    O --> Q[Update Database]
    P --> Q
    M --> Q
```

### 5. API Integration Flow

```mermaid
sequenceDiagram
    participant U as User
    participant UI as Dictionary UI
    participant VM as DictionaryViewModel
    participant R as ApiRepository
    participant API as Cambridge Dictionary API
    
    U->>UI: Enter word "cook"
    UI->>VM: searchWord("cook")
    VM->>R: fetchWordDefinition("cook")
    R->>API: GET /api/v1/dictionary/en-tw/cook
    
    alt API Success
        API-->>R: Definition Response
        R-->>VM: Parsed Definitions List
        VM-->>UI: Update LiveData
        UI-->>U: Display Definition Options
        
        U->>UI: Select Definition
        UI->>VM: saveWord(word, definition)
        VM->>R: saveToDatabase(word)
        R-->>VM: Save Success
        VM-->>UI: Show Success Message
    else API Failure
        API-->>R: Error Response
        R-->>VM: Error Result
        VM-->>UI: Update Error State
        UI-->>U: Show Error Message
    end
```

### 6. Revision System Flow

```mermaid
graph TD
    A[Select Revision Stage] --> B[Load Words from Stage]
    B --> C{Words Available?}
    
    C -->|No| D[Show Empty Stage Message]
    C -->|Yes| E[Get First Word]
    
    E --> F[Display Chinese Translation]
    F --> G[Show Example with Blank]
    G --> H[User Inputs Answer]
    H --> I[Validate Answer]
    
    I --> J{Answer Correct?}
    J -->|Yes| JC[Show Correct Feedback]
    J -->|No| JI[Show Incorrect Feedback]
    
    JC --> KC[Show Continue Button]
    JI --> KI[Show Continue Button]
    
    KC --> WC[Wait for User Click Continue]
    KI --> WI[Wait for User Click Continue]
    
    WC --> K[Calculate Next Stage]
    WI --> L[Keep Current Stage]
    
    K --> M{Current Stage < 5?}
    M -->|Yes| N[Stage = Current + 1]
    M -->|No| O[Stage = 5 'or above']
    
    N --> P[Update Word in Database]
    O --> P
    L --> P
    
    P --> Q[Remove from Current Position]
    Q --> R[Add to End of Target Stage]
    R --> S[Load Next Word]
    S --> C
```

### 7. Navigation Flow

```mermaid
stateDiagram-v2
    [*] --> MainMenu
    
    MainMenu --> Dictionary : "English-Chinese-Traditional"
    MainMenu --> Revision : "Revision"
    
    Dictionary --> WordSearch
    WordSearch --> DefinitionSelection : Search Results
    DefinitionSelection --> WordSaved : Save Word
    WordSaved --> WordSearch : Continue
    
    Revision --> StageSelection
    StageSelection --> WordDisplay : Select Stage
    WordDisplay --> AnswerInput : Show Word
    AnswerInput --> AnswerValidation : Submit
    AnswerValidation --> AnswerFeedback : Show Result
    AnswerFeedback --> WordDisplay : Continue to Next Word
    
    Dictionary --> MainMenu : Back Button
    Revision --> MainMenu : Back Button
    WordSearch --> Dictionary : Back
    DefinitionSelection --> WordSearch : Back
    StageSelection --> Revision : Back
    WordDisplay --> StageSelection : Change Stage
```

### 8. Error Handling Flow

```mermaid
graph TD
    A[User Action] --> B[Try Operation]
    B --> C{Operation Success?}
    
    C -->|Yes| D[Continue Normal Flow]
    C -->|No| E[Identify Error Type]
    
    E --> F{Network Error?}
    F -->|Yes| G[Show Network Error Message]
    F -->|No| H{Database Error?}
    
    H -->|Yes| I[Show Database Error Message]
    H -->|No| J{Validation Error?}
    
    J -->|Yes| K[Show Validation Error]
    J -->|No| L[Show Generic Error]
    
    G --> M[Provide Retry Option]
    I --> N[Log Error & Report]
    K --> O[Highlight Invalid Field]
    L --> N
    
    M --> A
    N --> P[Return to Previous State]
    O --> A
    P --> A
```

### 9. Stage Progression Logic Flow

```mermaid
graph LR
    A[Not Revised - Stage 0] -->|Correct Answer| B[1st - Stage 1]
    B -->|Correct Answer| C[2nd - Stage 2]
    C -->|Correct Answer| D[3rd - Stage 3]
    D -->|Correct Answer| E[4th - Stage 4]
    E -->|Correct Answer| F[5th or above - Stage 5]
    
    A -->|Wrong Answer| A
    B -->|Wrong Answer| B
    C -->|Wrong Answer| C
    D -->|Wrong Answer| D
    E -->|Wrong Answer| E
    F -->|Correct/Wrong Answer| F
    
    style A fill:#ffcccc
    style B fill:#ffe6cc
    style C fill:#ffffcc
    style D fill:#e6ffcc
    style E fill:#ccffcc
    style F fill:#ccffff
```

### 10. Performance Optimization Flow

```mermaid
graph TD
    A[App Launch] --> B[Initialize Core Components]
    B --> C[Preload Common Data]
    
    D[User Search] --> E[Cache Check]
    E --> F{Cache Hit?}
    F -->|Yes| G[Return Cached Result]
    F -->|No| H[Make API Call]
    H --> I[Cache Response]
    I --> G
    
    J[Database Query] --> K[Use Indexes]
    K --> L[Limit Result Set]
    L --> M[Return Results]
    
    N[Memory Management] --> O[Cleanup Unused Objects]
    O --> P[Optimize Image Loading]
    P --> Q[Background Thread Operations]
```

---

## Flow Diagram Legend

### Symbols Used:
- **Rectangle**: Process/Action
- **Diamond**: Decision Point
- **Circle**: Start/End Point
- **Parallelogram**: Input/Output
- **Arrow**: Flow Direction

### Color Coding:
- **Red**: Error States
- **Green**: Success States
- **Yellow**: Warning/Pending States
- **Blue**: Information States

### 3. Delete Operations Flow

```mermaid
graph TD
    %% Delete All Words Flow
    DA[Delete All Words Button] --> DCA[Show Delete All Confirmation]
    DCA --> DCC{User Confirms?}
    DCC -->|Yes| DAE[Execute Delete All]
    DCC -->|No| B[Return to Main Menu]
    DAE --> DAS{Delete Success?}
    DAS -->|Yes| DASM[Show Success Message]
    DAS -->|No| DAEM[Show Error Message]
    DASM --> B
    DAEM --> B
    
    %% Delete Individual Word Flow
    DW[Delete Word Button] --> DCW[Show Delete Word Confirmation]
    DCW --> DWCC{User Confirms?}
    DWCC -->|Yes| DWE[Execute Delete Word]
    DWCC -->|No| T[Return to Word Display]
    DWE --> DWS{Delete Success?}
    DWS -->|Yes| DWSM[Show Success Message]
    DWS -->|No| DWEM[Show Error Message]
    DWSM --> Q[Reload Current Stage]
    DWEM --> T
```

### Notes:
1. All flows are designed to handle both success and error scenarios
2. User can navigate back at any point using standard Android back button
3. Database operations are performed asynchronously to maintain UI responsiveness
4. API calls include timeout and retry mechanisms
5. Stage progression follows spaced repetition principles for optimal learning
6. Delete operations include confirmation dialogs to prevent accidental data loss
7. Individual word deletion refreshes the current stage view automatically
