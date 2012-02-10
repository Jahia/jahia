/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.ajax.gwt.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.jahia.ajax.gwt.client.messages.Messages;

/**
 * Converts the resource bundle files into JavaScript files to be used as GWt
 * I18N dictionaries.
 * 
 * @author Sergiy Shyrkov
 */
public class GWTDictionaryBuilder {

    /**
     * Performs conversion of the property file into JavaScript file.
     * 
     * @param bundleName the resource bundle name
     * @param locale locale to be used
     * @param targetFolder the target folder
     * @throws IOException in case of an error
     */
    private static void convert(String bundleName, Locale locale, File targetFolder, String targetFileName, boolean minified)
            throws IOException {
        InputStream is = GWTDictionaryBuilder.class.getClassLoader().getResourceAsStream(bundleName.replace('.', '/') + ".properties");
        if (is == null) {
            is = GWTDictionaryBuilder.class.getClassLoader().getResourceAsStream(
                    bundleName.replace('.', '/') + "_en.properties");
            if (is == null) {
                throw new FileNotFoundException("ERROR : Couldn't find bundle with name "
                        + bundleName.replace('.', '/') + ".properties nor "
                        + bundleName.replace('.', '/')
                        + "_en.properties in class loader, skipping...");
            }
        }
        ResourceBundle defBundle = new PropertyResourceBundle(is);
        is.close();

        ResourceBundle bundle = null;
        if (locale != null) {
            try {
                bundle = ResourceBundle.getBundle(bundleName, locale);
            } catch (MissingResourceException e) {
                bundle = defBundle;
            }
        } else {
            bundle = defBundle;
        }
        File target = new File(targetFolder, targetFileName + (locale != null ? "_" + locale.toString() : "") + ".js");
        System.out.print("Creating " + target + " ...");
        PrintWriter out = new PrintWriter(target);
        Enumeration<String> keyEnum = defBundle.getKeys();
        List<String> keys = new LinkedList<String>();
        while (keyEnum.hasMoreElements()) {
            keys.add(keyEnum.nextElement());
        }
        Collections.sort(keys);
        out.print("var " + Messages.DICTIONARY_NAME + "={");
        if (!minified) {
            out.println();
        }
        for (Iterator<String> iterator = keys.iterator(); iterator.hasNext();) {
            String key = iterator.next();
            String value = null;
            try {
                value = bundle.getString(key);
            } catch (MissingResourceException e) {
                try {
                    value = defBundle.getString(key);
                } catch (MissingResourceException e2) {
                    value = null;
                }
            }
            if (value != null) {
                out.append(key.replace('.', '_')).append(":\"").append(escape(value)).append("\"");
                if (iterator.hasNext()) {
                    out.append(",");
                }
                if (!minified) {
                    out.println();
                }
            }
        }
        out.print("};");
        if (!minified) {
            out.println();
        }

        out.flush();
        out.close();
        System.out.println("done");
    }

    /**
     * Performs conversion of the property files into XML language packs.
     * 
     * @param args
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws IOException {
        String bundleName = args[0];
        String[] locales = args[1].split(",");
        File targetDir = new File(args[2]);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        String targetFileName = args[3];
        System.out.println("Converting resource bundle " + bundleName + " into JavaScript files...");
        System.out.println("...no locale (default file)");
        boolean minified = args.length > 4 && Boolean.valueOf(args[4]);
        convert(bundleName, null, targetDir, targetFileName, minified);
        for (String lc : locales) {
            Locale currentLocale = new Locale(lc.trim());
            System.out.println("...locale " + currentLocale.getDisplayName(Locale.ENGLISH));
            convert(bundleName, currentLocale, targetDir, targetFileName, minified);
        }
        System.out.println("...conversion done.");
    }

    private static String escape(String value) {
        StringBuilder out = new StringBuilder(value.length() * 2);
        int sz = value.length();
        for (int i = 0; i < sz; i++) {
            char ch = value.charAt(i);

            // handle unicode
            if (ch > 0xfff) {
                out.append("\\u" + hex(ch));
            } else if (ch > 0xff) {
                out.append("\\u0" + hex(ch));
            } else if (ch > 0x7f) {
                out.append("\\u00" + hex(ch));
            } else if (ch < 32) {
                switch (ch) {
                case '\b':
                    out.append('\\');
                    out.append('b');
                    break;
                case '\n':
                    out.append('\\');
                    out.append('n');
                    break;
                case '\t':
                    out.append('\\');
                    out.append('t');
                    break;
                case '\f':
                    out.append('\\');
                    out.append('f');
                    break;
                case '\r':
                    out.append('\\');
                    out.append('r');
                    break;
                default:
                    if (ch > 0xf) {
                        out.append("\\u00" + hex(ch));
                    } else {
                        out.append("\\u000" + hex(ch));
                    }
                    break;
                }
            } else {
                switch (ch) {
                case '\'':
                    out.append('\\');
                    out.append('\'');
                    break;
                case '"':
                    out.append('\\');
                    out.append('"');
                    break;
                case '\\':
                    out.append('\\');
                    out.append('\\');
                    break;
                case '/':
                    out.append('\\');
                    out.append('/');
                    break;
                default:
                    out.append(ch);
                    break;
                }
            }
        }

        return out.toString();
    }

    private static String hex(char ch) {
        return Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
    }
}
