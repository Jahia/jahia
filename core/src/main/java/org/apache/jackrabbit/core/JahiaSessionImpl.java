/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.apache.jackrabbit.core;

import org.apache.jackrabbit.core.config.WorkspaceConfig;
import org.apache.jackrabbit.core.security.JahiaAccessManager;
import org.apache.jackrabbit.core.security.authentication.AuthContext;

import javax.jcr.AccessDeniedException;
import javax.jcr.Item;
import javax.jcr.NamespaceException;
import javax.jcr.RepositoryException;
import javax.security.auth.Subject;

import java.util.HashMap;
import java.util.Map;

/**
 * Jackrabbit XASession extension for jahia
 */
public class JahiaSessionImpl extends XASessionImpl {
    private NodeTypeInstanceHandler myNtInstanceHandler;
    private Map<String, Object> jahiaAttributes;

    public JahiaSessionImpl(RepositoryContext repositoryContext, AuthContext loginContext, WorkspaceConfig wspConfig) throws AccessDeniedException, RepositoryException {
        super(repositoryContext, loginContext, wspConfig);
        init();
    }

    public JahiaSessionImpl(RepositoryContext repositoryContext, Subject subject, WorkspaceConfig wspConfig) throws AccessDeniedException, RepositoryException {
        super(repositoryContext, subject, wspConfig);
        init();
    }

    private void init() {
        jahiaAttributes = new HashMap<String, Object>();
        WorkspaceImpl ws = (WorkspaceImpl) getWorkspace();
        ws.versionMgr = new JahiaVersionManagerImpl(this.context, ws.stateMgr, ws.hierMgr);
    }

    // @Override

    public JahiaNodeTypeInstanceHandler getNodeTypeInstanceHandler() throws RepositoryException {
        if (myNtInstanceHandler == null) {
            myNtInstanceHandler = super.getNodeTypeInstanceHandler();
        }
        return (JahiaNodeTypeInstanceHandler) myNtInstanceHandler;
    }

    public String getPrefix(String uri) throws NamespaceException {
        try {
            return getNamespacePrefix(uri);
        } catch (NamespaceException e) {
            return repositoryContext.getNamespaceRegistry().getPrefix(uri);
        } catch (RepositoryException e) {
            throw new NamespaceException("Namespace not found: " + uri, e);
        }
    }

    public String getURI(String prefix) throws NamespaceException {
        try {
            return getNamespaceURI(prefix);
        } catch (NamespaceException e) {
            return repositoryContext.getNamespaceRegistry().getURI(prefix);
        } catch (RepositoryException e) {
            throw new NamespaceException("Namespace not found: " + prefix, e);
        }
    }

    public void setJahiaAttributes(String attributeName, Object attributeValue) {
        jahiaAttributes.put(attributeName, attributeValue);
    }

    public void toggleThisSessionAsAliased() {
        setJahiaAttributes("isAliasedUser", Boolean.TRUE);
        JahiaAccessManager accessManager = (JahiaAccessManager)context.getAccessManager();
        if (jahiaAttributes.containsKey("isAliasedUser") && (Boolean) jahiaAttributes.get("isAliasedUser")) {
            accessManager.setAliased(true);
        }
    }
    
    /**
     * Forces the removal of the node or property, skipping various checks, like constraints etc.
     * 
     * @param itemToRemove
     *            the item to be removed
     * @throws RepositoryException
     *             in case of a remove operation failure
     */
    public void removeItemForce(Item itemToRemove) throws RepositoryException {
        new ItemRemoveOperation((ItemImpl) itemToRemove, false).perform(this.context);
    }
}
