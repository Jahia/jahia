package org.jahia.ajax.gwt.client.data.node;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 1/4/11
 * Time: 17:44
 * To change this template use File | Settings | File Templates.
 */
public class GWTBitSet implements Cloneable, Serializable {
    private final static int ADDRESS_BITS_PER_WORD = 6;
    private final static int BITS_PER_WORD = 1 << ADDRESS_BITS_PER_WORD;
    private final static int BIT_INDEX_MASK = BITS_PER_WORD - 1;

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
        result.words = words.clone();
        return result;
    }

}
