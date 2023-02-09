/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
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
    protected JCRNodeWrapper node;
    protected Locale locale;
    protected String singlePattern = null;
    protected String[] patternArray = null;
    protected PropertyIterator propertyIterator;
    protected PropertyIterator i18nPropertyIterator;
    protected Property tempNext = null;
    protected String fallbackLocale;

    public LazyPropertyIterator(JCRNodeWrapper node) {
        this.node = node;
    }

    public LazyPropertyIterator(JCRNodeWrapper node, Locale locale) {
        this.node = node;
        this.locale = locale;
    }

    public LazyPropertyIterator(JCRNodeWrapper node, Locale locale, String singlePattern) {
        this.node = node;
        this.locale = locale;
        this.singlePattern = singlePattern;
    }

    public LazyPropertyIterator(JCRNodeWrapper node, Locale locale, String[] patternArray) {
        this.node = node;
        this.locale = locale;
        this.patternArray = patternArray;
    }

    public int size() {
        return (int) (getPropertiesIterator().getSize() + getI18NPropertyIterator().getSize());
    }

    protected PropertyIterator getPropertiesIterator() {
        if (propertyIterator == null) {
            try {
                if (patternArray != null) {
                    propertyIterator = node.getRealNode().getProperties(patternArray);
                } else if (singlePattern == null) {
                    propertyIterator = node.getRealNode().getProperties();
                } else {
                    propertyIterator = node.getRealNode().getProperties(singlePattern);
                }
            } catch (RepositoryException e) {
                throw new RuntimeException("getI18NPropertyIterator", e);
            }
        }
        return propertyIterator;
    }

    protected PropertyIterator getI18NPropertyIterator() {
        if (i18nPropertyIterator == null) {
            try {
                if (locale != null) {
                    final Node localizedNode = node.getI18N(locale);
                    fallbackLocale = localizedNode.getProperty("jcr:language").getString();
                    if (patternArray != null) {
                        i18nPropertyIterator = localizedNode.getProperties(patternArray);
                    } else if (singlePattern == null) {
                        i18nPropertyIterator = localizedNode.getProperties();
                    } else {
                        i18nPropertyIterator = localizedNode.getProperties(singlePattern);
                    }
                } else {
                    i18nPropertyIterator = new EmptyPropertyIterator();
                }
            } catch (ItemNotFoundException e) {
                i18nPropertyIterator = new EmptyPropertyIterator();
            } catch (RepositoryException e) {
                throw new RuntimeException("getI18NPropertyIterator", e);
            }
        }
        return i18nPropertyIterator;
    }

    public boolean isEmpty() {
        return getPropertiesIterator().getSize() == 0 &&
                getI18NPropertyIterator().getSize() == 0;
    }

    public Property nextProperty() {
        if (hasNext()) {
            Property res = tempNext;
            tempNext = null;
            return res;
        }
        throw new NoSuchElementException();
    }

    public void skip(long skipNum) {
        for (int i = 0; i < skipNum; i++) {
            if (getPropertiesIterator().hasNext()) {
                getPropertiesIterator().skip(1);
            } else if (getI18NPropertyIterator().hasNext()) {
                getI18NPropertyIterator().skip(1);
            }
        }
    }

    public long getSize() {
        return size();
    }

    public long getPosition() {
        return getPropertiesIterator().getPosition() +
                getI18NPropertyIterator().getPosition();
    }

    public boolean hasNext() {
        if (tempNext != null) {
            return true;
        }
        try {
            if (getPropertiesIterator().hasNext()) {
                Property property = getPropertiesIterator().nextProperty();
                ExtendedPropertyDefinition epd = node.getApplicablePropertyDefinition(property.getName(), property.getType(), property.isMultiple());
                if (epd == null) {
                    return hasNext();
                }
                tempNext = new JCRPropertyWrapperImpl(node, property, node.getSession(), node.getProvider(), epd);
                return true;
            } else if (getI18NPropertyIterator().hasNext()) {
                do {
                    Property property = getI18NPropertyIterator().nextProperty();
                    final String name = property.getName();
                    final ExtendedPropertyDefinition def = node.getApplicablePropertyDefinition(name);
                    if (def != null && def.isInternationalized()) {
                        tempNext = new JCRPropertyWrapperImpl(node, property, node.getSession(), node.getProvider(), def, name);
                        return true;
                    }
                } while (true);
            } else {
                return false;
            }
        } catch (ConstraintViolationException e) {
            return hasNext();
        } catch (InvalidItemStateException e) {
            return hasNext();
        } catch (NoSuchElementException e) {
            return false;
        } catch (RepositoryException e) {
            throw new RuntimeException("nextProperty", e);
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
            return node.hasProperty((String) o);
        } catch (ConstraintViolationException e) {
            return false;
        } catch (InvalidItemStateException e) {
            return false;
        } catch (RepositoryException e) {
            throw new RuntimeException("containsKey", e);
        }
    }

    public boolean containsValue(Object o) {
        throw new UnsupportedOperationException("containsValue");
    }

    public Object get(Object o) {
        try {
            if (!node.hasProperty((String) o)) {
                return null;
            }
            Property p = node.getProperty((String) o);

            if (p.isMultiple()) {
                return p.getValues();
            } else {
                return p.getValue();
            }
        } catch (PathNotFoundException e) {
            return null;
        } catch (ConstraintViolationException e) {
            return null;
        } catch (InvalidItemStateException e) {
            return null;
        } catch (RepositoryException e) {
            throw new RuntimeException("get", e);
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
