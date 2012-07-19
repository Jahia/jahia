    drop table jgroupsping;
    
    create table jgroupsping (
    	own_addr varchar(200) not null,
    	cluster_name varchar(200) not null,
		ping_data varchar(5000) for bit data default null,
		primary key (own_addr, cluster_name)
	);
