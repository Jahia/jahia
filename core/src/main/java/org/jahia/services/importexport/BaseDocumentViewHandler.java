/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
