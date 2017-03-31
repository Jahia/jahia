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
package org.jahia.services.content.interceptor;

import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.content.JCRStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

/**
 * Helper bean for registering {@link PropertyInterceptor} instances within {@link JCRStoreService}.
 * 
 * @author Sergiy Shyrkov
 */
public class PropertyInterceptorRegistrator implements InitializingBean, DisposableBean {

    private static final Logger logger = LoggerFactory
            .getLogger(PropertyInterceptorRegistrator.class);

    private JCRStoreService jcrStoreService;

    private int position;

    private PropertyInterceptor propertyInterceptor;

    public void afterPropertiesSet() throws Exception {
        if (propertyInterceptor != null) {
            logger.info("Registering property interceptor {}", propertyInterceptor);

            jcrStoreService.removeInterceptor(propertyInterceptor);

            if (position != -1) {
                jcrStoreService.addInterceptor(position, propertyInterceptor);
            } else {
                jcrStoreService.addInterceptor(propertyInterceptor);
            }
        }
    }

    public void destroy() throws Exception {
        if (JahiaContextLoaderListener.isRunning() && jcrStoreService != null && propertyInterceptor != null) {
            if (propertyInterceptor instanceof BaseInterceptor) {
                logger.info("Unregistering property interceptor "
                        + propertyInterceptor.getClass().getName() + " for types "
                        + ((BaseInterceptor) propertyInterceptor).getRequiredTypes()
                        + " and selectors "
                        + ((BaseInterceptor) propertyInterceptor).getSelectors());

            } else {
                logger.info("Unregistering property interceptor {}", propertyInterceptor.getClass()
                        .getName());
            }

            jcrStoreService.removeInterceptor(propertyInterceptor);
        }
    }

    public void setJcrStoreService(JCRStoreService jcrStoreService) {
        this.jcrStoreService = jcrStoreService;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setPropertyInterceptor(PropertyInterceptor propertyInterceptor) {
        this.propertyInterceptor = propertyInterceptor;
    }
}
