/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.operations.valves;

import java.util.Iterator;

import javax.portlet.MimeResponse;
import javax.portlet.PortletException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.pluto.PortletContainer;
import org.apache.pluto.PortletContainerException;
import org.apache.pluto.PortletWindow;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.driver.AttributeKeys;
import org.apache.pluto.driver.core.PortalRequestContext;
import org.apache.pluto.driver.core.PortletWindowImpl;
import org.apache.pluto.driver.services.portal.PortletWindowConfig;
import org.apache.pluto.driver.url.PortalURL;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.pipelines.PipelineException;
import org.jahia.pipelines.valves.Valve;
import org.jahia.pipelines.valves.ValveContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.applications.pluto.JahiaUserRequestWrapper;
import org.jahia.services.cache.CacheKeyGeneratorService;
import org.jahia.services.cache.CacheService;

public class PlutoProcessActionValve implements Valve {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(PlutoProcessActionValve.class);
    private CacheService cacheInstance = null;
    private CacheKeyGeneratorService cacheKeyGeneratorService = null;

    public PlutoProcessActionValve() {
    }

    /**
     * initialize
     */
    public void initialize() {
    }

    /**
     * invoke
     *
     * @param context      Object
     * @param valveContext ValveContext
     * @throws PipelineException
     */
    public void invoke(Object context, ValveContext valveContext)
            throws PipelineException {

        ProcessingContext processingContext = (ProcessingContext) context;
        PortletContainer container = (PortletContainer)
                ((ParamBean) processingContext).getContext().getAttribute(AttributeKeys.PORTLET_CONTAINER);

        try {
            final ParamBean jParams = ((ParamBean) processingContext);
            JahiaUserRequestWrapper request = new JahiaUserRequestWrapper(jParams.getUser(), jParams.getRequest());
            HttpServletResponse response = jParams.getResponse();
                PortalRequestContext portalRequestContext =
                    new PortalRequestContext(((ParamBean)processingContext).getContext(), request, response);

                PortalURL portalURL = portalRequestContext.getRequestedPortalURL();
                String actionWindowId = portalURL.getActionWindow();
                String resourceWindowId = portalURL.getResourceWindow();

                PortletWindowConfig actionWindowConfig = null;
                PortletWindowConfig resourceWindowConfig = null;

                if (resourceWindowId != null){
                    resourceWindowConfig = PortletWindowConfig.fromId(resourceWindowId);
                } else if(actionWindowId != null){
                     actionWindowConfig = PortletWindowConfig.fromId(actionWindowId);
                }

                // Action window config will only exist if there is an action request.
                if (actionWindowConfig != null) {
                    flushPortletCache(processingContext, jParams, actionWindowConfig);
                    PortletWindowImpl portletWindow = new PortletWindowImpl(
                            actionWindowConfig, portalURL);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Processing action request for window: "
                                + portletWindow.getId().getStringId());
                    }

                    EntryPointInstance entryPointInstance = ServicesRegistry.getInstance().getApplicationsManagerService().getEntryPointInstance(actionWindowConfig.getMetaInfo());
                    if (entryPointInstance != null) {
                        request.setEntryPointInstance(entryPointInstance);
                    } else {
                        logger.warn("Couldn't find related entryPointInstance, roles might not work properly !");
                    }
                    copyAttribute("org.jahia.data.JahiaData", jParams, request, portletWindow);
                    copyAttribute("currentRequest", jParams, request, portletWindow);
                    copyAttribute("currentSite", jParams, request, portletWindow);
                    copyAttribute("currentPage", jParams, request, portletWindow);
                    copyAttribute("currentUser", jParams, request, portletWindow);
                    copyAttribute("currentJahia", jParams, request, portletWindow);
                    try {
                        container.doAction(portletWindow, request, jParams.getResponse());
                    } catch (PortletContainerException ex) {
                        throw new ServletException(ex);
                    } catch (PortletException ex) {
                        throw new ServletException(ex);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("Action request processed.\n\n");
                    }
                    return;
                }
                //Resource request
                else if (resourceWindowConfig != null) {
                    try {
                        if (request.getParameterNames().hasMoreElements())
                            setPublicRenderParameter(container, request, portalURL, portalURL.getResourceWindow());
                    } catch (PortletContainerException e) {
                        logger.warn(e);
                    }
                    PortletWindowImpl portletWindow = new PortletWindowImpl(
                                       resourceWindowConfig, portalURL);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Processing resource Serving request for window: "
                                       + portletWindow.getId().getStringId());
                    }
                    try {
                        container.doServeResource(portletWindow, request, jParams.getRealResponse());
                    } catch (PortletContainerException ex) {
                        throw new ServletException(ex);
                    } catch (PortletException ex) {
                        throw new ServletException(ex);
                    }
                    if (logger.isDebugEnabled()) {
                       logger.debug("Action request processed.\n\n");
                    }
                    return;
                }
        } catch (Exception t) {
            logger.error("Error while processing action", t);
        } finally {
        }

        // continue valve processing...
        valveContext.invokeNext(context);
    }

    private void flushPortletCache(ProcessingContext processingContext, ParamBean jParams, PortletWindowConfig actionWindowConfig) throws JahiaException {
        String cacheKey = null;
        // Check if cache is available for this portlet
        cacheKey = "portlet_instance_" + actionWindowConfig.getMetaInfo();
        final EntryPointInstance entryPointInstance = ServicesRegistry.getInstance().getApplicationsManagerService().getEntryPointInstance(actionWindowConfig.getMetaInfo());
        if (entryPointInstance != null && entryPointInstance.getCacheScope() != null && entryPointInstance.getCacheScope().equals(MimeResponse.PRIVATE_SCOPE)) {
            cacheKey += "_" + jParams.getUser().getUserKey();
        }
        // Try to flush the entry in cache
        cacheInstance.getContainerHTMLCacheInstance().remove(cacheKeyGeneratorService.computeContainerEntryKey(
                null, cacheKey, processingContext.getUser(),
                processingContext.getLocale().toString(),
                processingContext.getOperationMode(),
                processingContext.getScheme()));
    }

    public void setCacheInstance(CacheService cacheInstance) {
        this.cacheInstance = cacheInstance;
    }

    public void setCacheKeyGeneratorService(CacheKeyGeneratorService cacheKeyGeneratorService) {
        this.cacheKeyGeneratorService = cacheKeyGeneratorService;
    }

    private void setPublicRenderParameter(PortletContainer container, HttpServletRequest request, PortalURL portalURL, String portletID)throws ServletException, PortletContainerException {
		String applicationId = PortletWindowConfig.parseContextPath(portletID);
		String portletName = PortletWindowConfig.parsePortletName(portletID);
		PortletDD portletDD = container.getOptionalContainerServices().getPortletRegistryService()
								.getPortletDescriptor(applicationId, portletName);
		Iterator<String> parameterNames = new EnumerationIterator(request.getParameterNames());
		if (parameterNames != null){
			while(parameterNames.hasNext()){
				String parameterName = parameterNames.next();
				if (portletDD.getPublicRenderParameter() != null){
					if (portletDD.getPublicRenderParameter().contains(parameterName)){
						String value = request.getParameter(parameterName);
						portalURL.addPublicParameterActionResourceParameter(parameterName, value);
					}
				}
			}
		}
    }
    
    private void copyAttribute(String attributeName, ProcessingContext processingContext, HttpServletRequest portalRequest, PortletWindow window) {
        Object objectToCopy = processingContext.getAttribute(attributeName);
        if (objectToCopy != null) {
            portalRequest.setAttribute("Pluto_" + window.getId().getStringId() + "_" + attributeName, objectToCopy);
        }

    }

}
