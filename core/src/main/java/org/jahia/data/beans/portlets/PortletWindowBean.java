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
package org.jahia.data.beans.portlets;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.pluto.PortletWindow;
import org.jahia.data.applications.EntryPointDefinition;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.params.ProcessingContext;

/**
 * <p>Title: Bean that contains all information relative to a portlet
 * window.</p>
 * <p>Description: Used to build user interfaces for template developers when
 * using portlets</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class PortletWindowBean {
    private int ID;
    private EntryPointInstance entryPointInstance;
    private ProcessingContext processingContext;
    private org.jahia.data.applications.EntryPointDefinition
            entryPointDefinition;
    private PortletWindow portletWindow;

    public PortletWindowBean() {
    }

    public PortletWindowBean(ProcessingContext processingContext, PortletWindow portletWindow) {
        this.processingContext = processingContext;
        this.portletWindow = portletWindow;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getEntryPointInstanceID() {
        return entryPointInstance.getID();
    }

    public List<PortletModeBean> getPortletModeBeans() {
        List<PortletModeBean> portletModeBeans = new ArrayList<PortletModeBean>();
        if (entryPointDefinition != null) {
            for (PortletMode curPortletMode : entryPointDefinition.getPortletModes()) {
                String modeName = curPortletMode.toString();
                if (modeName != null && entryPointInstance.isModeAllowed(processingContext.getUser(), modeName)) {
                    PortletModeBean curPortletModeBean = new PortletModeBean(processingContext, this);
                    curPortletModeBean.setName(modeName);
                    portletModeBeans.add(curPortletModeBean);
                }
            }
        }
        return portletModeBeans;
    }

    public List<WindowStateBean> getWindowStateBeans() {
        List<WindowStateBean> windowStateBeans = new ArrayList<WindowStateBean>();
        if (entryPointDefinition != null) {
            for (WindowState curWindowState : entryPointDefinition.getWindowStates()) {
                WindowStateBean curWindowStateBean = new WindowStateBean(processingContext,
                        this);
                curWindowStateBean.setName(curWindowState.toString());
                windowStateBeans.add(curWindowStateBean);
            }
        }
        return windowStateBeans;
    }

    public PortletModeBean getCurrentPortletModeBean() {

        /*
        NavigationalStateComponent nav = (NavigationalStateComponent)Jetspeed.getComponentManager().getComponent(NavigationalStateComponent.class);
        RequestContextComponent contextComponent = null;
        RequestContext context = null;
        contextComponent = (RequestContextComponent)Jetspeed.getComponentManager().getComponent(RequestContextComponent.class);
        HttpServletRequest request = ((ParamBean) processingContext).getRequest();
        HttpServletResponse response = ((ParamBean) processingContext).getResponse();
        context = ApplicationsManagerJetspeedProvider.getRequestContext((ParamBean) processingContext,contextComponent);
        request.setAttribute(JahiaJ2SessionPortalURL.
                             PARAMBEAN_REQUEST_ATTRIBUTEKEY, processingContext);
        request.setAttribute(JahiaJ2SessionPortalURL.
                             APPUNIQUEID_REQUEST_ATTRIBUTEKEY,
                             getPortletWindow().getId() + "_" +
                             Integer.toString(getEntryPointInstanceID()));
        PortalURL portalURL = nav.createURL(request, response.getCharacterEncoding());
        MutableNavigationalState navState = (MutableNavigationalState) portalURL.getNavigationalState();
        if (navState != null && context!=null) {
            navState.sync(context);
            PortletMode portletMode = navState.getMode(getPortletWindow());
            request.removeAttribute(JahiaJ2SessionPortalURL.PARAMBEAN_REQUEST_ATTRIBUTEKEY);
            request.removeAttribute(JahiaJ2SessionPortalURL.APPUNIQUEID_REQUEST_ATTRIBUTEKEY);
            PortletModeBean currentPortletModeBean = new PortletModeBean(processingContext, this);
            currentPortletModeBean.setName(portletMode.toString());
            return currentPortletModeBean;
        }
        */
        PortletModeBean portletModeBean = new PortletModeBean(processingContext, this);
        portletModeBean.setName(portletWindow.getPortletMode().toString());
        return portletModeBean;
    }

    public WindowStateBean getCurrentWindowStateBean() {
        /*
        NavigationalStateComponent nav = (NavigationalStateComponent)Jetspeed.getComponentManager().getComponent(NavigationalStateComponent.class);
        RequestContextComponent contextComponent = null;
        RequestContext context = null;
        contextComponent = (RequestContextComponent)Jetspeed.getComponentManager().getComponent(RequestContextComponent.class);
        HttpServletRequest request = ((ParamBean) processingContext).getRequest();
        HttpServletResponse response = ((ParamBean) processingContext).getResponse();
        context = ApplicationsManagerJetspeedProvider.getRequestContext(processingContext,contextComponent);
        request.setAttribute(JahiaJ2SessionPortalURL.
                             PARAMBEAN_REQUEST_ATTRIBUTEKEY, processingContext);
        request.setAttribute(JahiaJ2SessionPortalURL.
                             APPUNIQUEID_REQUEST_ATTRIBUTEKEY,
                             getPortletWindow().getId() + "_" +
                             Integer.toString(getEntryPointInstanceID()));
        PortalURL portalURL = nav.createURL(request, response.getCharacterEncoding());
        MutableNavigationalState navState = (MutableNavigationalState) portalURL.getNavigationalState();
        WindowStateBean currentWindowStateBean = new WindowStateBean(processingContext, this);
        if (navState != null && context!=null) {
            navState.sync(context);
            WindowState windowState = navState.getState(getPortletWindow());
            request.removeAttribute(JahiaJ2SessionPortalURL.PARAMBEAN_REQUEST_ATTRIBUTEKEY);
            request.removeAttribute(JahiaJ2SessionPortalURL.APPUNIQUEID_REQUEST_ATTRIBUTEKEY);
            currentWindowStateBean.setName(windowState.toString());
        }
        */
        WindowStateBean currentWindowStateBean = new WindowStateBean(processingContext, this);
        currentWindowStateBean.setName(portletWindow.getWindowState().toString());
        return currentWindowStateBean;
    }

    public EntryPointInstance getEntryPointInstance() {
        return entryPointInstance;
    }

    public void setEntryPointInstance(EntryPointInstance entryPointInstance) {
        this.entryPointInstance = entryPointInstance;
    }

    public ProcessingContext getParamBean() {
        return processingContext;
    }

    public EntryPointDefinition getEntryPointDefinition() {
        return entryPointDefinition;
    }

    public void setEntryPointDefinition(EntryPointDefinition
            entryPointDefinition) {
        this.entryPointDefinition = entryPointDefinition;
    }

    public PortletWindow getPortletWindow() {
        return portletWindow;
    }

}
