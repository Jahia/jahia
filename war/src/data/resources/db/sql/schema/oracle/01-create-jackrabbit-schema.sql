-- DbFileSystem - global
create table JR_FSG_FSENTRY (FSENTRY_PATH varchar2(2048) not null, FSENTRY_NAME varchar2(255) not null, FSENTRY_DATA blob null, FSENTRY_LASTMOD number(38,0) not null, FSENTRY_LENGTH number(38,0) null);
create unique index JR_FSG_FSENTRY_IDX on JR_FSG_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);

-- PersistenceManager - default workspace
create table JR_DEFAULT_BUNDLE (NODE_ID raw(16) not null, BUNDLE_DATA blob not null);
create unique index JR_DEFAULT_BUNDLE_IDX on JR_DEFAULT_BUNDLE (NODE_ID);
create table JR_DEFAULT_REFS (NODE_ID raw(16) not null, REFS_DATA blob not null);
create unique index JR_DEFAULT_REFS_IDX on JR_DEFAULT_REFS (NODE_ID);
create table JR_DEFAULT_BINVAL (BINVAL_ID varchar2(64) not null, BINVAL_DATA blob null);
create unique index JR_DEFAULT_BINVAL_IDX on JR_DEFAULT_BINVAL (BINVAL_ID);
create table JR_DEFAULT_NAMES (ID INTEGER primary key, NAME varchar2(255) not null);
create unique index JR_DEFAULT_NAMES_IDX on JR_DEFAULT_NAMES (NAME);
create sequence JR_DEFAULT_seq_names_id;
create trigger JR_DEFAULT_t1 before insert on JR_DEFAULT_NAMES for each row begin select JR_DEFAULT_seq_names_id.nextval into :new.id from dual; end;
/

-- PersistenceManager - live workspace
create table JR_LIVE_BUNDLE (NODE_ID raw(16) not null, BUNDLE_DATA blob not null);
create unique index JR_LIVE_BUNDLE_IDX on JR_LIVE_BUNDLE (NODE_ID);
create table JR_LIVE_REFS (NODE_ID raw(16) not null, REFS_DATA blob not null);
create unique index JR_LIVE_REFS_IDX on JR_LIVE_REFS (NODE_ID);
create table JR_LIVE_BINVAL (BINVAL_ID varchar2(64) not null, BINVAL_DATA blob null);
create unique index JR_LIVE_BINVAL_IDX on JR_LIVE_BINVAL (BINVAL_ID);
create table JR_LIVE_NAMES (ID INTEGER primary key, NAME varchar2(255) not null);
create unique index JR_LIVE_NAMES_IDX on JR_LIVE_NAMES (NAME);
create sequence JR_LIVE_seq_names_id;
create trigger JR_LIVE_t1 before insert on JR_LIVE_NAMES for each row begin select JR_LIVE_seq_names_id.nextval into :new.id from dual; end;
/

-- PersistenceManager - versioning
create table JR_V_BUNDLE (NODE_ID raw(16) not null, BUNDLE_DATA blob not null);
create unique index JR_V_BUNDLE_IDX on JR_V_BUNDLE (NODE_ID);
create table JR_V_REFS (NODE_ID raw(16) not null, REFS_DATA blob not null);
create unique index JR_V_REFS_IDX on JR_V_REFS (NODE_ID);
create table JR_V_BINVAL (BINVAL_ID varchar2(64) not null, BINVAL_DATA blob null);
create unique index JR_V_BINVAL_IDX on JR_V_BINVAL (BINVAL_ID);
create table JR_V_NAMES (ID INTEGER primary key, NAME varchar2(255) not null);
create unique index JR_V_NAMES_IDX on JR_V_NAMES (NAME);
create sequence JR_V_seq_names_id;
create trigger JR_V_t1 before insert on JR_V_NAMES for each row begin select JR_V_seq_names_id.nextval into :new.id from dual; end;
/

-- Journal
create table JR_J_JOURNAL (REVISION_ID number(20,0) NOT NULL, JOURNAL_ID varchar(255), PRODUCER_ID varchar(255), REVISION_DATA blob);
create unique index JR_J_JOURNAL_IDX on JR_J_JOURNAL (REVISION_ID);
create table JR_J_GLOBAL_REVISION (REVISION_ID number(20,0) NOT NULL);
create unique index JR_J_GLOBAL_REVISION_IDX on JR_J_GLOBAL_REVISION (REVISION_ID);
create table JR_J_LOCAL_REVISIONS (JOURNAL_ID varchar(255) NOT NULL PRIMARY KEY, REVISION_ID number(20,0) NOT NULL);
create table JR_J_LOCKS (NODE_ID CHAR(40) PRIMARY KEY, JOURNAL_ID VARCHAR(255) NOT NULL);
insert into JR_J_GLOBAL_REVISION VALUES(0);

-- DbDataStore
create table JR_DATASTORE (ID VARCHAR(255) PRIMARY KEY, LENGTH NUMBER, LAST_MODIFIED NUMBER, DATA BLOB);

