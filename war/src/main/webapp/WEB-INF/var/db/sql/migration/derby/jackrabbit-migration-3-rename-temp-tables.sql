# DbFileSystem - global
rename table COPY_JR_FSG_FSENTRY to JR_FSG_FSENTRY;
rename index COPY_JR_FSG_FSENTRY_IDX to JR_FSG_FSENTRY;

# PersistenceManager - default workspace
rename table COPY_JR_DEFAULT_BUNDLE to JR_DEFAULT_BUNDLE;
rename table COPY_JR_DEFAULT_REFS to JR_DEFAULT_REFS;
rename table COPY_JR_DEFAULT_BINVAL to JR_DEFAULT_BINVAL;
rename table COPY_JR_DEFAULT_NAMES to JR_DEFAULT_NAMES;
# PersistenceManager - live workspace
rename table COPY_JR_LIVE_BUNDLE to JR_LIVE_BUNDLE;
rename table COPY_JR_LIVE_REFS to JR_LIVE_REFS;
rename table COPY_JR_LIVE_BINVAL to JR_LIVE_BINVAL;
rename table COPY_JR_LIVE_NAMES to JR_LIVE_NAMES;
# PersistenceManager - versioning
rename table COPY_JR_V_BUNDLE to JR_V_BUNDLE;
rename table COPY_JR_V_REFS to JR_V_REFS;
rename table COPY_JR_V_BINVAL to JR_V_BINVAL;
rename table COPY_JR_V_NAMES to JR_V_NAMES;

# DbDataStore
rename table COPY_JR_DATASTORE to JR_DATASTORE;
