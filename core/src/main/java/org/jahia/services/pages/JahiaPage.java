/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

// $Id$

package org.jahia.services.pages;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.jahia.bin.Jahia;
import org.jahia.data.fields.JahiaField;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaTemplateNotFoundException;
import org.jahia.hibernate.manager.JahiaPagesManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.jahia.services.acl.ACLResourceInterface;
import org.jahia.services.acl.JahiaACLException;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.metadata.CoreMetadataConstant;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.version.ActivationTestResults;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.version.JahiaSaveVersion;
import org.jahia.services.version.StateModificationContext;
import org.jahia.utils.textdiff.HunkTextDiffVisitor;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import name.fraser.neil.plaintext.DiffMatchPatch;


/**
 * <p>Title: Jahia Page</p>
 * <p>Description: Represents a Jahia page for a specified language and
 * version. This is strongly attached to the instance of the ProcessingContext
 * object passed in the constructor. There can be multiple JahiaPage in memory
 * at once, but only one ContentPage for all the languages and all the
 * versions.  </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 *
 * @author Eric Vassalli
 * @author Fulco Houkes
 * @author Serge Huber
 * @version 2.0
 */
public class JahiaPage implements PageInfoInterface, ACLResourceInterface, Comparator<JahiaPage>, Serializable {

    private static final long serialVersionUID = 310941453247643548L;

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (JahiaPage.class);


    // private JahiaPageInfo       mPageInfo;
    private ContentPage mContentPage = null;
    private JahiaPageDefinition mPageTemplate;
    private JahiaBaseACL mACL;
    private EntryLoadRequest mEntryLoadRequest = null;

    private JahiaPageDefinition mTempPageTemplate;
    private Map<String, PageProperty> mProperties = null;

    private transient JahiaPagesManager pageManager;
    private String title;
    private String rawTitle;
//    private Map checkAccesses = new HashMap(64);
    private int pageLinkID = -2;
    private int pageType = -2;
    private int parentID = -2;
    private String remoteURL = null;
    
    /* used in Compare mode, if true this means the page is actually moved */
    private boolean moved = false;

    //-------------------------------------------------------------------------
    protected JahiaPage (ContentPage contentPage,
                         JahiaPageDefinition pageTemplate,
                         JahiaBaseACL acl,
                         EntryLoadRequest loadRequest)
            throws JahiaException {

        mContentPage = contentPage;

        mPageTemplate = pageTemplate;
        mACL = acl;
        mEntryLoadRequest = loadRequest;

        // Coherence check here
        // if the loadRequest is active, but the page exist only in staging
        // ( for some reason, it was not actived ), so change the loadRequest to
        // staging ohterwhise calls to getType(), getJahiaID() will fail !!!!
        EntryLoadRequest newLoadRequest = loadRequest;
        Locale locale = loadRequest.getFirstLocale(true);
        if (locale == null) {
            locale = loadRequest.getFirstLocale(false);
        }
        if (loadRequest != null && loadRequest.isCurrent()
                && contentPage != null) {
            if (!contentPage.hasActiveEntries()
                    || (!contentPage.getSite().isMixLanguagesActive() && !contentPage
                            .hasEntries(ContentPage.ACTIVE_PAGE_INFOS,
                                    locale.toString()))) {
                newLoadRequest = new EntryLoadRequest(
                        EntryLoadRequest.STAGING_WORKFLOW_STATE, 0, loadRequest
                                .getLocales());
                newLoadRequest.setWithDeleted(loadRequest.isWithDeleted());
                newLoadRequest.setWithMarkedForDeletion(loadRequest
                        .isWithMarkedForDeletion());
                mEntryLoadRequest = newLoadRequest;
            }
        }
        if (mPageTemplate == null) {
            // retrieve the staging
            mPageTemplate = contentPage.getPageTemplate (newLoadRequest);
        }

        mTempPageTemplate = mPageTemplate;
        pageManager = (JahiaPagesManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaPagesManager.class.getName());
    }

    /**
     * Return the internal contentPage
     *
     * @return ContentPage
     */
    public ContentPage getContentPage () {
        return this.mContentPage;
    }

    //-------------------------------------------------------------------------
    /**
     * Check if the user has administration access on the specified page. Admin
     * access means having the ability to add pages, containers and fields, but
     * also giving rights to users to the different objects/applications in the
     * specified page.
     *
     * @param user Reference to the user.
     *
     * @return Return true if the user has read access for the specified page,
     *         or false in any other case.
     */
    public final boolean checkAdminAccess (JahiaUser user) {
        return getContentPage().checkAccess (user, JahiaBaseACL.ADMIN_RIGHTS,false);
    }

    public final boolean checkAdminAccess (JahiaUser user,boolean checkChilds) {
        return getContentPage().checkAccess (user, JahiaBaseACL.ADMIN_RIGHTS,checkChilds);
    }


