
    alter table jahia_pwd_policy_rule_params 
        drop constraint FKBE451EF45A0DB19B;

    alter table jahia_pwd_policy_rules 
        drop constraint FK2BC650026DA1D1E6;

    alter table jahia_sites_users 
        drop constraint FKEA2BF1BF6CF683C0;

    drop table jahia_db_test;

    drop table jahia_grp_access;

    drop table jahia_grp_prop;

    drop table jahia_grps;

    drop table jahia_installedpatch;

    drop table jahia_pwd_policies;

    drop table jahia_pwd_policy_rule_params;

    drop table jahia_pwd_policy_rules;

    drop table jahia_sites_users;

    drop table jahia_subscriptions;

    drop table jahia_user_prop;

    drop table jahia_users;

    drop table jahia_version;

    create table jahia_db_test (
        testfield nvarchar(255) not null,
        primary key (testfield)
    );

    create table jahia_grp_access (
        id_jahia_member nvarchar(150) not null,
        id_jahia_grps nvarchar(150) not null,
        membertype_grp_access int not null,
        primary key (id_jahia_member, id_jahia_grps, membertype_grp_access)
    );

    create table jahia_grp_prop (
        id_jahia_grp int not null,
        name_jahia_grp_prop nvarchar(50) not null,
        provider_jahia_grp_prop nvarchar(50) not null,
        grpkey_jahia_grp_prop nvarchar(200) not null,
        value_jahia_grp_prop nvarchar(255) null,
        primary key (id_jahia_grp, name_jahia_grp_prop, provider_jahia_grp_prop, grpkey_jahia_grp_prop)
    );

    create table jahia_grps (
        id_jahia_grps int not null,
        name_jahia_grps nvarchar(195) null,
        key_jahia_grps nvarchar(200) null unique,
        siteid_jahia_grps int null,
        hidden_jahia_grps tinyint null,
        primary key (id_jahia_grps)
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

    create table jahia_sites_users (
        username_sites_users nvarchar(50) not null,
        siteid_sites_users int not null,
        userid_sites_users nvarchar(50) null,
        primary key (username_sites_users, siteid_sites_users)
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

    create table jahia_user_prop (
        id_jahia_users int not null,
        name_jahia_user_prop nvarchar(150) not null,
        provider_jahia_user_prop nvarchar(50) not null,
        userkey_jahia_user_prop nvarchar(50) not null,
        value_jahia_user_prop nvarchar(255) null,
        primary key (id_jahia_users, name_jahia_user_prop, provider_jahia_user_prop, userkey_jahia_user_prop)
    );

    create table jahia_users (
        id_jahia_users int not null,
        name_jahia_users nvarchar(255) null,
        password_jahia_users nvarchar(255) null,
        key_jahia_users nvarchar(50) not null unique,
        primary key (id_jahia_users)
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

    alter table jahia_sites_users 
        add constraint FKEA2BF1BF6CF683C0 
        foreign key (userid_sites_users) 
        references jahia_users (key_jahia_users);
