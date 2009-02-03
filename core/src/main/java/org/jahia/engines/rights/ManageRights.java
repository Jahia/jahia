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

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.log4j.Logger;
import org.jahia.content.*;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.events.JahiaEvent;
import org.jahia.engines.EngineMessage;
import org.jahia.engines.EngineMessages;
import org.jahia.engines.JahiaEngine;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.engines.validation.ValidationError;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaSessionExpirationException;
import org.jahia.hibernate.model.JahiaAcl;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.hibernate.model.JahiaFieldXRef;
import org.jahia.hibernate.manager.JahiaFieldXRefManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.params.ParamBean;
import org.jahia.params.ProcessingContext;
import org.jahia.params.SessionState;
import org.jahia.registries.ServicesRegistry;
import org.jahia.registries.EnginesRegistry;
import org.jahia.services.acl.*;
import org.jahia.services.cache.ContainerHTMLCache;
import org.jahia.services.cache.SkeletonCache;
import org.jahia.services.fields.ContentField;
import org.jahia.services.lock.LockKey;
import org.jahia.services.lock.LockPrerequisites;
import org.jahia.services.lock.LockPrerequisitesResult;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.pages.JahiaPage;
import org.jahia.services.pages.JahiaPageContentRights;
import org.jahia.services.pages.JahiaPageService;
import org.jahia.services.usermanager.*;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.containers.ContentContainer;
import org.jahia.utils.JahiaTools;

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

    public static final String GWT_JSP_FILE = "/jsp/jahia/engines/rights/gwtAcl.jsp";
    public static final String JSP_FILE = "/jsp/jahia/engines/rights/manage_rights.jsp";
    public static final String READONLY_JSP = "/jsp/jahia/engines/rights/readonly_rights.jsp";

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
            final Map engineMap, final int aclID, ParentACLFinder parentACLFinder, ACLResourceInterface aclResource)
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
                                                final Map engineMap, final int aclID, ParentACLFinder parentACLFinder, ACLResourceInterface aclResource, boolean newAcl)
            throws JahiaException, JahiaSessionExpirationException {
        return handleActions(jParams, mode, engineMap, aclID, parentACLFinder, aclResource, newAcl, String.valueOf(aclID));
    }

    public EngineValidationHelper handleActions(final ProcessingContext jParams, final int mode,
                                                final Map engineMap, final int aclID, ParentACLFinder parentACLFinder, ACLResourceInterface aclResource, boolean newAcl, String sessionIdentifier)
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
                                                final Map engineMap, final JahiaBaseACL theACL,
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
    private boolean loadACLEntries(final ProcessingContext jParams, final Map engineMap,
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
        Map aclEntries = (Map) engineMap.get(ACL_ENTRIES+"_"+sessionIdentifier);
        if (aclEntries == null) {
            Set readGroups = new HashSet();
            final JahiaAclEntry aclEntry = new JahiaAclEntry();
            aclEntry.setPermission(JahiaBaseACL.READ_RIGHTS, JahiaAclEntry.ACL_YES);
            readGroups.addAll(acl.getGroupnameListNoAdmin(parentACLFinder, aclResource, aclEntry));
            if (logger.isDebugEnabled()) logger.debug("Loaded readGroups: " + readGroups);
            engineMap.put(READ_GROUPS+"_"+sessionIdentifier, readGroups);

            int currentInheritance = acl.getInheritance();
            engineMap.put(INHERITANCE+"_"+sessionIdentifier, new Integer(newAcl?0:currentInheritance));

            aclEntries = new HashMap();
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
            final List userList = acl.getUsernameList(parentACLFinder, aclResource, null);
            final Iterator userListIter = userList.iterator();
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
            final List groupList;
            if(showAdminGroups.booleanValue())
                groupList = acl.getGroupnameList(parentACLFinder, aclResource, null);
            else
                groupList = acl.getGroupnameListNoAdmin(parentACLFinder, aclResource, null);
            final Iterator groupListIter = groupList.iterator();
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

    /**
     * @param jParams   a Parambean object
     * @param engineMap the engine parameters stored in a HashMap
     * @param theACL    the ACL DB identifier from "jahia_acl" table
     *
     * @param newAcl
     * @param sessionIdentifier
     * @return true if no warnings
     */
    private boolean update(final ProcessingContext jParams, final Map engineMap,
                           final JahiaBaseACL theACL, ParentACLFinder parentACLFinder, ACLResourceInterface aclResource, boolean newAcl, String sessionIdentifier) throws JahiaException {
        final String theScreen = (String) engineMap.get("screen");
        if ( theScreen.equals("cancel") ) {
            return true;
        }
        if (logger.isDebugEnabled()) logger.debug("Update (aclID = "+theACL.getID()+")");

        if (! processUserValues(jParams, theACL, parentACLFinder, aclResource, engineMap, newAcl, sessionIdentifier)) {
            return false;
        }

        if ( validate(jParams, engineMap, theACL, sessionIdentifier) ) {
            engineMap.put("rightsUpdated", Boolean.TRUE);
            if (logger.isDebugEnabled()) logger.debug("rightsUpdated: "+Boolean.TRUE);
            return true;
        }
        engineMap.put("rightsUpdated", Boolean.FALSE);
        return false;
    }

    /**
     *
     */
    private boolean processUserValues(final ProcessingContext jParams,
                                      final JahiaBaseACL acl, ParentACLFinder parentACLFinder, ACLResourceInterface aclResource, Map engineMap, boolean newAcl, String sessionIdentifier) throws JahiaException {
        // Check if the inheritance has to be permanently prevent or not.
        if (logger.isDebugEnabled())  logger.debug("processUserValues: "+acl.getID());

        final String cutRestAllInherit = jParams.getParameter("cutRestAllInherit");
        if (logger.isDebugEnabled()) logger.debug("cutRestAllInherit: "+ cutRestAllInherit);
        if (cutRestAllInherit != null) { // Is the check box checked ?
            if (((Integer)engineMap.get(INHERITANCE+"_"+sessionIdentifier)).intValue() == ACLInfo.INHERITANCE) { // Has it changed ?
                if (logger.isDebugEnabled()) logger.debug("currentInheritance = ACLInfo.NO_INHERITANCE");
                engineMap.put(INHERITANCE+"_"+sessionIdentifier, new Integer(ACLInfo.NO_INHERITANCE));
            }
        } else {
            if (((Integer)engineMap.get(INHERITANCE+"_"+sessionIdentifier)).intValue() == ACLInfo.NO_INHERITANCE) { // Has the checkbox changed ?
                if (logger.isDebugEnabled()) logger.debug("currentInheritance = ACLInfo.INHERITANCE");
                engineMap.put(INHERITANCE+"_"+sessionIdentifier, new Integer(ACLInfo.INHERITANCE));
            }
        }

        // format: [00000000r--*users gusers:1]
        // [0..7] - flags
        // [8..8+nbOfPerm] - permissions
        // [8+nbOpPerm] - inheritance
        // [inheritance+1..inheritance+1+namelength] - name
        // [name+nameLength+2..*] - key
        // [key-1] - type        
        final String[] submitedACLEntries = jParams.
                getParameterValues(ACL_ENTRIES);
        final Integer userNameWidth = (Integer) jParams.getSessionState().
                getAttribute("userNameWidth");

        Set readGroups = (Set) engineMap.get(READ_GROUPS+"_"+sessionIdentifier);
        Map aclEntries = (Map) engineMap.get(ACL_ENTRIES+"_"+sessionIdentifier);
        Set removedEntries = (Set) engineMap.get(REMOVED_ENTRIES+"_"+sessionIdentifier);

        if (submitedACLEntries == null || submitedACLEntries[0].substring(0, 4).equals("null")) {
            readGroups.clear();
            return true;
        }

        for (int i = 0; i < submitedACLEntries.length; i++) {
            String aclEntry = submitedACLEntries[i];            
            if ("00000000".equals(aclEntry.substring(0, 8))) {
                if (logger.isDebugEnabled()) logger.debug("No change to AclEntry: "+aclEntry);

                continue; // Don't do anything if no changes to the ACL entry.
            }
            // What kind of object has to be saved : user or group ?
            final Principal usr_grp;
            final String usr_grpName;
            final boolean isUser; // Only used for clearer log messages

            // Very nice way to kill weird non breaking space when non-utf8
            aclEntry = JahiaTools.replacePattern(aclEntry, "\u00C2\u00A0", " ");

            if (logger.isDebugEnabled()) logger.debug("Submited ACL entry : " + aclEntry);
            int typeOffs = getVKey(userNameWidth) - 1;
            switch (aclEntry.charAt(typeOffs)) {            
            case 'u':
                usr_grp = ServicesRegistry.getInstance().getJahiaUserManagerService().
                        lookupUserByKey(aclEntry.substring(getVKey(userNameWidth)));
                if (usr_grp == null) continue;
                usr_grpName = ((JahiaUser) usr_grp).getUserKey();
                isUser = true;
                break;
            case 'g':
                usr_grp = ServicesRegistry.getInstance().getJahiaGroupManagerService().
                        lookupGroup(aclEntry.substring(getVKey(userNameWidth)));
                if (usr_grp == null) continue;
                usr_grpName = ((JahiaGroup) usr_grp).getGroupKey();
                isUser = false;
                break;
            default:
                logger.error("Unknown principal type");
                return false;
            }
            // Decode the select box value parameters
            final String permissions = aclEntry.substring(V_DEFAULT_PERMISSION, getVDefaultInherit()).toUpperCase();

            if (aclEntry.charAt(V_ADDED_ACL) == '1') {
                if (isUser) {
                    if (logger.isDebugEnabled()) logger.debug("Added ACL entry for user : " + usr_grpName);
                } else {
                    if (logger.isDebugEnabled()) logger.debug("Added ACL entry for group : " + usr_grpName);
                }
                setACLEntry(usr_grp, permissions, aclEntries, readGroups);
            } else if (aclEntry.charAt(getVDefaultInherit()) == '%') {
                if (isUser) {
                    if (logger.isDebugEnabled()) logger.debug("Cut inheritance for user : " + usr_grpName);
                } else {
                    if (logger.isDebugEnabled()) logger.debug("Cut inheritance for group : " + usr_grpName);
                }
                setACLEntry(usr_grp, permissions, aclEntries, readGroups);
            } else if (aclEntry.charAt(V_REM_ACL_ENTRY) == '1') {
                if (isUser) {
                    if (logger.isDebugEnabled()) logger.debug("Remove ACL entry for user : " + usr_grpName);
                    JahiaBaseACL parentACL = null;
                    if (parentACLFinder != null) {
                        ACLResourceInterface parentAclResource = parentACLFinder.getParent(aclResource);
                        if (parentAclResource != null ) {
                            parentACL = new JahiaBaseACL(parentAclResource.getAclID());
                        }
                    } else {
                        parentACL = acl.getParent();
                    }
                    if (parentACL != null && parentACL.getPermission((JahiaUser) usr_grp,
                            JahiaBaseACL.READ_RIGHTS)) {
                        readGroups.add(usr_grpName);
                    }
                } else {
                    if (logger.isDebugEnabled()) logger.debug("Remove ACL entry for group : " + usr_grpName);
                    JahiaBaseACL parentACL = null;
                    if (parentACLFinder != null) {
                        ACLResourceInterface parentAclResource = parentACLFinder.getParent(aclResource);
                        if (parentAclResource != null ) {
                            parentACL = new JahiaBaseACL(parentAclResource.getAclID());
                        }
                    } else {
                        parentACL = acl.getParent();
                    }
                    if ((parentACL != null) &&
                            parentACL.getPermission((JahiaGroup) usr_grp,
                            JahiaBaseACL.READ_RIGHTS)) {
                        readGroups.add(usr_grpName);
                    }
                }

                aclEntries.remove(usr_grp);
                removedEntries.add(usr_grp);
            } else if (aclEntry.charAt(V_RIGHT_CHANGE) == '1') {
                if (isUser) {
                    if (logger.isDebugEnabled()) logger.debug("Permissions have changed to " +
                            permissions + " for user : " + usr_grpName);
                } else {
                    if (logger.isDebugEnabled()) logger.debug("Permissions have changed to " +
                            permissions + " for group : " + usr_grpName);
                }
                setACLEntry(usr_grp, permissions, aclEntries, readGroups);
            }

//            if (aclEntry.charAt(V_RESET_CHILD_PERMISSION) == '1') {
//                logger.debug("Reset child permissions for user : " + user.getUsername());
//                todo Reset child permissions
//            }
        }
        return true;
    }

    private void putInt(Map map, Object key, int value) {
        map.put(key, new Integer(value));
    }    
    
    /**
     * Save or update the ACL modifications to the DB.
     *
     */
    private void setACLEntry(final Principal p, final String perm, Map aclEntries, Set readGroups) {
        final JahiaAclEntry permissions = new JahiaAclEntry();
        char[] actions = JahiaBaseACL.getUppercaseSymbols();
        int size = perm.length() < JahiaBaseACL.RIGHTS_MAX_OFFSET ? perm
                .length() : JahiaBaseACL.RIGHTS_MAX_OFFSET;
        for (int i = 0; i < size; i++)
            permissions.setPermission(i,
                perm.charAt(i) == actions[i] ? JahiaACLEntry.ACL_YES
                        : JahiaACLEntry.ACL_NO);
        
        final String name;
        final Integer[] value = {new Integer(permissions.getEntryState()),
                new Integer(permissions.getEntryTri())};
        if (p instanceof JahiaUser) {
            final JahiaUser user = (JahiaUser) p;
            name = user.getUserKey();
            aclEntries.put(user, value);
        } else {
            final JahiaGroup group = (JahiaGroup) p;
            name = group.getGroupKey();
            aclEntries.put(group, value);
        }

        if (permissions.getPermission (
                JahiaBaseACL.READ_RIGHTS) == JahiaAclEntry.ACL_YES) {
            if (! readGroups.contains(name)) {
                readGroups.add(name);
                if (logger.isDebugEnabled()) logger.debug("Read Group Added: " + name);
            }
        } else {
            readGroups.remove(name);
        }
        if (logger.isDebugEnabled()) logger.debug("setACLEntry: " + permissions);
    }


    /**
     *
     */
    private boolean validate(final ProcessingContext jParams,
                             final Map engineMap,
                             final JahiaBaseACL theACL, String sessionIdentifier) throws JahiaException {
        if (logger.isDebugEnabled()) logger.debug("Validating: " + theACL.getID());
        if (!jParams.getSite().isURLIntegrityCheckEnabled()) return true;
        if (jParams.getParameter("ignoreWarnings") == null &&
                theACL.getID() == jParams.getPage().getAclID()) {
            // this means that we are updating the ACL of the page, not one
            // of its content objects...

            final Map possibleFuture403 = contentObjectsPointingOnPage(jParams);
            final StringBuffer buff = new StringBuffer();
            Set readGroups = (Set) engineMap.get(READ_GROUPS+"_"+sessionIdentifier);
            if (possibleFuture403.size() > 0 ) {
                try {
                    final EngineMessages result = new EngineMessages();

                    // Keys are Integers representing the pageID
                    final Iterator keysIte = possibleFuture403.keySet().iterator();

                    while (keysIte.hasNext()) {
                        final Integer pageIDObj = (Integer)keysIte.next();
                        final Set set = (Set) possibleFuture403.get(pageIDObj);
                        // Set values are ObjectKeys
                        final Iterator ite = set.iterator();

                        while (ite.hasNext()) {
                            // key = key of the BigText field (ContentFieldKey)
                            final ObjectKey key = (ObjectKey)ite.next();
                            final JahiaObject jahiaObject = JahiaObject.getInstance(key);
                            final ContentObject curContentObject = (ContentObject)jahiaObject;

                            final int pageID = (curContentObject instanceof ContentField) ?
                                ((ContentField)curContentObject).getPageID() : -1;

                            final Collection c = getFieldGroupsNotHavingAccessOnPage(
                                    jParams.getPageID(), curContentObject, readGroups);

                            if (c.size() > 0) {
                                final Iterator iteC = c.iterator();

                                while (iteC.hasNext()) {
                                    final String groupID = (String)iteC.next();
                                    buff.append(groupID).append(",");
                                }

                                if (buff.length() > 0) {
                                    buff.deleteCharAt(buff.length() - 1);
                                }

                                final EngineMessage msg;
                                if (pageIDObj.intValue() == jParams.getPageID()) {
                                    msg = new EngineMessage(
                                            "org.jahia.engines.rights.ManageRights.future403warning",
                                            buff.toString(), Integer.toString(pageID));
                                } else {
                                    msg = new EngineMessage(
                                            "org.jahia.engines.rights.ManageRights.futureChild403warning",
                                            buff.toString(), Integer.toString(pageID), pageIDObj.toString());
                                }
                                result.add("manageRights.future403", msg);
                                buff.delete(0, buff.length());
                            }
                        }
                    }

                    if ( !result.isEmpty() ) {
                        result.saveMessages("manageRights.warning.",((ParamBean)jParams).getRequest());
                        if (logger.isDebugEnabled()) logger.debug("There are some warnings ! "+result);
                        engineMap.put("rightsUpdated", Boolean.FALSE);
                        return false;
                    }
                } catch (ClassNotFoundException cnfe) {
                    logger.error("Error while loading content object", cnfe);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Finds all the ContentObjects which point to the current BigText.
     * @param jParams
     * @return A Set of ObjectKey Objects
     */
    private Map contentObjectsPointingOnPage(final ProcessingContext jParams)
    throws JahiaException {
        final int pageID = jParams.getPageID();
        final Set childs = ServicesRegistry.getInstance().getJahiaPageService().getUncheckedPageSubTreeIDs(pageID);
        final Map result = new HashMap();

        goForIt(pageID, result);

        if (childs.size() > 0) {
            Iterator it = childs.iterator();
            while (it.hasNext()) {
                Integer childPageId = (Integer) it.next();
                goForIt(childPageId.intValue(), result);
            }
        }

        if (logger.isDebugEnabled()) logger.debug("Found: "+result+ " ContentObjects referencing this page or its subPages");
        return result;
    }

    private void goForIt(final int pageID, final Map<Integer,Set> result) throws JahiaException {
        final Set<ObjectKey> set = new HashSet<ObjectKey>();
        JahiaFieldXRefManager fieldXRefManager = (JahiaFieldXRefManager) SpringContextSingleton.getInstance().getContext().getBean(JahiaFieldXRefManager.class.getName());
        Collection<JahiaFieldXRef> c = fieldXRefManager.getReferencesForTarget(JahiaFieldXRefManager.PAGE + pageID);
        for (JahiaFieldXRef fieldXRef : c) {
            ContentField contentField = ContentField.getField(fieldXRef.getComp_id().getFieldId());
            if (contentField != null) {
                    if (!contentField.getActiveAndStagingEntryStates().isEmpty()) {
                        set.add(contentField.getObjectKey());
                    } else {
                        if (logger.isDebugEnabled())
                            logger.debug(
                                "No active or staging entries found for objectKey=" +
                                contentField.getObjectKey() +
                                ", not displaying deleted or archived references");
                    }
                } else {
                    logger.warn(
                            "Hardcoded URL link found for object that no longer exists:" +
                            fieldXRef.getComp_id().getFieldId() + ", removing reference");
                }
        }
        result.put(pageID, set);
    }

    /**
     * Returns a Collection of JahiaGroupIDs not having Read Access on the given
     * target page, but that do have access on the current BigText field.
     *
     * @param pageID The target page ID
     * @param field  The BigText field
     *
     * @param readGroups
     * @return A Collection of JahiaGroupIDs (String objects)
     * @throws JahiaException If Something goes wrong
     */
    private Collection getFieldGroupsNotHavingAccessOnPage(int pageID,
                                                           ContentObject field, Set readGroups) throws JahiaException {
        if (logger.isDebugEnabled())
            logger.debug("Comparing ACLs of field "+field.getID()+ " and page "+
                pageID);

        final ContentPage page = ContentPage.getPage(pageID);

        // Define an ACLEntry for "Read" Rights
        final JahiaAclEntry aclEntry = new JahiaAclEntry();
        aclEntry.setPermission(JahiaBaseACL.READ_RIGHTS, JahiaAclEntry.ACL_YES);

        // Get all the groups which can see the field
        final List fieldACLGroups = field.getACL().getGroupnameListNoAdmin(aclEntry);
        if (logger.isDebugEnabled()) logger.debug("fieldACLGroups: "+fieldACLGroups);

        if (page == null) { return fieldACLGroups; }

        // Get all the groups which can see the target page
        if (logger.isDebugEnabled()) logger.debug("pageACLGroups: "+readGroups);

        final List result = new ArrayList(fieldACLGroups.size());

        final Iterator ite = fieldACLGroups.iterator();
        while (ite.hasNext()) {
            String groupID = (String)ite.next();

            if (! readGroups.contains(groupID)) {
                // get rid of the site ID
                groupID = groupID.split(":")[0];

                // only add the group name once
                if (!result.contains(groupID)) {
                    result.add(groupID);
                }
            }
        }
        return result;
    }


    /**
     *
     */
    private boolean saveACLEntries(JahiaBaseACL acl, ProcessingContext processingContext, Map engineMap, boolean newAcl, String sessionIdentifier) {
        Map aclEntries = (Map) engineMap.remove(ACL_ENTRIES+"_"+sessionIdentifier);
        Set removedEntries = (Set) engineMap.remove(REMOVED_ENTRIES+"_"+sessionIdentifier);
        Integer inheritance = (Integer) engineMap.remove(INHERITANCE+"_"+sessionIdentifier);

        if (aclEntries == null) {
            return true;
        }

        try {
            if (newAcl) {
                int aclID = acl.getID();
                acl = new JahiaBaseACL();
                acl.create(aclID);
                engineMap.put(NEW_ACL+"_"+sessionIdentifier, acl);
            }
        } catch (JahiaException e) {
            // ???
            return false;
        }

        if (logger.isDebugEnabled())
            logger.debug("saveACLEntries: currentAclEntries: "+aclEntries +
                "\ncurrentAclEntriesToRemove" + removedEntries +
                "\ncurrentInheritance: " + inheritance);
        //keeps track of wether the current ACL was modified (for ESI)
        boolean aclAltered = false;
        try {
            //(esi) holds all users whos entries should be invalidated
            Set esiUsersToInvalidate = new HashSet();

            // Add or update all current entries
            final Iterator ite = aclEntries.keySet().iterator();
            final boolean skeletonCacheActivated = processingContext.settings().isOutputContainerCacheActivated();
            while (ite.hasNext()) {
                final Object usr_grp = ite.next();
                final Integer[] value = (Integer[]) aclEntries.get(usr_grp);

                if (value.length < 2) { continue; } // Values have not been changed

                final JahiaAclEntry entry = new JahiaAclEntry(value[0].intValue(),
                                                              value[1].intValue());

                if (usr_grp instanceof JahiaUser) {
                    final JahiaUser user = (JahiaUser) usr_grp;
                    acl.setUserEntry(user, entry);
                } else {
                    final JahiaGroup group = (JahiaGroup) usr_grp;
                    acl.setGroupEntry(group, entry);
                    Iterator users = new EnumerationIterator(group.members());
                    while (users.hasNext()) {
                        JahiaUser user = (JahiaUser) users.next();
                    }
                }
                aclAltered = true;
            }

            // Don't forget to remove the local entries.
            final Iterator ite2 = removedEntries.iterator();
            while (ite2.hasNext()) {
                final Object usr_grp = ite2.next();

                if (usr_grp instanceof JahiaUser) {
                    JahiaUser user = (JahiaUser) usr_grp;
                    acl.removeUserEntry(user);
                } else {
                    JahiaGroup group = (JahiaGroup) usr_grp;
                    acl.removeGroupEntry(group);
                    Iterator users = new EnumerationIterator(group.members());
                    while (users.hasNext()) {
                        JahiaUser user = (JahiaUser) users.next();
                    }
                }
                aclAltered = true;
            }
            if (acl.getInheritance()!=inheritance.intValue()) {
                aclAltered = true;
            }
            acl.setInheritance(inheritance.intValue());
            if (aclAltered) {
                logger.debug("InvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsiInvalidateEsi");
                //(esi) holds all pages which should be invalidated
                Set esiPagesToInvalidate = new HashSet();

                // Flush PageCache
                JahiaPageService pageService = ServicesRegistry.getInstance().getJahiaPageService();
                int pageID = processingContext.getPageID();
                final ContainerHTMLCache containerHTMLCache = ServicesRegistry.getInstance()
                        .getCacheService()
                        .getContainerHTMLCacheInstance();
                if (engineMap.containsKey("thePage")) {
                    if (skeletonCacheActivated) {
                        JahiaPage jahiaPage = ((JahiaPage) engineMap.get("thePage"));
                        pageID = jahiaPage.getID();
                        List<Locale> languageSettings = processingContext.getSite().getLanguageSettingsAsLocales(true);
                        SkeletonCache skeletonCache = ServicesRegistry.getInstance()
                                .getCacheService()
                                .getSkeletonCacheInstance();
                        try {
                            ContentObject object = jahiaPage.getContentPage().getParent(EntryLoadRequest.STAGED);
                            if (object != null) {
                                ContentObject parent = object.getParent(EntryLoadRequest.STAGED);
                                for (int j = 0; j < languageSettings.size(); j++) {
                                    Locale locale = (Locale) languageSettings.get(j);
                                    containerHTMLCache
                                            .invalidateContainerEntriesInAllModes((new ContentContainerKey(parent.getID())).toString(),
                                                                                  locale.toString());
                                    skeletonCache
                                            .invalidateSkeletonEntriesInAllModes((new ContentPageKey(pageID)).toString(),
                                                                                 locale.toString());
                                }
                                Set pickerObjects = parent.getPickerObjects();
                                if (pickerObjects != null && pickerObjects.size() > 0) {
                                    Iterator it = pickerObjects.iterator();
                                    while (it.hasNext()) {
                                        ContentObject contentObject = (ContentObject) it.next();
                                        for (int j = 0; j < languageSettings.size(); j++) {
                                            Locale locale = (Locale) languageSettings.get(j);
                                            containerHTMLCache
                                                    .invalidateContainerEntriesInAllModes((new ContentContainerKey(
                                                            contentObject.getID())).toString(), locale.toString());
                                            skeletonCache.invalidateSkeletonEntriesInAllModes((new ContentPageKey(contentObject.getPageID())).toString(), locale.toString());
                                        }
                                    }
                                }
                            }
                        } catch (JahiaException e) {
                            logger.warn("Could not found the page parent field");
                        }
                    }
                }
                else if (engineMap.containsKey("theContainer")) {
                    JahiaContainer jahiaContainer = ((JahiaContainer) engineMap.get("theContainer"));
                    pageID = jahiaContainer.getPageID();
                    if (skeletonCacheActivated) {
                        int parentContainerListID = jahiaContainer.getListID();
                        List<Locale> languageSettings = processingContext.getSite().getLanguageSettingsAsLocales(true);
                        SkeletonCache skeletonCache = ServicesRegistry.getInstance()
                                    .getCacheService()
                                    .getSkeletonCacheInstance();
                        if (parentContainerListID > 0) {
                            if (logger.isDebugEnabled()) logger.debug("ACL [" +
                                                                      acl +
                                                                      "] was altered so triggering invalidation of parent container :" +
                                                                      parentContainerListID);
                            for (int j = 0; j < languageSettings.size(); j++) {
                                Locale locale = (Locale) languageSettings.get(j);
                                containerHTMLCache
                                        .invalidateContainerEntriesInAllModes((new ContentContainerListKey(
                                                parentContainerListID)).toString(), locale.toString());

                                skeletonCache
                                        .invalidateSkeletonEntriesInAllModes((new ContentPageKey(pageID)).toString(),
                                                                             locale.toString());
                            }
                        }
                        Set pickerObjects = jahiaContainer.getContentContainer().getPickerObjects();
                        if (pickerObjects != null && pickerObjects.size() > 0) {
                            Iterator it = pickerObjects.iterator();
                            while (it.hasNext()) {
                                ContentObject contentObject = (ContentObject) it.next();
                                for (int j = 0; j < languageSettings.size(); j++) {
                                    Locale locale = (Locale) languageSettings.get(j);
                                    containerHTMLCache
                                            .invalidateContainerEntriesInAllModes((new ContentContainerKey(contentObject.getID())).toString(),
                                                                                  locale.toString());
                                    skeletonCache.invalidateSkeletonEntriesInAllModes((new ContentPageKey(contentObject.getPageID())).toString(), locale.toString());
                                }
                            }
                        }
                    }
                }
                else if (engineMap.containsKey("theContainerList")) {
                    JahiaContainerList jahiaContainerList = ((JahiaContainerList) engineMap.get("theContainerList"));

                    List<ContentObject> list = jahiaContainerList.getContentContainerList()
                            .getChilds(JahiaAdminUser.getAdminUser(0), EntryLoadRequest.STAGED);
                    ContainerHTMLCache cacheInstance = containerHTMLCache;
                    SkeletonCache skeletonCache = ServicesRegistry.getInstance()
                            .getCacheService()
                            .getSkeletonCacheInstance();
                    List<Locale> languageSettings = processingContext.getSite().getLanguageSettingsAsLocales(true);
                    for (ContentObject contentObject : list) {
                        if (contentObject instanceof ContentContainer) {
                            ContentContainer contentContainer = (ContentContainer) contentObject;
                            int id = contentContainer.getID() + 1000;
                            ContentObject ctnList = contentContainer.getParent(EntryLoadRequest.STAGED);
                            pageID = ctnList.getPageID();
                            for (int j = 0; j < languageSettings.size(); j++) {
                                Locale locale = (Locale) languageSettings.get(j);
                                cacheInstance.invalidateContainerEntriesInAllModes(contentContainer.getObjectKey().toString(),
                                                                                   locale.toString());
                                cacheInstance.invalidateContainerEntriesInAllModes(ctnList.getObjectKey().toString(),
                                                                                   locale.toString());
                                skeletonCache
                                        .invalidateSkeletonEntriesInAllModes((new ContentPageKey(pageID)).toString(),
                                                                             locale.toString());
                            }
                            Set pickerObjects = contentContainer.getPickerObjects();
                            if (pickerObjects != null && pickerObjects.size() > 0) {
                                Iterator it1 = pickerObjects.iterator();
                                while (it1.hasNext()) {
                                    ContentObject contentObject1 = (ContentObject) it1.next();
                                    for (int j = 0; j < languageSettings.size(); j++) {
                                        Locale locale = (Locale) languageSettings.get(j);
                                        containerHTMLCache
                                                .invalidateContainerEntriesInAllModes((new ContentContainerKey(
                                                        contentObject1.getID())).toString(), locale.toString());
                                        skeletonCache.invalidateSkeletonEntriesInAllModes((new ContentPageKey(contentObject1.getPageID())).toString(), locale.toString());
                                    }
                                }
                            }
                        }
                    }
                }
               
                List childs = pageService
                        .getPageIDsWithAclIDs(getAllInheritedChildAclIDs(acl
                                .getID(), new HashSet()));
                for (Iterator it = childs.iterator(); it.hasNext();) {
                    JahiaPageContentRights childPage = (JahiaPageContentRights) it
                            .next();
                    pageService.invalidatePageCache(childPage.getPageID()
                            .intValue());
                    // only invalidate esi cache entries of sub-pages if inheritance is changed
                    // if (aclInheritanceAltered && esiCacheActivated) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("ACL ["
                                        + acl
                                        + "] was altered so triggering invalidation of subpage :"
                                        + childPage.getPageID());
                    }
                    esiPagesToInvalidate.add(childPage.getPageID());
                    // }
                }
                pageService.invalidatePageCache(pageID);
            }
            JahiaEvent setRightsEvent = new JahiaEvent(this, processingContext, acl);
            ServicesRegistry.getInstance ().getJahiaEventService ().fireSetRights(setRightsEvent);

            if (newAcl && !aclAltered) {
                engineMap.remove(NEW_ACL+"_"+sessionIdentifier);
                acl.delete();
            }
            
            return true;
        } catch (Exception e) {
            logger.error("Cannot save acl "+acl, e);
            return false;
        }
    }

	private Set getAllInheritedChildAclIDs(int aclID, Set aclIDs) {
		List childAclList = ServicesRegistry.getInstance()
				.getJahiaACLManagerService().getChildAcls(aclID);
		aclIDs.add(new Integer(aclID));
		for (Iterator it = childAclList.iterator(); it.hasNext();) {
			JahiaAcl childAcl = (JahiaAcl) it.next();
			if (childAcl.getInheritance().intValue() == 1) {
				aclIDs = getAllInheritedChildAclIDs(childAcl.getAclID(), aclIDs);
			}
		}
		return aclIDs;
	}
    
}
