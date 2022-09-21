/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.jackrabbit.core;

import org.apache.jackrabbit.core.config.WorkspaceConfig;
import org.apache.jackrabbit.core.security.JahiaAccessManager;
import org.apache.jackrabbit.core.security.authentication.AuthContext;
import org.apache.jackrabbit.core.session.SessionContext;

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
    private final Map<String, Object> jahiaAttributes = new HashMap<>();
    private NodeTypeInstanceHandler myNtInstanceHandler;

    public JahiaSessionImpl(RepositoryContext repositoryContext, AuthContext loginContext, WorkspaceConfig wspConfig) throws AccessDeniedException, RepositoryException {
        super(repositoryContext, loginContext, wspConfig);
        init();
    }

    public JahiaSessionImpl(RepositoryContext repositoryContext, Subject subject, WorkspaceConfig wspConfig) throws AccessDeniedException, RepositoryException {
        super(repositoryContext, subject, wspConfig);
        init();
    }

    private void init() {
        WorkspaceImpl ws = this.context.getWorkspace();
        ws.versionMgr = new JahiaVersionManagerImpl(this.context, ws.stateMgr, ws.hierMgr);
    }

    @Override
    public JahiaNodeTypeInstanceHandler getNodeTypeInstanceHandler() throws RepositoryException {
        if (myNtInstanceHandler == null) {
            myNtInstanceHandler = super.getNodeTypeInstanceHandler();
        }

        return (JahiaNodeTypeInstanceHandler) myNtInstanceHandler;
    }

    @Override
    public String getPrefix(String uri) throws NamespaceException {
        try {
            return getNamespacePrefix(uri);
        } catch (NamespaceException e) {
            return repositoryContext.getNamespaceRegistry().getPrefix(uri);
        } catch (RepositoryException e) {
            throw new NamespaceException("Namespace not found: " + uri, e);
        }
    }

    @Override
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

    /**
     * Get the internal sessionContext
     * @return SessionContext
     */
    public SessionContext getContext() {
        return this.context;
    }

}
