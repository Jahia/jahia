
    create table jahia_contenthistory (
        id varchar(32) not null,
        entry_action varchar(255),
        entry_date bigint,
        message varchar(255),
        entry_path varchar(MAX),
        property_name varchar(50),
        user_key varchar(255),
        uuid varchar(36),
        primary key (id)
    );

    create index idx_history_duplicate_check on jahia_contenthistory(uuid, entry_date, property_name, entry_action);
    create index idx_history_date on jahia_contenthistory(entry_date);
    create index idx_history_uuid_date on jahia_contenthistory(uuid, entry_date);

    create table jahia_db_test (
        testfield varchar(255) not null,
        primary key (testfield)
    );

    create table jahia_nodetypes_provider (
        id int identity not null,
        cndFile varchar(MAX) not null,
        filename varchar(255) not null,
        primary key (id)
    );

    create index jahia_nodetypes_provider_ix1 on jahia_nodetypes_provider (filename);
