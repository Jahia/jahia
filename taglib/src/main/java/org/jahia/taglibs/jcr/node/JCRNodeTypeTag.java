/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.taglibs.jcr.node;

import org.slf4j.Logger;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeType;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;

/**
 * Tag for getting node type.
 *
 * @author cmailleux
 * @since Jahia 6.1
 */
public class JCRNodeTypeTag extends AbstractJahiaTag {
    private static final long serialVersionUID = 8871438422796392031L;
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRNodeTypeTag.class);
    private String var;
    private String name;
    private int scope = PageContext.PAGE_SCOPE;

    public void setName(String name) {
        this.name = name;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setScope(String scope) {
        this.scope = Util.getScope(scope);
    }

    /**
     * Default processing of the start tag, returning SKIP_BODY.
     *
     * @return SKIP_BODY
     * @throws javax.servlet.jsp.JspException if an error occurs while processing this tag
     * @see javax.servlet.jsp.tagext.Tag#doStartTag()
     */
    @Override
    public int doStartTag() throws JspException {
        try {
            NodeType type = NodeTypeRegistry.getInstance().getNodeType(name);
            pageContext.setAttribute(var, type, scope);
        } catch (NoSuchNodeTypeException e) {
            logger.warn(name + " is not a valid node type");
            return SKIP_BODY;
        }

        return EVAL_BODY_INCLUDE;
    }

    /**
     * Default processing of the end tag returning EVAL_PAGE.
     *
     * @return EVAL_PAGE
     * @throws javax.servlet.jsp.JspException if an error occurs while processing this tag
     * @see javax.servlet.jsp.tagext.Tag#doEndTag()
     */
    @Override
    public int doEndTag() throws JspException {
        resetState();
        return super.doEndTag();
    }

    @Override
    protected void resetState() {
        var = null;
        name = null;
        scope = PageContext.PAGE_SCOPE;
        super.resetState();
    }
}
