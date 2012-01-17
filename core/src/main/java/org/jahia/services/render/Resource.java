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

package org.jahia.services.render;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;

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

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(Resource.class);
    private JCRNodeWrapper node;
    private String templateType;
    private String template;
    private String contextConfiguration;
    private Stack<String> wrappers;

    private Set<String> dependencies;
    private List<String> missingResources;

    private List<Option> options;
    private ExtendedNodeType resourceNodeType;
    private Map<String, Serializable> moduleParams = new HashMap<String, Serializable>();
    private HashMap<String,Map<String,List<String>>> formInputs;
    private Set<String> regexpDependencies;

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
        this.templateType = templateType;
        this.template = template;
        this.contextConfiguration = contextConfiguration;
        dependencies = new LinkedHashSet<String>();
        dependencies.add(node.getCanonicalPath());
        regexpDependencies = new LinkedHashSet<String>();
        missingResources = new ArrayList<String>();
        wrappers = new Stack<String>();
        options = new ArrayList<Option>();
        formInputs = null;
    }

    public JCRNodeWrapper getNode() {
        return node;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public String getWorkspace() {
        try {
            return node.getSession().getWorkspace().getName();
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public Locale getLocale() {
        try {
            return node.getSession().getLocale();
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
//
//    public List<String> getTemplates() {
//        List<String> l = new ArrayList<String>();
//
//        if (template != null && template.length() > 0) {
//            l.add(template);
//            return l;
//        }
//        try {
//            if (node.isNodeType("jmix:renderable") && node.hasProperty("j:template")) {
//                l.add(node.getProperty("j:template").getString());
//            }
//        } catch (RepositoryException e) {
//            logger.error(e.getMessage(), e);
//        }
//        l.add("default");
//        return l;
//    }

    public String getResolvedTemplate() {
        String resolvedTemplate = template;
        if (StringUtils.isEmpty(resolvedTemplate)) {
            try {
                if (node.isNodeType("jmix:renderable") && node.hasProperty("j:view")) {
                    resolvedTemplate = node.getProperty("j:view").getString();
                } else {
                    resolvedTemplate = "default";
                }
            } catch (RepositoryException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return resolvedTemplate;
    }

    public String getTemplate() {
        if (StringUtils.isEmpty(template)) {
            template = "default";
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

    public String popWrapper() {
        return wrappers.pop();
    }

    public String pushWrapper(String wrapper) {
        return wrappers.push(wrapper);
    }


    @Override
    public String toString() {
        String primaryNodeTypeName = null;
        try {
            primaryNodeTypeName = node.getPrimaryNodeTypeName();
        } catch (RepositoryException e) {
            logger.error("Error while retrieving node primary node type name", e);
        }
        return "Resource{" + "node=" + node.getPath() + ", primaryNodeTypeName='" + primaryNodeTypeName + "', templateType='" + templateType + "', template='" +
                template + "', configuration='" + contextConfiguration + "'}";
    }

    public void addOption(String wrapper, ExtendedNodeType nodeType) {
        options.add(new Option(wrapper, nodeType));
    }

    public List<Option> getOptions() {
        return options;
    }

    public boolean hasOptions() {
        return !options.isEmpty();
    }

    public void removeOption(ExtendedNodeType mixinNodeType) {
        options.remove(new Option("", mixinNodeType));
    }

    public ExtendedNodeType getResourceNodeType() {
        return resourceNodeType;
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

        if (node != null ? !node.equals(resource.node) : resource.node != null) {
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
        int result = node != null ? node.hashCode() : 0;
        result = 31 * result + (templateType != null ? templateType.hashCode() : 0);
        result = 31 * result + (template != null ? template.hashCode() : 0);
        result = 31 * result + (wrappers != null ? wrappers.hashCode() : 0);
        result = 31 * result + (options != null ? options.hashCode() : 0);
        result = 31 * result + (resourceNodeType != null ? resourceNodeType.hashCode() : 0);
        result = 31 * result + (moduleParams != null ? moduleParams.hashCode() : 0);
        return result;
    }

    public void addFormInputs(String id, Map<String, List<String>> hiddenInputs) {
        if(formInputs==null) {
            formInputs = new HashMap<String, Map<String, List<String>>>();
        }
        formInputs.put(id,hiddenInputs);
    }

    public HashMap<String, Map<String, List<String>>> getFormInputs() {
        return formInputs;
    }

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
