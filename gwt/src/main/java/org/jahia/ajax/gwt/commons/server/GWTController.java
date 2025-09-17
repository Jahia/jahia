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
package org.jahia.ajax.gwt.commons.server;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.server.rpc.RPC;
import com.google.gwt.user.server.rpc.RPCRequest;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.google.gwt.user.server.rpc.SerializationPolicy;
import com.google.gwt.user.server.rpc.impl.SerializabilityUtil;
import org.codehaus.plexus.util.StringUtils;
import org.jahia.bin.JahiaControllerUtils;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.sites.JahiaSitesService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.jcr.ItemNotFoundException;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

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
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public ModelAndView handleRequest(HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        long startTime = System.currentTimeMillis();
        if (allowPostMethodOnly && !"POST".equals(request.getMethod())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        if (JahiaUserManagerService.isGuest(JCRSessionFactory.getInstance().getCurrentUser())
                || !isAllowed(request)) {
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

    protected void onAfterResponseSerialized(String serializedResponse) {
        logger.debug("GWT response payload: {}", serializedResponse);
    }

    @Override
    protected void onBeforeRequestDeserialized(String serializedRequest) {
        logger.debug("GWT request payload: {}", serializedRequest);
    }

    /**
     * Checks the required permission to access the GWT service.
     *
     * @param request current HTTP request
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

        boolean allowed = false;
        try {
            JahiaUser currentUser = JCRSessionFactory.getInstance().getCurrentUser();
            // Check on given site
            String siteId = request.getParameter("site");
            JCRSessionWrapper currentUserSession = JCRSessionFactory.getInstance().getCurrentUserSession();
            if (StringUtils.isNotEmpty(siteId)) {
                if (siteId.startsWith("/")) {
                    allowed = JahiaControllerUtils.hasRequiredPermission(currentUserSession.getNode(siteId), currentUser, requiredPermission);
                } else {
                    allowed = JahiaControllerUtils.hasRequiredPermission(currentUserSession.getNodeByUUID(siteId), currentUser, requiredPermission);
                }
            }
            // Check that the user has read access and required permission at least to one site
            if (!allowed) {
                for (JCRSiteNode siteNode : JahiaSitesService.getInstance().getSitesNodeList(currentUserSession)) {
                    if (JahiaControllerUtils.hasRequiredPermission(siteNode, currentUser, requiredPermission)) {
                        // One site match
                        allowed = true;
                        break;
                    }
                }
            }

            if (session != null) {
                if (allowed) {
                    session.setAttribute(SESSION_ATTRIBUTE_PERMISSION_CHECK, Boolean.TRUE);
                } else {
                    session.removeAttribute(SESSION_ATTRIBUTE_PERMISSION_CHECK);
                }
            }
        } catch (ItemNotFoundException | PathNotFoundException e) {
            logger.debug("Item not found for User: {}, Site: {}, Path: {}",
                    getCurrentUserSafely(),
                    request.getParameter("site"),
                    e.getMessage());
        } catch (RepositoryException e) {
            logger.warn("Repository error during permission check for user: {}, set this class in debug for more detail", getCurrentUserSafely());
            logger.debug("site: {}. Error: {}",
                    request.getParameter("site"),
                    e.getMessage(), e);
        }
        return allowed;
    }

    private String getCurrentUserSafely() {
        try {
            JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser();
            return user != null ? user.getUsername() : "<unknown>";
        } catch (Exception e) {
            return "<error-getting-user>";
        }
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

            Class<?> serializer = SerializabilityUtil.hasCustomFieldSerializer(Long.class);
            if (serializer == null) {
                logger.warn("Serializer for java.lang.Long is null, some calls will fail");
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
     * @param remoteServiceName the Spring bean ID for the target GWT service to be called
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
     * @param requiredPermission a permission, required to access the GWT services
     */
    public void setRequiredPermission(String requiredPermission) {
        this.requiredPermission = requiredPermission;
    }

    /**
     * Set it to <code>true</code> if we allow to cache the successful required permission check in a session.
     *
     * @param requiredPermissionCheckCache <code>true</code> if we allow to cache the successful required permission check in a session; <code>false</code> otherwise
     */
    public void setRequiredPermissionCheckCache(boolean requiredPermissionCheckCache) {
        this.requiredPermissionCheckCache = requiredPermissionCheckCache;
    }
}
