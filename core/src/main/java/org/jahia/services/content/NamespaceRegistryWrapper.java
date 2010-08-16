/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content;

import javax.jcr.*;
import org.jahia.api.Constants;
import java.util.Map;
import java.util.HashMap;

/**
 * Jahia's implementation of the JCR <code>javax.jcr.NamespaceRegistry</code>. 
 * The namespace registry contains the default prefixes of the registered
 * namespaces. 
 *
 * @author toto
 */
public class NamespaceRegistryWrapper implements NamespaceRegistry {
    private Map nsToPrefix = new HashMap();
    private Map prefixToNs = new HashMap();

    public NamespaceRegistryWrapper() {
        internalRegister(Constants.JAHIA_PREF, Constants.JAHIA_NS);
        internalRegister(Constants.JAHIANT_PREF, Constants.JAHIANT_NS);
        internalRegister(Constants.JAHIAMIX_PREF, org.jahia.api.Constants.JAHIAMIX_NS);
        internalRegister(Constants.NT_PREF, org.jahia.api.Constants.NT_NS);
        internalRegister(Constants.MIX_PREF, org.jahia.api.Constants.MIX_NS);
        internalRegister(Constants.JCR_PREF, org.jahia.api.Constants.JCR_NS);
//        internalRegister(Constants.XML_PREF, org.jahia.api.Constants.XML_NS);
    }

    public void registerNamespace(String prefix, String uri) throws NamespaceException, UnsupportedRepositoryOperationException, AccessDeniedException, RepositoryException {
        if (prefixToNs.containsKey(prefix)) {
            throw new NamespaceException();
        }

        internalRegister(uri,prefix);
    }

    private void internalRegister(String prefix, String uri) {
        nsToPrefix.put(uri, prefix);
        prefixToNs.put(prefix, uri);
    }

    public void unregisterNamespace(String prefix) throws NamespaceException, UnsupportedRepositoryOperationException, AccessDeniedException, RepositoryException {
        if (!prefixToNs.containsKey(prefix)) {
            throw new NamespaceException();
        }
        Object k = prefixToNs.remove(prefix);
        nsToPrefix.remove(k);
    }

    public String[] getPrefixes() throws RepositoryException {
        return (String[]) prefixToNs.keySet().toArray(new String[prefixToNs.size()]);
    }

    public String[] getURIs() throws RepositoryException {
        return (String[]) nsToPrefix.keySet().toArray(new String[nsToPrefix.size()]);
    }

    public String getURI(String s) throws NamespaceException, RepositoryException {
        if (!prefixToNs.containsKey(s)) {
            throw new NamespaceException();
        }
        return (String) prefixToNs.get(s);
    }

    public String getPrefix(String s) throws NamespaceException, RepositoryException {
        if (!nsToPrefix.containsKey(s)) {
            throw new NamespaceException();
        }
        return (String) nsToPrefix.get(s);
    }

}
