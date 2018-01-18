(ns looped-in.macros)

(defmacro get-in-items [m ks] `(get-in ~m (vec (interpose :children ~ks))))
