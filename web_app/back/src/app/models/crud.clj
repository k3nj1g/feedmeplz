(ns app.models.crud)

(defprotocol CRUD
  (create! [this data])
  (read [this id])
  (update! [this id data])
  (delete! [this id])
  (list-all [this]))
