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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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

    String getDefaultLanguage();

    /**
     * Returns the description, provided for this site.
     *
     * @return the description, provided for this site
     */
    String getDescription();


    /**
     * Returns a set of languages, which are deactivated completely for browsing and editing.
     *
     * @return a set of languages, which are deactivated completely for browsing and editing
     */
    Set<String> getInactiveLanguages();

    /**
     * Returns a set of languages, which are not considered in live mode browsing, i.e. are currently inactive in navigation.
     *
     * @return a set of languages, which are not considered in live mode browsing, i.e. are currently inactive in navigation
     */
    Set<String> getInactiveLiveLanguages();

    List<String> getInstalledModules();

    /**
     * Returns a List of site language settings. The order of this List
     * corresponds to the ranking of the languages.
     *
     * @return a List containing String elements.
     */
    Set<String> getLanguages() ;


    /**
     * Returns an List of site language  ( as Locale ).
     *
     * @return an List of Locale elements.
     */
    List<Locale> getLanguagesAsLocales() ;


    Set<String> getMandatoryLanguages() ;

    /**
     * Return the Full Qualified Domain Name ( www.jahia.org )
     */
    String getServerName() ;

    /**
     * Returns a list of additional server names (aliases).
     *
     * @return list of additional server names (aliases)
     */
    List<String> getServerNameAliases();

    /**
     * Return the list of accepted server names.
     */
    List<String> getAllServerNames() ;

    /**
     * Return the unique String identifier key ( ex: jahia )
     */
    String getSiteKey();

    /**
     * Returns the corresponding template set name of this virtual site.
     *
     * @return the corresponding template set name of this virtual site
     */
    String getTemplatePackageName();

    String getTemplateFolder();

    String getTitle();

    /**
     * Returns <code>true</code> if this site is the default one on the server.
     *
     * @return <code>true</code> if this site is the default one on the server
     */
    boolean isDefault();

    boolean isMixLanguagesActive();

    boolean isAllowsUnlistedLanguages();

    void setDefaultLanguage(String defaultLanguage);

    void setDescr(String descr);

    void setDescription(String description);

    /**
     * Sets languages, which are completely deactivated for browsing and editing.
     *
     * @param inactiveLanguages
     *            the set of inactive languages
     */
    void setInactiveLanguages(Set<String> inactiveLanguages);

    /**
     * Sets languages, which are not considered in live mode browsing, i.e. are currently inactive in navigation.
     *
     * @param inactiveLiveLanguages
     *            the set of inactive languages
     */
    void setInactiveLiveLanguages(Set<String> inactiveLiveLanguages);

    void setInstalledModules(List<String> installedModules);


    /**
     * Sets the language settings for this site. This directly interfaces with
     * the persistant storage to store the modifications if there were any.
     */
    void setLanguages(Set<String> languages);

    void setMandatoryLanguages(Set<String> mandatoryLanguages);

    /**
     * Sets the value of the site property that controls
     *
     * @param mixLanguagesActive true or false
     */
    void setMixLanguagesActive(boolean mixLanguagesActive);

    void setAllowsUnlistedLanguages(boolean allowsUnlistedLanguages);

    /**
     * Set the Full Qualified Domain Name ( www.jahia.org )
     */
    void setServerName(String name);

    /**
     * Sets a list of additional server names (aliases).
     *
     * @param names a list of additional server names (aliases)
     */
    void setServerNameAliases(List<String> names);

    void setTitle(String value);

    String getJCRLocalPath();

}
