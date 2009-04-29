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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * An extended BitSet class that hold a list of ordered id
 * 
 */
public class OrderedBitSet extends BitSet {

    private boolean ordered = false;
    private List<Integer> orderedBits = new ArrayList<Integer>();

    public OrderedBitSet() {
        super();
    }

    /**
     * Creates a bit set whose initial size is large enough to explicitly represent bits with indices in the range <code>0</code> through
     * <code>nbits-1</code>. All bits are initially <code>false</code>.
     * 
     * @param nbits
     *            the initial size of the bit set.
     * @exception NegativeArraySizeException
     *                if the specified initial size is negative.
     */
    public OrderedBitSet(int nbits) {
        super(nbits);
    }

    public List<Integer> getOrderedBits() {
        return orderedBits;
    }

    public void setOrderedBits(List<Integer> orderedBits) {
        this.orderedBits = orderedBits;
    }

    public boolean isOrdered() {
        return ordered;
    }

    public void setOrdered(boolean ordered) {
        this.ordered = ordered;
    }

    public Object clone() {
        OrderedBitSet clone = new OrderedBitSet();
        clone.setOrdered(this.ordered);
        List<Integer> orderedBits = new ArrayList<Integer>();
        orderedBits.addAll(this.orderedBits);
        clone.setOrderedBits(orderedBits);
        clone.or(this);
        return clone;
    }
}
