# DbFileSystem
drop table COPY_JR_FSG_FSENTRY;

# PersistenceManager
drop table COPY_JR_DEFAULT_BUNDLE;
drop table COPY_JR_DEFAULT_REFS;
drop table COPY_JR_DEFAULT_BINVAL;
drop table COPY_JR_DEFAULT_NAMES;
drop table COPY_JR_LIVE_BUNDLE;
drop table COPY_JR_LIVE_REFS;
drop table COPY_JR_LIVE_BINVAL;
drop table COPY_JR_LIVE_NAMES;
drop table COPY_JR_V_BUNDLE;
drop table COPY_JR_V_REFS;
drop table COPY_JR_V_BINVAL;
drop table COPY_JR_V_NAMES;

# DbDataStore
drop table COPY_JR_DATASTORE;

# DbFileSystem - global
create table COPY_JR_FSG_FSENTRY (FSENTRY_PATH varchar(2048) not null, FSENTRY_NAME varchar(255) not null, FSENTRY_DATA image null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create unique index COPY_JR_FSG_FSENTRY_IDX on COPY_JR_FSG_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);

# PersistenceManager - default workspace
create table COPY_JR_DEFAULT_BUNDLE (NODE_ID binary(16) not null, BUNDLE_DATA image not null);
create unique index COPY_JR_DEFAULT_BUNDLE_IDX on COPY_JR_DEFAULT_BUNDLE (NODE_ID);
create table COPY_JR_DEFAULT_REFS (NODE_ID binary(16) not null, REFS_DATA image not null);
create unique index COPY_JR_DEFAULT_REFS_IDX on COPY_JR_DEFAULT_REFS (NODE_ID);
create table COPY_JR_DEFAULT_BINVAL (BINVAL_ID varchar(64) not null, BINVAL_DATA image not null);
create unique index COPY_JR_DEFAULT_BINVAL_IDX on COPY_JR_DEFAULT_BINVAL (BINVAL_ID);
create table COPY_JR_DEFAULT_NAMES (ID INTEGER IDENTITY(1,1) PRIMARY KEY, NAME varchar(255) COLLATE Latin1_General_CS_AS not null);
# PersistenceManager - live workspace
create table COPY_JR_LIVE_BUNDLE (NODE_ID binary(16) not null, BUNDLE_DATA image not null);
create unique index COPY_JR_LIVE_BUNDLE_IDX on COPY_JR_LIVE_BUNDLE (NODE_ID);
create table COPY_JR_LIVE_REFS (NODE_ID binary(16) not null, REFS_DATA image not null);
create unique index COPY_JR_LIVE_REFS_IDX on COPY_JR_LIVE_REFS (NODE_ID);
create table COPY_JR_LIVE_BINVAL (BINVAL_ID varchar(64) not null, BINVAL_DATA image not null);
create unique index COPY_JR_LIVE_BINVAL_IDX on COPY_JR_LIVE_BINVAL (BINVAL_ID);
create table COPY_JR_LIVE_NAMES (ID INTEGER IDENTITY(1,1) PRIMARY KEY, NAME varchar(255) COLLATE Latin1_General_CS_AS not null);
# PersistenceManager - versioning
create table COPY_JR_V_BUNDLE (NODE_ID binary(16) not null, BUNDLE_DATA image not null);
create unique index COPY_JR_V_BUNDLE_IDX on COPY_JR_V_BUNDLE (NODE_ID);
create table COPY_JR_V_REFS (NODE_ID binary(16) not null, REFS_DATA image not null);
create unique index COPY_JR_V_REFS_IDX on COPY_JR_V_REFS (NODE_ID);
create table COPY_JR_V_BINVAL (BINVAL_ID varchar(64) not null, BINVAL_DATA image not null);
create unique index COPY_JR_V_BINVAL_IDX on COPY_JR_V_BINVAL (BINVAL_ID);
create table COPY_JR_V_NAMES (ID INTEGER IDENTITY(1,1) PRIMARY KEY, NAME varchar(255) COLLATE Latin1_General_CS_AS not null);

# DbDataStore
create table COPY_JR_DATASTORE (ID VARCHAR(255) PRIMARY KEY, LENGTH BIGINT, LAST_MODIFIED BIGINT, DATA IMAGE);
