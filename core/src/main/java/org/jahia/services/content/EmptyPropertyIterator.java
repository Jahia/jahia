package org.jahia.services.content;

import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import java.util.NoSuchElementException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Feb 8, 2010
 * Time: 5:53:46 PM
 * To change this template use File | Settings | File Templates.
 */
public class EmptyPropertyIterator implements PropertyIterator {
    public Property nextProperty() {
        throw new NoSuchElementException();
    }

    public void skip(long skipNum) {
        throw new NoSuchElementException();
    }

    public long getSize() {
        return 0;
    }

    public long getPosition() {
        return 0;
    }

    public boolean hasNext() {
        return false;
    }

    public Object next() {
        throw new NoSuchElementException();
    }

    public void remove() {
    }
}
