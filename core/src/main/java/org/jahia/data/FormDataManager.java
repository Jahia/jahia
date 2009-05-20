/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
//  FormDataManager
//  EV    18.11.2000
//  POL   08.02.2001
//
//  encode              // encodes data from http form     (' -> &#39;)
//  formDecode          // encodes special chars for forms (" -> &quot; and '<' -> &lt;)
//  decode              // decodes everything
//  checkIntegrity
//  isTagClosed         // check if a tag is closed
//  removeTag
//  removeTags
//  removeTagWithContent
//

package org.jahia.data;

import org.jahia.utils.JahiaTools;

/**
 * <p>Title: HTML Form processing singleton</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Jahia Ltd</p>
 *
 * @author Eric Vassalli
 * @author Philippe Vollenweider
 * @author Serge Huber
 * @version 1.0
 */
public class FormDataManager {

    /**
     * formEncode
     * from the file to the form
     * substitutions: < to &lt;
     * " to &quot;
     * EV    19.11.2000
     * POL   09.02.2001
     */
    public static String formEncode(String str) {
        if (str == null) {
            return null;
        }

//        str = JahiaTools.replacePattern(str, "&", "&amp;");
//        str = JahiaTools.replacePattern(str, "< ", "&lt;");
        str = JahiaTools.text2XML(str);
        return str;
    }

    /**
     * decode
     * From the file to a web page
     * 1. if we are not in a <html> session -> replace \n with <br/> tag
     * 2. replace special char with html code
     * POL   08.02.2001
     */
    public static String formDecode(String str) {
        if (str == null) {
            return null;
        }

        str = JahiaTools.replacePattern(str, "&amp;", "&");
        str = JahiaTools.replacePattern(str, "&lt;", "<");
        str = JahiaTools.replacePattern(str, "&gt;", ">");
        str = JahiaTools.html2text(str);

        return str;
    } // end decode

    /**
     * @param str
     */
    public static String htmlEncode(String str) {
        if (str == null) {
            return null;
        }
        final StringBuffer result = new StringBuffer(str.length() + 500);
        String strLower = str.toLowerCase();
        int startOfIndex = 0;
        int i = strLower.indexOf("<html>");
        while (i != -1) {
            result.append(JahiaTools.replacePattern(str.substring(startOfIndex,
                    i), "\n", "<br/>"));
            startOfIndex = i + 6; // 6 -> <html>
            i = strLower.indexOf("</html>", startOfIndex);
            if (i != -1) {
                result.append(str.substring(startOfIndex, i));
                startOfIndex = i + 7; // 7 -> </html>
            }
            i = strLower.indexOf("<html>", startOfIndex);
        }
        str = removeSpecialTags(str);
        //str = JahiaTools.replacePattern(str, "&#64;", "@");
        str = JahiaTools.replacePattern(str, "&quot;", "\"");
        return str;
    }

    /**
     * Remove tags <body>, <head>, <title>, <frame>, <frameset>
     *
     * @param str
     */
    public static String removeSpecialTags(String str) {
        if (str == null) {
            return null;
        }

        str = removeTags(str, "body");
        str = removeTagWithContent(str, "head");
        str = removeTagWithContent(str, "title");
        str = removeTagWithContent(str, "frame");
        str = removeTagWithContent(str, "frameset");

        return str;
    }

    /*
     * Remove a start & end tag in a string
     * For example removeTags(str,"title") will remove <title> and </title>
     * @author POL
     * @version 1.0   POL 23/01/2002
     * @param  str    Input String
     * @param  tag    Tag to remove
     * @return str
     **/
    private static String removeTags(String str, String tag) {
        str = removeTag(str, tag);
        str = removeTag(str, "/" + tag);
        return str;
    }

    /*
     * Remove a tag from a string
     * Exemple: removeTag(str,"body") will remove <body bgcolor="#ffffff">
     * @author POL
     * @version 1.0   POL 23/01/2002
     * @param  str    Input String
     * @param  tag    Tag to remove
     * @return str
     **/
    private static String removeTag(String str, String tag) {
        if (str == null) {
            return null;
        }
        StringBuffer result = new StringBuffer(str.length());
        String strLower = str.toLowerCase();
        tag = tag.toLowerCase();
        int startOfIndex = 0;
        int i = strLower.indexOf("<" + tag);
        while (i != -1) {
            result.append(str.substring(startOfIndex, i));
            i = strLower.indexOf(">", i);
            if (i != -1) {
                startOfIndex = i + 1;
            }
            i = strLower.indexOf("<" + tag, startOfIndex);
        }
        str = result.append(str.substring(startOfIndex, str.length())).toString();
        return str;
    }

    /*
     * Remove a start and end tag with content from a string
         * Exemple: removeTag(str,"title") will remove <title>thist is a title</title>
     * @author POL
     * @version 1.0   POL 23/01/2002
     * @param  str    Input String
     * @param  tag    Tag to remove
     * @return str
     **/
    private static String removeTagWithContent(String str, String tag) {
        if (str == null) {
            return null;
        }
        StringBuffer result = new StringBuffer(str.length());
        String strLower = str.toLowerCase();
        tag = tag.toLowerCase();
        int startOfIndex = 0;
        int i = strLower.indexOf("<" + tag);
        while (i != -1) {
            result.append(str.substring(startOfIndex, i));
            startOfIndex = i + tag.length() + 1;
            i = strLower.indexOf(tag + ">", startOfIndex);
            if (i != -1) {
                startOfIndex = i + tag.length() + 1;
            }
            i = strLower.indexOf("<" + tag, startOfIndex);
        }
        str = result.append(str.substring(startOfIndex, str.length())).toString();
        return str;
    }

}