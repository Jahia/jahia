/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.templates;

import com.google.common.base.Functions;
import com.google.common.collect.Ordering;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ConstraintsHelper;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ParseException;
import org.jahia.utils.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeTypeIterator;
import java.io.IOException;
import java.util.*;

/**
 * Components service.
 *
 * @author Sergiy Shyrkov
 */
public class ComponentRegistry {

    private static final Ordering<String> CASE_INSENSITIVE_ORDERING = Ordering.from(String.CASE_INSENSITIVE_ORDER);

    private static Logger logger = LoggerFactory.getLogger(ComponentRegistry.class);

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

    public static Map<String, String> getComponentTypes(final JCRNodeWrapper node,
                                                        final List<String> includeTypeList, final List<String> excludeTypeList,
                                                        Locale displayLocale) throws PathNotFoundException, RepositoryException {

        long timer = System.currentTimeMillis();

        if (displayLocale == null) {
            displayLocale = node.getSession().getLocale();
        }

        Map<String, String> finalComponents = new HashMap<String, String>();

        JCRSiteNode resolvedSite = node.getResolveSite();

        String[] constraints = Patterns.SPACE.split(ConstraintsHelper.getConstraints(node));

        Set<String> l = new HashSet<String>();
        l.add("system-jahia");

        if (resolvedSite != null) {
            l.addAll(resolvedSite.getInstalledModulesWithAllDependencies());
        }

        for (String aPackage : l) {
            for (ExtendedNodeType type : NodeTypeRegistry.getInstance().getNodeTypes(aPackage)) {
                if (allowType(type, includeTypeList, excludeTypeList)) {
                    for (String s : constraints) {
                        if (!finalComponents.containsKey(type.getName()) && type.isNodeType(s)) {
                            finalComponents.put(type.getName(), type.getLabel(displayLocale));
                            break;
                        }
                    }
                }
            }
        }

        SortedMap<String, String> sortedComponents = new TreeMap<String, String>(
                CASE_INSENSITIVE_ORDERING.onResultOf(Functions.forMap(finalComponents)));
        sortedComponents.putAll(finalComponents);

        if (logger.isDebugEnabled()) {
            logger.debug("Execution took {} ms", (System.currentTimeMillis() - timer));
        }

        return sortedComponents;
    }

    private boolean registerComponent(JCRNodeWrapper components, ExtendedNodeType nt) throws RepositoryException {
        if (!Arrays.asList(nt.getDeclaredSupertypeNames()).contains("jmix:accessControllableContent")) {
            return false;
        }

        boolean updated = false;

        final String name = "component-" + nt.getName().replace(':', '_');
        if (!components.hasNode(name)) {
            components.addNode(name, "jnt:permission");
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
                    types.addAll(NodeTypeRegistry.getInstance().getDefinitionsFromFile(pkg.getResource(s), pkg.getId()));
                }
            } catch (ParseException e) {
                logger.error("Cannot parse definitions file "+s,e);
            } catch (IOException e) {
                logger.error("Cannot parse definitions file " + s, e);
            }
        }

        if (modules.hasNode(pkg.getIdWithVersion())) {
            JCRNodeWrapper module = modules.getNode(pkg.getIdWithVersion());
            boolean emptyPermissions = false;
            if (!module.hasNode("permissions")) {
                emptyPermissions = true;
                module.addNode("permissions", "jnt:permission");
            }
            JCRNodeWrapper permissions = module.getNode("permissions");
            if (!permissions.hasNode("components")) {
                permissions.addNode("components", "jnt:permission");
            }
            JCRNodeWrapper components = permissions.getNode("components");

            if (pkg.isDefault()) {
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

            if (count == 0) {
                components.remove();
                if (emptyPermissions) {
                    permissions.remove();
                }
            }
        } else {
            logger.warn("Unable to find module node for path {}."
                    + " Skip registering components for module {}.",
                    modules.getPath() + "/" + pkg.getId(), pkg.getName());
        }
        session.save();
        return count;
    }

}