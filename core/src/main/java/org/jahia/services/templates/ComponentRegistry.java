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

package org.jahia.services.templates;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.query.InvalidQueryException;

import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.content.JCRCallback;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRTemplate;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ConstraintsHelper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.utils.LanguageCodeConverters;
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
        if (resolvedSite.isNodeType(Constants.JAHIANT_VIRTUALSITE)
                && resolvedSite.hasProperty("j:dependencies")) {
            for (Value dep : resolvedSite.getProperty("j:dependencies").getValues()) {
                String path = "/templateSets/" + dep.getString() + "/components";
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
            } else if (n.isNodeType("jnt:simpleComponent") && n.hasPermission("useComponent")) {
                ExtendedNodeType t = NodeTypeRegistry.getInstance().getNodeType(n.getName());
                if (allowType(t, includeTypeList, excludeTypeList)) {
                    typeComponentMap.put(t, n);
                }
            }
        }

        String[] constraints = ConstraintsHelper.getConstraints(node).split(" ");
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
    
    private JCRNodeWrapper findComponent(JCRNodeWrapper components, String name,
            JCRSessionWrapper session) throws InvalidQueryException, RepositoryException {
        if (components.hasNode(name)) {
            return components.getNode(name);
        }
        JCRNodeWrapper found = null;
        for (NodeIterator ni = components.getNodes(); ni.hasNext();) {
            found = findComponent((JCRNodeWrapper) ni.nextNode(), name, session);
            if (found != null) {
                break;
            }
        }

        return found;
        // traversal seems 10x faster than the query below
        // return session
        // .getWorkspace()
        // .getQueryManager()
        // .createQuery(
        // "select * from [jmix:droppableContent] where localname()='" + name
        // + "' and isdescendantnode('" + components.getPath() + "')",
        // Query.JCR_SQL2).execute().getNodes().hasNext();
    }

    private boolean registerComponent(JCRSessionWrapper session, boolean newDeployment, JCRNodeWrapper components, ExtendedNodeType nt) throws RepositoryException {
        boolean created = false;

        List<Locale> locales = LanguageCodeConverters.getAvailableBundleLocales();

        if (!nt.isMixin() && !nt.isAbstract() && !JMIX_DROPPABLE_CONTENT.equals(nt.getName())
                && nt.isNodeType(JMIX_DROPPABLE_CONTENT)) {
            String name = nt.getName();
            if (logger.isDebugEnabled()) {
                logger.debug("Detected component type {}", name);
            }
            if (newDeployment || findComponent(components, name, session) == null) {
                JCRNodeWrapper folder = components;
                for (ExtendedNodeType st : nt.getSupertypes()) {
                    if (st.isMixin() && !st.getName().equals(JMIX_DROPPABLE_CONTENT)
                            && st.isNodeType(JMIX_DROPPABLE_CONTENT)) {
                        if (components.hasNode(st.getName())) {
                            folder = components.getNode(st.getName());
                        } else {
                            folder = components.addNode(st.getName(),JNT_COMPONENT_FOLDER);
                            for (Locale locale : locales) {
                                String label = st.getLabel(locale);
                                if (label != null) {
                                    folder.getOrCreateI18N(locale).setProperty("jcr:title", label);
                                }
                                String description = st.getDescription(locale);
                                if (description != null) {
                                    folder.getOrCreateI18N(locale).setProperty("jcr:description", description);
                                }
                            }
                        }
                        break;
                    }
                }
                if (!folder.hasNode(name)) {
                    JCRNodeWrapper comp = folder.addNode(name, JNT_SIMPLE_COMPONENT);

                    for (Locale locale : locales) {
                        String label = nt.getLabel(locale);
                        if (label != null) {
                            comp.getOrCreateI18N(locale).setProperty("jcr:title", label);
                        }
                        String description = nt.getDescription(locale);
                        if (description != null) {
                            folder.getOrCreateI18N(locale).setProperty("jcr:description", description);
                        }
                    }
                    if (nt.isNodeType(JMIX_STUDIO_ONLY)) {
                        comp.addMixin(JMIX_STUDIO_ONLY);
                    }
                    created = true;
                    if (logger.isDebugEnabled()) {
                        logger.debug("Created component node {}", comp.getPath());
                    }
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Component {} already exists", name);
                }
            }
        } else if (!nt.isMixin() && !nt.isAbstract()) {
            JCRNodeWrapper folder = components.hasNode("nonDroppableComponents") ?
                    components.getNode("nonDroppableComponents") : components.addNode("nonDroppableComponents",
                    JNT_COMPONENT_FOLDER);
            folder.addMixin(Constants.JAHIAMIX_HIDDEN_NODE);
            if (!folder.hasNode(nt.getName())) {
                folder.addNode(nt.getName(), JNT_SIMPLE_COMPONENT);
                created = true;
            }
        }
        return created;
    }

    /**
     * Performs the registration of the components from modules into JCR tree.
     */
    protected void registerComponents() {
        long timer = System.currentTimeMillis();
        logger.info("Start registering UI droppable components...");
        int newComponents = 0;
        try {
            newComponents = JCRTemplate.getInstance().doExecuteWithSystemSession(
                    new JCRCallback<Integer>() {
                        public Integer doInJCR(JCRSessionWrapper session)
                                throws RepositoryException {
                            int count = 0;
                            for (JahiaTemplatesPackage pkg : templatePackageRegistry
                                    .getAvailablePackages()) {
                                count = count + registerComponents(pkg, session);
                            }
                            if (count > 0) {
                                session.save();
                            }
                            return count;
                        }
                    });
        } catch (Exception e) {
            logger.error("Error registering components. Cause: " + e.getMessage(), e);
        }
        if (newComponents > 0) {
            logger.info("Registered {} new component(s) in {} ms.", newComponents,
                    (System.currentTimeMillis() - timer));
        } else {
            logger.info("No new components detected. Done in {} ms.",
                    (System.currentTimeMillis() - timer));
        }
    }
    
    /**
     * Performs the registration of the components from the specified module into JCR tree.
     * 
     * @param pkg
     *            the module package
     * @param session
     *            current JCR session
     * @throws RepositoryException
     *             in case of a JCR error
     */
    private int registerComponents(JahiaTemplatesPackage pkg, JCRSessionWrapper session)
            throws RepositoryException {
        int count = 0;
        JCRNodeWrapper modules = null;
        if (!session.nodeExists("/templateSets")) {
            modules = session.getRootNode().addNode("templateSets", "jnt:templateSets");
        } else {
            modules = session.getNode("/templateSets");
        }

        if (modules.hasNode(pkg.getRootFolder())) {
            JCRNodeWrapper module = modules.getNode(pkg.getRootFolder());
            boolean newDeployment = !module.hasNode(NODE_COMPONENTS);
            JCRNodeWrapper components = newDeployment ? module.addNode(NODE_COMPONENTS,
                    JNT_COMPONENT_FOLDER) : module.getNode(NODE_COMPONENTS);

            if (pkg.getRootFolder().equals("default")) {
                for (NodeTypeIterator nti = NodeTypeRegistry.getInstance().getNodeTypes("system-jahia"); nti
                        .hasNext();) {
                    if (registerComponent(session, newDeployment, components, (ExtendedNodeType) nti.nextNodeType())) {
                        count++;
                    }
                }
            }

            for (NodeTypeIterator nti = NodeTypeRegistry.getInstance().getNodeTypes(pkg.getName()); nti
                    .hasNext();) {
                if (registerComponent(session, newDeployment, components, (ExtendedNodeType) nti.nextNodeType())) {
                    count++;
                }
            }
        } else {
            logger.warn("Unable to find module node for path {}."
                    + " Skip registering components for module {}.",
                    modules.getPath() + "/" + pkg.getRootFolder(), pkg.getName());
        }

        return count;
    }

    public void setTemplatePackageRegistry(TemplatePackageRegistry tmplPackageRegistry) {
        templatePackageRegistry = tmplPackageRegistry;
    }

}