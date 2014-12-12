package org.jahia.test;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

@Configuration
@ImportResource({
        "classpath:/org/jahia/defaults/config/spring/logging/applicationcontext-logging.xml",
        "classpath:/org/jahia/defaults/config/spring/applicationcontext-hibernate.xml",
        "classpath:/org/jahia/defaults/config/spring/applicationcontext-basejahiaconfig.xml",
        "classpath:/org/jahia/defaults/config/spring/applicationcontext-cache.xml",
        "classpath:/org/jahia/defaults/config/spring/users-groups/applicationcontext-groups.xml",
        "classpath:/org/jahia/defaults/config/spring/users-groups/applicationcontext-users.xml",
        "classpath:/org/jahia/defaults/config/spring/applicationcontext-scheduler.xml",
        "classpath:/org/jahia/defaults/config/spring/applicationcontext-channels.xml",
        "classpath:/org/jahia/defaults/config/spring/applicationcontext-seo.xml",
        "classpath:/org/jahia/defaults/config/spring/applicationcontext-pwdpolicy.xml",
        "classpath:/org/jahia/defaults/config/spring/applicationcontext-text-extraction.xml",
        "classpath:/org/jahia/defaults/config/spring/applicationcontext-renderer.xml",
        "classpath:/org/jahia/defaults/config/spring/applicationcontext-notification.xml",
        "classpath:/org/jahia/defaults/config/spring/jcr/applicationcontext-jcr.xml",
        "classpath:/org/jahia/defaults/config/spring/workflow/applicationcontext-workflow.xml",
        "classpath:/org/jahia/defaults/config/spring/auth/applicationcontext-cookie.xml",
        "classpath:/org/jahia/defaults/config/spring/auth/applicationcontext-auth-pipeline.xml",
        "classpath:/org/jahia/defaults/config/spring/applicationcontext-services.xml",
        "classpath:/org/jahia/defaults/config/spring/applicationcontext-test-datasource.xml",
        "classpath:/org/jahia/defaults/config/spring/applicationcontext-test-scheduler.xml",
        "classpath:/org/jahia/defaults/config/spring/jcr/applicationcontext-test-jcr.xml",
        "classpath:/org/jahia/defaults/config/spring/applicationcontext-test-plutodriver.xml"        
        })
public class WebAppConfig {

}
