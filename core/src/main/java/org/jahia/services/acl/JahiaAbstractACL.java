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

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.model.JahiaAcl;
import org.jahia.hibernate.model.JahiaAclEntry;
import org.jahia.hibernate.model.JahiaAclName;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.apache.log4j.Logger;

/**
 * Abstract implementation of ACL functionality for public API use. Most of
 * the implementation of this class delegates to the private JahiaAcl class
 * for the real functionality.
 *
 * This class acts as a proxy to the real implementation, so that we can
 * create instances of this class even if a real ACL doesn't exist yet (for
 * example by using the create(parentID) method).
 *
 * @author Fulco Houkes
 * @author MAP
 * @version 1.2
 */
public abstract class JahiaAbstractACL implements Cloneable, Serializable {

    private static final long serialVersionUID = -6567260139588140718L;

    /** 
     *  A pseudo-symbol, which indicates that this action is
     *  not assigned in action list.
     */
    public static char SYMBOL_EMPTY = '-';

    private transient static Logger logger = Logger.getLogger (JahiaAbstractACL.class);

    /** Reference to the ACL object */
    protected JahiaAcl mACL;
    protected JahiaAclName mACLName = null;

    /** Reference on the ACL Manager Service */
    private JahiaACLManagerService mACLService;

    protected static volatile char[] sharedActions = null;
    protected static volatile char[] uppercaseSharedActions = null;    

    protected static volatile List<String> sharedActionsNames = null;
    
    protected Boolean inheritance = null;        
    
    /** Error message constant */
    private static final String INIT_ERROR_MSG = "ACL Object not initialized";

    public static List<String> getActionNames() {
        if (sharedActionsNames == null)
            getSharedActions(); // init list
        return sharedActionsNames;
    }
    
    protected static char[] getSharedActions() {
        if (sharedActions == null) {
            sharedActionsNames = Collections.unmodifiableList(Arrays.asList(new String[]{"Read", "Write", "Admin"}));
            sharedActions = new char[]{'r', 'w', 'A'};            
            uppercaseSharedActions = new char[]{'R', 'W', 'A'};            
        }

        return sharedActions;
    }
    
    protected static char[] getUppercaseSharedActions() {
        if (uppercaseSharedActions == null)
            getSharedActions(); // init list        
        return uppercaseSharedActions;        
    }
    
    
    //-------------------------------------------------------------------------
    // Each of the derived classes should have thier own bit signification,
    // therefore each derived classe has to define the meaning of each bit.
    //
    /**
     * Return a human understandable desc of the bits.
     *
     * @return Return a List containing the desc of each bit of the
     *         ACL.
     */
    public abstract List<String> getBitdesc ();

    //-------------------------------------------------------------------------
    /**
     * Instanciate a new ACL proxy and try to load the specified ACL.
     *
     * @param aclID Identification number of the ACL to be loaded.
     *
     * @throws ACLNotFoundException Throws an exception if the current ACL object was not initialized
     *                              correctly.
     * @throws JahiaException       Throws a JahiaException if the ACL Proxy could not be initialized
     *                              properly.
     */
    public JahiaAbstractACL (int aclID)
            throws ACLNotFoundException,
            JahiaException {
        init ();
        load (aclID);
    }

    //-------------------------------------------------------------------------
    /**
     * Instanciates a new empty ACL proxy.
     *
     * @throws JahiaException Throws a JahiaException if the ACL Proxy could not be initialized
     *                        properly.
     */
    protected JahiaAbstractACL ()
            throws JahiaException {
        init ();
    }

    /**
     * Accessor to mACL
     *
     * @return the current ACL
     */
    public JahiaAcl getACL () {
        return mACL;
    }

    public JahiaAclName getACLName() {
        return mACLName;
    }

    //-------------------------------------------------------------------------
    /**
     * Create a new ACL object. If the specified parent can not be found, then
     * a null parent is transmitted to the ACL Manager's creation method.
     *
     * @param parentID Unique identification number of the parent ACL object. Set this
     *                 parameter to -1 if there is not parameter.
     *
     * @return Return true if the ACL object could be created successfully, or
     *         return false on any failure.
     *
     * @throws ACLNotFoundException
     */
    public boolean create (int parentID)
            throws ACLNotFoundException {
        // get the parent ACL reference if found
        JahiaAcl parent = null;
        if (parentID > 0) {
            parent = getService().lookupACL (parentID);
        }

        // Create the new ACL object.
        mACL = getService().createACL (parent);

        return (mACL != null);
    }

