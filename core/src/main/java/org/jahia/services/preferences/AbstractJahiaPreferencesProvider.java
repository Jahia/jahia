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
import org.jahia.services.preferences.exception.JahiaPreferenceNotDefinedAttributeException;
import org.jahia.services.preferences.exception.JahiaPreferenceNotDefinedPropertyException;
import org.jahia.services.preferences.exception.JahiaPreferencesNotValidException;
import org.jahia.services.preferences.exception.JahiaPreferencesVersionningException;

import java.util.Date;
import java.util.Map;
import java.security.Principal;

/**
 * User: jahia
 * Date: 19 mars 2008
 * Time: 12:33:39
 */
public abstract class AbstractJahiaPreferencesProvider implements JahiaPreferencesProvider {

    /**
     * Create an empty JahiaPreferenceKey object. The implementation depends on the provider
     *
     * @return
     */
    abstract public JahiaPreferenceKey createEmptyJahiaPreferenceKey();

    /**
     * Create an empty JahiaPreference object. The implementation depends on the provider
     *
     * @return a JahiaPreference object that depends on the provider implementation
     */
    abstract public JahiaPreferenceValue createEmptyJahiaPreferenceValue();

    /**
     * Create a JahiaPreferenceKey object with {principal_type = user,principal_key = current_user_key}
     *
     * @param processingContext
     * @return
     */
    public JahiaPreferenceKey createPartialJahiaPreferenceKey(ProcessingContext processingContext) {
        return createPartialJahiaPreferenceKey(processingContext.getTheUser());
    }

    /**
     * Create a jahia peference key with "empty" properties
     *
     * @param principal
     * @return
     */
    public JahiaPreferenceKey createPartialJahiaPreferenceKey(Principal principal) {
        JahiaPreferenceKey key = createEmptyJahiaPreferenceKey();
        key.setPrincipal(principal);
        return key;
    }

    /**
     * Restore preferences
     *
     * @param date
     * @throws JahiaPreferencesVersionningException
     *
     */
    public void restore(Date date) throws JahiaPreferencesVersionningException {
        throw new JahiaPreferencesVersionningException();
    }

    /**
     * Save a version of all preferences
     *
     * @param date
     * @throws JahiaPreferencesVersionningException
     *
     */
    public void save(Date date) throws JahiaPreferencesVersionningException {
        throw new JahiaPreferencesVersionningException();
    }


    /**
     * Set preferences only if validated
     *
     * @param jahiaPreferenceKey
     * @param jahiaPreferenceValue
     * @throws JahiaPreferencesNotValidException
     *
     */
    public void validateThenSetJahiaPreference(JahiaPreferenceKey jahiaPreferenceKey, JahiaPreferenceValue jahiaPreferenceValue) throws JahiaPreferencesNotValidException {
        if (validate(jahiaPreferenceValue)) {
            setJahiaPreference(jahiaPreferenceKey, jahiaPreferenceValue);
        }
    }

    /**
     * Create a JahiaPreferenceKey with principal = ${current_user}
     *
     * @param processingContext
     * @param properties
     * @return
     * @throws JahiaPreferenceNotDefinedAttributeException
     *
     */
    public JahiaPreferenceKey createJahiaPreferenceKey(ProcessingContext processingContext, Map<String, String> properties) throws JahiaPreferenceNotDefinedAttributeException {
        return createJahiaPreferenceKey(processingContext.getTheUser(), properties);
    }

    /**
     * Create a jahia prefrence key object. The implementation depends on the provider
     *
     * @param principal
     * @param properties
     * @return
     * @throws org.jahia.services.preferences.exception.JahiaPreferenceNotDefinedAttributeException
     *
     */
    public JahiaPreferenceKey createJahiaPreferenceKey(Principal principal, Map<String, String> properties) throws JahiaPreferenceNotDefinedAttributeException {
        JahiaPreferenceKey key = createPartialJahiaPreferenceKey(principal);
        key.setProperties(properties);
        return key;
    }


    /**
     * Get a jahiaPreference
     *
     * @param jahiaPreferenceKeyAttributes
     * @return
     * @throws JahiaPreferenceNotDefinedAttributeException
     *
     */
    public JahiaPreference getJahiaPreference(Map<String, String> jahiaPreferenceKeyAttributes) throws JahiaPreferenceNotDefinedAttributeException {
        JahiaPreferenceKey key = createEmptyJahiaPreferenceKey();
        key.setProperties(jahiaPreferenceKeyAttributes);
        return getJahiaPreference(key);
    }

