(ns looped-in.logging)

(defn info [& args]
  (apply js/console.info "[Looped In]" (map clj->js args)))

(defn error [& args]
  (apply js/console.error "[Looped In]" (map clj->js args)))

(defn debug [& args]
  (apply js/console.debug "[Looped In]" (map clj->js args)))

(defn warn [& args]
  (apply js/console.warn "[Looped In]" (map clj->js args)))
