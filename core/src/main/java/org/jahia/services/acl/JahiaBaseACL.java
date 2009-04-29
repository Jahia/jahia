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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.model.JahiaAcl;

/**
 * This is the public class for an ACL, that will mostly be used when
 * manipulating ACLs in the content API.
 *
 * @author Fulco Houkes
 * @version 1.0
 */
public class JahiaBaseACL extends JahiaAbstractACL implements Serializable {

    private static final long serialVersionUID = 1858790995719577603L;

    //      +--+- ... -+--+--+--+
    //      | n|       | 2| 1| 0|
    //      +--+- ... -+--+--+--+
    //                   |  |  |
    //                   |  |  +--- READ_RIGHTS
    //                   |  +------ WRITE_RIGHTS
    //                   +--------- ADMIN_RIGHTS

    public static final int READ_RIGHTS = 0;
    public static final int WRITE_RIGHTS = 1;
    public static final int ADMIN_RIGHTS = 2;

    public static final int RIGHTS_MAX_OFFSET = 3;
    
    public static final int ALL_RIGHTS = 0x7; // 1 + 2 + 4

    public static final int RIGHTS_INHERITANCE_FLAG = 1 << RIGHTS_MAX_OFFSET; // 1<<3;
    
    //-------------------------------------------------------------------------
    /**
     * Default Constructor
     *
     * @param aclID ACL ID to construct
     *
     * @throws ACLNotFoundException
     * @throws JahiaException
     */
    public JahiaBaseACL (int aclID)
            throws ACLNotFoundException,
            JahiaException {
        super (aclID);
    }

    //-------------------------------------------------------------------------
    /**
     * @throws JahiaException
     */
    public JahiaBaseACL ()
            throws JahiaException {
        super ();
    }


    //-------------------------------------------------------------------------
    /**
     * @return A List of string {"Read", "Write", "Admin"}
     */
    public List<String> getBitdesc () {
        final List<String> bits = new ArrayList<String>(getActionNames());

        return bits;
    }

    public static char[] getSymbols() {
        int size = getActionNames().size();
        if (size > getSharedActions().length)
            size = getSharedActions().length;
        char[] symbols = new char[size];
        System.arraycopy(getSharedActions(), 0, symbols, 0, size);
        return symbols;
    }
    
    public static char[] getUppercaseSymbols() {
        int size = getActionNames().size();
        if (size > getSharedActions().length)
            size = getSharedActions().length;
        char[] symbols = new char[size];
        System.arraycopy(getUppercaseSharedActions(), 0, symbols, 0, size);
        return symbols;
    }    

    // -------------------------------------------------------------------------
    public final int size() {
        return getActionNames().size();
    }

    public JahiaBaseACL getParent() {
        JahiaAcl parent = super.mACL.getParent();
        return parent!=null?parent.getACL():null;
    }

    public static JahiaBaseACL getACL(final int aclid) throws JahiaException {
        return new JahiaBaseACL(aclid);
    }
}
