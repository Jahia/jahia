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
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.RepositoryException;
import java.io.Serializable;
import java.text.Collator;
import java.util.*;

import static org.jahia.services.sites.SitesSettings.*;


/**
 * Represent a virtual site (web project) in Jahia's context.
 *
 * @author Khue ng
 */
public class JahiaSite implements Serializable {

    public static class TitleComparator implements Comparator<JahiaSite> {

        private Collator collator = Collator.getInstance();

        public TitleComparator() {
        }

        public TitleComparator(Locale locale) {
            if (locale != null) {
                collator = Collator.getInstance(locale);
            }
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

    private static final long serialVersionUID = 5114861205251079794L;

    public static TitleComparator getTitleComparator() {
        return new TitleComparator();
    }

    public static TitleComparator getTitleComparator(Locale locale) {
        return new TitleComparator(locale);
    }

    private String defaultLanguage;

    private Set<String> inactiveLanguages = new HashSet<String>();
    
    private Set<String> inactiveLiveLanguages = new HashSet<String>();
    
    private List<String> installedModules;

    private String JCRLocalPath;

    private Set<String> languages;

    private Set<String> mandatoryLanguages;

    private String mDescr;
    
    private Boolean mixLanguagesActive;
    /**
     * Server Name www.jahia.org *
     */
    private String mServerName = "";
    
    private Properties mSettings = new Properties();

    /**
     * the site id *
     */
    private int mSiteID = -1;

    /**
     * a unique String identifier key chosen by the creator *
     */
    private String mSiteKey = "";

    /**
     * the site display title *
     */
    private String mTitle = "";

    private String templatePackageName;


    private String uuid;

    /**
     * Constructor, the purpose of this empty constructor is to enable
     * <jsp:useBean...> tag in JSP
     */
    public JahiaSite() {
        super();
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

    public String getDefaultLanguage() {
        return defaultLanguage;
    }


    public String getDescr() {
        return mDescr;
    }

    /**
     * returns the default homepage definition for groups
     */
    public int getGroupDefaultHomepageDef() {
        return -1;

    }

    public int getHomePageID() {
        return -1;
    }

    public String getHtmlMarkupFilteringTags() {
        return mSettings.getProperty(HTML_MARKUP_FILTERING_TAGS);
    }

    public int getID() {
        return mSiteID;
    }


    /**
     * Returns a set of languages, which are deactivated completely for browsing and editing.
     * 
     * @return a set of languages, which are deactivated completely for browsing and editing
     */
    public Set<String> getInactiveLanguages() {
        return inactiveLanguages;
    }

    /**
     * Returns a set of languages, which are not considered in live mode browsing, i.e. are currently inactive in navigation.
     * 
     * @return a set of languages, which are not considered in live mode browsing, i.e. are currently inactive in navigation
     */
    public Set<String> getInactiveLiveLanguages() {
        return inactiveLiveLanguages;
    }

    public List<String> getInstalledModules() {
        return installedModules;
    }



    public String getJCRLocalPath() {
        return JCRLocalPath;
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


    public Set<String> getMandatoryLanguages() {
        return mandatoryLanguages;
    }

    public JCRNodeWrapper getNode() throws RepositoryException {
        return JCRSessionFactory.getInstance().getCurrentUserSession().getNode(getJCRLocalPath());
    }

    private String getProperty(String key, String defaultValue) {
        Object value = mSettings.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * Return the Full Qualified Domain Name ( www.jahia.org )
     */
    public String getServerName() {
        return mServerName;
    }

    public Properties getSettings() {
        return mSettings;
    }

    /**
     * Return the unique String identifier key ( ex: jahia )
     */
    public String getSiteKey() {
        return mSiteKey;
    }

    public String getTemplateFolder() {
        return ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                .getTemplatePackage(getTemplatePackageName()).getRootFolder();
    }

    /**
     * Returns the corresponding template set name of this virtual site.
     *
     * @return the corresponding template set name of this virtual site
     */
    public String getTemplatePackageName() {
        return templatePackageName;
    }

    public String getTitle() {
        return mTitle;
    }

    /**
     * returns the default homepage definition for users
     * -1 : undefined
     */
    public int getUserDefaultHomepageDef() {
        return -1;

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

    public boolean isHtmlMarkupFilteringEnabled() {
        return Boolean.valueOf(getProperty(HTML_MARKUP_FILTERING_ENABLED, "false"));
    }

    public boolean isMixLanguagesActive() {
        return mixLanguagesActive;
    }

    public boolean isWCAGComplianceCheckEnabled() {
        return Boolean.valueOf(getProperty(WCAG_COMPLIANCE_CHECKING_ENABLED, "false"));
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
        if (inactiveLiveLanguages.contains(defaultLanguage)) {
            inactiveLiveLanguages.remove(defaultLanguage);
        }
    }

    public void setDescr(String descr) {
        mDescr = descr;
    }

    public void setID(int id) {
        mSiteID = id;
    }

    /**
     * Sets languages, which are completely deactivated for browsing and editing.
     * 
     * @param inactiveLanguages
     *            the set of inactive languages
     */
    public void setInactiveLanguages(Set<String> inactiveLanguages) {
        this.inactiveLanguages = inactiveLanguages == null ? new HashSet<String>()
                : inactiveLanguages;
    }

    /**
     * Sets languages, which are not considered in live mode browsing, i.e. are currently inactive in navigation.
     * 
     * @param inactiveLiveLanguages
     *            the set of inactive languages
     */
    public void setInactiveLiveLanguages(Set<String> inactiveLiveLanguages) {
        this.inactiveLiveLanguages = inactiveLiveLanguages == null ? new HashSet<String>()
                : inactiveLiveLanguages;
    }

    public void setInstalledModules(List<String> installedModules) {
        this.installedModules = installedModules;
    }

    public void setJCRLocalPath(String JCRLocalPath) {
        this.JCRLocalPath = JCRLocalPath;
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

    public void setMandatoryLanguages(Set<String> mandatoryLanguages) {
        this.mandatoryLanguages = mandatoryLanguages;
    }

    /**
     * Sets the value of the site property that controls
     *
     * @param mixLanguagesActive
     */
    public void setMixLanguagesActive(boolean mixLanguagesActive) {
        this.mixLanguagesActive = mixLanguagesActive;
    }

    /**
     * Set the Full Qualified Domain Name ( www.jahia.org )
     */
    public void setServerName(String name) {
        mServerName = name;
    }

    /**
     * Sets the template package name for this virtual site.
     *
     * @param packageName the new template package name for this virtual site
     */
    public void setTemplatePackageName(String packageName) {
        this.templatePackageName = packageName;
    }

    public void setTitle(String value) {
        mTitle = value;
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

    public String toString() {
        final StringBuffer buff = new StringBuffer();
        buff.append("JahiaSite: ID = ").append(mSiteID).
                append(", Settings: ").append(mSettings);
        return buff.toString();
    }
}
