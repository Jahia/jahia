/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.workflow.jbpm;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.decorator.JCRGroupNode;
import org.jahia.services.content.decorator.JCRUserNode;
import org.jahia.services.preferences.user.UserPreferencesHelper;
import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.kie.api.task.model.Group;
import org.kie.api.task.model.OrganizationalEntity;
import org.kie.api.task.model.User;
import org.kie.internal.task.api.TaskIdentityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.persistence.Entity;
import javax.persistence.Id;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;

/**
 * Identity Manager for connecting jBPM to Jahia Users
 */
public class JBPMTaskIdentityService implements TaskIdentityService {
    private static transient Logger logger = LoggerFactory.getLogger(JBPMTaskIdentityService.class);
    protected JahiaGroupManagerService groupService;
    protected JahiaUserManagerService userService;

    public JBPMTaskIdentityService() {
        groupService = ServicesRegistry.getInstance().getJahiaGroupManagerService();
        userService = ServicesRegistry.getInstance().getJahiaUserManagerService();
    }

    public List<Group> findGroupsByUser(String userId) {
        List<Group> results = new ArrayList<Group>();
        JCRUserNode user = this.userService.lookupUserByPath(userId);
        if (user != null) {
            List<String> l = groupService.getMembershipByPath(user.getPath());
            for (String groupKey : l) {
                Group groupById = getGroupById(groupKey);
                if (groupById != null) {
                    results.add(groupById);
                }
            }
        }
        return results;
    }

    @Override
    public void addUser(User user) {
        throw new UnsupportedOperationException("addUser");
    }

    @Override
    public void addGroup(Group group) {
        throw new UnsupportedOperationException("addGroup");
    }

