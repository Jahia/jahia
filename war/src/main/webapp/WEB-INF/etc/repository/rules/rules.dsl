[condition][]A file content has been modified=property : ChangedPropertyFact ( name == "jcr:data", contentnode : node ) and AddedNodeFact ( name == "jcr:content" ) from contentnode and node : AddedNodeFact () from contentnode.parent
[condition][]A new node "{name}" is created=node : AddedNodeFact ( name == "{name}")
[condition][]A new node is created=node : AddedNodeFact ( )
[condition][]A node is deleted=node : DeletedNodeFact ( )
[condition][]A node is moved=node : MovedNodeFact ( )
[condition][]A node is published=node : PublishedNodeFact ( )
[condition][]A node is copied=node : CopiedNodeFact ( )
[condition][]A node is a top copy=node : CopiedNodeFact ( top == true )
[condition][]A property has been set on a node=property : ChangedPropertyFact ( propertyName : name, propertyValue : stringValues , node : node )
[condition][]A property has been removed from a node=property : DeletedPropertyFact ( propertyName : name, node : node )
[condition][]A property {property} has been set on a node=property : ChangedPropertyFact ( name == "{property}" , propertyValue : stringValues , propertyValueAsString : stringValue , node : node )
[condition][]A property {property} has been removed from a node=property : DeletedPropertyFact ( name == "{property}", propertyName : name, node : node )
[condition][]A new page is created with template {template}=node : AddedNodeFact() ; property : ChangedPropertyFact ( name == "j:templateNode", stringValue matches "/.*/{template}", node == node )
[condition][]The metadata field "{name}" has been extracted=metadata : ExtractedVariable ( node == node.path, name == "{name}", {name} : value )
[condition][]The metadata field "{name}" identified by {field} has been extracted=metadata : ExtractedVariable ( node == node.path, name == "{name}", {field} : value )
[condition][]A metadata field has been extracted=metadata : ExtractedVariable ( node == node.path, metadataName : name, metadataValue : value, knownType : correspondingNodeTypeName, knownProperty : correspondingPropertyName )
[condition][]A well known metadata field has been extracted=metadata : ExtractedVariable ( node == node.path, correspondingPropertyName != null, metadataName : name, metadataValue : value, knownType : correspondingNodeTypeName, knownProperty : correspondingPropertyName )
[condition][]The current user belongs to a group=g : Group (groupName : name) from user.groups
[condition][]The current user has a property named {userproperty}=userProperty : UserProperty( name == "{userproperty}", propertyValue : value ) from user.properties
[condition][]The node has a parent=parent : AddedNodeFact () from node.parent
[condition][]The property {property} has not been modified yet on the {node}=not ChangedPropertyFact ( name=="{property}" , node=={node} )
[condition][]The property {property} has not the value "{value}" on the {node}=ChangedPropertyFact ( name == "{property}" , stringValue != "{value}" ) from {node}.properties
[condition][]The property {property} is not defined for the {node}=not ( ChangedPropertyFact ( name == "{property}" ) from {node}.properties )
[condition][]The type {name} has been assigned to a node=m : ChangedPropertyFact ( name == "jcr:mixinTypes", stringValues contains "{name}", node : node )
[condition][]The {node} has a child=child : AddedNodeFact ( ) from node.childNodes
[condition][]The {node} has a property {property}=property : ChangedPropertyFact ( name == "{property}" , propertyValue : stringValues ) from {node}.properties
[condition][]The rule {ruleName} is executing = job : JobRuleExecution ( ruleToExecute=={ruleName}) and node : AddedNodeFact() from job.node
[condition][]- it has the extension type {type}=types contains "{type}"
[condition][]- it has the type {type}=types contains "{type}"
[condition][]- it has no type {type}=types not contains "{type}"
[condition][]- it is in {path}=node.path matches "{path}/*"
[condition][]- its name is not {name}=name != "{name}"
[condition][]- its name is {name}=name == "{name}"
[condition][]- its name matches "{namePattern}"=name matches "{namePattern}"
[condition][]- the mimetype is not {mimetype}=mimeType != "{mimetype}"
[condition][]- the mimetype is {mimetype}=mimeType == "{mimetype}"
[condition][]- the mimetype matches {mimetype}=mimeType matches "{mimetype}"
[condition][]- the mimetype group is "{typeGroups}" = eval(org.jahia.services.content.JCRContentUtils.isMimeTypeGroup(mimeType, "{typeGroups}"))
[condition][]- the node has the type {type}=node.types contains "{type}"
[condition][]- the parent has the type {type}=parent.types contains "{type}"
[condition][]- the value is not "{value}"=stringValue != "{value}"
[condition][]- the value is {value}=stringValue == {value}
[condition][]- in {workspaceName} workspace=node.workspace == "{workspaceName}"
[condition][]- not in operation {operation}=operationType != "{operation}"
[condition][]- in operation {operation}=operationType == "{operation}"
[condition][]- installed modules contains {module}=installedModules contains "{module}"
[condition][]A search result hit is present=searchHit : JCRNodeHit ( )
[condition][]- the node is of type {type}=type == "{type}"
[condition][]The {node} has not been added=not AddedNodeFact ( path == ({node}.getPath()) )
[condition][]The {node} has not been moved=not MovedNodeFact ( path == ({node}.getPath()) )
[condition][]The {node} has not been copied=not CopiedNodeFact ( path == ({node}.getPath()) )
[condition][]The {node} is not moved=not MovedNodeFact ( originalPath == ({node}.getPath()) )
[condition][]Not in operation {operation}=not OperationTypeFact( operationType == "{operation}")
[condition][]In operation {operation}=OperationTypeFact( operationType == "{operation}")
[consequence][]Append URL query-parameter "{parameterName}" with {parameterValue}=urlService.addURLQueryParameter(searchHit, "{parameterName}", {parameterValue});
[consequence][]Add the type {type}=node.addType ( "{type}", drools );
[consequence][]Remove the type {type}=node.removeType ( "{type}", drools );
[consequence][]Break all ACL inheritance on the {node}=service.setAclInheritanceBreak({node},true);
[consequence][]Create a new folder {nodename} under the {node}=AddedNodeFact {nodename} = new AddedNodeFact({node}, "{nodename}", "jnt:folder", drools);insert ({nodename});
[consequence][]Create a new node {nodename} of type {type} under the {node}=AddedNodeFact {nodename} = new AddedNodeFact({node}, "{nodename}", "{type}", drools);insert ({nodename});
[consequence][]Create a new node {nodename} under the {node}=AddedNodeFact {nodename} = new AddedNodeFact({node}, "{nodename}", null, drools);insert ({nodename});
[consequence][]Create a square thumbnail on reference "{name}" of size {size}=imageService.addSquareThumbnail(property, "{name}",{size}, drools);
[consequence][]Create a thumbnail on reference "{name}" of size {size}=imageService.addThumbnail(property, "{name}",{size}, drools);
[consequence][]Create an image "{name}" of size {size}=imageService.addThumbnail(node, "{name}",{size}, drools);
[consequence][]Dispose image=imageService.disposeImageForNode(node, drools);
[consequence][]Extract properties from the file=extractionService.extractProperties(node, drools);
[consequence][]Get the ancestor "{name}" of type {type}=AddedNodeFact {name} = node.getAncestor("{type}");
[consequence][]Import the node=service.importNode(node,drools);
[consequence][]Import file {xmlFile} into {node}=service.importXML(node, {xmlFile}, drools);
[consequence][]Log {message}= logger.info({message});
[consequence][]LogDebug {message}= logger.debug({message});
[consequence][]Remove this property=insert (new DeletedPropertyFact(property, drools));
[consequence][]Restore ACL inheritance on the {node}=service.setAclInheritanceBreak({node},false);
[consequence][]Revoke all permissions on the {node}=service.revokeAllPermissions({node});
[consequence][]Set not existing property {property} of the {node} with the current time=if ({node} != null) insert (new ChangedPropertyFact({node}, "{property}", new java.util.Date(), drools, false,false));
[consequence][]Set not existing property {property} of the {node} with the name of the current user=if ({node} != null) insert (new ChangedPropertyFact({node}, "{property}", user.getName(), drools, false,false));
[consequence][]Set the property {property} of the {node} with the current time=if ({node} != null) insert (new ChangedPropertyFact({node}, "{property}", new java.util.Date(), drools));
[consequence][]Set the property {property} of the {node} with the height of the image= imageService.setHeight({node}, "{property}", drools);
[consequence][]Set the property {property} of the {node} with the name of the current user=if ({node} != null) insert (new ChangedPropertyFact({node}, "{property}", user.getName(), drools));
[consequence][]Set the property {property} of the {node} with the name of the node=if ({node} != null) insert (new ChangedPropertyFact({node}, "{property}", provider.decodeInternalName(node.getName()), drools));
[consequence][]Set the property {property} of the {node} with the path of the node=if ({node} != null) insert (new ChangedPropertyFact({node}, "{property}", provider.decodeInternalName(node.getPath()), drools));
[consequence][]Set the property {property} of the {node} with the value "{value}"=if ({node} != null) insert (new ChangedPropertyFact({node}, "{property}", "{value}", drools));
[consequence][]Set the property {property} of the {node} with the value of that property=if ({node} != null) insert (new ChangedPropertyFact({node}, "{property}", propertyValue, drools));
[consequence][]Set the property {property} of the {node} with the value of {variable}=if ({node} != null) insert (new ChangedPropertyFact({node}, "{property}", {variable}, drools));
[consequence][]Set the property {property} of the {node} with the width of the image= imageService.setWidth({node}, "{property}", drools);
[consequence][]Set corresponding property with the value of the extracted {metadata}=if (node != null) {node.addType(metadata.getCorrespondingNodeTypeName(), drools ); insert (new ChangedPropertyFact(node, metadata.getCorrespondingPropertyName(), metadata.getValue(), drools, false)); }
[consequence][]Increment the property {property} of the {node}=service.incrementProperty(node,"{property}", drools);
[consequence][]Add the property value to the property {property} of the {node}=service.addToProperty(node,"{property}",propertyValue, drools);
[consequence][]Tag the {node} with the {tag}=service.addNewTag(node, {tag}, drools);
[consequence][]Execute the rule {ruleName} at {property} for the {node}=service.executeRuleLater(node, "{property}",{ruleName}, drools);
[consequence][]Execute the action {actionName} at {property} on the {node}=service.executeActionLater(node, "{property}",{actionName}, drools);
[consequence][]Execute the action {actionName} now on the {node}=service.executeActionNow(node,{actionName}, drools);
[consequence][]Cancel execution of action {actionName} on the {node}=service.cancelActionExecution(node, {actionName}, drools);
[consequence][]Schedule the background action {actionName} on the {node} using cron expression {cron}=service.scheduleAction(node, {actionName}, {cron}, drools);
[consequence][]Publish the {node}=service.publishNode({node}, drools);
[consequence][]Start the workflow {processKey} from {provider} on the {node}=service.startWorkflowOnNode(node,{processKey},{provider}, drools);
[consequence][]Enable auto-splitting for subnodes of the {node} into folders of type {folderNodeType} using configuration "{config}"=service.enableAutoSplitting(node, "{config}", "{folderNodeType}",drools);
[consequence][]Move to split folder {node}=service.moveToSplitFolder(node, drools);
[consequence][]Move subnodes of {node} to split folder=service.moveSubnodesToSplitFolder(node, drools);
[consequence][]Flush group cache=service.flushGroupCaches();
[consequence][]Flush all caches=service.flushAllCaches(drools);
[consequence][]Flush {cacheName}=service.flushCache("{cacheName}",drools);
[consequence][]Remove entry {cacheEntry} from cache {cacheName}=service.flushCacheEntry({cacheName},{cacheEntry},drools);
[consequence][]Notify new user with mail template "{template}"=notificationService.notifyNewUser(node,"{template}",drools);
[consequence][]Notify current user with mail template "{template}" from "{fromMail}" copy to "{ccList}" blind copy to "{bccList}"=notificationService.notifyCurrentUser(user,"{template}","{fromMail}","{ccList}","{bccList}",drools);
[consequence][]Notify current user with mail template "{template}" from "{fromMail}"=notificationService.notifyCurrentUser(user,"{template}","{fromMail}",drools);
[consequence][]Notify {user} with mail template "{template}" from "{fromMail}" copy to "{ccList}" blind copy to "{bccList}"=notificationService.notifyUser({user},"{template}","{fromMail}","{ccList}","{bccList}",drools);
[consequence][]Notify {user} user with mail template "{template}" from "{fromMail}"=notificationService.notifyUser({user},"{template}","{fromMail}",drools);
[consequence][]Notify {user} user with mail template "{template}" from {fromMail}=notificationService.notifyUser({user},"{template}",{fromMail},drools);
[consequence][]Store password history for user {user}=service.storeUserPasswordHistory({user}, drools);
[consequence][]Deploy module {module} on site {site}=service.deployModule("{module}",{site}, drools);
[consequence][]Grant role {role} on the {node} to the current user=service.grantRoleToUser({node}, user.getName(), "{role}", drools);
[consequence][]Grant role {role} on the {node} to the user {user}=service.grantRoleToUser({node}, "{user}", "{role}", drools);
[consequence][]Grant role {role} on the {node} to the group {group}=service.grantRoleToGroup({node}, "{group}", "{role}", drools);
[consequence][]Revoke role {role} from everybody on the {node}=service.revokeRoleFromEverybody({node}, "{role}", drools);
[consequence][]Create a permission in {path} named {name}=service.createPermission("{path}",{name}, drools);
[consequence][]Update the privileged users=service.updatePrivileges(node);
[consequence][]Update dependencies for module=service.updateDependencies(node);
[consequence][]Delete nodes of type {type} with property {property} referencing the {node}=service.deleteNodesWithReference("{type}", "{property}", {node} );
