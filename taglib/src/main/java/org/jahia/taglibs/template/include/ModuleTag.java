/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.taglibs.template.include;

import org.apache.commons.lang.StringUtils;
import org.apache.taglibs.standard.tag.common.core.ParamParent;
import org.jahia.api.Constants;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ConstraintsHelper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.*;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.AggregateFilter;
import org.jahia.services.render.filter.TemplateAttributesFilter;
import org.jahia.services.render.filter.TemplateNodeFilter;
import org.jahia.services.render.scripting.Script;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.touk.throwing.ThrowingPredicate;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Handler for the &lt;template:module/&gt; tag, used to render content objects.
 * User: toto
 * Date: May 14, 2009
 * Time: 7:18:15 PM
 */
public class ModuleTag extends BodyTagSupport implements ParamParent {

    private static final long serialVersionUID = -8968618483176483281L;
    private static final Logger logger = LoggerFactory.getLogger(ModuleTag.class);

    private static volatile AbstractFilter exclusionFilter = null;
    private static volatile boolean exclusionFilterChecked;

    protected String path;
    protected JCRNodeWrapper node;
    protected JCRSiteNode contextSite;
    protected String nodeName;
    protected String view;
    protected String templateType = null;
    protected boolean editable = true;
    protected String nodeTypes = null;
    protected int listLimit = -1;
    protected String constraints = null;
    protected String var = null;
    protected StringBuilder builder = new StringBuilder();
    protected Map<String, String> parameters = new HashMap<String, String>();
    protected boolean checkConstraints = true;
    protected boolean showAreaButton = true;
    protected boolean skipAggregation = false;

    public String getPath() {
        return path;
    }

    public String getNodeName() {
        return nodeName;
    }

    public JCRNodeWrapper getNode() {
        return node;
    }

    public String getView() {
        return view;
    }

    public String getTemplateType() {
        return templateType;
    }

    public boolean isEditable() {
        return editable;
    }

