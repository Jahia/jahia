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

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.utils.manifest.Clause;
import org.apache.felix.utils.manifest.Parser;
import org.eclipse.gemini.blueprint.extender.OsgiBeanFactoryPostProcessor;
import org.eclipse.gemini.blueprint.util.OsgiStringUtils;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.templates.JahiaModuleAwareProcessor;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;

/**
 * Post processor that registers the {@link JahiaModuleAwareProcessor} for beans, that would like to be aware of the current module.
 * 
 * Also exposes Spring beans which are in the <code>Export-Package</code> manifest header as OSGi services and "imports" external services
 * as Spring beans in the module Spring context if they are mentioned in the <code>Import-Package</code> manifest header.
 * 
 * @author Sergiy Shyrkov
 */
public class JahiaOsgiBeanFactoryPostProcessor implements OsgiBeanFactoryPostProcessor {

    private static final String BEAN_NAME_PROPERTY = "org.jahia.modules.spring.bean.name";

    private static final Logger logger = LoggerFactory.getLogger(JahiaOsgiBeanFactoryPostProcessor.class);

    protected static final boolean PERFORM_CROSS_CONTEXT_BEAN_IMPORT = false;

    protected static int exportServices(Bundle bundle, ListableBeanFactory beanFactory) {
        String headerValue = (String) bundle.getHeaders().get("Export-Package");
        if (StringUtils.isEmpty(headerValue)) {
            // bundle does not export anything
            return 0;
        }

        String[] beanNames = beanFactory.getBeanNamesForType(null, false, false);
        if (beanNames.length == 0) {
            return 0;
        }
        int exported = 0;
        Clause[] headerClauses = Parser.parseHeader(headerValue);
        for (String beanName : beanNames) {
            try {
                Object bean = beanFactory.getBean(beanName);
                String beanClazz = bean.getClass().getName();
                if (!isSystemClass(beanClazz) && isClassInHeader(beanClazz, headerClauses)) {
                    List<String> classNames = new ArrayList<String>();
                    classNames.add(bean.getClass().getName());
                    for (Class<?> classInterface : bean.getClass().getInterfaces()) {
                        if (!isSystemClass(classInterface.getName())
                                && isClassInHeader(classInterface.getName(), headerClauses)) {
                            classNames.add(classInterface.getName());
                        }
                    }
                    Hashtable<String, String> serviceProperties = new Hashtable<String, String>(1);
                    serviceProperties.put(BEAN_NAME_PROPERTY, beanName);
                    bundle.getBundleContext().registerService(classNames.toArray(new String[classNames.size()]), bean,
                            serviceProperties);
                    exported++;
                    logger.debug("Exported Spring bean {} as OSGi service under names: {}", beanName, classNames);
                }
            } catch (Throwable t) {
                logger.warn("Couldn't register bean " + beanName + " since it couldn't be retrieved: " + t.getMessage());
            }
        }

        return exported;
    }

    protected static int importServices(BundleContext bundleContext, SingletonBeanRegistry beanFactory) {
        String headerValue = (String) bundleContext.getBundle().getHeaders().get("Import-Package");
        if (StringUtils.isEmpty(headerValue)) {
            // bundle does not import anything
            return 0;
        }
        int imported = 0;
        try {
            ServiceReference[] refs = bundleContext.getAllServiceReferences(null, "(" + BEAN_NAME_PROPERTY + "=*)");
            if (refs != null && refs.length > 0) {
                Clause[] headerClauses = Parser.parseHeader(headerValue);
                for (ServiceReference ref : refs) {
                    Object service = bundleContext.getService(ref);
                    String clazzName = service.getClass().getName();
                    if (!isSystemClass(clazzName) && isClassInHeader(clazzName, headerClauses)) {
                        String beanId = ref.getProperty(BEAN_NAME_PROPERTY).toString();
                        beanFactory.registerSingleton(beanId, service);
                        imported++;
                        logger.debug("Importing Spring bean with ID {}", beanId);
                    }
                }
            }
        } catch (InvalidSyntaxException e) {
            throw new IllegalArgumentException(e);
        }

        return imported;
    }

    private static boolean isClassInHeader(String classOrInterfaceName, Clause[] headerClauses) {
        String classPackageName = StringUtils.substringBeforeLast(classOrInterfaceName, ".");
        for (Clause clause : headerClauses) {
            String importedPackageName = clause.getName();
            if (classPackageName.equals(importedPackageName)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isSystemClass(String classOrInterfaceName) {
        // we ignore all Java classes for the moment.
        // we ignore all Felix framework classes for the moment.
        return classOrInterfaceName.startsWith("java.")
                || classOrInterfaceName.startsWith("org.apache.felix.framework.");
    }

    @Override
    public void postProcessBeanFactory(BundleContext bundleContext, ConfigurableListableBeanFactory beanFactory)
            throws BeansException, InvalidSyntaxException, BundleException {
        long timer = System.currentTimeMillis();
        String bundleName = OsgiStringUtils.nullSafeNameAndSymName(bundleContext.getBundle());

        logger.info("Start post-processing of the Spring bean factory for bundle {}", bundleName);

        // register bean post-processor for JahiaModuleAware implementors
        beanFactory.addBeanPostProcessor(new JahiaModuleAwareProcessor(BundleUtils.getModuleForBundle(bundleContext
                .getBundle())));

        int imported = PERFORM_CROSS_CONTEXT_BEAN_IMPORT ? importServices(bundleContext, beanFactory) : 0;

        logger.info("Finished post-processing of the Spring bean factory for bundle {} in {} ms.{}", new Object[] {
                bundleName, (System.currentTimeMillis() - timer),
                imported > 0 ? (" " + imported + " beans were imported into the module context.") : "" });
    }

}
