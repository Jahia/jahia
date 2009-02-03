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
