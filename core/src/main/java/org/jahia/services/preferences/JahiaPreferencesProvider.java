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

package org.jahia.services.preferences;

import org.jahia.params.ProcessingContext;
import org.jahia.services.preferences.exception.*;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * User: jahia
 * Date: 19 mars 2008
 * Time: 11:44:07
 */
public interface JahiaPreferencesProvider {

    /**
     * Get the type of the provider. Each provider has a unique type.
     *
     * @return
     */
    abstract public String getType();


    /**
     * Create a Preference key object for the current user and with the attributes.
     * The attributes depend on the JahiaPreferencesProvider implementations. (ie.: preference_name, pid, ... )
     * The implementation depends on the provider.
     *
     * @param processingContext
     * @param attributes
     * @throws JahiaPreferenceNotDefinedAttributeException
     *
     */
    abstract public JahiaPreferenceKey createJahiaPreferenceKey(ProcessingContext processingContext, Map<String, String> attributes) throws JahiaPreferenceNotDefinedAttributeException;


    /**
     * Create a jahia prefrence key object. The implementation depends on the provider
     *
     * @param principal
     * @param attributes
     * @return
     * @throws JahiaPreferenceNotDefinedAttributeException
     *
     */
    abstract public JahiaPreferenceKey createJahiaPreferenceKey(Principal principal, Map<String, String> attributes) throws JahiaPreferenceNotDefinedAttributeException;


    /**
     * Create a jahia preference key  with principal = ${current_jahiaUser}
     * The implementation depends on the provider.
     *
     * @param processingContext
     * @return
     */
    abstract public JahiaPreferenceKey createPartialJahiaPreferenceKey(ProcessingContext processingContext);


    /**
     * Create a jahia peference key with "empty" properties
     *
     * @param principal
     * @return
     */
    abstract public JahiaPreferenceKey createPartialJahiaPreferenceKey(Principal principal);


    /**
     * Create an empty JahiaPreference object. The implementation depends on the provider
     *
     * @return a JahiaPreference object that depends on the provider implementation
     */
    abstract public JahiaPreferenceValue createEmptyJahiaPreferenceValue();


    /**
     * @param jahiaPreferenceValue
     * @return true if the preference value is correct
     * @throws JahiaPreferencesNotValidException
     *
     */
    abstract public boolean validate(JahiaPreferenceValue jahiaPreferenceValue) throws JahiaPreferencesNotValidException;

    /**
     * @param date of the backup to loaded
     * @throws JahiaPreferencesVersionningException
     *
     */
    abstract public void restore(Date date) throws JahiaPreferencesVersionningException;

    /**
     * @param date of the backup
     * @throws JahiaPreferencesVersionningException
     *
     */
    abstract public void save(Date date) throws JahiaPreferencesVersionningException;

    /**
     * Get jahia preference by a jahia preference key attributes. A jahia preference key is dynamically created.
     *
     * @param jahiaPreferenceKeyAttributes
     * @return
     * @throws JahiaPreferenceNotDefinedAttributeException
     *          if the jahia preference key attributes map contains a un-valid attributes.
     */
    abstract public JahiaPreference getJahiaPreference(Map<String, String> jahiaPreferenceKeyAttributes) throws JahiaPreferenceNotDefinedAttributeException;

    /**
     * Get jahia preference by a jahia preference key.
     *
     * @param jahiaPreferenceKey
     * @return
     */
    abstract public JahiaPreference getJahiaPreference(JahiaPreferenceKey jahiaPreferenceKey);

     /**
     * Get jahia preference by a jahia preference value.
     *
     * @param jahiaPreferenceKey
     * @return
     */
    abstract public JahiaPreferenceValue getJahiaPreferenceValue(JahiaPreferenceKey jahiaPreferenceKey);

    /**
     * Get all preferences of a principal
     * WARNING: if there is lots of preferences, it can be time consuming.
     *
     * @param principal
     * @return
     */
    abstract public List<JahiaPreference> getJahiaAllPreferences(Principal principal);

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
    abstract public List<JahiaPreference> getJahiaPreferencesByPartialKey(JahiaPreferenceKey jahiaPreferenceKey, List<String> revelantProperties);


