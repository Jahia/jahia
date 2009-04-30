drop table jr_default_NODE;
drop table jr_default_REFS;
drop table jr_default_BINVAL;
drop table jr_default_PROP;
drop table jr_v_NODE;
drop table jr_v_REFS;
drop table jr_v_BINVAL;
drop table jr_v_PROP;
drop table jr_fsdefault_FSENTRY;
drop table jr_fsg_FSENTRY;
drop table jr_fsv_FSENTRY;
drop table JOURNAL;
drop table GLOBAL_REVISION;
drop table LOCAL_REVISIONS;

create table jr_default_NODE (NODE_ID char(36) not null, NODE_DATA varbinary not null);
create table jr_default_PROP (PROP_ID varchar not null, PROP_DATA varbinary not null);
create table jr_default_REFS (NODE_ID char(36) not null, REFS_DATA varbinary not null);
create table jr_default_BINVAL (BINVAL_ID varchar not null, BINVAL_DATA varbinary not null);
create table jr_v_NODE (NODE_ID char(36) not null, NODE_DATA varbinary not null);
create table jr_v_PROP (PROP_ID varchar not null, PROP_DATA varbinary not null);
create table jr_v_REFS (NODE_ID char(36) not null, REFS_DATA varbinary not null);
create table jr_v_BINVAL (BINVAL_ID varchar not null, BINVAL_DATA varbinary not null);
create table jr_fsdefault_FSENTRY (FSENTRY_PATH varchar not null, FSENTRY_NAME varchar not null, FSENTRY_DATA varbinary null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create table jr_fsg_FSENTRY (FSENTRY_PATH varchar not null, FSENTRY_NAME varchar not null, FSENTRY_DATA varbinary null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create table jr_fsv_FSENTRY (FSENTRY_PATH varchar not null, FSENTRY_NAME varchar not null, FSENTRY_DATA varbinary null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create table JOURNAL (REVISION_ID BIGINT NOT NULL, JOURNAL_ID varchar(255), PRODUCER_ID varchar(255), REVISION_DATA varbinary);
create table GLOBAL_REVISION (REVISION_ID BIGINT NOT NULL);
create table LOCAL_REVISIONS (JOURNAL_ID varchar(255) NOT NULL, REVISION_ID BIGINT NOT NULL);

create unique index jr_default_NODE_IDX on jr_default_NODE (NODE_ID);
create unique index jr_default_PROP_IDX on jr_default_PROP (PROP_ID);
create unique index jr_default_REFS_IDX on jr_default_REFS (NODE_ID);
create unique index jr_default_BINVAL_IDX on jr_default_BINVAL (BINVAL_ID);
create unique index jr_v_NODE_IDX on jr_v_NODE (NODE_ID);
create unique index jr_v_PROP_IDX on jr_v_PROP (PROP_ID);
create unique index jr_v_REFS_IDX on jr_v_REFS (NODE_ID);
create unique index jr_v_BINVAL_IDX on jr_v_BINVAL (BINVAL_ID);

create unique index jr_fsdefault_FSENTRY_IDX on jr_fsdefault_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);
create unique index jr_fsg_FSENTRY_IDX on jr_fsg_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);
create unique index jr_fsv_FSENTRY_IDX on jr_fsv_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);
create unique index JOURNAL_IDX on JOURNAL (REVISION_ID);
create unique index GLOBAL_REVISION_IDX on GLOBAL_REVISION (REVISION_ID);

insert into GLOBAL_REVISION VALUES(0);
