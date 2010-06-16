[consequence][]Update site language permissions for {node}=roleService.updateSiteLangPermissions({node},drools);
[consequence][]Create a role {name} for each language based on role {base} for {node}=roleService.createTranslatorRole({node},{name},{base},drools);
[consequence][]Grant permission {permission} to role {role}=roleService.grantPermissionToRole({role},{permission},drools);
[consequence][]Grant a list of permissions {permissionList} to role {role}=roleService.grantPermissionsToRole({role},{permissionList},drools);
[consequence][]Grant permissions in group {permissionGroup} to role {role}=roleService.grantPermissionGroupToRole({role},{permissionGroup},drools);
[consequence][]Grant role {role} to user {user}=roleService.grantRoleToUser({user},{role},drools);
[consequence][]Grant role {role} to group {group}=roleService.grantRoleToGroup({group},{role},drools);
[consequence][]Grant role {role} to principals {principalList}=roleService.grantRoleToPrincipals({principalList},{role},drools);
