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
package org.jahia.ajax.gwt.client.data.opensearch;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 19 oct. 2007
 * Time: 11:09:52
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaParameterName implements Serializable {

    private String name = "";
    private GWTJahiaNamespace namespace = GWTJahiaNamespace.OPEN_SEARCH_NS;

    public GWTJahiaParameterName() {
    }

    public GWTJahiaParameterName(String name) {
        this.name = name;
    }

    public GWTJahiaParameterName(String name, GWTJahiaNamespace namespace) {
        this(name);
        this.namespace = namespace;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GWTJahiaNamespace getNamespace() {
        return namespace;
    }

    public void setNamespace(GWTJahiaNamespace namespace) {
        this.namespace = namespace;
    }

    public static GWTJahiaParameterName getParameterName(String name, Map<String, GWTJahiaNamespace> namespaces){
        if ( name.indexOf(":") == -1 ){
            return new GWTJahiaParameterName(name);
        }
        String[] tokens = name.split(":");
        if ( tokens.length> 1 ){
            GWTJahiaNamespace namespace = namespaces.get(tokens[0]);
            return new GWTJahiaParameterName(tokens[1],namespace);
        } else {
            return new GWTJahiaParameterName(name);
        }
    }
}
