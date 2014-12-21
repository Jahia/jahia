/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.utils;

import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Christophe Laprun
 */
public class TextUtils {

    public static String replaceBoundedString(String initial, String prefix, String suffix, String replacement) {
        return replaceBoundedString(initial, prefix, suffix, new ConstantStringReplacementGenerator(replacement));
    }

    public static String replaceBoundedString(final String initial, final String prefix, final String suffix, StringReplacementGenerator generator) {
        if (initial == null || initial.length() == 0) {
            return initial;
        }

        if (generator == null) {
            generator = ConstantStringReplacementGenerator.REPLACE_BY_EMPTY;
        }

        if (StringUtils.isEmpty(prefix) || StringUtils.isEmpty(suffix)) {
            throw new IllegalArgumentException("Must provide non-null, non-empty prefix and suffix to match!");
        }

        int prefixIndex = initial.indexOf(prefix);
        int suffixIndex;
        if (prefixIndex < 0) {
            // we don't have a prefix
            return initial;
        } else {
            suffixIndex = initial.lastIndexOf(suffix);
            if (suffixIndex < 0 || suffixIndex < prefixIndex) {
                // we don't have a suffix or it's not located after the prefix so no replacement
                return initial;
            }
        }

        // replace the prefix and suffix instances using the specified replacement generator in the string between the first prefix and last suffix
        String replacement = new Replacer(prefix, suffix, initial.substring(prefixIndex, suffixIndex), generator).replace();

        // add text before first prefix and after last suffix if any
        return initial.substring(0, prefixIndex) + replacement + initial.substring(suffixIndex + suffix.length());
    }

    private static class Replacer {
        private final String prefix;
        private final String suffix;
        private final int prefixLength;
        private final int suffixLength;
        private final String tmp;
        private final int length;
        private final StringReplacementGenerator generator;
        private final SortedMap<Integer, Integer> matchedPairs = new TreeMap<>();

        public Replacer(String prefix, String suffix, String initial, StringReplacementGenerator generator) {
            this.prefix = prefix;
            this.prefixLength = prefix.length();

            this.suffix = suffix;
            this.suffixLength = suffix.length();

            this.tmp = initial;
            this.length = initial.length();

            this.generator = generator;
        }

        public String replace() {
            // we already match the first prefix
            int prefixIndex = 0;
            // look for first suffix right after
            int suffixIndex = tmp.indexOf(suffix);

            // if we don't have a suffix, the whole String is a match, so replace it and return
            if(suffixIndex < 0) {
                return generator.getReplacementFor(tmp.substring(prefixLength), prefix, suffix);
            }

            // as long as we can find new prefixes to match
            while (prefixIndex >= 0) {

                // check if the given prefix and suffix are matching until they do, match method accumulates in-between matches in matchedPairs map
                while (!match(prefixIndex, suffixIndex)) {
                    // if not, we have a nested pair and we need to extend our search to the next suffix, checking that we don't go out of bounds
                    final int nextSuffix = tmp.indexOf(suffix, suffixIndex + suffixLength);
                    // if we didn't find one, then it means we've reached the end of the String which ended with a (excluded) suffix match, remember? :)
                    suffixIndex = ensureSuffixIndex(nextSuffix);
                }

                // move on to the next potential prefix / suffix pair
                final int previousSuffix = suffixIndex;
                // next suffix is the one after the one we just matched to the prefix we were looking at, checking that we don't go out of bounds
                final int nextSuffix = tmp.indexOf(suffix, matchedPairs.get(prefixIndex) + suffixLength);
                suffixIndex = ensureSuffixIndex(nextSuffix);
                // next prefix is the one after the suffix we just matched
                prefixIndex = tmp.indexOf(prefix, previousSuffix + suffixLength);
            }

            // once we have matched all our pairs, build the replaced String
            StringBuilder builder = new StringBuilder(length);
            replaceMatch(matchedPairs, builder, -1);

            return builder.toString();
        }

        private int ensureSuffixIndex(int potentialNextSuffix) {
            return potentialNextSuffix >= 0 ? potentialNextSuffix : length - 1;
        }

        /**
         * Replaces matching prefix / suffix pairs, using the specified StringBuilder to build the final String
         *
         * @param matches        remaining matching pairs (key: prefix index, value: suffix index)
         * @param builder        the StringBuilder used to create the final replaced String
         * @param previousSuffix the index of the last suffix we matched to add the text in between the previous match and the new ones or a strictly negative int if there isn't
         *                       one
         */
        private void replaceMatch(SortedMap<Integer, Integer> matches, StringBuilder builder, int previousSuffix) {
            if (!matches.isEmpty()) {
                int pairPrefix = matches.firstKey();
                int pairSuffix = matches.get(pairPrefix);

                // text to replace
                String match;
                if(pairPrefix == pairSuffix || pairPrefix >= length - 1) {
                    match = "";
                }
                else {
                    match = tmp.substring(pairPrefix + prefixLength, pairSuffix);
                }

                // use the generator to replace it
                String replacement = generator.getReplacementFor(match, prefix, suffix);

                // if we had a previous suffix
                if (previousSuffix > 0 && previousSuffix < length - suffixLength) {
                    // add the text between the previous suffix and the new prefix
                    builder.append(tmp.substring(previousSuffix + suffixLength, pairPrefix));
                }

                // add the replaced text
                builder.append(replacement);

                // remove current match
                matchedPairs.remove(pairPrefix);
                // repeat on all the pairs that are after the currently matched suffix since all in between pairs are replaced
                replaceMatch(matchedPairs.tailMap(pairSuffix), builder, pairSuffix);
            }
        }

        private boolean match(int prefixIndex, int suffixIndex) {
            if(prefixIndex == suffixIndex) {
                matchedPairs.put(prefixIndex, suffixIndex);
                return true;
            }

            int inBetweenPrefix = tmp.lastIndexOf(prefix, suffixIndex - 1);

            if (inBetweenPrefix >= 0) {
                // if we already have matched this in between prefix, find the previous unmatched one
                while (matchedPairs.get(inBetweenPrefix) != null) {
                    inBetweenPrefix = tmp.lastIndexOf(prefix, inBetweenPrefix - 1);
                }

                if (inBetweenPrefix == prefixIndex) {
                    // we have a match, record it
                    matchedPairs.put(prefixIndex, suffixIndex);
                    return true;
                }

                while (!match(inBetweenPrefix, suffixIndex)) {
                    inBetweenPrefix = tmp.lastIndexOf(prefix, inBetweenPrefix - 1);
                }
            }

            return false;
        }
    }

    public static interface StringReplacementGenerator {
        String getReplacementFor(String match, String prefix, String suffix);
    }

    public static class ConstantStringReplacementGenerator implements StringReplacementGenerator {
        public static final ConstantStringReplacementGenerator REPLACE_BY_EMPTY = new ConstantStringReplacementGenerator("");
        private String replacement;

        public ConstantStringReplacementGenerator(String replacement) {
            this.replacement = replacement;
        }

        public String getReplacementFor(String match, String prefix, String suffix) {
            return replacement;
        }
    }
}
