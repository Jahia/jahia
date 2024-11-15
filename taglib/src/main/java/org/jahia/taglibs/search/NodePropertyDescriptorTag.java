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
package org.jahia.taglibs.search;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

import org.jahia.services.search.SearchCriteria.NodePropertyDescriptor;
import org.jahia.services.search.SearchCriteriaFactory;
import org.jahia.taglibs.AbstractJahiaTag;

/**
 * Exposes a descriptor for the specified node property into the page scope.
 * The descriptor contains information about property type (boolean, text, date,
 * category), if it is constrained with a list of allowed values etc.
 *
 * @author Sergiy Shyrkov
 */
@SuppressWarnings("serial")
public class NodePropertyDescriptorTag extends AbstractJahiaTag {

    private static final String DEF_VAR = "descriptor";

    private String nodeType;

    private String name;

    private String var = DEF_VAR;

    @Override
    public int doEndTag() throws JspException {
        pageContext.removeAttribute(var, PageContext.PAGE_SCOPE);
        resetState();

        return EVAL_PAGE;
    }

    @Override
    public int doStartTag() throws JspException {
        NodePropertyDescriptor descriptor = null;
        try {
            descriptor = SearchCriteriaFactory.getPropertyDescriptor(nodeType, name, getCurrentResource().getLocale());
            if (descriptor != null) {
                pageContext.setAttribute(var, descriptor);
            }
        } catch (RepositoryException e) {
            throw new JspTagException(e);
        }

        return descriptor != null ? EVAL_BODY_INCLUDE : SKIP_BODY;
    }

    @Override
    protected void resetState() {
        var = DEF_VAR;
        nodeType = null;
        name = null;
        super.resetState();
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVar(String var) {
        this.var = var;
    }

}
