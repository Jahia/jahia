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
 package org.jahia.services.applications;

import java.security.Principal;
import java.util.Map;

/**
 * <p>Title: Interface for pluggeable isUserInRole resolution.</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public interface UserRoleResolver {

    /**
     * Tests if the user is part of a role
     * @param context Map a context map object for passing context information,
     * whatever it might be.
     * @param user Principal the user to test
     * @param roleName String the role name to test the user
     * @return boolean true if the user is part of the specified role, false
     * otherwise.
     */
    public boolean isUserInRole(Map context, Principal user, String roleName);
}
