-- Thanks to Patrick Lightbody for submitting this...
--
-- In your Quartz properties file, you'll need to set 
-- org.quartz.jobStore.driverDelegateClass = org.quartz.impl.jdbcjobstore.PostgreSQLDelegate

drop table jahia_qrtz_job_listeners;
drop table jahia_qrtz_trigger_listeners;
drop table jahia_qrtz_fired_triggers;
drop table jahia_qrtz_paused_trigger_grps;
drop table jahia_qrtz_scheduler_state;
drop table jahia_qrtz_locks;
drop table jahia_qrtz_simple_triggers;
drop table jahia_qrtz_cron_triggers;
drop table jahia_qrtz_blob_triggers;
drop table jahia_qrtz_triggers;
drop table jahia_qrtz_job_details;
drop table jahia_qrtz_calendars;

CREATE TABLE jahia_qrtz_job_details
  (
    JOB_NAME  VARCHAR(200) NOT NULL,
    JOB_GROUP VARCHAR(200) NOT NULL,
    DESCRIPTION VARCHAR(250) NULL,
    JOB_CLASS_NAME   VARCHAR(250) NOT NULL, 
    IS_DURABLE BOOL NOT NULL,
    IS_VOLATILE BOOL NOT NULL,
    IS_STATEFUL BOOL NOT NULL,
    REQUESTS_RECOVERY BOOL NOT NULL,
    JOB_DATA BYTEA NULL,
    PRIMARY KEY (JOB_NAME,JOB_GROUP)
);

CREATE TABLE jahia_qrtz_job_listeners
  (
    JOB_NAME  VARCHAR(200) NOT NULL, 
    JOB_GROUP VARCHAR(200) NOT NULL,
    JOB_LISTENER VARCHAR(200) NOT NULL,
    PRIMARY KEY (JOB_NAME,JOB_GROUP,JOB_LISTENER),
    FOREIGN KEY (JOB_NAME,JOB_GROUP) 
	REFERENCES JAHIA_QRTZ_JOB_DETAILS(JOB_NAME,JOB_GROUP) 
);

CREATE TABLE jahia_qrtz_triggers
  (
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    JOB_NAME  VARCHAR(200) NOT NULL, 
    JOB_GROUP VARCHAR(200) NOT NULL,
    IS_VOLATILE BOOL NOT NULL,
    DESCRIPTION VARCHAR(250) NULL,
    NEXT_FIRE_TIME BIGINT NULL,
    PREV_FIRE_TIME BIGINT NULL,
    PRIORITY INTEGER NULL,
    TRIGGER_STATE VARCHAR(16) NOT NULL,
    TRIGGER_TYPE VARCHAR(8) NOT NULL,
    START_TIME BIGINT NOT NULL,
    END_TIME BIGINT NULL,
    CALENDAR_NAME VARCHAR(200) NULL,
    MISFIRE_INSTR SMALLINT NULL,
    JOB_DATA BYTEA NULL,
    PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (JOB_NAME,JOB_GROUP) 
	REFERENCES JAHIA_QRTZ_JOB_DETAILS(JOB_NAME,JOB_GROUP) 
);

