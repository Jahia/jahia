/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.decorator;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.LazyPropertyIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.util.List;
import java.util.Map;

/**
 * This abstract decorator allow implementations to protect some properties from reading operation
 * Useful in case of passwords stored in the jcr

 * @author Kevan
 */
public abstract class JCRProtectedNodeAbstractDecorator extends JCRNodeDecorator {

    private boolean alwaysAllowSystem;

    /**
     * Instantiate this decorator, by default properties will be protect also from system session
     * @param node the node
     */
    public JCRProtectedNodeAbstractDecorator(JCRNodeWrapper node) {
        super(node);
        alwaysAllowSystem = false;
    }

    /**
     * Instantiate this decorator, you can specify if system session should be able to read in any case
     * @param node the node
     * @param alwaysAllowSystem set to true if you want system session to be able to read protected properties
     */
    public JCRProtectedNodeAbstractDecorator(JCRNodeWrapper node, boolean alwaysAllowSystem) {
        super(node);
        this.alwaysAllowSystem = alwaysAllowSystem;
    }

    private transient static Logger logger = LoggerFactory.getLogger(JCRProtectedNodeAbstractDecorator.class);

    private class FilteredIterator extends LazyPropertyIterator {
        public FilteredIterator() throws RepositoryException {
            super(JCRProtectedNodeAbstractDecorator.this, JCRProtectedNodeAbstractDecorator.this.getSession().getLocale());
        }

        public FilteredIterator(String singlePattern) throws RepositoryException {
            super(JCRProtectedNodeAbstractDecorator.this, JCRProtectedNodeAbstractDecorator.this.getSession().getLocale(),
                    singlePattern);
        }

        public FilteredIterator(String[] patternArray) throws RepositoryException {
            super(JCRProtectedNodeAbstractDecorator.this, JCRProtectedNodeAbstractDecorator.this.getSession().getLocale(),
                    patternArray);
        }

        @Override
        public boolean hasNext() {
            while (super.hasNext()) {
                try {
                    if (!canGetProperty(tempNext.getName())) {
                        tempNext = null;
                    } else {
                        return true;
                    }
                } catch (RepositoryException e) {
                    tempNext = null;
                    logger.error("Cannot read property", e);
                }
            }
            return false;
        }
    }

    /**
     * Only method to implement, that check the readability of a property, based on the property name
     * @param propertyName the property name to check
     * @return true if the property is readable, false if not
     * @throws RepositoryException in case of JCR-related errors
     */
    protected abstract boolean canReadProperty(String propertyName) throws RepositoryException;

    public boolean canGetProperty(String propertyName) throws RepositoryException {
        return alwaysAllowSystem && getSession().isSystem() || canReadProperty(propertyName);
    }

    @Override
    public PropertyIterator getProperties() throws RepositoryException {
        return (alwaysAllowSystem && getSession().isSystem()) ? super.getProperties() : new FilteredIterator();
    }

    @Override
    public PropertyIterator getProperties(String s) throws RepositoryException {
        return (alwaysAllowSystem && getSession().isSystem()) ? super.getProperties(s) : new FilteredIterator(s);
    }

    @Override
    public PropertyIterator getProperties(String[] strings) throws RepositoryException {
        return (alwaysAllowSystem && getSession().isSystem()) ? super.getProperties(strings) : new FilteredIterator(strings);
    }

    @Override
    public Map<String, String> getPropertiesAsString() throws RepositoryException {
        return (alwaysAllowSystem && getSession().isSystem()) ? super.getPropertiesAsString() : Maps.filterKeys(super.getPropertiesAsString(),
                new Predicate<String>() {
                    @Override
                    public boolean apply(String input) {
                        try {
                            return canGetProperty(input);
                        } catch (RepositoryException e) {
                            return false;
                        }
                    }
                });
    }

    @Override
    public JCRPropertyWrapper getProperty(String s) throws PathNotFoundException, RepositoryException {
        if (!canGetProperty(s)) {
            throw new PathNotFoundException(s);
        }

        return super.getProperty(s);
    }

    @Override
    public String getPropertyAsString(String name) {
        try {
            return canGetProperty(name) ? super.getPropertyAsString(name) : null;
        } catch (RepositoryException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean hasProperty(String s) throws RepositoryException {
        return super.hasProperty(s) && canGetProperty(s);
    }

}
