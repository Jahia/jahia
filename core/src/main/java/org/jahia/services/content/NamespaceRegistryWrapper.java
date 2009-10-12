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
