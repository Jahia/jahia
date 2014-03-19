/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
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
 *
 * JAHIA'S DUAL LICENSING IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
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
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
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
package org.jahia.ajax.gwt.helper;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.servlet.ServletContext;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.ajax.gwt.client.data.GWTJahiaValueDisplayBean;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaItemDefinition;
import org.jahia.ajax.gwt.client.data.definition.GWTJahiaNodeType;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.bin.listeners.JahiaContextLoaderListener;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.extjs.gxt.ui.client.data.RpcMap;

/**
 * Service to provide Stubs (code snippets) for source edition in studio mode.
 */
public class StubHelper {

    private static Logger logger = LoggerFactory.getLogger(StubHelper.class);

    private ContentDefinitionHelper contentDefinition;

    /**
     * @param fileType
     *            type of the file
     * @param snippetType
     *            type of the snippet
     * @return Map of snippets for file type
     */

    private Map<String, String> nodeTypeView;

    private List<String> propertiesSnippetTypes;

    private TemplateHelper template;

    private String detectNodeTypeName(final String path, final JCRSessionWrapper session) {
        String ntName = null;
        try {
            JCRNodeWrapper node = session.getNode(path);
            JCRNodeWrapper parent = node.isNodeType("jnt:nodeTypeFolder") ? node : JCRContentUtils.getParentOfType(
                    node, "jnt:nodeTypeFolder");
            if (parent != null) {
                ntName = StringUtils.replaceOnce(parent.getName(), "_", ":");
            }
        } catch (RepositoryException e) {
            logger.error("Error while trying to find the node type associated with this path " + path, e);
        }
        return ntName;
    }

