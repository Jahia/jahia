/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.compass.core.lucene;

import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.compass.core.converter.mapping.ResourcePropertyConverter;
import org.compass.core.engine.RepeatableReader;
import org.compass.core.mapping.ResourcePropertyMapping;
import org.compass.core.spi.InternalProperty;

/**
 * @author kimchy
 */
public class LuceneProperty implements InternalProperty {

    private static final long serialVersionUID = 3690475809949104182L;

    private Fieldable field;

    private RepeatableReader reader;

    private transient ResourcePropertyMapping propertyMapping;

    public LuceneProperty(Fieldable field) {
        this.field = field;
    }

    public LuceneProperty(Fieldable field, RepeatableReader reader) {
        this.field = field;
        this.reader = reader;
    }

    public void setPropertyMapping(ResourcePropertyMapping propertyMapping) {
        this.propertyMapping = propertyMapping;
    }

    public ResourcePropertyMapping getPropertyMapping() {
        return propertyMapping;
    }

    public RepeatableReader getRepeatableReader() {
        return this.reader;
    }

    public Fieldable getField() {
        return this.field;
    }

    public String getName() {
        return field.name();
    }

    public Object getObjectValue() {
        String value = getStringValue();
        if (propertyMapping == null) {
            return value;
        }
        ResourcePropertyConverter converter = propertyMapping.getResourcePropertyConverter();
        if (converter == null) {
            return null;
        }
        return converter.fromString(value, propertyMapping);
    }

    public String getStringValue() {
        return field.stringValue();
    }

    public byte[] getBinaryValue() {
        return field.binaryValue();
    }

    public float getBoost() {
        return field.getBoost();
    }

    public void setBoost(float boost) {
        field.setBoost(boost);
    }

    public boolean isIndexed() {
        return field.isIndexed();
    }

    public boolean isStored() {
        return field.isStored();
    }

    public boolean isCompressed() {
        return field.isCompressed();
    }

    public boolean isBinary() {
        return field.isBinary();
    }

    public boolean isTokenized() {
        return field.isTokenized();
    }

    public boolean isTermVectorStored() {
        return field.isTermVectorStored();
    }

    /**
     * Not exported to the users since it is always false when loading the Field
     * from Lucene.
     */
    public boolean isStoreOffsetWithTermVector() {
        return field.isStoreOffsetWithTermVector();
    }

    /**
     * Not exported to the users since it is always false when loading the Field
     * from Lucene.
     */
    public boolean isStorePositionWithTermVector() {
        return field.isStorePositionWithTermVector();
    }

    public boolean isOmitNorms() {
        return field.getOmitNorms();
    }

    public void setOmitNorms(boolean omitNorms) {
        field.setOmitNorms(omitNorms);
    }

    public String toString() {
        return "[" + field + "]";
    }
}
