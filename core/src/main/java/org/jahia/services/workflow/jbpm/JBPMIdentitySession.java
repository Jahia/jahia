package org.jahia.services.workflow.jbpm;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.jbpm.api.identity.Group;
import org.jbpm.api.identity.User;
import org.jbpm.pvm.internal.identity.spi.IdentitySession;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Identity Manager for connecting jBPM to Jahia Users
 */
public class JBPMIdentitySession implements IdentitySession {
    protected JahiaGroupManagerService groupService;
    protected JahiaUserManagerService userService;

    public JBPMIdentitySession() {
        groupService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        userService = ServicesRegistry.getInstance().getJahiaUserManagerService();
    }

    public String createUser(String s, String s1, String s2, String s3) {
        throw new UnsupportedOperationException();
    }

    public User findUserById(String userId) {
        JahiaUserManagerService service = ServicesRegistry.getInstance().getJahiaUserManagerService();
        JahiaUser user = service.lookupUserByKey(userId);
        if (user != null) {
            Properties properties = user.getProperties();
            return new UserImpl(userId, properties.getProperty("firstname"), properties.getProperty("lastname"), properties.getProperty("email"));
        }
        return null;
    }

    public List<User> findUsersById(String... strings) {
        throw new UnsupportedOperationException();
    }

    public List<User> findUsers() {
        throw new UnsupportedOperationException();
    }

    public void deleteUser(String s) {
        throw new UnsupportedOperationException();
    }

    public String createGroup(String s, String s1, String s2) {
        throw new UnsupportedOperationException();
    }

    public List<User> findUsersByGroup(String s) {
        throw new UnsupportedOperationException();
    }

    public Group findGroupById(String groupId) {
        JahiaGroupManagerService service = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        JahiaGroup group = service.lookupGroup(groupId);
        if (group != null) {
            return new GroupImpl(groupId, group.getName(),"jahia");
        }
        return null;
    }

    public List<Group> findGroupsByUserAndGroupType(String s, String s1) {
        throw new UnsupportedOperationException();
    }

    public List<Group> findGroupsByUser(String userId) {
        List<Group> results = new ArrayList<Group>();
        JahiaUser user = this.userService.lookupUserByKey(userId);
        if (user != null) {
            List<String> l = groupService.getUserMembership(user);
            for (String groupKey : l) {
                results.add(findGroupById(groupKey));
            }
        }
        return results;
    }

    public void deleteGroup(String s) {
        throw new UnsupportedOperationException();
    }

    public void createMembership(String s, String s1, String s2) {
        throw new UnsupportedOperationException();
    }

    public void deleteMembership(String s, String s1, String s2) {
        throw new UnsupportedOperationException();
    }

    class UserImpl implements User {
        private String id;
        private String givenName;
        private String familyName;
        private String businessEmail;

        UserImpl(String id, String givenName, String familyName, String businessEmail) {
            this.id = id;
            this.givenName = givenName;
            this.familyName = familyName;
            this.businessEmail = businessEmail;
        }

        public String getId() {
            return id;
        }

        public String getGivenName() {
            return givenName;
        }

        public String getFamilyName() {
            return familyName;
        }

        public String getBusinessEmail() {
            return businessEmail;
        }
    }

    class GroupImpl implements Group {
        private String id;
        private String name;
        private String type;

        GroupImpl(String id, String name, String type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }
    }
}

