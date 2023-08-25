alter table JR_J_LOCAL_REVISIONS add primary key IF NOT EXISTS (JOURNAL_ID);
alter table JR_FSG_FSENTRY add primary key IF NOT EXISTS (FSENTRY_PATH,FSENTRY_NAME);
alter table jbpm_delegation_delegates add primary key IF NOT EXISTS (task_id,entity_id);
alter table jbpm_notification_bas add primary key IF NOT EXISTS (task_id,entity_id);
alter table jbpm_notification_recipients add primary key IF NOT EXISTS (task_id,entity_id);
alter table jbpm_people_assignments_bas add primary key IF NOT EXISTS (task_id,entity_id);
alter table jbpm_people_assignm_pot_owners add primary key IF NOT EXISTS (task_id,entity_id);
alter table jbpm_people_assignm_recipients add primary key IF NOT EXISTS (task_id,entity_id);
alter table jbpm_people_assignm_stakehold add primary key IF NOT EXISTS (task_id,entity_id);
alter table jbpm_people_ass_excl_owners add primary key IF NOT EXISTS (task_id,entity_id);
alter table jbpm_reass_potential_owners add primary key IF NOT EXISTS (task_id,entity_id);
