# Climora – Android Weather App

Climora is a production-quality Android weather application built with **Clean Architecture** and **MVVM**. It is designed for scalability, testability, and maintainability — with a clear separation between data, domain, and presentation layers.

---

## Architecture

The project follows **Clean Architecture** with three distinct layers:

```
app/
├── data/           # DTOs, Retrofit API, repository implementations, location provider
├── domain/         # Entities, repository protocols, use cases, domain errors
└── presentation/   # ViewModels (MVVM), Fragments, UI state, adapters
```

### Layer Responsibilities

| Layer | Responsibility |
|---|---|
| **Domain** | Pure Kotlin. Defines `Weather`, `ForecastDay`, `Coordinates` models, `WeatherRepository` interface, `GetCurrentWeatherUseCase`, `GetForecastUseCase`, and `DomainError`. No Android dependencies. |
| **Data** | Implements `WeatherRepository`. Owns `WeatherApi` (Retrofit), `WeatherDto`/`ForecastDto` response models, `NetworkErrorMapper`, and `LocationProviderImpl`. Maps API responses to domain models. |
| **Presentation** | `WeatherViewModel` exposes `StateFlow<WeatherUiState>` and `StateFlow<ForecastUiState>` consumed by `WeatherFragment` via `lifecycleScope`. |
| **DI** | Hilt modules (`AppModule`, `RepositoryModule`, `LocationModule`) wire all dependencies. |

### Key Patterns

- **Repository Pattern** — `WeatherRepository` interface in Domain; `WeatherRepositoryImpl` in Data. The presentation layer never touches the network directly.
- **Use Case Layer** — `GetCurrentWeatherUseCase` and `GetForecastUseCase` encapsulate business logic. Each supports city-name and coordinate-based lookups.
- **MVVM + StateFlow** — `WeatherViewModel` uses `MutableStateFlow` with `.update {}` for atomic state transitions. The fragment collects state using `repeatOnLifecycle`.
- **Dependency Injection** — Hilt handles all dependency wiring. No manual service locators or singletons.
- **Single Activity** — `MainActivity` is a thin shell hosting `WeatherFragment` via `FragmentContainerView`.
- **Parallel async loading** — Current weather and 3-day forecast are fetched concurrently using `async`/`await` in the same coroutine scope. Each fails independently.

---

## Features

- Search weather by city name
- Current location weather via FusedLocationProvider
- 3-day forecast loaded in parallel with current weather
- Humidity and feels-like detail card
- Dynamic background based on weather condition
- Adaptive light/dark mode via `values/` and `values-night/` color resources
- Secure API key via `local.properties` (never committed)

---

## Project Structure

```
app/src/main/java/com/flash/climora/
├── core/           # Shared Result type
├── data/           # Retrofit API, DTOs, repository impl, location provider
├── di/             # Hilt modules
├── domain/         # Models, repository interface, use cases, domain errors
└── presentation/   # Activity, Fragment, ViewModel, UI state, adapters
```

---

## Tech Stack

| Technology | Usage |
|---|---|
| Kotlin | Primary language |
| Android Views (XML) | UI layout |
| Coroutines + Flow | Async operations and reactive state |
| Hilt | Dependency injection |
| Retrofit + Gson | HTTP networking |
| FusedLocationProvider | Device GPS coordinates |
| View Binding | Type-safe view access |

---

## Configuration

The project reads the API key from `local.properties` at build time.

1. Open `local.properties` (created automatically by Android Studio)
2. Add your [WeatherAPI](https://www.weatherapi.com) key:
   ```
   WEATHER_API_KEY=your_api_key_here
   ```
3. Build and run.

`local.properties` is excluded from version control via `.gitignore`.

