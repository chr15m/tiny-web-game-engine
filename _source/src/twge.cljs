(ns twge
  (:require
    [promesa.core :as p]
    ["preact" :refer [h render]]))

(defn ^:export timeout [ms]
  (p/delay ms))

(defn ^:export draw []
  (let [app (h "h1" nil "Hello")
        el (.getElementById js/document "twge-default")]
    (aset el "innerHTML" "")
    (render app el)))
