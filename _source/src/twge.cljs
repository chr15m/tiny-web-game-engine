(ns twge
  (:require
    [promesa.core :as p]))

(defn ^:export timeout [ms]
  (p/delay ms))

(defn ^:export goober [] "goober")
