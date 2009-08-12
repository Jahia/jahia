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
//  GroupRoleUtils
//  MJ      09.03.2001
//
//

package org.jahia.services.usermanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.LoadFlags;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.pages.JahiaPage;


/**
 * Utility class to manipulate groupnames that represent application roles.
 *
 * @author MJ
 */


public class GroupRoleUtils {
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (GroupRoleUtils.class);

    private static RE groupNameRE;
    private static ServicesRegistry sReg;
    private static JahiaGroupManagerService gMgr;

    static {
        try {

            // example : 2_4_administrator
            // is a group defining the administrator role
            // for application 2 in container 4
            String groupNamePattern = "^([0-9]+)_([0-9]+)_(.+)$";
            groupNameRE = new RE (groupNamePattern);

        } catch (RESyntaxException e) {
            String errorMsg = "RE exception : " + e.getMessage ();
            logger.error (errorMsg);
        }
    }


    /**
     * Translates a programmatic group role name into human readable format.
     *
     * @param groupname The group name to check.
     *
     * @return A <code>String</code> representing the human readable group name.
     */
    public static String translate (String groupname) {
        if ((groupname != null) && (groupNameRE.match (groupname))) {
            Map roleInfo = decompose (groupname);
            StringBuffer buf = new StringBuffer ();
            buf.append ((String) roleInfo.get ("fieldName"));
            buf.append (":");
            buf.append ((String) roleInfo.get ("appName"));
            buf.append (":");
            buf.append ((String) roleInfo.get ("roleName"));

            return buf.toString ();
        } else {
            return groupname;
        }
    }


    /**
     * Get the Field ID out of an application role name.
     *
     * @param groupname The group name to check.
     *
     * @return the associated Field ID, as an <code>int</code>
     */
    public static int getfieldID (String groupname) {
        int fieldID = 0;
        if ((groupname != null) && (groupNameRE.match (groupname))) {
            Map groupInfo = decompose (groupname);
            Integer fieldIDObj = (Integer) groupInfo.get ("fieldID");
            fieldID = fieldIDObj.intValue ();
        }
        return fieldID;
    }


    /**
     * Get a list of groups serving as roles for an application, given the application's ID.
     *
     * @param appID The ID of the application to get the roles for.
     *
     * @return a <code>List</code> of Strings containing all the role names for the application.
     */
    public static List getRolesForApp (int appID) {
        List rolesList = new ArrayList();

        // obtain an instance of the Group Manager Service, and the list of all group names...
        ServicesRegistry sReg = ServicesRegistry.getInstance ();
        if (sReg != null) {
            gMgr = sReg.getJahiaGroupManagerService ();
            if (gMgr != null) {
                List groupnames = gMgr.getGroupnameList ();
                Iterator enumeration = groupnames.iterator();

                // dump all group names matching the specified application ID into a List
                while (enumeration.hasNext ()) {
                    String groupname = (String) enumeration.next ();
                    if (isRole (groupname)) {
                        if (getAppID (groupname) == appID) {
                            rolesList.add (groupname);
                        }
                    }
                }
            }
        }
        return rolesList;
    }


    /**
     * Get a list of groups serving as roles for an application, given the application's name.
     *
     * @param appName The name of the application to get the roles for.
     *
     * @return a <code>List</code> of Strings containing all the role names for the application.
     */
    public static List getRolesForApp (String appName) {
        List rolesList = new ArrayList();

        // obtain an instance of the Group Manager Service, and the list of all group names...
        sReg = ServicesRegistry.getInstance ();
        if (sReg != null) {
            gMgr = sReg.getJahiaGroupManagerService ();
            if (gMgr != null) {
                List groupnames = gMgr.getGroupnameList ();
                Iterator enumeration = groupnames.iterator();

                // dump all group names matching the specified application name into a List
                while (enumeration.hasNext ()) {
                    String groupname = (String) enumeration.next ();
                    if (isRole (groupname)) {
                        if (getAppName (groupname).equals (appName)) {
                            rolesList.add (groupname);
                        }
                    }
                }
            }
        }
        return rolesList;
    }


