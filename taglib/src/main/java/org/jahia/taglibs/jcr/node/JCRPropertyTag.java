/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
                        logger.debug("Property : " + name + " not defined in node " + node.getPath());
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
     * @param name
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
