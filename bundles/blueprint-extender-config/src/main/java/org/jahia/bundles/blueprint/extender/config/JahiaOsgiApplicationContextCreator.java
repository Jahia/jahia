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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
import org.osgi.framework.wiring.BundleWire;
import org.osgi.framework.wiring.BundleWiring;
import org.osgi.namespace.extender.ExtenderNamespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

/**
 * Jahia module application context creator implementation that sets core Jahia Spring context as a parent and also uses
 * <code>modules-applicationcontext-registry.xml</code> resource in the configuration locations to load common bean definitions for module.
 *
 * If the bundle is not detected as a Jahia module bundle or any other kind of Jahia bundle, than default context creation scheme is used.
 *
 * @author Sergiy Shyrkov
 */
public class JahiaOsgiApplicationContextCreator implements OsgiApplicationContextCreator {

    private static final String EXTENDER_CAPABILITY_NAME = "org.jahia.bundles.blueprint.extender.config";

    private static final Logger logger = LoggerFactory.getLogger(JahiaOsgiApplicationContextCreator.class);

    private ConfigurationScanner configurationScanner = new DefaultConfigurationScanner() {
        @Override
        public String[] getConfigurations(Bundle bundle) {
            String[] cfgArray = super.getConfigurations(bundle);
            if (cfgArray.length == 0) {
                return cfgArray;
            }
            List<String> cfgs = new LinkedList<>(Arrays.asList(cfgArray));
            cfgs.add(0, "classpath:org/jahia/defaults/config/spring/modules-applicationcontext-registry.xml");
            return cfgs.toArray(new String[] {});
        }

    };

    private ConfigurationScanner defaultConfigurationScanner = new DefaultConfigurationScanner();

    @Override
    public DelegatedExecutionOsgiBundleApplicationContext createApplicationContext(BundleContext bundleContext) throws Exception {
        if (bundleContext == null) {
            // Something goes wrong, bundle is probably stopping at the same time
            return null;
        }

        Bundle bundle = bundleContext.getBundle();

        boolean isJahiaModuleBundle = BundleUtils.isJahiaModuleBundle(bundle);
        boolean isJahiaBundle = isJahiaModuleBundle || BundleUtils.isJahiaBundle(bundle);

        ApplicationContextConfiguration config = new ApplicationContextConfiguration(bundle,
                isJahiaBundle ? configurationScanner : defaultConfigurationScanner);

        if (logger.isDebugEnabled()) {
            logger.debug("Created configuration {} for bundle {}", config, OsgiStringUtils.nullSafeNameAndSymName(bundle));
        }

        // it's not allowed or not a Spring bundle, ignore it
        if (!isAllowedForBundle(bundle) || !config.isSpringPoweredBundle()) {
            return null;
        }

        OsgiBundleXmlApplicationContext ctx = new JahiaOsgiBundleXmlApplicationContext(config.getConfigurationLocations());
        ApplicationContext parentContext = SpringContextSingleton.getInstance().getContext();
        ctx.setBundleContext(bundleContext);
        ctx.setPublishContextAsService(config.isPublishContextAsService());
        if (isJahiaBundle) {
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

    /**
     * Checks whether or not an ApplicationContext can be created for a given bundle.
     *
     * <p>As part of BACKLOG-11758, usage of this {@code blueprint-extender-config} bundle is deprecated bundle
     * implementors are expected to access core services through OSGI services, and to use alternatives to using
     * the Spring framework from core for bundles internal wiring (see BACKLOG-11784).
     *
     * <p>However to preserve backward compatibility, bundles can still benefit from {@code blueprint-extender-config}
     * by adding the following entry to their MANIFEST:
     * <code>
     *      <Require-Capability>
     *          osgi.extender;filter:="(osgi.extender=org.jahia.bundles.blueprint.extender.config)"
     *      </Require-Capability>
     * </code>
     */
    private boolean isAllowedForBundle(Bundle bundle) {
        BundleWiring wiring = bundle.adapt(BundleWiring.class);
        if (wiring != null) {
            for (BundleWire wire : wiring.getRequiredWires(ExtenderNamespace.EXTENDER_NAMESPACE)) {
                Object object = wire.getCapability().getAttributes().get(ExtenderNamespace.EXTENDER_NAMESPACE);
                if (EXTENDER_CAPABILITY_NAME.equals(object)) {
                    return true;
                }
            }
        }

        return false;
    }

}
