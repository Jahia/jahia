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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.templates.components.opensearch.client.model;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 19 oct. 2007
 * Time: 11:09:52
 * To change this template use File | Settings | File Templates.
 */
public class ParameterName implements Serializable {

    private String name = "";
    private Namespace namespace = Namespace.OPEN_SEARCH_NS;

    public ParameterName() {
    }

    public ParameterName(String name) {
        this.name = name;
    }

    public ParameterName(String name, Namespace namespace) {
        this(name);
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Namespace getNamespace() {
        return namespace;
    }

    public void setNamespace(Namespace namespace) {
        this.namespace = namespace;
    }

    public static ParameterName getParameterName(String name, Map<String, Namespace> namespaces){
        if ( name.indexOf(":") == -1 ){
            return new ParameterName(name);
        }
        String[] tokens = name.split(":");
        if ( tokens.length> 1 ){
            Namespace namespace = namespaces.get(tokens[0]);
            return new ParameterName(tokens[1],namespace);
        } else {
            return new ParameterName(name);
        }
    }
}
