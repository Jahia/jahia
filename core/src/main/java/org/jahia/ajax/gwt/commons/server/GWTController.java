/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.commons.server;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gwt.user.server.rpc.SerializationPolicy;

import org.jahia.services.content.JCRSessionFactory;
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

    private static final long serialVersionUID = -74193665963116797L;

    private final static Logger logger = LoggerFactory.getLogger(GWTController.class);

    private String remoteServiceName;

    private Integer sessionExpiryTime = null;

    private ServletContext servletContext;
    
    private ApplicationContext applicationContext;
    
    private boolean allowPostMethodOnly = true;

    private boolean requireAuthenticatedUser = true;

    public void setSessionExpiryTime(int sessionExpiryTime) {
        this.sessionExpiryTime = sessionExpiryTime;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    public ModelAndView handleRequest(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        long startTime = System.currentTimeMillis();
        if (allowPostMethodOnly && !"POST".equals(request.getMethod())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
        if (requireAuthenticatedUser && JahiaUserManagerService.isGuest(JCRSessionFactory.getInstance().getCurrentUser())) {
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

    @Override
    public String processCall(String payload) throws SerializationException {
        RemoteService remoteService = null;
        RPCRequest rpcRequest = null;
        try {
            remoteService = (RemoteService) applicationContext.getBean(remoteServiceName);
            setServiceData(remoteService, false);

            rpcRequest = RPC.decodeRequest(payload, remoteService.getClass(), this);
            
            if (logger.isDebugEnabled()) {
                logger.debug("Executing method " + rpcRequest.getMethod());
            }

            return RPC.invokeAndEncodeResponse(remoteService, rpcRequest
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

    @Override protected SerializationPolicy doGetSerializationPolicy(HttpServletRequest request, String moduleBaseURL,
                                                                     String strongName) {
        SerializationPolicy policy = super.doGetSerializationPolicy(request, moduleBaseURL, strongName);
        if (policy == null) {
            // NEVER use or cache a legacy serializer
            return new SerializationPolicy() {
                @Override public boolean shouldDeserializeFields(Class<?> clazz) {
                    return (clazz != Object.class);
                }

                @Override public boolean shouldSerializeFields(Class<?> clazz) {
                    return (clazz != Object.class);
                }

                @Override public void validateDeserialize(Class<?> clazz) throws SerializationException {
                }

                @Override public void validateSerialize(Class<?> clazz) throws SerializationException {
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

    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext; 
    }

    public void log(String message, Throwable t) {
        logger.error(message, t);
    }

    public void log(String msg) {
        logger.info(msg);
    }

    public void setAllowPostMethodOnly(boolean allowPostMethodOnly) {
        this.allowPostMethodOnly = allowPostMethodOnly;
    }

    public void setRequireAuthenticatedUser(boolean requireAuthenticatedUser) {
        this.requireAuthenticatedUser = requireAuthenticatedUser;
    }
}
