# DAVY

![GitHub License](https://img.shields.io/github/license/AsfhtgkDavid/davy)
![GitHub Issues](https://img.shields.io/github/issues/AsfhtgkDavid/davy)
![F-Droid Version](https://img.shields.io/f-droid/v/dev.daika.davy)

DAVY is a modern Android anime app for discovering and watching anime from the current season. It is
designed to feel lightweight and approachable while still offering a polished experience for
browsing, selecting episodes, and playing videos.

## Description

DAVY is built with Kotlin and Jetpack Compose, with interfaces adapted for both phones and TV
devices, and a built-in media player. The app pulls anime data from the Yummy API and helps users
move quickly from discovery to playback.

On phones, DAVY provides a touch-friendly mobile interface for browsing, searching, viewing anime
details, selecting episodes, and watching videos. On TV platforms (Android TV / Google TV), the app
uses focus-based navigation and remote-friendly controls. The initial mode can be selected when the
app starts.

## Architecture

DAVY uses a modular architecture that separates shared infrastructure, business logic, UI, and
feature flows:

- `app` — application entry point, dependency injection, and navigation
- `core:common` — shared utilities and preferences
- `core:domain` — domain models, repository contracts, and use cases
- `core:data` — Yummy API integration, DTOs, and repository implementations
- `core:ui` — shared Compose components and mobile/TV themes
- `feature:*` — isolated feature modules for mode selection, home, anime details, player, and
  search

Feature modules reuse the shared domain and data layers while providing separate `mobile` and `tv`
composables where the interaction model differs.

## Tech stack

- Kotlin
- Jetpack Compose (Compose for TV)
- ExoPlayer (media playback)
- AndroidX TV
- Yummy API (data source)
- Coroutines (asynchronous work)
- Retrofit & OkHttp (networking)

## Features

- Browse the current season's anime lineup
- Open detailed anime pages with synopsis, genres, ratings, and alternative titles
- Choose between available translations and players
- Select episodes directly from the anime details flow
- Watch videos with a built-in player powered by ExoPlayer
- Mobile UI for phones with touch-friendly navigation
- TV-friendly layout with focus-based navigation

## Roadmap

- [x] Current season anime browsing
- [x] Built-in player
- [x] Search ([#3](https://github.com/AsfhtgkDavid/davy/issues/3))
- [x] Phone support with a dedicated mobile UI
- [ ] Local watch history
- [ ] Integration with MyAnimeList and other watchlists
- [ ] Integration with other anime aggregators

## Screenshots

![HomeScreen.png](fastlane/metadata/android/en-US/images/tvScreenshots/HomeScreen.png)

![DetailView.png](fastlane/metadata/android/en-US/images/tvScreenshots/DetailView.png)

## Donate

If you find DAVY helpful, consider supporting the project with a cryptocurrency donation:

- **TON/Gram (Toncoin)**: `UQD6fbJ73BmC7qPgoR_UrilSBu6kcBMPeNg-J9ZHlZOE1gFQ`
- **BTC (Bitcoin)**: `bc1q5xmq99krq0vykhzpgc5upl5vzttfdl2feflf53`
- **ETH (Ethereum)**: `0x87B0F2f291015Acd4a2A941cefE492F56E362d11`
- **XMR (Monero)**:
  `8Bf41hGnngn1S8vHzaRuTg14VfowCWU1nLQUmWerudh11fSZVDGMuXu4qSmnpoCw8tWbKYBPc6KWEWHb1PvjNTy1Q4SUywR`

Every contribution helps keep the project alive and funded!

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) for the full development
workflow, coding guidelines, and pull request process.

If you want to add support for new player parsers, please work in the separate parser repository:

- https://github.com/AsfhtgkDavid/davy-parsers

Before submitting, make sure the project still builds:

```bash
./gradlew assembleDebug
```

## License

This project is licensed under the GNU Affero General Public License v3.0. See
the [LICENSE](LICENSE) file for more information.
