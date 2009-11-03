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
 package org.jahia.services.pages;

import org.apache.commons.httpclient.methods.GetMethod;
import org.jahia.bin.Jahia;
import org.jahia.content.ContentObject;
import org.jahia.content.CoreFilterNames;
import org.jahia.data.JahiaDOMObject;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.fields.JahiaPageField;
import org.jahia.exceptions.*;
import org.jahia.hibernate.manager.JahiaObjectManager;
import org.jahia.hibernate.manager.JahiaPagesManager;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.params.AdvPreviewSettings;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.acl.ACLNotFoundException;
import org.jahia.services.cache.Cache;
import org.jahia.services.cache.CacheService;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.workflow.WorkflowEvent;
import org.jahia.services.timebasedpublishing.TimeBasedPublishingService;
import org.jahia.utils.JahiaTools;
import org.jahia.utils.LanguageCodeConverters;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Class JahiaPageBaseService
 *
 * @author Eric Vassalli
 * @author Khue
 * @author Fulco Houkes
 * @version 1.0
 */
public class JahiaPageBaseService extends JahiaPageService {
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(JahiaPageBaseService.class);

    private static final int VERSION_START_STATUS = 2;

    /** the unique instance of this service */
    private static JahiaPageBaseService instance;

    // the Page Info cache name.
    public static final String PAGE_INFO_CACHE = "PageInfoCache";
    // the Page Versioning Info cache name.
    public static final String VERSIONING_PAGE_INFO_CACHE = "VersioningPageInfoCache";
    // the ContentPage cache name
    public static final String CONTENT_PAGE_CACHE = "ContentPageCache";
    
    /** The active Page info cache */

//    private static Cache mPageInfosCache;

    /** The versioning info cache */

//    private static Cache mVersioningPageInfosCache;

    /** the page child cache */
    private static Cache<Integer, List<Integer>> mPageChildIDsCache;

    private Cache<Integer, ContentPage> mContentPageCache;

    private JahiaPageTemplateService templateService;
    private JahiaPagesManager pageManager;

    private JahiaObjectManager jahiaObjectManager;

    private CacheService cacheService;

    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    public void setPageManager(JahiaPagesManager pageManager) {
        this.pageManager = pageManager;
    }

    public JahiaObjectManager getJahiaObjectManager() {
        return jahiaObjectManager;
    }

    public void setJahiaObjectManager(JahiaObjectManager jahiaObjectManager) {
        this.jahiaObjectManager = jahiaObjectManager;
    }

    public void setTemplateService(JahiaPageTemplateService templateService) {
        this.templateService = templateService;
    }

    /**
     * Default construtor, creates a new <code>JahiaPageBaseService</code> instance
     *
     */
    protected JahiaPageBaseService() {
    }

    /**
     * @param page
     * @param loadFlag
     * @param user
     * @return boolean
     */
    private boolean authorizePageLoad (JahiaPage page, int loadFlag,
                                       JahiaUser user) {

        if (!page.checkReadAccess(user)) {
            return false;
        }
        if (loadFlag == PageLoadFlags.ALL) {
            return true;
        }
        switch (page.getPageType()) {
            case (JahiaPageInfo.TYPE_DIRECT):
                return loadFlag == PageLoadFlags.DIRECT;
            case (JahiaPageInfo.TYPE_LINK):
                return loadFlag == PageLoadFlags.INTERNAL;
            case (JahiaPageInfo.TYPE_URL):
                return loadFlag == PageLoadFlags.URL;
        }
        return false;
    }

    //-------------------------------------------------------------------------
    public JahiaPage createPage(int siteID,
                                             int parentID,
                                             int pageType,
                                             String title,
                                             int pageTemplateID,
                                             String remoteURL,
                                             int pageLinkID,
                                             String creator,
                                             int parentAclID,
                                             ProcessingContext jParam)
            throws JahiaException {
       return createPage(siteID, parentID, pageType, title, pageTemplateID, remoteURL,pageLinkID,creator, parentAclID, jParam, null);
    }

    public JahiaPage createPage(int siteID,
                                             int parentID,
                                             int pageType,
                                             String title,
                                             int pageTemplateID,
                                             String remoteURL,
                                             int pageLinkID,
                                             String creator,
                                             int parentAclID,
                                             ProcessingContext jParam,
                                                JahiaPageField parentField)
            throws JahiaException {

        if (logger.isDebugEnabled()) {
            logger.debug("Creating Page " + title + " ...");
        }

        // Verify if the specified parent page does exist
        //
        // khatmandu--------------
        // MULTISITE ISSUE: if parent id equal zero, don't check parent page info...
        //
        if (parentID > 0) {
            List<JahiaPageInfo> parentPageInfoList = lookupPageInfos(parentID,
                EntryLoadRequest.CURRENT);
            if (parentPageInfoList == null) {
                throw new JahiaException("Could not create page.",
                                         "Could not create a new page : parent page [" +
                                         parentID + "] not found.",
                                         JahiaException.PAGE_ERROR,
                                         JahiaException.ERROR_SEVERITY);
            }
            // FIXME (NK) : What is this ??????????
            parentPageInfoList = null;
        }

        // Get the next available counter.
        int pageID = 0;

        ContentPage linkedPage = null;
        JahiaBaseACL acl = null;

        // An new ACL is created and associated automaticaly to the new page
        // only if the page is a DIRECT page or an URL link.
        // In case of a LINK typed page, the ACL of the linked page will be
        // used.
        switch (pageType) {

            case JahiaPage.TYPE_DIRECT:
//                acl = new JahiaBaseACL(parentAclID);

                acl = new JahiaBaseACL();
                try {
                    acl.create(parentAclID);
                } catch (ACLNotFoundException ex) {
                    throw new JahiaException("Could not create page.",
                            "The parent ACL ID [" +
                                    parentAclID +
                                    "] could not be found," +
                                    " while trying to create a new page.",
                            JahiaException.PAGE_ERROR,
                            JahiaException.ERROR_SEVERITY);
                }
                break;
            case JahiaPage.TYPE_URL:
                // Create a new ACL for the page/URL.
                acl = new JahiaBaseACL(parentAclID);
                break;
            case JahiaPage.TYPE_LINK:
                linkedPage =
                        lookupContentPage(pageLinkID, jParam.getEntryLoadRequest(), true);
                //acl = linkedPage.getACL ();
                acl = new JahiaBaseACL(parentAclID);
                break;
        }

        JahiaPageDefinition pageTemplate = null;
        switch (pageType) {
            case JahiaPageInfo.TYPE_DIRECT:
                pageTemplate = templateService.lookupPageTemplate(
                    pageTemplateID);
                break;

            case JahiaPageInfo.TYPE_LINK:
                pageTemplate = linkedPage.getPageTemplate(jParam);
                pageTemplateID = pageTemplate.getID();
                break;
        }

        /**
         * @todo FIXME we must test here if the page exists in other languages
         * before attempting to create it all the time. For the moment we just
         * added a boolean variable set to true but this value will have to
         * be determined by trying to load the page before creating it.
         */
        ContentPage contentPage = null;
        JahiaPage page = null;

        JahiaSite site = ServicesRegistry.getInstance()
                .getJahiaSitesService().getSite(siteID);
        /*
        int newVersionID = ServicesRegistry.getInstance()
                         .getJahiaVersionService().getCurrentVersionID();
        */
        int newVersionID = 0; // Create a staging entry so the version ID should be 0

        int newVersionStatus = VERSION_START_STATUS;
        // At this point, the page definition exist and has been instantiate,
        // as well as the ACL object could be created.
        // Now create the raw page info object

        String languageCode = null;
        if (jParam != null) {
            languageCode = jParam.getEntryLoadRequest().getFirstLocale(true).
                    toString();
        } else {
            languageCode = site.getLanguageSettingsAsLocales(true).get(0).
                    toString();
        }

        JahiaPageInfo pageInfo = new JahiaPageInfo(pageID, siteID, parentID,
                pageType, title, pageTemplateID, remoteURL, pageLinkID,
                acl.getID(), newVersionID,
                newVersionStatus,
                languageCode, pageManager);
        //jParam.getLocale().toString());
        List<JahiaPageInfo> pageInfoList = new ArrayList<JahiaPageInfo>();
        pageInfoList.add(pageInfo);

        // insert the page info into the database.
        try {
            pageManager.createPageInfo(pageInfo);
        } catch(Exception e) {
            throw new JahiaException("Could not create page.",
                    "Could not insert the page info into the database",
                    JahiaException.PAGE_ERROR,
                    JahiaException.CRITICAL_SEVERITY, e);
        }
        pageID = pageInfo.getID();
        // Let's create the page!!
        contentPage = new ContentPage(pageID, pageInfoList, acl);
        page =
                new JahiaPage(contentPage, pageTemplate, acl,
                        jParam.getEntryLoadRequest());

        WorkflowEvent theEvent = new WorkflowEvent (this, contentPage, jParam.getUser(), languageCode, false);
        ServicesRegistry.getInstance ().getJahiaEventService ().fireObjectChanged(theEvent);

        JahiaEvent objectCreatedEvent = new JahiaEvent(this, jParam, contentPage);
        ServicesRegistry.getInstance ().getJahiaEventService ().fireContentObjectCreated(objectCreatedEvent);

        // Reset child cache for this parent page
        mPageChildIDsCache.remove(new Integer(parentID));
        // log the page creation Event
        JahiaEvent theWfEvent = new JahiaEvent(this, jParam, page);
        ServicesRegistry.getInstance().getJahiaEventService().
                fireAddPage(theWfEvent);
        if (site.getHomePageID() == -1) {
            site.setHomePageID(page.getID());
            ServicesRegistry.getInstance().getJahiaSitesService().updateSite(site);
        }

        if (parentField != null) {
            parentField.setValue(Integer.toString(page.getID()));
            parentField.setObject(page);
            ServicesRegistry.getInstance ().getJahiaFieldService ().
                    saveField (parentField, -1, jParam);
        }

        return page;
    }

