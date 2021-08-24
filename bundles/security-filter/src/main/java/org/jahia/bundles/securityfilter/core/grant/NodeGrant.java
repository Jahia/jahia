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
        boolean includesMatch = nodeTypes.isEmpty() || (node != null && nodeTypes.stream().anyMatch(ThrowingPredicate.unchecked(node::isNodeType)));
        boolean excludesMatch = excludesNodeTypes.isEmpty() || node == null || excludesNodeTypes.stream().noneMatch(ThrowingPredicate.unchecked(node::isNodeType));
        return includesMatch && excludesMatch;
    }

    private boolean pathMatches(Node node) {
        boolean includesMatch = pathPatterns.isEmpty() || (node != null && pathPatterns.stream().anyMatch(ThrowingPredicate.unchecked(pattern -> pattern.matcher(node.getPath()).matches())));
        boolean excludesMatch = excludedPathPatterns.isEmpty() || node == null || excludedPathPatterns.stream().noneMatch(ThrowingPredicate.unchecked(pattern -> pattern.matcher(node.getPath()).matches()));
        return includesMatch && excludesMatch;
    }

    private boolean workspaceMatches(Node node) throws RepositoryException {
        return workspaces.isEmpty() || (node != null && workspaces.contains(node.getSession().getWorkspace().getName()));
    }
}
