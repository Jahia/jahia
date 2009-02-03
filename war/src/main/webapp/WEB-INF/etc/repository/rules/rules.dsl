[when]The current user belongs to a group=g : Group (groupName : name) from user.groups
[when]The current user has a property named {userproperty}=userProperty : UserProperty( name == "{userproperty}", propertyValue : value ) from user.properties

[when]A new node is created=node : NodeWrapper ( )
[when]A new node "{name}" is created=node : NodeWrapper ( name == "{name}")
[when]- the mimetype is {mimetype}=mimeType == "{mimetype}"
[when]- the mimetype is not {mimetype}=mimeType != "{mimetype}"
[when]- the mimetype matches {mimetype}=mimeType matches "{mimetype}"
[when]- it has the extension type {type}=types contains "{type}"
[when]- it has the type {type}=types contains "{type}"
[when]- it is in {path}=path matches "{path}/*"

[when]A file content has been modified=property : PropertyWrapper ( name == "jcr:data", node : node )

[when]A variable {name} has been extracted=ExtractedVariable ( node == node, name == "{name}", {name} : value )

[when]The type {name} has been assigned to a node=m : PropertyWrapper ( name == "jcr:mixinTypes", stringValues contains "{name}", node : node )

[when]The {node} has a property {property}=property : PropertyWrapper ( name == "{property}" , propertyValue : stringValue ) from {node}.properties
[when]The property {property} is not defined for the {node}=not ( PropertyWrapper ( name == "{property}" ) from {node}.properties )

[when]A property has been set on a node=property : PropertyWrapper ( propertyName : name, propertyValue : stringValue , node : node )
[when]- its name is not {name}=name != "{name}"
[when]- the node has the type {type}=node.types contains "{type}"
[when]A property {property} has been set on a node=property : PropertyWrapper ( name == "{property}" , propertyValue : stringValue , node : node )
[when]- the value is {value}=stringValue == {value}
[when]The property {property} has not been modified yet on the {node}=not PropertyWrapper ( name=="{property}" , node=={node} )
[when]A node has been published=property : PropertyWrapper ( name == "j:workflowState" , stringValue == "active" , node : node )
[when]- the node has the type {type}=node.types contains "{type}"

[when]The node has a parent=parent : NodeWrapper () from node.parent
[when]- the mimetype is {mimetype}=mimeType == "{mimetype}"
[when]- the mimetype is not {mimetype}=mimeType != "{mimetype}"
[when]- the mimetype matches {mimetype}=mimeType matches "{mimetype}"
[when]- it has the extension type {type}=types contains "{type}"

[then]Get the ancestor "{name}" of type {type}=NodeWrapper {name} = node.getAncestor("{type}");

[when]The {node} has a child=child : NodeWrapper ( ) from node.childNodes
[when]- the mimetype is {mimetype}=mimeType == "{mimetype}"
[when]- the mimetype is not {mimetype}=mimeType != "{mimetype}"
[when]- the mimetype matches {mimetype}=mimeType matches "{mimetype}"
[when]- it has the extension type {type}=types contains "{type}"

[then]Add the type {type}=node.addType ( "{type}", drools );

[then]Create a new node {nodename} under the {node}=NodeWrapper {nodename} = new NodeWrapper({node}, "{nodename}", null, drools);insert ({nodename});
[then]Create a new folder {nodename} under the {node}=NodeWrapper {nodename} = new NodeWrapper({node}, "{nodename}", "jnt:folder", drools);insert ({nodename});
[then]Create a new node {nodename} of type {type} under the {node}=NodeWrapper {nodename} = new NodeWrapper({node}, "{nodename}", "{type}", drools);insert ({nodename});

[then]Set the property {property} of the {node} with the name of the current user=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", user.getName(), drools));
[then]Set the property {property} of the {node} with the name of the node=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", provider.decodeInternalName(node.getName()), drools));
[then]Set the property {property} of the {node} with the path of the node=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", provider.decodeInternalName(node.getPath()), drools));
[then]Set the property {property} of the {node} with the value of that property=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", propertyValue, drools));
[then]Set the property {property} of the {node} with the current time=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", new java.util.Date(), drools));
[then]Set the property {property} of the {node} with the value of {variable}=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", {variable}, drools));
[then]Set the property {property} of the {node} with the value "{value}"=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", "{value}", drools));
[then]Remove this property=insert (new DeletedPropertyWrapper(property, drools));

[then]Assign permissions "{perms}" on the {node} to this group=service.setPermissions({node},"g:" + groupName + ":{perms}", drools);
[then]Assign permissions "{perms}" on the {node} to a group matching that property=service.setPermissions({node},"g:" + propertyValue + ":{perms}", drools);
[then]Assign permissions "{perms}" on the {node} to the group {group}=service.setPermissions({node},"g:" + {group} + ":{perms}", drools);
[then]Assign permissions "{perms}" on the {node} to the current user=service.setPermissions({node},"u:" + user.getName() + ":{perms}", drools);
[then]Assign permissions on the {node} from the property value=service.setPermissions({node},propertyValue, drools);
[then]Revoke all permissions on the {node}=service.revokeAllPermissions({node});
[then]Break all ACL inheritance on the {node}=service.setAclInheritanceBreak({node},true);
[then]Restore ACL inheritance on the {node}=service.setAclInheritanceBreak({node},false);

[then]Extract the properties from the file=extractionService.extractProperties(node, drools);
[then]Create an image "{name}" of size {size}=imageService.addThumbnail(node, "{name}",{size}, drools);

[then]Set the property {property} of the {node} with the height of the image = imageService.setHeight({node}, "{property}", drools);
[then]Set the property {property} of the {node} with the width of the image = imageService.setWidth({node}, "{property}", drools);

[then]Import the node=service.importNode(node,drools);

[then]Log {message} = logger.info({message});

[then]Fire {eventType} notification event for {node}=service.notify({node}, "{eventType}", drools);

