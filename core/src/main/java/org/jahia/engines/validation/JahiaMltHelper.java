/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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
