/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.bundles.blueprint.extender.config;

import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.jahia.data.templates.ModuleState;
import org.jahia.osgi.BundleUtils;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OsgiBundleXmlApplicationContext that does not start until jahia module is registered.
 */
public class JahiaOsgiBundleXmlApplicationContext extends OsgiBundleXmlApplicationContext {

    private static final Logger logger = LoggerFactory.getLogger(JahiaOsgiBundleXmlApplicationContext.class);

    /**
     * Verifies if the bundle context is valid.
     *
     * @param ctx the bundle context to check
     * @return <code>true</code> if the supplied bundle context is valid; <code>false</code> otherwise
     */
    static boolean isBundleContextValid(BundleContext ctx) {
        try {
            ctx.getBundle();
        } catch (IllegalStateException e) {
            return false;
        }
        return true;
    }

    public JahiaOsgiBundleXmlApplicationContext(String[] configLocations) {
        super(configLocations);
    }

    private final Object monitor = new Object();

    private static Map<String, Resource[]> rootClassPathResourcesCache = new ConcurrentHashMap<>();

    @Override
    public void refresh() throws BeansException, IllegalStateException {
        if (!isBundleContextValid(getBundleContext())) {
            if (logger.isInfoEnabled()) {
                logger.info("The bundle context is no longer valid for bundle {} and application context {}. Skipping Spring application context refresh.", OsgiStringUtils.nullSafeNameAndSymName(getBundle()), this);
            }
            return;
        }
        if (BundleUtils.isJahiaModuleBundle(getBundle())) {
            final ModuleState state = BundleUtils.getModule(getBundle()).getState();
            if (state != null && state.getState() != null && state.getState() == ModuleState.State.SPRING_STARTING) {
                // Module is already started by activator, start context now
                BundleUtils.setContextToStartForModule(getBundle(), null);
                try {
                    JahiaOsgiBundleXmlApplicationContext.super.refresh();
                } catch (Exception e) {
                    setFailingSpringStartup(e);
                    throw e;
                }
            } else {
                // Delegate start to activator
                BundleUtils.setContextToStartForModule(getBundle(), JahiaOsgiBundleXmlApplicationContext.this);
            }
        } else {
            // Standard bundle, start context now
            JahiaOsgiBundleXmlApplicationContext.super.refresh();
        }
    }

    public Resource[] getResources(String locationPattern) throws IOException {
        if (locationPattern.startsWith("rootclasspath")) {
            if (!rootClassPathResourcesCache.containsKey(locationPattern)) {
                rootClassPathResourcesCache.put(locationPattern, SpringContextSingleton.getInstance().getResources(locationPattern.substring(4)));
            }
            return rootClassPathResourcesCache.get(locationPattern);
        }
        return super.getResources(locationPattern);
    }

    @Override
    public void startRefresh() {
        synchronized (monitor) {
            super.startRefresh();
        }
    }

    @Override
    public void completeRefresh() {
        try {
            synchronized (monitor) {
                super.completeRefresh();
            }
        } catch (Throwable e) {
            if (isBundleContextValid(getBundleContext())) {
                setFailingSpringStartup(e);
                throw e;
            }
        }
    }

    @Override
    public void normalRefresh() {
        synchronized (monitor) {
            super.normalRefresh();
        }
    }

    @Override
    public void normalClose() {
        synchronized (monitor) {
            super.normalClose();
        }
    }

    private void setFailingSpringStartup(Throwable e) {
        // Catch failed spring startup to update module state
        ModuleState moduleState = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getModuleStates().get(getBundle());
        if (moduleState != null) {
            moduleState.setState(ModuleState.State.SPRING_NOT_STARTED);
            moduleState.setDetails(e);
        }
    }
}
