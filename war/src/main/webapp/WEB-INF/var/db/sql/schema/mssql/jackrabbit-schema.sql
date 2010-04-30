# DbFileSystem
drop table JR_FSG_FSENTRY;
drop table JR_FS_DEFAULT_FSENTRY;
drop table JR_FS_LIVE_FSENTRY;
drop table JR_FSV_FSENTRY;

# PersistenceManager
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

# Journal
drop table JR_J_JOURNAL;
drop table JR_J_GLOBAL_REVISION;
drop table JR_J_LOCAL_REVISIONS;

# DbFileSystem - global
create table JR_FSG_FSENTRY (FSENTRY_PATH varchar(2048) not null, FSENTRY_NAME varchar(255) not null, FSENTRY_DATA image null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create unique index JR_FSG_FSENTRY_IDX on JR_FSG_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);
# DbFileSystem - default workspace
create table JR_FS_DEFAULT_FSENTRY (FSENTRY_PATH varchar(2048) not null, FSENTRY_NAME varchar(255) not null, FSENTRY_DATA image null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create unique index JR_FS_DEFAULT_FSENTRY_IDX on JR_FS_DEFAULT_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);
# DbFileSystem - live workspace
create table JR_FS_LIVE_FSENTRY (FSENTRY_PATH varchar(2048) not null, FSENTRY_NAME varchar(255) not null, FSENTRY_DATA image null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create unique index JR_FS_LIVE_FSENTRY_IDX on JR_FS_LIVE_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);
# DbFileSystem - versioning
create table JR_FSV_FSENTRY (FSENTRY_PATH varchar(2048) not null, FSENTRY_NAME varchar(255) not null, FSENTRY_DATA image null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create unique index JR_FSV_FSENTRY_IDX on JR_FSV_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);

# PersistenceManager - default workspace
create table JR_DEFAULT_BUNDLE (NODE_ID binary(16) not null, BUNDLE_DATA image not null);
create unique index JR_DEFAULT_BUNDLE_IDX on JR_DEFAULT_BUNDLE (NODE_ID);
create table JR_DEFAULT_REFS (NODE_ID binary(16) not null, REFS_DATA image not null);
create unique index JR_DEFAULT_REFS_IDX on JR_DEFAULT_REFS (NODE_ID);
create table JR_DEFAULT_BINVAL (BINVAL_ID varchar(64) not null, BINVAL_DATA image not null);
create unique index JR_DEFAULT_BINVAL_IDX on JR_DEFAULT_BINVAL (BINVAL_ID);
create table JR_DEFAULT_NAMES (ID INTEGER IDENTITY(1,1) PRIMARY KEY, NAME varchar(255) COLLATE Latin1_General_CS_AS not null);
# PersistenceManager - live workspace
create table JR_LIVE_BUNDLE (NODE_ID binary(16) not null, BUNDLE_DATA image not null);
create unique index JR_LIVE_BUNDLE_IDX on JR_LIVE_BUNDLE (NODE_ID);
create table JR_LIVE_REFS (NODE_ID binary(16) not null, REFS_DATA image not null);
create unique index JR_LIVE_REFS_IDX on JR_LIVE_REFS (NODE_ID);
create table JR_LIVE_BINVAL (BINVAL_ID varchar(64) not null, BINVAL_DATA image not null);
create unique index JR_LIVE_BINVAL_IDX on JR_LIVE_BINVAL (BINVAL_ID);
create table JR_LIVE_NAMES (ID INTEGER IDENTITY(1,1) PRIMARY KEY, NAME varchar(255) COLLATE Latin1_General_CS_AS not null);
# PersistenceManager - versioning
create table JR_V_BUNDLE (NODE_ID binary(16) not null, BUNDLE_DATA image not null);
create unique index JR_V_BUNDLE_IDX on JR_V_BUNDLE (NODE_ID);
create table JR_V_REFS (NODE_ID binary(16) not null, REFS_DATA image not null);
create unique index JR_V_REFS_IDX on JR_V_REFS (NODE_ID);
create table JR_V_BINVAL (BINVAL_ID varchar(64) not null, BINVAL_DATA image not null);
create unique index JR_V_BINVAL_IDX on JR_V_BINVAL (BINVAL_ID);
create table JR_V_NAMES (ID INTEGER IDENTITY(1,1) PRIMARY KEY, NAME varchar(255) COLLATE Latin1_General_CS_AS not null);

# Journal
create table JR_J_JOURNAL (REVISION_ID BIGINT NOT NULL, JOURNAL_ID varchar(255), PRODUCER_ID varchar(255), REVISION_DATA IMAGE);
create unique index JR_J_JOURNAL_IDX on JR_J_JOURNAL (REVISION_ID);
create table JR_J_GLOBAL_REVISION (REVISION_ID BIGINT NOT NULL);
create unique index JR_J_GLOBAL_REVISION_IDX on JR_J_GLOBAL_REVISION (REVISION_ID);
create table JR_J_LOCAL_REVISIONS (JOURNAL_ID varchar(255) NOT NULL, REVISION_ID BIGINT NOT NULL);
# Inserting the one and only revision counter record now helps avoiding race conditions
insert into JR_J_GLOBAL_REVISION VALUES(0);