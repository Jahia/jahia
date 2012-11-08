package org.jahia.services.templates;

import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.JahiaAfterInitializationService;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContextEvent;

public class TestInitializingBean implements JahiaAfterInitializationService,ApplicationListener {
    private static TestInitializingBean instance;

    public static TestInitializingBean getInstance() {
        return instance;
    }

    public static void resetInstance() {
        instance = null;
    }

    private boolean initialized = false;
    private boolean started = false;
    private boolean stopped = false;

    public TestInitializingBean() {
        instance = this;
    }

    @Override
    public void initAfterAllServicesAreStarted() throws JahiaInitializationException {
        started = true;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof TemplatePackageApplicationContextLoader.ContextInitializedEvent)  {
            initialized = true;
        } else if (event instanceof ContextClosedEvent) {
            stopped = true;
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isStopped() {
        return stopped;
    }
}
