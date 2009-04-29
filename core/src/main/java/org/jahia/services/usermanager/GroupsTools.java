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