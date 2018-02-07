# Looped In
> A browser extension that displays Hacker News comments for the current webpage

Looped In is a browser extension that displays Hacker News comments for the current webpage in a sidebar. It is written in [ClojureScript](https://clojurescript.org).

Looped In is available from the [Firefox Add-ons site](https://addons.mozilla.org/en-US/firefox/addon/looped-in) and will be available from the Chrome Web Store pending review. If you'd prefer to clone the repository and run the extension locally, read on.

## Usage

To build Looped In locally, you'll need [Leiningen](https://leiningen.org) and [GNU Make](https://www.gnu.org/software/make).

To obtain a copy of the source code:

    $ git clone git@github.com:jdormit/looped-in.git
    
To build the source code once, navigate to the project root and run:

    $ make clean dev

This will output the generated JavaScript to `ext/js/generated`.

To load the extension locally in your browser, see [instructions for Firefox](https://developer.mozilla.org/en-US/Add-ons/WebExtensions/Temporary_Installation_in_Firefox) or [instructions for Chrome](https://developer.chrome.com/extensions/getstarted#unpacked).

## License

Copyright © 2018 Jeremy Dormitzer

Distributed under the GNU General Public License v3. See [LICENSE](./LICENSE).

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
