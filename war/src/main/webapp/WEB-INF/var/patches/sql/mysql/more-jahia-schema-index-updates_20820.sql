DROP INDEX jahia_ctn_entries_index5 ON jahia_ctn_entries ;

DROP INDEX jahia_ctn_entries_index6 ON jahia_ctn_entries ;

DROP INDEX jahia_ctn_struct_index ON jahia_ctn_struct ;

DROP INDEX jahia_ctn_struct_index2 ON jahia_ctn_struct ;

DROP INDEX jahia_fields_data_index11 ON jahia_fields_data ;

DROP INDEX jahia_link_index ON jahia_link ;

DROP INDEX jahia_obj_index2 ON jahia_obj;

DROP INDEX jahia_sites_index ON jahia_sites ;

DROP INDEX jahia_sl2_permissions_index ON jahia_sl2_permissions ;

DROP INDEX jahia_workflow_index ON jahia_workflow ;

CREATE INDEX jahia_ctn_entries_index5 ON jahia_ctn_entries (id_jahia_ctn_entries, workflow_state, listid_jahia_ctn_entries);

CREATE INDEX jahia_ctn_entries_index6 ON jahia_ctn_entries (ctndefid_jahia_ctn_entries);

CREATE INDEX jahia_ctn_struct_index ON jahia_ctn_struct (ctnsubdefid_jahia_ctn_struct, rank_jahia_ctn_struct);

CREATE INDEX jahia_ctn_struct_index2 ON jahia_ctn_struct (objdefid_jahia_ctn_struct, objtype_jahia_ctn_struct, ctnsubdefid_jahia_ctn_struct);

CREATE INDEX jahia_fields_data_index11 ON jahia_fields_data (id_jahia_fields_data, workflow_state, version_id, pageid_jahia_fields_data);

CREATE INDEX jahia_link_index ON jahia_link (type, id);

CREATE INDEX jahia_obj_index2 ON jahia_obj (type_jahia_obj, id_jahia_obj);

CREATE INDEX jahia_sites_index ON jahia_sites(servername_jahia_sites);

CREATE INDEX jahia_sl2_permissions_index ON jahia_sl2_permissions(object_id, succession);

CREATE INDEX jahia_workflow_index ON jahia_workflow(MAINOBJECTKEY);