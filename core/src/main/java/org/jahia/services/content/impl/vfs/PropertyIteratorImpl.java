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

package org.jahia.services.content.impl.vfs;

import org.apache.commons.collections.IteratorUtils;
import org.jahia.services.content.JCRPropertyWrapperImpl;
import org.jahia.services.content.RangeIteratorImpl;

import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import java.util.*;

/**
 * Jahia's wrapper of the JCR <code>javax.jcr.PropertyIterator</code>.
 * 
 * @author toto 
 */
public class PropertyIteratorImpl extends RangeIteratorImpl implements PropertyIterator, Map {
    public static final PropertyIteratorImpl EMPTY = new PropertyIteratorImpl(IteratorUtils.EMPTY_ITERATOR, 0);
    private Map map = null;

    public PropertyIteratorImpl(List<JCRPropertyWrapperImpl> list, long size) {
        super(list.iterator(), size);
        map = new HashMap();
        for (JCRPropertyWrapperImpl pi : list) {
            try {
                if (pi.isMultiple()) {
                    map.put(pi.getName(),pi.getValues());
                }
                else {
                    map.put(pi.getName(),pi.getValue());
                }
            } catch (RepositoryException e) {
                e.printStackTrace();
            }
        }
    }

    public PropertyIteratorImpl(Iterator iterator, long size) {
        super(iterator, size);
    }

    public Property nextProperty() {
        return (Property) next();
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(Object o) {
        return map.containsKey(o);
    }

    public boolean containsValue(Object o) {
        return map.containsValue(o);
    }

    public Object get(Object o) {
        return map.get(o);
    }

    public Object put(Object o, Object o1) {
        return map.put(o, o1);
    }

    public Object remove(Object o) {
        return map.remove(o);
    }

    public void putAll(Map map) {
        this.map.putAll(map);
    }

    public void clear() {
        map.clear();
    }

    public Set keySet() {
        return map.keySet();
    }

    public Collection values() {
        return map.values();
    }

    public Set entrySet() {
        return map.entrySet();
    }

    public boolean equals(Object o) {
        return map.equals(o);
    }

    public int hashCode() {
        return map.hashCode();
    }
}
