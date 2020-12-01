# DbFileSystem
drop table COPY_JR_FSG_FSENTRY cascade constraints;

# PersistenceManager
drop trigger COPY_JR_DEFAULT_t1;
drop trigger COPY_JR_LIVE_t1;
drop trigger COPY_JR_V_t1;
drop sequence COPY_JR_DEFAULT_seq_names_id;
drop sequence COPY_JR_LIVE_seq_names_id;
drop sequence COPY_JR_V_seq_names_id;
drop table COPY_JR_DEFAULT_BUNDLE cascade constraints;
drop table COPY_JR_DEFAULT_REFS cascade constraints;
drop table COPY_JR_DEFAULT_BINVAL cascade constraints;
drop table COPY_JR_DEFAULT_NAMES cascade constraints;
drop table COPY_JR_LIVE_BUNDLE cascade constraints;
drop table COPY_JR_LIVE_REFS cascade constraints;
drop table COPY_JR_LIVE_BINVAL cascade constraints;
drop table COPY_JR_LIVE_NAMES cascade constraints;
drop table COPY_JR_V_BUNDLE cascade constraints;
drop table COPY_JR_V_REFS cascade constraints;
drop table COPY_JR_V_BINVAL cascade constraints;
drop table COPY_JR_V_NAMES cascade constraints;

# DbDataStore
drop table COPY_JR_DATASTORE cascade constraints;

# DbFileSystem - global
create table COPY_JR_FSG_FSENTRY (FSENTRY_PATH varchar2(2048) not null, FSENTRY_NAME varchar2(255) not null, FSENTRY_DATA blob null, FSENTRY_LASTMOD number(38,0) not null, FSENTRY_LENGTH number(38,0) null);
create unique index COPY_JR_FSG_FSENTRY_IDX on COPY_JR_FSG_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);

# PersistenceManager - default workspace
create table COPY_JR_DEFAULT_BUNDLE (NODE_ID raw(16) not null, BUNDLE_DATA blob not null);
create unique index COPY_JR_DEFAULT_BUNDLE_IDX on COPY_JR_DEFAULT_BUNDLE (NODE_ID);
create table COPY_JR_DEFAULT_REFS (NODE_ID raw(16) not null, REFS_DATA blob not null);
create unique index COPY_JR_DEFAULT_REFS_IDX on COPY_JR_DEFAULT_REFS (NODE_ID);
create table COPY_JR_DEFAULT_BINVAL (BINVAL_ID varchar2(64) not null, BINVAL_DATA blob null);
create unique index COPY_JR_DEFAULT_BINVAL_IDX on COPY_JR_DEFAULT_BINVAL (BINVAL_ID);
create table COPY_JR_DEFAULT_NAMES (ID INTEGER primary key, NAME varchar2(255) not null);
create unique index COPY_JR_DEFAULT_NAMES_IDX on COPY_JR_DEFAULT_NAMES (NAME);
create sequence COPY_JR_DEFAULT_seq_names_id;
create trigger COPY_JR_DEFAULT_t1 before insert on COPY_JR_DEFAULT_NAMES for each row begin select COPY_JR_DEFAULT_seq_names_id.nextval into :new.id from dual; end;
# PersistenceManager - live workspace
create table COPY_JR_LIVE_BUNDLE (NODE_ID raw(16) not null, BUNDLE_DATA blob not null);
create unique index COPY_JR_LIVE_BUNDLE_IDX on COPY_JR_LIVE_BUNDLE (NODE_ID);
create table COPY_JR_LIVE_REFS (NODE_ID raw(16) not null, REFS_DATA blob not null);
create unique index COPY_JR_LIVE_REFS_IDX on COPY_JR_LIVE_REFS (NODE_ID);
create table COPY_JR_LIVE_BINVAL (BINVAL_ID varchar2(64) not null, BINVAL_DATA blob null);
create unique index COPY_JR_LIVE_BINVAL_IDX on COPY_JR_LIVE_BINVAL (BINVAL_ID);
create table COPY_JR_LIVE_NAMES (ID INTEGER primary key, NAME varchar2(255) not null);
create unique index COPY_JR_LIVE_NAMES_IDX on COPY_JR_LIVE_NAMES (NAME);
create sequence COPY_JR_LIVE_seq_names_id;
create trigger COPY_JR_LIVE_t1 before insert on COPY_JR_LIVE_NAMES for each row begin select COPY_JR_LIVE_seq_names_id.nextval into :new.id from dual; end;
# PersistenceManager - versioning
create table COPY_JR_V_BUNDLE (NODE_ID raw(16) not null, BUNDLE_DATA blob not null);
create unique index COPY_JR_V_BUNDLE_IDX on COPY_JR_V_BUNDLE (NODE_ID);
create table COPY_JR_V_REFS (NODE_ID raw(16) not null, REFS_DATA blob not null);
create unique index COPY_JR_V_REFS_IDX on COPY_JR_V_REFS (NODE_ID);
create table COPY_JR_V_BINVAL (BINVAL_ID varchar2(64) not null, BINVAL_DATA blob null);
create unique index COPY_JR_V_BINVAL_IDX on COPY_JR_V_BINVAL (BINVAL_ID);
create table COPY_JR_V_NAMES (ID INTEGER primary key, NAME varchar2(255) not null);
create unique index COPY_JR_V_NAMES_IDX on COPY_JR_V_NAMES (NAME);
create sequence COPY_JR_V_seq_names_id;
create trigger COPY_JR_V_t1 before insert on COPY_JR_V_NAMES for each row begin select COPY_JR_V_seq_names_id.nextval into :new.id from dual; end;

# DbDataStore
create table COPY_JR_DATASTORE (ID VARCHAR(255) PRIMARY KEY, LENGTH NUMBER, LAST_MODIFIED NUMBER, DATA BLOB);
