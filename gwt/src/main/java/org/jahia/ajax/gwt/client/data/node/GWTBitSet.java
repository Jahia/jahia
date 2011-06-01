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

}
