(ns app.server.next-jdbc-config
  (:require [cheshire.core :as json]
            [java-time :as jt]

            [next.jdbc.prepare    :as prepare]
            [next.jdbc.result-set :as rs])
  (:import
   (java.sql PreparedStatement Timestamp)
   (org.postgresql.util PGobject)
   (org.postgresql.jdbc PgArray)))

(def ->json json/generate-string)

(defn <-json
  [v]
  (json/parse-string v keyword))

(defn ->pgobject
  "Transforms Clojure data to a PGobject that contains the data as JSON. 
   PGObject type defaults to `jsonb` but can be changed via metadata key `:pgtype`"
  [x]
  (let [pgtype (or (:pgtype (meta x)) "jsonb")]
    (doto (PGobject.)
      (.setType pgtype)
      (.setValue (->json x)))))

(defn <-pgobject
  "Transform PGobject containing `json` or `jsonb` value to Clojure data."
  [^PGobject v]
  (let [type  (.getType v)
        value (.getValue v)]
    (if (#{"jsonb" "json"} type)
      (when value
        (with-meta (<-json value) {:pgtype type}))
      value)))
                              
(extend-protocol prepare/SettableParameter
  clojure.lang.IPersistentMap
  (set-parameter [m ^PreparedStatement s i]
    (.setObject s i (->pgobject m)))

  clojure.lang.IPersistentVector
  (set-parameter [v ^PreparedStatement s i]
    (.setObject s i (->pgobject v))))
                              
(extend-protocol rs/ReadableColumn
  PGobject
  (read-column-by-label [^PGobject v _]
    (<-pgobject v))
  (read-column-by-index [^PGobject v _2 _3]
    (<-pgobject v))

  Timestamp
  (read-column-by-label [^Timestamp v _]
    (-> v (java-time/instant) (str)))
  (read-column-by-index [^Timestamp v _2 _3]
    (-> v (java-time/instant) (str)))

  PgArray
  (read-column-by-index [val _meta _idx] (vec (.getArray val))))
