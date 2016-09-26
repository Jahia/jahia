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

import org.eclipse.gemini.blueprint.extender.OsgiBeanFactoryPostProcessor;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.osgi.BundleUtils;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.templates.JahiaModuleAwareProcessor;
import org.jahia.services.templates.JahiaModulesBeanPostProcessor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
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

        if (!BundleUtils.isJahiaModuleBundle(bundleContext.getBundle())) {
            return;
        }
        long timer = System.currentTimeMillis();
        String bundleName = OsgiStringUtils.nullSafeNameAndSymName(bundleContext.getBundle());

        logger.info("Start post-processing of the Spring bean factory for bundle {}", bundleName);

        registerCoreBeanPostProcessors(bundleContext, beanFactory);

        registerModulesBeanPostProcessors(beanFactory);

        logger.info("Finished post-processing of the Spring bean factory for bundle {} in {} ms", bundleName,
                System.currentTimeMillis() - timer);
    }

    private void registerCoreBeanPostProcessors(BundleContext bundleContext, ConfigurableListableBeanFactory beanFactory) {

        // Register bean post-processor for JahiaModuleAware implementors
        beanFactory.addBeanPostProcessor(new JahiaModuleAwareProcessor(BundleUtils.getModule(bundleContext.getBundle())));

        // Register bean post-processor for expected beans lookup
        beanFactory.addBeanPostProcessor(new BeanPostProcessor() {

            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
                return bean;
            }

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                // check if there is expected beans for this spring context
                SpringContextSingleton.releaseExpectedBean(beanName);
                return bean;
            }
        });
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
