/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ======================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 *
 *
 * ==========================================================================================
 * =                                   ABOUT JAHIA                                          =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia’s Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to “the Tunnel effect”, the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
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
                out.append(normalizeKey(key)).append(":\"").append(escape(value)).append("\"");
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

    private static String normalizeKey(String key) {
        String normalized = key.indexOf('.') != -1 ? key.replace('.', '_') : key;
        if (normalized.indexOf('-') != -1) {
            normalized = normalized.replace('-', '_');
        }
        return normalized;
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
