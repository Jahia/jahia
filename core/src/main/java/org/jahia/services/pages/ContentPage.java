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


import org.jahia.bin.Jahia;
import org.jahia.content.*;
import org.jahia.content.events.ContentUndoStagingEvent;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerStructure;
import org.jahia.data.events.JahiaEvent;
import org.jahia.data.fields.LoadFlags;
import org.jahia.engines.EngineMessage;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaPageNotFoundException;
import org.jahia.exceptions.JahiaTemplateNotFoundException;
import org.jahia.hibernate.manager.JahiaPagesManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.hibernate.model.JahiaAcl;
import org.jahia.params.AdvPreviewSettings;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.ACLResourceInterface;
import org.jahia.services.acl.JahiaACLException;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.containers.JahiaContainersService;
import org.jahia.services.events.JahiaEventGeneratorBaseService;
import org.jahia.services.fields.ContentField;
import org.jahia.services.fields.ContentPageField;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.sites.SiteLanguageSettings;
import org.jahia.services.timebasedpublishing.TimeBasedPublishingService;
import org.jahia.services.usermanager.JahiaAdminUser;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.version.*;
import org.jahia.services.workflow.WorkflowEvent;
import org.jahia.services.workflow.WorkflowService;
import org.jahia.utils.LanguageCodeConverters;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;


/**
 * <p>Title: ContentPage - all the content for a page</p>
 * <p>Description: This class contains all the content in multiple languages
 * and multiple active versions for a given page. For a single language version
 * this is represented by JahiaPage.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 *
 * @author Serge Huber
 * @version 1.0
 *          todo FIXME no support for old version restoring exists in this class as
 *          of yet.
 */
