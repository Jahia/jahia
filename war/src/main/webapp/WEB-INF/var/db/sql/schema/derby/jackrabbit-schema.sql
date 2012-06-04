-- DbFileSystem
drop table JR_FSG_FSENTRY;

-- PersistenceManager
drop table JR_DEFAULT_BUNDLE;
drop table JR_DEFAULT_REFS;
drop table JR_DEFAULT_BINVAL;
drop table JR_DEFAULT_NAMES;
drop table JR_LIVE_BUNDLE;
drop table JR_LIVE_REFS;
drop table JR_LIVE_BINVAL;
drop table JR_LIVE_NAMES;
drop table JR_V_BUNDLE;
drop table JR_V_REFS;
drop table JR_V_BINVAL;
drop table JR_V_NAMES;

-- Journal
drop table JR_J_JOURNAL;
drop table JR_J_GLOBAL_REVISION;
drop table JR_J_LOCAL_REVISIONS;

-- DbDataStore
drop table JR_DATASTORE;

-- DbFileSystem - global
create table JR_FSG_FSENTRY (FSENTRY_PATH varchar(2048) not null, FSENTRY_NAME varchar(255) not null, FSENTRY_DATA blob(100M), FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create unique index JR_FSG_FSENTRY_IDX on JR_FSG_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);

-- PersistenceManager - default workspace
create table JR_DEFAULT_BUNDLE (NODE_ID_HI bigint not null, NODE_ID_LO bigint not null, BUNDLE_DATA blob(2G) not null, PRIMARY KEY (NODE_ID_HI, NODE_ID_LO));
create table JR_DEFAULT_REFS (NODE_ID_HI bigint not null, NODE_ID_LO bigint not null, REFS_DATA blob(2G) not null, PRIMARY KEY (NODE_ID_HI, NODE_ID_LO));
create table JR_DEFAULT_BINVAL (BINVAL_ID char(64) PRIMARY KEY, BINVAL_DATA blob(2G) not null);
create table JR_DEFAULT_NAMES (ID INTEGER GENERATED ALWAYS AS IDENTITY, NAME varchar(255) not null, PRIMARY KEY (ID, NAME));
-- PersistenceManager - live workspace
create table JR_LIVE_BUNDLE (NODE_ID_HI bigint not null, NODE_ID_LO bigint not null, BUNDLE_DATA blob(2G) not null, PRIMARY KEY (NODE_ID_HI, NODE_ID_LO));
create table JR_LIVE_REFS (NODE_ID_HI bigint not null, NODE_ID_LO bigint not null, REFS_DATA blob(2G) not null, PRIMARY KEY (NODE_ID_HI, NODE_ID_LO));
create table JR_LIVE_BINVAL (BINVAL_ID char(64) PRIMARY KEY, BINVAL_DATA blob(2G) not null);
create table JR_LIVE_NAMES (ID INTEGER GENERATED ALWAYS AS IDENTITY, NAME varchar(255) not null, PRIMARY KEY (ID, NAME));
-- PersistenceManager - versioning
create table JR_V_BUNDLE (NODE_ID_HI bigint not null, NODE_ID_LO bigint not null, BUNDLE_DATA blob(2G) not null, PRIMARY KEY (NODE_ID_HI, NODE_ID_LO));
create table JR_V_REFS (NODE_ID_HI bigint not null, NODE_ID_LO bigint not null, REFS_DATA blob(2G) not null, PRIMARY KEY (NODE_ID_HI, NODE_ID_LO));
create table JR_V_BINVAL (BINVAL_ID char(64) PRIMARY KEY, BINVAL_DATA blob(2G) not null);
create table JR_V_NAMES (ID INTEGER GENERATED ALWAYS AS IDENTITY, NAME varchar(255) not null, PRIMARY KEY (ID, NAME));

-- Journal
create table JR_J_JOURNAL (REVISION_ID BIGINT NOT NULL, JOURNAL_ID varchar(255), PRODUCER_ID varchar(255), REVISION_DATA blob);
create unique index JR_J_JOURNAL_IDX on JR_J_JOURNAL (REVISION_ID);
create table JR_J_GLOBAL_REVISION (REVISION_ID BIGINT NOT NULL);
create unique index JR_J_GLOBAL_REVISION_IDX on JR_J_GLOBAL_REVISION (REVISION_ID);
create table JR_J_LOCAL_REVISIONS (JOURNAL_ID varchar(255) NOT NULL, REVISION_ID BIGINT NOT NULL);
-- Inserting the one and only revision counter record now helps avoiding race conditions;
insert into JR_J_GLOBAL_REVISION VALUES(0);

-- DbDataStore
create table JR_DATASTORE (ID VARCHAR(255) PRIMARY KEY, LENGTH BIGINT, LAST_MODIFIED BIGINT, DATA BLOB);