    /**
     * @param pageID
     * @param levelNb
     * @param jParams
     * @return
     * @throws JahiaException
     */
    public int findPageIDFromLevel(int pageID, int levelNb, ProcessingContext jParams)
            throws JahiaException {
        int levelCount = 0;
        int parentID = -1;
        ContentPage thePage = lookupContentPage(pageID, true);

        if (levelNb != -1) {
            while (levelCount < levelNb) {
                parentID = thePage.getParentID(jParams.getEntryLoadRequest());
                if (parentID > 0) {
                    thePage =
                            lookupContentPage(parentID, jParams.getEntryLoadRequest(), true);
                } else {
                    return -1;
                }
                levelCount++;
            }
        } else {
            while (parentID > 0) {
                parentID = thePage.getParentID(jParams.getEntryLoadRequest());
                if (parentID > 0) {
                    thePage = lookupContentPage(parentID, true);
                }
            }
        }

        return thePage.getID();
    }

    /**
     * @param siteID
     * @param loadFlag
     * @param jParam
     * @param user
     * @return
     * @throws JahiaException
     */
    public Iterator<JahiaPage> getAllPages(int siteID, int loadFlag, ProcessingContext jParam,
                                   JahiaUser user)
            throws JahiaException {

        List<JahiaPage> result = new ArrayList<JahiaPage>();

        // the user must be non null
        if (user != null) {

            // get all the pages IDs in the specified site.
            List<Integer> allPages = getPageIDsInSite(siteID);

            // for each page ID, lookup the page, and check if the page
            // matches the load flags and if the user is authorized to see it.
            for (Integer id : allPages) {            
                try {
                    if (id.intValue() > 0) {
                        JahiaPage page = lookupPage(id.intValue(),
                                jParam.getEntryLoadRequest(),
                                jParam.getOperationMode(),
                                jParam.getUser(), false);

                        if (page != null) {
                            if (authorizePageLoad(page, loadFlag, user)) {
                                result.add(page);
                            }
                        }
                    }
                    id = null;
                } catch (JahiaPageNotFoundException ex) {
                    // the page could not be found, ignore and don't add it
                    // into the resulting List.
                } catch (JahiaTemplateNotFoundException ex) {
                    // The page has an invalid page template, ignore and
                    // don't add it into the resulting List.
                } catch (JahiaException ex) {
                    // An error occured, just don't add the page into the
                    // resulting List.
                }
            }
        }
        return result.iterator();
    }

    /**
     * Returns an instance of the page service class
     *
     * @return the unique instance of this class
     */
    public static synchronized JahiaPageBaseService getInstance()
            throws JahiaException {
        if (instance == null) {
            instance = new JahiaPageBaseService();
        }
        return instance;
    }

    /**
     * Return a <code>List</code> of all the site IDs
     *
     * @return the List of all the site IDs
     * @throws JahiaException when a general failure occured
     */
    public List<Integer> getAllSiteIDs()
            throws JahiaException {

        List<Integer> sites = new ArrayList<Integer>();
        sites.add(new Integer(1));    // big fuckin' fake :) .... as you said !!! (Fulco)
        return sites;
    }

    /**
     * @param siteID
     * @return
     * @throws JahiaException
     */
    public List<Integer> getPageIDsInSite(int siteID)
            throws JahiaException {

        return pageManager.getPageIdsInSite(siteID);
    }

    /**
     * @param siteID
     * @return
     * @throws JahiaException
     */
    public List<Integer> getPageIdsInSiteOrderById(int siteID)
            throws JahiaException {

        return pageManager.getPageIdsInSiteOrderById(siteID);
    }

    //-------------------------------------------------------------------------
    public List<Integer> getPageIDsInSite(int siteID, int linkType)
            throws JahiaException {

        return pageManager.getPageIDsInSiteWithSpecifiedLink(siteID, linkType);
    }

    //-------------------------------------------------------------------------
    public List<Integer> getPageIDsWithTemplate(int templateID)
            throws JahiaException {

        return pageManager.getPageIDsWithTemplate(templateID);
    }
    
    /**
     * Retrieves all the page IDs that have the specified ACL-IDs
     *
     * @param aclIDs a Set of ACL-IDs
     *
     * @throws JahiaException Return this exception if any failure occurred.
     * @returns Returns a List of page IDs stored as Integers. 
     */
    public List<JahiaPageContentRights> getPageIDsWithAclIDs(Set<Integer> aclIDs) throws JahiaException {
        return pageManager.getPageIDsWithAclIDs(aclIDs);
    }            

    // -------------------------------------------------------------------------
    /**
     * Returns the page field id  that is parent of the given pageId.
     * The order is by workflows state from most staged to active.
     * Without marked for delete
     *
     * @param pageID int
     * @throws JahiaException
     * @return int
     */
    public int getPageFieldID(int pageID)
            throws JahiaException {
        return pageManager.getPageFieldID(pageID);
    }

    //-------------------------------------------------------------------------
    /**
     * Returns the active page field id that is parent of the given pageId.
     *
     * @param pageID int
     * @throws JahiaException
     * @return int
     */
    public int getActivePageFieldID (int pageID)
        throws JahiaException {
        return pageManager.getActivePageFieldID(pageID);
    }

    //-------------------------------------------------------------------------
    /**
     * Returns the staged page field id that is parent of the given pageId.
     *
     * @param pageID int
     * @throws JahiaException
     * @return int
     */
    public int getStagedPageFieldID (int pageID)
        throws JahiaException {
        return pageManager.getStagedPageFieldID(pageID);
    }

    /**
     * Returns all the different field of type page IDs in staging mode in a given page.
     *
     * @param  pageID the page for which to retrieve the field of type page
     * @return always returns a Set object, but it might be empty. If non
     * empty it contains Integer objects that represent the page IDs.
     * @throws JahiaException
     */
    public Set<Integer> getStagingPageFieldIDsInPage (int pageID)
        throws JahiaException {
        return pageManager.getStagingPageFieldIDsInPage (pageID);
    }

    /**
     * In case of a page move, a same page is pointed by both an active page field and a staged page field
     * This method return a set of theses pages fields.
     * The ids are sorted by workflows state ( most staged first )
     *
     * @param pageID int
     * @return List
     */
    public List<Integer> getStagingAndActivePageFieldIDs (int pageID)
        throws JahiaException {
        return pageManager.getStagingAndActivePageFieldIDs(pageID);
    }

    //-------------------------------------------------------------------------
    public List<JahiaPage> getPageChilds (int pageID, int loadFlag,
                                 EntryLoadRequest loadRequest)
            throws JahiaException {

        // let's retrieve the guest user first...
        JahiaUser user = ServicesRegistry.getInstance().
                              getJahiaUserManagerService()
                .lookupUser("guest");
        ProcessingContext jParams = Jahia.getThreadParamBean();
        if ( jParams != null ){
            user = jParams.getUser();
        }

        return getPageChilds(pageID, loadFlag, user, loadRequest);
    }

    public List<JahiaPage> getPageChilds(int pageID, int loadFlag, ProcessingContext jParam)
            throws JahiaException {
        if (jParam != null) {
            return getPageChilds(pageID, loadFlag, jParam.getUser(),
                                 jParam.getEntryLoadRequest());
        } else {
            logger.error(
                "FIXME : Method called with null ProcessingContext, returning null... ");
            return null;
        }
    }

    public List<JahiaPage> getPageChilds(int pageID, int loadFlag, JahiaUser user)
            throws JahiaException {
        // Suppose we have a site in French, using an arbitrary EntryLoadRequest.CURRENT doesn't work very well
        // because it request page in English. So we prefer
        ProcessingContext jParams = Jahia.getThreadParamBean();
        EntryLoadRequest loadRequest = null;
        if ( jParams != null ){
            loadRequest = jParams.getEntryLoadRequest();
        }
        return getPageChilds(pageID, loadFlag, user,loadRequest);
    }

