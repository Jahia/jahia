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

package org.jahia.services.templates;

import java.io.IOException;
import java.util.*;

import javax.jcr.*;
import javax.jcr.nodetype.NodeTypeIterator;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.*;
import org.jahia.utils.LanguageCodeConverters;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Functions;
import com.google.common.collect.Ordering;

/**
 * Components service.
 *
 * @author Sergiy Shyrkov
 */
public class ComponentRegistry {

    private static final Ordering<String> CASE_INSENSITIVE_ORDERING = Ordering.from(String.CASE_INSENSITIVE_ORDER);

    private static final String JMIX_DROPPABLE_CONTENT = "jmix:droppableContent";

    protected static final String JMIX_STUDIO_ONLY = "jmix:studioOnly";

    protected static final String JNT_COMPONENT = "jnt:component";

    protected static final String JNT_COMPONENT_FOLDER = "jnt:componentFolder";

    private static final String JNT_SIMPLE_COMPONENT = "jnt:simpleComponent";

    private static Logger logger = LoggerFactory.getLogger(ComponentRegistry.class);

    private static final String NODE_COMPONENTS = "components";

    private static boolean allowType(ExtendedNodeType t, List<String> includeTypeList,
                                     List<String> excludeTypeList) {
        boolean include = true;
        String typeName = t.getName();

        if (excludeTypeList != null && !excludeTypeList.isEmpty()) {
            include = !excludeTypeList.contains(typeName);
            if (include) {
                for (String s : excludeTypeList) {
                    if (t.isNodeType(s)) {
                        include = false;
                        break;
                    }
                }
            }
        }

        if (!include) {
            return false;
        }

        if (includeTypeList != null && !includeTypeList.isEmpty()) {
            include = false;
            include = includeTypeList.contains(typeName);
            if (!include) {
                for (String s : includeTypeList) {
                    if (t.isNodeType(s)) {
                        include = true;
                        break;
                    }
                }
            }
        }

        return include;
    }

