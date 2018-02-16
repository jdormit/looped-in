# Change Log
All notable changes to this project will be documented in this file. This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/) and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Changed
- Refactored the directory structure and build process to enable separate development and production resources.
- Enabled [Figwheel](https://github.com/bhauman/lein-figwheel) support for the development build.

## [1.1.2]
### Changed
- Revamped the whole build and package process to produce an artifact that meets Chrome and Firefox web store standards.

### Removed
- Removed unecessary permission requests and the custom CSP from the extension manifest, now that the build process enables that.

## [1.1.1] - 2018-02-06
### Changed
- Comments are now sorted by total number of descendant comments, not just number of immediate children.

## [1.1.0] - 2018-02-06
### Fixed
- Pointed the Firefox Add-On Store link in the readme to the right URL.
- Fixed project.clj so that the extension can be compiled with no additional profiles enabled.

### Removed
- Removed all analytics and usage tracking.

## [1.0.1] - 2018-02-05
### Added
- `lein build` alias to clean and compile JavaScript.

### Fixed
- Updated `manifest.json` version to match project version.

## [1.0.0] - 2018-02-04
### Added
- Functionality to fetch and display Hacker News comments for the current URL.
- Analytics through [Amplitude](https://amplitude.com).

[Unreleased]:https://github.com/jdormit/looped-in/compare/v1.1.2...HEAD
[1.1.2]: https://github.com/jdormit/looped-in/compare/v1.1.1...v1.1.2
[1.1.1]: https://github.com/jdormit/looped-in/compare/v1.1.0...v1.1.1
[1.1.0]: https://github.com/jdormit/looped-in/compare/v1.0.1...v1.0.0
[1.0.1]: https://github.com/jdormit/looped-in/compare/v1.0.0...v1.0.1
[1.0.0]: https://github.com/jdormit/looped-in/compare/9bf8d142c6a49b743da4b97574dfed0797dd5b2f...v1.0.0
