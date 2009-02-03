DROP INDEX jahia_acl_index ON jahia_acl ;

DROP INDEX jahia_acl_index2 ON jahia_acl ;

DROP INDEX jahia_acl_entries_index ON acl_entries;

DROP INDEX jahia_ctn_def_index ON jahia_ctn_def ;

DROP INDEX jahia_ctn_def_properties_index ON jahia_ctn_def_properties ;

DROP INDEX jahia_ctn_entries_index ON jahia_ctn_entries ;

DROP INDEX jahia_ctn_entries_index2 ON jahia_ctn_entries ;

DROP INDEX jahia_ctn_entries_index5 ON jahia_ctn_entries ;

DROP INDEX jahia_ctn_lists_index2 ON jahia_ctn_lists ;

DROP INDEX jahia_ctn_lists_index3 ON jahia_ctn_lists ;

DROP INDEX jahia_ctn_lists_index4 ON jahia_ctn_lists ;

DROP INDEX jahia_ctn_lists_index5 ON jahia_ctn_lists ;

DROP INDEX jahia_ctn_lists_index6 ON jahia_ctn_lists ;

DROP INDEX jahia_ctnlists_prop_index ON jahia_ctnlists_prop ;

DROP INDEX jahia_fields_data_index ON jahia_fields_data ;

DROP INDEX jahia_fields_data_index2 ON jahia_fields_data ;

DROP INDEX jahia_fields_data_index3 ON jahia_fields_data ;

DROP INDEX jahia_fields_data_index4 ON jahia_fields_data ;

DROP INDEX jahia_fields_data_index5 ON jahia_fields_data ;

DROP INDEX jahia_fields_data_index6 ON jahia_fields_data ;

DROP INDEX jahia_fields_data_index7 ON jahia_fields_data ;

DROP INDEX jahia_fields_data_index8 ON jahia_fields_data ;

DROP INDEX jahia_fields_data_index9 ON jahia_fields_data ;

DROP INDEX jahia_fields_data_index10 ON jahia_fields_data ;

DROP INDEX jahia_fields_data_index11 ON jahia_fields_data ;

DROP INDEX jahia_fields_data_index12 ON jahia_fields_data ;

DROP INDEX jahia_fields_data_index13 ON jahia_fields_data ;

DROP INDEX jahia_fields_data_index14 ON jahia_fields_data ;

DROP INDEX jahia_fields_def_prop_index ON jahia_fields_def_prop ;

DROP INDEX jahia_fields_def_prop_index3 ON jahia_fields_def_prop ;

DROP INDEX jahia_indexingjobs_index ON jahia_indexingjobs;

DROP INDEX jahia_languages_states_index ON jahia_languages_states ;

DROP INDEX jahia_languagestates_index ON jahia_languages_states ;

DROP INDEX jahia_link_index ON jahia_link ;

DROP INDEX jahia_link_index1 ON jahia_link ;

DROP INDEX jahia_link_index2 ON jahia_link ;

DROP INDEX jahia_link_index3 ON jahia_link ;

DROP INDEX jahia_link_index4 ON jahia_link ;

DROP INDEX jahia_link_index5 ON jahia_link ;

DROP INDEX jahia_locks_index ON jahia_locks_non_excl ;

DROP INDEX jahia_obj_index ON jahia_obj;

DROP INDEX jahia_pages_data_index ON jahia_pages_data ;

DROP INDEX jahia_pages_data_index2 ON jahia_pages_data ;

DROP INDEX jahia_pages_def_index ON jahia_pages_def ;

DROP INDEX jahia_pages_def_prop_index ON jahia_pages_def_prop ;

DROP INDEX jahia_pages_def_prop_index2 ON jahia_pages_def_prop ;

DROP INDEX TRIGGER_NAME ON JAHIA_QRTZ_BLOB_TRIGGERS;
DROP INDEX TRIGGER_NAME ON JAHIA_QRTZ_CRON_TRIGGERS;
DROP INDEX JOB_NAME ON JAHIA_QRTZ_JOB_LISTENERS;
DROP INDEX TRIGGER_NAME ON JAHIA_QRTZ_SIMPLE_TRIGGERS;
DROP INDEX TRIGGER_NAME ON JAHIA_QRTZ_TRIGGER_LISTENERS;

DROP INDEX IDX_QRTZ_JD_JOB_GROUP ON JAHIA_QRTZ_JOB_DETAILS;
DROP INDEX IDX_QRTZ_JD_REQ_RECOVERY ON JAHIA_QRTZ_JOB_DETAILS;

