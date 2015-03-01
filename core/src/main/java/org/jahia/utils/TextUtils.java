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
 * @author Christophe Laprun
 */
public class TextUtils {

    public static <T> T visitBoundedString(String initial, String prefix, String suffix, BoundedStringVisitor<T> visitor) {
        return visitBoundedString(initial, prefix, suffix, visitor, new Matcher<>(prefix, suffix, visitor));
    }

    private static <T> T visitBoundedString(String initial, String prefix, String suffix, BoundedStringVisitor<T> visitor, Matcher<T> matcher) {
        if (visitor == null) {
            throw new IllegalArgumentException("Must provide a non-null visitor!");
        }

        final T initialValue = visitor.initialValue(initial);
        if (initial == null || initial.isEmpty()) {
            return initialValue;
        }


        if (StringUtils.isEmpty(prefix) || StringUtils.isEmpty(suffix)) {
            throw new IllegalArgumentException("Must provide non-null, non-empty prefix and suffix to match!");
        }

        int prefixIndex = initial.indexOf(prefix);
        int suffixIndex;
        if (prefixIndex < 0) {
            // we don't have a prefix
            return initialValue;
        } else {
            suffixIndex = initial.lastIndexOf(suffix);
            if (suffixIndex < 0 || suffixIndex < prefixIndex) {
                // we don't have a suffix or it's not located after the prefix so no replacement
                return initialValue;
            }
        }

        // find all matches and visit them
        return matcher.needsPreAndPostMatches() ?
                matcher.match(initial.substring(0, prefixIndex), initial.substring(prefixIndex, suffixIndex), initial.substring(suffixIndex + suffix.length())) :
                matcher.match(initial.substring(prefixIndex, suffixIndex));
    }

    public static String replaceBoundedString(String initial, String prefix, String suffix, String replacement) {
        return replaceBoundedString(initial, prefix, suffix, new ConstantStringReplacementGenerator(replacement));
    }

    public static String replaceBoundedString(final String initial, final String prefix, final String suffix, BoundedStringVisitor<String> visitor) {
        return visitBoundedString(initial, prefix, suffix, visitor, new Replacer(prefix, suffix, visitor));
    }

    private static class Matcher<T> {
        protected final String prefix;
        protected final String suffix;
        protected final int prefixLength;
        protected final int suffixLength;
        protected int length;
        protected BoundedStringVisitor<T> visitor;
        protected final SortedMap<Integer, Integer> matchedPairs = new TreeMap<>();

        public Matcher(String prefix, String suffix, BoundedStringVisitor<T> visitor) {
            this.prefix = prefix;
            this.prefixLength = prefix.length();

            this.suffix = suffix;
            this.suffixLength = suffix.length();

            this.visitor = visitor;
        }

        public T match(final String initialString) {
            // we already match the first prefix
            int prefixIndex = 0;
            // look for first suffix right after
            int suffixIndex = initialString.indexOf(suffix);

            // adjust length to match the String we're considering
            length = initialString.length();

            // if we don't have a suffix, the whole String is a match, so visit it and return
            if (suffixIndex < 0) {
                return visitor.visit(initialString.substring(prefixLength), prefix, suffix, 0, initialString.length(), initialString);
            }

            // as long as we can find new prefixes to match
            while (prefixIndex >= 0) {

                // check if the given prefix and suffix are matching until they do, match method accumulates in-between matches in matchedPairs map
                while (!match(prefixIndex, suffixIndex, initialString)) {
                    // if not, we have a nested pair and we need to extend our search to the next suffix, checking that we don't go out of bounds
                    final int nextSuffix = initialString.indexOf(suffix, suffixIndex + suffixLength);
                    // if we didn't find one, then it means we've reached the end of the String which ended with a (excluded) suffix match, remember? :)
                    suffixIndex = ensureSuffixIndex(nextSuffix);
                }

                // move on to the next potential prefix / suffix pair
                final int previousSuffix = suffixIndex;
                // next suffix is the one after the one we just matched to the prefix we were looking at, checking that we don't go out of bounds
                final int nextSuffix = initialString.indexOf(suffix, matchedPairs.get(prefixIndex) + suffixLength);
                suffixIndex = ensureSuffixIndex(nextSuffix);
                // next prefix is the one after the suffix we just matched
                prefixIndex = initialString.indexOf(prefix, previousSuffix + suffixLength);
            }

            // once we have matched all our pairs, visit them
            return visitMatches(matchedPairs, visitor.initialValue(initialString), initialString);
        }

        public T match(String preMatches, String betweenMatches, String postMatches) {
            return match(betweenMatches);
        }

