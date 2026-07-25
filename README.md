# DAVY

DAVY is a modern Android anime app for discovering and watching anime from the current season. It is
designed to feel lightweight and approachable while still offering a polished experience for
browsing, selecting episodes, and playing videos.

## Description

DAVY is built with Kotlin and Jetpack Compose, using a TV-friendly interface and a built-in media
player. The app pulls anime data from the Yummy API and helps users move quickly from discovery to
playback.

## Features

- Browse the current season's anime lineup
- Open detailed anime pages with synopsis, genres, ratings, and alternative titles
- Choose between available translations and players
- Select episodes directly from the anime details flow
- Watch videos with a built-in player powered by ExoPlayer
- TV-friendly layout with focus-based navigation

## Roadmap

- [x] Current season anime browsing
- [x] Built-in player
- [ ] Search ([#3](https://github.com/AsfhtgkDavid/davy/issues/3))
- [ ] Local watch history
- [ ] Integration with MyAnimeList and other watchlists
- [ ] Integration with other anime aggregators

## Screenshots

![HomeScreen.png](images/HomeScreen.png)

![DetailView.png](images/DetailView.png)

## Contributing

Contributions are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md) for the full development workflow, coding guidelines, and pull request process.

If you want to add support for new player parsers, please work in the separate parser repository:

- https://github.com/AsfhtgkDavid/davy-parsers

Before submitting, make sure the project still builds:

```bash
./gradlew assembleDebug
```

## License

This project is licensed under the GNU Affero General Public License v3.0. See
the [LICENSE](LICENSE) file for more information.
