-- :name create-table
-- :command :execute
-- :result :raw
-- :doc Create some test table, call it thingone
--  auto_increment and current_timestamp are
--  H2 Database specific (adjust to your DB)
CREATE TABLE thingone (
  id serial PRIMARY KEY,
  nameone varchar(40),
  created_at timestamp not null default current_timestamp
);

-- :name drop-table
-- :command :execute
-- :doc Drop thingone table if exists
DROP TABLE IF EXISTS thingone;
