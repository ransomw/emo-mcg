(ns emcg.e2e-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [emcg.comm-test]
   [emcg.act-test]
   ))

(doo-tests
 'emcg.comm-test
 'emcg.act-test
 )
