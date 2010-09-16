package org.jahia.services.workflow;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Sep 16, 2010
 * Time: 5:37:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class WorkflowRule {
    private String providerKey;
    private String workflowDefinitionKey;

    private List<Permission> permissions;

    public WorkflowRule(String providerKey, String workflowDefinitionKey, List<Permission> permissions) {
        this.providerKey = providerKey;
        this.workflowDefinitionKey = workflowDefinitionKey;
        this.permissions = permissions;
    }

    public String getProviderKey() {
        return providerKey;
    }

    public String getWorkflowDefinitionKey() {
        return workflowDefinitionKey;
    }

    public List<Permission> getPermissions() {
        return permissions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WorkflowRule)) {
            return false;
        }

        WorkflowRule rule = (WorkflowRule) o;

        if (permissions != null ? !permissions.equals(rule.permissions) : rule.permissions != null) {
            return false;
        }
        if (providerKey != null ? !providerKey.equals(rule.providerKey) : rule.providerKey != null) {
            return false;
        }
        if (workflowDefinitionKey != null ? !workflowDefinitionKey.equals(rule.workflowDefinitionKey) :
                rule.workflowDefinitionKey != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = providerKey != null ? providerKey.hashCode() : 0;
        result = 31 * result + (workflowDefinitionKey != null ? workflowDefinitionKey.hashCode() : 0);
        result = 31 * result + (permissions != null ? permissions.hashCode() : 0);
        return result;
    }

    /**
     * Created by IntelliJ IDEA.
     * User: toto
     * Date: Sep 16, 2010
     * Time: 5:38:18 PM
     * To change this template use File | Settings | File Templates.
     */
    public static class Permission {
        private String path;
        private String type;
        private String name;
        private String principal;

        public Permission(String path, String type, String name, String principal) {
            this.path = path;
            this.type = type;
            this.name = name;
            this.principal = principal;
        }

        public String getPath() {
            return path;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getPrincipal() {
            return principal;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Permission)) {
                return false;
            }

            Permission that = (Permission) o;

            if (name != null ? !name.equals(that.name) : that.name != null) {
                return false;
            }
            if (path != null ? !path.equals(that.path) : that.path != null) {
                return false;
            }
            if (principal != null ? !principal.equals(that.principal) : that.principal != null) {
                return false;
            }
            if (type != null ? !type.equals(that.type) : that.type != null) {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode() {
            int result = path != null ? path.hashCode() : 0;
            result = 31 * result + (type != null ? type.hashCode() : 0);
            result = 31 * result + (name != null ? name.hashCode() : 0);
            result = 31 * result + (principal != null ? principal.hashCode() : 0);
            return result;
        }
    }
}