    public boolean create(int parentID, int pickedID)
            throws ACLNotFoundException {
        // get the parent ACL reference if found
        JahiaAcl parent = null;
        if (parentID > 0) {
            parent = getService().lookupACL (parentID);
        }
        JahiaAcl picked = null;
        if (pickedID > 0) {
            picked = getService().lookupACL (pickedID);
        }

        // Create the new ACL object.
        mACL = getService().createACL (parent, picked);

        return (mACL != null);
    }

    public boolean create (String name, int parentID)
            throws ACLNotFoundException {
        // get the parent ACL reference if found
        JahiaAcl parent = null;
        if (parentID > 0) {
            parent = getService().lookupACL (parentID);
        }

        // Create the new ACL object.
        mACLName = getService().createACLName (name, parent);
        mACL = mACLName.getAcl();

        return (mACLName != null);
    }


    //-------------------------------------------------------------------------
    /**
     * Destroy the current ACL referenced inside of the ACL proxy. The proxy
     * itself will be empty, and any further ACL operation on the proxy will
     * raise a JahiaACLException.
     *
     * @return Return true on success or false on any failure.
     *
     * @throws JahiaACLException Throws a JahiaACLException if the current ACL object was not
     *                           initialized correctly.
     */
    public synchronized boolean delete ()
            throws JahiaACLException {
        testProxy ();

        boolean result = false;
        synchronized (mACL) {
            if (getService().deleteACL (mACL)) {
                mACL = null;
                result = true;
            }
        }
        return result;
    }

    //-------------------------------------------------------------------------
    /**
     * Load the specified ACL into the proxy.
     *
     * @param aclID ACL's unique identification number.
     *
     * @throws ACLNotFoundException   Raise this exception when the specified acl could not be found.
     */
    public void load (int aclID)
            throws ACLNotFoundException {
        if(aclID>0)
        mACL = getService().lookupACL (aclID);
        else throw new ACLNotFoundException(aclID);
    }

    public void load (String name)
            throws ACLNotFoundException {
        mACL = getService().lookupACL (name);
    }