    public List<JahiaPage> getPageChilds (int pageID, int loadFlag, JahiaUser user,
                                 EntryLoadRequest loadRequest)
            throws JahiaException {

        List<JahiaPage> childs = new ArrayList<JahiaPage>();

        boolean directPageOnly = (( loadFlag & ( PageLoadFlags.ALL |
                                                 PageLoadFlags.INTERNAL |
                                                 PageLoadFlags.JAHIA |
                                                 PageLoadFlags.LINKS |
                                                 PageLoadFlags.URL ) )==0 );

        List<ContentPage> contentPageChilds =
            getContentPageChilds(pageID,user,
                                 (ContentPage.STAGING_PAGE_INFOS | ContentPage.ACTIVE_PAGE_INFOS),
                                 ContentObject.SHARED_LANGUAGE,directPageOnly);

        // For each child page ID, get the page reference.
        for (Iterator<ContentPage> it = contentPageChilds.iterator(); it.hasNext();) {
            ContentPage contentPage = it.next();

            // try to get the page reference.
            try {
                JahiaPage page = lookupPage(contentPage.getID(), loadRequest, user);
                // if the page exists add it to the child list if it matches
                // the loading flag.
                if ((page != null) && (user != null)) {
                    if (authorizePageLoad(page, loadFlag, user)) {
                        childs.add(page);
                    }
                }
            } catch (JahiaPageNotFoundException ex) {
                // The page could not be found, don't add it into the resulting
                // List.
            } catch (JahiaTemplateNotFoundException ex) {
                // The page template could not be found, don't add it into the
                // resulting List.
            }
        }

        // sort pages by IDs
        Collections.sort(childs,new Comparator<JahiaPage>() {
            public int compare(JahiaPage o1, JahiaPage o2) {
                final int id1 = o1.getID();
                final int id2 = o2.getID();
                if(id1>id2) return 1;
                else if(id1 == id2) return 0;
                else return -1;
            }
        });
        return childs;
    }

    public int getPageChildrenCount(int pageID, int loadFlag, JahiaUser user)
            throws JahiaException {

        boolean directPageOnly = (( loadFlag & ( PageLoadFlags.ALL |
                                                 PageLoadFlags.INTERNAL |
                                                 PageLoadFlags.JAHIA |
                                                 PageLoadFlags.LINKS |
                                                 PageLoadFlags.URL ) )==0 );

        List<ContentPage> contentPageChilds = getContentPageChilds(pageID,user,
                                    (ContentPage.STAGING_PAGE_INFOS | ContentPage.ACTIVE_PAGE_INFOS),
                                    ContentObject.SHARED_LANGUAGE,directPageOnly);

        return contentPageChilds.size() ;
    }

    // TODO optimize method
    public boolean pageHasChildren(int pageID, int loadFlag, JahiaUser user)
            throws JahiaException {

        boolean directPageOnly = (( loadFlag & ( PageLoadFlags.ALL |
                                                 PageLoadFlags.INTERNAL |
                                                 PageLoadFlags.JAHIA |
                                                 PageLoadFlags.LINKS |
                                                 PageLoadFlags.URL ) )==0 );

        List<ContentPage> contentPageChilds = getContentPageChilds(pageID,user,
                                    (ContentPage.STAGING_PAGE_INFOS | ContentPage.ACTIVE_PAGE_INFOS),
                                    ContentObject.SHARED_LANGUAGE,directPageOnly);

        return contentPageChilds != null && contentPageChilds.size() > 0 ;
    }

    /**
     * Return an Iterator holding all the child PAGE(!) of the specified page.
     * This method checks the rights for a user and loads only the pages a user
     * is allowed to see.
     *
     * @param pageID        The source page ID
     * @param user          A JahiaUser object for which to check the rights on the pages
     * @param pageInfosFlag if Archived, return all
     * @param languageCode  if <code>null</code>, return all language
     * @return a List of JahiaPage objects that are the childs of this page
     * @throws JahiaException when problems while loading data from the persistent storage occured
     */
    public List<ContentPage> getDirectContentPageChilds(int pageID, JahiaUser user,
                                             int pageInfosFlag,
                                             String languageCode)
            throws JahiaException {
        List<ContentPage> childs = new ArrayList<ContentPage>();
        Integer intPageID = new Integer(pageID);
        List<Integer> childIDs = mPageChildIDsCache.get(intPageID);
        boolean isWithArchived = (pageInfosFlag & ContentPage.ARCHIVED_PAGE_INFOS) != 0;
        if (childIDs == null || isWithArchived) {
            // get all the child page IDs
            childIDs = pageManager.getPageChildIDs(pageID);
            // FIXME : NO SUPPORT FOR VERSIONING
            if(!isWithArchived)
             mPageChildIDsCache.put(intPageID, childIDs);
        }

        EntryLoadRequest loadRequest = null;
        List<Locale> langs = new ArrayList<Locale>();
        langs.add(EntryLoadRequest.SHARED_LANG_LOCALE);
        if (languageCode != null) {
            langs.add(
                org.jahia.utils.LanguageCodeConverters.languageCodeToLocale(
                languageCode));
        }
        if ((pageInfosFlag & ContentPage.STAGING_PAGE_INFOS) != 0) {
            loadRequest =
                new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE, 0,
                                     langs);
            loadRequest.setWithMarkedForDeletion(true);
        } else if ((pageInfosFlag & ContentPage.ACTIVE_PAGE_INFOS) != 0) {
            loadRequest =
                new EntryLoadRequest(EntryLoadRequest.ACTIVE_WORKFLOW_STATE, 0,
                                     langs);
            loadRequest.setWithMarkedForDeletion(true);
        } else if (isWithArchived) {
            loadRequest =
                new EntryLoadRequest(EntryLoadRequest.VERSIONED_WORKFLOW_STATE,
                                     0, langs);
        loadRequest.setWithDeleted(true);
        loadRequest.setWithMarkedForDeletion(true);
        }

