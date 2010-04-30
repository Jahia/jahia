# DbFileSystem
drop table if exists jr_fsg_FSENTRY;
drop table if exists jr_fs_default_FSENTRY;
drop table if exists jr_fs_live_FSENTRY;
drop table if exists jr_fsv_FSENTRY;

# PersistenceManager
drop table if exists jr_default_BUNDLE;
drop table if exists jr_default_REFS;
drop table if exists jr_default_BINVAL;
drop table if exists jr_default_NAMES;
drop table if exists jr_live_BUNDLE;
drop table if exists jr_live_REFS;
drop table if exists jr_live_BINVAL;
drop table if exists jr_live_NAMES;
drop table if exists jr_v_BUNDLE;
drop table if exists jr_v_REFS;
drop table if exists jr_v_BINVAL;
drop table if exists jr_v_NAMES;

# Journal
drop table if exists jr_j_JOURNAL;
drop table if exists jr_j_GLOBAL_REVISION;
drop table if exists jr_j_LOCAL_REVISIONS;

# DbFileSystem - global
create table jr_fsg_FSENTRY (FSENTRY_PATH text not null, FSENTRY_NAME varchar(255) not null, FSENTRY_DATA longblob null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null) character set latin1;
create unique index jr_fsg_FSENTRY_IDX on jr_fsg_FSENTRY (FSENTRY_PATH(245), FSENTRY_NAME);
# DbFileSystem - default workspace
create table jr_fs_default_FSENTRY (FSENTRY_PATH text not null, FSENTRY_NAME varchar(255) not null, FSENTRY_DATA longblob null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null) character set latin1;
create unique index jr_fs_default_FSENTRY_IDX on jr_fs_default_FSENTRY (FSENTRY_PATH(245), FSENTRY_NAME);
# DbFileSystem - live workspace
create table jr_fs_live_FSENTRY (FSENTRY_PATH text not null, FSENTRY_NAME varchar(255) not null, FSENTRY_DATA longblob null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null) character set latin1;
create unique index jr_fs_live_FSENTRY_IDX on jr_fs_live_FSENTRY (FSENTRY_PATH(245), FSENTRY_NAME);
# DbFileSystem - versioning
create table jr_fsv_FSENTRY (FSENTRY_PATH text not null, FSENTRY_NAME varchar(255) not null, FSENTRY_DATA longblob null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null) character set latin1;
create unique index jr_fsv_FSENTRY_IDX on jr_fsv_FSENTRY (FSENTRY_PATH(245), FSENTRY_NAME);

# PersistenceManager - default workspace
create table jr_default_BUNDLE (NODE_ID varbinary(16) not null, BUNDLE_DATA longblob not null);
create unique index jr_default_BUNDLE_IDX on jr_default_BUNDLE (NODE_ID);
create table jr_default_REFS (NODE_ID varbinary(16) not null, REFS_DATA longblob not null);
create unique index jr_default_REFS_IDX on jr_default_REFS (NODE_ID);
create table jr_default_BINVAL (BINVAL_ID varchar(64) not null, BINVAL_DATA longblob not null);
create unique index jr_default_BINVAL_IDX on jr_default_BINVAL (BINVAL_ID);
create table jr_default_NAMES (ID INTEGER AUTO_INCREMENT PRIMARY KEY, NAME varchar(255) character set utf8 collate utf8_bin not null);
# PersistenceManager - live workspace
create table jr_live_BUNDLE (NODE_ID varbinary(16) not null, BUNDLE_DATA longblob not null);
create unique index jr_live_BUNDLE_IDX on jr_live_BUNDLE (NODE_ID);
create table jr_live_REFS (NODE_ID varbinary(16) not null, REFS_DATA longblob not null);
create unique index jr_live_REFS_IDX on jr_live_REFS (NODE_ID);
create table jr_live_BINVAL (BINVAL_ID varchar(64) not null, BINVAL_DATA longblob not null);
create unique index jr_live_BINVAL_IDX on jr_live_BINVAL (BINVAL_ID);
create table jr_live_NAMES (ID INTEGER AUTO_INCREMENT PRIMARY KEY, NAME varchar(255) character set utf8 collate utf8_bin not null);
# PersistenceManager - versioning
create table jr_v_BUNDLE (NODE_ID varbinary(16) not null, BUNDLE_DATA longblob not null);
create unique index jr_v_BUNDLE_IDX on jr_v_BUNDLE (NODE_ID);
create table jr_v_REFS (NODE_ID varbinary(16) not null, REFS_DATA longblob not null);
create unique index jr_v_REFS_IDX on jr_v_REFS (NODE_ID);
create table jr_v_BINVAL (BINVAL_ID varchar(64) not null, BINVAL_DATA longblob not null);
create unique index jr_v_BINVAL_IDX on jr_v_BINVAL (BINVAL_ID);
create table jr_v_NAMES (ID INTEGER AUTO_INCREMENT PRIMARY KEY, NAME varchar(255) character set utf8 collate utf8_bin not null);

# Journal
create table jr_j_JOURNAL (REVISION_ID BIGINT NOT NULL, JOURNAL_ID varchar(255), PRODUCER_ID varchar(255), REVISION_DATA longblob);
create unique index jr_j_JOURNAL_IDX on jr_j_JOURNAL (REVISION_ID);
create table jr_j_GLOBAL_REVISION (REVISION_ID BIGINT NOT NULL);
create unique index jr_j_GLOBAL_REVISION_IDX on jr_j_GLOBAL_REVISION (REVISION_ID);
create table jr_j_LOCAL_REVISIONS (JOURNAL_ID varchar(255) NOT NULL, REVISION_ID BIGINT NOT NULL);
# Inserting the one and only revision counter record now helps avoiding race conditions
insert into jr_j_GLOBAL_REVISION VALUES(0);