DROP INDEX IDX_QRTZ_FT_TRIG_NAME ON JAHIA_QRTZ_FIRED_TRIGGERS;
DROP INDEX IDX_QRTZ_FT_TRIG_GROUP ON JAHIA_QRTZ_FIRED_TRIGGERS;
DROP INDEX IDX_QRTZ_FT_TRIG_N_G ON JAHIA_QRTZ_FIRED_TRIGGERS;
DROP INDEX IDX_QRTZ_FT_TRIG_VOLATILE ON JAHIA_QRTZ_FIRED_TRIGGERS;
DROP INDEX IDX_QRTZ_FT_TRIG_INST_NAME ON JAHIA_QRTZ_FIRED_TRIGGERS;
DROP INDEX IDX_QRTZ_FT_JOB_NAME ON JAHIA_QRTZ_FIRED_TRIGGERS;
DROP INDEX IDX_QRTZ_FT_JOB_GROUP ON JAHIA_QRTZ_FIRED_TRIGGERS;
DROP INDEX IDX_QRTZ_FT_JOB_STATEFUL ON JAHIA_QRTZ_FIRED_TRIGGERS;
DROP INDEX IDX_QRTZ_FT_JOB_REQ_RECOVERY ON JAHIA_QRTZ_FIRED_TRIGGERS;

DROP INDEX IDX_QRTZ_T_NEXT_FIRE_TIME ON JAHIA_QRTZ_TRIGGERS;
DROP INDEX IDX_QRTZ_T_STATE ON JAHIA_QRTZ_TRIGGERS;
DROP INDEX IDX_QRTZ_T_NF_ST ON JAHIA_QRTZ_TRIGGERS;
DROP INDEX IDX_QRTZ_T_GROUP ON JAHIA_QRTZ_TRIGGERS;
DROP INDEX IDX_QRTZ_T_VOLATILE ON JAHIA_QRTZ_TRIGGERS;

DROP INDEX jahia_reference_index ON jahia_reference;

DROP INDEX jahia_reference_index2 ON jahia_reference;

DROP INDEX jahia_users_index3 ON jahia_users;

---------------------------------------------------------------------

CREATE INDEX jahia_acl_index ON jahia_acl (parent_id_jahia_acl, id_jahia_acl);

CREATE INDEX jahia_acl_entries_index ON jahia_acl_entries (type_jahia_acl_entries);

CREATE INDEX jahia_ctn_def_index ON jahia_ctn_def (name_jahia_ctn_def, jahiaid_jahia_ctn_def);

CREATE INDEX jahia_ctn_entries_index ON jahia_ctn_entries (pageid_jahia_ctn_entries, rights_jahia_ctn_entries);

CREATE INDEX jahia_ctn_entries_index2 ON jahia_ctn_entries (rank_jahia_ctn_entries);

CREATE INDEX jahia_ctn_lists_index2 ON jahia_ctn_lists (pageid_jahia_ctn_lists, parententryid_jahia_ctn_lists, id_jahia_ctn_lists);

CREATE INDEX jahia_ctn_lists_index3 ON jahia_ctn_lists (parententryid_jahia_ctn_lists, id_jahia_ctn_lists);

CREATE INDEX jahia_ctn_lists_index4 ON jahia_ctn_lists (pageid_jahia_ctn_lists, ctndefid_jahia_ctn_lists, id_jahia_ctn_lists);

CREATE INDEX jahia_ctn_lists_index5 ON jahia_ctn_lists (pageid_jahia_ctn_lists, workflow_state, id_jahia_ctn_lists);

CREATE INDEX jahia_ctn_lists_index6 ON jahia_ctn_lists (pageid_jahia_ctn_lists, rights_jahia_ctn_lists);

CREATE INDEX jahia_fields_data_index ON jahia_fields_data (id_jahia_fields_data, workflow_state, pageid_jahia_fields_data);

CREATE INDEX jahia_fields_data_index2 ON jahia_fields_data (pageid_jahia_fields_data, ctnid_jahia_fields_data, id_jahia_fields_data, workflow_state);

CREATE INDEX jahia_fields_data_index3 ON jahia_fields_data (pageid_jahia_fields_data, rights_jahia_fields_data, workflow_state);

CREATE INDEX jahia_fields_data_index4 ON jahia_fields_data (ctnid_jahia_fields_data, id_jahia_fields_data);

CREATE INDEX jahia_fields_data_index5 ON jahia_fields_data (type_jahia_fields_data, value_jahia_fields_data, workflow_state, version_id);

CREATE INDEX jahia_fields_data_index6 ON jahia_fields_data (id_jahia_obj, ctnid_jahia_fields_data, workflow_state);

CREATE INDEX jahia_fields_data_index7 ON jahia_fields_data (fielddefid_jahia_fields_data, id_jahia_obj, type_jahia_obj, id_jahia_fields_data);

CREATE INDEX jahia_fields_data_index8 ON jahia_fields_data (ctnid_jahia_fields_data, workflow_state, id_jahia_fields_data);

