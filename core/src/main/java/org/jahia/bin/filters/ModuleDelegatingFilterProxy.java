/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
