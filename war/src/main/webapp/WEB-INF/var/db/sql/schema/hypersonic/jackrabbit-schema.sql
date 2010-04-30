# DbFileSystem
drop table jr_fsg_FSENTRY;
drop table jr_fs_default_FSENTRY;
drop table jr_fs_live_FSENTRY;
drop table jr_fsv_FSENTRY;

# PersistenceManager
drop table jr_default_NODE;
drop table jr_default_PROP;
drop table jr_default_REFS;
drop table jr_default_BINVAL;
drop table jr_live_NODE;
drop table jr_live_PROP;
drop table jr_live_REFS;
drop table jr_live_BINVAL;
drop table jr_v_NODE;
drop table jr_v_PROP;
drop table jr_v_REFS;
drop table jr_v_BINVAL;

# Journal
drop table jr_j_JOURNAL;
drop table jr_j_GLOBAL_REVISION;
drop table jr_j_LOCAL_REVISIONS;

# DbFileSystem - global
create table jr_fsg_FSENTRY (FSENTRY_PATH varchar not null, FSENTRY_NAME varchar not null, FSENTRY_DATA varbinary null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create unique index jr_fsg_FSENTRY_IDX on jr_fsg_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);
# DbFileSystem - default workspace
create table jr_fs_default_FSENTRY (FSENTRY_PATH varchar not null, FSENTRY_NAME varchar not null, FSENTRY_DATA varbinary null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create unique index jr_fs_default_FSENTRY_IDX on jr_fs_default_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);
# DbFileSystem - live workspace
create table jr_fs_live_FSENTRY (FSENTRY_PATH varchar not null, FSENTRY_NAME varchar not null, FSENTRY_DATA varbinary null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create unique index jr_fs_live_FSENTRY_IDX on jr_fs_live_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);
# DbFileSystem - versioning
create table jr_fsv_FSENTRY (FSENTRY_PATH varchar not null, FSENTRY_NAME varchar not null, FSENTRY_DATA varbinary null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create unique index jr_fsv_FSENTRY_IDX on jr_fsv_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);

# PersistenceManager - default workspace
create table jr_default_NODE (NODE_ID char(36) not null, NODE_DATA varbinary not null);
create unique index jr_default_NODE_IDX on jr_default_NODE (NODE_ID);
create table jr_default_PROP (PROP_ID varchar not null, PROP_DATA varbinary not null);
create unique index jr_default_PROP_IDX on jr_default_PROP (PROP_ID);
create table jr_default_REFS (NODE_ID char(36) not null, REFS_DATA varbinary not null);
create unique index jr_default_REFS_IDX on jr_default_REFS (NODE_ID);
create table jr_default_BINVAL (BINVAL_ID varchar not null, BINVAL_DATA varbinary not null);
create unique index jr_default_BINVAL_IDX on jr_default_BINVAL (BINVAL_ID);
# PersistenceManager - live workspace
create table jr_live_NODE (NODE_ID char(36) not null, NODE_DATA varbinary not null);
create unique index jr_live_NODE_IDX on jr_live_NODE (NODE_ID);
create table jr_live_PROP (PROP_ID varchar not null, PROP_DATA varbinary not null);
create unique index jr_live_PROP_IDX on jr_live_PROP (PROP_ID);
create table jr_live_REFS (NODE_ID char(36) not null, REFS_DATA varbinary not null);
create unique index jr_live_REFS_IDX on jr_live_REFS (NODE_ID);
create table jr_live_BINVAL (BINVAL_ID varchar not null, BINVAL_DATA varbinary not null);
create unique index jr_live_BINVAL_IDX on jr_live_BINVAL (BINVAL_ID);
# PersistenceManager - versioning
create table jr_v_NODE (NODE_ID char(36) not null, NODE_DATA varbinary not null);
create unique index jr_v_NODE_IDX on jr_v_NODE (NODE_ID);
create table jr_v_PROP (PROP_ID varchar not null, PROP_DATA varbinary not null);
create unique index jr_v_PROP_IDX on jr_v_PROP (PROP_ID);
create table jr_v_REFS (NODE_ID char(36) not null, REFS_DATA varbinary not null);
create unique index jr_v_REFS_IDX on jr_v_REFS (NODE_ID);
create table jr_v_BINVAL (BINVAL_ID varchar not null, BINVAL_DATA varbinary not null);
create unique index jr_v_BINVAL_IDX on jr_v_BINVAL (BINVAL_ID);

# Journal
create table jr_v_JOURNAL (REVISION_ID BIGINT NOT NULL, JOURNAL_ID varchar(255), PRODUCER_ID varchar(255), REVISION_DATA varbinary);
create unique index jr_v_JOURNAL_IDX on jr_v_JOURNAL (REVISION_ID);
create table jr_v_GLOBAL_REVISION (REVISION_ID BIGINT NOT NULL);
create unique index jr_v_GLOBAL_REVISION_IDX on jr_v_GLOBAL_REVISION (REVISION_ID);
create table jr_v_LOCAL_REVISIONS (JOURNAL_ID varchar(255) NOT NULL, REVISION_ID BIGINT NOT NULL);
# Inserting the one and only revision counter record now helps avoiding race conditions
insert into jr_v_GLOBAL_REVISION VALUES(0);
