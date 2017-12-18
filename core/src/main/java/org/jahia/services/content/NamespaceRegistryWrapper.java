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
package org.jahia.services.content;

import javax.jcr.*;

import org.apache.jackrabbit.spi.Name;
import org.apache.jackrabbit.util.XMLChar;
import org.jahia.api.Constants;

import java.util.HashSet;
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
    private Map<String, String> nsToPrefix = new HashMap<String, String>();
    private Map<String, String> prefixToNs = new HashMap<String, String>();
    
    private static final HashSet<String> reservedPrefixes = new HashSet<String>();
    private static final HashSet<String> reservedURIs = new HashSet<String>();

    static {
        // reserved prefixes
        reservedPrefixes.add(Name.NS_XML_PREFIX);
        reservedPrefixes.add(Name.NS_XMLNS_PREFIX);
        // predefined (e.g. built-in) prefixes
        reservedPrefixes.add(Name.NS_REP_PREFIX);
        reservedPrefixes.add(Name.NS_JCR_PREFIX);
        reservedPrefixes.add(Name.NS_NT_PREFIX);
        reservedPrefixes.add(Name.NS_MIX_PREFIX);
        reservedPrefixes.add(Name.NS_SV_PREFIX);
        // reserved namespace URI's
        reservedURIs.add(Name.NS_XML_URI);
        reservedURIs.add(Name.NS_XMLNS_URI);
        // predefined (e.g. built-in) namespace URI's
        reservedURIs.add(Name.NS_REP_URI);
        reservedURIs.add(Name.NS_JCR_URI);
        reservedURIs.add(Name.NS_NT_URI);
        reservedURIs.add(Name.NS_MIX_URI);
        reservedURIs.add(Name.NS_SV_URI);
    }

    public NamespaceRegistryWrapper() {
        internalRegister(Constants.JAHIA_PREF, Constants.JAHIA_NS);
        internalRegister(Constants.JAHIANT_PREF, Constants.JAHIANT_NS);
        internalRegister(Constants.JAHIAMIX_PREF, Constants.JAHIAMIX_NS);
        internalRegister(Name.NS_NT_PREFIX, Name.NS_NT_URI);
        internalRegister(Name.NS_MIX_PREFIX, Name.NS_MIX_URI);
        internalRegister(Name.NS_JCR_PREFIX, Name.NS_JCR_URI);
        internalRegister(Name.NS_SV_PREFIX, Name.NS_SV_URI);
        internalRegister(Name.NS_EMPTY_PREFIX, Name.NS_DEFAULT_URI);
        internalRegister(Name.NS_XML_PREFIX, Name.NS_XML_URI);
        internalRegister(Name.NS_REP_PREFIX, Name.NS_REP_URI);        
    }

    public synchronized void registerNamespace(String prefix, String uri) throws NamespaceException, UnsupportedRepositoryOperationException, AccessDeniedException, RepositoryException {
        if (prefix == null || uri == null) {
            throw new IllegalArgumentException("prefix/uri can not be null");
        }        
        if (Name.NS_EMPTY_PREFIX.equals(prefix) || Name.NS_DEFAULT_URI.equals(uri)) {
            throw new NamespaceException("default namespace is reserved and can not be changed");
        }        
        if (prefixToNs.containsKey(prefix)) {
            throw new NamespaceException();
        }
        if (reservedURIs.contains(uri)) {
            throw new NamespaceException("failed to register namespace "
                    + prefix + " -> " + uri + ": reserved URI");
        }
        if (reservedPrefixes.contains(prefix)) {
            throw new NamespaceException("failed to register namespace "
                    + prefix + " -> " + uri + ": reserved prefix");
        }
        // special case: prefixes xml*
        if (prefix.toLowerCase().startsWith(Name.NS_XML_PREFIX)) {
            throw new NamespaceException("failed to register namespace "
                    + prefix + " -> " + uri + ": reserved prefix");
        }
        // check if the prefix is a valid XML prefix
        if (!XMLChar.isValidNCName(prefix)) {
            throw new NamespaceException("failed to register namespace "
                    + prefix + " -> " + uri + ": invalid prefix");
        }

        // check existing mappings
        String oldPrefix = nsToPrefix.get(uri);
        if (prefix.equals(oldPrefix)) {
            throw new NamespaceException("failed to register namespace "
                    + prefix + " -> " + uri + ": mapping already exists");
        }
        if (prefixToNs.containsKey(prefix)) {
            /**
             * prevent remapping of existing prefixes because this would in effect
             * remove the previously assigned namespace;
             * as we can't guarantee that there are no references to this namespace
             * (in names of nodes/properties/node types etc.) we simply don't allow it.
             */
            throw new NamespaceException("failed to register namespace "
                    + prefix + " -> " + uri
                    + ": remapping existing prefixes is not supported.");
        }        

        internalRegister(prefix, uri);
    }

    private void internalRegister(String prefix, String uri) {
        nsToPrefix.put(uri, prefix);
        prefixToNs.put(prefix, uri);
    }

    public void unregisterNamespace(String prefix) throws NamespaceException, UnsupportedRepositoryOperationException, AccessDeniedException, RepositoryException {
        if (reservedPrefixes.contains(prefix)) {
            throw new NamespaceException("reserved prefix: " + prefix);
        }
        if (!prefixToNs.containsKey(prefix)) {
            throw new NamespaceException("unknown prefix: " + prefix);
        }
        /**
         * as we can't guarantee that there are no references to the specified
         * namespace (in names of nodes/properties/node types etc.) we simply
         * don't allow it.
         */
        throw new NamespaceException("unregistering namespaces is not supported.");
    }

    public String[] getPrefixes() throws RepositoryException {
        return (String[]) prefixToNs.keySet().toArray(new String[prefixToNs.size()]);
    }

    public String[] getURIs() throws RepositoryException {
        return (String[]) nsToPrefix.keySet().toArray(new String[nsToPrefix.size()]);
    }

    public String getURI(String prefix) throws NamespaceException, RepositoryException {
        if (!prefixToNs.containsKey(prefix)) {
            throw new NamespaceException("Prefix "+ prefix +" is not a registered namespace prefix in this system");
        }
        return prefixToNs.get(prefix);
    }

    public String getPrefix(String uri) throws NamespaceException, RepositoryException {
        if (!nsToPrefix.containsKey(uri)) {
            throw new NamespaceException("URI "+ uri +" is not a registered namespace URI in this system");
        }
        return nsToPrefix.get(uri);
    }

}
