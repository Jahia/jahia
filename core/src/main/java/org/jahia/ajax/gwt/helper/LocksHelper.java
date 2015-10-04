package org.jahia.ajax.gwt.helper;

import org.atmosphere.cpr.AtmosphereResource;
import org.jahia.ajax.gwt.commons.server.ManagedGWTResource;
import org.jahia.bin.filters.jcr.JcrSessionFilter;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.utils.LanguageCodeConverters;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocksHelper implements ApplicationListener {

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
        for (List<String> list : locks.values()) {
            closeAllLocks(list);
        }
    }

    private void closeAllLocks(List<String> locks) {
        try {
            for (String lock : locks) {
                String[] vals = lock.split("/");
                JCRSessionWrapper jcrsession = JCRSessionFactory.getInstance().getCurrentUserSession(null, LanguageCodeConverters.languageCodeToLocale(vals[0]));
                jcrsession.getNodeByUUID(vals[1]).unlock("engine");
            }
            locks.clear();
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

}