        EntryLoadRequest activeLoadRequest =
            new EntryLoadRequest(EntryLoadRequest.ACTIVE_WORKFLOW_STATE, 0,
                                 langs);
        JahiaPageService jahiaPageService = ServicesRegistry.getInstance().
                        getJahiaPageService();
        // For each child page ID, get the page reference.
        for (Integer id : childIDs) {        
            // try to get the page reference.
            try {
                ContentPage pageContent = jahiaPageService.lookupContentPage(id.intValue(),
                                                            loadRequest,
                                                            false);
                if ((pageContent != null) && (user != null)) {
                    if (pageContent.getPageType(loadRequest) ==
                        PageInfoInterface.TYPE_DIRECT &&
                            pageContent.checkReadAccess(user)) {
                        if (pageID != pageContent.getParentID(activeLoadRequest)) {
                            continue;
                        }
                        if (pageInfosFlag == ContentPage.ARCHIVED_PAGE_INFOS) {
                            childs.add(pageContent);
                        } else {
                            JahiaPageInfo pageInfo = null;
                            if (languageCode != null &&
                                pageContent.hasEntries(pageInfosFlag,
                                    languageCode)) {
                                pageInfo =
                                        pageContent.getPageInfoVersion(loadRequest, false,
                                                false);
                            } else if (pageContent.hasEntries(pageInfosFlag)) {
                                pageInfo =
                                        pageContent.getPageInfoVersion(loadRequest, false,
                                                true);
                            }
                            if (pageInfo != null) {
                                childs.add(pageContent);
                            }
                        }
                    }
                }
            } catch (JahiaPageNotFoundException ex) {
                // The page could not be found, don't add it into the resulting
                // List.
            } catch (JahiaTemplateNotFoundException ex) {
                // The page template could not be found, don't add it into the
                // resulting List.
            }
        }
        childIDs = null;
        return childs;
    }

    /**
     * An implementation that support versioning !
     *
     * if pageInfosFlag is ContentPage.ACTIVE_PAGE_INFOS,
     * returns active child pages only
     *
     * if pageInfosFlag is ContentPage.STAGING_PAGE_INFOS,
     * returns staging and child pages ( returns pages that are marked for delete too )
     *
     * if pageInfosFlag is ContentPage.ARCHIVED_PAGE_INFOS,
     * returns child pages ( returns pages that are active or deleted too )
     *
     * if pageInfosFlag is ContentPage.ACTIVE_PAGE_INFOS | ContentPage.STAGING_PAGE_INFOS,
     * returns child pages ( returns pages that are active or deleted or only staged  )
     *
     * if pageInfosFlag is ContentPage.ARCHIVED_PAGE_INFOS | ContentPage.STAGING_PAGE_INFOS,
     * returns child pages ( returns pages that are active or deleted or only staged  )
     *
     * Return an Iterator holding all the child PAGE(!) of the specified page.
     * This method checks the rights for a user and loads only the pages a user
     * is allowed to see.
     *
     * @param pageID         the source page ID
     * @param user           a <code>JahiaUser</code> reference for which to check
     *                       the rights on the
     *                       pages
     * @param pageInfosFlag  if Archived, return all
     * @param languageCode   if <code>null</code>, return all language
     * @param versionId      used only if pageInfosFlag =  ContentPage.ARCHIVED_PAGE_INFOS
     * @param directPageOnly sould we return only direct page or not
     * @return a List of JahiaPage objects that are the childs of this page
     * @throws JahiaException thrown in the case we have problems while loading data from the persistent storage
     */
    public List<ContentPage> getContentPageChilds (int pageID,
                                        JahiaUser user,
                                        int pageInfosFlag,
                                        String languageCode,
                                        int versionId,
                                        boolean directPageOnly)
        throws JahiaException {
        List<ContentPage> childs = new ArrayList<ContentPage>();
        List<Integer> childIDs = new ArrayList<Integer>();

        if ( ((pageInfosFlag & ContentPage.STAGING_PAGE_INFOS) != 0)
           && ((pageInfosFlag & ContentPage.ACTIVE_PAGE_INFOS) != 0) ) {
            childIDs = pageManager.getStagingPageChildIDs(pageID);
            List<Integer> activeChildIDs = pageManager.getActivePageChildIDs(pageID);
            // add the deleted childs too
            Integer I = null;
            for (Iterator<Integer> it = activeChildIDs.iterator(); it.hasNext();){
                I = it.next();
                if ( !childIDs.contains(I) ){
                    int size2 = childIDs.size();
                    Integer J = null;
                    int pos = -1;
                    boolean bigger = false;
                    for ( int j=0; j<size2; j++ ){
                        J = childIDs.get(j);
                        bigger = false;
                        if ( I.intValue() > J.intValue() ){
                            pos = j;
                            bigger = true;
                        }
                        if ( pos != -1 && !bigger ){
                            break;
                        }
                    }
                    if ( pos != -1 ){
                        childIDs.add(pos+1,I);
                    } else {
                        childIDs.add(0,I);
                    }
                }
            }
        } else if ( ((pageInfosFlag & ContentPage.ARCHIVED_PAGE_INFOS) != 0)
               && ((pageInfosFlag & ContentPage.STAGING_PAGE_INFOS) != 0) ) {
            childIDs = pageManager.getVersioningPageChildIDs(pageID, versionId);
            List<Integer> stagingChildIDs = pageManager.getStagingPageChildIDs(pageID);
            // add the deleted childs too
            for (Integer I : stagingChildIDs){            
                if ( !childIDs.contains(I) ){
                    int size2 = childIDs.size();
                    Integer J = null;
                    int pos = -1;
                    boolean bigger = false;
                    for ( int j=0; j<size2; j++ ){
                        J = childIDs.get(j);
                        bigger = false;
                        if ( I.intValue() > J.intValue() ){
                            pos = j;
                            bigger = true;
                        }
                        if ( pos != -1 && !bigger ){
                            break;
                        }
                    }
                    if ( pos != -1 ){
                        childIDs.add(pos+1,I);
                    } else {
                        childIDs.add(0,I);
                    }
                }
            }
        } else if ( (pageInfosFlag & ContentPage.STAGING_PAGE_INFOS) != 0) {
            childIDs = pageManager.getStagingPageChildIDs(pageID);
        } else if ( (pageInfosFlag & ContentPage.ACTIVE_PAGE_INFOS) != 0) {
            childIDs = pageManager.getActivePageChildIDs(pageID);
        } else if ( (pageInfosFlag & ContentPage.ARCHIVED_PAGE_INFOS) != 0) {
            childIDs = pageManager.getVersioningPageChildIDs(pageID,versionId);
        }

        EntryLoadRequest loadRequest = null;
        List<Locale> langs = new ArrayList<Locale>();
        langs.add(EntryLoadRequest.SHARED_LANG_LOCALE);
        if (languageCode != null) {
            langs.add(
                org.jahia.utils.LanguageCodeConverters.languageCodeToLocale(
                languageCode));
        }
        if ( (pageInfosFlag & ContentPage.STAGING_PAGE_INFOS) != 0) {
            loadRequest =
                new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE, 0,
                                     langs);
            loadRequest.setWithMarkedForDeletion(true);
        } else if ( (pageInfosFlag & ContentPage.ACTIVE_PAGE_INFOS) != 0) {
            loadRequest =
                new EntryLoadRequest(EntryLoadRequest.ACTIVE_WORKFLOW_STATE, 0,
                                     langs);
            loadRequest.setWithMarkedForDeletion(true);
        } else if ( (pageInfosFlag & ContentPage.ARCHIVED_PAGE_INFOS) != 0) {
            loadRequest =
                new EntryLoadRequest(EntryLoadRequest.VERSIONED_WORKFLOW_STATE,
                                     0, langs);
            loadRequest.setWithDeleted(true);
            loadRequest.setWithMarkedForDeletion(true);
        }