CREATE TABLE jahia_qrtz_simple_triggers
  (
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    REPEAT_COUNT BIGINT NOT NULL,
    REPEAT_INTERVAL BIGINT NOT NULL,
    TIMES_TRIGGERED BIGINT NOT NULL,
    PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (TRIGGER_NAME,TRIGGER_GROUP) 
	REFERENCES JAHIA_QRTZ_TRIGGERS(TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE jahia_qrtz_cron_triggers
  (
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    CRON_EXPRESSION VARCHAR(120) NOT NULL,
    TIME_ZONE_ID VARCHAR(80),
    PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (TRIGGER_NAME,TRIGGER_GROUP) 
	REFERENCES JAHIA_QRTZ_TRIGGERS(TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE jahia_qrtz_blob_triggers
  (
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    BLOB_DATA BYTEA NULL,
    PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP),
    FOREIGN KEY (TRIGGER_NAME,TRIGGER_GROUP) 
        REFERENCES JAHIA_QRTZ_TRIGGERS(TRIGGER_NAME,TRIGGER_GROUP)
);

CREATE TABLE jahia_qrtz_trigger_listeners
  (
    TRIGGER_NAME  VARCHAR(200) NOT NULL, 
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    TRIGGER_LISTENER VARCHAR(200) NOT NULL,
    PRIMARY KEY (TRIGGER_NAME,TRIGGER_GROUP,TRIGGER_LISTENER),
    FOREIGN KEY (TRIGGER_NAME,TRIGGER_GROUP) 
	REFERENCES JAHIA_QRTZ_TRIGGERS(TRIGGER_NAME,TRIGGER_GROUP)
);


CREATE TABLE jahia_qrtz_calendars
  (
    CALENDAR_NAME  VARCHAR(200) NOT NULL, 
    CALENDAR BYTEA NOT NULL,
    PRIMARY KEY (CALENDAR_NAME)
);


CREATE TABLE jahia_qrtz_paused_trigger_grps
  (
    TRIGGER_GROUP  VARCHAR(200) NOT NULL, 
    PRIMARY KEY (TRIGGER_GROUP)
);

CREATE TABLE jahia_qrtz_fired_triggers 
  (
    ENTRY_ID VARCHAR(95) NOT NULL,
    TRIGGER_NAME VARCHAR(200) NOT NULL,
    TRIGGER_GROUP VARCHAR(200) NOT NULL,
    IS_VOLATILE BOOL NOT NULL,
    INSTANCE_NAME VARCHAR(200) NOT NULL,
    FIRED_TIME BIGINT NOT NULL,
    PRIORITY INTEGER NOT NULL,
    STATE VARCHAR(16) NOT NULL,
    JOB_NAME VARCHAR(200) NULL,
    JOB_GROUP VARCHAR(200) NULL,
    IS_STATEFUL BOOL NULL,
    REQUESTS_RECOVERY BOOL NULL,
    PRIMARY KEY (ENTRY_ID)
);

CREATE TABLE jahia_qrtz_scheduler_state 
  (
    INSTANCE_NAME VARCHAR(200) NOT NULL,
    LAST_CHECKIN_TIME BIGINT NOT NULL,
    CHECKIN_INTERVAL BIGINT NOT NULL,
    PRIMARY KEY (INSTANCE_NAME)
);

CREATE TABLE jahia_qrtz_locks
  (
    LOCK_NAME  VARCHAR(40) NOT NULL, 
    PRIMARY KEY (LOCK_NAME)
);


INSERT INTO jahia_qrtz_locks values('TRIGGER_ACCESS');
INSERT INTO jahia_qrtz_locks values('JOB_ACCESS');
INSERT INTO jahia_qrtz_locks values('CALENDAR_ACCESS');
INSERT INTO jahia_qrtz_locks values('STATE_ACCESS');
INSERT INTO jahia_qrtz_locks values('MISFIRE_ACCESS');

create index idx_jahia_qrtz_j_req_recovery on jahia_qrtz_job_details(REQUESTS_RECOVERY);
create index idx_jahia_qrtz_t_next_fire_time on jahia_qrtz_triggers(NEXT_FIRE_TIME);
create index idx_jahia_qrtz_t_state on jahia_qrtz_triggers(TRIGGER_STATE);
create index idx_jahia_qrtz_t_nft_st on jahia_qrtz_triggers(NEXT_FIRE_TIME,TRIGGER_STATE);
create index idx_jahia_qrtz_t_volatile on jahia_qrtz_triggers(IS_VOLATILE);
create index idx_jahia_qrtz_ft_trig_name on jahia_qrtz_fired_triggers(TRIGGER_NAME);
create index idx_jahia_qrtz_ft_trig_group on jahia_qrtz_fired_triggers(TRIGGER_GROUP);
create index idx_jahia_qrtz_ft_trig_nm_gp on jahia_qrtz_fired_triggers(TRIGGER_NAME,TRIGGER_GROUP);
create index idx_jahia_qrtz_ft_trig_volatile on jahia_qrtz_fired_triggers(IS_VOLATILE);
create index idx_jahia_qrtz_ft_trig_inst_name on jahia_qrtz_fired_triggers(INSTANCE_NAME);
create index idx_jahia_qrtz_ft_job_name on jahia_qrtz_fired_triggers(JOB_NAME);
create index idx_jahia_qrtz_ft_job_group on jahia_qrtz_fired_triggers(JOB_GROUP);
create index idx_jahia_qrtz_ft_job_stateful on jahia_qrtz_fired_triggers(IS_STATEFUL);
create index idx_jahia_qrtz_ft_job_req_recovery on jahia_qrtz_fired_triggers(REQUESTS_RECOVERY);

commit;
