/**
 *
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.data.applications;

import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPortletNode;
import org.jahia.registries.ServicesRegistry;
import org.jahia.api.user.JahiaUserService;
import org.jahia.bin.Jahia;
import org.apache.log4j.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.Node;
import java.io.Serializable;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.security.AccessControlException;

/**
 * <p>Title: EntryPointInstance for a web application</p>
 * <p>Description: This class represents an entry point instance into a web
 * application. It is an instance of an EntryPointDefinition, meaning that
 * a web application can have multiple EntryPointDefinitions, which in turn
 * may be multiple EntryPointInstances. Only the ApplicationBean and the
 * EntryPointInstance are persistant objects. The EntryPointDefinitions are
 * objects that are built dynamically by the ApplicationManagerProvider
 * services.</p>
 * <p>Copyright: Copyright (c) 2004</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Serge Huber
 * @version 1.0
 * @todo properties not implemented, will do later.
 */

public class EntryPointInstance implements Serializable {
    private transient static final Logger logger = Logger.getLogger(EntryPointInstance.class);
    private String ID;
    private String defName;
    private String contextName;
    private String resKeyName;
    private long expirationTime;
    private String cacheScope;


    public EntryPointInstance(String ID, String contextName, String definitionName) {
        this.defName = contextName + "." + definitionName;
        this.ID = ID;
        this.contextName = contextName;
    }

    public EntryPointInstance(String ID, String contextName, String definitionName, String resKeyName) {
        this.defName = contextName + "." + definitionName;
        this.ID = ID;
        this.contextName = contextName;
        this.resKeyName = resKeyName;
    }

    public String getID() {
        return ID;
    }

    public String getDefName() {
        return defName;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getContextName() {
        return contextName;
    }

    public String getResKeyName() {
        return resKeyName;
    }

    public void setResKeyName(String resKeyName) {
        this.resKeyName = resKeyName;
    }

    public String getDisplayName(Locale locale) {
        // Todo get the localized display name for this entry point from the resource manager
        return resKeyName;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public void setCacheScope(String cacheScope) {
        this.cacheScope = cacheScope;
    }

    public String getCacheScope() {
        return cacheScope;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public boolean isUserInRole(JahiaUser user, String role) {
        // This method maps servlet roles on Jahia's groups
        return hasPermission(user, role);
    }

    public boolean isModeAllowed(JahiaUser user, String mode) {
        return hasPermission(user, mode);
    }

    private boolean hasPermission(JahiaUser user, String role) {
        try {
            JCRNodeWrapper node = ServicesRegistry.getInstance().getJCRStoreService().getNodeByUUID(ID, user);
            Map<String, List<String[]>> aclEntriesMap = node.getAclEntries();

            Set<String> principalSet = aclEntriesMap.keySet();
            for (String currentPrincipal : principalSet) {
                boolean isUser = currentPrincipal.indexOf("u:") == 0;
                String principalName = currentPrincipal.substring(2);

                // test if the principal is the user or if the user belongs to the principal (group)
                if ((isUser && principalName.equalsIgnoreCase(user.getUsername())) || user.isMemberOfGroup(Jahia.getThreadParamBean().getSiteID(), principalName)) {
                    List<String[]> principalPermValues = aclEntriesMap.get(currentPrincipal);
                    for (String[] currentPrincipalPerm : principalPermValues) {
                        String currentPrincipalPermValue = currentPrincipalPerm[1];
                        String currentPrincipalPermName = currentPrincipalPerm[2];
                        if (currentPrincipalPermName != null && currentPrincipalPermName.equalsIgnoreCase(role)) {
                            if (currentPrincipalPermValue != null && currentPrincipalPermValue.equalsIgnoreCase("GRANT")) {
                                return true;
                            }
                        }
                    }
                }

            }
            return false;
        } catch (Exception e) {
            logger.error(e,e);
            return false;
        }
    }

}