    /**
     * Get the Application ID out of an application role name.
     *
     * @param groupname The group name to check.
     *
     * @return the associated Application ID, as an <code>int</code>
     */
    public static int getAppID (String groupname) {
        int appID = 0;
        if ((groupname != null) && (groupNameRE.match (groupname))) {
            Map groupInfo = decompose (groupname);
            Integer appIDObj = (Integer) groupInfo.get ("appID");
            appID = appIDObj.intValue ();
        }
        return appID;
    }


    /**
     * Get the Field name out of an application role name.
     *
     * @param groupname The group name to check.
     *
     * @return the associated Field name, as a <code>String</code>
     */
    public static String getFieldName (String groupname) {
        String fieldName = "unknown";
        if ((groupname != null) && (groupNameRE.match (groupname))) {
            Map groupInfo = decompose (groupname);
            fieldName = (String) groupInfo.get ("fieldName");
        }
        return fieldName;
    }


    /**
     * Get the Application name out of an application role name.
     *
     * @param groupname The group name to check.
     *
     * @return the associated Application name, as a <code>String</code>
     */
    public static String getAppName (String groupname) {
        String appName = "unknown";
        if ((groupname != null) && (groupNameRE.match (groupname))) {
            Map groupInfo = decompose (groupname);
            appName = (String) groupInfo.get ("appName");
        }
        return appName;
    }


    /**
     * Get the Role name out of an application role name.
     *
     * @param groupname The group name to check.
     *
     * @return the associated Role name, as a <code>String</code>
     */
    public static String getRoleName (String groupname) {
        String roleName = "unknown";
        if ((groupname != null) && (groupNameRE.match (groupname))) {
            Map groupInfo = decompose (groupname);
            roleName = (String) groupInfo.get ("roleName");
        }
        return roleName;
    }


    /**
     * Utility method to decompose a programmatic application role name into info values.
     *
     * @param groupname The group name to decompose.
     *
     * @return A <code>Map</code> containing decomposed role info values :
     *         (appID, fieldID, appName, fieldName, roleName).
     */
    private static Map decompose (String groupname) {
        // set default values...
        Map roleInfo = new HashMap();

        // application ID is what's left of 1st undrescore, remains is what's right of it
        String appIDStr = groupname.substring (0, groupname.indexOf ("_"));

        String remains = groupname.substring ((groupname.indexOf ("_") + 1));

        // fieldID is what's left of 1st underscore in remains, role name is what's right of it
        String fieldIDStr = remains.substring (0, remains.indexOf ("_"));

        try {

            // get the name of the app
            String appName = ServicesRegistry.getInstance().getApplicationsManagerService().getApplication(appIDStr).getName();

            // get the name of the field
            String fieldName = null;
            int fieldID = Integer.parseInt (fieldIDStr);
            JahiaField field = ServicesRegistry.getInstance ().getJahiaFieldService ().loadField (
                    fieldID, LoadFlags.NOTHING);
            if (field == null) {
                fieldName = "unknown";
            } else {
                int pageID = field.getPageID ();
                JahiaPage page = ServicesRegistry.getInstance ().getJahiaPageService ()
                        .lookupPage (pageID);
                fieldName = page.getTitle ();
            }

            // get the name of the role
            String roleName = remains.substring ((remains.indexOf ("_") + 1));

            roleInfo.put ("appID", new Integer (appIDStr));
            roleInfo.put ("fieldID", new Integer (fieldID));
            roleInfo.put ("appName", appName);
            roleInfo.put ("fieldName", fieldName);
            roleInfo.put ("roleName", roleName);

        } catch (JahiaException je) {
            //
        } catch (NumberFormatException nfe) {
            //
        }

        return roleInfo;
    }


    /**
     * Determines if a group represents an application role or not.
     *
     * @param groupname The group name to check.
     *
     * @return A <code>String</code> representing the human readable group name.
     */
    public static boolean isRole (String groupname) {
        return (groupname != null && groupNameRE.match (groupname));
    }

}
