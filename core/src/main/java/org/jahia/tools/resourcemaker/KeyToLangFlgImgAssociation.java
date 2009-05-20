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
 * merged to "JahiaInternalResources.properties" file.
 *
 * WARNING ! The image resource flag should exist in path
 * "/engines/images/flags/".
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

    private static final String _imagePath = "/engines/images/flags/";
    private static final String _resourceFileName = "lang_flg_resource_bundle.properties";
}
