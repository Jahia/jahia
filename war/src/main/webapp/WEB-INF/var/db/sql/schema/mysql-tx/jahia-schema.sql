alter table jahia_acl_entries drop foreign key FKDBE729858C498B01;
alter table jahia_acl_names drop foreign key FK3C5F357D48212BE1;
alter table jahia_app_def drop foreign key FKE6C84991455C901A;
alter table jahia_appdef_prop drop foreign key FK8D4A0C287FCEE0FB;
alter table jahia_apps_share drop foreign key FK7FE512E81F78D07;
alter table jahia_apps_share drop foreign key FK7FE512E83D3F9BB0;
alter table jahia_audit_log drop foreign key FKF4669B0AC75E28FD;
alter table jahia_category drop foreign key FKAFAF3D94348193C9;
alter table jahia_ctn_def_properties drop foreign key FKA2522525B1CEB792;
alter table jahia_ctn_entries drop foreign key FK6719F01827313F9D;
alter table jahia_ctn_lists drop foreign key FKB490111D2A3FB9A2;
alter table jahia_ctn_struct drop foreign key FKE9FE8F4D62299E65;
alter table jahia_ctndef_prop drop foreign key FK5FB05824447AD44A;
alter table jahia_fields_data drop foreign key FK891B251A291A9BF4;
alter table jahia_fields_def drop foreign key FK5700E095767D5370;
alter table jahia_fields_def_extprop drop foreign key FKC2B7575A477EEAEC;
alter table jahia_grps drop foreign key FKE530C7C492027B41;
alter table jahia_nstep_workflowinstance drop foreign key FKDA6D7CCFA3F4D577;
alter table jahia_nstep_workflowinstance drop foreign key FKDA6D7CCF801AE453;
alter table jahia_nstep_workflowinstance drop foreign key FKDA6D7CCFED90E370;
alter table jahia_nstep_workflowstep drop foreign key FK6A6E1C067F20B53;
alter table jahia_obj drop foreign key FKF6E0A6A143AACCE0;
alter table jahia_pages_def drop foreign key FK1EA2B334B5FF0C79;
alter table jahia_pages_def_prop drop foreign key FK8840898E47E25CC;
alter table jahia_pwd_policy_rule_params drop foreign key FKBE451EF45A0DB19B;
alter table jahia_pwd_policy_rules drop foreign key FK2BC650026DA1D1E6;
alter table jahia_retrule drop foreign key FK578E2BC72D76FCE6;
alter table jahia_retrule_range drop foreign key FK688A96C57D611258;
alter table jahia_site_lang_list drop foreign key FK1DDBC16D7EED26D3;
alter table jahia_site_lang_maps drop foreign key FK1DDC17667EED26D3;
alter table jahia_sites_grps drop foreign key FK7B245597F46755FE;
alter table jahia_slideindexjobserver drop foreign key FK17554649A2228D92;
drop table if exists jahia_acl;
drop table if exists jahia_acl_entries;
drop table if exists jahia_acl_names;
drop table if exists jahia_app_def;
drop table if exists jahia_appdef_prop;
drop table if exists jahia_appentry;
drop table if exists jahia_apps_share;
drop table if exists jahia_audit_log;
drop table if exists jahia_bigtext_data;
drop table if exists jahia_category;
drop table if exists jahia_category_prop;
drop table if exists jahia_ctn_def;
drop table if exists jahia_ctn_def_properties;
drop table if exists jahia_ctn_entries;
drop table if exists jahia_ctn_lists;
drop table if exists jahia_ctn_struct;
drop table if exists jahia_ctndef_prop;
drop table if exists jahia_ctnentries_prop;
drop table if exists jahia_ctnlists_prop;
drop table if exists jahia_db_test;
drop table if exists jahia_fields_data;
drop table if exists jahia_fields_def;
drop table if exists jahia_fields_def_extprop;
drop table if exists jahia_fields_prop;
drop table if exists jahia_grp_access;
drop table if exists jahia_grp_prop;
drop table if exists jahia_grps;
drop table if exists jahia_installedpatch;
drop table if exists jahia_languages_states;
drop table if exists jahia_link;
drop table if exists jahia_link_metadata;
drop table if exists jahia_locks_non_excl;
drop table if exists jahia_nstep_workflow;
drop table if exists jahia_nstep_workflowhistory;
drop table if exists jahia_nstep_workflowinstance;
drop table if exists jahia_nstep_workflowstep;
drop table if exists jahia_nstep_workflowuser;
drop table if exists jahia_obj;
drop table if exists jahia_pages_data;
drop table if exists jahia_pages_def;
drop table if exists jahia_pages_def_prop;
drop table if exists jahia_pages_prop;
drop table if exists jahia_pwd_policies;
drop table if exists jahia_pwd_policy_rule_params;
drop table if exists jahia_pwd_policy_rules;
drop table if exists jahia_reference;
drop table if exists jahia_resources;
drop table if exists jahia_retrule;
drop table if exists jahia_retrule_range;
drop table if exists jahia_retruledef;
drop table if exists jahia_serverprops;
drop table if exists jahia_site_lang_list;
drop table if exists jahia_site_lang_maps;
drop table if exists jahia_site_prop;
drop table if exists jahia_sites;
drop table if exists jahia_sites_grps;
drop table if exists jahia_sites_users;
drop table if exists jahia_slideindexingjob;
drop table if exists jahia_slideindexjobserver;
drop table if exists jahia_statistics;
drop table if exists jahia_user_prop;
drop table if exists jahia_users;
drop table if exists jahia_version;
drop table if exists jahia_workflow;

