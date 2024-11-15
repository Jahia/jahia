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
package org.jahia.params.valves;

import org.jahia.bin.listeners.JahiaContextLoaderListener.RootContextInitializedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Login configuration settings.
 *
 * @author Sergiy Shyrkov
 */
public class LoginConfig implements ApplicationListener<ApplicationEvent> {

    private static final Logger logger = LoggerFactory.getLogger(LoginConfig.class);

    private LoginConfig() {
    }

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final LoginConfig INSTANCE = new LoginConfig();
    }

    public static LoginConfig getInstance() {
        return Holder.INSTANCE;
    }

    private LinkedList<LoginUrlProvider> loginUrlProviders = new LinkedList<>();

    /**
     * Returns custom login URL if the corresponding authentication provider is found. <code>null</code> otherwise.
     *
     * @param request current servlet request
     * @return custom login URL if the corresponding authentication provider is found. <code>null</code> otherwise.
     */
    public String getCustomLoginUrl(HttpServletRequest request) {
        return loginUrlProviders.isEmpty() ? null : loginUrlProviders.getFirst().getLoginUrl(request);
    }

    public void onApplicationEvent(ApplicationEvent event) {
        Map<String, LoginUrlProvider> beansOfType = null;
        if (event instanceof RootContextInitializedEvent) {
            RootContextInitializedEvent rootContextInitializedEvent = (RootContextInitializedEvent) event;
            beansOfType = BeanFactoryUtils.beansOfTypeIncludingAncestors(
                    rootContextInitializedEvent.getContext(),
                    LoginUrlProvider.class);
        } else if (event instanceof ContextRefreshedEvent) {
            ContextRefreshedEvent contextRefreshedEvent = (ContextRefreshedEvent) event;
            beansOfType = BeanFactoryUtils.beansOfTypeIncludingAncestors(
                    contextRefreshedEvent.getApplicationContext(),
                    LoginUrlProvider.class);
        }
        if (beansOfType != null && !beansOfType.isEmpty()) {
            for (LoginUrlProvider provider : beansOfType.values()) {
                osgiBind(provider);
            }
        }
    }

    public void osgiBind(LoginUrlProvider provider) {
        if (provider != null && provider.hasCustomLoginUrl()) {
            logger.info("Using login URL provider {}", provider);
            loginUrlProviders.addFirst(provider);
        }
    }

    public void osgiUnbind(LoginUrlProvider provider) {
        if (provider != null && provider.hasCustomLoginUrl()) {
            logger.info("Using login URL provider {}", provider);
            loginUrlProviders.remove(provider);
        }
    }

}
