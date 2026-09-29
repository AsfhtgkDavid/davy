# Changelog

All notable changes to this project will be documented in this file.

## [1.3.0] - 2026-09-29

### Added

- Title and episode number display inside the player
  overlay ([#25](https://github.com/AsfhtgkDavid/davy/issues/25)).
- Loading animations and error message views on several
  screens ([#34](https://github.com/AsfhtgkDavid/davy/issues/34)).

### Changed

- Hide the episode selector grid for single-episode
  media ([#29](https://github.com/AsfhtgkDavid/davy/issues/29)).
- Blocked unsupported streaming balancers ([#30](https://github.com/AsfhtgkDavid/davy/issues/30)).

### Fixed

- Playback timestamp formatting failing to display hours for videos exceeding 60
  minutes ([#26](https://github.com/AsfhtgkDavid/davy/issues/26)).

## [1.2.0] - 2026-09-19

### Added

- Search filtering support ([#3](https://github.com/AsfhtgkDavid/davy/issues/3)).

### Changed

- Prevented on-screen keyboard from opening on search bar focus, requiring an explicit OK action.
- Improved player seeking behavior for predictable playback scrubbing.

### Optimized

- Anime details caching mechanism.

### Fixed

- Miscellaneous minor bugs and stability issues.

## [1.1.0] - 2026-08-20

### Added

- **Subtitles**: Added support for external subtitles (specifically for Alloha).
- **Playback**: Video quality options are now automatically sorted, with the highest quality
  selected by default.

### Changed

- **Performance**: Reduced the number of API requests through improved caching mechanisms.

### Fixed

- **Player**: Resolved an issue with the back button behavior in the video
  player ([#12](https://github.com/AsfhtgkDavid/davy/issues/12)).
- **UI**: Fixed the incorrect display of quality
  settings ([#11](https://github.com/AsfhtgkDavid/davy/issues/11)).
