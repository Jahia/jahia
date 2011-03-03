/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2011 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.LanguageCodeConverters;

import java.io.Serializable;
import java.text.Collator;
import java.util.*;

import static org.jahia.services.sites.SitesSettings.*;


/**
 * Class JahiaSite.<br>
 * A site item in Jahia
 *
 * @author Khue ng
 * @version 1.0
 */
public class JahiaSite implements Serializable {

    private static final long serialVersionUID = 5114861205251079794L;

    /**
     * the site id *
     */
    private int mSiteID = -1;

    /**
     * the site display title *
     */
    private String mTitle = "";

    /**
     * a unique String identifier key chosen by the creator *
     */
    private String mSiteKey = "";

    /**
     * Server Name www.jahia.org *
     */
    private String mServerName = "";

    private String templatePackageName;
    private List<String> installedModules;

    /**
     * desc *
     */
    private String mDescr;

    private Properties mSettings = new Properties();

    private Boolean mixLanguagesActive;
    private Set<String> languages;
    private Set<String> mandatoryLanguages;
    private String defaultLanguage;

    private String JCRLocalPath;

    private String uuid;

    /**
     * Constructor, the purpose of this empty constructor is to enable
     * <jsp:useBean...> tag in JSP
     */
    public JahiaSite() {
    }

    /**
     * Constructor
     */
    public JahiaSite(int id, String title, String serverName, String siteKey,
                     String descr,
                     Properties settings, String JCRLocalPath) {
        this();
        mSiteID = id;
        mTitle = title;
        mServerName = serverName;
        mSiteKey = siteKey;

        if (descr == null) {
            descr = "no desc";
        }
        mDescr = descr;

        if (settings != null) {
            mSettings = settings;
        }

        this.JCRLocalPath = JCRLocalPath;
    }


    public int getID() {
        return mSiteID;
    }

    public void setID(int id) {
        mSiteID = id;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String value) {
        mTitle = value;
    }

    /**
     * Return the Full Qualified Domain Name ( www.jahia.org )
     */
    public String getServerName() {
        return mServerName;
    }


    /**
     * Set the Full Qualified Domain Name ( www.jahia.org )
     */
    public void setServerName(String name) {
        mServerName = name;
    }

    /**
     * Return the unique String identifier key ( ex: jahia )
     */
    public String getSiteKey() {
        return mSiteKey;
    }


    public int getHomePageID() {
        return -1;
    }

    public boolean isWCAGComplianceCheckEnabled() {
        return Boolean.valueOf(getProperty(WCAG_COMPLIANCE_CHECKING_ENABLED, "false"));
    }

    public boolean isHtmlMarkupFilteringEnabled() {
        return Boolean.valueOf(getProperty(HTML_MARKUP_FILTERING_ENABLED, "false"));
    }

    public String getHtmlMarkupFilteringTags() {
        return mSettings.getProperty(HTML_MARKUP_FILTERING_TAGS);
    }

    public String getTemplateFolder() {
        return ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                .getTemplatePackage(getTemplatePackageName()).getRootFolder();
    }


    public String getDescr() {
        return mDescr;
    }

    public void setDescr(String descr) {
        mDescr = descr;
    }



    /**
     * returns the default homepage definition for users
     * -1 : undefined
     */
    public int getUserDefaultHomepageDef() {
        return -1;

    }

    /**
     * returns the default homepage definition for groups
     */
    public int getGroupDefaultHomepageDef() {
        return -1;

    }


    /**
     * Returns a List of site language settings. The order of this List
     * corresponds to the ranking of the languages.
     *
     * @return a List containing String elements.
     */
    public Set<String> getLanguages() {
        return languages;
    }


    /**
     * Returns an List of site language  ( as Locale ).
     *
     * @return an List of Locale elements.
     */
    public List<Locale> getLanguagesAsLocales() {

        List<Locale> localeList = new ArrayList<Locale>();
        if (languages != null) {
            for (String language : languages) {
                Locale tempLocale = LanguageCodeConverters.languageCodeToLocale(language);
                localeList.add(tempLocale);
            }

        }
        return localeList;
    }

