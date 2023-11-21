(ns promesa.core
  (:refer-clojure :exclude [delay]))

(defmacro delay [ms]
  `(js/Promise. (fn [resolve# _#]
                  (js/setTimeout resolve# ~ms))))
