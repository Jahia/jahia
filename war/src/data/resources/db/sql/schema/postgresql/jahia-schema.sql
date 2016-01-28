
    drop table if exists jahia_contenthistory cascade;

    drop table if exists jahia_db_test cascade;

    drop table if exists jahia_nodetypes_provider cascade;

    drop sequence jahia_nodetypes_provider_seq;

    create table jahia_contenthistory (
        id varchar(32) not null,
        entry_action varchar(255),
        entry_date int8,
        message varchar(255),
        entry_path text,
        property_name varchar(50),
        user_key varchar(255),
        uuid varchar(36),
        primary key (id)
    );

    create table jahia_db_test (
        testfield varchar(255) not null,
        primary key (testfield)
    );

    create table jahia_nodetypes_provider (
        id int4 not null,
        cndFile text not null,
        filename varchar(255) not null,
        primary key (id)
    );

    create index jahia_nodetypes_provider_ix1 on jahia_nodetypes_provider (filename);

    create sequence jahia_nodetypes_provider_seq;
