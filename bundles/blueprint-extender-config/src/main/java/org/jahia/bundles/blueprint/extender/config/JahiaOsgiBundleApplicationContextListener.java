/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.jahia.bundles.blueprint.extender.config;

import org.apache.commons.lang.StringUtils;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleApplicationContextListener;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleContextClosedEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleContextFailedEvent;
import org.eclipse.gemini.blueprint.context.event.OsgiBundleContextRefreshedEvent;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.TemplatePackageRegistry;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;

/**
 * Listener for OSGi bundle application context life cycle events. On context refresh injects the application context instance into the
 * corresponding {@link JahiaTemplatesPackage} object if exists.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaOsgiBundleApplicationContextListener implements
        OsgiBundleApplicationContextListener<OsgiBundleApplicationContextEvent> {

    private static final Logger logger = LoggerFactory.getLogger(JahiaOsgiBundleApplicationContextListener.class);

    protected void logEvent(OsgiBundleApplicationContextEvent event) {
        String applicationContextString = event.getApplicationContext().getDisplayName();

        if (event instanceof OsgiBundleContextRefreshedEvent) {
            logger.info("Application context successfully refreshed (" + applicationContextString + ")");
        }

        if (event instanceof OsgiBundleContextFailedEvent) {
            OsgiBundleContextFailedEvent failureEvent = (OsgiBundleContextFailedEvent) event;
            logger.error("Application context refresh failed (" + applicationContextString + ")",
                    failureEvent.getFailureCause());

        }

        if (event instanceof OsgiBundleContextClosedEvent) {
            OsgiBundleContextClosedEvent closedEvent = (OsgiBundleContextClosedEvent) event;
            Throwable error = closedEvent.getFailureCause();

            if (error == null) {
                logger.info("Application context succesfully closed (" + applicationContextString + ")");
            } else {
                logger.error("Application context close failed (" + applicationContextString + ")", error);
            }
        }
    }

    public void onOsgiApplicationEvent(OsgiBundleApplicationContextEvent event) {
        logEvent(event);

        Bundle bundle = event.getBundle();

        if (!JahiaOsgiApplicationContextCreator.isJahiaModuleBundle(bundle)) {
            // if not Jahia module bundle -> skip
            return;
        }

        if (!(event instanceof OsgiBundleContextRefreshedEvent) && !(event instanceof OsgiBundleContextFailedEvent)
                && !(event instanceof OsgiBundleContextClosedEvent)) {
            // we are only interested in those three event types
            return;
        }

        TemplatePackageRegistry reg = ((JahiaTemplateManagerService) event.getApplicationContext().getParent()
                .getBean("JahiaTemplateManagerService")).getTemplatePackageRegistry();
        JahiaTemplatesPackage pkg = reg.lookupByFileName(StringUtils.defaultString(
                (String) bundle.getHeaders().get("Jahia-Root-Folder"), bundle.getSymbolicName()));
        if (pkg == null) {
            // no corresponding Jahia package
            return;
        }

        if (event instanceof OsgiBundleContextRefreshedEvent) {
            if (pkg.getContext() == null) {
                logger.info("Setting application context and classloader for module {}", pkg.getRootFolder());
                pkg.setContext((AbstractApplicationContext) event.getApplicationContext());
                pkg.setClassLoader(event.getApplicationContext().getClassLoader());
            }
        }

        if (event instanceof OsgiBundleContextFailedEvent || event instanceof OsgiBundleContextClosedEvent) {
            pkg.setContext(null);
        }
    }
}