CREATE TABLE jahia_acl (
    id_jahia_acl INTEGER NOT NULL,
    inheritance_jahia_acl INTEGER,
    hasentries_jahia_acl INTEGER,
    parent_id_jahia_acl INTEGER,
    picked_id_jahia_acl INTEGER,    
    PRIMARY KEY(id_jahia_acl)
);
ALTER TABLE jahia_acl TYPE=InnoDB;

CREATE TABLE jahia_acl_entries (
    id_jahia_acl INTEGER NOT NULL,
    type_jahia_acl_entries INTEGER NOT NULL,
    target_jahia_acl_entries VARCHAR (50) NOT NULL,
    entry_state_jahia_acl_entries INTEGER NOT NULL,
    entry_trist_jahia_acl_entries INTEGER NOT NULL,
    PRIMARY KEY(id_jahia_acl,type_jahia_acl_entries,target_jahia_acl_entries)
);
ALTER TABLE jahia_acl_entries TYPE=InnoDB;

CREATE TABLE jahia_acl_names (
    acl_name VARCHAR (255) NOT NULL,
    acl_id INTEGER UNIQUE,
    PRIMARY KEY(acl_name)
);
ALTER TABLE jahia_acl_names TYPE=InnoDB;

CREATE TABLE jahia_app_def (
    id_jahia_app_def INTEGER NOT NULL,
    name_jahia_app_def VARCHAR (250),
    context_jahia_app_def VARCHAR (250),
    visible_jahia_app_def INTEGER,
    shared_jahia_app_def INTEGER,
    rights_jahia_app_def INTEGER,
    filename_jahia_app_def VARCHAR (100),
    desc_jahia_app_def VARCHAR (250),
    type_jahia_app_def VARCHAR (30),
    PRIMARY KEY(id_jahia_app_def)
);
ALTER TABLE jahia_app_def TYPE=InnoDB;

CREATE TABLE jahia_appdef_prop (
    appdefid_appdef_prop INTEGER NOT NULL,
    propname_appdef_prop VARCHAR (250) NOT NULL,
    propvalue_appdef_prop VARCHAR (250),
    PRIMARY KEY(appdefid_appdef_prop,propname_appdef_prop)
);
ALTER TABLE jahia_appdef_prop TYPE=InnoDB;

CREATE TABLE jahia_appentry (
    id_jahia_appentry INTEGER NOT NULL,
    appid_jahia_appentry INTEGER,
    defname_jahia_appentry VARCHAR (250),
    PRIMARY KEY(id_jahia_appentry)
);
ALTER TABLE jahia_appentry TYPE=InnoDB;

CREATE TABLE jahia_apps_share (
    definition INTEGER NOT NULL,
    site INTEGER NOT NULL,
    PRIMARY KEY(definition,site)
);
ALTER TABLE jahia_apps_share TYPE=InnoDB;

CREATE TABLE jahia_audit_log (
    id_jahia_audit_log INTEGER NOT NULL,
    time_jahia_audit_log BIGINT,
    username_jahia_audit_log VARCHAR (50),
    objecttype_jahia_audit_log INTEGER,
    objectid_jahia_audit_log INTEGER,
    parenttype_jahia_audit_log INTEGER,
    parentid_jahia_audit_log INTEGER,
    operation_jahia_audit_log VARCHAR (50),
    site_jahia_audit_log VARCHAR (50),
    content_jahia_audit_log VARCHAR (250),
    parent_id_jahia_audit_log INTEGER,
    eventType VARCHAR (255),
    eventInformation BLOB,    
    PRIMARY KEY(id_jahia_audit_log)
);
ALTER TABLE jahia_audit_log TYPE=InnoDB;

CREATE TABLE jahia_bigtext_data (
    id_bigtext_data VARCHAR (255) NOT NULL,
    raw_value LONGTEXT,
    PRIMARY KEY(id_bigtext_data)
);
ALTER TABLE jahia_bigtext_data TYPE=InnoDB;

CREATE TABLE jahia_category (
    id_category INTEGER NOT NULL,
    key_category VARCHAR (250) NOT NULL UNIQUE,
    aclid_category INTEGER,
    PRIMARY KEY(id_category)
);
ALTER TABLE jahia_category TYPE=InnoDB;

CREATE TABLE jahia_category_prop (
    id_category INTEGER NOT NULL,
    name_category_prop VARCHAR (250) NOT NULL,
    value_category_prop VARCHAR (250) NOT NULL,
    PRIMARY KEY(id_category,name_category_prop)
);
ALTER TABLE jahia_category_prop TYPE=InnoDB;

CREATE TABLE jahia_ctn_def (
    id_jahia_ctn_def INTEGER NOT NULL,
    jahiaid_jahia_ctn_def INTEGER NOT NULL,
    name_jahia_ctn_def VARCHAR (250),
    PRIMARY KEY(id_jahia_ctn_def)
);
ALTER TABLE jahia_ctn_def TYPE=InnoDB;

