
    create table jbpm_attachment (
        id int8 not null,
        access_type int4,
        attached_at timestamp,
        attachment_content_id int8 not null,
        content_type varchar(255),
        name varchar(255),
        attachment_size int4,
        attached_by varchar(255),
        task_data_attachments_id int8,
        primary key (id)
    );

    create table jbpm_bamtask_summary (
        pk int8 not null,
        created_date timestamp,
        duration int8,
        end_date timestamp,
        process_instance_id int8 not null,
        start_date timestamp,
        status varchar(255),
        task_id int8 not null,
        task_name varchar(255),
        user_id varchar(255),
        primary key (pk)
    );

    create table jbpm_boolean_expression (
        id int8 not null,
        expression text,
        type varchar(255),
        escalation_constraints_id int8,
        primary key (id)
    );

    create table jbpm_content (
        id int8 not null,
        content oid,
        primary key (id)
    );

    create table jbpm_context_mapping_info (
        mapping_id int8 not null,
        context_id varchar(255) not null,
        ksession_id int4 not null,
        optlock int4,
        primary key (mapping_id)
    );

    create table jbpm_correlation_key_info (
        key_id int8 not null,
        name varchar(255),
        process_instance_id int8 not null,
        optlock int4,
        primary key (key_id)
    );

    create table jbpm_correlation_property_info (
        property_id int8 not null,
        name varchar(255),
        value varchar(255),
        optlock int4,
        correlation_key int8,
        primary key (property_id)
    );

    create table jbpm_deadline (
        id int8 not null,
        deadline_date timestamp,
        escalated int2,
        deadlines_start_dead_line_id int8,
        deadlines_end_dead_line_id int8,
        primary key (id)
    );

    create table jbpm_delegation_delegates (
        task_id int8 not null,
        entity_id varchar(255) not null
    );

    create table jbpm_email_header (
        id int8 not null,
        body text,
        from_address varchar(255),
        language varchar(255),
        reply_to_address varchar(255),
        subject varchar(255),
        primary key (id)
    );

    create table jbpm_escalation (
        id int8 not null,
        name varchar(255),
        deadline_escalation_id int8,
        primary key (id)
    );

    create table jbpm_event_types (
        instance_id int8 not null,
        element varchar(255)
    );

    create table jbpm_i18ntext (
        id int8 not null,
        language varchar(255),
        short_text varchar(255),
        text text,
        task_subjects_id int8,
        task_names_id int8,
        task_descriptions_id int8,
        reassignment_documentation_id int8,
        notification_subjects_id int8,
        notification_names_id int8,
        notification_documentation_id int8,
        notification_descriptions_id int8,
        deadline_documentation_id int8,
        primary key (id)
    );

    create table jbpm_node_instance_log (
        id int8 not null,
        r_connection varchar(255),
        log_date timestamp,
        external_id varchar(255),
        node_id varchar(255),
        node_instance_id varchar(255),
        node_name varchar(255),
        node_type varchar(255),
        process_id varchar(255),
        process_instance_id int8 not null,
        type int4 not null,
        work_item_id int8,
        primary key (id)
    );

    create table jbpm_notificat_email_headers (
        notification int8 not null,
        email_headers int8 not null,
        mapkey varchar(255) not null,
        primary key (notification, mapkey)
    );

    create table jbpm_notification (
        dtype varchar(31) not null,
        id int8 not null,
        priority int4 not null,
        escalation_notifications_id int8,
        primary key (id)
    );

    create table jbpm_notification_bas (
        task_id int8 not null,
        entity_id varchar(255) not null
    );

    create table jbpm_notification_recipients (
        task_id int8 not null,
        entity_id varchar(255) not null
    );

    create table jbpm_organizational_entity (
        dtype varchar(31) not null,
        id varchar(255) not null,
        primary key (id)
    );

    create table jbpm_people_ass_excl_owners (
        task_id int8 not null,
        entity_id varchar(255) not null
    );

    create table jbpm_people_assignm_pot_owners (
        task_id int8 not null,
        entity_id varchar(255) not null
    );

    create table jbpm_people_assignm_recipients (
        task_id int8 not null,
        entity_id varchar(255) not null
    );

    create table jbpm_people_assignm_stakehold (
        task_id int8 not null,
        entity_id varchar(255) not null
    );

    create table jbpm_people_assignments_bas (
        task_id int8 not null,
        entity_id varchar(255) not null
    );

    create table jbpm_process_instance_info (
        instance_id int8 not null,
        last_modification_date timestamp,
        last_read_date timestamp,
        process_id varchar(255),
        process_instance_byte_array oid,
        start_date timestamp,
        state int4 not null,
        optlock int4,
        primary key (instance_id)
    );

    create table jbpm_process_instance_log (
        id int8 not null,
        duration int8,
        end_date timestamp,
        external_id varchar(255),
        user_identity varchar(255),
        outcome varchar(255),
        parent_process_instance_id int8,
        process_id varchar(255),
        process_instance_id int8 not null,
        process_name varchar(255),
        process_version varchar(255),
        start_date timestamp,
        status int4,
        primary key (id)
    );

    create table jbpm_reass_potential_owners (
        task_id int8 not null,
        entity_id varchar(255) not null
    );

    create table jbpm_reassignment (
        id int8 not null,
        escalation_reassignments_id int8,
        primary key (id)
    );

    create table jbpm_session_info (
        id int4 not null,
        last_modification_date timestamp,
        rules_byte_array oid,
        start_date timestamp,
        optlock int4,
        primary key (id)
    );

    create table jbpm_task (
        id int8 not null,
        archived int2,
        allowed_to_delegate varchar(255),
        form_name varchar(255),
        priority int4 not null,
        sub_task_strategy varchar(255),
        activation_time timestamp,
        created_on timestamp,
        deployment_id varchar(255),
        document_access_type int4,
        document_content_id int8 not null,
        document_type varchar(255),
        expiration_time timestamp,
        fault_access_type int4,
        fault_content_id int8 not null,
        fault_name varchar(255),
        fault_type varchar(255),
        output_access_type int4,
        output_content_id int8 not null,
        output_type varchar(255),
        parent_id int8 not null,
        previous_status int4,
        process_id varchar(255),
        process_instance_id int8 not null,
        process_session_id int4 not null,
        skipable boolean not null,
        status varchar(255),
        work_item_id int8 not null,
        task_type varchar(255),
        optlock int4,
        task_initiator varchar(255),
        actual_owner varchar(255),
        created_by varchar(255),
        primary key (id)
    );

    create table jbpm_task_comment (
        id int8 not null,
        added_at timestamp,
        text text,
        added_by varchar(255),
        task_data_comments_id int8,
        primary key (id)
    );

    create table jbpm_task_event (
        id int8 not null,
        log_time timestamp,
        task_id int8,
        type varchar(255),
        user_id varchar(255),
        primary key (id)
    );

    create table jbpm_variable_instance_log (
        id int8 not null,
        log_date timestamp,
        external_id varchar(255),
        old_value varchar(255),
        process_id varchar(255),
        process_instance_id int8 not null,
        value varchar(255),
        variable_id varchar(255),
        variable_instance_id varchar(255),
        primary key (id)
    );

    create table jbpm_work_item_info (
        work_item_id int8 not null,
        creation_date timestamp,
        name varchar(255),
        process_instance_id int8 not null,
        state int8 not null,
        optlock int4,
        work_item_byte_array oid,
        primary key (work_item_id)
    );

    alter table jbpm_notificat_email_headers 
        add constraint UK_epvmc6tmrn1wq6nu8peaywt1k unique (email_headers);

    alter table jbpm_attachment 
        add constraint FK_anhlt1ywoxmhaippy7h78baxk 
        foreign key (attached_by) 
        references jbpm_organizational_entity;

    alter table jbpm_attachment 
        add constraint FK_s73kjv5ko89qn5xnvxulcd67g 
        foreign key (task_data_attachments_id) 
        references jbpm_task;

    alter table jbpm_boolean_expression 
        add constraint FK_nbrfj26gq8axs7ayswkuxq0i5 
        foreign key (escalation_constraints_id) 
        references jbpm_escalation;

    alter table jbpm_correlation_property_info 
        add constraint FK_91u8nc7udwpc496ldpaett8oa 
        foreign key (correlation_key) 
        references jbpm_correlation_key_info;

    alter table jbpm_deadline 
        add constraint FK_qd0gx9omsbf3aoghofg2v3oxr 
        foreign key (deadlines_start_dead_line_id) 
        references jbpm_task;

    alter table jbpm_deadline 
        add constraint FK_rklcbb9hsd16cc9acp7cdvwmx 
        foreign key (deadlines_end_dead_line_id) 
        references jbpm_task;

    alter table jbpm_delegation_delegates 
        add constraint FK_9apnaigsuutvonmkr21peocui 
        foreign key (entity_id) 
        references jbpm_organizational_entity;

    alter table jbpm_delegation_delegates 
        add constraint FK_oriwrke8vcl3opfut68n472r9 
        foreign key (task_id) 
        references jbpm_task;

    alter table jbpm_escalation 
        add constraint FK_7g5gvv0kep2olcpvmef7kvoi2 
        foreign key (deadline_escalation_id) 
        references jbpm_deadline;

    alter table jbpm_event_types 
        add constraint FK_6n2ptqm4wpn907h23e6tqbbha 
        foreign key (instance_id) 
        references jbpm_process_instance_info;

    alter table jbpm_i18ntext 
        add constraint FK_5innbucbtx4lfii02bnpve8p 
        foreign key (task_subjects_id) 
        references jbpm_task;

    alter table jbpm_i18ntext 
        add constraint FK_e03i5m0xowu3tlckdntdvcmj2 
        foreign key (task_names_id) 
        references jbpm_task;

    alter table jbpm_i18ntext 
        add constraint FK_kqgti9te780wtpviwdnfrufr7 
        foreign key (task_descriptions_id) 
        references jbpm_task;

    alter table jbpm_i18ntext 
        add constraint FK_oc0h7p2a4abkflrmdh8iib35t 
        foreign key (reassignment_documentation_id) 
        references jbpm_reassignment;

    alter table jbpm_i18ntext 
        add constraint FK_me9v2v0ea50skfj700g3a2t61 
        foreign key (notification_subjects_id) 
        references jbpm_notification;

    alter table jbpm_i18ntext 
        add constraint FK_9y5pivj1g7xkuicw96cu8u2i 
        foreign key (notification_names_id) 
        references jbpm_notification;

    alter table jbpm_i18ntext 
        add constraint FK_sf8hq8wp349bf2pt22vjopoma 
        foreign key (notification_documentation_id) 
        references jbpm_notification;

    alter table jbpm_i18ntext 
        add constraint FK_h2gg5m9ylnih8mfjx1iunwnbe 
        foreign key (notification_descriptions_id) 
        references jbpm_notification;

    alter table jbpm_i18ntext 
        add constraint FK_esqmn5micj3ljsjadbbmyjgfc 
        foreign key (deadline_documentation_id) 
        references jbpm_deadline;

    alter table jbpm_notificat_email_headers 
        add constraint FK_epvmc6tmrn1wq6nu8peaywt1k 
        foreign key (email_headers) 
        references jbpm_email_header;

    alter table jbpm_notificat_email_headers 
        add constraint FK_kf8fd1lw8m7jxkyekbsex81c8 
        foreign key (notification) 
        references jbpm_notification;

    alter table jbpm_notification 
        add constraint FK_lsx8dgmw3ilrncmkruoqimm28 
        foreign key (escalation_notifications_id) 
        references jbpm_escalation;

    alter table jbpm_notification_bas 
        add constraint FK_ai0cdcnws5a5ht5xpe3c01mxp 
        foreign key (entity_id) 
        references jbpm_organizational_entity;

    alter table jbpm_notification_bas 
        add constraint FK_rrdpb88hvc55jslc4msf6u440 
        foreign key (task_id) 
        references jbpm_notification;

    alter table jbpm_notification_recipients 
        add constraint FK_je2kqu2jgy4j17wsli8ex57c7 
        foreign key (entity_id) 
        references jbpm_organizational_entity;

    alter table jbpm_notification_recipients 
        add constraint FK_c5x1hqb09c91br1oy949yg5i7 
        foreign key (task_id) 
        references jbpm_notification;

    alter table jbpm_people_ass_excl_owners 
        add constraint FK_9rfplx6e347cgvpt9b442vje8 
        foreign key (entity_id) 
        references jbpm_organizational_entity;

    alter table jbpm_people_ass_excl_owners 
        add constraint FK_g6xxe0615p0hu79q0d111clnl 
        foreign key (task_id) 
        references jbpm_task;

    alter table jbpm_people_assignm_pot_owners 
        add constraint FK_1vlj3kfu51ukgo45p3fm4krwx 
        foreign key (entity_id) 
        references jbpm_organizational_entity;

    alter table jbpm_people_assignm_pot_owners 
        add constraint FK_1sqbvhk1obasgfp839uk387tb 
        foreign key (task_id) 
        references jbpm_task;

    alter table jbpm_people_assignm_recipients 
        add constraint FK_gcw7a7bs3m50jhvfa5jx7gg60 
        foreign key (entity_id) 
        references jbpm_organizational_entity;

    alter table jbpm_people_assignm_recipients 
        add constraint FK_l5h85stwvy0aetdg6saoblec0 
        foreign key (task_id) 
        references jbpm_task;

    alter table jbpm_people_assignm_stakehold 
        add constraint FK_b30nkrrs9b4fmsr6lgf4rrox8 
        foreign key (entity_id) 
        references jbpm_organizational_entity;

    alter table jbpm_people_assignm_stakehold 
        add constraint FK_eblnc1w5r25dekutmwn3dujn6 
        foreign key (task_id) 
        references jbpm_task;

    alter table jbpm_people_assignments_bas 
        add constraint FK_ojn5ekkacrgupp069yrqss9pd 
        foreign key (entity_id) 
        references jbpm_organizational_entity;

    alter table jbpm_people_assignments_bas 
        add constraint FK_ekd3d8qrxvxa7kq3iw5d34bfw 
        foreign key (task_id) 
        references jbpm_task;

    alter table jbpm_reass_potential_owners 
        add constraint FK_rm075l73m0whh7uxwnu9cx3vr 
        foreign key (entity_id) 
        references jbpm_organizational_entity;

    alter table jbpm_reass_potential_owners 
        add constraint FK_jsldbpqi48q1w9c9g99d6wnqk 
        foreign key (task_id) 
        references jbpm_reassignment;

    alter table jbpm_reassignment 
        add constraint FK_fkj43wo6ovwdhwt1q2d8ce0pd 
        foreign key (escalation_reassignments_id) 
        references jbpm_escalation;

    alter table jbpm_task 
        add constraint FK_3flcq2koknmx53wygbeyuteng 
        foreign key (task_initiator) 
        references jbpm_organizational_entity;

    alter table jbpm_task 
        add constraint FK_r4b5cbeyk3axu7rr8aek6u9w1 
        foreign key (actual_owner) 
        references jbpm_organizational_entity;

    alter table jbpm_task 
        add constraint FK_7i7nh49lv4kdks9ljkomxj2uh 
        foreign key (created_by) 
        references jbpm_organizational_entity;

    alter table jbpm_task_comment 
        add constraint FK_bl2skw5rlmgsg4xi0f50061tm 
        foreign key (added_by) 
        references jbpm_organizational_entity;

    alter table jbpm_task_comment 
        add constraint FK_nuqjfe1rpp1ad7jlfjjki6tog 
        foreign key (task_data_comments_id) 
        references jbpm_task;

    create sequence ATTACHMENT_ID_SEQ;

    create sequence BAM_TASK_ID_SEQ;

    create sequence BOOLEANEXPR_ID_SEQ;

    create sequence COMMENT_ID_SEQ;

    create sequence CONTENT_ID_SEQ;

    create sequence CONTEXT_MAPPING_INFO_ID_SEQ;

    create sequence CORRELATION_KEY_ID_SEQ;

    create sequence CORRELATION_PROP_ID_SEQ;

    create sequence DEADLINE_ID_SEQ;

    create sequence EMAILNOTIFHEAD_ID_SEQ;

    create sequence ESCALATION_ID_SEQ;

    create sequence I18NTEXT_ID_SEQ;

    create sequence NODE_INST_LOG_ID_SEQ;

    create sequence NOTIFICATION_ID_SEQ;

    create sequence PROCESS_INSTANCE_INFO_ID_SEQ;

    create sequence PROC_INST_LOG_ID_SEQ;

    create sequence REASSIGNMENT_ID_SEQ;

    create sequence SESSIONINFO_ID_SEQ;

    create sequence TASK_EVENT_ID_SEQ;

    create sequence TASK_ID_SEQ;

    create sequence VAR_INST_LOG_ID_SEQ;

    create sequence WORKITEMINFO_ID_SEQ;
