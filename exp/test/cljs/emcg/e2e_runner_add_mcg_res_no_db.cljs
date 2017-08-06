(ns emcg.e2e-runner-add-mcg-res-no-db
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [emcg.add-mcg-res-no-db-test]
   ))

(doo-tests
 'emcg.add-mcg-res-no-db-test
 )
