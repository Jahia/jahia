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
