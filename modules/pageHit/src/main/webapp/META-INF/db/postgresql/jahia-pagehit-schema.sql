
    drop table jahia_pagehit;

    create table jahia_pagehit (
        uuid varchar(255) not null,
        hits int8,
        page_path text,
        primary key (uuid)
    );
