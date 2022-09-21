/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.data.applications;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.portlet.PortletMode;

import org.apache.pluto.container.om.portlet.InitParam;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.apache.pluto.container.om.portlet.Supports;
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

    private static final long serialVersionUID = 687873504629004699L;

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
