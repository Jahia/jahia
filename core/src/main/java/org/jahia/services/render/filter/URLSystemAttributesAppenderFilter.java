/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
