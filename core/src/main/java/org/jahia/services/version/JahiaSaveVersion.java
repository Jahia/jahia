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
