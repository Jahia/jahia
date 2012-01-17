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

package org.jahia.data.beans.portlets;

import java.util.ArrayList;
import java.util.List;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.container.PortletWindow;
import org.jahia.data.applications.EntryPointDefinition;
import org.jahia.data.applications.EntryPointInstance;
import org.jahia.params.ProcessingContext;
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
