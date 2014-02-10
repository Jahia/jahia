/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
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
     * Extracts the language code that might be available in the form of a <code>jcr:language = 'lang'</code> constraint from the given query statement.
     *
     * @param statement the query statement from which to extract a potential language code
     * @return the language code associated with the jcr:language constraint if it exists in the specified query statement or <code>null</code> otherwise.
     */
    public static String extractLanguageOrNullFromStatement(String statement) {
        int langIndex = statement.indexOf(Constants.JCR_LANGUAGE);
        if (langIndex >= 0) {
            int begLang = statement.indexOf('\'', langIndex);
            if (begLang >= 0) {
                begLang = begLang + 1; // move past '
                int endLang = statement.indexOf('\'', begLang);
                if (endLang > 0 && endLang < statement.length()) {
                    return statement.substring(begLang, endLang).trim();
                }
            }
        }

        return null;
    }
}