    public String getVar() {
        return var;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setNodeName(String node) {
        this.nodeName = node;
    }

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    public void setView(String view) {
        this.view = view;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public void setNodeTypes(String nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public void setListLimit(int listLimit) {
        this.listLimit = listLimit;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setContextSite(JCRSiteNode contextSite) {
        this.contextSite = contextSite;
    }

    public void setSkipAggregation(boolean skipAggregation) {
        this.skipAggregation = skipAggregation;
    }

    @Override
    public int doEndTag() throws JspException {

        try {

            RenderContext renderContext =
                    (RenderContext) pageContext.getAttribute("renderContext", PageContext.REQUEST_SCOPE);
            builder = new StringBuilder();

            Resource currentResource =
                    (Resource) pageContext.getAttribute("currentResource", PageContext.REQUEST_SCOPE);

            findNode(renderContext, currentResource);

            String resourceNodeType = null;
            if (parameters.containsKey("resourceNodeType")) {
                resourceNodeType = URLDecoder.decode(parameters.get("resourceNodeType"), "UTF-8");
            }

            if (node != null) {

                try {
                    constraints = ConstraintsHelper.getConstraints(node);
                } catch (RepositoryException e) {
                    logger.error("Error when getting list constraints", e);
                }

                if (templateType == null) {
                    templateType = currentResource.getTemplateType();
                }

                Resource resource = new Resource(node, templateType, view, getConfiguration());

                String charset = pageContext.getResponse().getCharacterEncoding();
                for (Map.Entry<String, String> param : parameters.entrySet()) {
                    resource.getModuleParams().put(URLDecoder.decode(param.getKey(), charset),
                            URLDecoder.decode(param.getValue(), charset));
                }

                if (resourceNodeType != null) {
                    try {
                        resource.setResourceNodeType(NodeTypeRegistry.getInstance().getNodeType(resourceNodeType));
                    } catch (NoSuchNodeTypeException e) {
                        throw new JspException(e);
                    }
                }

                boolean isVisible = true;
                try {
                    isVisible = renderContext.getEditModeConfig() == null || renderContext.isVisible(node);
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }

                try {

                    boolean canEdit = canEdit(renderContext) && contributeAccess(renderContext,
                            resource.getNode()) && !isExcluded(renderContext, resource);

                    boolean nodeEditable = checkNodeEditable(renderContext, node);
                    resource.getModuleParams().put("editableModule", canEdit && nodeEditable);

                    if (canEdit) {

                        String type = getModuleType(renderContext);
                        List<String> contributeTypes = contributeTypes(renderContext, resource.getNode());
                        String oldNodeTypes = nodeTypes;
                        StringBuilder add = new StringBuilder();
                        if (!nodeEditable) {
                            add.append("editable=\"false\"");
                        }
                        if (node.isNodeType(Constants.JAHIAMIX_BOUND_COMPONENT)) {
                            add.append(" bindable=\"true\"");
                        }

                        List<Locale> existingTranslations = node.getExistingLocales();
                        if (existingTranslations != null && !existingTranslations.isEmpty()) {
                            try {
                                node.getI18N(renderContext.getMainResourceLocale());
                            } catch (ItemNotFoundException e) {
                                add.append(" translatable=\"").append(existingTranslations.get(0)).append("\"");
                            }
                        }

                        appendExtraAdditionalParameters(add);

                        Script script = null;
                        try {
                            script = RenderService.getInstance().resolveScript(resource, renderContext);
                            printModuleStart(type, node.getPath(), resource.getResolvedTemplate(),
                                    script, add.toString(), isReferencesAllowed(node));
                        } catch (TemplateNotFoundException e) {
                            printModuleStart(type, node.getPath(), resource.getResolvedTemplate(),
                                    null, add.toString(), isReferencesAllowed(node));
                        }
                        nodeTypes = oldNodeTypes;
                        currentResource.getDependencies().add(node.getCanonicalPath());
                        if (isVisible) {
                            render(renderContext, resource);
                        }
                        //Copy dependencies to parent Resource (only for include of the same node)
                        if (currentResource.getNode().getPath().equals(resource.getNode().getPath())) {
                            currentResource.getRegexpDependencies().addAll(resource.getRegexpDependencies());
                            currentResource.getDependencies().addAll(resource.getDependencies());
                        }
                        printModuleEnd();
                    } else {
                        currentResource.getDependencies().add(node.getCanonicalPath());
                        if (isVisible) {
                            render(renderContext, resource);
                        } else {
                            pageContext.getOut().print("&nbsp;");
                        }
                        //Copy dependencies to parent Resource (only for include of the same node)
                        if (currentResource.getNode().getPath().equals(resource.getNode().getPath())) {
                            currentResource.getRegexpDependencies().addAll(resource.getRegexpDependencies());
                            currentResource.getDependencies().addAll(resource.getDependencies());
                        }
                    }
                } catch (RepositoryException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (RenderException ex) {
            throw new JspException(ex.getCause());
        } catch (IOException ex) {
            throw new JspException(ex);
        } finally {
            if (var != null) {
                pageContext.setAttribute(var, builder.toString());
            }
            path = null;
            node = null;
            contextSite = null;
            nodeName = null;
            view = null;
            templateType = null;
            editable = true;
            nodeTypes = null;
            listLimit = -1;
            constraints = null;
            var = null;
            builder = null;
            parameters.clear();
            checkConstraints = true;
            showAreaButton = true;
            skipAggregation = false;
        }
        return EVAL_PAGE;
    }

    private boolean isExcluded(RenderContext renderContext, Resource resource) throws RepositoryException {
        AbstractFilter filter = getExclusionFilter();
        if (filter == null) {
            return false;
        }
        try {
            return filter.prepare(renderContext, resource, null) != null;
        } catch (Exception e) {
            logger.error("Cannot evaluate exclude filter", e);
        }
        return false;
    }

    private static AbstractFilter getExclusionFilter() {
        if (!exclusionFilterChecked) {
            synchronized (ModuleTag.class) {
                if (!exclusionFilterChecked) {
                    try {
                        exclusionFilter = (AbstractFilter) SpringContextSingleton.getBeanInModulesContext("ChannelExclusionFilter");
                    } catch (Exception e) {
                    }
                    exclusionFilterChecked = true;
                }
            }
        }
        return exclusionFilter;
    }

    protected List<String> contributeTypes(RenderContext renderContext, JCRNodeWrapper node) {

        JCRNodeWrapper contributeNode = null;
        if (renderContext.getRequest().getAttribute("areaListResource") != null) {
            contributeNode = (JCRNodeWrapper) renderContext.getRequest().getAttribute("areaListResource");
        }

        try {
            if (node.hasProperty(Constants.JAHIA_CONTRIBUTE_TYPES)) {
                contributeNode = node;
            }
            if (contributeNode != null && contributeNode.hasProperty(Constants.JAHIA_CONTRIBUTE_TYPES)) {
                LinkedHashSet<String> l = new LinkedHashSet<String>();
                Value[] v = contributeNode.getProperty(Constants.JAHIA_CONTRIBUTE_TYPES).getValues();
                if (v.length == 0) {
                    l.add("jmix:editorialContent");
                } else {
                    for (Value value : v) {
                        l.add(value.getString());
                    }
                }
                LinkedHashSet<String> subtypes = new LinkedHashSet<String>();
                final Set<String> installedModulesWithAllDependencies = renderContext.getSite().getInstalledModulesWithAllDependencies();
                for (String s : l) {
                    ExtendedNodeType nt = NodeTypeRegistry.getInstance().getNodeType(s);
                    if (nt != null) {
                        if (isValidContributeType(installedModulesWithAllDependencies, nt)) {
                            subtypes.add(nt.getName());
                        }
                        for (ExtendedNodeType subtype : nt.getSubtypesAsList()) {
                            if (isValidContributeType(installedModulesWithAllDependencies, subtype)) {
                                subtypes.add(subtype.getName());
                            }
                        }
                    }
                }
                if (subtypes.size() < 10) {
                    return new ArrayList<String>(subtypes);
                }
                return new ArrayList<String>(l);
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private boolean isValidContributeType(Set<String> installedModulesWithAllDependencies, ExtendedNodeType nt) {
        boolean isBaseType = !nt.isAbstract() && !nt.isMixin();
        return isBaseType &&
                !nt.isNodeType("jmix:hiddenType") &&
                (nt.getTemplatePackage() == null || installedModulesWithAllDependencies.contains(nt.getTemplatePackage().getId()));
    }

    private boolean contributeAccess(RenderContext renderContext, JCRNodeWrapper node) {

        if (!"contributemode".equals(renderContext.getEditModeConfigName())) {
            return true;
        }
        JCRNodeWrapper contributeNode;
        final Object areaListResource = renderContext.getRequest().getAttribute("areaListResource");
        if (areaListResource != null) {
            contributeNode = (JCRNodeWrapper) areaListResource;
        } else {
            contributeNode = (JCRNodeWrapper) renderContext.getRequest().getAttribute(TemplateAttributesFilter.AREA_RESOURCE);
        }

        try {
            final Boolean nodeStatus = isNodeEditableInContributeMode(node);
            final Boolean contributeNodeStatus = contributeNode != null ? isNodeEditableInContributeMode(contributeNode) : null;

            final String sitePath = renderContext.getSite().getPath();
            if (nodeStatus != null) {
                // first look at the current node's status with respect to editable in contribution mode, if it's determined, then use that
                return nodeStatus;
            } else if (contributeNodeStatus != null) {
                // otherwise, look at the contribute node's status if it exists and use that
                return contributeNodeStatus;
            } else if (node.getPath().startsWith(sitePath)) {
                // otherwise, if the property wasn't defined on the nodes we are interested in, look at the parent iteratively until we know the status of the property
                while (!node.getPath().equals(sitePath)) {
                    node = node.getParent();

                    final Boolean parentStatus = isNodeEditableInContributeMode(node);
                    if (parentStatus != null) {
                        return parentStatus;
                    }
                }
            }
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    /**
     * Returns <code>null</code> if the node we're looking at doesn't have the editable in contribution mode property, otherwise returns the value of the property.
     *
     * @param node the node we're interested in
     * @return <code>null</code> if the node we're looking at doesn't have the editable in contribution mode property, otherwise returns the value of the property.
     * @throws RepositoryException
     */
    private Boolean isNodeEditableInContributeMode(JCRNodeWrapper node) throws RepositoryException {
        final boolean hasProperty = node.hasProperty(Constants.JAHIA_EDITABLE_IN_CONTRIBUTION);
        if (hasProperty) {
            return node.getProperty(Constants.JAHIA_EDITABLE_IN_CONTRIBUTION).getBoolean();
        } else {
            return null;
        }
    }

    protected void findNode(RenderContext renderContext, Resource currentResource) throws IOException {
        if (nodeName != null) {
            node = (JCRNodeWrapper) pageContext.findAttribute(nodeName);
        } else if (path != null && currentResource != null) {
            try {
                if (!path.startsWith("/")) {
                    JCRNodeWrapper nodeWrapper = currentResource.getNode();
                    if (!path.equals("*") && nodeWrapper.hasNode(path)) {
                        node = (JCRNodeWrapper) nodeWrapper.getNode(path);
                    } else {
                        missingResource(renderContext, currentResource);
                    }
                } else if (path.startsWith("/")) {
                    JCRSessionWrapper session = currentResource.getNode().getSession();
                    try {
                        node = (JCRNodeWrapper) session.getItem(path);
                    } catch (PathNotFoundException e) {
                        missingResource(renderContext, currentResource);
                    }
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    protected String getConfiguration() {
        return Resource.CONFIGURATION_MODULE;
    }

    protected boolean checkNodeEditable(RenderContext renderContext, JCRNodeWrapper node) {
        try {
            if (node != null && !renderContext.isEditable(node)) {
                return false;
            }
        } catch (RepositoryException e) {
            logger.error("Failed to check if the node " + node.getPath() + " is editable.", e);
        }
        return true;
    }

    protected boolean canEdit(RenderContext renderContext) {
        return renderContext.isEditMode() && editable &&
                !Boolean.TRUE.equals(renderContext.getRequest().getAttribute("inWrapper")) &&
                renderContext.getRequest().getAttribute(TemplateNodeFilter.ATTR_IN_AREA) == null;
    }

    protected void printModuleStart(String type, String path, String resolvedTemplate, Script script,
                                    String additionalParameters, boolean isReferenceAllowed)
            throws RepositoryException, IOException {

        builder.append("<div class=\"jahia-template-gxt\" jahiatype=\"module\" ").append("id=\"module")
                .append(UUID.randomUUID().toString()).append("\" type=\"").append(type).append("\"");

        builder.append((script != null && script.getView().getInfo() != null) ? " scriptInfo=\"" + script.getView().getInfo() + "\"" : "");

        if (script != null && script.getView().getModule().getSourcesFolder() != null) {
            String version = script.getView().getModule().getIdWithVersion();
            builder.append(" sourceInfo=\"/modules/").append(version).append("/sources/src/main/resources").append(StringUtils.substringAfter(script.getView().getPath(), "/modules/" + script.getView().getModule().getId())).append("\"");
        }

        builder.append(" path=\"").append(path != null && path.indexOf('"') != -1 ? Patterns.DOUBLE_QUOTE.matcher(path).replaceAll("&quot;") : path).append("\"");

        nodeTypes = filterNodeTypes(nodeTypes);
        constraints = filterNodeTypes(constraints);

        if (!StringUtils.isEmpty(nodeTypes)) {
            builder.append(" nodetypes=\"").append(nodeTypes).append("\"");
            builder.append(" allowReferences=\"").append(isReferenceAllowed).append("\"");
        } else if (!StringUtils.isEmpty(constraints)) {
            builder.append(" nodetypes=\"").append(constraints).append("\"");
            builder.append(" allowReferences=\"").append(isReferenceAllowed).append("\"");
        }

        // Override listLimit if listLimitSize mixin was used in content-editor
        overrideLimit(node);

        if (listLimit > -1) {
            builder.append(" listlimit=\"").append(listLimit).append("\"");
        }

        if (!StringUtils.isEmpty(constraints)) {
            String referenceTypes = ConstraintsHelper.getReferenceTypes(constraints, nodeTypes);
            builder.append((!StringUtils.isEmpty(referenceTypes)) ? " referenceTypes=\"" + referenceTypes + "\"" : " referenceTypes=\"none\"");
        }

        if (additionalParameters != null) {
            builder.append(" ").append(additionalParameters);
        }

        builder.append("showAreaButton=\"").append(showAreaButton).append("\"");

        builder.append(">");

        printAndClean();
    }

    private String filterNodeTypes(String nodeTypes) throws RepositoryException {
        if (nodeTypes == null || StringUtils.isEmpty(StringUtils.trim(nodeTypes))) {
            return null;
        }
        Set<String> modules = getNode() != null && getNode().getResolveSite() != null ?
                new LinkedHashSet<>(getNode().getResolveSite().getInstalledModulesWithAllDependencies()) : null;
        if (modules != null) {
            modules.add("system-jahia");
        }
        return Arrays.stream(StringUtils.split(nodeTypes))
                .filter(ThrowingPredicate.unchecked(nt -> NodeTypeRegistry.getInstance().hasNodeType(nt)))
                .filter(ThrowingPredicate.unchecked(nt -> modules == null || modules.contains(NodeTypeRegistry.getInstance().getNodeType(nt).getSystemId())))
                .filter(ThrowingPredicate.unchecked(nt -> !NodeTypeRegistry.getInstance().getNodeType(nt).isNodeType("jmix:hiddenType")))
                .sorted()
                .collect(Collectors.joining(" "));
    }

    private void overrideLimit(JCRNodeWrapper node) throws RepositoryException {
        if (node != null && node.isNodeType("jmix:listSizeLimit") && node.hasProperty("limit")) {
            setListLimit((int) node.getProperty("limit").getLong());
        }
    }

    protected boolean isReferencesAllowed(final JCRNodeWrapper node) throws RepositoryException {
        if (node == null) {
            return false;
        }

        try {
            node.getApplicableChildNodeDefinition("*", "jnt:contentReference");
            return true;
        } catch (ConstraintViolationException e) {
            return false;
        }
    }

    protected void printModuleEnd() throws IOException {
        builder.append("</div>");
        printAndClean();
    }

    private void printAndClean() throws IOException {
        if (var == null) {
            pageContext.getOut().print(builder);
            builder.delete(0, builder.length());
        }
    }

    protected void render(RenderContext renderContext, Resource resource) throws IOException, RenderException {
        HttpServletRequest request = renderContext.getRequest();
        Boolean oldSkipAggregation = (Boolean) request.getAttribute(AggregateFilter.SKIP_AGGREGATION);
        try {
            JCRSiteNode previousSite = renderContext.getSite();
            if (contextSite != null) {
                renderContext.setSite(contextSite);
            }
            if (skipAggregation) {
                request.setAttribute(AggregateFilter.SKIP_AGGREGATION, skipAggregation);
                resource.getRegexpDependencies().add(resource.getNodePath() + "/.*");
            }
            builder.append(RenderService.getInstance().render(resource, renderContext));
            renderContext.setSite(previousSite);
            printAndClean();
        } catch (TemplateNotFoundException io) {
            builder.append(io);
            printAndClean();
        } catch (RenderException e) {
            if (renderContext.isEditMode() && ((e.getCause() instanceof TemplateNotFoundException) || (e.getCause() instanceof AccessDeniedException))) {
                if (!(e.getCause() instanceof AccessDeniedException)) {
                    logger.error(e.getMessage(), e);
                }
                builder.append(e.getCause().getMessage());
                printAndClean();
            } else {
                throw e;
            }
        } finally {
            request.setAttribute(AggregateFilter.SKIP_AGGREGATION, oldSkipAggregation);
        }
    }

    protected String getModuleType(RenderContext renderContext) throws RepositoryException {
        String type = "existingNode";
        if (node.isNodeType("jmix:listContent")) {
            type = "list";
        } else if (renderContext.getEditModeConfig().isForceHeaders()) {
            type = "existingNodeWithHeader";
        }
        return type;
    }

    protected void missingResource(RenderContext renderContext, Resource currentResource)
            throws RepositoryException, IOException {

        String currentPath = currentResource.getNode().getPath();
        if (path.startsWith(currentPath + "/") && path.substring(currentPath.length() + 1).indexOf('/') == -1) {
            currentResource.getMissingResources().add(path.substring(currentPath.length() + 1));
        } else if (!path.startsWith("/")) {
            currentResource.getMissingResources().add(path);
        }

        if (!"*".equals(path) && (path.indexOf("/") == -1)) {
            // we have a named path that is missing, let's see if we can figure out it's node type.
            constraints = ConstraintsHelper.getConstraints(currentResource.getNode(), path);
        }

        if (canEdit(renderContext) && checkNodeEditable(renderContext, currentResource.getNode()) && contributeAccess(renderContext, currentResource.getNode())) {
            if (currentResource.getNode().hasPermission("jcr:addChildNodes")) {
                List<String> contributeTypes = contributeTypes(renderContext, currentResource.getNode());
                if (contributeTypes != null) {
                    nodeTypes = StringUtils.join(contributeTypes, " ");
                }
                printModuleStart("placeholder", path, null, null, null, isReferencesAllowed(currentResource.getNode()));
                printModuleEnd();
            }
        }
    }

    @Override
    public void addParameter(String name, String value) {
        parameters.put(name, value);
    }

    @Deprecated(since = "7.3.1.0", forRemoval = true)
    public void setCheckConstraints(boolean checkConstraints) {
        // constraint are now resolved by JCRFilterTag when called by list jsp
        this.checkConstraints = checkConstraints;
    }

    protected void appendExtraAdditionalParameters(StringBuilder additionalParameters) {
    }
}
