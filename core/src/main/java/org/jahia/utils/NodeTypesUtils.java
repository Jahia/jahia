/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.utils;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.jcr.nodetype.NodeTypeIterator;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class to manipulate nodetypes
 */
public class NodeTypesUtils {

    private static Logger logger = LoggerFactory.getLogger(NodeTypesUtils.class);


    /**
     * Utility method that build a list of jmix:droppableContent nodeTypes for a given path as a tree.
     * @param nodeTypes is a list of nodeTypes to return.
     * @param excludedNodeTypes is a list of nodeTypes to filter out.
     * @param includeSubTypes is a boolean, if true it checks the nodeTypes' sub types.
     * @param path on witch we check the available types.
     * @param session JCR Session
     * @param uiLocale current UI locale
     * @return a list of trees of jmix:droppableContent nodeTypes for a given path.
     * @throws RepositoryException
     */
    public static Set<NodeTypeTreeEntry> getContentTypesAsTree(List<String> nodeTypes, final List<String> excludedNodeTypes, final boolean includeSubTypes, String path, JCRSessionWrapper session, Locale uiLocale) throws RepositoryException {
        List<JahiaTemplatesPackage> packages = new ArrayList<JahiaTemplatesPackage>();
        JCRSiteNode site = session.getNode(path).getResolveSite();
        if (site.isNodeType("jnt:module")) {
            packages.add(site.getTemplatePackage());
        } else {
            for (String s : site.getInstalledModules()) {
                JahiaTemplatesPackage aPackage = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageById(s);
                packages.add(aPackage);
            }
        }

        for (int i = 0; i < packages.size(); i++) {
            JahiaTemplatesPackage aPackage = packages.get(i);
            if (aPackage != null) {
                for (JahiaTemplatesPackage dep : aPackage.getDependencies()) {
                    if (!packages.contains(dep)) {
                        packages.add(dep);
                    }
                }
            }
        }

        List<ExtendedNodeType> types = new ArrayList<>();
        for (JahiaTemplatesPackage pkg : packages) {
            if (pkg != null) {
                for (NodeTypeIterator nti = NodeTypeRegistry.getInstance().getNodeTypes(pkg.getId()); nti.hasNext(); ) {
                    ExtendedNodeType extendedNodeType = (ExtendedNodeType) nti.nextNodeType();
                    if (isValidNodeType(extendedNodeType, nodeTypes, excludedNodeTypes, includeSubTypes, site)) {
                        types.add(extendedNodeType);
                    }
                }
                if (pkg.isDefault()) {
                    for (NodeTypeIterator nti = NodeTypeRegistry.getInstance().getNodeTypes("system-jahia"); nti.hasNext(); ) {
                        ExtendedNodeType extendedNodeType = (ExtendedNodeType) nti.nextNodeType();
                        if (isValidNodeType(extendedNodeType, nodeTypes, excludedNodeTypes, includeSubTypes, site)) {
                            types.add(extendedNodeType);
                        }
                    }
                }
            }
        }

        Map<ExtendedNodeType, List<ExtendedNodeType>> r = new HashMap<ExtendedNodeType, List<ExtendedNodeType>>();
        for (ExtendedNodeType nt : types) {
            if (!nt.isMixin() && !nt.isAbstract()) {
                ExtendedNodeType parent = findFolder(nt);
                if (!r.containsKey(parent)) {
                    r.put(parent, new ArrayList<>());
                }
                r.get(parent).add(nt);
            }
        }

        Set<NodeTypeTreeEntry> roots = new TreeSet<>();
        for (Map.Entry<ExtendedNodeType, List<ExtendedNodeType>> entry : r.entrySet()) {
            ExtendedNodeType entryType = entry.getKey() != null ? entry.getKey() : NodeTypeRegistry.getInstance().getNodeType("nt:base");
            NodeTypeTreeEntry nt = new NodeTypeTreeEntry(entryType, uiLocale);
            roots.add(nt);

            List<NodeTypeTreeEntry> children = new ArrayList<>(entry.getValue().size());
            for (ExtendedNodeType type : entry.getValue()) {
                children.add(new NodeTypeTreeEntry(type, uiLocale));
            }

            disambiguateLabels(children);

            for (NodeTypeTreeEntry type : children) {
                nt.add(type);
            }
        }

        if (roots.size() == 1) {
            NodeTypeTreeEntry singleEntry = roots.iterator().next();
            if (singleEntry.getNodeType().isMixin() || singleEntry.getName().equals("nt:base")) {
                Set<NodeTypeTreeEntry> l = singleEntry.getChildren();
                roots.clear();
                roots.addAll(l);
            }
        }
        return roots;
    }

