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
package org.jahia.taglibs.template.include;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * Tag handler for used in conjunction with {@link IncludeTag} to pass
 * additional parameters.
 * 
 * @author Sergiy Shyrkov
 * @see IncludeTag
 */
@SuppressWarnings("serial")
public class ParamTag extends TagSupport {

    private String name;

    private String value;

    /**
     * Initializes an instance of this class.
     */
    public ParamTag() {
        super();
        init();
    }

    @Override
    public int doStartTag() throws JspException {
        IncludeTag includeTag = (IncludeTag) findAncestorWithClass(this,
                IncludeTag.class);
        if (includeTag == null)
            throw new JspTagException(
                    "ParamTag must be nested inside IncludeTag");

        includeTag.addParameter(name, value);

        return SKIP_BODY;
    }

    private void init() {
        name = null;
        value = null;
    }

    @Override
    public void release() {
        init();
        super.release();
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
