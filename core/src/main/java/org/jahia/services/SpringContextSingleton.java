/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
/*
 * Copyright (c) 2004 Your Corporation. All Rights Reserved.
 */
package org.jahia.services;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.templates.JahiaTemplateManagerService.TemplatePackageRedeployedEvent;
import org.jahia.settings.SettingsBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.Resource;

/**
 * Spring application context holder.
 *
 * @author Sergiy Shyrkov
 */
public class SpringContextSingleton implements ApplicationContextAware, ApplicationListener<TemplatePackageRedeployedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(SpringContextSingleton.class);
    private static SpringContextSingleton ourInstance = new SpringContextSingleton();
    private static final List<ExpectedBean> expectedBeans = Collections.synchronizedList(new ArrayList<ExpectedBean>());

    private ApplicationContext context;
    private boolean initialized;
    private Map<String, Resource[]> resourcesCache;

    /**
     * Returns an instance of the requested bean.
     *
     * @param beanId the requested bean ID
     * @return an instance of the requested bean
     */
    public static Object getBean(String beanId) {
        try {
            return getInstance().getContext().getBean(beanId);
        } catch (BeansException e) {
            return getBeanInModulesContext(beanId);
        }
    }

    public static Object getBeanInModulesContext(String beanId) {
        return getBeanInModulesContext(beanId, SettingsBean.getInstance().getModuleSpringBeansWaitingTimeout());
    }

    private static Object getBeanInModulesContext(String beanId, long waitTimeout) {

        for (JahiaTemplatesPackage aPackage : ServicesRegistry.getInstance().getJahiaTemplateManagerService().getAvailableTemplatePackages()) {
            if (aPackage.getContext() != null && aPackage.getContext().containsBean(beanId)) {
                return aPackage.getContext().getBean(beanId);
            }
        }

        if (waitTimeout > 0) {
            logger.info("Bean '{}' not found yet, will wait for its availability max {} ms...", beanId, waitTimeout);
            ExpectedBean expectedBean = new ExpectedBean(beanId);
            expectedBeans.add(expectedBean);
            // It's possible that a thread find the expected bean before the waitBean() call,
            // In that case the waiting thread will time out, and then will get the bean on second recursive getBeanInModulesContext invocation.
            try {
                expectedBean.waitBean(waitTimeout);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } finally {
                expectedBeans.remove(expectedBean);
            }

            logger.debug("Waiting for bean '{}' either succeeded or timed out, will try to get the bean again...", beanId);
            return getBeanInModulesContext(beanId, 0);
        }

        logger.error("Bean '{}' not found in module contexts", beanId);
        throw new NoSuchBeanDefinitionException(beanId);
    }

    /**
     * Used to release an expected bean for the given bean name,
     * Potentially some threads are waiting for this bean to be available, this method is used to notify them that this
     * bean is now available.
     * We iterate on the expected beans list, each expected beans with this beanName is then released using notify()
     *
     * @param beanName the bean name
     */
    public static void releaseExpectedBean(String beanName) {
        if (StringUtils.isEmpty(beanName)) {
            return;
        }
        synchronized (expectedBeans) {
            Iterator<ExpectedBean> i = expectedBeans.iterator();
            while (i.hasNext()) {
                ExpectedBean expectedBean = i.next();
                if (beanName.equals(expectedBean.getBeanId())) {
                    logger.debug("Excepted bean '{}' detected, notify it", beanName);
                    expectedBean.releaseBean();
                }
            }
        }
    }

    /**
     * Returns a map with beans of the specified type, including beans in modules.
     *
     * @param type
     *            the bean type to search for
     * @return a map with beans of the specified type, including beans in modules
     * @throws BeansException
     *             in case of a lookup error
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
        Map<String, T> found = new LinkedHashMap<String, T>();
        found.putAll(SpringContextSingleton.getInstance().getContext().getBeansOfType(type));
        for (JahiaTemplatesPackage aPackage : ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                .getAvailableTemplatePackages()) {
            if (aPackage.getContext() != null) {
                found.putAll(aPackage.getContext().getBeansOfType(type));
            }
        }
        return found;
    }

    public static SpringContextSingleton getInstance() {
        return ourInstance;
    }

    private SpringContextSingleton() {
        resourcesCache = new HashMap<String, Resource[]>(2);
    }

    /**
     * Returns the Spring application context instance.
     *
     * @return the Spring application context instance
     */
    public ApplicationContext getContext() {
        if (!initialized) {
            logger.warn("Trying to access Spring context before it is available ! Please refactor code to avoid this !");
        }
        return context;
    }

    /**
     * Publishes the specified event in the core Spring context and publishes to each module's context. When publishing the event to a
     * module, the {@link ApplicationEventMulticaster#multicastEvent(ApplicationEvent)} method is used to skip publishing event to the
     * module's parent context (which is our Spring core context).
     *
     * @param event
     *            the Spring event to be published
     */
    public void publishEvent(ApplicationEvent event) {
        publishEvent(event, true);
    }

    /**
     * Publishes the specified event in the core Spring context and if <code>propagateToModules</code> is set to true, publishes that event
     * to each module's context. When publishing the event to a module, the
     * {@link ApplicationEventMulticaster#multicastEvent(ApplicationEvent)} method is used to skip publishing event to the module's parent
     * context (which is our Spring core context).
     *
     * @param event
     *            the Spring event to be published
     * @param propagateToModules
     *            whether to propagate the event to all modules
     */
    public void publishEvent(ApplicationEvent event, boolean propagateToModules) {
        getContext().publishEvent(event);
        for (JahiaTemplatesPackage aPackage : ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                .getAvailableTemplatePackages()) {
            if (aPackage.getContext() != null) {
                multicastEvent(event, aPackage.getContext());
            }
        }
    }

    private void multicastEvent(ApplicationEvent event, AbstractApplicationContext ctx) {
        if (!ctx.isActive()) {
            return;
        }
        if (ctx.containsBean(AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME)) {
            ((ApplicationEventMulticaster) ctx
                    .getBean(AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME)).multicastEvent(event);
        } else {
            // fall back to publishEvent()
            ctx.publishEvent(event);
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void onApplicationEvent(TemplatePackageRedeployedEvent event) {
        resourcesCache.clear();
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
        initialized = true;
    }

    /**
     * Searches for Spring resource locations given the specified (pattern-based) location. Multiple locations can be provided separated by
     * comma (or any delimiter, defined in {@link org.springframework.context.ConfigurableApplicationContext#CONFIG_LOCATION_DELIMITERS} ).
     *
     * @param locationPatterns
     *            (pattern-based) location to search for resources. Multiple locations can be provided separated by comma (or any delimiter,
     *            defined in {@link org.springframework.context.ConfigurableApplicationContext#CONFIG_LOCATION_DELIMITERS} )
     * @return an array of {@link Resource} objects found
     * @throws IOException
     *             in case of a lookup error
     */
    public Resource[] getResources(String locationPatterns) throws IOException {
        return getResources(locationPatterns, true);
    }

    /**
     * Searches for Spring resource locations given the specified (pattern-based) location. Multiple locations can be provided separated by
     * comma (or any delimiter, defined in {@link org.springframework.context.ConfigurableApplicationContext#CONFIG_LOCATION_DELIMITERS} ).
     *
     * @param locationPatterns
     *            (pattern-based) location to search for resources. Multiple locations can be provided separated by comma (or any delimiter,
     *            defined in {@link org.springframework.context.ConfigurableApplicationContext#CONFIG_LOCATION_DELIMITERS} )
     * @param useCache can we use lookup caches?
     * @return an array of {@link Resource} objects found
     * @throws IOException
     *             in case of a lookup error
     */
    public Resource[] getResources(String locationPatterns, boolean useCache) throws IOException {
        Resource[] allResources = useCache ? resourcesCache.get(locationPatterns) : null;
        if (allResources == null) {
            allResources = new Resource[0];
            for (String location : org.springframework.util.StringUtils.tokenizeToStringArray(
                    locationPatterns, ConfigurableApplicationContext.CONFIG_LOCATION_DELIMITERS)) {
                try {
                    allResources = (Resource[]) ArrayUtils.addAll(allResources,
                            context.getResources(location.trim()));
                } catch (FileNotFoundException e) {
                    // Ignore
                    logger.debug("Cannot find resources",e);
                }
            }
            if (useCache) {
                resourcesCache.put(locationPatterns, allResources);
            }
        }
        return allResources;
    }

    /**
     * Expected bean is used to wait a specific bean when using SpringContextSingleton to access beans defined
     * in other modules, from a module.
     *
     * In 7.2 a module Spring context can start independently from its dependencies Spring contexts. This class
     * supports waiting for a bean to become accessible before returning it to a module that needs it.
     */
    private static class ExpectedBean {

        private String beanId;

        public ExpectedBean(String beanId) {
            this.beanId = beanId;
        }

        public String getBeanId() {
            return beanId;
        }

        /**
         * Wait for this bean, put the current thread in waiting until notified or the timeout is reached
         * @param timeout timeout for the wait in ms
         * @throws InterruptedException
         */
        synchronized public void waitBean(long timeout) throws InterruptedException {
            wait(timeout);
        }

        /**
         * Release this bean meaning it's now available in the modules context
         */
        synchronized public void releaseBean() {
            notify();
        }
    }
}
