/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
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
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.util.Text;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.query.QOMBuilder;
import org.jahia.taglibs.AbstractJahiaTag;

import javax.jcr.RepositoryException;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import java.util.Collections;
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

            String selectorName;
            if (existing == null) {
                // here we assume that if existing is null, then bound component is not of type jnt:query
                String wantedNodeType = "jnt:content";
                if (currentNode.hasProperty("j:type")) {
                    wantedNodeType = currentNode.getPropertyAsString("j:type");
                }

                selectorName = wantedNodeType;
                qomBuilder.setSource(factory.selector(wantedNodeType, selectorName));

                // replace the site name in bound component by the one from the render context
                String path = boundComponent.getPath();
                final String siteName = getRenderContext().getSite().getName();
                final int afterSites = "/sites/".length();
                final int afterSite = path.indexOf('/', afterSites + 1);
                if (afterSite > 0 && afterSite < path.length()) {
                    String restOfPath = path.substring(afterSite);
                    path = "/sites/" + siteName + restOfPath;
                }
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

                // extra query parameters
                String extra = null;

                // min count
                final String minCount = facet.getPropertyAsString("mincount");

                // field components
                final String field = facet.getPropertyAsString("field");
                final String[] fieldComponents = StringUtils.split(field, ";");
                final String facetNodeTypeName = fieldComponents != null ? fieldComponents[0] : null;
                final String facetPropertyName = fieldComponents != null ? fieldComponents[1] : null;

                // are we dealing with a query facet?
                boolean isQuery = facet.hasProperty("query");

                // query value if it exists
                final String queryProperty = isQuery ? facet.getPropertyAsString("query") : null;

                // key used in metadata maps
                final String metadataKey = isQuery ? queryProperty : facetPropertyName;

                // get node type if we can
                ExtendedNodeType nodeType = null;
                if (StringUtils.isNotEmpty(facetNodeTypeName)) {
                    nodeType = NodeTypeRegistry.getInstance().getNodeType(facetNodeTypeName);
                    if (facetValueNodeTypes != null && StringUtils.isNotEmpty(metadataKey)) {
                        facetValueNodeTypes.put(metadataKey, nodeType);
                    }
                }

                // label
                String currentFacetLabel = null;
                // use label property if it exists
                if (facet.hasProperty("label")) {
                    currentFacetLabel = facet.getPropertyAsString("label");
                }
                // otherwise try to derive a label from node type and field name
                if (StringUtils.isEmpty(currentFacetLabel) && StringUtils.isNotEmpty(facetNodeTypeName) && StringUtils.isNotEmpty(facetPropertyName)) {
                    final String labelKey = facetNodeTypeName.replace(':', '_') + "." + facetPropertyName.replace(':', '_');

                    currentFacetLabel = getMessage(labelKey);
                }
                if (facetLabels != null && StringUtils.isNotEmpty(currentFacetLabel)) {
                    facetLabels.put(metadataKey, currentFacetLabel);
                }

                // value format
                if (facetValueFormats != null && facet.hasProperty("labelFormat")) {
                    facetValueFormats.put(metadataKey, facet.getPropertyAsString("labelFormat"));
                }

                // label renderer
                String labelRenderer = null;
                if (facetValueRenderers != null && facet.hasProperty("labelRenderer")) {
                    labelRenderer = facet.getPropertyAsString("labelRenderer");
                    facetValueRenderers.put(metadataKey, labelRenderer);
                }

                // value label
                if (facetValueLabels != null && facet.hasProperty("valueLabel")) {
                    facetValueLabels.put(metadataKey, facet.getPropertyAsString("valueLabel"));
                }

                if (nodeType != null
                        && StringUtils.isNotEmpty(facetPropertyName)
                        && !Functions.isFacetApplied(facetPropertyName, activeFacets, nodeType.getPropertyDefinition(facetPropertyName))) {

                    StringBuilder extraBuilder = new StringBuilder();

                    // deal with facets with labelRenderers, currently only jnt:dateFacet or jnt:rangeFacet
                    String prefix = facet.isNodeType("jnt:dateFacet") ? "date." : (facet.isNodeType("jnt:rangeFacet") ? "range." : "");
                    if (StringUtils.isNotEmpty(labelRenderer)) {
                        extraBuilder.append(prefixedNameValuePair(prefix, "labelRenderer", labelRenderer));
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
                                    final List<KeyValue> active = activeFacets != null ? this.activeFacets.get(facetPropertyName) : Collections.<KeyValue>emptyList();
                                    if (active == null || active.isEmpty()) {
                                        value = Functions.getIndexPrefixedPath(value);
                                    } else {
                                        value = Functions.getDrillDownPrefix((String) active.get(active.size() - 1).getKey());
                                    }
                                }

                                extraBuilder.append(prefixedNameValuePair(prefix, name, value));
                            }
                        }
                    }

                    extra = extraBuilder.toString();

                }

                if (isQuery && !Functions.isFacetApplied(queryProperty, activeFacets, null)) {
                    extra = "&facet.query=" + queryProperty;
                }

                // key used in the solr query string
                final String key = isQuery ? facet.getName() : facetPropertyName;
                String query = buildQueryString(facetNodeTypeName, key, minCount, extra);
                final String columnPropertyName = StringUtils.isNotEmpty(facetPropertyName) ? facetPropertyName : "rep:facet()";
                qomBuilder.getColumns().add(factory.column(selectorName, columnPropertyName, query));
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
        final String nodeType = facetNodeTypeName != null ? "nodetype=" + facetNodeTypeName + "&" : "";
        return "rep:facet(" + nodeType + "key=" + key + "&mincount=" + minCount + extra + ")";
    }
}
