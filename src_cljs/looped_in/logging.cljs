(ns looped-in.logging)

(defn info [& args]
  (apply js/console.info "[Looped In]" args))

(defn error [& args]
  (apply js/console.error "[Looped In]" args))

(defn debug [& args]
  (apply js/console.debug "[Looped In]" args))

(defn warn [& args]
  (apply js/console.warn "[Looped In]" args))
