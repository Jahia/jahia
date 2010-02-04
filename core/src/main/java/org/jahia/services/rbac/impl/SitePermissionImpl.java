/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.rbac.impl;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Default implementation of a permission in Jahia that is targeted for a
 * particular virtual site.
 * 
 * @author Sergiy Shyrkov
 */
public class SitePermissionImpl extends PermissionImpl {

    private String site;

    /**
     * Initializes an instance of this class.
     * 
     * @param name the name of the the permission
     * @param group the name of the permission group
     * @param site the virtual site key current permission is limited to
     */
    public SitePermissionImpl(String name, String group, String site) {
        super(name, group);
        this.site = site;
    }

    /**
     * Initializes an instance of this class.
     * 
     * @param name the name of the the permission
     * @param group the name of the permission group
     * @param site the virtual site key current permission is limited to
     * @param description a short description for this permission
     */
    public SitePermissionImpl(String name, String group, String site, String description) {
        super(name, group, description);
        this.site = site;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof SitePermissionImpl)) {
            return false;
        }
        SitePermissionImpl another = (SitePermissionImpl) obj;
        return new EqualsBuilder().append(getName(), another.getName()).append(getGroup(), another.getGroup()).append(
                getSite(), another.getSite()).isEquals();
    }

    /**
     * The virtual site key current permission is limited to.
     * 
     * @return virtual site key current permission is limited to
     */
    public String getSite() {
        return site;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getName()).append(getGroup()).toHashCode();
    }

    /**
     * Sets the key of the target virtual site.
     * 
     * @param site the key of the target virtual site
     */
    public void setSite(String site) {
        this.site = site;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}