# Looped In
> A browser extension that displays Hacker News comments for the current webpage

Looped In is a browser extension that displays Hacker News comments for the current webpage in a sidebar. It is written in [ClojureScript](https://clojurescript.org).

Looped In will be available from the Firefox Add-ons site and the Chrome Web Store pending review. If you'd prefer to clone the repository and run the extension locally, read on.

## Usage

To obtain a copy of the source code:

    $ git clone git@github.com:jdormit/looped-in.git
    
### Building

To build Looped In locally, you'll need [Leiningen](https://leiningen.org) and [GNU Make](https://www.gnu.org/software/make).

To build the source code, navigate to the project root and run:

    $ make dev

This will output a development build of the extension to `ext`. 

If you want a production build instead, run:

    $ make prod

This will output an optimized production build of the extension to `ext`.

To load the extension locally in your browser, see [instructions for Firefox](https://developer.mozilla.org/en-US/Add-ons/WebExtensions/Temporary_Installation_in_Firefox) or [instructions for Chrome](https://developer.chrome.com/extensions/getstarted#unpacked).

### Figwheel

The development build of Looped In supports live-reloading of the background and sidebar scripts via [Figwheel](https://github.com/bhauman/lein-figwheel). You have two options for connecting to Figwheel: directly in the terminal or through Emacs via [CIDER](https://github.com/clojure-emacs/cider).

#### Connecting to Figwheel from the terminal

Spinning up Figwheel in the terminal is straightforward:

    $ make fig

This will compile a development build of the extension and attach a Figwheel REPL to the background script.

#### Connecting to Figwheel through CIDER

You'll need to install and configure [clojure-mode](https://github.com/clojure-emacs/clojure-mode) and [CIDER](https://github.com/clojure-emacs/cider). If you use [Spacemacs](https://spacemacs.org), just install the Clojure layer and you will be all set. 

Before connecting to Figwheel from Emacs, make sure you have compiled a development build:

    $ make dev

The `.dir-locals.el` file configures CIDER to start Figwheel when it launches a ClojureScript REPL. Launch a ClojureScript REPL with `M-x cider-jack-in-clojurescript`. Then switch to the REPL buffer with `M-x cider-switch-to-repl-buffer`.

#### Switching builds in Figwheel

Whether you use CIDER or the terminal, Figwheel will start connected to and live-reloading the background script. If you want to connect to and live-reload the sidebar script instead, run this command in the Figwheel REPL:

    cljs.user> (switch-to-build sidebar)

### Packaging

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
