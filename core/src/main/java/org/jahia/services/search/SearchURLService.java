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
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.search;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.lang.StringUtils;

public class SearchURLService {

    private static volatile SearchURLService instance;

    private SearchURLService() {
    }

    public static SearchURLService getInstance() {
        if (instance == null) {
            synchronized (SearchURLService.class) {
                if (instance == null) {
                    instance = new SearchURLService();
                }
            }
        }
        return instance;
    }

    public void addURLQueryParameter(AbstractHit<?> searchHit, String parameterName,
            String parameterValue) {
        searchHit.setQueryParameter(appendParams(searchHit.getQueryParameter(), parameterName + "=" + encode(parameterValue)));
        return;
    }

    public void updateHitLinkTemplateType (AbstractHit<?> searchHit, String templateType) {
        searchHit.setLinkTemplateType(templateType);
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
