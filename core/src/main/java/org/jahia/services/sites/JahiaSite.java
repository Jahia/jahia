/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
//
//
//  JahiaSite
//
//  NK      12.03.2001
//  AK      28.04.2001  move this class from data/sites to services/sites.
//  NK      02.05.2001  added purge apps, purge templates, purge users
//
//

package org.jahia.services.sites;

import java.util.*;



/**
 * Represent a virtual site (web project) in Jahia's context.
 *
 * @author Khue ng
 */
public interface JahiaSite {

    public String getDefaultLanguage();

    /**
     * Returns the description, provided for this site.
     * 
     * @return the description, provided for this site
     */
    public String getDescription();


    /**
     * Returns a set of languages, which are deactivated completely for browsing and editing.
     * 
     * @return a set of languages, which are deactivated completely for browsing and editing
     */
    public Set<String> getInactiveLanguages();

    /**
     * Returns a set of languages, which are not considered in live mode browsing, i.e. are currently inactive in navigation.
     * 
     * @return a set of languages, which are not considered in live mode browsing, i.e. are currently inactive in navigation
     */
    public Set<String> getInactiveLiveLanguages();

    public List<String> getInstalledModules();

    /**
     * Returns a List of site language settings. The order of this List
     * corresponds to the ranking of the languages.
     *
     * @return a List containing String elements.
     */
    public Set<String> getLanguages() ;


    /**
     * Returns an List of site language  ( as Locale ).
     *
     * @return an List of Locale elements.
     */
    public List<Locale> getLanguagesAsLocales() ;


    public Set<String> getMandatoryLanguages() ;

    /**
     * Return the Full Qualified Domain Name ( www.jahia.org )
     */
    public String getServerName() ;

    public List<String> getServerNameAliases();

    /**
     * Return the list of accepted server names
     */
    public List<String> getAllServerNames() ;

    /**
     * Return the unique String identifier key ( ex: jahia )
     */
    public String getSiteKey();

    /**
     * Returns the corresponding template set name of this virtual site.
     *
     * @return the corresponding template set name of this virtual site
     */
    public String getTemplatePackageName();

    public String getTemplateFolder();

    public String getTitle();

    /**
     * Returns <code>true</code> if this site is the default one on the server.
     *
     * @return <code>true</code> if this site is the default one on the server
     */
    public boolean isDefault();

    public boolean isMixLanguagesActive();

    public boolean isAllowsUnlistedLanguages();

    public void setDefaultLanguage(String defaultLanguage);

    public void setDescr(String descr);

    public void setDescription(String description);

    /**
     * Sets languages, which are completely deactivated for browsing and editing.
     * 
     * @param inactiveLanguages
     *            the set of inactive languages
     */
    public void setInactiveLanguages(Set<String> inactiveLanguages);

    /**
     * Sets languages, which are not considered in live mode browsing, i.e. are currently inactive in navigation.
     * 
     * @param inactiveLiveLanguages
     *            the set of inactive languages
     */
    public void setInactiveLiveLanguages(Set<String> inactiveLiveLanguages);

    public void setInstalledModules(List<String> installedModules);


    /**
     * Sets the language settings for this site. This directly interfaces with
     * the persistant storage to store the modifications if there were any.
     */
    public void setLanguages(Set<String> languages);

    public void setMandatoryLanguages(Set<String> mandatoryLanguages);

    /**
     * Sets the value of the site property that controls
     *
     * @param mixLanguagesActive true or false
     */
    public void setMixLanguagesActive(boolean mixLanguagesActive);

    public void setAllowsUnlistedLanguages(boolean allowsUnlistedLanguages);

    /**
     * Set the Full Qualified Domain Name ( www.jahia.org )
     */
    public void setServerName(String name);

    public void setServerNameAliases(List<String> names);

    public void setTitle(String value);

    String getJCRLocalPath();

}
