[condition][]A file content has been modified=property : PropertyWrapper ( name == "jcr:data", contentnode : node ) and NodeWrapper ( name == "jcr:content" ) from contentnode and node : NodeWrapper () from contentnode.parent
[condition][]A new node "{name}" is created=node : NodeWrapper ( name == "{name}")
[condition][]A new node is created=node : NodeWrapper ( )
[condition][]A node is deleted=node : DeletedNodeWrapper ( )
[condition][]A property has been set on a node=property : PropertyWrapper ( propertyName : name, propertyValue : stringValues , node : node )
[condition][]A property has been removed from a node=property : DeletedPropertyWrapper ( propertyName : name, node : node )
[condition][]A property {property} has been set on a node=property : PropertyWrapper ( name == "{property}" , propertyValue : stringValues , propertyValueAsString : stringValue , node : node )
[condition][]A search result hit is present=searchHit : JahiaSearchHit ( )
[condition][]A variable {name} has been extracted=ExtractedVariable ( node == node, name == "{name}", {name} : value )
[condition][]The current user belongs to a group=g : Group (groupName : name) from user.groups
[condition][]The current user has a property named {userproperty}=userProperty : UserProperty( name == "{userproperty}", propertyValue : value ) from user.properties
[condition][]The node has a parent=parent : NodeWrapper () from node.parent
[condition][]The property {property} has not been modified yet on the {node}=not PropertyWrapper ( name=="{property}" , node=={node} )
[condition][]The property {property} has not the value "{value}" on the {node}=PropertyWrapper ( name == "{property}" , stringValue != "{value}" ) from {node}.properties
[condition][]The property {property} is not defined for the {node}=not ( PropertyWrapper ( name == "{property}" ) from {node}.properties )
[condition][]The type {name} has been assigned to a node=m : PropertyWrapper ( name == "jcr:mixinTypes", stringValues contains "{name}", node : node )
[condition][]The {node} has a child=child : NodeWrapper ( ) from node.childNodes
[condition][]The {node} has a property {property}=property : PropertyWrapper ( name == "{property}" , propertyValue : stringValues ) from {node}.properties
[condition][]The rule {ruleName} is executing = job : JobRuleExecution ( ruleToExecute=={ruleName}) and node : NodeWrapper() from job.node
[condition][]- it has the extension type {type}=types contains "{type}"
[condition][]- it has the type {type}=node.types contains "{type}"
[condition][]- it has the type {type}=types contains "{type}"
[condition][]- it is in {path}=path matches "{path}/*"
[condition][]- its name is not {name}=name != "{name}"
[condition][]- the mimetype is not {mimetype}=mimeType != "{mimetype}"
[condition][]- the mimetype is {mimetype}=mimeType == "{mimetype}"
[condition][]- the mimetype matches {mimetype}=mimeType matches "{mimetype}"
[condition][]- the node has the type {type}=node.types contains "{type}"
[condition][]- the parent has the type {type}=parent.types contains "{type}"
[condition][]- the value is not "{value}"=stringValue != "{value}"
[condition][]- the value is {value}=stringValue == {value}
[consequence][]Add the type {type}=node.addType ( "{type}", drools );
[consequence][]Remove the type {type}=node.removeType ( "{type}", drools );
[consequence][]Assign permissions "{perms}" on the {node} to a group matching that property=service.setPermissions({node},"g:" + propertyValue + ":{perms}", drools);
[consequence][]Assign permissions "{perms}" on the {node} to the current user=service.setPermissions({node},"u:" + user.getName() + ":{perms}", drools);
[consequence][]Assign permissions "{perms}" on the {node} to the user {user}=service.setPermissions({node},"u:" + {user} + ":{perms}", drools);
[consequence][]Assign permissions "{perms}" on the {node} to the group {group}=service.setPermissions({node},"g:" + {group} + ":{perms}", drools);
[consequence][]Assign permissions "{perms}" on the {node} to this group=service.setPermissions({node},"g:" + groupName + ":{perms}", drools);
[consequence][]Assign permissions on the {node} from the property value=service.setPermissions({node},propertyValue, drools);
[consequence][]Break all ACL inheritance on the {node}=service.setAclInheritanceBreak({node},true);
[consequence][]Create a new folder {nodename} under the {node}=NodeWrapper {nodename} = new NodeWrapper({node}, "{nodename}", "jnt:folder", drools);insert ({nodename});
[consequence][]Create a new node {nodename} of type {type} under the {node}=NodeWrapper {nodename} = new NodeWrapper({node}, "{nodename}", "{type}", drools);insert ({nodename});
[consequence][]Create a new node {nodename} under the {node}=NodeWrapper {nodename} = new NodeWrapper({node}, "{nodename}", null, drools);insert ({nodename});
[consequence][]Create a square thumbnail on reference "{name}" of size {size}=imageService.addSquareThumbnail(property, "{name}",{size}, drools);
[consequence][]Create a thumbnail on reference "{name}" of size {size}=imageService.addThumbnail(property, "{name}",{size}, drools);
[consequence][]Create an image "{name}" of size {size}=imageService.addThumbnail(node, "{name}",{size}, drools);
[consequence][]Extract the properties from the file=extractionService.extractProperties(node, drools);
[consequence][]Get the ancestor "{name}" of type {type}=NodeWrapper {name} = node.getAncestor("{type}");
[consequence][]Import the node=service.importNode(node,drools);
[consequence][]Import file {xmlFile} into {node}=service.importXML(node, {xmlFile}, drools);
[consequence][]Log {message}= logger.info({message});
[consequence][]Remove this property=insert (new DeletedPropertyWrapper(property, drools));
[consequence][]Restore ACL inheritance on the {node}=service.setAclInheritanceBreak({node},false);
[consequence][]Revoke all permissions on the {node}=service.revokeAllPermissions({node});
[consequence][]Set and copy to staging the property {property} of the {node} with the current time=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", new java.util.Date(), drools, true));
[consequence][]Set and copy to staging the property {property} of the {node} with the name of the current user=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", user.getName(), drools, true));
[consequence][]Set not existing property {property} of the {node} with the current time=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", new java.util.Date(), drools, false,false));
[consequence][]Set not existing property {property} of the {node} with the name of the current user=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", user.getName(), drools, false,false));
[consequence][]Set the property {property} of the {node} with the current time=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", new java.util.Date(), drools, false));
[consequence][]Set the property {property} of the {node} with the height of the image= imageService.setHeight({node}, "{property}", drools);
[consequence][]Set the property {property} of the {node} with the name of the current user=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", user.getName(), drools, false));
[consequence][]Set the property {property} of the {node} with the name of the node=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", provider.decodeInternalName(node.getName()), drools, false));
[consequence][]Set the property {property} of the {node} with the path of the node=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", provider.decodeInternalName(node.getPath()), drools, false));
[consequence][]Set the property {property} of the {node} with the value "{value}"=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", "{value}", drools, false));
[consequence][]Set the property {property} of the {node} with the value of that property=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", propertyValue, drools, false));
[consequence][]Set the property {property} of the {node} with the value of {variable}=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", {variable}, drools, false));
[consequence][]Set the property {property} of the {node} with the width of the image= imageService.setWidth({node}, "{property}", drools);
[consequence][]Increment the property {property} of the {node}=service.incrementProperty(node,"{property}", drools);
[consequence][]Add the property value to the property {property} of the {node}=service.addToProperty(node,"{property}",propertyValue, drools);
[consequence][]Tag the {node} with the {tag}=service.addNewTag(node, {tag}, drools);
[consequence][]Execute the rule {ruleName} at {property} for the {node}=service.executeLater(node, "{property}",{ruleName}, drools);
[consequence][]Create reusable {node}=service.createReusableComponent(node, drools);
