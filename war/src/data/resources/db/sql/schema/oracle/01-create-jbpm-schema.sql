
    create table jbpm_attachment (
        id number(19,0) not null,
        access_type number(10,0),
        attached_at timestamp,
        attachment_content_id number(19,0) not null,
        content_type varchar2(255 char),
        name varchar2(255 char),
        attachment_size number(10,0),
        attached_by varchar2(255 char),
        task_data_attachments_id number(19,0),
        primary key (id)
    );

    create table jbpm_bamtask_summary (
        pk number(19,0) not null,
        created_date timestamp,
        duration number(19,0),
        end_date timestamp,
        process_instance_id number(19,0) not null,
        start_date timestamp,
        status varchar2(255 char),
        task_id number(19,0) not null,
        task_name varchar2(255 char),
        user_id varchar2(255 char),
        primary key (pk)
    );

    create table jbpm_boolean_expression (
        id number(19,0) not null,
        expression clob,
        type varchar2(255 char),
        escalation_constraints_id number(19,0),
        primary key (id)
    );

    create table jbpm_content (
        id number(19,0) not null,
        content blob,
        primary key (id)
    );

    create table jbpm_context_mapping_info (
        mapping_id number(19,0) not null,
        context_id varchar2(255 char) not null,
        ksession_id number(10,0) not null,
        optlock number(10,0),
        primary key (mapping_id)
    );

    create table jbpm_correlation_key_info (
        key_id number(19,0) not null,
        name varchar2(255 char),
        process_instance_id number(19,0) not null,
        optlock number(10,0),
        primary key (key_id)
    );

    create table jbpm_correlation_property_info (
        property_id number(19,0) not null,
        name varchar2(255 char),
        value varchar2(255 char),
        optlock number(10,0),
        correlation_key number(19,0),
        primary key (property_id)
    );

    create table jbpm_deadline (
        id number(19,0) not null,
        deadline_date timestamp,
        escalated number(5,0),
        deadlines_start_dead_line_id number(19,0),
        deadlines_end_dead_line_id number(19,0),
        primary key (id)
    );

    create table jbpm_delegation_delegates (
        task_id number(19,0) not null,
        entity_id varchar2(255 char) not null,
        primary key (task_id, entity_id)
    );

    create table jbpm_email_header (
        id number(19,0) not null,
        body clob,
        from_address varchar2(255 char),
        language varchar2(255 char),
        reply_to_address varchar2(255 char),
        subject varchar2(255 char),
        primary key (id)
    );

    create table jbpm_escalation (
        id number(19,0) not null,
        name varchar2(255 char),
        deadline_escalation_id number(19,0),
        primary key (id)
    );

    create table jbpm_event_types (
        instance_id number(19,0) not null,
        element varchar2(255 char)
    );

    create table jbpm_i18ntext (
        id number(19,0) not null,
        language varchar2(255 char),
        short_text varchar2(255 char),
        text clob,
        task_subjects_id number(19,0),
        task_names_id number(19,0),
        task_descriptions_id number(19,0),
        reassignment_documentation_id number(19,0),
        notification_subjects_id number(19,0),
        notification_names_id number(19,0),
        notification_documentation_id number(19,0),
        notification_descriptions_id number(19,0),
        deadline_documentation_id number(19,0),
        primary key (id)
    );

    create table jbpm_node_instance_log (
        id number(19,0) not null,
        r_connection varchar2(255 char),
        log_date timestamp,
        external_id varchar2(255 char),
        node_id varchar2(255 char),
        node_instance_id varchar2(255 char),
        node_name varchar2(255 char),
        node_type varchar2(255 char),
        process_id varchar2(255 char),
        process_instance_id number(19,0) not null,
        type number(10,0) not null,
        work_item_id number(19,0),
        primary key (id)
    );

    create table jbpm_notificat_email_headers (
        notification number(19,0) not null,
        email_headers number(19,0) not null,
        mapkey varchar2(255 char) not null,
        primary key (notification, mapkey)
    );

    create table jbpm_notification (
        dtype varchar2(31 char) not null,
        id number(19,0) not null,
        priority number(10,0) not null,
        escalation_notifications_id number(19,0),
        primary key (id)
    );

    create table jbpm_notification_bas (
        task_id number(19,0) not null,
        entity_id varchar2(255 char) not null,
        primary key (task_id, entity_id)
    );

    create table jbpm_notification_recipients (
        task_id number(19,0) not null,
        entity_id varchar2(255 char) not null,
        primary key (task_id, entity_id)
    );

    create table jbpm_organizational_entity (
        dtype varchar2(31 char) not null,
        id varchar2(255 char) not null,
        primary key (id)
    );

    create table jbpm_people_ass_excl_owners (
        task_id number(19,0) not null,
        entity_id varchar2(255 char) not null,
        primary key (task_id, entity_id)
    );

    create table jbpm_people_assignm_pot_owners (
        task_id number(19,0) not null,
        entity_id varchar2(255 char) not null,
        primary key (task_id, entity_id)
    );

    create table jbpm_people_assignm_recipients (
        task_id number(19,0) not null,
        entity_id varchar2(255 char) not null,
        primary key (task_id, entity_id)
    );

    create table jbpm_people_assignm_stakehold (
        task_id number(19,0) not null,
        entity_id varchar2(255 char) not null,
        primary key (task_id, entity_id)
    );

    create table jbpm_people_assignments_bas (
        task_id number(19,0) not null,
        entity_id varchar2(255 char) not null,
        primary key (task_id, entity_id)
    );

    create table jbpm_process_instance_info (
        instance_id number(19,0) not null,
        last_modification_date timestamp,
        last_read_date timestamp,
        process_id varchar2(255 char),
        process_instance_byte_array blob,
        start_date timestamp,
        state number(10,0) not null,
        optlock number(10,0),
        primary key (instance_id)
    );

    create table jbpm_process_instance_log (
        id number(19,0) not null,
        duration number(19,0),
        end_date timestamp,
        external_id varchar2(255 char),
        user_identity varchar2(255 char),
        outcome varchar2(255 char),
        parent_process_instance_id number(19,0),
        process_id varchar2(255 char),
        process_instance_id number(19,0) not null,
        process_name varchar2(255 char),
        process_version varchar2(255 char),
        start_date timestamp,
        status number(10,0),
        primary key (id)
    );

    create table jbpm_reass_potential_owners (
        task_id number(19,0) not null,
        entity_id varchar2(255 char) not null,
        primary key (task_id, entity_id)
    );

    create table jbpm_reassignment (
        id number(19,0) not null,
        escalation_reassignments_id number(19,0),
        primary key (id)
    );

    create table jbpm_session_info (
        id number(10,0) not null,
        last_modification_date timestamp,
        rules_byte_array blob,
        start_date timestamp,
        optlock number(10,0),
        primary key (id)
    );

    create table jbpm_task (
        id number(19,0) not null,
        archived number(5,0),
        allowed_to_delegate varchar2(255 char),
        form_name varchar2(255 char),
        priority number(10,0) not null,
        sub_task_strategy varchar2(255 char),
        activation_time timestamp,
        created_on timestamp,
        deployment_id varchar2(255 char),
        document_access_type number(10,0),
        document_content_id number(19,0) not null,
        document_type varchar2(255 char),
        expiration_time timestamp,
        fault_access_type number(10,0),
        fault_content_id number(19,0) not null,
        fault_name varchar2(255 char),
        fault_type varchar2(255 char),
        output_access_type number(10,0),
        output_content_id number(19,0) not null,
        output_type varchar2(255 char),
        parent_id number(19,0) not null,
        previous_status number(10,0),
        process_id varchar2(255 char),
        process_instance_id number(19,0) not null,
        process_session_id number(10,0) not null,
        skipable number(1,0) not null,
        status varchar2(255 char),
        work_item_id number(19,0) not null,
        task_type varchar2(255 char),
        optlock number(10,0),
        task_initiator varchar2(255 char),
        actual_owner varchar2(255 char),
        created_by varchar2(255 char),
        primary key (id)
    );

    create table jbpm_task_comment (
        id number(19,0) not null,
        added_at timestamp,
        text clob,
        added_by varchar2(255 char),
        task_data_comments_id number(19,0),
        primary key (id)
    );

    create table jbpm_task_event (
        id number(19,0) not null,
        log_time timestamp,
        task_id number(19,0),
        type varchar2(255 char),
        user_id varchar2(255 char),
        primary key (id)
    );

    create table jbpm_variable_instance_log (
        id number(19,0) not null,
        log_date timestamp,
        external_id varchar2(255 char),
        old_value varchar2(255 char),
        process_id varchar2(255 char),
        process_instance_id number(19,0) not null,
        value varchar2(255 char),
        variable_id varchar2(255 char),
        variable_instance_id varchar2(255 char),
        primary key (id)
    );

    create table jbpm_work_item_info (
        work_item_id number(19,0) not null,
        creation_date timestamp,
        name varchar2(255 char),
        process_instance_id number(19,0) not null,
        state number(19,0) not null,
        optlock number(10,0),
        work_item_byte_array blob,
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
