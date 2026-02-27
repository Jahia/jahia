
    create table jahia_contenthistory (
        id varchar2(32 char) not null,
        entry_action varchar2(255 char),
        entry_date number(19,0),
        message varchar2(255 char),
        entry_path clob,
        property_name varchar2(50 char),
        user_key varchar2(255 char),
        uuid varchar2(36 char),
        primary key (id)
    );

    create index idx_history_duplicate_check on jahia_contenthistory(uuid, entry_date, property_name, entry_action);
    create index idx_history_date on jahia_contenthistory(entry_date);
    create index idx_history_uuid_date on jahia_contenthistory(uuid, entry_date);

    create table jahia_db_test (
        testfield varchar2(255 char) not null,
        primary key (testfield)
    );

    create table jahia_nodetypes_provider (
        id number(10,0) not null,
        cndFile clob not null,
        filename varchar2(255 char) not null,
        primary key (id)
    );

    create index jahia_nodetypes_provider_ix1 on jahia_nodetypes_provider (filename);

    create sequence jahia_nodetypes_provider_seq;
