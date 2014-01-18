
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
