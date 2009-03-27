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

package org.jahia.engines.rights;

import org.apache.log4j.Logger;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.engines.validation.ValidationError;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.registries.ServicesRegistry;
import org.jahia.registries.EnginesRegistry;
import org.jahia.services.acl.*;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockPrerequisites;
import org.jahia.services.lock.LockPrerequisitesResult;
import org.jahia.services.usermanager.*;

import java.security.Principal;
import java.util.*;

/**
 * <p>Title: Jahia right management</p> <p>Description: CMS Enterprise Portal</p> <p>Copyright:
 * Copyright (c) 2002-2003</p> <p>Company: Jahia SA</p>
 *
 * @author MAP, Xavier Lawrence
 * @version 1.1
 */
public class ManageRights {
// ------------------------------ FIELDS ------------------------------

    public static final String GWT_JSP_FILE = "/engines/rights/gwtAcl.jsp";
    public static final String JSP_FILE = "/engines/rights/manage_rights.jsp";
    public static final String READONLY_JSP = "/engines/rights/readonly_rights.jsp";

    // Variable used to store the ACL entries submitted by the user, when a
    // refresh is performed due to warnings.
    public static final String ACL_ENTRIES = "aclEntries";
    public static final String REMOVED_ENTRIES = "removedEntries";
    public static final String INHERITANCE = "inheritance";
    public static final String NEW_ACL = "newacl";

    // Contains groups which have read access to the current ACL
    public static final String READ_GROUPS = "readGroups";

    private static final transient Logger logger = Logger.getLogger(ManageRights.class);

    private static ManageRights instance = null;

    public static final int V_ADDED_ACL = 0;

    public static final int V_RESET_CHILD_PERMISSION = 1;

    public static final int V_REM_ACL_ENTRY = 2;

    public static final int V_RIGHT_CHANGE = 3;

    public static final int V_DEFAULT_PERMISSION = 8;

    private int vNbOfPerm = -1;

    private int vDefualtInherit = -1;

    private int vName = -1;
    
    
// -------------------------- STATIC METHODS --------------------------

    /**
     * @return a single instance of the object
     */
    public static ManageRights getInstance() {
        if (instance == null) {
            synchronized (ManageRights.class) {
                if (instance == null) {
                    instance = new ManageRights();
                }
            }
        }
        return instance;
    }

// --------------------------- CONSTRUCTORS ---------------------------

    // Don't let anyone instantiate this class
    private ManageRights() {
        super();
    }

// -------------------------- OTHER METHODS --------------------------

    public int getNumberOfPermissions() {
        if (vNbOfPerm == -1) {
            try {
                vNbOfPerm = new JahiaBaseACL().size();
            } catch (JahiaException ex) {
                logger.error("Error getting number of permissions", ex);
            }
        }
        return vNbOfPerm;
    }

    public int getVDefaultInherit() {
        if (vDefualtInherit == -1)
            vDefualtInherit = V_DEFAULT_PERMISSION + getNumberOfPermissions();
        return vDefualtInherit;
    }

    public int getVName() {
        if (vName == -1)
            vName = getVDefaultInherit() + 1;
        return vName;
    }

    public int getVKey(Integer userNameWidth) {
        return getVName() + userNameWidth.intValue() + 1;
    }
    
    /**
     * @param jParams   a ProcessingContext object
     * @param mode      the mode, according to JahiaEngine
     * @param engineMap the engine parameters stored in a HashMap
     * @param aclID     the ACL DB identifier from "jahia_acl" table
     *
     * @throws JahiaException
     * @throws JahiaSessionExpirationException
     *
     * @see org.jahia.engines.JahiaEngine
     */
    public EngineValidationHelper handleActions(final ProcessingContext jParams, final int mode,
            final Map<String, Object> engineMap, final int aclID, ParentACLFinder parentACLFinder, ACLResourceInterface aclResource)
        throws JahiaException, JahiaSessionExpirationException {
        return handleActions(jParams,mode, engineMap, aclID, parentACLFinder, aclResource, false);
    }

    /**
     * @param jParams   a ProcessingContext object
     * @param mode      the mode, according to JahiaEngine
     * @param engineMap the engine parameters stored in a HashMap
     * @param aclID     the ACL DB identifier from "jahia_acl" table
     *
     * @throws JahiaException
     * @throws JahiaSessionExpirationException
     *
     * @see org.jahia.engines.JahiaEngine
     */
    public EngineValidationHelper handleActions(final ProcessingContext jParams, final int mode,
                                                final Map<String, Object> engineMap, final int aclID, ParentACLFinder parentACLFinder, ACLResourceInterface aclResource, boolean newAcl)
            throws JahiaException, JahiaSessionExpirationException {
        return handleActions(jParams, mode, engineMap, aclID, parentACLFinder, aclResource, newAcl, String.valueOf(aclID));
    }

