/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