CREATE INDEX jahia_fields_data_index9 ON jahia_fields_data (id_jahia_fields_data, workflow_state,language_code);

CREATE INDEX jahia_fields_data_index10 ON jahia_fields_data (id_jahia_obj, type_jahia_obj, workflow_state);

ALTER TABLE jahia_fields_def_prop DROP PRIMARY KEY, ADD PRIMARY KEY (flddefid_jahia_fields_def_prop, pdefid_jahia_fields_def_prop);

CREATE INDEX jahia_fields_def_prop_index ON jahia_fields_def_prop (id_jahia_fields_def_prop);

CREATE INDEX jahia_languages_states_index ON jahia_languages_states (workflow_state, siteid);

CREATE INDEX jahia_link_index ON jahia_link (type);

CREATE INDEX jahia_link_index1 ON jahia_link (right_oid,type);

CREATE INDEX jahia_link_index2 ON jahia_link (right_oid,left_oid,type);

CREATE INDEX jahia_link_index3 ON jahia_link (left_oid,type);

CREATE INDEX jahia_locks_index ON jahia_locks_non_excl (context_locks);

CREATE INDEX jahia_obj_index ON jahia_obj (timebpstate_jahia_obj, validfrom_jahia_obj, validto_jahia_obj);

ALTER TABLE jahia_pages_data DROP PRIMARY KEY, ADD PRIMARY KEY (id_jahia_pages_data, workflow_state, version_id, language_code);

CREATE INDEX jahia_pages_data_index ON jahia_pages_data (pagetype_jahia_pages_data, pagelinkid_jahia_pages_data);

CREATE INDEX jahia_pages_data_index2 ON jahia_pages_data (parentid_jahia_pages_data, workflow_state, version_id, id_jahia_pages_data);

CREATE INDEX jahia_pages_def_prop_index ON jahia_pages_def_prop (name_pages_def_prop, value_pages_def_prop);

CREATE INDEX IDX_QRTZ_JD_JOB_GROUP ON JAHIA_QRTZ_JOB_DETAILS(JOB_GROUP);
CREATE INDEX IDX_QRTZ_JD_REQ_RECOVERY ON JAHIA_QRTZ_JOB_DETAILS(REQUESTS_RECOVERY);

CREATE INDEX IDX_QRTZ_FT_TRIG_NAME ON JAHIA_QRTZ_FIRED_TRIGGERS(TRIGGER_NAME);
CREATE INDEX IDX_QRTZ_FT_TRIG_GROUP ON JAHIA_QRTZ_FIRED_TRIGGERS(TRIGGER_GROUP);
CREATE INDEX IDX_QRTZ_FT_TRIG_N_G ON JAHIA_QRTZ_FIRED_TRIGGERS(TRIGGER_NAME,TRIGGER_GROUP);
CREATE INDEX IDX_QRTZ_FT_TRIG_VOLATILE ON JAHIA_QRTZ_FIRED_TRIGGERS(IS_VOLATILE);
CREATE INDEX IDX_QRTZ_FT_TRIG_INST_NAME ON JAHIA_QRTZ_FIRED_TRIGGERS(INSTANCE_NAME);
CREATE INDEX IDX_QRTZ_FT_JOB_NAME ON JAHIA_QRTZ_FIRED_TRIGGERS(JOB_NAME);
CREATE INDEX IDX_QRTZ_FT_JOB_GROUP ON JAHIA_QRTZ_FIRED_TRIGGERS(JOB_GROUP);
CREATE INDEX IDX_QRTZ_FT_JOB_STATEFUL ON JAHIA_QRTZ_FIRED_TRIGGERS(IS_STATEFUL);
CREATE INDEX IDX_QRTZ_FT_JOB_REQ_RECOVERY ON JAHIA_QRTZ_FIRED_TRIGGERS(REQUESTS_RECOVERY);

CREATE INDEX IDX_QRTZ_T_NEXT_FIRE_TIME ON JAHIA_QRTZ_TRIGGERS(NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_STATE ON JAHIA_QRTZ_TRIGGERS(TRIGGER_STATE);
CREATE INDEX IDX_QRTZ_T_NF_ST ON JAHIA_QRTZ_TRIGGERS(TRIGGER_STATE,NEXT_FIRE_TIME);
CREATE INDEX IDX_QRTZ_T_GROUP ON JAHIA_QRTZ_TRIGGERS(TRIGGER_GROUP);
CREATE INDEX IDX_QRTZ_T_VOLATILE ON JAHIA_QRTZ_TRIGGERS(IS_VOLATILE);

ALTER TABLE jahia_reference DROP PRIMARY KEY, ADD PRIMARY KEY (ref_id, page_id, ref_type);

CREATE INDEX jahia_reference_index ON jahia_reference (ref_type, ref_id);

CREATE INDEX jahia_reference_index2 ON jahia_reference (page_id, ref_type);
