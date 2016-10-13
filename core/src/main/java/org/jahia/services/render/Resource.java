/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.services.render;

import org.apache.commons.lang.StringUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.render.scripting.Script;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.io.Serializable;
import java.util.*;

/**
 * A resource is the aggregation of a node and a specific template
 * It's something that can be handled by the render engine to be displayed.
 *
 * @author toto
 */
public class Resource {

    public static final String CONFIGURATION_PAGE = "page";
    public static final String CONFIGURATION_GWT = "gwt";
    public static final String CONFIGURATION_MODULE = "module";
    public static final String CONFIGURATION_INCLUDE = "include";
    public static final String CONFIGURATION_WRAPPER = "wrapper";
    public static final String CONFIGURATION_WRAPPEDCONTENT = "wrappedcontent";

    private static Logger logger = LoggerFactory.getLogger(Resource.class);

    private JCRNodeWrapper node;
    private String templateType;
    private String template;
    private String resolvedTemplate;
    private String contextConfiguration;
    private Stack<String> wrappers = new Stack<String>();;
    private Script script;
    // flag to avoid script calculation when not found first time
    private boolean scriptNotFound = false;

    // lazy properties
    private String nodePath;
    private JCRSessionWrapper sessionWrapper;
    // Flag set after cache filter have been executed
    // used to detect node load before the cache filter that should be avoid for perf issues
    private boolean lazyNodeFlagWarning = true;

    private Set<String> dependencies = new HashSet<String>();;
    private List<String> missingResources = new ArrayList<String>();

    private List<Option> options = new ArrayList<Option>();
    private ExtendedNodeType resourceNodeType;
    private Map<String, Serializable> moduleParams = new HashMap<String, Serializable>();
    private Set<String> regexpDependencies = new LinkedHashSet<String>();;

    /**
     * Creates a resource from the specified parameter
     *
     * @param node                 The node to display
     * @param templateType         template type
     * @param template
     * @param contextConfiguration
     */
    public Resource(JCRNodeWrapper node, String templateType, String template, String contextConfiguration) {
        this.node = node;
        this.nodePath = node.getPath();
        this.templateType = templateType;
        this.template = template;
        this.contextConfiguration = contextConfiguration;
        dependencies.add(node.getCanonicalPath());
    }

    /**
     * Lazy resource, that take the path instead of a node, the node will be load at first getNode() call using sessionWrapper
     *
     * @param path                 The path to the node to display
     * @param sessionWrapper       The session that will be used to load the node
     * @param templateType         template type
     * @param template
     * @param contextConfiguration
     */
    public Resource(String path, JCRSessionWrapper sessionWrapper, String templateType, String template, String contextConfiguration) {
        this.nodePath = path;
        this.sessionWrapper = sessionWrapper;
        this.templateType = templateType;
        this.template = template;
        this.contextConfiguration = contextConfiguration;
        dependencies.add(path);
    }

    /**
     * Get the node associated to the current resource, in case of a lazy resource the node will be load from jcr if it's null
     * This function shouldn't not be call before the CacheFilter to avoid loading of node from JCR.
     * Since CacheFilter is executed for each fragments now even if there are in cache.
     *
     * If the node is needed it should be requested after the CacheFilter or for cache key generation during aggregation
     *
     * @return The JCR Node if found, null if not
     */
    public JCRNodeWrapper getNode() {
        if (!isNodeLoaded()) {
            try {
                if (lazyNodeFlagWarning &&
                        sessionWrapper.getWorkspace().getName().equals(Constants.LIVE_WORKSPACE) &&
                        !CONFIGURATION_PAGE.equals(contextConfiguration)) {

                    logger.warn("Performance warning: node loaded from JCR before the cache filter for resource: " +
                            nodePath + ", render filter implementations have to be review, " +
                            "to avoid reading the node from the resource before the cache. " +
                            "You can enable debug log level to look at the current thread stack, " +
                            "this could help to identify the calling method");
                    if(logger.isDebugEnabled()) {
                        Thread.dumpStack();
                    }
                }

                node = sessionWrapper.getNode(nodePath);
            } catch (RepositoryException e) {
                throw new IllegalStateException("Lazy Node from resource failed to be load, because Node is not found anymore", e);
            }
        }
        return node;
    }

    /**
     * get the associate Node, but set the flag for lazy load to "It's ok to load the node from JCR if necessary"
     * To avoid warning message about performance.
     *
     * This function should be use ONLY after Cache Filter or for key generation.
     * @return The JCR Node if found, null if not
     */
    public JCRNodeWrapper safeLoadNode() {
        lazyNodeFlagWarning = false;
        return getNode();
    }

    /**
     * Know if the JCR Node is available immediately, since the node may require lazy loading from JCR
     * @return true if JCR Node is available immediately
     */
    public boolean isNodeLoaded() {
        return node != null;
    }

