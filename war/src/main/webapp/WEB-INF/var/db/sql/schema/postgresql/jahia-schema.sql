
    drop table jahia_contenthistory;

    drop table jahia_db_test;

    drop table jahia_external_mapping;

    drop table jahia_external_provider_id;

    drop sequence jahia_external_provider_id_seq;

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

    create table jahia_external_mapping (
        internalUuid varchar(36) not null,
        externalId text not null,
        externalIdHash int4,
        providerKey varchar(255) not null,
        primary key (internalUuid)
    );

    create table jahia_external_provider_id (
        id int4 not null,
        providerKey varchar(255) not null,
        primary key (id)
    );

    create index jahia_external_mapping_index1 on jahia_external_mapping (externalIdHash, providerKey);

    create index jahia_external_provider_id_index1 on jahia_external_provider_id (providerKey);

    create sequence jahia_external_provider_id_seq;
