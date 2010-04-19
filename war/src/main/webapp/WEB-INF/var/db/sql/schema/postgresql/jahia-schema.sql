
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
        install_number int4 not null,
        name varchar(100),
        build int4,
        result_code int4,
        install_date timestamp,
        primary key (install_number)
    );

    create table jahia_pwd_policies (
        jahia_pwd_policy_id int4 not null,
        name varchar(255) not null,
        primary key (jahia_pwd_policy_id)
    );

    create table jahia_pwd_policy_rule_params (
        jahia_pwd_policy_rule_param_id int4 not null,
        name varchar(50) not null,
        position_index int4 not null,
        jahia_pwd_policy_rule_id int4 not null,
        type char(1) not null,
        value varchar(255),
        primary key (jahia_pwd_policy_rule_param_id)
    );

    create table jahia_pwd_policy_rules (
        jahia_pwd_policy_rule_id int4 not null,
        action char(1) not null,
        rule_condition text not null,
        evaluator char(1) not null,
        name varchar(255) not null,
        jahia_pwd_policy_id int4 not null,
        position_index int4 not null,
        active bool not null,
        last_rule bool not null,
        periodical bool not null,
        primary key (jahia_pwd_policy_rule_id)
    );

    create table jahia_subscriptions (
        id_jahia_subscriptions int4 not null,
        object_key varchar(40),
        include_children bool not null,
        event_type varchar(50) not null,
        channel char(1) not null,
        notification_type char(1) not null,
        username varchar(255) not null,
        user_registered bool not null,
        site_id int4 not null,
        enabled bool not null,
        suspended bool not null,
        confirmation_key varchar(32),
        confirmation_request_timestamp int8,
        properties text,
        primary key (id_jahia_subscriptions)
    );

    create table jahia_version (
        install_number int4 not null,
        build int4,
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
