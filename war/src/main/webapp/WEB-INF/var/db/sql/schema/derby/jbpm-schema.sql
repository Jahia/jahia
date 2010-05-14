
    alter table JBPM4_DEPLOYPROP 
        drop constraint FK_DEPLPROP_DEPL;

    alter table JBPM4_EXECUTION 
        drop constraint FK_EXEC_SUBPI;

    alter table JBPM4_EXECUTION 
        drop constraint FK_EXEC_INSTANCE;

    alter table JBPM4_EXECUTION 
        drop constraint FK_EXEC_SUPEREXEC;

    alter table JBPM4_EXECUTION 
        drop constraint FK_EXEC_PARENT;

    alter table JBPM4_HIST_ACTINST 
        drop constraint FK_HACTI_HPROCI;

    alter table JBPM4_HIST_ACTINST 
        drop constraint FK_HTI_HTASK;

    alter table JBPM4_HIST_DETAIL 
        drop constraint FK_HDETAIL_HVAR;

    alter table JBPM4_HIST_DETAIL 
        drop constraint FK_HDETAIL_HPROCI;

    alter table JBPM4_HIST_DETAIL 
        drop constraint FK_HDETAIL_HTASK;

    alter table JBPM4_HIST_DETAIL 
        drop constraint FK_HDETAIL_HACTI;

    alter table JBPM4_HIST_TASK 
        drop constraint FK_HSUPERT_SUB;

    alter table JBPM4_HIST_VAR 
        drop constraint FK_HVAR_HPROCI;

    alter table JBPM4_HIST_VAR 
        drop constraint FK_HVAR_HTASK;

    alter table JBPM4_ID_GROUP 
        drop constraint FK_GROUP_PARENT;

    alter table JBPM4_ID_MEMBERSHIP 
        drop constraint FK_MEM_GROUP;

    alter table JBPM4_ID_MEMBERSHIP 
        drop constraint FK_MEM_USER;

    alter table JBPM4_JOB 
        drop constraint FK_JOB_CFG;

    alter table JBPM4_LOB 
        drop constraint FK_LOB_DEPLOYMENT;

    alter table JBPM4_PARTICIPATION 
        drop constraint FK_PART_SWIMLANE;

    alter table JBPM4_PARTICIPATION 
        drop constraint FK_PART_TASK;

    alter table JBPM4_SWIMLANE 
        drop constraint FK_SWIMLANE_EXEC;

    alter table JBPM4_TASK 
        drop constraint FK_TASK_SWIML;

    alter table JBPM4_TASK 
        drop constraint FK_TASK_SUPERTASK;

    alter table JBPM4_VARIABLE 
        drop constraint FK_VAR_EXESYS;

    alter table JBPM4_VARIABLE 
        drop constraint FK_VAR_LOB;

    alter table JBPM4_VARIABLE 
        drop constraint FK_VAR_TASK;

    alter table JBPM4_VARIABLE 
        drop constraint FK_VAR_EXECUTION;

    drop table JBPM4_DEPLOYMENT;

    drop table JBPM4_DEPLOYPROP;

    drop table JBPM4_EXECUTION;

    drop table JBPM4_HIST_ACTINST;

    drop table JBPM4_HIST_DETAIL;

    drop table JBPM4_HIST_PROCINST;

    drop table JBPM4_HIST_TASK;

    drop table JBPM4_HIST_VAR;

    drop table JBPM4_ID_GROUP;

    drop table JBPM4_ID_MEMBERSHIP;

    drop table JBPM4_ID_USER;

    drop table JBPM4_JOB;

    drop table JBPM4_LOB;

    drop table JBPM4_PARTICIPATION;

    drop table JBPM4_PROPERTY;

    drop table JBPM4_SWIMLANE;

    drop table JBPM4_TASK;

    drop table JBPM4_VARIABLE;

    create table JBPM4_DEPLOYMENT (
        DBID_ bigint not null,
        NAME_ clob(255),
        TIMESTAMP_ bigint,
        STATE_ varchar(255),
        primary key (DBID_)
    );

    create table JBPM4_DEPLOYPROP (
        DBID_ bigint not null,
        DEPLOYMENT_ bigint,
        OBJNAME_ varchar(255),
        KEY_ varchar(255),
        STRINGVAL_ varchar(255),
        LONGVAL_ bigint,
        primary key (DBID_)
    );

    create table JBPM4_EXECUTION (
        DBID_ bigint not null,
        CLASS_ varchar(255) not null,
        DBVERSION_ integer not null,
        ACTIVITYNAME_ varchar(255),
        PROCDEFID_ varchar(255),
        HASVARS_ smallint,
        NAME_ varchar(255),
        KEY_ varchar(255),
        ID_ varchar(255),
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
    );

    create table JBPM4_HIST_ACTINST (
        DBID_ bigint not null,
        CLASS_ varchar(255) not null,
        DBVERSION_ integer not null,
        HPROCI_ bigint,
        TYPE_ varchar(255),
        EXECUTION_ varchar(255),
        ACTIVITY_NAME_ varchar(255),
        START_ timestamp,
        END_ timestamp,
        DURATION_ bigint,
        TRANSITION_ varchar(255),
        NEXTIDX_ integer,
        HTASK_ bigint,
        primary key (DBID_)
    );

    create table JBPM4_HIST_DETAIL (
        DBID_ bigint not null,
        CLASS_ varchar(255) not null,
        DBVERSION_ integer not null,
        USERID_ varchar(255),
        TIME_ timestamp,
        HPROCI_ bigint,
        HPROCIIDX_ integer,
        HACTI_ bigint,
        HACTIIDX_ integer,
        HTASK_ bigint,
        HTASKIDX_ integer,
        HVAR_ bigint,
        HVARIDX_ integer,
        MESSAGE_ clob(255),
        OLD_STR_ varchar(255),
        NEW_STR_ varchar(255),
        OLD_INT_ integer,
        NEW_INT_ integer,
        OLD_TIME_ timestamp,
        NEW_TIME_ timestamp,
        PARENT_ bigint,
        PARENT_IDX_ integer,
        primary key (DBID_)
    );

    create table JBPM4_HIST_PROCINST (
        DBID_ bigint not null,
        DBVERSION_ integer not null,
        ID_ varchar(255),
        PROCDEFID_ varchar(255),
        KEY_ varchar(255),
        START_ timestamp,
        END_ timestamp,
        DURATION_ bigint,
        STATE_ varchar(255),
        ENDACTIVITY_ varchar(255),
        NEXTIDX_ integer,
        primary key (DBID_)
    );

    create table JBPM4_HIST_TASK (
        DBID_ bigint not null,
        DBVERSION_ integer not null,
        EXECUTION_ varchar(255),
        OUTCOME_ varchar(255),
        ASSIGNEE_ varchar(255),
        PRIORITY_ integer,
        STATE_ varchar(255),
        CREATE_ timestamp,
        END_ timestamp,
        DURATION_ bigint,
        NEXTIDX_ integer,
        SUPERTASK_ bigint,
        primary key (DBID_)
    );

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
    );

    create table JBPM4_ID_GROUP (
        DBID_ bigint not null,
        DBVERSION_ integer not null,
        ID_ varchar(255),
        NAME_ varchar(255),
        TYPE_ varchar(255),
        PARENT_ bigint,
        primary key (DBID_)
    );

    create table JBPM4_ID_MEMBERSHIP (
        DBID_ bigint not null,
        DBVERSION_ integer not null,
        USER_ bigint,
        GROUP_ bigint,
        NAME_ varchar(255),
        primary key (DBID_)
    );

    create table JBPM4_ID_USER (
        DBID_ bigint not null,
        DBVERSION_ integer not null,
        ID_ varchar(255),
        PASSWORD_ varchar(255),
        GIVENNAME_ varchar(255),
        FAMILYNAME_ varchar(255),
        BUSINESSEMAIL_ varchar(255),
        primary key (DBID_)
    );

    create table JBPM4_JOB (
        DBID_ bigint not null,
        CLASS_ varchar(255) not null,
        DBVERSION_ integer not null,
        DUEDATE_ timestamp,
        STATE_ varchar(255),
        ISEXCLUSIVE_ smallint,
        LOCKOWNER_ varchar(255),
        LOCKEXPTIME_ timestamp,
        EXCEPTION_ clob(255),
        RETRIES_ integer,
        PROCESSINSTANCE_ bigint,
        EXECUTION_ bigint,
        CFG_ bigint,
        SIGNAL_ varchar(255),
        EVENT_ varchar(255),
        REPEAT_ varchar(255),
        primary key (DBID_)
    );

    create table JBPM4_LOB (
        DBID_ bigint not null,
        DBVERSION_ integer not null,
        BLOB_VALUE_ blob,
        DEPLOYMENT_ bigint,
        NAME_ clob(255),
        primary key (DBID_)
    );

    create table JBPM4_PARTICIPATION (
        DBID_ bigint not null,
        DBVERSION_ integer not null,
        GROUPID_ varchar(255),
        USERID_ varchar(255),
        TYPE_ varchar(255),
        TASK_ bigint,
        SWIMLANE_ bigint,
        primary key (DBID_)
    );

    create table JBPM4_PROPERTY (
        KEY_ varchar(255) not null,
        VERSION_ integer not null,
        VALUE_ varchar(255),
        primary key (KEY_)
    );

    create table JBPM4_SWIMLANE (
        DBID_ bigint not null,
        DBVERSION_ integer not null,
        NAME_ varchar(255),
        ASSIGNEE_ varchar(255),
        EXECUTION_ bigint,
        primary key (DBID_)
    );

    create table JBPM4_TASK (
        DBID_ bigint not null,
        CLASS_ char(1) not null,
        DBVERSION_ integer not null,
        NAME_ varchar(255),
        DESCR_ clob(255),
        STATE_ varchar(255),
        SUSPHISTSTATE_ varchar(255),
        ASSIGNEE_ varchar(255),
        FORM_ varchar(255),
        PRIORITY_ integer,
        CREATE_ timestamp,
        DUEDATE_ timestamp,
        PROGRESS_ integer,
        SIGNALLING_ smallint,
        EXECUTION_ID_ varchar(255),
        ACTIVITY_NAME_ varchar(255),
        HASVARS_ smallint,
        SUPERTASK_ bigint,
        EXECUTION_ bigint,
        PROCINST_ bigint,
        SWIMLANE_ bigint,
        TASKDEFNAME_ varchar(255),
        primary key (DBID_)
    );

    create table JBPM4_VARIABLE (
        DBID_ bigint not null,
        CLASS_ varchar(255) not null,
        DBVERSION_ integer not null,
        KEY_ varchar(255),
        CONVERTER_ varchar(255),
        HIST_ smallint,
        EXECUTION_ bigint,
        TASK_ bigint,
        LOB_ bigint,
        DATE_VALUE_ timestamp,
        DOUBLE_VALUE_ double,
        CLASSNAME_ varchar(255),
        LONG_VALUE_ bigint,
        STRING_VALUE_ varchar(255),
        TEXT_VALUE_ clob(255),
        EXESYS_ bigint,
        primary key (DBID_)
    );

    create index IDX_DEPLPROP_DEPL on JBPM4_DEPLOYPROP (DEPLOYMENT_);

    alter table JBPM4_DEPLOYPROP 
        add constraint FK_DEPLPROP_DEPL 
        foreign key (DEPLOYMENT_) 
        references JBPM4_DEPLOYMENT;

    create index IDX_EXEC_SUBPI on JBPM4_EXECUTION (SUBPROCINST_);

    create index IDX_EXEC_PARENT on JBPM4_EXECUTION (PARENT_);

    create index IDX_EXEC_SUPEREXEC on JBPM4_EXECUTION (SUPEREXEC_);

    create index IDX_EXEC_INSTANCE on JBPM4_EXECUTION (INSTANCE_);

    alter table JBPM4_EXECUTION 
        add constraint FK_EXEC_SUBPI 
        foreign key (SUBPROCINST_) 
        references JBPM4_EXECUTION;

    alter table JBPM4_EXECUTION 
        add constraint FK_EXEC_INSTANCE 
        foreign key (INSTANCE_) 
        references JBPM4_EXECUTION;

    alter table JBPM4_EXECUTION 
        add constraint FK_EXEC_SUPEREXEC 
        foreign key (SUPEREXEC_) 
        references JBPM4_EXECUTION;

    alter table JBPM4_EXECUTION 
        add constraint FK_EXEC_PARENT 
        foreign key (PARENT_) 
        references JBPM4_EXECUTION;

    create index IDX_HTI_HTASK on JBPM4_HIST_ACTINST (HTASK_);

    create index IDX_HACTI_HPROCI on JBPM4_HIST_ACTINST (HPROCI_);

    alter table JBPM4_HIST_ACTINST 
        add constraint FK_HACTI_HPROCI 
        foreign key (HPROCI_) 
        references JBPM4_HIST_PROCINST;

    alter table JBPM4_HIST_ACTINST 
        add constraint FK_HTI_HTASK 
        foreign key (HTASK_) 
        references JBPM4_HIST_TASK;

    create index IDX_HDET_HVAR on JBPM4_HIST_DETAIL (HVAR_);

    create index IDX_HDET_HACTI on JBPM4_HIST_DETAIL (HACTI_);

    create index IDX_HDET_HTASK on JBPM4_HIST_DETAIL (HTASK_);

    create index IDX_HDET_HPROCI on JBPM4_HIST_DETAIL (HPROCI_);

    alter table JBPM4_HIST_DETAIL 
        add constraint FK_HDETAIL_HVAR 
        foreign key (HVAR_) 
        references JBPM4_HIST_VAR;

    alter table JBPM4_HIST_DETAIL 
        add constraint FK_HDETAIL_HPROCI 
        foreign key (HPROCI_) 
        references JBPM4_HIST_PROCINST;

    alter table JBPM4_HIST_DETAIL 
        add constraint FK_HDETAIL_HTASK 
        foreign key (HTASK_) 
        references JBPM4_HIST_TASK;

    alter table JBPM4_HIST_DETAIL 
        add constraint FK_HDETAIL_HACTI 
        foreign key (HACTI_) 
        references JBPM4_HIST_ACTINST;

    create index IDX_HSUPERT_SUB on JBPM4_HIST_TASK (SUPERTASK_);

    alter table JBPM4_HIST_TASK 
        add constraint FK_HSUPERT_SUB 
        foreign key (SUPERTASK_) 
        references JBPM4_HIST_TASK;

    create index IDX_HVAR_HTASK on JBPM4_HIST_VAR (HTASK_);

    create index IDX_HVAR_HPROCI on JBPM4_HIST_VAR (HPROCI_);

    alter table JBPM4_HIST_VAR 
        add constraint FK_HVAR_HPROCI 
        foreign key (HPROCI_) 
        references JBPM4_HIST_PROCINST;

    alter table JBPM4_HIST_VAR 
        add constraint FK_HVAR_HTASK 
        foreign key (HTASK_) 
        references JBPM4_HIST_TASK;

    create index IDX_GROUP_PARENT on JBPM4_ID_GROUP (PARENT_);

    alter table JBPM4_ID_GROUP 
        add constraint FK_GROUP_PARENT 
        foreign key (PARENT_) 
        references JBPM4_ID_GROUP;

    create index IDX_MEM_GROUP on JBPM4_ID_MEMBERSHIP (GROUP_);

    create index IDX_MEM_USER on JBPM4_ID_MEMBERSHIP (USER_);

    alter table JBPM4_ID_MEMBERSHIP 
        add constraint FK_MEM_GROUP 
        foreign key (GROUP_) 
        references JBPM4_ID_GROUP;

    alter table JBPM4_ID_MEMBERSHIP 
        add constraint FK_MEM_USER 
        foreign key (USER_) 
        references JBPM4_ID_USER;

    create index IDX_JOBRETRIES on JBPM4_JOB (RETRIES_);

    create index IDX_JOBDUEDATE on JBPM4_JOB (DUEDATE_);

    create index IDX_JOBLOCKEXP on JBPM4_JOB (LOCKEXPTIME_);

    create index IDX_JOB_CFG on JBPM4_JOB (CFG_);

    create index IDX_JOB_EXE on JBPM4_JOB (EXECUTION_);

    create index IDX_JOB_PRINST on JBPM4_JOB (PROCESSINSTANCE_);

    alter table JBPM4_JOB 
        add constraint FK_JOB_CFG 
        foreign key (CFG_) 
        references JBPM4_LOB;

    create index IDX_LOB_DEPLOYMENT on JBPM4_LOB (DEPLOYMENT_);

    alter table JBPM4_LOB 
        add constraint FK_LOB_DEPLOYMENT 
        foreign key (DEPLOYMENT_) 
        references JBPM4_DEPLOYMENT;

    create index IDX_PART_TASK on JBPM4_PARTICIPATION (TASK_);

    alter table JBPM4_PARTICIPATION 
        add constraint FK_PART_SWIMLANE 
        foreign key (SWIMLANE_) 
        references JBPM4_SWIMLANE;

    alter table JBPM4_PARTICIPATION 
        add constraint FK_PART_TASK 
        foreign key (TASK_) 
        references JBPM4_TASK;

    create index IDX_SWIMLANE_EXEC on JBPM4_SWIMLANE (EXECUTION_);

    alter table JBPM4_SWIMLANE 
        add constraint FK_SWIMLANE_EXEC 
        foreign key (EXECUTION_) 
        references JBPM4_EXECUTION;

    create index IDX_TASK_SUPERTASK on JBPM4_TASK (SUPERTASK_);

    alter table JBPM4_TASK 
        add constraint FK_TASK_SWIML 
        foreign key (SWIMLANE_) 
        references JBPM4_SWIMLANE;

    alter table JBPM4_TASK 
        add constraint FK_TASK_SUPERTASK 
        foreign key (SUPERTASK_) 
        references JBPM4_TASK;

    create index IDX_VAR_EXESYS on JBPM4_VARIABLE (EXESYS_);

    create index IDX_VAR_TASK on JBPM4_VARIABLE (TASK_);

    create index IDX_VAR_EXECUTION on JBPM4_VARIABLE (EXECUTION_);

    create index IDX_VAR_LOB on JBPM4_VARIABLE (LOB_);

    alter table JBPM4_VARIABLE 
        add constraint FK_VAR_EXESYS 
        foreign key (EXESYS_) 
        references JBPM4_EXECUTION;

    alter table JBPM4_VARIABLE 
        add constraint FK_VAR_LOB 
        foreign key (LOB_) 
        references JBPM4_LOB;

    alter table JBPM4_VARIABLE 
        add constraint FK_VAR_TASK 
        foreign key (TASK_) 
        references JBPM4_TASK;

    alter table JBPM4_VARIABLE 
        add constraint FK_VAR_EXECUTION 
        foreign key (EXECUTION_) 
        references JBPM4_EXECUTION;