    /**
     * Get jahia preferences of by principal_key and principal_type.
     *
     * @param jahiaPreferenceKey
     * @return map <key,preference> that contains all preferences of the principal.
     */
    abstract public Map<JahiaPreferenceKey, JahiaPreference> getJahiaPreferences(JahiaPreferenceKey jahiaPreferenceKey);


    /**
     * Get jahia preferences of the current user
     *
     * @param processingContext
     * @return map <key,preference> that contains all preferences of the principal.
     */
    abstract public Map<JahiaPreferenceKey, JahiaPreference> getJahiaPreferences(ProcessingContext processingContext);

    /**
     * Delete a jahia preference that has a key created from jahiaPreferenceKeyAttributes.
     *
     * @param jahiaPreferenceKeyAttributes
     * @throws JahiaPreferenceNotDefinedAttributeException
     *
     */
    abstract public void deleteJahiaPreference(Map<String, String> jahiaPreferenceKeyAttributes) throws JahiaPreferenceNotDefinedAttributeException;


    /**
     * Delete a jahia preference that has jahiaPreferenceKey as key.
     *
     * @param jahiaPreferenceKey
     */
    abstract public void deleteJahiaPreference(JahiaPreferenceKey jahiaPreferenceKey);

    /**
     * Set a jahia preference value.
     *
     * @param jahiaPreferenceKey
     * @param jahiaPreferenceValue
     * @throws JahiaPreferencesNotValidException
     *
     */
    abstract public void setJahiaPreference(JahiaPreferenceKey jahiaPreferenceKey, JahiaPreferenceValue jahiaPreferenceValue);


    /**
     * Set a jahia preference.
     *
     * @param jahiaPreference
     * @throws JahiaPreferencesNotValidException
     *
     */
    abstract public void setJahiaPreference(JahiaPreference jahiaPreference);

    /**
     * Set a jahia preference value. The jahia preference key object is created dynamically from the corresponding map.
     *
     * @param jahiaPreferenceKeyAttributes
     * @param jahiaPreferenceProperties
     * @throws JahiaPreferenceNotDefinedAttributeException
     *
     * @throws JahiaPreferenceNotDefinedPropertyException
     *
     * @throws JahiaPreferencesNotValidException
     *
     */
    abstract public void setJahiaPreferenceByMaps(Map<String, String> jahiaPreferenceKeyAttributes, Map<String, String> jahiaPreferenceProperties) throws JahiaPreferenceNotDefinedAttributeException, JahiaPreferenceNotDefinedPropertyException, JahiaPreferencesNotValidException;

    /**
     * Set a jahia preference value. The jahia preverence value object is created dynamically from the corresponding map.
     *
     * @param jahiaPreferenceKey
     * @param jahiaPreferenceProperties
     * @throws JahiaPreferenceNotDefinedPropertyException
     *
     * @throws JahiaPreferencesNotValidException
     *
     */
    abstract public void setJahiaPreferenceByValuesMap(JahiaPreferenceKey jahiaPreferenceKey, Map<String, String> jahiaPreferenceProperties) throws JahiaPreferenceNotDefinedPropertyException, JahiaPreferencesNotValidException;

    /**
     * Set a jahia preference value. The jahia preverence value object and the jahia preference key object are created dynamically from the corresponding map.
     *
     * @param jahiaPreferenceKeyAttributes
     * @param jahiaPreferenceValue
     * @throws JahiaPreferenceNotDefinedAttributeException
     *
     * @throws JahiaPreferencesNotValidException
     *
     */
    abstract public void setJahiaPreference(Map<String, String> jahiaPreferenceKeyAttributes, JahiaPreferenceValue jahiaPreferenceValue) throws JahiaPreferenceNotDefinedAttributeException, JahiaPreferencesNotValidException;

    /**
     * Delete all preferences of principal. All others attributes of the jahia preference key are ignored.
     *
     * @param principal
     */
    abstract public void deleteAllPreferencesByPrincipal(Principal principal);

    /**
     * Import preferences
     *
     * @param xmlDocument
     * @throws JahiaPreferencesException
     */
    abstract public void importPreferences(String xmlDocument) throws JahiaPreferencesException;

    /**
     * @return A string representing an XML document
     * @throws JahiaPreferencesException
     */
    abstract public String exportPreferences() throws JahiaPreferencesException;
}
