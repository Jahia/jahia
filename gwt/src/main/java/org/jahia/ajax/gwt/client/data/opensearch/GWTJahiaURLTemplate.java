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

package org.jahia.ajax.gwt.client.data.opensearch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hollis
 * Date: 19 oct. 2007
 * Time: 11:18:39
 * To change this template use File | Settings | File Templates.
 */
public class GWTJahiaURLTemplate implements Serializable {

    private final String SEARCH_TERMS = "{searchTerms}";
    private String template;
    private String searchURL;
    private List<GWTJahiaParameter> parameters = new ArrayList<GWTJahiaParameter>();

    public GWTJahiaURLTemplate() {
    }

    public GWTJahiaURLTemplate(String template, Map<String, GWTJahiaNamespace> namespaces) {
        this.template = template;
        parseParameters(namespaces);
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public List<GWTJahiaParameter> getParameters() {
        return parameters;
    }

    private void parseParameters(Map<String, GWTJahiaNamespace> namespaces){
        this.searchURL = template;
        this.parameters = new ArrayList<GWTJahiaParameter>();
        int pos = template.indexOf("?");
        if ( pos != -1 ){
            this.searchURL = template.substring(0,pos);
            String queryString = template.substring(pos+1);
            String[] tokens = queryString.split("&");
            String token = null;
            GWTJahiaParameter parameter = null;
            GWTJahiaParameterName parameterName = null;
            for (int i=0; i<tokens.length; i++){
                token = tokens[i];
                String[] paramTokens = token.split("=");
                parameterName = GWTJahiaParameterName.getParameterName(paramTokens[0],namespaces);
                parameter = new GWTJahiaParameter(parameterName,paramTokens[1]);
                this.parameters.add(parameter);
            }
        }
    }

    public String getSearchURL(String searchTerm){
        String rewrittedURL = this.searchURL;
        if (!rewrittedURL.endsWith("?")){
            rewrittedURL+="?";
        }
        int nbParameter = 0;
        for (GWTJahiaParameter parameter : this.parameters){
            if (nbParameter > 0){
                rewrittedURL +="&";
            }
            String parameterValue = parameter.getValue();
            if (parameterValue.equals(SEARCH_TERMS)){
                parameterValue = searchTerm;
            }
            rewrittedURL += parameter.getName().getName() + "=" +  parameterValue;
            nbParameter++;
        }
        return rewrittedURL;
    }

}
