(ns emcg.db.expone-hug
  (:require
   [hugsql.core :as hugsql]
   ))

(hugsql/def-db-fns "emcg/db/expone.sql")
(hugsql/def-sqlvec-fns "emcg/db/expone.sql")
