/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.apache.jackrabbit.core;

import org.apache.jackrabbit.core.config.WorkspaceConfig;
import org.apache.jackrabbit.core.security.JahiaAccessManager;
import org.apache.jackrabbit.core.security.authentication.AuthContext;

import javax.jcr.AccessDeniedException;
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
}
