/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.taglibs.facet;

import org.apache.commons.collections.KeyValue;
import org.apache.jackrabbit.util.Text;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.query.QOMBuilder;
import org.jahia.taglibs.AbstractJahiaTag;
import org.jahia.taglibs.jcr.node.JCRTagUtils;

import javax.jcr.RepositoryException;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.List;
import java.util.Map;

/**
 * @author Christophe Laprun
 */
public class SetupQueryAndMetadataTag extends AbstractJahiaTag {

    private JCRNodeWrapper boundComponent;
    private QueryObjectModel existing;
    private Map<String, List<KeyValue>> activeFacets;
    private String var;

    public void setBoundComponent(JCRNodeWrapper boundComponent) {
        this.boundComponent = boundComponent;
    }

    public void setExistingQuery(QueryObjectModel existingQuery) {
        this.existing = existingQuery;
    }

    public void setActiveFacets(Map<String, List<KeyValue>> activeFacets) {
        this.activeFacets = activeFacets;
    }

    public void setVar(String var) {
        this.var = var;
    }

    @Override
    public int doStartTag() throws JspException {
        try {
            final JCRNodeWrapper currentNode = getCurrentResource().getNode();
            final JCRSessionWrapper session = currentNode.getSession();
            QueryObjectModelFactory factory = session.getWorkspace().getQueryManager().getQOMFactory();
            QOMBuilder qomBuilder = new QOMBuilder(factory, session.getValueFactory());

            // what should be done here if boundComponent is of type jnt:query?
            String selectorName;
            if (existing == null && !JCRTagUtils.isNodeType(boundComponent, "jnt:query")) {
                String wantedNodeType = "jnt:content";
                if (currentNode.hasProperty("j:type")) {
                    wantedNodeType = currentNode.getPropertyAsString("j:type");
                }

                selectorName = wantedNodeType;
                qomBuilder.setSource(factory.selector(wantedNodeType, selectorName));
                String path = boundComponent.getPath();
                                /*path = path.substring(path.indexOf("/sites/"));
                                path = "/sites/" + context.getSite().getName() + "/" + path;*/
                qomBuilder.andConstraint(factory.descendantNode(selectorName, path));
            } else {
                final Selector selector = (Selector) existing.getSource();
                selectorName = selector.getSelectorName();
                qomBuilder.setSource(selector);
                qomBuilder.andConstraint(existing.getConstraint());
            }

            // metadata for display, passed to JSP
            Map facetValueNodeTypes = (Map) pageContext.getAttribute("facetValueNodeTypes", PageContext.REQUEST_SCOPE);
            Map facetLabels = (Map) pageContext.getAttribute("facetLabels", PageContext.REQUEST_SCOPE);
            Map facetValueLabels = (Map) pageContext.getAttribute("facetValueLabels", PageContext.REQUEST_SCOPE);
            Map facetValueFormats = (Map) pageContext.getAttribute("facetValueFormats", PageContext.REQUEST_SCOPE);
            Map facetValueRenderers = (Map) pageContext.getAttribute("facetValueRenderers", PageContext.REQUEST_SCOPE);

            // specify query for unapplied facets
            final List<JCRNodeWrapper> facets = JCRContentUtils.getNodes(currentNode, "jnt:facet");
            for (JCRNodeWrapper facet : facets) {

                // field components
                final String field = facet.getPropertyAsString("field");
                final String[] fieldComponents = field.split(";");
                final String facetNodeTypeName = fieldComponents[0];
                final String facetPropertyName = fieldComponents[1];
                if (facetNodeTypeName != null && facetPropertyName != null && !facetNodeTypeName.isEmpty() && !facetPropertyName.isEmpty()) {
                    // node type
                    final ExtendedNodeType nodeType = NodeTypeRegistry.getInstance().getNodeType(facetNodeTypeName);
                    if (facetValueNodeTypes != null) {
                        facetValueNodeTypes.put(facetPropertyName, nodeType);
                    }

                    // min count
                    final String minCount = facet.getPropertyAsString("mincount");

                    // label
                    final String currentFacetLabel;
                    if (facet.hasProperty("label")) {
                        currentFacetLabel = facet.getPropertyAsString("label");
                    } else {
                        final String labelKey = facetNodeTypeName.replace(':', '_') + "." + facetPropertyName.replace(':', '_');

                        currentFacetLabel = getMessage(labelKey);
                    }
                    if (facetLabels != null) {
                        facetLabels.put(facetPropertyName, currentFacetLabel);
                    }

                    String key = null;
                    String extra = null;
                    if (facet.hasProperty("query")) {
                        final String queryProperty = facet.getPropertyAsString("query");

                        // value label
                        if (facetValueLabels != null) {
                            if (facet.hasProperty("valueLabel")) {
                                final String valueLabel = facet.getPropertyAsString("valueLabel");
                                facetValueLabels.put(queryProperty, valueLabel);
                            }
                        }

                        // query label
                        if (facetLabels != null) {
                            facetLabels.put(queryProperty, currentFacetLabel);
                        }

                        if (!Functions.isFacetApplied(queryProperty, activeFacets, null)) {
                            key = facet.getName();
                            extra = "&facet.query=" + queryProperty;
                        }
                    } else {
                        if (facetValueFormats != null && facet.hasProperty("labelFormat")) {
                            facetValueFormats.put(facetPropertyName, facet.getPropertyAsString("labelFormat"));
                        }

                        String labelRenderer = null;
                        if (facetValueRenderers != null && facet.hasProperty("labelRenderer")) {
                            labelRenderer = facet.getPropertyAsString("labelRenderer");
                            facetValueRenderers.put(facetNodeTypeName, labelRenderer);
                        }

                        if (!Functions.isFacetApplied(facetPropertyName, activeFacets, nodeType.getPropertyDefinition(facetPropertyName))) {

                            key = facetPropertyName;

                            StringBuilder extraBuilder = new StringBuilder();

                            // deal with facets with labelRenderers, currently only jnt:dateFacet or jnt:rangeFacet
                            String prefix = facet.isNodeType("jnt:dateFacet") ? "date." : (facet.isNodeType("jnt:rangeFacet") ? "range." : "");
                            if (labelRenderer != null) {
                                extraBuilder.append("&").append(prefix).append("labelRenderer=").append(labelRenderer);
                            }

                            for (ExtendedPropertyDefinition propertyDefinition : Functions.getPropertyDefinitions(facet)) {
                                final String name = propertyDefinition.getName();
                                if (facet.hasProperty(name)) {
                                    final JCRPropertyWrapper property = facet.getProperty(name);

                                    if (property.isMultiple()) {
                                        // if property is multiple append prefixed name value pair to query
                                        for (JCRValueWrapper value : property.getValues()) {
                                            extraBuilder.append(prefixedNameValuePair(prefix, name, value.getString()));
                                        }
                                    } else {
                                        String value = property.getString();

                                        // adjust value for hierarchical facets
                                        if (facet.isNodeType("jnt:fieldHierarchicalFacet") && name.equals("prefix")) {
                                            if (activeFacets != null) {
                                                final List<KeyValue> activeFacets = this.activeFacets.get(facetPropertyName);
                                                if (activeFacets.isEmpty()) {
                                                    value = Functions.getIndexPrefixedPath(value);
                                                } else {
                                                    value = Functions.getDrillDownPrefix((String) activeFacets.get(activeFacets.size() - 1).getKey());
                                                }
                                            }
                                        }

                                        extraBuilder.append(prefixedNameValuePair(prefix, name, value));
                                    }
                                }
                                else {
//                                    System.out.println("Property definition without property named = " + name);
                                    // todo: do something?
                                }
                            }

                            extra = extraBuilder.toString();
                        }
                    }

                    if (key != null) {
                        String query = buildQueryString(facetNodeTypeName, key, minCount, extra);
                        qomBuilder.getColumns().add(factory.column(selectorName, facetPropertyName, query));
                    }
                }
            }

            // repeat applied facets
            if (activeFacets != null) {
                for (Map.Entry<String, List<KeyValue>> appliedFacet : activeFacets.entrySet()) {
                    for (KeyValue keyValue : appliedFacet.getValue()) {
                        final String propertyName = "rep:filter(" + Text.escapeIllegalJcrChars(appliedFacet.getKey()) + ")";
                        qomBuilder.andConstraint(factory.fullTextSearch(selectorName, propertyName, factory.literal(qomBuilder.getValueFactory().createValue(keyValue.getValue().toString()))));
                    }
                }
            }

            pageContext.setAttribute(var, qomBuilder.createQOM(), PageContext.REQUEST_SCOPE);
        } catch (RepositoryException e) {
            throw new JspException(e);
        }

        return SKIP_BODY;
    }

    @Override
    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }

    private static String prefixedNameValuePair(String prefix, String name, String value) {
        return "&" + prefix + name + "=" + value;
    }

    private static String buildQueryString(String facetNodeTypeName, String key, String minCount, String extra) {
        return "rep:facet(nodetype=" + facetNodeTypeName + "&key=" + key + "&mincount=" + minCount + extra + ")";
    }
}