    //-------------------------------------------------------------------------
    /**
     * Check if the user has read access on the specified page. Read access means
     * having the rights to display and read the page.
     *
     * @param user Reference to the user.
     *
     * @return Return true if the user has read access for the specified page,
     *         or false in any other case.
     */
    public final boolean checkReadAccess (JahiaUser user) {
        return getContentPage().checkAccess (user, JahiaBaseACL.READ_RIGHTS,false);
    }


    //-------------------------------------------------------------------------
    /**
     * Check if the user has Write access on the specified page. Write access means
     * adding new pages, containers and fields in the specified page.
     *
     * @param user Reference to the user.
     *
     * @return Return true if the user has read access for the specified page,
     *         or false in any other case.
     */
    public final boolean checkWriteAccess (JahiaUser user) {
        return getContentPage().checkAccess (user, JahiaBaseACL.WRITE_RIGHTS,false);
    }

    public final boolean checkWriteAccess (JahiaUser user,boolean checkChilds) {
        return getContentPage().checkAccess (user, JahiaBaseACL.WRITE_RIGHTS,checkChilds);
    }


    //-------------------------------------------------------------------------
    /**
     * Check if the Guest user of a site has read access.
     *
     * @param siteID the site id.
     *
     * @return Return true if the site's guest user has read access for this page,
     *         or false in any other case.
     */
    public final boolean checkGuestAccess (int siteID) {
        // get the User Manager service instance.
        JahiaUserManagerService userMgr = ServicesRegistry.getInstance ().
                getJahiaUserManagerService ();
        if (userMgr == null)
            return false;

        JahiaUser theUser = userMgr.lookupUser (JahiaUserManagerService.GUEST_USERNAME);
        if (theUser == null)
            return false;

        return getContentPage().checkAccess (theUser, JahiaBaseACL.READ_RIGHTS,false);
    }


    //-------------------------------------------------------------------------
    /**
     * Commit the changes made in the page object to the database.
     *
     * @param flushCaches specifies whether we should flush all the caches
     *                    corresponding to this page update (useful for example when we have
     *                    modified a page title that will be display on multiple other pages).
     *
     * @throws JahiaException raised if there were problems either updating
     *                        the persistant data or flushing the cache.
     * todo this is called even for a page counter update, can we avoid that
     * in the future ?
     */
    public void commitChanges (boolean flushCaches, JahiaUser user)
            throws JahiaException {
        logger.debug ("called.");

        if (flushCaches) {
            // let's flush the cache of all the pages referencing this one.
            int siteID = getJahiaID ();
            JahiaSite site = ServicesRegistry.getInstance ().getJahiaSitesService ().getSite (
                    siteID);
            if (site == null) {
                logger.debug ("Invalid site for page, cannot flush cache.");
            }
        }

        mContentPage.commitChanges (flushCaches,true,user);
        mPageTemplate = mTempPageTemplate;
    }


    //-------------------------------------------------------------------------
    /**
     * Return the page's ACL object.
     *
     * @return Return the page's ACL.
     */
    public final JahiaBaseACL getACL () {
        return mACL;
    }


    //-------------------------------------------------------------------------
    /**
     * Return the ACL unique identification number.
     *
     * @return Return the ACL ID.
     */
    public final int getAclID () {
        int id = -1;
        try {
            id = mACL.getID ();
        } catch (JahiaACLException ex) {
            // This exception should not happen ... :)
        }
        return id;
    }


    //-------------------------------------------------------------------------
    /**
     * Return the page's hit counter.
     *
     * @return Return the page counter.
     * @deprecated
     */
    public final int getCounter () {
        return 0;
    }


    //-------------------------------------------------------------------------
    /**
     * Return the user nickname who created the page. This nickname is the
     * user name used internally by Jahia.
     *
     * @return Return the creator nickname.
     * @see org.jahia.services.pages.ContentPage#getMetadataValue(java.lang.String name,org.jahia.params.ProcessingContext jParams,java.lang.String default)
     * @deprecated
     */
    public final String getCreator () {
        try {
            return getContentPage().getMetadataValue(CoreMetadataConstant.CREATOR,Jahia.getThreadParamBean(),""+System.currentTimeMillis());
        } catch (JahiaException e) {
            return "n.a.";
        }
    }


    //-------------------------------------------------------------------------
    /**
     * Return the page's date of creation in ms from 1975.
     *
     * @return Return the date of creation.
     * @see org.jahia.services.pages.ContentPage#getMetadataAsJahiaField(java.lang.String name,org.jahia.params.ProcessingContext jParams)
     * @deprecated
     */
    public final String getDoc () {
        String longString="0";
        try {
            JahiaField metadataDateField=getContentPage().getMetadataAsJahiaField(CoreMetadataConstant.CREATION_DATE, Jahia.getThreadParamBean());
            if (metadataDateField == null)
                return "0";
            longString=(String) metadataDateField.getObject();
            if(longString==null || longString.equals(""))
                longString="0";
            return longString;
        } catch (JahiaException e) {
            return "0";
        }

    }


