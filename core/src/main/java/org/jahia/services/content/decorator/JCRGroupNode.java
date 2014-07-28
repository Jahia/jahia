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

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
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
            JCRNodeIteratorWrapper ni = getNode("j:members").getNodes();
            for (JCRNodeWrapper wrapper : ni) {
                result.add(wrapper.getProperty("j:member").getValue().getNode());
            }
        } catch (RepositoryException e) {
            logger.error("Cannot read group members",e);
        }
        return result;
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

    public boolean isMember(JCRNodeWrapper principal) {
        if(JahiaGroupManagerService.GUEST_GROUPPATH.equals(getPath())) return true;
        if(!JahiaUserManagerService.GUEST_USERNAME.equals(principal.getName()) && JahiaGroupManagerService.USERS_GROUPPATH.equals(getPath())) return true;
        try {
            JCRNodeIteratorWrapper nodes = getNode("j:members").getNodes();
            boolean isMemberB = false;
            for (JCRNodeWrapper jcrNodeWrapper : nodes) {
                Node node1 = jcrNodeWrapper.getProperty("j:member").getNode();
                if(node1.isNodeType("jnt:group")){
                    isMemberB |= ((JCRGroupNode)node1).isMember(principal);
                }
                else {
                    isMemberB |= (node1.getName().equals(principal.getName()));
                }
            }
            return isMemberB;
        } catch (RepositoryException e) {
            logger.error("Cannot read group members",e);
            return false;
        }
    }

    public void addMember(Principal principal) {
        if (principal instanceof JahiaUser) {
            addMember(ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(((JahiaUser) principal).getLocalPath()));
        } else if (principal instanceof JahiaGroup) {
            addMember(ServicesRegistry.getInstance().getJahiaGroupManagerService().lookupGroup(((JahiaGroup) principal).getLocalPath()));
        }
    }

    public void addMembers(final Collection<JCRNodeWrapper> members) {
        for (JCRNodeWrapper candidate : members) {
            addMember(candidate);
        }
    }

    public void addMember(JCRNodeWrapper principal) {
        try {
            JCRNodeWrapper member = null;
            if(principal.isNodeType("jnt:user")) {
                member = getNode("j:members").addNode(principal.getName(), "jnt:member");
            } else if (principal.isNodeType("jnt:group")){
                member = getNode("j:members").addNode(principal.getPath().replaceAll("/", "_"), "jnt:member");
            }
            if(member!=null) {
                member.setProperty("j:member", principal.getIdentifier());
            }
        } catch (RepositoryException e) {
            logger.warn(e.getMessage(), e);
        }
    }

    public void removeMember(JCRNodeWrapper principal) {
        try {
            JCRNodeIteratorWrapper nodes = getNode("j:members").getNodes();
            for (JCRNodeWrapper jcrNodeWrapper : nodes) {
                String node1 = jcrNodeWrapper.getProperty("j:member").getString();
                if (node1.equals(principal.getIdentifier())) {
                    jcrNodeWrapper.remove();
                    return;
                }
            }
        } catch (RepositoryException e) {
            logger.error("Cannot read group members",e);
        }

    }

    public void removeMembers() {

    }

    public boolean isHidden() {
        return false;
    }

    public boolean isMember(String userPath) {
        return isMember(ServicesRegistry.getInstance().getJahiaUserManagerService().lookupUserByKey(userPath));
    }

}
