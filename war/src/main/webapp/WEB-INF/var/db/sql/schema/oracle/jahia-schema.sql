
    drop table jahia_db_test cascade constraints;

    drop table jahia_grp_access cascade constraints;

    drop table jahia_grp_prop cascade constraints;

    drop table jahia_grps cascade constraints;

    drop table jahia_installedpatch cascade constraints;

    drop table jahia_pwd_policies cascade constraints;

    drop table jahia_pwd_policy_rule_params cascade constraints;

    drop table jahia_pwd_policy_rules cascade constraints;

    drop table jahia_sites_users cascade constraints;

    drop table jahia_subscriptions cascade constraints;

    drop table jahia_user_prop cascade constraints;

    drop table jahia_users cascade constraints;

    drop table jahia_version cascade constraints;

    create table jahia_db_test (
        testfield varchar2(255 char) not null,
        primary key (testfield)
    );

    create table jahia_grp_access (
        id_jahia_member varchar2(150 char) not null,
        id_jahia_grps varchar2(150 char) not null,
        membertype_grp_access number(10,0) not null,
        primary key (id_jahia_member, id_jahia_grps, membertype_grp_access)
    );

    create table jahia_grp_prop (
        id_jahia_grp number(10,0) not null,
        name_jahia_grp_prop varchar2(50 char) not null,
        provider_jahia_grp_prop varchar2(50 char) not null,
        grpkey_jahia_grp_prop varchar2(200 char) not null,
        value_jahia_grp_prop varchar2(255 char),
        primary key (id_jahia_grp, name_jahia_grp_prop, provider_jahia_grp_prop, grpkey_jahia_grp_prop)
    );

    create table jahia_grps (
        id_jahia_grps number(10,0) not null,
        name_jahia_grps varchar2(195 char),
        key_jahia_grps varchar2(200 char) unique,
        siteid_jahia_grps number(10,0),
        hidden_jahia_grps number(1,0),
        primary key (id_jahia_grps)
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

    create table jahia_sites_users (
        username_sites_users varchar2(50 char) not null,
        siteid_sites_users number(10,0) not null,
        userid_sites_users varchar2(50 char),
        primary key (username_sites_users, siteid_sites_users)
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

    create table jahia_user_prop (
        id_jahia_users number(10,0) not null,
        name_jahia_user_prop varchar2(150 char) not null,
        provider_jahia_user_prop varchar2(50 char) not null,
        userkey_jahia_user_prop varchar2(50 char) not null,
        value_jahia_user_prop varchar2(255 char),
        primary key (id_jahia_users, name_jahia_user_prop, provider_jahia_user_prop, userkey_jahia_user_prop)
    );

    create table jahia_users (
        id_jahia_users number(10,0) not null,
        name_jahia_users varchar2(255 char),
        password_jahia_users varchar2(255 char),
        key_jahia_users varchar2(50 char) not null unique,
        primary key (id_jahia_users)
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

    alter table jahia_sites_users 
        add constraint FKEA2BF1BF6CF683C0 
        foreign key (userid_sites_users) 
        references jahia_users (key_jahia_users);
