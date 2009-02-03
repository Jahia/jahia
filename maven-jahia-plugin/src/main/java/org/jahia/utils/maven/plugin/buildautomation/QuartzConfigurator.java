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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.utils.maven.plugin.buildautomation;



import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileWriter;

/**
 * Created by IntelliJ IDEA.
 * User: islam
 * Date: 25 juin 2008
 * Time: 11:04:01
 * To change this template use File | Settings | File Templates.
 */
public class QuartzConfigurator {

    public static void updateDataSourceConfiguration(String fileName, Map values)
    {

        File jahiaConfigFile = new File(fileName);
        if (jahiaConfigFile.exists()) {
            // let's load the file's content in memory, assuming it won't be
            // too big.
            StringBuffer fileContentBuf = new StringBuffer();
            try {
                FileReader fileReader = new FileReader(jahiaConfigFile);
                BufferedReader bufReader = new BufferedReader(fileReader);
                int ch = -1;
                while ( (ch = bufReader.read()) != -1) {
                    fileContentBuf.append( (char) ch);
                }
                bufReader.close();
                fileReader.close();

                String fileContent = fileContentBuf.toString();
                String transactionIsolationLevel = getValue(values,"jahia.quartz.selectWithLockSQL");
                String jdbcDelegate = getValue(values,"jahia.quartz.jdbcDelegate");
                String serializable = getValue(values,"jahia.quartz.serializable");
                fileContent = StringUtils.replace(fileContent, "@SELECTWITHLOCKSQL@", transactionIsolationLevel);
                fileContent = StringUtils.replace(fileContent, "@JDBCDELEGATE@", jdbcDelegate);
                fileContent = StringUtils.replace(fileContent, "@SERIALIZABLE@", serializable);
                // we have finished replacing values, let's save the modified
                // file.
                FileWriter fileWriter = new FileWriter(jahiaConfigFile);
                fileWriter.write(fileContent);
                fileWriter.close();

            } catch (java.io.FileNotFoundException fnfe) {

            } catch (java.io.IOException ioe) {

            }

        }
    }

    private static String getValue (Map values, String key) {
        String replacement = (String) values.get(key);
        if (replacement == null) {
            return "";
        }
        replacement = replacement.replaceAll("&", "&amp;");
        return replacement;
    }
}
