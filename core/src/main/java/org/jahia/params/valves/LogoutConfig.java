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
package org.jahia.params.valves;

import org.jahia.bin.listeners.JahiaContextLoaderListener.RootContextInitializedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import javax.servlet.http.HttpServletRequest;
import java.util.LinkedList;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Logout configuration settings.
 *
 * @author Sergiy Shyrkov
 */
public class LogoutConfig implements ApplicationListener<ApplicationEvent> {
    private static final Logger logger = LoggerFactory.getLogger(LogoutConfig.class);

    // Initialization on demand holder idiom: thread-safe singleton initialization
    private static class Holder {
        static final LogoutConfig INSTANCE = new LogoutConfig();
    }

    public static LogoutConfig getInstance() {
        return Holder.INSTANCE;
    }

    private LogoutConfig() {
    }

    private LinkedList<LogoutUrlProvider> logoutUrlProviders = new LinkedList<>();

    /**
     * Returns custom logout URL if the corresponding authentication provider is found. <code>null</code> otherwise.
     *
     * @param request current servlet request
     * @return custom logout URL if the corresponding authentication provider is found. <code>null</code> otherwise.
     */
    public String getCustomLogoutUrl(HttpServletRequest request) {
        return logoutUrlProviders.isEmpty() ? null : logoutUrlProviders.getFirst().getLogoutUrl(request);
    }

    public void onApplicationEvent(ApplicationEvent event) {
        Map<String, LogoutUrlProvider> beansOfType = null;
        if (event instanceof RootContextInitializedEvent) {
            RootContextInitializedEvent rootContextInitializedEvent = (RootContextInitializedEvent) event;
            beansOfType = BeanFactoryUtils.beansOfTypeIncludingAncestors(
                    rootContextInitializedEvent.getContext(),
                    LogoutUrlProvider.class);
        } else if (event instanceof ContextRefreshedEvent) {
            ContextRefreshedEvent contextRefreshedEvent = (ContextRefreshedEvent) event;
            beansOfType = BeanFactoryUtils.beansOfTypeIncludingAncestors(
                    contextRefreshedEvent.getApplicationContext(),
                    LogoutUrlProvider.class);
        }
        if (beansOfType != null && !beansOfType.isEmpty()) {
            for (LogoutUrlProvider provider : beansOfType.values()) {
                osgiBind(provider);
            }
        }
    }

    public void osgiBind(LogoutUrlProvider provider) {
        if (provider != null && provider.hasCustomLogoutUrl()) {
            logger.info("Using login URL provider {}", provider);
            logoutUrlProviders.addFirst(provider);
        }
    }

    public void osgiUnbind(LogoutUrlProvider provider) {
        if (provider != null && provider.hasCustomLogoutUrl()) {
            logger.info("Using login URL provider {}", provider);
            logoutUrlProviders.remove(provider);
        }
    }
}
