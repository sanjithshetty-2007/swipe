# CLAUDE.md — SwipeIt (Android photo-cleaner)

Read this file fully before every task. These rules override any default habits.

## 1. What we are building
SwipeIt: an offline Android app that loops through the phone's photo gallery one card at a time.
- Swipe LEFT  → move to Trash (app-level trash, nothing is deleted from the device yet)
- Swipe RIGHT → Keep
- Swipe UP    → Keep (alternate gesture)
- "Recent 5" stack shows the last 5 decided photos left→right; tap any one to preview and change the decision.
- Trash tab: grid of trashed photos, restore, multi-select, "Delete All from Trash" (real device delete via system dialog).
- Kept tab: grid of kept photos, can move back to Trash or reset to Unsorted.
- Everything updates in real time (Room Flow + MediaStore ContentObserver).
- 100% local. NO internet, NO analytics, NO cloud, NO ads.

## 2. Tech stack (do not swap without asking)
- Language: Kotlin only. No Java files.
- UI: Jetpack Compose + Material 3. No XML layouts (except manifest/resources).
- Architecture: MVVM + unidirectional data flow (UiState / UiEvent).
- DI: Hilt (with KSP).
- Persistence: Room (with KSP) for decisions. DataStore (Preferences) for settings.
- Async: Kotlin Coroutines + Flow / StateFlow. No RxJava, no LiveData.
- Images: Coil (Compose) loading `content://` URIs from MediaStore.
- Navigation: Navigation Compose (type-safe routes with @Serializable objects).
- Build: Gradle Kotlin DSL + version catalog (`gradle/libs.versions.toml`). Use the latest STABLE versions that are mutually compatible; never alpha/beta unless asked.
- minSdk 26, target/compileSdk = latest stable.

