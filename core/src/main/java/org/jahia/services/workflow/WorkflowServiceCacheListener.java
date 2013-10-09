package org.jahia.services.workflow;

import org.apache.jackrabbit.spi.Event;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.content.DefaultEventListener;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import javax.jcr.observation.EventIterator;
import java.util.Map;

/**
 * Event listener to flush workflow caches
 */
public class WorkflowServiceCacheListener extends DefaultEventListener implements ApplicationListener<ApplicationEvent> {
    private CacheService cacheService;
    private Cache<String, Map<String,WorkflowRule>> cache;

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public int getEventTypes() {
        return Event.ALL_TYPES;
    }

    /**
     * This method is called when a bundle of events is dispatched.
     *
     * @param events The event set received.
     */
    @Override
    public void onEvent(EventIterator events) {
        clearCaches();
    }

    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof JahiaTemplateManagerService.TemplatePackageRedeployedEvent || event instanceof JahiaTemplateManagerService.ModuleDeployedOnSiteEvent
                || event instanceof JahiaTemplateManagerService.ModuleDependenciesEvent) {
            clearCaches();
        }
    }

    private void clearCaches() {
        if (cacheService != null && cache == null) {
            try {
                cache = cacheService.getCache("WorkflowRuleCache", true);
            } catch (JahiaInitializationException e) {
                // never thrown
            }
        }

        cache.flush();
    }

    @Override
    public String[] getNodeTypes() {
        return new String[] {"jnt:workflowRules", "jnt:workflowRule"};
    }


}
