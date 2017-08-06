(ns emcg.e2e-runner-add-mcg-res
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [emcg.add-mcg-res-test]
   ))

(doo-tests
 'emcg.add-mcg-res-test
 )
