/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content;

import org.apache.jackrabbit.util.ChildrenCollectorFilter;
import org.jahia.services.content.nodetypes.ExtendedPropertyDefinition;

import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.util.*;

/**
 * A property iterator for the external reference properties. This was required as they are treated a little bit
 * differently than regular properties and we couldn't re-use the existing PropertyIterator implementation easily.
 */
public class LazyExternalRefPropertyIterator implements PropertyIterator {

    private Set<String> propertyNames;
    private long position = 0;
    private JCRNodeWrapper node;
    private Iterator<String> propertyNameIterator;

    public LazyExternalRefPropertyIterator(JCRNodeWrapper node, Set<String> propertyNames, String pattern) {
        this.node = node;
        this.propertyNames = propertyNames;
        if (pattern != null) {
            Set<String> filteredNames = new HashSet<String>();
            for (String name : propertyNames) {
                if (ChildrenCollectorFilter.matches(name, pattern)) {
                    filteredNames.add(name);
                }
            }
            this.propertyNames = filteredNames;
        }
        propertyNameIterator = this.propertyNames.iterator();
    }

    public Property nextProperty() {
        String propertyName = propertyNameIterator.next();
        ExtendedPropertyDefinition epd = null;
        try {
            epd = node.getApplicablePropertyDefinition(propertyName);
            Property property = ((JCRNodeWrapperImpl)node).retrieveExternalReferenceProperty(propertyName, epd);
            return property;
        } catch (RepositoryException e) {
            throw new NoSuchElementException("Could retrieve property " + propertyName + ": " + e.getMessage());
        }
    }

    public void skip(long skipNum) {
        if (skipNum > 0) {
            long leftToSkip = skipNum;
            while (propertyNameIterator.hasNext() && leftToSkip > 0) {
                propertyNameIterator.next();
                leftToSkip--;
            }
        }
    }

    public long getSize() {
        return propertyNames.size();
    }

    public long getPosition() {
        return position;
    }

    public boolean hasNext() {
        return propertyNameIterator.hasNext();
    }

    public Object next() {
        return nextProperty();
    }

    public void remove() {
        throw new UnsupportedOperationException("Remove operation is not supported on external reference property iterator");
    }
}
