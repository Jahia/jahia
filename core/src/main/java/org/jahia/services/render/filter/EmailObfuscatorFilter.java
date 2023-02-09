/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.render.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Simple email obfuscation filter. Replaces all mail addresses by entity-encoded values.
 *
 * Based on http://obfuscatortool.sourceforge.net
 */
public class EmailObfuscatorFilter extends AbstractFilter {

    // Whitespace rules
    private static final String WSP = "[\\x20\\x09]";
    private static final String CRLF = "(\\x0D\\x0A)";
    private static final String FWS = "((" + WSP + "*" + CRLF + ")?" + WSP + "+)";
    private static final String NOWSCTL = "\\x01-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F";

    private static final String SP = "\\x21\\x23-\\x27\\x2A\\x2B\\x2D\\x2F\\x3D\\x3F\\x5E-\\x60\\x7B-\\x7E";
    private static final String ATEXT = "[a-zA-Z0-9" + SP + "]";
    private static final String ATOM = FWS + "?" + ATEXT + "+" + FWS + "?";
    private static final String DOT_ATOM = "\\." + ATOM;
    private static final String DOT_ATOM_TEXT = FWS + "?" + ATOM + "(" + DOT_ATOM + ")*" + FWS + "?";

    // quoted string stuff
    private static final String QTEXT = "[" + NOWSCTL + "\\x21\\x23-\\x5B\\x5D-\\x7E]";
    private static final String TEXT = "[\\x01-\\x09\\x0B\\x0C\\x0E-\\x7F]";
    private static final String QUOTED_PAIR = "\\x5C" + TEXT;
    private static final String QCONTENT = "(" + QTEXT + "|" + QUOTED_PAIR + ")";
    private static final String QUOTED_STRING = FWS + "?" + "\\x22(" + FWS + "?" + QCONTENT + ")*" + FWS + "?\\x22" + FWS + "?";
    private static final String LOCAL_PART = "(" + DOT_ATOM_TEXT + "|" + QUOTED_STRING + ")";

    // DOMAIN stuff
    private static final String DTEXT = "[" + NOWSCTL + "\\x21-\\x5A\\x5E-\\x7E]";
    private static final String DCONTENT = "(" + DTEXT + "|" + QUOTED_PAIR + ")";
    private static final String DOMAIN_LITERAL = FWS + "?" + "\\x5B(" + FWS + "?" + DCONTENT + ")*" + FWS + "?\\x5D" + FWS + "?";
    private static final String DOMAIN = "(" + DOT_ATOM_TEXT + "|" + DOMAIN_LITERAL + ")";

    // final actual address (used in the simple version)
    private static final String ADDR_SPEC = "(" + LOCAL_PART + "@" + DOMAIN + ")";

    // compile version to check email within string
    public static final Pattern VALID_EMAIL_IN_STRING_SIMPLE = Pattern.compile(".*" + ADDR_SPEC + ".*", Pattern.DOTALL);

    @Override
    public String execute(String previousOut, RenderContext renderContext, Resource resource, RenderChain chain)
            throws Exception {
        StringBuilder wholeHtml = new StringBuilder(previousOut);

        StringTokenizer st = new StringTokenizer(previousOut);

        while (st.hasMoreTokens()) {
            String current = st.nextToken();
            if (containsAddress(current)) {
                String[] split = current.split(ADDR_SPEC, 2);
                // separate the email out
                String email = current.substring(split[0].length(), current.length() - split[1].length());

                // now go through all occurances of the found email in the document
                int index = wholeHtml.indexOf(email);

                // as long as we still find one, keep going
                while (index != -1) {

                    // index to search from next time
                    int lastIndex = index + 1;

                    String entityVersion;

                    // check for mailto:
                    if (index > 7 && wholeHtml.substring(index - 7, index).equals("mailto:")) {
                        entityVersion = convertToHtmlEntity("mailto:" + email);
                        wholeHtml.replace(index - 7, index + email.length(), entityVersion);
                    } else {
                        entityVersion = convertToHtmlEntity(email);
                        wholeHtml.replace(index, index + email.length(), entityVersion);
                    }

                    // get the next index of the email address!
                    index = wholeHtml.indexOf(email, lastIndex);
                }

            }
        }
        return wholeHtml.toString();
    }

    public static boolean containsAddress(String string) {
        if (!string.contains("@")) {
            return false;
        }
        return VALID_EMAIL_IN_STRING_SIMPLE.matcher(string).matches();
    }

    static String convertToHtmlEntity(String email) {
        StringBuilder toReturn = new StringBuilder();

        for (int i = 0; i < email.length(); i++) {
            toReturn.append("&#").append( (int) email.charAt(i) ).append(";");
        }

        return toReturn.toString();
    }

}
