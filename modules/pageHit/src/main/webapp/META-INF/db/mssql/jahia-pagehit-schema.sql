
    drop table jahia_pagehit;

    create table jahia_pagehit (
        uuid nvarchar(255) not null,
        hits numeric(19,0) null,
        page_path ntext null,
        primary key (uuid)
    );
