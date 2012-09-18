[consequence][]Send site message {message} = sitePubSubService.sendSiteMessage(node,{message}, drools);
[consequence][]Send message {message} to absolute {name}= sitePubSubService.sendAbsoluteMessage({name},{message}, drools);
[consequence][]Send message {message} to {node} channel= sitePubSubService.sendNodeMessage(node,{message}, drools);