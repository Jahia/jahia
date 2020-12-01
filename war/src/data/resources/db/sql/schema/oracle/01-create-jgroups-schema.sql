    create table JGROUPSPING (
    	own_addr varchar(200) not null,
    	cluster_name varchar(200) not null,
    	ping_data blob default null,
    	primary key (own_addr, cluster_name)
    );

