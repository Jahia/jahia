
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
        testfield varchar(255) not null,
        primary key (testfield)
    );

    create table jahia_grp_access (
        id_jahia_member varchar(150) not null,
        id_jahia_grps varchar(150) not null,
        membertype_grp_access int4 not null,
        primary key (id_jahia_member, id_jahia_grps, membertype_grp_access)
    );

    create table jahia_grp_prop (
        id_jahia_grp int4 not null,
        name_jahia_grp_prop varchar(50) not null,
        provider_jahia_grp_prop varchar(50) not null,
        grpkey_jahia_grp_prop varchar(200) not null,
        value_jahia_grp_prop varchar(255),
        primary key (id_jahia_grp, name_jahia_grp_prop, provider_jahia_grp_prop, grpkey_jahia_grp_prop)
    );

    create table jahia_grps (
        id_jahia_grps int4 not null,
        name_jahia_grps varchar(195),
        key_jahia_grps varchar(200) unique,
        siteid_jahia_grps int4,
        hidden_jahia_grps bool,
        primary key (id_jahia_grps)
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

    create table jahia_sites_users (
        username_sites_users varchar(50) not null,
        siteid_sites_users int4 not null,
        userid_sites_users varchar(50),
        primary key (username_sites_users, siteid_sites_users)
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

    create table jahia_user_prop (
        id_jahia_users int4 not null,
        name_jahia_user_prop varchar(150) not null,
        provider_jahia_user_prop varchar(50) not null,
        userkey_jahia_user_prop varchar(50) not null,
        value_jahia_user_prop varchar(255),
        primary key (id_jahia_users, name_jahia_user_prop, provider_jahia_user_prop, userkey_jahia_user_prop)
    );

    create table jahia_users (
        id_jahia_users int4 not null,
        name_jahia_users varchar(255),
        password_jahia_users varchar(255),
        key_jahia_users varchar(50) not null unique,
        primary key (id_jahia_users)
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

    alter table jahia_sites_users 
        add constraint FKEA2BF1BF6CF683C0 
        foreign key (userid_sites_users) 
        references jahia_users (key_jahia_users);
