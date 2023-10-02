
    drop table if exists jahia_contenthistory cascade;

    drop table if exists jahia_db_test cascade;

    drop table if exists jahia_nodetypes_provider cascade;

    drop sequence if exists jahia_nodetypes_provider_seq;

    DROP TRIGGER IF EXISTS t_oid_jahia_contenthistory_entry_path ON jahia_contenthistory;
    DROP TRIGGER IF EXISTS t_oid_jahia_nodetypes_provider_cndfile ON jahia_nodetypes_provider;
