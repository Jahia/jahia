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

    @Override
    public String toString() {
        return (withPermission == null ? "" : String.format(" - permission: [%s]", withPermission)) +
                (nodeTypes == null || nodeTypes.isEmpty() ? "" : String.format(" - nodeTypes: [%s]", String.join(",", nodeTypes))) +
                (excludesNodeTypes == null || excludesNodeTypes.isEmpty() ? "" : String.format(" - excludesNodeTypes: [%s]", String.join(",", excludesNodeTypes))) +
                (pathPatterns == null || pathPatterns.isEmpty() ? "" : String.format(" - pathPatterns: [%s]", pathPatterns.stream().map(Pattern::toString).collect(Collectors.toSet()))) +
                (excludedPathPatterns == null || excludedPathPatterns.isEmpty() ? "" : String.format(" - excludedPathPatterns: [%s]", excludedPathPatterns.stream().map(Pattern::toString).collect(Collectors.toSet()))) +
                (workspaces == null || workspaces.isEmpty() ? "" : String.format(" - workspaces: [%s]", String.join(",", workspaces)));
    }
}
