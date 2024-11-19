/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.data.beans.portlets;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.container.PortletWindow;
import org.jahia.data.applications.EntryPointDefinition;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.services.usermanager.JahiaUser;

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
    private JahiaUser jahiaUser;
    private HttpServletRequest httpServletRequest;
    private EntryPointDefinition entryPointDefinition;
    private PortletWindow portletWindow;

    public PortletWindowBean() {
    }

    public PortletWindowBean(JahiaUser jahiaUser, HttpServletRequest httpServletRequest, PortletWindow portletWindow) {
        this.jahiaUser = jahiaUser;
        this.httpServletRequest = httpServletRequest;
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

    /**
     * Get list of supported portlet modes (view,edit,help)
     * @return
     */
    public List<PortletModeBean> getPortletModeBeans(String workspaceName) {
        List<PortletModeBean> portletModeBeans = new ArrayList<PortletModeBean>();
        if (entryPointDefinition != null) {
            for (PortletMode curPortletMode : entryPointDefinition.getPortletModes()) {
                String modeName = curPortletMode.toString();
                if (modeName != null && entryPointInstance.isModeAllowed(jahiaUser, modeName, workspaceName)) {
                    PortletModeBean curPortletModeBean = new PortletModeBean(httpServletRequest, this);
                    curPortletModeBean.setName(modeName);
                    portletModeBeans.add(curPortletModeBean);
                }
            }
        }
        return portletModeBeans;
    }

    /**
     * Get supported window state (minimized, normal and maximized)
     * @return
     */
    public List<WindowStateBean> getWindowStateBeans() {
        List<WindowStateBean> windowStateBeans = new ArrayList<WindowStateBean>();
        if (entryPointDefinition != null) {
            for (WindowState curWindowState : entryPointDefinition.getWindowStates()) {
                WindowStateBean curWindowStateBean = new WindowStateBean(httpServletRequest,
                        this);
                curWindowStateBean.setName(curWindowState.toString());
                windowStateBeans.add(curWindowStateBean);
            }
        }
        return windowStateBeans;
    }

    /**
     * Get current portlet mode (view, edit or help)
     * @return
     */
    public PortletModeBean getCurrentPortletModeBean() {
        PortletModeBean portletModeBean = new PortletModeBean(httpServletRequest, this);
        portletModeBean.setName(portletWindow.getPortletMode().toString());
        return portletModeBean;
    }

    /**
     * Get current window state (minimized, normal, maximized)
     * @return
     */
    public WindowStateBean getCurrentWindowStateBean() {
        WindowStateBean currentWindowStateBean = new WindowStateBean(httpServletRequest, this);
        currentWindowStateBean.setName(portletWindow.getWindowState().toString());
        return currentWindowStateBean;
    }

    /**
     * Get the entryPointInstance
     * @return
     */
    public EntryPointInstance getEntryPointInstance() {
        return entryPointInstance;
    }

    /**
     * Set the entrypointInstance
     * @param entryPointInstance
     */
    public void setEntryPointInstance(EntryPointInstance entryPointInstance) {
        this.entryPointInstance = entryPointInstance;
    }

    /**
     * Get the entrypointDefinition
     * @return
     */
    public EntryPointDefinition getEntryPointDefinition() {
        return entryPointDefinition;
    }

    /**
     * Set the entryPointDefinitin
     * @param entryPointDefinition
     */
    public void setEntryPointDefinition(EntryPointDefinition entryPointDefinition) {
        this.entryPointDefinition = entryPointDefinition;
    }


    /**
     * Get the portlet window object
     * @return
     */
    public PortletWindow getPortletWindow() {
        return portletWindow;
    }

}