CREATE TABLE jahia_ctn_def_properties (
    id_jahia_ctn_def_properties INTEGER NOT NULL,
    ctndefid_jahia_ctn_def_prop INTEGER NOT NULL,
    pagedefid_jahia_ctn_def_prop INTEGER,
    title_jahia_ctn_def_properties VARCHAR (250),
    PRIMARY KEY(id_jahia_ctn_def_properties)
);
ALTER TABLE jahia_ctn_def_properties TYPE=InnoDB;

CREATE TABLE jahia_ctn_entries (
    id_jahia_ctn_entries INTEGER NOT NULL,
    version_id BIGINT NOT NULL,
    workflow_state INTEGER NOT NULL,    
    ctndefid_jahia_ctn_entries INTEGER,    
    rights_jahia_ctn_entries INTEGER,    
    listid_jahia_ctn_entries INTEGER,    
    pageid_jahia_ctn_entries INTEGER,
    rank_jahia_ctn_entries INTEGER,
    jahiaid_jahia_ctn_entries INTEGER,    
    PRIMARY KEY(id_jahia_ctn_entries,workflow_state,version_id)
);
ALTER TABLE jahia_ctn_entries TYPE=InnoDB;

CREATE TABLE jahia_ctn_lists (
    id_jahia_ctn_lists INTEGER NOT NULL,
    version_id BIGINT NOT NULL,
    workflow_state INTEGER NOT NULL,    
    pageid_jahia_ctn_lists INTEGER,
    parententryid_jahia_ctn_lists INTEGER,    
    ctndefid_jahia_ctn_lists INTEGER,
    rights_jahia_ctn_lists INTEGER,
    PRIMARY KEY(id_jahia_ctn_lists,version_id,workflow_state)
);
ALTER TABLE jahia_ctn_lists TYPE=InnoDB;

CREATE TABLE jahia_ctn_struct (
    ctnsubdefid_jahia_ctn_struct INTEGER NOT NULL,
    objtype_jahia_ctn_struct INTEGER NOT NULL,
    objdefid_jahia_ctn_struct INTEGER NOT NULL,
    rank_jahia_ctn_struct INTEGER,
    PRIMARY KEY(ctnsubdefid_jahia_ctn_struct,objtype_jahia_ctn_struct,objdefid_jahia_ctn_struct)
);
ALTER TABLE jahia_ctn_struct TYPE=InnoDB;

CREATE TABLE jahia_ctndef_prop (
    id_jahia_ctn_def INTEGER NOT NULL,
    name_jahia_ctndef_prop VARCHAR (255) NOT NULL,
    value_jahia_ctndef_prop VARCHAR (255),
    PRIMARY KEY(id_jahia_ctn_def,name_jahia_ctndef_prop)
);
ALTER TABLE jahia_ctndef_prop TYPE=InnoDB;

CREATE TABLE jahia_ctnentries_prop (
    ctnid_ctnentries_prop INTEGER NOT NULL,
    name_ctnentries_prop VARCHAR (255) NOT NULL,
    jahiaid_ctnentries_prop INTEGER,
    value_ctnentries_prop VARCHAR (255),
    PRIMARY KEY(ctnid_ctnentries_prop,name_ctnentries_prop)
);
ALTER TABLE jahia_ctnentries_prop TYPE=InnoDB;

CREATE TABLE jahia_ctnlists_prop (
    ctnlistid_ctnlists_prop INTEGER NOT NULL,
    name_ctnlists_prop VARCHAR (255) NOT NULL,
    jahiaid_ctnlists_prop INTEGER,    
    value_ctnlists_prop VARCHAR (255),
    PRIMARY KEY(ctnlistid_ctnlists_prop,name_ctnlists_prop)
);
ALTER TABLE jahia_ctnlists_prop TYPE=InnoDB;

CREATE TABLE jahia_db_test (
    testfield VARCHAR (255) NOT NULL,
    PRIMARY KEY(testfield)
);
ALTER TABLE jahia_db_test TYPE=InnoDB;

CREATE TABLE jahia_fields_data (
    id_jahia_fields_data INTEGER NOT NULL,
    version_id BIGINT NOT NULL,
    workflow_state INTEGER NOT NULL,    
    language_code VARCHAR (10) NOT NULL,    
    connecttype_jahia_fields_data INTEGER,    
    ctnid_jahia_fields_data INTEGER,
    fielddefid_jahia_fields_data INTEGER,    
    id_jahia_obj INTEGER,
    type_jahia_obj VARCHAR (22),    
    rights_jahia_fields_data INTEGER,    
    pageid_jahia_fields_data INTEGER,
    jahiaid_jahia_fields_data INTEGER,    
    type_jahia_fields_data INTEGER,
    value_jahia_fields_data VARCHAR (250)
    PRIMARY KEY(id_jahia_fields_data,version_id,workflow_state,language_code)
);
ALTER TABLE jahia_fields_data TYPE=InnoDB;

CREATE TABLE jahia_fields_def (
    id_jahia_fields_def INTEGER NOT NULL,
    ismdata_jahia_fields_def INTEGER,
    jahiaid_jahia_fields_def INTEGER,
    name_jahia_fields_def VARCHAR (250),
    PRIMARY KEY(id_jahia_fields_def)
);
ALTER TABLE jahia_fields_def TYPE=InnoDB;

CREATE TABLE jahia_fields_def_extprop (
    id_jahia_fields_def INTEGER NOT NULL,
    prop_name VARCHAR (200) NOT NULL,
    prop_value VARCHAR (255),
    PRIMARY KEY(id_jahia_fields_def,prop_name)
);
ALTER TABLE jahia_fields_def_extprop TYPE=InnoDB;

