CREATE EXTENSION IF NOT EXISTS lo;

ALTER TABLE jahia_contenthistory ADD COLUMN new_column oid;
UPDATE jahia_contenthistory SET new_column = cast(entry_path as oid);
ALTER TABLE jahia_contenthistory DROP COLUMN entry_path;
ALTER TABLE jahia_contenthistory RENAME COLUMN new_column TO entry_path;

ALTER TABLE jahia_nodetypes_provider ADD COLUMN new_column oid;
UPDATE jahia_nodetypes_provider SET new_column = cast(cndfile as oid);
ALTER TABLE jahia_nodetypes_provider DROP COLUMN cndfile;
ALTER TABLE jahia_nodetypes_provider RENAME COLUMN new_column TO cndfile;

ALTER TABLE jbpm_boolean_expression ADD COLUMN new_column oid;
UPDATE jbpm_boolean_expression SET new_column = cast(expression as oid);
ALTER TABLE jbpm_boolean_expression DROP COLUMN expression;
ALTER TABLE jbpm_boolean_expression RENAME COLUMN new_column TO expression;

ALTER TABLE jbpm_email_header ADD COLUMN new_column oid;
UPDATE jbpm_email_header SET new_column = cast(body as oid);
ALTER TABLE jbpm_email_header DROP COLUMN body;
ALTER TABLE jbpm_email_header RENAME COLUMN new_column TO body;

ALTER TABLE jbpm_i18ntext ADD COLUMN new_column oid;
UPDATE jbpm_i18ntext SET new_column = cast(text as oid);
ALTER TABLE jbpm_i18ntext DROP COLUMN text;
ALTER TABLE jbpm_i18ntext RENAME COLUMN new_column TO text;

ALTER TABLE jbpm_task_comment ADD COLUMN new_column oid;
UPDATE jbpm_task_comment SET new_column = cast(text as oid);
ALTER TABLE jbpm_task_comment DROP COLUMN text;
ALTER TABLE jbpm_task_comment RENAME COLUMN new_column TO text;

DROP TRIGGER IF EXISTS t_oid_jahia_contenthistory_entry_path ON jahia_contenthistory;
CREATE TRIGGER t_oid_jahia_contenthistory_entry_path BEFORE DELETE OR UPDATE ON jahia_contenthistory FOR EACH ROW EXECUTE FUNCTION lo_manage('entry_path');
DROP TRIGGER IF EXISTS t_oid_jahia_nodetypes_provider_cndfile ON jahia_nodetypes_provider;
CREATE TRIGGER t_oid_jahia_nodetypes_provider_cndfile BEFORE DELETE OR UPDATE ON jahia_nodetypes_provider FOR EACH ROW EXECUTE FUNCTION lo_manage('cndfile');

DROP TRIGGER IF EXISTS t_oid_jbpm_boolean_expression_expression ON jbpm_boolean_expression;
CREATE TRIGGER t_oid_jbpm_boolean_expression_expression BEFORE DELETE OR UPDATE ON jbpm_boolean_expression FOR EACH ROW EXECUTE FUNCTION lo_manage('expression');
DROP TRIGGER IF EXISTS t_oid_jbpm_content_content ON jbpm_content;
CREATE TRIGGER t_oid_jbpm_content_content BEFORE DELETE OR UPDATE ON jbpm_content FOR EACH ROW EXECUTE FUNCTION lo_manage('content');
DROP TRIGGER IF EXISTS t_oid_jbpm_email_header_body ON jbpm_email_header;
CREATE TRIGGER t_oid_jbpm_email_header_body BEFORE DELETE OR UPDATE ON jbpm_email_header FOR EACH ROW EXECUTE FUNCTION lo_manage('body');
DROP TRIGGER IF EXISTS t_oid_jbpm_i18ntext_text ON jbpm_i18ntext;
CREATE TRIGGER t_oid_jbpm_i18ntext_text BEFORE DELETE OR UPDATE ON jbpm_i18ntext FOR EACH ROW EXECUTE FUNCTION lo_manage('text');
DROP TRIGGER IF EXISTS t_oid_jbpm_process_instance_info_process_instance_byte_array ON jbpm_process_instance_info;
CREATE TRIGGER t_oid_jbpm_process_instance_info_process_instance_byte_array BEFORE DELETE OR UPDATE ON jbpm_process_instance_info FOR EACH ROW EXECUTE FUNCTION lo_manage('process_instance_byte_array');
DROP TRIGGER IF EXISTS t_oid_jbpm_session_info_rules_byte_array ON jbpm_session_info;
CREATE TRIGGER t_oid_jbpm_session_info_rules_byte_array BEFORE DELETE OR UPDATE ON jbpm_session_info FOR EACH ROW EXECUTE FUNCTION lo_manage('rules_byte_array');
DROP TRIGGER IF EXISTS t_oid_jbpm_task_comment_text ON jbpm_task_comment;
CREATE TRIGGER t_oid_jbpm_task_comment_text BEFORE DELETE OR UPDATE ON jbpm_task_comment FOR EACH ROW EXECUTE FUNCTION lo_manage('text');
DROP TRIGGER IF EXISTS t_oid_jbpm_work_item_info_work_item_byte_array ON jbpm_work_item_info;
CREATE TRIGGER t_oid_jbpm_work_item_info_work_item_byte_array BEFORE DELETE OR UPDATE ON jbpm_work_item_info FOR EACH ROW EXECUTE FUNCTION lo_manage('work_item_byte_array');
