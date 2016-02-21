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
package org.jahia.bundles.blueprint.extender.config;

import org.eclipse.gemini.blueprint.context.DelegatedExecutionOsgiBundleApplicationContext;
import org.eclipse.gemini.blueprint.context.support.OsgiBundleXmlApplicationContext;
import org.eclipse.gemini.blueprint.extender.OsgiApplicationContextCreator;
import org.eclipse.gemini.blueprint.extender.support.ApplicationContextConfiguration;
import org.eclipse.gemini.blueprint.extender.support.scanning.ConfigurationScanner;
import org.eclipse.gemini.blueprint.extender.support.scanning.DefaultConfigurationScanner;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.SpringContextSingleton;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Jahia module application context creator implementation that sets core Jahia Spring context as a parent and also uses
 * <code>modules-applicationcontext-registry.xml</code> resource in the configuration locations to load common bean definitions for module.
 * 
 * If the bundle is not detected as a Jahia module bundle or any other kind of Jahia bundle, than default context creation scheme is used.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaOsgiApplicationContextCreator implements OsgiApplicationContextCreator {

    private static final Logger logger = LoggerFactory.getLogger(JahiaOsgiApplicationContextCreator.class);

    private ConfigurationScanner configurationScanner = new DefaultConfigurationScanner() {
        @Override
        public String[] getConfigurations(Bundle bundle) {
            String[] cfgArray = super.getConfigurations(bundle);
            if (cfgArray.length == 0) {
                return cfgArray;
            }
            List<String> cfgs = new LinkedList<String>(Arrays.asList(cfgArray));
            cfgs.add(0, "classpath:org/jahia/defaults/config/spring/modules-applicationcontext-registry.xml");
            return cfgs.toArray(new String[] {});
        }

    };

    private ConfigurationScanner defaultConfigurationScanner = new DefaultConfigurationScanner();

    public DelegatedExecutionOsgiBundleApplicationContext createApplicationContext(BundleContext bundleContext)
            throws Exception {
        if (bundleContext == null) {
            // Something goes wrong, bundle is probably stopping at the same time
            return null;
        }
        Bundle bundle = bundleContext.getBundle();

        boolean isJahiaModuleBundle = BundleUtils.isJahiaModuleBundle(bundle);
        boolean isJahiaBundle = isJahiaModuleBundle || BundleUtils.isJahiaBundle(bundle);

        ApplicationContextConfiguration config = new ApplicationContextConfiguration(bundle,
                isJahiaBundle ? configurationScanner : defaultConfigurationScanner);

        if (logger.isDebugEnabled())
            logger.debug("Created configuration {} for bundle {}", config,
                    OsgiStringUtils.nullSafeNameAndSymName(bundle));

        // it's not a spring bundle, ignore it
        if (!config.isSpringPoweredBundle()) {
            return null;
        }

        OsgiBundleXmlApplicationContext ctx = new JahiaOsgiBundleXmlApplicationContext(config.getConfigurationLocations());
        ApplicationContext parentContext = SpringContextSingleton.getInstance().getContext();
        ctx.setBundleContext(bundleContext);
        ctx.setPublishContextAsService(config.isPublishContextAsService());
        if (isJahiaBundle || isJahiaModuleBundle) {
            ctx.setParent(parentContext);
            if (isJahiaModuleBundle) {
                JahiaTemplatesPackage module = BundleUtils.getModule(bundle);
                if (module != null) {
                    ctx.setClassLoader(module.getClassLoader());
                }
            } else {
                ctx.setClassLoader(BundleUtils.createBundleClassLoader(bundle));
            }
        }

        return ctx;
    }
}