    public EngineValidationHelper handleActions(final ProcessingContext jParams, final int mode,
                                                final Map<String, Object> engineMap, final int aclID, ParentACLFinder parentACLFinder, ACLResourceInterface aclResource, boolean newAcl, String sessionIdentifier)
            throws JahiaException, JahiaSessionExpirationException {
        JahiaBaseACL theACL;

        final EngineValidationHelper evh = new EngineValidationHelper();
        evh.setNextScreen("rightsMgmt");
        evh.setPreviousScreen("rightsMgmt");
        try {
            theACL = new JahiaBaseACL(aclID);
        } catch (JahiaException ex) {
            final ValidationError ve = new ValidationError(this, ex.getMessage());
            evh.addError(ve);
            return evh;
        }
        return handleActions(jParams, mode, engineMap, theACL, parentACLFinder, aclResource, newAcl, sessionIdentifier);
    }

    /**
     * @param jParams   a ProcessingContext object
     * @param mode      the mode, according to JahiaEngine
     * @param engineMap the engine parameters stored in a HashMap
     * @param theACL     the ACL
     *
     * @param newAcl
     * @param sessionIdentifier
     * @throws JahiaException
     * @throws JahiaSessionExpirationException
     *
     * @see org.jahia.engines.JahiaEngine
     */
    public EngineValidationHelper handleActions(final ProcessingContext jParams, final int mode,
                                                final Map<String, Object> engineMap, final JahiaBaseACL theACL,
                                                ParentACLFinder parentACLFinder, ACLResourceInterface aclResource, boolean newAcl, String sessionIdentifier)
            throws JahiaException, JahiaSessionExpirationException {
        int aclID = theACL.getID();

        if (logger.isDebugEnabled()) logger.debug("handling mode : " + mode + " for acl " + aclID);

        final EngineValidationHelper evh = new EngineValidationHelper();
        evh.setNextScreen("rightsMgmt");
        evh.setPreviousScreen("rightsMgmt");

        final SessionState session = jParams.getSessionState ();
        session.setAttribute ("jahia_session_acl", theACL);
        switch (mode) {
            case (JahiaEngine.LOAD_MODE):
                if (loadACLEntries(jParams, engineMap, theACL, parentACLFinder, aclResource, newAcl, sessionIdentifier)) {
                    return null;
                }
                final ValidationError ve = new ValidationError(this,
                        "Cannot load ACL: "+theACL.getID());
                evh.addError(ve);
                break;

            case (JahiaEngine.UPDATE_MODE):
//                if (update(jParams, engineMap, theACL, parentACLFinder, aclResource, newAcl, sessionIdentifier)) {
//                    return null;
//                }
//                final ValidationError ve1 = new ValidationError(this,
//                        "Cannot update ACL: "+theACL.getID());
//                evh.addError(ve1);
                break;

            case (JahiaEngine.VALIDATE_MODE):
//                if(!engineMap.containsKey(READ_GROUPS+"_"+sessionIdentifier))
//                    loadACLEntries(jParams, engineMap, theACL, parentACLFinder, aclResource, newAcl, sessionIdentifier);
//                final Object o = engineMap.get("rightsUpdated");
//                if ((o != null && (o).equals(Boolean.TRUE)) || validate(jParams, engineMap, theACL, sessionIdentifier)){
//                    return null;
//                }
//                final ValidationError ve3 = new ValidationError(this,
//                        "Cannot validate ACL: "+theACL.getID());
//                evh.addError(ve3);
                break;

            case (JahiaEngine.SAVE_MODE):
//                if (saveACLEntries(theACL, jParams, engineMap, newAcl, sessionIdentifier)){
//                    return null;
//                }
//                final ValidationError ve2 = new ValidationError(this,
//                        "Cannot save ACL: "+theACL.getID());
//                evh.addError(ve2);
                break;

            case (JahiaEngine.CANCEL_MODE):
                return null;

            default:
                throw new JahiaException("Unkown mode: "+mode,
                        "Unkown mode: "+mode,
                        JahiaException.ENGINE_ERROR,
                        JahiaException.ERROR_SEVERITY);
        }
        return evh;
    }