CREATE TABLE jahia_fields_prop (
    fieldid_jahia_fields_prop INTEGER NOT NULL,
    propertyname_jahia_fields_prop VARCHAR (250) NOT NULL,
    propvalue_jahia_fields_prop VARCHAR (50),
    PRIMARY KEY(fieldid_jahia_fields_prop,propertyname_jahia_fields_prop)
);
ALTER TABLE jahia_fields_prop TYPE=InnoDB;

CREATE TABLE jahia_grp_access (
    id_jahia_member VARCHAR (150) NOT NULL,
    id_jahia_grps VARCHAR (150) NOT NULL,
    membertype_grp_access INTEGER NOT NULL,
    PRIMARY KEY(id_jahia_member,id_jahia_grps,membertype_grp_access)
);
ALTER TABLE jahia_grp_access TYPE=InnoDB;

CREATE TABLE jahia_grp_prop (
    id_jahia_grp INTEGER NOT NULL,
    name_jahia_grp_prop VARCHAR (50) NOT NULL,
    provider_jahia_grp_prop VARCHAR (50) NOT NULL,
    grpkey_jahia_grp_prop VARCHAR (200) NOT NULL,    
    value_jahia_grp_prop VARCHAR (255),
    PRIMARY KEY(id_jahia_grp,name_jahia_grp_prop,provider_jahia_grp_prop,grpkey_jahia_grp_prop)
);
ALTER TABLE jahia_grp_prop TYPE=InnoDB;

CREATE TABLE jahia_grps (
    id_jahia_grps INTEGER NOT NULL,
    name_jahia_grps VARCHAR (195),
    key_jahia_grps VARCHAR (200) UNIQUE,
    siteid_jahia_grps INTEGER,
    PRIMARY KEY(id_jahia_grps)
);
ALTER TABLE jahia_grps TYPE=InnoDB;

CREATE TABLE jahia_installedpatch (
    install_number INTEGER NOT NULL,
    name VARCHAR(100),
    build INTEGER,
    result_code INTEGER,
    install_date DATETIME,
    PRIMARY KEY(install_number)
);
ALTER TABLE jahia_installedpatch TYPE=InnoDB;  

CREATE TABLE jahia_languages_states (
    objectkey VARCHAR(40) NOT NULL,
    language_code VARCHAR(10) NOT NULL,
    workflow_state INTEGER,
    siteid INTEGER,
    PRIMARY KEY (objectkey, language_code)
);
ALTER TABLE jahia_languages_states TYPE=InnoDB;  

CREATE TABLE jahia_link (
    id INTEGER NOT NULL,
    left_oid VARCHAR(100),
    right_oid VARCHAR(100),
    type VARCHAR(100),
    PRIMARY KEY(id)
);
ALTER TABLE jahia_link TYPE=InnoDB;

CREATE TABLE jahia_link_metadata (
    link_id INTEGER NOT NULL,
    link_position VARCHAR(20) NOT NULL,
    property_name VARCHAR(255) NOT NULL,
    property_value VARCHAR(255),
    PRIMARY KEY(link_id,link_position,property_name)
);
ALTER TABLE jahia_link_metadata TYPE=InnoDB;

CREATE TABLE jahia_locks_non_excl (
    name_locks VARCHAR (50) NOT NULL,
    targetid_locks INTEGER NOT NULL,
    action_locks VARCHAR(50) NOT NULL,
    context_locks VARCHAR(80) NOT NULL,    
    owner_locks VARCHAR (50),
    timeout_locks INTEGER,
    expirationDate_locks BIGINT,
    serverid_locks VARCHAR(30),    
    stolen_locks VARCHAR(10),
    PRIMARY KEY(name_locks, targetid_locks, action_locks, context_locks)
);
ALTER TABLE jahia_locks TYPE=InnoDB;

CREATE TABLE jahia_nstep_workflow (
    id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);
ALTER TABLE jahia_nstep_workflow TYPE=InnoDB;

CREATE TABLE jahia_nstep_workflowhistory (
    id BIGINT NOT NULL,
    action VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    message VARCHAR(255),
    actionDate DATETIME NOT NULL,
    languageCode VARCHAR(255) NOT NULL,
    objectKey VARCHAR(255) NOT NULL,
    process VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);
ALTER TABLE jahia_nstep_workflowhistory TYPE=InnoDB;

CREATE TABLE jahia_nstep_workflowinstance (
    id bigint NOT NULL,
    authorEmail VARCHAR(255),
    languageCode VARCHAR(255) NOT NULL,
    objectKey VARCHAR(255) NOT NULL,
    step BIGINT,
    user_id BIGINT,
    workflow BIGINT,
    startDate DATETIME,
    PRIMARY KEY (id)
);
ALTER TABLE jahia_nstep_workflowinstance TYPE=InnoDB;

CREATE TABLE jahia_nstep_workflowstep (
    id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    workflow_id BIGINT,
    step_index INTEGER,
    PRIMARY KEY (id)
);
ALTER TABLE jahia_nstep_workflowstep TYPE=InnoDB;

CREATE TABLE jahia_nstep_workflowuser (
    id BIGINT NOT NULL,
    login VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);
ALTER TABLE jahia_nstep_workflowuser TYPE=InnoDB;

