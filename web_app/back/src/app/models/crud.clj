(ns app.models.crud
  (:refer-clojure :exclude [read]))

(defprotocol CRUD
  (create! [this data])
  (read [this id])
  (update! [this id data])
  (delete! [this id])
  (list-all [this params]))
