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
 package org.jahia.hibernate.model;

import java.io.Serializable;

/**
 * This class represents a "named" Jahia ACL, meaning that we
 * can now identified ACLs by logical names, such as :
 * org.jahia.admin.ManageUsers.addUser
 * etc, in order to define who can perform specific actions.
 *
 * @hibernate.class table="jahia_acl_names" lazy="false"
 * @hibernate.cache usage="nonstrict-read-write"
 */
public class JahiaAclName implements Serializable {

    private String aclName;

    private JahiaAcl acl;

    /**
     * @hibernate.id generator-class="assigned"
     * type="java.lang.String"
     * column="acl_name"
     */
    public String getAclName() {
        return aclName;
    }

    public void setAclName(String aclName) {
        this.aclName = aclName;
    }

    /**
     * @hibernate.many-to-one not-null="false" unique="true" column="acl_id"
     */
    public JahiaAcl getAcl() {
        return acl;
    }

    public void setAcl(JahiaAcl acl) {
        this.acl = acl;
    }
}
