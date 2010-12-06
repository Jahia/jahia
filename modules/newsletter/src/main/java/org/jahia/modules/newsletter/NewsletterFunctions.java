package org.jahia.modules.newsletter;

import org.jahia.services.SpringContextSingleton;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.notification.SubscriptionService;
import org.jahia.services.usermanager.JahiaUser;

import javax.jcr.RepositoryException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Dec 6, 2010
 * Time: 5:49:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class NewsletterFunctions {

    public static boolean hasSubscribed(JCRNodeWrapper target, JahiaUser user) {
        SubscriptionService service = (SubscriptionService) SpringContextSingleton.getInstance().getContext().getBean("subscriptionService");
        try {
            return service.getSubscription(target.getIdentifier(), user.getUserKey(), JCRSessionFactory.getInstance().getCurrentUserSession("live")) != null;
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return false;
    }

}
