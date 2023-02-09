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
