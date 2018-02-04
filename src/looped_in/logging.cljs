;; Copyright © 2018 Jeremy Dormitzer
;;
;; This file is part of Looped In.
;;
;; Looped In is free software: you can redistribute it and/or modify
;; it under the terms of the GNU General Public License as published by
;; the Free Software Foundation, either version 3 of the License, or
;; (at your option) any later version.
;;
;; Looped In is distributed in the hope that it will be useful,
;; but WITHOUT ANY WARRANTY; without even the implied warranty of
;; MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
;; GNU General Public License for more details.
;;
;; You should have received a copy of the GNU General Public License
;; along with Looped In.  If not, see <http://www.gnu.org/licenses/>.

(ns looped-in.logging)

(defn info [& args]
  (apply js/console.info "[Looped In]" (map clj->js args)))

(defn error [& args]
  (apply js/console.error "[Looped In]" (map clj->js args)))

(defn debug [& args]
  (apply js/console.debug "[Looped In]" (map clj->js args)))

(defn warn [& args]
  (apply js/console.warn "[Looped In]" (map clj->js args)))
