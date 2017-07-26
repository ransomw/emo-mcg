(ns emcg.test-runner
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [emcg.core-test]
   [emcg.comm-test]
   [emcg.common-test]))

(enable-console-print!)

(doo-tests 'emcg.core-test
           'emcg.comm-test
           'emcg.common-test)
