drop table jr_default_BUNDLE;
drop table jr_default_REFS;
drop table jr_default_BINVAL;
drop table jr_default_NAMES;
drop table jr_v_BUNDLE;
drop table jr_v_REFS;
drop table jr_v_BINVAL;
drop table jr_v_NAMES;
drop table jr_fsdefault_FSENTRY;
drop table jr_fsg_FSENTRY;
drop table jr_fsv_FSENTRY;
drop table JOURNAL;
drop table GLOBAL_REVISION;
drop table LOCAL_REVISIONS;

create table jr_default_BUNDLE (NODE_ID_HI bigint not null, NODE_ID_LO bigint not null, BUNDLE_DATA bytea not null, PRIMARY KEY (NODE_ID_HI, NODE_ID_LO));
create table jr_default_REFS (NODE_ID_HI bigint not null, NODE_ID_LO bigint not null, REFS_DATA bytea not null, PRIMARY KEY (NODE_ID_HI, NODE_ID_LO));
create table jr_default_BINVAL (BINVAL_ID varchar(64) not null, BINVAL_DATA bytea not null);
create table jr_default_NAMES (ID SERIAL PRIMARY KEY, NAME varchar(255) not null);
create table jr_v_BUNDLE (NODE_ID_HI bigint not null, NODE_ID_LO bigint not null, BUNDLE_DATA bytea not null, PRIMARY KEY (NODE_ID_HI, NODE_ID_LO));
create table jr_v_REFS (NODE_ID_HI bigint not null, NODE_ID_LO bigint not null, REFS_DATA bytea not null, PRIMARY KEY (NODE_ID_HI, NODE_ID_LO));
create table jr_v_BINVAL (BINVAL_ID varchar(64) not null, BINVAL_DATA bytea not null);
create table jr_v_NAMES (ID SERIAL PRIMARY KEY, NAME varchar(255) not null);
create table jr_fsdefault_FSENTRY (FSENTRY_PATH varchar not null, FSENTRY_NAME varchar not null, FSENTRY_DATA bytea null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create table jr_fsg_FSENTRY (FSENTRY_PATH varchar not null, FSENTRY_NAME varchar not null, FSENTRY_DATA bytea null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create table jr_fsv_FSENTRY (FSENTRY_PATH varchar not null, FSENTRY_NAME varchar not null, FSENTRY_DATA bytea null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null);
create table JOURNAL (REVISION_ID BIGINT NOT NULL, JOURNAL_ID varchar(255), PRODUCER_ID varchar(255), REVISION_DATA bytea);
create table GLOBAL_REVISION (REVISION_ID BIGINT NOT NULL);
create table LOCAL_REVISIONS (JOURNAL_ID varchar(255) NOT NULL, REVISION_ID BIGINT NOT NULL);

create unique index jr_default_BINVAL_IDX on jr_default_BINVAL (BINVAL_ID);
create unique index jr_v_BINVAL_IDX on jr_v_BINVAL (BINVAL_ID);
create unique index jr_fsdefault_FSENTRY_IDX on jr_fsdefault_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);
create unique index jr_fsg_FSENTRY_IDX on jr_fsg_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);
create unique index jr_fsv_FSENTRY_IDX on jr_fsv_FSENTRY (FSENTRY_PATH, FSENTRY_NAME);
create unique index JOURNAL_IDX on JOURNAL (REVISION_ID);
create unique index GLOBAL_REVISION_IDX on GLOBAL_REVISION (REVISION_ID);

insert into GLOBAL_REVISION VALUES(0);