    public void setNode(JCRNodeWrapper node) {
        this.node = node;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    private JCRSessionWrapper getSession() throws RepositoryException {
        return isNodeLoaded() ? node.getSession() : sessionWrapper;
    }

    public String getWorkspace() {
        try {
            return getSession().getWorkspace().getName();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public Locale getLocale() {
        try {
            return getSession().getLocale();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getContextConfiguration() {
        return contextConfiguration;
    }

    public String getResolvedTemplate() {
        if (StringUtils.isEmpty(resolvedTemplate)) {
            resolvedTemplate = getTemplate();
            try {
                if (node.isNodeType("jmix:renderable") && node.hasProperty("j:view")) {
                    resolvedTemplate = node.getProperty("j:view").getString();
                } 
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return resolvedTemplate;
    }

    public String getTemplate() {
        if (StringUtils.isEmpty(template)) {
            return "default";
        }
        return template;
    }

    public Set<String> getDependencies() {
        return dependencies;
    }

    public Set<String> getRegexpDependencies() {
        return regexpDependencies;
    }

    public List<String> getMissingResources() {
        return missingResources;
    }

    public Map<String, Serializable> getModuleParams() {
        return moduleParams;
    }

    public boolean hasWrapper() {
        return !wrappers.isEmpty();
    }

    public boolean hasWrapper(String wrapper) {
        return wrappers.contains(wrapper);
    }

    public String popWrapper() {
        return wrappers.pop();
    }

    public String pushWrapper(String wrapper) {
        return wrappers.push(wrapper);
    }

    public String getPath() {
        return nodePath + "." + getTemplate() + "." + templateType;
    }

    @Override
    public String toString() {
        String primaryNodeTypeName = null;
        try {
            primaryNodeTypeName = getNode().getPrimaryNodeTypeName();
        } catch (RepositoryException e) {
            logger.error("Error while retrieving node primary node type name", e);
        }
        return "Resource{" + "node=" + nodePath + ", primaryNodeTypeName='" + primaryNodeTypeName + "', templateType='" + templateType + "', template='" +
                getTemplate() + "', configuration='" + contextConfiguration + "'}";
    }

    /**
     * @deprecated not used anymore
     */
    @Deprecated
    public void addOption(String wrapper, ExtendedNodeType nodeType) {
        options.add(new Option(wrapper, nodeType));
    }

    /**
     * @deprecated not used anymore
     */
    @Deprecated
    public List<Option> getOptions() {
        return options;
    }

    /**
     * @deprecated not used anymore
     */
    @Deprecated
    public boolean hasOptions() {
        return !options.isEmpty();
    }

    /**
     * @deprecated not used anymore
     */
    @Deprecated
    public void removeOption(ExtendedNodeType mixinNodeType) {
        options.remove(new Option("", mixinNodeType));
    }

    public ExtendedNodeType getResourceNodeType() {
        return resourceNodeType;
    }

    public void setContextConfiguration(String contextConfiguration) {
        this.contextConfiguration = contextConfiguration;
    }

    /**
     * Get the script for the current resource, if the script is null and renderContext is provide as parameter
     * we try do the resolution of the script, then store it in the current resource, so it's available for the rest
     * of the render chain.
     *
     * @param context renderContext, needed to resolve the script
     * @return the Script if one is found, null if not
     */
    public Script getScript(RenderContext context) throws RepositoryException {
        if (script == null && !scriptNotFound) {
            try {
                script = RenderService.getInstance().resolveScript(this, context);
            } catch (TemplateNotFoundException e) {
                scriptNotFound = true;
                logger.debug("Script not found for resource: ", this.getPath());
                // do nothing keep the current script to null, script can be null
            }
        }
        return script;
    }

    public String getNodePath() {
        return nodePath;
    }

    public void setResourceNodeType(ExtendedNodeType resourceNodeType) {
        this.resourceNodeType = resourceNodeType;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Resource resource = (Resource) o;

        if (nodePath != null ? !nodePath.equals(resource.nodePath) : resource.nodePath != null) {
            return false;
        }
        if (templateType != null ? !templateType.equals(resource.templateType) : resource.templateType != null) {
            return false;
        }
        if (template != null ? !template.equals(resource.template) : resource.template != null) {
            return false;
        }
        if (wrappers != null ? !wrappers.equals(resource.wrappers) : resource.wrappers != null) {
            return false;
        }
        if (options != null ? !options.equals(resource.options) : resource.options != null) {
            return false;
        }
        if (resourceNodeType != null ? !resourceNodeType.equals(resource.resourceNodeType) :
                resource.resourceNodeType != null) {
            return false;
        }
        if (moduleParams != null ? !moduleParams.equals(resource.moduleParams) : resource.moduleParams != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = isNodeLoaded() ? node.hashCode() : 0;
        result = 31 * result + (nodePath != null ? nodePath.hashCode() : 0);
        result = 31 * result + (templateType != null ? templateType.hashCode() : 0);
        result = 31 * result + (getResolvedTemplate() != null ? getResolvedTemplate().hashCode() : 0);
        result = 31 * result + (wrappers != null ? wrappers.hashCode() : 0);
        result = 31 * result + (options != null ? options.hashCode() : 0);
        result = 31 * result + (resourceNodeType != null ? resourceNodeType.hashCode() : 0);
        result = 31 * result + (moduleParams != null ? moduleParams.hashCode() : 0);
        return result;
    }

    @Deprecated
    public class Option implements Comparable<Option> {

        private final String wrapper;
        private final ExtendedNodeType nodeType;

        public Option(String wrapper, ExtendedNodeType nodeType) {
            this.wrapper = wrapper;
            this.nodeType = nodeType;
        }

        public ExtendedNodeType getNodeType() {
            return nodeType;
        }

        public String getWrapper() {
            return wrapper;
        }

        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(Option o) {
            return nodeType.getName().compareTo(o.getNodeType().getName());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Option option = (Option) o;

            return nodeType.getName().equals(option.nodeType.getName());

        }

        @Override
        public int hashCode() {
            return nodeType.getName().hashCode();
        }
    }
}
