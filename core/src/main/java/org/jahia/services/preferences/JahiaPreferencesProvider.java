/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.services.preferences;

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
