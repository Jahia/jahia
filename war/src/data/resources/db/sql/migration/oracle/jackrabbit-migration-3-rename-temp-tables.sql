# DbFileSystem - global
alter table COPY_JR_FSG_FSENTRY rename to JR_FSG_FSENTRY;
alter index COPY_JR_FSG_FSENTRY_IDX rename to JR_FSG_FSENTRY_IDX;

# PersistenceManager - default workspace
alter table COPY_JR_DEFAULT_BUNDLE rename to JR_DEFAULT_BUNDLE;
alter index COPY_JR_DEFAULT_BUNDLE_IDX rename to JR_DEFAULT_BUNDLE_IDX;
alter table COPY_JR_DEFAULT_REFS rename to JR_DEFAULT_REFS;
alter index COPY_JR_DEFAULT_REFS_IDX rename to JR_DEFAULT_REFS_IDX;
alter table COPY_JR_DEFAULT_BINVAL rename to JR_DEFAULT_BINVAL;
alter index COPY_JR_DEFAULT_BINVAL_IDX rename to JR_DEFAULT_BINVAL_IDX;
alter table COPY_JR_DEFAULT_NAMES rename to JR_DEFAULT_NAMES;
drop trigger COPY_JR_DEFAULT_t1;
rename COPY_JR_DEFAULT_seq_names_id to JR_DEFAULT_seq_names_id;
create trigger JR_DEFAULT_t1 before insert on JR_DEFAULT_NAMES for each row begin select JR_DEFAULT_seq_names_id.nextval into :new.id from dual; end;
# PersistenceManager - live workspace
alter table COPY_JR_LIVE_BUNDLE rename to JR_LIVE_BUNDLE;
alter index COPY_JR_LIVE_BUNDLE_IDX rename to JR_LIVE_BUNDLE_IDX;
alter table COPY_JR_LIVE_REFS rename to JR_LIVE_REFS;
alter index COPY_JR_LIVE_REFS_IDX rename to JR_LIVE_REFS_IDX;
alter table COPY_JR_LIVE_BINVAL rename to JR_LIVE_BINVAL;
alter index COPY_JR_LIVE_BINVAL_IDX rename to JR_LIVE_BINVAL_IDX;
alter table COPY_JR_LIVE_NAMES rename to JR_LIVE_NAMES;
drop trigger COPY_JR_LIVE_t1;
rename COPY_JR_LIVE_seq_names_id to JR_LIVE_seq_names_id;
create trigger JR_LIVE_t1 before insert on JR_LIVE_NAMES for each row begin select JR_LIVE_seq_names_id.nextval into :new.id from dual; end;
# PersistenceManager - versioning
alter table COPY_JR_V_BUNDLE rename to JR_V_BUNDLE;
alter index COPY_JR_V_BUNDLE_IDX rename to JR_V_BUNDLE_IDX;
alter table COPY_JR_V_REFS rename to JR_V_REFS;
alter index COPY_JR_V_REFS_IDX rename to JR_V_REFS_IDX;
alter table COPY_JR_V_BINVAL rename to JR_V_BINVAL;
alter index COPY_JR_V_BINVAL_IDX rename to JR_V_BINVAL_IDX;
alter table COPY_JR_V_NAMES rename to JR_V_NAMES;
drop trigger COPY_JR_V_t1;
rename COPY_JR_V_seq_names_id to JR_V_seq_names_id;
create trigger JR_V_t1 before insert on JR_V_NAMES for each row begin select JR_V_seq_names_id.nextval into :new.id from dual; end;

# DbDataStore
alter table COPY_JR_DATASTORE rename to JR_DATASTORE;
