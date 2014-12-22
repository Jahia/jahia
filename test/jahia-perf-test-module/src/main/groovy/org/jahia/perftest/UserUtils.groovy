package org.jahia.perftest

import org.jahia.api.Constants
import org.jahia.registries.ServicesRegistry
import org.jahia.services.content.JCRSessionFactory
import org.jahia.services.content.JCRSessionWrapper
import org.jahia.services.content.decorator.JCRUserNode

/**
 * Created by IntelliJ IDEA.
 * @author : rincevent
 * @since : JAHIA 6.1
 * Created : 22/12/14
 */
class UserUtils {

    def removeUsers(String userPrefix, String siteKey) {
        def userService = ServicesRegistry.instance.jahiaUserManagerService
        def JCRSessionWrapper session = JCRSessionFactory.instance.getCurrentSystemSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH, Locale.ENGLISH)
        def props = new Properties()
        props.put("username",userPrefix+"*")
        def users
        if(siteKey==null)
            users = userService.searchUsers(props, null, session)
        else
            users = userService.searchUsers(props, siteKey, null, session)
        users.each {user -> println "deleting user = $user.path";
            userService.deleteUser(user.path,session)
            session.save()
        }
        if(!users.isEmpty()){
            removeUsers(userPrefix, siteKey);
        }
    };
}
