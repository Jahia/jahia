package org.jahia.services.content;

import javax.jcr.observation.EventIterator;
import javax.jcr.observation.Event;


/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 25, 2009
 * Time: 1:59:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class JCRObservationManagerDispatcher extends DefaultEventListener {

    protected JCRStoreProvider provider;

    public void setProvider(JCRStoreProvider provider) {
        this.provider = provider;
    }

    public int getEventTypes() {
        return Event.NODE_ADDED + Event.NODE_REMOVED + Event.PROPERTY_ADDED + Event.PROPERTY_CHANGED + Event.PROPERTY_REMOVED + Event.NODE_MOVED;
    }

    public String getPath() {
        return "/";
    }

    public String[] getNodeTypes() {
        return null;
    }

    /**
     * This method is called when a bundle of events is dispatched.
     *
     * @param events The event set received.
     */
    public void onEvent(EventIterator events) {
        while (events.hasNext()) {
            Event event = (Event) events.next();
            
            JCRObservationManager.addEvent(event);
        }
    }
}
