package org.jahia.services.workflow.jbpm;

import org.jahia.services.usermanager.JahiaGroupManagerService;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.kie.internal.task.api.UserGroupCallback;

import java.util.List;

public class JahiaUserGroupCallback implements UserGroupCallback {

    protected JahiaGroupManagerService groupService;
    protected JahiaUserManagerService userService;

    public void setGroupService(JahiaGroupManagerService groupService) {
        this.groupService = groupService;
    }

    public void setUserService(JahiaUserManagerService userService) {
        this.userService = userService;
    }

    @Override
    public boolean existsUser(String userId) {
        return userService.lookupUserByKey(userId) != null;
    }

    @Override
    public boolean existsGroup(String groupId) {
        return groupService.lookupGroup(groupId) != null;
    }

    @Override
    public List<String> getGroupsForUser(String userId, List<String> groupIds, List<String> allExistingGroupIds) {
        return groupService.getUserMembership(userService.lookupUserByKey(userId));
    }
}
