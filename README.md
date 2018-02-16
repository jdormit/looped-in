# Looped In
> A browser extension that displays Hacker News comments for the current webpage

Looped In is a browser extension that displays Hacker News comments for the current webpage in a sidebar. It is written in [ClojureScript](https://clojurescript.org).

Looped In will be available from the Firefox Add-ons site and the Chrome Web Store pending review. If you'd prefer to clone the repository and run the extension locally, read on.

## Usage

To build Looped In locally, you'll need [Leiningen](https://leiningen.org) and [GNU Make](https://www.gnu.org/software/make).

To obtain a copy of the source code:

    $ git clone git@github.com:jdormit/looped-in.git
    
To build the source code, navigate to the project root and run:

    $ make dev

This will output a development build of the extension to `ext`. Additionally, it will start a [Figwheel](https://github.com/bhauman/lein-figwheel) session for the background and sidebar scripts, enabling live reloading and connecting a ClojureScript REPL to the sidebar script. Due to the strict content security policy for content scripts, Figwheel cannot be enabled for the content script.

If you want a production build instead, run:

    $ make prod

This will output an optimized production build of the extension to `ext`. The production build does not feature live reloading. It also takes significantly longer than the development build.

To load the extension locally in your browser, see [instructions for Firefox](https://developer.mozilla.org/en-US/Add-ons/WebExtensions/Temporary_Installation_in_Firefox) or [instructions for Chrome](https://developer.chrome.com/extensions/getstarted#unpacked).

To package the extension for publication, run:

    $ make package

This will package a production build of the extension to `dist/looped-in.zip`.

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
