(ns looped-in.macros)

(defmacro get-in-item [m ks]
  `(if (seq ~ks)
     (get-in ~m (cons :children (interpose :children ~ks)))
     (get-in ~m ~ks)))
