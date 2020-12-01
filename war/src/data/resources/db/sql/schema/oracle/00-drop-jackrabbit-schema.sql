-- DbFileSystem
drop table JR_FSG_FSENTRY cascade constraints;

-- PersistenceManager
drop trigger JR_DEFAULT_t1;
drop trigger JR_LIVE_t1;
drop trigger JR_V_t1;
drop sequence JR_DEFAULT_seq_names_id;
drop sequence JR_LIVE_seq_names_id;
drop sequence JR_V_seq_names_id;
drop table JR_DEFAULT_BUNDLE cascade constraints;
drop table JR_DEFAULT_REFS cascade constraints;
drop table JR_DEFAULT_BINVAL cascade constraints;
drop table JR_DEFAULT_NAMES cascade constraints;
drop table JR_LIVE_BUNDLE cascade constraints;
drop table JR_LIVE_REFS cascade constraints;
drop table JR_LIVE_BINVAL cascade constraints;
drop table JR_LIVE_NAMES cascade constraints;
drop table JR_V_BUNDLE cascade constraints;
drop table JR_V_REFS cascade constraints;
drop table JR_V_BINVAL cascade constraints;
drop table JR_V_NAMES cascade constraints;

-- Journal
drop table JR_J_JOURNAL cascade constraints;
drop table JR_J_GLOBAL_REVISION cascade constraints;
drop table JR_J_LOCAL_REVISIONS cascade constraints;
drop table JR_J_LOCKS cascade constraints;

-- DbDataStore
drop table JR_DATASTORE cascade constraints;

