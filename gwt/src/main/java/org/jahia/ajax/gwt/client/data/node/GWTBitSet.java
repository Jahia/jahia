/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
public class GWTBitSet implements Serializable {
    private final static int ADDRESS_BITS_PER_WORD = 6;
    private long[] words;
    private int referenceHashCode;

    public GWTBitSet() {
        words = new long[0];
    }

    public GWTBitSet(int nbits) {
        words = new long[wordIndex(nbits-1) + 1];
    }

    public GWTBitSet(GWTBitSet bitSet) {
        referenceHashCode = bitSet.referenceHashCode;
        words = new long[bitSet.words.length];
        System.arraycopy(bitSet.words, 0, words, 0, bitSet.words.length);
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
