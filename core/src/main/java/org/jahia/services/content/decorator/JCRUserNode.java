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

import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.ISO8601;
import org.apache.jackrabbit.value.BinaryImpl;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ValueImpl;
import org.jahia.services.pwdpolicy.PasswordHistoryEntry;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.PropertyDefinition;
import javax.jcr.version.VersionException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

/**
 * Represent a user JCR node.
 *
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 17 juin 2010
 */
public class JCRUserNode extends JCRNodeDecorator {
    private transient static Logger logger = LoggerFactory.getLogger(JCRUserNode.class);
    public static final String ROOT_USER_UUID = "b32d306a-6c74-11de-b3ef-001e4fead50b";
    public static final String PROVIDER_NAME = "jcr";
    public static final String J_DISPLAYABLE_NAME = "j:displayableName";
    public static final String J_PASSWORD = "j:password";
    public static final String J_EXTERNAL = "j:external";
    public static final String J_EXTERNAL_SOURCE = "j:externalSource";
    public final List<String> publicProperties = Arrays.asList("j:external", "j:externalSource", "j:publicProperties");

    public JCRUserNode(JCRNodeWrapper node) {
        super(node);
        try {
            ExtendedNodeType type = NodeTypeRegistry.getInstance().getNodeType("jnt:user");
        } catch (NoSuchNodeTypeException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public JahiaUser getJahiaUser() {
        return new JahiaUser(getName(), getPath());
    }

    public String getUserKey() {
        return getPath();
    }

    public String getProviderName() {
        return getProvider().getKey();
    }

    public boolean isPropertyEditable(String name) {
        try {
            return !("j:external".equals(name) || Constants.CHECKIN_DATE.equals(name)) && canGetProperty(name);
        } catch (RepositoryException e) {
            return false;
        }
    }

    public boolean isRoot() {
        try {
            return getIdentifier().equals(JCRUserNode.ROOT_USER_UUID);
        } catch (RepositoryException e) {
            return false;
        }
    }

    private boolean canGetProperty(String s) throws RepositoryException {
        if (publicProperties.contains(s) || hasPermission("jcr:write")) {
            return true;
        }
        if (!super.hasProperty("j:publicProperties")) {
            return false;
        }
        Property p = super.getProperty("j:publicProperties");
        Value[] values = p.getValues();
        for (Value value : values) {
            if (s.equals(value.getString())) {
                return true;
            }
        }
        return false;
    }

    public boolean verifyPassword(String userPassword) {
        try {
            return StringUtils.isNotEmpty(userPassword) && JahiaUserManagerService.encryptPassword(userPassword).equals(getProperty(J_PASSWORD).getString());
        } catch (RepositoryException e) {
            return false;
        }
    }

    public boolean setPassword(String pwd) {
        try {
            setProperty(J_PASSWORD, JahiaUserManagerService.encryptPassword(pwd));
            return true;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public boolean isAccountLocked() {
        return false;
    }

    public boolean isAdminMember(String siteKey) {
        return isRoot() || isMemberOfGroup(siteKey, siteKey == null ? JahiaGroupManagerService.ADMINISTRATORS_GROUPNAME : JahiaGroupManagerService.SITE_ADMINISTRATORS_GROUPNAME);
    }

    public List<PasswordHistoryEntry> getPasswordHistory() {
        return null;
    }

    public long getLastPasswordChangeTimestamp() {
        return 0;
    }

    public boolean isMemberOfGroup(String siteKey, String name) {
        if (JahiaGroupManagerService.GUEST_GROUPNAME.equals(name)) {
            return true;
        }
        if (JahiaGroupManagerService.USERS_GROUPNAME.equals(name)) {
            return !JahiaUserManagerService.GUEST_USERNAME.equals(getName());
        }
        if (isRoot() && JahiaGroupManagerService.POWERFUL_GROUPS.contains(name)) {
            return true;
        }
        // Get the services registry
        ServicesRegistry servicesRegistry = ServicesRegistry.getInstance();
        if (servicesRegistry != null) {

            // get the group management service
            JahiaGroupManagerService groupService = servicesRegistry.getJahiaGroupManagerService();

            // lookup the requested group
            JCRGroupNode group = groupService.lookupGroup(siteKey, name);
            if (group != null) {
                return group.isMember(this);
            }
        }
        return false;
    }

    protected JCRUserNode lookupUser() {
        return ServicesRegistry.getInstance().getJahiaUserManagerService()
                .lookupUser(node.getName());
    }
}
