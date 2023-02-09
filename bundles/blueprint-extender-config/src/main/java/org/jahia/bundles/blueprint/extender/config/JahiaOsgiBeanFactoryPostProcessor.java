/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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

import org.eclipse.gemini.blueprint.extender.OsgiBeanFactoryPostProcessor;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.osgi.BundleUtils;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.templates.JahiaModuleAwareProcessor;
import org.jahia.services.templates.JahiaModulesBeanPostProcessor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.Map;

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
        Bundle bundle = null;
        try {
            bundle = bundleContext.getBundle();
        } catch (IllegalStateException e) {
            logger.info("The bundle context is no longer valid for the application context {}."
                    + " Skipping post processing Spring bean factory.", beanFactory);
            return;
        }

        if (!BundleUtils.isJahiaModuleBundle(bundle)) {
            return;
        }
        long timer = System.currentTimeMillis();
        String bundleName = OsgiStringUtils.nullSafeNameAndSymName(bundle);

        logger.info("Start post-processing of the Spring bean factory for bundle {}", bundleName);

        registerCoreBeanPostProcessors(bundle, beanFactory);

        registerModulesBeanPostProcessors(beanFactory);

        logger.info("Finished post-processing of the Spring bean factory for bundle {} in {} ms", bundleName,
                System.currentTimeMillis() - timer);
    }

    private void registerCoreBeanPostProcessors(Bundle bundle, ConfigurableListableBeanFactory beanFactory) {

        // Register bean post-processor for JahiaModuleAware implementors
        beanFactory.addBeanPostProcessor(new JahiaModuleAwareProcessor(BundleUtils.getModule(bundle)));
    }

    /**
     * This function is kept to maintain compatibility with JahiaModulesBeanPostProcessor instances defined in modules,
     * but this mechanism is buggy because modules' spring contexts are started independently since 7.2, so beans could
     * be registered before the JahiaModulesBeanPostProcessor causing unintended side effects.
     *
     * We recommend to use a more OSGI compliant way to perform intermodule beans communication (service trackers, osgi:list, etc.)
     *
     * @param beanFactory current bean factory
     */
    private void registerModulesBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
        for (JahiaTemplatesPackage aPackage : ServicesRegistry.getInstance().getJahiaTemplateManagerService().getAvailableTemplatePackages()) {
            if (aPackage.getContext() != null && aPackage.getContext().isActive()) {
                Map<String, JahiaModulesBeanPostProcessor> postProcessors = aPackage.getContext().getBeansOfType(JahiaModulesBeanPostProcessor.class);
                for (JahiaModulesBeanPostProcessor pp : postProcessors.values()) {
                    beanFactory.addBeanPostProcessor(pp);
                }
            }
        }
    }
}
