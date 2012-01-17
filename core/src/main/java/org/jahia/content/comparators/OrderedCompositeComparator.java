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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * <p>Title: A comparator that can contains multiple sub-comparators, that
 * are used in sequence while objects are equal.</p>
 * <p>Description: This comparator allows object to be sorted by multiple
 * comparators, starting with the most relevant comparator first, and going
 * down to the least important. This class is often sub-classed for different
 * type of JahiaObjects descendants, in order to offer a framework for
 * flexible comparator that reduce hard-coding.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class OrderedCompositeComparator implements Comparator {

    private static org.slf4j.Logger logger =
        org.slf4j.LoggerFactory.getLogger(OrderedCompositeComparator.class);

    List orderedComparators = new ArrayList();

    public OrderedCompositeComparator() {
    }

    public void addComparator(Comparator comparator) {
        orderedComparators.add(comparator);
    }

    public void addComparator(int index, Comparator comparator) {
        orderedComparators.add(index, comparator);
    }

    public int compare(Object o1, Object o2) {
        if (orderedComparators.size() < 1) {
            logger.warn("No configured comparators in ordered comparator, returning objects by toString comparison");
            return o1.toString().compareTo(o2.toString());
        }
        Iterator comparatorIter = orderedComparators.iterator();
        int curCompareResult = 0;
        while (comparatorIter.hasNext()) {
            Comparator curComparator = (Comparator) comparatorIter.next();
            curCompareResult = curComparator.compare(o1, o2);
            if (curCompareResult != 0) {
                return curCompareResult;
            }
        }
        return curCompareResult;
    }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        
        if (obj != null && this.getClass() == obj.getClass()) {
            final OrderedCompositeComparator otherComparator = (OrderedCompositeComparator) obj;
            return orderedComparators.equals(otherComparator.orderedComparators);
        }
        return false;
    }

}