    @Override
    public void removeGroup(String groupId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeUser(String userId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<User> getUsers() {
        List<String> userKeyList = userService.getUserList();
        List<User> results = new ArrayList<User>();
        for (String userKey : userKeyList) {
            JCRUserNode jahiaUser = userService.lookupUserByPath(userKey);
            results.add(new UserImpl(jahiaUser.getPath(), jahiaUser.getPropertyAsString("firstName"), jahiaUser
                    .getPropertyAsString("lastName"), jahiaUser.getPropertyAsString("email"),
                    UserPreferencesHelper.areEmailNotificationsDisabled(jahiaUser)));
        }
        return results;
    }

    @Override
    public List<Group> getGroups() {
        List<Group> results = new ArrayList<Group>();
        List<String> groupKeyList = groupService.getGroupList();
        for (String groupKey : groupKeyList) {
            JCRGroupNode jahiaGroup = groupService.lookupGroupByPath(groupKey);
            results.add(new GroupImpl(jahiaGroup.getPath(), jahiaGroup.getName(), "jahia"));
        }
        return results;
    }

    @Override
    public User getUserById(String userId) {
        JahiaUserManagerService service = ServicesRegistry.getInstance().getJahiaUserManagerService();
        JCRUserNode user = service.lookupUserByPath(userId);
        if (user != null) {
            return new UserImpl(userId, user.getPropertyAsString("j:firstName"),
                    user.getPropertyAsString("j:lastName"), user.getPropertyAsString("j:email"),
                    UserPreferencesHelper.areEmailNotificationsDisabled(user));
        }
        return null;
    }

    @Override
    public Group getGroupById(String groupId) {
        JahiaGroupManagerService service = JahiaGroupManagerService.getInstance();
        JCRGroupNode group = service.lookupGroupByPath(groupId);
        if (group != null) {
            return new GroupImpl(groupId, group.getName(), "jahia");
        }
        return null;
    }

    @Override
    public OrganizationalEntity getOrganizationalEntityById(String entityId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Entity
    public class UserImpl implements User, Comparable<UserImpl> {
        private String id;
        private String givenName;
        private String familyName;
        private String businessEmail;
        private boolean emailNotifioncationsDisabled;

        UserImpl(String id, String givenName, String familyName, String businessEmail, boolean emailNotifioncationsDisabled) {
            this.id = id;
            this.givenName = givenName;
            this.familyName = familyName;
            this.businessEmail = businessEmail;
            this.emailNotifioncationsDisabled = emailNotifioncationsDisabled;
        }

        protected UserImpl() {
            super();
        }

        @Id
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getGivenName() {
            return givenName;
        }

        public void setGivenName(String givenName) {
            this.givenName = givenName;
        }

        public String getFamilyName() {
            return familyName;
        }

        public void setFamilyName(String familyName) {
            this.familyName = familyName;
        }

        public String getBusinessEmail() {
            return businessEmail;
        }

        public void setBusinessEmail(String businessEmail) {
            this.businessEmail = businessEmail;
        }

        /**
         * Compares this object with the specified object for order.  Returns a
         * negative integer, zero, or a positive integer as this object is less
         * than, equal to, or greater than the specified object.
         * <p/>
         * <p>The implementor must ensure <tt>sgn(x.compareTo(y)) ==
         * -sgn(y.compareTo(x))</tt> for all <tt>x</tt> and <tt>y</tt>.  (This
         * implies that <tt>x.compareTo(y)</tt> must throw an exception iff
         * <tt>y.compareTo(x)</tt> throws an exception.)
         * <p/>
         * <p>The implementor must also ensure that the relation is transitive:
         * <tt>(x.compareTo(y)&gt;0 &amp;&amp; y.compareTo(z)&gt;0)</tt> implies
         * <tt>x.compareTo(z)&gt;0</tt>.
         * <p/>
         * <p>Finally, the implementor must ensure that <tt>x.compareTo(y)==0</tt>
         * implies that <tt>sgn(x.compareTo(z)) == sgn(y.compareTo(z))</tt>, for
         * all <tt>z</tt>.
         * <p/>
         * <p>It is strongly recommended, but <i>not</i> strictly required that
         * <tt>(x.compareTo(y)==0) == (x.equals(y))</tt>.  Generally speaking, any
         * class that implements the <tt>Comparable</tt> interface and violates
         * this condition should clearly indicate this fact.  The recommended
         * language is "Note: this class has a natural ordering that is
         * inconsistent with equals."
         * <p/>
         * <p>In the foregoing description, the notation
         * <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the mathematical
         * <i>signum</i> function, which is defined to return one of <tt>-1</tt>,
         * <tt>0</tt>, or <tt>1</tt> according to whether the value of
         * <i>expression</i> is negative, zero or positive.
         *
         * @param o the object to be compared.
         * @return a negative integer, zero, or a positive integer as this object
         *         is less than, equal to, or greater than the specified object.
         * @throws ClassCastException if the specified object's type prevents it
         *                            from being compared to this object.
         */
        public int compareTo(UserImpl o) {
            return (getFamilyName() + getGivenName()).compareTo(o.getFamilyName() + o.getGivenName());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            UserImpl user = (UserImpl) o;

            return id.equals(user.id);

        }

        @Override
        public int hashCode() {
            return id.hashCode();
        }

        @Override
        public void writeExternal(ObjectOutput objectOutput) throws IOException {
            objectOutput.writeUTF(id);
            objectOutput.writeUTF(givenName);
            objectOutput.writeUTF(familyName);
            objectOutput.writeUTF(businessEmail);
            objectOutput.writeBoolean(emailNotifioncationsDisabled);
        }

        @Override
        public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
            id = objectInput.readUTF();
            givenName = objectInput.readUTF();
            familyName = objectInput.readUTF();
            businessEmail = objectInput.readUTF();
            try {
                emailNotifioncationsDisabled = objectInput.readBoolean();
            } catch (EOFException e) {
                // in case we are reading older version of the object
            }
        }

        /**
         * Returns <code>true</code> if the user has explicitly disabled e-mail
         * notification in the profile.
         * 
         * @return <code>true</code> if the user has explicitly disabled e-mail
         *         notification in the profile
         */
        public boolean areEmailNotificationsDisabled() {
            return emailNotifioncationsDisabled;
        }
    }

    @Entity
    public class GroupImpl implements Group {
        private String id;
        private String name;
        private String type;

        GroupImpl(String id, String name, String type) {
            this.id = id;
            this.name = name;
            this.type = type;
        }

        protected GroupImpl() {
        }

        @Id
        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        @Override
        public void writeExternal(ObjectOutput objectOutput) throws IOException {
            objectOutput.writeUTF(id);
            objectOutput.writeUTF(name);
            objectOutput.writeUTF(type);
        }

        @Override
        public void readExternal(ObjectInput objectInput) throws IOException, ClassNotFoundException {
            id = objectInput.readUTF();
            name = objectInput.readUTF();
            type = objectInput.readUTF();
        }
    }

}

