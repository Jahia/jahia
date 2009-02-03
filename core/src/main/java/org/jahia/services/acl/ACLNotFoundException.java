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

package org.jahia.services.acl;

import org.jahia.exceptions.JahiaException;

/**
 * This exception is thrown when an ACL object is requested and is not
 * present in the database.
 *
 * @author Fulco Houkes
 * @version 2.0
 */
public class ACLNotFoundException extends JahiaException {
    /** Unique identification number of the ACL that could not be found. */
    private int mAclID = -1;


    //-------------------------------------------------------------------------
    // Fulco    8 Jan. 2001
    /**
     * Default constructor
     */
    public ACLNotFoundException (int aclID) {
        super ("Access Control List not found.",
                "ACL [" + aclID + "] could not be found.", ACL_ERROR, ERROR_SEVERITY);

        mAclID = aclID;
    }


    //-------------------------------------------------------------------------
    // Fulco    18 Apr. 2001
    /**
     * Return the Access Control List unique identification number that could
     * not be found.
     *
     * @return The ACL ID that could not be found.
     */
    public int getAclID () {
        return mAclID;
    }
}
