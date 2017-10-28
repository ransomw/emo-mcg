(ns emcg.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [emcg.core-test]
   ))

(enable-console-print!)

(doo-tests
 'emcg.core-test
 )
