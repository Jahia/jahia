
    drop table jahia_db_test cascade constraints;

    drop table jahia_installedpatch cascade constraints;

    drop table jahia_pwd_policies cascade constraints;

    drop table jahia_pwd_policy_rule_params cascade constraints;

    drop table jahia_pwd_policy_rules cascade constraints;

    drop table jahia_subscriptions cascade constraints;

    drop table jahia_version cascade constraints;

    create table jahia_db_test (
        testfield varchar2(255 char) not null,
        primary key (testfield)
    );

    create table jahia_installedpatch (
        install_number number(10,0) not null,
        name varchar2(100 char),
        build number(10,0),
        result_code number(10,0),
        install_date timestamp,
        primary key (install_number)
    );

    create table jahia_pwd_policies (
        jahia_pwd_policy_id number(10,0) not null,
        name varchar2(255 char) not null,
        primary key (jahia_pwd_policy_id)
    );

    create table jahia_pwd_policy_rule_params (
        jahia_pwd_policy_rule_param_id number(10,0) not null,
        name varchar2(50 char) not null,
        position_index number(10,0) not null,
        jahia_pwd_policy_rule_id number(10,0) not null,
        type char(1 char) not null,
        value varchar2(255 char),
        primary key (jahia_pwd_policy_rule_param_id)
    );

    create table jahia_pwd_policy_rules (
        jahia_pwd_policy_rule_id number(10,0) not null,
        action char(1 char) not null,
        rule_condition clob not null,
        evaluator char(1 char) not null,
        name varchar2(255 char) not null,
        jahia_pwd_policy_id number(10,0) not null,
        position_index number(10,0) not null,
        active number(1,0) not null,
        last_rule number(1,0) not null,
        periodical number(1,0) not null,
        primary key (jahia_pwd_policy_rule_id)
    );

    create table jahia_subscriptions (
        id_jahia_subscriptions number(10,0) not null,
        object_key varchar2(40 char),
        include_children number(1,0) not null,
        event_type varchar2(50 char) not null,
        channel char(1 char) not null,
        notification_type char(1 char) not null,
        username varchar2(255 char) not null,
        user_registered number(1,0) not null,
        site_id number(10,0) not null,
        enabled number(1,0) not null,
        suspended number(1,0) not null,
        confirmation_key varchar2(32 char),
        confirmation_request_timestamp number(19,0),
        properties clob,
        primary key (id_jahia_subscriptions)
    );

    create table jahia_version (
        install_number number(10,0) not null,
        build number(10,0),
        release_number varchar2(20 char),
        install_date timestamp,
        primary key (install_number)
    );

    alter table jahia_pwd_policy_rule_params 
        add constraint FKBE451EF45A0DB19B 
        foreign key (jahia_pwd_policy_rule_id) 
        references jahia_pwd_policy_rules;

    alter table jahia_pwd_policy_rules 
        add constraint FK2BC650026DA1D1E6 
        foreign key (jahia_pwd_policy_id) 
        references jahia_pwd_policies;