    /**
     * Check if the provided type match permission requirements on the provided node
     * @param typename type to check
     * @param node node on witch reading the permission
     * @return true if the type is allowed
     * @throws RepositoryException
     */
    public static boolean checkPermissionForType(String typename, JCRNodeWrapper node) throws RepositoryException {
        ExtendedNodeType type = NodeTypeRegistry.getInstance().getNodeType(typename);
        return checkPermissionForType(type, node);
    }

    /*
     * Appends its name to a {@link GWTJahiaNodeType}'s label for disambiguation if any sibling has the same one.
     */
    private static Collection<NodeTypeTreeEntry> disambiguateLabels(Collection<NodeTypeTreeEntry> nodeTypes) {
        List<NodeTypeTreeEntry> ambiguousNodeTypes = nodeTypes
                .stream()
                .collect(Collectors.groupingBy(NodeTypeTreeEntry::getLabel))
                .values()
                .stream()
                .filter(l -> l.size() > 1)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        for (NodeTypeTreeEntry nodeType : ambiguousNodeTypes) {
            nodeType.setLabel(nodeType.getLabel() + " (" + nodeType.getName() + ")");
        }
        return nodeTypes;
    }

    private static ExtendedNodeType findFolder(ExtendedNodeType nt) {
        if (!"jmix:droppableContent".equals(nt.getName()) && nt.isNodeType("jmix:droppableContent")) {
            if (logger.isDebugEnabled()) {
                logger.debug("Detected component type {}", nt.getName());
            }

            ExtendedNodeType[] supertypes = nt.getSupertypes();
            for (int i = supertypes.length - 1; i >= 0; i--) {
                ExtendedNodeType st = supertypes[i];
                if (st.isMixin() && !st.getName().equals("jmix:droppableContent")
                        && st.isNodeType("jmix:droppableContent")) {
                    return st;
                }
            }
        }
        return null;
    }

    private static boolean isValidNodeType(ExtendedNodeType ent, List<String> nodeTypes, List<String> excludedNodeTypes, boolean includeSubTypes, JCRNodeWrapper node) throws RepositoryException {
        if (ent == null) {
            return false;
        }

        if (includeSubTypes) {
            if (isNodeType(nodeTypes, ent) && checkPermissionForType(ent, node)) {
                return excludedNodeTypes == null || !isNodeType(excludedNodeTypes, ent);
            }
        } else {
            if (nodeTypes == null) {
                return false;
            }
            for (String nodeType : nodeTypes) {
                if (ent.getName().equals(nodeType) && checkPermissionForType(ent, node)) {
                    return excludedNodeTypes == null || !isNodeType(excludedNodeTypes, ent);
                }
            }
        }
        return false;
    }

    private static boolean checkPermissionForType(ExtendedNodeType type, JCRNodeWrapper node) throws NoSuchNodeTypeException {
        ExtendedNodeType[] supertypesArray = type.getSupertypes();
        if (supertypesArray.length == 0) {
            // nothing to check
            return true;
        }
        Set<ExtendedNodeType> superTypes = new HashSet<>(supertypesArray.length);
        superTypes.addAll(Arrays.asList(supertypesArray));
        NodeTypeIterator it = NodeTypeRegistry.getInstance().getNodeType("jmix:accessControllableContent").getDeclaredSubtypes();

        boolean allowed = true;
        while (it.hasNext()) {
            ExtendedNodeType next = (ExtendedNodeType) it.next();
            if (superTypes.contains(next)) {
                allowed = node.hasPermission("component-" + next.getName().replace(":", "_"));
                // Keep only last (nearest) accessControllableContent mixin if type inherits from multiple ones, so continue looping
            }
        }
        return allowed;
    }

    private static boolean isNodeType(List<String> nodeTypes, ExtendedNodeType type) {
        if (nodeTypes != null) {
            for (String nodeType : nodeTypes) {
                if (type.isNodeType(nodeType)) {
                    return true;
                }
            }
            return false;
        }

        return true;
    }

}
