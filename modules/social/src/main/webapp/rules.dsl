[consequence][]Add activity for user {user} with message {message}=socialService.addActivity({user},{message}, drools);
[consequence][]Send message {message} with subject {subject} from user {fromUser} to user {toUser}=socialService.sendMessage({fromUser}, {toUser}, {subject}, {message}, drools);