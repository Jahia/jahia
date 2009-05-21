/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
