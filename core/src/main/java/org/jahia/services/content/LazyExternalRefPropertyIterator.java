package org.jahia.services.content;

import org.apache.jackrabbit.util.ChildrenCollectorFilter;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.util.*;

/**
 * A property iterator for the external reference properties. This was required as they are treated a little bit
 * differently than regular properties and we couldn't re-use the existing PropertyIterator implementation easily.
 */
public class LazyExternalRefPropertyIterator implements PropertyIterator {

    private Set<String> propertyNames;
    private long position = 0;
    private JCRNodeWrapper node;
    private Iterator<String> propertyNameIterator;

    public LazyExternalRefPropertyIterator(JCRNodeWrapper node, Set<String> propertyNames, String pattern) {
        this.node = node;
        this.propertyNames = propertyNames;
        if (pattern != null) {
            Set<String> filteredNames = new HashSet<String>();
            for (String name : propertyNames) {
                if (ChildrenCollectorFilter.matches(name, pattern)) {
                    filteredNames.add(name);
                }
            }
            this.propertyNames = filteredNames;
        }
        propertyNameIterator = this.propertyNames.iterator();
    }

    public Property nextProperty() {
        String propertyName = propertyNameIterator.next();
        ExtendedPropertyDefinition epd = null;
        try {
            epd = node.getApplicablePropertyDefinition(propertyName);
            Property property = ((JCRNodeWrapperImpl)node).retrieveExternalReferenceProperty(propertyName, epd);
            return property;
        } catch (RepositoryException e) {
            throw new NoSuchElementException("Could retrieve property " + propertyName + ": " + e.getMessage());
        }
    }

    public void skip(long skipNum) {
        if (skipNum > 0) {
            long leftToSkip = skipNum;
            while (propertyNameIterator.hasNext() && leftToSkip > 0) {
                propertyNameIterator.next();
                leftToSkip--;
            }
        }
    }

    public long getSize() {
        return propertyNames.size();
    }

    public long getPosition() {
        return position;
    }

    public boolean hasNext() {
        return propertyNameIterator.hasNext();
    }

    public Object next() {
        return nextProperty();
    }

    public void remove() {
        throw new UnsupportedOperationException("Remove operation is not supported on external reference property iterator");
    }
}
