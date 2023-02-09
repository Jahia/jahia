
    alter table if exists jbpm_attachment
        drop constraint FK_anhlt1ywoxmhaippy7h78baxk;

    alter table if exists jbpm_attachment
        drop constraint FK_s73kjv5ko89qn5xnvxulcd67g;

    alter table if exists jbpm_boolean_expression
        drop constraint FK_nbrfj26gq8axs7ayswkuxq0i5;

    alter table if exists jbpm_correlation_property_info
        drop constraint FK_91u8nc7udwpc496ldpaett8oa;

    alter table if exists jbpm_deadline
        drop constraint FK_qd0gx9omsbf3aoghofg2v3oxr;

    alter table if exists jbpm_deadline
        drop constraint FK_rklcbb9hsd16cc9acp7cdvwmx;

    alter table if exists jbpm_delegation_delegates
        drop constraint FK_9apnaigsuutvonmkr21peocui;

    alter table if exists jbpm_delegation_delegates
        drop constraint FK_oriwrke8vcl3opfut68n472r9;

    alter table if exists jbpm_escalation
        drop constraint FK_7g5gvv0kep2olcpvmef7kvoi2;

    alter table if exists jbpm_event_types
        drop constraint FK_6n2ptqm4wpn907h23e6tqbbha;

    alter table if exists jbpm_i18ntext
        drop constraint FK_5innbucbtx4lfii02bnpve8p;

    alter table if exists jbpm_i18ntext
        drop constraint FK_e03i5m0xowu3tlckdntdvcmj2;

    alter table if exists jbpm_i18ntext
        drop constraint FK_kqgti9te780wtpviwdnfrufr7;

    alter table if exists jbpm_i18ntext
        drop constraint FK_oc0h7p2a4abkflrmdh8iib35t;

    alter table if exists jbpm_i18ntext
        drop constraint FK_me9v2v0ea50skfj700g3a2t61;

    alter table if exists jbpm_i18ntext
        drop constraint FK_9y5pivj1g7xkuicw96cu8u2i;

    alter table if exists jbpm_i18ntext
        drop constraint FK_sf8hq8wp349bf2pt22vjopoma;

    alter table if exists jbpm_i18ntext
        drop constraint FK_h2gg5m9ylnih8mfjx1iunwnbe;

    alter table if exists jbpm_i18ntext
        drop constraint FK_esqmn5micj3ljsjadbbmyjgfc;

    alter table if exists jbpm_notificat_email_headers
        drop constraint FK_epvmc6tmrn1wq6nu8peaywt1k;

    alter table if exists jbpm_notificat_email_headers
        drop constraint FK_kf8fd1lw8m7jxkyekbsex81c8;

    alter table if exists jbpm_notification
        drop constraint FK_lsx8dgmw3ilrncmkruoqimm28;

    alter table if exists jbpm_notification_bas
        drop constraint FK_ai0cdcnws5a5ht5xpe3c01mxp;

    alter table if exists jbpm_notification_bas
        drop constraint FK_rrdpb88hvc55jslc4msf6u440;

    alter table if exists jbpm_notification_recipients
        drop constraint FK_je2kqu2jgy4j17wsli8ex57c7;

    alter table if exists jbpm_notification_recipients
        drop constraint FK_c5x1hqb09c91br1oy949yg5i7;

    alter table if exists jbpm_people_ass_excl_owners
        drop constraint FK_9rfplx6e347cgvpt9b442vje8;

    alter table if exists jbpm_people_ass_excl_owners
        drop constraint FK_g6xxe0615p0hu79q0d111clnl;

    alter table if exists jbpm_people_assignm_pot_owners
        drop constraint FK_1vlj3kfu51ukgo45p3fm4krwx;

    alter table if exists jbpm_people_assignm_pot_owners
        drop constraint FK_1sqbvhk1obasgfp839uk387tb;

    alter table if exists jbpm_people_assignm_recipients
        drop constraint FK_gcw7a7bs3m50jhvfa5jx7gg60;

    alter table if exists jbpm_people_assignm_recipients
        drop constraint FK_l5h85stwvy0aetdg6saoblec0;

    alter table if exists jbpm_people_assignm_stakehold
        drop constraint FK_b30nkrrs9b4fmsr6lgf4rrox8;

    alter table if exists jbpm_people_assignm_stakehold
        drop constraint FK_eblnc1w5r25dekutmwn3dujn6;

    alter table if exists jbpm_people_assignments_bas
        drop constraint FK_ojn5ekkacrgupp069yrqss9pd;

    alter table if exists jbpm_people_assignments_bas
        drop constraint FK_ekd3d8qrxvxa7kq3iw5d34bfw;

    alter table if exists jbpm_reass_potential_owners
        drop constraint FK_rm075l73m0whh7uxwnu9cx3vr;

    alter table if exists jbpm_reass_potential_owners
        drop constraint FK_jsldbpqi48q1w9c9g99d6wnqk;

    alter table if exists jbpm_reassignment
        drop constraint FK_fkj43wo6ovwdhwt1q2d8ce0pd;

    alter table if exists jbpm_task
        drop constraint FK_3flcq2koknmx53wygbeyuteng;

    alter table if exists jbpm_task
        drop constraint FK_r4b5cbeyk3axu7rr8aek6u9w1;

    alter table if exists jbpm_task
        drop constraint FK_7i7nh49lv4kdks9ljkomxj2uh;

    alter table if exists jbpm_task_comment
        drop constraint FK_bl2skw5rlmgsg4xi0f50061tm;

    alter table if exists jbpm_task_comment
        drop constraint FK_nuqjfe1rpp1ad7jlfjjki6tog;

    drop table if exists jbpm_attachment cascade;

    drop table if exists jbpm_bamtask_summary cascade;

    drop table if exists jbpm_boolean_expression cascade;

    drop table if exists jbpm_content cascade;

    drop table if exists jbpm_context_mapping_info cascade;

    drop table if exists jbpm_correlation_key_info cascade;

    drop table if exists jbpm_correlation_property_info cascade;

    drop table if exists jbpm_deadline cascade;

    drop table if exists jbpm_delegation_delegates cascade;

    drop table if exists jbpm_email_header cascade;

    drop table if exists jbpm_escalation cascade;

    drop table if exists jbpm_event_types cascade;

    drop table if exists jbpm_i18ntext cascade;

    drop table if exists jbpm_node_instance_log cascade;

    drop table if exists jbpm_notificat_email_headers cascade;

    drop table if exists jbpm_notification cascade;

    drop table if exists jbpm_notification_bas cascade;

    drop table if exists jbpm_notification_recipients cascade;

    drop table if exists jbpm_organizational_entity cascade;

    drop table if exists jbpm_people_ass_excl_owners cascade;

    drop table if exists jbpm_people_assignm_pot_owners cascade;

    drop table if exists jbpm_people_assignm_recipients cascade;

    drop table if exists jbpm_people_assignm_stakehold cascade;

    drop table if exists jbpm_people_assignments_bas cascade;

    drop table if exists jbpm_process_instance_info cascade;

    drop table if exists jbpm_process_instance_log cascade;

    drop table if exists jbpm_reass_potential_owners cascade;

    drop table if exists jbpm_reassignment cascade;

    drop table if exists jbpm_session_info cascade;

    drop table if exists jbpm_task cascade;

    drop table if exists jbpm_task_comment cascade;

    drop table if exists jbpm_task_event cascade;

    drop table if exists jbpm_variable_instance_log cascade;

    drop table if exists jbpm_work_item_info cascade;

    drop sequence if exists ATTACHMENT_ID_SEQ;

    drop sequence if exists BAM_TASK_ID_SEQ;

    drop sequence if exists BOOLEANEXPR_ID_SEQ;

    drop sequence if exists COMMENT_ID_SEQ;

    drop sequence if exists CONTENT_ID_SEQ;

    drop sequence if exists CONTEXT_MAPPING_INFO_ID_SEQ;

    drop sequence if exists CORRELATION_KEY_ID_SEQ;

    drop sequence if exists CORRELATION_PROP_ID_SEQ;

    drop sequence if exists DEADLINE_ID_SEQ;

    drop sequence if exists EMAILNOTIFHEAD_ID_SEQ;

    drop sequence if exists ESCALATION_ID_SEQ;

    drop sequence if exists I18NTEXT_ID_SEQ;

    drop sequence if exists NODE_INST_LOG_ID_SEQ;

    drop sequence if exists NOTIFICATION_ID_SEQ;

    drop sequence if exists PROCESS_INSTANCE_INFO_ID_SEQ;

    drop sequence if exists PROC_INST_LOG_ID_SEQ;

    drop sequence if exists REASSIGNMENT_ID_SEQ;

    drop sequence if exists SESSIONINFO_ID_SEQ;

    drop sequence if exists TASK_EVENT_ID_SEQ;

    drop sequence if exists TASK_ID_SEQ;

    drop sequence if exists VAR_INST_LOG_ID_SEQ;

    drop sequence if exists WORKITEMINFO_ID_SEQ;

    DROP TRIGGER IF EXISTS t_oid_jbpm_boolean_expression_expression ON jbpm_boolean_expression;
    DROP TRIGGER IF EXISTS t_oid_jbpm_content_content ON jbpm_content;
    DROP TRIGGER IF EXISTS t_oid_jbpm_email_header_body ON jbpm_email_header;
    DROP TRIGGER IF EXISTS t_oid_jbpm_i18ntext_text ON jbpm_i18ntext;
    DROP TRIGGER IF EXISTS t_oid_jbpm_process_instance_info_process_instance_byte_array ON jbpm_process_instance_info;
    DROP TRIGGER IF EXISTS t_oid_jbpm_session_info_rules_byte_array ON jbpm_session_info;
    DROP TRIGGER IF EXISTS t_oid_jbpm_task_comment_text ON jbpm_task_comment;
    DROP TRIGGER IF EXISTS t_oid_jbpm_work_item_info_work_item_byte_array ON jbpm_work_item_info;