    //-------------------------------------------------------------------------
    // this function try to get a valid reference on the ACL Manager, needed
    // for future operations.
    private void init ()
            throws JahiaException {
        mACLService = JahiaACLManagerService.getInstance ();
        if (mACLService == null) {
            throw new JahiaException ("JahiaAbstractACL",
                    "Abstract ACL could not get the ACL Manager Instance.",
                    JahiaException.SERVICE_ERROR, JahiaException.CRITICAL_SEVERITY);
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Return the unique identification number of the current ACL object.
     *
     * @return Return the unique identification number of the current ACL.
     *
     * @throws JahiaACLException Throws an exception if the current ACL object was not initialized
     *                           correctly.
     */
    public int getID ()
            throws JahiaACLException {
        testProxy ();
        return mACL.getId().intValue();
    }


    //-------------------------------------------------------------------------
    /**
     * Get the parent ID from the current ACL.
     *
     * @return the parent ID from the current ACL.
     *
     * @throws JahiaACLException
     */
    public int getParentID ()
            throws JahiaACLException {
        testProxy ();
        return mACL.getParent() !=null ? mACL.getParent().getId().intValue():0;
    }

    /**
     * Get the inheritance flag from the current ACL
     *
     * @return the ACL inheritance status.
     *
     * @throws JahiaACLException
     */
    public final int getInheritance ()
            throws JahiaACLException {
        testProxy ();
        return mACL.getInheritance().intValue();
    }

    /**
     * Set the inheritance flag to the current ACL and update cache.
     *
     * @param inheritance The inheritance flag (INHERITANCE, NO_INHERITANCE).
     *
     * @return true if flag set whithout problem.
     *
     * @throws JahiaACLException
     */
    public final boolean setInheritance (int inheritance)
            throws JahiaACLException {
        testProxy ();
        synchronized (mACL) {
            JahiaAcl newACL = (JahiaAcl) mACL.clone();            
            boolean result = newACL.setInheritance(new Integer(inheritance));
            getService().updateCache(newACL);
            mACL = newACL;
            return result;
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Return a clone of the user entry in the ACL.
     *
     * @param user The user reference
     *
     * @return The user entry clone, or null if the user has no entry in the ACL.
     *
     * @throws JahiaACLException
     */
    public JahiaAclEntry getLocalUserEntry (JahiaUser user)
            throws JahiaACLException {
        testProxy ();
        return (JahiaAclEntry) mACL.getUserEntries().get(user.getUserKey());
    }

    //-------------------------------------------------------------------------
    /**
     * Return a clone of the user entry in the ACL.
     *
     * @param user The user reference
     *
     * @return The user entry clone, or null if the user has no entry in the ACL.
     *
     * @throws JahiaACLException
     */
    public JahiaAclEntry getUserEntry (JahiaUser user)
            throws JahiaACLException {
        testProxy ();
        return (JahiaAclEntry) getACL().getUserEntries().get(user.getUserKey());
    }


    //-------------------------------------------------------------------------
    /**
     * Add a new access for the specified user. If the user is already present
     * in the ACL object, the access is replaced by the new one.
     *
     * @param user  Reference to a non-null user object.
     * @param entry Reference to a valid non-null ACL entry.
     *
     * @return Return true on success or false on any failure.
     *
     * @throws JahiaACLException Throws an exception if the current ACL object was not initialized
     *                           correctly.
     */
    public boolean setUserEntry (JahiaUser user, JahiaAclEntry entry)
            throws JahiaACLException {
        testProxy ();
        synchronized (mACL) {
            JahiaAcl newACL = (JahiaAcl) mACL.clone();            
            boolean result = newACL.setUserEntry(user, entry);
            getService().updateCache(newACL);
            mACL = newACL;
            return result;
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Remove the user access in the current ACL object.
     *
     * @param user Reference to a non-null user object.
     *
     * @return Return true on success or false on any failure.
     *
     * @throws JahiaACLException Throws an exception if the current ACL object was not initialized
     *                           correctly.
     */
    public boolean removeUserEntry (JahiaUser user)
            throws JahiaACLException {
        testProxy ();
        synchronized (mACL) {
            JahiaAcl newACL = (JahiaAcl) mACL.clone();            
            boolean result = newACL.removeUserEntry(user);
            getService().updateCache(newACL);
            mACL = newACL;
            return result;
        }
    }


    //-------------------------------------------------------------------------
    /**
     * Remove all the entries related to users in the current ACL object.
     *
     * @return Return true on success or false on any failure.
     *
     * @throws JahiaACLException Throws an exception if the current ACL object was not initialized
     *                           correctly.
     */
    public boolean removeAllUserEntries ()
            throws JahiaACLException {
        testProxy ();
        synchronized (mACL) {
            JahiaAcl newACL = (JahiaAcl) mACL.clone();            
            boolean result = newACL.clearEntries(JahiaAcl.USER_TYPE_ENTRY);
            getService().updateCache(newACL);
            mACL = newACL;
            return result;
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Same as the other getUsernameList method except that it uses the
     * current ACL as ParentACLFinder implementation and as the ACLResource
     * @see #getUsernameList(ParentACLFinder, ACLResourceInterface, JahiaAclEntry)
     *
     * @param entry Access rights bits map. Set this parameter to null to get all the
     *              user names regarding their access rights.
     *
     * @return Return a List holding all the String representation of the users'
     *         usernames. The returned List is never null, but if no user is
     *         present in the ACL, it will be empty.
     *
     * @throws JahiaACLException Throws an exception if the current ACL object was not initialized
     *                           correctly.
     */

    public List<String> getUsernameList (JahiaAclEntry entry)
            throws JahiaACLException {
        testProxy ();
        return getACL().getUsernameList (entry);
    }

    /**
     * Return all the user names present in the ACL object having the same
     * rights as specified.
     *
     * @param parentACLFinder an implementation of the parent ACL finder
     * interface that allows finding of the parent object for the object
     * passed as the aclResource parameter.
     * @param aclResource the object on which we are working on, to be passed
     * to the parent ACL finder implementation when looking for it's parent.
     * @param entry Access rights bits map. Set this parameter to null to get all the
     *              user names regarding their access rights.
     *
     * @return Return a List holding all the String representation of the users'
     *         usernames. The returned List is never null, but if no user is
     *         present in the ACL, it will be empty.
     *
     * @throws JahiaACLException Throws an exception if the current ACL object was not initialized
     *                           correctly.
     */
    public List<String> getUsernameList (ParentACLFinder parentACLFinder, ACLResourceInterface aclResource, JahiaAclEntry entry)
            throws JahiaACLException {
        testProxy ();
        return getACL().getUsernameList (parentACLFinder, aclResource, entry);
    }


    //-------------------------------------------------------------------------
    /**
     * Return a clone of the user entry in the ACL.
     *
     * @param group The group reference
     *
     * @return The user entry clone, or null if the user has no entry in the ACL.
     *
     * @throws JahiaACLException
     */
    public JahiaAclEntry getLocalGroupEntry (JahiaGroup group)
            throws JahiaACLException {
        testProxy ();
        return (JahiaAclEntry) mACL.getGroupEntries().get(group.getGroupKey());
    }

    //-------------------------------------------------------------------------
    /**
     * Return a clone of the user entry in the ACL.
     *
     * @param group The group reference
     *
     * @return The user entry clone, or null if the user has no entry in the ACL.
     *
     * @throws JahiaACLException
     */
    public JahiaAclEntry getGroupEntry (JahiaGroup group)
            throws JahiaACLException {
        testProxy ();
        return (JahiaAclEntry) getACL().getGroupEntries().get(group.getGroupKey());
    }


    //-------------------------------------------------------------------------
    /**
     * Add a new access for the specified group. If the group is already present
     * in the ACL object, the access is replaced by the new one.
     *
     * @param group Reference to a non-null group object.
     * @param entry Reference to a valid non-null ACL entry.
     *
     * @return Return true on success or false on any failure.
     *
     * @throws JahiaACLException Throws an exception if the current ACL object was not initialized
     *                           correctly.
     */
    public boolean setGroupEntry (JahiaGroup group, JahiaAclEntry entry)
            throws JahiaACLException {
        testProxy ();
        synchronized (mACL) {
            JahiaAcl newACL = (JahiaAcl) mACL.clone();            
            boolean result = newACL.setGroupEntry(group, entry);
            getService().updateCache(newACL);
            mACL = newACL;
            return result;
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Remove the group access in the current ACL object.
     *
     * @param group Reference to a non-null group object.
     *
     * @return Return true on success or false on any failure.
     *
     * @throws JahiaACLException Throws an exception if the current ACL object was not initialized
     *                           correctly.
     */
    public boolean removeGroupEntry (JahiaGroup group)
            throws JahiaACLException {
        testProxy ();
        synchronized (mACL) {
            JahiaAcl newACL = (JahiaAcl) mACL.clone();            
            boolean result = newACL.removeGroupEntry(group);
            getService().updateCache(newACL);
            mACL = newACL;
            return result;
        }
    }


    //-------------------------------------------------------------------------
    /**
     * Remove all the entries related to groups in the current ACL object.
     *
     * @return Return true on success or false on any failure.
     *
     * @throws JahiaACLException Throws an exception if the current ACL object was not initialized
     *                           correctly.
     */
    public boolean removeAllGroupEntries ()
            throws JahiaACLException {
        testProxy ();
        synchronized (mACL) {
            JahiaAcl newACL = (JahiaAcl) mACL.clone(); 
            boolean result = newACL.clearEntries(JahiaAcl.GROUP_TYPE_ENTRY);
            getService().updateCache(newACL);
            mACL = newACL;
            return result;
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Same as the other getGroupNameList method except that it uses the
     * current ACL as ParentACLFinder implementation and as the ACLResource
     * @see #getGroupnameList(ParentACLFinder, ACLResourceInterface, JahiaAclEntry)
     * @param entry Access rights bits map. Set this parameter to null to get all the
     *              group names regarding their access rights.
     * @return Return a List holding all the String representation of the
     *         groups' usernames. The returned List is never null, but if no
     *         group is present in the ACL, it will be empty.
     * @throws JahiaACLException Throws an exception if the current ACL object was not initialized
     *                           correctly.
     */
    public List<String> getGroupnameList (JahiaAclEntry entry)
            throws JahiaACLException {
        testProxy ();
        return getACL().getGroupnameList (entry);
    }

    /**
     * Return all the group names present in the ACL object having the same
     * rights as specified.
     *
     * @param parentACLFinder an implementation of the parent ACL finder
     * interface that allows finding of the parent object for the object
     * passed as the aclResource parameter.
     * @param aclResource the object on which we are working on, to be passed
     * to the parent ACL finder implementation when looking for it's parent.
     * @param entry Access rights bits map. Set this parameter to null to get all the
     *              group names regarding their access rights.
     *
     * @return Return a List holding all the String representation of the
     *         groups' usernames. The returned List is never null, but if no
     *         group is present in the ACL, it will be empty.
     * @throws JahiaACLException Throws an exception if the current ACL object was not initialized
     *                           correctly.
     */
    public List<String> getGroupnameList (ParentACLFinder parentACLFinder, ACLResourceInterface aclResource, JahiaAclEntry entry)
            throws JahiaACLException {
        testProxy ();
        return getACL().getGroupnameList (parentACLFinder, aclResource, entry);
    }

    //-------------------------------------------------------------------------
    /**
     * Same as the other getGroupnameListNoAdmin method except that it uses the
     * current ACL as ParentACLFinder implementation and as the ACLResource
     * @see #getGroupnameListNoAdmin(ParentACLFinder, ACLResourceInterface, JahiaAclEntry)
     *
     * @param entry Access rights bits map. Set this parameter to null to get all the
     *              group names regarding their access rights.
     *
     * @return Return a List holding all the String representation of the
     *         groupnames. The returned List is never null, but if no group is
     *         present in the ACL, it will be empty.
     * @throws JahiaACLException
     */
    public List<String> getGroupnameListNoAdmin (JahiaAclEntry entry)
            throws JahiaACLException {
        testProxy ();
        return getACL().getGroupnameListNoAdmin (entry);
    }

    /**
     * Return all the group names present in the ACL object having the same
     * rights as specified.
     *
     * @param parentACLFinder an implementation of the parent ACL finder
     * interface that allows finding of the parent object for the object
     * passed as the aclResource parameter.
     * @param aclResource the object on which we are working on, to be passed
     * to the parent ACL finder implementation when looking for it's parent.
     * @param entry Access rights bits map. Set this parameter to null to get all the
     *              group names regarding their access rights.
     *
     * @return Return a List holding all the String representation of the
     *         groups' usernames. The returned List is never null, but if no
     *         group is present in the ACL, it will be empty.
     * @throws JahiaACLException
     */
    public List<String> getGroupnameListNoAdmin (ParentACLFinder parentACLFinder, ACLResourceInterface aclResource, JahiaAclEntry entry)
            throws JahiaACLException {
        testProxy ();
        return getACL().getGroupnameListNoAdmin (parentACLFinder, aclResource, entry);
    }

    //-------------------------------------------------------------------------
    /**
     * Same as the other getPermission method except that it uses the
     * current ACL as ParentACLFinder implementation and as the ACLResource
     * @see #getPermission(ParentACLFinder, ACLResourceInterface, JahiaAclEntry, JahiaUser, int)
     *
     * @param user       Reference to a non-null user object.
     * @param permission Bit index of the requested access, this index should be defined as
     *                   a constant in the derived classes.
     *
     * @return True if the specified user has the requested rights in the ACL,
     *         or in one of the parent's ACLs.
     *
     * @throws JahiaACLException Throws an exception if the current ACL object was not initialized
     *                           correctly.
     */
    public boolean getPermission (JahiaUser user, int permission)
            throws JahiaACLException {
        testProxy ();
        return getACL().getPermission (user, permission);
    }

    /**
     * Check the permission of a given user recursively from the acl tree.
     *
     * @param parentACLFinder an implementation of the parent ACL finder
     * interface that allows finding of the parent object for the object
     * passed as the aclResource parameter.
     * @param aclResource the object on which we are working on, to be passed
     * to the parent ACL finder implementation when looking for it's parent.
     * @param user       Reference to a non-null user object.
     * @param permission Bit index of the requested access, this index should be defined as
     *                   a constant in the derived classes.
     *
     * @return True if the specified user has the requested rights in the ACL,
     *         or in one of the parent's ACLs.
     * @throws JahiaACLException Throws an exception if the current ACL object was not initialized
     *                           correctly.
     */
    public boolean getPermission (ParentACLFinder parentACLFinder, ACLResourceInterface aclResource, JahiaUser user, int permission)
            throws JahiaACLException {
        testProxy ();
        return getACL().getPermission (parentACLFinder, aclResource, user, permission);
    }

    /**
     * Same as the other getPermission method except that it uses the
     * current ACL as ParentACLFinder implementation and as the ACLResource
     * @see #getPermission(ParentACLFinder, ACLResourceInterface, JahiaAclEntry, JahiaUser, int, boolean)
     *
     * @param user             Reference to a non-null user object.
     * @param permission       Bit index of the requested access, this index should be defined as
     *                         a constant in the derived classes.
     * @param aclIsDefined if true, check on locale ACL entry.
     *
     * @return Return true on success or false on any failure.
     *
     * @throws JahiaACLException Throws an exception if the current ACL object was not initialized
     *                           correctly.
     */
    public boolean getPermission (JahiaUser user, int permission, boolean aclIsDefined)
            throws JahiaACLException {
        testProxy ();
        boolean hasPermission = getACL().getPermission (user, permission);
        if (!hasPermission) {
            boolean foundAclEntry = false;
            if (aclIsDefined) {
                JahiaAclEntry aclEntry = getUserEntry (user);
                if (aclEntry == null) {
                    // we need to investiguate further in group

                    // first we reduce the permission from admin to write, or
                    // from write to read
                    /**
                     * @todo this is very hardcoded to the existing permissiong
                     * and might not work with extended permissions
                     */
                    aclEntry = new JahiaAclEntry (permission - 1, 0);
                    List<String> v = getGroupnameList (aclEntry);
                    if (v.size () == 0) {
                        // if no group permission has been set we assume that
                        // no permission is denied, and therefore return true.
                        // (note:this is the opposite from the default ACL resolution
                        // behavior,where we usually assume that if no permission
                        // exists then it is denied.)
                        hasPermission = true;
                    } else {
                        int size = v.size ();
                        String grpName;
                        JahiaGroup grp;
                        for (int i = 0; i < size; i++) {
                            grpName = v.get (i);
                            grp = ServicesRegistry.getInstance ()
                                    .getJahiaGroupManagerService ()
                                    .lookupGroup (grpName);
                            if (grp != null) {
                                if (grp.isMember (user)) {
                                    JahiaAclEntry grpACLEntry = getGroupEntry (grp);
                                    if (grpACLEntry != null) {
                                        foundAclEntry = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    foundAclEntry = true;
                }
            }
            if (!foundAclEntry) {
                hasPermission = true;
            }
        }
        return hasPermission;
    }

    /**
     * Return true if the specified user has the requested rights in the ACL,
     * or in one of the parent's ACLs, if not, return true if there is a local
     * ACL entry with lesser permission.
     *
     * This method is used notably in the ContainerListFieldACLs, and is
     * different from the default getPermission behavior. Basically it will
     * default to true if NO permission has been set for a user. So for example
     * if we test if the user has write permission on an object, and that in
     * all the ACLs (including the parents), no entry exists, then this method
     * will return true.
     *
     * @param parentACLFinder an implementation of the parent ACL finder
     * interface that allows finding of the parent object for the object
     * passed as the aclResource parameter.
     * @param aclResource the object on which we are working on, to be passed
     * @param user             Reference to a non-null user object.
     * @param permission       Bit index of the requested access, this index should be defined as
     *                         a constant in the derived classes.
     * @param checkLocaleEntry if true, check on locale ACL entry.
     *
     * @return Return true on success or false on any failure.
     * @throws JahiaACLException Throws an exception if the current ACL object was not initialized
     *                           correctly.
     */
    public boolean getPermission (ParentACLFinder parentACLFinder, ACLResourceInterface aclResource, JahiaUser user, int permission, boolean checkLocaleEntry)
            throws JahiaACLException {
        testProxy ();
        boolean hasPermission = getACL().getPermission (parentACLFinder, aclResource, user, permission);
        if (!hasPermission) {
            boolean foundAclEntry = false;
            JahiaAclEntry aclEntry = getUserEntry (user);
            if (aclEntry == null) {
                // we need to investiguate further in group

                // first we reduce the permission from admin to write, or
                // from write to read
                /**
                 * @todo this is very hardcoded to the existing permissiong
                 * and might not work with extended permissions
                 */
                aclEntry = new JahiaAclEntry (permission - 1, 0);
                List<String> v = getGroupnameList (aclEntry);
                if (v.size () == 0) {
                    // if no group permission has been set we assume that
                    // no permission is denied, and therefore return true.
                    // (note:this is the opposite from the default ACL resolution
                    // behavior,where we usually assume that if no permission
                    // exists then it is denied.)
                    hasPermission = true;
                } else {
                    int size = v.size ();
                    String grpName;
                    JahiaGroup grp;
                    for (int i = 0; i < size; i++) {
                        grpName = v.get (i);
                        grp = ServicesRegistry.getInstance ()
                                .getJahiaGroupManagerService ()
                                .lookupGroup (grpName);
                        if (grp != null) {
                            if (grp.isMember (user)) {
                                JahiaAclEntry grpACLEntry = getGroupEntry (grp);
                                if (grpACLEntry != null) {
                                    foundAclEntry = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            } else {
                foundAclEntry = true;
            }
            if (!foundAclEntry) {
                hasPermission = true;
            }
        }
        return hasPermission;
    }

    //-------------------------------------------------------------------------
    /**
     * Same as the other getPermission method except that it uses the
     * current ACL as ParentACLFinder implementation and as the ACLResource
     * @see #getPermission(ParentACLFinder, ACLResourceInterface, JahiaAclEntry, JahiaGroup, int, boolean)
     *
     * @param group      Reference to a non-null group object.
     * @param permission Bit index of the requested access, this index should be defined as
     *                   a constant in the derived classes.
     *
     * @return Return true on success or false on any failure.
     *
     * @throws JahiaACLException Throws an exception if the current ACL object was not initialized
     *                           correctly.
     */
    public boolean getPermission (JahiaGroup group, int permission)
            throws JahiaACLException {
        testProxy ();
        return getACL().getPermission (group, permission);
    }

    /**
     * Return true if the specified group has the requested rights in the ACL,
     * or in one of the parent's ACLs.
     *
     * @param parentACLFinder an implementation of the parent ACL finder
     * interface that allows finding of the parent object for the object
     * passed as the aclResource parameter.
     * @param aclResource the object on which we are working on, to be passed
     * @param group      Reference to a non-null group object.
     * @param permission Bit index of the requested access, this index should be defined as
     *                   a constant in the derived classes.
     *
     * @return Return true on success or false on any failure.
     *
     * @throws JahiaACLException Throws an exception if the current ACL object was not initialized
     *                           correctly.
     */
    public boolean getPermission (ParentACLFinder parentACLFinder, ACLResourceInterface aclResource, JahiaGroup group, int permission)
            throws JahiaACLException {
        testProxy ();
        return getACL().getPermission (parentACLFinder, aclResource, group, permission);
    }

    public boolean getPermission (ParentACLFinder parentACLFinder, ACLResourceInterface aclResource, JahiaUser user,
                                  int permission, boolean siteAdminHaveAllRights, int objectSiteID)
            throws JahiaACLException {
        testProxy ();
        return getACL().getPermission (parentACLFinder, aclResource, user, permission, siteAdminHaveAllRights, objectSiteID);
    }

    //-------------------------------------------------------------------------
    public String toString () {
        if (mACL == null) {
            return "-ACL proxy not initialized-";
        }
        return mACL.toString ();
    }


    //-------------------------------------------------------------------------
    /**
     * Same as the other getUsernameListAlsoGroupUsers method except that it uses the
     * current ACL as ParentACLFinder implementation and as the ACLResource
     * @see #getUsernameListAlsoGroupUsers(ParentACLFinder, ACLResourceInterface, JahiaAclEntry)
     *
     * @param entry Access rights bits map. Set this parameter to null to get all the
     *              user names regarding their access rights. Only one bit of the entry
     *              should be set to ACL_YES!
     *
     * @return Return a List holding all the String representation of the users'
     *         usernames. The returned List is never null, but if no user is
     *         present in the ACL, it will be empty.
     * @throws JahiaACLException Throws an exception if the current ACL object was not initialized
     *                           correctly.
     */
    public List<String> getUsernameListAlsoGroupUsers (JahiaAclEntry entry)
            throws JahiaACLException {
        testProxy ();
        return getACL().getUsernameListAlsoGroupUsers (entry);
    }
    
    /**
     * Same as the other getUsernameListAlsoGroupUsers method except that it uses the
     * current ACL as ParentACLFinder implementation and as the ACLResource
     * @see #getUsernameListAlsoGroupUsers(ParentACLFinder, ACLResourceInterface, JahiaAclEntry)
     *
     * @param entry Access rights bits map. Set this parameter to null to get all the
     *              user names regarding their access rights. Only one bit of the entry
     *              should be set to ACL_YES!
     * @param ignoreGuestAndUsersGroup set to "true" if user members of the UserGroup and GuestGroup should
     *              be ignored              
     *
     * @return Return a List holding all the String representation of the users'
     *         usernames. The returned List is never null, but if no user is
     *         present in the ACL, it will be empty.
     * @throws JahiaACLException Throws an exception if the current ACL object was not initialized
     *                           correctly.
     */
    public List<String> getUsernameListAlsoGroupUsers (JahiaAclEntry entry, boolean ignoreGuestAndUsersGroup)
            throws JahiaACLException {
        testProxy ();
        return getACL().getUsernameListAlsoGroupUsers (entry, ignoreGuestAndUsersGroup);
    }        

    /**
     * Return all the user names present in the ACL object having the same
     * rights as specified, including users members of groups having the same
     * rights as specified.
     *
     * @param parentACLFinder an implementation of the parent ACL finder
     * interface that allows finding of the parent object for the object
     * passed as the aclResource parameter.
     * @param aclResource the object on which we are working on, to be passed
     * to the parent ACL finder implementation when looking for it's parent.
     * @param entry Access rights bits map. Set this parameter to null to get all the
     *              user names regarding their access rights. Only one bit of the entry
     *              should be set to ACL_YES!
     *
     * @return Return a List holding all the String representation of the users'
     *         usernames. The returned List is never null, but if no user is
     *         present in the ACL, it will be empty.
     * @throws JahiaACLException Throws an exception if the current ACL object was not initialized
     *                           correctly.
     */
    public List<String> getUsernameListAlsoGroupUsers (ParentACLFinder parentACLFinder, ACLResourceInterface aclResource, JahiaAclEntry entry)
            throws JahiaACLException {
        testProxy ();
        return getACL().getUsernameListAlsoGroupUsers (parentACLFinder, aclResource, entry);
    }

    //-------------------------------------------------------------------------
    private void testProxy ()
            throws JahiaACLException {
        if (mACL == null) {
            throw new JahiaACLException (INIT_ERROR_MSG,
                    JahiaACLException.ACL_NOT_INITIALIZED);
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Sets the ACL identifier for the parent of the current ACL. This is used
     * to reparent ACLs and has an immediate effect (ie changes are persisted
     * immediately), provided the parent does exist.
     *
     * @param parentID the identifier of the parent ACL
     * @throws ACLNotFoundException raised in case the parent ACL does not
     * exist. In this case no change is persisted.
     */
    public void setParentID (int parentID)
            throws ACLNotFoundException {
        JahiaAcl parentACL = null;

        synchronized (mACL) {
            if (parentID > 0) {
                parentACL = getService().lookupACL(parentID);

                JahiaAcl current = parentACL;
                do {
                    if (mACL.getId().intValue() == current.getId().intValue()) {
                        ACLNotFoundException aclNotFoundException = new ACLNotFoundException(parentID);
                        logger.error("Permission loop detected : " + mACL.getId() +" --> " + parentID, aclNotFoundException);
                        throw aclNotFoundException;
                    }
                    current = current.getParent();
                } while(current != null);
            }

            JahiaAcl newACL = (JahiaAcl) mACL.clone();
            newACL.setParent(parentACL);
            getService().updateCache(newACL);
            mACL = newACL;
        }
    }

    // -------------------------------------------------------------------------
    /**
     * Make an exact copy of the ACL except the ACL ID.
     *
     * @return the clone Object
     */
    public Object clone () {
        return mACL.clone ();
    }

    //--------------------------------------------------------------------------
    /**
     * Return true if the given aclId is in this acl parents path
     *
     * @param aclId the ACL identifier to test for presence in the parents
     * of this ACL
     *
     * @return true if the ACL identifier is present in the parents, false
     * otherwise.
     */
    public boolean isAclInParents (int aclId) {
        return this.getACL ().isAclInParents (aclId);
    }

    private void writeObject(java.io.ObjectOutputStream out)
        throws IOException {
        out.writeObject(mACL);
    }
    private void readObject(java.io.ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        mACL = (JahiaAcl) in.readObject();
        mACLService = null;
    }

    /**
     * Used to reconnect to ACL service, notably after an object is
     * unserialized. We can't reconnect while unserializing because the
     * services might not yet have been initialized yet (as in the case when
     * restoring objects from serialized session).
     * @return JahiaACLManagerService
     */
    private JahiaACLManagerService getService() {
        if (mACLService == null) {
            mACLService = ServicesRegistry.getInstance().getJahiaACLManagerService();
        }
        return mACLService;
    }
}