    /**
     * Sets the language settings for this site. This directly interfaces with
     * the persistant storage to store the modifications if there were any.
     *
     * @throws JahiaException when an error occured while storing the modified
     *                        site language settings values.
     */
    public void setLanguages(Set<String> languages) {
        this.languages = languages;
    }

    /**
     * Sets the value of the site property that controls
     *
     * @param mixLanguagesActive
     */
    public void setMixLanguagesActive(boolean mixLanguagesActive) {
        this.mixLanguagesActive = mixLanguagesActive;
    }

    public boolean isMixLanguagesActive() {
        return mixLanguagesActive;
    }

    public void setMandatoryLanguages(Set<String> mandatoryLanguages) {
        this.mandatoryLanguages = mandatoryLanguages;
    }

    public Set<String> getMandatoryLanguages() {
        return mandatoryLanguages;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public static class TitleComparator implements Comparator<JahiaSite> {

        private Collator collator = Collator.getInstance();

        public TitleComparator(Locale locale) {
            if (locale != null) {
                collator = Collator.getInstance(locale);
            }
        }

        public TitleComparator() {
        }

        public int compare(JahiaSite site1,
                           JahiaSite site2) {
            if (site1 == null || site1.getTitle() == null) {
                return 1;
            }
            if (site2 == null || site2.getTitle() == null) {
                return -1;
            }
            return collator.compare(site1.getTitle(), site2.getTitle());
        }

        public boolean equals(Object obj) {
            if (obj instanceof TitleComparator) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static TitleComparator getTitleComparator() {
        return new TitleComparator();
    }

    public static TitleComparator getTitleComparator(Locale locale) {
        return new TitleComparator(locale);
    }

    public String toString() {
        final StringBuffer buff = new StringBuffer();
        buff.append("JahiaSite: ID = ").append(mSiteID).
                append(", Settings: ").append(mSettings);
        return buff.toString();
    }

    /**
     * Returns the corresponding template set name of this virtual site.
     *
     * @return the corresponding template set name of this virtual site
     */
    public String getTemplatePackageName() {
        return templatePackageName;
    }

    /**
     * Sets the template package name for this virtual site.
     *
     * @param packageName the new template package name for this virtual site
     */
    public void setTemplatePackageName(String packageName) {
        this.templatePackageName = packageName;
    }

    public List<String> getInstalledModules() {
        return installedModules;
    }

    public void setInstalledModules(List<String> installedModules) {
        this.installedModules = installedModules;
    }

    public boolean equals(Object obj) {
        // fix for PEU-77 (contributed by PELTIER Olivier)
        if (this == obj) {
            return true;
        }
        if (obj instanceof JahiaSite) {
            return mSiteID == ((JahiaSite) obj).getID();
        } else {
            return false;
        }
    }

    public int hashCode() {
        // fix for PEU-77 (by PELTIER Olivier)
        return new HashCodeBuilder().append(mSiteID).toHashCode();
    }

    /**
     * Returns <code>true</code> if this site is the default one on the server.
     *
     * @return <code>true</code> if this site is the default one on the server
     */
    public boolean isDefault() {
        JahiaSite defaultSite = ServicesRegistry.getInstance()
                .getJahiaSitesService().getDefaultSite();
        return defaultSite != null && defaultSite.equals(this);
    }

    private String getProperty(String key, String defaultValue) {
        Object value = mSettings.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    public String getJCRLocalPath() {
        return JCRLocalPath;
    }

    public void setJCRLocalPath(String JCRLocalPath) {
        this.JCRLocalPath = JCRLocalPath;
    }

    /**
     * Returns the corresponding JCR node identifier or <code>null</code> if the
     * site is coming not from the JCR provider.
     * 
     * @return the the corresponding JCR node identifier or <code>null</code> if
     *         the site is coming not from the JCR provider
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the corresponding JCR node identifier or <code>null</code> if the
     * site is coming not from the JCR provider.
     * 
     * @param uuid the corresponding JCR node identifier or <code>null</code> if
     *            the site is coming not from the JCR provider
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Properties getSettings() {
        return mSettings;
    }
}
