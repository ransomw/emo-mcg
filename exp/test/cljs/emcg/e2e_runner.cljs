(ns emcg.e2e-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [emcg.act-test]
   ))

(doo-tests
 'emcg.act-test
 )
