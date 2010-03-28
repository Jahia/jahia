
    alter table jahia_pwd_policy_rule_params 
        drop constraint FKBE451EF45A0DB19B;

    alter table jahia_pwd_policy_rules 
        drop constraint FK2BC650026DA1D1E6;

    alter table jahia_sites_users 
        drop constraint FKEA2BF1BF6CF683C0;

    drop table jahia_db_test if exists;

    drop table jahia_grp_access if exists;

    drop table jahia_grp_prop if exists;

    drop table jahia_grps if exists;

    drop table jahia_installedpatch if exists;

    drop table jahia_pwd_policies if exists;

    drop table jahia_pwd_policy_rule_params if exists;

    drop table jahia_pwd_policy_rules if exists;

    drop table jahia_sites_users if exists;

    drop table jahia_subscriptions if exists;

    drop table jahia_user_prop if exists;

    drop table jahia_users if exists;

    drop table jahia_version if exists;

    create table jahia_db_test (
        testfield varchar(255) not null,
        primary key (testfield)
    );

    create table jahia_grp_access (
        id_jahia_member varchar(150) not null,
        id_jahia_grps varchar(150) not null,
        membertype_grp_access integer not null,
        primary key (id_jahia_member, id_jahia_grps, membertype_grp_access)
    );

    create table jahia_grp_prop (
        id_jahia_grp integer not null,
        name_jahia_grp_prop varchar(50) not null,
        provider_jahia_grp_prop varchar(50) not null,
        grpkey_jahia_grp_prop varchar(200) not null,
        value_jahia_grp_prop varchar(255),
        primary key (id_jahia_grp, name_jahia_grp_prop, provider_jahia_grp_prop, grpkey_jahia_grp_prop)
    );

    create table jahia_grps (
        id_jahia_grps integer not null,
        name_jahia_grps varchar(195),
        key_jahia_grps varchar(200),
        siteid_jahia_grps integer,
        hidden_jahia_grps bit,
        primary key (id_jahia_grps),
        unique (key_jahia_grps)
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
        rule_condition longvarchar not null,
        evaluator char(1) not null,
        name varchar(255) not null,
        jahia_pwd_policy_id integer not null,
        position_index integer not null,
        active bit not null,
        last_rule bit not null,
        periodical bit not null,
        primary key (jahia_pwd_policy_rule_id)
    );

    create table jahia_sites_users (
        username_sites_users varchar(50) not null,
        siteid_sites_users integer not null,
        userid_sites_users varchar(50),
        primary key (username_sites_users, siteid_sites_users)
    );

    create table jahia_subscriptions (
        id_jahia_subscriptions integer not null,
        object_key varchar(40),
        include_children bit not null,
        event_type varchar(50) not null,
        channel char(1) not null,
        notification_type char(1) not null,
        username varchar(255) not null,
        user_registered bit not null,
        site_id integer not null,
        enabled bit not null,
        suspended bit not null,
        confirmation_key varchar(32),
        confirmation_request_timestamp bigint,
        properties longvarchar,
        primary key (id_jahia_subscriptions)
    );

    create table jahia_user_prop (
        id_jahia_users integer not null,
        name_jahia_user_prop varchar(150) not null,
        provider_jahia_user_prop varchar(50) not null,
        userkey_jahia_user_prop varchar(50) not null,
        value_jahia_user_prop varchar(255),
        primary key (id_jahia_users, name_jahia_user_prop, provider_jahia_user_prop, userkey_jahia_user_prop)
    );

    create table jahia_users (
        id_jahia_users integer not null,
        name_jahia_users varchar(255),
        password_jahia_users varchar(255),
        key_jahia_users varchar(50) not null,
        primary key (id_jahia_users),
        unique (key_jahia_users)
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

    alter table jahia_sites_users 
        add constraint FKEA2BF1BF6CF683C0 
        foreign key (userid_sites_users) 
        references jahia_users (key_jahia_users);
