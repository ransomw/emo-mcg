(ns emcg.e2e-runner-create-exp
  (:require
   [doo.runner :refer-macros [doo-tests]]
   [emcg.create-exp-test]
   ))

(doo-tests
 'emcg.create-exp-test
 )
