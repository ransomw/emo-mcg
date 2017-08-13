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