CREATE TABLE jahia_obj (
    id_jahia_obj INTEGER NOT NULL,
    type_jahia_obj VARCHAR (22) NOT NULL,
    jahiaid_jahia_obj INTEGER,
    timebpstate_jahia_obj INTEGER,
    validfrom_jahia_obj BIGINT,
    validto_jahia_obj BIGINT,
    retrule_jahia_obj INTEGER,
    PRIMARY KEY(id_jahia_obj,type_jahia_obj)
);
ALTER TABLE jahia_obj TYPE=InnoDB;

CREATE TABLE jahia_pages_data (
    id_jahia_pages_data INTEGER NOT NULL,
    workflow_state INTEGER NOT NULL,
    version_id BIGINT NOT NULL,
    language_code VARCHAR (10) NOT NULL,
    rights_jahia_pages_data INTEGER,    
    pagelinkid_jahia_pages_data INTEGER,    
    pagetype_jahia_pages_data INTEGER,
    pagedefid_jahia_pages_data INTEGER,
    parentid_jahia_pages_data INTEGER,
    remoteurl_jahia_pages_data VARCHAR (250),                
    jahiaid_jahia_pages_data INTEGER,
    title_jahia_pages_data VARCHAR (250),
    PRIMARY KEY(id_jahia_pages_data,workflow_state,version_id,language_code)
);
ALTER TABLE jahia_pages_data TYPE=InnoDB;

CREATE TABLE jahia_pages_def (
    id_jahia_pages_def INTEGER NOT NULL,
    jahiaid_jahia_pages_def INTEGER,
    name_jahia_pages_def VARCHAR (250),
    sourcepath_jahia_pages_def VARCHAR (250),
    visible_jahia_pages_def BIT,
    browsable_jahia_pages_def INTEGER,
    warning_msg_jahia_pages_def VARCHAR (250),
    img_jahia_pages_def VARCHAR (150),
    PRIMARY KEY(id_jahia_pages_def)
);
ALTER TABLE jahia_pages_def TYPE=InnoDB;

CREATE TABLE jahia_pages_def_prop (
    id_jahia_pages_def_prop INTEGER NOT NULL,
    jahiaid_pages_def_prop INTEGER NOT NULL,
    name_pages_def_prop VARCHAR (100) NOT NULL,
    value_pages_def_prop VARCHAR (200),
    PRIMARY KEY(id_jahia_pages_def_prop,jahiaid_pages_def_prop,name_pages_def_prop)
);
ALTER TABLE jahia_pages_def_prop TYPE=InnoDB;

CREATE TABLE jahia_pages_prop (
    page_id INTEGER NOT NULL,
    prop_name VARCHAR (150) NOT NULL,
    language_code VARCHAR (100) NOT NULL,
    prop_value VARCHAR (255),    
    PRIMARY KEY(page_id,prop_name,language_code)
);
ALTER TABLE jahia_pages_prop TYPE=InnoDB;

CREATE TABLE jahia_pwd_policies (
    jahia_pwd_policy_id INTEGER NOT NULL,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY(jahia_pwd_policy_id)
);
ALTER TABLE jahia_pwd_policies TYPE=InnoDB;

CREATE TABLE jahia_pwd_policy_rule_params (
    jahia_pwd_policy_rule_param_id INTEGER NOT NULL,
    name VARCHAR(50) NOT NULL,
    position_index INTEGER NOT NULL,
    jahia_pwd_policy_rule_id INTEGER NOT NULL,
    type CHAR(1) NOT NULL,
    value VARCHAR(255),
    PRIMARY KEY(jahia_pwd_policy_rule_param_id)
);
ALTER TABLE jahia_pwd_policy_rule_params TYPE=InnoDB;

CREATE TABLE jahia_pwd_policy_rules (
    jahia_pwd_policy_rule_id INTEGER NOT NULL,
    action CHAR(1) NOT NULL,
    rule_condition MEDIUMTEXT NOT NULL,
    evaluator CHAR(1) NOT NULL,
    name VARCHAR(255) NOT NULL,
    jahia_pwd_policy_id INTEGER NOT NULL,
    position_index INTEGER NOT NULL,
    active BIT NOT NULL,
    last_rule BIT NOT NULL,
    periodical BIT NOT NULL,
    PRIMARY KEY(jahia_pwd_policy_rule_id)
);
ALTER TABLE jahia_pwd_policy_rules TYPE=InnoDB;

CREATE TABLE jahia_reference (
    page_id INTEGER NOT NULL,
    ref_id INTEGER NOT NULL,
    ref_type INTEGER NOT NULL,
    PRIMARY KEY (ref_id, page_id, ref_type)
);
ALTER TABLE jahia_reference TYPE=InnoDB;

CREATE TABLE jahia_resources (
    name_resource VARCHAR (200) NOT NULL,
    languagecode_resource VARCHAR (10) NOT NULL,
    value_resource VARCHAR (255),
    PRIMARY KEY(name_resource,languagecode_resource)
);
ALTER TABLE jahia_resources TYPE=InnoDB;

CREATE TABLE jahia_retrule (
    id_jahia_retrule INTEGER NOT NULL,
    id_jahia_retruledef INTEGER,
    inherited_retrule BIT,
    enabled_retrule BIT,
    title_retrule VARCHAR(255),
    comment_retrule VARCHAR(255),
    shared_retrule BIT,
    settings_retrule LONGTEXT,
    PRIMARY KEY (id_jahia_retrule)
);
ALTER TABLE jahia_retrule TYPE=InnoDB;

