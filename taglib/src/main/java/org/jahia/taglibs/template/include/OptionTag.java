/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2015 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 */
package org.jahia.taglibs.template.include;

import org.apache.commons.lang.StringUtils;
import org.apache.taglibs.standard.tag.common.core.ParamParent;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.*;
import org.jahia.services.render.scripting.Script;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Includes an option as a module into the page. The nodetype of the option to render is defined by nodetype attribute.
 * The node to display is determined by node attribute. template attribute determines the way the node is rendered.
 *
 * @author : rincevent
 * @since JAHIA 6.5 Created : 27 oct. 2009
 */
public class OptionTag extends BodyTagSupport implements ParamParent {
    private static final long serialVersionUID = -4688234914421053917L;
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(OptionTag.class);
    private String nodetype;
    private JCRNodeWrapper node;
    private String view;
    private transient Map<String, String> parameters = new HashMap<String, String>();

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
            renderNodeWithViewAndTypes(node, view, nodetype, pageContext, parameters);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new JspException(e);
        }

        nodetype = null;
        node = null;
        view = null;
        parameters.clear();
        return super.doEndTag();
    }

    public static void renderNodeWithViewAndTypes(JCRNodeWrapper node, String view, String commaConcatenatedNodeTypes, PageContext pageContext, Map<String, String> parameters) throws RepositoryException, IOException, RenderException {
        String charset = pageContext.getResponse().getCharacterEncoding();
        // Todo test if module is active
        RenderContext renderContext = (RenderContext) pageContext.getAttribute("renderContext", PageContext.REQUEST_SCOPE);
        Resource currentResource = (Resource) pageContext.getAttribute("currentResource", PageContext.REQUEST_SCOPE);
        String[] nodeTypes = StringUtils.split(commaConcatenatedNodeTypes, ",");

        if (nodeTypes.length > 0) {
            final String primaryNodeType = nodeTypes[0];

            if (node.isNodeType(primaryNodeType)) {
                ExtendedNodeType mixinNodeType = NodeTypeRegistry.getInstance().getNodeType(primaryNodeType);

                // what is this for? This doesn't seem to be used anywhere else in the code
                if (pageContext.getAttribute("optionsAutoRendering", PageContext.REQUEST_SCOPE) == null) {
                    currentResource.removeOption(mixinNodeType);
                }

                // create a resource to render the current node with the specified view
                Resource wrappedResource = new Resource(node, currentResource.getTemplateType(), view, Resource.CONFIGURATION_INCLUDE);
                wrappedResource.setResourceNodeType(mixinNodeType);

                // set parameters
                for (Map.Entry<String, String> param : parameters.entrySet()) {
                    wrappedResource.getModuleParams().put(URLDecoder.decode(param.getKey(), charset), URLDecoder.decode(param.getValue(), charset));
                }

                // attempt to resolve script for the newly created resource
                Script script = null;
                try {
                    script = RenderService.getInstance().resolveScript(wrappedResource, renderContext);
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                } catch (TemplateNotFoundException e) {
                    // if we didn't find a script, attempt to locate one based on secondary node type if one was specified
                    if (nodeTypes.length > 1) {
                        mixinNodeType = NodeTypeRegistry.getInstance().getNodeType(nodeTypes[1]);
                        wrappedResource.setResourceNodeType(mixinNodeType);
                        script = RenderService.getInstance().resolveScript(wrappedResource, renderContext);
                    }
                }

                // if we have found a script, render it
                if (script != null) {
                    final ServletRequest request = pageContext.getRequest();

                    //save environment
                    Object currentNode = request.getAttribute("currentNode");
                    Resource currentOption = (Resource) request.getAttribute("optionResource");

                    // set attributes to render the newly created resource
                    request.setAttribute("optionResource", currentResource);
                    request.setAttribute("currentNode", node);
                    request.setAttribute("currentResource", wrappedResource);
                    try {
                        pageContext.getOut().write(script.execute(wrappedResource, renderContext));
                    } finally {
                        // restore environment as it previously was
                        request.setAttribute("optionResource", currentOption);
                        request.setAttribute("currentNode", currentNode);
                        request.setAttribute("currentResource", currentResource);
                    }
                }
            }
        }
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
