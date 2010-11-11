
    drop table jahia_pagehit cascade constraints;

    create table jahia_pagehit (
        uuid varchar2(36 char) not null,
        hits number(19,0),
        page_path clob,
        primary key (uuid)
    );
