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

package org.jahia.services.preferences.generic.provider;

import org.jahia.services.preferences.JahiaPreference;
import org.jahia.services.preferences.JahiaPreferenceKey;
import org.jahia.services.preferences.JahiaPreferenceValue;
import org.jahia.services.preferences.exception.JahiaPreferencesException;
import org.jahia.services.preferences.generic.GenericJahiaPreference;
import org.jahia.services.preferences.generic.GenericJahiaPreferenceKey;
import org.jahia.services.preferences.generic.GenericJahiaPreferenceValue;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.security.Principal;

/**
 * : jahia
 * Date: 27 mars 2008
 * Time: 16:26:01
 * TO DO: remove abstract and implement missing methods
 */
public class GenericJahiaPreferenceAPIProvider extends AbstractGenericJahiaPreferenceProvider {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(GenericJahiaPreferenceAPIProvider.class);
    private static final String PRINCIPAL_TYPE_PREFIX = "org.jahia.principal.type_";
    private static final String GENERIC_TYPE_PREFIX = "org.jahia.preferences.generic.type_";
    private static final String PRINCIPAL_KEY_PREFIX = "org.jahia.principal.key_";
    public static final String PROVIDER_TYPE = "org.jahia.preferences.provider.generic";
    private static final String VALUE = "org.jahia.value";
    private static final String NAME_PREFIX = "org.jahia.name_";

    /**
     * Get jahia preference by a jahia preference key.
     *
     * @param jahiaPreferenceKey
     * @return
     */
    public JahiaPreference getJahiaPreference(JahiaPreferenceKey jahiaPreferenceKey) {
        // get name node
        GenericJahiaPreferenceKey genericJahiaPreferenceKey = getAsGenericJahiaPreferenceKey(jahiaPreferenceKey);
        Preferences userPreferencesNode = Preferences.userNodeForPackage(getClass());
        String prefValuePath = NAME_PREFIX + genericJahiaPreferenceKey.getName();
        Preferences nameNode = userPreferencesNode.node(prefValuePath);

        //get value
        GenericJahiaPreferenceValue value = new GenericJahiaPreferenceValue();
        value.setValue(nameNode.get(VALUE, ""));

        // create the preference
        GenericJahiaPreference genericJahiaPreference = new GenericJahiaPreference();
        genericJahiaPreference.setGenericPreferenceKey(getAsGenericJahiaPreferenceKey(jahiaPreferenceKey));
        genericJahiaPreference.setValue(value);
        return genericJahiaPreference;
    }

    /**
     * Get jahia preference of by principal
     *
     * @param jahiaPreferenceKey
     * @return map <key,preference> that contains all preferences of the principal. Other properties of the key,specfifics to the jahiaPreferenceKey subclass, has to be ignored.
     */
    public Map<JahiaPreferenceKey, JahiaPreference> getJahiaPreferences(JahiaPreferenceKey jahiaPreferenceKey) {
        Map<JahiaPreferenceKey, JahiaPreference> preferencesMap = new HashMap();
        // Get the preferences node for this user and this package.
        Preferences userNode = getPreferenceTypeUserNode(getAsGenericJahiaPreferenceKey(jahiaPreferenceKey));

        try {
            String[] nameNodesName = userNode.childrenNames();
            for (String currentNameNodeName : nameNodesName) {
                // retrieve currentNameNodeName
                logger.debug("currentNameNodeName: " + currentNameNodeName);
                String name = currentNameNodeName.replaceAll(NAME_PREFIX, "");
                logger.debug("name: " + name);
                Preferences nameNode = userNode.node(currentNameNodeName);
                GenericJahiaPreferenceKey key = new GenericJahiaPreferenceKey(jahiaPreferenceKey);
                key.setName(name);

                String value = nameNode.get(VALUE, "");
                GenericJahiaPreferenceValue genericJahiaPreferenceValue = new GenericJahiaPreferenceValue();
                genericJahiaPreferenceValue.setValue(value);
                logger.debug("value: " + value);

                // create preference
                GenericJahiaPreference preference = new GenericJahiaPreference();
                preference.setKey(key);
                preference.setValue(genericJahiaPreferenceValue);

                // put into map
                preferencesMap.put(key, preference);

            }
        } catch (BackingStoreException e) {
            logger.error(e, e);
        }

        return preferencesMap;
    }

    /**
     * Get all preferences of a principal
     * WARNING: if there is lots of preferences, it can be time consuming.
     *
     * @param principal
     * @return
     */
    public List<JahiaPreference> getJahiaAllPreferences(Principal principal) {
        return null;
    }

    /**
     * Get a List of preferences depending on the revelant properties
     * Example:
     * JahiaPreferenceKey = {pid,wid}
     * getJahiaPreferencesByPartialKey(key,{wid}) --> get list of preferences with key.getWid() == wid ; pid properties is ignored.
     *
     * @param jahiaPreferenceKey
     * @param revelantProperties
     * @return
     */
    public List<JahiaPreference> getJahiaPreferencesByPartialKey(JahiaPreferenceKey jahiaPreferenceKey, List<String> revelantProperties) {
        return null;
    }

    /**
     * Set a jahia preference value.
     *
     * @param jahiaPreferenceKey
     * @param jahiaPreferenceValue
     * @throws org.jahia.services.preferences.exception.JahiaPreferencesNotValidException
     *
     */
    public void setJahiaPreference(JahiaPreferenceKey jahiaPreferenceKey, JahiaPreferenceValue jahiaPreferenceValue) {
        Preferences userNode = getPreferenceTypeUserNode(getAsGenericJahiaPreferenceKey(jahiaPreferenceKey));
        Preferences nameNode = userNode.node(NAME_PREFIX + getAsGenericJahiaPreferenceKey(jahiaPreferenceKey).getName());

        // store values
        GenericJahiaPreferenceValue value = getAsGenericJahiaPreferenceValue(jahiaPreferenceValue);
        if (value != null) {
            nameNode.put(VALUE, value.getValue());
        }
    }

    /**
     * Delete all preferences of principal. All others attributes of the jahia preference key are ignored.
     *
     * @param principal
     */
    public void deleteAllPreferencesByPrincipal(Principal principal) {
        JahiaPreferenceKey key = createPartialJahiaPreferenceKey(principal);
        Preferences userNode = getPreferenceTypeUserNode(getAsGenericJahiaPreferenceKey(key));
        try {
            userNode.clear();
        } catch (BackingStoreException e) {
            logger.error(e, e);
        }
    }

    /**
     * Import preferences
     *
     * @param xmlDocument
     * @throws org.jahia.services.preferences.exception.JahiaPreferencesException
     *
     */
    public void importPreferences(String xmlDocument) throws JahiaPreferencesException {

    }

    /**
     * @return A string representing an XML document
     * @throws org.jahia.services.preferences.exception.JahiaPreferencesException
     *
     */
    public String exportPreferences() throws JahiaPreferencesException {
        return null;
    }

    private Preferences getPreferenceTypeUserNode(GenericJahiaPreferenceKey genericJahiaPreferenceKey) {
        Preferences providerNode = getProviderNode();
        Preferences userPreferences = providerNode.node("/" + PRINCIPAL_TYPE_PREFIX + genericJahiaPreferenceKey.getPrincipalType() + "/" + PRINCIPAL_KEY_PREFIX + genericJahiaPreferenceKey.getPrincipalKey());
        return userPreferences;
    }

    private Preferences getProviderNode() {
        Preferences providerNode = Preferences.systemNodeForPackage(getClass());
        return providerNode;
    }

}