/*
        ContentPage contentPage = null;
        while ( iterator.hasNext() ){
            contentPage = (ContentPage) iterator.next();

            // check page move
            EntryLoadRequest loadRequest = new EntryLoadRequest(EntryLoadRequest.VERSIONED_WORKFLOW_STATE,
                                                versionId, EntryLoadRequest.VERSIONED.getLocales());
        int parentId = childPage.getParentID(loadRequest);
*/

        // For each child page ID, get the page reference.
        for (Iterator<Integer> it = childIDs.iterator(); it.hasNext();){        
            Integer id = it.next();
            // try to get the page reference.
            try {

                ContentPage pageContent = ContentPage.getPage(id.intValue());

                // check if the page is currently moved
                if ( (pageInfosFlag == ContentPage.ACTIVE_PAGE_INFOS) ){
                    if ( pageID != pageContent.getParentID(loadRequest) ){
                        continue;
                    }
                }

                if ( (pageContent != null) && (user != null)) {
                    if ( (!directPageOnly ||
                          (directPageOnly && pageContent.getPageType(
                        loadRequest) == PageInfoInterface.TYPE_DIRECT)) &&
                        pageContent.checkReadAccess(user)) {
                       childs.add(pageContent);
                    }
                }
            } catch (JahiaPageNotFoundException ex) {
                // The page could not be found, don't add it into the resulting
                // List.
            } catch (JahiaTemplateNotFoundException ex) {
                // The page template could not be found, don't add it into the
                // resulting List.
            }
        }

        return childs;
    }

    /**
     * Return an Iterator holding all the child PAGE(!) of the specified page.
     * This method checks the rights for a user and loads only the pages a user
     * is allowed to see.
     *
     * @param pageID         the source page ID
     * @param user           a <code>JahiaUser</code> reference for which to check
     *                       the rights on the
     *                       pages
     * @param pageInfosFlag  if Archived, return all
     * @param languageCode   if <code>null</code>, return all language
     * @param directPageOnly sould we return only direct page or not
     * @return a List of JahiaPage objects that are the childs of this page
     * @throws JahiaException thrown in the case we have problems while loading data from the persistent storage
     */
    public List<ContentPage> getContentPageChilds (int pageID, JahiaUser user,
                                      int pageInfosFlag,
                                      String languageCode,
                                      boolean directPageOnly)
            throws JahiaException {
        if (logger.isDebugEnabled()) {
            logger.debug("getting page childs for page=" + pageID + ", loadFlags=" + pageInfosFlag + ", languageCode=" + languageCode + ", directPageOnly=" +  directPageOnly);
        }
        List<ContentPage> childs = new ArrayList<ContentPage>();
        // get all the child page IDs        
        List<Integer> childIDs = pageManager.getPageChildIDs(pageID);
        // FIXME : NO SUPPORT FOR VERSIONING
        // mPageChildIDsCache.put(intPageID, childIDs);

        EntryLoadRequest loadRequest = null;
        List<Locale> langs = new ArrayList<Locale>();
        langs.add(EntryLoadRequest.SHARED_LANG_LOCALE);
        if (languageCode != null) {
            langs.add(
                LanguageCodeConverters.languageCodeToLocale(
                languageCode));
        }
        if ((pageInfosFlag & ContentPage.STAGING_PAGE_INFOS) != 0) {
            loadRequest =
                new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE, 0,
                                     langs);
            loadRequest.setWithMarkedForDeletion(true);
        } else if ((pageInfosFlag & ContentPage.ACTIVE_PAGE_INFOS) != 0) {
            loadRequest =
                new EntryLoadRequest(EntryLoadRequest.ACTIVE_WORKFLOW_STATE, 0,
                                     langs);
            loadRequest.setWithMarkedForDeletion(true);
        } else if ((pageInfosFlag & ContentPage.ARCHIVED_PAGE_INFOS) != 0) {
            loadRequest =
                new EntryLoadRequest(EntryLoadRequest.VERSIONED_WORKFLOW_STATE,
                                     0, langs);
            loadRequest.setWithDeleted(true);
            loadRequest.setWithMarkedForDeletion(true);
        }

        // For each child page ID, get the page reference.
        for (Integer id : childIDs) {
            // try to get the page reference.
            try {

                /*
                ContentPage pageContent = ServicesRegistry.getInstance().
                        getJahiaPageService().lookupContentPage(id.intValue(), loadRequest, false);
                */
                ContentPage pageContent = ContentPage.getPage(id.intValue());

                // check if the page is currently moved
                if ( (pageInfosFlag == ContentPage.ACTIVE_PAGE_INFOS) ){
                    if ( pageID != pageContent.getParentID(loadRequest) ){
                        continue;
                    }
                }

                boolean writeAccess = false;
                if ( user != null ){
                    writeAccess = pageContent.checkWriteAccess(user);
                    if ( (!pageContent.isAvailable() || (pageInfosFlag == ContentPage.STAGING_PAGE_INFOS))
                        && !writeAccess ){
                       continue;
                    }
                }

                if ((pageContent != null) && (user != null)) {
                    if ( (!directPageOnly ||
                          (directPageOnly && pageContent.getPageType(
                            loadRequest) == PageInfoInterface.TYPE_DIRECT)) &&
                            (writeAccess || pageContent.checkReadAccess(user)) ) {
                        if (pageInfosFlag == ContentPage.ARCHIVED_PAGE_INFOS) {
                            childs.add(pageContent);
                        } else {
                            JahiaPageInfo pageInfo = null;
                            if (languageCode != null &&
                                pageContent.hasEntries(pageInfosFlag,
                                    languageCode)) {
                                pageInfo =
                                        pageContent.getPageInfoVersion(loadRequest, false,
                                                false);
                            } else if (pageContent.hasEntries(pageInfosFlag)) {
                                pageInfo =
                                        pageContent.getPageInfoVersion(loadRequest, false,
                                                true);
                            }
                            if (pageInfo != null) {
                                childs.add(pageContent);
                            }
                        }
                    }
                }
            } catch (JahiaPageNotFoundException ex) {
                // The page could not be found, don't add it into the resulting
                // List.
            } catch (JahiaTemplateNotFoundException ex) {
                // The page template could not be found, don't add it into the
                // resulting List.
            }
        }

        return childs;
    }

    /**
     * Should be used in place of getPagePath , use ContentPage instead of JahiaPage
     *
     * @param pageID
     * @param loadRequest
     * @param opMode
     * @param user
     * @return
     * @throws JahiaException
     */
    public List<ContentPage> getContentPagePath(int pageID, EntryLoadRequest loadRequest,
                                     String opMode, JahiaUser user)
            throws JahiaException {
        return getContentPagePath(pageID, loadRequest,
                opMode, user, PAGEPATH_SHOW_ALL);        
    }
    
    /**
     * Should be used in place of getPagePath , use ContentPage instead of JahiaPage
     *
     * @param pageID
     * @param loadRequest
     * @param opMode
     * @param user
     * @param command JahiaPageService.PAGEPATH_SHOW_ALL, JahiaPageService.PAGEPATH_BREAK_ON_RESTRICTED or JahiaPageService.PAGEPATH_REMOVE_RESTRICTED
     * @return
     * @throws JahiaException
     */
    public List<ContentPage> getContentPagePath(int pageID, EntryLoadRequest loadRequest,
                                     String opMode, JahiaUser user, int command)
            throws JahiaException {
//        GroupCacheKey groupCacheKey = new GroupCacheKey("PAGE_ID" + pageID
//                + "-" + loadRequest + "-" + opMode,
//                new HashSet());
//        if (mContentPagePathCache.containsKey(groupCacheKey)) {
//            List result = (List) mContentPagePathCache.get(groupCacheKey);
//            if (result != null) {
//                return modifyContentPagePath(result, command, loadRequest, opMode, user);
//            }
//        }
        List<ContentPage> path = new ArrayList<ContentPage>();
        ContentPage contentPage = lookupContentPage(pageID, true);
        List<Integer> ids = new ArrayList<Integer>();

        while (contentPage != null && !ids.contains(new Integer(contentPage.getID()))) {
            path.add(0, contentPage);
            ids.add(new Integer(contentPage.getID()));
//            groupCacheKey.getGroups().add("PAGE_ID" + contentPage.getID());
            
            // Coherence check here
            // if the loadRequest is active, but the page exist only in staging
            // ( for some reason, it was not actived ), so change the loadRequest to
            // staging ohterwhise calls to getType(), getJahiaID() will fail !!!!
            EntryLoadRequest newLoadRequest = loadRequest;
            if (loadRequest != null && loadRequest.isCurrent()
                    && !contentPage.hasActiveEntries()) {
                newLoadRequest = new EntryLoadRequest(
                        EntryLoadRequest.STAGING_WORKFLOW_STATE, 0, loadRequest
                                .getLocales());
                newLoadRequest.setWithDeleted(loadRequest.isWithDeleted());
                newLoadRequest.setWithMarkedForDeletion(loadRequest
                        .isWithMarkedForDeletion());
            }
            int parentId = contentPage.getParentID(newLoadRequest);
            contentPage = parentId > 0 ? lookupContentPage(parentId, true)
                    : null;
            if (contentPage != null && contentPage.getID() == pageID) { // Check to avoid infinite loop when creating cyclic page move
                contentPage = null;
            }
        }
//        mContentPagePathCache.put(groupCacheKey, path);
        path = modifyContentPagePath(path, command, loadRequest, opMode, user);
        
        return path;
    }


    /**
     * Retrieve the pids composing the path from the home page to the current page.
     *
     * @param jParams processing context
     * @return an ordered array of PIDs
     * @throws JahiaException retrieval failed
     */
    public int[] getCurrentPagePathAsPIDs(ProcessingContext jParams) throws JahiaException {
        List<ContentPage> pages = getContentPagePath(jParams.getPageID(), jParams) ;
        int[] path = new int[pages.size()] ;
        for (int i=0; i<pages.size(); i++) {
            path[i] = Integer.valueOf(pages.get(i).getPageID()) ;
        }
        return path ;
    }
    
    public List<ContentPage> modifyContentPagePath(List<ContentPage> path, int command,
            EntryLoadRequest loadRequest, String opMode, JahiaUser user) {

        if (command == PAGEPATH_BREAK_ON_RESTRICTED
                || command == PAGEPATH_REMOVE_RESTRICTED) {
            List<ContentPage> newPath = null;
            int pathSize = path.size();
            for (int index = pathSize; index > 0;) {
                ContentPage contentPage = path.get(--index);
                if (command == PAGEPATH_BREAK_ON_RESTRICTED
                        && !contentPage.isReachableByUser(loadRequest, opMode,
                                user)) {
                    if (index == pathSize - 1) {
                        newPath = new ArrayList<ContentPage>();
                    } else {
                        newPath = new ArrayList<ContentPage>(path.subList(index + 1, pathSize));
                    }
                    break;
                }

                if (command == PAGEPATH_REMOVE_RESTRICTED) {
                    if (!contentPage.isReachableByUser(loadRequest, opMode,
                            user)) {
                        if (index == pathSize - 1) {
                            newPath = new ArrayList<ContentPage>();
                        } else {
                            newPath = new ArrayList<ContentPage>(path.subList(index + 1,
                                    pathSize));
                        }
                    } else if (newPath != null) {
                        path.add(0, contentPage);
                    }
                }
            }
            if (newPath != null) {
                path = newPath;
            }
        }

        return path;
    }

    /**
     * Deprecated, use getContentPagePath
     *
     * @param pageID
     * @param loadRequest
     * @param opMode
     * @param user
     * @return
     * @throws JahiaException
     * @deprecated use getContentPagePath
     */
    public List<JahiaPage> getPagePath (int pageID, EntryLoadRequest loadRequest,
                               String opMode,
                               JahiaUser user)
            throws JahiaException {

        List<ContentPage> contentPagePath = getContentPagePath(pageID, loadRequest, opMode,
                user, JahiaPageService.PAGEPATH_BREAK_ON_RESTRICTED);
        List<JahiaPage> pagePath = new ArrayList<JahiaPage>();
        for (Iterator<ContentPage> iterator = contentPagePath.iterator(); iterator.hasNext();) {
            ContentPage contentPage = iterator.next();
            try {
                pagePath.add(contentPage.getPage(loadRequest, opMode, user));
            } catch (JahiaException ex) {
            	logger.warn("Cannot resolve JahiaPage for PagePath - rather use getContentPagePath", ex);
            }
        }
        return pagePath;
    }

    /**
     * List of contentPagePath
     *
     * @param pageID
     * @param jParams
     * @return
     * @throws JahiaException
     */
    public List<ContentPage> getContentPagePath(int pageID, ProcessingContext jParams)
            throws JahiaException {
        if (jParams != null) {
            return getContentPagePath(pageID, jParams.getEntryLoadRequest(),
                                      jParams.getOperationMode(),
                                      jParams.getUser());
        } else {
            logger.debug(
                "FIXME : Method called with null ProcessingContext, returning null");
            return null;
        }
    }

    /**
     * @param pageID
     * @param jParams
     * @return
     * @throws JahiaException
     * @deprecated used getContentPagePath
     */
    public List<JahiaPage> getPagePath(int pageID, ProcessingContext jParams)
            throws JahiaException {
        if (jParams != null) {
            return getPagePath(pageID, jParams.getEntryLoadRequest(),
                    jParams.getOperationMode(), jParams.getUser());
        } else {
            logger.debug(
                "FIXME : Method called with null ProcessingContext, returning null");
            return null;
        }
    }

    public List<JahiaPage> getPagesPointingOnPage(int pageID, ProcessingContext jParam)
            throws JahiaException {
        return getPagesPointingOnPage(pageID, jParam.getEntryLoadRequest());
    }

    public List<JahiaPage> getPagesPointingOnPage(int pageID,
                                         EntryLoadRequest loadRequest)
            throws JahiaException {

        List<JahiaPage> pages = new ArrayList<JahiaPage>();
        List<Integer> pageIDs = pageManager.getPageIDsPointingOnPage(pageID, loadRequest);
        
        for (int pID : pageIDs) {
            // get the page reference
            try {
                JahiaPage aPage = lookupPage(pID, loadRequest);

                // if the page exists, add it to the List
                if (aPage != null) {
                    if (aPage.getPageLinkID() != pageID) {
                        // this could happen if staging value and active
                        // value differ.
                        if (logger.isDebugEnabled()) {
                            logger.debug("Page link " + pID + " no longer points to page " + pageID);
                        }
                    } else {
                        pages.add(aPage);
                    }
                }
            } catch (JahiaPageNotFoundException ex) {
                logger.debug("Not returning page " + pID +
                             " for referring pages, error:", ex);
            } catch (JahiaTemplateNotFoundException ex) {
                logger.debug("Not returning page " + pID +
                             " for referring pages, error:", ex);
            }
        }

        return pages;
    }

    public List<JahiaPage> getPageSubTree(int pageID, int loadFlag, ProcessingContext jParam)
            throws JahiaException {

        List<JahiaPage> subTree = getPageChilds(pageID, loadFlag, jParam);
        List<JahiaPage> results = new ArrayList<JahiaPage>(subTree);
        for (Iterator<JahiaPage> it = subTree.iterator(); it.hasNext(); ) {
            JahiaPage aPage = it.next();
            results.addAll(getPageSubTree(aPage.getID(), loadFlag, jParam,pageID));
//            aPage = null;
        }

        return results;
    }

    protected List<JahiaPage> getPageSubTree(int pageID, int loadFlag, ProcessingContext jParam, int startPage)
            throws JahiaException {

        List<JahiaPage> subTree = getPageChilds(pageID, loadFlag, jParam);
        List<JahiaPage> results = new ArrayList<JahiaPage>(subTree);
        for (Iterator<JahiaPage> it = subTree.iterator(); it.hasNext(); ) {
            JahiaPage aPage = it.next();
            if ( aPage.getID() == startPage ){
                continue;
            }
            results.addAll(getPageSubTree(aPage.getID(), loadFlag, jParam, startPage));
//            aPage = null;
        }

        return results;
    }

    /**
     * Gets page-IDs of complete subtree under page not checked for any rights or workflow status
     *
     * @param pageID   Page unique identification number.
     *
     * @return Return a Set of page-IDs
     *
     * @throws JahiaException Return this exception if any failure occured.
     */
    public Set<Integer> getUncheckedPageSubTreeIDs(int pageID) throws JahiaException {
        return getUncheckedPageSubTreeIDs(pageID, false, 0);
    }
  
    
    /**
     * Gets page-IDs of complete subtree under page not checked for any rights or workflow status.
     *
     * @param pageID            Page unique identification number.
     * @param withoutDeleted    If true, deleted or "marked-for-delete" pages are not returned  
     * @param limitedReturnSize If the given size is reached (or exceeded) the method stops iterating 
     *                          through the child pages (0=unlimited)
     * 
     * @return Return a Set of page-IDs
     *
     * @throws JahiaException Return this exception if any failure occured.
     */    
    public Set<Integer> getUncheckedPageSubTreeIDs(int pageID, 
            boolean withoutDeleted, int limitedReturnSize) throws JahiaException {

        Set<Integer> results = new HashSet<Integer>(limitedReturnSize > 0 ? limitedReturnSize : 50);
        results = getUncheckedPageSubTreeIDs(pageID, withoutDeleted, 
                limitedReturnSize, results);
        
        return results;
    }

    protected Set<Integer> getUncheckedPageSubTreeIDs(int pageID, 
            boolean withoutDeleted, int limitedReturnSize, Set<Integer> results) throws JahiaException {

        List<Integer> childIDs = withoutDeleted ? pageManager
                .getNonDeletedPageChildIDs(pageID) : pageManager
                .getPageChildIDs(pageID);
        results.addAll(childIDs);
        
        for (Iterator<Integer> it = childIDs.iterator(); it.hasNext() && (limitedReturnSize == 0 || results.size() < limitedReturnSize);) {
            int aPageID = it.next().intValue();

            results = getUncheckedPageSubTreeIDs(aPageID, withoutDeleted, 
                    limitedReturnSize, results);
        }        
        
        return results;
    }    
    
    public synchronized void start()
        throws JahiaInitializationException {
        logger.debug("** Initializing the Page Service ...");
        // do not allow initialization when the service is still running
        // Initialize the pages cache
        logger.debug("   - Instanciate the page cache ...");

//        mPageInfosCache = cacheService.createCacheInstance(PAGE_INFO_CACHE);

//        mVersioningPageInfosCache = cacheService.createCacheInstance(
//            VERSIONING_PAGE_INFO_CACHE);

        mPageChildIDsCache = cacheService.createCacheInstance(
            PAGE_CHILD_CACHE);

        mContentPageCache = cacheService.createCacheInstance(
            CONTENT_PAGE_CACHE);

        // get the template service reference.
        logger.debug("   - Get the Page Template Service instance ...");

        if (templateService != null) {
            logger.debug("   ** Page Service successfully initialized!");
        } else {
            // invalidate the previous initializations
            templateService = null;

            // and raise an exception :(
            throw new JahiaInitializationException("Page Service initialization error.");
        }
    }

    /**
     * @param pageID
     * @return
     * @throws JahiaException
     * @throws JahiaPageNotFoundException
     * @throws JahiaTemplateNotFoundException
     * @deprecated use lookupPage(int, EntryLoadRequest) and be aware that 
     * EntryLoadRequest.CURRENT only covers the English locale
     */    
    public JahiaPage lookupPageWhitoutTemplates(int pageID)
            throws JahiaException,
            JahiaPageNotFoundException,
            JahiaTemplateNotFoundException {
        ProcessingContext jParams = Jahia.getThreadParamBean();
        List<Locale> locales = jParams != null ? jParams.getEntryLoadRequest().getLocales() : EntryLoadRequest.CURRENT
                .getLocales();
        EntryLoadRequest activeContentLoadRequest = new EntryLoadRequest(EntryLoadRequest.ACTIVE_WORKFLOW_STATE, 0,
                locales);        
        return lookupPage(pageID, activeContentLoadRequest, false);
    }

    /**
     * @param pageID
     * @return
     * @throws JahiaException
     * @throws JahiaPageNotFoundException
     * @throws JahiaTemplateNotFoundException
     * @deprecated use lookupPage(int, EntryLoadRequest) and be aware that 
     * EntryLoadRequest.CURRENT only covers the English locale
     */
    public JahiaPage lookupPage(int pageID)
            throws JahiaException,
            JahiaPageNotFoundException,
            JahiaTemplateNotFoundException {
        ProcessingContext jParams = Jahia.getThreadParamBean();
        List<Locale> locales = jParams != null ? jParams.getEntryLoadRequest().getLocales() : EntryLoadRequest.CURRENT
                .getLocales();
        EntryLoadRequest activeContentLoadRequest = new EntryLoadRequest(EntryLoadRequest.ACTIVE_WORKFLOW_STATE, 0,
                locales);
        return lookupPage(pageID, activeContentLoadRequest, true);
    }

    //-------------------------------------------------------------------------
    /**
     * @param pageID
     * @param jParam
     * @return
     * @throws JahiaException
     * @throws JahiaPageNotFoundException
     * @throws JahiaTemplateNotFoundException
     */
    public JahiaPage lookupPage(int pageID, ProcessingContext jParam)
            throws JahiaException,
            JahiaPageNotFoundException,
            JahiaTemplateNotFoundException {
        return lookupPage(pageID, jParam, true);
    }

    /**
     *
     * @param pageID int
     * @param loadRequest EntryLoadRequest
     * @param withTemplates boolean
     * @return JahiaPage
     * @throws JahiaException
     * @throws JahiaPageNotFoundException
     * @throws JahiaTemplateNotFoundException
     * @deprecated, use lookupPage (int pageID, EntryLoadRequest loadRequest,
                                 JahiaUser user, boolean withTemplates) instead, otherwize this is the site's default guest user that is used for permission access
     */
    public JahiaPage lookupPage(int pageID, EntryLoadRequest loadRequest,
                                boolean withTemplates)
            throws JahiaException,
            JahiaPageNotFoundException,
            JahiaTemplateNotFoundException {

        JahiaUser user = null;
        ProcessingContext jParams = Jahia.getThreadParamBean();
        if ( jParams != null ){
            user = jParams.getUser();
        }
        return lookupPage(pageID, loadRequest, user, withTemplates);
    }

    /**
     *
     * @param pageID int
     * @param loadRequest EntryLoadRequest
     * @param user JahiaUser
     * @param withTemplates boolean
     * @return JahiaPage
     * @throws JahiaException
     * @throws JahiaPageNotFoundException
     * @throws JahiaTemplateNotFoundException
     */
    public JahiaPage lookupPage (int pageID, EntryLoadRequest loadRequest,
                                 JahiaUser user, boolean withTemplates)
        throws JahiaException,
        JahiaPageNotFoundException,
        JahiaTemplateNotFoundException {
        String operationMode = null;
        if (loadRequest.getWorkflowState() ==
            EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
            operationMode = ProcessingContext.NORMAL;
        }
        if ( (loadRequest.getWorkflowState() >
              EntryLoadRequest.ACTIVE_WORKFLOW_STATE)
             || (loadRequest.getWorkflowState() < EntryLoadRequest.VERSIONED_WORKFLOW_STATE) ) {
            operationMode = ProcessingContext.EDIT;
        }
        return lookupPage(pageID, loadRequest, operationMode, user,
                          withTemplates);
    }

    /**
     *
     * @param pageID int
     * @param loadRequest EntryLoadRequest
     * @return JahiaPage
     * @throws JahiaException
     * @throws JahiaPageNotFoundException
     * @throws JahiaTemplateNotFoundException
     * @deprecated, use lookupPage (int pageID, EntryLoadRequest loadRequest, JahiaUser user) instead, otherwize this is the site's default guest user that is used for permission access
     */
    public JahiaPage lookupPage(int pageID, EntryLoadRequest loadRequest)
            throws JahiaException,
            JahiaPageNotFoundException,
            JahiaTemplateNotFoundException {
        return lookupPage(pageID, loadRequest, true);
    }

    /**
     *
     * @param pageID int
     * @param loadRequest EntryLoadRequest
     * @param user JahiaUser
     * @return JahiaPage
     * @throws JahiaException
     * @throws JahiaPageNotFoundException
     * @throws JahiaTemplateNotFoundException
     */
    public JahiaPage lookupPage (int pageID,
                                 EntryLoadRequest loadRequest,
                                 JahiaUser user)
        throws JahiaException,
        JahiaPageNotFoundException,
        JahiaTemplateNotFoundException {
        return lookupPage(pageID, loadRequest, user, true);
    }

    /**
     * Retrieves the page content object, that represents all the page versions
     * both active and staging. This object also contains all the multiple
     * language version of a page.
     *
     * @param pageID        the identifier of the page to load.
     * @param withTemplates specifies whether page templates should be also
     *                      loaded or not.
     * @return a ContentPage object.
     * @throws JahiaException
     * @throws JahiaPageNotFoundException
     * @throws JahiaTemplateNotFoundException
     */
    public ContentPage lookupContentPage(int pageID,
                                         boolean withTemplates)
            throws JahiaException,
            JahiaPageNotFoundException,
            JahiaTemplateNotFoundException {
        return lookupContentPage(pageID, EntryLoadRequest.CURRENT,
                                 withTemplates, false);
    }

    /**
     * Retrieves the page content object, that represents all the page versions
     * both active and staging. This object also contains all the multiple
     * language version of a page.
     *
     * @param pageID        the identifier of the page to load.
     * @param withTemplates specifies whether page templates should be also
     *                      loaded or not.
     * @param forceLoadFromDB  false if page can be read from cache                      
     * @return a ContentPage object.
     * @throws JahiaException
     * @throws JahiaPageNotFoundException
     * @throws JahiaTemplateNotFoundException
     */
    public ContentPage lookupContentPage (int pageID,
                                          boolean withTemplates, 
                                          boolean forceLoadFromDB)
        throws JahiaException,
        JahiaPageNotFoundException,
        JahiaTemplateNotFoundException {
        return lookupContentPage(pageID, EntryLoadRequest.CURRENT,
                                 withTemplates, forceLoadFromDB);
    }     
    
    /**
     * Retrieves the page content object, that represents all the page versions
     * both active and staging. This object also contains all the multiple
     * language version of a page.
     *
     * @param pageID        the identifier of the page to load.
     * @param loadRequest   specifies the EntryLoadRequest to specify which
     *                      contains the entry to be loaded, such as deleted, versioned (with
     *                      version ID, etc...)
     * @param withTemplates specifies whether page templates should be also
     *                      loaded or not.
     * @return a ContentPage object.
     * @throws JahiaException
     * @throws JahiaPageNotFoundException
     * @throws JahiaTemplateNotFoundException
     * @deprecated replaced by {@link #lookupContentPage(int,boolean)}
     */
    public ContentPage lookupContentPage(int pageID,
                                         EntryLoadRequest loadRequest,
                                         boolean withTemplates)
            throws JahiaException,
            JahiaPageNotFoundException,
            JahiaTemplateNotFoundException {
        return lookupContentPage(pageID, loadRequest,
                withTemplates, false);        
    }
    
    /**
     * Retrieves the page content object, that represents all the page
     * versions both active and staging. This object also contains all the
     * multiple language version of a page.
     * 
     * @param pageID
     *            the identifier of the page to load.
     * @param loadRequest
     *            specifies the EntryLoadRequest to specify which contains
     *            the entry to be loaded, such as deleted, versioned (with
     *            version ID, etc...)
     * @param withTemplates
     *            specifies whether page templates should be also loaded or
     *            not.
     * @param forceLoadFromDB
     *            false if page can be read from cache
     * @return a ContentPage object.
     * @throws JahiaException
     * @throws JahiaPageNotFoundException
     * @throws JahiaTemplateNotFoundException
     * @deprecated replaced by {@link #lookupContentPage(int,boolean,boolean)}
     */
    public ContentPage lookupContentPage(int pageID,
            EntryLoadRequest loadRequest, boolean withTemplates,
            boolean forceLoadFromDB) throws JahiaException,
            JahiaPageNotFoundException, JahiaTemplateNotFoundException {

        ContentPage contentPage = null;
        Integer entryKey = new Integer(pageID);
        
        if (!forceLoadFromDB) {
            contentPage = mContentPageCache.get(entryKey);
        }
        
        if (contentPage != null) {
            JahiaBaseACL acl = contentPage.getACL();
            contentPage.setACL(acl);
            return contentPage;
        }

        List<JahiaPageInfo> archivePageInfoList = new ArrayList<JahiaPageInfo>();
        List<JahiaPageInfo> activePageInfoList = lookupPageInfos(pageID,
                EntryLoadRequest.CURRENT);

        if (activePageInfoList.isEmpty()) {
            archivePageInfoList = lookupPageInfos(pageID, EntryLoadRequest.VERSIONED);
            if (archivePageInfoList.isEmpty()) {
                throw new JahiaPageNotFoundException(pageID);
            }
        }

        List<JahiaPageInfo> pageInfoList = new ArrayList<JahiaPageInfo>();
        pageInfoList.addAll(activePageInfoList);
        pageInfoList.addAll(archivePageInfoList);

        JahiaPageInfo sharedPageInfo = pageInfoList.get(0);
        //
        // //////////////////////////////////////////////////////////////////////////////////////
        // // FIXME -Fulco-
        // //
        // // this check should be removed the day we decide to make of links
        // real objects and
        // // not a special case of a page.
        // //
        // //////////////////////////////////////////////////////////////////////////////////////
        //
        // // If the page is of DIRECT type, try to get the page Template
        // // reference. If the page is not of this type, the page template
        // // is not needed and so the page template reference will be set to
        // null.
        // JahiaPageDefinition pageTemplate = null;
        //
        // if (withTemplates) { // AK. only if templates are requested.
        // int pageType = sharedPageInfo.getPageType();
        // if ((pageType == JahiaPageInfo.TYPE_DIRECT) ||
        // (pageType == JahiaPageInfo.TYPE_LINK)) {
        // pageTemplate = templateService.lookupPageTemplate(
        // sharedPageInfo.getPageTemplateID());
        // }
        // }

        // Get the ACL object associated to the page, if the ACL is not found,
        // report the error.
        JahiaBaseACL acl = new JahiaBaseACL(sharedPageInfo.getAclID());

        // Looks like everything is going well, let's create the page facade !
        contentPage = new ContentPage(pageID, pageInfoList, acl);
        if (contentPage.getPickedObject() != null) {
            acl = JahiaBaseACL.getACL(sharedPageInfo.getAclID());
            contentPage.setACL(acl);
        }
        mContentPageCache.put(entryKey, contentPage);
        return contentPage;
    }

    // -------------------------------------------------------------------------
    // AK 03.05.2001 lookup page whitout checks on templates... by passing new
    // boolean (false).
    public JahiaPage lookupPage(int pageID, EntryLoadRequest loadRequest,
                                String operationMode, JahiaUser user,
                                boolean withTemplates)
            throws JahiaException,
            JahiaPageNotFoundException,
            JahiaTemplateNotFoundException {

        ContentPage contentPage = lookupContentPage(pageID, withTemplates);
        //JahiaPage page = new JahiaPage (contentPage, contentPage.getPageTemplate(jParam), contentPage.getACL(), jParam);
        JahiaPage page = null;
        if (contentPage != null) {
            ProcessingContext context = Jahia.getThreadParamBean();
            if (context == null || !context.isFilterDisabled(CoreFilterNames.TIME_BASED_PUBLISHING_FILTER)) {
                if (ParamBean.NORMAL.equals(operationMode) && !contentPage.isAvailable()){
                    return null;
                } else if ( ParamBean.PREVIEW.equals(operationMode) ){
                    final TimeBasedPublishingService tbpServ = ServicesRegistry.getInstance()
                            .getTimeBasedPublishingService();
                    if (!tbpServ.isValid(contentPage.getObjectKey(),user,loadRequest,operationMode,
                            AdvPreviewSettings.getThreadLocaleInstance())){
                        return null;
                    }
                }
            }
            page = contentPage.getPage(loadRequest, operationMode, user);
        }
        return page;
    }

    public JahiaPage lookupPage (int pageID, ProcessingContext jParam,
                                 boolean withTemplates)
            throws JahiaException,
            JahiaPageNotFoundException,
            JahiaTemplateNotFoundException {
        if (jParam != null) {
            JahiaPage jahiaPage = jParam.getPage();
            if(jahiaPage==null || jahiaPage.getID()!=pageID) {
                jahiaPage = lookupPage(pageID, jParam.getEntryLoadRequest(),
                              jParam.getOperationMode(), jParam.getUser(),
                              withTemplates);
            }
            return jahiaPage;
        } else {
            logger.debug(
                "FIXME : Method called with null ProcessingContext, returning null");
            return null;
        }
    }

    public List<ContentPage> findPagesByPropertyNameAndValue(String name, String value) throws JahiaException {
        List<Integer> l = pageManager.findPageIdByPropertyNameAndValue(name, value);
        List<ContentPage> r = new ArrayList<ContentPage>();
        for (Iterator<Integer> iterator = l.iterator(); iterator.hasNext();) {
            Integer integer = iterator.next();
            r.add(lookupContentPage(integer.intValue(), false));
        }
        return r;
    }

    public List<Object[]> getPagePropertiesByName(String name) {
        return pageManager.getPagePropertiesByName(name);
    }

    //-------------------------------------------------------------------------
    // Return the List of the requested page infos. First try to extract
    // the info out of the cache, if not present extract if from the
    // database, and add it to the cache.
    // Return null if the page info doesn't exist.
    private List<JahiaPageInfo> lookupPageInfos(int pageID, EntryLoadRequest loadRequest) {
        // Get the raw page infos
        List<JahiaPageInfo> pageInfoList = pageManager.loadPageInfos(pageID, loadRequest);
        if (pageInfoList == null) {
            pageInfoList = new ArrayList<JahiaPageInfo>();
        }

        return pageInfoList;
    }

    // -------------------------------------------------------------------------
    public synchronized void stop() {
        //////////////////////////////////////////////////////////////////////////////////////
        // FIXME -Fulco- :
        //   before shutting down the service, a check should be done to know
        //   if a page has an update-lock active. If any active update-lock is
        //   active, the system can not be shutdown !
        //   If the shutdown process can be forced, a message should indicate the editing
        //   user the service is in shutdown process, as soon as he does an action.
        //////////////////////////////////////////////////////////////////////////////////////
        mPageChildIDsCache.flush();
    }



    //-------------------------------------------------------------------------
    // FH   2 May 2001
    // javadocs automaticaly imported.
    //
    public int getNbPages()
            throws JahiaException {
        return pageManager.getNbPages();
    }

    //-------------------------------------------------------------------------
    // FH   2 May 2001
    // javadocs automaticaly imported.
    //
    public int getNbPages(int siteID)
            throws JahiaException {
        return pageManager.getNbPages(siteID);
    }

    //-------------------------------------------------------------------------
    // FH   2 May 2001
    // javadocs automaticaly imported.
    //
    public int getRealActiveNbPages()
            throws JahiaException {
        return pageManager.getRealActiveNbPages();
    }

    //-------------------------------------------------------------------------
    // FH   2 May 2001
    // javadocs automaticaly imported.
    //
    public int getRealActiveNbPages(int siteID)
            throws JahiaException {
        return pageManager.getRealActiveNbPages(siteID);
    }

    // NK
    /**
     * returns a DOM representation of all pages of a site
     *
     * @param siteID
     */
    public JahiaDOMObject getPagesAsDOM (int siteID)
        throws JahiaException {

        return null;

    }

    // NK
    /**
     * Returns a List of all pages' Acl ID of this site
     * Need this for site extraction
     *
     * @param siteID
     */
    public List<Integer> getAclIDs(int siteID)
            throws JahiaException {
        return pageManager.getAllAclId(siteID);
    }

    public void invalidatePageCache(int pageID) {
        synchronized (mContentPageCache) {
            mPageChildIDsCache.remove(new Integer(pageID));
            // PagePropertyDB.getInstance().invalidateCacheForPage(pageID);
            mContentPageCache.remove(new Integer(pageID));
        }
    }

    /**
     * Return the map containing the page's properties
     *
     * @param pageID
     * @return map
     */
    public Map<String, PageProperty> getPageProperties(int pageID)
            throws JahiaException {
        return pageManager.getPageProperties(pageID);
        }

    /**
     * Looks up PageProperties by using the value. This is useful when we have
     * unique values and we want to lookup a page by a property value.
     * @param propertyValue String the value used to search the properties
     * @throws JahiaException thrown in case there was a problem communicating
     * with the database.
     * @return List returns a list of PageProperty objects that contain
     * the search value.
     */
    public List<PageProperty> getPagePropertiesByValue (String propertyValue)
        throws JahiaException {
        return pageManager.getPagePropertiesByValue(propertyValue);
    }

    public List<PageProperty> getPagePropertiesByValueAndSiteID (String propertyValue, int siteID)
        throws JahiaException {
        return pageManager.getPagePropertiesByValueAndSiteID(propertyValue,siteID);
    }

    public List<PageProperty> getPagePropertiesByNameValueSiteIDAndParentID (String propertyName, String propertyValue, int siteID, int parentPageID)
        throws JahiaException {
        return pageManager.getPagePropertiesByNameValueSiteIDAndParentID(propertyName, propertyValue, siteID, parentPageID);
    }
    /**
     * sort pages child first
     *
     * @param pageIDs List list of pageIDs
     * @throws JahiaException
     * @return List
     */
    public List<Integer> sortPages(List<Integer> pageIDs, EntryLoadRequest loadRequest,
                            JahiaUser user, String operationMode)
    throws JahiaException {
        // sort pages to activate childs first
        List<Integer> sortedPages = new ArrayList<Integer>();
        if ( pageIDs == null ){
            return sortedPages;
        }

        Map<Integer, List<ContentPage>> sortedPagePaths = new HashMap<Integer, List<ContentPage>>();

        boolean found = false;
        Iterator<Integer> iterator = pageIDs.iterator();
        int pos = -1;
        while (iterator.hasNext ()) {
            pos = -1;
            Integer pageId = iterator.next();
            int size = sortedPages.size();
            Integer I = null;
            for (int i = 0; i < size; i++) {
                I = sortedPages.get(i);
                List<ContentPage> thePath = sortedPagePaths.get(I);
                if ( thePath == null ){
                    thePath = ServicesRegistry.getInstance()
                        .getJahiaPageService().getContentPagePath(I.
                        intValue(),
                        loadRequest, operationMode, user);
                    if ( thePath != null ){
                        sortedPagePaths.put(I,thePath);
                    } else {
                        thePath = new ArrayList<ContentPage>();
                    }
                }
                Iterator<ContentPage> pageEnum = thePath.iterator();
                found = false;
                while (pageEnum.hasNext()) {
                    ContentPage aPage = pageEnum.next();
                    if (aPage.getID() == pageId.intValue()) {
                        pos = i;
                        found = true;
                        break;
                    }
                }
                if (pos != -1 && !found) {
                    break;
                }
            }
            if (pos == -1) {
                sortedPages.add(0,pageId);
            } else {
                sortedPages.add(pos+1,pageId);
            }
        }
        if ( sortedPages == null ){
            sortedPages = new ArrayList<Integer>();
        }
        return sortedPages;
    }

    public int getParentPageFieldId(int pageId,EntryLoadRequest loadRequest) {
        return pageManager.getPageFieldID(pageId, loadRequest);
    }

	public int getPageIDByURLKeyAndSiteID(String pageURLKey, int siteID) {
		return pageManager.getPageIDByURLKeyAndSiteID(pageURLKey, siteID);
	}

    public void loadPage(int pageID, ProcessingContext jParams) throws JahiaException {
        GetMethod method = null;
        /// Try again after page load
        try {
            logger.debug("Cannot find definitions, try to initialize page");
            URL url = new URL(org.jahia.settings.SettingsBean.getInstance().getLocalAccessUri() + jParams.composePageUrl(pageID));

            InputStream is = JahiaTools.makeJahiaRequest(url, jParams.getUser(), null, null, 5);
            is.close();
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Unsupported HTTP status code [500]")) {
                logger.error("Compilation error",e);
            } else {
                logger.error("Cant make a local request at "+org.jahia.settings.SettingsBean.getInstance().getLocalAccessUri()+", check your localAccessUri parameter ("+e.toString()+")");
            }
        }
        finally {
            if (method != null)
                method.releaseConnection();
        }
    }

    public Map<String, String> getVersions(int site, String lang) {
        return pageManager.getVersions(site, lang);
    }

}
