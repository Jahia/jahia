/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.client.data.node;

import java.io.Serializable;

/**
 * User: toto
 * Date: 1/4/11
 * Time: 17:44
 */
public class GWTBitSet implements Cloneable, Serializable {
    private final static int ADDRESS_BITS_PER_WORD = 6;
    private long[] words;
    private int referenceHashCode;

    public GWTBitSet() {
    }

    public GWTBitSet(int nbits) {
        words = new long[wordIndex(nbits-1) + 1];
    }

    public void set(int bitIndex) {
        if (bitIndex < 0)
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

        int wordIndex = wordIndex(bitIndex);
        words[wordIndex] |= (1L << bitIndex);
    }


    public boolean get(int bitIndex) {
        if (bitIndex < 0)
            throw new IndexOutOfBoundsException("bitIndex < 0: " + bitIndex);

        int wordIndex = wordIndex(bitIndex);
        return ((words[wordIndex] & (1L << bitIndex)) != 0);
    }

    private static int wordIndex(int bitIndex) {
        return bitIndex >> ADDRESS_BITS_PER_WORD;
    }

    public void and(GWTBitSet set) {
        if (this == set)
            return;

        // Perform logical AND on words in common
        for (int i = 0; i < words.length; i++)
            words[i] &= set.words[i];
    }

    public Object clone() {
        GWTBitSet result = new GWTBitSet();
        result.words = new long[words.length];
        System.arraycopy(words, 0, result.words, 0, words.length);
        return result;
    }

    /**
     * @return the hashcode of an eventual reference
     */
    public int getReferenceHashCode() {
        return referenceHashCode;
    }

    /**
     * Setter of a reference HashCode. If the bitset is used to reference to another list/array, then a 
     * hashcode can be set to uniquely identify that reference.  
     * @param referenceHashCode
     */
    public void setReferenceHashCode(int referenceHashCode) {
        this.referenceHashCode = referenceHashCode;
    }

}
