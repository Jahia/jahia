/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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

import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextListener;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleContextClosedEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleContextFailedEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleContextRefreshedEvent;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.data.templates.ModuleState;
import org.jahia.osgi.BundleUtils;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.templates.TemplatePackageRegistry;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * Listener for OSGi bundle application context life cycle events. Performs logging of events. Stops the corresponding bundle if the context
 * initialization fails. Performs cross-module import/exports of Spring beans.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaOsgiBundleApplicationContextListener implements
        OsgiBundleApplicationContextListener<OsgiBundleApplicationContextEvent> {

    private static final Logger logger = LoggerFactory.getLogger(JahiaOsgiBundleApplicationContextListener.class);

    private boolean stopBundleIfContextFails = true;

    protected void logEvent(OsgiBundleApplicationContextEvent event, String bundleDisplayName) {
        if (event instanceof OsgiBundleContextRefreshedEvent) {
            logger.info("Application context successfully refreshed for bundle {}", bundleDisplayName);
        }

        if (event instanceof OsgiBundleContextFailedEvent) {
            OsgiBundleContextFailedEvent failureEvent = (OsgiBundleContextFailedEvent) event;
            logger.error("Application context refresh failed for bundle " + bundleDisplayName,
                    failureEvent.getFailureCause());

        }

        if (event instanceof OsgiBundleContextClosedEvent) {
            OsgiBundleContextClosedEvent closedEvent = (OsgiBundleContextClosedEvent) event;
            Throwable error = closedEvent.getFailureCause();

            if (error == null) {
                logger.info("Application context succesfully closed for bundle {}", bundleDisplayName);
            } else {
                logger.error("Application context close failed for bundle " + bundleDisplayName, error);
            }
        }
    }

    public void onOsgiApplicationEvent(OsgiBundleApplicationContextEvent event) {
        Bundle bundle = event.getBundle();
        String bundleDisplayName = OsgiStringUtils.nullSafeNameAndSymName(event.getBundle());

        logEvent(event, bundleDisplayName);

        if (stopBundleIfContextFails && (event instanceof OsgiBundleContextFailedEvent)) {
            if (!SettingsBean.getInstance().isDevelopmentMode()) {
                logger.info("Stopping bundle {}", bundleDisplayName);
                try {
                    bundle.stop();
                    logger.info("...bundle {} stopped", bundleDisplayName);
                } catch (BundleException e) {
                    logger.error("Unable to stop bundle " + bundleDisplayName + " due to: " + e.getMessage(), e);
                }
            } else {
                logger.error("Cannot start spring context for bundle {}", bundleDisplayName);
            }
            return;
        }

        if (event instanceof OsgiBundleContextRefreshedEvent && BundleUtils.isJahiaModuleBundle(bundle)) {
            JahiaTemplatesPackage module = BundleUtils.getModule(bundle);
            // set the context
            module.setContext((AbstractApplicationContext) event.getApplicationContext());

            TemplatePackageRegistry moduleRegistry = null;
            // if module's Jahia late-initialization services were not initialized yet and the global initialization was already done
            // (isAfterInitializeDone() == true) -> initialize services
            if (module != null
                    && !module.isServiceInitialized()
                    && (moduleRegistry = ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                            .getTemplatePackageRegistry()).isAfterInitializeDone()) {
                // initializing services for module
                moduleRegistry.afterInitializationForModule(module);
            }
        }
    }

    public void setStopBundleIfContextFails(boolean stopBundleIfContextFails) {
        this.stopBundleIfContextFails = stopBundleIfContextFails;
    }
}
