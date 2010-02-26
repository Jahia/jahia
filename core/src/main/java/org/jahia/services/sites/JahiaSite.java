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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaSitePropertyManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.ACLResourceInterface;
import org.jahia.services.acl.JahiaACLException;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;

import static org.jahia.services.sites.SitesSettings.*;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.utils.LanguageCodeConverters;

import javax.jcr.RepositoryException;
import java.io.Serializable;
import java.text.Collator;
import java.util.*;


/**
 * Class JahiaSite.<br>
 * A site item in Jahia
 *
 * @author Khue ng
 * @version 1.0
 */
public class JahiaSite implements ACLResourceInterface, Serializable {

    private static final long serialVersionUID = 5114861205251079794L;

    public static final String PROPERTY_ENFORCE_PASSWORD_POLICY = "enforcePasswordPolicy";

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (JahiaSite.class);


    /** the site id * */
    private int mSiteID = -1;

    /** the site display title * */
    private String mTitle = "";

    /** a unique String identifier key choosed by the creator * */
    private String mSiteKey = "";

    /** Server Name www.jahia.org * */
    private String mServerName = "";

    private String templatePackageName;

    /** desc * */
    private String mDescr;

    private JahiaBaseACL mACL;

    private Properties mSettings = new Properties ();

    private Boolean mixLanguagesActive;
    private Set<String> languages;
    private Set<String> mandatoryLanguages;
    private String defaultLanguage;

    private String JCRLocalPath;

