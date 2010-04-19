
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
        testfield nvarchar(255) not null,
        primary key (testfield)
    );

    create table jahia_installedpatch (
        install_number int not null,
        name nvarchar(100) null,
        build int null,
        result_code int null,
        install_date datetime null,
        primary key (install_number)
    );

    create table jahia_pwd_policies (
        jahia_pwd_policy_id int not null,
        name nvarchar(255) not null,
        primary key (jahia_pwd_policy_id)
    );

    create table jahia_pwd_policy_rule_params (
        jahia_pwd_policy_rule_param_id int not null,
        name nvarchar(50) not null,
        position_index int not null,
        jahia_pwd_policy_rule_id int not null,
        type char(1) not null,
        value nvarchar(255) null,
        primary key (jahia_pwd_policy_rule_param_id)
    );

    create table jahia_pwd_policy_rules (
        jahia_pwd_policy_rule_id int not null,
        action char(1) not null,
        rule_condition ntext not null,
        evaluator char(1) not null,
        name nvarchar(255) not null,
        jahia_pwd_policy_id int not null,
        position_index int not null,
        active tinyint not null,
        last_rule tinyint not null,
        periodical tinyint not null,
        primary key (jahia_pwd_policy_rule_id)
    );

    create table jahia_subscriptions (
        id_jahia_subscriptions int not null,
        object_key nvarchar(40) null,
        include_children tinyint not null,
        event_type nvarchar(50) not null,
        channel char(1) not null,
        notification_type char(1) not null,
        username nvarchar(255) not null,
        user_registered tinyint not null,
        site_id int not null,
        enabled tinyint not null,
        suspended tinyint not null,
        confirmation_key nvarchar(32) null,
        confirmation_request_timestamp numeric(19,0) null,
        properties ntext null,
        primary key (id_jahia_subscriptions)
    );

    create table jahia_version (
        install_number int not null,
        build int null,
        release_number nvarchar(20) null,
        install_date datetime null,
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
