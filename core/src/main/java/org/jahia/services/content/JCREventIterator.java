package org.jahia.services.content;

import javax.jcr.observation.Event;
import javax.jcr.observation.EventIterator;
import java.util.Iterator;

/**
* Created by IntelliJ IDEA.
* User: toto
* Date: Jan 25, 2010
* Time: 1:59:57 PM
* To change this template use File | Settings | File Templates.
*/
public class JCREventIterator extends RangeIteratorImpl implements EventIterator {
    private JCRSessionWrapper session;
    private int operationType;

    JCREventIterator(JCRSessionWrapper session, int operationType, Iterator iterator, long size) {
        super(iterator, size);
        this.session = session;
        this.operationType = operationType;
    }

    public JCRSessionWrapper getSession() {
        return session;
    }

    public int getOperationType() {
        return operationType;
    }

    /**
     * Returns the next <code>Event</code> in the iteration.
     *
     * @return the next <code>Event</code> in the iteration.
     * @throws java.util.NoSuchElementException
     *          if iteration has no more <code>Event</code>s.
     */
    public Event nextEvent() {
        return (Event) next();
    }
}