        /**
         * Visits matching prefix / suffix pairs, using the specified StringBuilder to build the final String
         *
         * @param matches        remaining matching pairs (key: prefix index, value: suffix index)
         * @param previousResult the result that has been accumulated so far during the matching process
         * @param initialString  the String on which the matching is performed
         */
        private T visitMatches(SortedMap<Integer, Integer> matches, T previousResult, String initialString) {
            if (!matches.isEmpty()) {
                int pairPrefix = matches.firstKey();
                int pairSuffix = matches.get(pairPrefix);

                // match
                String match;
                if (pairPrefix == pairSuffix || pairPrefix >= length - 1) {
                    match = "";
                } else {
                    match = initialString.substring(pairPrefix + prefixLength, pairSuffix);
                }

                // visit the current match
                T result = visitor.visit(match, prefix, suffix, pairPrefix, pairSuffix, initialString);

                // remove current match
                matchedPairs.remove(pairPrefix);
                // repeat on all the pairs that are after the currently matched suffix since all in between pairs are replaced
                return visitMatches(matchedPairs.tailMap(pairSuffix), result, initialString);
            } else {
                return previousResult;
            }
        }

        private int ensureSuffixIndex(int potentialNextSuffix) {
            return potentialNextSuffix >= 0 ? potentialNextSuffix : length - 1;
        }

        private boolean match(final int prefixIndex, final int suffixIndex, final String initialString) {
            if (prefixIndex == suffixIndex) {
                matchedPairs.put(prefixIndex, suffixIndex);
                return true;
            }

            int inBetweenPrefix = initialString.lastIndexOf(prefix, suffixIndex - 1);

            if (inBetweenPrefix >= 0) {
                // if we already have matched this in between prefix, find the previous unmatched one
                while (matchedPairs.get(inBetweenPrefix) != null) {
                    inBetweenPrefix = initialString.lastIndexOf(prefix, inBetweenPrefix - 1);
                }

                if (inBetweenPrefix == prefixIndex) {
                    // we have a match, record it
                    matchedPairs.put(prefixIndex, suffixIndex);
                    return true;
                }

                while (!match(inBetweenPrefix, suffixIndex, initialString)) {
                    inBetweenPrefix = initialString.lastIndexOf(prefix, inBetweenPrefix - 1);
                }
            }

            return false;
        }

        public boolean needsPreAndPostMatches() {
            return false;
        }
    }

    private static class Replacer extends Matcher<String> {

        public Replacer(String prefix, final String suffix, final BoundedStringVisitor<String> visitor) {
            super(prefix, suffix, visitor);
        }

        @Override
        public String match(String beforeFirstPrefix, String initialString, String afterLastSuffix) {
            // wrap the original visitor to add our replacement behavior
            final StringBuilder builder = new StringBuilder(beforeFirstPrefix.length() + initialString.length() + afterLastSuffix.length());

            // add String before first prefix
            builder.append(beforeFirstPrefix);

            this.visitor = new ReplacerVisitor(builder, suffix, visitor, initialString.length());

            // perform matching
            super.match(initialString);

            // add String after last suffix
            builder.append(afterLastSuffix);

            // and finally, extract the new string
            return builder.toString();
        }

        @Override
        public boolean needsPreAndPostMatches() {
            return true;
        }

        private static class ReplacerVisitor implements BoundedStringVisitor<String> {
            private final BoundedStringVisitor<String> visitor;
            private final int length;
            private final int suffixLength;
            private final StringBuilder builder;

            private int previousSuffix;

            public ReplacerVisitor(StringBuilder builder, String suffix, BoundedStringVisitor<String> visitor, int lengthOfBetweenMatchesString) {
                this.visitor = visitor;
                this.length = lengthOfBetweenMatchesString;
                suffixLength = suffix.length();
                previousSuffix = -1;
                this.builder = builder;
            }

            @Override
            public String visit(String match, String prefix, String suffix, int prefixPosition, int suffixPosition, final String initialString) {
                // use the generator to replace it
                String replacement = visitor.visit(match, prefix, suffix, prefixPosition, suffixPosition, initialString);

                // if we had a previous suffix
                if (previousSuffix > 0 && previousSuffix < length - suffixLength) {
                    // add the text between the previous suffix and the new prefix
                    builder.append(initialString.substring(previousSuffix + suffixLength, prefixPosition));
                }

                // add the replaced text
                builder.append(replacement);

                // update previous suffix
                previousSuffix = suffixPosition;

                return replacement;
            }

            @Override
            public String initialValue(String initial) {
                return initial;
            }
        }
    }

    public static interface BoundedStringVisitor<T> {
        T visit(String match, String prefix, String suffix, int prefixPosition, int suffixPosition, final String initialString);

        T initialValue(String initial);
    }

    public static abstract class StringReplacementGenerator implements BoundedStringVisitor<String> {
        public abstract String getReplacementFor(String match, String prefix, String suffix);

        @Override
        public String visit(String match, String prefix, String suffix, int prefixPosition, int suffixPosition, final String initialString) {
            return getReplacementFor(match, prefix, suffix);
        }

        @Override
        public String initialValue(String initial) {
            return initial;
        }
    }

    public static class ConstantStringReplacementGenerator extends StringReplacementGenerator {
        public static final ConstantStringReplacementGenerator REPLACE_BY_EMPTY = new ConstantStringReplacementGenerator("");
        private final String replacement;

        public ConstantStringReplacementGenerator(String replacement) {
            this.replacement = replacement;
        }

        public String getReplacementFor(String match, String prefix, String suffix) {
            return replacement;
        }
    }
}
