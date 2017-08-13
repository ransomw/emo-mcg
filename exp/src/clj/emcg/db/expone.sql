-- :name add-exp
-- :command :returning-execute
-- :result :raw
-- :doc insert a row for a new experiment
INSERT INTO expone DEFAULT VALUES RETURNING id;

-- :name add-emo-stim
-- :command :returning-execute
-- :result :raw
-- :doc insert a row for an emotional stimulus in an experiment
INSERT INTO expone_emo (expone_id, seq_num, idx_stim)
VALUES (:exp-id, :seq-num, :idx-stim)
RETURNING id;

-- :name add-mcg-stim
-- :command :returning-execute
-- :result :raw
-- :doc a McGurik stimulus following an emotional stimulus
INSERT INTO expone_mcg
(expone_emo_id, seq_num, idx_a_stim, idx_v_stim)
VALUES (:emo-stim-id, :seq-num, :idx-a, :idx-v)
RETURNING id;

-- :name get-exp :? :*
SELECT emo_id, idx_stim, seq_num_emo,
       id as mcg_id, seq_num as seq_num_mcg, idx_a_stim, idx_v_stim
FROM (SELECT expone_emo.id as emo_id, idx_stim,
             expone_emo.seq_num as seq_num_emo
     FROM expone JOIN expone_emo
     ON expone.id = expone_id
     WHERE expone.id = :exp-id) as emo
     JOIN expone_mcg
     ON emo_id = expone_emo_id;

-- :name get-mcg :? :*
SELECT * FROM expone_mcg WHERE id = :mcg-id;

-- :name set-mcg-resp :! :n
UPDATE expone_mcg
SET idx_resp = :idx-resp
WHERE id = :mcg-id;
