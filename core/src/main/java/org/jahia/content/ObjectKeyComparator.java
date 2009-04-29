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
 package org.jahia.content;

import java.util.Comparator;

import org.jahia.utils.JahiaConsole;
import java.io.Serializable;

/**
 * Title:        Jahia
 * Description:  This class is used to define a comparator to be used in all
 * sorts of sorted Java collections such as TreeSets, SortedLists, etc...
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 * @author Serge Huber
 * @version 1.0
 */

public class ObjectKeyComparator implements Comparator, Serializable {

    public ObjectKeyComparator() {
    }

    public int compare(Object o1, Object o2) {
        if ((o1 instanceof ObjectKey) && (o2 instanceof ObjectKey)) {
            ObjectKey key1 = (ObjectKey) o1;
            ObjectKey key2 = (ObjectKey) o2;
            return key1.getKey().compareTo(key2.getKey());
        } else {
            JahiaConsole.println("ObjectKeyComparator.compare",
                                 "Comparator called on non ObjectKey objects, returning -1...");
            return -1; // default is keep order
        }
    }

    /**
     * Must only return true if the passed object is a comparator and has the
     * same order as this one, so we simply test if it is an instance of this
     * comparator class...
     */
    public boolean equals(Object obj) { 
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            return true;
        }
        return false;
    }
}
