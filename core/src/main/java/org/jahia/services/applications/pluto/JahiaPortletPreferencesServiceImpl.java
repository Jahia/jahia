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
                JahiaPortletPreference curPortletPreference = (JahiaPortletPreference) currentPreference.getNode();
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
                JahiaPreference portletPreference = portletPreferenceProvider.getJahiaPreference(request.getUserPrincipal(), JahiaPreferencesXpathHelper.getPortletXpath(portletName, curPlutoPreference.getName()));
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
