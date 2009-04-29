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
package org.jahia.engines.validation;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
*
* <p>Title: JahiaMltHelper</p>
* <p>Description: This class converts all languages of a field to an array. This is then
*                 used in the user input validation methods.</p>
* <p>Copyright: Copyright (c) 2004</p>
* <p>Company: Jahia Ltd</p>
* @author not attributable
* @version 1.0
*/
public class JahiaMltHelper {
    private String[] text = null;
    private String[] language = null;
    private List languageSettings = null;

    /**
     * 
     */
    public JahiaMltHelper(List currentLanguageSettings) {
        super();
        languageSettings = currentLanguageSettings;
    }

    /**
     * Get the text for a given language key, returns empty string if not found
     * 
     * @param lang a language key
     * @return text
     */
    public String getText(String lang) {
        for (int i = 0; i < language.length; i++) {
            if (language[i].equals(lang)) {
                return text[i];
            }
        }
        return "";
    }

    /**
     * @return language
     */
    public String[] getLanguage() {
        return language;
    }

    /**
     * @return text
     */
    public String[] getText() {
        return text;
    }

    /**
     * @param strings
     */
    public void setLanguage(String[] strings) {
        language = strings;
    }

    /**
     * @param strings
     */
    public void setText(String[] strings) {
        text = strings;
    }

    /**
     * 
     * @param lang
     * @param txt
     */
    public void addMltItem(String lang, String txt) {
        if (language != null && text != null) {
            int oldLen = language.length;
            String newLanguage[] = new String[oldLen + 1];
            String newText[] = new String[oldLen + 1];

            System.arraycopy(language, 0, newLanguage, 0, oldLen);
            System.arraycopy(text, 0, newText, 0, oldLen);
            newLanguage[oldLen] = lang;
            newText[oldLen] = txt;
            language = newLanguage;
            text = newText;
        } else {
            language = new String[1];
            language[0] = lang;
            text = new String[1];
            text[0] = txt;
        }
    }

    /**
     * @return languageSettings
     */
    public List getLanguageSettings() {
        return languageSettings;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public boolean isEmpty() {
        boolean nonEmpty = false;
        for (int i = 0; i < text.length && !nonEmpty; i++) {
            String s = text[i];
            if(StringUtils.trimToNull(s)!=null)nonEmpty =true;
        }
        return !nonEmpty;
    }
}
