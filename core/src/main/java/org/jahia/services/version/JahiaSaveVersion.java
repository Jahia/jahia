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
//  JahiaSaveVersion
//  DJ 03/05/02
//

package org.jahia.services.version;

import org.jahia.registries.ServicesRegistry;

/**
 * Class describing which version of a field/container/containerlist to save
 */
public class JahiaSaveVersion {

    private boolean     staging;
    private boolean     versioning;
    private int         versionID;

    private ServicesRegistry sr = ServicesRegistry.getInstance();

   /**
    * constructor to be used when we know the versionStatus and the versionID to save
    */
    public JahiaSaveVersion (boolean staging, boolean versioning, int versionID)
    {
        this.staging = staging;
        this.versioning = versioning;
        this.versionID = versionID;
    }
   /**
    * constructor to be used when you want to get the current versionID automatically
    */

    public JahiaSaveVersion (boolean staging, boolean versioning)
    {
        this.staging = staging;
        this.versioning = versioning;
        this.versionID = sr.getJahiaVersionService().getCurrentVersionID();
    }

    public JahiaSaveVersion() {
    }

    public int  getVersionID()        { return versionID; }

    public int  getWorkflowState()    { return staging?2:1; }

    public boolean isCurrent()
    {
        return (!staging);
    }

    public boolean isStaging()
    {
        return (staging);
    }

    public boolean isVersioned()
    {
        return (versioning);
    }

    public String toString()
    {
        return new String ("[SAVE:staging="+staging+", versioning="+versioning+", versionID="+versionID+"]");
    }


} // end JahiaSaveVersion
