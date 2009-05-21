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
 package org.jahia.data.applications;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.pluto.descriptors.portlet.UserAttributeDD;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.acl.ACLNotFoundException;
import org.jahia.services.acl.ACLResourceInterface;
import org.jahia.services.acl.JahiaBaseACL;

/**
 * This object contains all the data relative to an application, notably the
 * context in which it should run, it's type (servlet or JSP) and additionnal
 * information such as rights, etc...
 * @author Serge Huber
 * @version 1.0
 */
public class ApplicationBean implements Serializable, Comparator<ApplicationBean>, Cloneable, ACLResourceInterface {

    private static final long serialVersionUID = -5886294839254670413L;

    private static final transient Logger logger = Logger.getLogger(ApplicationBean.class);
    
    private int ID;
    private String name;
    private String context;
    private int visible = 0;
    private boolean shared = false;
    private int rights;
    private String filename = ""; // the file name of the .war or .ear package
    private String desc = "";
    private String type;
    private List<EntryPointInstance> entryPointInstances;
    private List<UserAttributeDD> userAttributes;

    /**
     *
     */
    public ApplicationBean(int ID,
                           String name,
                           String context,
                           int visible,
                           boolean shared,
                           int rights,
                           String filename,
                           String desc,
                           String type
    ) {
        setID(ID);
        this.name = name;
        this.context = context;
        this.visible = visible;
        this.shared = shared;
        this.rights = rights;
        this.filename = filename;
        this.desc = desc;
        this.type = type;
    } // end constructor

    /**
     * accessor methods
     * {
     */
    public int getID () {return ID;
    }

    public String getName () {return name;
    }

    public String getContext () {return context;
    }

    public int getVisibleStatus () {return visible;
    }

    public boolean isShared () {return shared;
    }

    public int getRights () {return rights;
    }

    public String getFilename () {return filename;
    }

    public String getdesc () {return desc;
    }

    public void setID (int ID) {this.ID = ID;
    }

    public void setName (String name) {this.name = name;
    }

    public void setVisible (int visible) {this.visible = visible;
    }

    public void setShared (boolean shared) {this.shared = shared;
    }

    public void setRights (int rights) {this.rights = rights;
    }

    public void setFilename (String filename) {this.filename = filename;
    }

    public void setdesc (String descr) {this.desc = descr;
    }

    // end accessor methods

    //-------------------------------------------------------------------------
    /**
     * Compare between two objects, sort by their name
     *
     * @param Object
     * @param Object
     */
    public int compare (ApplicationBean c1, ApplicationBean c2)
        throws ClassCastException {

        return (c1.getName().compareToIgnoreCase(c2.getName()));

    }

    public String getType () {
        return type;
    }

    public List<EntryPointDefinition> getEntryPointDefinitions() {
        return ServicesRegistry.getInstance().getApplicationsManagerService().getAppEntryPointDefinitions(this);
    }

    public EntryPointDefinition getEntryPointDefinitionByName(String definitionName) {
        EntryPointDefinition entryPointDefinition = null;
        Iterator<EntryPointDefinition> entryPointDefinitions = getEntryPointDefinitions().
                                         iterator();
        while (entryPointDefinitions.hasNext()) {
            EntryPointDefinition curEntryPointDefinition = (
                EntryPointDefinition) entryPointDefinitions.next();
            if (curEntryPointDefinition.getName().equals(definitionName)) {
                entryPointDefinition = curEntryPointDefinition;
                break;
            }
        }
        return entryPointDefinition;
    }

    public List<EntryPointInstance> getEntryPointInstances() {
        return entryPointInstances;
    }
    public void setEntryPointInstances(List<EntryPointInstance> entryPointInstances) {
        this.entryPointInstances = entryPointInstances;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException cnse) {
            // should never happen as we do support Cloneable in the interface
            return null;
        }
    }

    public JahiaBaseACL getACL() {
        JahiaBaseACL acl = null;
        try {
            acl = new JahiaBaseACL (rights);
        } catch (ACLNotFoundException ex) {
            logger.error(ex.getMessage(), ex);
        } catch (Exception t) {
            logger.error(t.getMessage(), t);
        }
        return acl;
    }

    public int getAclID() {
        return rights;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ApplicationBean that = (ApplicationBean) o;

        return ID == that.ID;

    }

    public int hashCode() {
        return ID;
    }

    public List<UserAttributeDD> getUserAttributes() {
        return userAttributes;
    }

    public void setUserAttributes(List<UserAttributeDD> userAttributes) {
        this.userAttributes = userAttributes;
    }
} // end ApplicationBean
