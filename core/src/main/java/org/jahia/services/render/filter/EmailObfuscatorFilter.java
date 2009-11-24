package org.jahia.services.render.filter;

import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;

import javax.jcr.RepositoryException;
import java.io.IOException;
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

    private static final String sp = "\\x21\\x23-\\x27\\x2A\\x2B\\x2D\\x2F\\x3D\\x3F\\x5E-\\x60\\x7B-\\x7E";
    private static final String atext = "[a-zA-Z0-9" + sp + "]";
    private static final String atom = FWS + "?" + atext + "+" + FWS + "?";
    private static final String dotAtom = "\\." + atom;
    private static final String dotAtomText = FWS + "?" + atom + "(" + dotAtom + ")*" + FWS + "?";

    // quoted string stuff
    private static final String qtext = "[" + NOWSCTL + "\\x21\\x23-\\x5B\\x5D-\\x7E]";
    private static final String text = "[\\x01-\\x09\\x0B\\x0C\\x0E-\\x7F]";
    private static final String quotedPair = "\\x5C" + text;
    private static final String qcontent = "(" + qtext + "|" + quotedPair + ")";
    private static final String quotedString = FWS + "?" + "\\x22(" + FWS + "?" + qcontent + ")*" + FWS + "?\\x22" + FWS + "?";
    private static final String localpart = "(" + dotAtomText + "|" + quotedString + ")";

    // domain stuff
    private static final String dtext = "[" + NOWSCTL + "\\x21-\\x5A\\x5E-\\x7E]";
    private static final String dcontent = "(" + dtext + "|" + quotedPair + ")";
    private static final String domainLiteral = FWS + "?" + "\\x5B(" + FWS + "?" + dcontent + ")*" + FWS + "?\\x5D" + FWS + "?";
    private static final String domain = "(" + dotAtomText + "|" + domainLiteral + ")";

    // final actual address (used in the simple version)
    private static final String addrSpec = "(" + localpart + "@" + domain + ")";

    // compile version to check email within string
    public static final Pattern VALID_EMAIL_IN_STRING_SIMPLE = Pattern.compile(".*" + addrSpec + ".*", Pattern.DOTALL);

    public String doFilter(RenderContext renderContext, Resource resource, RenderChain chain) throws IOException, RepositoryException {
        String out = chain.doFilter(renderContext, resource);

        StringBuffer wholeHtml = new StringBuffer(out);

        StringTokenizer st = new StringTokenizer(out);

        while (st.hasMoreTokens()) {
            String current = st.nextToken();
            if (containsAddress(current)) {
                String[] split = current.split(addrSpec, 2);
                // separate the email out
                String email = current.substring(split[0].length(), current.length() - split[1].length());

                // now go through all occurances of the found email in the document
                int index = wholeHtml.indexOf(email);
                int lastIndex = index;

                // as long as we still find one, keep going
                while (index != -1) {

                    // index to search from next time
                    lastIndex = index + 1;

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
        return (string != null) && VALID_EMAIL_IN_STRING_SIMPLE.matcher(string).matches();
    }

    static String convertToHtmlEntity(String email) {
        String toReturn = "";

        for (int i = 0; i < email.length(); i++) {
            toReturn += "&#" + (int) email.charAt(i) + ";";
        }

        return toReturn;
    }

}
