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
package org.jahia.data.containers;

import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.DynaClass;
import org.apache.commons.beanutils.LazyDynaClass;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;

public class DynaContainerValidatorBase extends ContainerValidatorBase
        implements DynaBean {

    private DynaClass dynaClass = new LazyDynaClass();
    
    /**
     * The constructor obtains the container facade and the ProcessingContext object used
     * in the Jahia engines.
     *
     * @param newCf
     * @param newParams
     */
    public DynaContainerValidatorBase(ContainerFacadeInterface newCf,
                                  ProcessingContext newParams) {
        super(newCf, newParams);
    }    
    
    public boolean contains(String fieldName, String key) {
        try {
            fieldName = fieldName.toLowerCase();
            String matchingFieldName = getMatchingFieldName(fieldName);
            return matchingFieldName != null; 
        } catch (JahiaException ex) {
            return false;
        }
    }

    public Object get(String name, int index) {
        throw new UnsupportedOperationException();
    }

    public Object get(String name, String key) {
        throw new UnsupportedOperationException();
    }

    public Object get(String fieldName) {
        try {
            fieldName = fieldName.toLowerCase();
            String matchingFieldName = getMatchingFieldName(fieldName);

            return !fieldName.endsWith("mlt") ? getJahiaField(matchingFieldName)
                    : getJahiaMultiLanguageField(matchingFieldName);
        } catch (JahiaException ex) {
            return null;
        }
    }

    public DynaClass getDynaClass() {
        return dynaClass;
    }

    public void remove(String name, String key) {
        throw new UnsupportedOperationException();
    }

    public void set(String name, int index, Object value) {
        throw new UnsupportedOperationException();
    }

    public void set(String name, Object value) {
        throw new UnsupportedOperationException();
    }

    public void set(String name, String key, Object value) {
        throw new UnsupportedOperationException();
    }    
}
