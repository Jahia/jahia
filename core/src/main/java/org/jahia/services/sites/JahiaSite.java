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
import org.jahia.bin.Jahia;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaSiteLanguageListManager;
import org.jahia.hibernate.manager.JahiaSiteLanguageMappingManager;
import org.jahia.hibernate.manager.JahiaSitePropertyManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.ACLResourceInterface;
import org.jahia.services.acl.JahiaACLException;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageService;
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

    /** is active or not * */
    private boolean mIsActive = false;

    /** the site's home page * */
    private int mHomePageID = -1;

    /** desc * */
    private String mDescr;

    private JahiaBaseACL mACL;

    private Properties mSettings = new Properties ();

    public static final String LANGUAGES_PROP_SEPARATOR = ",";

    private Boolean mixLanguagesActive;
    private List<SiteLanguageMapping> siteLanguageMappings;

    /**
     * Constructor, the purpose of this empty constructor is to enable
     * <jsp:useBean...> tag in JSP
     */
    public JahiaSite () {
    }

    /**
     * Constructor
     */
    public JahiaSite (int id, String title, String serverName, String siteKey,
                      boolean isActive, int homePageID, String descr,
                      JahiaBaseACL acl, Properties settings) {
        this();
        mSiteID = id;
        mTitle = title;
        mServerName = serverName;
        mSiteKey = siteKey;
        mIsActive = isActive;
        mHomePageID = homePageID;

        if (descr == null) {
            descr = "no desc";
        }
        mDescr = descr;

        mACL = acl;

        if (settings != null) {
            mSettings = settings;
        }
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


    public boolean isActive () {
        return mIsActive;
    }

    public void setActive (boolean value) {
        mIsActive = value;
    }

    public int getHomePageID () {
        return mHomePageID;
    }

    /**
     * @return
     */
    public JahiaPage getHomePage () {
        JahiaPage page = null;
        JahiaPageService ps = ServicesRegistry.getInstance ().getJahiaPageService ();
        if (ps == null) {
            logger.error("Cannot find home page, null pageService");
            return null;
        }
        try {
            page = ps.lookupPage (getHomePageID (), Jahia.getThreadParamBean().getEntryLoadRequest(), Jahia.getThreadParamBean().getUser(), true);
        } catch (JahiaException je) {
            logger.error("Cannot find home page, "+mHomePageID, je);
            return null;
        }
        return page;
    }

    /**
     * @return
     */
    public JahiaPage getHomePage (EntryLoadRequest entryLoadRequest) {
        JahiaPage page = null;
        JahiaPageService ps = ServicesRegistry.getInstance ().getJahiaPageService ();
        if (ps == null) {
            return null;
        }
        try {
            page = ps.lookupPage (getHomePageID (), entryLoadRequest);
        } catch (JahiaException je) {
            return null;
        }
        return page;
    }

    public ContentPage getHomeContentPage () {
        ContentPage contentPage = null;
        JahiaPageService ps = ServicesRegistry.getInstance ().getJahiaPageService ();
        if (ps == null) {
            return null;
        }
        try {
            contentPage = ps.lookupContentPage (getHomePageID (), true);
        } catch (JahiaException je) {
            return null;
        }
        return contentPage;
    }


    public void setHomePageID (int id) {
        mHomePageID = id;
    }

    public int getDefaultTemplateID () {
        return Integer.parseInt(getProperty("defaultTemplateID", "-1"));
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

    public void setDefaultTemplateID (int id) {
        mSettings.setProperty("defaultTemplateID", Integer.toString (id));
    }

    public boolean getTemplatesAutoDeployMode () {

        String val = mSettings.getProperty ("templatesAutoDeployMode");
        if (val == null) {
            return false;
        }
        return (val.equals ("true"));
    }

    public void setTemplatesAutoDeployMode (boolean mode) {
        mSettings.setProperty("templatesAutoDeployMode", mode ? "true" : "false");
    }

    public boolean getWebAppsAutoDeployMode () {

        String val = mSettings.getProperty ("webAppsAutoDeployMode");
        if (val == null) {
            return false;
        }
        return (val.equals ("true"));
    }

    public void setWebAppsAutoDeployMode (boolean mode) {
        mSettings.setProperty("webAppsAutoDeployMode", mode ? "true" : "false");
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
     * Change the versioning status.
     *
     * @param status active or deactivate versioning
     */
    public boolean setVersioning (boolean status) {
        synchronized (mSettings) {
            try {
                int value = 0; // not active;
                if (status)
                    value = 1;
                JahiaSitePropertyManager manager = (JahiaSitePropertyManager) SpringContextSingleton.
                getInstance().getContext().
                getBean(JahiaSitePropertyManager.class.getName());
                manager.save (this,
                        VERSIONING_ENABLED, Integer.toString (value));

                mSettings.setProperty (VERSIONING_ENABLED, Integer.toString (value));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return false;
            }
        }
        return true;
    }

    /**
     * returns the versioning status.
     */
    public boolean isVersioningEnabled () {

        synchronized (mSettings) {
            String prop = mSettings.getProperty (VERSIONING_ENABLED);
            try {
                if (prop != null) {
                    return (Integer.parseInt (prop) == 1);
                } else {
                    JahiaSitePropertyManager manager = (JahiaSitePropertyManager) SpringContextSingleton.
                getInstance().getContext().
                getBean(JahiaSitePropertyManager.class.getName());
                    prop = manager.getProperty (this,
                                    VERSIONING_ENABLED);
                    if (prop == null || prop.trim ().equals ("")) {
                        return false;
                    } else {
                        mSettings.setProperty (VERSIONING_ENABLED, prop.trim ());
                    }
                    return (Integer.parseInt (prop) == 1);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * Change the staging status.
     *
     * @param status active or deactivate staging
     */
    public boolean setStaging (boolean status) {
        synchronized (mSettings) {
            try {
                int value = 0; // not active;
                if (status)
                    value = 1;
                JahiaSitePropertyManager manager = (JahiaSitePropertyManager) SpringContextSingleton.
                getInstance().getContext().
                getBean(JahiaSitePropertyManager.class.getName());
                manager.save(this,
                        STAGING_ENABLED, Integer.toString (value));

                mSettings.setProperty (STAGING_ENABLED, Integer.toString (value));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return false;
            }
        }
        return true;
    }

    /**
     * returns the staging status.
     */
    public boolean isStagingEnabled () {

        synchronized (mSettings) {
            String prop = mSettings.getProperty (STAGING_ENABLED);
            try {
                if (prop != null) {
                    return (Integer.parseInt (prop) == 1);
                } else {
                    JahiaSitePropertyManager manager = (JahiaSitePropertyManager) SpringContextSingleton.
                getInstance().getContext().
                getBean(JahiaSitePropertyManager.class.getName());
                    prop = manager.getProperty (this,
                                    STAGING_ENABLED);
                    if (prop == null || prop.trim ().equals ("")) {
                        return false;
                    } else {
                        mSettings.setProperty (STAGING_ENABLED, prop.trim ());
                    }
                    return (Integer.parseInt (prop) == 1);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return false;
    }


    /**
     * Returns a List of site language settings. The order of this List
     * corresponds to the ranking of the languages.
     *
     * @return a List containing SiteLanguageSettings elements.
     *
     * @throws JahiaException if an error occured while retrieving the
     *                        list of languages
     */
    public List<SiteLanguageSettings> getLanguageSettings ()
            throws JahiaException {
        return getLanguageSettings (false);
    }

    /**
     * Returns a List of site language settings. The order of this List
     * corresponds to the ranking of the languages.
     * If activeOnly is true, return the active language only.
     *
     * @return a List containing SiteLanguageSettings elements.
     *
     * @throws JahiaException if an error occured while retrieving the
     *                        list of languages
     */
    @SuppressWarnings("unchecked")
    public List<SiteLanguageSettings> getLanguageSettings (boolean activeOnly)
            throws JahiaException {
        List<SiteLanguageSettings> siteLanguageSettings = new ArrayList<SiteLanguageSettings>();
        JahiaSiteLanguageListManager listManager = (JahiaSiteLanguageListManager)
                SpringContextSingleton.getInstance().getContext().getBean(JahiaSiteLanguageListManager.class.getName());
        ArrayList<SiteLanguageSettings> v = new ArrayList<SiteLanguageSettings>(listManager.getSiteLanguages (getID ()));
        if ( !activeOnly ){
            if ( v != null ){
                return (List<SiteLanguageSettings>) v.clone();
            }
        } else {
            if (v != null) {
                for (int i = 0; i < v.size (); i++) {
                    SiteLanguageSettings curSetting = (SiteLanguageSettings)
                            v.get (i);
                    if (curSetting.isActivated ()) {
                        siteLanguageSettings.add(curSetting);
                    }
                }
            }
        }
        return siteLanguageSettings;
    }

    /**
     * Returns an List of site language settings ( as Locale ).
     * If activeOnly is true, return the active language only.
     *
     * @param activeOnly
     *
     * @return an List of SiteLanguageSettings elements.
     *
     * @throws JahiaException if an error occured while retrieving the
     *                        list of languages
     */
    public List<Locale> getLanguageSettingsAsLocales (boolean activeOnly)
            throws JahiaException {
        List<SiteLanguageSettings> siteLanguageSettings = this.getLanguageSettings ();
        List<Locale> localeList = new ArrayList<Locale>();
        if (siteLanguageSettings != null) {
            for (int i = 0; i < siteLanguageSettings.size (); i++) {
                SiteLanguageSettings curSetting = (SiteLanguageSettings)
                        siteLanguageSettings.get (i);
                if (!activeOnly || curSetting.isActivated ()) {
                    Locale tempLocale =
                            LanguageCodeConverters.languageCodeToLocale (curSetting.getCode ());
                    localeList.add (tempLocale);
                }
            }
        }
        return localeList;
    }


    /**
     * Returns a List of site language mappings.
     *
     * @return a List containing SiteLanguageMappings elements.
     *
     * @throws JahiaException if an error occured while retrieving the
     *                        list of languages
     */
    public List<SiteLanguageMapping> getLanguageMappings ()
            throws JahiaException {
        if (siteLanguageMappings==null) {
            JahiaSiteLanguageMappingManager mappingManager = (JahiaSiteLanguageMappingManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaSiteLanguageMappingManager.class.getName());
            siteLanguageMappings = mappingManager.getSiteLanguageMappings (getID ());
        }
        return siteLanguageMappings;
    }

    public void setSiteLanguageMappings(List<SiteLanguageMapping> siteLanguageMappings) {
        this.siteLanguageMappings = siteLanguageMappings;
    }

    /**
     * Sets the language settings for this site. This directly interfaces with
     * the persistant storage to store the modifications if there were any.
     *
     * @param siteLanguagesSettings a List of SiteLanguageSettings objects.
     *
     * @throws JahiaException when an error occured while storing the modified
     *                        site language settings values.
     */
    public void setLanguageSettings (List<SiteLanguageSettings> siteLanguagesSettings)
            throws JahiaException {
        JahiaSiteLanguageListManager listManager = (JahiaSiteLanguageListManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaSiteLanguageListManager.class.getName());
        for (SiteLanguageSettings curLanguageSettings : siteLanguagesSettings) {
            if (curLanguageSettings.isInPersistantStorage()) {
                if (curLanguageSettings.isDataModified()) {
                    listManager.updateSiteLanguageSettings(curLanguageSettings);
                }
            } else {
                listManager.addSiteLanguageSettings(curLanguageSettings);
            }
        }
    }

    /**
     * Sets the value of the site property that controls
     *
     * @param mixLanguagesActive
     */
    public void setMixLanguagesActive (boolean mixLanguagesActive) {
        try {
            // delete old value first
            JahiaSitePropertyManager manager = (JahiaSitePropertyManager) SpringContextSingleton.
                getInstance().getContext().
                getBean(JahiaSitePropertyManager.class.getName());
            manager.remove (this, MIX_LANGUAGES_ACTIVE);
            String value = Boolean.valueOf(mixLanguagesActive).toString();
            manager.save (this, MIX_LANGUAGES_ACTIVE, value);
            this.mixLanguagesActive = Boolean.valueOf(mixLanguagesActive);
            this.mSettings.setProperty(MIX_LANGUAGES_ACTIVE,value);
            ServicesRegistry.getInstance().getJahiaSitesService().updateSite(this);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public boolean isMixLanguagesActive () {

        if (this.mixLanguagesActive != null) {
            return mixLanguagesActive.booleanValue ();
        }

        try {
            JahiaSitePropertyManager sitePropertyManager = (JahiaSitePropertyManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaSitePropertyManager.class.getName());
            String value = sitePropertyManager.getProperty (this,
                            MIX_LANGUAGES_ACTIVE);
            if (value == null || value.trim ().equals ("")) {
                return false;
            }
            Boolean mixLanguagesActive = Boolean.valueOf (value);

            this.mixLanguagesActive = mixLanguagesActive;

            return mixLanguagesActive.booleanValue ();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * The context from which to get the JahiaSite
     * @param context
     * @return
     */
    public static boolean isMixLanguagesActiveForSite (ProcessingContext context) {

        if (context==null || context.getSite()==null){
            return false;
        }
        return context.getSite().isMixLanguagesActive();
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
            return JCRSessionFactory.getInstance().getCurrentUserSession().getNodeByUUID("jahia", getUUID()).getPath();
        } catch (RepositoryException e) {
            throw new JahiaException("","",0,0,e);
        }
    }


    public String getUUID() throws JahiaException {
        return (String) getSettings().get("uuid");
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
        return mSettings.getProperty(TEMPLATE_PACKAGE_NAME);
    }

    /**
     * Sets the template package name for this virtual site.
     * 
     * @param packageName
     *            the new template package name for this virtual site
     */
    public void setTemplatePackageName(String packageName) {
        mSettings.setProperty(TEMPLATE_PACKAGE_NAME, packageName);
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

}
