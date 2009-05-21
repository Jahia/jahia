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
//  GroupsTools
//  NK      21.03.2001
//
//


package org.jahia.services.usermanager;

import org.apache.regexp.RE;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;

import java.util.List;
import java.util.ArrayList;


/**
 * Tools class to handle Application Role Groups
 *
 * @author NK
 */


public class GroupsTools {
    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (GroupsTools.class);

    /**
     * Pattern used to match Application Role Group Name:
     *
     *
     * ex:
     *
     *		In case of Application Role Group Names:
     *		appid + "_" + fieldid + "_" + role name
     *
     *		23_1_administrator :
     *									app id = 23,
     * 									field id = 1,
     *									stored database group name = 23_1_administrator
     *									role name part = administrator
     *
     **/

    /**
     * Pattern used to match the identifier composed by : appid + "_" + fieldid + "_" + role name
     * in case of groups that are applications' roles
     */
    private static String mRole_GroupName_Pattern = "^\\d+_\\d+_[\\w|_]+";

    /** Pattern used to extract the first id part */
    private static String mFirstIDPart = "(^\\d+)_.+";

    /** Pattern used to extract the second id part */
    private static String mSecondIDPart = "^\\d+_(\\d+)_.+";


    /**
     * Pattern used to extract the human readable role name part
     * in case of Role Group Name : appid + "_" + fieldid + "_" + role name
     */
    private static String mRoleNamePart = "^\\d+_\\d+_([\\w|_]+$)";


    /** The RExp used to match the Application Role Group Name Pattern */
    private static org.apache.regexp.RE mRExpRoleGroupName = null;

    /** The RExp used to extract the first id part */
    private static org.apache.regexp.RE mRExpFirstIDPart = null;

    /** The RExp used to extract the second id part */
    private static org.apache.regexp.RE mRExpSecondIDPart = null;

    /** The RExp used to extract the role name part in the Application role group name pattern */
    private static org.apache.regexp.RE mRExpRoleNamePart = null;


    static {

        try {
            mRExpRoleGroupName = new RE (mRole_GroupName_Pattern);
            mRExpFirstIDPart = new RE (mFirstIDPart);
            mRExpSecondIDPart = new RE (mSecondIDPart);
            mRExpRoleNamePart = new RE (mRoleNamePart);
        } catch (Exception t) {
            //System.out.println(" Reg Exp Exception " + t.getMessage() );
        }

    }



    //--------------------------------------------------------------------------
    /**
     * Return the role name part in the Application Role Group Name
     *
     * @param String Application Role Group Ident
     *
     * @return String The Role Name Part or null if not matching
     *
     * @author NK
     */
    public static String getRoleNamePart (String appRoleGroupNameIdent) {

        if (appRoleGroupNameIdent == null) {
            return null;
        }

        mRExpRoleNamePart.match (appRoleGroupNameIdent);
        return mRExpRoleNamePart.getParen (1);
    }


    //--------------------------------------------------------------------------
    /**
     * Return the first id part ( = the app id ) in the Application Role Group Name
     *
     * @param String Application Role Group Ident
     *
     * @return String The First ID part or null if not matching
     *
     * @author NK
     */
    public static String getAppIDPart (String appRoleGroupNameIdent) {

        if (appRoleGroupNameIdent == null) {
            return null;
        }

        mRExpFirstIDPart.match (appRoleGroupNameIdent);
        return mRExpFirstIDPart.getParen (1);
    }


    //--------------------------------------------------------------------------
    /**
     * Return the second id Part ( = the field id ) in the Application Role Group Name
     *
     * @param String Application Role Group Ident
     *
     * @return String The Second ID part or null if not matching
     *
     * @author NK
     */
    public static String getFieldIDPart (String appRoleGroupNameIdent) {

        if (appRoleGroupNameIdent == null) {
            return null;
        }

        mRExpSecondIDPart.match (appRoleGroupNameIdent);
        return mRExpSecondIDPart.getParen (1);
    }


