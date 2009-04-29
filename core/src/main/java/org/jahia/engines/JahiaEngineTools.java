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
package org.jahia.engines;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jcr.nodetype.NoSuchNodeTypeException;

import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.JahiaFieldDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.acl.ACLNotFoundException;
import org.jahia.services.acl.JahiaBaseACL;
import org.jahia.services.containers.ContentContainerList;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.usermanager.JahiaUser;


/**
 * @author Khue Nguyen
 */

public class JahiaEngineTools {

    /** logging */
    private static final org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger (JahiaEngineTools.class);


    //--------------------------------------------------------------------------
    // @author Khue Nguyen
    /**
     * Return the container list field Acl object for a given field
     *
     * @param aclsMap the key is the fieldID and the value is the aclID
     * @param fieldID the field identification number
     *
     * @return the acl reference, or <code>null</code> if not found
     */
    public static JahiaBaseACL getCtnListFieldACL (Map<Integer, Integer> aclsMap, int fieldID) {
        if (aclsMap == null)
            return null;
        Integer I = (Integer) aclsMap.get (new Integer (fieldID));
        if (I != null) {
            try {
                int aclID = I.intValue ();
                logger.debug ("Looking for acl [" + aclID + "]");
                JahiaBaseACL theACL = null;
                try {
                    theACL = new JahiaBaseACL (aclID);

                } catch (ACLNotFoundException ex) {
                    logger.warn ("ACL Not found", ex);

                } catch (JahiaException ex) {
                    logger.warn ("JahiaException", ex);
                }
                return theACL;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public static boolean isCtnListFieldACLDefined (Map<Integer, Integer> aclsMap, int fieldID) {
        if (aclsMap == null)
            return false;
        Integer I1 = (Integer) aclsMap.get (new Integer (fieldID));
        Integer I2 = (Integer) aclsMap.get (new Integer (0));
        return I1 != null && I2 != null && !I1.equals(I2);
    }

    /**
     * Build a map that contains a pair of [fieldID (INTEGER),aclID (INTEGER)] for fields of a
     * given container list. The acl is the one set at container list level , not at field level
     * !!!
     *
     * @param theContainer the container instance
     * @param jParams      the Jahia parameters
     *
     * @return Map the map containing the list of editable fields
     */
    static public Map<Integer, Integer> getCtnListFieldAclMap (JahiaContainer theContainer,
                                                 ProcessingContext jParams) {
        if (logger.isDebugEnabled()) {
            logger.debug ("Started for container [" + (theContainer != null ? theContainer.getID() : "null") + "]");
        }

        Map<Integer, Integer> map = new HashMap<Integer, Integer>();

        if (theContainer == null)
            return map;

        if (theContainer.getListID () == 0) {
            return map;
        }
        try {
            ContentContainerList cList = ContentContainerList.getContainerList( theContainer.getListID ());

            if (cList == null)
                return map;

            String prop = null;
            JahiaField aField = null;
            JahiaFieldDefinition theDef = null;
            map.put(new Integer(0), new Integer(cList.getAclID()));
            Iterator<JahiaField> fList = theContainer.getFields ();
            while (fList.hasNext ()) {
                aField = (JahiaField) fList.next ();
                if (aField != null) {
                    logger.debug ("Field [" + aField.getID () + "]");

                    /*
                         aField = ServicesRegistry.getInstance().
                         getJahiaFieldService().loadField( aField.getID(), LoadFlags.ALL, jParams );
                     */
                    logger.debug ("Field [" + aField.getID () + "]");

                    theDef = aField.getDefinition ();
                    if (theDef != null) {
                        prop = cList.getProperty ("view_field_acl_" +
                                theDef.getName ());
                        if (prop != null) {
                            logger.debug ("ACL found = " + prop);
                            try {
                                int aclID = Integer.parseInt (prop);
                                map.put (new Integer (aField.getID ()),
                                        new Integer (aclID));
                                //logger.debug ("ACL added");
                            } catch (Exception t) {
                                logger.debug (" requested acl ( view_field_acl_" +
                                        theDef.getName () +
                                        "), exception :" + t.getMessage ());
                            }
                        } else {
                            // create the acl, because field declaration can change in template
//                            JahiaBaseACL newAcl = null;
//                            newAcl = new JahiaBaseACL ();
//                            newAcl.create (cList.getAclID ());
//                            cList.setProperty ("view_field_acl_" +
//                                    theDef.getName (),
//                                    String.valueOf (newAcl.getID ()));

                            map.put (new Integer (aField.getID ()),
                                    new Integer (cList.getAclID ()));
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return map;
    }

    /**
     * <p>Build a map which keys are field def ids of the container list fields, if they are
     * visible ( READ or WRITE or ADMIN RIGHT of the acl return true ).</p>
     * <p/>
     * <p>Note: <i>if the container's list id is 0 (that is, the list is not created yet, then
     * returned map contains all fields).</i></p>
     *
     * @param theContainer the container reference
     * @param user         the user to check access
     * @param aclsMap      the key is the fieldID and the value is the aclID.This map should be
     *                     the one returned by getCtnListFieldAclMap.
     *
     * @return Set the list of visible fields
     */
    static public Set<Integer> getCtnListVisibleFields (JahiaContainer theContainer,
                                               JahiaUser user, Map<Integer, Integer> aclsMap) {
        if (logger.isDebugEnabled()) {
            logger.debug ("Started for container [" + (theContainer != null ? theContainer.getID() : "") + "]");
        }

        Set<Integer> visibleFields = new HashSet<Integer>();
        try {
            ExtendedNodeType nt =  NodeTypeRegistry.getInstance().getNodeType(theContainer.getDefinition().getContainerType());

            if (theContainer == null)
                return visibleFields;

            boolean cListExist = (theContainer.getListID () != 0);

            Iterator<JahiaField> fields = theContainer.getFields ();
            JahiaBaseACL acl = null;
            JahiaField aField = null;
            while (fields.hasNext ()) {
                aField = (JahiaField) fields.next ();
                if (aField != null) {
                    ExtendedPropertyDefinition epd = nt.getPropertyDefinitionsAsMap().get(aField.getDefinition().getName().substring(theContainer.getDefinition().getName().length()+1));

//                    if (epd == null || !epd.getDeclaringNodeType().isMixin() || !epd.getDeclaringNodeType().isNodeType("jmix:layout")) {
                    if (epd == null || !epd.isHidden()) {
                        if (!cListExist) {
                            visibleFields.add (new Integer (aField.getID ()));
                        } else {
                            acl = JahiaEngineTools.getCtnListFieldACL (aclsMap,
                                    aField.getID ());
                            if (acl != null) {
                                try {
                                    if (acl.getPermission (user,
                                            JahiaBaseACL.READ_RIGHTS)
                                            ||
                                            acl.getPermission (user, JahiaBaseACL.WRITE_RIGHTS)
                                            ||
                                            acl.getPermission (user, JahiaBaseACL.ADMIN_RIGHTS)) {
                                        visibleFields.add (new Integer (aField.getID ()));
                                    }
                                } catch (Exception t) {
                                    logger.error ("Exception : " + t.getMessage (), t);
                                }
                            }
                        }
                    }
                }
            }
        } catch (NoSuchNodeTypeException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (JahiaException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return visibleFields;
    }

}

