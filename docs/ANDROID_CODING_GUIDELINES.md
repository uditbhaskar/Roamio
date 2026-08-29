# Android Coding Guidelines

Standards for this multi-module Compose project.

> Follow this file for all code changes.

## Structure

```
app/          → Application, MainActivity, NavGraph, theme, AppConstants, app DI
core/         → HttpClient, CoreConstants, preferences, location, network
feature-*/    → di/, ui/, viewModel/ (+ data/ for repositories) — depends on core only
```

## Hard rules

- No raw string literals: routes → `AppConstants`, network/errors → `CoreConstants`, UI → `strings.xml`.
- No feature-to-feature dependencies; wire feature UIs in `:app`.
- Screens: `*ScreenRoot` + `*ScreenContent`.
- Every `*ScreenContent` and reusable Compose UI must have `@Preview` functions for loaded, loading, and error (or empty) states. Preview `*ScreenContent`, never `*ScreenRoot`.
- ViewModels: `StateFlow`, `handleAction`, navigation flags with `resetNavigation` where needed.
- Single Ktor `HttpClient` from `:core`.
- Koin: `single` / `viewModelOf`.
- Tests: ViewModel unit tests + Content UI tests for features.

## Compose previews

Previews are how screens scale: review layout and states in the IDE without running the app.

- Add previews in the same file as the composable, at the bottom.
- Target `*ScreenContent` and any reusable public/private UI component with its own visual states.
- Do **not** preview `*ScreenRoot` (Koin, permission launchers, navigation).
- Cover the states a reviewer would actually open: loaded, loading, error or empty. Add more when a state looks different (last onboarding page, fallback banner, selected sheet).
- Wrap in `MaterialTheme` (features cannot depend on `:app` / `RoamioTheme`).
- Use `@Preview(name = "...", showBackground = true)`. Private preview functions — no KDoc.
- Preview fixtures go in `*UiState` / domain models. No raw UI copy; error text may use `CoreConstants.Errors`.

```kotlin
@Preview(name = "Loaded", showBackground = true)
@Composable
private fun WeatherScreenContentLoadedPreview() {
    MaterialTheme {
        WeatherScreenContent(
            state = WeatherUiState(temperature = 20.0, weatherCode = 0),
            onAction = {},
            placeName = "London",
        )
    }
}
```

## KDoc

Document **public API only** — not every line of code.

### Required on

- Public classes, objects, interfaces, enums
- Public functions and composables
- Constructor parameters via `@param` on the class or function

### Format

```kotlin
/**
 * Short description of what this does.
 *
 * @param name Description of parameter.
 * @return Description when not Unit.
 * @author udit
 */
```

### Do NOT add KDoc to

- Properties or fields (`val` / `var`) — no `@property`, no docs above `uiState`, `navigateNext`, etc.
- `const val` entries inside constant objects
- Enum entries (`CONTINUE`, `Retry`, etc.)
- Sealed interface / enum members (`Load`, `Select`, `data object Retry`)
- Private functions
- Android framework overrides (`override fun onCreate`, `LocationListener` callbacks, etc.)
- Test rules (`@get:Rule`), `@Before`, `@After`
- DTO / serialization model fields

### Examples

```kotlin
// GOOD — class + public method
/**
 * ViewModel for the weather screen.
 *
 * @param weatherRepository Repository for Open-Meteo API calls.
 * @author udit
 */
class WeatherViewModel(private val weatherRepository: WeatherRepository) : ViewModel() {

    val uiState: StateFlow<WeatherUiState> = _uiState  // no KDoc

    /**
     * Handles user actions from the weather screen.
     *
     * @param action The user action to process.
     * @author udit
     */
    fun handleAction(action: WeatherAction) { ... }
}

// BAD — do not do this
/** Observable UI state. @author udit */
val uiState: StateFlow<WeatherUiState> = _uiState

/** @property temperature Current temp. */
data class WeatherUiState(val temperature: Double?)

override fun onLocationChanged(location: Location) { ... }  // no KDoc
```
