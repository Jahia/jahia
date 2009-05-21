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