    //-------------------------------------------------------------------------
    /**
     * Check if a group name is in fact an Application Role
     */
    public static boolean isRole (String grpName) {

        if (grpName == null) {
            return false;
        }

        return mRExpRoleGroupName.match (grpName);
    }


    //-------------------------------------------------------------------------
    /**
     * Return a List of Jahia Groups or Application Role Groups
     *
     * @param boolean isRoleGroup if true, return only Application Role Groups
     *                otherwise return only Jahia Groups
     */
    public static List getGroups (boolean isRoleGroup) {


        List allGroups = ServicesRegistry.getInstance ()
                .getJahiaGroupManagerService ()
                .getGroupList ();

        if (allGroups == null) {
            return null;
        }

        List reqGroups = new ArrayList();

        int size = allGroups.size ();
        String grpKey = null;
        JahiaGroup grp = null;

        for (int i = 0; i < size; i++) {
            grpKey = (String) allGroups.get (i);
            grp = ServicesRegistry.getInstance ()
                    .getJahiaGroupManagerService ()
                    .lookupGroup (grpKey);
            if (grp != null) {
                if (isRoleGroup && mRExpRoleGroupName.match (grp.getGroupname ())) {
                    reqGroups.add (grp);
                } else if (!isRoleGroup && !mRExpRoleGroupName.match (grp.getGroupname ())) {
                    reqGroups.add (grp);
                }
            }
        }
        return reqGroups;
    }


    //-------------------------------------------------------------------------
    /**
     * Return a List of Jahia Groups or Application Role Groups
     *
     * @param int     the site id
     * @param boolean isRoleGroup if true, return only Application Role Groups
     *                otherwise return only Jahia Groups
     */
    public static List getGroups (int siteID, boolean isRoleGroup)
            throws JahiaException {

        //logger.debug("started site=" + siteID);

        List reqGroups = new ArrayList();


        List fieldIDs = ServicesRegistry.getInstance ()
                .getJahiaFieldService ()
                .getAllFieldIDs (siteID);

        //logger.debug("nbfields=" + fieldIDs.size());


        if (fieldIDs == null) {
            return reqGroups;
        }

        int nbFieldIDs = fieldIDs.size ();

        List allGroups = null;
        int id = siteID;
        if (isRoleGroup) {
            id = 0;
        }
        allGroups = ServicesRegistry.getInstance ()
                .getJahiaGroupManagerService ()
                .getGroupList (id);

        //logger.debug("all grps=" + allGroups.size());

        if (allGroups == null) {
            return reqGroups;
        }


        int size = allGroups.size ();
        String grpKey = null;
        JahiaGroup grp = null;
        int fieldID = 0;
        Integer vFieldID = null;

        for (int i = 0; i < size; i++) {
            grpKey = (String) allGroups.get (i);
            grp = ServicesRegistry.getInstance ()
                    .getJahiaGroupManagerService ()
                    .lookupGroup (grpKey);
            if (grp != null) {
                if (isRoleGroup && mRExpRoleGroupName.match (grp.getGroupname ())) {

                    //logger.debug("all grp=" + grp.getGroupname());

                    // Need to check on field
                    try {
                        fieldID = Integer.parseInt (getFieldIDPart (grp.getGroupname ()));

                        //logger.debug("field part=" + fieldID);

                        for (int j = 0; j < nbFieldIDs; j++) {
                            vFieldID = (Integer) fieldIDs.get (j);
                            if (fieldID == vFieldID.intValue ()) {
                                reqGroups.add (grp);
                            }
                        }
                    } catch (Exception t) {
                        logger.error ("exception ", t);
                    }
                } else if (!isRoleGroup && !mRExpRoleGroupName.match (grp.getGroupname ())) {
                    reqGroups.add (grp);
                }
            }
        }
        return reqGroups;
    }


}