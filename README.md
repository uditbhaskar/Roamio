# Roamio: Smart Travel Companion

**Roamio** is a travel companion Android app for nearby places, weather, saved spots, and currency conversion. Built with Kotlin, Jetpack Compose, Koin, and Ktor.

## Features

- **Onboarding** – short intro, remembered via DataStore
- **Home** – city search, activity chips, featured place
- **Popular** – destination stack
- **Place** – detail, save, nearby cafe
- **Saved** – starred places
- **Convert** – amounts via [Frankfurter](https://www.frankfurter.app/)
- **Settings** – name, units, home currency

Location is optional: if permission is denied, the app uses a default city (London).

## Modules

```
:app                  Application shell, NavGraph, theme
:core                 HttpClient, constants, location, preferences
:feature-onboarding   First-run only
:feature-home         Home, Popular, Place, Saved, Convert, Settings
```

## Getting started

1. Clone the repository and open it in Android Studio.
2. Sync Gradle (no API keys required). Optional: `PEXELS_API_KEY` in `local.properties`.
3. Run the `:app` configuration on a device or emulator (API 31+).

```bash
./gradlew :app:assembleDebug
./gradlew test
```

## API attributions

Data sources used by this project (please respect their terms of use):

| Feature | Source | License / notes |
| :------ | :----- | :-------------- |
| Weather | [Open-Meteo](https://open-meteo.com/) | Free for non-commercial; attribution appreciated |
| Places | [OpenStreetMap](https://www.openstreetmap.org/) / Overpass | ODbL – © OpenStreetMap contributors |
| Summaries | [Wikipedia](https://www.mediawiki.org/wiki/API:Main_page) | CC BY-SA – User-Agent required |
| Currency | [Frankfurter](https://www.frankfurter.app/) | Free ECB-based rates |

## Architecture

- Modules: `app` / `core` / `feature-onboarding` / `feature-home`
- UI: Compose Root/Content split, `StateFlow`, `handleAction`
- DI: Koin
- Network: single Ktor `HttpClient` in `:core`
- No raw string literals for routes, URLs, or UI copy (constants + `strings.xml`)

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).
