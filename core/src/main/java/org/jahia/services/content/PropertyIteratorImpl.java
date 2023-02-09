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

import org.slf4j.Logger;

import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Iterator on properties
 */
public class PropertyIteratorImpl implements PropertyIterator {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(PropertyIteratorImpl.class);
    private PropertyIterator iterator;
    private JCRSessionWrapper session;
    private JCRStoreProvider jcrStoreProvider;
    private JCRPropertyWrapper nextProperty;

    public PropertyIteratorImpl(PropertyIterator iterator, JCRSessionWrapper session, JCRStoreProvider jcrStoreProvider) {
        this.iterator = iterator;
        this.session = session;
        this.jcrStoreProvider = jcrStoreProvider;
        prefetchNext();
    }

    /**
     * {@inheritDoc}
     */
    public Property nextProperty() {
        if (nextProperty == null) {
            throw new NoSuchElementException();
        }
        JCRPropertyWrapper property = nextProperty;
        prefetchNext();
        return property;
    }

    private void prefetchNext() {
        nextProperty = null;
        while (nextProperty == null && iterator.hasNext()) {
            try {
                Property next = (Property) iterator.next();
                if (jcrStoreProvider.getMountPoint().equals("/")) {
                    for (Map.Entry<String, JCRStoreProvider> entry : JCRSessionFactory.getInstance().getMountPoints().entrySet()) {
                        if (next.getPath().startsWith(entry.getKey() + '/')) {
                            nextProperty = entry.getValue().getPropertyWrapper(next, session);
                            break;
                        }
                    }
                }
                if (nextProperty == null) {
                    nextProperty = jcrStoreProvider.getPropertyWrapper(next, session);
                }
            } catch (RepositoryException e) {
                if(logger.isDebugEnabled()) {
                    logger.debug("failed to wrap property, skipping it...", e);
                } else {
                    logger.warn("failed to wrap property ({}), skipping it...", e.getMessage());
                }
                iterator.remove();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void skip(long skipNum) {
        iterator.skip(skipNum);
    }

    /**
     * {@inheritDoc}
     */
    public long getSize() {
        return iterator.getSize();
    }

    /**
     * {@inheritDoc}
     */
    public long getPosition() {
        return iterator.getPosition();
    }

    /**
     * {@inheritDoc}
     */
    public boolean hasNext() {
        return nextProperty != null;
    }

    /**
     * {@inheritDoc}
     */
    public Object next() {
        return nextProperty();
    }

    /**
     * {@inheritDoc}
     */
    public void remove() {
        iterator.remove();
    }
}