    /**
     * Constructor, the purpose of this empty constructor is to enable
     * <jsp:useBean...> tag in JSP
     */
    public JahiaSite () {
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


    public int getID () {
        return mSiteID;
    }

    public void setID (int id) {
        mSiteID = id;
    }

    public String getTitle () {
        return mTitle;
    }

    public void setTitle (String value) {
        mTitle = value;
    }

    /**
     * Return the Full Qualified Domain Name ( www.jahia.org )
     */
    public String getServerName () {
        return mServerName;
    }


    /**
     * Set the Full Qualified Domain Name ( www.jahia.org )
     */
    public void setServerName (String name) {
        mServerName = name;
    }

    /**
     * Return the unique String identifier key ( ex: jahia )
     */
    public String getSiteKey () {
        return mSiteKey;
    }


    public int getHomePageID () {
        return -1;
    }

    /**
     * @return
     */
    public JahiaPage getHomePage () {
        return null;
    }

    /**
     * @return
     */
    public JahiaPage getHomePage (EntryLoadRequest entryLoadRequest) {
        return null;
    }

    public ContentPage getHomeContentPage () {
        return null;
    }

    public boolean isURLIntegrityCheckEnabled () {
        // we activate URL integrity checks by default if no setting was found.
        String value = getProperty(
                URL_INTEGRITY_CHECKING_ENABLED, "true");
        // backward compatibility
        if ("0".equals(value) || "1".equals(value)) {
            return "1".equals(value);
        } else {
            return Boolean.valueOf(value);
        }
    }
    
    public void setURLIntegrityCheckEnabled (boolean val) {
        mSettings.setProperty(URL_INTEGRITY_CHECKING_ENABLED, 
                String.valueOf(val));
    }
    
    public boolean isWAIComplianceCheckEnabled() {
        // we activate WAI compliance checks by default if no setting was found.
        final String value = getProperty(WAI_COMPLIANCE_CHECKING_ENABLED,
                "true");
        // backward compatibility
        if ("0".equals(value) || "1".equals(value)) {
            return "1".equals(value);
        } else {
            return Boolean.valueOf(value);
        }
    }
    
    public void setWAIComplianceCheckEnabled (boolean val) {
        mSettings.setProperty(WAI_COMPLIANCE_CHECKING_ENABLED,
                String.valueOf(val));
    }
    
    public boolean isHtmlCleanupEnabled () {
        // we activate HTML cleanup by default if no setting was found.
        String value = getProperty(HTML_CLEANUP_ENABLED, "true");
        // backward compatibility
        if ("0".equals(value) || "1".equals(value)) {
            return "1".equals(value);  
        } else {
            return Boolean.valueOf(value);
        }
    }

    public void setHtmlCleanupEnabled (boolean val) {
        mSettings.setProperty(HTML_CLEANUP_ENABLED, String.valueOf(val));
    }

    public boolean isHtmlMarkupFilteringEnabled () {
        // we activate HTML markup filtering by default if no setting was found.
        String value = getProperty(HTML_MARKUP_FILTERING_ENABLED, "true");
        // backward compatibility
        if ("0".equals(value) || "1".equals(value)) {
            return "1".equals(value);  
        } else {
            return Boolean.valueOf(value);
        }
    }

    public String getHtmlMarkupFilteringTags() {
        return mSettings
                .getProperty(HTML_MARKUP_FILTERING_TAGS);
    }

    public void setHtmlMarkupFilteringEnabled (boolean val) {
        mSettings.setProperty(HTML_MARKUP_FILTERING_ENABLED,
                String.valueOf (val));
    }

    public void setHtmlMarkupFilteringTags(String tags) {
        mSettings.setProperty(HTML_MARKUP_FILTERING_TAGS,
                tags != null ? StringUtils.join(StringUtils.split(tags
                        .toLowerCase(), ", ;/<>"), ",") : "");
    }

    public String getTemplateFolder () {
        return ServicesRegistry.getInstance().getJahiaTemplateManagerService()
                .getTemplatePackage(getTemplatePackageName()).getRootFolder();
    }


    public String getDescr () {
        return mDescr;
    }

    public void setDescr (String descr) {
        mDescr = descr;
    }


    //-------------------------------------------------------------------------
    /*
    public String getSiteUrl(ProcessingContext jParam){

        try {
            return jParam.composeSiteUrl(this);
        } catch ( JahiaException je ){
            logger.debug("JahiaSite.getSiteUrl() exception " + je.getMessage(), je);
        }
        return "";
    }
    */



    /**
     * ACL handling based on JahiaPage model
     */

    private boolean checkAccess (JahiaUser user, int permission) {
        if (user == null) {
            return false;
        }

        // Test the access rights
        boolean result = false;
        try {
            result = mACL.getPermission (user, permission);
        } catch (JahiaACLException ex) {
            // if an error occured, just return false;
        }

        if (!result) {
            logger.debug ("Permission denied for user [" +
                    user.getName () + "] to page [" + getID () +
                    "] for access permission [" + permission + "]");
        } else {
            logger.debug ("Permission granted for user [" +
                    user.getName () + "] to page [" + getID () +
                    "] for access permission [" + permission + "]");
        }

        return result;
    }


    /**
     * Check if the user has administration access on the specified site. Admin
     * access means having the ability to admin the site ( manage users, groups,..)
     *
     * @param user Reference to the user.
     *
     * @return Return true if the user has admin right
     */
    public final boolean checkAdminAccess (JahiaUser user) {
        return checkAccess (user, JahiaBaseACL.ADMIN_RIGHTS);
    }

    /**
     * Check if the user has read access on the site.
     *
     * @param user Reference to the user.
     *
     * @return Return true if the user has read access
     *         or false in any other case.
     */
    public final boolean checkReadAccess (JahiaUser user) {
        return checkAccess (user, JahiaBaseACL.READ_RIGHTS);
    }

    /**
     * Check if the user has Write access on the site.
     *
     * @param user Reference to the user.
     *
     * @return Return true if the user has read access
     *         or false in any other case.
     */
    public final boolean checkWriteAccess (JahiaUser user) {
        return checkAccess (user, JahiaBaseACL.WRITE_RIGHTS);
    }


    /**
     * Return the site's ACL object.
     *
     * @return Return the page's ACL.
     */
    public final JahiaBaseACL getACL () {
        return mACL;
    }

    /**
     * Return the ACL unique identification number.
     *
     * @return Return the ACL ID.
     */
    public final int getAclID () {
        int id = 0;
        try {
            id = mACL.getID ();
        } catch (JahiaACLException ex) {
            // This exception should not happen ... :)
        }
        return id;
    }





    /**
     * Site's Settings stored in jahia_site_prop table
     *
     * Khue : better to store in a XML config file. TODO
     *
     */

    /**
     * set the default homepage definition for users
     */
    public boolean setUserDefaultHomepageDef (int id) {

        try {
            JahiaSitePropertyManager manager = (JahiaSitePropertyManager) SpringContextSingleton.
                getInstance().getContext().
                getBean(JahiaSitePropertyManager.class.getName());
            String value = Integer.toString(id);
            manager.save(this,
                         USER_DEFAULT_HOMEPAGE_DEF,
                         value);
            this.mSettings.setProperty(USER_DEFAULT_HOMEPAGE_DEF,value);
            ServicesRegistry.getInstance().getJahiaSitesService().updateSite(this);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * returns the default homepage definition for users
     * -1 : undefined
     */
    public int getUserDefaultHomepageDef () {

        try {
            JahiaSitePropertyManager manager = (JahiaSitePropertyManager) SpringContextSingleton.
                getInstance().getContext().
                getBean(JahiaSitePropertyManager.class.getName());
            String value = manager.getProperty (this,
                            USER_DEFAULT_HOMEPAGE_DEF);
            if (value == null || value.trim ().equals ("")) {
                return -1;
            }
            return Integer.parseInt (value);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return -1;

    }

    /**
     * set the default homepage definition for groups
     */
    public boolean setGroupDefaultHomepageDef (int id) {

        try {
            JahiaSitePropertyManager manager = (JahiaSitePropertyManager) SpringContextSingleton.
                getInstance().getContext().
                getBean(JahiaSitePropertyManager.class.getName());
            String value = Integer.toString(id);
            manager.save (this,
                          GROUP_DEFAULT_HOMEPAGE_DEF,
                          value);
            mSettings.setProperty(GROUP_DEFAULT_HOMEPAGE_DEF, value);
            ServicesRegistry.getInstance().getJahiaSitesService().updateSite(this);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;

    }

    /**
     * returns the default homepage definition for groups
     */
    public int getGroupDefaultHomepageDef () {

        try {
            JahiaSitePropertyManager manager = (JahiaSitePropertyManager) SpringContextSingleton.
                getInstance().getContext().
                getBean(JahiaSitePropertyManager.class.getName());
            String value = manager.getProperty (this,
                            GROUP_DEFAULT_HOMEPAGE_DEF);
            if (value == null || value.trim ().equals ("")) {
                return -1;
            }
            return Integer.parseInt (value);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return -1;

    }


    /**
     * set the default homepage definition for users activation
     */
    public boolean setUserDefaultHomepageDefActiveState (boolean active) {

        try {
            int value = 0; // not active;
            if (active)
                value = 1;
            JahiaSitePropertyManager manager = (JahiaSitePropertyManager) SpringContextSingleton.
                getInstance().getContext().
                getBean(JahiaSitePropertyManager.class.getName());
            String bool = Integer.toString(value);
            manager.save(this,
                         USER_DEFAULT_HOMEPAGE_DEF_ACTIVE,
                         bool);
            mSettings.setProperty(USER_DEFAULT_HOMEPAGE_DEF_ACTIVE,bool);
            ServicesRegistry.getInstance().getJahiaSitesService().updateSite(this);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return true;

    }

    /**
     * returns the default homepage definition for users activation,
     * -1: if not defined
     * 0: not active
     * 1. active
         */
    public int getUserDefaultHomepageDefActiveState () {

        try {
            JahiaSitePropertyManager manager = (JahiaSitePropertyManager) SpringContextSingleton.
                getInstance().getContext().
                getBean(JahiaSitePropertyManager.class.getName());
            String value = manager.getProperty (this,
                            USER_DEFAULT_HOMEPAGE_DEF_ACTIVE);
            if (value == null || value.trim ().equals ("")) {
                return -1;
            }

            return Integer.parseInt (value);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return -1;

    }


    /**
     * set the default homepage definition for groups activation
     */
    public boolean setGroupDefaultHomepageDefActiveState (boolean active) {

        try {
            int value = 0; // not active;
            if (active)
                value = 1;
            JahiaSitePropertyManager manager = (JahiaSitePropertyManager) SpringContextSingleton.
                getInstance().getContext().
                getBean(JahiaSitePropertyManager.class.getName());
            String bool = Integer.toString(value);
            manager.save(this, GROUP_DEFAULT_HOMEPAGE_DEF_ACTIVE, bool);
            mSettings.setProperty(GROUP_DEFAULT_HOMEPAGE_DEF_ACTIVE, bool);
            ServicesRegistry.getInstance().getJahiaSitesService().updateSite(this);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;

    }

    /**
     * returns the default homepage definition for users activation,
     * -1: if not defined
     * 0: not active
     * 1. active
     */
    public int getGroupDefaultHomepageDefActiveState () {

        try {
            JahiaSitePropertyManager manager = (JahiaSitePropertyManager) SpringContextSingleton.
                getInstance().getContext().
                getBean(JahiaSitePropertyManager.class.getName());
            String value = manager.getProperty (this,
                            GROUP_DEFAULT_HOMEPAGE_DEF_ACTIVE);
            if (value == null || value.trim ().equals ("")) {
                return -1;
            }

            return Integer.parseInt (value);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return -1;

    }

    /**
     * set the default homepage definition for users at creation only state
     */
    public boolean setUserDefaultHomepageDefAtCreationOnly (boolean active) {

        try {
            int value = 0; // not active;
            if (active)
                value = 1;
            JahiaSitePropertyManager manager = (JahiaSitePropertyManager) SpringContextSingleton.
                getInstance().getContext().
                getBean(JahiaSitePropertyManager.class.getName());
            String bool = Integer.toString(value);
            manager.save(this,
                         USER_DEFAULT_HOMEPAGE_DEF_ATCREATION,
                         bool);
            mSettings.setProperty(USER_DEFAULT_HOMEPAGE_DEF_ATCREATION,bool);
            ServicesRegistry.getInstance().getJahiaSitesService().updateSite(this);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;

    }

    /**
     * returns the default homepage definition for users at creation only,
     * -1: if not defined
     * 0: false
     * 1. true
     */
    public int getUserDefaultHomepageDefAtCreationOnly () {

        try {
            JahiaSitePropertyManager manager = (JahiaSitePropertyManager) SpringContextSingleton.
                getInstance().getContext().
                getBean(JahiaSitePropertyManager.class.getName());
            String value = manager.getProperty (this,
                            USER_DEFAULT_HOMEPAGE_DEF_ATCREATION);
            if (value == null || value.trim ().equals ("")) {
                return -1;
            }

            return Integer.parseInt (value);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return -1;

    }

    /**
     * set the default homepage definition for groups at creation only state
     */
    public boolean setGroupDefaultHomepageDefAtCreationOnly (boolean active) {

        try {
            int value = 0; // not active;
            if (active)
                value = 1;
            JahiaSitePropertyManager manager = (JahiaSitePropertyManager) SpringContextSingleton.
                getInstance().getContext().
                getBean(JahiaSitePropertyManager.class.getName());
            String bool = Integer.toString(value);
            manager.save (this,
                          GROUP_DEFAULT_HOMEPAGE_DEF_ATCREATION,
                          bool);
            mSettings.setProperty(GROUP_DEFAULT_HOMEPAGE_DEF_ATCREATION,bool);
            ServicesRegistry.getInstance().getJahiaSitesService().updateSite(this);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;

    }

    /**
     * returns the default homepage definition for groups activation,
     * -1: if not defined
     * 0: false
     * 1. true
     */
    public int getGroupDefaultHomepageDefAtCreationOnly () {

        try {
            JahiaSitePropertyManager manager = (JahiaSitePropertyManager) SpringContextSingleton.
                getInstance().getContext().
                getBean(JahiaSitePropertyManager.class.getName());
            String value = manager.getProperty (this,
                            GROUP_DEFAULT_HOMEPAGE_DEF_ATCREATION);
            if (value == null || value.trim ().equals ("")) {
                return -1;
            }

            return Integer.parseInt (value);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return -1;

    }

    /**
     * Returns a List of site language settings. The order of this List
     * corresponds to the ranking of the languages.
     *
     * @return a List containing String elements.
     *
     */
    public Set<String> getLanguages () {
        return languages;
    }

    public String[] getActiveLanguageCodes() {
        return getLanguages().toArray(new String[getLanguages().size()]);
    }


    /**
     * Returns an List of site language  ( as Locale ).
     *
     * @return an List of Locale elements.
     *
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
    public void setLanguages (Set<String> languages) {
        this.languages = languages;
    }

    /**
     * Sets the value of the site property that controls
     *
     * @param mixLanguagesActive
     */
    public void setMixLanguagesActive (boolean mixLanguagesActive) {
        this.mixLanguagesActive = mixLanguagesActive;
    }

    public boolean isMixLanguagesActive () {
        return mixLanguagesActive;
    }

    public void setSettings (Properties props) {
        this.mSettings = props != null ? props : new Properties();
    }

    public Properties getSettings () {
        return mSettings;
    }

    /**
     * Return jcr path "/siteKey/ContentPage_10/ContainerList_332"
     *
     * @return
     * @throws JahiaException
     */
    public String getJCRPath() throws JahiaException {
        try {
            if (getUUID() != null) {
                return JCRSessionFactory.getInstance().getCurrentUserSession().getNodeByUUID("jahia", getUUID()).getPath();
            } else {
                return getJCRLocalPath();
            }
        } catch (RepositoryException e) {
            throw new JahiaException("Error while retrieving site's JCR Path","Error while retrieving site's JCR Path",0,0,e);
        }
    }


    public String getUUID() throws JahiaException {
        return (String) getSettings().get("uuid");
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
            if (site1 == null || site1.getTitle()== null) {
                return 1;
            }
            if (site2 == null || site2.getTitle()== null) {
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
     * @param packageName
     *            the new template package name for this virtual site
     */
    public void setTemplatePackageName(String packageName) {
        this.templatePackageName = packageName;
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
        String value = mSettings.getProperty(key);
        return value != null ? value : defaultValue;
    }
    
    public boolean isFileLockOnPublicationEnabled() {
        return Boolean.valueOf(getProperty(FILE_LOCK_ON_PUBLICATION, "false"));
    }
    
    public void setFileLockOnPublicationEnabled(
            boolean fileLockOnPublicationEnabled) {
        mSettings.setProperty(FILE_LOCK_ON_PUBLICATION, String
                .valueOf(fileLockOnPublicationEnabled));
    }
     // get a google analyitcs parameter
    public String getGAproperty(String key, String defaultValue) {
        String value = mSettings.getProperty(key);
        return value != null ? value : defaultValue;
    }

    public String getJCRLocalPath() {
        return JCRLocalPath;
    }

    public void setJCRLocalPath(String JCRLocalPath) {
        this.JCRLocalPath = JCRLocalPath;
    }
}
