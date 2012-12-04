package org.jahia.osgi.http.bridge;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * OSGi web app startup listener
 *
 * @author loom
 *         Date: Oct 11, 2010
 *         Time: 5:16:56 PM
 */
public class StartupListener
        implements ServletContextListener {
    private FrameworkService service;

    public void contextInitialized(ServletContextEvent event) {
        this.service = new FrameworkService(event.getServletContext());
        this.service.start();
    }

    public void contextDestroyed(ServletContextEvent event) {
        this.service.stop();
    }
}
