/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.utils;

import org.apache.commons.lang.StringUtils;

import java.util.Arrays;

/**
 * @author Christophe Laprun
 */
public class TextUtils {

    public static String getStringBetween(char[] charArray, int start, int end) {
        return new String(charArray, start, end - start);
    }

    public static <T> T visitBoundedString(String initial, String prefix, String suffix, BoundedStringVisitor<T> visitor) {
        return visitBoundedString(initial, prefix, suffix, new Matcher<>(prefix, suffix, visitor));
    }

    private static <T> T visitBoundedString(String initial, String prefix, String suffix, Matcher<T> matcher) {
        if (matcher == null) {
            throw new IllegalArgumentException("Must provide a non-null matcher!");
        }

        final BoundedStringVisitor<T> visitor = matcher.getVisitor();

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

    public static String replaceBoundedString(final String initial, final String prefix, final String suffix, ReplacementGenerator visitor) {
        return visitBoundedString(initial, prefix, suffix, new Replacer(prefix, suffix, visitor));
    }


    private static class ArrayMatches implements Matches {
        private Match[] matches = new Match[25];
        private int lastMatchIndex = 0;
        private int nbOfMatches = 0;
        private boolean isMatching = false;
        private static final Match INEXISTING = new Match(Integer.MAX_VALUE, -1);

        public ArrayMatches() {
            Arrays.fill(matches, INEXISTING);
        }

        public void matchingComplete() {
            lastMatchIndex = 0;
            Arrays.sort(matches);
            isMatching = true;
        }

        public void add(int start, int end) {
            // grow array if needed
            final int length = matches.length;
            if (lastMatchIndex == length - 1) {
                final int newSize = length * 2;
                Match[] newMatches = new Match[newSize];
                System.arraycopy(matches, 0, newMatches, 0, length);
                Arrays.fill(newMatches, length, newSize, INEXISTING);
                matches = newMatches;
            }

            matches[lastMatchIndex++] = new Match(start, end);
            nbOfMatches++;
        }

        @Override
        public String toString() {
            return Arrays.toString(matches);
        }

        public boolean matchExists(int start) {
            return indexOf(start) >= 0;
        }

        private int indexOf(int start) {
            // if we're matching, we are already sorted so no need to do it again
            if (!isMatching) {
                Arrays.sort(matches);
            }
            return Arrays.binarySearch(matches, new Match(start, -1));
        }

        public void remove(int start) {
            lastMatchIndex++;
        }

        public int get(int start) {
            final int i = indexOf(start);
            return matches[i].end;
        }

        public boolean isEmpty() {
            return lastMatchIndex == nbOfMatches || lastMatchIndex == matches.length || matches.length == 0;
        }

        public Match firstMatch() {
            return matches[lastMatchIndex];
        }

        public Matches after(int position) {
            while (lastMatchIndex < matches.length) {
                final Match match = matches[lastMatchIndex];
                if (match.start >= position) {
                    break;
                }
                lastMatchIndex++;
            }
            return this;
        }

    }


    private interface Matches {
        void matchingComplete();

        void add(int start, int end);

        boolean matchExists(int start);

        void remove(int start);

        int get(int start);

        boolean isEmpty();

        Match firstMatch();

        Matches after(int position);
    }

    private static class Matcher<T> {
        protected final String prefix;
        protected final String suffix;
        protected final int prefixLength;
        protected final int suffixLength;
        protected int length;
        protected BoundedStringVisitor<T> visitor;
        private final Matches matches = new ArrayMatches();

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
                return visitor.visit(prefix, suffix, prefixLength, length, initialString.toCharArray());
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
                if (suffixIndex < length) {
                    final int previousSuffix = suffixIndex;
                    // next suffix is the one after the one we just matched to the prefix we were looking at, checking that we don't go out of bounds
                    final int nextSuffix = initialString.indexOf(suffix, matches.get(prefixIndex) + suffixLength);
                    suffixIndex = ensureSuffixIndex(nextSuffix);
                    // next prefix is the one after the suffix we just matched
                    prefixIndex = initialString.indexOf(prefix, previousSuffix + suffixLength);
                }
            }

            // once we have matched all our pairs, visit them
            // prepare matches for visits if needed
            matches.matchingComplete();

            return visitMatches(matches, visitor.initialValue(initialString), initialString.toCharArray());
        }