    //-------------------------------------------------------------------------
    /**
     * Return the page's unique identification number.
     *
     * @return Return the page ID.
     */
    public final int getID () {
        return mContentPage.getID ();
    }


    //-------------------------------------------------------------------------
    /**
     * Return the site ID in which the page is located.
     *
     * @return Return the page site ID.
     */
    public final int getJahiaID () {
        return mContentPage.getJahiaID ();
    }


    //-------------------------------------------------------------------------
    /**
     * Return the reference on the page definition object.
     *
     * @return Return the page definition object.
     */
    public final JahiaPageDefinition getPageTemplate () {
        return mPageTemplate;
    }


    //-------------------------------------------------------------------------
    /**
     * Return the page definition ID.
     *
     * @return Return the page definition ID.
     */
    public final int getPageTemplateID () {
        return mPageTemplate.getID();
    }

    //-------------------------------------------------------------------------
    /**
     * Return the internal jahia page ID in case the page is an internal
     * jahia link.
     *
     * @return Return the page link ID.
     */
    public final int getPageLinkID () {
        if(pageLinkID==-2)
            pageLinkID = mContentPage.getPageLinkID(mEntryLoadRequest);
        return pageLinkID;
    }


    //-------------------------------------------------------------------------
    /**
     * Return the page type
     *
     * @return Return the page type
     */
    public final int getPageType () {
        if(pageType == -2)
        pageType = mContentPage.getPageType(mEntryLoadRequest);
        return pageType;
    }


    //-------------------------------------------------------------------------
    /**
     * Return the parent page unique identification number.
     *
     * @return Return the parent page ID.
     */
    public final int getParentID () {
        if(parentID==-2)
        parentID = mContentPage.getParentID(mEntryLoadRequest);
        return parentID;
    }


    //-------------------------------------------------------------------------
    /**
     * Return the remote URL in case the page is an external reference (a non
     * Jahia page. If the page is not an external URL, "<no url>" is returned.
     *
     * @return Return the remote URL.
     */
    public final String getRemoteURL () {
        if(remoteURL==null)
        remoteURL = mContentPage.getRemoteURL(mEntryLoadRequest);
        return remoteURL;
    }


    //-------------------------------------------------------------------------
    /**
     * Return the page title.
     *
     * @return Return the page title.
     */
    public final String getTitle () {
        if(title==null) {
            title = mContentPage.getTitle(mEntryLoadRequest);
        }
        return title;
    }

    /**
     * Return the title with highlighted differences
     *
     * @param jParams
     * @return
     */
    public String getHighLightDiffTitle(ProcessingContext jParams) {
        return getHighLightDiffTitle(jParams.getDiffVersionID(),jParams);
    }

    /**
     * Return the title with highlighted differences
     *
     * @param jParams
     * @return
     */
    public String getHighLightDiffTitle(int diffVersionID, ProcessingContext jParams) {

        if ( diffVersionID == 0 ){
            return this.getTitle();
        }
        if ( isMoved() ){
            String title = getTitle();
            title =  HunkTextDiffVisitor.getDeletedText(title);
            title += " (" + JahiaResourceBundle.getJahiaInternalResource("org.jahia.moved.label",
                    Jahia.getThreadParamBean().getLocale()) + ")";
            return title;
        }
        String oldValue = this.getTitle();
        if ( oldValue == null ){
            oldValue = "";
        }
        String newValue = "";
        String mergedValue = "";

        JahiaPageInfo currentPageInfo = this.mContentPage
                .getPageInfoVersion(jParams.getEntryLoadRequest(),false,false);

        EntryLoadRequest loadVersion =
                EntryLoadRequest.getEntryLoadRequest(diffVersionID,
                jParams.getLocale().toString());
        int newValueWorkflowState = this.mEntryLoadRequest.getWorkflowState();
        JahiaPageInfo pageInfo = this.mContentPage.getPageInfoVersion(loadVersion,false,false);
        if ( pageInfo != null ){
            newValue = pageInfo.getTitle();
            newValueWorkflowState = pageInfo.getWorkflowState();
        }

        // Highlight text diff
        DiffMatchPatch hunkTextDiffV = new DiffMatchPatch();
            LinkedList<DiffMatchPatch.Diff> diffs;
        if ( currentPageInfo == null ||
                (currentPageInfo.getWorkflowState() == EntryLoadRequest.DELETED_WORKFLOW_STATE) ){
            // does not exists
            if ("".equals(oldValue)){
                oldValue = newValue; // we should display something
            }
            return HunkTextDiffVisitor.getDeletedText(oldValue);
        } else if ( currentPageInfo.getVersionID() == -1 && newValueWorkflowState==1){
            // currently marked for delete compared with active
            // does not exists
            if ("".equals(oldValue)){
                oldValue = newValue; // we should display something
            }
            return HunkTextDiffVisitor.getDeletedText(oldValue);
        } else if ( currentPageInfo.getWorkflowState() < newValueWorkflowState ){
            diffs = hunkTextDiffV.diff_main(oldValue,newValue);
        } else {
            if ( newValueWorkflowState == EntryLoadRequest.DELETED_WORKFLOW_STATE ){
                // was deleted
                newValue = "";
            }
            diffs = hunkTextDiffV.diff_main(newValue,oldValue);
        }
        hunkTextDiffV.diff_cleanupSemantic(diffs);
        mergedValue = hunkTextDiffV.diff_prettyHtml(diffs);

        return mergedValue;
    }

