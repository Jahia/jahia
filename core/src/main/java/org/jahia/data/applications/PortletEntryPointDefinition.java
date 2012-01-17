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

package org.jahia.data.applications;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.portlet.PortletMode;

import org.apache.pluto.container.om.portlet.impl.PortletType;
import org.apache.pluto.container.om.portlet.Supports;
import org.apache.pluto.container.om.portlet.InitParam;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.jahia.registries.ServicesRegistry;

/**
 * <p>Title: Portlet API Entry point definition</p>
 * <p>Description: This class is basically a wrapper around a Portlet API
 * compliant PortletDefinition. This allows us to treat the PortletDefinition
 * as one of our internal definition object, while also protecting us against
 * future API changes.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 */
public class PortletEntryPointDefinition implements Serializable, EntryPointDefinition {

    /**
     * logging
     */
    private static org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger(PortletEntryPointDefinition.class);

    private String applicationID;
    private String context;
    private PortletDefinition portletDefinition;
    private List<PortletMode> portletModes = null;

    public PortletEntryPointDefinition(String applicationID, String context, PortletDefinition portletDefinition) {
        this.applicationID = applicationID;
        this.context = context;
        this.portletDefinition = portletDefinition;
    }

    /**
     * Get the context
     *
     * @return
     */
    public String getContext() {
        return context;
    }

    /**
     * Get the portlet name
     *
     * @return
     */
    public String getName() {
        return portletDefinition.getPortletName();
    }

    /**
     * Get display name.
     *
     * @param locale
     * @return
     */
    public String getDisplayName(java.util.Locale locale) {
        org.apache.pluto.container.om.portlet.DisplayName displayName = portletDefinition.getDisplayName(locale);
        if (displayName == null || displayName.getDisplayName() == null || displayName.getDisplayName().length() == 0) {
            return getName();
        }
        return displayName.getDisplayName();
    }


    /**
     * Get description depending on locale
     *
     * @param locale
     * @return
     */
    public String getDescription(java.util.Locale locale) {
        org.apache.pluto.container.om.portlet.Description description = portletDefinition.getDescription(locale);
        if (description != null) {
            return description.getDescription();
        }
        return null;
    }

    /**
     * Get the application id
     *
     * @return
     */
    public String getApplicationID() {
        return applicationID;
    }

    /**
     * Get list of portlet modes supported by the portlet
     *
     * @return
     */
    public List<PortletMode> getPortletModes() {
        if (portletModes == null) {

            java.util.List<? extends Supports> supportsList = portletDefinition.getSupports();
            List<String> definitionPortletModes = null;
            for (Supports currentSupport : supportsList) {
                if ("text/html".equals(currentSupport.getMimeType())) {
                    definitionPortletModes = currentSupport.getPortletModes();
                }
            }

            if (definitionPortletModes == null) {
                logger.error("Couldn't find portlet mode definition for portlet" + portletDefinition.getPortletName() + " returning empty list !");
                return new ArrayList<PortletMode>();
            }

            // we must lowercase everything, since we will compare it with values that *should* also be always
            // lowercase.
            List<String> lowercaseDefinitionPortletModes = new ArrayList<String>();
            for (String curPortletMode : definitionPortletModes) {
                lowercaseDefinitionPortletModes.add(curPortletMode.toLowerCase());
            }

            // returns only mode that are activated by the portlet and supported by the portal
            List<PortletMode> portalPortletModes = ServicesRegistry.getInstance().getApplicationsManagerService().getSupportedPortletModes();
            List<PortletMode> resultPortletModes = new ArrayList<PortletMode>();
            Iterator portalPortletModesIter = portalPortletModes.iterator();
            while (portalPortletModesIter.hasNext()) {
                PortletMode curPortletMode = (PortletMode)portalPortletModesIter. next();
                if (lowercaseDefinitionPortletModes.contains(curPortletMode.toString().toLowerCase())) {
                    resultPortletModes.add(curPortletMode);
                }
            }
            portletModes = resultPortletModes;
            return resultPortletModes;
        } else {
            return portletModes;
        }
    }

    /**
     * Get List of window states
     *
     * @return
     */
    public List getWindowStates() {
        return ServicesRegistry.getInstance().getApplicationsManagerService().getSupportedWindowStates();
    }

    /**
     * Get portlet defintion
     *
     * @return
     */
    public PortletDefinition getPortletDefinition() {
        return portletDefinition;
    }

    /**
     * Get init parameter
     *
     * @param param
     * @return
     */
    public String getInitParameter(String param) {
        if (portletDefinition.getInitParams() != null) {
            for (InitParam initParam : portletDefinition.getInitParams()) {
                if (param.equals(initParam.getParamName())) {
                    return initParam.getParamValue();
                }
            }
        }
        return null;
    }

    /**
     * Get cache scope
     *
     * @return
     */
    public String getCacheScope() {
        return portletDefinition.getCacheScope();
    }

    /**
     * Get expiration cache
     *
     * @return
     */
    public int getExpirationCache() {
        return portletDefinition.getExpirationCache();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PortletEntryPointDefinition that = (PortletEntryPointDefinition) o;

        return applicationID.equals(that.applicationID);

    }

    @Override
    public int hashCode() {
        return applicationID.hashCode();
    }
}
