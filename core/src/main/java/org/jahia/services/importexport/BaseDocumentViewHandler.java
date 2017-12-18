/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.importexport;

import java.util.Set;
import java.util.Stack;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;

import org.jahia.services.content.JCRSessionWrapper;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.collect.ImmutableSet;

/**
 * Base class for SAX-based handlers for JCR content in a document format.
 * 
 * @author Sergiy Shyrkov
 * @since Jahia 6.6
 */
public abstract class BaseDocumentViewHandler extends DefaultHandler {
    
    protected Stack<String> pathes = new Stack<String>();

    protected JCRSessionWrapper session;

    public BaseDocumentViewHandler() {
        pathes.add("");
    }

    public BaseDocumentViewHandler(JCRSessionWrapper session) {
        this();
        setSession(session);
    }

    public void setSession(JCRSessionWrapper session) {
        this.session = session;
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        try {
            NamespaceRegistry nsRegistry = session.getWorkspace().getNamespaceRegistry();
            Set<String> prefixes = ImmutableSet.copyOf(nsRegistry.getPrefixes());
            if (!prefixes.contains(prefix)) {
                nsRegistry.registerNamespace(prefix, uri);
                session.setNamespacePrefix(prefix, uri);
            }
        } catch (RepositoryException re) {
            throw new SAXException(re);
        }
    }

    public Stack<String> getPathes() {
        return pathes;
    }

}
