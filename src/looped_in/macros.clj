(ns looped-in.macros)

(defmacro get-in-item [m ks] `(get-in ~m (vec (interpose :children ~ks))))
