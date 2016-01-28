# DbFileSystem - global
alter table COPY_JR_FSG_FSENTRY rename to JR_FSG_FSENTRY;
drop index COPY_JR_FSG_FSENTRY_IDX on JR_FSG_FSENTRY;
create unique index JR_FSG_FSENTRY_IDX on JR_FSG_FSENTRY (FSENTRY_PATH(245), FSENTRY_NAME);

# PersistenceManager - default workspace
alter table COPY_JR_DEFAULT_BUNDLE rename to JR_DEFAULT_BUNDLE;
drop index COPY_JR_DEFAULT_BUNDLE_IDX on JR_DEFAULT_BUNDLE;
create unique index JR_DEFAULT_BUNDLE_IDX on JR_DEFAULT_BUNDLE (NODE_ID);
alter table COPY_JR_DEFAULT_REFS rename to JR_DEFAULT_REFS;
drop index COPY_JR_DEFAULT_REFS_IDX on JR_DEFAULT_REFS;
create unique index JR_DEFAULT_REFS_IDX on JR_DEFAULT_REFS (NODE_ID);
alter table COPY_JR_DEFAULT_BINVAL rename to JR_DEFAULT_BINVAL;
drop index COPY_JR_DEFAULT_BINVAL_IDX on JR_DEFAULT_BINVAL;
create unique index JR_DEFAULT_BINVAL_IDX on JR_DEFAULT_BINVAL (BINVAL_ID);
alter table COPY_JR_DEFAULT_NAMES rename to JR_DEFAULT_NAMES;

# PersistenceManager - live workspace
alter table COPY_JR_LIVE_BUNDLE rename to JR_LIVE_BUNDLE;
drop index COPY_JR_LIVE_BUNDLE_IDX on JR_LIVE_BUNDLE;
create unique index JR_LIVE_BUNDLE_IDX on JR_LIVE_BUNDLE (NODE_ID);
alter table COPY_JR_LIVE_REFS rename to JR_LIVE_REFS;
drop index COPY_JR_LIVE_REFS_IDX on JR_LIVE_REFS;
create unique index JR_LIVE_REFS_IDX on JR_LIVE_REFS (NODE_ID);
alter table COPY_JR_LIVE_BINVAL rename to JR_LIVE_BINVAL;
drop index COPY_JR_LIVE_BINVAL_IDX on JR_LIVE_BINVAL;
create unique index JR_LIVE_BINVAL_IDX on JR_LIVE_BINVAL (BINVAL_ID);
alter table COPY_JR_LIVE_NAMES rename to JR_LIVE_NAMES;

# PersistenceManager - versioning
alter table COPY_JR_V_BUNDLE rename to JR_V_BUNDLE;
drop index COPY_JR_V_BUNDLE_IDX on JR_V_BUNDLE;
create unique index JR_V_BUNDLE_IDX on JR_V_BUNDLE (NODE_ID);
alter table COPY_JR_V_REFS rename to JR_V_REFS;
drop index COPY_JR_V_REFS_IDX on JR_V_REFS;
create unique index JR_V_REFS_IDX on JR_V_REFS (NODE_ID);
alter table COPY_JR_V_BINVAL rename to JR_V_BINVAL;
drop index COPY_JR_V_BINVAL_IDX on JR_V_BINVAL;
create unique index JR_V_BINVAL_IDX on JR_V_BINVAL (BINVAL_ID);
alter table COPY_JR_V_NAMES rename to JR_V_NAMES;

# DbDataStore
alter table COPY_JR_DATASTORE rename to JR_DATASTORE;
