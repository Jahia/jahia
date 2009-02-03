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

package org.jahia.services.content.nodetypes;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 14 mars 2008
 * Time: 18:43:40
 * To change this template use File | Settings | File Templates.
 */
public class Name {
    private String localName;
    private String prefix;
    private String uri;

    public Name(String localName, String prefix, String uri) {
        this.localName = localName;
        this.prefix = prefix;
        this.uri = uri;
    }

    public Name(String qualifiedName, Map<String,String> namespaceMapping) {
        String s[] = qualifiedName.split(":");
        if (s.length == 2) {
            prefix = s[0];
            localName = s[1];
            uri = namespaceMapping.get(prefix);
        } else {
            prefix = "";
            localName = s[0];
            uri = namespaceMapping.get("");
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
        if (prefix.equals("")) {
            return localName;
        } else {
            return prefix + ":" + localName;
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
        int result;
        result = (localName != null ? localName.hashCode() : 0);
        result = 31 * result + (prefix != null ? prefix.hashCode() : 0);
        result = 31 * result + (uri != null ? uri.hashCode() : 0);
        return result;
    }
}
