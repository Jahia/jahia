DROP INDEX jahia_ctn_def_index ON jahia_ctn_def ;

DROP INDEX jahia_ctn_def_properties_index ON jahia_ctn_def_properties ;

DROP INDEX jahia_ctn_entries_index ON jahia_ctn_entries ;

DROP INDEX jahia_ctn_entries_index2 ON jahia_ctn_entries ;

DROP INDEX jahia_ctn_entries_index3 ON jahia_ctn_entries ;

DROP INDEX jahia_ctn_entries_index4 ON jahia_ctn_entries ;

DROP INDEX jahia_ctn_entries_index5 ON jahia_ctn_entries ;

DROP INDEX jahia_ctn_entries_index6 ON jahia_ctn_entries ;

DROP INDEX jahia_ctn_lists_index ON jahia_ctn_lists ;

DROP INDEX jahia_ctn_lists_index2 ON jahia_ctn_lists ;

DROP INDEX jahia_ctn_lists_index3 ON jahia_ctn_lists ;

DROP INDEX jahia_ctn_lists_index4 ON jahia_ctn_lists ;

DROP INDEX jahia_ctn_lists_index5 ON jahia_ctn_lists ;

DROP INDEX jahia_ctn_lists_index6 ON jahia_ctn_lists ;

DROP INDEX jahia_ctn_struct_index ON jahia_ctn_struct ;

DROP INDEX jahia_ctn_struct_index2 ON jahia_ctn_struct ;

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

DROP INDEX jahia_fields_def_index ON jahia_fields_def ;

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

DROP INDEX jahia_obj_index2 ON jahia_obj;

DROP INDEX jahia_pages_data_index ON jahia_pages_data ;

DROP INDEX jahia_pages_data_index2 ON jahia_pages_data ;

DROP INDEX jahia_pages_data_index3 ON jahia_pages_data ;

DROP INDEX jahia_pages_def_index ON jahia_pages_def ;

DROP INDEX jahia_pages_def_prop_index ON jahia_pages_def_prop ;

DROP INDEX jahia_pages_def_prop_index2 ON jahia_pages_def_prop ;

DROP INDEX jahia_pages_prop_value_index ON jahia_pages_prop ;
 
DROP INDEX jahia_reference_index ON jahia_reference ;

DROP INDEX jahia_reference_index2 ON jahia_reference ;

DROP INDEX jahia_sites_index ON jahia_sites ;

DROP INDEX jahia_user_prop_index ON jahia_user_prop ;

DROP INDEX jahia_users_index ON jahia_users ;

DROP INDEX jahia_workflow_index ON jahia_workflow ;

CREATE INDEX jahia_ctn_def_index ON jahia_ctn_def (name_jahia_ctn_def, jahiaid_jahia_ctn_def);

CREATE INDEX jahia_ctn_entries_index ON jahia_ctn_entries (pageid_jahia_ctn_entries, rights_jahia_ctn_entries, workflow_state);

CREATE INDEX jahia_ctn_entries_index2 ON jahia_ctn_entries (rank_jahia_ctn_entries);

CREATE INDEX jahia_ctn_entries_index3 ON jahia_ctn_entries (pageid_jahia_ctn_entries, workflow_state);

CREATE INDEX jahia_ctn_entries_index4 ON jahia_ctn_entries (listid_jahia_ctn_entries, workflow_state, rank_jahia_ctn_entries, id_jahia_ctn_entries);

CREATE INDEX jahia_ctn_entries_index5 ON jahia_ctn_entries (id_jahia_ctn_entries, workflow_state, listid_jahia_ctn_entries);

CREATE INDEX jahia_ctn_entries_index6 ON jahia_ctn_entries (ctndefid_jahia_ctn_entries);

CREATE INDEX jahia_ctn_lists_index ON jahia_ctn_lists (id_jahia_ctn_lists, workflow_state);

CREATE INDEX jahia_ctn_lists_index2 ON jahia_ctn_lists (pageid_jahia_ctn_lists, parententryid_jahia_ctn_lists, id_jahia_ctn_lists);

CREATE INDEX jahia_ctn_lists_index3 ON jahia_ctn_lists (parententryid_jahia_ctn_lists, id_jahia_ctn_lists);

CREATE INDEX jahia_ctn_lists_index4 ON jahia_ctn_lists (pageid_jahia_ctn_lists, ctndefid_jahia_ctn_lists, id_jahia_ctn_lists);

CREATE INDEX jahia_ctn_lists_index5 ON jahia_ctn_lists (pageid_jahia_ctn_lists, workflow_state, id_jahia_ctn_lists);

CREATE INDEX jahia_ctn_lists_index6 ON jahia_ctn_lists (pageid_jahia_ctn_lists, rights_jahia_ctn_lists, workflow_state);

CREATE INDEX jahia_ctn_struct_index ON jahia_ctn_struct (ctnsubdefid_jahia_ctn_struct, rank_jahia_ctn_struct);

CREATE INDEX jahia_ctn_struct_index2 ON jahia_ctn_struct (objdefid_jahia_ctn_struct, objtype_jahia_ctn_struct, ctnsubdefid_jahia_ctn_struct);

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

CREATE INDEX jahia_fields_data_index11 ON jahia_fields_data (id_jahia_fields_data, workflow_state, version_id, pageid_jahia_fields_data);

CREATE INDEX jahia_fields_def_index ON jahia_fields_def (name_jahia_fields_def);

CREATE INDEX jahia_languages_states_index ON jahia_languages_states (workflow_state, siteid);

CREATE INDEX jahia_link_index ON jahia_link (type, id);

CREATE INDEX jahia_link_index1 ON jahia_link (right_oid,type);

CREATE INDEX jahia_link_index2 ON jahia_link (right_oid,left_oid,type);

CREATE INDEX jahia_link_index3 ON jahia_link (left_oid,type);

CREATE INDEX jahia_locks_index ON jahia_locks_non_excl (context_locks);

CREATE INDEX jahia_obj_index ON jahia_obj (timebpstate_jahia_obj, validfrom_jahia_obj, validto_jahia_obj);

CREATE INDEX jahia_obj_index2 ON jahia_obj (type_jahia_obj, id_jahia_obj);

CREATE INDEX jahia_pages_data_index ON jahia_pages_data (pagetype_jahia_pages_data, pagelinkid_jahia_pages_data);

CREATE INDEX jahia_pages_data_index2 ON jahia_pages_data (parentid_jahia_pages_data, workflow_state, version_id, id_jahia_pages_data);

CREATE INDEX jahia_pages_data_index3 ON jahia_pages_data (rights_jahia_pages_data);

CREATE INDEX jahia_pages_def_prop_index ON jahia_pages_def_prop (name_pages_def_prop, value_pages_def_prop);

CREATE INDEX jahia_pages_prop_value_index ON jahia_pages_prop (prop_value);

CREATE INDEX jahia_reference_index ON jahia_reference (ref_type, ref_id);

CREATE INDEX jahia_reference_index2 ON jahia_reference (page_id, ref_type);

CREATE INDEX jahia_sites_index ON jahia_sites(servername_jahia_sites);

CREATE INDEX jahia_user_prop_index ON jahia_user_prop (id_jahia_users, provider_jahia_user_prop, userkey_jahia_user_prop);

CREATE INDEX jahia_users_index ON jahia_users (name_jahia_users);

CREATE INDEX jahia_workflow_index ON jahia_workflow(MAINOBJECTKEY);