/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

/*
 * ----- BEGIN LICENSE BLOCK -----
 * Version: JCSL 1.0
 *
 * The contents of this file are subject to the Jahia Community Source License
 * 1.0 or later (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.jahia.org/license
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the rights, obligations and limitations governing use of the contents
 * of the file. The Original and Upgraded Code is the Jahia CMS and Portal
 * Server. The developer of the Original and Upgraded Code is JAHIA Ltd. JAHIA
 * Ltd. owns the copyrights in the portions it created. All Rights Reserved.
 *
 * The Shared Modifications are Jahia tools resource maker.
 *
 * The Developer of the Shared Modifications is Jahia Solution S�rl.
 * Portions created by the Initial Developer are Copyright (C) 2002 by the
 * Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Sep 24 2002 Jahia Solutions S�rl: MAP Initial release.
 *
 * ----- END LICENSE BLOCK -----
 */

package org.jahia.tools.resourcemaker;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Locale;

/**
 * <p>Title: Jahia tools resource maker</p>
 * <p>Description: Generate the resource bundle file that can be merged to the
 * Jahia resource bundle main file. This file contain the flag (on/off) resource
 * image key followed by the path to the resource in question.</p>
 *
 * Usage example: In "./jahia/build/classes" directory type :
 * # java org.jahia.tools.resourcemaker.KeyToLangFlgImgAssociation
 * The output "lang_flg_resource_bundle.properties" file is generated and can be
 * merged to "JahiaEnginesResources.properties" file.
 *
 * WARNING ! The image resource flag should exist in path
 * "/jsp/jahia/engines/images/flags/".
 *
 * <p>Copyrights: MAP (Jahia Solution S�rl 2002)</p>
 * <p>Company: Jahia Solutions S�rl 2002</p>
 * @author MAP
 * @version 1.0
 */
public final class KeyToLangFlgImgAssociation {

    public static void main(String[] args) {

        Locale[] availableLocales = Locale.getAvailableLocales();
        StringBuffer fileContent = new StringBuffer("");
        for (int i=0; i < availableLocales.length; i++) {
            Locale curLocale = availableLocales[i];
            String country = curLocale.getCountry();
            fileContent.append(curLocale.getLanguage() + (country.equals("") ? "" : "_" + country) +
                               "FlagOn = " + _imagePath + curLocale.getLanguage() + "_on.gif\n");
            fileContent.append(curLocale.getLanguage() + (country.equals("") ? "" : "_" + country) +
                               "FlagOff = " + _imagePath + curLocale.getLanguage() + "_off.gif\n");
        }
        writeFile(_resourceFileName, fileContent.toString());
    }

    private static void writeFile(String fileName, String fileContent) {
        try {
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(
                    new FileOutputStream(fileName)));
            out.write(fileContent);
            out.close();
        } catch (IOException ie) {
            System.out.println(ie.getMessage());
        }
    }

    private static final String _imagePath = "/jsp/jahia/engines/images/flags/";
    private static final String _resourceFileName = "lang_flg_resource_bundle.properties";
}
