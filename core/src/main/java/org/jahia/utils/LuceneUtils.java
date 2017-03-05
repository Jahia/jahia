/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.utils;

import org.apache.jackrabbit.core.query.lucene.FieldNames;
import org.jahia.api.Constants;

import java.util.Arrays;
import java.util.Locale;

/**
 * An utility class to gather useful methods to work with Lucene.
 *
 * @author Christophe Laprun
 */
public class LuceneUtils {
    public static final String DASH = "-";

    /**
     * Retrieves the name used for full text fields for the specified site name and language.
     *
     * @param siteName
     * @param language
     * @return
     */
    public static String getFullTextFieldName(String siteName, String language) {
        StringBuilder fulltextNameBuilder = new StringBuilder(64);
        fulltextNameBuilder.append(FieldNames.FULLTEXT);
        if (siteName != null) {
            fulltextNameBuilder.append(DASH).append(siteName);
        }
        if (language != null) {
            fulltextNameBuilder.append(DASH).append(language.trim().toLowerCase());
        }
        return fulltextNameBuilder.toString();
    }

    /**
     * Extracts a language code from the specified field name if one is there to be found or <code>null</code> otherwise. Note that
     * the way how field names are currently encoded might result in false positives.
     *
     * @param fieldName
     * @return
     */
    public static String extractLanguageOrNullFrom(String fieldName) {
        if (fieldName.startsWith(FieldNames.FULLTEXT)) {
            final int lastDash = fieldName.lastIndexOf(DASH);
            if (lastDash > 0) {
                final String languageCode = fieldName.substring(lastDash + DASH.length());
                if (Arrays.binarySearch(Locale.getISOLanguages(), languageCode) >= 0) {
                    return languageCode;
                }
            }
        }

        return null;
    }

    /**
     * Extracts the language code that might be available in the form of a <code>jcr:language</code> constraint from the given query statement.
     * More specifically, the following cases should be properly handled:
     *
     * <ul>
     * <li><code>jcr:language = 'en'</code>  (in that case analyzer should be English)</li>
     * <li><code>jcr:language = "en"</code>  (in that case analyzer should be English)</li>
     * <li><code>jcr:language is null</code> (in that case analyzer cannot be determined by query language - take the default)</li>
     * <li><code>jcr:language is null and jcr:language = 'en'</code> (in that case the query language specific analyzer should be English, but it is set only on the second
     * jcr:language constraint)</li>
     * <li><code>jcr:language &lt;&gt; 'en'</code> (in that case analyzer cannot be determined by query language - take the default)</li>
     * <li><code>jcr:language = 'fr' or jcr:language='en'</code> (in that case analyzer can also not be determined by query language - take the default)</li>
     * <li><code>([jcr:language] = "en" or [jcr:language] is null) and [someOtherProperty] = "test" </code></li>
     * </ul>
     *
     * @param statement the query statement from which to extract a potential language code
     * @return the language code associated with the jcr:language constraint if it exists in the specified query statement or <code>null</code> otherwise.
     */
    public static String extractLanguageOrNullFromStatement(String statement) {
        // search for jcr:language in the statement
        int langIndex = statement.indexOf(Constants.JCR_LANGUAGE);
        String languageCandidate = null; // potential language code candidate
        while (langIndex >= 0) {
            // start looking for the language code after jcr:language
            langIndex = langIndex + Constants.JCR_LANGUAGE.length();

            // look if we have another jcr:language constraints in the rest of the statement?
            int nextLangIndex = statement.indexOf(Constants.JCR_LANGUAGE, langIndex);

            // only look at rest of statement until next jcr:language or end
            String constraint = nextLangIndex < 0 ? statement.substring(langIndex) : statement.substring(langIndex, nextLangIndex);

            // check if there's an equals sign after jcr:language
            int equalsIndex = constraint.indexOf('=');

            // but only consider it if it happens before the next jcr:language constraint or we don't have a next jcr:language
            if (equalsIndex >= 0) {

                // check whether we have a language quoted in single or double quotes
                char quoteChar = '\''; // quote character to use for language identification
                boolean hasQuote = false; // did we find a quote character after equals?

                // potential beginning of the language code
                int begLang = constraint.indexOf(quoteChar);
                if (begLang < 0) {
                    // if we didn't find a single quote, try a double one
                    begLang = constraint.indexOf('\"');

                    if (begLang >= 0) {
                        // we found a double quote, so use that as quote character
                        quoteChar = '\"';
                        hasQuote = true;
                    }
                } else {
                    hasQuote = true;
                }

                if (hasQuote) {
                    // we found a quote character so look for a matching one that would enclose a language code
                    begLang = begLang + 1; // move past quote character

                    // determine the end of the constraint: should end by either ")", "or" or "and" so select the min between all matches for these
                    int endConstraint = getEndOfConstraintIndex(constraint);

                    // only consider the end of the language code if it's within the end of the constraint
                    int endLang = constraint.indexOf(quoteChar, begLang);
                    if (endLang > 0 && endLang <= endConstraint) {

                        // we found a matching closing quote but we need to check that we didn't already identify a language quote
                        if (languageCandidate != null) {
                            // if we already had a candidate language code, we can't decide which to use so return null
                            // case: jcr:language = 'fr' or jcr:language='en'
                            return null;
                        }

                        // extract language code candidate
                        languageCandidate = constraint.substring(begLang, endLang).trim();
                    }
                }
            }

            // look for the next jcr:language statement if it exists
            langIndex = nextLangIndex;
        }

        return languageCandidate;
    }

    private static int getEndOfConstraintIndex(String constraint) {
        int rightParenIndex = constraint.indexOf(')');
        rightParenIndex = rightParenIndex < 0 ? Integer.MAX_VALUE : rightParenIndex;

        int orIndex = constraint.indexOf("or");
        orIndex = orIndex < 0 ? constraint.indexOf("OR") : orIndex;
        orIndex = orIndex < 0 ? Integer.MAX_VALUE : orIndex;

        int andIndex = constraint.indexOf("and");
        andIndex = andIndex < 0 ? constraint.indexOf("AND") : andIndex;
        andIndex = andIndex < 0 ? Integer.MAX_VALUE : andIndex;

        int endConstraint = Math.min(rightParenIndex, Math.min(orIndex, andIndex));
        endConstraint = endConstraint == Integer.MAX_VALUE ? constraint.length() - 1 : endConstraint;
        return endConstraint;
    }
}
