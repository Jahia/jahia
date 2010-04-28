
    drop table JBPM4_DEPLOYMENT cascade constraints;

    drop table JBPM4_DEPLOYPROP cascade constraints;

    drop table JBPM4_EXECUTION cascade constraints;

    drop table JBPM4_HIST_ACTINST cascade constraints;

    drop table JBPM4_HIST_DETAIL cascade constraints;

    drop table JBPM4_HIST_PROCINST cascade constraints;

    drop table JBPM4_HIST_TASK cascade constraints;

    drop table JBPM4_HIST_VAR cascade constraints;

    drop table JBPM4_ID_GROUP cascade constraints;

    drop table JBPM4_ID_MEMBERSHIP cascade constraints;

    drop table JBPM4_ID_USER cascade constraints;

    drop table JBPM4_JOB cascade constraints;

    drop table JBPM4_LOB cascade constraints;

    drop table JBPM4_PARTICIPATION cascade constraints;

    drop table JBPM4_PROPERTY cascade constraints;

    drop table JBPM4_SWIMLANE cascade constraints;

    drop table JBPM4_TASK cascade constraints;

    drop table JBPM4_VARIABLE cascade constraints;

    create table JBPM4_DEPLOYMENT (
        DBID_ number(19,0) not null,
        NAME_ clob,
        TIMESTAMP_ number(19,0),
        STATE_ varchar2(255 char),
        primary key (DBID_)
    );

    create table JBPM4_DEPLOYPROP (
        DBID_ number(19,0) not null,
        DEPLOYMENT_ number(19,0),
        OBJNAME_ varchar2(255 char),
        KEY_ varchar2(255 char),
        STRINGVAL_ varchar2(255 char),
        LONGVAL_ number(19,0),
        primary key (DBID_)
    );

    create table JBPM4_EXECUTION (
        DBID_ number(19,0) not null,
        CLASS_ varchar2(255 char) not null,
        DBVERSION_ number(10,0) not null,
        ACTIVITYNAME_ varchar2(255 char),
        PROCDEFID_ varchar2(255 char),
        HASVARS_ number(1,0),
        NAME_ varchar2(255 char),
        KEY_ varchar2(255 char),
        ID_ varchar2(255 char) unique,
        STATE_ varchar2(255 char),
        SUSPHISTSTATE_ varchar2(255 char),
        PRIORITY_ number(10,0),
        HISACTINST_ number(19,0),
        PARENT_ number(19,0),
        INSTANCE_ number(19,0),
        SUPEREXEC_ number(19,0),
        SUBPROCINST_ number(19,0),
        PARENT_IDX_ number(10,0),
        primary key (DBID_)
    );

    create table JBPM4_HIST_ACTINST (
        DBID_ number(19,0) not null,
        CLASS_ varchar2(255 char) not null,
        DBVERSION_ number(10,0) not null,
        HPROCI_ number(19,0),
        TYPE_ varchar2(255 char),
        EXECUTION_ varchar2(255 char),
        ACTIVITY_NAME_ varchar2(255 char),
        START_ timestamp,
        END_ timestamp,
        DURATION_ number(19,0),
        TRANSITION_ varchar2(255 char),
        NEXTIDX_ number(10,0),
        HTASK_ number(19,0),
        primary key (DBID_)
    );

    create table JBPM4_HIST_DETAIL (
        DBID_ number(19,0) not null,
        CLASS_ varchar2(255 char) not null,
        DBVERSION_ number(10,0) not null,
        USERID_ varchar2(255 char),
        TIME_ timestamp,
        HPROCI_ number(19,0),
        HPROCIIDX_ number(10,0),
        HACTI_ number(19,0),
        HACTIIDX_ number(10,0),
        HTASK_ number(19,0),
        HTASKIDX_ number(10,0),
        HVAR_ number(19,0),
        HVARIDX_ number(10,0),
        MESSAGE_ clob,
        OLD_STR_ varchar2(255 char),
        NEW_STR_ varchar2(255 char),
        OLD_INT_ number(10,0),
        NEW_INT_ number(10,0),
        OLD_TIME_ timestamp,
        NEW_TIME_ timestamp,
        PARENT_ number(19,0),
        PARENT_IDX_ number(10,0),
        primary key (DBID_)
    );

    create table JBPM4_HIST_PROCINST (
        DBID_ number(19,0) not null,
        DBVERSION_ number(10,0) not null,
        ID_ varchar2(255 char),
        PROCDEFID_ varchar2(255 char),
        KEY_ varchar2(255 char),
        START_ timestamp,
        END_ timestamp,
        DURATION_ number(19,0),
        STATE_ varchar2(255 char),
        ENDACTIVITY_ varchar2(255 char),
        NEXTIDX_ number(10,0),
        primary key (DBID_)
    );

    create table JBPM4_HIST_TASK (
        DBID_ number(19,0) not null,
        DBVERSION_ number(10,0) not null,
        EXECUTION_ varchar2(255 char),
        OUTCOME_ varchar2(255 char),
        ASSIGNEE_ varchar2(255 char),
        PRIORITY_ number(10,0),
        STATE_ varchar2(255 char),
        CREATE_ timestamp,
        END_ timestamp,
        DURATION_ number(19,0),
        NEXTIDX_ number(10,0),
        SUPERTASK_ number(19,0),
        primary key (DBID_)
    );

    create table JBPM4_HIST_VAR (
        DBID_ number(19,0) not null,
        DBVERSION_ number(10,0) not null,
        PROCINSTID_ varchar2(255 char),
        EXECUTIONID_ varchar2(255 char),
        VARNAME_ varchar2(255 char),
        VALUE_ varchar2(255 char),
        HPROCI_ number(19,0),
        HTASK_ number(19,0),
        primary key (DBID_)
    );

    create table JBPM4_ID_GROUP (
        DBID_ number(19,0) not null,
        DBVERSION_ number(10,0) not null,
        ID_ varchar2(255 char),
        NAME_ varchar2(255 char),
        TYPE_ varchar2(255 char),
        PARENT_ number(19,0),
        primary key (DBID_)
    );

    create table JBPM4_ID_MEMBERSHIP (
        DBID_ number(19,0) not null,
        DBVERSION_ number(10,0) not null,
        USER_ number(19,0),
        GROUP_ number(19,0),
        NAME_ varchar2(255 char),
        primary key (DBID_)
    );

    create table JBPM4_ID_USER (
        DBID_ number(19,0) not null,
        DBVERSION_ number(10,0) not null,
        ID_ varchar2(255 char),
        PASSWORD_ varchar2(255 char),
        GIVENNAME_ varchar2(255 char),
        FAMILYNAME_ varchar2(255 char),
        BUSINESSEMAIL_ varchar2(255 char),
        primary key (DBID_)
    );

    create table JBPM4_JOB (
        DBID_ number(19,0) not null,
        CLASS_ varchar2(255 char) not null,
        DBVERSION_ number(10,0) not null,
        DUEDATE_ timestamp,
        STATE_ varchar2(255 char),
        ISEXCLUSIVE_ number(1,0),
        LOCKOWNER_ varchar2(255 char),
        LOCKEXPTIME_ timestamp,
        EXCEPTION_ clob,
        RETRIES_ number(10,0),
        PROCESSINSTANCE_ number(19,0),
        EXECUTION_ number(19,0),
        CFG_ number(19,0),
        SIGNAL_ varchar2(255 char),
        EVENT_ varchar2(255 char),
        REPEAT_ varchar2(255 char),
        primary key (DBID_)
    );

    create table JBPM4_LOB (
        DBID_ number(19,0) not null,
        DBVERSION_ number(10,0) not null,
        BLOB_VALUE_ blob,
        DEPLOYMENT_ number(19,0),
        NAME_ clob,
        primary key (DBID_)
    );

    create table JBPM4_PARTICIPATION (
        DBID_ number(19,0) not null,
        DBVERSION_ number(10,0) not null,
        GROUPID_ varchar2(255 char),
        USERID_ varchar2(255 char),
        TYPE_ varchar2(255 char),
        TASK_ number(19,0),
        SWIMLANE_ number(19,0),
        primary key (DBID_)
    );

    create table JBPM4_PROPERTY (
        KEY_ varchar2(255 char) not null,
        VERSION_ number(10,0) not null,
        VALUE_ varchar2(255 char),
        primary key (KEY_)
    );

    create table JBPM4_SWIMLANE (
        DBID_ number(19,0) not null,
        DBVERSION_ number(10,0) not null,
        NAME_ varchar2(255 char),
        ASSIGNEE_ varchar2(255 char),
        EXECUTION_ number(19,0),
        primary key (DBID_)
    );

    create table JBPM4_TASK (
        DBID_ number(19,0) not null,
        CLASS_ char(1 char) not null,
        DBVERSION_ number(10,0) not null,
        NAME_ varchar2(255 char),
        DESCR_ clob,
        STATE_ varchar2(255 char),
        SUSPHISTSTATE_ varchar2(255 char),
        ASSIGNEE_ varchar2(255 char),
        FORM_ varchar2(255 char),
        PRIORITY_ number(10,0),
        CREATE_ timestamp,
        DUEDATE_ timestamp,
        PROGRESS_ number(10,0),
        SIGNALLING_ number(1,0),
        EXECUTION_ID_ varchar2(255 char),
        ACTIVITY_NAME_ varchar2(255 char),
        HASVARS_ number(1,0),
        SUPERTASK_ number(19,0),
        EXECUTION_ number(19,0),
        PROCINST_ number(19,0),
        SWIMLANE_ number(19,0),
        TASKDEFNAME_ varchar2(255 char),
        primary key (DBID_)
    );

    create table JBPM4_VARIABLE (
        DBID_ number(19,0) not null,
        CLASS_ varchar2(255 char) not null,
        DBVERSION_ number(10,0) not null,
        KEY_ varchar2(255 char),
        CONVERTER_ varchar2(255 char),
        HIST_ number(1,0),
        EXECUTION_ number(19,0),
        TASK_ number(19,0),
        LOB_ number(19,0),
        DATE_VALUE_ timestamp,
        DOUBLE_VALUE_ double precision,
        CLASSNAME_ varchar2(255 char),
        LONG_VALUE_ number(19,0),
        STRING_VALUE_ varchar2(255 char),
        TEXT_VALUE_ clob,
        EXESYS_ number(19,0),
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