CREATE TABLE jahia_retrule_range (
    id_retrule_range INTEGER NOT NULL,
    validfrom_retrule_range BIGINT,
    validto_retrule_range BIGINT,
    notiffromd_retrule_range BIT,
    notiftod_retrule_range BIT,
    PRIMARY KEY (id_retrule_range)
);
ALTER TABLE jahia_retrule_range TYPE=InnoDB;

CREATE TABLE jahia_retruledef (
    id_jahia_retruledef INTEGER NOT NULL,
    name_retruledef VARCHAR(255) UNIQUE,
    title_retruledef VARCHAR(255),
    ruleclass_retruledef VARCHAR(255),
    rulehelperclass_retruledef VARCHAR(255),
    dateformat_retruledef VARCHAR(255),
    PRIMARY KEY (id_jahia_retruledef)
);
ALTER TABLE jahia_retruledef TYPE=InnoDB;

CREATE TABLE jahia_serverprops (
    id_serverprops VARCHAR(50) NOT NULL,
    propname_serverprops VARCHAR(200) NOT NULL,
    propvalue_serverprops VARCHAR(250) NOT NULL,
    PRIMARY KEY (id_serverprops, propname_serverprops)
);
ALTER TABLE jahia_serverprops TYPE=InnoDB;

CREATE TABLE jahia_site_lang_list (
    id INTEGER NOT NULL,
    site_id INTEGER,
    code VARCHAR (255),
    rank INTEGER,
    activated BIT,
    mandatory BIT,
    PRIMARY KEY(id)
);
ALTER TABLE jahia_site_lang_list TYPE=InnoDB;

CREATE TABLE jahia_site_lang_maps (
    id INTEGER NOT NULL,
    site_id INTEGER,
    from_lang_code VARCHAR (255),
    to_lang_code VARCHAR (255),
    PRIMARY KEY(id)
);
ALTER TABLE jahia_site_lang_maps TYPE=InnoDB;

CREATE TABLE jahia_site_prop (
    id_jahia_site INTEGER NOT NULL,
    name_jahia_site_prop VARCHAR (255) NOT NULL,
    value_jahia_site_prop VARCHAR (255),
    PRIMARY KEY(id_jahia_site,name_jahia_site_prop)
);
ALTER TABLE jahia_site_prop TYPE=InnoDB;

CREATE TABLE jahia_sites (
    id_jahia_sites INTEGER NOT NULL,
    title_jahia_sites VARCHAR (100),
    servername_jahia_sites VARCHAR (200),
    key_jahia_sites VARCHAR (50) UNIQUE,
    active_jahia_sites INTEGER,
    defaultpageid_jahia_sites INTEGER,
    defaulttemplateid_jahia_sites INTEGER,
    tpl_deploymode_jahia_sites INTEGER,
    webapps_deploymode_jahia_sites INTEGER,
    rights_jahia_sites INTEGER,
    descr_jahia_sites VARCHAR (250),
    default_site_jahia_sites BIT,    
    PRIMARY KEY(id_jahia_sites)
);
ALTER TABLE jahia_sites TYPE=InnoDB;

CREATE TABLE jahia_sites_grps (
    grpname_sites_grps VARCHAR (50) NOT NULL,
    siteid_sites_grps INTEGER NOT NULL,
    grpid_sites_grps VARCHAR (255),
    PRIMARY KEY(grpname_sites_grps,siteid_sites_grps)
);
ALTER TABLE jahia_sites_grps TYPE=InnoDB;

CREATE TABLE jahia_sites_users (
    username_sites_users VARCHAR (50) NOT NULL,
    siteid_sites_users INTEGER NOT NULL,
    userid_sites_users VARCHAR (255),
    PRIMARY KEY(username_sites_users,siteid_sites_users)
);
ALTER TABLE jahia_sites_users TYPE=InnoDB;

CREATE TABLE jahia_slideindexingjob (
    id_slideindexingjob VARCHAR(50) NOT NULL,
    date_slideindexingjob BIGINT,
    ns_slideindexingjob VARCHAR(50),
    uri_slideindexingjob VARCHAR(255),
    content_slideindexingjob BIT,
    PRIMARY KEY (id_slideindexingjob)
);
ALTER TABLE jahia_slideindexingjob TYPE=InnoDB;

CREATE TABLE jahia_slideindexjobserver (
    indexjob_id VARCHAR(50) NOT NULL,
    server_id VARCHAR(255),
    process_date BIGINT,
    PRIMARY KEY(indexjob_id)
);
ALTER TABLE jahia_slideindexingjobserver TYPE=InnoDB;

CREATE TABLE jahia_statistics (
    id_jahia_statistics integer NOT NULL,
    serverid_jahia_statistics VARCHAR(255) NOT NULL,
    datetime_jahia_statistics DATETIME,
    username_jahia_statistics VARCHAR(255),
    parentid_jahia_statistics VARCHAR(255),
    eventtype_jahia_statistics VARCHAR(255),
    eventinfo_jahia_statistics BLOB,
    siteid_jahia_statistics VARCHAR(255),
    objecttype_jahia_statistics VARCHAR(255),
    objectid_jahia_statistics VARCHAR(255),
    actionid_jahia_statistics VARCHAR(255),
    systemid_jahia_statistics VARCHAR(255),
    year_jahia_statistics INTEGER,
    month_jahia_statistics INTEGER,
    yearmonth_jahia_statistics INTEGER,
    week_jahia_statistics INTEGER,
    day_jahia_statistics INTEGER,
    dayofweek_jahia_statistics INTEGER,
    level0_jahia_statistics VARCHAR(255),
    level1_jahia_statistics VARCHAR(255),
    level2_jahia_statistics VARCHAR(255),
    level3_jahia_statistics VARCHAR(255),
    level4_jahia_statistics VARCHAR(255),
    level5_jahia_statistics VARCHAR(255),
    PRIMARY KEY(id_jahia_statistics, serverid_jahia_statistics)
);
ALTER TABLE jahia_statistics TYPE=InnoDB;