    private static Map<String, String> getComponentTypes(JCRNodeWrapper node,
                                                         List<String> includeTypeList, List<String> excludeTypeList)
            throws PathNotFoundException, RepositoryException {

        Map<ExtendedNodeType, JCRNodeWrapper> typeComponentMap = new HashMap<ExtendedNodeType, JCRNodeWrapper>();
        List<JCRNodeWrapper> components = new LinkedList<JCRNodeWrapper>();

        JCRSiteNode resolvedSite = node.getResolveSite();

        if (resolvedSite != null && resolvedSite.hasNode("components")) {
            components.add(resolvedSite.getNode("components"));
        }
        if (resolvedSite != null && ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageByFileName(resolvedSite.getName()) != null) {

            for (JahiaTemplatesPackage dep : ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageByFileName(resolvedSite.getName()).getDependencies()) {
                String version = dep.getRootFolderWithVersion();
                String path = "/modules/" + version + "/components";

                if (resolvedSite.getSession().nodeExists(path)) {
                    components.add(resolvedSite.getSession().getNode(path));
                }
            }
        }
        for (int i = 0; i < components.size(); i++) {
            JCRNodeWrapper n = components.get(i);
            if (n.isNodeType("jnt:componentFolder")) {
                NodeIterator nodeIterator = n.getNodes();
                while (nodeIterator.hasNext()) {
                    JCRNodeWrapper next = (JCRNodeWrapper) nodeIterator.next();
                    components.add(next);
                }
            } else if (n.isNodeType("jnt:simpleComponent") /*&& n.hasPermission("useComponentForCreate")*/) {
                ExtendedNodeType t = NodeTypeRegistry.getInstance().getNodeType(n.getName());
                if (allowType(t, includeTypeList, excludeTypeList)) {
                    typeComponentMap.put(t, n);
                }
            }
        }

        String[] constraints = Patterns.SPACE.split(ConstraintsHelper.getConstraints(node));
        Set<ExtendedNodeType> finaltypes = new HashSet<ExtendedNodeType>();
        for (ExtendedNodeType type : typeComponentMap.keySet()) {
            for (String s : constraints) {
                if (!finaltypes.contains(type) && type.isNodeType(s)) {
                    finaltypes.add(type);
                    continue;
                }
            }
        }

        if (finaltypes.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, String> finalComponents = new HashMap<String, String>(finaltypes.size());
        for (ExtendedNodeType type : finaltypes) {
            finalComponents.put(type.getName(), typeComponentMap.get(type).getDisplayableName());
        }

        SortedMap<String, String> sortedComponents = new TreeMap<String, String>(
                CASE_INSENSITIVE_ORDERING.onResultOf(Functions.forMap(finalComponents)));
        sortedComponents.putAll(finalComponents);

        return sortedComponents;
    }

    public static Map<String, String> getComponentTypes(final JCRNodeWrapper node,
                                                        final List<String> includeTypeList, final List<String> excludeTypeList,
                                                        Locale displayLocale) throws PathNotFoundException, RepositoryException {

        long timer = System.currentTimeMillis();

        Map<String, String> sortedComponents = null;

        if (displayLocale == null || displayLocale.equals(node.getSession().getLocale())) {
            sortedComponents = getComponentTypes(node, includeTypeList, excludeTypeList);
        } else {
            sortedComponents = JCRTemplate.getInstance().doExecuteWithUserSession(
                    node.getSession().getUser().getUsername(),
                    node.getSession().getWorkspace().getName(), displayLocale,
                    new JCRCallback<Map<String, String>>() {

                        public Map<String, String> doInJCR(JCRSessionWrapper session)
                                throws RepositoryException {
                            return getComponentTypes(session.getNodeByUUID(node.getIdentifier()),
                                    includeTypeList, excludeTypeList);
                        }
                    });
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Execution took {} ms", (System.currentTimeMillis() - timer));
        }

        return sortedComponents;
    }

    private TemplatePackageRegistry templatePackageRegistry;

    private boolean registerComponent(JCRNodeWrapper components, ExtendedNodeType nt) throws RepositoryException {
        boolean updated = false;

        if (!components.hasNode(nt.getName())) {
            components.addNode(nt.getName(), JNT_SIMPLE_COMPONENT);
            updated = true;
        }

        return updated;
    }

    /**
     * Performs the registration of the components from the specified module into JCR tree.
     *
     * @param pkg     the module package
     * @param session current JCR session
     * @throws RepositoryException in case of a JCR error
     */
    public int registerComponents(JahiaTemplatesPackage pkg, JCRSessionWrapper session)
            throws RepositoryException {
        int count = 0;
        JCRNodeWrapper modules = null;
        if (!session.nodeExists("/modules")) {
            modules = session.getRootNode().addNode("modules", "jnt:modules");
        } else {
            modules = session.getNode("/modules");
        }

        List<ExtendedNodeType> types = new ArrayList<ExtendedNodeType>();
        for (String s : pkg.getDefinitionsFiles()) {
            try {
                if (pkg.getResource(s) != null) {
                    types.addAll(NodeTypeRegistry.getInstance().getDefinitionsFromFile(pkg.getResource(s), pkg.getRootFolder()));
                }
            } catch (ParseException e) {
                logger.error("Cannot parse definitions file "+s,e);
            } catch (IOException e) {
                logger.error("Cannot parse definitions file " + s, e);
            }
        }

        if (modules.hasNode(pkg.getRootFolderWithVersion())) {
            JCRNodeWrapper module = modules.getNode(pkg.getRootFolderWithVersion());
            boolean newDeployment = !module.hasNode(NODE_COMPONENTS);
            JCRNodeWrapper components = newDeployment ? module.addNode(NODE_COMPONENTS,
                    JNT_COMPONENT_FOLDER) : module.getNode(NODE_COMPONENTS);

            if (pkg.getRootFolder().equals("default")) {
                for (NodeTypeIterator nti = NodeTypeRegistry.getInstance().getNodeTypes("system-jahia"); nti.hasNext(); ) {
                    if (registerComponent(components, (ExtendedNodeType) nti.nextNodeType())) {
                        count++;
                    }
                }
            }

            for (ExtendedNodeType type : types) {
                if (registerComponent(components, type)) {
                    count++;
                }
            }
        } else {
            logger.warn("Unable to find module node for path {}."
                    + " Skip registering components for module {}.",
                    modules.getPath() + "/" + pkg.getRootFolder(), pkg.getName());
        }
        session.save();
        return count;
    }

    public void setTemplatePackageRegistry(TemplatePackageRegistry tmplPackageRegistry) {
        templatePackageRegistry = tmplPackageRegistry;
    }

}