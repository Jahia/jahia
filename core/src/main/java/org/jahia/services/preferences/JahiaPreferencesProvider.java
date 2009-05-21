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
package org.jahia.services.preferences;

import org.jahia.params.ProcessingContext;
import org.jahia.services.preferences.exception.*;
import org.jahia.services.content.JCRNodeWrapper;

import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * User: jahia
 * Date: 19 mars 2008
 * Time: 11:44:07
 */
public interface JahiaPreferencesProvider<T extends JCRNodeWrapper> {

    /**
     * Get the type of the provider. Each provider has a unique type.
     *
     * @return
     */
    abstract public String getType();


    /**
     * Create a JahiaPreference   with principal = ${current_jahiaUser}
     * The implementation depends on the provider.
     *
     * @param processingContext
     * @return
     */
    abstract public JahiaPreference<T> createJahiaPreferenceNode(ProcessingContext processingContext);


    /**
     * Create a JahiaPeference with "empty" properties
     *
     * @param principal
     * @return
     */
    abstract public JahiaPreference<T> createJahiaPreferenceNode(Principal principal);


    /**
     * @param jahiaPreference
     * @return true if the preference value is correct
     * @throws JahiaPreferencesNotValidException
     *
     */
    abstract public boolean validate(JahiaPreference<T> jahiaPreference) throws JahiaPreferencesNotValidException;


    /**
     * Get jahia preference by a jahia preference key attributes. A jahia preference key is dynamically created.
     *
     * @param xpathKey
     * @return
     * @throws JahiaPreferenceNotDefinedAttributeException
     *          if the jahia preference key attributes map contains a un-valid attributes.
     */
    abstract public JahiaPreference<T> getJahiaPreference(Principal principal, String xpathKey);


    /**
     *
     * @param principal
     * @param xpathKey
     * @param notNull
     * @return
     */
    abstract public JahiaPreference<T> getJahiaPreference(Principal principal, String xpathKey, boolean notNull);


    /**
     * Get all preferences of a principal
     * WARNING: if there is lots of preferences, it can be time consuming.
     *
     * @param principal
     * @return
     */
    abstract public List<JahiaPreference<T>> getJahiaAllPreferences(Principal principal);


    /**
     * Get a List of preferences depending on the revelant properties
     * Example:
     * JahiaPreferenceKey = {pid,wid}
     * getJahiaPreferencesByPartialKey(key,{wid}) --> get list of preferences with key.getWid() == wid ; pid properties is ignored.
     *
     * @param principal
     * @param xpath
     * @return
     */
    abstract public List<JahiaPreference<T>> findJahiaPreferences(Principal principal, String xpath);


    /**
     * Get jahia preferences of the current user
     *
     * @param processingContext
     * @return list <preference> that contains all preferences of the principal.
     */

    abstract public List<JahiaPreference<T>> getAllJahiaPreferences(ProcessingContext processingContext);

    /**
     * Delete a jahia preference that has a key created from jahiaPreferenceKeyAttributes.
     *
     * @param principal
     * @param xpath
     * @throws JahiaPreferenceNotDefinedAttributeException
     *
     */
    abstract public void deleteJahiaPreference(Principal principal, String xpath) throws JahiaPreferenceNotDefinedAttributeException;


    /**
     * Delete a jahia preference.
     *
     * @param jahiaPreference
     */
    abstract public void deleteJahiaPreference(JahiaPreference<T> jahiaPreference);


    /**
     * Set a jahia preference.
     *
     * @param jahiaPreference
     * @throws JahiaPreferencesNotValidException
     *
     */
    abstract public void setJahiaPreference(JahiaPreference<T> jahiaPreference);

    /**
     * Set a jahia preference value. The jahia preference key object is created dynamically from the corresponding map.
     *
     * @param jahiaPreferenceAttributes
     * @throws JahiaPreferenceNotDefinedAttributeException
     *
     * @throws JahiaPreferenceNotDefinedPropertyException
     *
     * @throws JahiaPreferencesNotValidException
     *
     */
    abstract public void setJahiaPreferenceByMaps(Map<String, String> jahiaPreferenceAttributes) throws JahiaPreferenceNotDefinedAttributeException, JahiaPreferenceNotDefinedPropertyException, JahiaPreferencesNotValidException;


    /**
     * Delete all preferences of principal. All others attributes of the jahia preference key are ignored.
     *
     * @param principal
     */
    abstract public void deleteAllPreferencesByPrincipal(Principal principal);


}
