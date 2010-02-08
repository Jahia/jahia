package org.jahia.services.content;

import org.apache.log4j.Logger;

import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 8, 2010
 * Time: 5:47:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class PropertyIteratorImpl implements PropertyIterator {
    private static Logger logger = Logger.getLogger(PropertyIteratorImpl.class);
    private JCRNodeWrapper node;
    private PropertyIterator iterator;

    public PropertyIteratorImpl(PropertyIterator iterator, JCRNodeWrapper node) {
        this.iterator = iterator;
        this.node = node;
    }

    public Property nextProperty() {
        try {
            return node.getProvider().getPropertyWrapper(iterator.nextProperty(), node.getSession());
        } catch (RepositoryException e) {
            logger.error("",e);
        }
        return null;
    }

    public void skip(long skipNum) {
        iterator.skip(skipNum);
    }

    public long getSize() {
        return iterator.getSize();
    }

    public long getPosition() {
        return iterator.getPosition();
    }

    public boolean hasNext() {
        return iterator.hasNext();
    }

    public Object next() {
        return nextProperty();
    }

    public void remove() {
        iterator.remove();
    }
}
