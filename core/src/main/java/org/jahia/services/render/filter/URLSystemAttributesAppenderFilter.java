/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.render.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author rincevent
 * @since JAHIA 6.5
 * Created : 23 sept. 2010
 */
public class URLSystemAttributesAppenderFilter extends AbstractFilter {
    private List<String> attributesToKeep;
    private HtmlTagAttributeTraverser traverser;
    private boolean alwaysIncludeAllParams=false;

    public void setAttributesToKeep(List<String> attributesToKeep) {
        this.attributesToKeep = attributesToKeep;
    }

    public void setAlwaysIncludeAllParams(boolean alwaysIncludeAllParams) {
        this.alwaysIncludeAllParams = alwaysIncludeAllParams;
    }

    @Override
    public String execute(String previousOut, final RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        if (alwaysIncludeAllParams || !Collections.disjoint(attributesToKeep, renderContext.getRequest().getParameterMap().keySet())) {
            previousOut = traverser.traverse(previousOut, renderContext, resource,
                    new HtmlTagAttributeTraverser.HtmlTagAttributeVisitor() {
                        public String visit(String value, RenderContext context, String tagName, String attrName, Resource resource) {
                            if (!value.startsWith("javascript:") && !value.startsWith("#") && !value.contains("##requestParameters##")) {
                                final String urlContext = renderContext.getURLGenerator().getContext();
                                final Map<String, String[]> parameterMap = renderContext.getRequest().getParameterMap();

                                StringBuilder newValue = new StringBuilder(30 * parameterMap.size());
                                newValue.append(value);

                                String separateChar = value.contains("?") ? "&" : "?";

                                for (String s : attributesToKeep) {
                                    if (s.contains("*")) {
                                        if (value.startsWith(urlContext) && !value.matches(s.replace("*", ".*="))) {
                                            for (String paramName : parameterMap.keySet()) {
                                                if (paramName.matches(s.replace("*", ".*"))) {
                                                    String parameter = renderContext.getRequest().getParameter(paramName);
                                                    if (parameter != null) {
                                                        newValue.append(separateChar).append(paramName).append("=").append(parameter);
                                                        separateChar = "&";
                                                    }
                                                }
                                            }
                                        }
                                    } else if (!value.contains(s + "=") && value.startsWith(urlContext)) {
                                        String parameter = renderContext.getRequest().getParameter(s);
                                        if (parameter != null) {
                                            newValue.append(separateChar).append(s).append("=").append(parameter);
                                            separateChar = "&";
                                        }
                                    }
                                }

                                return newValue.toString();
                            }
                            return value;
                        }
                    });
        }
        return previousOut;
    }

    public void setTraverser(HtmlTagAttributeTraverser traverser) {
        this.traverser = traverser;
    }
}
