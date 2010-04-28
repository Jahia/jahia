
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
        DBID_ numeric(19,0) not null,
        NAME_ ntext null,
        TIMESTAMP_ numeric(19,0) null,
        STATE_ nvarchar(255) null,
        primary key (DBID_)
    );

    create table JBPM4_DEPLOYPROP (
        DBID_ numeric(19,0) not null,
        DEPLOYMENT_ numeric(19,0) null,
        OBJNAME_ nvarchar(255) null,
        KEY_ nvarchar(255) null,
        STRINGVAL_ nvarchar(255) null,
        LONGVAL_ numeric(19,0) null,
        primary key (DBID_)
    );

    create table JBPM4_EXECUTION (
        DBID_ numeric(19,0) not null,
        CLASS_ nvarchar(255) not null,
        DBVERSION_ int not null,
        ACTIVITYNAME_ nvarchar(255) null,
        PROCDEFID_ nvarchar(255) null,
        HASVARS_ tinyint null,
        NAME_ nvarchar(255) null,
        KEY_ nvarchar(255) null,
        ID_ nvarchar(255) null unique,
        STATE_ nvarchar(255) null,
        SUSPHISTSTATE_ nvarchar(255) null,
        PRIORITY_ int null,
        HISACTINST_ numeric(19,0) null,
        PARENT_ numeric(19,0) null,
        INSTANCE_ numeric(19,0) null,
        SUPEREXEC_ numeric(19,0) null,
        SUBPROCINST_ numeric(19,0) null,
        PARENT_IDX_ int null,
        primary key (DBID_)
    );

    create table JBPM4_HIST_ACTINST (
        DBID_ numeric(19,0) not null,
        CLASS_ nvarchar(255) not null,
        DBVERSION_ int not null,
        HPROCI_ numeric(19,0) null,
        TYPE_ nvarchar(255) null,
        EXECUTION_ nvarchar(255) null,
        ACTIVITY_NAME_ nvarchar(255) null,
        START_ datetime null,
        END_ datetime null,
        DURATION_ numeric(19,0) null,
        TRANSITION_ nvarchar(255) null,
        NEXTIDX_ int null,
        HTASK_ numeric(19,0) null,
        primary key (DBID_)
    );

    create table JBPM4_HIST_DETAIL (
        DBID_ numeric(19,0) not null,
        CLASS_ nvarchar(255) not null,
        DBVERSION_ int not null,
        USERID_ nvarchar(255) null,
        TIME_ datetime null,
        HPROCI_ numeric(19,0) null,
        HPROCIIDX_ int null,
        HACTI_ numeric(19,0) null,
        HACTIIDX_ int null,
        HTASK_ numeric(19,0) null,
        HTASKIDX_ int null,
        HVAR_ numeric(19,0) null,
        HVARIDX_ int null,
        MESSAGE_ ntext null,
        OLD_STR_ nvarchar(255) null,
        NEW_STR_ nvarchar(255) null,
        OLD_INT_ int null,
        NEW_INT_ int null,
        OLD_TIME_ datetime null,
        NEW_TIME_ datetime null,
        PARENT_ numeric(19,0) null,
        PARENT_IDX_ int null,
        primary key (DBID_)
    );

    create table JBPM4_HIST_PROCINST (
        DBID_ numeric(19,0) not null,
        DBVERSION_ int not null,
        ID_ nvarchar(255) null,
        PROCDEFID_ nvarchar(255) null,
        KEY_ nvarchar(255) null,
        START_ datetime null,
        END_ datetime null,
        DURATION_ numeric(19,0) null,
        STATE_ nvarchar(255) null,
        ENDACTIVITY_ nvarchar(255) null,
        NEXTIDX_ int null,
        primary key (DBID_)
    );

    create table JBPM4_HIST_TASK (
        DBID_ numeric(19,0) not null,
        DBVERSION_ int not null,
        EXECUTION_ nvarchar(255) null,
        OUTCOME_ nvarchar(255) null,
        ASSIGNEE_ nvarchar(255) null,
        PRIORITY_ int null,
        STATE_ nvarchar(255) null,
        CREATE_ datetime null,
        END_ datetime null,
        DURATION_ numeric(19,0) null,
        NEXTIDX_ int null,
        SUPERTASK_ numeric(19,0) null,
        primary key (DBID_)
    );

    create table JBPM4_HIST_VAR (
        DBID_ numeric(19,0) not null,
        DBVERSION_ int not null,
        PROCINSTID_ nvarchar(255) null,
        EXECUTIONID_ nvarchar(255) null,
        VARNAME_ nvarchar(255) null,
        VALUE_ nvarchar(255) null,
        HPROCI_ numeric(19,0) null,
        HTASK_ numeric(19,0) null,
        primary key (DBID_)
    );

    create table JBPM4_ID_GROUP (
        DBID_ numeric(19,0) not null,
        DBVERSION_ int not null,
        ID_ nvarchar(255) null,
        NAME_ nvarchar(255) null,
        TYPE_ nvarchar(255) null,
        PARENT_ numeric(19,0) null,
        primary key (DBID_)
    );

    create table JBPM4_ID_MEMBERSHIP (
        DBID_ numeric(19,0) not null,
        DBVERSION_ int not null,
        USER_ numeric(19,0) null,
        GROUP_ numeric(19,0) null,
        NAME_ nvarchar(255) null,
        primary key (DBID_)
    );

    create table JBPM4_ID_USER (
        DBID_ numeric(19,0) not null,
        DBVERSION_ int not null,
        ID_ nvarchar(255) null,
        PASSWORD_ nvarchar(255) null,
        GIVENNAME_ nvarchar(255) null,
        FAMILYNAME_ nvarchar(255) null,
        BUSINESSEMAIL_ nvarchar(255) null,
        primary key (DBID_)
    );

    create table JBPM4_JOB (
        DBID_ numeric(19,0) not null,
        CLASS_ nvarchar(255) not null,
        DBVERSION_ int not null,
        DUEDATE_ datetime null,
        STATE_ nvarchar(255) null,
        ISEXCLUSIVE_ tinyint null,
        LOCKOWNER_ nvarchar(255) null,
        LOCKEXPTIME_ datetime null,
        EXCEPTION_ ntext null,
        RETRIES_ int null,
        PROCESSINSTANCE_ numeric(19,0) null,
        EXECUTION_ numeric(19,0) null,
        CFG_ numeric(19,0) null,
        SIGNAL_ nvarchar(255) null,
        EVENT_ nvarchar(255) null,
        REPEAT_ nvarchar(255) null,
        primary key (DBID_)
    );

    create table JBPM4_LOB (
        DBID_ numeric(19,0) not null,
        DBVERSION_ int not null,
        BLOB_VALUE_ image null,
        DEPLOYMENT_ numeric(19,0) null,
        NAME_ ntext null,
        primary key (DBID_)
    );

    create table JBPM4_PARTICIPATION (
        DBID_ numeric(19,0) not null,
        DBVERSION_ int not null,
        GROUPID_ nvarchar(255) null,
        USERID_ nvarchar(255) null,
        TYPE_ nvarchar(255) null,
        TASK_ numeric(19,0) null,
        SWIMLANE_ numeric(19,0) null,
        primary key (DBID_)
    );

    create table JBPM4_PROPERTY (
        KEY_ nvarchar(255) not null,
        VERSION_ int not null,
        VALUE_ nvarchar(255) null,
        primary key (KEY_)
    );

    create table JBPM4_SWIMLANE (
        DBID_ numeric(19,0) not null,
        DBVERSION_ int not null,
        NAME_ nvarchar(255) null,
        ASSIGNEE_ nvarchar(255) null,
        EXECUTION_ numeric(19,0) null,
        primary key (DBID_)
    );

    create table JBPM4_TASK (
        DBID_ numeric(19,0) not null,
        CLASS_ char(1) not null,
        DBVERSION_ int not null,
        NAME_ nvarchar(255) null,
        DESCR_ ntext null,
        STATE_ nvarchar(255) null,
        SUSPHISTSTATE_ nvarchar(255) null,
        ASSIGNEE_ nvarchar(255) null,
        FORM_ nvarchar(255) null,
        PRIORITY_ int null,
        CREATE_ datetime null,
        DUEDATE_ datetime null,
        PROGRESS_ int null,
        SIGNALLING_ tinyint null,
        EXECUTION_ID_ nvarchar(255) null,
        ACTIVITY_NAME_ nvarchar(255) null,
        HASVARS_ tinyint null,
        SUPERTASK_ numeric(19,0) null,
        EXECUTION_ numeric(19,0) null,
        PROCINST_ numeric(19,0) null,
        SWIMLANE_ numeric(19,0) null,
        TASKDEFNAME_ nvarchar(255) null,
        primary key (DBID_)
    );

    create table JBPM4_VARIABLE (
        DBID_ numeric(19,0) not null,
        CLASS_ nvarchar(255) not null,
        DBVERSION_ int not null,
        KEY_ nvarchar(255) null,
        CONVERTER_ nvarchar(255) null,
        HIST_ tinyint null,
        EXECUTION_ numeric(19,0) null,
        TASK_ numeric(19,0) null,
        LOB_ numeric(19,0) null,
        DATE_VALUE_ datetime null,
        DOUBLE_VALUE_ double precision null,
        CLASSNAME_ nvarchar(255) null,
        LONG_VALUE_ numeric(19,0) null,
        STRING_VALUE_ nvarchar(255) null,
        TEXT_VALUE_ ntext null,
        EXESYS_ numeric(19,0) null,
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
