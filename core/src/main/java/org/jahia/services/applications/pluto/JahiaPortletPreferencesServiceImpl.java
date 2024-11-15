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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.applications.pluto;
import org.apache.pluto.container.PortletPreference;
import org.apache.pluto.container.impl.PortletPreferenceImpl;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.PortletContainerException;
import org.apache.pluto.driver.container.DefaultPortletPreferencesService;
import org.jahia.services.preferences.JahiaPreferencesService;
import org.jahia.services.preferences.JahiaPreferencesProvider;
import org.jahia.services.preferences.JahiaPreference;
import org.jahia.services.preferences.JahiaPreferencesQueryHelper;
import org.jahia.services.preferences.exception.JahiaPreferenceProviderException;
import org.jahia.services.usermanager.JahiaUser;
import javax.portlet.PortletRequest;
import javax.jcr.RepositoryException;
import java.util.*;

/**
 *
 * User: loom
 * Date: Nov 18, 2008
 * Time: 2:24:14 PM
 */
public class JahiaPortletPreferencesServiceImpl extends DefaultPortletPreferencesService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(JahiaPortletPreferencesServiceImpl.class);

    private JahiaPreferencesService jahiaPreferencesService;

    public JahiaPreferencesService getJahiaPreferencesService() {
        return jahiaPreferencesService;
    }

    public void setJahiaPreferencesService(JahiaPreferencesService jahiaPreferencesService) {
        this.jahiaPreferencesService = jahiaPreferencesService;
    }


    /**
     * Returns the stored portlet preferences array. The preferences managed by
     * this service should be protected from being directly accessed, so this
     * method returns a cloned copy of the stored preferences.
     *
     * @param portletWindow the portlet window.
     * @param request       the portlet request from which the remote user is retrieved.
     * @return a copy of the stored portlet preferences array.
     * @throws PortletContainerException
     */
    public Map<String,PortletPreference> getStoredPreferences(PortletWindow portletWindow, PortletRequest request) throws PortletContainerException {
        try {
            JahiaPreferencesProvider portletPreferenceProvider = jahiaPreferencesService.getPreferencesProviderByType("portlet");
            JahiaUser jahiaUser = (JahiaUser) request.getUserPrincipal();
            String portletName = portletWindow.getPortletDefinition().getApplication().getContextPath() + "." + portletWindow.getPortletDefinition().getPortletName();
            List<JahiaPreference> foundPreferences = portletPreferenceProvider.findJahiaPreferences(jahiaUser, JahiaPreferencesQueryHelper.getPortletSQL(portletName));
            if (foundPreferences == null) {
                return  new HashMap<String,PortletPreference>();
            }
            Map<String,PortletPreference> portletPreferences = new HashMap<String,PortletPreference>();

            for (JahiaPreference currentPreference : foundPreferences) {
                JahiaPortletPreference curPortletPreference = (JahiaPortletPreference) currentPreference.getNode();
                PortletPreferenceImpl portletPreferenceImpl = new PortletPreferenceImpl(curPortletPreference.getPrefName(), curPortletPreference.getValues(), curPortletPreference.getReadOnly());
                portletPreferences.put(portletPreferenceImpl.getName(),portletPreferenceImpl);
            }
            return portletPreferences;
        } catch (JahiaPreferenceProviderException e) {
            logger.error("Error while retrieving portlet preferences", e);
        } catch (RepositoryException e) {
            logger.error("Error while retrieving portlet preferences", e);
        }
        return null;
    }

    /**
     * Stores the portlet preferences to the in-memory storage. This method
     * should be invoked after the portlet preferences are validated by the
     * preference validator (if defined).
     * <p>
     * The preferences managed by this service should be protected from being
     * directly accessed, so this method clones the passed-in preferences array
     * and saves it.
     * </p>
     *
     * @param portletWindow the portlet window
     * @param request       the portlet request from which the remote user is retrieved.
     * @param stringPortletPreferenceMap   the portlet preferences to store.
     * @throws PortletContainerException
     * @see javax.portlet.PortletPreferences#store()
     */
    public void store(PortletWindow portletWindow, PortletRequest request, Map<String,PortletPreference> stringPortletPreferenceMap) throws PortletContainerException {
        try {
            JahiaPreferencesProvider portletPreferenceProvider = jahiaPreferencesService.getPreferencesProviderByType("portlet");
            Collection<PortletPreference > preferences = stringPortletPreferenceMap.values();
            for (PortletPreference curPlutoPreference : preferences) {
                String portletName = portletWindow.getPortletDefinition().getApplication().getContextPath() + "." + portletWindow.getPortletDefinition().getPortletName();
                JahiaPreference portletPreference = portletPreferenceProvider.getJahiaPreference(request.getUserPrincipal(), JahiaPreferencesQueryHelper.getPortletSQL(portletName, curPlutoPreference.getName()));
                if (portletPreference == null) {
                    portletPreference = portletPreferenceProvider.createJahiaPreferenceNode(request.getUserPrincipal());
                    JahiaPortletPreference node = (JahiaPortletPreference) portletPreference.getNode();
                    node.setPortletName(portletName);
                    node.setPrefName(curPlutoPreference.getName());
                } else {
                    // if values == null then delete the corresponding preference
                    if (curPlutoPreference.getValues() == null) {
                        portletPreferenceProvider.deleteJahiaPreference(portletPreference);
                    }
                }
                // if values == null the pref is not saved
                if (curPlutoPreference.getValues() != null) {
                    JahiaPortletPreference node = (JahiaPortletPreference) portletPreference.getNode();
                    node.setReadOnly(curPlutoPreference.isReadOnly());
                    node.setValues(curPlutoPreference.getValues());
                    portletPreferenceProvider.setJahiaPreference(portletPreference);
                }
            }
        } catch (JahiaPreferenceProviderException e) {
            logger.error("Error while storing portlet preferences", e);
        } catch (RepositoryException e) {
            logger.error("Error while retrieving portlet preferences", e);
        }
    }
}
