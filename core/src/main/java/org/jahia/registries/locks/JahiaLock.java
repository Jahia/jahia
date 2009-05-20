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
package org.jahia.registries.locks;

import java.util.Date;
import java.util.Map;


/**
 * @auhtor  Fulco Houkes
 * @version 1.0
 */
class JahiaLock
{
    private String      mLockName;
    private Map<String, Object>   mLockData;
    private int         mTimeout;
    private long        mExpirationTime;


    //-------------------------------------------------------------------------
    public JahiaLock (String name, Map<String, Object> data, int timeout) {
        mLockName   = name;
        mLockData   = data;

        mTimeout    = timeout * 1000;
            // timeout in seconds, but the mExpirationTime is in milliseconds.

        resetTimeout();
    }


    //-------------------------------------------------------------------------
    public final String getLockName () {
        return mLockName;
    }

    //-------------------------------------------------------------------------
    public final Map<String, Object> getLockData () {
        return mLockData;
    }

    //-------------------------------------------------------------------------
    public final void setLockData (Map<String, Object> data) {
        mLockData = data;
    }

    //-------------------------------------------------------------------------
    public final void resetTimeout () {
        Date date = new Date();
        mExpirationTime = date.getTime() + mTimeout;
    }

    //-------------------------------------------------------------------------
    public boolean isValid () {
        Date date = new Date();
        return (date.getTime() < mExpirationTime);
    }
}
