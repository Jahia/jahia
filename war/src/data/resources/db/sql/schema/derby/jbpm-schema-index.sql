    create index IDX_anhlt1ywoxmhaippy7h78baxk on jbpm_attachment (attached_by);

    create index IDX_s73kjv5ko89qn5xnvxulcd67g on jbpm_attachment (task_data_attachments_id);

    create index IDX_nbrfj26gq8axs7ayswkuxq0i5 on jbpm_boolean_expression (escalation_constraints_id);

    create index IDX_91u8nc7udwpc496ldpaett8oa on jbpm_correlation_property_info (correlation_key);

    create index IDX_qd0gx9omsbf3aoghofg2v3oxr on jbpm_deadline (deadlines_start_dead_line_id);

    create index IDX_rklcbb9hsd16cc9acp7cdvwmx on jbpm_deadline (deadlines_end_dead_line_id);

    create index IDX_9apnaigsuutvonmkr21peocui on jbpm_delegation_delegates (entity_id);

    create index IDX_oriwrke8vcl3opfut68n472r9 on jbpm_delegation_delegates (task_id);

    create index IDX_7g5gvv0kep2olcpvmef7kvoi2 on jbpm_escalation (deadline_escalation_id);

    create index IDX_6n2ptqm4wpn907h23e6tqbbha on jbpm_event_types (instance_id);

    create index IDX_5innbucbtx4lfii02bnpve8p on jbpm_i18ntext (task_subjects_id);

    create index IDX_e03i5m0xowu3tlckdntdvcmj2 on jbpm_i18ntext (task_names_id);

    create index IDX_kqgti9te780wtpviwdnfrufr7 on jbpm_i18ntext (task_descriptions_id);

    create index IDX_oc0h7p2a4abkflrmdh8iib35t on jbpm_i18ntext (reassignment_documentation_id);

    create index IDX_me9v2v0ea50skfj700g3a2t61 on jbpm_i18ntext (notification_subjects_id);

    create index IDX_9y5pivj1g7xkuicw96cu8u2i on jbpm_i18ntext (notification_names_id);

    create index IDX_sf8hq8wp349bf2pt22vjopoma on jbpm_i18ntext (notification_documentation_id);

    create index IDX_h2gg5m9ylnih8mfjx1iunwnbe on jbpm_i18ntext (notification_descriptions_id);

    create index IDX_esqmn5micj3ljsjadbbmyjgfc on jbpm_i18ntext (deadline_documentation_id);

    create index IDX_kf8fd1lw8m7jxkyekbsex81c8 on jbpm_notificat_email_headers (notification);

    create index IDX_lsx8dgmw3ilrncmkruoqimm28 on jbpm_notification (escalation_notifications_id);

    create index IDX_ai0cdcnws5a5ht5xpe3c01mxp on jbpm_notification_bas (entity_id);

    create index IDX_rrdpb88hvc55jslc4msf6u440 on jbpm_notification_bas (task_id);

    create index IDX_je2kqu2jgy4j17wsli8ex57c7 on jbpm_notification_recipients (entity_id);

    create index IDX_c5x1hqb09c91br1oy949yg5i7 on jbpm_notification_recipients (task_id);

    create index IDX_9rfplx6e347cgvpt9b442vje8 on jbpm_people_ass_excl_owners (entity_id);

    create index IDX_g6xxe0615p0hu79q0d111clnl on jbpm_people_ass_excl_owners (task_id);

    create index IDX_1vlj3kfu51ukgo45p3fm4krwx on jbpm_people_assignm_pot_owners (entity_id);

    create index IDX_1sqbvhk1obasgfp839uk387tb on jbpm_people_assignm_pot_owners (task_id);

    create index IDX_gcw7a7bs3m50jhvfa5jx7gg60 on jbpm_people_assignm_recipients (entity_id);

    create index IDX_l5h85stwvy0aetdg6saoblec0 on jbpm_people_assignm_recipients (task_id);

    create index IDX_b30nkrrs9b4fmsr6lgf4rrox8 on jbpm_people_assignm_stakehold (entity_id);

    create index IDX_eblnc1w5r25dekutmwn3dujn6 on jbpm_people_assignm_stakehold (task_id);

    create index IDX_ojn5ekkacrgupp069yrqss9pd on jbpm_people_assignments_bas (entity_id);

    create index IDX_ekd3d8qrxvxa7kq3iw5d34bfw on jbpm_people_assignments_bas (task_id);

    create index IDX_rm075l73m0whh7uxwnu9cx3vr on jbpm_reass_potential_owners (entity_id);

    create index IDX_jsldbpqi48q1w9c9g99d6wnqk on jbpm_reass_potential_owners (task_id);

    create index IDX_fkj43wo6ovwdhwt1q2d8ce0pd on jbpm_reassignment (escalation_reassignments_id);

    create index IDX_3flcq2koknmx53wygbeyuteng on jbpm_task (task_initiator);

    create index IDX_r4b5cbeyk3axu7rr8aek6u9w1 on jbpm_task (actual_owner);

    create index IDX_7i7nh49lv4kdks9ljkomxj2uh on jbpm_task (created_by);

    create index IDX_bl2skw5rlmgsg4xi0f50061tm on jbpm_task_comment (added_by);

    create index IDX_nuqjfe1rpp1ad7jlfjjki6tog on jbpm_task_comment (task_data_comments_id);