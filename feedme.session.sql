select * from ragtime_migrations;

select * from menu;

TRUNCATE menu;

select * from pg_sequences;

ALTER SEQUENCE category_id_seq RESTART;

ALTER SEQUENCE menu_id_seq RESTART;