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

package org.jahia.taglibs.template.include;

import org.jahia.services.render.*;
import org.slf4j.Logger;
import org.apache.taglibs.standard.tag.common.core.ParamParent;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.scripting.Script;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * 
 *
 * @author : rincevent
 * @since JAHIA 6.5
 *        Created : 27 oct. 2009
 */
public class OptionTag extends BodyTagSupport implements ParamParent {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(OptionTag.class);
    private String nodetype;
    private JCRNodeWrapper node;
    private String view;
    private Map<String, String> parameters = new HashMap<String, String>();

    /**
     * Default processing of the end tag returning EVAL_PAGE.
     *
     * @return EVAL_PAGE
     * @throws javax.servlet.jsp.JspException if an error occurred while processing this tag
     * @see javax.servlet.jsp.tagext.Tag#doEndTag
     */
    @Override
    public int doEndTag() throws JspException {
        try {
            String charset = pageContext.getResponse().getCharacterEncoding();
            // Todo test if module is active
            RenderContext renderContext = (RenderContext) pageContext.getAttribute("renderContext",
                                                                                   PageContext.REQUEST_SCOPE);
            Resource currentResource = (Resource) pageContext.getAttribute("currentResource",
                                                                           PageContext.REQUEST_SCOPE);
            String[] nodetypes = nodetype.split(",");
            if (node.isNodeType(nodetypes[0])) {
                ExtendedNodeType mixinNodeType = NodeTypeRegistry.getInstance().getNodeType(nodetypes[0]);
                if (pageContext.getAttribute("optionsAutoRendering", PageContext.REQUEST_SCOPE) == null) {
                    currentResource.removeOption(mixinNodeType);
                }
                Resource wrappedResource = new Resource(node, currentResource.getTemplateType(), view,
                        Resource.CONFIGURATION_INCLUDE);
                wrappedResource.setResourceNodeType(mixinNodeType);
                for (Map.Entry<String, String> param : parameters.entrySet()) {
                    wrappedResource.getModuleParams().put(URLDecoder.decode(param.getKey(), charset), URLDecoder.decode(
                            param.getValue(), charset));
                }

                Script script = null;
                try {
                    script = RenderService.getInstance().resolveScript(wrappedResource, renderContext);
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                } catch (TemplateNotFoundException e) {
                    if(nodetypes.length>1) {
                        mixinNodeType = NodeTypeRegistry.getInstance().getNodeType(nodetypes[1]);
                        wrappedResource.setResourceNodeType(mixinNodeType);
                        script = RenderService.getInstance().resolveScript(wrappedResource, renderContext);
                    }
                }
                if(script!=null) {
                Object attribute = pageContext.getRequest().getAttribute("currentNode");
                pageContext.getRequest().setAttribute("currentNode",node);
                pageContext.getRequest().setAttribute("currentResource",wrappedResource);
                pageContext.getOut().write(script.execute(wrappedResource, renderContext));
                pageContext.getRequest().setAttribute("currentNode",attribute);
                pageContext.getRequest().setAttribute("currentResource",currentResource);
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            throw new JspException(e);
        } catch (RenderException e) {
            logger.error(e.getMessage(), e);
            throw new JspException(e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new JspException(e);
        }
        nodetype = null;
        node = null;
        view = null;
        parameters.clear();
        return super.doEndTag();
    }

    public void setNodetype(String nodetype) {
        this.nodetype = nodetype;
    }

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    public void setView(String view) {
        this.view = view;
    }

    public void addParameter(String name, String value) {
        parameters.put(name, value);
    }
}
