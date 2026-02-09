-- Fix Quartz tables VARCHAR2 columns to support Oracle JDBC 23c which now the new boolean data type,
-- which gets converted to 'true'/'false' strings instead of '0'/'1' strings when persisted into VARCHAR2 columns.
-- This addresses ORA-12899 error when Oracle driver tries to insert 'false' (5 bytes) into VARCHAR2(1) columns

ALTER TABLE jahia_qrtz_job_details MODIFY (
    IS_DURABLE VARCHAR2(5 CHAR),
    IS_VOLATILE VARCHAR2(5 CHAR),
    IS_STATEFUL VARCHAR2(5 CHAR),
    REQUESTS_RECOVERY VARCHAR2(5 CHAR)
);

ALTER TABLE jahia_qrtz_triggers MODIFY (
    IS_VOLATILE VARCHAR2(5 CHAR)
);

ALTER TABLE jahia_qrtz_fired_triggers MODIFY (
    IS_VOLATILE VARCHAR2(5 CHAR),
    IS_STATEFUL VARCHAR2(5 CHAR),
    REQUESTS_RECOVERY VARCHAR2(5 CHAR)
);

commit;
