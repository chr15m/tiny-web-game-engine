(ns doc
  (:require 
    ["fs" :as fs]
    [clojure.core :refer [read-string]]
    [clojure.string :refer [join]]))

(let [f (-> (fs/readFileSync "src/twge.cljs") .toString)
      code (read-string (str "[" f "]"))]
  (doseq [form code]
    (case (keyword (first form))
      :defn (let [fn-name (second form)
                  doc (nth form 2)
                  args (nth form 3)]
              (when (= (type doc) js/String)
                (print "## " fn-name " (" (join " " args) ")")
                (print)
                (print doc)
                (print)))
      ;:def (print "DEF" form)
      ;:ns (print "NS" form)
      nil)))
