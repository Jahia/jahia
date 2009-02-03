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
//


package org.jahia.services.pages;

import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.JahiaPagesManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.services.version.EntryStateable;

import java.io.Serializable;
import java.io.IOException;

/**
 * This class is used internally in Jahia to keep the page information stored
 * in the database. Each time a page is loaded it's information are stored
 * into the cache using this class.<br/>
 * <br/>
 * This class knows how to save it's content, but it's not an automatic process
 * to avoid one database access for each little change made to the page data.
 * Therefore a call to the {@link #commitChanges commitChanges()} method is
 * needed to save the update information to the database.
 *
 * @author Fulco Houkes
 * @version 1.0
 */
public class JahiaPageInfo implements PageInfoInterface, EntryStateable, Serializable {
    private int mID;                // page unique identification number
    private int mSiteID;           // site unique identification number
    private int mParentID;          // parent page ID
    private int mPageType;          // page type
    private String mTitle;             // page title
    private int mPageTemplateID;    // page definition ID
    private String mRemoteURL;         // external URL (not a jahia internal link)
    private int mPageLinkID;        // if the page is an internal link, hold the page ID
    private int mAclID;             // access control list ID

    private String mLanguageCode;
    private int mVersionID;
    private int mVersionStatus;

    // these variables are
    private int mTempParentID;          // temporary, parent page ID
    private int mTempAclID;             // temporary, access control list ID
    private int mTempPageType;          // temporary, page type
    private String mTempTitle;             // temporary, page title
    private int mTempPageTemplateID;    // temporary, page definition ID
    private String mTempRemoteURL;         // temporary, external URL (not a jahia internal link)
    private int mTempPageLinkID;        // temporary, if the page is an internal link, hold the page ID

    private boolean mDataChanged = false;  // true if the internal state of the page
    private boolean mTemplateChanged = false;  // true if the internal state of the page
    // has been changed with a setxxxx method.

    private int mHashCode = -1; // used to avoid recalculating hash code if
    // nothing has changed.

    private transient JahiaPagesManager pageManager;

    private transient String cacheToString = null;
    /*
    protected JahiaPageInfo (int ID, int jahiaID, int parentID, int pageType,
                             String title, int pageTemplateID, String remoteURL,
                             int pageLinkID, String creator, String doc,
                             int counter, int aclID) {
        this(ID, jahiaID, parentID, pageType, title, pageTemplateID, remoteURL,
             pageLinkID, creator, doc, counter, aclID, 0, 0, "*");
    }
    */

    //-------------------------------------------------------------------------
    /**
     * constructor
     */
    public JahiaPageInfo(int ID, int jahiaID, int parentID, int pageType,
                         String title, int pageTemplateID, String remoteURL,
                         int pageLinkID,
                         int aclID,
                         int versionID, int versionStatus, String languageCode,
                         JahiaPagesManager pageManager) {
        mID = ID;
        mSiteID = jahiaID;
        mParentID = parentID;
        mTitle = title;
        mAclID = aclID;
        mPageType = pageType;
        mPageTemplateID = pageTemplateID;
        mVersionID = versionID;
        mVersionStatus = versionStatus;
        mLanguageCode = languageCode;

        if (remoteURL == null) {
            mRemoteURL = NO_REMOTE_URL;
        } else {
            mRemoteURL = remoteURL;
        }

        mPageLinkID = pageLinkID;

        // temporary variables
        mTempParentID = mParentID;
        mTempAclID = mAclID;
        mTempPageType = mPageType;
        mTempTitle = mTitle;
        mTempPageTemplateID = pageTemplateID;
        mTempRemoteURL = mRemoteURL;
        mTempPageLinkID = mPageLinkID;
        mDataChanged = false;
        mTemplateChanged = false;
        this.pageManager = pageManager;
        cacheToString = null;
    } // end constructor


