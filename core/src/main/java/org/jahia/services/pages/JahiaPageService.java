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

import org.jahia.content.TimeBasedPublishingState;
import org.jahia.data.JahiaDOMObject;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaPageNotFoundException;
import org.jahia.exceptions.JahiaTemplateNotFoundException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.JahiaService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;

import java.util.*;

/**
 * This interface defines all the methods a page service should implement, so that
 * it can be intergrated into Jahia.
 *
 * @version 2.0
 */
public abstract class JahiaPageService extends JahiaService
        implements TimeBasedPublishingState {

    // the Page Properties cache by page ID name.
    public static final String PAGE_CHILD_CACHE = "PageChildCache";
    
    public static final int PAGEPATH_SHOW_ALL = 0;
    public static final int PAGEPATH_REMOVE_RESTRICTED = 1;
    public static final int PAGEPATH_BREAK_ON_RESTRICTED = 2;    

    //-------------------------------------------------------------------------
    /**
     * Create a new page.
     *
     * @param siteID      The Jahia ID (site ID).
     * @param parentID    The parent page ID.
     * @param pageType    The page type (see constants in JahiaPage).
     * @param title       The page title.
     * @param pageDefID   The page defiNition ID.
     * @param creator     The creators nickname used by Jahia internally.
     * @param parentAclID The parent ACL ID.
     * @param jParam      The Jahia parameters.
     *
     * @return Return a new JahiaPage instanciated object.
     *
     * @throws JahiaException Throws this exception when any error occured in the page
     *                        creation process.
     */
    public abstract JahiaPage createPage (
            int siteID,
            int parentID,
            int pageType,
            String title,
            int pageDefID,
            String remoteURL,
            int pageLinkID,
            String creator,
            int parentAclID,
            ProcessingContext jParam)
            throws JahiaException;

    //-------------------------------------------------------------------------
    /**
     * Finds a page id from a page id, and going up a certain number of levels
     *
     * @param pageID  The page id
     * @param levelNb The number of levels to go up
     *
     * @return the page id if found, -1 if not found
     */
    public abstract int findPageIDFromLevel (int pageID, int levelNb,
                                             ProcessingContext jParams)
            throws JahiaException;

    //-------------------------------------------------------------------------
    /**
     * Get all pages of the specified "website".
     * If the submitted user is not null, then this method won't return
     * pages that the user can't read.
     *
     * @param siteID   The jahia site ID
     * @param loadFlag Mask of page types to be loaded. See
     *                 {@link org.jahia.services.pages.PageLoadFlags PageLoadFlags}
     *                 class constants for more informaion.
     * @param jParam   Jahia parameters. This parameter can be set to
     *                 null in case no dynamic context is needed.
     * @param user  The current user, to check rights
     *
     * @return a List of JahiaPage objects
     *
     * @throws JahiaException Return this exception if any failure occured.
     */
    public abstract Iterator<JahiaPage> getAllPages (
            int siteID,
            int loadFlag,
            ProcessingContext jParam,
            JahiaUser user)
            throws JahiaException;

    //-------------------------------------------------------------------------
    /**
     * Return all the site IDs.
     *
     * @return Return a List holding all the site IDs as Integers.
     */
    public abstract List<Integer> getAllSiteIDs ()
            throws JahiaException;

    //--------------------------------------------------------------------------
    /**
     * Gets all the page IDs of a specified site.
     *
     * @param siteID The jahia site ID.
     *
     * @return Return a valid List of page IDs stored as Integers. The List
     *         might be empty if no page was found.
     *
     * @throws JahiaException Return this exception if any failure occured.
     */
    public abstract List<Integer> getPageIDsInSite (int siteID)
            throws JahiaException;

    //--------------------------------------------------------------------------
    /**
     * Gets all the page IDs of a specified site. ordered by id
     *
     * @param siteID The jahia site ID.
     *
     * @return Return a valid List of page IDs stored as Integers. The List
     *         might be empty if no page was found.
     *
     * @throws JahiaException Return this exception if any failure occured.
     */
    public abstract List<Integer> getPageIdsInSiteOrderById(int siteID) throws JahiaException;

    //--------------------------------------------------------------------------
    /**
     * Get the jahia page id in a site corresponding to a specified link type.
     *
     * @param siteID   The jahia site ID.
     * @param linkType One of these TYPE_DIRECT, TYPE_LINK, TYPE_URL links.
     *
     * @return Return a valid List of page IDs stored as Integers. The List
     *         might be empty if no page was found.
     *
     * @throws JahiaException Return this exception if any failure occured.
     */
    public abstract List<Integer> getPageIDsInSite (int siteID, int linkType)
            throws JahiaException;


    //--------------------------------------------------------------------------
    /**
     * Retrieves all the page IDs that use the specified template
     *
     * @param templateID the identifier of the template we want the page IDs
     *                   for
     *
     * @throws JahiaException Return this exception if any failure occurred.
     * @returns Returns a List of page IDs stored as Integers. The List
     * might be empty if no page was found.
     */
    public abstract List<Integer> getPageIDsWithTemplate (int templateID)
            throws JahiaException;
    
    /**
     * Retrieves all the page IDs that have the specified ACL-IDs
     *
     * @param aclIDs a Set of ACL-IDs
     *
     * @throws JahiaException Return this exception if any failure occurred.
     * @returns Returns a List of page IDs stored as Integers. 
     */
    public abstract List<JahiaPageContentRights> getPageIDsWithAclIDs (Set<Integer> aclIDs)
            throws JahiaException;    

    //-------------------------------------------------------------------------
    /**
     * Returns the page field id  that is parent of the given pageId.
     * The order is by workflows state from most staged to active.
     * Without marked for delete
     *
     * @param pageID int
     * @throws JahiaException
     * @return int
     */
    public abstract int getPageFieldID (int pageID)
        throws JahiaException;

    //-------------------------------------------------------------------------
    /**
     * Returns the active page field id that is parent of the given pageId.
     *
     * @param pageID int
     * @throws JahiaException
     * @return int
     */
    public abstract int getActivePageFieldID (int pageID)
            throws JahiaException;

    //-------------------------------------------------------------------------
    /**
     * Returns the staged page field id that is parent of the given pageId.
     *
     * @param pageID int
     * @throws JahiaException
     * @return int
     */
    public abstract int getStagedPageFieldID (int pageID)
        throws JahiaException;

    /**
     * In case of a page move, a same page is pointed by both an active page field and a staged page field
     * This method return a set of theses pages fields.
     * The ids are sorted by workflows state ( most staged first )
     *
     * @param pageID int
     * @return List
     */
    public abstract List<Integer> getStagingAndActivePageFieldIDs (int pageID)
            throws JahiaException;

    /**
     * Returns all the different field of type page IDs in staging mode in a given page.
     *
     * @param  pageID the page for which to retrieve the field of type page
     * @return always returns a Set object, but it might be empty. If non
     * empty it contains Integer objects that represent the page IDs.
     * @throws JahiaException
     */
    public abstract Set<Integer> getStagingPageFieldIDsInPage (int pageID)
        throws
    JahiaException;

    //-------------------------------------------------------------------------
    /**
     * Return a List holding all the child pages of the specified page. The
     * loading flag filters the kind of pages to return.
     *
     * @param pageID   Page unique identification number.
     * @param loadFlag Mask of page types to be loaded. See
     *                 {@link PageLoadFlags PageLoadFlags}
     *                 class constants for more informaion.
     * @param jParam   Jahia parameters. This parameter can be set to
     *                 null in case no dynamic context is needed.
     *
     * @return Return a List of JahiaPage objects. The returned List is
     *         always non-null, but might have no pages if the specified page
     *         has not childs, or if no childs matching the loading flag were
     *         found.
     *
     * @throws JahiaException Return this exception if any failure occured.
     */
    public abstract List<JahiaPage> getPageChilds (
            int pageID,
            int loadFlag,
            ProcessingContext jParam)
            throws JahiaException;

    public abstract List<JahiaPage> getPageChilds (int pageID, int loadFlag,
                                          EntryLoadRequest loadRequest)
            throws JahiaException;

    //-------------------------------------------------------------------------
    /**
     * Return a List holding all the child pages of the specified page. The
     * loading flag filters the kind of pages to return.
     *
     * @param pageID   Page unique identification number.
     * @param loadFlag Mask of page types to be loaded. See
     *                 {@link PageLoadFlags PageLoadFlags}
     *                 class constants for more informaion.
     * @param user     Jahia User.
     *
     * @return Return a List of JahiaPage objects. The returned List is
     *         always non-null, but might have no pages if the specified page
     *         has not childs, or if no childs matching the loading flag were
     *         found.
     *
     * @throws JahiaException Return this exception if any failure occured.
     */
    public abstract List<JahiaPage> getPageChilds (
            int pageID,
            int loadFlag,
            JahiaUser user)
            throws JahiaException;

    /**
     * Return the number of subpages for a given JahiaPage. The returned value concerns only pages
     * visible to the current user.
     *
     * @param pageID page unique identificaiton number
     * @param loadFlag  mask of page types to include in the count
     * @param user the current user
     * @return the number of subpages
     * @throws JahiaException if any issue occured
     */
    public abstract int getPageChildrenCount(
            int pageID,
            int loadFlag,
            JahiaUser user)
            throws JahiaException;

    /**
     * Check if a given JahiaPage has children (only list pages available to the current user).
     *
     * @param pageID page unique identificaiton number
     * @param loadFlag  mask of page types to include in the count
     * @param user the current user
     * @return the number of subpages
     * @throws JahiaException if any issue occured
     */
    public abstract boolean pageHasChildren(
            int pageID,
            int loadFlag,
            JahiaUser user)
            throws JahiaException;

    /**
     * Return an Iterator holding all the child PAGE(!) of the specified page.
     * This method checks the rights for a user and loads only the pages a user
     * is allowed to see.
     *
     * @param pageID        The source page ID
     * @param user          A JahiaUser object for which to check the rights on the
     *                      pages
     * @param pageInfosFlag, if Archived, return all
     * @param languageCode, if null, return all language
     *
     * @return a List of ContentPage objects that are the childs of this
     *         page
     *
     * @throws JahiaException thrown in the case we have problems while loading
     *                        data from the persistent storage
     */
    public abstract List<ContentPage> getDirectContentPageChilds (int pageID,
        JahiaUser user,
                                                       int pageInfosFlag, String languageCode)
            throws JahiaException;

    /**
     * Return an Iterator holding all the child PAGE(!) of the specified page.
     * This method checks the rights for a user and loads only the pages a user
     * is allowed to see.
     *
     * @param pageID          The source page ID
     * @param user            A JahiaUser object for which to check the rights on the
     *                        pages
     * @return a List of ContentPage objects that are the childs of this
     *         page
     *
     * @throws JahiaException thrown in the case we have problems while loading
     *                        data from the persistent storage
     */
    public abstract List<ContentPage> getContentPageChilds (int pageID, JahiaUser user,
                                                 int pageInfosFlag,
                                                 String languageCode,
                                                 boolean directPageOnly)
            throws JahiaException;

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
     * if pageInfosFlag is ContentPage.ACTIVE_PAGE_INFOS & ContentPage.STAGING_PAGE_INFOS,
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
    public abstract List<ContentPage> getContentPageChilds (int pageID,
                                        JahiaUser user,
                                        int pageInfosFlag,
                                        String languageCode,
                                        int versionId,
                                        boolean directPageOnly) throws JahiaException;

    /**
     * Should be used in place of getPagePath , use ContentPage instead of JahiaPage
     *
     * @param pageID
     * @param loadRequest
     * @param opMode
     * @param user
     *
     * @return
     *
     * @throws JahiaException
     */
    public abstract List<ContentPage> getContentPagePath (int pageID,
                                               EntryLoadRequest loadRequest,
                                               String opMode, JahiaUser user)
            throws JahiaException;
    
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
    public abstract List<ContentPage> getContentPagePath(int pageID, EntryLoadRequest loadRequest,
                                     String opMode, JahiaUser user, int command)
            throws JahiaException;

    /**
     * Retrieve the pids composing the path from the home page to the specified page.
     * @param jParams processing context
     * @return an ordered array of PIDs
     * @throws JahiaException retrieval failed
     */
    public abstract int[] getCurrentPagePathAsPIDs(ProcessingContext jParams) throws JahiaException ;

    //-------------------------------------------------------------------------
    /**
     * Return the page path. The page path consist of all the parent pages of
     * the specified page until the site's root page.
     *
     * @param pageID The page id
     * @param jParams Jahia parameter reference. Specify null if to
     *               page context is needed.
     *
     * @return Return a valid List of JahiaPage objects defining the path.
     *         The first element will be the root page, and the specified
     *         page will be the last element in the List. In case the
     *         specified page is the root page, only this page will be
     *         present in the List.
     *
     * @throws JahiaException Return this exception if any failure occured.
     * @deprecated used getContentPagePath
     */
    public abstract List<JahiaPage> getPagePath (int pageID, ProcessingContext jParams)
            throws JahiaException;
    
    /**
    * @deprecated used getContentPagePath
    */    
    public abstract List<JahiaPage> getPagePath (int pageID,
                                        EntryLoadRequest loadRequest,
                                        String opMode, JahiaUser user)
            throws JahiaException;

    /**
     * List of contentPagePath
     *
     * @param pageID
     * @param jParams
     *
     * @return
     *
     * @throws JahiaException
     */
    public abstract List<ContentPage> getContentPagePath (int pageID, ProcessingContext jParams)
            throws JahiaException;

    //-------------------------------------------------------------------------
    /**
     * Return a list of pages pointing on the specified page. Pages can hold
     * the context information if needed.
     *
     * @param pageID the page ID on which the pages should point to.
     * @param jParam Jahia parameter reference. Specify null if to
     *               page context is needed.
     *
     * @return Return List of page objects
     *
     * @throws JahiaException Return this exception if any failure occured.
     */
    public abstract List<JahiaPage> getPagesPointingOnPage (int pageID, ProcessingContext jParam)
            throws JahiaException;

    /**
     * Return a list of pages pointing on the specified page.
     *
     * @param pageID      the page ID on which the pages should point to.
     * @param loadRequest the load request for which to load the pages.
     *
     * @return Return List of page objects
     *
     * @throws JahiaException Return this exception if any failure occured.
     */
    public abstract List<JahiaPage> getPagesPointingOnPage (int pageID,
                                                   EntryLoadRequest loadRequest)
            throws JahiaException;

    //-------------------------------------------------------------------------
    /**
     * Gets complete subtree under page.
     *
     * @param pageID   Page unique identification number.
     * @param loadFlag Mask of page types to be loaded. See
     *                 {@link PageLoadFlags PageLoadFlags}
     *                 class constants for more informaion.
     * @param jParam   Jahia parameters. This parameter can be set to null in case no
     *                 dynamic context is needed.
     *
     * @return Return a List of JahiaPage objects
     *
     * @throws JahiaException Return this exception if any failure occured.
     */
    public abstract List<JahiaPage> getPageSubTree (
            int pageID,
            int loadFlag,
            ProcessingContext jParam)
            throws JahiaException;
    
    /**
     * Gets page-IDs of complete subtree under page not checked for any rights or workflow status
     *
     * @param pageID   Page unique identification number.
     *
     * @return Return a Set of page-IDs
     *
     * @throws JahiaException Return this exception if any failure occured.
     */
    public abstract Set<Integer> getUncheckedPageSubTreeIDs(int pageID)
            throws JahiaException;

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
    public abstract Set<Integer> getUncheckedPageSubTreeIDs(int pageID, 
            boolean withoutDeleted, int limitedReturnSize) throws JahiaException;
    
    // -------------------------------------------------------------------------
    /**
     * Try to find the specified page. Create a new JahiaPage object, in
     * which will be included a reference to the raw page info, a reference
     * to the page definition associated to the page, a reference to the page's
     * ACL object and finally a reference to the current Jahia parameters.
     *
     * @param pageID The ID of the page to be looked up.
     * @param jParam Jahia parameters.
     *
     * @return Return a valid instance of a JahiaPage class. If the page does
     *         not exist, or any of its content is unavailable (ACL, page
     *         definition) then an exception will be thrown.
     *
     * @throws JahiaException             Throws this exception if any error occured in the lookup process.
     * @throws JahiaPageNotFoundException Throws this exception when the page could not be found in the
     *                                    database.
     */
    public abstract JahiaPage lookupPage (int pageID, ProcessingContext jParam)
            throws JahiaException,
            JahiaPageNotFoundException,
            JahiaTemplateNotFoundException;

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
    public abstract JahiaPage lookupPage (int pageID, EntryLoadRequest loadRequest,
                                 boolean withTemplates)
        throws JahiaException,
        JahiaPageNotFoundException,
        JahiaTemplateNotFoundException;

    /**
     *
     * @param pageID int
     * @param loadRequest EntryLoadRequest
     * @param user JahiaUser
     * @return JahiaPage
     */
    public abstract JahiaPage lookupPage(int pageID,
                                     EntryLoadRequest loadRequest,
                                     JahiaUser user,
                                     boolean withTemplates) throws
        JahiaException,
        JahiaPageNotFoundException,
        JahiaTemplateNotFoundException;

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
    public abstract JahiaPage lookupPage (int pageID,
                                          EntryLoadRequest loadRequest)
            throws JahiaException,
            JahiaPageNotFoundException,
            JahiaTemplateNotFoundException;

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
    public abstract JahiaPage lookupPage (int pageID,
                                          EntryLoadRequest loadRequest,
                                          JahiaUser user)
            throws JahiaException,
            JahiaPageNotFoundException,
            JahiaTemplateNotFoundException;

    public abstract JahiaPage lookupPage (int pageID,
                                          EntryLoadRequest loadRequest,
                                          String operationMode,
                                          JahiaUser user,
                                          boolean withTemplates)
            throws JahiaException,
            JahiaPageNotFoundException,
            JahiaTemplateNotFoundException;

    /**
     * Retrieves the page content object, that represents all the page versions
     * both active and staging. This object also contains all the multiple
     * language version of a page.
     *
     * @param pageID        the identifier of the page to load.
     * @param withTemplates specifies whether page templates should be also
     *                      loaded or not.
     *
     * @return a ContentPage object.
     *
     * @throws JahiaException
     * @throws JahiaPageNotFoundException
     * @throws JahiaTemplateNotFoundException
     */
    public abstract ContentPage lookupContentPage (int pageID,
        boolean withTemplates)
            throws JahiaException,
            JahiaPageNotFoundException,
            JahiaTemplateNotFoundException;

    /**
     * Retrieves the page content object, that represents all the page versions
     * both active and staging. This object also contains all the multiple
     * language version of a page.
     *
     * @param pageID        the identifier of the page to load.



     * @param withTemplates specifies whether page templates should be also
     *                      loaded or not.
     * @param forceLoadFromDB   false if page can be read from cache
     * 
     * @return a ContentPage object.
     *
     * @throws JahiaException
     * @throws JahiaPageNotFoundException
     * @throws JahiaTemplateNotFoundException
     */
    public abstract ContentPage lookupContentPage (int pageID,
        boolean withTemplates, boolean forceLoadFromDB)
            throws JahiaException,
            JahiaPageNotFoundException,
            JahiaTemplateNotFoundException;    
    
    
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
     *
     * @return a ContentPage object.
     *
     * @throws JahiaException
     * @throws JahiaPageNotFoundException
     * @throws JahiaTemplateNotFoundException
     */
    public abstract ContentPage lookupContentPage (int pageID,
                                                   EntryLoadRequest loadRequest,
                                                   boolean withTemplates)
            throws JahiaException,
            JahiaPageNotFoundException,
            JahiaTemplateNotFoundException;

    //-------------------------------------------------------------------------
    /**
     * Try to find the specified page. Create a new JahiaPage object, in
     * which will be included a reference to the raw page info, a reference
     * to the page definition associated to the page, a reference to the page's
     * ACL object and finally a reference to the current Jahia parameters.
     *
     * @param pageID The ID of the page to be looked up.
     *
     * @return Return a valid instance of a JahiaPage class. If the page does
     *         not exist, or any of its content is unavailable (ACL, page
     *         definition) then an exception will be thrown.
     *
     * @throws JahiaException             Throws this exception if any error occured in the lookup process.
     * @throws JahiaPageNotFoundException Throws this exception when the page could not be found in the
     *                                    database.
     */

    public abstract JahiaPage lookupPage (int pageID)
            throws JahiaException,
            JahiaPageNotFoundException,
            JahiaTemplateNotFoundException;

    /**
     * return a list of content pages matching a property name/value pair
     *
     * @param name property name
     * @param value property value
     *
     * @return return a ContentPage which is the clone of the page in parameter.
     */
    public abstract List<ContentPage> findPagesByPropertyNameAndValue(String name, String value) 
            throws JahiaException;

    public abstract List<Object[]> getPagePropertiesByName(String name);

    //-------------------------------------------------------------------------
    /**
     * Return the number of pages in the database.
     *
     * @return Return the number of pages.
     */
    public abstract int getNbPages ()
            throws JahiaException;

    //-------------------------------------------------------------------------
    /**
     * Return the number of pages in the database.
     *
     * @return Return the number of pages.
     */
    public abstract int getNbPages (int siteID)
            throws JahiaException;

    //-------------------------------------------------------------------------
    /**
     * Return the number of pages in the database.
     *
     * @return Return the number of pages.
     */
    public abstract int getRealActiveNbPages ()
            throws JahiaException;

    //-------------------------------------------------------------------------
    /**
     * Return the number of pages in the database.
     *
     * @return Return the number of pages.
     */
    public abstract int getRealActiveNbPages (int siteID)
            throws JahiaException;

    //--------------------------------------------------------------------------
    /**
     * returns a DOM representation of all pages of a site
     *
     * @param siteID
     *
     * @auhtor NK
     */
    public abstract JahiaDOMObject getPagesAsDOM (int siteID)
            throws JahiaException;

    //--------------------------------------------------------------------------
    /**
     * Returns a List of all Acl ID used by pages for a site
     * Need this for site extraction
     *
     * @auhtor NK
     */
    public abstract List<Integer> getAclIDs (int siteID)
            throws JahiaException;

    /**
     * Invalidate all page related cache info in this service
     *
     * @param pageID the identifier of the page for which to flush all page
     *               cached-info.
     */
    public abstract void invalidatePageCache (int pageID);

    /**
     * Return the map containing the page's properties
     *
     * @param pageID
     *
     * @return
     */
    public abstract Map<String, PageProperty> getPageProperties (int pageID)
            throws JahiaException;

    /**
     * Looks up PageProperties by using the value. This is useful when we have
     * unique values and we want to lookup a page by a property value.
     * @param propertyValue String the value used to search the properties
     * @throws JahiaException thrown in case there was a problem communicating
     * with the database.
     * @return List returns a list of PageProperty objects that contain
     * the search value.
     */
    public abstract List<PageProperty> getPagePropertiesByValue (String propertyValue)
        throws JahiaException;

    /**
     * sort pages child first
     *
     * @param pageIDs List list of pageIDs
     * @throws JahiaException
     * @return List
     */
    public abstract List<Integer> sortPages(List<Integer> pageIDs, EntryLoadRequest loadRequest,
                            JahiaUser user, String operationMode)
    throws JahiaException;

    public abstract List<PageProperty> getPagePropertiesByValueAndSiteID(String pageURLKey, int siteID) throws JahiaException;

    public abstract List<PageProperty> getPagePropertiesByNameValueSiteIDAndParentID (String propertyName, String propertyValue, int siteID, int parentPageID) throws JahiaException;

    public abstract int getParentPageFieldId(int pageId,EntryLoadRequest loadRequest);

    /**
	 * Returns the page ID with the specified URL key value for the given site.
	 * 
	 * @param pageURLKey
	 *            the page URL key value to search for
	 * @param siteID
	 *            the target site ID
	 * @return the page ID with the specified URL key value for the given site
	 */
	public abstract int getPageIDByURLKeyAndSiteID(String pageURLKey, int siteID);

    public abstract Map<String, String> getVersions(int site, String lang);

    public abstract void loadPage(int pageID, ProcessingContext jParams) throws JahiaException;    

}
