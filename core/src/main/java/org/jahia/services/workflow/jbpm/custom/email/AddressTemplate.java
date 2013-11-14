package org.jahia.services.workflow.jbpm.custom.email;

/**
 * User: loom
 */
public class AddressTemplate {

    private String addresses;
    private String users;
    private String groups;

    public String getAddresses() {
        return addresses;
    }

    public void setAddresses(String addresses) {
        this.addresses = addresses;
    }

    public String getUsers() {
        return users;
    }

    public void setUsers(String users) {
        this.users = users;
    }

    public String getGroups() {
        return groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }
}
