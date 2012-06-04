-- DbFileSystem
drop table if exists JR_FSG_FSENTRY;

-- PersistenceManager
drop table if exists JR_DEFAULT_BUNDLE;
drop table if exists JR_DEFAULT_REFS;
drop table if exists JR_DEFAULT_BINVAL;
drop table if exists JR_DEFAULT_NAMES;
drop table if exists JR_LIVE_BUNDLE;
drop table if exists JR_LIVE_REFS;
drop table if exists JR_LIVE_BINVAL;
drop table if exists JR_LIVE_NAMES;
drop table if exists JR_V_BUNDLE;
drop table if exists JR_V_REFS;
drop table if exists JR_V_BINVAL;
drop table if exists JR_V_NAMES;

-- Journal
drop table if exists JR_J_JOURNAL;
drop table if exists JR_J_GLOBAL_REVISION;
drop table if exists JR_J_LOCAL_REVISIONS;

-- DbDataStore
drop table if exists JR_DATASTORE;

-- DbFileSystem - global
create table JR_FSG_FSENTRY (FSENTRY_PATH text not null, FSENTRY_NAME varchar(255) not null, FSENTRY_DATA longblob null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null) ENGINE=InnoDB character set latin1;
create unique index JR_FSG_FSENTRY_IDX on JR_FSG_FSENTRY (FSENTRY_PATH(245), FSENTRY_NAME);

-- PersistenceManager - default workspace
create table JR_DEFAULT_BUNDLE (NODE_ID varbinary(16) not null, BUNDLE_DATA longblob not null) ENGINE=InnoDB;
create unique index JR_DEFAULT_BUNDLE_IDX on JR_DEFAULT_BUNDLE (NODE_ID);
create table JR_DEFAULT_REFS (NODE_ID varbinary(16) not null, REFS_DATA longblob not null) ENGINE=InnoDB;
create unique index JR_DEFAULT_REFS_IDX on JR_DEFAULT_REFS (NODE_ID);
create table JR_DEFAULT_BINVAL (BINVAL_ID varchar(64) not null, BINVAL_DATA longblob not null) ENGINE=InnoDB;
create unique index JR_DEFAULT_BINVAL_IDX on JR_DEFAULT_BINVAL (BINVAL_ID);
create table JR_DEFAULT_NAMES (ID INTEGER AUTO_INCREMENT PRIMARY KEY, NAME varchar(255) character set utf8 collate utf8_bin not null) ENGINE=InnoDB;
-- PersistenceManager - live workspace
create table JR_LIVE_BUNDLE (NODE_ID varbinary(16) not null, BUNDLE_DATA longblob not null) ENGINE=InnoDB;
create unique index JR_LIVE_BUNDLE_IDX on JR_LIVE_BUNDLE (NODE_ID);
create table JR_LIVE_REFS (NODE_ID varbinary(16) not null, REFS_DATA longblob not null) ENGINE=InnoDB;
create unique index JR_LIVE_REFS_IDX on JR_LIVE_REFS (NODE_ID);
create table JR_LIVE_BINVAL (BINVAL_ID varchar(64) not null, BINVAL_DATA longblob not null) ENGINE=InnoDB;
create unique index JR_LIVE_BINVAL_IDX on JR_LIVE_BINVAL (BINVAL_ID);
create table JR_LIVE_NAMES (ID INTEGER AUTO_INCREMENT PRIMARY KEY, NAME varchar(255) character set utf8 collate utf8_bin not null) ENGINE=InnoDB;
-- PersistenceManager - versioning
create table JR_V_BUNDLE (NODE_ID varbinary(16) not null, BUNDLE_DATA longblob not null) ENGINE=InnoDB;
create unique index JR_V_BUNDLE_IDX on JR_V_BUNDLE (NODE_ID);
create table JR_V_REFS (NODE_ID varbinary(16) not null, REFS_DATA longblob not null) ENGINE=InnoDB;
create unique index JR_V_REFS_IDX on JR_V_REFS (NODE_ID);
create table JR_V_BINVAL (BINVAL_ID varchar(64) not null, BINVAL_DATA longblob not null) ENGINE=InnoDB;
create unique index JR_V_BINVAL_IDX on JR_V_BINVAL (BINVAL_ID);
create table JR_V_NAMES (ID INTEGER AUTO_INCREMENT PRIMARY KEY, NAME varchar(255) character set utf8 collate utf8_bin not null) ENGINE=InnoDB;

-- Journal
create table JR_J_JOURNAL (REVISION_ID BIGINT NOT NULL, JOURNAL_ID varchar(255), PRODUCER_ID varchar(255), REVISION_DATA longblob) ENGINE=InnoDB;
create unique index JR_J_JOURNAL_IDX on JR_J_JOURNAL (REVISION_ID);
create table JR_J_GLOBAL_REVISION (REVISION_ID BIGINT NOT NULL) ENGINE=InnoDB;
create unique index JR_J_GLOBAL_REVISION_IDX on JR_J_GLOBAL_REVISION (REVISION_ID);
create table JR_J_LOCAL_REVISIONS (JOURNAL_ID varchar(255) NOT NULL, REVISION_ID BIGINT NOT NULL) ENGINE=InnoDB;
-- Inserting the one and only revision counter record now helps avoiding race conditions
insert into JR_J_GLOBAL_REVISION VALUES(0);

-- DbDataStore
create table JR_DATASTORE (ID VARCHAR(255) PRIMARY KEY, LENGTH BIGINT, LAST_MODIFIED BIGINT, DATA BLOB(2147483647)) ENGINE=InnoDB;
