package org.jahia.services.content;

import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.*;
import javax.jcr.nodetype.ConstraintViolationException;
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

    public LazyPropertyIterator(JCRNodeWrapper node) {
        this.node = node;
    }

    public LazyPropertyIterator(JCRNodeWrapper node, Locale locale) {
        this.node = node;
        this.locale = locale;
    }

    public int size() {
        return (int) (getPropertiesIterator().getSize() + getI18NPropertyIterator().getSize());
    }

    private PropertyIterator getPropertiesIterator() {
        if (propertyIterator == null) {
            try {
                propertyIterator = node.getRealNode().getProperties();
            } catch (RepositoryException e) {
                throw new RuntimeException("getI18NPropertyIterator",e);
            }
        }
        return propertyIterator;
    }

    private PropertyIterator getI18NPropertyIterator() {
        if (i18nPropertyIterator == null) {
            try {
                if (locale != null) {
                    i18nPropertyIterator = node.getI18N(locale).getProperties();
                } else {
                    i18nPropertyIterator = new PropertyIteratorImpl(Collections.<JCRPropertyWrapperImpl>emptyList(),0);
                }
            } catch (ItemNotFoundException e) {
                i18nPropertyIterator = new PropertyIteratorImpl(Collections.<JCRPropertyWrapperImpl>emptyList(),0);
            } catch (RepositoryException e) {
                throw new RuntimeException("getI18NPropertyIterator",e);
            }
        }
        return i18nPropertyIterator;
    }

    public boolean isEmpty() {
        return getPropertiesIterator().getSize() == 0 && getI18NPropertyIterator().getSize() == 0;
    }

    public Property nextProperty() {
        try {
            if (tempNext != null) {
                Property res = tempNext;
                tempNext = null;
                return res;
            }

            if (getPropertiesIterator().hasNext()) {
                Property property = getPropertiesIterator().nextProperty();
                ExtendedPropertyDefinition epd = node.getApplicablePropertyDefinition(property.getName());
                return new JCRPropertyWrapperImpl(node, property, node.getSession(), node.getProvider(), epd);
            } else {
                do {
                    Property property = getI18NPropertyIterator().nextProperty();
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
            if (getPropertiesIterator().hasNext()) {
                getPropertiesIterator().skip(1);
            } else {
                getI18NPropertyIterator().skip(1);
            }
        }
    }

    public long getSize() {
        return size();
    }

    public long getPosition() {
        return getPropertiesIterator().getPosition() + getI18NPropertyIterator().getPosition();
    }

    public boolean hasNext() {
        try {
            if (getPropertiesIterator().hasNext()) {
                Property property = getPropertiesIterator().nextProperty();
                ExtendedPropertyDefinition epd = node.getApplicablePropertyDefinition(property.getName());
                tempNext = new JCRPropertyWrapperImpl(node, property, node.getSession(), node.getProvider(), epd);
                return true;
            } else {
                do {
                    Property property = getI18NPropertyIterator().nextProperty();
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
        } catch (ConstraintViolationException e) {
            return false;
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
        } catch (ConstraintViolationException e) {
            return null;
        } catch (RepositoryException e) {
            throw new RuntimeException("get",e);
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
