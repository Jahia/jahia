
    drop table jahia_pagehit;

    create table jahia_pagehit (
        uuid varchar(36) not null,
        hits bigint,
        page_path clob,
        primary key (uuid)
    );
