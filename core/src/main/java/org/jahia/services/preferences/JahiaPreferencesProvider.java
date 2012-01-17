/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.preferences;

import org.jahia.params.ProcessingContext;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.preferences.exception.JahiaPreferenceNotDefinedAttributeException;
import org.jahia.services.preferences.exception.JahiaPreferenceNotDefinedPropertyException;
import org.jahia.services.preferences.exception.JahiaPreferencesNotValidException;

import java.security.Principal;
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
     * Get the existing JahiaPreference associated to this path
     * @param principal the user for searching the preference
     * @param sqlConstraint the sql constraint of the preference
     * @return a JahiaPreference if found null otherwise
     */
    abstract public JahiaPreference<T> getJahiaPreference(Principal principal, String sqlConstraint);


    /**
     * Get or create the requested preference at the path
     * @param principal the user for searching the preference
     * @param sqlConstraint the sql constraint of the preference
     * @param notNull if true create the preference if not found
     * @return a JahiaPreference if prefrence found or notNull is true, null otherwise
     */
    abstract public JahiaPreference<T> getJahiaPreference(Principal principal, String sqlConstraint, boolean notNull);


    /**
     * Get all preferences of a user.
     * WARNING: if there is lots of preferences, it can be time consuming.
     * @param principal the user for whom we want the prefrences
     * @return a List of all JahiaPreference for this user
     */
    abstract public List<JahiaPreference<T>> getJahiaAllPreferences(Principal principal);


    /**
     * Find all preferences for a user mathing certain sqlConstraint
     * @param principal the user for whom we want the prefrences
     * @param sqlConstraint the sql constraint of the preference
     * @return a List of all JahiaPreference for this user
     */
    abstract public List<JahiaPreference<T>> findJahiaPreferences(Principal principal, String sqlConstraint);


    /**
     * Get jahia preferences of the current user
     *
     * @param processingContext
     * @return list <preference> that contains all preferences of the principal.
     */

    abstract public List<JahiaPreference<T>> getAllJahiaPreferences(ProcessingContext processingContext);

    /**
     * Delete Jahia Preference
     *
     * @param principal the user for whom we want the prefrences
     * @param sqlConstraint the sql constraint of the preference
     * @throws JahiaPreferenceNotDefinedAttributeException
     */
    abstract public void deleteJahiaPreference(Principal principal, String sqlConstraint) throws JahiaPreferenceNotDefinedAttributeException;


    /**
     * Delete Jahia Prefernce
     *
     * @param jahiaPreference the preference to delete
     */
    abstract public void deleteJahiaPreference(JahiaPreference<T> jahiaPreference);


    /**
     * Set Jahia preference
     *
     * @param jahiaPreference the preference to set
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
