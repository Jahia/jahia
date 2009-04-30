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

create table jr_default_BUNDLE (NODE_ID varbinary(16) not null, BUNDLE_DATA longblob not null);
create table jr_default_REFS (NODE_ID varbinary(16) not null, REFS_DATA longblob not null);
create table jr_default_BINVAL (BINVAL_ID varchar(64) not null, BINVAL_DATA longblob not null);
create table jr_default_NAMES (ID INTEGER AUTO_INCREMENT PRIMARY KEY, NAME varchar(255) character set utf8 collate utf8_bin not null);
create table jr_v_BUNDLE (NODE_ID varbinary(16) not null, BUNDLE_DATA longblob not null);
create table jr_v_REFS (NODE_ID varbinary(16) not null, REFS_DATA longblob not null);
create table jr_v_BINVAL (BINVAL_ID varchar(64) not null, BINVAL_DATA longblob not null);
create table jr_v_NAMES (ID INTEGER AUTO_INCREMENT PRIMARY KEY, NAME varchar(255) character set utf8 collate utf8_bin not null);
create table jr_fsdefault_FSENTRY (FSENTRY_PATH text not null, FSENTRY_NAME varchar(255) not null, FSENTRY_DATA longblob null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null) character set latin1;
create table jr_fsg_FSENTRY (FSENTRY_PATH text not null, FSENTRY_NAME varchar(255) not null, FSENTRY_DATA longblob null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null) character set latin1;
create table jr_fsv_FSENTRY (FSENTRY_PATH text not null, FSENTRY_NAME varchar(255) not null, FSENTRY_DATA longblob null, FSENTRY_LASTMOD bigint not null, FSENTRY_LENGTH bigint not null) character set latin1;
create table JOURNAL (REVISION_ID BIGINT NOT NULL, JOURNAL_ID varchar(255), PRODUCER_ID varchar(255), REVISION_DATA longblob);
create table GLOBAL_REVISION (REVISION_ID BIGINT NOT NULL);
create table LOCAL_REVISIONS (JOURNAL_ID varchar(255) NOT NULL, REVISION_ID BIGINT NOT NULL);

create unique index jr_default_BUNDLE_IDX on jr_default_BUNDLE (NODE_ID);
create unique index jr_default_REFS_IDX on jr_default_REFS (NODE_ID);
create unique index jr_default_BINVAL_IDX on jr_default_BINVAL (BINVAL_ID);
create unique index jr_v_BUNDLE_IDX on jr_v_BUNDLE (NODE_ID);
create unique index jr_v_REFS_IDX on jr_v_REFS (NODE_ID);
create unique index jr_v_BINVAL_IDX on jr_v_BINVAL (BINVAL_ID);
create unique index jr_fsdefault_FSENTRY_IDX on jr_fsdefault_FSENTRY (FSENTRY_PATH(245), FSENTRY_NAME);
create unique index jr_fsg_FSENTRY_IDX on jr_fsg_FSENTRY (FSENTRY_PATH(245), FSENTRY_NAME);
create unique index jr_fsv_FSENTRY_IDX on jr_fsv_FSENTRY (FSENTRY_PATH(245), FSENTRY_NAME);
create unique index JOURNAL_IDX on JOURNAL (REVISION_ID);
create unique index GLOBAL_REVISION_IDX on GLOBAL_REVISION (REVISION_ID);

insert into GLOBAL_REVISION VALUES(0);