        public T match(String preMatches, String betweenMatches, String postMatches) {
            return match(betweenMatches);
        }

        /**
         * Visits matching prefix / suffix pairs, using the specified StringBuilder to build the final String
         *  @param matches        remaining matching pairs (key: prefix index, value: suffix index)
         * @param previousResult the result that has been accumulated so far during the matching process
         * @param stringAsCharArray  the String on which the matching is performed as a char[]
         */
        private T visitMatches(Matches matches, T previousResult, final char[] stringAsCharArray) {
            T result = previousResult;
            while (!matches.isEmpty()) {
                final Match match = matches.firstMatch();
                int pairPrefix = match.start;
                int pairSuffix = match.end;

                // visit the current match
                final int prefixPosition;
                final int suffixPosition;
                if (pairPrefix >= length - 1) {
                    prefixPosition = length;
                    suffixPosition = length;
                } else {
                    prefixPosition = pairPrefix + prefixLength;
                    suffixPosition = pairSuffix;
                }
                result = visitor.visit(prefix, suffix, prefixPosition, suffixPosition, stringAsCharArray);

                // remove current match
                this.matches.remove(pairPrefix);

                // move to next matches after the current suffix
                matches = matches.after(pairSuffix);
            }
            return result;
        }

        private int ensureSuffixIndex(int potentialNextSuffix) {
            return potentialNextSuffix >= 0 ? potentialNextSuffix : length - 1;
        }

