package org.jahia.bundles.securityfilter.core.constraint;

import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.modulemanager.util.PropertiesValues;
import pl.touk.throwing.exception.WrappedException;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;

/**
 * Check for a permission on a specific node
 */
public class PermissionConstraint implements Constraint {
    private String nodePath;
    private String workspace;
    private String permission;

    public PermissionConstraint(String nodePath, String workspace, String permission) {
        this.nodePath = nodePath;
        this.workspace = workspace;
        this.permission = permission;
    }

    public static Constraint build(PropertiesValues grantValues) {
        if (grantValues.getKeys().contains("user_permission") && grantValues.getKeys().contains("path")) {
            return new PermissionConstraint(grantValues.getProperty("path"), grantValues.getProperty("workspace"), grantValues.getProperty("user_permission"));
        }

        return null;
    }

    @Override
    public boolean isValid(HttpServletRequest request) {
        try {
            JCRNodeWrapper node = JCRSessionFactory.getInstance().getCurrentUserSession(workspace).getNode(nodePath);
            return node.hasPermission(permission);
        } catch (WrappedException | RepositoryException e) {
            return false;
        }
    }
}