## 3. Hard rules
1. NEVER add `android.permission.INTERNET` or any network library. The app must work in airplane mode.
2. NEVER delete a device file directly with File APIs. Device deletion goes ONLY through `MediaStore.createDeleteRequest` (API 30+) with the system confirmation dialog; on API 26–29 use `ContentResolver.delete` and handle `RecoverableSecurityException`.
3. Swiping left must NEVER delete from the device — it only marks status = TRASHED in Room.
4. Permissions: `READ_MEDIA_IMAGES` (API 33+), `READ_MEDIA_VISUAL_USER_SELECTED` (API 34+ partial access), `READ_EXTERNAL_STORAGE` with `maxSdkVersion="32"`. Handle denied / partial / permanently-denied states with a clear UI.
5. No work on the main thread: MediaStore queries and Room writes run on `Dispatchers.IO` (inject dispatchers, don't hardcode).
6. Never load full-resolution bitmaps for cards/grids — let Coil size them; full-res only in Preview.
7. Do not invent features beyond the current phase. Finish the phase, then stop and summarise.
8. Do not delete or rewrite working code outside the scope of the current prompt.
9. The project must compile (`./gradlew assembleDebug`) at the end of every phase. If you cannot run Gradle, say so explicitly and list what to verify.

## 4. Package structure (package-by-feature, layered inside)
```
com.swipeit.app
├── SwipeItApp.kt                 // @HiltAndroidApp
├── MainActivity.kt               // single activity, setContent { SwipeItRoot() }
├── core/
│   ├── designsystem/             // theme, colors, typography, shapes, reusable components
│   ├── common/                   // Result wrappers, dispatchers, extensions, formatters
│   └── permissions/              // permission state + composables
├── data/
│   ├── media/                    // MediaStoreDataSource, MediaObserver (ContentObserver → Flow)
│   ├── db/                       // Room: AppDatabase, PhotoDecisionEntity, PhotoDecisionDao
│   ├── prefs/                    // DataStore settings
│   └── repository/               // PhotoRepositoryImpl, SettingsRepositoryImpl
├── domain/
│   ├── model/                    // Photo, Decision (UNSORTED/KEPT/TRASHED), Stats
│   ├── repository/               // interfaces only
│   └── usecase/                  // KeepPhoto, TrashPhoto, UndoLast, RestorePhoto, EmptyTrash, ...
├── feature/
│   ├── sort/                     // SortScreen, SortViewModel, SwipeCard, CardStack, RecentStrip
│   ├── preview/                  // PreviewScreen (zoom, decide)
│   ├── trash/                    // TrashScreen, TrashViewModel
│   ├── kept/                     // KeptScreen, KeptViewModel
│   ├── settings/                 // SettingsScreen
│   └── onboarding/               // permission intro
├── navigation/                   // routes, NavHost, bottom bar
└── di/                           // Hilt modules (DatabaseModule, DataModule, DispatchersModule)
```
Dependency direction: `feature → domain ← data`. Features never import `data` directly. `domain` has zero Android framework imports (except `Uri` if unavoidable — prefer `String` id + uri string).

## 5. Coding conventions
- One public class/composable concept per file. Files under ~300 lines; split when larger.
- Composables: stateless "Content" composable receives `UiState` + lambdas; a thin "Screen" composable wires the ViewModel. Every Content composable gets a `@Preview` with fake data.
- ViewModels expose `StateFlow<UiState>` (collected with `collectAsStateWithLifecycle`) and take `onEvent(UiEvent)`. One-shot effects (snackbar, launch delete dialog) via `Channel`/`SharedFlow`.
- `UiState` is an immutable `data class`; lists use `ImmutableList` or are marked `@Immutable`/`@Stable`.
- Use `sealed interface` for events, results and decisions.
- No magic numbers in UI: dimensions/colors/durations live in `core/designsystem`.
- Strings in `strings.xml`. No hardcoded user-visible text in composables.
- Names: `XxxScreen`, `XxxContent`, `XxxViewModel`, `XxxUiState`, `XxxUiEvent`, `XxxUseCase`, `XxxRepository(Impl)`, `XxxDao`, `XxxEntity`.
- Error handling: never swallow exceptions silently; log with `Log.w(TAG, ...)` and surface a UI error state.
- Keep KDoc short on public domain/repo functions. No commented-out code.
- Use `remember`/`derivedStateOf` correctly; give `LazyGrid` items stable `key = photo.id`.

## 6. Data model rules
- Room table `photo_decisions(mediaId PK Long, uri String, status String, decidedAt Long, sizeBytes Long, dateTaken Long, displayName String)`.
- A photo with no row = UNSORTED. Unsorted queue = MediaStore images minus ids in Room with status KEPT/TRASHED.
- When MediaStore reports a photo was removed externally, delete its Room row (reconcile on every MediaStore change).
- "Recent 5" = last 5 rows ordered by `decidedAt DESC`.
- Undo = revert the most recent decision (delete row → back to UNSORTED, returns to top of the deck).

## 7. Design system (from the UI inspiration)
- Dark theme only (for now). Background `#0B0B10`, surface `#16161D`, surfaceVariant `#1F1F28`, outline `#2A2A35`.
- Keep / accent green `#A6F56B` (with glow), Trash coral `#FF6B6B`, info/blue `#5AC8FA`, text primary `#F5F5F7`, text secondary `#9A9AA5`.
- Cards: 24dp corner radius, subtle border, image fills card, bottom gradient overlay with filename, date · size, and an expand icon.
- Swipe feedback: card rotates up to ±12°, tints green ("KEEP!" stamp + heart) when dragged right/up, coral ("TRASH" stamp) when left.
- Action row: big coral trash circle (left), up-arrow circle (middle), green ring heart (right).
- Bottom nav: Sort · Trash · Kept, active item in green.
- Logo "SwipeIt" uses a handwritten-style font (bundle an OFL font in `res/font`, e.g. a marker/handwriting font) — no downloadable fonts (offline).

## 8. Workflow for every prompt
1. Restate the goal of the prompt in 2–3 bullets.
2. List files you will create/modify.
3. Implement.
4. Build / check for compile errors.
5. Finish with: what changed, how to test it on the phone, and known gaps. Then STOP and wait.

## 9. Testing
- Unit tests for use cases and repository logic (fake DAO / fake MediaStore source) with JUnit + kotlinx-coroutines-test + Turbine.
- Room DAO tests with in-memory DB (instrumented).
- Manual test checklist lives at the end of each phase in the prompt guide.
