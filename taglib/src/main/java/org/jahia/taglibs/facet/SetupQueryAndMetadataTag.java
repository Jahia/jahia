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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Christophe Laprun
 */
public class SetupQueryAndMetadataTag extends AbstractJahiaTag {
    private static final long serialVersionUID = 1L;
    private JCRNodeWrapper boundComponent;
    private QueryObjectModel existing;
    private Map<String, List<KeyValue>> activeFacets;
    private static final String CURRENT_FACET_KEY = "currentIndexFacetKey";
    private static final String LABEL_RENDERER = "labelRenderer";
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

    @SuppressWarnings("unchecked")
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
            Map<String, ExtendedNodeType> facetValueNodeTypes = (Map<String, ExtendedNodeType>) pageContext.getAttribute("facetValueNodeTypes", PageContext.REQUEST_SCOPE);
            Map<String, String> facetLabels = (Map<String, String>) pageContext.getAttribute("facetLabels", PageContext.REQUEST_SCOPE);
            Map<String, String> facetValueLabels = (Map<String, String>) pageContext.getAttribute("facetValueLabels", PageContext.REQUEST_SCOPE);
            Map<String, String> facetValueFormats = (Map<String, String>) pageContext.getAttribute("facetValueFormats", PageContext.REQUEST_SCOPE);
            Map<String, String> facetValueRenderers = (Map<String, String>) pageContext.getAttribute("facetValueRenderers", PageContext.REQUEST_SCOPE);
            Map<String, ExtendedPropertyDefinition> facetValuePropDefs = (Map<String, ExtendedPropertyDefinition>) pageContext.getAttribute("facetValuePropDefs", PageContext.REQUEST_SCOPE);
            if (facetValuePropDefs == null) {
                facetValuePropDefs = new HashMap<>();
                pageContext.setAttribute("facetValuePropDefs", facetValuePropDefs, PageContext.REQUEST_SCOPE);
            }

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
                int i = 0;
                final String facetNodeTypeName = fieldComponents != null && fieldComponents.length > 1 ? fieldComponents[i++] : null;
                final String facetPropertyName = fieldComponents != null ? fieldComponents[i] : null;

                // are we dealing with a query facet?
                boolean isQuery = facet.hasProperty("query");

                // query value if it exists
                final String queryProperty = isQuery ? facet.getPropertyAsString("query") : null;

                // key used in metadata maps
                // get current keys from the query
                Integer facetKey = (Integer) pageContext.getRequest().getAttribute(CURRENT_FACET_KEY);
                if (facetKey == null) {
                    facetKey = 1;
                } else {
                    facetKey ++;
                }
                final String facetIdentifier = CURRENT_FACET_KEY + facetKey;
                pageContext.getRequest().setAttribute(CURRENT_FACET_KEY, facetKey);
                final String metadataKey = isQuery ? queryProperty  :
                        facet.isNodeType("jnt:fieldFacet") ? facetIdentifier : facetPropertyName;

                // get node type if we can
                ExtendedNodeType nodeType = null;
                if (StringUtils.isNotEmpty(facetNodeTypeName)) {
                    // first check if we don't already have resolved the nodeType to avoid resolving it again
                    if (facetValueNodeTypes != null) {
                        nodeType = facetValueNodeTypes.get(metadataKey);
                    }

                    // since we haven't already resolved it, try that now
                    if (nodeType == null) {
                        nodeType = NodeTypeRegistry.getInstance().getNodeType(facetNodeTypeName);
                        if (facetValueNodeTypes != null && StringUtils.isNotEmpty(metadataKey)) {
                            facetValueNodeTypes.put(metadataKey, nodeType);
                        }
                    }
                    if (nodeType != null) {
                        facetValuePropDefs.put(metadataKey, nodeType.getPropertyDefinitionsAsMap().get(facetPropertyName));
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
                if (facetValueRenderers != null && facet.hasProperty(LABEL_RENDERER)) {
                    labelRenderer = facet.getPropertyAsString(LABEL_RENDERER);
                    facetValueRenderers.put(metadataKey, labelRenderer);
                }

                // value label
                if (facetValueLabels != null && facet.hasProperty("valueLabel")) {
                    facetValueLabels.put(metadataKey, facet.getPropertyAsString("valueLabel"));
                }

                // should current facet be evaluated? (logic: evaluate only if not already applied or it is a multiple valued or hierarchical facet)
                final ExtendedPropertyDefinition propDef = nodeType != null ? nodeType.getPropertyDefinition(facetPropertyName) : null;
                final boolean isFacetToBuild = !Functions.isFacetApplied(metadataKey, activeFacets, propDef)
                        || propDef != null && (propDef.isMultiple() || propDef.isHierarchical());

                if (nodeType != null && StringUtils.isNotEmpty(facetPropertyName) && isFacetToBuild) {
                    StringBuilder extraBuilder = new StringBuilder();

                    // deal with facets with labelRenderers, currently only jnt:dateFacet or jnt:rangeFacet
                    String prefix = facet.isNodeType("jnt:dateFacet") ? "date." : (facet.isNodeType("jnt:rangeFacet") ? "range." : "");
                    if (StringUtils.isNotEmpty(labelRenderer)) {
                        extraBuilder.append(prefixedNameValuePair(prefix, LABEL_RENDERER, labelRenderer));
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
                                    final List<KeyValue> active = activeFacets != null ? this.activeFacets.get(facetIdentifier) : Collections.<KeyValue>emptyList();
                                    if (active == null || active.isEmpty()) {
                                        value = Functions.getIndexPrefixedPath(value, getRenderContext().getWorkspace());
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

                if (isQuery && isFacetToBuild) {
                    extra = "&facet.query=" + queryProperty;
                }

                // only add a column if the facet isn't already applied
                if (isFacetToBuild) {
                    // key used in the solr query string
                    final String key = isQuery ? facet.getName() :
                            facet.isNodeType("jnt:fieldFacet") ? facetIdentifier : facetPropertyName;
                    String query = buildQueryString(facetNodeTypeName, key, minCount, extra);
                    final String columnPropertyName = StringUtils.isNotEmpty(facetPropertyName) ? facetPropertyName : "rep:facet()";
                    qomBuilder.getColumns().add(factory.column(selectorName, columnPropertyName, query));
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
        resetState();
        return EVAL_PAGE;
    }

    private static String prefixedNameValuePair(String prefix, String name, String value) {
        return "&" + prefix + name + "=" + value;
    }

    private static String buildQueryString(String facetNodeTypeName, String key, String minCount, String extra) {
        final String nodeType = facetNodeTypeName != null ? "nodetype=" + facetNodeTypeName + "&" : "";
        return "rep:facet(" + nodeType + "key=" + key + "&mincount=" + minCount + (extra != null ? extra : "") + ")";
    }

    @Override
    protected void resetState() {
        activeFacets = null;
        boundComponent = null;
        existing = null;
        var = null;
        super.resetState();
    }
}
