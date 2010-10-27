
    alter table jahia_pwd_policy_rule_params 
        drop 
        foreign key FKBE451EF45A0DB19B;

    alter table jahia_pwd_policy_rules 
        drop 
        foreign key FK2BC650026DA1D1E6;

    drop table if exists jahia_contenthistory;

    drop table if exists jahia_db_test;

    drop table if exists jahia_installedpatch;

    drop table if exists jahia_pwd_policies;

    drop table if exists jahia_pwd_policy_rule_params;

    drop table if exists jahia_pwd_policy_rules;

    drop table if exists jahia_version;

    create table jahia_contenthistory (
        id bigint not null,
        entry_action varchar(255),
        entry_date datetime,
        message varchar(255),
        entry_path longtext,
        property_name varchar(50),
        user_key varchar(255),
        uuid varchar(50),
        primary key (id),
        unique (entry_date, uuid, property_name)
    ) ENGINE=InnoDB;

    create table jahia_db_test (
        testfield varchar(255) not null,
        primary key (testfield)
    ) ENGINE=InnoDB;

    create table jahia_installedpatch (
        install_number integer not null,
        name varchar(100),
        build integer,
        result_code integer,
        install_date datetime,
        primary key (install_number)
    ) ENGINE=InnoDB;

    create table jahia_pwd_policies (
        jahia_pwd_policy_id integer not null,
        name varchar(255) not null,
        primary key (jahia_pwd_policy_id)
    ) ENGINE=InnoDB;

    create table jahia_pwd_policy_rule_params (
        jahia_pwd_policy_rule_param_id integer not null,
        name varchar(50) not null,
        position_index integer not null,
        jahia_pwd_policy_rule_id integer not null,
        type char(1) not null,
        value varchar(255),
        primary key (jahia_pwd_policy_rule_param_id)
    ) ENGINE=InnoDB;

    create table jahia_pwd_policy_rules (
        jahia_pwd_policy_rule_id integer not null,
        action char(1) not null,
        rule_condition longtext not null,
        evaluator char(1) not null,
        name varchar(255) not null,
        jahia_pwd_policy_id integer not null,
        position_index integer not null,
        active bit not null,
        last_rule bit not null,
        periodical bit not null,
        primary key (jahia_pwd_policy_rule_id)
    ) ENGINE=InnoDB;

    create table jahia_version (
        install_number integer not null,
        build integer,
        release_number varchar(20),
        install_date datetime,
        primary key (install_number)
    ) ENGINE=InnoDB;

    alter table jahia_pwd_policy_rule_params 
        add index FKBE451EF45A0DB19B (jahia_pwd_policy_rule_id), 
        add constraint FKBE451EF45A0DB19B 
        foreign key (jahia_pwd_policy_rule_id) 
        references jahia_pwd_policy_rules (jahia_pwd_policy_rule_id);

    alter table jahia_pwd_policy_rules 
        add index FK2BC650026DA1D1E6 (jahia_pwd_policy_id), 
        add constraint FK2BC650026DA1D1E6 
        foreign key (jahia_pwd_policy_id) 
        references jahia_pwd_policies (jahia_pwd_policy_id);
