package org.jahia.services.content;

import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import java.util.List;

/**
 * Aggregate multiple properties iterators
 */
public class MultiplePropertyIterator extends MultipleIterator<PropertyIterator> implements PropertyIterator {

    public MultiplePropertyIterator(List<PropertyIterator> iterators, long limit) {
        super(iterators, limit);
    }

    @Override
    public Property nextProperty() {
        return (Property) next();
    }
}