    public boolean equals (Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final JahiaPageInfo rightPageInfo = (JahiaPageInfo) obj;
            return ((getID () == rightPageInfo.getID ()) &&
                (getWorkflowState () == rightPageInfo.getWorkflowState ()) &&
                (getVersionID () == rightPageInfo.getVersionID ()) &&
                (getLanguageCode ().equals (rightPageInfo.getLanguageCode ())));
        }
        return false;
    }

    public int hashCode () {
        if ((mDataChanged) || (mHashCode == -1)) {
        StringBuffer buff = new StringBuffer (30);
        buff.append (mID);
        buff.append ("_");
        buff.append (mVersionID);
        buff.append ("_");
        buff.append (mVersionStatus);
        buff.append ("_");
        buff.append (mLanguageCode);
            mHashCode = buff.toString ().hashCode ();
        }
        return mHashCode;
    }

    //-------------------------------------------------------------------------
    /**
     * Return the page's unique identification number.
     *
     * @return Return the page ID.
     */
    public int getID () {
        return mID;
    }

    public void setID(int ID) {
        this.mID = ID;
        cacheToString = null;
    }
    //-------------------------------------------------------------------------
    /**
     * Return the site ID in which the page is located.
     *
     * @return Return the page site ID.
     */
    public int getJahiaID () {
        return mSiteID;
    }

    //-------------------------------------------------------------------------
    /**
     * Return the parent page unique identification number.
     *
     * @return Return the parent page ID.
     */
    public int getParentID () {
        return mParentID;
    }

    //-------------------------------------------------------------------------
    /**
     * Return the page definition ID.
     *
     * @return Return the page definition ID.
     */
    public int getPageTemplateID () {
        return mPageTemplateID;
    }

    //-------------------------------------------------------------------------
    /**
     * Return the page title.
     *
     * @return Return the page title.
     */
    public String getTitle () {
        return mTitle;
    }

    //-------------------------------------------------------------------------
    /**
     * Return the ACL unique identification number.
     *
     * @return Return the ACL ID.
     */
    public int getAclID () {
        return mAclID;
    }

    //-------------------------------------------------------------------------
    /**
     * Return the page type
     *
     * @return Return the page type
     */
    public int getPageType () {
        return mPageType;
    }

    //-------------------------------------------------------------------------
    /**
     * Return the remote URL in case the page is an external reference (a non
     * Jahia page. If the page is not an external URL, "<no url>" is returned.
     *
     * @return Return the remote URL.
     */
    public String getRemoteURL () {
        return mRemoteURL;
    }

    //-------------------------------------------------------------------------
    /**
     * Return the internal jahia page ID in case the page is an internal
     * jahia link.
     *
     * @return Return the page link ID.
     */
    public int getPageLinkID () {
        return mPageLinkID;
    }




    //-------------------------------------------------------------------------
    /**
     * Change the page title.
     *
     * @param value String holding the new page title.
     */
    public synchronized void setTitle (String value) {
        mTempTitle = value;
        mDataChanged = true;
        cacheToString = null;
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
    public synchronized void setPageType (int value) {
        mTempPageType = value;

        switch (value) {
            case TYPE_LINK:
                // the page in a link to an internal page. Disable the
                // remote URL.
                mTempRemoteURL = NO_REMOTE_URL;
                break;

            case TYPE_URL:
                // the page is an extenal URL, disable the internal page link.
                mTempPageLinkID = TERMINATION_PAGE_ID;
                break;

            case TYPE_DIRECT:
                // the page is a REAL page, disable the internal and external
                // links.
                mTempRemoteURL = NO_REMOTE_URL;
                mTempPageLinkID = TERMINATION_PAGE_ID;
                break;
        }
        mDataChanged = true;
        cacheToString = null;
    }

    //-------------------------------------------------------------------------
    /**
     * Set the parent ID, which must point to an existing page.
     *
     * @param value The new parent ID.
     */
    public synchronized void setParentID (int value) {
        mTempParentID = value;
        mDataChanged = true;
        cacheToString = null;
    }

    /**
     * Set the ACL ID to an existing ACL.
     *
     * @param aclID
     */
    public synchronized void setAclID (int aclID) {
        mTempAclID = aclID;
        mDataChanged = true;
        cacheToString = null;
    }

    //-------------------------------------------------------------------------
    /**
     * Set the new remote URL. The page type will change accordingly.
     *
     * @param value The new remoteURL.
     */
    public synchronized void setRemoteURL (String value) {
        mTempRemoteURL = value;
        mTempPageType = TYPE_URL;
        mTempPageLinkID = TERMINATION_PAGE_ID;
        mDataChanged = true;
        cacheToString = null;
    }


    //-------------------------------------------------------------------------
    /**
     * Set the new internal link ID. This ID must be an existing page ID.
     *
     * @param value The new page link ID.
     */
    public synchronized void setPageLinkID (int value) {
        mTempPageLinkID = value;
        mTempPageType = TYPE_LINK;
        mTempRemoteURL = NO_REMOTE_URL;
        mDataChanged = true;
        cacheToString = null;
    }


    //-------------------------------------------------------------------------
    /**
     * Set the new page defintion ID. The ID must point to a existing page
     * definition.
     *
     * @param value The new page defintion ID.
     */
    public synchronized final void setPageTemplateID (int value) {
        mTempPageTemplateID = value;
        mDataChanged = true;
        mTemplateChanged = true;
        cacheToString = null;
    }


    //-------------------------------------------------------------------------
    /**
     * Commit into the database all the previous changes on the page.
     *
     * @throws JahiaException Throws a JahiaException if the data could not
     *                        be saved successfully into the database.
     */
    public synchronized void commitChanges () throws JahiaException
    {
        commitChanges(true);
    }

    //-------------------------------------------------------------------------
    /** Commit into the database all the previous changes on the page.
     *
     * @param commitToDB, if true, store in db, else only copy temporary change
     * to effective values.
     *
     * @exception   JahiaException Throws a JahiaException if the data could not
     *              be saved successfully into the database.
     */
    public synchronized void commitChanges (boolean commitToDB)
    throws JahiaException
    {
        // commit only if the data has really changed
        if (mDataChanged) {
            // commit the modified changes.
            mParentID = mTempParentID;
            mAclID = mTempAclID;
            mPageType = mTempPageType;
            mTitle = mTempTitle;
            mPageTemplateID = mTempPageTemplateID;
            mRemoteURL      = mTempRemoteURL;
            mPageLinkID     = mTempPageLinkID;
            mRemoteURL = mTempRemoteURL;
            mPageLinkID = mTempPageLinkID;

            // commit the changes into the database.
            if (commitToDB)
                pageManager.updatePageInfo(this, mVersionID, mVersionStatus);
            mDataChanged = false;
            mTemplateChanged = false;
            cacheToString = null;
        }
    }

    protected JahiaPageInfo clonePageInfo (int versionID, int versionStatus,
                                           String languageCode) {
        return new JahiaPageInfo (mID, mSiteID, mParentID, mPageType,
                mTitle, mPageTemplateID, mRemoteURL,
                mPageLinkID,
                mAclID, versionID, versionStatus,
                languageCode, pageManager);
    }

    public String getLanguageCode () {
        return mLanguageCode;
    }

    public int getVersionID () {
        return mVersionID;
    }

    public int getWorkflowState () {
        return mVersionStatus;
    }

    public void setVersionID (int newVersionID) {
        mVersionID = newVersionID;
        mDataChanged = true;
        cacheToString = null;
    }

    public void setVersionStatus (int newVersionStatus) {
        mVersionStatus = newVersionStatus;
        mDataChanged = true;
        cacheToString = null;
    }

    public void setLanguageCode (String newLanguageCode) {
        mLanguageCode = newLanguageCode;
        mDataChanged = true;
        cacheToString = null;
    }

    public String toString () {
        if(cacheToString==null) {
        StringBuffer result = new StringBuffer (1024);
        result.append ("JahiaPageInfo:\n");
        result.append ("  id      =" + mID + "\n");
        /*result.append ("  siteID  =" + mSiteID + "\n");
        result.append ("  parentID=" + mParentID + "\n");
        result.append ("  pageType=" + mPageType + "\n");
        result.append ("  title=" + mTitle + "\n");
        result.append ("  pageTemplateID=" + mPageTemplateID + "\n");
        result.append ("  remoteURL=" + mRemoteURL + "\n");
        result.append ("  pageLinkID=" + mPageLinkID + "\n");
        result.append ("  creator=" + mCreator + "\n");
        result.append ("  date of creation=" + mDoc + "\n");
        result.append ("  counter=" + mCounter + "\n");
        result.append ("  aclID=" + mAclID + "\n");*/
        result.append ("  versionID=" + mVersionID + "\n");
        result.append ("  versionStatus=" + mVersionStatus + "\n");
        result.append ("  languageCode=" + mLanguageCode + "\n");
            cacheToString = result.toString();
        }
        return cacheToString;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

     private void readObject(java.io.ObjectInputStream in)
         throws IOException, ClassNotFoundException {
         in.defaultReadObject();
         pageManager = (JahiaPagesManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaPagesManager.class.getName());
     }

     public boolean hasChanged() {
         return mDataChanged;
     }

    public boolean hasTemplateChanged() {
        return mTemplateChanged;
    }
} // end JahiaPage
