# DbFileSystem
drop table jr_fsg_FSENTRY cascade constraints;
drop table jr_fs_default_FSENTRY cascade constraints;
drop table jr_fs_live_FSENTRY cascade constraints;
drop table jr_fsv_FSENTRY cascade constraints;

# PersistenceManager
trigger jr_default_t1;
trigger jr_live_t1;
trigger jr_v_t1;
drop sequence jr_default_seq_names_id;
drop sequence jr_live_seq_names_id;
drop sequence jr_v_seq_names_id;
drop table jr_default_BUNDLE cascade constraints;
drop table jr_default_REFS cascade constraints;
drop table jr_default_BINVAL cascade constraints;
drop table jr_default_NAMES cascade constraints;
drop table jr_live_BUNDLE cascade constraints;
drop table jr_live_REFS cascade constraints;
drop table jr_live_BINVAL cascade constraints;
drop table jr_live_NAMES cascade constraints;
drop table jr_v_BUNDLE cascade constraints;
drop table jr_v_REFS cascade constraints;
drop table jr_v_BINVAL cascade constraints;
drop table jr_v_NAMES cascade constraints;

# Journal
drop table jr_j_JOURNAL cascade constraints;
drop table jr_j_GLOBAL_REVISION cascade constraints;
drop table jr_j_LOCAL_REVISIONS cascade constraints;

# DbFileSystem - global
create table jr_fsg_FSENTRY (FSENTRY_PATH varchar2(2048) not null, FSENTRY_NAME varchar2(255) not null, FSENTRY_DATA blob null, FSENTRY_LASTMOD number(38,0) not null, FSENTRY_LENGTH number(38,0) null);
create unique index jr_fsg_FSENTRY_IDX on jr_fsg_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);
# DbFileSystem - default workspace
create table jr_fs_default_FSENTRY (FSENTRY_PATH varchar2(2048) not null, FSENTRY_NAME varchar2(255) not null, FSENTRY_DATA blob null, FSENTRY_LASTMOD number(38,0) not null, FSENTRY_LENGTH number(38,0) null);
create unique index jr_fs_default_FSENTRY_IDX on jr_fs_default_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);
# DbFileSystem - live workspace
create table jr_fs_live_FSENTRY (FSENTRY_PATH varchar2(2048) not null, FSENTRY_NAME varchar2(255) not null, FSENTRY_DATA blob null, FSENTRY_LASTMOD number(38,0) not null, FSENTRY_LENGTH number(38,0) null);
create unique index jr_fs_live_FSENTRY_IDX on jr_fs_live_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);
# DbFileSystem - versioning
create table jr_fsv_FSENTRY (FSENTRY_PATH varchar2(2048) not null, FSENTRY_NAME varchar2(255) not null, FSENTRY_DATA blob null, FSENTRY_LASTMOD number(38,0) not null, FSENTRY_LENGTH number(38,0) null);
create unique index jr_fsv_FSENTRY_IDX on jr_fsv_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);

# PersistenceManager - default workspace
create table jr_default_BUNDLE (NODE_ID raw(16) not null, BUNDLE_DATA blob not null);
create unique index jr_default_BUNDLE_IDX on jr_default_BUNDLE (NODE_ID);
create table jr_default_REFS (NODE_ID raw(16) not null, REFS_DATA blob not null);
create unique index jr_default_REFS_IDX on jr_default_REFS (NODE_ID);
create table jr_default_BINVAL (BINVAL_ID varchar2(64) not null, BINVAL_DATA blob null);
create unique index jr_default_BINVAL_IDX on jr_default_BINVAL (BINVAL_ID);
create table jr_default_NAMES (ID INTEGER primary key, NAME varchar2(255) not null);
create unique index jr_default_NAMES_IDX on jr_default_NAMES (NAME);
create sequence jr_default_seq_names_id;
create trigger jr_default_t1 before insert on jr_default_NAMES for each row begin select jr_default_seq_names_id.nextval into :new.id from dual; end;
# PersistenceManager - live workspace
create table jr_live_BUNDLE (NODE_ID raw(16) not null, BUNDLE_DATA blob not null);
create unique index jr_live_BUNDLE_IDX on jr_live_BUNDLE (NODE_ID);
create table jr_live_REFS (NODE_ID raw(16) not null, REFS_DATA blob not null);
create unique index jr_live_REFS_IDX on jr_live_REFS (NODE_ID);
create table jr_live_BINVAL (BINVAL_ID varchar2(64) not null, BINVAL_DATA blob null);
create unique index jr_live_BINVAL_IDX on jr_live_BINVAL (BINVAL_ID);
create table jr_live_NAMES (ID INTEGER primary key, NAME varchar2(255) not null);
create unique index jr_live_NAMES_IDX on jr_live_NAMES (NAME);
create sequence jr_live_seq_names_id;
create trigger jr_live_t1 before insert on jr_live_NAMES for each row begin select jr_live_seq_names_id.nextval into :new.id from dual; end;
# PersistenceManager - versioning
create table jr_v_BUNDLE (NODE_ID raw(16) not null, BUNDLE_DATA blob not null);
create unique index jr_v_BUNDLE_IDX on jr_v_BUNDLE (NODE_ID);
create table jr_v_REFS (NODE_ID raw(16) not null, REFS_DATA blob not null);
create unique index jr_v_REFS_IDX on jr_v_REFS (NODE_ID);
create table jr_v_BINVAL (BINVAL_ID varchar2(64) not null, BINVAL_DATA blob null);
create unique index jr_v_BINVAL_IDX on jr_v_BINVAL (BINVAL_ID);
create table jr_v_NAMES (ID INTEGER primary key, NAME varchar2(255) not null);
create unique index jr_v_NAMES_IDX on jr_v_NAMES (NAME);
create sequence jr_v_seq_names_id;
create trigger jr_v_t1 before insert on jr_v_NAMES for each row begin select jr_v_seq_names_id.nextval into :new.id from dual; end;

# Journal
create table jr_j_JOURNAL (REVISION_ID number(20,0) NOT NULL, JOURNAL_ID varchar(255), PRODUCER_ID varchar(255), REVISION_DATA blob);
create unique index jr_j_JOURNAL_IDX on jr_j_JOURNAL (REVISION_ID);
create table jr_j_GLOBAL_REVISION (REVISION_ID number(20,0) NOT NULL);
create unique index jr_j_GLOBAL_REVISION_IDX on jr_j_GLOBAL_REVISION (REVISION_ID);
create table jr_j_LOCAL_REVISIONS (JOURNAL_ID varchar(255) NOT NULL, REVISION_ID number(20,0) NOT NULL);
insert into jr_j_GLOBAL_REVISION VALUES(0);
