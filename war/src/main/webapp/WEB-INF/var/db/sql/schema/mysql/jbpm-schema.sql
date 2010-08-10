
    alter table JBPM4_DEPLOYPROP 
        drop 
        foreign key FK_DEPLPROP_DEPL;

    alter table JBPM4_EXECUTION 
        drop 
        foreign key FK_EXEC_SUBPI;

    alter table JBPM4_EXECUTION 
        drop 
        foreign key FK_EXEC_INSTANCE;

    alter table JBPM4_EXECUTION 
        drop 
        foreign key FK_EXEC_SUPEREXEC;

    alter table JBPM4_EXECUTION 
        drop 
        foreign key FK_EXEC_PARENT;

    alter table JBPM4_HIST_ACTINST 
        drop 
        foreign key FK_HACTI_HPROCI;

    alter table JBPM4_HIST_ACTINST 
        drop 
        foreign key FK_HTI_HTASK;

    alter table JBPM4_HIST_DETAIL 
        drop 
        foreign key FK_HDETAIL_HVAR;

    alter table JBPM4_HIST_DETAIL 
        drop 
        foreign key FK_HDETAIL_HPROCI;

    alter table JBPM4_HIST_DETAIL 
        drop 
        foreign key FK_HDETAIL_HTASK;

    alter table JBPM4_HIST_DETAIL 
        drop 
        foreign key FK_HDETAIL_HACTI;

    alter table JBPM4_HIST_TASK 
        drop 
        foreign key FK_HSUPERT_SUB;

    alter table JBPM4_HIST_VAR 
        drop 
        foreign key FK_HVAR_HPROCI;

    alter table JBPM4_HIST_VAR 
        drop 
        foreign key FK_HVAR_HTASK;

    alter table JBPM4_ID_GROUP 
        drop 
        foreign key FK_GROUP_PARENT;

    alter table JBPM4_ID_MEMBERSHIP 
        drop 
        foreign key FK_MEM_GROUP;

    alter table JBPM4_ID_MEMBERSHIP 
        drop 
        foreign key FK_MEM_USER;

    alter table JBPM4_JOB 
        drop 
        foreign key FK_JOB_CFG;

    alter table JBPM4_LOB 
        drop 
        foreign key FK_LOB_DEPLOYMENT;

    alter table JBPM4_PARTICIPATION 
        drop 
        foreign key FK_PART_SWIMLANE;

    alter table JBPM4_PARTICIPATION 
        drop 
        foreign key FK_PART_TASK;

    alter table JBPM4_SWIMLANE 
        drop 
        foreign key FK_SWIMLANE_EXEC;

    alter table JBPM4_TASK 
        drop 
        foreign key FK_TASK_SWIML;

    alter table JBPM4_TASK 
        drop 
        foreign key FK_TASK_SUPERTASK;

    alter table JBPM4_VARIABLE 
        drop 
        foreign key FK_VAR_EXESYS;

    alter table JBPM4_VARIABLE 
        drop 
        foreign key FK_VAR_LOB;

    alter table JBPM4_VARIABLE 
        drop 
        foreign key FK_VAR_TASK;

    alter table JBPM4_VARIABLE 
        drop 
        foreign key FK_VAR_EXECUTION;

    drop table if exists JBPM4_DEPLOYMENT;

    drop table if exists JBPM4_DEPLOYPROP;

    drop table if exists JBPM4_EXECUTION;

    drop table if exists JBPM4_HIST_ACTINST;

    drop table if exists JBPM4_HIST_DETAIL;

    drop table if exists JBPM4_HIST_PROCINST;

    drop table if exists JBPM4_HIST_TASK;

    drop table if exists JBPM4_HIST_VAR;

    drop table if exists JBPM4_ID_GROUP;

    drop table if exists JBPM4_ID_MEMBERSHIP;

    drop table if exists JBPM4_ID_USER;

    drop table if exists JBPM4_JOB;

    drop table if exists JBPM4_LOB;

    drop table if exists JBPM4_PARTICIPATION;

    drop table if exists JBPM4_PROPERTY;

    drop table if exists JBPM4_SWIMLANE;

    drop table if exists JBPM4_TASK;

    drop table if exists JBPM4_VARIABLE;

    create table JBPM4_DEPLOYMENT (
        DBID_ bigint not null,
        NAME_ longtext,
        TIMESTAMP_ bigint,
        STATE_ varchar(255),
        primary key (DBID_)
    ) ENGINE=InnoDB;

    create table JBPM4_DEPLOYPROP (
        DBID_ bigint not null,
        DEPLOYMENT_ bigint,
        OBJNAME_ varchar(255),
        KEY_ varchar(255),
        STRINGVAL_ varchar(255),
        LONGVAL_ bigint,
        primary key (DBID_)
    ) ENGINE=InnoDB;

    create table JBPM4_EXECUTION (
        DBID_ bigint not null,
        CLASS_ varchar(255) not null,
        DBVERSION_ integer not null,
        ACTIVITYNAME_ varchar(255),
        PROCDEFID_ varchar(255),
        HASVARS_ bit,
        NAME_ varchar(255),
        KEY_ varchar(255),
        ID_ varchar(255) unique,
        STATE_ varchar(255),
        SUSPHISTSTATE_ varchar(255),
        PRIORITY_ integer,
        HISACTINST_ bigint,
        PARENT_ bigint,
        INSTANCE_ bigint,
        SUPEREXEC_ bigint,
        SUBPROCINST_ bigint,
        PARENT_IDX_ integer,
        primary key (DBID_)
    ) ENGINE=InnoDB;

    create table JBPM4_HIST_ACTINST (
        DBID_ bigint not null,
        CLASS_ varchar(255) not null,
        DBVERSION_ integer not null,
        HPROCI_ bigint,
        TYPE_ varchar(255),
        EXECUTION_ varchar(255),
        ACTIVITY_NAME_ varchar(255),
        START_ datetime,
        END_ datetime,
        DURATION_ bigint,
        TRANSITION_ varchar(255),
        NEXTIDX_ integer,
        HTASK_ bigint,
        primary key (DBID_)
    ) ENGINE=InnoDB;

    create table JBPM4_HIST_DETAIL (
        DBID_ bigint not null,
        CLASS_ varchar(255) not null,
        DBVERSION_ integer not null,
        USERID_ varchar(255),
        TIME_ datetime,
        HPROCI_ bigint,
        HPROCIIDX_ integer,
        HACTI_ bigint,
        HACTIIDX_ integer,
        HTASK_ bigint,
        HTASKIDX_ integer,
        HVAR_ bigint,
        HVARIDX_ integer,
        MESSAGE_ longtext,
        OLD_STR_ varchar(255),
        NEW_STR_ varchar(255),
        OLD_INT_ integer,
        NEW_INT_ integer,
        OLD_TIME_ datetime,
        NEW_TIME_ datetime,
        PARENT_ bigint,
        PARENT_IDX_ integer,
        primary key (DBID_)
    ) ENGINE=InnoDB;

    create table JBPM4_HIST_PROCINST (
        DBID_ bigint not null,
        DBVERSION_ integer not null,
        ID_ varchar(255),
        PROCDEFID_ varchar(255),
        KEY_ varchar(255),
        START_ datetime,
        END_ datetime,
        DURATION_ bigint,
        STATE_ varchar(255),
        ENDACTIVITY_ varchar(255),
        NEXTIDX_ integer,
        primary key (DBID_)
    ) ENGINE=InnoDB;

    create table JBPM4_HIST_TASK (
        DBID_ bigint not null,
        DBVERSION_ integer not null,
        EXECUTION_ varchar(255),
        OUTCOME_ varchar(255),
        ASSIGNEE_ varchar(255),
        PRIORITY_ integer,
        STATE_ varchar(255),
        CREATE_ datetime,
        END_ datetime,
        DURATION_ bigint,
        NEXTIDX_ integer,
        SUPERTASK_ bigint,
        primary key (DBID_)
    ) ENGINE=InnoDB;

    create table JBPM4_HIST_VAR (
        DBID_ bigint not null,
        DBVERSION_ integer not null,
        PROCINSTID_ varchar(255),
        EXECUTIONID_ varchar(255),
        VARNAME_ varchar(255),
        VALUE_ varchar(255),
        HPROCI_ bigint,
        HTASK_ bigint,
        primary key (DBID_)
    ) ENGINE=InnoDB;

    create table JBPM4_ID_GROUP (
        DBID_ bigint not null,
        DBVERSION_ integer not null,
        ID_ varchar(255),
        NAME_ varchar(255),
        TYPE_ varchar(255),
        PARENT_ bigint,
        primary key (DBID_)
    ) ENGINE=InnoDB;

    create table JBPM4_ID_MEMBERSHIP (
        DBID_ bigint not null,
        DBVERSION_ integer not null,
        USER_ bigint,
        GROUP_ bigint,
        NAME_ varchar(255),
        primary key (DBID_)
    ) ENGINE=InnoDB;

    create table JBPM4_ID_USER (
        DBID_ bigint not null,
        DBVERSION_ integer not null,
        ID_ varchar(255),
        PASSWORD_ varchar(255),
        GIVENNAME_ varchar(255),
        FAMILYNAME_ varchar(255),
        BUSINESSEMAIL_ varchar(255),
        primary key (DBID_)
    ) ENGINE=InnoDB;

    create table JBPM4_JOB (
        DBID_ bigint not null,
        CLASS_ varchar(255) not null,
        DBVERSION_ integer not null,
        DUEDATE_ datetime,
        STATE_ varchar(255),
        ISEXCLUSIVE_ bit,
        LOCKOWNER_ varchar(255),
        LOCKEXPTIME_ datetime,
        EXCEPTION_ longtext,
        RETRIES_ integer,
        PROCESSINSTANCE_ bigint,
        EXECUTION_ bigint,
        CFG_ bigint,
        SIGNAL_ varchar(255),
        EVENT_ varchar(255),
        REPEAT_ varchar(255),
        primary key (DBID_)
    ) ENGINE=InnoDB;

    create table JBPM4_LOB (
        DBID_ bigint not null,
        DBVERSION_ integer not null,
        BLOB_VALUE_ longblob,
        DEPLOYMENT_ bigint,
        NAME_ longtext,
        primary key (DBID_)
    ) ENGINE=InnoDB;

    create table JBPM4_PARTICIPATION (
        DBID_ bigint not null,
        DBVERSION_ integer not null,
        GROUPID_ varchar(255),
        USERID_ varchar(255),
        TYPE_ varchar(255),
        TASK_ bigint,
        SWIMLANE_ bigint,
        primary key (DBID_)
    ) ENGINE=InnoDB;

    create table JBPM4_PROPERTY (
        KEY_ varchar(255) not null,
        VERSION_ integer not null,
        VALUE_ varchar(255),
        primary key (KEY_)
    ) ENGINE=InnoDB;

    create table JBPM4_SWIMLANE (
        DBID_ bigint not null,
        DBVERSION_ integer not null,
        NAME_ varchar(255),
        ASSIGNEE_ varchar(255),
        EXECUTION_ bigint,
        primary key (DBID_)
    ) ENGINE=InnoDB;

    create table JBPM4_TASK (
        DBID_ bigint not null,
        CLASS_ char(1) not null,
        DBVERSION_ integer not null,
        NAME_ varchar(255),
        DESCR_ longtext,
        STATE_ varchar(255),
        SUSPHISTSTATE_ varchar(255),
        ASSIGNEE_ varchar(255),
        FORM_ varchar(255),
        PRIORITY_ integer,
        CREATE_ datetime,
        DUEDATE_ datetime,
        PROGRESS_ integer,
        SIGNALLING_ bit,
        EXECUTION_ID_ varchar(255),
        ACTIVITY_NAME_ varchar(255),
        HASVARS_ bit,
        SUPERTASK_ bigint,
        EXECUTION_ bigint,
        PROCINST_ bigint,
        SWIMLANE_ bigint,
        TASKDEFNAME_ varchar(255),
        primary key (DBID_)
    ) ENGINE=InnoDB;

    create table JBPM4_VARIABLE (
        DBID_ bigint not null,
        CLASS_ varchar(255) not null,
        DBVERSION_ integer not null,
        KEY_ varchar(255),
        CONVERTER_ varchar(255),
        HIST_ bit,
        EXECUTION_ bigint,
        TASK_ bigint,
        LOB_ bigint,
        DATE_VALUE_ datetime,
        DOUBLE_VALUE_ double precision,
        CLASSNAME_ varchar(255),
        LONG_VALUE_ bigint,
        STRING_VALUE_ varchar(255),
        TEXT_VALUE_ longtext,
        EXESYS_ bigint,
        primary key (DBID_)
    ) ENGINE=InnoDB;

    create index IDX_DEPLPROP_DEPL on JBPM4_DEPLOYPROP (DEPLOYMENT_);

    alter table JBPM4_DEPLOYPROP 
        add index FK_DEPLPROP_DEPL (DEPLOYMENT_), 
        add constraint FK_DEPLPROP_DEPL 
        foreign key (DEPLOYMENT_) 
        references JBPM4_DEPLOYMENT (DBID_);

    create index IDX_EXEC_SUBPI on JBPM4_EXECUTION (SUBPROCINST_);

    create index IDX_EXEC_PARENT on JBPM4_EXECUTION (PARENT_);

    create index IDX_EXEC_SUPEREXEC on JBPM4_EXECUTION (SUPEREXEC_);

    create index IDX_EXEC_INSTANCE on JBPM4_EXECUTION (INSTANCE_);

    alter table JBPM4_EXECUTION 
        add index FK_EXEC_SUBPI (SUBPROCINST_), 
        add constraint FK_EXEC_SUBPI 
        foreign key (SUBPROCINST_) 
        references JBPM4_EXECUTION (DBID_);

    alter table JBPM4_EXECUTION 
        add index FK_EXEC_INSTANCE (INSTANCE_), 
        add constraint FK_EXEC_INSTANCE 
        foreign key (INSTANCE_) 
        references JBPM4_EXECUTION (DBID_);

    alter table JBPM4_EXECUTION 
        add index FK_EXEC_SUPEREXEC (SUPEREXEC_), 
        add constraint FK_EXEC_SUPEREXEC 
        foreign key (SUPEREXEC_) 
        references JBPM4_EXECUTION (DBID_);

    alter table JBPM4_EXECUTION 
        add index FK_EXEC_PARENT (PARENT_), 
        add constraint FK_EXEC_PARENT 
        foreign key (PARENT_) 
        references JBPM4_EXECUTION (DBID_);

    create index IDX_HTI_HTASK on JBPM4_HIST_ACTINST (HTASK_);

    create index IDX_HACTI_HPROCI on JBPM4_HIST_ACTINST (HPROCI_);

    alter table JBPM4_HIST_ACTINST 
        add index FK_HACTI_HPROCI (HPROCI_), 
        add constraint FK_HACTI_HPROCI 
        foreign key (HPROCI_) 
        references JBPM4_HIST_PROCINST (DBID_);

    alter table JBPM4_HIST_ACTINST 
        add index FK_HTI_HTASK (HTASK_), 
        add constraint FK_HTI_HTASK 
        foreign key (HTASK_) 
        references JBPM4_HIST_TASK (DBID_);

    create index IDX_HDET_HVAR on JBPM4_HIST_DETAIL (HVAR_);

    create index IDX_HDET_HACTI on JBPM4_HIST_DETAIL (HACTI_);

    create index IDX_HDET_HTASK on JBPM4_HIST_DETAIL (HTASK_);

    create index IDX_HDET_HPROCI on JBPM4_HIST_DETAIL (HPROCI_);

    alter table JBPM4_HIST_DETAIL 
        add index FK_HDETAIL_HVAR (HVAR_), 
        add constraint FK_HDETAIL_HVAR 
        foreign key (HVAR_) 
        references JBPM4_HIST_VAR (DBID_);

    alter table JBPM4_HIST_DETAIL 
        add index FK_HDETAIL_HPROCI (HPROCI_), 
        add constraint FK_HDETAIL_HPROCI 
        foreign key (HPROCI_) 
        references JBPM4_HIST_PROCINST (DBID_);

    alter table JBPM4_HIST_DETAIL 
        add index FK_HDETAIL_HTASK (HTASK_), 
        add constraint FK_HDETAIL_HTASK 
        foreign key (HTASK_) 
        references JBPM4_HIST_TASK (DBID_);

    alter table JBPM4_HIST_DETAIL 
        add index FK_HDETAIL_HACTI (HACTI_), 
        add constraint FK_HDETAIL_HACTI 
        foreign key (HACTI_) 
        references JBPM4_HIST_ACTINST (DBID_);

    create index IDX_HSUPERT_SUB on JBPM4_HIST_TASK (SUPERTASK_);

    alter table JBPM4_HIST_TASK 
        add index FK_HSUPERT_SUB (SUPERTASK_), 
        add constraint FK_HSUPERT_SUB 
        foreign key (SUPERTASK_) 
        references JBPM4_HIST_TASK (DBID_);

    create index IDX_HVAR_HTASK on JBPM4_HIST_VAR (HTASK_);

    create index IDX_HVAR_HPROCI on JBPM4_HIST_VAR (HPROCI_);

    alter table JBPM4_HIST_VAR 
        add index FK_HVAR_HPROCI (HPROCI_), 
        add constraint FK_HVAR_HPROCI 
        foreign key (HPROCI_) 
        references JBPM4_HIST_PROCINST (DBID_);

    alter table JBPM4_HIST_VAR 
        add index FK_HVAR_HTASK (HTASK_), 
        add constraint FK_HVAR_HTASK 
        foreign key (HTASK_) 
        references JBPM4_HIST_TASK (DBID_);

    create index IDX_GROUP_PARENT on JBPM4_ID_GROUP (PARENT_);

    alter table JBPM4_ID_GROUP 
        add index FK_GROUP_PARENT (PARENT_), 
        add constraint FK_GROUP_PARENT 
        foreign key (PARENT_) 
        references JBPM4_ID_GROUP (DBID_);

    create index IDX_MEM_GROUP on JBPM4_ID_MEMBERSHIP (GROUP_);

    create index IDX_MEM_USER on JBPM4_ID_MEMBERSHIP (USER_);

    alter table JBPM4_ID_MEMBERSHIP 
        add index FK_MEM_GROUP (GROUP_), 
        add constraint FK_MEM_GROUP 
        foreign key (GROUP_) 
        references JBPM4_ID_GROUP (DBID_);

    alter table JBPM4_ID_MEMBERSHIP 
        add index FK_MEM_USER (USER_), 
        add constraint FK_MEM_USER 
        foreign key (USER_) 
        references JBPM4_ID_USER (DBID_);

    create index IDX_JOBRETRIES on JBPM4_JOB (RETRIES_);

    create index IDX_JOBDUEDATE on JBPM4_JOB (DUEDATE_);

    create index IDX_JOBLOCKEXP on JBPM4_JOB (LOCKEXPTIME_);

    create index IDX_JOB_CFG on JBPM4_JOB (CFG_);

    create index IDX_JOB_EXE on JBPM4_JOB (EXECUTION_);

    create index IDX_JOB_PRINST on JBPM4_JOB (PROCESSINSTANCE_);

    alter table JBPM4_JOB 
        add index FK_JOB_CFG (CFG_), 
        add constraint FK_JOB_CFG 
        foreign key (CFG_) 
        references JBPM4_LOB (DBID_);

    create index IDX_LOB_DEPLOYMENT on JBPM4_LOB (DEPLOYMENT_);

    alter table JBPM4_LOB 
        add index FK_LOB_DEPLOYMENT (DEPLOYMENT_), 
        add constraint FK_LOB_DEPLOYMENT 
        foreign key (DEPLOYMENT_) 
        references JBPM4_DEPLOYMENT (DBID_);

    create index IDX_PART_TASK on JBPM4_PARTICIPATION (TASK_);

    alter table JBPM4_PARTICIPATION 
        add index FK_PART_SWIMLANE (SWIMLANE_), 
        add constraint FK_PART_SWIMLANE 
        foreign key (SWIMLANE_) 
        references JBPM4_SWIMLANE (DBID_);

    alter table JBPM4_PARTICIPATION 
        add index FK_PART_TASK (TASK_), 
        add constraint FK_PART_TASK 
        foreign key (TASK_) 
        references JBPM4_TASK (DBID_);

    create index IDX_SWIMLANE_EXEC on JBPM4_SWIMLANE (EXECUTION_);

    alter table JBPM4_SWIMLANE 
        add index FK_SWIMLANE_EXEC (EXECUTION_), 
        add constraint FK_SWIMLANE_EXEC 
        foreign key (EXECUTION_) 
        references JBPM4_EXECUTION (DBID_);

    create index IDX_TASK_SUPERTASK on JBPM4_TASK (SUPERTASK_);

    alter table JBPM4_TASK 
        add index FK_TASK_SWIML (SWIMLANE_), 
        add constraint FK_TASK_SWIML 
        foreign key (SWIMLANE_) 
        references JBPM4_SWIMLANE (DBID_);

    alter table JBPM4_TASK 
        add index FK_TASK_SUPERTASK (SUPERTASK_), 
        add constraint FK_TASK_SUPERTASK 
        foreign key (SUPERTASK_) 
        references JBPM4_TASK (DBID_);

    create index IDX_VAR_EXESYS on JBPM4_VARIABLE (EXESYS_);

    create index IDX_VAR_TASK on JBPM4_VARIABLE (TASK_);

    create index IDX_VAR_EXECUTION on JBPM4_VARIABLE (EXECUTION_);

    create index IDX_VAR_LOB on JBPM4_VARIABLE (LOB_);

    alter table JBPM4_VARIABLE 
        add index FK_VAR_EXESYS (EXESYS_), 
        add constraint FK_VAR_EXESYS 
        foreign key (EXESYS_) 
        references JBPM4_EXECUTION (DBID_);

    alter table JBPM4_VARIABLE 
        add index FK_VAR_LOB (LOB_), 
        add constraint FK_VAR_LOB 
        foreign key (LOB_) 
        references JBPM4_LOB (DBID_);

    alter table JBPM4_VARIABLE 
        add index FK_VAR_TASK (TASK_), 
        add constraint FK_VAR_TASK 
        foreign key (TASK_) 
        references JBPM4_TASK (DBID_);

    alter table JBPM4_VARIABLE 
        add index FK_VAR_EXECUTION (EXECUTION_), 
        add constraint FK_VAR_EXECUTION 
        foreign key (EXECUTION_) 
        references JBPM4_EXECUTION (DBID_);
