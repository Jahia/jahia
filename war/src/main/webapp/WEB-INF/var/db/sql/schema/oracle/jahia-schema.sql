
    drop table jahia_acl cascade constraints;

    drop table jahia_acl_entries cascade constraints;

    drop table jahia_acl_names cascade constraints;

    drop table jahia_audit_log cascade constraints;

    drop table jahia_bigtext_data cascade constraints;

    drop table jahia_ctn_def cascade constraints;

    drop table jahia_ctn_def_properties cascade constraints;

    drop table jahia_ctn_entries cascade constraints;

    drop table jahia_ctn_lists cascade constraints;

    drop table jahia_ctn_struct cascade constraints;

    drop table jahia_ctndef_prop cascade constraints;

    drop table jahia_ctnentries_prop cascade constraints;

    drop table jahia_ctnlists_prop cascade constraints;

    drop table jahia_db_test cascade constraints;

    drop table jahia_fieldreference cascade constraints;

    drop table jahia_fields_data cascade constraints;

    drop table jahia_fields_def cascade constraints;

    drop table jahia_fields_def_extprop cascade constraints;

    drop table jahia_fields_prop cascade constraints;

    drop table jahia_grp_access cascade constraints;

    drop table jahia_grp_prop cascade constraints;

    drop table jahia_grps cascade constraints;

    drop table jahia_installedpatch cascade constraints;

    drop table jahia_languages_states cascade constraints;

    drop table jahia_link cascade constraints;

    drop table jahia_link_metadata cascade constraints;

    drop table jahia_locks_non_excl cascade constraints;

    drop table jahia_nstep_workflow cascade constraints;

    drop table jahia_nstep_workflowhistory cascade constraints;

    drop table jahia_nstep_workflowinstance cascade constraints;

    drop table jahia_nstep_workflowstep cascade constraints;

    drop table jahia_nstep_workflowuser cascade constraints;

    drop table jahia_obj cascade constraints;

    drop table jahia_pages_data cascade constraints;

    drop table jahia_pages_def cascade constraints;

    drop table jahia_pages_def_prop cascade constraints;

    drop table jahia_pages_prop cascade constraints;

    drop table jahia_pages_users_prop cascade constraints;

    drop table jahia_pwd_policies cascade constraints;

    drop table jahia_pwd_policy_rule_params cascade constraints;

    drop table jahia_pwd_policy_rules cascade constraints;

    drop table jahia_reference cascade constraints;

    drop table jahia_resources cascade constraints;

    drop table jahia_retrule cascade constraints;

    drop table jahia_retrule_range cascade constraints;

    drop table jahia_retruledef cascade constraints;

    drop table jahia_serverprops cascade constraints;

    drop table jahia_site_lang_list cascade constraints;

    drop table jahia_site_lang_maps cascade constraints;

    drop table jahia_site_prop cascade constraints;

    drop table jahia_sites cascade constraints;

    drop table jahia_sites_grps cascade constraints;

    drop table jahia_sites_users cascade constraints;

    drop table jahia_subscriptions cascade constraints;

    drop table jahia_user_prop cascade constraints;

    drop table jahia_users cascade constraints;

    drop table jahia_version cascade constraints;

    drop table jahia_workflow cascade constraints;

    create table jahia_acl (
        id_jahia_acl number(10,0) not null,
        inheritance_jahia_acl number(10,0),
        hasentries_jahia_acl number(10,0),
        parent_id_jahia_acl number(10,0),
        picked_id_jahia_acl number(10,0),
        primary key (id_jahia_acl)
    );

    create table jahia_acl_entries (
        id_jahia_acl number(10,0) not null,
        type_jahia_acl_entries number(10,0) not null,
        target_jahia_acl_entries varchar2(50 char) not null,
        entry_state_jahia_acl_entries number(10,0) not null,
        entry_trist_jahia_acl_entries number(10,0) not null,
        primary key (id_jahia_acl, type_jahia_acl_entries, target_jahia_acl_entries)
    );

    create table jahia_acl_names (
        acl_name varchar2(255 char) not null,
        acl_id number(10,0) unique,
        primary key (acl_name)
    );

    create table jahia_audit_log (
        id_jahia_audit_log number(10,0) not null,
        time_jahia_audit_log number(19,0),
        username_jahia_audit_log varchar2(50 char),
        objecttype_jahia_audit_log number(10,0),
        objectid_jahia_audit_log number(10,0),
        parenttype_jahia_audit_log number(10,0),
        parentid_jahia_audit_log number(10,0),
        operation_jahia_audit_log varchar2(50 char),
        site_jahia_audit_log varchar2(50 char),
        content_jahia_audit_log varchar2(250 char),
        parent_id_jahia_audit_log number(10,0),
        eventType varchar2(255 char),
        eventInformation blob,
        primary key (id_jahia_audit_log)
    );

    create table jahia_bigtext_data (
        id_bigtext_data varchar2(255 char) not null,
        raw_value clob,
        primary key (id_bigtext_data)
    );

    create table jahia_ctn_def (
        id_jahia_ctn_def number(10,0) not null,
        jahiaid_jahia_ctn_def number(10,0),
        name_jahia_ctn_def varchar2(250 char),
        pctnname_jahia_ctndef_def varchar2(250 char),
        ctntype_jahia_ctn_def varchar2(150 char),
        primary key (id_jahia_ctn_def)
    );

    create table jahia_ctn_def_properties (
        id_jahia_ctn_def_properties number(10,0) not null,
        ctndefid_jahia_ctn_def_prop number(10,0) not null,
        pagedefid_jahia_ctn_def_prop number(10,0),
        primary key (id_jahia_ctn_def_properties)
    );

    create table jahia_ctn_entries (
        id_jahia_ctn_entries number(10,0) not null,
        version_id number(10,0) not null,
        workflow_state number(10,0) not null,
        ctndefid_jahia_ctn_entries number(10,0),
        rights_jahia_ctn_entries number(10,0),
        listid_jahia_ctn_entries number(10,0),
        pageid_jahia_ctn_entries number(10,0),
        rank_jahia_ctn_entries number(10,0),
        jahiaid_jahia_ctn_entries number(10,0),
        primary key (id_jahia_ctn_entries, version_id, workflow_state)
    );

    create table jahia_ctn_lists (
        id_jahia_ctn_lists number(10,0) not null,
        version_id number(10,0) not null,
        workflow_state number(10,0) not null,
        pageid_jahia_ctn_lists number(10,0),
        parententryid_jahia_ctn_lists number(10,0),
        ctndefid_jahia_ctn_lists number(10,0),
        rights_jahia_ctn_lists number(10,0),
        primary key (id_jahia_ctn_lists, version_id, workflow_state)
    );

    create table jahia_ctn_struct (
        ctnsubdefid_jahia_ctn_struct number(10,0) not null,
        objtype_jahia_ctn_struct number(10,0) not null,
        objdefid_jahia_ctn_struct number(10,0) not null,
        rank_jahia_ctn_struct number(10,0),
        primary key (ctnsubdefid_jahia_ctn_struct, objtype_jahia_ctn_struct, objdefid_jahia_ctn_struct)
    );

    create table jahia_ctndef_prop (
        id_jahia_ctn_def number(10,0) not null,
        name_jahia_ctndef_prop varchar2(255 char) not null,
        value_jahia_ctndef_prop varchar2(255 char),
        primary key (id_jahia_ctn_def, name_jahia_ctndef_prop)
    );

    create table jahia_ctnentries_prop (
        ctnid_ctnentries_prop number(10,0) not null,
        name_ctnentries_prop varchar2(255 char) not null,
        jahiaid_ctnentries_prop number(10,0),
        value_ctnentries_prop varchar2(255 char),
        primary key (ctnid_ctnentries_prop, name_ctnentries_prop)
    );

    create table jahia_ctnlists_prop (
        ctnlistid_ctnlists_prop number(10,0) not null,
        name_ctnlists_prop varchar2(255 char) not null,
        jahiaid_ctnlists_prop number(10,0),
        value_ctnlists_prop varchar2(255 char),
        primary key (ctnlistid_ctnlists_prop, name_ctnlists_prop)
    );

    create table jahia_db_test (
        testfield varchar2(255 char) not null,
        primary key (testfield)
    );

    create table jahia_fieldreference (
        fieldId number(10,0) not null,
        language varchar2(10 char) not null,
        workflow number(10,0) not null,
        target varchar2(255 char) not null,
        siteId number(10,0),
        primary key (fieldId, language, workflow, target)
    );

    create table jahia_fields_data (
        id_jahia_fields_data number(10,0) not null,
        version_id number(10,0) not null,
        workflow_state number(10,0) not null,
        language_code varchar2(10 char) not null,
        connecttype_jahia_fields_data number(10,0),
        ctnid_jahia_fields_data number(10,0),
        fielddefid_jahia_fields_data number(10,0),
        id_jahia_obj number(10,0),
        type_jahia_obj varchar2(22 char),
        rights_jahia_fields_data number(10,0),
        pageid_jahia_fields_data number(10,0),
        jahiaid_jahia_fields_data number(10,0),
        type_jahia_fields_data number(10,0),
        value_jahia_fields_data varchar2(250 char),
        primary key (id_jahia_fields_data, version_id, workflow_state, language_code)
    );

    create table jahia_fields_def (
        id_jahia_fields_def number(10,0) not null,
        ismdata_jahia_fields_def number(10,0),
        jahiaid_jahia_fields_def number(10,0),
        ctnname_jahia_fields_def varchar2(250 char),
        name_jahia_fields_def varchar2(250 char),
        primary key (id_jahia_fields_def)
    );

    create table jahia_fields_def_extprop (
        id_jahia_fields_def number(10,0) not null,
        prop_name varchar2(200 char) not null,
        prop_value varchar2(255 char),
        primary key (id_jahia_fields_def, prop_name)
    );

    create table jahia_fields_prop (
        fieldid_jahia_fields_prop number(10,0) not null,
        propertyname_jahia_fields_prop varchar2(250 char) not null,
        propvalue_jahia_fields_prop varchar2(50 char),
        primary key (fieldid_jahia_fields_prop, propertyname_jahia_fields_prop)
    );

    create table jahia_grp_access (
        id_jahia_member varchar2(150 char) not null,
        id_jahia_grps varchar2(150 char) not null,
        membertype_grp_access number(10,0) not null,
        primary key (id_jahia_member, id_jahia_grps, membertype_grp_access)
    );

    create table jahia_grp_prop (
        id_jahia_grp number(10,0) not null,
        name_jahia_grp_prop varchar2(50 char) not null,
        provider_jahia_grp_prop varchar2(50 char) not null,
        grpkey_jahia_grp_prop varchar2(200 char) not null,
        value_jahia_grp_prop varchar2(255 char),
        primary key (id_jahia_grp, name_jahia_grp_prop, provider_jahia_grp_prop, grpkey_jahia_grp_prop)
    );

    create table jahia_grps (
        id_jahia_grps number(10,0) not null,
        name_jahia_grps varchar2(195 char),
        key_jahia_grps varchar2(200 char) unique,
        siteid_jahia_grps number(10,0),
        hidden_jahia_grps number(1,0),
        primary key (id_jahia_grps)
    );

    create table jahia_installedpatch (
        install_number number(10,0) not null,
        name varchar2(100 char),
        build number(10,0),
        result_code number(10,0),
        install_date timestamp,
        primary key (install_number)
    );

    create table jahia_languages_states (
        objectkey varchar2(40 char) not null,
        language_code varchar2(10 char) not null,
        workflow_state number(10,0),
        siteid number(10,0),
        primary key (objectkey, language_code)
    );

    create table jahia_link (
        id number(10,0) not null,
        left_oid varchar2(100 char),
        right_oid varchar2(100 char),
        type varchar2(100 char),
        primary key (id)
    );

    create table jahia_link_metadata (
        link_id number(10,0) not null,
        link_position varchar2(20 char) not null,
        property_name varchar2(255 char) not null,
        property_value varchar2(255 char),
        primary key (link_id, link_position, property_name)
    );

    create table jahia_locks_non_excl (
        name_locks varchar2(50 char) not null,
        targetid_locks number(10,0) not null,
        action_locks varchar2(50 char) not null,
        context_locks varchar2(80 char) not null,
        owner_locks varchar2(50 char),
        timeout_locks number(10,0),
        expirationDate_locks number(19,0),
        serverid_locks varchar2(30 char),
        stolen_locks varchar2(10 char),
        primary key (name_locks, targetid_locks, action_locks, context_locks)
    );

    create table jahia_nstep_workflow (
        id number(19,0) not null,
        name varchar2(255 char) not null,
        primary key (id)
    );

    create table jahia_nstep_workflowhistory (
        id number(19,0) not null,
        action varchar2(255 char) not null,
        author varchar2(255 char) not null,
        message clob,
        actionDate timestamp not null,
        languageCode varchar2(255 char) not null,
        objectKey varchar2(255 char) not null,
        process varchar2(255 char) not null,
        username varchar2(255 char) not null,
        primary key (id)
    );

    create table jahia_nstep_workflowinstance (
        id number(19,0) not null,
        authorEmail varchar2(255 char),
        languageCode varchar2(255 char) not null,
        objectKey varchar2(255 char) not null,
        step number(19,0),
        user_id number(19,0),
        workflow number(19,0),
        startDate timestamp,
        primary key (id)
    );

    create table jahia_nstep_workflowstep (
        id number(19,0) not null,
        name varchar2(255 char) not null,
        workflow_id number(19,0),
        step_index number(10,0),
        primary key (id)
    );

    create table jahia_nstep_workflowuser (
        id number(19,0) not null,
        login varchar2(255 char) not null,
        primary key (id)
    );

    create table jahia_obj (
        id_jahia_obj number(10,0) not null,
        type_jahia_obj varchar2(22 char) not null,
        jahiaid_jahia_obj number(10,0),
        timebpstate_jahia_obj number(10,0),
        validfrom_jahia_obj number(19,0),
        validto_jahia_obj number(19,0),
        retrule_jahia_obj number(10,0),
        primary key (id_jahia_obj, type_jahia_obj)
    );

    create table jahia_pages_data (
        id_jahia_pages_data number(10,0) not null,
        version_id number(10,0) not null,
        workflow_state number(10,0) not null,
        language_code varchar2(10 char) not null,
        rights_jahia_pages_data number(10,0),
        pagelinkid_jahia_pages_data number(10,0),
        pagetype_jahia_pages_data number(10,0),
        pagedefid_jahia_pages_data number(10,0),
        parentid_jahia_pages_data number(10,0),
        remoteurl_jahia_pages_data varchar2(250 char),
        jahiaid_jahia_pages_data number(10,0),
        title_jahia_pages_data varchar2(250 char),
        primary key (id_jahia_pages_data, version_id, workflow_state, language_code)
    );

    create table jahia_pages_def (
        id_jahia_pages_def number(10,0) not null,
        jahiaid_jahia_pages_def number(10,0),
        name_jahia_pages_def varchar2(250 char),
        sourcepath_jahia_pages_def varchar2(250 char),
        visible_jahia_pages_def number(1,0),
        browsable_jahia_pages_def number(10,0),
        warning_msg_jahia_pages_def varchar2(250 char),
        img_jahia_pages_def varchar2(150 char),
        pagetype_jahia_pages_def varchar2(150 char),
        primary key (id_jahia_pages_def)
    );

    create table jahia_pages_def_prop (
        id_jahia_pages_def_prop number(10,0) not null,
        jahiaid_pages_def_prop number(10,0) not null,
        name_pages_def_prop varchar2(100 char) not null,
        value_pages_def_prop varchar2(200 char),
        primary key (id_jahia_pages_def_prop, jahiaid_pages_def_prop, name_pages_def_prop)
    );

    create table jahia_pages_prop (
        page_id number(10,0) not null,
        prop_name varchar2(150 char) not null,
        language_code varchar2(100 char) not null,
        prop_value varchar2(255 char),
        primary key (page_id, prop_name, language_code)
    );

    create table jahia_pages_users_prop (
        page_id number(10,0) not null,
        principal_key varchar2(70 char) not null,
        principal_type varchar2(40 char) not null,
        prop_type varchar2(40 char) not null,
        prop_name varchar2(150 char) not null,
        prop_value varchar2(255 char),
        primary key (page_id, principal_key, principal_type, prop_type, prop_name)
    );

    create table jahia_pwd_policies (
        jahia_pwd_policy_id number(10,0) not null,
        name varchar2(255 char) not null,
        primary key (jahia_pwd_policy_id)
    );

    create table jahia_pwd_policy_rule_params (
        jahia_pwd_policy_rule_param_id number(10,0) not null,
        name varchar2(50 char) not null,
        position_index number(10,0) not null,
        jahia_pwd_policy_rule_id number(10,0) not null,
        type char(1 char) not null,
        value varchar2(255 char),
        primary key (jahia_pwd_policy_rule_param_id)
    );

    create table jahia_pwd_policy_rules (
        jahia_pwd_policy_rule_id number(10,0) not null,
        action char(1 char) not null,
        rule_condition clob not null,
        evaluator char(1 char) not null,
        name varchar2(255 char) not null,
        jahia_pwd_policy_id number(10,0) not null,
        position_index number(10,0) not null,
        active number(1,0) not null,
        last_rule number(1,0) not null,
        periodical number(1,0) not null,
        primary key (jahia_pwd_policy_rule_id)
    );

    create table jahia_reference (
        page_id number(10,0) not null,
        ref_id number(10,0) not null,
        ref_type number(10,0) not null,
        primary key (page_id, ref_id, ref_type)
    );

    create table jahia_resources (
        name_resource varchar2(200 char) not null,
        languagecode_resource varchar2(10 char) not null,
        value_resource varchar2(255 char),
        primary key (name_resource, languagecode_resource)
    );

    create table jahia_retrule (
        id_jahia_retrule number(10,0) not null,
        id_jahia_retruledef number(10,0),
        inherited_retrule number(1,0),
        enabled_retrule number(1,0),
        title_retrule varchar2(255 char),
        comment_retrule varchar2(255 char),
        shared_retrule number(1,0),
        settings_retrule clob,
        primary key (id_jahia_retrule)
    );

    create table jahia_retrule_range (
        id_retrule_range number(10,0) not null,
        validfrom_retrule_range number(19,0),
        validto_retrule_range number(19,0),
        notiffromd_retrule_range number(1,0),
        notiftod_retrule_range number(1,0),
        primary key (id_retrule_range)
    );

    create table jahia_retruledef (
        id_jahia_retruledef number(10,0) not null,
        name_retruledef varchar2(255 char) unique,
        title_retruledef varchar2(255 char),
        ruleclass_retruledef varchar2(255 char),
        rulehelperclass_retruledef varchar2(255 char),
        dateformat_retruledef varchar2(255 char),
        primary key (id_jahia_retruledef)
    );

    create table jahia_serverprops (
        id_serverprops varchar2(50 char) not null,
        propname_serverprops varchar2(200 char) not null,
        propvalue_serverprops varchar2(250 char) not null,
        primary key (id_serverprops, propname_serverprops)
    );

    create table jahia_site_lang_list (
        id number(10,0) not null,
        site_id number(10,0),
        code varchar2(255 char),
        rank number(10,0),
        activated number(1,0),
        mandatory number(1,0),
        primary key (id)
    );

    create table jahia_site_lang_maps (
        id number(10,0) not null,
        site_id number(10,0),
        from_lang_code varchar2(255 char),
        to_lang_code varchar2(255 char),
        primary key (id)
    );

    create table jahia_site_prop (
        id_jahia_site number(10,0) not null,
        name_jahia_site_prop varchar2(255 char) not null,
        value_jahia_site_prop varchar2(255 char),
        primary key (id_jahia_site, name_jahia_site_prop)
    );

    create table jahia_sites (
        id_jahia_sites number(10,0) not null,
        title_jahia_sites varchar2(100 char),
        servername_jahia_sites varchar2(200 char),
        key_jahia_sites varchar2(50 char) unique,
        active_jahia_sites number(10,0),
        defaultpageid_jahia_sites number(10,0),
        defaulttemplateid_jahia_sites number(10,0),
        tpl_deploymode_jahia_sites number(10,0),
        webapps_deploymode_jahia_sites number(10,0),
        rights_jahia_sites number(10,0),
        descr_jahia_sites varchar2(250 char),
        default_site_jahia_sites number(1,0),
        primary key (id_jahia_sites)
    );

    create table jahia_sites_grps (
        grpname_sites_grps varchar2(50 char) not null,
        siteid_sites_grps number(10,0) not null,
        grpid_sites_grps varchar2(200 char),
        primary key (grpname_sites_grps, siteid_sites_grps)
    );

    create table jahia_sites_users (
        username_sites_users varchar2(50 char) not null,
        siteid_sites_users number(10,0) not null,
        userid_sites_users varchar2(50 char),
        primary key (username_sites_users, siteid_sites_users)
    );

    create table jahia_subscriptions (
        id_jahia_subscriptions number(10,0) not null,
        object_key varchar2(40 char),
        include_children number(1,0) not null,
        event_type varchar2(50 char) not null,
        channel char(1 char) not null,
        notification_type char(1 char) not null,
        username varchar2(255 char) not null,
        user_registered number(1,0) not null,
        site_id number(10,0) not null,
        enabled number(1,0) not null,
        suspended number(1,0) not null,
        confirmation_key varchar2(32 char),
        confirmation_request_timestamp number(19,0),
        properties clob,
        primary key (id_jahia_subscriptions)
    );

    create table jahia_user_prop (
        id_jahia_users number(10,0) not null,
        name_jahia_user_prop varchar2(150 char) not null,
        provider_jahia_user_prop varchar2(50 char) not null,
        userkey_jahia_user_prop varchar2(50 char) not null,
        value_jahia_user_prop varchar2(255 char),
        primary key (id_jahia_users, name_jahia_user_prop, provider_jahia_user_prop, userkey_jahia_user_prop)
    );

    create table jahia_users (
        id_jahia_users number(10,0) not null,
        name_jahia_users varchar2(255 char),
        password_jahia_users varchar2(255 char),
        key_jahia_users varchar2(50 char) not null unique,
        primary key (id_jahia_users)
    );

    create table jahia_version (
        install_number number(10,0) not null,
        build number(10,0),
        release_number varchar2(20 char),
        install_date timestamp,
        primary key (install_number)
    );

    create table jahia_workflow (
        OBJECTKEY varchar2(255 char) not null,
        MODEVALUE number(10,0),
        EXTERNALNAME varchar2(255 char),
        EXTERNALPROCESS varchar2(255 char),
        MAINOBJECTKEY varchar2(255 char),
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

    alter table jahia_audit_log 
        add constraint FKF4669B0AC75E28FD 
        foreign key (parent_id_jahia_audit_log) 
        references jahia_audit_log;

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

    alter table jahia_site_lang_list 
        add constraint FK1DDBC16D7EED26D3 
        foreign key (site_id) 
        references jahia_sites;

    alter table jahia_site_lang_maps 
        add constraint FK1DDC17667EED26D3 
        foreign key (site_id) 
        references jahia_sites;

    alter table jahia_sites_grps 
        add constraint FK7B24559790F996AC 
        foreign key (grpid_sites_grps) 
        references jahia_grps (key_jahia_grps);

    alter table jahia_sites_grps 
        add constraint FK7B245597F46755FE 
        foreign key (siteid_sites_grps) 
        references jahia_sites;

    alter table jahia_sites_users 
        add constraint FKEA2BF1BF6CF683C0 
        foreign key (userid_sites_users) 
        references jahia_users (key_jahia_users);
