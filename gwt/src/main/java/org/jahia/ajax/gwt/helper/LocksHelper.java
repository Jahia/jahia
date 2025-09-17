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
package org.jahia.ajax.gwt.helper;

import org.atmosphere.cpr.AtmosphereResource;
import org.jahia.ajax.gwt.commons.server.ManagedGWTResource;
import org.jahia.api.Constants;
import org.jahia.bin.filters.jcr.JcrSessionFilter;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.LanguageCodeConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import javax.jcr.RepositoryException;
import javax.jcr.lock.LockException;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper related to locking/unlocking
 */
public class LocksHelper implements ApplicationListener<ApplicationEvent> {

    private static final Logger logger = LoggerFactory.getLogger(LocksHelper.class);

    @Override
    @SuppressWarnings("unchecked")
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof JahiaContextLoaderListener.HttpSessionDestroyedEvent) {
            HttpSession httpSession = ((JahiaContextLoaderListener.HttpSessionDestroyedEvent) applicationEvent).getSession();
            if (httpSession.getAttribute(Constants.SESSION_USER) != null) {
                if (JCRSessionFactory.getInstance().getCurrentUser() != null) {
                    closeAllLocks(httpSession);
                } else {
                    try {
                        JCRSessionFactory.getInstance().setCurrentUser((JahiaUser) httpSession.getAttribute(Constants.SESSION_USER));
                        closeAllLocks(httpSession);
                    } finally {
                        JcrSessionFilter.endRequest();
                    }
                }
            }
        } else if (applicationEvent instanceof ManagedGWTResource.AtmosphereClientDisconnectedEvent) {
            AtmosphereResource resource = ((ManagedGWTResource.AtmosphereClientDisconnectedEvent) applicationEvent).getResource();
            HttpSession httpSession = resource.getRequest().getSession();
            Map<String, List<String>> locks = (Map<String, List<String>>) httpSession.getAttribute("engineLocks");
            Map<String, String> atmosphereResources = (Map<String, String>) resource.getRequest().getSession().getAttribute("atmosphereResources");
            if (atmosphereResources != null && atmosphereResources.containsKey(resource.uuid())) {
                String key = atmosphereResources.remove(resource.uuid());
                if (locks != null && locks.containsKey(key)) {
                    if (JCRSessionFactory.getInstance().getCurrentUser() != null) {
                        closeAllLocks(locks.remove(key));
                    } else {
                        try {
                            JCRSessionFactory.getInstance()
                                    .setCurrentUser((JahiaUser) httpSession.getAttribute(Constants.SESSION_USER));
                            closeAllLocks(locks.remove(key));
                        } finally {
                            JcrSessionFilter.endRequest();
                        }
                    }
                }
            }
        } else if (applicationEvent instanceof ManagedGWTResource.AtmosphereClientReadyEvent) {
            AtmosphereResource resource = ((ManagedGWTResource.AtmosphereClientReadyEvent) applicationEvent).getResource();
            if (resource.getRequest().getParameter("windowId") != null) {
                Map<String, String> atmosphereResouces = (Map<String, String>) resource.getRequest().getSession().getAttribute("atmosphereResources");
                if (atmosphereResouces == null) {
                    atmosphereResouces = new HashMap<String, String>();
                }
                atmosphereResouces.put(resource.uuid(), resource.getRequest().getParameter("windowId"));
                resource.getRequest().getSession().setAttribute("atmosphereResources", atmosphereResouces);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void closeAllLocks(HttpSession httpSession) {
        Map<String, List<String>> locks = (Map<String, List<String>>) httpSession.getAttribute("engineLocks");
        if (locks != null) {
            for (List<String> list : locks.values()) {
                closeAllLocks(list);
            }
        }
    }

    private void closeAllLocks(List<String> locks) {
        try {
            for (String lock : locks) {
                String[] vals = lock.split("/");
                String localeForLock = vals[0];
                String lockedNodeId = vals[1];
                JCRSessionWrapper jcrsession = JCRSessionFactory.getInstance().getCurrentUserSession(null, LanguageCodeConverters.languageCodeToLocale(localeForLock));
                try {
                    jcrsession.getNodeByUUID(lockedNodeId).unlock("engine");
                } catch (LockException e) {
                    // We still want other nodes to get unlocked, so just log and not re-throw.
                    logger.warn("Problem while trying to unlock node: " + lockedNodeId + " - " + e);
                } catch (Exception e) {
                    // We still want other nodes to get unlocked, so just log and not re-throw.
                    logger.error("Unexpected problem while trying to unlock node - node may remain locked: " + lockedNodeId, e);
                }
            }
            locks.clear();
        } catch (RepositoryException e) {
            logger.error("Cannot release locks", e);
        }
    }

}
