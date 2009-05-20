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