    /**
     * Load all the ACL entries and inherited ACL from parent object and store the resulting
     * principals to an HashSet.
     *
     * @param jParams   a Parambean object
     * @param engineMap the engine parameters stored in a HashMap
     * @param acl       the ACL DB identifier from "jahia_acl" table
     *
     * @param newAcl
     * @param sessionIdentifier
     * @return true if the entries are loaded successfully
     *
     * @throws JahiaException when acl "getID" failed or when renderLink to select user popup
     *                        failed.
     */
    private boolean loadACLEntries(final ProcessingContext jParams, final Map<String, Object> engineMap,
                                   final JahiaBaseACL acl, ParentACLFinder parentACLFinder, ACLResourceInterface aclResource, boolean newAcl, String sessionIdentifier) throws JahiaException {
        if (logger.isDebugEnabled()) logger.debug("Load ACL entries in ManageRights - AclID: " + acl.getID());

        Boolean showAdminGroups = (Boolean) engineMap.get("showAdminGroups");
        if(showAdminGroups==null)
                showAdminGroups = Boolean.FALSE;
        // Do we have to restrict the entries to the specified site only ?
        int restrictedSiteID = -1;
        Integer integer = (Integer) engineMap.get("restrictRightsToSite");
        if(null!=integer)
            restrictedSiteID = integer.intValue();
        Map<Principal, Integer[]> aclEntries = (Map<Principal, Integer[]>) engineMap.get(ACL_ENTRIES+"_"+sessionIdentifier);
        if (aclEntries == null) {
            Set<String> readGroups = new HashSet<String>();
            final JahiaAclEntry aclEntry = new JahiaAclEntry();
            aclEntry.setPermission(JahiaBaseACL.READ_RIGHTS, JahiaAclEntry.ACL_YES);
            readGroups.addAll(acl.getGroupnameListNoAdmin(parentACLFinder, aclResource, aclEntry));
            if (logger.isDebugEnabled()) logger.debug("Loaded readGroups: " + readGroups);
            engineMap.put(READ_GROUPS+"_"+sessionIdentifier, readGroups);

            int currentInheritance = acl.getInheritance();
            engineMap.put(INHERITANCE+"_"+sessionIdentifier, new Integer(newAcl?0:currentInheritance));

            aclEntries = new HashMap<Principal, Integer[]>();
            engineMap.put(ACL_ENTRIES+"_"+sessionIdentifier, aclEntries);

            engineMap.put(REMOVED_ENTRIES+"_"+sessionIdentifier, new HashSet());

            engineMap.put("hasNegativePermissions", Boolean.FALSE);
            engineMap.put("actionNames", JahiaAbstractACL.getActionNames());
            engineMap.put("symbols", JahiaBaseACL.getSymbols());
            Integer userNameWidth = new Integer(15);
            engineMap.put("userNameWidth", userNameWidth);
            putInt(engineMap, "nbOfPerm", getNumberOfPermissions());
            putInt(engineMap, "vDefInherit", getVDefaultInherit());
            putInt(engineMap, "vDefPerm", V_DEFAULT_PERMISSION);
            putInt(engineMap, "vAddAcl", V_ADDED_ACL);
            putInt(engineMap, "vRemoveAcl", V_REM_ACL_ENTRY);
            putInt(engineMap, "vChangeAcl", V_RIGHT_CHANGE);
            putInt(engineMap, "vKey", getVKey(userNameWidth));            

            ServicesRegistry sReg = ServicesRegistry.getInstance();
            JahiaUserManagerService uMgr = sReg.getJahiaUserManagerService();
            JahiaGroupManagerService gMgr = sReg.getJahiaGroupManagerService();            
            
            // Lookup for user ACL entries and inherited ACL
            final List<String> userList = acl.getUsernameList(parentACLFinder, aclResource, null);
            final Iterator<String> userListIter = userList.iterator();
            while (userListIter.hasNext()) {
                final String userKey = (String) userListIter.next();
                final JahiaUser user = uMgr.lookupUserByKey(userKey);
                if (user != null) {
                    final Integer[] value = {new Integer(getPermissions(user,acl, parentACLFinder,aclResource, newAcl))};
                    if (logger.isDebugEnabled()) logger.debug("Loaded value: "+value[0]+ " for user "+
                            user);
                    aclEntries.put(user, value);
                }
            }
            // Look up group
            final List<String> groupList;
            if(showAdminGroups.booleanValue())
                groupList = acl.getGroupnameList(parentACLFinder, aclResource, null);
            else
                groupList = acl.getGroupnameListNoAdmin(parentACLFinder, aclResource, null);
            final Iterator<String> groupListIter = groupList.iterator();
            while (groupListIter.hasNext()) {
                final String groupKey = (String) groupListIter.next();
                final JahiaGroup group = gMgr.lookupGroup(groupKey);
                if (group != null) {
                    final Integer[] value = {new Integer(getPermissions(group, acl, parentACLFinder, aclResource, newAcl))};
                    if (logger.isDebugEnabled()) logger.debug("Loaded value: " + value[0] + " for group " +
                            group);
                    if (restrictedSiteID == -1 || !JahiaGroupManagerDBProvider.PROVIDER_NAME.equals(group.getProviderName()) || group.getSiteID() == restrictedSiteID)
                        aclEntries.put(group, value);
                }
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("aclEntries: "+ aclEntries);
            logger.debug("inheritance: "+ engineMap.get(INHERITANCE+"_"+sessionIdentifier));
        }

        Boolean selectSite = (Boolean) engineMap.get("selectSiteInSelectUsrGrp");
        if (selectSite == null) {
            selectSite = Boolean.FALSE;
        }
        if (selectSite.booleanValue()) {
            engineMap.put("selectUsrGrp", EnginesRegistry.getInstance().getEngineByBeanName("selectUGEngine").renderLink(jParams, "&selectSite=true"));
        } else {
            engineMap.put("selectUsrGrp", EnginesRegistry.getInstance().getEngineByBeanName("selectUGEngine").renderLink(jParams, ""));
        }

        engineMap.put(ACL_ENTRIES, engineMap.get(ACL_ENTRIES+"_"+sessionIdentifier));
        engineMap.put(REMOVED_ENTRIES, engineMap.get(REMOVED_ENTRIES+"_"+sessionIdentifier));
        engineMap.put(INHERITANCE, engineMap.get(INHERITANCE+"_"+sessionIdentifier));

        engineMap.put("fieldsEditCallingEngineName","managerights_engine");

        final LockPrerequisitesResult results = LockPrerequisites.getInstance().getLockPrerequisitesResult((LockKey) engineMap.get("LockKey"));
        boolean isLocked = false;
        if (results != null) {
            isLocked = results.getReadOnlyTabs().contains(LockPrerequisites.RIGHTS) ||
                            results.getReadOnlyTabs().contains(LockPrerequisites.ALL_LEFT);
        }
        final boolean readOnly = (results != null && isLocked);

        if (readOnly) {
            engineMap.put("readOnly", Boolean.TRUE);
        }

        engineMap.put("newAcl",newAcl);
        engineMap.put("acl",acl);    
        engineMap.put("sessionIdentifier",sessionIdentifier);
        engineMap.put("fieldsEditCallingEngineName","managerights_engine");
        engineMap.put("managerights_engine.fieldForm",
                      ServicesRegistry.getInstance().getJahiaFetcherService().fetchServlet((ParamBean)jParams, GWT_JSP_FILE));


        return true;
    }

    /**
     *
     */
    private int getPermissions(final Principal p, final JahiaBaseACL acl, final ParentACLFinder aclFinder, final ACLResourceInterface aclResourceInterface, boolean newAcl) {
        try {
            JahiaAclEntry aclEntry = (p instanceof JahiaUser) ?
                acl.getLocalUserEntry((JahiaUser) p) :
                acl.getLocalGroupEntry((JahiaGroup) p);
            if (aclEntry != null) {
                // Look up permissions in the local ACL entry; forget the partial inheritance.
                int permissions = aclEntry.getEntryState() ;
                if (logger.isDebugEnabled()) logger.debug("Returning EntryState: " + permissions);
                if (newAcl) {
                    permissions |= JahiaBaseACL.RIGHTS_INHERITANCE_FLAG;
                }
                return permissions;
            } else { // Look up permissions; pur inheritance
                int permissions = JahiaBaseACL.RIGHTS_INHERITANCE_FLAG;
                if (p instanceof JahiaUser) {
                    JahiaUser user = (JahiaUser) p;
                    for (int i = 0; i < JahiaBaseACL.RIGHTS_MAX_OFFSET; i++)
                        permissions |= acl.getPermission(aclFinder, aclResourceInterface, user, i) ? 1 << i : 0;
                } else {
                    JahiaGroup group = (JahiaGroup) p;
                    for (int i = 0; i < JahiaBaseACL.RIGHTS_MAX_OFFSET; i++)
                        permissions |= acl.getPermission(aclFinder, aclResourceInterface, group, i) ? 1 << i : 0;
                }
                if (logger.isDebugEnabled()) logger.debug("Returning inherited permissions: " + permissions);
                aclEntry = (p instanceof JahiaUser) ? acl.getUserEntry((JahiaUser) p) : acl.getGroupEntry((JahiaGroup) p);
                if (aclEntry != null) {
                    permissions += 16;
                }
                return permissions;
            }
        } catch (JahiaACLException jae) {
            logger.error("Cannot get user or group ACL entry !!", jae);
            return 0;
        }
    }

    private void putInt(Map<String, Object> map, String key, int value) {
        map.put(key, new Integer(value));
    }    
    
}
