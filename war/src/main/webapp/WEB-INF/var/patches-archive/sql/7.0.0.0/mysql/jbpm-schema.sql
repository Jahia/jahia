
    alter table jbpm_attachment 
        drop 
        foreign key FK_anhlt1ywoxmhaippy7h78baxk;

    alter table jbpm_attachment 
        drop 
        foreign key FK_s73kjv5ko89qn5xnvxulcd67g;

    alter table jbpm_boolean_expression 
        drop 
        foreign key FK_nbrfj26gq8axs7ayswkuxq0i5;

    alter table jbpm_correlation_property_info 
        drop 
        foreign key FK_91u8nc7udwpc496ldpaett8oa;

    alter table jbpm_deadline 
        drop 
        foreign key FK_qd0gx9omsbf3aoghofg2v3oxr;

    alter table jbpm_deadline 
        drop 
        foreign key FK_rklcbb9hsd16cc9acp7cdvwmx;

    alter table jbpm_delegation_delegates 
        drop 
        foreign key FK_9apnaigsuutvonmkr21peocui;

    alter table jbpm_delegation_delegates 
        drop 
        foreign key FK_oriwrke8vcl3opfut68n472r9;

    alter table jbpm_escalation 
        drop 
        foreign key FK_7g5gvv0kep2olcpvmef7kvoi2;

    alter table jbpm_event_types 
        drop 
        foreign key FK_6n2ptqm4wpn907h23e6tqbbha;

    alter table jbpm_i18ntext 
        drop 
        foreign key FK_5innbucbtx4lfii02bnpve8p;

    alter table jbpm_i18ntext 
        drop 
        foreign key FK_e03i5m0xowu3tlckdntdvcmj2;

    alter table jbpm_i18ntext 
        drop 
        foreign key FK_kqgti9te780wtpviwdnfrufr7;

    alter table jbpm_i18ntext 
        drop 
        foreign key FK_oc0h7p2a4abkflrmdh8iib35t;

    alter table jbpm_i18ntext 
        drop 
        foreign key FK_me9v2v0ea50skfj700g3a2t61;

    alter table jbpm_i18ntext 
        drop 
        foreign key FK_9y5pivj1g7xkuicw96cu8u2i;

    alter table jbpm_i18ntext 
        drop 
        foreign key FK_sf8hq8wp349bf2pt22vjopoma;

    alter table jbpm_i18ntext 
        drop 
        foreign key FK_h2gg5m9ylnih8mfjx1iunwnbe;

    alter table jbpm_i18ntext 
        drop 
        foreign key FK_esqmn5micj3ljsjadbbmyjgfc;

    alter table jbpm_notificat_email_headers 
        drop 
        foreign key FK_epvmc6tmrn1wq6nu8peaywt1k;

    alter table jbpm_notificat_email_headers 
        drop 
        foreign key FK_kf8fd1lw8m7jxkyekbsex81c8;

    alter table jbpm_notification 
        drop 
        foreign key FK_lsx8dgmw3ilrncmkruoqimm28;

    alter table jbpm_notification_bas 
        drop 
        foreign key FK_ai0cdcnws5a5ht5xpe3c01mxp;

    alter table jbpm_notification_bas 
        drop 
        foreign key FK_rrdpb88hvc55jslc4msf6u440;

    alter table jbpm_notification_recipients 
        drop 
        foreign key FK_je2kqu2jgy4j17wsli8ex57c7;

    alter table jbpm_notification_recipients 
        drop 
        foreign key FK_c5x1hqb09c91br1oy949yg5i7;

    alter table jbpm_people_ass_excl_owners 
        drop 
        foreign key FK_9rfplx6e347cgvpt9b442vje8;

    alter table jbpm_people_ass_excl_owners 
        drop 
        foreign key FK_g6xxe0615p0hu79q0d111clnl;

    alter table jbpm_people_assignm_pot_owners 
        drop 
        foreign key FK_1vlj3kfu51ukgo45p3fm4krwx;

    alter table jbpm_people_assignm_pot_owners 
        drop 
        foreign key FK_1sqbvhk1obasgfp839uk387tb;

    alter table jbpm_people_assignm_recipients 
        drop 
        foreign key FK_gcw7a7bs3m50jhvfa5jx7gg60;

    alter table jbpm_people_assignm_recipients 
        drop 
        foreign key FK_l5h85stwvy0aetdg6saoblec0;

    alter table jbpm_people_assignm_stakehold 
        drop 
        foreign key FK_b30nkrrs9b4fmsr6lgf4rrox8;

    alter table jbpm_people_assignm_stakehold 
        drop 
        foreign key FK_eblnc1w5r25dekutmwn3dujn6;

    alter table jbpm_people_assignments_bas 
        drop 
        foreign key FK_ojn5ekkacrgupp069yrqss9pd;

    alter table jbpm_people_assignments_bas 
        drop 
        foreign key FK_ekd3d8qrxvxa7kq3iw5d34bfw;

    alter table jbpm_reass_potential_owners 
        drop 
        foreign key FK_rm075l73m0whh7uxwnu9cx3vr;

    alter table jbpm_reass_potential_owners 
        drop 
        foreign key FK_jsldbpqi48q1w9c9g99d6wnqk;

    alter table jbpm_reassignment 
        drop 
        foreign key FK_fkj43wo6ovwdhwt1q2d8ce0pd;

    alter table jbpm_task 
        drop 
        foreign key FK_3flcq2koknmx53wygbeyuteng;

    alter table jbpm_task 
        drop 
        foreign key FK_r4b5cbeyk3axu7rr8aek6u9w1;

    alter table jbpm_task 
        drop 
        foreign key FK_7i7nh49lv4kdks9ljkomxj2uh;

    alter table jbpm_task_comment 
        drop 
        foreign key FK_bl2skw5rlmgsg4xi0f50061tm;

    alter table jbpm_task_comment 
        drop 
        foreign key FK_nuqjfe1rpp1ad7jlfjjki6tog;

    drop table if exists jbpm_attachment;

    drop table if exists jbpm_bamtask_summary;

    drop table if exists jbpm_boolean_expression;

    drop table if exists jbpm_content;

    drop table if exists jbpm_context_mapping_info;

    drop table if exists jbpm_correlation_key_info;

    drop table if exists jbpm_correlation_property_info;

    drop table if exists jbpm_deadline;

    drop table if exists jbpm_delegation_delegates;

    drop table if exists jbpm_email_header;

    drop table if exists jbpm_escalation;

    drop table if exists jbpm_event_types;

    drop table if exists jbpm_i18ntext;

    drop table if exists jbpm_node_instance_log;

    drop table if exists jbpm_notificat_email_headers;

    drop table if exists jbpm_notification;

    drop table if exists jbpm_notification_bas;

    drop table if exists jbpm_notification_recipients;

    drop table if exists jbpm_organizational_entity;

    drop table if exists jbpm_people_ass_excl_owners;

    drop table if exists jbpm_people_assignm_pot_owners;

    drop table if exists jbpm_people_assignm_recipients;

    drop table if exists jbpm_people_assignm_stakehold;

    drop table if exists jbpm_people_assignments_bas;

    drop table if exists jbpm_process_instance_info;

    drop table if exists jbpm_process_instance_log;

    drop table if exists jbpm_reass_potential_owners;

    drop table if exists jbpm_reassignment;

    drop table if exists jbpm_session_info;

    drop table if exists jbpm_task;

    drop table if exists jbpm_task_comment;

    drop table if exists jbpm_variable_instance_log;

    drop table if exists jbpm_work_item_info;

    create table jbpm_attachment (
        id bigint not null auto_increment,
        access_type integer,
        attached_at datetime,
        attachment_content_id bigint not null,
        content_type varchar(255),
        name varchar(255),
        attachment_size integer,
        attached_by varchar(255),
        task_data_attachments_id bigint,
        primary key (id)
    ) ENGINE=InnoDB;

    create table jbpm_bamtask_summary (
        pk bigint not null auto_increment,
        created_date datetime,
        duration bigint,
        end_date datetime,
        process_instance_id bigint not null,
        start_date datetime,
        status varchar(255),
        task_id bigint not null,
        task_name varchar(255),
        user_id varchar(255),
        primary key (pk)
    ) ENGINE=InnoDB;

    create table jbpm_boolean_expression (
        id bigint not null auto_increment,
        expression longtext,
        type varchar(255),
        escalation_constraints_id bigint,
        primary key (id)
    ) ENGINE=InnoDB;

    create table jbpm_content (
        id bigint not null auto_increment,
        content longblob,
        primary key (id)
    ) ENGINE=InnoDB;

    create table jbpm_context_mapping_info (
        mapping_id bigint not null auto_increment,
        context_id varchar(255) not null,
        ksession_id integer not null,
        optlock integer,
        primary key (mapping_id)
    ) ENGINE=InnoDB;

    create table jbpm_correlation_key_info (
        key_id bigint not null auto_increment,
        name varchar(255),
        process_instance_id bigint not null,
        optlock integer,
        primary key (key_id)
    ) ENGINE=InnoDB;

    create table jbpm_correlation_property_info (
        property_id bigint not null auto_increment,
        name varchar(255),
        value varchar(255),
        optlock integer,
        correlation_key bigint,
        primary key (property_id)
    ) ENGINE=InnoDB;

    create table jbpm_deadline (
        id bigint not null auto_increment,
        deadline_date datetime,
        escalated smallint,
        deadlines_start_dead_line_id bigint,
        deadlines_end_dead_line_id bigint,
        primary key (id)
    ) ENGINE=InnoDB;

    create table jbpm_delegation_delegates (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) ENGINE=InnoDB;

    create table jbpm_email_header (
        id bigint not null auto_increment,
        body longtext,
        from_address varchar(255),
        language varchar(255),
        reply_to_address varchar(255),
        subject varchar(255),
        primary key (id)
    ) ENGINE=InnoDB;

    create table jbpm_escalation (
        id bigint not null auto_increment,
        name varchar(255),
        deadline_escalation_id bigint,
        primary key (id)
    ) ENGINE=InnoDB;

    create table jbpm_event_types (
        instance_id bigint not null,
        element varchar(255)
    ) ENGINE=InnoDB;

    create table jbpm_i18ntext (
        id bigint not null auto_increment,
        language varchar(255),
        short_text varchar(255),
        text longtext,
        task_subjects_id bigint,
        task_names_id bigint,
        task_descriptions_id bigint,
        reassignment_documentation_id bigint,
        notification_subjects_id bigint,
        notification_names_id bigint,
        notification_documentation_id bigint,
        notification_descriptions_id bigint,
        deadline_documentation_id bigint,
        primary key (id)
    ) ENGINE=InnoDB;

    create table jbpm_node_instance_log (
        id bigint not null auto_increment,
        r_connection varchar(255),
        log_date datetime,
        external_id varchar(255),
        node_id varchar(255),
        node_instance_id varchar(255),
        node_name varchar(255),
        node_type varchar(255),
        process_id varchar(255),
        process_instance_id bigint not null,
        type integer not null,
        work_item_id bigint,
        primary key (id)
    ) ENGINE=InnoDB;

    create table jbpm_notificat_email_headers (
        notification bigint not null,
        email_headers bigint not null,
        mapkey varchar(255) not null,
        primary key (notification, mapkey)
    ) ENGINE=InnoDB;

    create table jbpm_notification (
        dtype varchar(31) not null,
        id bigint not null auto_increment,
        priority integer not null,
        escalation_notifications_id bigint,
        primary key (id)
    ) ENGINE=InnoDB;

    create table jbpm_notification_bas (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) ENGINE=InnoDB;

    create table jbpm_notification_recipients (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) ENGINE=InnoDB;

    create table jbpm_organizational_entity (
        dtype varchar(31) not null,
        id varchar(255) not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create table jbpm_people_ass_excl_owners (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) ENGINE=InnoDB;

    create table jbpm_people_assignm_pot_owners (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) ENGINE=InnoDB;

    create table jbpm_people_assignm_recipients (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) ENGINE=InnoDB;

    create table jbpm_people_assignm_stakehold (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) ENGINE=InnoDB;

    create table jbpm_people_assignments_bas (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) ENGINE=InnoDB;

    create table jbpm_process_instance_info (
        instance_id bigint not null auto_increment,
        last_modification_date datetime,
        last_read_date datetime,
        process_id varchar(255),
        process_instance_byte_array longblob,
        start_date datetime,
        state integer not null,
        optlock integer,
        primary key (instance_id)
    ) ENGINE=InnoDB;

    create table jbpm_process_instance_log (
        id bigint not null auto_increment,
        duration bigint,
        end_date datetime,
        external_id varchar(255),
        user_identity varchar(255),
        outcome varchar(255),
        parent_process_instance_id bigint,
        process_id varchar(255),
        process_instance_id bigint not null,
        process_name varchar(255),
        process_version varchar(255),
        start_date datetime,
        status integer,
        primary key (id)
    ) ENGINE=InnoDB;

    create table jbpm_reass_potential_owners (
        task_id bigint not null,
        entity_id varchar(255) not null
    ) ENGINE=InnoDB;

    create table jbpm_reassignment (
        id bigint not null auto_increment,
        escalation_reassignments_id bigint,
        primary key (id)
    ) ENGINE=InnoDB;

    create table jbpm_session_info (
        id integer not null auto_increment,
        last_modification_date datetime,
        rules_byte_array longblob,
        start_date datetime,
        optlock integer,
        primary key (id)
    ) ENGINE=InnoDB;

    create table jbpm_task (
        id bigint not null auto_increment,
        archived smallint,
        allowed_to_delegate varchar(255),
        form_name varchar(255),
        priority integer not null,
        sub_task_strategy varchar(255),
        activation_time datetime,
        created_on datetime,
        deployment_id varchar(255),
        document_access_type integer,
        document_content_id bigint not null,
        document_type varchar(255),
        expiration_time datetime,
        fault_access_type integer,
        fault_content_id bigint not null,
        fault_name varchar(255),
        fault_type varchar(255),
        output_access_type integer,
        output_content_id bigint not null,
        output_type varchar(255),
        parent_id bigint not null,
        previous_status integer,
        process_id varchar(255),
        process_instance_id bigint not null,
        process_session_id integer not null,
        skipable boolean not null,
        status varchar(255),
        work_item_id bigint not null,
        task_type varchar(255),
        optlock integer,
        task_initiator varchar(255),
        actual_owner varchar(255),
        created_by varchar(255),
        primary key (id)
    ) ENGINE=InnoDB;

    create table jbpm_task_comment (
        id bigint not null auto_increment,
        added_at datetime,
        text longtext,
        added_by varchar(255),
        task_data_comments_id bigint,
        primary key (id)
    ) ENGINE=InnoDB;

    create table jbpm_variable_instance_log (
        id bigint not null auto_increment,
        log_date datetime,
        external_id varchar(255),
        old_value varchar(255),
        process_id varchar(255),
        process_instance_id bigint not null,
        value varchar(255),
        variable_id varchar(255),
        variable_instance_id varchar(255),
        primary key (id)
    ) ENGINE=InnoDB;

    create table jbpm_work_item_info (
        work_item_id bigint not null auto_increment,
        creation_date datetime,
        name varchar(255),
        process_instance_id bigint not null,
        state bigint not null,
        optlock integer,
        work_item_byte_array longblob,
        primary key (work_item_id)
    ) ENGINE=InnoDB;

    alter table jbpm_notificat_email_headers 
        add constraint UK_epvmc6tmrn1wq6nu8peaywt1k unique (email_headers);

    alter table jbpm_attachment 
        add index FK_anhlt1ywoxmhaippy7h78baxk (attached_by), 
        add constraint FK_anhlt1ywoxmhaippy7h78baxk 
        foreign key (attached_by) 
        references jbpm_organizational_entity (id);

    alter table jbpm_attachment 
        add index FK_s73kjv5ko89qn5xnvxulcd67g (task_data_attachments_id), 
        add constraint FK_s73kjv5ko89qn5xnvxulcd67g 
        foreign key (task_data_attachments_id) 
        references jbpm_task (id);

    alter table jbpm_boolean_expression 
        add index FK_nbrfj26gq8axs7ayswkuxq0i5 (escalation_constraints_id), 
        add constraint FK_nbrfj26gq8axs7ayswkuxq0i5 
        foreign key (escalation_constraints_id) 
        references jbpm_escalation (id);

    alter table jbpm_correlation_property_info 
        add index FK_91u8nc7udwpc496ldpaett8oa (correlation_key), 
        add constraint FK_91u8nc7udwpc496ldpaett8oa 
        foreign key (correlation_key) 
        references jbpm_correlation_key_info (key_id);

    alter table jbpm_deadline 
        add index FK_qd0gx9omsbf3aoghofg2v3oxr (deadlines_start_dead_line_id), 
        add constraint FK_qd0gx9omsbf3aoghofg2v3oxr 
        foreign key (deadlines_start_dead_line_id) 
        references jbpm_task (id);

    alter table jbpm_deadline 
        add index FK_rklcbb9hsd16cc9acp7cdvwmx (deadlines_end_dead_line_id), 
        add constraint FK_rklcbb9hsd16cc9acp7cdvwmx 
        foreign key (deadlines_end_dead_line_id) 
        references jbpm_task (id);

    alter table jbpm_delegation_delegates 
        add index FK_9apnaigsuutvonmkr21peocui (entity_id), 
        add constraint FK_9apnaigsuutvonmkr21peocui 
        foreign key (entity_id) 
        references jbpm_organizational_entity (id);

    alter table jbpm_delegation_delegates 
        add index FK_oriwrke8vcl3opfut68n472r9 (task_id), 
        add constraint FK_oriwrke8vcl3opfut68n472r9 
        foreign key (task_id) 
        references jbpm_task (id);

    alter table jbpm_escalation 
        add index FK_7g5gvv0kep2olcpvmef7kvoi2 (deadline_escalation_id), 
        add constraint FK_7g5gvv0kep2olcpvmef7kvoi2 
        foreign key (deadline_escalation_id) 
        references jbpm_deadline (id);

    alter table jbpm_event_types 
        add index FK_6n2ptqm4wpn907h23e6tqbbha (instance_id), 
        add constraint FK_6n2ptqm4wpn907h23e6tqbbha 
        foreign key (instance_id) 
        references jbpm_process_instance_info (instance_id);

    alter table jbpm_i18ntext 
        add index FK_5innbucbtx4lfii02bnpve8p (task_subjects_id), 
        add constraint FK_5innbucbtx4lfii02bnpve8p 
        foreign key (task_subjects_id) 
        references jbpm_task (id);

    alter table jbpm_i18ntext 
        add index FK_e03i5m0xowu3tlckdntdvcmj2 (task_names_id), 
        add constraint FK_e03i5m0xowu3tlckdntdvcmj2 
        foreign key (task_names_id) 
        references jbpm_task (id);

    alter table jbpm_i18ntext 
        add index FK_kqgti9te780wtpviwdnfrufr7 (task_descriptions_id), 
        add constraint FK_kqgti9te780wtpviwdnfrufr7 
        foreign key (task_descriptions_id) 
        references jbpm_task (id);

    alter table jbpm_i18ntext 
        add index FK_oc0h7p2a4abkflrmdh8iib35t (reassignment_documentation_id), 
        add constraint FK_oc0h7p2a4abkflrmdh8iib35t 
        foreign key (reassignment_documentation_id) 
        references jbpm_reassignment (id);

    alter table jbpm_i18ntext 
        add index FK_me9v2v0ea50skfj700g3a2t61 (notification_subjects_id), 
        add constraint FK_me9v2v0ea50skfj700g3a2t61 
        foreign key (notification_subjects_id) 
        references jbpm_notification (id);

    alter table jbpm_i18ntext 
        add index FK_9y5pivj1g7xkuicw96cu8u2i (notification_names_id), 
        add constraint FK_9y5pivj1g7xkuicw96cu8u2i 
        foreign key (notification_names_id) 
        references jbpm_notification (id);

    alter table jbpm_i18ntext 
        add index FK_sf8hq8wp349bf2pt22vjopoma (notification_documentation_id), 
        add constraint FK_sf8hq8wp349bf2pt22vjopoma 
        foreign key (notification_documentation_id) 
        references jbpm_notification (id);

    alter table jbpm_i18ntext 
        add index FK_h2gg5m9ylnih8mfjx1iunwnbe (notification_descriptions_id), 
        add constraint FK_h2gg5m9ylnih8mfjx1iunwnbe 
        foreign key (notification_descriptions_id) 
        references jbpm_notification (id);

    alter table jbpm_i18ntext 
        add index FK_esqmn5micj3ljsjadbbmyjgfc (deadline_documentation_id), 
        add constraint FK_esqmn5micj3ljsjadbbmyjgfc 
        foreign key (deadline_documentation_id) 
        references jbpm_deadline (id);

    alter table jbpm_notificat_email_headers 
        add index FK_epvmc6tmrn1wq6nu8peaywt1k (email_headers), 
        add constraint FK_epvmc6tmrn1wq6nu8peaywt1k 
        foreign key (email_headers) 
        references jbpm_email_header (id);

    alter table jbpm_notificat_email_headers 
        add index FK_kf8fd1lw8m7jxkyekbsex81c8 (notification), 
        add constraint FK_kf8fd1lw8m7jxkyekbsex81c8 
        foreign key (notification) 
        references jbpm_notification (id);

    alter table jbpm_notification 
        add index FK_lsx8dgmw3ilrncmkruoqimm28 (escalation_notifications_id), 
        add constraint FK_lsx8dgmw3ilrncmkruoqimm28 
        foreign key (escalation_notifications_id) 
        references jbpm_escalation (id);

    alter table jbpm_notification_bas 
        add index FK_ai0cdcnws5a5ht5xpe3c01mxp (entity_id), 
        add constraint FK_ai0cdcnws5a5ht5xpe3c01mxp 
        foreign key (entity_id) 
        references jbpm_organizational_entity (id);

    alter table jbpm_notification_bas 
        add index FK_rrdpb88hvc55jslc4msf6u440 (task_id), 
        add constraint FK_rrdpb88hvc55jslc4msf6u440 
        foreign key (task_id) 
        references jbpm_notification (id);

    alter table jbpm_notification_recipients 
        add index FK_je2kqu2jgy4j17wsli8ex57c7 (entity_id), 
        add constraint FK_je2kqu2jgy4j17wsli8ex57c7 
        foreign key (entity_id) 
        references jbpm_organizational_entity (id);

    alter table jbpm_notification_recipients 
        add index FK_c5x1hqb09c91br1oy949yg5i7 (task_id), 
        add constraint FK_c5x1hqb09c91br1oy949yg5i7 
        foreign key (task_id) 
        references jbpm_notification (id);

    alter table jbpm_people_ass_excl_owners 
        add index FK_9rfplx6e347cgvpt9b442vje8 (entity_id), 
        add constraint FK_9rfplx6e347cgvpt9b442vje8 
        foreign key (entity_id) 
        references jbpm_organizational_entity (id);

    alter table jbpm_people_ass_excl_owners 
        add index FK_g6xxe0615p0hu79q0d111clnl (task_id), 
        add constraint FK_g6xxe0615p0hu79q0d111clnl 
        foreign key (task_id) 
        references jbpm_task (id);

    alter table jbpm_people_assignm_pot_owners 
        add index FK_1vlj3kfu51ukgo45p3fm4krwx (entity_id), 
        add constraint FK_1vlj3kfu51ukgo45p3fm4krwx 
        foreign key (entity_id) 
        references jbpm_organizational_entity (id);

    alter table jbpm_people_assignm_pot_owners 
        add index FK_1sqbvhk1obasgfp839uk387tb (task_id), 
        add constraint FK_1sqbvhk1obasgfp839uk387tb 
        foreign key (task_id) 
        references jbpm_task (id);

    alter table jbpm_people_assignm_recipients 
        add index FK_gcw7a7bs3m50jhvfa5jx7gg60 (entity_id), 
        add constraint FK_gcw7a7bs3m50jhvfa5jx7gg60 
        foreign key (entity_id) 
        references jbpm_organizational_entity (id);

    alter table jbpm_people_assignm_recipients 
        add index FK_l5h85stwvy0aetdg6saoblec0 (task_id), 
        add constraint FK_l5h85stwvy0aetdg6saoblec0 
        foreign key (task_id) 
        references jbpm_task (id);

    alter table jbpm_people_assignm_stakehold 
        add index FK_b30nkrrs9b4fmsr6lgf4rrox8 (entity_id), 
        add constraint FK_b30nkrrs9b4fmsr6lgf4rrox8 
        foreign key (entity_id) 
        references jbpm_organizational_entity (id);

    alter table jbpm_people_assignm_stakehold 
        add index FK_eblnc1w5r25dekutmwn3dujn6 (task_id), 
        add constraint FK_eblnc1w5r25dekutmwn3dujn6 
        foreign key (task_id) 
        references jbpm_task (id);

    alter table jbpm_people_assignments_bas 
        add index FK_ojn5ekkacrgupp069yrqss9pd (entity_id), 
        add constraint FK_ojn5ekkacrgupp069yrqss9pd 
        foreign key (entity_id) 
        references jbpm_organizational_entity (id);

    alter table jbpm_people_assignments_bas 
        add index FK_ekd3d8qrxvxa7kq3iw5d34bfw (task_id), 
        add constraint FK_ekd3d8qrxvxa7kq3iw5d34bfw 
        foreign key (task_id) 
        references jbpm_task (id);

    alter table jbpm_reass_potential_owners 
        add index FK_rm075l73m0whh7uxwnu9cx3vr (entity_id), 
        add constraint FK_rm075l73m0whh7uxwnu9cx3vr 
        foreign key (entity_id) 
        references jbpm_organizational_entity (id);

    alter table jbpm_reass_potential_owners 
        add index FK_jsldbpqi48q1w9c9g99d6wnqk (task_id), 
        add constraint FK_jsldbpqi48q1w9c9g99d6wnqk 
        foreign key (task_id) 
        references jbpm_reassignment (id);

    alter table jbpm_reassignment 
        add index FK_fkj43wo6ovwdhwt1q2d8ce0pd (escalation_reassignments_id), 
        add constraint FK_fkj43wo6ovwdhwt1q2d8ce0pd 
        foreign key (escalation_reassignments_id) 
        references jbpm_escalation (id);

    alter table jbpm_task 
        add index FK_3flcq2koknmx53wygbeyuteng (task_initiator), 
        add constraint FK_3flcq2koknmx53wygbeyuteng 
        foreign key (task_initiator) 
        references jbpm_organizational_entity (id);

    alter table jbpm_task 
        add index FK_r4b5cbeyk3axu7rr8aek6u9w1 (actual_owner), 
        add constraint FK_r4b5cbeyk3axu7rr8aek6u9w1 
        foreign key (actual_owner) 
        references jbpm_organizational_entity (id);

    alter table jbpm_task 
        add index FK_7i7nh49lv4kdks9ljkomxj2uh (created_by), 
        add constraint FK_7i7nh49lv4kdks9ljkomxj2uh 
        foreign key (created_by) 
        references jbpm_organizational_entity (id);

    alter table jbpm_task_comment 
        add index FK_bl2skw5rlmgsg4xi0f50061tm (added_by), 
        add constraint FK_bl2skw5rlmgsg4xi0f50061tm 
        foreign key (added_by) 
        references jbpm_organizational_entity (id);

    alter table jbpm_task_comment 
        add index FK_nuqjfe1rpp1ad7jlfjjki6tog (task_data_comments_id), 
        add constraint FK_nuqjfe1rpp1ad7jlfjjki6tog 
        foreign key (task_data_comments_id) 
        references jbpm_task (id);
