    CREATE EXTENSION IF NOT EXISTS lo;

    create table jahia_contenthistory (
        id varchar(32) not null,
        entry_action varchar(255),
        entry_date int8,
        message varchar(255),
        entry_path oid,
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
        cndFile oid not null,
        filename varchar(255) not null,
        primary key (id)
    );

    create index jahia_nodetypes_provider_ix1 on jahia_nodetypes_provider (filename);

    create sequence jahia_nodetypes_provider_seq;

    CREATE TRIGGER t_oid_jahia_contenthistory_entry_path BEFORE DELETE OR UPDATE ON jahia_contenthistory FOR EACH ROW EXECUTE FUNCTION lo_manage('entry_path');
    CREATE TRIGGER t_oid_jahia_nodetypes_provider_cndfile BEFORE DELETE OR UPDATE ON jahia_nodetypes_provider FOR EACH ROW EXECUTE FUNCTION lo_manage('cndfile');
