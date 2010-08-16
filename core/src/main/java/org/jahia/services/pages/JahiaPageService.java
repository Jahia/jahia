/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------

    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------

    //--------------------------------------------------------------------------


    //--------------------------------------------------------------------------

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

    //-------------------------------------------------------------------------

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

    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------

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

    //-------------------------------------------------------------------------

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

    //--------------------------------------------------------------------------

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

    public abstract void loadPage(int pageID, ProcessingContext jParams) throws JahiaException;

}
