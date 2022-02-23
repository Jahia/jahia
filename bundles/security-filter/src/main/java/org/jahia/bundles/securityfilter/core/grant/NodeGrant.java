/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.bundles.securityfilter.core.grant;

import org.jahia.bundles.securityfilter.core.ParserHelper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.modulemanager.util.PropertiesValues;
import pl.touk.throwing.ThrowingPredicate;
import pl.touk.throwing.exception.WrappedException;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Check conditions on the context node
 */
public class NodeGrant implements Grant {
    private Set<String> nodeTypes;
    private Set<String> excludesNodeTypes;
    private Set<Pattern> pathPatterns;
    private Set<Pattern> excludedPathPatterns;
    private Set<String> workspaces;
    private String withPermission;

    private NodeGrant(Set<String> nodeTypes, Set<String> excludesNodeTypes, Set<Pattern> pathPatterns, Set<Pattern> excludedPathPatterns, Set<String> workspaces, String withPermission) {
        this.nodeTypes = nodeTypes;
        this.excludesNodeTypes = excludesNodeTypes;
        this.pathPatterns = pathPatterns;
        this.excludedPathPatterns = excludedPathPatterns;
        this.workspaces = workspaces;
        this.withPermission = withPermission;
    }

    public static Grant build(PropertiesValues grantValues) {
        PropertiesValues nodeValues = grantValues.getValues("node");
        if (!nodeValues.getKeys().isEmpty()) {
            return new NodeGrant(ParserHelper.buildSet(nodeValues, "nodeType"),
                    ParserHelper.buildSet(nodeValues, "excludedNodeType"),
                    ParserHelper.buildSet(nodeValues, "pathPattern").stream().map(Pattern::compile).collect(Collectors.toSet()),
                    ParserHelper.buildSet(nodeValues, "excludedPathPattern").stream().map(Pattern::compile).collect(Collectors.toSet()),
                    ParserHelper.buildSet(nodeValues, "workspace"),
                    nodeValues.getProperty("withPermission"));
        }
        if ("none".equals(grantValues.getProperty("node"))) {
            return new NodeGrant(Collections.emptySet(),
                    Collections.emptySet(),
                    Collections.emptySet(),
                    Collections.singleton(Pattern.compile(".*")),
                    Collections.emptySet(),
                    null);
        }

        return null;
    }

    @Override
    public boolean matches(Map<String, Object> query) {
        JCRNodeWrapper node = (JCRNodeWrapper) query.get("node");
        try {
            return permissionMatches(node) && nodeTypeMatches(node) && pathMatches(node) && workspaceMatches(node);
        } catch (WrappedException | RepositoryException e) {
            return false;
        }
    }

    private boolean permissionMatches(JCRNodeWrapper node) throws RepositoryException {
        if (withPermission == null) {
            return true;
        }
        return node != null && node.hasPermission(withPermission);
    }

    private boolean nodeTypeMatches(Node node) {
        Predicate<String> predicate = ThrowingPredicate.unchecked(nt -> node != null && node.isNodeType(nt));
        return Grant.anyMatch(nodeTypes, predicate) && Grant.noneMatch(excludesNodeTypes, predicate);
    }

    private boolean pathMatches(Node node) {
        Predicate<Pattern> predicate = ThrowingPredicate.unchecked(pattern -> node != null && pattern.matcher(node.getPath()).matches());
        return Grant.anyMatch(pathPatterns, predicate) && Grant.noneMatch(excludedPathPatterns, predicate);
    }

    private boolean workspaceMatches(Node node) throws RepositoryException {
        return workspaces.isEmpty() || (node != null && workspaces.contains(node.getSession().getWorkspace().getName()));
    }
}
