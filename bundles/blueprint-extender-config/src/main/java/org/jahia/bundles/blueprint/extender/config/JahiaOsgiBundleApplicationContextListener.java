/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import org.eclipse.gemini.blueprint.context.event.*;
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * Listener for OSGi bundle application context life cycle events. Performs logging of events. Stops the corresponding bundle if the context
 * initialization fails. Performs cross-module import/exports of Spring beans.
 *
 * @author Sergiy Shyrkov
 */
public class JahiaOsgiBundleApplicationContextListener implements
        OsgiBundleApplicationContextListener<OsgiBundleApplicationContextEvent>, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(JahiaOsgiBundleApplicationContextListener.class);

    private static Boolean getBooleanValue(SettingsBean settings, String key, boolean defaultValue) {
        String value = settings.getPropertiesFile().getProperty(key);
        return value != null ? Boolean.valueOf(value.trim()) : defaultValue;
    }

    private boolean skipStopOnInvalidBundleContext = true;

    private boolean stopBundleIfContextFails = true;

    private boolean isBundleContextValid(OsgiBundleContextFailedEvent event) {
        return event.getSource() instanceof JahiaOsgiBundleXmlApplicationContext && JahiaOsgiBundleXmlApplicationContext.isBundleContextValid(((JahiaOsgiBundleXmlApplicationContext) event.getSource()).getBundleContext());
    }

    protected void logEvent(OsgiBundleApplicationContextEvent event, String bundleDisplayName) {
        if (event instanceof OsgiBundleContextRefreshedEvent) {
            logger.info("Application context successfully refreshed for bundle {}", bundleDisplayName);
        }

        if (event instanceof OsgiBundleContextFailedEvent) {
            OsgiBundleContextFailedEvent failureEvent = (OsgiBundleContextFailedEvent) event;
            if (shouldStopBundle(failureEvent)) {
                logger.error("Application context refresh failed for bundle {}", bundleDisplayName, failureEvent.getFailureCause());
            } else {
                logger.warn("Application context refresh failed for bundle {} because the bundle context is no longer valid", bundleDisplayName);
            }
        }

        if (event instanceof OsgiBundleContextClosedEvent) {
            OsgiBundleContextClosedEvent closedEvent = (OsgiBundleContextClosedEvent) event;
            Throwable error = closedEvent.getFailureCause();

            if (error == null) {
                logger.info("Application context succesfully closed for bundle {}", bundleDisplayName);
            } else {
                logger.error("Application context close failed for bundle {}", bundleDisplayName, error);
            }
        }
    }

    @SuppressWarnings("java:S1872")
    public void onOsgiApplicationEvent(OsgiBundleApplicationContextEvent event) {
        Bundle bundle = event.getBundle();
        String bundleDisplayName = OsgiStringUtils.nullSafeNameAndSymName(event.getBundle());

        logEvent(event, bundleDisplayName);

        if (event instanceof OsgiBundleContextFailedEvent) {
            OsgiBundleContextFailedEvent contextFailedEvent = (OsgiBundleContextFailedEvent) event;
            Throwable cause = getRootCause(contextFailedEvent.getFailureCause());
            BundleUtils.setContextStartException(bundle.getSymbolicName(),cause);
            JahiaTemplatesPackage module = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageRegistry().getRegisteredModules().get(bundle.getSymbolicName());
            if (module != null) {
                module.getState().setState(ModuleState.State.SPRING_NOT_STARTED);
            }

            if (cause.getClass().getName().equals("org.jahia.security.license.LicenseCheckException")) {
                logger.info("Stopping module, no license");
                try {
                    bundle.stop();
                    logger.info("...bundle {} stopped", bundleDisplayName);
                } catch (BundleException e) {
                    logger.error("Unable to stop bundle " + bundleDisplayName + " due to: " + e.getMessage(), e);
                }
            } else {
                // do we need to stop the bundle on context failure?
                if (stopBundleIfContextFails) {
                    if (shouldStopBundle(contextFailedEvent)) {
                        logger.info("Stopping bundle {}", bundleDisplayName);
                        try {
                            bundle.stop();
                            logger.info("...bundle {} stopped", bundleDisplayName);
                        } catch (BundleException e) {
                            logger.error("Unable to stop bundle " + bundleDisplayName + " due to: " + e.getMessage(), e);
                        }
                    }
                } else {
                    logger.error("Cannot start spring context for bundle {}", bundleDisplayName);
                }
                return;
            }
        }

        if (event instanceof OsgiBundleContextRefreshedEvent && BundleUtils.isJahiaModuleBundle(bundle)) {
            JahiaTemplatesPackage module = BundleUtils.getModule(bundle);
            // set the context
            module.setContext((AbstractApplicationContext) event.getApplicationContext());
            BundleUtils.setContextStartException(bundle.getSymbolicName(), null);

            TemplatePackageRegistry moduleRegistry;
            // if module's Jahia late-initialization services were not initialized yet and the global initialization was already done
            // (isAfterInitializeDone() == true) -> initialize services
            if (!module.isServiceInitialized()) {
                moduleRegistry = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageRegistry();
                if (moduleRegistry.isAfterInitializeDone()) {
                    // initializing services for module
                    moduleRegistry.afterInitializationForModule(module);
                }
            }
        }
    }

    public Throwable getRootCause(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }

    public void setStopBundleIfContextFails(boolean stopBundleIfContextFails) {
        this.stopBundleIfContextFails = stopBundleIfContextFails;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        SettingsBean settings = SettingsBean.getInstance();

        // if not explicitly set, we stop the bundle by default unless we are in development mode
        stopBundleIfContextFails = getBooleanValue(settings, "jahia.modules.stopBundleIfContextFails",
                !settings.isDevelopmentMode());

        // if not explicitly set, we skip stopping bundle with invalid bundle context by default
        skipStopOnInvalidBundleContext = getBooleanValue(settings, "jahia.modules.skipStopOnInvalidBundleContext",
                true);

        logger.info(
                "Initialized listener for OSGi bundle application context life cycle events with settings: "
                        + "stopBundleIfContextFails={}, skipStopOnInvalidBundleContext={}",
                stopBundleIfContextFails, skipStopOnInvalidBundleContext);
    }

    private boolean shouldStopBundle(OsgiBundleContextFailedEvent contextFailedEvent) {
        // should we skip stop if the bundle context is already invalid?
        return !skipStopOnInvalidBundleContext || isBundleContextValid(contextFailedEvent);
    }
}
