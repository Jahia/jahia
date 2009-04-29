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
package org.jahia.taglibs.search;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;

import org.jahia.engines.search.SearchCriteriaFactory;
import org.jahia.engines.search.SearchCriteria.DocumentPropertyDescriptor;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.utility.Utils;

/**
 * Exposes a descriptor for the specified document property into the page scope.
 * The descriptor contains information about property type (boolean, text, date,
 * category), if it is constrained with a list of allowed values etc.
 * 
 * @author Sergiy Shyrkov
 */
@SuppressWarnings("serial")
public class DocumentPropertyDescriptorTag extends AbstractJahiaTag {

    private static final String DEF_VAR = "descriptor";

    private String documentType;

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
        DocumentPropertyDescriptor descriptor = null;
        try {
            descriptor = SearchCriteriaFactory.getPropertyDescriptor(
                    documentType, name, Utils.getProcessingContext(pageContext));
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
        documentType = null;
        name = null;
        super.resetState();
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVar(String var) {
        this.var = var;
    }

}