    /**
     * Returns a map of code snippets.
     * 
     * @param fileType
     *            the type of the file to lookup snippets for, e.g. "jsp"
     * @param snippetType
     *            the snippet type to lookup code snippets for, e.g. "conditionals", "loops" etc.
     * @param nodeTypeName
     *            null or the node type associated with the file
     * @return a map of code snippets
     */
    private Map<String, String> getCodeSnippets(String fileType, String snippetType, String nodeTypeName) {
        Map<String, String> stub = new LinkedHashMap<String, String>();
        InputStream is = null;
        try {
            ServletContext servletContext = JahiaContextLoaderListener.getServletContext();
            @SuppressWarnings("unchecked")
            Set<String> resources = servletContext.getResourcePaths("/WEB-INF/etc/snippets/" + fileType + "/"
                    + snippetType + "/");
            ExtendedNodeType n = null;
            if (resources != null) {
                for (String resource : resources) {
                    String resourceName = StringUtils.substringAfterLast(resource, "/");
                    String viewNodeType = getResourceViewNodeType(resourceName);
                    if (nodeTypeName != null && viewNodeType != null) {
                        // check if the view node type matches the requested one
                        if (null == n) {
                            try {
                                n = NodeTypeRegistry.getInstance().getNodeType(nodeTypeName);
                            } catch (NoSuchNodeTypeException e) {
                                // node type not found, do nothing
                            }
                        }
                        if (n != null && !n.isNodeType(viewNodeType)) {
                            // we skip that stub as it's node type does not match the requested one
                            continue;
                        }
                    }
                    is = servletContext.getResourceAsStream(resource);
                    try {
                        stub.put(viewNodeType != null ? (resourceName + "/" + viewNodeType) : resourceName,
                                StringUtils.join(IOUtils.readLines(is), "\n"));
                    } finally {
                        IOUtils.closeQuietly(is);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to read code snippets from " + fileType + "/" + snippetType, e);
        }
        return stub;
    }

    /**
     * Returns the localized display label template.
     * 
     * @param fileType
     *            the type of the file
     * @param snippetType
     *            the snippet type
     * @param fileName
     *            a file name
     * @param locale
     *            locale to be used for the label
     * @return the localized display label template
     */
    private String getLabelTemplate(String fileType, String snippetType, String fileName, Locale locale) {
        String fileNameWithoutExtension = StringUtils.substringBeforeLast(fileName, ".");
        return Messages.getInternal("label.codesnippets." + fileType + "." + snippetType + "."
                + fileNameWithoutExtension, locale, fileNameWithoutExtension);
    }

    private String getResourceViewNodeType(String resourceName) {
        int dotPosition = resourceName.indexOf('.');
        if (dotPosition == -1 || dotPosition >= resourceName.length() - 1
                || resourceName.indexOf('.', dotPosition + 1) == -1) {
            return null;
        }
        String viewKey = StringUtils.substringAfter(StringUtils.substringBeforeLast(resourceName, "."), ".");
        return nodeTypeView.get(viewKey);
    }

    /**
     * Initialize a map with all data needed to render the code editor.
     * 
     * @param path
     *            path from where we are trying to open the code editor
     * @param isNew
     *            is this a new file or an existing one
     * @param nodeTypeName
     *            null or the node type associated with the file
     * @param fileType
     *            the type of file we are creating/editing
     * @return a RpcMap containing all information to display code editor
     * @throws GWTJahiaServiceException
     *             if something happened
     */

    public RpcMap initializeCodeEditor(final String path, final boolean isNew, final String nodeTypeName,
            final String fileType, final String siteName, final Locale uiLocale, final JCRSessionWrapper session)
            throws GWTJahiaServiceException {
        RpcMap r = new RpcMap();

        String ntName = nodeTypeName == null && path != null ? detectNodeTypeName(path, session) : nodeTypeName;

        r.put("stubs", getCodeSnippets(fileType, "stub", ntName));

        List<GWTJahiaValueDisplayBean> snippetsByType;
        Map<String, List<GWTJahiaValueDisplayBean>> snippets = new LinkedHashMap<String, List<GWTJahiaValueDisplayBean>>();

        if (ntName != null) {
            GWTJahiaNodeType nodeType = contentDefinition.getNodeType(ntName, uiLocale);
            if (nodeType != null) {
                r.put("nodeType", nodeType);
                List<GWTJahiaItemDefinition> items = new ArrayList<GWTJahiaItemDefinition>(nodeType.getItems());
                items.addAll(nodeType.getInheritedItems());
                for (String snippetType : propertiesSnippetTypes) {
                    snippetsByType = new ArrayList<GWTJahiaValueDisplayBean>();
                    for (Map.Entry<String, String> propertySnippetEntry : getCodeSnippets(fileType, snippetType, null)
                            .entrySet()) {
                        MessageFormat labelTemplate = null;
                        for (GWTJahiaItemDefinition definition : items) {
                            String defName = definition.getName();
                            if (!"*".equals(defName) && !definition.isNode() && !definition.isHidden()) {
                                String propertySnippet = StringUtils.replace(propertySnippetEntry.getValue(), "__value__",
                                        defName);
                                if (null == labelTemplate) {
                                    labelTemplate = new MessageFormat(getLabelTemplate(fileType, snippetType,
                                            propertySnippetEntry.getKey(), uiLocale));
                                }
                                GWTJahiaValueDisplayBean displayBean = new GWTJahiaValueDisplayBean(propertySnippet,
                                        labelTemplate.format(new String[] { defName }));
                                displayBean
                                        .set("text", StringUtils.replace(StringUtils.replace(propertySnippet, "<", "&lt;"),
                                                ">", "&gt;"));
                                snippetsByType.add(displayBean);
                            }

                        }
                    }
                    snippets.put(snippetType, snippetsByType);
                }
            }
        }

        Map<String, Set<String>> availableResources = template.getAvailableResources(siteName);

        snippetsByType = new ArrayList<GWTJahiaValueDisplayBean>();
        for (Map.Entry<String, String> resourceSnippetEntry : getCodeSnippets(fileType, "resources", null).entrySet()) {
            MessageFormat labelTemplate = null;
            for (Map.Entry<String, Set<String>> resourcesEntry : availableResources.entrySet()) {
                for (String resource : resourcesEntry.getValue()) {
                    String resourceSnippet = StringUtils.replace(
                            StringUtils.replace(resourceSnippetEntry.getValue(), "__resource__", resource),
                            "__resourceType__", resourcesEntry.getKey());
                    if (null == labelTemplate) {
                        labelTemplate = new MessageFormat(getLabelTemplate(fileType, "resources",
                                resourceSnippetEntry.getKey(), uiLocale));
                    }
                    snippetsByType.add(new GWTJahiaValueDisplayBean(resourceSnippet, labelTemplate
                            .format(new String[] { resource })));
                }
            }
        }
        if (!snippetsByType.isEmpty() || !snippets.isEmpty()) {
            r.put("availableResources", availableResources);
            snippets.put("resources", snippetsByType);
            r.put("snippets", snippets);
        }

        return r;
    }

    /**
     * Injects an instance of the {@link ContentDefinitionHelper}.
     * 
     * @param contentDefinition
     *            an instance of the {@link ContentDefinitionHelper}
     */
    public void setContentDefinition(ContentDefinitionHelper contentDefinition) {
        this.contentDefinition = contentDefinition;
    }

    /**
     * Injects the mapping between the view and node type
     * 
     * @param nodeTypeView
     *            the mapping between the view and node type
     */
    public void setNodeTypeView(Map<String, String> nodeTypeView) {
        this.nodeTypeView = nodeTypeView;
    }

    /**
     * Injection of the list of code snippets we want to display in the code editor.
     * 
     * @param propertiesSnippetTypes
     *            List of type of snippets to be displayed in the code editor.
     */
    public void setPropertiesSnippetTypes(List<String> propertiesSnippetTypes) {
        this.propertiesSnippetTypes = propertiesSnippetTypes;
    }

    /**
     * Passes an instance of the {@link TemplateHelper}.
     * 
     * @param template
     *            an instance of the {@link TemplateHelper}
     */
    public void setTemplate(TemplateHelper template) {
        this.template = template;
    }

}
