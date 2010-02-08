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

package org.jahia.services.rbac.jcr;

import static org.jahia.services.rbac.jcr.RoleManager.PROPERTY_PERMISSSIONS;
import static org.jahia.services.rbac.jcr.RoleManager.PROPERTY_ROLES;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.rbac.RoleBasedAccessControlService;
import org.jahia.services.usermanager.JahiaPrincipal;

/**
 * Service implementation for the Role Based Access Control.
 * 
 * @author Sergiy Shyrkov
 * @since 6.5
 */
public class RoleBasedAccessControlServiceImpl extends RoleBasedAccessControlService {

    private static Logger logger = Logger.getLogger(RoleBasedAccessControlServiceImpl.class);

    private RoleManager roleManager;

    public boolean hasRole(final JahiaPrincipal principal, final String role) {
        boolean hasRole = false;
        try {
            hasRole = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    boolean hasIt = false;
                    JCRNodeWrapper principalNode = roleManager.getPrincipalNode(principal, session);
                    if (principalNode != null) {
                        String searchRole = role.charAt(0) == '/' ? StringUtils.substringAfterLast(role, "/") : role;
                        QueryResult result = session.getWorkspace().getQueryManager().createQuery(
                                "/jcr:root" + principalNode.getPath() + "/jcr:deref(@" + PROPERTY_ROLES + ", '"
                                        + JCRContentUtils.stringToQueryLiteral(searchRole) + "')", Query.XPATH)
                                .execute();
                        if (role.charAt(0) == '/') {
                            // the role is specified by path
                            for (RowIterator iterator = result.getRows(); iterator.hasNext();) {
                                Row row = iterator.nextRow();
                                if (row.getValue("jcr:path").getString().equals(role)) {
                                    hasIt = true;
                                    break;
                                }
                            }
                        } else {
                            // assuming the role is specified by unique name
                            hasIt = result.getNodes().hasNext();
                        }

                        if (!hasIt) {
                            // check groups this principal is member of
                        }
                    }
                    return hasIt;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return hasRole;
    }

    public boolean isPermitted(final JahiaPrincipal principal, final String permission) {
        boolean hasPermisson = false;
        try {
            hasPermisson = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    boolean hasIt = false;
                    JCRNodeWrapper principalNode = roleManager.getPrincipalNode(principal, session);
                    if (principalNode != null) {
                        String searchPermission = permission.charAt(0) == '/' ? StringUtils.substringAfterLast(
                                permission, "/") : permission;
                        QueryResult result = session.getWorkspace().getQueryManager().createQuery(
                                "/jcr:root" + principalNode.getPath() + "/jcr:deref(@" + PROPERTY_ROLES
                                        + ", '*')/jcr:deref(@" + PROPERTY_PERMISSSIONS + ", '"
                                        + JCRContentUtils.stringToQueryLiteral(searchPermission) + "')", Query.XPATH)
                                .execute();
                        if (permission.charAt(0) == '/') {
                            // the permission is specified by path
                            for (RowIterator iterator = result.getRows(); iterator.hasNext();) {
                                Row row = iterator.nextRow();
                                if (row.getValue("jcr:path").getString().equals(permission)) {
                                    hasIt = true;
                                    break;
                                }

                            }
                        } else {
                            // assuming the permission is specified by unique
                            // name
                            hasIt = result.getNodes().hasNext();
                        }

                        if (!hasIt) {
                            // check groups this principal is member of
                        }
                    }
                    return hasIt;
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

        return hasPermisson;
    }

    public void setRoleManager(RoleManager roleManager) {
        this.roleManager = roleManager;
    }

}