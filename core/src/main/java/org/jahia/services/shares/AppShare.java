/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
//
//
//  AppShare
//
//  NK      31.04.2001
//
//

package org.jahia.services.shares;


/**
 * Class AppShare.<br>
 *
 * @author Khue ng
 * @version 1.0
 */
public class AppShare {

    /** the app id * */
    private int mAppID = -1;

    /** the site id * */
    private int mSiteID = -1;

    /**
     * Constructor
     */
    protected AppShare () {
    }

    /**
     * Constructor
     */
    public AppShare (int appID, int siteID) {

        mAppID = appID;
        mSiteID = siteID;
    }


    //-------------------------------------------------------------------------
    public int getAppID () {
        return mAppID;
    }

    //-------------------------------------------------------------------------
    public void setAppID (int id) {
        mAppID = id;
    }

    //-------------------------------------------------------------------------
    public int getSiteID () {
        return mSiteID;
    }

    //-------------------------------------------------------------------------
    public void setSiteID (int id) {
        mSiteID = id;
    }


}
