/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bin.filters;

import javax.servlet.Filter;
import javax.servlet.ServletException;

import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.SpringContextSingleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;

/**
 * Extends the behavior of standard Spring's {@link DelegatingFilterProxy} to allow for delegate bean defined either in the core or in a module.
 *
 * @author Sergiy Shyrkov
 */
public class ModuleDelegatingFilterProxy extends DelegatingFilterProxy {

    private static final Logger logger = LoggerFactory.getLogger(ModuleDelegatingFilterProxy.class);

    @Override
    protected Filter initDelegate(WebApplicationContext wac) throws ServletException {
        Filter delegate = null;
        String targetBeanName = getTargetBeanName();
        try {
            delegate = (Filter) SpringContextSingleton.getBeanInModulesContext(targetBeanName);
            logger.info("Found filter delegate for {}: {}", targetBeanName, delegate);
            if (isTargetFilterLifecycle()) {
                delegate.init(getFilterConfig());
            }
        } catch (NoSuchBeanDefinitionException e) {
            // we re-throw the exception if context has been initialized already; if not, we defer delegate lookup
            if (JahiaContextLoaderListener.isContextInitialized()) {
                throw e;
            } else {
                logger.info("Deferring filter delegate lookup {} as context has not been fully initialized yet",
                        targetBeanName);
            }
        }
        return delegate;
    }
}
