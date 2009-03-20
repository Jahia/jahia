/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.applications.pluto;

import org.apache.pluto.spi.optional.PortletPreferencesService;
import org.apache.pluto.internal.InternalPortletPreference;
import org.apache.pluto.internal.impl.PortletPreferenceImpl;
import org.apache.pluto.PortletWindow;
import org.apache.pluto.PortletContainerException;
import org.jahia.services.preferences.JahiaPreferencesService;
import org.jahia.services.preferences.JahiaPreferencesProvider;
import org.jahia.services.preferences.JahiaPreference;
import org.jahia.services.preferences.JahiaPreferencesXpathHelper;
import org.jahia.services.preferences.exception.JahiaPreferenceProviderException;
import org.jahia.services.usermanager.JahiaUser;

import javax.portlet.PortletRequest;
import javax.jcr.RepositoryException;
import javax.jcr.PropertyIterator;
import javax.jcr.Property;
import javax.jcr.Value;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: loom
 * Date: Nov 18, 2008
 * Time: 2:24:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class JahiaPortletPreferencesServiceImpl implements PortletPreferencesService {

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(JahiaPortletPreferencesServiceImpl.class);

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
    public InternalPortletPreference[] getStoredPreferences(PortletWindow portletWindow, PortletRequest request) throws PortletContainerException {
        try {
            JahiaPreferencesProvider portletPreferenceProvider = jahiaPreferencesService.getPreferencesProviderByType("portlet");
            JahiaUser jahiaUser = (JahiaUser) request.getUserPrincipal();
            String portletName = portletWindow.getContextPath() + "." + portletWindow.getPortletName();
            List<JahiaPreference> foundPreferences = portletPreferenceProvider.findJahiaPreferences(jahiaUser, JahiaPreferencesXpathHelper.getPortletXpath(portletName));
            if (foundPreferences == null) {
                return new InternalPortletPreference[0];
            }
            List<InternalPortletPreference> portletPreferences = new ArrayList<InternalPortletPreference>();

            for (JahiaPreference currentPreference : foundPreferences) {
                JahiaPortletPreference curPortletPreference = (JahiaPortletPreference) currentPreference;
                PortletPreferenceImpl portletPreferenceImpl = new PortletPreferenceImpl(curPortletPreference.getPrefName(), curPortletPreference.getValues(), curPortletPreference.getReadOnly());
                portletPreferences.add(portletPreferenceImpl);
            }
            InternalPortletPreference[] portletPreferenceArray = portletPreferences.toArray(new InternalPortletPreference[portletPreferences.size()]);
            return portletPreferenceArray;
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
     * @param preferences   the portlet preferences to store.
     * @throws PortletContainerException
     * @see javax.portlet.PortletPreferences#store()
     */
    public void store(PortletWindow portletWindow, PortletRequest request, InternalPortletPreference[] preferences) throws PortletContainerException {
        try {
            JahiaPreferencesProvider portletPreferenceProvider = jahiaPreferencesService.getPreferencesProviderByType("portlet");
            for (InternalPortletPreference curPlutoPreference : preferences) {
                String portletName = portletWindow.getContextPath() + "." + portletWindow.getPortletName();
                JahiaPortletPreference portletPreference = (JahiaPortletPreference) portletPreferenceProvider.getJahiaPreference(request.getUserPrincipal(), JahiaPreferencesXpathHelper.getPortletXpath(portletName, curPlutoPreference.getName()));
                if (portletPreference == null) {
                    portletPreference = (JahiaPortletPreference) portletPreferenceProvider.createJahiaPreferenceNode(request.getUserPrincipal());
                    portletPreference.setPortletName(portletName);
                    portletPreference.setPrefName(curPlutoPreference.getName());
                } else {
                    // if values == null then delete the corresponding preference
                    if (curPlutoPreference.getValues() == null) {
                        portletPreferenceProvider.deleteJahiaPreference(portletPreference);
                    }
                }
                // if values == null the pref is not saved
                if (curPlutoPreference.getValues() != null) {
                    portletPreference.setReadOnly(curPlutoPreference.isReadOnly());
                    portletPreference.setValues(curPlutoPreference.getValues());
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
