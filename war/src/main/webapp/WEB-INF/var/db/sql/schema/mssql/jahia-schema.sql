
    alter table jahia_acl_entries 
        drop constraint FKDBE729858C498B01;

    alter table jahia_acl_names 
        drop constraint FK3C5F357D48212BE1;

    alter table jahia_app_def 
        drop constraint FKE6C84991455C901A;

    alter table jahia_appdef_prop 
        drop constraint FK8D4A0C287FCEE0FB;

    alter table jahia_apps_share 
        drop constraint FK7FE512E81F78D07;

    alter table jahia_apps_share 
        drop constraint FK7FE512E83D3F9BB0;

    alter table jahia_audit_log 
        drop constraint FKF4669B0AC75E28FD;

    alter table jahia_category 
        drop constraint FKAFAF3D94348193C9;

    alter table jahia_ctn_def_properties 
        drop constraint FKA2522525B1CEB792;

    alter table jahia_ctn_entries 
        drop constraint FK6719F01827313F9D;

    alter table jahia_ctn_lists 
        drop constraint FKB490111D2A3FB9A2;

    alter table jahia_ctn_struct 
        drop constraint FKE9FE8F4D62299E65;

    alter table jahia_ctndef_prop 
        drop constraint FK5FB05824447AD44A;

    alter table jahia_fields_data 
        drop constraint FK891B251A291A9BF4;

    alter table jahia_fields_def_extprop 
        drop constraint FKC2B7575A477EEAEC;

    alter table jahia_grps 
        drop constraint FKE530C7C492027B41;

    alter table jahia_nstep_workflowinstance 
        drop constraint FKDA6D7CCF801AE453;

    alter table jahia_nstep_workflowinstance 
        drop constraint FKDA6D7CCFA3F4D577;

    alter table jahia_nstep_workflowinstance 
        drop constraint FKDA6D7CCFED90E370;

    alter table jahia_nstep_workflowstep 
        drop constraint FK6A6E1C067F20B53;

    alter table jahia_obj 
        drop constraint FKF6E0A6A143AACCE0;

    alter table jahia_pages_data 
        drop constraint FKB5B3A65BFC25DDC3;

    alter table jahia_pages_def 
        drop constraint FK1EA2B334B5FF0C79;

    alter table jahia_pages_def_prop 
        drop constraint FK8840898E47E25CC;

    alter table jahia_pwd_policy_rule_params 
        drop constraint FKBE451EF45A0DB19B;

    alter table jahia_pwd_policy_rules 
        drop constraint FK2BC650026DA1D1E6;

    alter table jahia_retrule 
        drop constraint FK578E2BC72D76FCE6;

    alter table jahia_retrule_range 
        drop constraint FK688A96C57D611258;

    alter table jahia_savedsearch 
        drop constraint FK782342991382CE9E;

    alter table jahia_savedsearch 
        drop constraint FK78234299ACC6817;

    alter table jahia_site_lang_list 
        drop constraint FK1DDBC16D7EED26D3;

    alter table jahia_site_lang_maps 
        drop constraint FK1DDC17667EED26D3;

    alter table jahia_sites_grps 
        drop constraint FK7B245597F46755FE;

    alter table jahia_sites_grps 
        drop constraint FK7B24559790F996AC;

    alter table jahia_sites_users 
        drop constraint FKEA2BF1BF6CF683C0;

    drop table jahia_acl;

    drop table jahia_acl_entries;

    drop table jahia_acl_names;

    drop table jahia_app_def;

    drop table jahia_appdef_prop;

    drop table jahia_appentry;

    drop table jahia_apps_share;

    drop table jahia_audit_log;

    drop table jahia_bigtext_data;

    drop table jahia_category;

    drop table jahia_category_prop;

    drop table jahia_ctn_def;

    drop table jahia_ctn_def_properties;

    drop table jahia_ctn_entries;

    drop table jahia_ctn_lists;

    drop table jahia_ctn_struct;

    drop table jahia_ctndef_prop;

    drop table jahia_ctnentries_prop;

    drop table jahia_ctnlists_prop;

    drop table jahia_db_test;

    drop table jahia_fieldreference;

    drop table jahia_fields_data;

    drop table jahia_fields_def;

    drop table jahia_fields_def_extprop;

    drop table jahia_fields_prop;

    drop table jahia_grp_access;

    drop table jahia_grp_prop;

    drop table jahia_grps;

    drop table jahia_indexingjobs;

    drop table jahia_indexingjobsserver;

    drop table jahia_installedpatch;

    drop table jahia_languages_states;

    drop table jahia_link;

    drop table jahia_link_metadata;

    drop table jahia_locks_non_excl;

    drop table jahia_nstep_workflow;

    drop table jahia_nstep_workflowhistory;

    drop table jahia_nstep_workflowinstance;

    drop table jahia_nstep_workflowstep;

    drop table jahia_nstep_workflowuser;

    drop table jahia_obj;

    drop table jahia_pages_data;

    drop table jahia_pages_def;

    drop table jahia_pages_def_prop;

    drop table jahia_pages_prop;

    drop table jahia_pages_users_prop;

    drop table jahia_pwd_policies;

    drop table jahia_pwd_policy_rule_params;

    drop table jahia_pwd_policy_rules;

    drop table jahia_reference;

    drop table jahia_resources;

    drop table jahia_retrule;

    drop table jahia_retrule_range;

    drop table jahia_retruledef;

    drop table jahia_savedsearch;

    drop table jahia_savedsearchview;

    drop table jahia_serverprops;

    drop table jahia_site_lang_list;

    drop table jahia_site_lang_maps;

    drop table jahia_site_prop;

    drop table jahia_sites;

    drop table jahia_sites_grps;

    drop table jahia_sites_users;

    drop table jahia_subscriptions;

    drop table jahia_user_prop;

    drop table jahia_users;

    drop table jahia_version;

    drop table jahia_workflow;

    create table jahia_acl (
        id_jahia_acl int not null,
        inheritance_jahia_acl int null,
        hasentries_jahia_acl int null,
        parent_id_jahia_acl int null,
        picked_id_jahia_acl int null,
        primary key (id_jahia_acl)
    );

    create table jahia_acl_entries (
        id_jahia_acl int not null,
        type_jahia_acl_entries int not null,
        target_jahia_acl_entries nvarchar(50) not null,
        entry_state_jahia_acl_entries int not null,
        entry_trist_jahia_acl_entries int not null,
        primary key (id_jahia_acl, type_jahia_acl_entries, target_jahia_acl_entries)
    );

    create table jahia_acl_names (
        acl_name nvarchar(255) not null,
        acl_id int null unique,
        primary key (acl_name)
    );

    create table jahia_app_def (
        id_jahia_app_def int not null,
        name_jahia_app_def nvarchar(250) null,
        context_jahia_app_def nvarchar(250) null,
        visible_jahia_app_def int null,
        shared_jahia_app_def int null,
        rights_jahia_app_def int null,
        filename_jahia_app_def nvarchar(250) null,
        desc_jahia_app_def nvarchar(250) null,
        type_jahia_app_def nvarchar(30) null,
        primary key (id_jahia_app_def)
    );

    create table jahia_appdef_prop (
        appdefid_appdef_prop int not null,
        propname_appdef_prop nvarchar(250) not null,
        propvalue_appdef_prop nvarchar(250) null,
        primary key (appdefid_appdef_prop, propname_appdef_prop)
    );

    create table jahia_appentry (
        id_jahia_appentry int not null,
        appid_jahia_appentry int null,
        defname_jahia_appentry nvarchar(250) null,
        rights_jahia_appentry int null,
        reskeyname_jahia_appentry nvarchar(250) null,
        expirationTime_jahia_appentry int null,
        cacheScope_jahia_appentry nvarchar(250) null,
        primary key (id_jahia_appentry)
    );

    create table jahia_apps_share (
        definition int not null,
        site int not null,
        primary key (definition, site)
    );

    create table jahia_audit_log (
        id_jahia_audit_log int not null,
        time_jahia_audit_log numeric(19,0) null,
        username_jahia_audit_log nvarchar(50) null,
        objecttype_jahia_audit_log int null,
        objectid_jahia_audit_log int null,
        parenttype_jahia_audit_log int null,
        parentid_jahia_audit_log int null,
        operation_jahia_audit_log nvarchar(50) null,
        site_jahia_audit_log nvarchar(50) null,
        content_jahia_audit_log nvarchar(250) null,
        parent_id_jahia_audit_log int null,
        eventType nvarchar(255) null,
        eventInformation image null,
        primary key (id_jahia_audit_log)
    );

    create table jahia_bigtext_data (
        id_bigtext_data nvarchar(255) not null,
        raw_value ntext null,
        primary key (id_bigtext_data)
    );

    create table jahia_category (
        id_category int not null,
        key_category nvarchar(250) not null unique,
        aclid_category int null,
        primary key (id_category)
    );

    create table jahia_category_prop (
        id_category int not null,
        name_category_prop nvarchar(250) not null,
        value_category_prop nvarchar(250) not null,
        primary key (id_category, name_category_prop)
    );

    create table jahia_ctn_def (
        id_jahia_ctn_def int not null,
        jahiaid_jahia_ctn_def int null,
        name_jahia_ctn_def nvarchar(250) null,
        pctnname_jahia_ctndef_def nvarchar(250) null,
        ctntype_jahia_ctn_def nvarchar(150) null,
        primary key (id_jahia_ctn_def)
    );

    create table jahia_ctn_def_properties (
        id_jahia_ctn_def_properties int not null,
        ctndefid_jahia_ctn_def_prop int not null,
        pagedefid_jahia_ctn_def_prop int null,
        primary key (id_jahia_ctn_def_properties)
    );

    create table jahia_ctn_entries (
        id_jahia_ctn_entries int not null,
        version_id int not null,
        workflow_state int not null,
        ctndefid_jahia_ctn_entries int null,
        rights_jahia_ctn_entries int null,
        listid_jahia_ctn_entries int null,
        pageid_jahia_ctn_entries int null,
        rank_jahia_ctn_entries int null,
        jahiaid_jahia_ctn_entries int null,
        primary key (id_jahia_ctn_entries, version_id, workflow_state)
    );

    create table jahia_ctn_lists (
        id_jahia_ctn_lists int not null,
        version_id int not null,
        workflow_state int not null,
        pageid_jahia_ctn_lists int null,
        parententryid_jahia_ctn_lists int null,
        ctndefid_jahia_ctn_lists int null,
        rights_jahia_ctn_lists int null,
        primary key (id_jahia_ctn_lists, version_id, workflow_state)
    );

    create table jahia_ctn_struct (
        ctnsubdefid_jahia_ctn_struct int not null,
        objtype_jahia_ctn_struct int not null,
        objdefid_jahia_ctn_struct int not null,
        rank_jahia_ctn_struct int null,
        primary key (ctnsubdefid_jahia_ctn_struct, objtype_jahia_ctn_struct, objdefid_jahia_ctn_struct)
    );

    create table jahia_ctndef_prop (
        id_jahia_ctn_def int not null,
        name_jahia_ctndef_prop nvarchar(255) not null,
        value_jahia_ctndef_prop nvarchar(255) null,
        primary key (id_jahia_ctn_def, name_jahia_ctndef_prop)
    );

    create table jahia_ctnentries_prop (
        ctnid_ctnentries_prop int not null,
        name_ctnentries_prop nvarchar(255) not null,
        jahiaid_ctnentries_prop int null,
        value_ctnentries_prop nvarchar(255) null,
        primary key (ctnid_ctnentries_prop, name_ctnentries_prop)
    );

    create table jahia_ctnlists_prop (
        ctnlistid_ctnlists_prop int not null,
        name_ctnlists_prop nvarchar(255) not null,
        jahiaid_ctnlists_prop int null,
        value_ctnlists_prop nvarchar(255) null,
        primary key (ctnlistid_ctnlists_prop, name_ctnlists_prop)
    );

    create table jahia_db_test (
        testfield nvarchar(255) not null,
        primary key (testfield)
    );

    create table jahia_fieldreference (
        fieldId int not null,
        language nvarchar(10) not null,
        workflow int not null,
        target nvarchar(255) not null,
        siteId int null,
        primary key (fieldId, language, workflow, target)
    );

    create table jahia_fields_data (
        id_jahia_fields_data int not null,
        version_id int not null,
        workflow_state int not null,
        language_code nvarchar(10) not null,
        connecttype_jahia_fields_data int null,
        ctnid_jahia_fields_data int null,
        fielddefid_jahia_fields_data int null,
        id_jahia_obj int null,
        type_jahia_obj nvarchar(22) null,
        rights_jahia_fields_data int null,
        pageid_jahia_fields_data int null,
        jahiaid_jahia_fields_data int null,
        type_jahia_fields_data int null,
        value_jahia_fields_data nvarchar(250) null,
        primary key (id_jahia_fields_data, version_id, workflow_state, language_code)
    );

    create table jahia_fields_def (
        id_jahia_fields_def int not null,
        ismdata_jahia_fields_def int null,
        jahiaid_jahia_fields_def int null,
        ctnname_jahia_fields_def nvarchar(250) null,
        name_jahia_fields_def nvarchar(250) null,
        primary key (id_jahia_fields_def)
    );

    create table jahia_fields_def_extprop (
        id_jahia_fields_def int not null,
        prop_name nvarchar(200) not null,
        prop_value nvarchar(255) null,
        primary key (id_jahia_fields_def, prop_name)
    );

    create table jahia_fields_prop (
        fieldid_jahia_fields_prop int not null,
        propertyname_jahia_fields_prop nvarchar(250) not null,
        propvalue_jahia_fields_prop nvarchar(50) null,
        primary key (fieldid_jahia_fields_prop, propertyname_jahia_fields_prop)
    );

    create table jahia_grp_access (
        id_jahia_member nvarchar(150) not null,
        id_jahia_grps nvarchar(150) not null,
        membertype_grp_access int not null,
        primary key (id_jahia_member, id_jahia_grps, membertype_grp_access)
    );

    create table jahia_grp_prop (
        id_jahia_grp int not null,
        name_jahia_grp_prop nvarchar(50) not null,
        provider_jahia_grp_prop nvarchar(50) not null,
        grpkey_jahia_grp_prop nvarchar(200) not null,
        value_jahia_grp_prop nvarchar(255) null,
        primary key (id_jahia_grp, name_jahia_grp_prop, provider_jahia_grp_prop, grpkey_jahia_grp_prop)
    );

    create table jahia_grps (
        id_jahia_grps int not null,
        name_jahia_grps nvarchar(195) null,
        key_jahia_grps nvarchar(200) null unique,
        siteid_jahia_grps int null,
        primary key (id_jahia_grps)
    );

    create table jahia_indexingjobs (
        id_indexingjob nvarchar(50) not null,
        classname_indexingjob nvarchar(255) not null,
        date_indexingjob numeric(19,0) null,
        indeximmdty_indexingjob tinyint null,
        ruleId_indexingjob int null,
        fromtime1_indexingjob int null,
        totime1_indexingjob int null,
        fromtime2_indexingjob int null,
        totime2_indexingjob int null,
        fromtime3_indexingjob int null,
        totime3_indexingjob int null,
        enableserver_indexingjob nvarchar(100) null,
        ctnlistid_indexingjob int null,
        ctnid_indexingjob int null,
        siteid_indexingjob int null,
        keyname_indexingjob nvarchar(255) null,
        keyvalue_indexingjob nvarchar(255) null,
        pageid_indexingjob int null,
        fieldid_indexingjob int null,
        primary key (id_indexingjob)
    );

    create table jahia_indexingjobsserver (
        serverid nvarchar(200) not null,
        indexingjobid nvarchar(50) not null,
        indexing_date numeric(19,0) null,
        primary key (serverid, indexingjobid)
    );

    create table jahia_installedpatch (
        install_number int not null,
        name nvarchar(100) null,
        build int null,
        result_code int null,
        install_date datetime null,
        primary key (install_number)
    );

    create table jahia_languages_states (
        objectkey nvarchar(40) not null,
        language_code nvarchar(10) not null,
        workflow_state int null,
        siteid int null,
        primary key (objectkey, language_code)
    );

    create table jahia_link (
        id int not null,
        left_oid nvarchar(100) null,
        right_oid nvarchar(100) null,
        type nvarchar(100) null,
        primary key (id)
    );

    create table jahia_link_metadata (
        link_id int not null,
        link_position nvarchar(20) not null,
        property_name nvarchar(255) not null,
        property_value nvarchar(255) null,
        primary key (link_id, link_position, property_name)
    );

    create table jahia_locks_non_excl (
        name_locks nvarchar(50) not null,
        targetid_locks int not null,
        action_locks nvarchar(50) not null,
        context_locks nvarchar(80) not null,
        owner_locks nvarchar(50) null,
        timeout_locks int null,
        expirationDate_locks numeric(19,0) null,
        serverid_locks nvarchar(30) null,
        stolen_locks nvarchar(10) null,
        primary key (name_locks, targetid_locks, action_locks, context_locks)
    );

    create table jahia_nstep_workflow (
        id numeric(19,0) not null,
        name nvarchar(255) not null,
        primary key (id)
    );

    create table jahia_nstep_workflowhistory (
        id numeric(19,0) not null,
        action nvarchar(255) not null,
        author nvarchar(255) not null,
        message ntext null,
        actionDate datetime not null,
        languageCode nvarchar(255) not null,
        objectKey nvarchar(255) not null,
        process nvarchar(255) not null,
        username nvarchar(255) not null,
        primary key (id)
    );

    create table jahia_nstep_workflowinstance (
        id numeric(19,0) not null,
        authorEmail nvarchar(255) null,
        languageCode nvarchar(255) not null,
        objectKey nvarchar(255) not null,
        step numeric(19,0) null,
        user_id numeric(19,0) null,
        workflow numeric(19,0) null,
        startDate datetime null,
        primary key (id)
    );

    create table jahia_nstep_workflowstep (
        id numeric(19,0) not null,
        name nvarchar(255) not null,
        workflow_id numeric(19,0) null,
        step_index int null,
        primary key (id)
    );

    create table jahia_nstep_workflowuser (
        id numeric(19,0) not null,
        login nvarchar(255) not null,
        primary key (id)
    );

    create table jahia_obj (
        id_jahia_obj int not null,
        type_jahia_obj nvarchar(22) not null,
        jahiaid_jahia_obj int null,
        timebpstate_jahia_obj int null,
        validfrom_jahia_obj numeric(19,0) null,
        validto_jahia_obj numeric(19,0) null,
        retrule_jahia_obj int null,
        primary key (id_jahia_obj, type_jahia_obj)
    );

    create table jahia_pages_data (
        id_jahia_pages_data int not null,
        version_id int not null,
        workflow_state int not null,
        language_code nvarchar(10) not null,
        rights_jahia_pages_data int null,
        pagelinkid_jahia_pages_data int null,
        pagetype_jahia_pages_data int null,
        pagedefid_jahia_pages_data int null,
        parentid_jahia_pages_data int null,
        remoteurl_jahia_pages_data nvarchar(250) null,
        jahiaid_jahia_pages_data int null,
        title_jahia_pages_data nvarchar(250) null,
        primary key (id_jahia_pages_data, version_id, workflow_state, language_code)
    );

    create table jahia_pages_def (
        id_jahia_pages_def int not null,
        jahiaid_jahia_pages_def int null,
        name_jahia_pages_def nvarchar(250) null,
        sourcepath_jahia_pages_def nvarchar(250) null,
        visible_jahia_pages_def tinyint null,
        browsable_jahia_pages_def int null,
        warning_msg_jahia_pages_def nvarchar(250) null,
        img_jahia_pages_def nvarchar(150) null,
        pagetype_jahia_pages_def nvarchar(150) null,
        primary key (id_jahia_pages_def)
    );

    create table jahia_pages_def_prop (
        id_jahia_pages_def_prop int not null,
        jahiaid_pages_def_prop int not null,
        name_pages_def_prop nvarchar(100) not null,
        value_pages_def_prop nvarchar(200) null,
        primary key (id_jahia_pages_def_prop, jahiaid_pages_def_prop, name_pages_def_prop)
    );

    create table jahia_pages_prop (
        page_id int not null,
        prop_name nvarchar(150) not null,
        language_code nvarchar(100) not null,
        prop_value nvarchar(255) null,
        primary key (page_id, prop_name, language_code)
    );

    create table jahia_pages_users_prop (
        page_id int not null,
        principal_key nvarchar(70) not null,
        principal_type nvarchar(40) not null,
        prop_type nvarchar(40) not null,
        prop_name nvarchar(150) not null,
        prop_value nvarchar(255) null,
        primary key (page_id, principal_key, principal_type, prop_type, prop_name)
    );

    create table jahia_pwd_policies (
        jahia_pwd_policy_id int not null,
        name nvarchar(255) not null,
        primary key (jahia_pwd_policy_id)
    );

    create table jahia_pwd_policy_rule_params (
        jahia_pwd_policy_rule_param_id int not null,
        name nvarchar(50) not null,
        position_index int not null,
        jahia_pwd_policy_rule_id int not null,
        type char(1) not null,
        value nvarchar(255) null,
        primary key (jahia_pwd_policy_rule_param_id)
    );

    create table jahia_pwd_policy_rules (
        jahia_pwd_policy_rule_id int not null,
        action char(1) not null,
        rule_condition ntext not null,
        evaluator char(1) not null,
        name nvarchar(255) not null,
        jahia_pwd_policy_id int not null,
        position_index int not null,
        active tinyint not null,
        last_rule tinyint not null,
        periodical tinyint not null,
        primary key (jahia_pwd_policy_rule_id)
    );

    create table jahia_reference (
        page_id int not null,
        ref_id int not null,
        ref_type int not null,
        primary key (page_id, ref_id, ref_type)
    );

    create table jahia_resources (
        name_resource nvarchar(200) not null,
        languagecode_resource nvarchar(10) not null,
        value_resource nvarchar(255) null,
        primary key (name_resource, languagecode_resource)
    );

    create table jahia_retrule (
        id_jahia_retrule int not null,
        id_jahia_retruledef int null,
        inherited_retrule tinyint null,
        enabled_retrule tinyint null,
        title_retrule nvarchar(255) null,
        comment_retrule nvarchar(255) null,
        shared_retrule tinyint null,
        settings_retrule ntext null,
        primary key (id_jahia_retrule)
    );

    create table jahia_retrule_range (
        id_retrule_range int not null,
        validfrom_retrule_range numeric(19,0) null,
        validto_retrule_range numeric(19,0) null,
        notiffromd_retrule_range tinyint null,
        notiftod_retrule_range tinyint null,
        primary key (id_retrule_range)
    );

    create table jahia_retruledef (
        id_jahia_retruledef int not null,
        name_retruledef nvarchar(255) null unique,
        title_retruledef nvarchar(255) null,
        ruleclass_retruledef nvarchar(255) null,
        rulehelperclass_retruledef nvarchar(255) null,
        dateformat_retruledef nvarchar(255) null,
        primary key (id_jahia_retruledef)
    );

    create table jahia_savedsearch (
        id_jahia_savedsearch int not null,
        title_jahia_savedsearch nvarchar(255) null,
        descr_jahia_savedsearch nvarchar(250) null,
        search_jahia_savedsearch ntext not null,
        creationdate_jahia_savedsearch numeric(19,0) null,
        owner_jahia_savedsearch nvarchar(255) null,
        class_jahia_savedsearch nvarchar(255) null,
        jahiaid_jahia_savedsearch int null,
        rights_jahia_search int null,
        primary key (id_jahia_savedsearch)
    );

    create table jahia_savedsearchview (
        smode_savedsearchview int not null,
        ctnid_savedsearchview nvarchar(100) not null,
        searchid_savedsearchview int not null,
        userkey_savedsearchview nvarchar(200) not null,
        setting_savedsearchview ntext not null,
        name_savedsearchview nvarchar(100) not null,
        primary key (smode_savedsearchview, ctnid_savedsearchview, searchid_savedsearchview, userkey_savedsearchview)
    );

    create table jahia_serverprops (
        id_serverprops nvarchar(50) not null,
        propname_serverprops nvarchar(200) not null,
        propvalue_serverprops nvarchar(250) not null,
        primary key (id_serverprops, propname_serverprops)
    );

    create table jahia_site_lang_list (
        id int not null,
        site_id int null,
        code nvarchar(255) null,
        rank int null,
        activated tinyint null,
        mandatory tinyint null,
        primary key (id)
    );

    create table jahia_site_lang_maps (
        id int not null,
        site_id int null,
        from_lang_code nvarchar(255) null,
        to_lang_code nvarchar(255) null,
        primary key (id)
    );

    create table jahia_site_prop (
        id_jahia_site int not null,
        name_jahia_site_prop nvarchar(255) not null,
        value_jahia_site_prop nvarchar(255) null,
        primary key (id_jahia_site, name_jahia_site_prop)
    );

    create table jahia_sites (
        id_jahia_sites int not null,
        title_jahia_sites nvarchar(100) null,
        servername_jahia_sites nvarchar(200) null,
        key_jahia_sites nvarchar(50) null unique,
        active_jahia_sites int null,
        defaultpageid_jahia_sites int null,
        defaulttemplateid_jahia_sites int null,
        tpl_deploymode_jahia_sites int null,
        webapps_deploymode_jahia_sites int null,
        rights_jahia_sites int null,
        descr_jahia_sites nvarchar(250) null,
        default_site_jahia_sites tinyint null,
        primary key (id_jahia_sites)
    );

    create table jahia_sites_grps (
        grpname_sites_grps nvarchar(50) not null,
        siteid_sites_grps int not null,
        grpid_sites_grps nvarchar(255) null,
        primary key (grpname_sites_grps, siteid_sites_grps)
    );

    create table jahia_sites_users (
        username_sites_users nvarchar(50) not null,
        siteid_sites_users int not null,
        userid_sites_users nvarchar(255) null,
        primary key (username_sites_users, siteid_sites_users)
    );

    create table jahia_subscriptions (
        id_jahia_subscriptions int not null,
        object_key nvarchar(40) null,
        include_children tinyint not null,
        event_type nvarchar(50) not null,
        channel char(1) not null,
        notification_type char(1) not null,
        username nvarchar(255) not null,
        user_registered tinyint not null,
        site_id int not null,
        enabled tinyint not null,
        suspended tinyint not null,
        confirmation_key nvarchar(32) null,
        confirmation_request_timestamp numeric(19,0) null,
        properties ntext null,
        primary key (id_jahia_subscriptions)
    );

    create table jahia_user_prop (
        id_jahia_users int not null,
        name_jahia_user_prop nvarchar(150) not null,
        provider_jahia_user_prop nvarchar(50) not null,
        userkey_jahia_user_prop nvarchar(50) not null,
        value_jahia_user_prop nvarchar(255) null,
        primary key (id_jahia_users, name_jahia_user_prop, provider_jahia_user_prop, userkey_jahia_user_prop)
    );

    create table jahia_users (
        id_jahia_users int not null,
        name_jahia_users nvarchar(255) null,
        password_jahia_users nvarchar(255) null,
        key_jahia_users nvarchar(50) not null unique,
        primary key (id_jahia_users)
    );

    create table jahia_version (
        install_number int not null,
        build int null,
        release_number nvarchar(20) null,
        install_date datetime null,
        primary key (install_number)
    );

    create table jahia_workflow (
        OBJECTKEY nvarchar(255) not null,
        MODEVALUE int null,
        EXTERNALNAME nvarchar(255) null,
        EXTERNALPROCESS nvarchar(255) null,
        MAINOBJECTKEY nvarchar(255) null,
        primary key (OBJECTKEY)
    );

    alter table jahia_acl_entries 
        add constraint FKDBE729858C498B01 
        foreign key (id_jahia_acl) 
        references jahia_acl;

    alter table jahia_acl_names 
        add constraint FK3C5F357D48212BE1 
        foreign key (acl_id) 
        references jahia_acl;

    alter table jahia_app_def 
        add constraint FKE6C84991455C901A 
        foreign key (rights_jahia_app_def) 
        references jahia_acl;

    alter table jahia_appdef_prop 
        add constraint FK8D4A0C287FCEE0FB 
        foreign key (appdefid_appdef_prop) 
        references jahia_app_def;

    alter table jahia_apps_share 
        add constraint FK7FE512E81F78D07 
        foreign key (site) 
        references jahia_sites;

    alter table jahia_apps_share 
        add constraint FK7FE512E83D3F9BB0 
        foreign key (definition) 
        references jahia_app_def;

    alter table jahia_audit_log 
        add constraint FKF4669B0AC75E28FD 
        foreign key (parent_id_jahia_audit_log) 
        references jahia_audit_log;

    alter table jahia_category 
        add constraint FKAFAF3D94348193C9 
        foreign key (aclid_category) 
        references jahia_acl;

    alter table jahia_ctn_def_properties 
        add constraint FKA2522525B1CEB792 
        foreign key (ctndefid_jahia_ctn_def_prop) 
        references jahia_ctn_def;

    alter table jahia_ctn_entries 
        add constraint FK6719F01827313F9D 
        foreign key (ctndefid_jahia_ctn_entries) 
        references jahia_ctn_def;

    alter table jahia_ctn_lists 
        add constraint FKB490111D2A3FB9A2 
        foreign key (ctndefid_jahia_ctn_lists) 
        references jahia_ctn_def;

    alter table jahia_ctn_struct 
        add constraint FKE9FE8F4D62299E65 
        foreign key (ctnsubdefid_jahia_ctn_struct) 
        references jahia_ctn_def_properties;

    alter table jahia_ctndef_prop 
        add constraint FK5FB05824447AD44A 
        foreign key (id_jahia_ctn_def) 
        references jahia_ctn_def;

    alter table jahia_fields_data 
        add constraint FK891B251A291A9BF4 
        foreign key (fielddefid_jahia_fields_data) 
        references jahia_fields_def;

    alter table jahia_fields_def_extprop 
        add constraint FKC2B7575A477EEAEC 
        foreign key (id_jahia_fields_def) 
        references jahia_fields_def;

    alter table jahia_grps 
        add constraint FKE530C7C492027B41 
        foreign key (siteid_jahia_grps) 
        references jahia_sites;

    alter table jahia_nstep_workflowinstance 
        add constraint FKDA6D7CCF801AE453 
        foreign key (user_id) 
        references jahia_nstep_workflowuser;

    alter table jahia_nstep_workflowinstance 
        add constraint FKDA6D7CCFA3F4D577 
        foreign key (workflow) 
        references jahia_nstep_workflow;

    alter table jahia_nstep_workflowinstance 
        add constraint FKDA6D7CCFED90E370 
        foreign key (step) 
        references jahia_nstep_workflowstep;

    alter table jahia_nstep_workflowstep 
        add constraint FK6A6E1C067F20B53 
        foreign key (workflow_id) 
        references jahia_nstep_workflow;

    alter table jahia_obj 
        add constraint FKF6E0A6A143AACCE0 
        foreign key (retrule_jahia_obj) 
        references jahia_retrule;

    alter table jahia_pages_data 
        add constraint FKB5B3A65BFC25DDC3 
        foreign key (pagedefid_jahia_pages_data) 
        references jahia_pages_def;

    alter table jahia_pages_def 
        add constraint FK1EA2B334B5FF0C79 
        foreign key (jahiaid_jahia_pages_def) 
        references jahia_sites;

    alter table jahia_pages_def_prop 
        add constraint FK8840898E47E25CC 
        foreign key (id_jahia_pages_def_prop) 
        references jahia_pages_def;

    alter table jahia_pwd_policy_rule_params 
        add constraint FKBE451EF45A0DB19B 
        foreign key (jahia_pwd_policy_rule_id) 
        references jahia_pwd_policy_rules;

    alter table jahia_pwd_policy_rules 
        add constraint FK2BC650026DA1D1E6 
        foreign key (jahia_pwd_policy_id) 
        references jahia_pwd_policies;

    alter table jahia_retrule 
        add constraint FK578E2BC72D76FCE6 
        foreign key (id_jahia_retruledef) 
        references jahia_retruledef;

    alter table jahia_retrule_range 
        add constraint FK688A96C57D611258 
        foreign key (id_retrule_range) 
        references jahia_retrule;

    alter table jahia_savedsearch 
        add constraint FK782342991382CE9E 
        foreign key (jahiaid_jahia_savedsearch) 
        references jahia_sites;

    alter table jahia_savedsearch 
        add constraint FK78234299ACC6817 
        foreign key (rights_jahia_search) 
        references jahia_acl;

    alter table jahia_site_lang_list 
        add constraint FK1DDBC16D7EED26D3 
        foreign key (site_id) 
        references jahia_sites;

    alter table jahia_site_lang_maps 
        add constraint FK1DDC17667EED26D3 
        foreign key (site_id) 
        references jahia_sites;

    alter table jahia_sites_grps 
        add constraint FK7B245597F46755FE 
        foreign key (siteid_sites_grps) 
        references jahia_sites;

    alter table jahia_sites_grps 
        add constraint FK7B24559790F996AC 
        foreign key (grpid_sites_grps) 
        references jahia_grps (key_jahia_grps);

    alter table jahia_sites_users 
        add constraint FKEA2BF1BF6CF683C0 
        foreign key (userid_sites_users) 
        references jahia_users (key_jahia_users);
