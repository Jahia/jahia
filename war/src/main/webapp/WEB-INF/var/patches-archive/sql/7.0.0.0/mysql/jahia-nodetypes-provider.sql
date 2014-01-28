    drop table if exists jahia_nodetypes_provider;

    create table jahia_nodetypes_provider (
        id integer not null auto_increment,
        cndFile longtext not null,
        filename varchar(255) not null,
        primary key (id)
    ) ENGINE=InnoDB;

    create index jahia_nodetypes_provider_ix1 on jahia_nodetypes_provider (filename);
