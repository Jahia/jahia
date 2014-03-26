    drop table if exists jahia_nodetypes_provider cascade;

    drop sequence jahia_nodetypes_provider_seq;

    create table jahia_nodetypes_provider (
        id int4 not null,
        cndFile text not null,
        filename varchar(255) not null,
        primary key (id)
    );

    create index jahia_nodetypes_provider_ix1 on jahia_nodetypes_provider (filename);

    create sequence jahia_nodetypes_provider_seq;
