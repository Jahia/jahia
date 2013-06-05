/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.content.nodetypes;

import java.util.Map;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.lang.StringUtils;

/**
 * User: toto
 * Date: 14 mars 2008
 * Time: 18:43:40
 */
public class Name {
    private String localName;
    private String prefix;
    private String uri;

    private String preComputedToString;
    private int preComputedHashCode;

    private static String getPrefix(String uri, Map<String, String> namespaceMapping) {
        if (uri == null || uri.length() == 0) {
            return null;
        }
        String p = null;
        if (namespaceMapping instanceof BidiMap) {
            p = (String) ((BidiMap) namespaceMapping).getKey(uri);
        } else {
            for (Map.Entry<String, String> entry : namespaceMapping.entrySet()) {
                if (entry.getValue().equals(uri)) {
                    p = entry.getKey();
                    break;
                }
            }
        }
        
        return p;
    }

    public Name(String localName, String prefix, String uri) {
        this.localName = localName;
        this.prefix = prefix;
        this.uri = uri;
    }

    public Name(String localName, String uri) {
        this(localName, getPrefix(uri, NodeTypeRegistry.getInstance().getNamespaces()), uri);
    }
    
    public Name(String localName, String uri, Map<String,String> namespaceMapping) {
        this(localName, getPrefix(uri, namespaceMapping), uri);
    }
    
    public Name(String qualifiedName, Map<String,String> namespaceMapping) {
        if (qualifiedName.startsWith("{")) {
            int endUri = qualifiedName.indexOf("}");
            if (endUri != -1 && qualifiedName.length() > endUri) {
                uri = StringUtils.substringBetween(qualifiedName, "{", "}");
                prefix = getPrefix(uri, namespaceMapping);
                localName = qualifiedName.substring(endUri + 1);
            } else {
                localName = qualifiedName;
                uri = namespaceMapping.get("");
            }
        }
        if (localName == null) {
            String s[] = StringUtils.split(qualifiedName, ":");
            if (s.length == 2) {
                prefix = s[0];
                localName = s[1];
                uri = namespaceMapping.get(prefix);
            } else {
                localName = s[0];
                uri = namespaceMapping.get("");
            }
        }
    }

    public String getLocalName() {
        return localName;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getUri() {
        return uri;
    }

    public String toString() {
        if (preComputedToString != null) {
            return preComputedToString;
        }
        if (prefix == null || prefix.length() == 0) {
            preComputedToString = localName;
            return preComputedToString;
        } else {
            preComputedToString = prefix + ":" + localName;
            return preComputedToString;
        }
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Name name = (Name) o;

        if (localName != null ? !localName.equals(name.localName) : name.localName != null) return false;
        if (uri != null ? !uri.equals(name.uri) : name.uri != null) return false;

        return true;
    }

    public int hashCode() {
        if (preComputedHashCode != 0) {
            return preComputedHashCode;
        }
        int result;
        result = (localName != null ? localName.hashCode() : 0);
        result = 31 * result + (prefix != null ? prefix.hashCode() : 0);
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        preComputedHashCode = result;
        return preComputedHashCode;
    }
}
