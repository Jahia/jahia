    drop table if exists jgroupsping;
    
    create table jgroupsping (
    	own_addr varchar(200) not null,
    	cluster_name varchar(200) not null,
		ping_data varbinary(5000) default null,
		primary key (own_addr, cluster_name)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;
