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
