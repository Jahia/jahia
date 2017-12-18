/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

/**
 * Iterator on properties
 */
public class PropertyIteratorImpl implements PropertyIterator {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(PropertyIteratorImpl.class);
    private PropertyIterator iterator;
    private JCRSessionWrapper session;
    private JCRStoreProvider jcrStoreProvider;

    public PropertyIteratorImpl(PropertyIterator iterator, JCRSessionWrapper session, JCRStoreProvider jcrStoreProvider) {
        this.iterator = iterator;
        this.session = session;
        this.jcrStoreProvider = jcrStoreProvider;
    }

    /**
     * {@inheritDoc}
     */
    public Property nextProperty() {
        try {
            Property next = (Property) iterator.next();
            if (jcrStoreProvider.getMountPoint().equals("/")) {
                for (Map.Entry<String, JCRStoreProvider> entry : JCRSessionFactory.getInstance().getMountPoints().entrySet()) {
                    if (next.getPath().startsWith(entry.getKey() + '/')) {
                        return entry.getValue().getPropertyWrapper(next, session);
                    }
                }
            }
            return jcrStoreProvider.getPropertyWrapper(next, session);
        } catch (RepositoryException e) {
            logger.error("",e);
        }
        return null;
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
        return iterator.hasNext();
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
