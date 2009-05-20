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
// NK 12.04.2001 - Added to support Multi Site and Servlet API


package org.jahia.services.usermanager;


import java.security.Principal;

/**
 * The minimal implementation of a Principal
 * Used in Servlet Request Wrapper getUserPrincipal().
 * It's a wrapper around a JahiaUser but its getName() return the login username,
 * not the jahia user internal key.
 *
 * @author NK
 */
public class GenericPrincipal implements Principal {

    private JahiaUser mUser;

    public GenericPrincipal (JahiaUser user) {
        mUser = user;
    }

    public String getName () {
        if (mUser == null) {
            return "";
        }
        return mUser.getUsername ();
    }

    public boolean equals (Object obj) {
        if (mUser == null) {
            return false;
        }
        
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            return (mUser.getName ().equals (((Principal) obj).getName ()));
        }
        return false;
    }

    public int hashCode () {
        if (mUser == null) {
            return -1;
        }
        return mUser.hashCode ();
    }


    public String toString () {
        if (mUser == null) {
            return "";
        }

        StringBuffer output = new StringBuffer ("Detail of user [" + mUser.getUsername () + "]\n");
        output.append ("  - ID [" + Integer.toString (mUser.hashCode ()) + "]");
        return output.toString ();
    }

}