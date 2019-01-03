delimiter $$;

CREATE TRIGGER trigger_autoinc_jbpm_process_instance_info BEFORE INSERT ON jbpm_process_instance_info FOR EACH ROW BEGIN declare auto_incr1 BIGINT;declare auto_incr2 BIGINT;SELECT AUTO_INCREMENT INTO auto_incr1 FROM information_schema.TABLES WHERE table_schema=DATABASE() AND table_name='jbpm_process_instance_info';SELECT AUTO_INCREMENT INTO auto_incr2 FROM information_schema.TABLES WHERE table_schema=DATABASE() AND table_name='jbpm_process_instance_log';  IF (auto_incr2 > auto_incr1 and NEW.instance_id<auto_incr2) THEN SET NEW.instance_id = auto_incr2; END IF;END $$;

delimiter ; ;