        private boolean match(final int prefixIndex, final int suffixIndex, final String initialString) {
            if (prefixIndex == suffixIndex) {
                matches.add(prefixIndex, suffixIndex);
                return true;
            }

            int inBetweenPrefix = initialString.lastIndexOf(prefix, suffixIndex - 1);

            if (inBetweenPrefix == prefixIndex) {
                // we have a match, record it
                matches.add(prefixIndex, suffixIndex);
                return true;
            }

            if (inBetweenPrefix >= 0) {
                // if we already have matched this in between prefix, find the previous unmatched one
                while (matches.matchExists(inBetweenPrefix)) {
                    inBetweenPrefix = initialString.lastIndexOf(prefix, inBetweenPrefix - 1);
                    if (inBetweenPrefix == -1) {
                        return false;
                    }
                }

                if (inBetweenPrefix == prefixIndex) {
                    // we have a match, record it
                    matches.add(prefixIndex, suffixIndex);
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

        public BoundedStringVisitor<T> getVisitor() {
            return visitor;
        }
    }

    private static class Replacer extends Matcher<String> {

        public Replacer(String prefix, final String suffix, final ReplacementGenerator visitor) {
            super(prefix, suffix, new ReplacerVisitor(suffix, visitor));
        }

        @Override
        public String match(String beforeFirstPrefix, String initialString, String afterLastSuffix) {
            final ReplacerVisitor replacerVisitor = (ReplacerVisitor) getVisitor();
            replacerVisitor.initVisitor(beforeFirstPrefix.length() + initialString.length() + afterLastSuffix.length(), initialString.length());
            StringBuilder builder = replacerVisitor.builder;

            // add String before first prefix
            builder.append(beforeFirstPrefix);

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
            private final ReplacementGenerator replacementGenerator;
            private final int suffixLength;

            private int length;
            private StringBuilder builder;
            private int previousSuffix;

            public ReplacerVisitor(String suffix, ReplacementGenerator replacementGenerator) {
                this.replacementGenerator = replacementGenerator;
                suffixLength = suffix.length();
                previousSuffix = -1;
            }

            void initVisitor(int builderCapacity, int lengthOfBetweenMatchesString) {
                builder = new StringBuilder(builderCapacity);
                this.length = lengthOfBetweenMatchesString;
            }

            @Override
            public String visit(String prefix, String suffix, int matchStart, int matchEnd, final char[] initialStringAsCharArray) {
                // if we had a previous suffix
                if (previousSuffix > 0 && previousSuffix < length - suffixLength) {
                    // add the text between the previous suffix and the new prefix
                    builder.append(initialStringAsCharArray, previousSuffix + suffixLength, matchStart - prefix.length() - previousSuffix - suffixLength);
                }

                // add the replaced text
                replacementGenerator.appendReplacementForMatch(matchStart, matchEnd, initialStringAsCharArray, builder, prefix, suffix);

                // update previous suffix
                previousSuffix = matchEnd;

                return null;
            }

            @Override
            public String initialValue(String initial) {
                return initial;
            }
        }
    }

    public interface ReplacementGenerator {
        void appendReplacementForMatch(final int matchStart, final int matchEnd, final char[] initialStringAsCharArray, StringBuilder builder, String prefix, String suffix);
    }

    public interface BoundedStringVisitor<T> {
        T visit(final String prefix, final String suffix, final int matchStart, final int matchEnd, final char[] initialStringAsCharArray);

        T initialValue(String initial);
    }

    public static class ConstantStringReplacementGenerator implements ReplacementGenerator {
        public static final ConstantStringReplacementGenerator REPLACE_BY_EMPTY = new ConstantStringReplacementGenerator("");
        private final String replacement;

        public ConstantStringReplacementGenerator(String replacement) {
            this.replacement = replacement;
        }

        @Override
        public void appendReplacementForMatch(int matchStart, int matchEnd, char[] initialStringAsCharArray, StringBuilder builder, String prefix, String suffix) {
            builder.append(replacement);
        }
    }

    /**
     * To replace accented characters in a String by unaccented equivalents.
     */
    public static String removeAccents(String term) {
        int length = term.length();
        char[] input = new char[length];
        term.getChars(0, length, input, 0);


        char[] output = new char[length];
        // Worst-case length required:
        final int maxSizeNeeded = 2 * length;

        int size = output.length;
        while (size < maxSizeNeeded)
            size *= 2;

        if (size != output.length)
            output = new char[size];

        int outputPos = 0;

        int pos = 0;

        for (int i = 0; i < length; i++, pos++) {
            final char c = input[pos];

            // Quick test: if it's not in range then just keep
            // current character
            if (c < '\u00c0' || c > '\uFB06')
                output[outputPos++] = c;
            else {
                switch (c) {
                    case '\u00C0': // À
                    case '\u00C1': // Á
                    case '\u00C2': // Â
                    case '\u00C3': // Ã
                    case '\u00C4': // Ä
                    case '\u00C5': // Å
                        output[outputPos++] = 'A';
                        break;
                    case '\u00C6': // Æ
                        output[outputPos++] = 'A';
                        output[outputPos++] = 'E';
                        break;
                    case '\u00C7': // Ç
                        output[outputPos++] = 'C';
                        break;
                    case '\u00C8': // È
                    case '\u00C9': // É
                    case '\u00CA': // Ê
                    case '\u00CB': // Ë
                        output[outputPos++] = 'E';
                        break;
                    case '\u00CC': // Ì
                    case '\u00CD': // Í
                    case '\u00CE': // Î
                    case '\u00CF': // Ï
                        output[outputPos++] = 'I';
                        break;
                    case '\u0132': // Ĳ
                        output[outputPos++] = 'I';
                        output[outputPos++] = 'J';
                        break;
                    case '\u00D0': // Ð
                        output[outputPos++] = 'D';
                        break;
                    case '\u00D1': // Ñ
                        output[outputPos++] = 'N';
                        break;
                    case '\u00D2': // Ò
                    case '\u00D3': // Ó
                    case '\u00D4': // Ô
                    case '\u00D5': // Õ
                    case '\u00D6': // Ö
                    case '\u00D8': // Ø
                        output[outputPos++] = 'O';
                        break;
                    case '\u0152': // Œ
                        output[outputPos++] = 'O';
                        output[outputPos++] = 'E';
                        break;
                    case '\u00DE': // Þ
                        output[outputPos++] = 'T';
                        output[outputPos++] = 'H';
                        break;
                    case '\u00D9': // Ù
                    case '\u00DA': // Ú
                    case '\u00DB': // Û
                    case '\u00DC': // Ü
                        output[outputPos++] = 'U';
                        break;
                    case '\u00DD': // Ý
                    case '\u0178': // Ÿ
                        output[outputPos++] = 'Y';
                        break;
                    case '\u00E0': // à
                    case '\u00E1': // á
                    case '\u00E2': // â
                    case '\u00E3': // ã
                    case '\u00E4': // ä
                    case '\u00E5': // å
                        output[outputPos++] = 'a';
                        break;
                    case '\u00E6': // æ
                        output[outputPos++] = 'a';
                        output[outputPos++] = 'e';
                        break;
                    case '\u00E7': // ç
                        output[outputPos++] = 'c';
                        break;
                    case '\u00E8': // è
                    case '\u00E9': // é
                    case '\u00EA': // ê
                    case '\u00EB': // ë
                        output[outputPos++] = 'e';
                        break;
                    case '\u00EC': // ì
                    case '\u00ED': // í
                    case '\u00EE': // î
                    case '\u00EF': // ï
                        output[outputPos++] = 'i';
                        break;
                    case '\u0133': // ĳ
                        output[outputPos++] = 'i';
                        output[outputPos++] = 'j';
                        break;
                    case '\u00F0': // ð
                        output[outputPos++] = 'd';
                        break;
                    case '\u00F1': // ñ
                        output[outputPos++] = 'n';
                        break;
                    case '\u00F2': // ò
                    case '\u00F3': // ó
                    case '\u00F4': // ô
                    case '\u00F5': // õ
                    case '\u00F6': // ö
                    case '\u00F8': // ø
                        output[outputPos++] = 'o';
                        break;
                    case '\u0153': // œ
                        output[outputPos++] = 'o';
                        output[outputPos++] = 'e';
                        break;
                    case '\u00DF': // ß
                        output[outputPos++] = 's';
                        output[outputPos++] = 's';
                        break;
                    case '\u00FE': // þ
                        output[outputPos++] = 't';
                        output[outputPos++] = 'h';
                        break;
                    case '\u00F9': // ù
                    case '\u00FA': // ú
                    case '\u00FB': // û
                    case '\u00FC': // ü
                        output[outputPos++] = 'u';
                        break;
                    case '\u00FD': // ý
                    case '\u00FF': // ÿ
                        output[outputPos++] = 'y';
                        break;
                    case '\uFB00': // ﬀ
                        output[outputPos++] = 'f';
                        output[outputPos++] = 'f';
                        break;
                    case '\uFB01': // ﬁ
                        output[outputPos++] = 'f';
                        output[outputPos++] = 'i';
                        break;
                    case '\uFB02': // ﬂ
                        output[outputPos++] = 'f';
                        output[outputPos++] = 'l';
                        break;
                    // following 2 are commented as they can break the maxSizeNeeded (and doing *3 could be expensive)
//          case '\uFB03': // ﬃ
//              output[outputPos++] = 'f';
//              output[outputPos++] = 'f';
//              output[outputPos++] = 'i';
//              break;
//          case '\uFB04': // ﬄ
//              output[outputPos++] = 'f';
//              output[outputPos++] = 'f';
//              output[outputPos++] = 'l';
//              break;
                    case '\uFB05': // ﬅ
                        output[outputPos++] = 'f';
                        output[outputPos++] = 't';
                        break;
                    case '\uFB06': // ﬆ
                        output[outputPos++] = 's';
                        output[outputPos++] = 't';
                        break;
                    default:
                        output[outputPos++] = c;
                        break;
                }
            }
        }
        return (new String(output)).trim();
    }

    private static class Match implements Comparable<Match> {
        public Match(int start, int end) {
            this.start = start;
            this.end = end;
        }

        final int start;
        final int end;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Match match = (Match) o;

            return start == match.start;
        }

        @Override
        public int hashCode() {
            return start;
        }

        @Override
        public String toString() {
            return "[" + start + ", " + end + "]";
        }

        @Override
        public int compareTo(Match o) {
            return o == null ? -1 : start - o.start;
        }
    }
}
