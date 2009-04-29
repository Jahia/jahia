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
