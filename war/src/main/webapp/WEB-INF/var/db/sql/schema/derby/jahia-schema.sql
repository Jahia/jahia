
    alter table jahia_pwd_policy_rule_params 
        drop constraint FKBE451EF45A0DB19B;

    alter table jahia_pwd_policy_rules 
        drop constraint FK2BC650026DA1D1E6;

    drop table jahia_db_test;

    drop table jahia_installedpatch;

    drop table jahia_pwd_policies;

    drop table jahia_pwd_policy_rule_params;

    drop table jahia_pwd_policy_rules;

    drop table jahia_subscriptions;

    drop table jahia_version;

    create table jahia_db_test (
        testfield varchar(255) not null,
        primary key (testfield)
    );

    create table jahia_installedpatch (
        install_number integer not null,
        name varchar(100),
        build integer,
        result_code integer,
        install_date timestamp,
        primary key (install_number)
    );

    create table jahia_pwd_policies (
        jahia_pwd_policy_id integer not null,
        name varchar(255) not null,
        primary key (jahia_pwd_policy_id)
    );

    create table jahia_pwd_policy_rule_params (
        jahia_pwd_policy_rule_param_id integer not null,
        name varchar(50) not null,
        position_index integer not null,
        jahia_pwd_policy_rule_id integer not null,
        type char(1) not null,
        value varchar(255),
        primary key (jahia_pwd_policy_rule_param_id)
    );

    create table jahia_pwd_policy_rules (
        jahia_pwd_policy_rule_id integer not null,
        action char(1) not null,
        rule_condition clob(1048576) not null,
        evaluator char(1) not null,
        name varchar(255) not null,
        jahia_pwd_policy_id integer not null,
        position_index integer not null,
        active smallint not null,
        last_rule smallint not null,
        periodical smallint not null,
        primary key (jahia_pwd_policy_rule_id)
    );

    create table jahia_subscriptions (
        id_jahia_subscriptions integer not null,
        object_key varchar(40),
        include_children smallint not null,
        event_type varchar(50) not null,
        channel char(1) not null,
        notification_type char(1) not null,
        username varchar(255) not null,
        user_registered smallint not null,
        site_id integer not null,
        enabled smallint not null,
        suspended smallint not null,
        confirmation_key varchar(32),
        confirmation_request_timestamp bigint,
        properties clob(1048576),
        primary key (id_jahia_subscriptions)
    );

    create table jahia_version (
        install_number integer not null,
        build integer,
        release_number varchar(20),
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
