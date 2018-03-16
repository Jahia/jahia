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
package org.jahia.ajax.gwt.commons.server;

import javax.jcr.ItemNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gwt.user.server.rpc.SerializationPolicy;

import org.codehaus.plexus.util.StringUtils;
import org.jahia.bin.JahiaControllerUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Spring MVC controller implementation to dispatch requests to GWT services.
 *
 * @author Sergiy Shyrkov
 */
public class GWTController extends RemoteServiceServlet implements Controller,
        ServletContextAware, ApplicationContextAware {

    private static final String SESSION_ATTRIBUTE_PERMISSION_CHECK = "org.jahia.gwt.requiredPermission.ok";

    private static final long serialVersionUID = -74193665963116797L;

    private static final Logger logger = LoggerFactory.getLogger(GWTController.class);

    private String remoteServiceName;

    private Integer sessionExpiryTime = null;

    private ServletContext servletContext;

    private ApplicationContext applicationContext;

    private boolean allowPostMethodOnly = true;

    private boolean requireAuthenticatedUser = true;

    /**
     * A permission, required to access the GWT services. A <code>null</code> or an empty value means no permission check is done.
     */
    private String requiredPermission;

    /**
     * Do we allow to cache the successful required permission check in a session?
     */
    private boolean requiredPermissionCheckCache = true;

    public void setSessionExpiryTime(int sessionExpiryTime) {
        this.sessionExpiryTime = sessionExpiryTime;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        long startTime = System.currentTimeMillis();
        if (allowPostMethodOnly && !"POST".equals(request.getMethod())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        if (requireAuthenticatedUser
                && JahiaUserManagerService.isGuest(JCRSessionFactory.getInstance().getCurrentUser())
                || StringUtils.isNotEmpty(requiredPermission) && !isAllowed(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }

        final HttpSession session = request.getSession(false);
        if (session != null) {
            if (sessionExpiryTime != null && session.getMaxInactiveInterval() != sessionExpiryTime * 60) {
                session.setMaxInactiveInterval(sessionExpiryTime * 60);
            }
        }
        doPost(request, response);
        if (logger.isDebugEnabled()) {
            logger.debug("Handled request to GWT service '{}' in {} ms", remoteServiceName, System.currentTimeMillis() - startTime);
        }
        return null;
    }

    /**
     * Checks the required permission to access the GWT service.
     *
     * @param request
     *            current HTTP request
     * @return <code>true</code> if the current user is permitted to access the GWT service
     */
    private boolean isAllowed(HttpServletRequest request) {
        HttpSession session = null;
        if (requiredPermissionCheckCache) {
            // see if we have already performed the check
            session = request.getSession(false);
            if (session != null && session.getAttribute(SESSION_ATTRIBUTE_PERMISSION_CHECK) != null) {
                return true;
            }
        }

        boolean debugEnabled = logger.isDebugEnabled();
        long startTime = debugEnabled ? System.currentTimeMillis() : 0;
        boolean allowed = false;
        try {
            JCRNodeWrapper targetNode = getTargetNodeForPermissionCheck(request);
            JahiaUser currentUser = JCRSessionFactory.getInstance().getCurrentUser();
            allowed = targetNode != null
                    && JahiaControllerUtils.hasRequiredPermission(targetNode, currentUser, requiredPermission);
            if (session != null) {
                if (allowed) {
                    session.setAttribute(SESSION_ATTRIBUTE_PERMISSION_CHECK, Boolean.TRUE);
                } else {
                    session.removeAttribute(SESSION_ATTRIBUTE_PERMISSION_CHECK);
                }
            }
            if (debugEnabled) {
                logger.debug(
                        "Checked permission for GWT service access and target node {} in {} ms."
                                + " User {} is {}allowed to access it.",
                        new Object[] { targetNode != null ? targetNode.getPath() : null,
                                System.currentTimeMillis() - startTime, currentUser.getUsername(),
                                allowed ? "" : "NOT " });
            }
        } catch (ItemNotFoundException e) {
            // ignore
        } catch (RepositoryException e) {
            logger.warn(e.getMessage(), e);
        }
        return allowed;
    }

    /**
     * Detects the target JCR node for permission check.
     *
     * @param request
     *            current HTTP request
     *
     * @return the detected target JCR node for permission check
     */
    private JCRNodeWrapper getTargetNodeForPermissionCheck(HttpServletRequest request) {
        String siteId = request.getParameter("site");

        try {
            JCRSessionWrapper currentUserSession = JCRSessionFactory.getInstance().getCurrentUserSession();
            if (StringUtils.isNotEmpty(siteId)) {
                return currentUserSession.getNodeByUUID(siteId);
            } else {
                JahiaSitesService siteService = JahiaSitesService.getInstance();
                JahiaSite defaultSite = siteService.getDefaultSite();
                if (defaultSite != null) {
                    return (JCRSiteNode) defaultSite;
                }
            }

            return currentUserSession.getRootNode();
        } catch (ItemNotFoundException e) {
            // no access
        } catch (RepositoryException e) {
            logger.warn("Unble to find target JCR node for permission check", e);
        }

        return null;
    }

    @Override
    public String processCall(String payload) throws SerializationException {
        RemoteService remoteService = null;
        RPCRequest rpcRequest = null;
        try {
            remoteService = (RemoteService) applicationContext.getBean(remoteServiceName);
            setServiceData(remoteService, false);

            rpcRequest = RPC.decodeRequest(payload, remoteService.getClass(), this);

            if (logger.isDebugEnabled()) {
                logger.debug("Executing method {}", rpcRequest.getMethod());
            }

            return JahiaRPC.invokeAndEncodeResponse(remoteService, rpcRequest
                    .getMethod(), rpcRequest.getParameters(), rpcRequest
                    .getSerializationPolicy());
        } catch (Exception e) {
            if (rpcRequest != null) {
                logger.error("An error occurred calling the GWT service method " + rpcRequest.getMethod() + ". Cause: " + e.getMessage(), e);
            } else {
                logger.error("An error occurred calling the GWT service " + (remoteService != null ? remoteService.getClass().getName() : remoteServiceName) + ". Cause: " + e.getMessage(), e);
            }
            return RPC.encodeResponseForFailure(null, e);
        } finally {
            if (remoteService != null) {
                setServiceData(remoteService, true);
            }

        }
    }

    @Override
    protected SerializationPolicy doGetSerializationPolicy(HttpServletRequest request, String moduleBaseURL,
                                                                     String strongName) {
        SerializationPolicy policy = super.doGetSerializationPolicy(request, moduleBaseURL, strongName);
        if (policy == null) {
            // NEVER use or cache a legacy serializer
            return new SerializationPolicy() {

                @Override
                public boolean shouldDeserializeFields(Class<?> clazz) {
                    return (clazz != Object.class);
                }

                @Override
                public boolean shouldSerializeFields(Class<?> clazz) {
                    return (clazz != Object.class);
                }

                @Override
                public void validateDeserialize(Class<?> clazz) throws SerializationException {
                }

                @Override
                public void validateSerialize(Class<?> clazz) throws SerializationException {
                }
            };
//            throw new UnsupportedOperationException("Bad id, javascript is probably not uptodate - flush your browser cache");
        }
        return policy;
    }

    /**
     * Injects the target GWT service to be called.
     *
     * @param remoteServiceName
     *            the Spring bean ID for the target GWT service to be called
     */
    public void setRemoteServiceName(String remoteServiceName) {
        this.remoteServiceName = remoteServiceName;
    }

    private void setServiceData(RemoteService remoteService, boolean cleanUp) {
        if (remoteService instanceof RequestResponseAware) {
            RequestResponseAware service = (RequestResponseAware) remoteService;
            service.setRequest(cleanUp ? null : getThreadLocalRequest());
            service.setResponse(cleanUp ? null : getThreadLocalResponse());
        }
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void log(String message, Throwable t) {
        logger.error(message, t);
    }

    @Override
    public void log(String msg) {
        logger.info(msg);
    }

    public void setAllowPostMethodOnly(boolean allowPostMethodOnly) {
        this.allowPostMethodOnly = allowPostMethodOnly;
    }

    public void setRequireAuthenticatedUser(boolean requireAuthenticatedUser) {
        this.requireAuthenticatedUser = requireAuthenticatedUser;
    }

    /**
     * Sets a permission, required to access the GWT services. A <code>null</code> or an empty value means no permission check is done.
     *
     * @param requiredPermission
     *            a permission, required to access the GWT services
     */
    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

    /**
     * Set it to <code>true</code> if we allow to cache the successful required permission check in a session.
     *
     * @param requiredPermissionCheckCache
     *            <code>true</code> if we allow to cache the successful required permission check in a session; <code>false</code> otherwise
     */
    public void setRequiredPermissionCheckCache(boolean requiredPermissionCheckCache) {
        this.requiredPermissionCheckCache = requiredPermissionCheckCache;
    }
}
