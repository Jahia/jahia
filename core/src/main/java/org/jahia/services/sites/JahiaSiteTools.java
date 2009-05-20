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
//  JahiaSite
//
//  NK      11.07.2001
//
//

package org.jahia.services.sites;

import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaSiteGroupManagerService;
import org.jahia.services.usermanager.JahiaSiteUserManagerService;
import org.jahia.services.usermanager.JahiaUser;


/**
 * Class JahiaSiteTools.<br>
 *
 * @author Khue ng
 * @version 1.0
 */
public final class JahiaSiteTools {

    /**
     * Create a new membership for a user
     *
     * @param user  the user to add as member
     * @param site  the site
     */
    public static boolean addMember (JahiaUser user, JahiaSite site)
            throws JahiaException {

        if (site == null || user == null) {
            return false;
        }
        JahiaSiteUserManagerService jsums = ServicesRegistry.getInstance ()
                .getJahiaSiteUserManagerService ();

        if (jsums == null) {
            return false;
        }

        return jsums.addMember (site.getID (), user);
    }


    /**
     * Add a group membership on this site ( in a group sharing context )
     *
     * @param grp   the group reference
     * @param site  the site reference
     */
    public static boolean addGroup (JahiaGroup grp, JahiaSite site)
            throws JahiaException {

        if (site == null || grp == null) {
            return false;
        }

        JahiaSiteGroupManagerService jsgms = ServicesRegistry.getInstance ()
                .getJahiaSiteGroupManagerService ();
        if (jsgms == null) {
            return false;
        }

        return jsgms.addGroup (site.getID (), grp);
    }


    /**
     * return the admin group of this site
     *
     * @param site  the site reference
     */
    public static JahiaGroup getAdminGroup (JahiaSite site)
            throws JahiaException {

        if (site == null) {
            return null;
        }

        JahiaGroupManagerService jgms = ServicesRegistry.getInstance ()
                .getJahiaGroupManagerService ();
        if (jgms == null) {
            return null;
        }

        return jgms.getAdministratorGroup (site.getID ());
    }



}
