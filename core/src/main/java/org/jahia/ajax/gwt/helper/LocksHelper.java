/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.ajax.gwt.helper;

import org.atmosphere.cpr.AtmosphereResource;
import org.jahia.ajax.gwt.commons.server.ManagedGWTResource;
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
public class LocksHelper implements ApplicationListener {

    private static Logger logger = LoggerFactory.getLogger(LocksHelper.class);

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof JahiaContextLoaderListener.HttpSessionDestroyedEvent) {
            HttpSession httpSession = ((JahiaContextLoaderListener.HttpSessionDestroyedEvent) applicationEvent).getSession();
            if (httpSession.getAttribute(org.jahia.api.Constants.SESSION_USER) != null) {
                if (JCRSessionFactory.getInstance().getCurrentUser() != null) {
                    closeAllLocks(httpSession);
                } else {
                    try {
                        JCRSessionFactory.getInstance().setCurrentUser((JahiaUser) httpSession.getAttribute(org.jahia.api.Constants.SESSION_USER));
                        closeAllLocks(httpSession);
                    } finally {
                        JcrSessionFilter.endRequest();
                    }
                }
            }
        } else if (applicationEvent instanceof ManagedGWTResource.AtmosphereClientDisconnectedEvent) {
            AtmosphereResource resource = ((ManagedGWTResource.AtmosphereClientDisconnectedEvent) applicationEvent).getResource();
            HttpSession httpSession = resource.getRequest().getSession();
            Map<String,List<String>> locks = (Map<String, List<String>>) httpSession.getAttribute("engineLocks");
            Map<String,String> atmosphereResources = (Map<String,String>) resource.getRequest().getSession().getAttribute("atmosphereResources");
            if (atmosphereResources != null && atmosphereResources.containsKey(resource.uuid())) {
                String key = atmosphereResources.remove(resource.uuid());
                if (locks != null && locks.containsKey(key)) {
                    closeAllLocks(locks.remove(key));
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