    //-------------------------------------------------------------------------
    /**
     * Return the page title without applying text2html transformation.
     *
     * @return Return the page title.
     */
    public final String getRawTitle () {
        if(rawTitle==null) {
            rawTitle = mContentPage.getTitle(mEntryLoadRequest,false);
        }
        return rawTitle;
    }

    /**
     * Return the title in the current language if it exists, otherwise the page ID + the titles in other languages.
     * 
     *
     * @param jParams the processing context
     * @return the title as a 2-items list, the first member being the localized title, the second the other titles
     */
    public final List<String> getDisplayableLocalizedTitle(ProcessingContext jParams) {
        String pageLabel = JahiaResourceBundle.getJahiaInternalResource("org.jahia.page.label", jParams.getLocale());
        String curTitle = getContentPage().getTitles(true).get(jParams.getCurrentLocale().getLanguage()) ;
        String otherLangTitles = null ;
        if (curTitle == null || curTitle.equals("")) {
            curTitle = new StringBuilder(pageLabel).append(" ").append(getID()).toString() ;
            StringBuilder otherLang = new StringBuilder() ;
            Map<String, String> titles = getContentPage().getTitles(true) ;
            for (String langCode : titles.keySet()) {
                String aTitle = titles.get(langCode) ;
                if (aTitle != null && !aTitle.equals("")) {
                    if (otherLang.length() == 0) {
                        otherLang.append("(") ;
                    } else {
                        otherLang.append(" / ") ;
                    }
                    otherLang.append(langCode).append(": ").append(aTitle) ;
                }
            }
            if (otherLang.length() > 0) {
                otherLang.append(")") ;
            }
            otherLangTitles = otherLang.toString() ;
        }
        List<String> ret = new ArrayList<String>() ;
        ret.add(curTitle) ;
        if (otherLangTitles != null) {
            ret.add(otherLangTitles) ;
        }
        return ret ;
    }

    //-------------------------------------------------------------------------
    /**
     * Increment by one unit the page hit counter.
     */
    public final void incrementCounter () {
//        mContentPage.incrementCounter (mEntryLoadRequest);
    }

    //-------------------------------------------------------------------------
    /**
     * Set the new page defintion ID. The ID must point to a existing page
     * definition.
     *
     * @param value The new page defintion ID.
     *
     * @throws JahiaException                 Throw this exception on any error. Only ERROR type error should
     *                                        be catched, all the other failures should be thrown further.
     * @throws JahiaTemplateNotFoundException raised in case the template
     *                                        is not found.
     */
    public boolean setPageTemplateID (int value)
            throws JahiaException, JahiaTemplateNotFoundException {
        boolean changed = this.getPageTemplateID() != value;
        mContentPage.setPageTemplateID (value, mEntryLoadRequest);
        mPageTemplate = mContentPage.getPageTemplate(mEntryLoadRequest);
        return changed;
    }


    //-------------------------------------------------------------------------
    public void setPageTemplate (JahiaPageDefinition value)
            throws JahiaTemplateNotFoundException, JahiaException {
        mContentPage.setPageTemplate (value, mEntryLoadRequest);
        mPageTemplate = value;
    }


