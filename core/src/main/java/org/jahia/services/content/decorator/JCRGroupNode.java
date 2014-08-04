/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.services.content.decorator;

import org.jahia.services.content.JCRNodeIteratorWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaPrincipal;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;

import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import java.security.Principal;
import java.util.*;

/**
 * A JCR group node decorator
 */
public class JCRGroupNode extends JCRNodeDecorator {

    protected transient static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRGroupNode.class);

    public static final String J_HIDDEN = "j:hidden";

    public JCRGroupNode(JCRNodeWrapper node) {
        super(node);
    }

    public JahiaGroup getJahiaGroup() {
        try {
            return new JahiaGroup(getName(), getPath(), getResolveSite().getName());
        } catch (RepositoryException e) {
            logger.error("Cannot get group",e);
        }
        return null;
    }

    public String getGroupKey() {
        return getPath();
    }

    public String getProviderName() {
        return getProvider().getKey();
    }

    public List<JCRNodeWrapper> getMembers() {
        List<JCRNodeWrapper> result = null;
        try {
            result = new ArrayList<JCRNodeWrapper>();
            getMembers(getNode("j:members"), result);
        } catch (RepositoryException e) {
            logger.error("Cannot read group members",e);
        }
        return result;
    }

    private void getMembers(JCRNodeWrapper members, List<JCRNodeWrapper> result) throws RepositoryException {
        JCRNodeIteratorWrapper ni = members.getNodes();
        for (JCRNodeWrapper wrapper : ni) {
            if (wrapper.isNodeType("jnt:members")) {
                getMembers(wrapper, result);
            } else if (wrapper.isNodeType("jnt:member")) {
                result.add(wrapper.getProperty("j:member").getValue().getNode());
            }
        }
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
        return JahiaGroupManagerService.GUEST_GROUPPATH.equals(getPath()) ||
                !JahiaUserManagerService.GUEST_USERPATH.equals(userPath) && JahiaGroupManagerService.USERS_GROUPPATH.equals(getPath()) ||
                JahiaGroupManagerService.getInstance().getMembershipByPath(userPath).contains(getPath());
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

    public void addMember(JCRNodeWrapper principal) {
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
                member = member.addNode(parts[parts.length - 1], "jnt:member");
                member.setProperty("j:member", principal.getIdentifier());
            }
        } catch (RepositoryException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public void removeMember(JCRNodeWrapper principal) {
        try {
            Query q = getSession().getWorkspace().getQueryManager().createQuery("select * from [jnt:member] as m where isdescendantnode(m,'"+getPath()+"') and [j:member]='"+principal.getIdentifier()+"'", Query.JCR_SQL2);
            NodeIterator ni = q.execute().getNodes();
            if (ni.hasNext()) {
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

}
