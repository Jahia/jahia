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

package org.jahia.modules.roles.rules;

import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.drools.spi.KnowledgeHelper;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.rules.NodeWrapper;
import org.jahia.services.rbac.PermissionIdentity;

/**
 * Roles/permissions service class that is used in right-hand-side
 * (consequences) of rules.
 * 
 * @author Sergiy Shyrkov
 */
public class RoleService {

    private org.jahia.services.rbac.jcr.RoleService roleService;

    /**
     * Injects an instance of the role manager service.
     * 
     * @param roleService an instance of the role manager service
     */
    public void setRoleService(org.jahia.services.rbac.jcr.RoleService roleService) {
        this.roleService = roleService;
    }

    /**
     * Creates permissions for site languages.
     * 
     * @param node the site node
     * @param drools the rule engine helper class
     * @throws RepositoryException in case of an error
     */
    public void updateSiteLangPermissions(final NodeWrapper node, KnowledgeHelper drools) throws RepositoryException {
        JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
            public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRNodeWrapper siteNode = session.getNode(node.getPath());
                final String siteKey = siteNode.getName();
                final Value[] languages = siteNode.getProperty("j:languages").getValues();
                for (Value language : languages) {
                    roleService.savePermission(new PermissionIdentity(language.getString(), "languages", siteKey));
                }
                return true;
            }
        });
    }

}