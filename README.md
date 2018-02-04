# Looped In
> A browser extension that displays Hacker News comments for the current webpage

Looped In is a browser extension that displays Hacker News comments for the current webpage in a sidebar. It is written in [ClojureScript](https://clojurescript.org).

Looped In is available from the [Chrome Web Store](TODO) and the [Firefox Add-ons site](TODO). If you'd prefer to clone the repository and run the extension locally, read on.

## Usage

To build Looped In locally, you'll need [Leiningen](https://leiningen.org), the Clojure build tool.

To obtain a copy of the source code:

    $ git clone git@github.com:jdormit/looped-in
    
To build the source code once, navigate to the project root and run:

    $ lein cljsbuild once

This will output the generated JavaScript to `ext/js/generated`.

To load the extension locally in your browser, see [instructions for Firefox](https://developer.mozilla.org/en-US/Add-ons/WebExtensions/Temporary_Installation_in_Firefox) or [instructions for Chrome](https://developer.chrome.com/extensions/getstarted#unpacked).

## Analytics
Looped In uses [Amplitude](https://amplitude.com) to track user actions such as clicking on a story or viewing replies to a comment. This data is useful for improving the program. However, some users find this tracking invasive, so Looped In respects the browser's [Do Not Track](https://www.w3.org/2011/tracking-protection/drafts/tracking-dnt.html) setting. If DNT is enabled, no analytics data is collected. To enable DNT, see [instructions for Firefox](https://support.mozilla.org/en-US/kb/how-do-i-turn-do-not-track-feature?redirectlocale=en-US&redirectslug=how-do-i-stop-websites-tracking-me) and [instructions for Chrome](https://support.google.com/chrome/answer/2790761).

If you are running the extension locally, you will either need to get an Amplitude API key or disable the analytics. The easiest way to disable the analytics is to edit the `src/looped_in/analytics.cljs` file. Delete the bodies of the `init-amplitude` and `log-event` functions. It should look something like this:

```clojure
(defn init-amplitude [] ())
(defn log-event []  ())
```

If you want to keep the analytics, you'll need an Amplitude API key. [Sign up for Amplitude] (https://amplitude.com/signup?ref=nav) and follow the getting started guide to create a new project. Once you have the API key, create the file `profiles.clj` at the project root with the following content:

```clojure
{:provided [:cljs-shared
            {:cljsbuild
             {:builds
              {:main
               {:compiler
                {:closure-defines {looped-in.analytics/amplitude-api-key
                                   "<your API key>"}}}}}}]
```

Then run `lein cljsbuild once` to rebuild the extension with the new API key (you may need to first delete `ext/js/generated` to force a fresh build).

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
