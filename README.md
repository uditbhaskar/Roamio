# Roamio – Smart Travel Companion

**Roamio** is a travel companion Android app for weather, nearby attractions, restaurants, and currency conversion. Built with Kotlin, Jetpack Compose, multi-module Clean MVVM, Koin, and Ktor.

## Features (MVP)

- **Onboarding** – short intro, remembered via DataStore
- **Weather** – current conditions + short forecast ([Open-Meteo](https://open-meteo.com/), free, no API key)
- **Attractions** – nearby tourism POIs ([OpenStreetMap Overpass](https://wiki.openstreetmap.org/wiki/Overpass_API)) + optional Wikipedia summary
- **Restaurants** – nearby restaurants via Overpass
- **Currency** – convert amounts ([Frankfurter](https://www.frankfurter.app/), ECB rates)

Location is optional: if permission is denied, the app uses a default city (London).

## Modules

```
:app                  Application shell, NavGraph, theme
:core                 HttpClient, constants, location, preferences
:feature-onboarding
:feature-home         Bottom navigation + location UX
:feature-weather
:feature-attractions
:feature-restaurants
:feature-currency
```

## Getting started

1. Clone the repository and open it in Android Studio.
2. Sync Gradle (no API keys required).
3. Run the `:app` configuration on a device or emulator (API 31+).

```bash
./gradlew :app:assembleDebug
./gradlew test
```

## API attributions

Data sources used by this project (please respect their terms of use):

| Feature | Source | License / notes |
|---------|--------|-----------------|
| Weather | [Open-Meteo](https://open-meteo.com/) | Free for non-commercial; attribution appreciated |
| Places | [OpenStreetMap](https://www.openstreetmap.org/) / Overpass | ODbL – © OpenStreetMap contributors |
| Summaries | [Wikipedia](https://www.mediawiki.org/wiki/API:Main_page) | CC BY-SA – User-Agent required |
| Currency | [Frankfurter](https://www.frankfurter.app/) | Free ECB-based rates |

## Architecture

- Multi-module: `app` / `core` / `feature-*`
- UI: Compose Root/Content split, `StateFlow`, `handleAction`
- DI: Koin
- Network: single Ktor `HttpClient` in `:core`
- No raw string literals for routes, URLs, or UI copy (constants + `strings.xml`)

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).
