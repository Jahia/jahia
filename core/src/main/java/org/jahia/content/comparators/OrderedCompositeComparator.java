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

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(OrderedCompositeComparator.class);

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