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
 package org.jahia.utils;

import java.util.*;
import java.io.Serializable;


/**
 * <p>Title: Implementation of a set that is ordered by insertion order</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 * @author Serge Huber
 * @version 1.0
 */

public class InsertionSortedSet<E> extends AbstractSet<E> implements Serializable {

    private List<E> internalSet;

    public InsertionSortedSet() {
        internalSet = new ArrayList<E>();
    }

    public InsertionSortedSet(Collection<? extends E> c) {
        internalSet = new ArrayList<E>(c);
    }

    protected void setInternalList (List<E> internalList) {
        this.internalSet = internalList;
    }

    public boolean add(E o)
        throws UnsupportedOperationException,
        NullPointerException,
        ClassCastException,
        IllegalArgumentException {
        if (internalSet.contains(o)) {
            return false;
        } else {
            internalSet.add(o);
            return true;
        }
    }

    public Iterator<E> iterator() {
        return internalSet.iterator();
    }

    public int size() {
        return internalSet.size();
    }

}