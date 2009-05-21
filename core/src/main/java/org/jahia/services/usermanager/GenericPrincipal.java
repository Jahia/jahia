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