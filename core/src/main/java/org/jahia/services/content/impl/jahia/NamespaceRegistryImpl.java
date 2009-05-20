/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.content.impl.jahia;

import javax.jcr.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 8 févr. 2008
 * Time: 15:30:48
 * To change this template use File | Settings | File Templates.
 */
public class NamespaceRegistryImpl implements NamespaceRegistry {
    private Map nsToPrefix = new HashMap();
    private Map prefixToNs = new HashMap();

    public void registerNamespace(String s, String s1) throws NamespaceException, UnsupportedRepositoryOperationException, AccessDeniedException, RepositoryException {
        nsToPrefix.put(s1,s);
        prefixToNs.put(s,s1);
    }

    public void unregisterNamespace(String s) throws NamespaceException, UnsupportedRepositoryOperationException, AccessDeniedException, RepositoryException {
        Object k = prefixToNs.remove(s);
        nsToPrefix.remove(k);
    }

    public String[] getPrefixes() throws RepositoryException {
        return (String[]) prefixToNs.keySet().toArray(new String[prefixToNs.size()]);
    }

    public String[] getURIs() throws RepositoryException {
        return (String[]) nsToPrefix.keySet().toArray(new String[nsToPrefix.size()]);
    }

    public String getURI(String s) throws NamespaceException, RepositoryException {
        return (String) prefixToNs.get(s);
    }

    public String getPrefix(String s) throws NamespaceException, RepositoryException {
        return (String) nsToPrefix.get(s);
    }
}
