/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.data.applications;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.portlet.PortletMode;

import org.apache.pluto.descriptors.common.InitParamDD;
import org.apache.pluto.descriptors.portlet.PortletDD;
import org.apache.pluto.descriptors.portlet.SupportsDD;
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
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(PortletEntryPointDefinition.class);

    private int applicationID;
    private String context;
    private PortletDD portletDefinition;
    private List<PortletMode> portletModes = null;

    public PortletEntryPointDefinition(int applicationID,
                                       String context,
                                       PortletDD portletDefinition) {
        this.applicationID = applicationID;
        this.context = context;
        this.portletDefinition = portletDefinition;
    }

    public String getContext() {
        return context;
    }

    public String getName() {
        return portletDefinition.getPortletName();
    }

    public String getDisplayName() {
        String displayName = portletDefinition.getDisplayName();
        if (displayName == null || displayName.length() == 0) {
            return getName();
        }
        return displayName;
    }

    public String getDescription() {
        return portletDefinition.getDescription();
    }

    public int getApplicationID() {
        return applicationID;
    }

    public List<PortletMode> getPortletModes() {
        if (portletModes == null) {

            List<SupportsDD> supportDDList = portletDefinition.getSupports();
            List<String> definitionPortletModes = null;
            for (SupportsDD currentSupportDD : supportDDList) {
                if ("text/html".equals(currentSupportDD.getMimeType())) {
                    definitionPortletModes = currentSupportDD.getPortletModes();
                }
            }

            if (definitionPortletModes == null) {
                logger.error("Couldn't find portlet mode definition for portlet" + portletDefinition.getDisplayName() + " returning empty list !");
                return new ArrayList<PortletMode>();
            }

            // we must lowercase everything, since we will compare it with values that *should* also be always
            // lowercase.
            List<String> lowercaseDefinitionPortletModes = new ArrayList<String>();
            for (String curPortletMode : definitionPortletModes) {
                lowercaseDefinitionPortletModes.add(curPortletMode.toLowerCase());
            }

            List portalPortletModes = ServicesRegistry.getInstance().
                    getApplicationsManagerService().
                    getSupportedPortletModes();
            List<PortletMode> resultPortletModes = new ArrayList<PortletMode>();
            Iterator portalPortletModesIter = portalPortletModes.iterator();
            while (portalPortletModesIter.hasNext()) {
                PortletMode curPortletMode = (PortletMode)
                        portalPortletModesIter.
                                next();
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

    public List getWindowStates() {
        return ServicesRegistry.getInstance().getApplicationsManagerService().
                getSupportedWindowStates();
    }

    public PortletDD getPortletDefinition() {
        return portletDefinition;
    }

    public String getInitParameter(String param) {
        if (portletDefinition.getInitParams() != null) {
            for (InitParamDD dd : portletDefinition.getInitParams()) {
                if (param.equals(dd.getParamName())) {
                    return dd.getParamValue();
                }
            }
        }
        return null;
    }

}
