[condition][]The current user belongs to a group=g : Group (groupName : name) from user.groups
[condition][]The current user has a property named {userproperty}=userProperty : UserProperty( name == "{userproperty}", propertyValue : value ) from user.properties
[condition][]A new node is created=node : NodeWrapper ( )
[condition][]A new node "{name}" is created=node : NodeWrapper ( name == "{name}")
[condition][]- the mimetype is {mimetype}=mimeType == "{mimetype}"
[condition][]- the mimetype is not {mimetype}=mimeType != "{mimetype}"
[condition][]- the mimetype matches {mimetype}=mimeType matches "{mimetype}"
[condition][]- it has the extension type {type}=types contains "{type}"
[condition][]- it has the type {type}=types contains "{type}"
[condition][]- it is in {path}=path matches "{path}/*"
[condition][]A file content has been modified=property : PropertyWrapper ( name == "jcr:data", node : node )
[condition][]A variable {name} has been extracted=ExtractedVariable ( node == node, name == "{name}", {name} : value )
[condition][]The type {name} has been assigned to a node=m : PropertyWrapper ( name == "jcr:mixinTypes", stringValues contains "{name}", node : node )
[condition][]The {node} has a property {property}=property : PropertyWrapper ( name == "{property}" , propertyValue : stringValue ) from {node}.properties
[condition][]The property {property} is not defined for the {node}=not ( PropertyWrapper ( name == "{property}" ) from {node}.properties )
[condition][]A property has been set on a node=property : PropertyWrapper ( propertyName : name, propertyValue : stringValue , node : node )
[condition][]- its name is not {name}=name != "{name}"
[condition][]- the node has the type {type}=node.types contains "{type}"
[condition][]A property {property} has been set on a node=property : PropertyWrapper ( name == "{property}" , propertyValue : stringValue , node : node )
[condition][]- the value is {value}=stringValue == {value}
[condition][]The property {property} has not been modified yet on the {node}=not PropertyWrapper ( name=="{property}" , node=={node} )
[condition][]A node has been published=property : PropertyWrapper ( name == "j:workflowState" , stringValue == "active" , node : node )
[condition][]- the node has the type {type}=node.types contains "{type}"
[condition][]The node has a parent=parent : NodeWrapper () from node.parent
[condition][]- the mimetype is {mimetype}=mimeType == "{mimetype}"
[condition][]- the mimetype is not {mimetype}=mimeType != "{mimetype}"
[condition][]- the mimetype matches {mimetype}=mimeType matches "{mimetype}"
[condition][]- it has the extension type {type}=types contains "{type}"
[consequence][]Get the ancestor "{name}" of type {type}=NodeWrapper {name} = node.getAncestor("{type}");
[condition][]The {node} has a child=child : NodeWrapper ( ) from node.childNodes
[condition][]- the mimetype is {mimetype}=mimeType == "{mimetype}"
[condition][]- the mimetype is not {mimetype}=mimeType != "{mimetype}"
[condition][]- the mimetype matches {mimetype}=mimeType matches "{mimetype}"
[condition][]- it has the extension type {type}=types contains "{type}"
[consequence][]Add the type {type}=node.addType ( "{type}", drools );
[consequence][]Create a new node {nodename} under the {node}=NodeWrapper {nodename} = new NodeWrapper({node}, "{nodename}", null, drools);insert ({nodename});
[consequence][]Create a new folder {nodename} under the {node}=NodeWrapper {nodename} = new NodeWrapper({node}, "{nodename}", "jnt:folder", drools);insert ({nodename});
[consequence][]Create a new node {nodename} of type {type} under the {node}=NodeWrapper {nodename} = new NodeWrapper({node}, "{nodename}", "{type}", drools);insert ({nodename});
[consequence][]Set the property {property} of the {node} with the name of the current user=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", user.getName(), drools));
[consequence][]Set the property {property} of the {node} with the name of the node=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", provider.decodeInternalName(node.getName()), drools));
[consequence][]Set the property {property} of the {node} with the path of the node=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", provider.decodeInternalName(node.getPath()), drools));
[consequence][]Set the property {property} of the {node} with the value of that property=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", propertyValue, drools));
[consequence][]Set the property {property} of the {node} with the current time=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", new java.util.Date(), drools));
[consequence][]Set the property {property} of the {node} with the value of {variable}=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", {variable}, drools));
[consequence][]Set the property {property} of the {node} with the value "{value}"=if ({node} != null) insert (new PropertyWrapper({node}, "{property}", "{value}", drools));
[consequence][]Remove this property=insert (new DeletedPropertyWrapper(property, drools));
[consequence][]Assign permissions "{perms}" on the {node} to this group=service.setPermissions({node},"g:" + groupName + ":{perms}", drools);
[consequence][]Assign permissions "{perms}" on the {node} to a group matching that property=service.setPermissions({node},"g:" + propertyValue + ":{perms}", drools);
[consequence][]Assign permissions "{perms}" on the {node} to the group {group}=service.setPermissions({node},"g:" + {group} + ":{perms}", drools);
[consequence][]Assign permissions "{perms}" on the {node} to the current user=service.setPermissions({node},"u:" + user.getName() + ":{perms}", drools);
[consequence][]Assign permissions on the {node} from the property value=service.setPermissions({node},propertyValue, drools);
[consequence][]Revoke all permissions on the {node}=service.revokeAllPermissions({node});
[consequence][]Break all ACL inheritance on the {node}=service.setAclInheritanceBreak({node},true);
[consequence][]Restore ACL inheritance on the {node}=service.setAclInheritanceBreak({node},false);
[consequence][]Extract the properties from the file=extractionService.extractProperties(node, drools);
[consequence][]Create an image "{name}" of size {size}=imageService.addThumbnail(node, "{name}",{size}, drools);
[consequence][]Set the property {property} of the {node} with the height of the image= imageService.setHeight({node}, "{property}", drools);
[consequence][]Set the property {property} of the {node} with the width of the image= imageService.setWidth({node}, "{property}", drools);
[consequence][]Import the node=service.importNode(node,drools);
[consequence][]Log {message}= logger.info({message});
[consequence][]Fire {eventType} notification event for {node}=service.notify({node}, "{eventType}", drools);
[condition][]A search result hit is present=searchHit : JahiaSearchHit ( )
[condition][]- the container is of type {containerType}=containerType == "{containerType}"
[consequence][]Append URL path "{urlPath}"=urlService.addURLPath(searchHit, "{urlPath}");
[consequence][]Append URL query-parameter "{parameterName}" with {parameterValue}=urlService.addURLQueryParameter(searchHit, "{parameterName}", {parameterValue});
