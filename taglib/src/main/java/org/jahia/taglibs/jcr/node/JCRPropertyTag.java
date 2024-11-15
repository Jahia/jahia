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
package org.jahia.taglibs.jcr.node;

import org.slf4j.Logger;
import org.apache.taglibs.standard.tag.common.core.Util;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.io.IOException;

/**
 * This Tag allows access to specific property of a node.
 * <p/>
 *
 * @author cmailleux
 */
public class JCRPropertyTag extends AbstractJahiaTag {
    private static final long serialVersionUID = 6088194510339167289L;
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(JCRPropertyTag.class);
    private JCRNodeWrapper node;
    private String name;
    private String var;
    private boolean inherited = false;
    private int scope = PageContext.PAGE_SCOPE;

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
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
        int returnValue = SKIP_BODY;
        try {
            if (var != null)
                pageContext.removeAttribute(var);
        } catch (IllegalStateException e) {
            logger.debug("Cannot remove property",e);
        }
        JCRNodeWrapper curNode = node;
        while (true) {
            try {
                if (curNode == null) {
                    return returnValue;
                }
                if (curNode.hasProperty(name)) {
                    Property property = curNode.getProperty(name);
                    if (property != null) {
                        if (var != null) {
                            returnValue = EVAL_BODY_INCLUDE;
                            if (property.getDefinition().isMultiple()) {
                                pageContext.setAttribute(var, property.getValues(), scope);
                            } else {
                                pageContext.setAttribute(var, property.getValue(), scope);
                            }
                        } else {
                            if (!property.getDefinition().isMultiple()) {
                                pageContext.getOut().print(property.getValue().getString());
                            }
                        }
                        return returnValue;
                    }
                } else {
                    if (!inherited) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Property : {} not defined in node {}", name, node.getPath());
                        }
                        return returnValue;
                    } else {
                        try {
                            if ("/".equals(curNode.getPath())) {
                                return returnValue;
                            }
                            curNode = curNode.getParent();
                        } catch (ItemNotFoundException e2) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Property {} not found in parent nodes {}", name, node.getPath());
                            }
                            return returnValue;
                        } catch (AccessDeniedException e1) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Property {} parent access denied {}", name, node.getPath());
                            }
                            return returnValue;
                        }
                    }
                }
            } catch (ConstraintViolationException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Property : " + name + " not found in node " + node.getPath());
                }
                return returnValue;
            } catch (NoSuchNodeTypeException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Property : " + name + " not found in node " + node.getPath());
                }
                return returnValue;
            } catch (PathNotFoundException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Property : " + name + " not found in node " + node.getPath());
                }
                return returnValue;
            } catch (RepositoryException e) {
                throw new JspException(e);
            } catch (IOException e) {
                throw new JspException(e);
            }
        }
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
        return EVAL_PAGE;
    }

    /**
     * Specify the name of the property you want to get value of.
     *
     * @param name the name of the property you want to get value of
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * If you do not want to output directly the value of the property (call javax.jcr.Value.getString())
     * The define a value for this.
     *
     * @param var The name in the pageContext in which you will find the javax.jcr.Value or javax.jcr.Value[] object associated with this property
     */
    public void setVar(String var) {
        this.var = var;
    }

    /**
     * If you want to recursively look for a property in current node parent's nodes
     * Set this value to true
     *
     * @param inherited (false is default) if true look up in parents properties
     */

    public void setInherited(boolean inherited) {
        this.inherited = inherited;
    }

    @Override
    protected void resetState() {
        node = null;
        name = null;
        inherited = false;
        var = null;
        scope = PageContext.PAGE_SCOPE;
        super.resetState();
    }
}
