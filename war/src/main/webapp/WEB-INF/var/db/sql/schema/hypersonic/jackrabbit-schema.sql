# DbFileSystem
drop table JR_FSG_FSENTRY;
drop table JR_FS_DEFAULT_FSENTRY;
drop table JR_FS_LIVE_FSENTRY;
drop table JR_FSV_FSENTRY;

# PersistenceManager
drop table JR_DEFAULT_NODE;
drop table JR_DEFAULT_PROP;
drop table JR_DEFAULT_REFS;
drop table JR_DEFAULT_BINVAL;
drop table JR_LIVE_NODE;
drop table JR_LIVE_PROP;
drop table JR_LIVE_REFS;
drop table JR_LIVE_BINVAL;
drop table JR_V_NODE;
drop table JR_V_PROP;
drop table JR_V_REFS;
drop table JR_V_BINVAL;

# Journal
drop table JR_J_JOURNAL;
drop table JR_J_GLOBAL_REVISION;
drop table JR_J_LOCAL_REVISIONS;

# DbFileSystem - global
create table JR_FSG_FSENTRY (FSENTRY_PATH varchar not null, FSENTRY_NAME varchar not null, FSENTRY_DATA varbinary null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create unique index JR_FSG_FSENTRY_IDX on JR_FSG_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);
# DbFileSystem - default workspace
create table JR_FS_DEFAULT_FSENTRY (FSENTRY_PATH varchar not null, FSENTRY_NAME varchar not null, FSENTRY_DATA varbinary null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create unique index JR_FS_DEFAULT_FSENTRY_IDX on JR_FS_DEFAULT_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);
# DbFileSystem - live workspace
create table JR_FS_LIVE_FSENTRY (FSENTRY_PATH varchar not null, FSENTRY_NAME varchar not null, FSENTRY_DATA varbinary null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create unique index JR_FS_LIVE_FSENTRY_IDX on JR_FS_LIVE_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);
# DbFileSystem - versioning
create table JR_FSV_FSENTRY (FSENTRY_PATH varchar not null, FSENTRY_NAME varchar not null, FSENTRY_DATA varbinary null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create unique index JR_FSV_FSENTRY_IDX on JR_FSV_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);

# PersistenceManager - default workspace
create table JR_DEFAULT_NODE (NODE_ID char(36) not null, NODE_DATA varbinary not null);
create unique index JR_DEFAULT_NODE_IDX on JR_DEFAULT_NODE (NODE_ID);
create table JR_DEFAULT_PROP (PROP_ID varchar not null, PROP_DATA varbinary not null);
create unique index JR_DEFAULT_PROP_IDX on JR_DEFAULT_PROP (PROP_ID);
create table JR_DEFAULT_REFS (NODE_ID char(36) not null, REFS_DATA varbinary not null);
create unique index JR_DEFAULT_REFS_IDX on JR_DEFAULT_REFS (NODE_ID);
create table JR_DEFAULT_BINVAL (BINVAL_ID varchar not null, BINVAL_DATA varbinary not null);
create unique index JR_DEFAULT_BINVAL_IDX on JR_DEFAULT_BINVAL (BINVAL_ID);
# PersistenceManager - live workspace
create table JR_LIVE_NODE (NODE_ID char(36) not null, NODE_DATA varbinary not null);
create unique index JR_LIVE_NODE_IDX on JR_LIVE_NODE (NODE_ID);
create table JR_LIVE_PROP (PROP_ID varchar not null, PROP_DATA varbinary not null);
create unique index JR_LIVE_PROP_IDX on JR_LIVE_PROP (PROP_ID);
create table JR_LIVE_REFS (NODE_ID char(36) not null, REFS_DATA varbinary not null);
create unique index JR_LIVE_REFS_IDX on JR_LIVE_REFS (NODE_ID);
create table JR_LIVE_BINVAL (BINVAL_ID varchar not null, BINVAL_DATA varbinary not null);
create unique index JR_LIVE_BINVAL_IDX on JR_LIVE_BINVAL (BINVAL_ID);
# PersistenceManager - versioning
create table JR_V_NODE (NODE_ID char(36) not null, NODE_DATA varbinary not null);
create unique index JR_V_NODE_IDX on JR_V_NODE (NODE_ID);
create table JR_V_PROP (PROP_ID varchar not null, PROP_DATA varbinary not null);
create unique index JR_V_PROP_IDX on JR_V_PROP (PROP_ID);
create table JR_V_REFS (NODE_ID char(36) not null, REFS_DATA varbinary not null);
create unique index JR_V_REFS_IDX on JR_V_REFS (NODE_ID);
create table JR_V_BINVAL (BINVAL_ID varchar not null, BINVAL_DATA varbinary not null);
create unique index JR_V_BINVAL_IDX on JR_V_BINVAL (BINVAL_ID);

# Journal
create table JR_V_JOURNAL (REVISION_ID BIGINT NOT NULL, JOURNAL_ID varchar(255), PRODUCER_ID varchar(255), REVISION_DATA varbinary);
create unique index JR_V_JOURNAL_IDX on JR_V_JOURNAL (REVISION_ID);
create table JR_V_GLOBAL_REVISION (REVISION_ID BIGINT NOT NULL);
create unique index JR_V_GLOBAL_REVISION_IDX on JR_V_GLOBAL_REVISION (REVISION_ID);
create table JR_V_LOCAL_REVISIONS (JOURNAL_ID varchar(255) NOT NULL, REVISION_ID BIGINT NOT NULL);
# Inserting the one and only revision counter record now helps avoiding race conditions
insert into JR_V_GLOBAL_REVISION VALUES(0);
