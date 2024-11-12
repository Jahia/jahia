/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.security.shiro;

import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.usermanager.JahiaUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class JahiaAccountRealm extends AuthorizingRealm {
    public static final Logger logger = LoggerFactory.getLogger(JahiaAccountRealm.class);

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        if (principals.getPrimaryPrincipal() instanceof JahiaUser) {
            JahiaUser user = (JahiaUser) principals.getPrimaryPrincipal();

            try {
                return new SimpleAuthorizationInfo(JCRTemplate.getInstance().doExecute(user, null, null, session -> {
                    JCRNodeWrapper node = session.getNode("/");

                    if (user.isRoot()) {
                        return node.getAvailableRoles().entrySet().stream().flatMap(e -> e.getValue().stream()).map(JCRNodeWrapper::getName).collect(Collectors.toSet());
                    } else {
                        return getRoles(user, node);
                    }
                }));
            } catch (RepositoryException e) {
                logger.error("Cannot login", e);
            }
        }

        return new SimpleAuthorizationInfo(Collections.emptySet());
    }

    public boolean isPermitted(PrincipalCollection principals, String permission) {
        JahiaUser jahiaUser = (JahiaUser) principals.getPrimaryPrincipal();
        PermissionOnPath permissionOnPath = new PermissionOnPath(permission);
        try {
            return JCRTemplate.getInstance().doExecute(jahiaUser, null, null, session -> session.getNode(permissionOnPath.getNode()).hasPermission(permissionOnPath.getPermission()));
        } catch (RepositoryException e) {
            logger.error("Cannot check permission", e);
        }
        return false;
    }

    private Set<String> getRoles(JahiaUser userNode, JCRNodeWrapper node) {
        Set<String> roles = new HashSet<>();
        Map<String, Map<String, String>> actualAclEntries = node.getActualAclEntries();
        for (Map.Entry<String, Map<String, String>> actualAclEntry : actualAclEntries.entrySet()) {
            String principal = actualAclEntry.getKey();
            final String principalName = principal.substring(2);
            if ((principal.charAt(0) == 'u' && principalName.equals(userNode.getName())) ||
                    (principal.charAt(0) == 'g' && ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(null, principalName).isMember(userNode.getLocalPath()))
            ) {
                for (Map.Entry<String, String> aclEntry : actualAclEntry.getValue().entrySet()) {
                    if ("GRANT".equals(aclEntry.getValue())) {
                        roles.add(aclEntry.getKey());
                    }
                }
            }
        }
        return roles;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        return new SimpleAuthenticationInfo(token.getPrincipal(), token.getPrincipal(), getName());
    }

    private class PermissionOnPath {
        private String node;
        private String permission;

        public PermissionOnPath(String value) {
            String[] split = StringUtils.split(value,':');
            if (split.length == 0) {
                node = "/";
                permission = split[0].replace('_', ':');
            } else {
                node = split[0];
                permission = split[1].replace('_', ':');
            }
        }

        public String getNode() {
            return node;
        }

        public String getPermission() {
            return permission;
        }
    }
}
