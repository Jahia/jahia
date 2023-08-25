-- DbFileSystem - global
create table JR_FSG_FSENTRY (FSENTRY_PATH varchar not null, FSENTRY_NAME varchar not null, FSENTRY_DATA bytea null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create unique index JR_FSG_FSENTRY_IDX on JR_FSG_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);

-- PersistenceManager - default workspace
create table JR_DEFAULT_BUNDLE (NODE_ID_HI bigint not null, NODE_ID_LO bigint not null, BUNDLE_DATA bytea not null, PRIMARY KEY (NODE_ID_HI, NODE_ID_LO));
create table JR_DEFAULT_REFS (NODE_ID_HI bigint not null, NODE_ID_LO bigint not null, REFS_DATA bytea not null, PRIMARY KEY (NODE_ID_HI, NODE_ID_LO));
create table JR_DEFAULT_BINVAL (BINVAL_ID varchar(64) not null, BINVAL_DATA bytea not null);
create unique index JR_DEFAULT_BINVAL_IDX on JR_DEFAULT_BINVAL (BINVAL_ID);
create table JR_DEFAULT_NAMES (ID SERIAL PRIMARY KEY, NAME varchar(255) not null);
-- PersistenceManager - live workspace
create table JR_LIVE_BUNDLE (NODE_ID_HI bigint not null, NODE_ID_LO bigint not null, BUNDLE_DATA bytea not null, PRIMARY KEY (NODE_ID_HI, NODE_ID_LO));
create table JR_LIVE_REFS (NODE_ID_HI bigint not null, NODE_ID_LO bigint not null, REFS_DATA bytea not null, PRIMARY KEY (NODE_ID_HI, NODE_ID_LO));
create table JR_LIVE_BINVAL (BINVAL_ID varchar(64) not null, BINVAL_DATA bytea not null);
create unique index JR_LIVE_BINVAL_IDX on JR_LIVE_BINVAL (BINVAL_ID);
create table JR_LIVE_NAMES (ID SERIAL PRIMARY KEY, NAME varchar(255) not null);
-- PersistenceManager - versioning
create table JR_V_BUNDLE (NODE_ID_HI bigint not null, NODE_ID_LO bigint not null, BUNDLE_DATA bytea not null, PRIMARY KEY (NODE_ID_HI, NODE_ID_LO));
create table JR_V_REFS (NODE_ID_HI bigint not null, NODE_ID_LO bigint not null, REFS_DATA bytea not null, PRIMARY KEY (NODE_ID_HI, NODE_ID_LO));
create table JR_V_BINVAL (BINVAL_ID varchar(64) not null, BINVAL_DATA bytea not null);
create unique index JR_V_BINVAL_IDX on JR_V_BINVAL (BINVAL_ID);
create table JR_V_NAMES (ID SERIAL PRIMARY KEY, NAME varchar(255) not null);

-- Journal
create table JR_J_JOURNAL (REVISION_ID BIGINT NOT NULL, JOURNAL_ID varchar(255), PRODUCER_ID varchar(255), REVISION_DATA bytea);
create unique index JR_J_JOURNAL_IDX on JR_J_JOURNAL (REVISION_ID);
create table JR_J_GLOBAL_REVISION (REVISION_ID BIGINT NOT NULL);
create unique index JR_J_GLOBAL_REVISION_IDX on JR_J_GLOBAL_REVISION (REVISION_ID);
create table JR_J_LOCAL_REVISIONS (JOURNAL_ID varchar(255) NOT NULL, REVISION_ID BIGINT NOT NULL, PRIMARY KEY (JOURNAL_ID));
create table JR_J_LOCKS (NODE_ID CHAR(40) NOT NULL, JOURNAL_ID VARCHAR(255) NOT NULL, PRIMARY KEY (NODE_ID));
-- Inserting the one and only revision counter record now helps avoiding race conditions
insert into JR_J_GLOBAL_REVISION VALUES(0);

-- DbDataStore
create table JR_DATASTORE (ID VARCHAR(255) PRIMARY KEY, LENGTH BIGINT, LAST_MODIFIED BIGINT, DATA BYTEA);

