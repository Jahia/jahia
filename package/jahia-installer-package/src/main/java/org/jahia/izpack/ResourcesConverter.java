package org.jahia.izpack;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

/**
 * Converts the property files into XML language packs for IzPack installer.
 * 
 * @author Sergiy Shyrkov
 */
public class ResourcesConverter {

    /**
     * Performs conversion of the property files into XML language packs.
     * 
     * @param args
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        String bundleName = args[0];
        String[] locales = args[1].split(",");
        File targetDir = new File(args[2]);
        System.out.println("Converting resource bundle " + bundleName + " to XML language packs...");
        for (String lc : locales) {
            Locale currentLocale = new Locale(lc.trim());
            System.out.println("...locale " + currentLocale.getDisplayName(Locale.ENGLISH));
            convert(bundleName, currentLocale, targetDir);
        }
        System.out.println("...converting done.");
    }

    /**
     * Performs conversion of the property file into XML language pack.
     * 
     * @param bundleName the resource bundle name
     * @param locale locale to be used
     * @param targetFolder the target folder
     * @throws FileNotFoundException
     */
    private static void convert(String bundleName, Locale locale, File targetFolder) throws FileNotFoundException {
        ResourceBundle enBundle = ResourceBundle.getBundle(bundleName, Locale.ENGLISH);
        ResourceBundle bundle = null;
        try {
            bundle = ResourceBundle.getBundle(bundleName, locale);
        } catch (MissingResourceException e) {
            bundle = enBundle;
        }
        PrintWriter out = new PrintWriter(new File(targetFolder, StringUtils.substringAfterLast(bundleName, ".")
                + ".xml_" + locale.getISO3Language()));
        Enumeration<String> keyEnum = enBundle.getKeys();
        List<String> keys = new LinkedList<String>();
        while (keyEnum.hasMoreElements()) {
            keys.add(keyEnum.nextElement());
        }
        Collections.sort(keys);
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println("<langpack>");
        for (String key : keys) {
            String value = null;
            try {
                value = bundle.getString(key);
            } catch (MissingResourceException e) {
                try {
                    value = enBundle.getString(key);
                } catch (MissingResourceException e2) {
                    value = key;
                }
            }
            out.append("    <str id=\"").append(key).append("\" txt=\"").append(StringEscapeUtils.escapeXml(value))
                    .append("\"/>");
            out.println();
        }
        out.println("</langpack>");
        out.flush();
        out.close();
    }
}
