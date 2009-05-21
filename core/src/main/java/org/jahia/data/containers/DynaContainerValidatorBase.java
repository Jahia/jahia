/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
