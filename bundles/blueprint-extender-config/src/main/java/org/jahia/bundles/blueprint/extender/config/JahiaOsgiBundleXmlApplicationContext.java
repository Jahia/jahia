/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.blueprint.extender.config;

import org.eclipse.gemini.blueprint.context.OsgiBundleApplicationContextExecutor;
import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.data.templates.ModuleState;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.SpringContextSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OsgiBundleXmlApplicationContext that does not start until jahia module is registered.
 */
public class JahiaOsgiBundleXmlApplicationContext extends OsgiBundleXmlApplicationContext {
    
    private static final Logger logger = LoggerFactory.getLogger(JahiaOsgiBundleXmlApplicationContext.class);

    private OsgiBundleApplicationContextExecutor executor = new JahiaOsgiApplicationContextExecutor();

    public JahiaOsgiBundleXmlApplicationContext(String[] configLocations) {
        super(configLocations);
    }

    @Override
    protected void doClose() {
        executor.close();
    }

    @Override
    public void refresh() throws BeansException, IllegalStateException {
        executor.refresh();
    }

    private static Map<String, Resource[]> rootClassPathResourcesCache = new ConcurrentHashMap<>();

    public Resource[] getResources(String locationPattern) throws IOException {
        if (locationPattern.startsWith("rootclasspath")) {
            if (!rootClassPathResourcesCache.containsKey(locationPattern)) {
                rootClassPathResourcesCache.put(locationPattern, SpringContextSingleton.getInstance().getResources(locationPattern.substring(4)));
            }
            return rootClassPathResourcesCache.get(locationPattern);
        }
        return super.getResources(locationPattern);
    }


    private class JahiaOsgiApplicationContextExecutor implements OsgiBundleApplicationContextExecutor {
        @Override
        public void refresh() throws BeansException, IllegalStateException {
            if (BundleUtils.isJahiaModuleBundle(getBundle())) {
                final ModuleState state = BundleUtils.getModule(getBundle()).getState();
                if (state != null && state.getState() != null && state.getState() == ModuleState.State.STARTED) {
                    // Module is already started by activator, start context now
                    BundleUtils.setContextToStartForModule(getBundle(), null);
                    JahiaOsgiBundleXmlApplicationContext.this.normalRefresh();
                    try {
                        for (AbstractApplicationContext context : BundleUtils.getDependantContexts(getBundle())) {
                            try {
                                logger.info("Refreshing dependant context of the bundle {}", getBundle());
                                context.refresh();
                            } catch (Exception e) {
                                logger.error("Failed refreshing dependant context " + context, e);
                            }
                        }
                    } finally {
                        BundleUtils.getDependantContexts(getBundle()).clear();
                    }
                } else {
                    // Delegate start to activator
                    BundleUtils.setContextToStartForModule(getBundle(), JahiaOsgiBundleXmlApplicationContext.this);
                }
            } else {
                // Standard bundle, start context now
                JahiaOsgiBundleXmlApplicationContext.this.normalRefresh();
            }
        }

        @Override
        public void close() {
            if (BundleUtils.isJahiaModuleBundle(getBundle())) {
                final ModuleState state = BundleUtils.getModule(getBundle()).getState();
                if (state != null && state.getState() != null && state.getState() == ModuleState.State.STOPPING) {
                    // Module is currently stopping,
                    JahiaOsgiBundleXmlApplicationContext.this.normalClose();

                    if (JahiaOsgiBundleXmlApplicationContext.this.hasBeanFactory() && JahiaContextLoaderListener.isRunning()) {
                        String symbolicName = getBundle().getSymbolicName();
                        try {
                            logger.info("Destroying beans for bundle {}", symbolicName);
                            
                            JahiaOsgiBundleXmlApplicationContext.this.destroyBeans();
                            
                            logger.info("Done destroying beans for bundle {}", symbolicName);
                        } catch (Exception e) {
                            logger.error("Error destroying beans for the context of a bundle " + symbolicName, e);
                        }
                    }
                } else {
                    // Reset contextToStart if module has never been registered in jahia
                    BundleUtils.setContextToStartForModule(getBundle(), null);
                }
            } else {
                JahiaOsgiBundleXmlApplicationContext.this.normalClose();
            }
        }
    }
}
