package org.jahia.services.render.filter.portlet;

import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletContainerException;
import org.apache.pluto.driver.AttributeKeys;
import org.apache.pluto.driver.core.PortalRequestContext;
import org.apache.pluto.driver.core.PortletWindowImpl;
import org.apache.pluto.driver.services.portal.PortletWindowConfig;
import org.apache.pluto.driver.url.PortalURL;
import org.jahia.bin.Jahia;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ParamBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.applications.pluto.JahiaPortletUtil;
import org.jahia.services.applications.pluto.JahiaUserRequestWrapper;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.sites.JahiaSitesBaseService;

import javax.portlet.MimeResponse;
import javax.portlet.PortletException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: ktlili
 * Date: Dec 21, 2009
 * Time: 3:37:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlutoProcessActionFilter extends AbstractFilter {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PlutoProcessActionFilter.class);

    @Override
    protected String execute(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
        try {
            final ParamBean jParams = (ParamBean) Jahia.getThreadParamBean();
            final JahiaUserRequestWrapper request = new JahiaUserRequestWrapper(renderContext.getUser(), renderContext.getRequest());
            final HttpServletResponse response = renderContext.getResponse();
            final ServletContext servletContext = jParams.getContext();
            final PortletContainer container = (PortletContainer) servletContext.getAttribute(AttributeKeys.PORTLET_CONTAINER);
            final PortalRequestContext portalRequestContext = new PortalRequestContext(servletContext, request, response);
            final PortalURL portalURL = portalRequestContext.getRequestedPortalURL();
            final String actionWindowId = portalURL.getActionWindow();
            final String resourceWindowId = portalURL.getResourceWindow();

            PortletWindowConfig actionWindowConfig = null;
            PortletWindowConfig resourceWindowConfig = null;

            if (resourceWindowId != null) {
                resourceWindowConfig = PortletWindowConfig.fromId(resourceWindowId);
            } else if (actionWindowId != null) {
                actionWindowConfig = PortletWindowConfig.fromId(actionWindowId);
            }

            // Action window config will only exist if there is an action request.
            if (actionWindowConfig != null) {
                flushPortletCache(jParams, actionWindowConfig);
                PortletWindowImpl portletWindow = new PortletWindowImpl(container, actionWindowConfig, portalURL);
                //if (logger.isDebugEnabled()) {
                logger.debug("Processing action request for window: "
                        + portletWindow.getId().getStringId());
                //}

                EntryPointInstance entryPointInstance = ServicesRegistry.getInstance().getApplicationsManagerService().getEntryPointInstance(actionWindowConfig.getMetaInfo());
                if (entryPointInstance != null) {
                    request.setEntryPointInstance(entryPointInstance);
                } else {
                    logger.warn("Couldn't find related entryPointInstance, roles might not work properly !");
                }

                // copy jahia attibutes nested by the portlet
                JahiaPortletUtil.copyJahiaAttributes(entryPointInstance, jParams, portletWindow, request, true);


                try {
                    container.doAction(portletWindow, request, jParams.getResponse());
                    JahiaPortletUtil.copySharedMapFromPortletToJahia(jParams, request, portletWindow);
                } catch (PortletContainerException ex) {
                    throw new ServletException(ex);
                } catch (PortletException ex) {
                    throw new ServletException(ex);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Action request processed, send a redirect.\n\n");

                }

                return "";
            }
            //Resource request
            else if (resourceWindowConfig != null) {
                PortletWindowImpl portletWindow = new PortletWindowImpl(container,
                        resourceWindowConfig, portalURL);
                if (logger.isDebugEnabled()) {
                    logger.debug("Processing resource Serving request for window: " + portletWindow.getId().getStringId());
                }
                try {
                    container.doServeResource(portletWindow, request, response);
                } catch (PortletContainerException ex) {
                    logger.error(ex.getMessage(), ex);
                    throw new ServletException(ex);
                } catch (PortletException ex) {
                    logger.error(ex.getMessage(), ex);
                    throw new ServletException(ex);
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Resource serving request processed.\n\n");
                }
                return "";
            }
        } catch (Exception t) {
            logger.error("Error while processing action", t);
        }
        logger.debug(renderContext.getRequest().getRequestURI() + " is a renderURL");
        return chain.doFilter(renderContext, resource);


    }

    /**
     * Flush the portlet Cache
     *
     * @param jParams
     * @param actionWindowConfig
     * @throws org.jahia.exceptions.JahiaException
     *
     */
    private void flushPortletCache(ParamBean jParams, PortletWindowConfig actionWindowConfig) throws JahiaException {
        String cacheKey = null;
        // Check if cache is available for this portlet
        cacheKey = "portlet_instance_" + actionWindowConfig.getMetaInfo();
        final EntryPointInstance entryPointInstance = ServicesRegistry.getInstance().getApplicationsManagerService().getEntryPointInstance(actionWindowConfig.getMetaInfo());
        if (entryPointInstance != null && entryPointInstance.getCacheScope() != null && entryPointInstance.getCacheScope().equals(MimeResponse.PRIVATE_SCOPE)) {
            cacheKey += "_" + jParams.getUser().getUserKey();
        }
    }
}
