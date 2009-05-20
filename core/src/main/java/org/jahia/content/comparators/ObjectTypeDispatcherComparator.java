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

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(ObjectTypeDispatcherComparator.class);

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