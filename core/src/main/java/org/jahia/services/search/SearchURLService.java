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
package org.jahia.services.search;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;

public class SearchURLService {
    private static SearchURLService instance;

    private SearchURLService() {
        super();
    }

    public static SearchURLService getInstance() {
        if (instance == null) {
            instance = new SearchURLService();
        }
        return instance;
    }


    public void addURLQueryParameter(AbstractHit<?> searchHit, String parameterName,
            String parameterValue) {
        searchHit.setQueryParameter(appendParams(searchHit.getQueryParameter(), parameterName + "=" + encode(parameterValue)));        
        return;
    }

    private String encode(String parameterValue) {
        String value;
        try {
            value = URLEncoder.encode(parameterValue, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
        return value;
    }

    protected String appendParams(String queryParam, String newParams) {
        if (!StringUtils.isEmpty(newParams)) {
            if (queryParam == null){
                queryParam = "";
            }
            if (queryParam.indexOf("?") == -1) {
                if (newParams.startsWith("&")) {
                    newParams = "?" + newParams.substring(1, newParams.length());
                } else if (!newParams.startsWith("?") && !newParams.startsWith("/")) {
                    newParams = "?" + newParams;
                }
            } else if (!newParams.startsWith("&") && !newParams.startsWith("/")) {
                newParams = "&" + newParams;
            }
            queryParam += newParams;
        }
        return queryParam;
    }
}
