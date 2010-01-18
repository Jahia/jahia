package org.jahia.services.content;

import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.util.*;

/**
 * Jahia's wrapper of the JCR <code>javax.jcr.PropertyIterator</code>.
 *
 * @author toto
 */
public class LazyPropertyIterator implements PropertyIterator, Map {
    private JCRNodeWrapper node;
    private Locale locale;
    private PropertyIterator propertyIterator;
    private PropertyIterator i18nPropertyIterator;
    private Property tempNext = null;

    public LazyPropertyIterator(JCRNodeWrapper node, PropertyIterator propertyIterator) {
        this.node = node;
        this.propertyIterator = propertyIterator;
        this.i18nPropertyIterator = new PropertyIteratorImpl(Collections.<JCRPropertyWrapperImpl>emptyList(),0);
    }

    public LazyPropertyIterator(JCRNodeWrapper node, PropertyIterator propertyIterator, PropertyIterator i18nPropertyIterator, Locale locale) {
        this.node = node;
        this.propertyIterator = propertyIterator;
        this.i18nPropertyIterator = i18nPropertyIterator;
        this.locale = locale;
    }

    public int size() {
        return (int) (propertyIterator.getSize() + i18nPropertyIterator.getSize());
    }

    public boolean isEmpty() {
        return propertyIterator.getSize() == 0 && i18nPropertyIterator.getSize() == 0;
    }

    public Property nextProperty() {
        try {
            if (tempNext != null) {
                Property res = tempNext;
                tempNext = null;
                return res;
            }

            if (propertyIterator.hasNext()) {
                Property property = propertyIterator.nextProperty();
                ExtendedPropertyDefinition epd = node.getApplicablePropertyDefinition(property.getName());
                return new JCRPropertyWrapperImpl(node, property, node.getSession(), node.getProvider(), epd);
            } else {
                do {
                    Property property = i18nPropertyIterator.nextProperty();
                    final String name = property.getName();
                    if (name.endsWith("_" + locale.toString())) {
                        final String name1 = property.getName();
                        final String s = name1.substring(0, name1.length() - locale.toString().length() - 1);
                        return new JCRPropertyWrapperImpl(node, property, node.getSession(), node.getProvider(), node.getApplicablePropertyDefinition(s), s);
                    }
                } while (true);
            }
        } catch (RepositoryException e) {
            throw new RuntimeException("nextProperty",e);
        }
    }

    public void skip(long skipNum) {
        for (int i=0; i < skipNum; i++) {
            if (propertyIterator.hasNext()) {
                propertyIterator.skip(1);
            } else {
                i18nPropertyIterator.skip(1);
            }
        }
    }

    public long getSize() {
        return size();
    }

    public long getPosition() {
        return propertyIterator.getPosition() + i18nPropertyIterator.getPosition();
    }

    public boolean hasNext() {
        try {
            if (propertyIterator.hasNext()) {
                Property property = propertyIterator.nextProperty();
                ExtendedPropertyDefinition epd = node.getApplicablePropertyDefinition(property.getName());
                tempNext = new JCRPropertyWrapperImpl(node, property, node.getSession(), node.getProvider(), epd);
                return true;
            } else {
                do {
                    Property property = i18nPropertyIterator.nextProperty();
                    final String name = property.getName();
                    if (name.endsWith("_" + locale.toString())) {
                        final String name1 = property.getName();
                        final String s = name1.substring(0, name1.length() - locale.toString().length() - 1);
                        tempNext = new JCRPropertyWrapperImpl(node, property, node.getSession(), node.getProvider(), node.getApplicablePropertyDefinition(s), s);
                        return true;
                    }
                } while (true);
            }
        } catch (NoSuchElementException e) {
            return false;
        } catch (RepositoryException e) {
            throw new RuntimeException("nextProperty",e);
        }
    }

    public Object next() {
        return nextProperty();
    }

    public void remove() {
        throw new UnsupportedOperationException("remove");
    }

    public boolean containsKey(Object o) {
        try {
            return node.hasProperty( (String) o );       
        } catch (RepositoryException e) {
            throw new RuntimeException("containsKey",e);
        }
    }

    public boolean containsValue(Object o) {
        throw new UnsupportedOperationException("containsValue");
    }

    public Object get(Object o) {
        try {
            Property p = node.getProperty( (String) o);

            if (p.isMultiple()) {
                return p.getValues();
            } else {
                return p.getValue();
            }
        } catch (PathNotFoundException e) {
            return null;
        } catch (RepositoryException e) {
            throw new RuntimeException("containsKey",e);
        }
    }

    public Object put(Object o, Object o1) {
        throw new UnsupportedOperationException("put");
    }

    public Object remove(Object o) {
        throw new UnsupportedOperationException("remove");
    }

    public void putAll(Map map) {
        throw new UnsupportedOperationException("putAll");
    }

    public void clear() {
        throw new UnsupportedOperationException("clear");
    }

    public Set keySet() {
        throw new UnsupportedOperationException("keySet");
    }

    public Collection values() {
        throw new UnsupportedOperationException("values");
    }

    public Set entrySet() {
        throw new UnsupportedOperationException("entrySet");
    }
}
