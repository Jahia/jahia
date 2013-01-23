package org.jahia.osgi.http.bridge;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.jahia.exceptions.JahiaRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGi web app startup listener
 * 
 * @author loom
 */
public class StartupListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(StartupListener.class);

    private FrameworkService service;

    public void contextDestroyed(ServletContextEvent event) {
        try {
            this.service.stop();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void contextInitialized(ServletContextEvent event) {
        this.service = new FrameworkService(event.getServletContext());
        try {
            this.service.start();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new JahiaRuntimeException(e);
        }
    }
}