     /**
     * Get jahia preference by a jahia preference value.
     *
     * @param jahiaPreferenceKey
     * @return
     */
    public JahiaPreferenceValue getJahiaPreferenceValue(JahiaPreferenceKey jahiaPreferenceKey) {
        JahiaPreference jahiaPreference = getJahiaPreference(jahiaPreferenceKey);
        if (jahiaPreference == null) {
            return null;
        }
        return jahiaPreference.getValue();
    }


    /**
     * Get all preferences of the current user for the current provider
     *
     * @param processingContext
     * @return
     */
    public Map<JahiaPreferenceKey, JahiaPreference> getJahiaPreferences(ProcessingContext processingContext) {
        JahiaPreferenceKey key = createPartialJahiaPreferenceKey(processingContext);
        return getJahiaPreferences(key);
    }


    /**
     * Delete preference by map attributes key
     *
     * @param jahiaPreferenceKeyProperties
     * @throws JahiaPreferenceNotDefinedAttributeException
     *
     */
    public void deleteJahiaPreference(Map<String, String> jahiaPreferenceKeyProperties) throws JahiaPreferenceNotDefinedAttributeException {
        JahiaPreferenceKey key = createEmptyJahiaPreferenceKey();
        key.setProperties(jahiaPreferenceKeyProperties);
        deleteJahiaPreference(key);
    }

    /**
     * Delete jahia preference by its key
     *
     * @param jahiaPreferenceKey
     */
    public void deleteJahiaPreference(JahiaPreferenceKey jahiaPreferenceKey) {
        setJahiaPreference(jahiaPreferenceKey, null);
    }


    /**
     * add or update jahia preference
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
    public void setJahiaPreferenceByMaps(Map<String, String> jahiaPreferenceKeyAttributes, Map<String, String> jahiaPreferenceProperties) throws JahiaPreferenceNotDefinedAttributeException, JahiaPreferenceNotDefinedPropertyException, JahiaPreferencesNotValidException {
        JahiaPreferenceKey key = createEmptyJahiaPreferenceKey();
        key.setProperties(jahiaPreferenceKeyAttributes);

        JahiaPreferenceValue value = createEmptyJahiaPreferenceValue();
        value.setProperties(jahiaPreferenceProperties);

        validateThenSetJahiaPreference(key, value);
    }

    /**
     * add or update jahia preference
     *
     * @param jahiaPreferenceKeyAttributes
     * @param jahiaPreferenceValue
     * @throws JahiaPreferenceNotDefinedAttributeException
     *
     * @throws JahiaPreferencesNotValidException
     *
     */
    public void setJahiaPreference(Map<String, String> jahiaPreferenceKeyAttributes, JahiaPreferenceValue jahiaPreferenceValue) throws JahiaPreferenceNotDefinedAttributeException, JahiaPreferencesNotValidException {
        JahiaPreferenceKey key = createEmptyJahiaPreferenceKey();
        validateThenSetJahiaPreference(key, jahiaPreferenceValue);
    }

    /**
     * Set a jahia preference.
     *
     * @param jahiaPreference
     * @throws org.jahia.services.preferences.exception.JahiaPreferencesNotValidException
     *
     */
    public void setJahiaPreference(JahiaPreference jahiaPreference) {
        if (jahiaPreference != null) {
            setJahiaPreference(jahiaPreference.getKey(), jahiaPreference.getValue());
        }
    }

    /**
     * add or update jahia preference
     *
     * @param jahiaPreferenceKey
     * @param jahiaPreferenceProperties
     * @throws JahiaPreferenceNotDefinedPropertyException
     *
     * @throws JahiaPreferencesNotValidException
     *
     */
    public void setJahiaPreferenceByValuesMap(JahiaPreferenceKey jahiaPreferenceKey, Map<String, String> jahiaPreferenceProperties) throws JahiaPreferenceNotDefinedPropertyException, JahiaPreferencesNotValidException {
        JahiaPreferenceValue value = createEmptyJahiaPreferenceValue();
        value.setProperties(jahiaPreferenceProperties);
        setJahiaPreference(jahiaPreferenceKey, value);
    }
}
