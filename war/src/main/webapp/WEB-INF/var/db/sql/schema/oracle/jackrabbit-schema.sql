drop table jr_default_BUNDLE cascade constraints;
drop table jr_default_REFS cascade constraints;
drop table jr_default_BINVAL cascade constraints;
drop table jr_default_NAMES cascade constraints;
drop table jr_v_BUNDLE cascade constraints;
drop table jr_v_REFS cascade constraints;
drop table jr_v_BINVAL cascade constraints;
drop table jr_v_NAMES cascade constraints;
drop table jr_fsdefault_FSENTRY cascade constraints;
drop table jr_fsg_FSENTRY cascade constraints;
drop table jr_fsv_FSENTRY cascade constraints;
drop table JOURNAL cascade constraints;
drop table GLOBAL_REVISION cascade constraints;
drop table LOCAL_REVISIONS cascade constraints;
drop sequence jr_default_seq_names_id;
drop sequence jr_v_seq_names_id;

create table jr_default_BUNDLE (NODE_ID raw(16) not null, BUNDLE_DATA blob not null);
create table jr_default_REFS (NODE_ID raw(16) not null, REFS_DATA blob not null);
create table jr_default_BINVAL (BINVAL_ID varchar2(64) not null, BINVAL_DATA blob null);
create table jr_default_NAMES (ID INTEGER primary key, NAME varchar2(255) not null);
create table jr_v_BUNDLE (NODE_ID raw(16) not null, BUNDLE_DATA blob not null);
create table jr_v_REFS (NODE_ID raw(16) not null, REFS_DATA blob not null);
create table jr_v_BINVAL (BINVAL_ID varchar2(64) not null, BINVAL_DATA blob null);
create table jr_v_NAMES (ID INTEGER primary key, NAME varchar2(255) not null);
create table jr_fsdefault_FSENTRY (FSENTRY_PATH varchar2(2048) not null, FSENTRY_NAME varchar2(255) not null, FSENTRY_DATA blob null, FSENTRY_LASTMOD number(38,0) not null, FSENTRY_LENGTH number(38,0) null);
create table jr_fsg_FSENTRY (FSENTRY_PATH varchar2(2048) not null, FSENTRY_NAME varchar2(255) not null, FSENTRY_DATA blob null, FSENTRY_LASTMOD number(38,0) not null, FSENTRY_LENGTH number(38,0) null);
create table jr_fsv_FSENTRY (FSENTRY_PATH varchar2(2048) not null, FSENTRY_NAME varchar2(255) not null, FSENTRY_DATA blob null, FSENTRY_LASTMOD number(38,0) not null, FSENTRY_LENGTH number(38,0) null);
create table JOURNAL (REVISION_ID number(20,0) NOT NULL, JOURNAL_ID varchar(255), PRODUCER_ID varchar(255), REVISION_DATA blob);
create table GLOBAL_REVISION (REVISION_ID number(20,0) NOT NULL);
create table LOCAL_REVISIONS (JOURNAL_ID varchar(255) NOT NULL, REVISION_ID number(20,0) NOT NULL);

create unique index jr_default_BUNDLE_IDX on jr_default_BUNDLE (NODE_ID);
create unique index jr_default_REFS_IDX on jr_default_REFS (NODE_ID);
create unique index jr_default_BINVAL_IDX on jr_default_BINVAL (BINVAL_ID);
create unique index jr_default_NAMES_IDX on jr_default_NAMES (NAME) ${tableSpace}
create sequence jr_default_seq_names_id
create trigger jr_default_t1 before insert on jr_default_NAMES for each row begin select jr_default_seq_names_id.nextval into :new.id from dual; end;
create unique index jr_v_BUNDLE_IDX on jr_v_BUNDLE (NODE_ID);
create unique index jr_v_REFS_IDX on jr_v_REFS (NODE_ID);
create unique index jr_v_BINVAL_IDX on jr_v_BINVAL (BINVAL_ID);
create unique index jr_v_NAMES_IDX on jr_v_NAMES (NAME) ${tableSpace}
create sequence jr_v_seq_names_id
create trigger jr_v_t1 before insert on jr_v_NAMES for each row begin select jr_v_seq_names_id.nextval into :new.id from dual; end;
create unique index jr_fsdefault_FSENTRY_IDX on jr_fsdefault_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);
create unique index jr_fsg_FSENTRY_IDX on jr_fsg_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);
create unique index jr_fsv_FSENTRY_IDX on jr_fsv_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);
create unique index JOURNAL_IDX on JOURNAL (REVISION_ID);
create unique index GLOBAL_REVISION_IDX on GLOBAL_REVISION (REVISION_ID);

insert into GLOBAL_REVISION VALUES(0);
