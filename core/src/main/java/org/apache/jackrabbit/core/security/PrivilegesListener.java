package org.apache.jackrabbit.core.security;

import org.jahia.services.content.*;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 3/30/11
 * Time: 21:02
 * To change this template use File | Settings | File Templates.
 */
public class PrivilegesListener  extends DefaultEventListener implements ExternalEventListener {
    private static org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PrivilegesListener.class);

    @Override
    public int getEventTypes() {
        return Event.NODE_ADDED + Event.NODE_REMOVED;
    }

    @Override
    public String getPath() {
        return "/permissions";
    }

    public void onEvent(EventIterator events) {
        try {
            JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    JahiaPrivilegeRegistry.init(session);
                    return null;
                }
            });
        } catch (RepositoryException e) {
            logger.error("Cannot refresh permissions", e);
        }
    }
}
