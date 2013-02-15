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

import org.eclipse.gemini.blueprint.extender.OsgiBeanFactoryPostProcessor;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.templates.JahiaModuleAwareProcessor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

/**
 * Post processor that registers the {@link JahiaModuleAwareProcessor} for beans, that would like to be aware of the current module.
 * 
 * Also exposes Spring beans which are in the <code>Export-Package</code> manifest header as OSGi services and "imports" external services
 * as Spring beans in the module Spring context if they are mentioned in the <code>Import-Package</code> manifest header.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaOsgiBeanFactoryPostProcessor implements OsgiBeanFactoryPostProcessor {

    private static final Logger logger = LoggerFactory.getLogger(JahiaOsgiBeanFactoryPostProcessor.class);

    @Override
    public void postProcessBeanFactory(BundleContext bundleContext, ConfigurableListableBeanFactory beanFactory)
            throws BeansException, InvalidSyntaxException, BundleException {
        long timer = System.currentTimeMillis();
        String bundleName = OsgiStringUtils.nullSafeNameAndSymName(bundleContext.getBundle());

        logger.info("Start post-processing of the Spring bean factory for bundle {}", bundleName);

        // register bean post-processor for JahiaModuleAware implementors
        beanFactory
                .addBeanPostProcessor(new JahiaModuleAwareProcessor(BundleUtils.getModule(bundleContext.getBundle())));

        logger.info("Finished post-processing of the Spring bean factory for bundle {} in {} ms", bundleName,
                System.currentTimeMillis() - timer);
    }

}
