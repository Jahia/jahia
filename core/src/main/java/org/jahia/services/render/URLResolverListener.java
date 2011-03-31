package org.jahia.services.render;

import org.apache.jackrabbit.core.security.JahiaLoginModule;
import org.jahia.services.content.*;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;

/**
 * JCR listener to invalidate URL resolver caches
 * @todo This implementation is not optimal, we should try to perfom finer invalidations.
 */
public class URLResolverListener extends DefaultEventListener {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(URLResolverListener.class);

    private URLResolverFactory urlResolverFactory;

    @Override
    public int getEventTypes() {
        return Event.NODE_ADDED + Event.NODE_REMOVED + Event.NODE_MOVED;
    }

    public void onEvent(final EventIterator events) {
        if (urlResolverFactory == null) {
            return;
        }
        try {
            String userId = ((JCREventIterator)events).getSession().getUserID();
            if (userId.startsWith(JahiaLoginModule.SYSTEM)) {
                userId = userId.substring(JahiaLoginModule.SYSTEM.length());
            }

            JCRTemplate.getInstance().doExecuteWithSystemSession(userId, workspace, new JCRCallback() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    while (events.hasNext()) {
                        Event event = events.nextEvent();

                        if (isExternal(event)) {
                            continue;
                        }

                        String path = event.getPath();
                        if (event.getType() == Event.NODE_ADDED) {
                            nodeAdded(session, path);
                        } else if (event.getType() == Event.NODE_REMOVED) {
                            nodeRemoved(session, path);
                        } else if (event.getType() == Event.NODE_MOVED) {
                            nodeMoved(session, path);
                        }
                    }
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }
            });
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
        }

    }

    public void setUrlResolverFactory(URLResolverFactory urlResolverFactory) {
        this.urlResolverFactory = urlResolverFactory;
    }

    private void nodeAdded(JCRSessionWrapper session, String path) throws RepositoryException {
        urlResolverFactory.flushCaches();
    }

    private void nodeRemoved(JCRSessionWrapper session, String path) throws RepositoryException {
        urlResolverFactory.flushCaches();
    }

    private void nodeMoved(JCRSessionWrapper session, String path) {
        urlResolverFactory.flushCaches();
    }

}
