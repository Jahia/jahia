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
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
    private String prefix = StringUtils.EMPTY;
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

        return StringUtils.defaultString(p);
    }

    public Name(String localName, String prefix, String uri) {
        this.localName = localName;
        this.prefix = StringUtils.defaultString(prefix);
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
        prefix = StringUtils.defaultString(prefix);
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