    //-------------------------------------------------------------------------
    /**
     * Set the new internal link ID. This ID must be an existing page ID.
     *
     * @param value The new page link ID.
     */
    public final void setPageLinkID (int value) {
        mContentPage.setPageLinkID (value, mEntryLoadRequest);
        pageLinkID = -2;
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
    public final void setPageType (int value)
            throws JahiaException {
        mContentPage.setPageType (value, mEntryLoadRequest);
        pageType = -2;
    }


    /**
     * Set the ACL ID. Actually used when a page has change it's type. Passing from
     * URL link or DIRECT page to a Jahia page LINK type and vice versa.
     * WARNING ! Be careful with this method. Use it if it is really necessary
     * to change the ACL ID page.
     *
     * @param aclID The ACL ID to set.
     *
     * @throws JahiaException
     */
    public final void setAclID (int aclID)
            throws JahiaException {
        mContentPage.setAclID (aclID, mEntryLoadRequest);
        mACL = mContentPage.getACL();
    }

    //-------------------------------------------------------------------------
    /**
     * Set the new remote URL. The page type will change accordingly.
     *
     * @param value The new remoteURL.
     */
    public final void setRemoteURL (String value)
            throws JahiaException {
        mContentPage.setRemoteURL (value, mEntryLoadRequest);
        remoteURL=null;
    }


    //-------------------------------------------------------------------------
    /**
     * Change the page title.
     *
     * @param value String holding the new page title.
     */
    public final void setTitle (String value) {
        mContentPage.setTitle (value, mEntryLoadRequest);
        title = null;
    }

    //--------------------------------------------------------------------------
    /**
     * Set the titles for a set of languages
     *
     * @param languageCode
     * @param title
     */
    public final void setTitle (String languageCode, String title)
            throws JahiaException {
        mContentPage.setTitle (languageCode, title, mEntryLoadRequest);
        title = null;
    }

    //--------------------------------------------------------------------------
    /**
     * Set the titles for a set of languages
     *
     * @param languagesSet a set of languages as String for which to set the title.
     * @param titles       Map of key/value pair ( languageCode/title value )
     */
    public final boolean setTitles (Set<String> languagesSet, Map<String, String> titles)
            throws JahiaException {
        mContentPage.setTitles (languagesSet, titles, mEntryLoadRequest);
        if ( this.getID() <=0 ){
            return true;
        }
        ContentPage contentPage = ContentPage.getPage(this.getID());
        Map<String, String> lastUpdatedTitles = contentPage.getTitles(true);
        for (String lang : languagesSet){
            String newTitle = titles.get(lang);
            String title = lastUpdatedTitles.get(lang);
            return (!new EqualsBuilder()
                .append(title, newTitle)
                .isEquals());
        }
        return false;
    }

    /**
     * This is used in Compare mode to display the warning that the page is moved.
     *
     * @return
     */
    public boolean isMoved() {
        return moved;
    }

    public void setMoved(boolean moved) {
        this.moved = moved;
    }

    //-------------------------------------------------------------------------
    /**
     * Return the page URL
     * for backward compatibility.
     *
     * @return Return the page URL string.
     *
     * @deprecated please use the getURL() instead in order to comply to
     *             Sun's conventions
     */
    public String getUrl (ProcessingContext jParams)
            throws JahiaException {
        return getURL (jParams);
    } // end getUrl


    //-------------------------------------------------------------------------
    /**
     * Return the page URL
     *
     * @return Return the page URL string.
     */
    public String getURL (ProcessingContext jParams)
            throws JahiaException {
        String outURL = "";
        String opmode ="";
        String title="";
        int type=getPageType ();
        switch (getPageType ()) {
            case (TYPE_DIRECT):
                if (jParams != null) {
                    opmode=jParams.getOperationMode();
                    outURL = jParams.composePageUrl(this);
                    title=this.getTitle();
                }
                break;

            case (TYPE_LINK):
                if (jParams != null) {
                    int linkPageID = -1;
                    opmode=jParams.getOperationMode();
                    try {
                        linkPageID = getPageLinkID ();
                        if (linkPageID != -1) {
                            ContentPage linkPage = ContentPage.getPage (linkPageID, false);
                            // require at least read access
                            if (linkPage.checkReadAccess (jParams.getUser ())) {
                                outURL = jParams.composePageUrl(linkPage.getPage(jParams));
                                title=linkPage.getPage(jParams).getTitle();
                            } else {
                                logger.debug("No read access to linked page [" + linkPageID + "]");
                            }
                        } else {
                             logger.debug("NO ID for this page object!!!!!!");
                        }

                    } catch (Exception t) {
                        logger.debug (
                                "Exception creating link url with page[" + linkPageID + "]", t);
                    }
                }
                break;
            case (TYPE_URL):
                outURL = getRemoteURL ();
                break;
        }
        if(opmode.equals("normal")){
            logger.debug("opmode="+opmode+" xx "+title+" type:"+type);
        }
        return outURL;
    } // end getURL


    // -------------------------------------------------------------------------
    /**
     * Return the page path. The page path consist of all the parent pages of
     * the specified page until the site's root page.
     * @deprecated since 6.0
     * @return Return a Iterator of JahiaPage objects. The returned
     *         Iterator is always non-null, but might have no pages if the
     *         specified page has not childs, or if no childs matching the
     *         loading flag were found.
     */
    public Iterator<ContentPage> getContentPagePath(String operationMode, JahiaUser user)
            throws JahiaException {
        return getContentPage().getContentPagePath(mEntryLoadRequest, operationMode, user);
    } // end getPath

    /**
     * Return the page path. The page path consist of all the parent pages of
     * the specified page until the site's root page.
     *
     * @return Return a Iterator of JahiaPage objects. The returned
     *         Iterator is always non-null, but might have no pages if the
     *         specified page has not childs, or if no childs matching the
     *         loading flag were found.
     */
    public List<ContentPage> getContentPagePathAsList(String operationMode, JahiaUser user)
            throws JahiaException {
        return getContentPage().getContentPagePathAsList(mEntryLoadRequest, operationMode, user);
    }
    
    // -------------------------------------------------------------------------
    /**
     * Return the page path. The page path consist of all the parent pages of
     * the specified page until the site's root page.
     *
     * @param levels an integer specifying the offset of levels to retrieve.
     *               So if the page is a depth level 5 and we specify that we want to retrieve
     *               only 2 levels, only levels 4 and 5 will be returned by this method.
     *
     * @return Return a Iterator of ContentPage objects. The returned
     *         Iterator is always non-null, but might have no pages if the
     *         specified page has not childs, or if no childs matching the
     *         loading flag were found.
     */
    public Iterator<ContentPage> getContentPagePath(int levels, String operationMode,
            JahiaUser user) throws JahiaException {
        return getContentPage().getContentPagePath(levels, mEntryLoadRequest,
                operationMode, user, JahiaPageService.PAGEPATH_SHOW_ALL);
    } // end getPath    

    //-------------------------------------------------------------------------
    /**
     * Return the page path. The page path consist of all the parent pages of
     * the specified page until the site's root page.
     *
     * @return Return a Iterator of JahiaPage objects. The returned
     *         Iterator is always non-null, but might have no pages if the
     *         specified page has not childs, or if no childs matching the
     *         loading flag were found.
     *
     * @deprecated used getContentPagePath
     */
    public Iterator<JahiaPage> getPagePath(String operationMode, JahiaUser user)
            throws JahiaException {
        return getContentPage().getPagePath(mEntryLoadRequest, operationMode, user);        
    } // end getPath    
    
    // -------------------------------------------------------------------------
    /**
     * Return the page path. The page path consist of all the parent pages of
     * the specified page until the site's root page.
     *
     * @param levels an integer specifying the offset of levels to retrieve.
     *               So if the page is a depth level 5 and we specify that we want to retrieve
     *               only 2 levels, only levels 4 and 5 will be returned by this method.
     *
     * @return Return a Iterator of JahiaPage objects. The returned
     *         Iterator is always non-null, but might have no pages if the
     *         specified page has not childs, or if no childs matching the
     *         loading flag were found.
     *
     * @deprecated use getContentPagePath
     */
    public Iterator<JahiaPage> getPagePath(int levels, String operationMode,
            JahiaUser user) throws JahiaException {
        return getContentPage().getPagePath(levels, mEntryLoadRequest, operationMode, user);        
    } // end getPath

    // -------------------------------------------------------------------------
    /**
     * Return an Iterator holding all the child pages of the specified page.
     * The loading flag filters the kind of pages to return.
     *
     * @return Return an Iterator of JahiaPage objects. Return null if not
     *         the current page has not childs.
     *
     * @throws JahiaException Return this exception if any failure occured.
     */
    public List<JahiaPage> getChilds ()
            throws JahiaException {
        List<JahiaPage> childs = ServicesRegistry.getInstance ().getJahiaPageService ().getPageChilds (getID (), PageLoadFlags.ALL,
                mEntryLoadRequest);
        if (childs != null) {
            return childs;
        }
        return null;
    } // end getChilds

    public List<JahiaPage> getChilds (JahiaUser user)
            throws JahiaException {
        List<JahiaPage> childs = ServicesRegistry.getInstance ().getJahiaPageService ().getPageChilds (getID (), PageLoadFlags.ALL, user);
        // logger.debug("Nb child found " + childs.size());
        if (childs != null) {
            return childs;
        }
        return null;
    } // end getChilds

    //-------------------------------------------------------------------------
    /**
     * Compare between two objects, sort by their name
     *
     * @param c1 left-side object
     * @param c2 right-side object
     *
     * @return <0 if c1 < c2, 0 if c1=c2, >0 if c1>c2
     *
     * @throws ClassCastException if the objects where not of type JahiaPage.
     */
    public int compare (JahiaPage c1, JahiaPage c2) throws ClassCastException {

        return (c1.getTitle ().toLowerCase ()
                .compareTo (c2.getTitle ().toLowerCase ()));

    }


    //-------------------------------------------------------------------------
    public String toString () {
        StringBuffer output = new StringBuffer ();
        output.append ("Detail of page [");
        output.append (getID ());
        output.append ("] :\n");
        output.append ("  - Site ID       [");
        output.append (getJahiaID ());
        output.append ("]\n");
        output.append ("  - Parent ID     [");
        output.append (getParentID ());
        output.append ("]\n");
        output.append ("  - Type          [");
        output.append (PAGE_TYPE_NAMES[getPageType ()]);
        output.append ("]\n");
        output.append ("  - Ttitle        [");
        output.append (getTitle ());
        output.append ("]\n");
        output.append ("  - Template ID   [");
        output.append (getPageTemplateID ());
        output.append ("]\n");
        output.append ("  - Remote URL    [");
        output.append (getRemoteURL ());
        output.append ("]\n");
        output.append ("  - Link ID       [");
        output.append (getPageLinkID ());
        output.append ("]\n");
        output.append ("  - ACL ID        [");
        output.append (getAclID ());
        output.append ("]\n");
        output.append("  - EntryLoadRequest=").append(mEntryLoadRequest).append("\n");

        return output.toString ();
    }

    private boolean checkPropertiesAvailability ()
            throws JahiaException {
        if (mProperties == null) {
            // properties never yet loaded, let's do it as quickly as we can.
            mProperties = pageManager.getPageProperties (getID ());
        }
        if (mProperties == null) {
            logger.debug ("Error while loading page properties !");
            return false;
        }
        return true;
    }


    /**
     * Retrieves a page's local property by name
     *
     * @param name a String containing the name of the page property to
     *             return.
     *
     * @return the PageProperty if it could be found for this page, or null
     * if not found.
     *
     * @throws JahiaException raised if there was a problem accessing the
     *                        backend systems that contain the properties
     */
    public PageProperty getPageLocalProperty (String name)
            throws JahiaException {
        if (checkPropertiesAvailability ()) {
            return (PageProperty) mProperties.get (name);
        } else {
            logger.debug ("Error accessing page property " + name +
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
     *
     * @return a String containing the value of the page property or null if
     *         the property couldn't be found.
     *
     * @throws JahiaException raised if there was a problem accessing the
     *                        backend systems that contain the properties
     */
    public String getProperty (String name)
            throws JahiaException {
        PageProperty curProp = getPageLocalProperty (name);
        if (curProp != null) {
            return curProp.getValue ();
        } else {
            // we could find it locally, let's try to find it in the parent if
            // this object has one.
            if (getParentID () > 0) {
                // we are not in the case of the root page or a page without
                // a parent
                JahiaPage parentPage = ServicesRegistry.getInstance ().getJahiaPageService ().lookupPage (getParentID (),
                        mEntryLoadRequest);
                if (parentPage != null) {
                    // let's recursively look for the property in parent
                    // pages
                    return parentPage.getProperty (name);
                }
            }
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
     *
     * @return a String containing the property value for the given language OR
     *         the default value if it couldn't be found.
     *
     * @throws JahiaException raised if there was a problem accessing the
     *                        backend systems that contain the properties
     */
    public String getProperty (String name, String languageCode)
            throws JahiaException {
        PageProperty curProp = getPageLocalProperty (name);
        if (curProp != null) {
            return curProp.getValue (languageCode);
        } else {
            // we could find it locally, let's try to find it in the parent if
            // this object has one.
            if (getParentID () > 0) {
                // we are not in the case of the root page or a page without
                // a parent
                JahiaPage parentPage = ServicesRegistry.getInstance ().getJahiaPageService ().lookupPage (getParentID (),
                        mEntryLoadRequest);
                if (parentPage != null) {
                    // let's recursively look for the property in parent
                    // pages
                    return parentPage.getProperty (name, languageCode);
                }
            }
            return null;
        }
    }

    /**
     * Sets a page property value. This updates both the in-memory and
     * persistant systems simultaneously so it might have a performance
     * impact. This sets the default value of the property.
     *
     * @param name  the name of the page property to be set
     * @param value the value of the page property
     *
     * @throws JahiaException raised if there was a problem accessing the
     *                        backend systems that contain the properties
     */
    public boolean setProperty (String name, String value)
            throws JahiaException {
        PageProperty targetProperty = null;
        if (mProperties != null) {
            targetProperty = (PageProperty) mProperties.get (name);
        }
        if (targetProperty == null) {
            targetProperty = new PageProperty (getID (), name);
        }
        if ((targetProperty.getValue() == null && value == null) || targetProperty.getValue().equals(value)) {
            return false;
        }
        targetProperty.setValue (value);
        pageManager.setPageProperty (targetProperty);
        if (mProperties != null) {
            mProperties.put (name, targetProperty);
        }
        return true;
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
     *
     * @throws JahiaException raised if there was a problem accessing the
     *                        backend systems that contain the properties
     */
    public void setProperty (String name, String languageCode, String value)
            throws JahiaException {
        PageProperty targetProperty = null;
        if (mProperties != null) {
            targetProperty = (PageProperty) mProperties.get (name);
        }
        if (targetProperty == null) {
            targetProperty = new PageProperty (getID (), name);
        }
        targetProperty.setValue (value, languageCode);
        pageManager.setPageProperty (targetProperty);
        if (mProperties != null) {
            mProperties.put (name, targetProperty);
        }
    }

    /**
     * Remove a property. This updates both the in-memory and
     * persistant systems simultaneously so it might have a performance
     * impact.
     *
     * @param name  the name of the page property to be removed
     *
     * @throws JahiaException raised if there was a problem accessing the
     *                        backend systems that contain the properties
     */
    public boolean removeProperty (String name)
            throws JahiaException {
        PageProperty targetProperty = null;
        if (mProperties != null) {
            targetProperty = getPageLocalProperty(name);
            if (targetProperty != null) {
                mProperties.remove(name);
                pageManager.removePageProperty(targetProperty);
                return true;
            }
        }
        return false;
    }

    /**
     * The purpose of this method is to "activate" all the data that is in the
     * staging state. This destroys all internal staging versions so make sure
     * you call this only when really ready to active. For changing staging
     * status use the other method : setWorkflowState also in this class.
     *
     * @param saveVersion the version save information needed in the case we
     *                    validate the page's content.
     * @param user        the user making the activation
     *
     * @throws JahiaException in the case there is an error while validating
     *                        the page's content (if withContent=true of course)
     */
    public ActivationTestResults activeStagingVersion (
            Set<String> languageCodes,
            JahiaSaveVersion saveVersion,
            JahiaUser user,
            ProcessingContext jParams,
            StateModificationContext stateModifContext)
            throws JahiaException {
        /** @todo FIXME add null checks here ! */
        boolean versioningActive = ServicesRegistry.getInstance ().getJahiaVersionService ()
                .isVersioningEnabled (jParams.getSiteID ());
        return mContentPage.activate (languageCodes, versioningActive, saveVersion,
                user, jParams, stateModifContext);
    }

    /**
     * Tests if a page is valid for activation.
     *
     * @param saveVersion
     * @param user
     * @param jParams
     *
     * @return an ActivationTestResults object that contains the status of
     *         the activation tests, including error and warning messages.
     *
     * @throws JahiaException
     */
    public ActivationTestResults isValidForActivation (
            Set<String> languageCodes,
            JahiaSaveVersion saveVersion,
            JahiaUser user,
            ProcessingContext jParams,
            StateModificationContext stateModifContext)
            throws JahiaException {
        return mContentPage.isValidForActivation (languageCodes,
                jParams, stateModifContext);
    }

    /**
     * Changes the status of the staging page infos. This is used to switch
     * to another status, before going to active status. No versioning is
     * done during a staging status change.
     *
     * @param newVersionStatus the new status mode. This must be bigger or
     *                         equal to JahiaLoadVersion.STAGING, otherwise this method will exist
     *                         immediately.
     */
    public void changeStagingStatus (Set<String> languageCodes, int newVersionStatus,
                                     ProcessingContext jParams,
                                     StateModificationContext stateModifContext)
            throws JahiaException {
        mContentPage.setWorkflowState (languageCodes, newVersionStatus,
                jParams, stateModifContext);
    }

    /**
     * Returns true if the page has active entries
     *
     * @return true if the page has at least an active state. false if there
     *         are only staged entries.
     */
    public boolean hasActiveEntries () {
        return mContentPage.hasActiveEntries ();
    }

    /**
     * Return true if the page has specified 'pageInfos' entries in the specified
     * language.
     *
     * @param pageInfosFlag Kind of page infos desired. This parameter can associate
     *                      the previous constants. For example ACTIVE_PAGE_INFOS | STAGING_PAGE_INFOS
     *                      look for both active and staged pages and return the appropriate result.
     * @param languageCode  The specified language code.
     *
     * @return True if it is at least one entry for this page.
     */
    public boolean hasEntry (int pageInfosFlag, String languageCode) {
        return mContentPage.hasEntries (pageInfosFlag, languageCode);
    }

    public Map<String, Integer> getLanguagesStates (boolean withContent) {
        return mContentPage.getLanguagesStates (withContent);
    }

    /**
     * Return jcr path "/siteKey/pageKey"
     *
     * @param context
     * @return
     * @throws JahiaException
     */
    public String getJCRPath(ProcessingContext context) throws JahiaException {
        if (this.getID()==-1){
            return "";
        }
        return this.getContentPage().getJCRPath(context);
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

     private void readObject(java.io.ObjectInputStream in)
         throws IOException, ClassNotFoundException {
         in.defaultReadObject();
         pageManager = (JahiaPagesManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaPagesManager.class.getName());
     }

    public int getSiteID() {
        return mContentPage.getSiteID();
    }
    
    public String getURLKey() throws JahiaException {
        return getProperty(PageProperty.PAGE_URL_KEY_PROPNAME);
    }
}
