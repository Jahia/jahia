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
package org.jahia.services.content.decorator;

import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeIteratorWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.usermanager.*;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;

import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.security.Principal;
import java.util.*;

/**
 * A JCR group node decorator
 */
public class JCRGroupNode extends JCRNodeDecorator {

    protected static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRGroupNode.class);

    public static final String J_HIDDEN = "j:hidden";

    public JCRGroupNode(JCRNodeWrapper node) {
        super(node);
    }

    public JahiaGroup getJahiaGroup() {
        try {
            Properties properties = new Properties();
            try {
                properties.putAll(getPropertiesAsString());
            } catch (RepositoryException e) {
                logger.error("Cannot read group properties",e);
            }
            return new JahiaGroupImpl(getName(), getPath(), getResolveSite().getName(), properties);
        } catch (RepositoryException e) {
            logger.error("Cannot get group",e);
        }
        return null;
    }

    /**
     * @deprecated
     */
    public String getGroupname() {
        return getName();
    }

    public String getGroupKey() {
        return getPath();
    }

    public String getProviderName() {
        return getProvider().getKey();
    }

    public List<JCRNodeWrapper> getMembers() {
        try {
            return new GroupNodeMembers(this);
        } catch (RepositoryException e) {
            // do not log error in case of a PathNotFoundException on a transient (newly created) group node
            if (!(e instanceof PathNotFoundException) || !isNew()) {
                logger.error("Cannot get member nodes");
            }
        }
        return Collections.emptyList();
    }

    public Set<JCRUserNode> getRecursiveUserMembers() {
        Set<JCRUserNode> result = new HashSet<JCRUserNode>();

        List<JCRNodeWrapper> members = getMembers();
        for (JCRNodeWrapper member : members) {
            if (member instanceof JCRUserNode) {
                result.add((JCRUserNode) member);
            } else if (member instanceof JCRGroupNode) {
                result.addAll(((JCRGroupNode)member).getRecursiveUserMembers());
            }
        }

        return result;
    }

    public boolean isMember(String userPath) {
        if (JahiaGroupManagerService.GUEST_GROUPPATH.equals(getPath()) ||
                (JahiaGroupManagerService.USERS_GROUPPATH.equals(getPath()) &&
                        !JahiaUserManagerService.GUEST_USERPATH.equals(userPath)) ||
                (JahiaGroupManagerService.SITE_USERS_GROUPNAME.equals(getName()) &&
                        (!userPath.startsWith("/sites/") || userPath.startsWith("/sites/"+StringUtils.substringBetween(getPath(), "/sites/", "/"))))
                ) {
            return true;
        }
        List<String> membershipByPath = JahiaGroupManagerService.getInstance().getMembershipByPath(userPath);
        return membershipByPath != null && membershipByPath.contains(getPath());
    }

    public boolean isMember(JCRNodeWrapper principal) {
        return isMember(principal.getPath());
    }

    public void addMember(Principal principal) {
        if (principal instanceof JahiaPrincipal) {
            try {
                addMember(getSession().getNode(((JahiaPrincipal) principal).getLocalPath()));
            } catch (RepositoryException e) {
                logger.error("Cannot find principal", e);
            }
        }
    }

    public void addMembers(final Collection<JCRNodeWrapper> members) {
        for (JCRNodeWrapper candidate : members) {
            addMember(candidate);
        }
    }

    public JCRNodeWrapper addMember(JCRNodeWrapper principal) {
        try {
            if (principal.isNodeType("jnt:user") || principal.isNodeType("jnt:group")) {
                String[] parts = principal.getPath().split("/");
                JCRNodeWrapper member;
                if (hasNode("j:members")) {
                    member = getNode("j:members");
                } else {
                    member = addNode("j:members", "jnt:members");
                }
                for (int i = 1; i < parts.length - 1; i++) {
                    if (member.hasNode(parts[i])) {
                        member = member.getNode(parts[i]);
                    } else {
                        member = member.addNode(parts[i], "jnt:members");
                    }
                }
                if (member.hasNode(parts[parts.length - 1])) {
                    member = member.getNode(parts[parts.length - 1]);
                } else {
                    member = member.addNode(parts[parts.length - 1], "jnt:member");
                    member.setProperty("j:member", principal.getIdentifier());
                }
                return member;
            }
        } catch (RepositoryException e) {
            logger.warn(e.getMessage(), e);
        }
        return null;
    }

    public void removeMember(JCRNodeWrapper principal) {
        try {
            Query q = getSession().getWorkspace().getQueryManager().createQuery("select * from [jnt:member] as m where isdescendantnode(m,'"+ JCRContentUtils.sqlEncode(getPath()) + "') and [j:member]='" + principal.getIdentifier() + "'", Query.JCR_SQL2);
            NodeIterator ni = q.execute().getNodes();
            while (ni.hasNext()) {
                ni.nextNode().remove();
            }
        } catch (RepositoryException e) {
            logger.error("Cannot read group members",e);
        }
    }

    public boolean isHidden() {
        try {
            return hasProperty("j:hidden") && getProperty("j:hidden").getBoolean();
        } catch (RepositoryException e) {
            logger.error("Cannot read group",e);
            return false;
        }
    }

    @Override
    public String getDisplayableName() {
        try {
            return getDisplayableName(getSession().getLocale());
        } catch (RepositoryException e) {
            logger.error("", e);
        }
        return super.getDisplayableName();
    }

    public String getDisplayableName(Locale locale) {
        final String groupName = getName();
        if (JahiaGroupManagerService.GUEST_GROUPNAME.equals(groupName)) {
            Locale l = locale;
            if (l == null) {
                try {
                    l = getSession().getLocale();
                } catch (RepositoryException e) {
                    logger.error("", e);
                }
                if (l == null) l = SettingsBean.getInstance().getDefaultLocale();
                if (l == null) l = Locale.ENGLISH;
            }
            return Messages.get(ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackage(SettingsBean.getInstance().getGuestGroupResourceModuleName()),
                    SettingsBean.getInstance().getGuestGroupResourceKey(), l, groupName);
        }
        return super.getDisplayableName();

    }
}
