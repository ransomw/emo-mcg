------ creates and drops

-- :name create-table-mcg
-- :command :execute
-- :result :raw
-- :doc create table for McGuirk effect stim and resp
CREATE TABLE expone_mcg (
  id serial PRIMARY KEY,
  expone_emo_id integer references expone_emo (id),
  seq_num integer, -- per emotional-stim index
  idx_a_stim integer,
  idx_v_stim integer,
  idx_resp integer
);

-- :name drop-table-mcg
-- :command :execute
-- :doc Drop expone_mcg table if exists
DROP TABLE IF EXISTS expone_mcg;

-- :name create-table-emo
-- :command :execute
-- :result :raw
-- :doc create table for sandbox experiment emotional stim
CREATE TABLE expone_emo (
  id serial PRIMARY KEY,
  expone_id integer references expone (id),
  seq_num integer,
  idx_stim integer
);

-- :name drop-table-emo
-- :command :execute
-- :doc Drop expone_emo table if exists
DROP TABLE IF EXISTS expone_emo;

-- :name create-table-exp
-- :command :execute
-- :result :raw
-- :doc create table for sandbox experiment
CREATE TABLE expone (
  id serial PRIMARY KEY,
  -- nameexp varchar(40),
  created_at timestamp not null default current_timestamp
);

-- :name drop-table-exp
-- :command :execute
-- :doc Drop expone table if exists
DROP TABLE IF EXISTS expone;

-------- inserts

-- :name add-exp-hug
-- :command :returning-execute
-- :result :raw
-- :doc insert a row for a new experiment
INSERT INTO expone DEFAULT VALUES RETURNING id;

-- :name add-emo-stim-hug
-- :command :returning-execute
-- :result :raw
-- :doc insert a row for an emotional stimulus in an experiment
INSERT INTO expone_emo (expone_id, seq_num, idx_stim)
VALUES (:exp-id, :seq-num, :idx-stim)
RETURNING id;

-- :name add-mcg-stim-hug
-- :command :returning-execute
-- :result :raw
-- :doc a McGurik stimulus following an emotional stimulus
INSERT INTO expone_mcg
(expone_emo_id, seq_num, idx_a_stim, idx_v_stim)
VALUES (:emo-stim-id, :seq-num, :idx-a, :idx-v)
RETURNING id;

-- :name get-exp-hug :? :*
SELECT emo_id, idx_stim, seq_num_emo,
       id as mcg_id, seq_num as seq_num_mcg, idx_a_stim, idx_v_stim
FROM (SELECT expone_emo.id as emo_id, idx_stim,
             expone_emo.seq_num as seq_num_emo
     FROM expone JOIN expone_emo
     ON expone.id = expone_id
     WHERE expone.id = :exp-id) as emo
     JOIN expone_mcg
     ON emo_id = expone_emo_id;

-- :name get-mcg-hug :? :*
SELECT * FROM expone_mcg WHERE id = :mcg-id;

-- :name set-mcg-resp-hug :! :n
UPDATE expone_mcg
SET idx_resp = :idx-resp
WHERE id = :mcg-id;
