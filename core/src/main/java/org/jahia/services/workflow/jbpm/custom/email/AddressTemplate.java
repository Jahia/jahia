package org.jahia.services.workflow.jbpm.custom.email;

/**
 * Created with IntelliJ IDEA.
 * User: loom
 * Date: 28.06.13
 * Time: 20:46
 * To change this template use File | Settings | File Templates.
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
