/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.taglibs.template;

import javax.servlet.jsp.JspException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Pattern;

/**
 * JSP tag for creating URLs. Inheriting from the JSTL c:url tag, but adds the possibility
 * to create the query string with the parameters of the current request (with ability to set
 * filters)
 *
 * @author Benjamin Papez
 *
 */
public class UrlTag extends org.apache.taglibs.standard.tag.el.core.UrlTag {
    /** The serialVersionUID. */
    private static final long serialVersionUID = 6171288389547456742L;

    private Set<String> addedParams;
    
    private Set<String> excludeParams;
    
    private Pattern paramIncludePattern;
    
    private boolean useRequestParams = false;

    /**
     * Initializes an instance of this class.
     */
    public UrlTag() {
        super();
        init();
    }

    public void setExcludeParams(String excludeParams) {
        this.excludeParams = new HashSet<String>(Arrays.asList(excludeParams.replace(" ", "").split(",")));
    }

    public void setParamIncludeRegex(String paramIncludeRegex) {
        this.paramIncludePattern = Pattern.compile(paramIncludeRegex);
    }
    
    public void setUseRequestParams(boolean useRequestParams) {
        this.useRequestParams = useRequestParams;
    }

    @Override
    public int doEndTag() throws JspException {
        if (useRequestParams) {
            for (Iterator<Map.Entry<String, String[]>> it = pageContext
                    .getRequest().getParameterMap().entrySet().iterator(); it
                    .hasNext();) {
                Map.Entry<String, String[]> requestParameter = it.next();
                if ((addedParams == null || !addedParams
                        .contains(requestParameter.getKey()))
                        && (excludeParams == null || !excludeParams
                                .contains(requestParameter.getKey()))
                        && (paramIncludePattern == null || paramIncludePattern
                                .matcher(requestParameter.getKey()).matches())) {
                    for (String value : requestParameter.getValue()) {
                        String enc = pageContext.getResponse().getCharacterEncoding();
                        try {
                            super.addParameter(URLEncoder.encode(requestParameter.getKey(), enc), URLEncoder.encode(value, enc));
                        } catch (UnsupportedEncodingException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        
        return super.doEndTag();
    }

    @Override
    public void addParameter(String name, String value) {
        if (addedParams == null) {
            addedParams = new HashSet<String>();
        }
        addedParams.add(name);
        super.addParameter(name, value);
    }

    @Override
    // Releases any resources we may have (or inherit)
    public void release() {
        super.release();
        init();
    }

    // (re)initializes state (during release() or construction)
    private void init() {
        // null implies "no expression"
        addedParams = null;
        excludeParams = null;
        paramIncludePattern = null;
        useRequestParams = false;
    }
    
}