CREATE TABLE jahia_user_prop (
    id_jahia_users INTEGER NOT NULL,
    name_jahia_user_prop VARCHAR (150) NOT NULL,
    provider_jahia_user_prop VARCHAR (50) NOT NULL,
    userkey_jahia_user_prop VARCHAR (50) NOT NULL,
    value_jahia_user_prop VARCHAR(255),    
    PRIMARY KEY(id_jahia_users,name_jahia_user_prop,provider_jahia_user_prop,userkey_jahia_user_prop)
);
ALTER TABLE jahia_user_prop TYPE=InnoDB;

CREATE TABLE jahia_users (
    id_jahia_users INTEGER NOT NULL,
    name_jahia_users VARCHAR (255),
    password_jahia_users VARCHAR (255),
    key_jahia_users VARCHAR (50) NOT NULL UNIQUE,
    PRIMARY KEY(id_jahia_users)
);
ALTER TABLE jahia_users TYPE=InnoDB;

CREATE TABLE jahia_version (
    install_number INTEGER NOT NULL,
    build INTEGER,
    release_number VARCHAR(20),
    install_date DATETIME,
    primary key (install_number)
);
ALTER TABLE jahia_version TYPE=InnoDB;  

CREATE TABLE jahia_workflow (
    OBJECTKEY VARCHAR (255) NOT NULL,
    MODEVALUE INTEGER,
    EXTERNALNAME VARCHAR (255),
    EXTERNALPROCESS VARCHAR (255),
    MAINOBJECTKEY VARCHAR (255),
    PRIMARY KEY(OBJECTKEY)
);
ALTER TABLE jahia_workflow TYPE=InnoDB;  

alter table jahia_acl_entries 
    add index FKDBE729858C498B01 (id_jahia_acl), 
    add constraint FKDBE729858C498B01 
    foreign key (id_jahia_acl) 
    references jahia_acl (id_jahia_acl);
alter table jahia_acl_names 
    add index FK3C5F357D48212BE1 (acl_id), 
    add constraint FK3C5F357D48212BE1 
    foreign key (acl_id) 
    references jahia_acl (id_jahia_acl);
alter table jahia_app_def 
    add index FKE6C84991455C901A (rights_jahia_app_def), 
    add constraint FKE6C84991455C901A 
    foreign key (rights_jahia_app_def) 
    references jahia_acl (id_jahia_acl);
alter table jahia_appdef_prop 
    add index FK8D4A0C287FCEE0FB (appdefid_appdef_prop), 
    add constraint FK8D4A0C287FCEE0FB 
    foreign key (appdefid_appdef_prop) 
    references jahia_app_def (id_jahia_app_def);
alter table jahia_apps_share 
    add index FK7FE512E81F78D07 (site), 
    add constraint FK7FE512E81F78D07 
    foreign key (site) 
    references jahia_sites (id_jahia_sites);
alter table jahia_apps_share 
    add index FK7FE512E83D3F9BB0 (definition), 
    add constraint FK7FE512E83D3F9BB0 
    foreign key (definition) 
    references jahia_app_def (id_jahia_app_def);
alter table jahia_audit_log 
    add index FKF4669B0AC75E28FD (parent_id_jahia_audit_log), 
    add constraint FKF4669B0AC75E28FD 
    foreign key (parent_id_jahia_audit_log) 
    references jahia_audit_log (id_jahia_audit_log);
alter table jahia_category 
    add index FKAFAF3D94348193C9 (aclid_category), 
    add constraint FKAFAF3D94348193C9 
    foreign key (aclid_category) 
    references jahia_acl (id_jahia_acl);
alter table jahia_ctn_def_properties 
    add index FKA2522525B1CEB792 (ctndefid_jahia_ctn_def_prop), 
    add constraint FKA2522525B1CEB792 
    foreign key (ctndefid_jahia_ctn_def_prop) 
    references jahia_ctn_def (id_jahia_ctn_def);
alter table jahia_ctn_entries 
    add index FK6719F01827313F9D (ctndefid_jahia_ctn_entries), 
    add constraint FK6719F01827313F9D 
    foreign key (ctndefid_jahia_ctn_entries) 
    references jahia_ctn_def (id_jahia_ctn_def);
alter table jahia_ctn_lists 
    add index FKB490111D2A3FB9A2 (ctndefid_jahia_ctn_lists), 
    add constraint FKB490111D2A3FB9A2 
    foreign key (ctndefid_jahia_ctn_lists) 
    references jahia_ctn_def (id_jahia_ctn_def);
