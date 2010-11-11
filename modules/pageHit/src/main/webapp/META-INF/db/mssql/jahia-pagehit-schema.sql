
    drop table jahia_pagehit;

    create table jahia_pagehit (
        uuid nvarchar(36) not null,
        hits numeric(19,0) null,
        page_path ntext null,
        primary key (uuid)
    );