public class ContentPage extends ContentObject implements
        PageInfoInterface, TimeBasedPublishingJahiaObject, ACLResourceInterface, Comparator<JahiaPage>, Serializable {

    private static final long serialVersionUID = 60613213284223604L;

    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(ContentPage.class);

    private Set<JahiaPageInfo> mPageInfos;
    private Map<String, JahiaPageInfo> mActivePageInfos;
    private Map<String, JahiaPageInfo> mStagingPageInfos;
    private Set<JahiaPageInfo> mArchivedPageInfos;
    private int aclID = -1;

    /**
     * Flag used to restore or not page's content ( fields, containers,...) *
     */
    private boolean mRestoreContent = true;

    private transient JahiaPagesManager pageManager;
    private transient JahiaVersionService versionService;
    private transient JahiaPageService pageService;

    private ContentObject parent;

    private Byte[] statusMapLock = new Byte[0];

    static {
        JahiaObject.registerType(ContentPageKey.PAGE_TYPE,
                ContentPage.class.getName());
    }

    public static ContentObject getChildInstance(String IDInType) {
        return getChildInstance(IDInType, false);
    }

    public static ContentObject getChildInstance(String IDInType, boolean forceLoadFromDB) {
        ContentObject result = null;
        try {
            result = getPage(Integer.parseInt(IDInType), true, forceLoadFromDB);
        } catch (JahiaException je) {
            logger.debug("Error retrieving page instance for id : " + IDInType, je);
        }
        return result;
    }

    //-------------------------------------------------------------------------

    /**
     * ContentPage constructor. This method is responsible for building
     * all the internal structure for the multilanguage and multiple staging
     * /active version data.
     *
     * @param pageInfoList a List of JahiaPageInfo object, that contain
     *                       all the database entries for the current page
     * @param acl            the JahiaBaseAcl object related to this page
     * @throws JahiaException thrown if there were errors while accessing
     *                        Jahia services (page and lock)
     */
    protected ContentPage(int pageID,
                          List<JahiaPageInfo> pageInfoList,
                          JahiaBaseACL acl)
            throws JahiaException {
        super(new ContentPageKey(pageID));

        if (pageInfoList == null) {
            throw new JahiaException("JahiaPage.constructor",
                    "Database object for page is inexistant ?",
                    JahiaException.PAGE_ERROR,
                    JahiaException.CRITICAL_SEVERITY);

        }
        buildInternalMaps(pageInfoList);

        if (acl != null) {
            aclID = acl.getID();
        }
        pageManager = (JahiaPagesManager) SpringContextSingleton.getInstance().getContext().getBean(
                JahiaPagesManager.class.getName());
    }

    /**
     * No arg constructor to properly support de-serialization
     */
    protected ContentPage() {
        pageManager = (JahiaPagesManager) SpringContextSingleton.getInstance().getContext().getBean(
                JahiaPagesManager.class.getName());
    }

    private void buildInternalMaps(List<JahiaPageInfo> pageInfoList) {
        synchronized (statusMapLock) {
            mPageInfos = new HashSet<JahiaPageInfo>(pageInfoList);
            rebuildStatusMaps();
        }
    }

    private Set<JahiaPageInfo> getPageInfos() {
        return mPageInfos;
    }

    private Map<String, JahiaPageInfo> getActivePageInfos() {
        return mActivePageInfos;
    }

    private Map<String, JahiaPageInfo> getStagingPageInfos() {
        return mStagingPageInfos;
    }

    private Set<JahiaPageInfo> getArchivedPageInfos() {
        return mArchivedPageInfos;
    }

    /**
     * //-------------------------------------------------------------------------
     * /**
     * Return the internal representation of the page information.
     *
     * @param withVersioned if true it specifies whether we should retrieve
     *                      all the page info entries including the versioned ones, if false it means
     *                      we only return the active and staged entries
     * @return Return the JahiaPageInfo object.
     */
    protected final List<JahiaPageInfo> getPageInfos(boolean withVersioned) {
        List<JahiaPageInfo> result = new ArrayList<JahiaPageInfo>();

        if (withVersioned) {
            loadVersioningEntryStates();
            result.addAll(getPageInfos());
        } else {
            result.addAll(getActivePageInfos().values());
            result.addAll(getStagingPageInfos().values());
        }
        return result;
    }

    private void addPageInfo(JahiaPageInfo newPageInfo) {
        if (newPageInfo != null) {
            synchronized (statusMapLock) {
                getPageInfos().add(newPageInfo);
                if (newPageInfo.getWorkflowState() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    getActivePageInfos().put(newPageInfo.getLanguageCode(),
                            newPageInfo);
                } else if (newPageInfo.getWorkflowState() >= EntryLoadRequest.STAGING_WORKFLOW_STATE) {
                    getStagingPageInfos().put(newPageInfo.getLanguageCode(),
                            newPageInfo);
                } else {
                    getArchivedPageInfos().add(newPageInfo);
                }
            }
        }
    }

    /*
    *
    * @throws JahiaException
    */
    private void loadVersioningEntryStates() {
        //if ( this.mArchivedPageInfos.size() == 0 ){
        if (getArchivedPageInfos() == null) {
            List<JahiaPageInfo> versionedPageInfos = pageManager.loadPageInfos(getID(), EntryLoadRequest.VERSIONED);
            if (!versionedPageInfos.isEmpty()) {
                synchronized (statusMapLock) {
                    getPageInfos().addAll(versionedPageInfos);
                    rebuildStatusMaps();
                }
            }
        }
    }

    /**
     * Removes an entry from the internal JahiaPageInfo List. Please note that
     * this method is far from fast as it goes through the entire PageInfo list
     * and then reconstructs the internal maps by parsing the list once again !
     * But throughout this design we have been using a pattern of fast reads/
     * slow writes so it should be acceptable.
     *
     * @param pageInfo the JahiaPageInfo object to remove from the list.
     */
    private void removePageInfo(JahiaPageInfo pageInfo) {
        if (pageInfo != null) {
            synchronized (statusMapLock) {
                getPageInfos().remove(pageInfo);
                if (pageInfo.getWorkflowState() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                    getActivePageInfos().remove(pageInfo.getLanguageCode());
                } else if (pageInfo.getWorkflowState() >= EntryLoadRequest.STAGING_WORKFLOW_STATE) {
                    getStagingPageInfos().remove(pageInfo.getLanguageCode());
                } else {
                    getArchivedPageInfos().remove(pageInfo);
                }
            }
        }
    }

    private void rebuildStatusMaps() {
        Map<String, JahiaPageInfo> newActivePageInfos = new HashMap<String, JahiaPageInfo>();
        Map<String, JahiaPageInfo> newStagingPageInfos = new HashMap<String, JahiaPageInfo>();
        Set<JahiaPageInfo> newArchivedPageInfos = new HashSet<JahiaPageInfo>();
        // small code to copy the List, not the List elements.
        for (JahiaPageInfo curPageInfo : getPageInfos()) {
            if (curPageInfo.getWorkflowState() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
                newActivePageInfos.put(curPageInfo.getLanguageCode(), curPageInfo);
            } else if (curPageInfo.getWorkflowState() >= EntryLoadRequest.STAGING_WORKFLOW_STATE) {
                newStagingPageInfos.put(curPageInfo.getLanguageCode(), curPageInfo);
            } else {
                newArchivedPageInfos.add(curPageInfo);
            }
        }
        synchronized (statusMapLock) {
            mActivePageInfos = newActivePageInfos;
            mStagingPageInfos = newStagingPageInfos;
            mArchivedPageInfos = newArchivedPageInfos;
        }
    }

    // -------------------------------------------------------------------------

    /**
     * Check if the Guest user of a site has read access.
     *
     * @param siteID the site id.
     * @return Return true if the site's guest user has read access for this page,
     *         or false in any other case.
     */
    public final boolean checkGuestAccess(int siteID) {
        // get the User Manager service instance.
        JahiaUserManagerService userMgr = ServicesRegistry.getInstance().
                getJahiaUserManagerService();
        if (userMgr == null)
            return false;

        JahiaUser theUser = userMgr.lookupUser(JahiaUserManagerService.GUEST_USERNAME);
        if (theUser == null)
            return false;

        return checkAccess(theUser, JahiaBaseACL.READ_RIGHTS, false);
    }

    /**
     * Check if the user has a specified access to the specified content object.
     *
     * @param user       Reference to the user.
     * @param permission One of READ_RIGHTS, WRITE_RIGHTS or ADMIN_RIGHTS permission
     *                   flag.
     * @return Return true if the user has the specified access to the specified
     *         object, or false in any other case.
     */
    public boolean checkAccess(JahiaUser user, int permission, boolean checkChilds) {
        boolean result = false;
        try {
            JahiaBaseACL localAcl = getACL();
            result = localAcl.getPermission(user, permission);
            if (!result && checkChilds) {
                List<Integer> aclList = new ArrayList<Integer>(1);
                aclList.add(localAcl.getACL().getId());
                result = isPermissionInChildAclOnPage(user, permission, aclList);
            }
        } catch (JahiaException ex) {
            logger.debug("Cannot load ACL ID " + getAclID(), ex);
        }
        return result;
    }

    /**
     * Check if the user has a specified access to the specified content object.
     *
     * @param user       Reference to the user.
     * @param permission One of READ_RIGHTS, WRITE_RIGHTS or ADMIN_RIGHTS permission
     *                   flag.
     * @return Return true if the user has the specified access to the specified
     *         object, or false in any other case.
     */
    public boolean checkAccess(JahiaUser user, int permission,
                               boolean checkChilds, boolean forceChildRights) {
        boolean allowed = true;
        boolean allPositive = true;
        try {
            JahiaBaseACL acl = getACL();
            allowed = acl.getPermission(user, permission);
            if (allowed && forceChildRights) {
                Map<Integer, Map<?,?>> deniedAclTree = new HashMap<Integer, Map<?,?>>();
                allPositive = checkAllChildPermissionsPositive(user,
                        permission, acl.getID(), deniedAclTree, new ArrayList<Integer>());
                if (!allPositive) {
                    Set<Integer> allAclIDs = new HashSet<Integer>();
                    allAclIDs.add(acl.getACL().getId());
                    Set<Integer> deniedAclIDs = new HashSet<Integer>();
                    getAllAclIdsFromMap(deniedAclTree, allAclIDs, deniedAclIDs);

                    Map<Integer, JahiaPageContentRights> children = convertPageListToMap(ServicesRegistry
                            .getInstance().getJahiaPageService()
                            .getPageIDsWithAclIDs(allAclIDs));
                    JahiaPageContentRights pageRights = (JahiaPageContentRights) children
                            .get(new Integer(getPageID()));
                    if (deniedAclIDs.contains(pageRights.getAclID())) {
                        allowed = false;
                    } else {
                        allowed = isNegativeAclInPageTree(children, pageRights,
                                deniedAclIDs);
                        if (allowed) {
                            allowed = isNegativeAclInListsOrContainersOrFields(children, pageRights, deniedAclIDs);
                        }
                    }
                }
            }
        } catch (JahiaException ex) {
            logger.debug("Cannot load ACL ID " + getAclID(), ex);
        }
        return allowed;
    }

    private boolean isNegativeAclInPageTree(Map<Integer, JahiaPageContentRights> children,
                                            JahiaPageContentRights currentPage, Set<Integer> deniedAclIDs) {
        boolean allowed = true;
        for (Iterator<Integer> it = currentPage.getChildrenPages().iterator(); it.hasNext()
                && allowed;) {
            JahiaPageContentRights pageRights = (JahiaPageContentRights) children
                    .get(it.next());
            if (deniedAclIDs.contains(pageRights.getAclID())) {
                allowed = false;
            } else {
                allowed = isNegativeAclInPageTree(children, pageRights,
                        deniedAclIDs);
            }
        }
        return allowed;
    }

    private boolean isNegativeAclInListsOrContainersOrFields(Map<Integer, JahiaPageContentRights> children,
                                                             JahiaPageContentRights currentPage, Set<Integer> deniedAclIDs) {
        Set<Integer> pageIDs = getAllPageIdsFromTree(children, currentPage,
                new HashSet<Integer>());
        JahiaContainersService containerService = ServicesRegistry
                .getInstance().getJahiaContainersService();
        boolean allowed = containerService
                .getContainerListIDsOnPagesHavingAcls(pageIDs, deniedAclIDs)
                .isEmpty();
        if (allowed) {
            allowed = containerService
                    .getContainerIDsOnPagesHavingAcls(pageIDs, deniedAclIDs)
                    .isEmpty();
        }
        if (allowed) {
            allowed = ServicesRegistry.getInstance().getJahiaFieldService()
                    .getFieldIDsOnPagesHavingAcls(pageIDs, deniedAclIDs)
                    .isEmpty();
        }
        return allowed;
    }

    private Set<Integer> getAllPageIdsFromTree(Map<Integer, JahiaPageContentRights> children,
                                               JahiaPageContentRights currentPage, Set<Integer> pageIDs) {
        pageIDs.add(currentPage.getPageID());

        for (Integer pageId : currentPage.getChildrenPages()) {
            JahiaPageContentRights pageRights = children.get(pageId);
            getAllPageIdsFromTree(children, pageRights, pageIDs);
        }
        return pageIDs;
    }

    private Map<Integer, JahiaPageContentRights> convertPageListToMap(List<JahiaPageContentRights> pages) {
        Map<Integer, JahiaPageContentRights> pageMap = new HashMap<Integer, JahiaPageContentRights>(pages.size());
        for (JahiaPageContentRights pageRights : pages) {
            pageMap.put(pageRights.getPageID(), pageRights);
        }
        for (JahiaPageContentRights pageRights : pages) {
            JahiaPageContentRights parentPageRights = pageMap.get(pageRights.getParentPageID());
            if (parentPageRights != null) {
                parentPageRights.getChildrenPages().add(pageRights.getPageID());
            }
        }
        return pageMap;
    }

    private void getAllAclIdsFromMap(Map<Integer, Map<?,?>> deniedAclTree, Set<Integer> allAclIds,
                                     Set<Integer> deniedAclIds) {

        allAclIds.addAll(deniedAclTree.keySet());
        for (Map.Entry<Integer, Map<?,?>> entry : deniedAclTree.entrySet()) {
            if (entry.getValue() == null) {
                deniedAclIds.add(entry.getKey());
            } else {
                getAllAclIdsFromMap((Map<Integer, Map<?,?>>)entry.getValue(), allAclIds,
                        deniedAclIds);
            }
        }
    }

    private boolean checkAllChildPermissionsPositive(JahiaUser user,
                                                     int permission, int parentAclId, Map<Integer, Map<?,?>> deniedAclTree, List<Integer> acls) {
        boolean allowed = true;
        acls.add(parentAclId);
        List<JahiaAcl> children = ServicesRegistry.getInstance().getJahiaACLManagerService().getChildAcls(parentAclId);
        for (JahiaAcl childAcl : children) {
            if (!acls.contains(childAcl.getAclID())) {
                boolean childAllowed = childAcl.getPermission(user, permission);
                Map<Integer, Map<?,?>> deniedChildrenAclTree = null;
                if (childAllowed) {
                    deniedChildrenAclTree = new HashMap<Integer, Map<?,?>>();
                    childAllowed = checkAllChildPermissionsPositive(user, permission,
                            childAcl.getAclID(), deniedChildrenAclTree, acls);
                }
                if (!childAllowed) {
                    allowed = false;
                    deniedAclTree.put(childAcl.getId(), deniedChildrenAclTree);
                }
            }
        }

        return allowed;
    }

    private boolean isPermissionInChildAclOnPage(JahiaUser user, int permission, List<Integer> parentAclIds) {
        boolean allowed = false;

        List<JahiaAcl> children = ServicesRegistry.getInstance().getJahiaACLManagerService().getChildAclsOnPage(parentAclIds,
                getPageID());
        List<Integer> aclIds = new ArrayList<Integer>(children.size());
        for (Iterator<JahiaAcl> it = children.iterator(); !allowed && it.hasNext();) {
            JahiaAcl childAcl = it.next();
            allowed = childAcl.getPermission(user, permission);
            aclIds.add(childAcl.getId());
        }
        if (!allowed && !aclIds.isEmpty()) {
            allowed = isPermissionInChildAclOnPage(user, permission, aclIds);
        }

        return allowed;
    }

    public void setACL(JahiaBaseACL mACL) {
        try {
            this.aclID = mACL.getID();
        } catch (JahiaACLException e) {
            logger.error("Cannot set ACL", e);
        }
    }

    /**
     * Flush all the caches corresponding to a page update (useful for example when we have
     * modified a page title that will be display on multiple other pages).
     *
     * @throws JahiaException raised if there were problems either updating
     *                        the persistant data or flushing the cache.
     */
    public synchronized void invalidateHtmlCache() throws JahiaException {
        // We need to check if there is still some page infos.
        // it could be empty when called this method after deleting a page
        // that exists only in staging
        if (!getPageInfos().isEmpty()) {
            // let's flush the cache of all the pages referencing this one.
            int siteID = getJahiaID();
            JahiaSite site = ServicesRegistry.getInstance()
                    .getJahiaSitesService().getSite(siteID);
            if (site == null) {
                logger.debug("Invalid site for page, cannot flush cache.");
            }
        }
    }

    //-------------------------------------------------------------------------

    /**
     * Commit the changes made in the page object to the database.
     *
     * @param flushCaches specifies whether we should flush all the caches
     *                    corresponding to this page update (useful for example when we have
     *                    modified a page title that will be display on multiple other pages).
     * @param fireEvent
     * @throws JahiaException raised if there were problems either updating
     *                        the persistant data or flushing the cache.
     *                        todo this is called even for a page counter update, can we avoid that
     *                        in the future ?
     */
    public synchronized void commitChanges(boolean flushCaches, boolean fireEvent, JahiaUser user)
            throws JahiaException {
        logger.debug("called.");
        if (flushCaches) {
            invalidateHtmlCache();
        }
        boolean templateChanged = false;
        for (JahiaPageInfo curInfo : getPageInfos()) {
            if (curInfo.hasChanged()) {
                templateChanged |= curInfo.hasTemplateChanged();
                curInfo.commitChanges();

                if (fireEvent) {
                    try {
                        WorkflowEvent theEvent = new WorkflowEvent(this, this, user, curInfo.getLanguageCode(), false);
                        ServicesRegistry.getInstance().getJahiaEventService().fireObjectChanged(theEvent);
                    } catch (JahiaException e) {
                        logger.warn("Exception while firing event", e);
                    }
                }
            }
        }

        getPageService().invalidatePageCache(getID());
        if (templateChanged && getPageType(EntryLoadRequest.STAGED) == JahiaPage.TYPE_DIRECT) {
            pageService.loadPage(getID(), Jahia.getThreadParamBean());
        }
    }

    /**
     * @param flushCaches
     * @param jParams
     * @throws JahiaException
     */
    public void commitChanges(boolean flushCaches, ProcessingContext jParams)
            throws JahiaException {
        if (jParams != null) {
            commitChanges(flushCaches, true, jParams.getUser());
        } else {
            logger.error("FIXME : Method called with null ProcessingContext, not executing...");
        }
    }

    //-------------------------------------------------------------------------

    /**
     * Return the page's ACL object.
     *
     * @return Return the page's ACL.
     */
    public final JahiaBaseACL getACL() {
        return super.getACL();
    }

    //-------------------------------------------------------------------------

    /**
     * Return the ACL unique identification number.
     *
     * @return Return the ACL ID.
     */
    public final int getAclID() {
        return aclID;
    }

    /**
     * Get the ACL id corresponding to the page workflow state. The ACL can
     * change between workflow state due to a page move or a type change. In
     * the case we get the ACL id from an ARCHIVED_PAGE_INFOS then the last
     * DIRECT page ACL is returned.
     *
     * @param pageInfosFlag Kind of page infos desired. This parameter can be
     *                      one of the constants ACTIVE_PAGE_INFOS, STAGING_PAGE_INFOS or
     *                      ARCHIVED_PAGE_INFOS.
     * @return The ACL id corresponding to the page workflow state.
     */
    public final int getAclID(int pageInfosFlag) {
        if ((pageInfosFlag & 0x01) != 0) {
            Iterator<JahiaPageInfo> it = getActivePageInfos().values().iterator();
            return it.hasNext() ? it.next().getAclID() : -1;
        } else if ((pageInfosFlag & 0x02) != 0) {
            Iterator<JahiaPageInfo> it = getStagingPageInfos().values().iterator();
            return it.hasNext() ? it.next().getAclID() : -1;
        } else if ((pageInfosFlag & 0x04) != 0) {
            // ensure to load versioning entries
            loadVersioningEntryStates();

            Iterator<JahiaPageInfo> it = getArchivedPageInfos().iterator();
            if (!it.hasNext()) {
                return -1;
            }

            int lastVersion = 0;
            int lastACL = -1;
            while (it.hasNext()) {
                JahiaPageInfo archivedPageInfo = it.next();
                int versionID = archivedPageInfo.getVersionID();
                if (versionID > lastVersion &&
                        archivedPageInfo.getPageType() == ContentPage.TYPE_DIRECT) {
                    lastVersion = versionID;
                    lastACL = archivedPageInfo.getAclID();
                }
            }
            return lastACL;
        }
        return -1;
    }

    //-------------------------------------------------------------------------

    //-------------------------------------------------------------------------

    /**
     * Return the site ID in which the page is located.
     *
     * @return Return the page site ID.
     */
    public final int getJahiaID() {
        JahiaPageInfo pageInfoVersion = getPageInfoVersion(null, false, true);
        int jahiaID;
        if (pageInfoVersion != null) {
            jahiaID = pageInfoVersion.getJahiaID();
        } else if (parent == null) {
            logger.warn("Parent is null, because it was never loaded ?");
            jahiaID = -1;
        } else {
            jahiaID = parent.getSiteID();
        }
        return jahiaID;
    }

    public final int getSiteID() {
        return getJahiaID();
    }

    //-------------------------------------------------------------------------

    /**
     * Returns the page definition (also known as template) for the current
     * ProcessingContext. (We ignore the language since templates are shared among
     * languages)
     *
     * @return a page definition according to the entry state we were trying
     *         to load.
     */
    public final JahiaPageDefinition getPageTemplate(EntryLoadRequest loadRequest) {
        JahiaPageDefinition pageDef = null;
        int pageDefID = getPageTemplateID(loadRequest);
        if (pageDefID != -1) {
            try {
                pageDef = ServicesRegistry.getInstance().getJahiaPageTemplateService().lookupPageTemplate(pageDefID);
            } catch (JahiaException je) {
                if (this.getPageType(loadRequest) != JahiaPage.TYPE_URL) {
                    logger.error("Error while loading page template : ", je);
                }
            }
        }
        return pageDef;
    }

    public final JahiaPageDefinition getPageTemplate(ProcessingContext jParams) {
        if (jParams != null) {
            return getPageTemplate(jParams.getEntryLoadRequest());
        } else {
            logger.error("FIXME : Method called with null ProcessingContext, returning null");
            return null;
        }
    }
    //-------------------------------------------------------------------------

    /**
     * Return the page definition ID.
     *
     * @param jParams a ProcessingContext object to be able to specify for which
     *                version/language to retrieve the value of this method.
     * @return Return the page definition ID.
     */
    public final int getPageTemplateID(ProcessingContext jParams) {
        if (jParams != null) {
            return getPageTemplateID(jParams.getEntryLoadRequest());
        } else {
            logger.error("FIXME : calling this method without parambean, returning -1");
            return -1;
        }
    }

    public final int getPageTemplateID(EntryLoadRequest loadRequest) {
        JahiaPageInfo pageInfo = getPageInfoVersion(loadRequest, false, false);
        if (pageInfo == null) {
            pageInfo = getPageInfoVersionIgnoreLanguage(loadRequest, false);
        }
        return pageInfo == null ? -1 : pageInfo.getPageTemplateID();
    }

    /**
     * Returns the identifier of the Content Definition for Content object.
     * For page, the definition is the Template Page
     *
     * @param loadRequest
     */
    public int getDefinitionID(EntryLoadRequest loadRequest) {
        return this.getPageTemplateID(loadRequest);
    }

    /**
     * Returns the Definition Key of the Content Definition for this Content object.
     * This is a PageDefinitionKey
     *
     * @param loadRequest
     */
    public ObjectKey getDefinitionKey(EntryLoadRequest loadRequest) {
        return new PageDefinitionKey(getDefinitionID(loadRequest));
    }

    //-------------------------------------------------------------------------

    /**
     * Return the internal jahia page ID in case the page is an internal
     * jahia link.
     *
     * @return Return the page link ID.
     */
    public final int getPageLinkID(EntryLoadRequest loadRequest) {
        JahiaPageInfo pageInfo = getPageInfoVersionIgnoreLanguage(loadRequest, false);
        return pageInfo == null ? -1 : pageInfo.getPageLinkID();
    }

    /**
     * @param jParams
     */
    public final int getPageLinkID(ProcessingContext jParams) {
        if (jParams != null) {
            return getPageLinkID(jParams.getEntryLoadRequest());
        } else {
            logger.error("FIXME : Method called with null ProcessingContext, returning -1");
            return -1;
        }
    }

    public static ContentPage getPage(int pageID, boolean withTemplate, boolean forceLoadFromDB)
            throws JahiaException {
        return ServicesRegistry.getInstance().getJahiaPageService()
                .lookupContentPage(pageID, withTemplate, forceLoadFromDB);
    }

    public static ContentPage getPage(int pageID, boolean withTemplate)
            throws JahiaException {
        return getPage(pageID, withTemplate, false);
    }

    public static ContentPage getPage(int pageID) throws JahiaException {
        return getPage(pageID, true, false);
    }

    /**
     * Returns a JahiaPage view object for the specified ProcessingContext (associated
     * with the current view mode, the state (moved or not), the current
     * language, etc...
     *
     * @return a JahiaPage object corresponding to the current ProcessingContext object,
     *         or null if the page is not available in this context (operation mode,
     *         ACLs, etc..)
     * @throws JahiaException if there was an error accessing the template
     *                        registry or the locks registry.
     */
    public JahiaPage getPage(EntryLoadRequest loadRequest, String operationMode,
                             JahiaUser user)
            throws JahiaException {
        JahiaPage page = new JahiaPage(this, getPageTemplate(loadRequest), getACL(), loadRequest);

        // Check for expired page
        ProcessingContext context = Jahia.getThreadParamBean();
        if (context == null || !context.isFilterDisabled(CoreFilterNames.TIME_BASED_PUBLISHING_FILTER)) {
            if (ParamBean.NORMAL.equals(operationMode) && !this.isAvailable()){
                return null;
            } else if ( ParamBean.PREVIEW.equals(operationMode) ){
                final TimeBasedPublishingService tbpServ = ServicesRegistry.getInstance()
                        .getTimeBasedPublishingService();
                if (!tbpServ.isValid(getObjectKey(),user,loadRequest,operationMode,
                        AdvPreviewSettings.getThreadLocaleInstance()) && (context.getPageID() != this.getID())){
                    return null;
                }
                /* @todo: preview mode should be done at a precise date not only what is published
                // as this is not fully supported yet actually no preview is supported at all for
                // page that are not available yet.
                /* @todo: if you change something here, perhaps it also needs to be changed in
                // the isReachableByUser method
                if ( ParamBean.PREVIEW.equals(operationMode)
                        && context.getPageID() == this.getID() ){
                    // in this case, we allow previsualisation of this page
                } else {
                    return null;
                }*/
            }
        }

        if ((loadRequest != null) && (user != null) && (operationMode != null)) {
            if (!page.checkReadAccess(user)) {
                return null;
            }

            if (operationMode.equals(ProcessingContext.NORMAL)) {
                // // do not allow null titles in normal mode
                if (page.getTitle() == null && !getSite().isMixLanguagesActive()) {
                    return null;
                } else {
                    if (loadRequest.getFirstLocale(true) != null && !page.hasEntry(ContentPage.ACTIVE_PAGE_INFOS, loadRequest.getFirstLocale(true).toString()) && !getSite().isMixLanguagesActive()) {
                        return null;
                    }
                }
            } else {
                boolean writeAccess = page.checkWriteAccess(user);

                if (!operationMode.equals(ProcessingContext.COMPARE)) {

                    // only return page in edit or preview mode if we have write
                    // access to it.
                    if (!writeAccess) {
                        if (isAvailable()) {
                            EntryLoadRequest activeLoadRequest = new EntryLoadRequest(
                                    EntryLoadRequest.ACTIVE_WORKFLOW_STATE, 0,
                                    loadRequest.getLocales());
                            if (getTitle(activeLoadRequest) == null && !getSite().isMixLanguagesActive()) {
                                return null;
                            }
                            page = new JahiaPage(this, this
                                    .getPageTemplate(activeLoadRequest), this
                                    .getACL(), activeLoadRequest);
                        } else {
                            return null;
                        }
                    }
                }
                // @todo complete TimeBased publishing with Preview mode
                /*
                 * if (operationMode.equals(ProcessingContext.PREVIEW) //&& !this.isAvailable() ) { ) { return null; }
                 */
                if (operationMode.equals(ProcessingContext.COMPARE)
                        && !this.isAvailable() && !writeAccess) {
                    return null;
                }
            }
        } else {
            logger.warn(
                    "No LoadRequest or User passed, assuming normal mode and using site guest user to check for read access");
            if (!this.checkGuestAccess(getJahiaID())) {
                return null;
            }
        }
        return page;
    }

    /**
     * @param jParams
     * @return
     * @throws JahiaException
     */
    public JahiaPage getPage(ProcessingContext jParams)
            throws JahiaException {
        if (jParams != null) {
            return getPage(jParams.getEntryLoadRequest(),
                    jParams.getOperationMode(), jParams.getUser());
        } else {
            return getPage(null, null, null);
        }
    }

    //-------------------------------------------------------------------------

    /**
     * Return the page type
     *
     * @return Return the page type, -1 if pageinfo doesn't exist for the requested entryload
     */
    public final int getPageType(EntryLoadRequest entryLoadRequest) {
        int pageType = -1;
        JahiaPageInfo pageInfo = getPageInfoVersionIgnoreLanguage(entryLoadRequest, false);
        if (pageInfo != null) {
            pageType = pageInfo.getPageType();
        } else if (entryLoadRequest != null && entryLoadRequest.isVersioned()) {
            // If there is no versioning entry, we should at least return the type
            // of this page as it exist
            try {
                Iterator<JahiaPageInfo> iterator = getPageInfos().iterator();
                if (iterator.hasNext()) {
                    pageType = iterator.next().getPageType();
                }
            } catch (Exception t) {
                logger.warn(t);
            }
        }
        return pageType;
    }

    //-------------------------------------------------------------------------

    /**
     * Return the parent page unique identification number.
     *
     * @return Return the parent page ID.
     */
    public final int getParentID(EntryLoadRequest loadRequest) {
        JahiaPageInfo pageInfo = getPageInfoVersionIgnoreLanguage(loadRequest, false);
        return pageInfo == null ? -1 : pageInfo.getParentID();
    }

    /**
     * @param jParams
     */
    public final int getParentID(ProcessingContext jParams) {
        if (jParams != null) {
            return getParentID(jParams.getEntryLoadRequest());
        } else {
            logger.error("FIXME : Method called with null ProcessingContext. Returning -1");
            return -1;
        }
    }

    // Define the same parent assertion.
    public static final int SAME_PARENT = -1;

    /**
     * Verify if the page has same parent between its activated version and
     * workflowed version. This case append especially when a page is moved.
     * In this case, the activated page is NOT changed but the workflowed versions
     * (will be created and) will have an other parents. This method test that.
     *
     * @return SAME_PARENT if page has same parents, otherwise return the ACTIVE parent
     *         page ID.
     */
    public int hasSameParentID() {
        if (getActivePageInfos().isEmpty() || getStagingPageInfos().isEmpty()) {
            return SAME_PARENT;
        }
        int activeParentID = getActivePageInfos().values().iterator().next().getParentID();

        int stagingParentID = activeParentID;
        for (JahiaPageInfo stagingPageInfo : getStagingPageInfos().values()) {
            if (stagingPageInfo.getVersionID()
                    != EntryLoadRequest.DELETED_WORKFLOW_STATE) {
                stagingParentID = stagingPageInfo.getParentID();
            }
        }

        return activeParentID == stagingParentID ? SAME_PARENT : activeParentID;
    }

    //-------------------------------------------------------------------------

    /**
     * Return the remote URL in case the page is an external reference (a non
     * Jahia page. If the page is not an external URL, "<no url>" is returned.
     *
     * @return Return the remote URL.
     */
    public final String getRemoteURL(EntryLoadRequest loadRequest) {
        String url;
        EntryLoadRequest lr = new EntryLoadRequest(loadRequest);
        lr.setWithDeleted(loadRequest.isWithDeleted());
        lr.setWithMarkedForDeletion(loadRequest.isWithMarkedForDeletion());
        lr.setCompareMode(loadRequest.isCompareMode());
        List<Locale> locales = new ArrayList<Locale>(loadRequest.getLocales());

        do {
            JahiaPageInfo pageInfo = getPageInfoVersion(lr, false, false);
            if (pageInfo == null) {
                return null;
            }
            url = pageInfo.getRemoteURL();

            if (!locales.isEmpty()) {
                locales.remove(lr.getFirstLocale(true));
                lr.setLocales(locales);
            }
        } while ((url == null || url.length() == 0) && lr.getFirstLocale(true) != null);

        return url;
    }

    /**
     * @param jParams
     */
    public final String getRemoteURL(ProcessingContext jParams) {
        if (jParams != null) {
            return getRemoteURL(jParams.getEntryLoadRequest());
        } else {
            logger.error("FIXME : Method called with null ProcessingContext, returning null");
            return null;
        }
    }

    public final Map<String, String> getRemoteURLs(boolean lastUpdated) {
        Map<String, String> remoteURLs = new HashMap<String, String>();
        for (Map.Entry<String, JahiaPageInfo> entry : getActivePageInfos().entrySet()) {
            remoteURLs.put(entry.getKey(), entry.getValue().getRemoteURL());
        }
        if (lastUpdated) {
            for (Map.Entry<String, JahiaPageInfo> entry : getStagingPageInfos().entrySet()) {
                String remote = entry.getValue().getRemoteURL();
                if (remote != null && remote.length() > 0) {
                    remoteURLs.put(entry.getKey(), remote);
                }
            }
        }
        return remoteURLs;

    }

    //-------------------------------------------------------------------------

    /**
     * Return the page title.
     *
     * @return Return the page title.
     */
    public final String getTitle(EntryLoadRequest loadRequest) {
        return getTitle(loadRequest, true);
    }

    /**
     * Returns the page title for the specified EntryLoadRequest, eventually
     * encoding it for HTML output.
     *
     * @param loadRequest   EntryLoadRequest
     * @param htmlCompliant boolean
     * @return String
     */
    public final String getTitle(EntryLoadRequest loadRequest, boolean htmlCompliant) {
        String title;
        EntryLoadRequest lr = new EntryLoadRequest(loadRequest);
        lr.setWithDeleted(loadRequest.isWithDeleted());
        lr.setWithMarkedForDeletion(loadRequest.isWithMarkedForDeletion());
        lr.setCompareMode(loadRequest.isCompareMode());
        List<Locale> locales = new ArrayList<Locale>(loadRequest.getLocales());

        do {
            JahiaPageInfo pageInfo = getPageInfoVersion(lr, false, false);
            if (pageInfo == null) {
                return null;
            }
            title = pageInfo.getTitle();

            if (!locales.isEmpty()) {
                locales.remove(lr.getFirstLocale(true));
                lr.setLocales(locales);
            }
        } while ((title == null || title.length() == 0) && lr.getFirstLocale(true) != null);

        return title;
    }

    /**
     * @param jParams
     */
    public final String getTitle(ProcessingContext jParams) {
        if (jParams != null) {
            return getTitle(jParams.getEntryLoadRequest());
        } else {
            logger.error("FIXME : Method called with null ProcessingContext, returning null");
            return null;
        }
    }

    //-------------------------------------------------------------------------

    /**
     * Increments the page counter for this page.
     */
    public final void incrementCounter(EntryLoadRequest loadRequest) {
        // getPageInfoVersion(jParams, false).incrementCounter ();
        try {
            int pageCounter = 0;
            PageProperty counterProperty = getPageLocalProperty("counter");
            if (counterProperty != null) {
                String counterStr = counterProperty.getValue();
                if (counterStr != null) {
                    pageCounter = Integer.parseInt(counterStr);
                }
            }
            if (pageCounter != -1) {
                setProperty("counter", Integer.toString(pageCounter + 1));
            }
        } catch (JahiaException je) {
            logger.warn(je, je);
        }
    }

    public final void incrementCounter(ProcessingContext jParams) {
        if (jParams != null) {
            incrementCounter(jParams.getEntryLoadRequest());
        } else {
            logger.error("FIXME : Method called with null ProcessingContext, aborting...");
        }
    }

    //-------------------------------------------------------------------------

    /**
     * Set the new page defintion ID. The ID must point to a existing page
     * definition.
     *
     * @param value The new page defintion ID.
     * @throws JahiaException                 Throw this exception on any error. Only ERROR type error should
     *                                        be catched, all the other failures should be thrown further.
     * @throws JahiaTemplateNotFoundException raised in case the template
     *                                        is not found.
     */
    public boolean setPageTemplateID(int value, EntryLoadRequest loadRequest)
            throws JahiaException,
            JahiaTemplateNotFoundException {
        boolean changed = false;
        // check if the specified definition exists
        ServicesRegistry services = ServicesRegistry.getInstance();
        if (services == null) {
            throw new JahiaException("Services registry error.",
                    "Could not access the Services Registry!",
                    JahiaException.SERVICE_ERROR, JahiaException.CRITICAL_SEVERITY);
        }

        if (getPageTemplateID(loadRequest) != value && value != -1) {
            JahiaPageTemplateService templateService = services.getJahiaPageTemplateService();
            if (templateService != null) {
                templateService.lookupPageTemplate(value);
                JahiaUser user = Jahia.getThreadParamBean() != null ? Jahia.getThreadParamBean().getUser() : null;
                for (JahiaPageInfo curPageInfo : getAllPageInfosForWrite()) {
                    // update the new definition ID in the page infos.
                    curPageInfo.setPageType(TYPE_DIRECT);
                    curPageInfo.setPageTemplateID(value);
                    WorkflowEvent theEvent = new WorkflowEvent(this, this, user, curPageInfo.getLanguageCode(), false);
                    ServicesRegistry.getInstance().getJahiaEventService().fireObjectChanged(theEvent);
                    changed = true;
                }
            } else {
                throw new JahiaException("Page templates services error.",
                        "Could not access the Page Templates Services.", JahiaException.SERVICE_ERROR,
                        JahiaException.CRITICAL_SEVERITY);
            }
        }
        return changed;
    }

    /**
     * @param value
     * @param jParams
     * @throws JahiaException
     * @throws JahiaTemplateNotFoundException
     */
    public void setPageTemplateID(int value, ProcessingContext jParams)
            throws JahiaException,
            JahiaTemplateNotFoundException {
        if (jParams != null) {
            setPageTemplateID(value, jParams.getEntryLoadRequest());
        } else {
            logger.error("FIXME : Method called with null ProcessingContext, aborting...");
        }
    }


    //-------------------------------------------------------------------------
    public void setPageTemplate(JahiaPageDefinition value, EntryLoadRequest loadRequest)
            throws JahiaException {
        if (value != null) {
            for (JahiaPageInfo curPageInfo : getAllPageInfosForWrite()) {
                curPageInfo.setPageType(TYPE_DIRECT);
                curPageInfo.setPageTemplateID(value.getID());
            }
        }
    }

    /**
     * @param value
     * @param jParams
     * @throws JahiaException
     */
    public void setPageTemplate(JahiaPageDefinition value, ProcessingContext jParams)
            throws JahiaException {
        if (jParams != null) {
            setPageTemplate(value, jParams.getEntryLoadRequest());
        } else {
            logger.error("FIXME : Method called with null ProcessingContext, aborting...");
        }
    }

    //-------------------------------------------------------------------------

    /**
     * Set the new internal link ID. This ID must be an existing page ID.
     *
     * @param value The new page link ID.
     */
    public final void setPageLinkID(int value, EntryLoadRequest loadRequest) {

        try {
            JahiaPage tempPage = getPageService().lookupPage(value, loadRequest);
            if (tempPage != null) {
                for (JahiaPageInfo curPageInfo : getAllPageInfosForWrite()) {
                    curPageInfo.setPageTemplateID(tempPage.getPageTemplateID());
                    curPageInfo.setPageType(TYPE_LINK);
                    curPageInfo.setPageLinkID(value);
                }
            }
        } catch (JahiaException ex) {
            logger.warn("Error while setting page link ID", ex);
        }
    }

    /**
     * @param value
     * @param jParams
     */
    public final void setPageLinkID(int value, ProcessingContext jParams) {
        if (jParams != null) {
            setPageLinkID(value, jParams.getEntryLoadRequest());
        } else {
            logger.error("FIXME : Method called with null ProcessingContext, aborting...");
        }
    }

    //-------------------------------------------------------------------------

    /**
     * Change the page type. By changing this information, be aware to change
     * also the according remote URL or page link ID information. See the
     * methods {@link #setPageLinkID setPageLinkID()} and
     * {@link #setRemoteURL setRemoteURL()}.
     *
     * @param value The new page type.
     */
    public final void setPageType(int value, EntryLoadRequest loadRequest) {
        for (JahiaPageInfo curPageInfo : getAllPageInfosForWrite()) {
            curPageInfo.setPageType(value);
        }
    }

    /**
     * @param value
     * @param jParams
     */
    public final void setPageType(int value, ProcessingContext jParams) {
        if (jParams != null) {
            setPageType(value, jParams.getEntryLoadRequest());
        } else {
            logger.error("FIXME : Method called with null ProcessingContext, aborting...");
        }
    }

    //-------------------------------------------------------------------------

    /**
     * Set the new parent ID. This method should be used carefully, all the
     * subtree pages will move with the page. This method is used to move a
     * page. Basically it marks the old position for deletion, or really
     * deletes it if in only existed in staging.
     *
     * @param value   The new parent page ID.
     * @param jParams a ProcessingContext object used to specify for which version/
     *                language we are setting the ParentID (basically this makes sure we have
     *                a staged version and that we have an entry for the current language).
     */
    public final void setParentID(int value, ProcessingContext jParams)
            throws JahiaException {
        setParentID(value, jParams.getUser(), jParams.getEntryLoadRequest());
    }

    //-------------------------------------------------------------------------

    /**
     * Set the new parent ID. This method should be used carefully, all the
     * subtree pages will move with the page. This method is used to move a
     * page. Basically it marks the old position for deletion, or really
     * deletes it if in only existed in staging.
     *
     * @param value       The new parent page ID.
     * @param user
     * @param loadRequest
     * @throws JahiaException
     */
    public final void setParentID(int value, JahiaUser user, EntryLoadRequest loadRequest)
            throws JahiaException {

        // before we start changing the parent ID we have go to remove all the
        // content at the previous location. We do this by marking it for
        // deletion.
        ContentPageKey pageKey = new ContentPageKey(getID());
        Map<String, Integer> languageStates = getLanguagesStates(true);
        Set<String> languageCodes = new HashSet<String>(languageStates.keySet());
        StateModificationContext stateModifContext = new
                StateModificationContext(new ContentPageKey(getID()), languageCodes, false);
        /* removed because not sure it is necessary !
        if (stateModifContext.isObjectIDInPath(pageKey)) {
            // found a recursive call, let's abort now !
            logger.debug("Deletion marking aborted because of recursive call on object " + pageKey.toString() + " ! ");
            return;
        }
        */
        stateModifContext.pushObjectID(pageKey);

        int parentFieldID = getPageService().getPageFieldID(getID());

        if (parentFieldID != -1) {
            // 1. Cut the page link in the parent field value
            ContentPageField contentPageField = (ContentPageField) ContentPageField.getField(
                    parentFieldID);
            if (contentPageField != null && contentPageField.getPageID() != value) {
                // PAGE_MOVE_LOGIC
                contentPageField.setValue(-1, Jahia.getThreadParamBean());
            }
        }

        int activeParentID = getParentID(EntryLoadRequest.CURRENT);
        if (activeParentID == value) {
            logger.debug("Moved page back to it's original position !");
        }

        // now that we've marked the content, we can set the new parent ID.
        // we have set the parent to all lang, not only.
        for (JahiaPageInfo curPageInfo : getAllPageInfosForWrite()) {
            curPageInfo.setParentID(value);

            // Note : we change staging page info without checking if this
            // staging page info is marked for delete or not.
            // If it is marked for delete, we should unmark it for deletion
            // which would have more meaning.
            curPageInfo.setVersionID(0);

            WorkflowEvent theEvent = new WorkflowEvent(this, this, user, curPageInfo.getLanguageCode(), false);
            ServicesRegistry.getInstance().getJahiaEventService().fireObjectChanged(theEvent);

        }
    }

    public void setAclID(int aclID) {
        this.aclID = aclID;
        for (JahiaPageInfo curPageInfo : getPageInfos(false)) {
            curPageInfo.setAclID(aclID);
        }
        try {
            commitChanges(true, false, null);
        } catch (JahiaException e) {
            logger.error("Cannot update page acl", e);
        }
    }

    /**
     * Set the ACL ID. Actually used when a page has change it's type. Passing from
     * URL link or DIRECT page to a Jahia page LINK type and vice versa.
     * WARNING ! Be careful with this method. Use it if it is really necessary
     * to change the ACL ID page.
     *
     * @param aclID The ACL ID to set.
     */
    public final void setAclID(int aclID, EntryLoadRequest loadRequest) {
        this.aclID = aclID;
        for (JahiaPageInfo curPageInfo : getAllPageInfosForWrite()) {
            curPageInfo.setAclID(aclID);
        }
    }

    /**
     * @param aclID
     * @param jParams
     */
    public final void setAclID(int aclID, ProcessingContext jParams) {
        if (jParams != null) {
            setAclID(aclID, jParams.getEntryLoadRequest());
        } else {
            logger.error("FIXME : Method called with null ProcessingContext, aborting...");
        }
    }

    //-------------------------------------------------------------------------

    /**
     * Set the new remote URL. The page type will change accordingly.
     *
     * @param value The new remoteURL.
     */
    public final void setRemoteURL(String value, EntryLoadRequest loadRequest) {
        for (JahiaPageInfo curPageInfo : getAllPageInfosForWrite()) {
            curPageInfo.setPageType(TYPE_URL);
        }
        JahiaPageInfo jahiaPageInfo = getPageInfoVersion(loadRequest, true, false);
        jahiaPageInfo.setRemoteURL(value);       
    }

    /**
     * @param value
     * @param jParams
     */
    public final void setRemoteURL(String value, ProcessingContext jParams) {
        if (jParams != null) {
            setRemoteURL(value, jParams.getEntryLoadRequest());
        } else {
            logger.error("FIXME : Method called with null ProcessingContext, aborting...");
        }
    }

    public final void setRemoteURL(String languageCode, String title,
                                  EntryLoadRequest loadRequest) {
        if (!languageCode.equals(ContentField.SHARED_LANGUAGE)) {
            List<Locale> locales = new ArrayList<Locale>();
            locales.add(LanguageCodeConverters
                    .languageCodeToLocale(languageCode));
            EntryLoadRequest languageLoadRequest = new EntryLoadRequest(
                    loadRequest.getWorkflowState(), loadRequest.getVersionID(), locales);
            this.setRemoteURL(title, languageLoadRequest);
        }
    }


    //-------------------------------------------------------------------------

    /**
     * Change the page title.
     *
     * @param value String holding the new page title.
     */
    public final boolean setTitle(String value, EntryLoadRequest loadRequest) {
        if (value != null && (!value.equals(getTitle(loadRequest)) ||
                !getTitles(true).containsKey(loadRequest.getFirstLocale(true).toString()))) {
            JahiaPageInfo jahiaPageInfo = getPageInfoVersion(loadRequest, true, false);
            jahiaPageInfo.setTitle(value);
            return true;
        }
        return false;
    }

    /**
     * @param value
     * @param jParams
     */
    public final void setTitle(String value, ProcessingContext jParams) {
        if (jParams != null) {
            setTitle(value, jParams.getEntryLoadRequest());
        } else {
            logger.error("FIXME : Method called with null ProcessingContext, aborting...");
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Set the titles for a set of languages
     *
     * @param languagesSet a set of languages as String for which to set the title.
     */
    public final boolean setTitles(Set<String> languagesSet, Map<String, String> titles,
                                   EntryLoadRequest loadRequest) {
        boolean changed = false;
        for (String languageCode : languagesSet) {
            changed |= setTitle(languageCode, titles.get(languageCode), loadRequest);
        }
        return changed;
    }

    /**
     * @param languagesSet
     * @param titles
     * @param jParams
     */
    public final void setTitles(Set<String> languagesSet, Map<String, String> titles, ProcessingContext jParams) {
        if (jParams != null) {
            setTitles(languagesSet, titles, jParams.getEntryLoadRequest());
        } else {
            logger.error("FIXME : Method called with null ProcessingContext, aborting...");
        }
    }

    /**
     * Set page titles for every language defined in the titles Map.
     *
     * @param titles The titles Map.
     */
    public final void setTitles(Map<String, String> titles, EntryLoadRequest loadRequest) {
        setTitles(titles.keySet(), titles, loadRequest);
    }

    /**
     * @param titles
     * @param jParams
     */
    public final void setTitles(Map<String, String> titles, ProcessingContext jParams) {
        if (jParams != null) {
            setTitles(titles, jParams.getEntryLoadRequest());
        } else {
            logger.error("FIXME : Method called with null ProcessingContext, aborting...");
        }
    }

    //--------------------------------------------------------------------------

    /**
     * Set the title for a language code
     *
     * @param languageCode
     * @param title
     */
    public final boolean setTitle(String languageCode, String title,
                                  EntryLoadRequest loadRequest) {
        if (!languageCode.equals(ContentField.SHARED_LANGUAGE)) {
            List<Locale> locales = new ArrayList<Locale>();
            locales.add(LanguageCodeConverters
                    .languageCodeToLocale(languageCode));
            EntryLoadRequest languageLoadRequest = new EntryLoadRequest(
                    loadRequest.getWorkflowState(), loadRequest.getVersionID(), locales);
            return this.setTitle(title, languageLoadRequest);
        }
        return false;
    }

    /**
     * @param languageCode
     * @param title
     * @param jParams
     */
    public final void setTitle(String languageCode, String title, ProcessingContext jParams) {
        if (jParams != null) {
            setTitle(languageCode, title, jParams.getEntryLoadRequest());
        } else {
            logger.error("FIXME : Method called with null ProcessingContext, aborting...");
        }
    }

    /**
     * Constant associated with the getTitles method. Signify that this method should
     * return the last updated Jahia page titles.
     */
    public static final boolean LAST_UPDATED_TITLES = true;

    /**
     * Constant associated with the getTitles method. Signify that this method should
     * only return the activated page titles.
     */
    public static final boolean ACTIVATED_PAGE_TITLES = false;

    /**
     * Get all page titles in all languages. First check titles coming from the
     * active pages. If 'lastUpdatedTitles' is set, then check titles from the
     * staged page meaning that last input tiles are always returned.
     *
     * @param lastUpdatedTitles One of the LAST_UPDATED_TITLES or ACTIVATED_PAGE_TITLES
     *                          boolean constants. This boolean specifies if we should try to find a
     *                          staged version for a language if we couldn't find an active one.
     * @return The last updated page titles. The object returned is a Map
     *         containing String as keys that contain the language code, and the value
     *         objects are String object containing the actual title in the language.
     */
    public Map<String, String> getTitles(boolean lastUpdatedTitles) {
        Map<String, String> titles = new HashMap<String, String>();

        for (Map.Entry<String, JahiaPageInfo> entry : getActivePageInfos().entrySet()) {
            titles.put(entry.getKey(), entry.getValue().getTitle());
        }
        if (lastUpdatedTitles) {
            for (Map.Entry<String, JahiaPageInfo> entry : getStagingPageInfos().entrySet()) {
                String pageTitle = entry.getValue().getTitle();
                if (pageTitle != null && pageTitle.length() > 0) {
                    titles.put(entry.getKey(), pageTitle);
                }
            }
        }

        if (!this.hasActiveEntries() && !this.hasStagingEntries()) {
            try {
                // Deleted
                List<SiteLanguageSettings> langs = this.getSite().getLanguageSettings();
                for (SiteLanguageSettings siteLanguageMapping : langs) {
                    ContentObjectEntryState entryState =
                            new ContentObjectEntryState(
                                    ContentObjectEntryState.WORKFLOW_STATE_ACTIVE,
                                    0, siteLanguageMapping.getCode());
                    EntryLoadRequest loadRequest = new EntryLoadRequest(entryState);
                    ContentDefinition definition =
                            ContentDefinition.getContentDefinitionInstance(
                                    this.getDefinitionKey(loadRequest));

                    String title = definition != null ? definition.getTitle(this, entryState) : "";
                    if (title != null) {
                        titles.put(siteLanguageMapping.getCode(), title);
                    }
                }
            } catch (Exception t) {
                logger.debug("Exception : retrieving titles or deleted page", t);
            }
        }

        return titles;
    }

    public JahiaSite getSite() throws JahiaException {
        return ServicesRegistry.getInstance().getJahiaSitesService().getSite(getJahiaID());
    }

    //-------------------------------------------------------------------------

    /**
     * Return the page URL
     * for backward compatibility.
     *
     * @param jParams a ProcessingContext object used to generate all the URLs using
     *                the ProcessingContext URL generation methods. The ProcessingContext is also used to
     *                retrieve a RemoteURL if we have that type.
     * @return Return the page URL string.
     * @throws JahiaException thrown in there were problems while generating
     *                        the URLs
     */
    public String getUrl(ProcessingContext jParams)
            throws JahiaException {
        return getURL(jParams);
    } // end getUrl

    //-------------------------------------------------------------------------

    /**
     * Return the page URL with the given language code.
     *
     * @param jParams      a ProcessingContext object used to generate all the URLs using
     *                     the ProcessingContext URL generation methods. The ProcessingContext is also used to
     *                     retrieve a RemoteURL if we have that type.
     * @param languageCode The 'iso639' composed with the 'iso3166' language code.
     * @return Return the page URL string.
     * @throws JahiaException thrown in there were problems while generating
     *                        the URLs
     */
    public String getURL(ProcessingContext jParams, String languageCode)
            throws JahiaException {
        String outURL = "";
        switch (getPageType(jParams.getEntryLoadRequest())) {
            case (TYPE_DIRECT):
                outURL = jParams.composePageUrl(this, languageCode);
                break;
            case (TYPE_LINK):
                int linkPageID = -1;
                try {
                    linkPageID = getPageLinkID(jParams.getEntryLoadRequest());
                    if (linkPageID != -1) {
                        ContentPage linkPage = ContentPage.getPage(linkPageID, false);
                        // require at least read access
                        if (linkPage.checkReadAccess(jParams.getUser())) {
                            outURL = jParams.composePageUrl(linkPageID, languageCode);
                        }
                    }
                } catch (Exception t) {
                    logger.debug("Exception creating link url with page[" + linkPageID + "]",
                            t);
                }
                break;
            case (TYPE_URL):
                outURL = getRemoteURL(jParams.getEntryLoadRequest());
                break;
        }
        return outURL;
    }

    /**
     * Return the page URL in the current language.
     *
     * @param jParams ;)
     * @return Return the page URL string.
     * @throws JahiaException
     */
    public String getURL(ProcessingContext jParams)
            throws JahiaException {
        return getURL(jParams, null);
    }

    //-------------------------------------------------------------------------
    /**
     * Return the page path. The page path consist of all the parent pages of
     * the specified page until the site's root page.
     *
     * @return Return a Iterator of ContentPage objects. The returned
     *         Iterator is always non-null, but might have no pages if the
     *         specified page has not childs, or if no childs matching the
     *         loading flag were found.
     * @throws JahiaException in the case that we have problems loading the
     *                        pages from the persistant storage
     */
    public Iterator<ContentPage> getContentPagePath(EntryLoadRequest loadRequest, String operationMode,
                                                       JahiaUser user)
            throws JahiaException {
        return getContentPagePath(loadRequest, operationMode, user, JahiaPageService.PAGEPATH_SHOW_ALL);
    } // end getContentPagePath

    /**
     * Return the page path. The page path consist of all the parent pages of
     * the specified page until the site's root page.
     *
     * @return Return a Iterator of ContentPage objects. The returned
     *         Iterator is always non-null, but might have no pages if the
     *         specified page has not childs, or if no childs matching the
     *         loading flag were found.
     * @throws JahiaException in the case that we have problems loading the
     *                        pages from the persistant storage
     */
    public List<ContentPage> getContentPagePathAsList(EntryLoadRequest loadRequest, String operationMode,
                                                       JahiaUser user)
            throws JahiaException {
        return getPageService().getContentPagePath(getID(), loadRequest, operationMode, user, JahiaPageService.PAGEPATH_SHOW_ALL);
    } // end getContentPagePath

    //-------------------------------------------------------------------------
    /**
     * @param jParams
     * @return
     * @throws JahiaException
     */
    public Iterator<ContentPage> getContentPagePath(ProcessingContext jParams)
            throws JahiaException {
        if (jParams != null) {
            return getContentPagePath(jParams.getEntryLoadRequest(), jParams.getOperationMode(),
                    jParams.getUser(), JahiaPageService.PAGEPATH_SHOW_ALL);
        } else {
            logger.error("FIXME : Method called with null ParamBean, returning null");
            return null;
        }
    }

    /**
     * Return the page path. The page path consist of all the parent pages of
     * the specified page until the site's root page.
     *
     * @return Return a Iterator of ContentPage objects. The returned
     *         Iterator is always non-null, but might have no pages if the
     *         specified page has not childs, or if no childs matching the
     *         loading flag were found.
     * @throws JahiaException in the case that we have problems loading the
     *                        pages from the persistant storage
     */
    public Iterator<ContentPage> getContentPagePath(EntryLoadRequest loadRequest, String operationMode,
                                                       JahiaUser user, int command)
            throws JahiaException {
        List<ContentPage> thePath = getPageService().getContentPagePath(getID(), loadRequest, operationMode, user, command);

        return thePath != null ? (new ArrayList<ContentPage>(thePath)).iterator() : null;
    } // end getContentPagePath

    //-------------------------------------------------------------------------
    /**
     * Return the page path. The page path consist of all the parent pages of
     * the specified page until the site's root page.
     *
     * @param levels  an integer specifying the offset of levels to retrieve.
     *                So if the page is a depth level 5 and we specify that we want to retrieve
     *                only 2 levels, only levels 4 and 5 will be returned by this method.
     * @param jParams a ParamBean object used to load pages in the path.
     * @return Return a Iterator of ContentPage objects. The returned
     *         Iterator is always non-null, but might have no pages if the
     *         specified page has not childs, or if no childs matching the
     *         loading flag were found.
     * @throws JahiaException in the case that we have problems loading the
     *                        pages from the persistant storage
     */
    public Iterator<ContentPage> getContentPagePath(int levels, ProcessingContext jParams)
            throws JahiaException {

        return getContentPagePath(levels, jParams.getEntryLoadRequest(),
                jParams.getOperationMode(), jParams.getUser(),
                JahiaPageService.PAGEPATH_SHOW_ALL);
    } // end getContentPagePath

    /**
     * Return the page path. The page path consist of all the parent pages of
     * the specified page until the site's root page.
     *
     * @param levels an integer specifying the offset of levels to retrieve.
     *               So if the page is a depth level 5 and we specify that we want to retrieve
     *               only 2 levels, only levels 4 and 5 will be returned by this method.
     * @return Return a Iterator of ContentPage objects. The returned
     *         Iterator is always non-null, but might have no pages if the
     *         specified page has not childs, or if no childs matching the
     *         loading flag were found.
     * @throws JahiaException in the case that we have problems loading the
     *                        pages from the persistant storage
     */
    public Iterator<ContentPage> getContentPagePath(int levels, EntryLoadRequest loadRequest, String operationMode,
                                                       JahiaUser user, int command) throws JahiaException {
        List<ContentPage> thePath = getPageService().getContentPagePath(getID(), loadRequest, operationMode, user, command);

        if (thePath != null) {
            int fromIndex = 0;
            if ((thePath.size() - levels) > 0) {
                fromIndex = thePath.size() - levels;
            }
            List<ContentPage> theShortPathList = thePath.subList(fromIndex,
                    thePath.size());
            return theShortPathList.iterator();
        }
        return null;
    } // end getContentPagePath

    /**
     * Return the page path. The page path consist of all the parent pages of
     * the specified page until the site's root page.
     *
     * @return Return a Iterator of JahiaPage objects. The returned
     *         Iterator is always non-null, but might have no pages if the
     *         specified page has not childs, or if no childs matching the
     *         loading flag were found.
     * @throws JahiaException in the case that we have problems loading the
     *                        pages from the persistant storage
     * @deprecated use getContentPagePath
     */
    public Iterator<JahiaPage> getPagePath(EntryLoadRequest loadRequest, String operationMode,
                                              JahiaUser user)
            throws JahiaException {
        Iterator<ContentPage> contentPagePath = getContentPagePath(loadRequest, operationMode,
                user, JahiaPageService.PAGEPATH_BREAK_ON_RESTRICTED);
        List<JahiaPage> pagePath = new ArrayList<JahiaPage>();
        while (contentPagePath.hasNext()) {
            ContentPage contentPage = contentPagePath.next();
            try {
                pagePath.add(contentPage.getPage(loadRequest, operationMode, user));
            } catch (JahiaException ex) {
                logger.warn("Cannot resolve JahiaPage for PagePath - rather use getContentPagePath", ex);
            }
        }
        return pagePath.iterator();
    } // end getPath

    /**
     * @param jParams
     * @return
     * @throws JahiaException
     * @deprecated use getContentPagePath
     */
    public Iterator<JahiaPage> getPagePath(ProcessingContext jParams)
            throws JahiaException {
        return getPagePath(jParams.getEntryLoadRequest(), jParams
                .getOperationMode(), jParams.getUser());
    }

    //-------------------------------------------------------------------------

    /**
     * Return the page path. The page path consist of all the parent pages of
     * the specified page until the site's root page.
     *
     * @param levels  an integer specifying the offset of levels to retrieve.
     *                So if the page is a depth level 5 and we specify that we want to retrieve
     *                only 2 levels, only levels 4 and 5 will be returned by this method.
     * @param jParams a ProcessingContext object used to load pages in the path.
     * @return Return a Iterator of JahiaPage objects. The returned
     *         Iterator is always non-null, but might have no pages if the
     *         specified page has not childs, or if no childs matching the
     *         loading flag were found.
     * @throws JahiaException in the case that we have problems loading the
     *                        pages from the persistant storage
     * @deprecated use getContentPagePath
     */
    public Iterator<JahiaPage> getPagePath(int levels, ProcessingContext jParams)
            throws JahiaException {
        return getPagePath(levels, jParams.getEntryLoadRequest(), jParams
                .getOperationMode(), jParams.getUser());
    }

    /**
     * Return the page path. The page path consist of all the parent pages of
     * the specified page until the site's root page.
     *
     * @param levels an integer specifying the offset of levels to retrieve.
     *               So if the page is a depth level 5 and we specify that we want to retrieve
     *               only 2 levels, only levels 4 and 5 will be returned by this method.
     * @return Return a Iterator of JahiaPage objects. The returned
     *         Iterator is always non-null, but might have no pages if the
     *         specified page has not childs, or if no childs matching the
     *         loading flag were found.
     * @throws JahiaException in the case that we have problems loading the
     *                        pages from the persistant storage
     * @deprecated use getContentPagePath
     */
    public Iterator<JahiaPage> getPagePath(int levels, EntryLoadRequest loadRequest, String operationMode,
                                              JahiaUser user)
            throws JahiaException {
        Iterator<ContentPage> contentPagePath = getContentPagePath(levels, loadRequest, operationMode,
                user, JahiaPageService.PAGEPATH_BREAK_ON_RESTRICTED);
        List<JahiaPage> pagePath = new ArrayList<JahiaPage>();
        while (contentPagePath.hasNext()) {
            ContentPage contentPage = contentPagePath.next();
            try {
                pagePath.add(contentPage.getPage(loadRequest, operationMode, user));
            } catch (JahiaException ex) {
                logger.warn("Cannot resolve JahiaPage for PagePath - rather use getContentPagePath", ex);
            }
        }
        return pagePath.iterator();
    } // end getPath

    //-------------------------------------------------------------------------

    /**
     * Return an Iterator holding all the child pages of the specified page.
     * The loading flag filters the kind of pages to return.
     *
     * @param jParams a ProcessingContext object used to specify which pages to load
     *                from persistant storage
     * @return Return an Iterator of JahiaPage objects. Return null if not
     *         the current page has not childs.
     * @throws JahiaException Return this exception if any failure occured.
     */
    public List<JahiaPage> getChildPages(ProcessingContext jParams)
            throws JahiaException {
        List<JahiaPage> childs = getPageService().getPageChilds(getID(), PageLoadFlags.ALL, jParams);
        return childs;
    } // end getChilds

    /**
     * Return an Iterator holding all the child pages of the specified page.
     * The loading flag filters the kind of pages to return. This method checks
     * the rights for a user and loads only the pages a user is allowed to
     * see.
     *
     * @param user a JahiaUser object for which to check the rights on the
     *             pages
     * @return an Iterator of JahiaPage objects that are the childs of this
     *         page
     * @throws JahiaException thrown in the case we have problems while loading
     *                        data from the persistent storage
     */
    public List<JahiaPage> getChildPages(JahiaUser user)
            throws JahiaException {
        List<JahiaPage> childs = getPageService().getPageChilds(getID(), PageLoadFlags.ALL, user);
        // logger.debug ("Nb child found " + childs.size());
        return childs;
    } // end getChilds

    public List<? extends ContentObject> getChilds(JahiaUser user, EntryLoadRequest loadRequest)
            throws JahiaException {
        return getChilds(user, loadRequest, JahiaContainerStructure.ALL_TYPES);
    }

    public List<? extends ContentObject> getChilds(JahiaUser user, EntryLoadRequest loadRequest,
                                              int loadFlag) throws JahiaException {
        List<ContentObject> resultList = new ArrayList<ContentObject>();
        switch (getPageType(loadRequest)) {
            case JahiaPage.TYPE_DIRECT:
                if ((loadFlag & JahiaContainerStructure.JAHIA_FIELD) != 0
                        && org.jahia.settings.SettingsBean.getInstance()
                        .areDeprecatedNonContainerFieldsUsed()) {
                    // first let's add all the fields that are directly attached to the
                    // page.
                    List<Integer> nonContainerFieldIDs = ServicesRegistry.getInstance()
                            .getJahiaFieldService()
                            .getNonContainerFieldIDsInPageByWorkflowState(
                                    getID(), loadRequest);
                    for (Integer curFieldID : nonContainerFieldIDs) {
                        ContentField curField = ContentField
                                .getField(curFieldID.intValue());
                        if (curField != null && !curField.isMetadata()) {
                            resultList.add(curField);
                        }
                    }
                }
                if ((loadFlag & JahiaContainerStructure.JAHIA_CONTAINER) != 0) {
                    // now let's add all the container lists that are the direct children
                    // of this page (subcontainers lists are NOT included here !)
                    SortedSet<Integer> containerListIDs = ServicesRegistry.getInstance()
                            .getJahiaContainersService()
                            .getAllPageTopLevelContainerListIDs(getID(),
                                    loadRequest);
                    for (Integer curContainerListID : containerListIDs) {
                        ContentContainerList curContainerList = ContentContainerList
                                .getContainerList(curContainerListID.intValue());
                        if (curContainerList != null) {
                            resultList.add(curContainerList);
                        }
                    }
                }
                break;
            case JahiaPage.TYPE_LINK:
            case JahiaPage.TYPE_URL:
                break;
        }

        return resultList;
    }

    public ContentObject getParent(EntryLoadRequest loadRequest)
            throws JahiaException {
//        if(parent ==null) {
        int pageFieldID = pageManager.getPageFieldID(getID(), loadRequest);

        if (pageFieldID != -1) {
            parent = ContentField.getField(pageFieldID);
        } else {
            return null;
        }
//    }
        return parent;
    }

    public ContentObject getParent(JahiaUser user,
                                   EntryLoadRequest loadRequest,
                                   String operationMode)
            throws JahiaException {
        return getParent(loadRequest);
    }

    /**
     * Return an Iterator holding all the child PAGE(!) of the specified page.
     * This method checks the rights for a user and loads only the pages a user
     * is allowed to see.
     *
     * @param user a JahiaUser object for which to check the rights on the
     *             pages
     * @return an Iterator of JahiaPageContent objects that are the childs of this
     *         page
     * @throws JahiaException thrown in the case we have problems while loading
     *                        data from the persistent storage
     */
    public Iterator<ContentPage> getDirectContentPageChilds(JahiaUser user, int pageInfosFlags,
                                                  String languageCode)
            throws JahiaException {
        List<ContentPage> childs = getPageService().getDirectContentPageChilds(getID(), user, pageInfosFlags,
                languageCode);
        return childs != null ? (new ArrayList<ContentPage>(childs)).iterator() : null;
    }

    /**
     * Return an Iterator holding all the child PAGE(!) of the specified page.
     * This method checks the rights for a user and loads only the pages a user
     * is allowed to see.
     *
     * @param user           a JahiaUser object for which to check the rights on the
     *                       pages
     * @param pageInfosFlags
     * @param languageCode
     * @param directPageOnly
     * @return an Iterator of JahiaPageContent objects that are the childs of this
     *         page
     * @throws JahiaException thrown in the case we have problems while loading
     *                        data from the persistent storage
     */
    public Iterator<ContentPage> getContentPageChilds(JahiaUser user, int pageInfosFlags,
                                         String languageCode, boolean directPageOnly)
            throws JahiaException {
        List<ContentPage> childs = getPageService().getContentPageChilds(getID(), user, pageInfosFlags,
                languageCode, directPageOnly);
        return childs != null ? childs.iterator() : null;
    }

    //-------------------------------------------------------------------------

    /**
     * Compare between two objects, sort by their name
     *
     * @param c1 left-side object
     * @param c2 right-side object
     * @return <0 if c1 < c2, 0 if c1=c2, >0 if c1>c2
     * @throws ClassCastException if the objects where not of type JahiaPage.
     */
    public int compare(JahiaPage c1, JahiaPage c2) throws ClassCastException {

        return (c1.getTitle().toLowerCase()
                .compareTo(c2.getTitle().toLowerCase()));

    }

    //-------------------------------------------------------------------------

    /**
     * Generates a String containing a status and debug information about this
     * ContentPage object.
     *
     * @return a String containing most of the internal variable states
     */
    public String toString() {
        final StringBuffer output = new StringBuffer();
        output.append("Detail of ContentPage [");
        output.append(getID());
        output.append("] :\n");
        output.append("Displaying JahiaPageInfo List : ");
        int counter = 0;
        for (JahiaPageInfo pageInfo : getPageInfos()) {
            output.append("JahiaPageInfo#");
            output.append(counter);
            output.append(":");
            output.append(pageInfo.toString());
            counter++;
        }
        output.append("end of ContentPage display.\n");

        return output.toString();
    }

    /**
     * Purges this page and ALL it's related content. This is the ultimate
     * deletion method. Please use carefully. This destroys everything, including
     * versions, staging, etc...
     *
     * @throws JahiaException in case there was an error removing some content
     *                        from database.
     */
    public void purge(EntryLoadRequest loadRequest) throws JahiaException {

        ServicesRegistry sr = ServicesRegistry.getInstance();

        switch (getPageType(loadRequest)) {

            case JahiaPage.TYPE_DIRECT:

                // we must now mark all page related content for deletion, that is to
                // say :

                // 1. all fields on the page
                sr.getJahiaFieldService().purgePageFields(getID());

                // 2. all container lists on the page
                sr.getJahiaContainersService().purgePageContainerLists(getID());

                break;
            case JahiaPage.TYPE_LINK:
                break;
            case JahiaPage.TYPE_URL:
                break;
        }

        if (!isAclSameAsParent()) {
            getACL().delete();
        }

        // first we must check if there are active versions for the page info
        // data. If not, we simply delete directly the existing staging entries
        for (JahiaPageInfo pageInfo : getPageInfos(true)) {
            deleteEntry(pageInfo);
        }

    }

    /**
     * Returns a local page property. This method does not do recursive upwards
     * checking in the parent pages, but only checks locally.
     *
     * @param name the name of the property to retrieve
     * @return a PageProperty object that may contain multiple language versions
     * @throws JahiaException thrown in case we have trouble loading the
     *                        properties from the persistant storage
     */
    public PageProperty getPageLocalProperty(String name)
            throws JahiaException {
        Map<String, PageProperty> pageProperties = getPageService().getPageProperties(this.getID());
        if (pageProperties != null) {
            return pageProperties.get(name);
        } else {
            logger.debug("Error accessing page property " + name +
                    " probably doesn't exist yet...");
            return null;
        }
    }

    /**
     * Retrieves a page property value. If the property couldn't be found for
     * this page, and that this page has a parent, this method will go up
     * the page hierarchy to look for this property. This method retrieves the
     * default value for a property, ignoring multi-language values if they
     * exist.
     *
     * @param name a String containing the name of the page property to
     *             return.
     * @return a String containing the value of the page property or null if
     *         the property couldn't be found.
     * @throws JahiaException raised if there was a problem accessing the
     *                        backend systems that contain the properties
     */
    public String getProperty(String name, EntryLoadRequest loadRequest)
            throws JahiaException {
        PageProperty curProp = getPageLocalProperty(name);
        if (curProp != null) {
            return curProp.getValue();
        } else {
            // we could find it locally, let's try to find it in the parent if
            // this object has one.
            if (getParentID(loadRequest) > 0) {
                // we are not in the case of the root page or a page without
                // a parent
                ContentPage parentPage = getPageService().lookupContentPage(
                        getParentID(loadRequest), true);
                if (parentPage != null) {
                    // let's recursively look for the property in parent
                    // pages
                    return parentPage.getProperty(name, loadRequest);
                }
            }
            return null;
        }
    }

    public String getProperty(String name, ProcessingContext jParams)
            throws JahiaException {
        if (jParams != null) {
            return getProperty(name, jParams.getEntryLoadRequest());
        } else {
            logger.error("FIXME : Method called with null ProcessingContext, returning null");
            return null;
        }
    }

    /**
     * Retrieves the page property value corresponding to the specified
     * language code. If the property is not defined for this page, this
     * method will try to walk up the page hierarchy to find it.
     *
     * @param name         the name of the property to retrieve
     * @param languageCode the RFC 3066 language code for which to retrieve
     *                     the page property value
     * @param jParams      a ProcessingContext object used to load the parent pages in the
     *                     case we do not find the property in this page.
     * @return a String containing the property value for the given language OR
     *         the default value if it couldn't be found.
     * @throws JahiaException raised if there was a problem accessing the
     *                        backend systems that contain the properties
     */
    public String getProperty(String name, String languageCode, ProcessingContext jParams)
            throws JahiaException {
        PageProperty curProp = getPageLocalProperty(name);
        if (curProp != null) {
            return curProp.getValue(languageCode);
        } else {
            // we could find it locally, let's try to find it in the parent if
            // this object has one.
            if (getParentID(jParams.getEntryLoadRequest()) > 0) {
                // we are not in the case of the root page or a page without
                // a parent
                JahiaPage parentPage = getPageService().lookupPage(
                        getParentID(jParams.getEntryLoadRequest()),
                        jParams.getEntryLoadRequest(), jParams.getOperationMode(),
                        jParams.getUser(), true);
                if (parentPage != null) {
                    // let's recursively look for the property in parent
                    // pages
                    return parentPage.getProperty(name, languageCode);
                }
            }
            return null;
        }
    }


    public String getProperty(String name) throws JahiaException {
        PageProperty pageLocalProperty = getPageLocalProperty(name);
        return pageLocalProperty == null ? null : pageLocalProperty.getValue();
    }

    /**
     * Sets a page property value. This updates both the in-memory and
     * persistant systems simultaneously so it might have a performance
     * impact. This sets the default value of the property.
     *
     * @param name  the name of the page property to be set
     * @param value the value of the page property
     * @throws JahiaException raised if there was a problem accessing the
     *                        backend systems that contain the properties
     */
    public void setProperty(Object name, Object value) throws JahiaException {
        setProperty(name, null, value);
    }

    /**
     * Sets a page property value. This updates both the in-memory and
     * persistant systems simultaneously so it might have a performance
     * impact. This sets the value for a given language code of the page
     * property
     *
     * @param name         the name of the page property to be set
     * @param languageCode the RFC 3066 language code for which to store
     *                     the property value.
     * @param value        the value of the page property
     * @throws JahiaException raised if there was a problem accessing the
     *                        backend systems that contain the properties
     */
    public synchronized void setProperty(Object name, String languageCode, Object value)
            throws JahiaException {
        Map<String, PageProperty> pageProperties = getPageService().getPageProperties(this.getID());

        PageProperty targetProperty = pageProperties != null ? pageProperties.get(name) : null;
        if (targetProperty == null) {
            targetProperty = new PageProperty(getID(), (String)name);
        }
        if (languageCode != null) {
            targetProperty.setValue((String)value, languageCode);
        } else {
            targetProperty.setValue((String)value);
        }
        pageManager.setPageProperty(targetProperty);

        if (pageProperties != null) {
            pageProperties.put((String)name, targetProperty);
        }
        getPageService().invalidatePageCache(getID());
    }

    /**
     * Remove a property. This updates both the in-memory and
     * persistant systems simultaneously so it might have a performance
     * impact.
     *
     * @param name the name of the page property to be removed
     * @throws JahiaException raised if there was a problem accessing the
     *                        backend systems that contain the properties
     */
    public void removeProperty(String name)
            throws JahiaException {
        Map<String, PageProperty> pageProperties = getPageService().getPageProperties(this.getID());
        if (pageProperties != null) {
            PageProperty targetProperty = getPageLocalProperty(name);
            if (targetProperty != null) {
                pageProperties.remove(name);
                pageManager.removePageProperty(targetProperty);
            }
        }
        getPageService().invalidatePageCache(getID());
    }

    public boolean setPageKey(String pageKey) throws JahiaException {
        if (pageKey != null) {
            String basePageKey = null;
            boolean ok = false;
            for (int i = 1; !ok; i++) {
                ok = true;
                List<PageProperty> pageProperties = ServicesRegistry.getInstance().getJahiaPageService().getPagePropertiesByValueAndSiteID(pageKey, getSiteID());
                for (Iterator<PageProperty> iterator = pageProperties.iterator(); iterator.hasNext();) {
                    PageProperty pageProperty = (PageProperty) iterator.next();
                    if (pageProperty.getName().equals(PageProperty.PAGE_URL_KEY_PROPNAME) && pageProperty.getPageID() != getID()) {
                        if (basePageKey == null) {
                            try {
                                int index = pageKey.lastIndexOf("_");
                                basePageKey = pageKey.substring(0, index);
                                i = Integer.parseInt(pageKey.substring(index + 1)) + 1;
                            } catch (Exception e) {
                                basePageKey = pageKey;
                            }
                        }
                        pageKey = basePageKey + "_" + i;
                        ok = false;
                        break;
                    }
                }
            }

            setProperty(PageProperty.PAGE_URL_KEY_PROPNAME, pageKey);
        } else {
            removeProperty(PageProperty.PAGE_URL_KEY_PROPNAME);
        }               

        for (Iterator<ContentObject> iterator = getPickerObjects().iterator(); iterator.hasNext();) {
            ContentPage picker = (ContentPage) iterator.next();
            picker.setPageKey(pageKey);
        }
        return true;
    }


    public int getPageID() {
        return getObjectKey().getIdInType();
    }

    /**
     * Determines with which JahiaPageInfo object we must work for a request.
     * This is different depending on whether we are trying to read values or
     * write them.
     * In the case of reading them, we want to resolve according to language
     * preferences, retrieved from the ProcessingContext.getLocales method. We will
     * also resolve whether to retrieve the active or staging version to read.
     * In the write case we want to write to a specific language, and a specific
     * version for writing which will be in staging mode.
     *
     * @param isWrite a boolean indicating if we want to retrieve a version for
     *                writing or just for reading (changes the resolving algorithm !)
     * @return a JahiaPageInfo resolved according to the above-explained rules.
     */
    protected JahiaPageInfo getPageInfoVersion(EntryLoadRequest loadRequest, boolean isWrite,
                                               boolean ignoreLanguage) {
        Set<JahiaPageInfo> pageInfos = getPageInfos();
        if (pageInfos.isEmpty()) {
            logger.debug("No page info for page, can't access database data");
            return null;
        }

        if (isWrite) {
            JahiaPageInfo writeInfo = null;
            try {
                writeInfo = getPageInfoVersionForWrite(loadRequest);
            } catch (JahiaException je) {
                logger.warn("Error while retrieving page info for writing", je);
            }
            return writeInfo;
        }

        JahiaPageInfo curInfo = null;

        if (loadRequest != null) {
            if (loadRequest.getWorkflowState() <= 0) {
                // we are in the case of an archived version, let's load it
                // just for this case. None of these operations are cached.
                loadVersioningEntryStates();
            }
            curInfo = (JahiaPageInfo) getVersionService().
                    resolveEntry(new ArrayList<EntryStateable>(pageInfos), loadRequest, ignoreLanguage);
        } else {

            // logger.debug( "No ProcessingContext passed, returning any page data for page...");
            /** todo FIXME this is ugly and random, we MUST do better ! */
            Iterator<JahiaPageInfo> pageInfoIter = pageInfos.iterator();
            if (pageInfoIter.hasNext()) {
                curInfo = pageInfoIter.next();
            }
        }

        // resolving the case in which the entry load request is equals to "shared"
        if (curInfo == null && (loadRequest.getFirstLocale(true) == null)) {
            // no language specified
            // we return the first entry matching the request but no matter in which language it is.
            int workflow_state = loadRequest.getWorkflowState();
            if (workflow_state <= 0) {
                // @todo loading versioned entries
            }
            if (workflow_state == ContentObjectEntryState.WORKFLOW_STATE_START_STAGING) {
                Iterator<JahiaPageInfo> it = getStagingPageInfos().values().iterator();
                if (!it.hasNext()) {
                    it = getActivePageInfos().values().iterator();
                }
                if (it.hasNext()) {
                    curInfo = it.next();
                }
            }
            if (curInfo == null
                    && workflow_state == ContentObjectEntryState.WORKFLOW_STATE_ACTIVE) {
                Iterator<JahiaPageInfo> it = getActivePageInfos().values().iterator();
                if (it.hasNext()) {
                    curInfo = it.next();
                }
            }
        }
        return curInfo;
    }

    /**
     * If getPageInfoVersion(jParams,isWrite) return null, try again with a
     * an entryLoadRequest with only one language (SHARED), that is, try to get
     * an active or staging page info no matter what language it is.
     *
     * @param isWrite a boolean indicating if we want to retrieve a version for
     *                writing or just for reading (changes the resolving algorithm !)
     * @return a JahiaPageInfo resolved according to the above-explained rules.
     */
    protected JahiaPageInfo getPageInfoVersionIgnoreLanguage(EntryLoadRequest loadRequest,
                                                             boolean isWrite) {
        JahiaPageInfo pageInfo = getPageInfoVersion(loadRequest, isWrite, true);
        if (pageInfo != null || loadRequest == null) {
            return pageInfo;
        }

        EntryLoadRequest noLanguageLoadRequest
                = new EntryLoadRequest(loadRequest.getWorkflowState(),
                loadRequest.getVersionID(), new ArrayList<Locale>());
        noLanguageLoadRequest.setWithDeleted(loadRequest.isWithDeleted());
        noLanguageLoadRequest.setWithMarkedForDeletion(loadRequest.isWithMarkedForDeletion());

        noLanguageLoadRequest.getLocales().add(EntryLoadRequest.SHARED_LANG_LOCALE);
        pageInfo = getPageInfoVersion(noLanguageLoadRequest, isWrite, true);
        return pageInfo;
    }

    /**
     * Returns the page info entry corresponding to the requested entry state.
     * Warning : this loads the versioning page infos if they were never loaded
     * and we are requesting a versioned entry state.
     *
     * @param entryState the entry state to find in the page info
     * @return a JahiaPageInfo if we found the entry corresponding to the
     *         entry state specified, or null if not found.
     * @throws JahiaException raised if there was a problem loading the versioned
     *                        entries from the database.
     */
    protected JahiaPageInfo getPageInfo(ContentObjectEntryState entryState)
            throws JahiaException {

        if (entryState == null) {
            return null;
        }

        // first let's load the versioned entries if necessary.
        if (entryState.getWorkflowState() < ContentObjectEntryState.WORKFLOW_STATE_ACTIVE) {
            loadVersioningEntryStates();
        }

        // now let's try to find the entry we are requesting.
        for (JahiaPageInfo curPageInfo : getPageInfos()) {
            if ((curPageInfo.getWorkflowState() == entryState.getWorkflowState()) &&
                    (curPageInfo.getVersionID() == entryState.getVersionID()) &&
                    (curPageInfo.getLanguageCode().equals(entryState.getLanguageCode()))) {
                return curPageInfo;
            }
        }
        return null;
    }

    /**
     * This method just checks all the staged JahiaPageInfos and returns their
     * status
     *
     * @return an int containing the current staging status
     */
    private int getMostRecentStagingStatus() {

        int currentStatus = EntryLoadRequest.STAGING_WORKFLOW_STATE;

        // let's see the highest staging mode we are currently in
        // shouldn't all these be equal ?
        for (JahiaPageInfo thisPageInfo : getStagingPageInfos().values()) {
            if (thisPageInfo.getWorkflowState() >= EntryLoadRequest.STAGING_WORKFLOW_STATE)
                currentStatus = thisPageInfo.getWorkflowState();
        }
        return currentStatus;
    }

    /**
     * Shortcut method to the real getAllPageInfosForWrite that takes a set
     * of languages codes to create. See that method's description for more
     * details.
     *
     * @return a List of JahiaPageInfo objects that contain all the different
     *         language versions for a given version (active or staging for the moment).
     */
    private List<JahiaPageInfo> getAllPageInfosForWrite() {
        Set<String> languageCodes = new HashSet<String>();
        languageCodes.addAll(getActivePageInfos().keySet());
        languageCodes.addAll(getStagingPageInfos().keySet());
        return getAllPageInfosForWrite(languageCodes);
    }

    /**
     * This method returns all the PageInfos for a given version, basically
     * all the different existing languages PageInfos for a given version.
     * It also adds versions if it doesn't exist yet. This is very useful
     * when writing values that are shared among languages such as PageLinkID,
     * PageTemplateID, etc...
     *
     * @param languageCodes a set of String objects that represents the
     *                      languages for which we want to create values if they do not yet exist.
     * @return a List of JahiaPageInfo objects that contain all the different
     *         language versions for a given version (active or staging for the moment).
     */
    private List<JahiaPageInfo> getAllPageInfosForWrite(Set<String> languageCodes) {
        List<JahiaPageInfo> pageInfos = new ArrayList<JahiaPageInfo>();

        // now we must insure that we create staging entries for ALL the
        // languages that exist in active mode, not only the ones we have
        // specified in our parameters. We also include the parameter languages
        // that will exist only in staging if they don't exist in active entries.
        Set<String> writeLanguageCodes = new HashSet<String>(languageCodes);
//        writeLanguageCodes.addAll (getActivePageInfos().keySet ());
//        writeLanguageCodes.removeAll (getStagingPageInfos().keySet ());
//        writeLanguageCodes.addAll (languageCodes);
        Map<String, JahiaPageInfo> activePageInfos = getActivePageInfos();
        Map<String, JahiaPageInfo> stagingPageInfos = getStagingPageInfos();
        // now that the language set is fully determined, let's create the
        // necessary entries in staging.
        int currentStatus = getMostRecentStagingStatus();
        for (String curLanguageCode : writeLanguageCodes) {
            boolean foundCurLanguage = false;
            // let's see if a currentInfo exist

            if (stagingPageInfos.containsKey(curLanguageCode)) {
                foundCurLanguage = true;
            }

            if (!foundCurLanguage) {
                // the current language doesn't exist in staging mode, does it
                // exist in active mode ?
                if (activePageInfos.containsKey(curLanguageCode)) {
                    // it does, let's copy from there
                    JahiaPageInfo sourceActiveInfo = (JahiaPageInfo) activePageInfos.get(
                            curLanguageCode);
                    JahiaPageInfo newStagingInfo = sourceActiveInfo.clonePageInfo(0,
                            currentStatus, sourceActiveInfo.getLanguageCode());
                    pageManager.createPageInfo(newStagingInfo);
                    addPageInfo(newStagingInfo);
                } else {
                    // it doesn't, let's create a copy from another language version

                    JahiaPageInfo sourceInfo = null;
                    // first we must know if there are staging versions we can
                    // copy the data from :
                    if (stagingPageInfos.isEmpty()) {
                        // no staging page infos yet, we must copy from the active
                        // versions

                        /*
                        List locales = jParams.getEntryLoadRequest().getLocales();
                        ListIterator localesIter = locales.listIterator();
                        while (localesIter.hasNext()) {
                            Locale curLocale = (Locale) localesIter.next();
                            JahiaPageInfo curPageInfo = (JahiaPageInfo) mActivePageInfos.get(curLocale.toString());
                            if (curPageInfo != null) {
                                sourceInfo = curPageInfo;
                            }
                        }
                        */

                        // NK : We just need to copy an entry no matter what language it is ?
                        if (!activePageInfos.isEmpty()) {
                            sourceInfo =
                                    (JahiaPageInfo) activePageInfos.values().iterator().next();
                        }

                    } else {

                        // there are staging page infos, let's determine which one
                        // we must copy from.
                        /*
                        List locales = jParams.getEntryLoadRequest().getLocales();
                        ListIterator localesIter = locales.listIterator();
                        while (localesIter.hasNext()) {
                            Locale curLocale = (Locale) localesIter.next();
                            JahiaPageInfo curPageInfo = getStagingPageInfos().get(curLocale.toString());
                            if (curPageInfo != null) {
                                sourceInfo = curPageInfo;
                            }
                        }
                        */

                        // NK : We just need to copy an entry no matter what language it is ?
                        if (!stagingPageInfos.isEmpty()) {
                            sourceInfo =
                                    (JahiaPageInfo) stagingPageInfos.values().iterator().next();
                        }
                    }
                    if (sourceInfo != null) {
                        JahiaPageInfo newStagingInfo = sourceInfo.clonePageInfo(0,
                                currentStatus, curLanguageCode);
                        newStagingInfo.setTitle(sourceInfo.getTitle());
                        // NK : Setting the remote URL will change the page Type !
                        // as we clone it no reason to set it.
                        //newStagingInfo.setRemoteURL(sourceInfo.getRemoteURL());
                        pageManager.createPageInfo(newStagingInfo);
                        addPageInfo(newStagingInfo);
                    } else {
                        logger.debug("Couldn't create page (" + getID() +
                                ")content entry for language " +
                                curLanguageCode +
                                " because a source language couldn't be found.");
                    }
                }
            }
        }

        pageInfos.addAll(stagingPageInfos.values());
        return pageInfos;
    }

    /**
     * Retrieves just one JahiaPageInfo for a writing operation. This is used
     * for non-shared language values such as the page title. It also creates
     * a language entry if it didn't exist previsouly.
     *
     * @return a JahiaPageInfo corresponding to the desired language in staging
     *         mode and ready for writing.
     * @throws JahiaException thrown if we couldn't find or generate a language
     *                        for some reason.
     */
    private JahiaPageInfo getPageInfoVersionForWrite(EntryLoadRequest loadRequest)
            throws JahiaException {
        Set<String> langs = new HashSet<String>();
        langs.add(loadRequest.getFirstLocale(true).toString());
        getAllPageInfosForWrite(langs);
        JahiaPageInfo resultInfo = getStagingPageInfos().get(
                loadRequest.getFirstLocale(true).toString());
        if (resultInfo == null) {
            throw new JahiaException("ContentPage.getPageInfoForWrite",
                    "Couldn't find staging page data for language " +
                            loadRequest.getFirstLocale(true).toString(),
                    JahiaException.PAGE_ERROR,
                    JahiaException.ERROR_SEVERITY);
        }

        return resultInfo;

    }

    /**
     * The purpose of this method is to "activate" all the data that is in the
     * staging state. This destroys all internal staging entries so make sure
     * you call this only when really ready to active. For changing staging
     * status use the other method : setWorkflowState also in this class.
     *
     * @param languageCodes     specifies for which languages to activate. All the
     *                          languages that are not specified here will not change state
     * @param versioningActive  specifies whether the versioning system is
     *                          active on this site
     * @param saveVersion       version save information passed to all the content
     *                          of the page for activation.
     * @param user              the user making the validation, using for validation the
     *                          content of the page
     * @param jParams           a ProcessingContext object used in sub-object (todo : find exactly
     *                          what it is used for !)
     * @param stateModifContext contains the current context of the activation,
     *                          including the current tree path stack and options that indicate how
     *                          the activation should be processed such as whether to recursively descend
     *                          in sub pages.
     * @return true if the page was successfully validated, false if it could
     *         not be validated due to it's dependency on another page or content that
     *         links to other pages.
     * @throws JahiaException if there was a problem while validating the
     *                        content of the page.
     */
    public ActivationTestResults activate(
            Set<String> languageCodes,
            boolean versioningActive, JahiaSaveVersion saveVersion,
            JahiaUser user,
            ProcessingContext jParams,
            StateModificationContext stateModifContext)
            throws JahiaException {

        boolean stateModified = false;
        if (isMarkedForDelete()) {
            stateModified = true;
            stateModifContext.pushAllLanguages(true);
            this.removeProperty(PageProperty.PAGE_URL_KEY_PROPNAME);
        }

        Set<String> activateLanguageCodes = new HashSet<String>(languageCodes);
        if (stateModifContext.isAllLanguages()) {
            activateLanguageCodes.addAll(getStagingLanguages(true));
        }

        ActivationTestResults activationResults = new ActivationTestResults();

        activateLanguageCodes.retainAll(getStagingPageInfos().keySet());
        if (activateLanguageCodes.isEmpty()) {
            return activationResults;
        }

        activationResults.merge(
                isValidForActivation(activateLanguageCodes,
                        jParams, stateModifContext));
        activationResults.merge(
                isPickedValidForActivation(activateLanguageCodes, stateModifContext));

        if (activationResults.getStatus() == ActivationTestResults.FAILED_OPERATION_STATUS) {
            if (stateModified) {
                stateModifContext.popAllLanguages();
            }
            return activationResults;
        }

        boolean stacked = false;

        // the first operation is to check if we validate the object or not,
        // depending on whether the objects pointed by this one are validated
        // or not. In the case of internal links we must check that the
        // page link has been validated or not, and depending on the parameters,
        // whether to validate it first too.
        int pageType;

        // FIXME NK : we should get the staged entry first should we ?
        Iterator<JahiaPageInfo> pageInfoIter = getStagingPageInfos().values().iterator();
        if (!pageInfoIter.hasNext()) {
            pageInfoIter = this.getActivePageInfos().values().iterator();
        }

        if (pageInfoIter.hasNext()) {
            JahiaPageInfo curPageInfo = pageInfoIter.next();
            pageType = curPageInfo.getPageType();
            if (pageType == JahiaPage.TYPE_LINK) {
                logger.debug("Activating link page object (id=" +
                        curPageInfo.getID() + ") to page ID : " +
                        curPageInfo.getPageLinkID());
            }
        }

        // Invalidate the JahiaPageCacheInfo
        this.commitChanges(true, false, user);
        rebuildStatusMaps();

        int sameParentID = hasSameParentID();
        if (!stateModifContext.isAllLanguages() && (sameParentID != SAME_PARENT)) {
            logger.debug(
                    "Activation of move detected for page " + getID() + ", activating all languages...");
            activateLanguageCodes.addAll(getStagingLanguages(true));
        }

        // NicolÃ¡s Charczewski - Neoris Argentina - modified 31/03/2006 - Begin
        boolean nonDeleted = activeNonDeletedEntries(activateLanguageCodes, saveVersion, jParams);
        // NicolÃ¡s Charczewski - Neoris Argentina - modified  31/03/2006 - End

        if (nonDeleted) {
            // if the activation was at least partially performed, we must now
            // also activate all the content that points on this page.
            activeReferringContent(languageCodes, saveVersion, user,
                    jParams, stateModifContext, activationResults, versioningActive);
        }

        boolean deletedEntries = activeDeletedEntries(activateLanguageCodes, saveVersion);
        if (deletedEntries) {
            // some entries have been deleted, need to reactivate referring page fields
            activeReferringContent(languageCodes, saveVersion, user,
                    jParams, stateModifContext, activationResults, versioningActive);
        }

        if (stacked) {
            stateModifContext.popObjectID();
        }

        if (stateModified) {
            stateModifContext.popAllLanguages();
        }

        // Invalidate the JahiaPageCacheInfo
        // todo : create a singleton for all pageInfo !!
        this.commitChanges(true, false, user);

        fireContentActivationEvent(activateLanguageCodes,
                versioningActive,
                saveVersion,
                jParams,
                stateModifContext,
                activationResults);

        syncClusterOnValidation();

        return activationResults;

    }

    // Nicolás Charczewski - Neoris Argentina - modified 07/04/2006
    // Added parameter jParams
    private boolean activeNonDeletedEntries(Set<String> languageCodes,
                                            JahiaSaveVersion saveVersion, ProcessingContext jParams) {
        boolean result = false;
        // now we must find which active versions to backup, by figuring
        // out what has changed. Here we only do the activation parts. The
        // deletion parts will be done later, because they must be done
        // in the end since we might have no references left to page info
        // objects after deletion.
        for (JahiaPageInfo curPageInfo : new ArrayList<JahiaPageInfo>(getStagingPageInfos().values())) {
            // only do the activation for the request languages.
            if (languageCodes.contains(curPageInfo.getLanguageCode())) {
                int newVersionStatus = 0;
                if (curPageInfo.getVersionID() != -1) {
                    result = true;

                    // now le'ts find the active page info corresponding to this
                    // staged page info.
                    JahiaPageInfo activeInfo = getActivePageInfos().get(curPageInfo.getLanguageCode());

                    if (activeInfo != null) {

                        // create a versioned version of the old active entry
                        //pageManager.
                        pageManager.updatePageInfo(
                                activeInfo,
                                activeInfo.getVersionID(),
                                newVersionStatus);

                        removePageInfo(activeInfo);

                        if (activeInfo.getParentID() != curPageInfo.getParentID()) {
                            WorkflowService.getInstance().flushCacheForPageCreatedOrDeleted(new ContentPageKey(activeInfo.getParentID()));
                        }
                    }

                    removePageInfo(curPageInfo);
                    // not marked for deletion, let's activate it.
                    pageManager.updatePageInfo(
                            curPageInfo,
                            saveVersion.getVersionID(),
                            EntryLoadRequest.ACTIVE_WORKFLOW_STATE);

                    curPageInfo.setVersionID(saveVersion.getVersionID());
                    curPageInfo.setVersionStatus(EntryLoadRequest.
                            ACTIVE_WORKFLOW_STATE);

                    addPageInfo(curPageInfo);

                    // Nicolás Charczewski - Neoris Argentina - added 31/03/2006 - Begin
                    try {
                        JahiaEvent je = new JahiaEvent(this, jParams, curPageInfo);
                        JahiaEventGeneratorBaseService.getInstance().fireAcceptPage(je);
                    } catch (JahiaException e) {
                        logger.warn("Exception while firing event", e);
                    }
                    // Nicolás Charczewski - Neoris Argentina - added 31/03/2006 - End

                }
            }
        }
        return result;
    }


    private void activeReferringContent(Set<String> languageCodes,
                                        JahiaSaveVersion saveVersion,
                                        JahiaUser user, ProcessingContext jParams,
                                        StateModificationContext
                                                stateModifContext,
                                        ActivationTestResults activationResults, boolean versioningActive)
            throws JahiaException {
        /**
         * todo we might be missing code to handle the "activation" of
         * deleted pages AND the content pointing on this page (ie broken
         * links that have to be navigated up to destroy the content)
         */
        if (activationResults.getStatus() !=
                ActivationTestResults.FAILED_OPERATION_STATUS
                && (this.getPageType(jParams.getEntryLoadRequest()) != JahiaPage.TYPE_URL)
                && (this.getPageType(jParams.getEntryLoadRequest()) != JahiaPage.TYPE_LINK)) {
            // now that we've activate the page entry, let's activate all the
            // related content on other pages.
            List<JahiaPage> pages = getPageService().getPagesPointingOnPage(getID(), jParams);
            for (JahiaPage curPage : pages) {
                /*
                Set curPageStagingFieldIDs =
                    JahiaPageUtilsDB.getInstance().getStagingPageFieldIDs(curPage.getID());
                */
                List<Integer> curPointingPageFieldIDs = pageManager.getStagingAndActivePageFieldIDs(curPage.getID());

                for (Integer curFieldIDObj : curPointingPageFieldIDs) {
                    int curFieldID = curFieldIDObj.intValue();
                    if (curFieldID != -1) {
                        ContentField curPageField = ContentField.getField(curFieldID);
                        if (curPageField == null) {
                            logger.debug("Couldn't find page field " +
                                    curFieldID +
                                    " pointing on page " + getID() +
                                    ", ignoring it...");
                        } else {

                            boolean notDeleted = (curPageField.hasActiveEntries()
                                    ||
                                    curPageField.hasStagingEntries());

                            // we must now activate the field.
                            stateModifContext.setDescendingInSubPages(false);
                            boolean pageFieldActivationResult = true;
                            if (curPageField.hasStagingEntries()) {
                                // fire event
                                JahiaEvent theEvent = new JahiaEvent(
                                        saveVersion, jParams, curPageField);
                                ServicesRegistry.getInstance()
                                        .getJahiaEventService()
                                        .fireBeforeFieldActivation(theEvent);
                                // end fire event
                                ActivationTestResults fieldActivationResult =
                                        curPageField.activate(
                                                languageCodes,
                                                saveVersion.getVersionID(),
                                                jParams,
                                                stateModifContext);
                                activationResults.merge(fieldActivationResult);
                                pageFieldActivationResult = (fieldActivationResult.getStatus() ==
                                        ActivationTestResults.COMPLETED_OPERATION_STATUS);

                            }
                            if (pageFieldActivationResult) {

                                if ((curPageField.getContainerID() > 0) &&
                                        notDeleted) {
                                    // field is inside a container.
                                    /**
                                     * todo we might want to check this code
                                     * to make sure we load the correct entry
                                     * state here
                                     */
                                    JahiaContainer curPageFieldContainer =
                                            ServicesRegistry.getInstance().
                                                    getJahiaContainersService().
                                                    loadContainer(
                                                            curPageField.getContainerID(),
                                                            LoadFlags.ALL, jParams,
                                                            jParams.getEntryLoadRequest());
                                    if (curPageFieldContainer == null) {
                                        logger.debug(
                                                "Error loading container (" +
                                                        curPageField.getContainerID() +
                                                        ") for a page field (" +
                                                        curFieldID +
                                                        ") that points on page " + getID());
                                    } else {
                                        // container was successfully loaded.
                                        // we must now try to activate it.
                                        stateModifContext.
                                                setDescendingInSubPages(false);

                                        // we must now activate all the other
                                        // field in the container first, and
                                        // then validate the container itself.
                                        EntryLoadRequest loadRequest = new EntryLoadRequest(
                                                EntryLoadRequest.STAGED);
                                        loadRequest.setWithMarkedForDeletion(true);

                                        List<Integer> fieldIDs = ServicesRegistry.
                                                getInstance().
                                                getJahiaContainersService().
                                                getFieldIDsInContainer(
                                                        curPageFieldContainer.getID(),
                                                        loadRequest);

                                        for (Integer fieldIDObj : fieldIDs) {
                                            int fieldID = fieldIDObj.intValue();
                                            if (fieldID != curFieldID) {
                                                ContentField currentField =
                                                        ContentField.getField(
                                                                fieldID);

                                                // fire event
                                                JahiaEvent theEvent = new JahiaEvent(
                                                        saveVersion, jParams, currentField);
                                                ServicesRegistry.getInstance()
                                                        .getJahiaEventService()
                                                        .fireBeforeFieldActivation(theEvent);

                                                activationResults.merge(
                                                        currentField.activate(
                                                                languageCodes,
                                                                saveVersion.getVersionID(), jParams,
                                                                stateModifContext));
                                            }
                                        }

                                        // now that we have activated, or at
                                        // least tried to activate all the
                                        // fields, we must activate the
                                        // container itself.

                                        ActivationTestResults
                                                containerActivationResult =
                                                curPageFieldContainer.getContentContainer().activate(languageCodes, true, saveVersion, user, jParams, stateModifContext);

                                        activationResults.merge(
                                                containerActivationResult);
                                    }
                                } else {
                                    // field is not in a container. nothing more to do...
                                }

                            }
                        }
                    }
                }
            }
        }
    }

    private boolean activeDeletedEntries(Set<String> languageCodes,
                                         JahiaSaveVersion saveVersion) {
        boolean result = false;
        // now we must find which active versions to backup, by figuring
        // out what has changed.
        for (JahiaPageInfo curPageInfo : new ArrayList<JahiaPageInfo>(getStagingPageInfos().values())) {
            // only do the activation for the request languages.
            if (languageCodes.contains(curPageInfo.getLanguageCode())) {
                if (curPageInfo.getVersionID() == -1) {
                    result = true;

                    // now le'ts find the active page info corresponding to this
                    // staged page info.
                    JahiaPageInfo activeInfo = getActivePageInfos().get(curPageInfo.getLanguageCode());

                    if (activeInfo != null) {
                        // create a versioned version of the old active entry
                        // First, we create an archive entry ( newVersionStatus = 0 , not -1 ! )
                        pageManager.updatePageInfo(
                                activeInfo,
                                activeInfo.getVersionID(),
                                ContentObjectEntryState.WORKFLOW_STATE_VERSIONED);

                        removePageInfo(activeInfo);

                        // Third, we replace the active with the staging entry
                        pageManager.updatePageInfo(curPageInfo,
                                saveVersion.getVersionID(),
                                ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED);
                        WorkflowService.getInstance().flushCacheForPageCreatedOrDeleted((ContentPageKey) getObjectKey());
                        JahiaPageInfo deletedPageInfo =
                                curPageInfo.clonePageInfo(saveVersion.getVersionID(),
                                        ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED,
                                        curPageInfo.getLanguageCode());

                        // ensure to load all versioning entryStates
                        this.loadVersioningEntryStates();

                        addPageInfo(deletedPageInfo);

                        // reset site map
                        ServicesRegistry.getInstance().getJahiaSiteMapService()
                                .resetSiteMap();

                    }

                    removePageInfo(curPageInfo);
                    // marked for deletion, let's remove the staged version
                    // too...
                    pageManager.deletePageInfo(curPageInfo);
                }
            }
        }
        return result;
    }


    /**
     * Tests if a page is valid for activation.
     *
     * @param languageCodes     a Set of languages for which to test if the page
     *                          is valid for activation.
     * @param jParams
     * @param stateModifContext contains the current context of the activation,
     *                          including the current tree path stack and options that indicate how
     *                          the activation should be processed such as whether to recursively descend
     *                          in sub pages.
     * @return an ActivationTestResults object that contains the status of
     *         the activation tests, including error and warning messages.
     * @throws JahiaException
     */
    public ActivationTestResults isValidForActivation(
            Set<String> languageCodes,
            ProcessingContext jParams,
            StateModificationContext stateModifContext)
            throws JahiaException {
        ActivationTestResults activationTestResults = super.isValidForActivation(languageCodes, jParams, stateModifContext);
        // first we must test if we have all the mandatory languages in our
        // page only if the page is not marked for deletion.
        ContentObjectKey mainKey = (ContentObjectKey) ServicesRegistry.getInstance().getWorkflowService().getMainLinkObject((ContentObjectKey) getObjectKey());
        if (!isMarkedForDelete()) {
            int pageType = getPageType(EntryLoadRequest.STAGED);
            JahiaSite theSite = jParams.getSite();
            Map<String, SiteLanguageSettings> siteLanguageSettings = new HashMap<String, SiteLanguageSettings>(languageCodes.size());
            final boolean isAdminMember = jParams.getUser().isAdminMember(jParams.getSiteID());
            List<SiteLanguageSettings> languageSettings = theSite.getLanguageSettings(true);
            for (SiteLanguageSettings curSettings : languageSettings) {
                siteLanguageSettings.put(curSettings.getCode(), curSettings);
            }
            for (SiteLanguageSettings curSettings : languageSettings) {
                if (curSettings.isMandatory()) {
                    // we found a mandatory language, let's check that there is at
                    // least an active or a staged entry for this field.
                    JahiaPageInfo foundPageInfo = getStagingPageInfos().get(curSettings.getCode());
                    if (foundPageInfo == null) {
                        foundPageInfo = getActivePageInfos().get(curSettings.getCode());
                    }

                    if (foundPageInfo == null) {
                        activationTestResults.mergeStatus(!isAdminMember ? ActivationTestResults.FAILED_OPERATION_STATUS : ActivationTestResults.PARTIAL_OPERATION_STATUS);
                        try {
                            final EngineMessage msg = new EngineMessage("org.jahia.services.pages.ContentPage.mandatoryLangMissingError", curSettings.getCode());
                            for (String code : languageCodes) {
                                if (!code.equals(curSettings.getCode())) {
                                    IsValidForActivationResults activationResults = new
                                            IsValidForActivationResults(mainKey,code, msg);
                                    if (!isAdminMember) {
                                        activationResults.setBlocker(true);
                                    }
                                    activationTestResults.appendError(activationResults);
                                }
                            }
                        } catch (ClassNotFoundException cnfe) {
                            logger.debug("Error while creating activation test node result", cnfe);
                        }
                    }
                    
                    if (!languageCodes.contains(curSettings.getCode())
                            && getActivePageInfos().get(curSettings.getCode()) == null) {
                        activationTestResults
                                .mergeStatus(!isAdminMember ? ActivationTestResults.FAILED_OPERATION_STATUS : ActivationTestResults.PARTIAL_OPERATION_STATUS);
                        try {
                            for (String code : languageCodes) {
                                if (!code.equals(curSettings.getCode())) {
                                    EngineMessage engineMessage = new EngineMessage("org.jahia.services.pages.ContentPage.mandatoryLangNotPublished", curSettings.getCode());
                                    activationTestResults.appendError(new IsValidForActivationResults(mainKey,code,engineMessage));
                                }
                            }
                        } catch (ClassNotFoundException e) {
                            logger
                                    .debug(
                                            "Error while creating activation test node result",
                                            e);
                        }
                        
                    }
                }
            }
            
            // we stop if the status is already 'failed'
            if (activationTestResults.getStatus() != ActivationTestResults.FAILED_OPERATION_STATUS) {
                // let's check that all the page titles exist in the languages to be
                // validated.
                if (getPageType(jParams.getEntryLoadRequest()) == JahiaPage.TYPE_DIRECT) {
                    boolean oneLanguageSet = false;
                    JahiaPageInfo curPageInfo = null;
                    for (Iterator<String> languageCodeIter = languageCodes.iterator(); languageCodeIter.hasNext();) {
                        String curLanguageCode = languageCodeIter.next();
                        curPageInfo = getStagingPageInfos().get(curLanguageCode);
                        if (curPageInfo == null) {
                            // no staging page info found, let's try in active...
                            curPageInfo = getActivePageInfos().get(curLanguageCode);
                        }
                        if (curPageInfo == null || curPageInfo.getTitle() == null) {
                            boolean b = "shared".equals(curLanguageCode.toLowerCase().trim());
                            // do not treat shared as even if you have "shared" the title page infos will return the same title
                            // for each language and the "shared" language do not exist in this case.
                            // The title is replciated among all languages so avoid testing on shared.
                            if (!b) {
                                try {
                                    final EngineMessage msg =
                                            new EngineMessage("org.jahia.services.pages.ContentPage.noTitleError");
                                    IsValidForActivationResults forActivationResults =
                                            new IsValidForActivationResults(mainKey,curLanguageCode,msg);
                                    activationTestResults.mergeStatus(ActivationTestResults.PARTIAL_OPERATION_STATUS);
                                    activationTestResults.appendError(forActivationResults);
                                } catch (ClassNotFoundException cnfe) {
                                    logger.debug("Error while creating activation test node result", cnfe);
                                }
                            }
                        } else if (curPageInfo.getTitle().trim().length() == 0) {
                            // empty title, let's signal it if mixed language mode is not activated
                            if (!theSite.isMixLanguagesActive()) {
                                activationTestResults.mergeStatus(ActivationTestResults.PARTIAL_OPERATION_STATUS);
                                try {
                                    final EngineMessage msg = new EngineMessage(
                                            "org.jahia.services.pages.ContentPage.emptyTitleError");
                                    IsValidForActivationResults forActivationResults = new IsValidForActivationResults(
                                          mainKey, curPageInfo.getLanguageCode(), msg);
                                    activationTestResults.appendError(forActivationResults);
                                } catch (ClassNotFoundException cnfe) {
                                    logger.debug("Error while creating activation test node result", cnfe);
                                }
                            }
                        } else {
                            oneLanguageSet = true;
                        }
                    }
                    if (theSite.isMixLanguagesActive() && !oneLanguageSet) {
                        activationTestResults.mergeStatus(ActivationTestResults.PARTIAL_OPERATION_STATUS);
                        try {
                            final EngineMessage msg = new EngineMessage(
                                    "org.jahia.services.pages.ContentPage.emptyTitleError");
                            IsValidForActivationResults forActivationResults = new IsValidForActivationResults(
                                 mainKey, curPageInfo.getLanguageCode(), msg);
                            forActivationResults.setBlocker(true);
                            activationTestResults.appendError(forActivationResults);
                        } catch (ClassNotFoundException cnfe) {
                            logger.debug("Error while creating activation test node result", cnfe);
                        }
                    }
                }
    
                // the next operation is to check if we validate the object or not,
                // depending on whether the objects pointed by this one are validated
                // or not. In the case of internal links we must check that the
                // page link has been validated or not, and depending on the parameters,
                // whether to validate it first too.
                pageType = getPageType(jParams.getEntryLoadRequest());
                if (pageType == JahiaPage.TYPE_LINK) {
                    JahiaPageInfo curPageInfo = getPageInfoVersionIgnoreLanguage(jParams.getEntryLoadRequest(), false);
                    int pageLinkId = curPageInfo.getPageLinkID();
                    ContentPage linkedPage = null;
                    try {
                        getPageService().lookupContentPage(pageLinkId, true);
                    } catch (JahiaPageNotFoundException jpnfe) {
                        linkedPage = null;
                        try {
                            final EngineMessage msg = new EngineMessage(
                                    "org.jahia.services.pages.ContentPage.linkNotToPageWarning");
                            activationTestResults.appendWarning(new IsValidForActivationResults(mainKey, curPageInfo.getLanguageCode(), msg));
                        } catch (ClassNotFoundException cnfe) {
                            logger.debug("Error while creating activation test node result", cnfe);
                        }
                    }
                    if (linkedPage != null) {
                        if (!linkedPage.hasActiveEntries()) {
                            logger.debug("Cannot validate page link " + curPageInfo.getID() + "since it links to a page ("
                                    + pageLinkId + ") with no active entries");
                            activationTestResults.mergeStatus(ActivationTestResults.PARTIAL_OPERATION_STATUS);
                            try {
                                final EngineMessage msg = new EngineMessage(
                                        "org.jahia.services.pages.ContentPage.linkNotToActivePageError");
                                activationTestResults.appendError(new IsValidForActivationResults(mainKey, curPageInfo.getLanguageCode(), msg));
                            } catch (ClassNotFoundException cnfe) {
                                logger.debug("Error while creating activation test node result", cnfe);
                            }
                        }
                    }
                } else if (pageType == JahiaPage.TYPE_DIRECT) {
                    // int siteID = getJahiaID();
                    // ServicesRegistry sr = ServicesRegistry.getInstance();
                    // we should modify this call to make it only test the fields
                    // that are directly attached to the page, and not in
                    // containers, otherwise we will
                    // get doubles when testing the containers.
                    // activationTestResults.merge(sr.getJahiaFieldService().areFieldsValidForActivation(
                    // languageCodes, getID(), user, saveVersion, jParams,
                    // withSubPages ));
    // toto : do not recursively check other content, this is now externalized in workflowservice
    /*
                    activationTestResults.merge (
                            sr.getJahiaFieldService ().areNonContainerFieldsValidForActivation (
                                    languageCodes, getID (), user, saveVersion, jParams,
                                    stateModifContext));
                    activationTestResults.merge (
                            sr.getJahiaContainersService ().areContainersValidForActivation (
                                    languageCodes, getID (), user, saveVersion, jParams,
                                    stateModifContext));
                    activationTestResults.merge (
                            sr.getJahiaContainersService ().areContainerListsValidForActivation (
                                    languageCodes, getID (), user, saveVersion, stateModifContext));
                                    */
                }
            }
        }

        return activationTestResults;
    }

    /**
     * Changes the status of the staging page infos. This is used to switch
     * to another status, before going to active status. No versioning is
     * done during a staging status change.
     *
     * @param languageCodes    a set of language codes for which we want to
     *                         change the state. Other languages will not change state.
     * @param newWorkflowState the new status mode. This must be bigger or
     *                         equal to JahiaLoadVersion.STAGING, otherwise this method will exist
     *                         immediately.
     * @param jParams          a ProcessingContext used in sub content during the changing
     *                         state (todo : find exactly what it is used for)
     * @throws JahiaException in the case there are problems interacting with
     *                        the persistant store while changing the state
     */
    public void setWorkflowState(Set<String> languageCodes, int newWorkflowState,
                                 ProcessingContext jParams,
                                 StateModificationContext stateModifContext)
            throws JahiaException {
        if (newWorkflowState < EntryLoadRequest.STAGING_WORKFLOW_STATE) {
            return;
        }

        Set<String> processedLangs = new HashSet<String>();
        try {
            for (JahiaPageInfo curPageInfo : getStagingPageInfos().values()) {
                if (languageCodes.contains(curPageInfo.getLanguageCode())) {
                    processedLangs.add(curPageInfo.getLanguageCode());
                    pageManager.updatePageInfo(curPageInfo,
                            curPageInfo.getVersionID(), newWorkflowState);

                    // NicolÃ¡s Charczewski - Neoris Argentina - added 31/03/2006 - Begin
                    int lastWorkflowState = curPageInfo.getWorkflowState();
                    curPageInfo.setVersionStatus(newWorkflowState);
                    if (lastWorkflowState == EntryLoadRequest.WAITING_WORKFLOW_STATE &&
                            newWorkflowState == EntryLoadRequest.STAGING_WORKFLOW_STATE) {
                        JahiaEvent je = new JahiaEvent(this, jParams, curPageInfo);
                        JahiaEventGeneratorBaseService.getInstance().fireRejectPage(je);
                    }
                    // NicolÃ¡s Charczewski - Neoris Argentina - added 31/03/2006 - End

                }
            }

            // we create an entry for missing language
            for (Iterator<String> stagingIter = languageCodes.iterator(); stagingIter.hasNext();) {
                String lang = stagingIter.next();
                if (!processedLangs.contains(lang)) {
                    // the staging does'nt exist in the given language, so
                    // we directly create one entry in the new workflow state
                    Iterator<ContentObjectEntryState> iterator = getActiveAndStagingEntryStates().iterator();
                    while (iterator.hasNext()) {
                        ContentObjectEntryState fromEntryState = iterator.next();
                        if (fromEntryState.getLanguageCode().equals(lang)) {
                            ContentObjectEntryState toEntryState =
                                    new ContentObjectEntryState(newWorkflowState, 0, lang);
                            this.copyEntry(fromEntryState, toEntryState);
                        }
                    }
                }
            }
        } catch (JahiaException je) {
            logger.debug("Error while trying to change the status of a staging version", je);
        }

        // Invalidate the JahiaPageCacheInfo
        this.commitChanges(true, false, jParams.getUser());
        rebuildStatusMaps();

    }

    /**
     * Marks a page's languages content for deletion.
     * Does not delete the languages entries until the page is published.
     * This method also goes done in the content to flag all the content
     * for deletion along with the page in the given set of languages.
     *
     * @param user              the user performing the operation, in order to perform
     *                          rights checks
     * @param languageCode      the language to mark for deletion
     * @param stateModifContext contains the start object of the
     *                          operation, as well as settings such as recursive descending in sub pages,
     *                          and content object stack trace.
     * @throws JahiaException thrown in the case we have trouble creating the
     *                        staged version to flag for deletion.
     */
    public void markLanguageForDeletion(JahiaUser user,
                                        String languageCode,
                                        StateModificationContext stateModifContext)
            throws JahiaException {
        boolean stateModified = false;
        Set<String> languageCodes = new HashSet<String>();
        if (stateModifContext.isAllLanguages()) {
            languageCodes.addAll(getStagingLanguages(false));
        } else if (willBeCompletelyDeleted(languageCode, null)) {
            stateModified = true;
            stateModifContext.pushAllLanguages(true);
        }

        if (languageCode != null
                && !ContentObject.SHARED_LANGUAGE.equals(languageCode)) {
            // otherwise we will create unwanted shared lang staged entry !!!!
            // should throws Exception here ?
            languageCodes.add(languageCode);
        }
        if (stateModified && stateModifContext.isAllLanguages()) {
            languageCodes.addAll(getStagingLanguages(false));
        }

        List<JahiaPageInfo> allStagingPageInfos = getAllPageInfosForWrite(languageCodes);

        ContentPageKey pageKey = new ContentPageKey(getID());
        boolean stacked = false;

        ServicesRegistry sr = ServicesRegistry.getInstance();

        EntryLoadRequest loadRequest =
                new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE, 0,
                        new ArrayList<Locale>());
        switch (getPageType(loadRequest)) {

            case JahiaPage.TYPE_DIRECT:

                // we must now mark all page related content for deletion, that is to
                // say :

                // 1. all fields on the page
                sr.getJahiaFieldService().markPageFieldsLanguageForDeletion(getID(),
                        user, languageCode, stateModifContext);

                // 2. all container lists on the page
                sr.getJahiaContainersService().
                        markPageContainerListsLanguageForDeletion(
                                getID(),
                                user,
                                languageCode,
                                stateModifContext);

                stateModifContext.pushObjectID(pageKey);
                stacked = true;

                // 3. all links on this page, including the fields they are in AND
                // the containers the fields are in.
                if (stateModifContext.isAllLanguages()) {
                    // we can only mark for delete page field, if the page is deleted in all langs.
                    markReferringContentForDeletion(user, languageCode,
                            stateModifContext);
                }

                break;
            case JahiaPage.TYPE_LINK:
                break;
            case JahiaPage.TYPE_URL:
                break;
        }

        // first we must check if there are active versions for the page info
        // data. If not, we simply delete directly the existing staging entries
        for (JahiaPageInfo curPageInfo : allStagingPageInfos) {
            if (languageCodes.contains(curPageInfo.getLanguageCode())) {
                Map<String, JahiaPageInfo> activePageInfos = getActivePageInfos();
                if (activePageInfos.containsKey(curPageInfo.getLanguageCode())) {
                    if (curPageInfo.getVersionID() != ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED) {

                        // Page Move issue here:
                        // switching to mark for delete without take care to undo
                        // any page move state create a moved-deleted-phantom page!!!
                        JahiaPageInfo activePageInfo = (JahiaPageInfo)
                                activePageInfos.get(curPageInfo.getLanguageCode());
                        curPageInfo.setParentID(activePageInfo.getParentID());
                        pageManager.updatePageInfo(curPageInfo, -1, curPageInfo.getWorkflowState());
                        curPageInfo.setVersionID(-1);
                        curPageInfo.commitChanges(false);
                    }
                } else {
                    JahiaEvent theEvent = new JahiaEvent(this, null, this);
                    ServicesRegistry.getInstance().getJahiaEventService().fireBeforeStagingContentIsDeleted(theEvent);

                    deleteEntry(curPageInfo);
                }
            }
        }

        if (stacked) {
            stateModifContext.popObjectID();
        }

        if (stateModified) {
            stateModifContext.popAllLanguages();
        }

        for (String currentLanguage : languageCodes) {
            WorkflowEvent theEvent = new WorkflowEvent(this, this, user, currentLanguage, true);
            ServicesRegistry.getInstance().getJahiaEventService().fireObjectChanged(theEvent);
        }

    }

    private void markReferringContentForDeletion(JahiaUser user,
                                                 String languageCode,
                                                 StateModificationContext
                                                         stateModifContext)
            throws JahiaException {

        List<Locale> locales = new ArrayList<Locale>();
        locales.add(LanguageCodeConverters.languageCodeToLocale(languageCode));

        EntryLoadRequest loadRequest = new EntryLoadRequest(
                EntryLoadRequest.STAGING_WORKFLOW_STATE, 0, locales);

        List<JahiaPage> referingPages = getPageService().
                getPagesPointingOnPage(getID(), loadRequest);

        if (stateModifContext.getStartObject().equals(new ContentPageKey(getID()))) {
            // let's add our own page.
            referingPages.add(new JahiaPage(this, getPageTemplate(loadRequest),
                    getACL(),
                    loadRequest));
        }

        markReferringContentForDeletion(user, languageCode, stateModifContext, referingPages);
    }

    private void markReferringContentForDeletion(JahiaUser user,
                                                 String languageCode,
                                                 StateModificationContext
                                                         stateModifContext,
                                                 List<JahiaPage> referingPages)
            throws JahiaException {
        for (JahiaPage page : referingPages) {
            int curPageFieldID = pageManager.getPageFieldID(page.getID());
            if (curPageFieldID != -1) {
                markReferringPageFieldLanguageForDeletion(curPageFieldID, user, languageCode,
                        stateModifContext);
            }
        }
    }

    private void markReferringPageFieldLanguageForDeletion(int curPageFieldID,
                                                           JahiaUser user,
                                                           String languageCode,
                                                           StateModificationContext stateModifContext)
            throws JahiaException {
        ContentField curPageField =
                ContentField.getField(curPageFieldID);
        if (curPageField == null) {
            logger.debug("Couldn't find page field " +
                    curPageFieldID +
                    " pointing on page " + getID() +
                    ", ignoring it...");
        } else {
            // we must now mark this field for deletion.
            curPageField.markLanguageForDeletion(
                    user, languageCode, stateModifContext);

            if (curPageField.getContainerID() > 0) {
                // field is inside a container.
                try {
                    ContentContainer curPageFieldContainer =
                            ContentContainer.getContainer(curPageField.
                                    getContainerID());
                    if (curPageFieldContainer == null) {
                        logger.debug(
                                "Container (" +
                                        curPageField.getContainerID()
                                        +
                                        ") for a page field (" +
                                        curPageFieldID +
                                        ") that points on page " +
                                        getID() +
                                        " couldn't be found, it might have been completely deleted already");
                    } else {
                        // container was successfully loaded.
                        // we must now try to mark it for deletion.
                        curPageFieldContainer.markLanguageForDeletion(
                                user,
                                languageCode,
                                stateModifContext);
                    }
                } catch (JahiaException je) {
                    logger.debug(
                            "Container " + curPageField.getContainerID() + " not found in database. This is normal if the container has already previously been marked for deletion in the shared language.");
                }
            }
        }

    }

    /**
     * This method is used to determine if all the active entries of this
     * content's childs will be deleted once this object is activated.
     *
     * @param markDeletedLanguageCode an extra language to be removed, to add
     *                                testing before actually marking for deletion. This may be null in the
     *                                case we just want to test the current state.
     * @param activationLanguageCodes a set of language for which we are
     *                                currently activating. May be null if we want to test for all the
     *                                languages, but if specified will test if the object will be completly
     *                                deleted when activated with only those languages.
     * @return true if in the next activation there will be no active entries
     *         left.
     * @throws JahiaException in case there was a problem retrieving the
     *                        entry states from the database
     */
    public boolean willAllChildsBeCompletelyDeleted(JahiaUser user,
                                                    String markDeletedLanguageCode,
                                                    Set<String> activationLanguageCodes)
            throws JahiaException {
        // we are not descending in childs
        return true;
    }

    /**
     * Deletes all the page's staging entries. Does not go into page content
     * to delete related fields, etc...
     *
     * @param jParams used because we have to delete some data in some cases,
     *                such as pages, fields and containerlists which need the ProcessingContext
     *                instance mostly for Event triggering and pageID retrieval
     * @throws JahiaException in the case we have trouble communicating with
     *                        the backend persistant storage for deleting staging versions
     */
    public void undoStaging(ProcessingContext jParams) throws JahiaException {
        for (JahiaPageInfo curPageInfo : new ArrayList<JahiaPageInfo>(getStagingPageInfos().values())) {
            // the next call is slow !
            deleteEntry(curPageInfo);
        }

        ContentUndoStagingEvent jahiaEvent = new ContentUndoStagingEvent(this, getSiteID(), jParams);
        ServicesRegistry.getInstance().getJahiaEventService()
                .fireContentObjectUndoStaging(jahiaEvent);
    }

    private boolean hasEntry(EntryStateable entryState) {

        if (entryState.getWorkflowState() < ContentObjectEntryState.WORKFLOW_STATE_ACTIVE) {
            loadVersioningEntryStates();
        }
        for (JahiaPageInfo curPageInfo : getPageInfos()) {
            if ((curPageInfo.getWorkflowState() == entryState.getWorkflowState()) &&
                    (curPageInfo.getVersionID() == entryState.getVersionID()) &&
                    (curPageInfo.getLanguageCode().equals(entryState.getLanguageCode()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * This method is called when a entry should be copied into a new entry
     * it is called when an    old version -> active version   move occurs
     * This method should not write/change the DBValue, the service handles that.
     *
     * @param fromEntryState the entry state that is currently was in the database
     * @param toEntryState   the entry state that will be written to the database
     */
    public void copyEntry(EntryStateable fromEntryState,
                          EntryStateable toEntryState)
            throws JahiaException {
        boolean changed = false;
        if (hasEntry(toEntryState)) {
            deleteEntry(toEntryState);
            changed = true;
        }
        boolean hasFromEntryState = hasEntry(fromEntryState);
        if (hasFromEntryState) {
            changed = true;
            JahiaPageInfo pageInfo = pageManager.copyEntry(getID(), fromEntryState, toEntryState);
            if (pageInfo != null) {
                addPageInfo(pageInfo);
            } else {
                logger.warn("Entrystate (" + toEntryState
                        + ") has been deleted, but system failed to copy a new one instead from Entrystate ("
                        + fromEntryState + "). Stacktrace:");
                Thread.dumpStack();
            }
        }
        if (changed){
            getPageService().invalidatePageCache(getID());

            // invalidate sitemap cahe too
            ServicesRegistry.getInstance().getJahiaSiteMapService().resetSiteMap();
        }
    }

    /**
     * This method is called when an entry should be deleted for real.
     * It is called when a field is deleted, and versioning is disabled, or
     * when staging values are undone.
     * For a bigtext for instance, this method should delete the text file
     * corresponding to this field entry.
     * Warning : this method is used mostly for use from the ContentPageField
     * object ! It is not recommend to use this from any other system as this
     * really destroys data and might be very dangerous !
     *
     * @param deleteEntryState the entry state for which to delete the entry
     * @throws JahiaException thrown in the case that we have errors while
     *                        communicating with the persistant storage system
     */
    public void deleteEntry(EntryStateable deleteEntryState) throws JahiaException {
        JahiaPageInfo targetPageInfo;

        if (deleteEntryState.getWorkflowState() >= EntryLoadRequest.STAGING_WORKFLOW_STATE) {
            // case of a staged version
            targetPageInfo = getStagingPageInfos().get(deleteEntryState.getLanguageCode());
            if (targetPageInfo != null) {
                pageManager.deletePageInfo(targetPageInfo);
                removePageInfo(targetPageInfo);
            }
        } else if (deleteEntryState.getWorkflowState() == EntryLoadRequest.ACTIVE_WORKFLOW_STATE) {
            // case of an active version
            targetPageInfo = getActivePageInfos().get(deleteEntryState.getLanguageCode());
            if (targetPageInfo != null) {
                pageManager.deletePageInfo(targetPageInfo);
                removePageInfo(targetPageInfo);
            }
        } else {
            // case of an archived version
            // first we have to find the archived version, so we load all of them... (ahem)
            /** todo FIXME it would be better not to have to load all the versions... */
            List<Locale> locales = new ArrayList<Locale>();
            locales.add(
                    LanguageCodeConverters.languageCodeToLocale(
                            deleteEntryState.getLanguageCode()));
            EntryLoadRequest deleteEntryRequest = new EntryLoadRequest(
                    deleteEntryState.getWorkflowState(), deleteEntryState.getVersionID(),
                    locales);
            List<JahiaPageInfo> versionedPageInfos = pageManager.loadPageInfos(getID(), deleteEntryRequest);

            // now that we have the archived versions, let's find the one we want to delete
            for (JahiaPageInfo curPageInfo : versionedPageInfos) {
                if ((curPageInfo.getLanguageCode().equals(deleteEntryState.getLanguageCode())) &&
                        (curPageInfo.getWorkflowState() == deleteEntryState.getWorkflowState()) &&
                        (curPageInfo.getVersionID() == deleteEntryState.getVersionID())) {
                    // we found the archived version to delete, let's do it.
                    targetPageInfo = curPageInfo;
                    pageManager.deletePageInfo(curPageInfo);
                    removePageInfo(curPageInfo);
                    break;
                }
            }
        }
        getPageService().invalidatePageCache(getID());
        // invalidate sitemap cahe too
        ServicesRegistry.getInstance().getJahiaSiteMapService().resetSiteMap();
    }

    /**
     * Returns true if the page has active entries
     *
     * @return true if their are entries in an active workflowState for this
     *         page
     */
    public boolean hasActiveEntries() {
        return !getActivePageInfos().isEmpty();
    }

    /**
     * Returns true if the page has staging entries
     *
     * @return true if their are entries in an staging workflowState for this
     *         page
     */
    public boolean hasStagingEntries() {
        return !getStagingPageInfos().isEmpty();
    }

    /**
     * Use these constants in association with the 'hasEntries' methods to set
     * which kind of page infos are contained in the entries.
     */
    public static final int ACTIVE_PAGE_INFOS = 0x01;
    public static final int STAGING_PAGE_INFOS = 0x02;
    public static final int ARCHIVED_PAGE_INFOS = 0x04;

    /**
     * Return true if the page has specified 'pageInfos' entries in the specified
     * language.
     *
     * @param pageInfosFlag Kind of page infos desired. This parameter can associate
     *                      the previous constants. For example ACTIVE_PAGE_INFOS | STAGING_PAGE_INFOS
     *                      look for both active and staged pages and return the appropriate result.
     * @param languageCode  The specified language code.
     * @return True if it is at least one entry for this page.
     */
    public boolean hasEntries(int pageInfosFlag, String languageCode) {
        boolean hasEntries = false;
        if (!hasEntries && ((pageInfosFlag & ACTIVE_PAGE_INFOS) != 0 || (pageInfosFlag & 0x08) != 0)) {
            hasEntries = getActivePageInfos().containsKey(languageCode);
        }
        if (!hasEntries && ((pageInfosFlag & STAGING_PAGE_INFOS) != 0 || (pageInfosFlag & 0x08) != 0)) {
            hasEntries |= getStagingPageInfos().containsKey(languageCode);
        }
        if (!hasEntries && ((pageInfosFlag & ARCHIVED_PAGE_INFOS) != 0 || (pageInfosFlag & 0x08) != 0)) {
            hasEntries |= hasArchivedPageInfos(languageCode);
        }
        return hasEntries;
    }

    /**
     * Return true if the page has specified 'pageInfos' entries.
     *
     * @param pageInfosFlag Kind of page infos desired. This parameter can associate
     *                      the previous constants. For example ACTIVE_PAGE_INFOS | STAGING_PAGE_INFOS
     *                      look for both active and staged pages and return the appropriate result.
     * @return True if it is at least one entry for this page.
     */
    public boolean hasEntries(int pageInfosFlag) {
        boolean hasEntries = false;
        if ((pageInfosFlag & ACTIVE_PAGE_INFOS) != 0) {
            hasEntries = !getActivePageInfos().isEmpty();
        }
        if (!hasEntries && ((pageInfosFlag & STAGING_PAGE_INFOS) != 0)) {
            hasEntries |= !getStagingPageInfos().isEmpty();
        }
        if (!hasEntries && ((pageInfosFlag & ARCHIVED_PAGE_INFOS) != 0)) {
            // ensure to load versioning entries
            try {
                loadVersioningEntryStates();
            } catch (Exception t) {
                logger.debug("Exception ocured loading versioning entries", t);
            }
            hasEntries |= !getArchivedPageInfos().isEmpty();
        }
        return hasEntries;
    }

    /**
     * Returns the versionID at which the page is deleted.
     *
     * @return -1 if the page is not actually deleted otherwise the versionID of the
     *         last delete operation.
     * @throws JahiaException
     */
    public int getDeleteVersionID() throws JahiaException {
        int versionID = -1;
        ContentObjectEntryState resultEntryState = null;

        for (Iterator<ContentObjectEntryState> iterator = getEntryStates().iterator(); iterator.hasNext();) {
            ContentObjectEntryState curEntryState = iterator.next();
            if (curEntryState.getWorkflowState()
                    == ContentObjectEntryState.WORKFLOW_STATE_ACTIVE) {
                return -1;
            }
            if (curEntryState.getWorkflowState()
                    == ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED) {
                if (resultEntryState == null) {
                    resultEntryState = curEntryState;
                } else {
                    if (resultEntryState.getVersionID() < curEntryState.getVersionID()) {
                        resultEntryState = curEntryState;
                    }
                }
            }
        }
        if (resultEntryState != null) {
            versionID = resultEntryState.getVersionID();
        }
        return versionID;
    }

    /**
     * Returns true if the page was deleted at a given versionID for all lang.
     */
    public boolean wasDeleted(int versionID) throws JahiaException {
        for (Iterator<ContentObjectEntryState> iterator = getEntryStates().iterator(); iterator.hasNext();) {
            ContentObjectEntryState curEntryState = iterator.next();
            if (curEntryState.getWorkflowState()
                    != ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED
                    && curEntryState.getVersionID() == versionID) {
                // we found an entry that is not deleted
                // this can be the case if we deleted some language but not all
                return false;
            }
        }
        return true;
    }

    /**
     * Return true if the staged entry is mark for deletion.
     *
     * @param languageCode The language code where the page is mark for deletion.
     * @return True if the staged entry is mark for deletion (is == -1).
     *         <p/>
     *         todo Please improve this code. Is the languageCode param really needed.
     */
    public boolean isStagedEntryMarkedForDeletion(String languageCode) {
        JahiaPageInfo pageInfo = getStagingPageInfos().get(languageCode);
        return pageInfo != null &&
                pageInfo.getVersionID() == EntryLoadRequest.DELETED_WORKFLOW_STATE;
    }

    /**
     * Returns the value of all the languages states for a given page. This is
     * used to know at what stage of workflow languages of a page currently are.
     *
     * @param withContent if this is true, the value returned will correspond
     *                    to a logical OR of the staging workflow states of the page itself with
     *                    the page's content (for the moment just the fields are check as the
     *                    containers should have the same staging workflow state). Use this if
     *                    you want to know whether a page needs validation of all of it's content.
     * @return a hash map containing language codes as keys, and as values
     *         Integer objects containing the state of each language.
     */
    public Map<String, Integer> getLanguagesStates(boolean withContent) {
        Map<String, Integer> result = null;
        if (withContent) {
            try {
                result = ServicesRegistry.getInstance().getWorkflowService().getLanguagesStates(this);
            } catch (JahiaException e) {
                logger.warn("Exception while retrieving language entry states", e);
                result = new HashMap<String, Integer>();
            }
        } else {
            result = new HashMap<String, Integer>();
            /* FIXME : we roolback a bit in optimization in order to have something stable
             * before implementing harsh optimization

            if (languageStateCache != null) {
                long nowDate = new Date().getTime();
                if (nowDate <= (langStateCacheLastAccess + LANGSTATECACHE_EXPIRATIONDELAY)) {
                    langStateCacheLastAccess = nowDate;
                    return languageStateCache;
                } else {
                    languageStateCache = null;
                }
            }*/

            for (JahiaPageInfo curPageInfo : getPageInfos()) {
                Integer resultState = result.get(curPageInfo.getLanguageCode());
                if (resultState != null) {
                    if (resultState.intValue() < curPageInfo.getWorkflowState()) {
                        result.put(curPageInfo.getLanguageCode(),
                                new Integer(curPageInfo.getWorkflowState()));
                    }
                } else {
                    result.put(curPageInfo.getLanguageCode(),
                            new Integer(curPageInfo.getWorkflowState()));
                }
            }
            //moved to workflow service
//            if (withContent) {
//                try {
//                    Map fieldsLanguagesStates = ServicesRegistry.getInstance ()
//                            .getJahiaFieldService ()
//                            .getFieldsLanguagesState (getID ());
//                    System.out.println("--- field language states = "+fieldsLanguagesStates);
//                    result = mergeLanguageStates (result, fieldsLanguagesStates);
            //
//                    long beforeContainersState = new Date().getTime();
            //
//                    Map containersLanguagesStates = ServicesRegistry.getInstance ()
//                            .getJahiaContainersService ()
//                            .getContainersLanguagesState (getID ());
//                    long afterContainersState = new Date().getTime();
//                    logger.debug("Time to retrieve container state=" + Long.toString(afterContainersState - beforeContainersState) + "ms");
//                    System.out.println("--- container language states = "+containersLanguagesStates);
//                    result = mergeLanguageStates (result, containersLanguagesStates);
//                } catch (JahiaException je) {
//                    logger.debug ("Error while retrieving language workflow states", je);
//                }

            /* commented out because we're probably going to do this another way.

                        // now that we've check the state of local page content, let's check
                        // the state of the content that is included from other pages.

                        // we will need the site throughout these operations so we get
                        // that first.
                        try {
                            JahiaSite thisSite = ServicesRegistry.getInstance().getJahiaSitesService().getSite(getJahiaID());

                            // now let's do it for absolute addresses fields
                            Set fieldKeys = FieldXRefManager.getInstance().getAbsoluteFieldsFromPageID(thisSite.getSiteKey(), getID());
                            Iterator fieldKeyIter = fieldKeys.iterator();
                            while (fieldKeyIter.hasNext()) {
                                FieldKey curFieldKey = (FieldKey) fieldKeyIter.next();
                                int fieldID = ServicesRegistry.getInstance().getJahiaFieldService().getFieldID(curFieldKey.getFieldName(), curFieldKey.getFieldSourcePageID());
                                ContentField curField = ContentField.getField(fieldID);
                                Map languageStates = curField.getLanguagesStates();
                                result = mergeLanguageStates ( result, languageStates );
                            }

                            // and now for absolute addressed containers
                            Set containerListKeys = ContainerListsXRefManager.getInstance().getAbsoluteContainerListsFromPageID(thisSite.getSiteKey(), getID());
                            Iterator containerListKeyIter = containerListKeys.iterator();
                            while (containerListKeyIter.hasNext()) {
                                ContainerListKey curContainerListKey = (ContainerListKey) containerListKeyIter.next();
                                int containerListID = ServicesRegistry.getInstance().getJahiaContainersService().getContainerListID(curContainerListKey.getContainerListName(), curContainerListKey.getContainerListSourcePageID());
                                List containerIDs = ServicesRegistry.getInstance().getJahiaContainersService().getctnidsInList(containerListID);
                                Iterator containerIDEnum = containerIDs.iterator();
                                while (containerIDEnum.hasNext()) {
                                    Integer curContainerID = (Integer) containerIDEnum.next();
                                    List fieldIDs = ServicesRegistry.getInstance().getJahiaContainersService().getFieldIDsInContainer(curContainerID.intValue());
                                    Iterator fieldIDEnum = fieldIDs.iterator();
                                    while (fieldIDEnum.hasNext()) {
                                        Integer curFieldID = (Integer) fieldIDEnum.next();
                                        ContentField curField = ContentField.getField(curFieldID.intValue());
                                        Map languageStates = curField.getLanguagesStates();
                                        result = mergeLanguageStates ( result, languageStates );
                                    }
                                }

                            }
                        } catch (JahiaException je) {
                            logger.debug("Error while accessing site for page " + getID(), je);
                        }
            */

//            }

        }
        return result;
    }

    public Map<String, Integer> getLanguagesStates() {
        return getLanguagesStates(false);
    }

    /**
     * Flushes the internal language state cache. Use this if the cache expiration
     * delay is not low enough and you need to flush this faster.
     */
    public void flushLanguageStateCache() {

    }

    /**
     * Merges two language states map. Warning : this modifies the destination
     * map so you'll want to be careful using this code and maybe clone maps
     * before using it.
     *
     * @param destination the destination languageStates map
     * @param source      the source languageState map that contains the languages
     *                    states to add to the destination
     *
     * @return the destination map modified by merging the source languageStates
     *         map.
     */


    /**
     * Look if the child page given by its ID has 'this' Jahia page content object
     * as parent.
     *
     * @param childPageID The child page in question
     * @return True if 'this' is parent of child page ID.
     */
    public boolean isParentPage(EntryLoadRequest loadRequest, int childPageID) {
        int pageID = childPageID;

        if (this.getID() == childPageID) {
            // it same page !
            return false;
        }

        try {
            do {
                ContentPage page = getPageService().lookupContentPage(pageID, false);
                if (pageID == getID()) {
                    return true;
                }
                pageID = page.getParentID(loadRequest);
            } while (pageID != 0);
        } catch (JahiaException je) {
            logger.warn("Page not found !! (Das isch doch komisch, gal)", je);
        }
        return false;
    }

    /**
     * @param jParams
     * @param childPageID
     */
    public boolean isParentPage(ProcessingContext jParams, int childPageID) {
        if (jParams != null) {
            return isParentPage(jParams.getEntryLoadRequest(), childPageID);
        } else {
            logger.error("FIXME : Method called with null ProcessingContext, returning false");
            return false;
        }
    }

    /**
     * Returns an iterator on a sorted Set of entry states that correspond to
     * the all the entry states for the page object. To get page content object
     * entry states use the getChilds method.
     *
     * @return a SortedSet of ContentFieldEntryState objects that contain the
     *         entry state information for all the content on the page
     * @throws JahiaException thrown if there was a problem loading entry state
     *                        information from the database.
     */
    public SortedSet<ContentObjectEntryState> getEntryStates()
            throws JahiaException {
        SortedSet<ContentObjectEntryState> entryStates = new TreeSet<ContentObjectEntryState>();

        loadVersioningEntryStates();

        // now we need to convert all this data to EntryState format.
        for (JahiaPageInfo curPageInfo : getPageInfos()) {
            ContentObjectEntryState entryState = new ContentObjectEntryState(
                    curPageInfo.getWorkflowState(), curPageInfo.getVersionID(),
                    curPageInfo.getLanguageCode());
            entryStates.add(entryState);
        }

        return entryStates;
    }

    /**
     * Get an Iterator of active and staged entry state.
     */
    public SortedSet<ContentObjectEntryState> getActiveAndStagingEntryStates() {
        SortedSet<ContentObjectEntryState> entries = new TreeSet<ContentObjectEntryState>();
        Iterator<JahiaPageInfo> pageInfoIter = getActivePageInfos().values().iterator();
        // now we need to convert all this data to EntryState format.
        while (pageInfoIter.hasNext()) {
            JahiaPageInfo curPageInfo = pageInfoIter.next();
            ContentObjectEntryState entryState = new ContentObjectEntryState(
                    curPageInfo.getWorkflowState(), curPageInfo.getVersionID(),
                    curPageInfo.getLanguageCode());
            entries.add(entryState);
        }
        pageInfoIter = this.getStagingPageInfos().values().iterator();
        while (pageInfoIter.hasNext()) {
            JahiaPageInfo curPageInfo = pageInfoIter.next();
            ContentObjectEntryState entryState = new ContentObjectEntryState(
                    curPageInfo.getWorkflowState(), curPageInfo.getVersionID(),
                    curPageInfo.getLanguageCode());
            entries.add(entryState);
        }
        return entries;
    }

    /**
     * Returns the biggest versionID of the active entry, 0 if doesn't exist
     *
     * @return
     * @throws JahiaException
     */
    public int getActiveVersionID()
            throws JahiaException {
        int versionID = 0;
        for (JahiaPageInfo pageInfo : getActivePageInfos().values()) {
            if (pageInfo.getVersionID() > versionID) {
                versionID = pageInfo.getVersionID();
            }
        }
        return versionID;
    }

    public RestoreVersionTestResults isValidForRestore(JahiaUser user,
                                                       String operationMode,
                                                       ContentObjectEntryState entryState,
                                                       boolean removeMoreRecentActive,
                                                       StateModificationContext stateModificationContext)
            throws JahiaException {

        RestoreVersionTestResults opResult = new RestoreVersionTestResults();
        /**
         * This test has no meaning because the language could miss for this object but
         * we should be able to restore other content ( fields,... )
         // first let's check if we have entries that correspond for this
         // page
         opResult.merge(super.isValidForRestore(user, operationMode, entryState, removeMoreRecentActive, stateModificationContext));
         if (opResult.getStatus() == RestoreVersionTestResults.FAILED_OPERATION_STATUS) {
         return opResult;
         }*/

        switch (getPageType(null)) {
            case JahiaPage.TYPE_DIRECT:
                if (!stateModificationContext.isDescendingInSubPages() &&
                        !stateModificationContext.getStartObject().equals(new
                                ContentPageKey(getID()))) {
                    // we are not descending in sub pages and this is not the
                    // start point.
                    opResult.setStatus(RestoreVersionTestResults.
                            FAILED_OPERATION_STATUS);
                    opResult.appendError(
                            new RestoreVersionNodeTestResult(
                                    getObjectKey(), entryState.getLanguageCode(),
                                    "Cannot restore sub page since recursive activation hasn't been requested"));
                    return opResult;
                }

                // now let's check for the children of this page. If only
                // one of them fails, we fail the whole page.
                List<Locale> locales = new ArrayList<Locale>();
                locales.add(EntryLoadRequest.SHARED_LANG_LOCALE);
                locales.add(LanguageCodeConverters.languageCodeToLocale(
                        entryState.getLanguageCode()));
                EntryLoadRequest loadRequest = new EntryLoadRequest(entryState.
                        getWorkflowState(), entryState.getVersionID(), locales);

                for (ContentObject curChild : getChilds(user, loadRequest)) {
                    RestoreVersionTestResults childResult = curChild.isValidForRestore(user,
                            operationMode, entryState, removeMoreRecentActive,
                            stateModificationContext);
                    if (childResult.getStatus() == RestoreVersionTestResults.FAILED_OPERATION_STATUS) {
                        childResult.setStatus(
                                RestoreVersionTestResults.PARTIAL_OPERATION_STATUS);
                        childResult.moveErrorsToWarnings();
                    }
                    opResult.merge(childResult);
                }

                break;
            case JahiaPage.TYPE_LINK:
            case JahiaPage.TYPE_URL:
                break;
        }
        return opResult;
    }

    /**
     * Return false it this page cannot be reached for given operationMode
     * and languageCode.
     * In example, if the operationMode is EDIT and the user has no write access
     * and this page doesn't exist in ACTIVE mode, the result is false.
     *
     * @param operationMode
     * @param languageCode
     * @param user
     */
    public boolean isReachable(String operationMode, String languageCode,
                               JahiaUser user) {
        try {
            String opMode = operationMode;
            if (opMode.equals(ProcessingContext.EDIT)) {
                if (!checkWriteAccess(user)) {
                    opMode = ProcessingContext.NORMAL;
                }
                if (opMode.equals(ProcessingContext.NORMAL)) {
                    // does the page exist in active state
                    if (!hasEntries(ContentPage.ACTIVE_PAGE_INFOS,
                            languageCode)) {
                        return false;
                    }
                }
            }
            if (opMode.equals(ProcessingContext.NORMAL) &&
                    !hasEntries(ContentPage.ACTIVE_PAGE_INFOS,
                            languageCode)) {
                return false;
            }
        } catch (Exception t) {
            logger.debug("Exception checking page reachability", t);
            return false;
        }
        return true;
    }

    /**
     * Return false it this page cannot be reached for given operationMode,
     * languageCode and user.
     * In example, if the operationMode is EDIT and the user has no write access
     * and this page doesn't exist in ACTIVE mode, the result is false.
     *
     * @param operationMode
     * @param user
     */
    public boolean isReachableByUser(EntryLoadRequest loadRequest,
                                     String operationMode, JahiaUser user) {
        ProcessingContext context = Jahia.getThreadParamBean();
        if (context == null || !context.isFilterDisabled(CoreFilterNames.TIME_BASED_PUBLISHING_FILTER)) {
            if (ParamBean.NORMAL.equals(operationMode) && !this.isAvailable()){
                return false;
            } else if ( ParamBean.PREVIEW.equals(operationMode) ){
                final TimeBasedPublishingService tbpServ = ServicesRegistry.getInstance()
                        .getTimeBasedPublishingService();
                try {
                    if (!tbpServ.isValid(getObjectKey(),user,loadRequest,operationMode,
                            AdvPreviewSettings.getThreadLocaleInstance())){
                        return false;
                    }
                } catch ( Throwable t ){
                    logger.debug(t);
                    return false;
                }
            }
        }

        if ((loadRequest != null) && (user != null) && (operationMode != null)) {
            if (!checkReadAccess(user)) {
                return false;
            }

            if (!ProcessingContext.NORMAL.equals(operationMode)) {
                boolean writeAccess = checkWriteAccess(user);

                if (!writeAccess && !isAvailable()) {
                    return false;
                }
                // @todo complete TimeBased publishing with Preview mode
                /*
                 * if (operationMode.equals(ProcessingContext.PREVIEW) //&& !this.isAvailable() ) { ) { return null; }
                 */
            }
        } else {
            logger
                    .warn("No LoadRequest or User passed, assuming normal mode and using site guest user to check for read access");
            if (!checkGuestAccess(getJahiaID())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Use this method to control whether we should reparent the page when
     * restoring or not ( page move issue ) and if content should be restored
     * or not.
     * Note : these attributes are local to this content page, they are not
     * propagated to sub pages.
     *
     * @param user
     * @param operationMode
     * @param entryState
     * @param removeMoreRecentActive
     * @param restoreContent,          if false, restore only page attribute without content
     * @param stateModificationContext
     * @return
     * @throws JahiaException
     */
    public RestoreVersionTestResults restoreVersion(JahiaUser user,
                                                    String operationMode,
                                                    ContentObjectEntryState entryState,
                                                    boolean removeMoreRecentActive,
                                                    boolean restoreContent,
                                                    RestoreVersionStateModificationContext stateModificationContext)
            throws JahiaException {
        this.mRestoreContent = restoreContent;
        return restoreVersion(user, operationMode, entryState, removeMoreRecentActive,
                stateModificationContext);
    }

    public RestoreVersionTestResults restoreVersion(JahiaUser user,
                                                    String operationMode,
                                                    ContentObjectEntryState entryState,
                                                    boolean removeMoreRecentActive,
                                                    RestoreVersionStateModificationContext stateModificationContext)
            throws JahiaException {

        int nowVersionId = getVersionService().getCurrentVersionID();

        RestoreVersionTestResults opResult = new RestoreVersionTestResults();

        getPageType(null);

        ContentObjectEntryState closestVersion;

        // Check if we are doing an undelete
        int deleteVersionID = this.getDeleteVersionID();
        boolean undeletePage = stateModificationContext.isUndelete();
        /*
        if (!removeMoreRecentActive && deleteVersionID != -1
                && entryState.getVersionID() == deleteVersionID) {
            undeletePage = true;
            // the page was deleted and we want to restore it
            // to do that, we want get the closest entry state before the delete date
            closestVersion = this.getClosestVersionedEntryState(entryState, true);
            if (closestVersion != null) {
                entryState = closestVersion;
            }
        }*/

        // do not allow mark for deletion on pages that are home page of a site
        boolean allowRemoveMoreRecentActive = removeMoreRecentActive;
        if (this.getParentID(new EntryLoadRequest(entryState)) == 0) {
            allowRemoveMoreRecentActive = false;
        }

        // retrieve the exact archive entry state
        closestVersion = getClosestVersionedEntryState(entryState);
        boolean markedForDelete = false;
        boolean isMoreRecentOrWasDeleted = closestVersion == null ||
                closestVersion.getWorkflowState() ==
                        ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED
                || !this.hasArchiveEntryState(entryState.getVersionID());

        if (!removeMoreRecentActive) {
            if (closestVersion != null && closestVersion.getWorkflowState() ==
                    ContentObjectEntryState.WORKFLOW_STATE_VERSIONING_DELETED && !undeletePage) {
                // was deleted, or doesn't exist in this language so stop restore deleted content
                return opResult;
            }
        } else {
            // apply an exact restore
            // -> remove more recent data or apply deleted archive state when restoring.
            if (!undeletePage && isMoreRecentOrWasDeleted) {

                if (allowRemoveMoreRecentActive) {
                    // check if this page is not actually staged ( mainly
                    // if it's involved in a move ), if so we undo any temporary
                    // page move state first.

                    // mark active for delete
                    Set<String> langs = new HashSet<String>();
                    langs.add(entryState.getLanguageCode());
                    RestoreVersionStateModificationContext
                            markForDeleteStateModifContext =
                            new RestoreVersionStateModificationContext(this.
                                    getObjectKey(), langs, false, entryState);

                    if (!this.hasArchiveEntryState(entryState.getVersionID(), true)) {
                        markForDeleteStateModifContext.pushAllLanguages(true);
                    }

                    // we want to remove only this language
                    this.markLanguageForDeletion(user,
                            entryState.getLanguageCode(),
                            markForDeleteStateModifContext);
                    markedForDelete = true;
                }
            }
        }

        if (!markedForDelete && this.mRestoreContent) {

            // first let's restore the content on the page
            switch (getPageType(null)) {
                case JahiaPage.TYPE_DIRECT:
                    if (!removeMoreRecentActive &&
                            !stateModificationContext.isDescendingInSubPages() &&
                            !stateModificationContext.getStartObject().equals(new
                                    ContentPageKey(getID()))) {
                        // we are not descending in sub pages and this is not the
                        // start point.
                        opResult.setStatus(RestoreVersionTestResults.
                                FAILED_OPERATION_STATUS);
                        opResult.appendError(new RestoreVersionNodeTestResult(
                                getObjectKey(), entryState.getLanguageCode(),
                                "Cannot restore sub page since recursive activation hasn't been requested"));
                        return opResult;
                    }

                    // now let's check for the children of this page. If only
                    // one of them fails, we fail the whole page.
                    List<Locale> locales = new ArrayList<Locale>();
                    locales.add(EntryLoadRequest.SHARED_LANG_LOCALE);
                    locales.add(LanguageCodeConverters.languageCodeToLocale(
                            entryState.getLanguageCode()));

                    // 1. First restore archive
                    // load archive to restore
                    EntryLoadRequest loadRequest = new EntryLoadRequest(entryState.
                            getWorkflowState(), entryState.getVersionID(), locales);

                    List<? extends ContentObject> children = null;
                    if (!undeletePage) {
                        children = getChilds(user, loadRequest);
                    } else {
                        children = getChilds(user, null);
                    }
                    // For performance issue, we don't want to restore twice objects.
                    List<String> processedChilds = new ArrayList<String>();

                    Iterator<? extends ContentObject> childrenIter = children.listIterator();
                    while (childrenIter.hasNext()) {
                        ContentObject curChild = childrenIter.next();
                        if (!undeletePage) {
                            opResult.merge(curChild.restoreVersion(user,
                                    operationMode, entryState, (removeMoreRecentActive),
                                    stateModificationContext));
                        } else {
                            ContentObjectEntryState childEntryState = ContentObjectEntryState.getEntryState(nowVersionId, entryState.getLanguageCode());
                            if (childEntryState != null && childEntryState.getWorkflowState() == -1) {
                                childEntryState = curChild.getClosestVersionedEntryState(childEntryState, true);
                            }
                            if (childEntryState != null) {
                                opResult.merge(curChild.restoreVersion(user,
                                        operationMode, childEntryState, false,
                                        stateModificationContext));
                            }
                        }
                        processedChilds.add(curChild.getObjectKey().toString());
                    }

                    //2. Second, remove more recent data
                    if (!undeletePage && removeMoreRecentActive) {
                        // load staging or active to perform a restore which will mark them for delete
                        loadRequest =
                                new EntryLoadRequest(EntryLoadRequest.
                                        STAGING_WORKFLOW_STATE,
                                        0, locales);
                        children = getChilds(user, loadRequest);
                        childrenIter = children.listIterator();
                        while (childrenIter.hasNext()) {
                            ContentObject curChild = (ContentObject)
                                    childrenIter.next();

                            if (!processedChilds.contains(curChild.getObjectKey().
                                    toString())) {
                                opResult.merge(curChild.restoreVersion(user,
                                        operationMode, entryState,
                                        removeMoreRecentActive,
                                        stateModificationContext));
                            }
                        }
                    }

                    break;
                case JahiaPage.TYPE_LINK:
                case JahiaPage.TYPE_URL:
                    break;
            }
        }

        // now let's restore the page's entry data. We might have to do this
        // for more than one entry state if we are restoring information that
        // contains moves, page template changes, remote URL or link ID changes,
        // or any shared language parameter.

        // So first we determine if we are doing operations on just a language
        // code or the full set of languages.

        JahiaPageInfo versionedPageInfo = getPageInfo(closestVersion);
        // now let's get any active entry that we currently have for this page
        // if there exists at least one.
        JahiaPageInfo anyStagingOrActivePageInfo = null;
        Iterator<JahiaPageInfo> it = getStagingPageInfos().values().iterator();
        if (!it.hasNext()) {
            it = getActivePageInfos().values().iterator();
        }
        if (it.hasNext()) {
            anyStagingOrActivePageInfo = it.next();
        }

        if (this.getDeleteVersionID() != -1 ||
                ((anyStagingOrActivePageInfo != null && versionedPageInfo != null)
                        &&
                        ((anyStagingOrActivePageInfo.getPageLinkID() !=
                                versionedPageInfo.getPageLinkID()) ||
                                (anyStagingOrActivePageInfo.getPageTemplateID() !=
                                        versionedPageInfo.getPageTemplateID()) ||
                                (anyStagingOrActivePageInfo.getPageType() !=
                                        versionedPageInfo.getPageType()) ||
                                (anyStagingOrActivePageInfo.getParentID() !=
                                        versionedPageInfo.getParentID()) ||
                                remoteURLisDiff(anyStagingOrActivePageInfo.getRemoteURL(),
                                        versionedPageInfo.getRemoteURL())))) {
            // major shared-language change detected, we must perform the
            // restores for all the languages.

            // the trick here is to correctly identify all the languages for
            // which we must restore. This set of language is actually the
            // combination of the active entry states and the versioned entry
            // state languages. If there are more languages in the active
            // entries then they will be marked for deletion since they didn't
            // exist in the past.
            Set<String> onlyActiveLanguageCodes = getActivePageInfos().keySet();
            List<ContentObjectEntryState> versionedLanguagesEntryStates = getClosestVersionedEntryStates(entryState.getVersionID());
            for (ContentObjectEntryState versionedEntryState : versionedLanguagesEntryStates) {
                // let's remove the versioned language code to make the difference
                // with the active set.
                onlyActiveLanguageCodes.remove(versionedEntryState.getLanguageCode());
            }

            // now that we have calculated the set of languages that must be
            // marked for deletion, let's do it baby :)

            for (String languageCode : onlyActiveLanguageCodes) {
                markLanguageForDeletion(user, languageCode,
                        new RestoreVersionStateModificationContext(getObjectKey(),
                                onlyActiveLanguageCodes, true, entryState));
            }

            // now that we have marked for deletion the languages that only
            // exist in the present, let's go "back to the future" now and
            // restore all the languages for the version ID we are interested
            // in.


            for (ContentObjectEntryState curEntryState : versionedLanguagesEntryStates) {
                opResult.merge(super.restoreVersion(user, operationMode,
                        curEntryState, allowRemoveMoreRecentActive,
                        stateModificationContext));
            }

            // need to recompute internal entry states map ?
            //this.rebuildStatusMaps();

            /**
             * FIXME : why are we doing this ??? Some old issues ???
             if (  (versionedPageInfo != null) &&
             (versionedPageInfo.getPageType()==ContentPage.TYPE_DIRECT) ){
             // take care to keep the current parent page instead of the archived one
             Set entries = this.getActiveAndStagingEntryStates();
             Iterator iterator = entries.iterator();
             while ( iterator.hasNext() ){
             ContentObjectEntryState curEntryState =
             (ContentObjectEntryState)iterator.next();
             if ( curEntryState.isStaging() ){
             JahiaPageInfo pageInfo = this.getPageInfoVersionIgnoreLanguage(
             new EntryLoadRequest(curEntryState), true);
             pageInfo.setParentID(currentParentId);
             pageInfo.setAclID(currentAclId);
             }
             }
             }**/

        } else {
            // no major shared-language change detected, let's just restore the
            // requested entry
            opResult.merge(super.restoreVersion(user, operationMode, entryState,
                    allowRemoveMoreRecentActive,
                    stateModificationContext));
        }

        // now let's restore the page's parent data. In order to do this we must
        // also check on which languages we must work. In the case of a restore
        // of a move we must make sure we mark for deletion ALL the languages.
        if (versionedPageInfo != null &&
                versionedPageInfo.getPageType() == ContentPage.TYPE_DIRECT) {

            /** todo implement referring content restore */
            List<Locale> locales = new ArrayList<Locale>();
            locales.add(LanguageCodeConverters.languageCodeToLocale(entryState.
                    getLanguageCode()));
            // here we don't bother with the staging entries since we will probably
            // be overwriting them...
            EntryLoadRequest loadRequest =
                    new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE, 0,
                            locales);
            ContentObject parentField = this.getParent(user, loadRequest,
                    operationMode);
            if (parentField == null) {
                loadRequest = new EntryLoadRequest(EntryLoadRequest.
                        ACTIVE_WORKFLOW_STATE, 0, locales);
                parentField = this.getParent(user, loadRequest, operationMode);
            }

            EntryLoadRequest versionedLoadRequest;
            if (!undeletePage) {
                versionedLoadRequest =
                        new EntryLoadRequest(EntryLoadRequest.
                                VERSIONED_WORKFLOW_STATE,
                                entryState.getVersionID(), locales);
            } else {
                versionedLoadRequest =
                        new EntryLoadRequest(EntryLoadRequest.
                                VERSIONED_WORKFLOW_STATE,
                                deleteVersionID, locales);
            }
            ContentObject versionedParentField = this.getParent(user,
                    versionedLoadRequest, operationMode);

            // first some quick sanity checks...
            boolean movedPage = false;
            if ((parentField != null) && (versionedParentField != null)) {
                if (!parentField.getObjectKey().equals(versionedParentField.
                        getObjectKey())) {
                    movedPage = true;
                }
            }

            Set<String> languageCodes = new HashSet<String>();
            languageCodes.add(entryState.getLanguageCode());
            languageCodes.add(ContentObject.SHARED_LANGUAGE);

            if (movedPage) {
                // PAGE_MOVE_LOGIC
                // page was moved, change current page field to -1.
                ((ContentPageField) parentField).setValue(-1,
                        Jahia.getThreadParamBean());
                // @FIXME : is it wanted to delete the whole container on page move....
                // mark for delete the current parent container
                ContentObject currentParentContainer =
                        parentField.getParent(user, EntryLoadRequest.STAGED, operationMode);
                if (currentParentContainer != null) {
                    currentParentContainer.markLanguageForDeletion(user, ContentObject.SHARED_LANGUAGE,
                            stateModificationContext);
                }
            }

            // now we can finally restore the parent content
            if (versionedParentField != null) {
                // It is more usefull to have the start object the page
                opResult.merge(versionedParentField.restoreVersion(user,
                        operationMode, entryState, removeMoreRecentActive,
                        new RestoreVersionStateModificationContext(new
                                ContentPageKey(this.getID()),
                                languageCodes, false, entryState, stateModificationContext.isUndelete())));

                // change page parent Acl accordingly to the new parent field acl
                if (this.getACL().getID() != versionedParentField.getAclID()) {
                    this.getACL().setParentID(versionedParentField.getAclID());
                }

                ContentObject versionedParentContainer = versionedParentField.
                        getParent(user, versionedLoadRequest, operationMode);

                if (versionedParentContainer != null) {
                    if (versionedParentContainer.isDeletedOrDoesNotExist(entryState.
                            getVersionID())
                            &&
                            !versionedParentField.isDeletedOrDoesNotExist(entryState.getVersionID())) {

                        // there are so much situation where a sub page is activated at v=t1, but not the parent container !!!!
                        // It's the case, when a user has admin right for the page (subpage),
                        // but not admin right on the parent container ( that is localted on the parent page ).
                        // If we restore at v=t1, the subpage exist while the parent container will be deleted ( as it doesn't exist at v=t1 )!
                        // This situation will create orphan pages ( pages that appear in sitemap but not in any container list )!!!!
                        // That is why we should not restore the parent container

                        // do not restore the parent container

                    } else {
                        // It is more usefull to have the start object the page
                        RestoreVersionStateModificationContext smc = new RestoreVersionStateModificationContext(new
                                ContentPageKey(this.
                                getID()),
                                languageCodes, false, entryState, undeletePage);
                        smc.setContainerPageChildId(this.getID());
                        opResult.merge(versionedParentContainer.restoreVersion(user,
                                operationMode, entryState, removeMoreRecentActive,
                                smc));
                    }
                }
            } else {
                logger.debug("Couldn't find parent field for page " +
                        this.getObjectKey() +
                        ", can't restore parent content...");
            }
        }

        this.commitChanges(true, true, user);

        // Invalidate sitemap
        ServicesRegistry.getInstance().getJahiaSiteMapService().resetSiteMap();

        // check for cyclic situation
        int parentId = this.getParentID(EntryLoadRequest.STAGED);
        ContentPage parentPage;
        try {
            if (parentId > 0) {
                parentPage = ContentPage.getPage(parentId);
                if (parentPage == null) {
                    // a corrupted situation
                    this.setParentID(parentId, user, EntryLoadRequest.STAGED);
                } else if (parentPage != null &&
                        parentPage.getParentID(EntryLoadRequest.STAGED) == this.getID()) {
                    // cyclic situation !!!
                    RestoreVersionStateModificationContext rsmc =
                            new RestoreVersionStateModificationContext(parentPage.getObjectKey(),
                                    stateModificationContext.getLanguageCodes(), false, entryState, stateModificationContext.isUndelete());
                    parentPage.restoreVersion(user, operationMode, entryState,
                            removeMoreRecentActive, false,
                            rsmc);
                }
            }
        } catch (Exception t) {
            logger.warn(t, t);
        }

        return opResult;
    }

    /**
     * Is this kind of object shared (i.e. not one version for each language, but one version for every language)
     */
    public boolean isShared() {
        return false;
    }

    private boolean remoteURLisDiff(String url1, String url2) {
        if (url1 == null && url2 == null) {
            return false;
        }
        if (url1 != null) {
            return (!url1.equals(url2));
        }
        return (!url2.equals(url1));
    }

    private boolean hasArchivedPageInfos(String languageCode) {
        if (languageCode == null) {
            return false;
        }
        
        loadVersioningEntryStates();
        
        if (languageCode.equals(ContentObject.SHARED_LANGUAGE)
                && !getArchivedPageInfos().isEmpty()) {
            return true;
        }

        for (JahiaPageInfo pageInfo : getArchivedPageInfos()) {
            if (pageInfo.getLanguageCode().equals(languageCode)) {
                return true;
            }
        }

        return false;
    }

    public String getDisplayName(ProcessingContext jParams) {
        return getTitle(jParams);
    }


    /**
     * Active staging child page links
     *
     * @param languageCodes    Set
     * @param versioningActive boolean
     * @param saveVersion      JahiaSaveVersion
     * @param user             JahiaUser
     * @param jParams          ProcessingContext
     * @return ActivationTestResults
     * @throws JahiaException
     */
    protected ActivationTestResults activeStagedLinkPages(
            Set<String> languageCodes,
            boolean versioningActive,
            JahiaSaveVersion saveVersion,
            JahiaUser user,
            ProcessingContext jParams)
            throws JahiaException {

        ActivationTestResults result = new ActivationTestResults();
        if (languageCodes == null) {
            return result;
        }

        for (String languageCode : languageCodes) {
            List<Locale> locales = new ArrayList<Locale>();
            locales.add(org.jahia.utils.LanguageCodeConverters.languageCodeToLocale(languageCode));
            EntryLoadRequest loadRequest = new EntryLoadRequest(EntryLoadRequest.STAGING_WORKFLOW_STATE, 0, locales);
            loadRequest.setWithMarkedForDeletion(true);

            // the following call also returns deleted content, we must not
            // try to activate deleted content !
            for (Iterator<ContentPage> childPages = getContentPageChilds(user, LoadFlags.ALL, languageCode, false); childPages.hasNext();) {
                ContentPage childPage = childPages.next();
                if (childPage.hasActiveEntries() || childPage.hasStagingEntries()) {
                    if (childPage.getPageType(loadRequest) !=
                            ContentPage.TYPE_DIRECT) {
                        StateModificationContext smc = new StateModificationContext(childPage.
                                getObjectKey(), languageCodes);
                        result.merge(childPage.activate(
                                languageCodes, versioningActive,
                                saveVersion, user, jParams, smc));
                    }
                } else {
                    // page is archived or deleted, we don't activate it.
                }
            }
        }
        return result;
    }

    /**
     * This method is called to notify that time based publishing state has changed
     */
    public void notifyStateChanged() {

        getPageService().invalidatePageCache(getID());
        // invalidate sitemap cahe too
        ServicesRegistry.getInstance().getJahiaSiteMapService().resetSiteMap();
    }

    /**
     * Returns the page path of the current content object.
     * The page path is of the form : /pid8/pid10/pid111 where pid8 is the site's home page and the pid111 is
     * the current page or the current content object's parent page
     * <p/>
     * Default implementation for Content implementing PageReferenceableInterface.
     * Other ContentObject must override this method if they do not implement PageReferenceableInterface
     *
     * @param context
     * @param ignoreMetadata if true , the path is resolved dynamically,
     *                       not the one stored in metadata
     */
    public String getPagePathString(ProcessingContext context, boolean ignoreMetadata) {
        return super.getPagePathString(context, ignoreMetadata);
        /*
        String pagePath = "";
        JahiaField jahiaField;
        if (!ignoreMetadata) {
            try {
                jahiaField = getMetadataAsJahiaField(CoreMetadataConstant.PAGE_PATH, context);
                if (jahiaField != null) {
                    pagePath = jahiaField.getValue();
                } else {
                }
            } catch (Exception t) {
                logger.debug(t);
            }
        }

        if (pagePath == null || "".equals(pagePath.trim())
                || !pagePath.startsWith(PAGEPATH_PAGEID_PREFIX)) {
            ContentPage parentPage;
            try {
                int parentId = this.getParentID(context);
                if (parentId > 0) {
                    parentPage = ContentPage.getPage(parentId);
                    pagePath = parentPage.getPagePathString(context) + PAGEPATH_PAGEID_PREFIX
                            + this.getID();
                } else {
                    pagePath = PAGEPATH_PAGEID_PREFIX + this.getID();
                }
            } catch (Exception t) {
                logger.warn(t, t);
            }
        }
        return pagePath;
        */
    }

    /*
    * Update all childs content's metadata PAGE_PATH bellow this node
    *
    * @param startNode the starting node
    */
    public void updateContentPagePath(ProcessingContext context) throws JahiaException {

        // update content childs
        for (ContentObject child : getChilds(JahiaAdminUser.getAdminUser(this.getJahiaID()), EntryLoadRequest.STAGED)) {
            child.updateContentPagePath(context);
        }

        // update subpage childs

        for (Iterator<ContentPage> iterator = getContentPageChilds(JahiaAdminUser.getAdminUser(this.getJahiaID()),
                LoadFlags.ALL, ContentObject.SHARED_LANGUAGE, true); iterator.hasNext();) {
            ContentPage childPage = iterator.next();
            if (this.getParentID(context) == childPage.getID()) {
                // infinite loop, this case can occurs with moved page
                continue;
            }
            childPage.updateContentPagePath(context);
        }
        /*
        String newPagePath = "";
        int parentId = this.getParentID(context);
        if (parentId > 0) {
            ContentPage parentPage = ContentPage.getPage(parentId);
            String pagePathString = parentPage.getPagePathString(context);
            String newId = ContentObject.PAGEPATH_PAGEID_PREFIX + this.getID();
            if(pagePathString.indexOf(newId)==-1) {
                newPagePath = pagePathString + newId;
            } else {
                // inconsistent situation, try to resolve dynamically
                pagePathString = parentPage.getPagePathString(context,true);
                if(pagePathString.indexOf(newId)==-1) {
                    newPagePath = pagePathString + newId;
                } else {
                    logger.debug("Wrong page path for page " + this.getID());
                    return;
                }
            }
        }
        setMetadataValue(CoreMetadataConstant.PAGE_PATH, newPagePath, context);

        // update content childs
        List contentChilds = this.getChilds(JahiaAdminUser.getAdminUser(this.getJahiaID()),
                EntryLoadRequest.STAGED);
        int size = contentChilds.size();
        ContentObject child;
        for (int i = 0; i < size; i++) {
            child = (ContentObject) contentChilds.get(i);
            child.updateContentPagePath(context);
        }

        // update subpage childs
        Iterator iterator = this.getContentPageChilds(JahiaAdminUser.getAdminUser(this.getJahiaID()),
                LoadFlags.ALL, ContentObject.SHARED_LANGUAGE, true);
        ContentPage childPage = null;
        while (iterator.hasNext()) {
            childPage = (ContentPage) iterator.next();
            if ( this.getParentID(context) == childPage.getID() ){
                // infinite loop, this case can occurs with moved page
                continue;
            }
            childPage.updateContentPagePath(context);
        }*/
    }

    /*
     * Return jcr path "/siteKey/ContentPage_XX/ContentPage_YY"
     *
     * @param context
     * @return
     * @throws JahiaException
     */

//    public String getJCRPath(ProcessingContext context) throws JahiaException {
//        String path = this.getObjectKey().toString();
//        int pageId = this.getParentID(context.getEntryLoadRequest());
//        String parentPath = null;
//        if (pageId > 0) {
//            ContentPage parentPage = ContentPage.getPage(pageId);
//            parentPath = parentPage.getJCRPath(context);
//        } else {
//            JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSite(this.getJahiaID());
//            parentPath = site.getJCRPath(context);
//        }
//        return parentPath + "/" + ServicesRegistry.getInstance().getQueryService()
//                .getNameFactory().create(Name.NS_DEFAULT_URI, path);
//    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        pageManager = (JahiaPagesManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaPagesManager.class.getName());
    }

    private JahiaPageService getPageService() {
        if (pageService == null) {
            pageService = ServicesRegistry.getInstance().getJahiaPageService();
        }
        return pageService;
    }

    private JahiaVersionService getVersionService() {
        if (versionService == null) {
            versionService = ServicesRegistry.getInstance()
                    .getJahiaVersionService();
        }
        return versionService;
    }
    
    public String getURLKey() throws JahiaException {
        return getProperty(PageProperty.PAGE_URL_KEY_PROPNAME);
    }
}