alter table jahia_ctn_struct 
    add index FKE9FE8F4D62299E65 (ctnsubdefid_jahia_ctn_struct), 
    add constraint FKE9FE8F4D62299E65 
    foreign key (ctnsubdefid_jahia_ctn_struct) 
    references jahia_ctn_def_properties (id_jahia_ctn_def_properties);
alter table jahia_ctndef_prop 
    add index FK5FB05824447AD44A (id_jahia_ctn_def), 
    add constraint FK5FB05824447AD44A 
    foreign key (id_jahia_ctn_def) 
    references jahia_ctn_def (id_jahia_ctn_def);
alter table jahia_fields_data 
    add index FK891B251A291A9BF4 (fielddefid_jahia_fields_data), 
    add constraint FK891B251A291A9BF4 
    foreign key (fielddefid_jahia_fields_data) 
    references jahia_fields_def (id_jahia_fields_def);
alter table jahia_fields_def 
    add index FK5700E095767D5370 (jahiaid_jahia_fields_def), 
    add constraint FK5700E095767D5370 
    foreign key (jahiaid_jahia_fields_def) 
    references jahia_sites (id_jahia_sites);
alter table jahia_fields_def_extprop 
    add index FKC2B7575A477EEAEC (id_jahia_fields_def), 
    add constraint FKC2B7575A477EEAEC 
    foreign key (id_jahia_fields_def) 
    references jahia_fields_def (id_jahia_fields_def);
alter table jahia_grps 
    add index FKE530C7C492027B41 (siteid_jahia_grps), 
    add constraint FKE530C7C492027B41 
    foreign key (siteid_jahia_grps) 
    references jahia_sites (id_jahia_sites);
alter table jahia_nstep_workflowinstance 
    add index FKDA6D7CCFA3F4D577 (workflow), 
    add constraint FKDA6D7CCFA3F4D577 
    foreign key (workflow) 
    references jahia_nstep_workflow (id);
alter table jahia_nstep_workflowinstance 
    add index FKDA6D7CCF801AE453 (user_id), 
    add constraint FKDA6D7CCF801AE453 
    foreign key (user_id) 
    references jahia_nstep_workflowuser (id);
alter table jahia_nstep_workflowinstance 
    add index FKDA6D7CCFED90E370 (step), 
    add constraint FKDA6D7CCFED90E370 
    foreign key (step) 
    references jahia_nstep_workflowstep (id);
alter table jahia_nstep_workflowstep 
    add index FK6A6E1C067F20B53 (workflow_id), 
    add constraint FK6A6E1C067F20B53 
    foreign key (workflow_id) 
    references jahia_nstep_workflow (id);
alter table jahia_obj 
    add index FKF6E0A6A143AACCE0 (retrule_jahia_obj), 
    add constraint FKF6E0A6A143AACCE0 
    foreign key (retrule_jahia_obj) 
    references jahia_retrule (id_jahia_retrule);
alter table jahia_pages_def
    add index FK1EA2B334B5FF0C79 (jahiaid_jahia_pages_def), 
    add constraint FK1EA2B334B5FF0C79 
    foreign key (jahiaid_jahia_pages_def) 
    references jahia_sites (id_jahia_sites);
alter table jahia_pages_def_prop 
    add index FK8840898E47E25CC (id_jahia_pages_def_prop), 
    add constraint FK8840898E47E25CC 
    foreign key (id_jahia_pages_def_prop) 
    references jahia_pages_def (id_jahia_pages_def);
alter table jahia_pwd_policy_rule_params 
    add index FKBE451EF45A0DB19B (jahia_pwd_policy_rule_id), 
    add constraint FKBE451EF45A0DB19B 
    foreign key (jahia_pwd_policy_rule_id) 
    references jahia_pwd_policy_rules (jahia_pwd_policy_rule_id);
alter table jahia_pwd_policy_rules 
    add index FK2BC650026DA1D1E6 (jahia_pwd_policy_id), 
    add constraint FK2BC650026DA1D1E6 
    foreign key (jahia_pwd_policy_id) 
    references jahia_pwd_policies (jahia_pwd_policy_id);
alter table jahia_retrule 
    add index FK578E2BC72D76FCE6 (id_jahia_retruledef), 
    add constraint FK578E2BC72D76FCE6 
    foreign key (id_jahia_retruledef) 
    references jahia_retruledef (id_jahia_retruledef);
alter table jahia_retrule_range 
    add index FK688A96C57D611258 (id_retrule_range), 
    add constraint FK688A96C57D611258 
    foreign key (id_retrule_range) 
    references jahia_retrule (id_jahia_retrule);
alter table jahia_site_lang_list 
    add index FK1DDBC16D7EED26D3 (site_id), 
    add constraint FK1DDBC16D7EED26D3 
    foreign key (site_id) 
    references jahia_sites (id_jahia_sites);
alter table jahia_site_lang_maps 
    add index FK1DDC17667EED26D3 (site_id), 
    add constraint FK1DDC17667EED26D3 
    foreign key (site_id) 
    references jahia_sites (id_jahia_sites);
alter table jahia_sites_grps 
    add index FK7B245597F46755FE (siteid_sites_grps), 
    add constraint FK7B245597F46755FE 
    foreign key (siteid_sites_grps) 
    references jahia_sites (id_jahia_sites);
alter table jahia_slideindexjobserver
    add index FK17554649A2228D92 (indexjob_id), 
    add constraint FK17554649A2228D92 
    foreign key (indexjob_id) 
    references jahia_slideindexingjob (id_slideindexingjob);