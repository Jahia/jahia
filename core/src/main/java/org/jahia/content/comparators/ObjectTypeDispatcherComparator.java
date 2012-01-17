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

 package org.jahia.content.comparators;

import java.util.Comparator;
import org.jahia.content.JahiaObject;
import java.util.Map;
import java.util.HashMap;

/**
 * <p>Title: A comparator that uses the JahiaObject's type to dispatch
 * to a sub comparator. This allows our comparators to avoid type-specific
 * hardcoded logic. </p>
 * <p>Description: The type is exctracted from the object key of the
 * JahiaObject we give to compare. Note that in the case of a comparison
 * between different types of JahiaObject descendants, the objects will be
 * tested for a compliance to the Comparable interface and compared that
 * way. If they do not implement that interface, the objects will be
 * converted to String using their toString() method and comparing the
 * resulting String objects.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class ObjectTypeDispatcherComparator implements Comparator {

    private static org.slf4j.Logger logger =
        org.slf4j.LoggerFactory.getLogger(ObjectTypeDispatcherComparator.class);

    private Map typeComparators = new HashMap();

    public ObjectTypeDispatcherComparator() {
    }

    public void addTypeComparator(String typeName, Comparator typeComparator) {
        typeComparators.put(typeName, typeComparator);
    }

    public int compare(Object o1, Object o2) {
        if ( (o1 instanceof JahiaObject) && (o2 instanceof JahiaObject)) {
            JahiaObject leftJahiaObject = (JahiaObject) o1;
            JahiaObject rightJahiaObject = (JahiaObject) o2;
            if (!leftJahiaObject.getObjectKey().getType().equals(rightJahiaObject.getObjectKey().getType())) {
                return leftJahiaObject.getObjectKey().compareTo(rightJahiaObject.getObjectKey());
            }
            // object key types are equals if we got this far, we must now
            // dispatch to a type-specific comparator.
            Comparator typeComparator = (Comparator) typeComparators.get(leftJahiaObject.getObjectKey().getType());
            if (typeComparator == null) {
                logger.warn("No comparator found for type : " + leftJahiaObject.getObjectKey().getType() + ", returning equal in comparison.");
                return 0;
            }
            return typeComparator.compare(o1, o2);
        } else {
            // these comparators should only be used with subclasses of JahiaObject,
            // never with anything else
            if ((o1 instanceof Comparable) && (o2 instanceof Comparable)) {
                Comparable leftComparable = (Comparable) o1;
                Comparable rightComparable = (Comparable) o2;
                return leftComparable.compareTo(rightComparable);
            } else {
                return o1.toString().compareTo(o2.toString());
            }
        }
    }

    public boolean equals(Object obj) {      
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final ObjectTypeDispatcherComparator otherComparator = (
                ObjectTypeDispatcherComparator) obj;
            return otherComparator.typeComparators.equals(typeComparators);
        }
        return false;
    }

}