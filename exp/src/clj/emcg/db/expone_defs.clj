(ns emcg.db.expone-defs)

;; experiment stimuli consist of:
;; -- a number of emotionally-charged audio-visual stimuli
;; -- the usual McGuirk-effect stimuli:
;;    * several audio stimuli for different syllables
;;    * corresponding video stimuli for the same syllables

(def emo-stim-filenames
  (list
   "placeholder_E1.mp4"
   "placeholder_E2.mp4"))

;; index first by visual, then by audio
(def mcg-stim-filenames
  (list
   (list
    "placeholder_V1A1.mp4"
    "placeholder_V1A2.mp4"
    "placeholder_V1A3.mp4")
   (list
    "placeholder_V2A1.mp4"
    "placeholder_V2A2.mp4"
    "placeholder_V2A3.mp4")
   (list
    "placeholder_V3A1.mp4"
    "placeholder_V3A2.mp4"
    "placeholder_V3A3.mp4")))

(assert
 (= 1 (count (set (map count mcg-stim-filenames))))
 "same number of audio stim for each visual stim")
(assert
 (= (count mcg-stim-filenames) (count (first mcg-stim-filenames)))
 "same number of visual stim as audio stim")

(def exp-stim-config
  {:num-emo (count emo-stim-filenames)
   :num-mcg (count mcg-stim-filenames)})
