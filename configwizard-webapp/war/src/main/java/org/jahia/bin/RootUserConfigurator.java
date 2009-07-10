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
package org.jahia.bin;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 *
 * @author : rincevent
 * @since : JAHIA 6.1
 *        Created : 9 juil. 2009
 */
public class RootUserConfigurator {
    private transient static Logger logger = Logger.getLogger(RootUserConfigurator.class);

    public static void updateRootUserConfiguration(String fileName, Map values) {

        File jahiaConfigFile = new File(fileName);
        if (jahiaConfigFile.exists()) {
            // let's load the file's content in memory, assuming it won't be
            // too big.
            StringBuffer fileContentBuf = new StringBuffer();
            try {
                // FileReader fileReader = new FileReader(jahiaConfigFile);
                FileInputStream fileInputStream = new FileInputStream(jahiaConfigFile);
                InputStreamReader fileReader = new InputStreamReader(fileInputStream, "UTF-8");
                BufferedReader bufReader = new BufferedReader(fileReader);
                int ch = -1;
                while ((ch = bufReader.read()) != -1) {
                    fileContentBuf.append((char) ch);
                }
                bufReader.close();
                fileReader.close();
                fileInputStream.close();

                String fileContent = fileContentBuf.toString();

                String rootName = getValue(values, "root_user");
                if ((rootName != null) && (!"".equals(rootName))) {
                    fileContent = StringUtils.replace(fileContent, "ROOT_NAME_PLACEHOLDER", rootName);
                }

                String rootPwd = encryptPassword(getValue(values, "root_pwd"));
                fileContent = StringUtils.replace(fileContent, "@ROOT_PASSWORD@", rootPwd);
                StringBuffer userProperties = new StringBuffer();
                String firstName = getValue(values,"root_firstname");
                if(firstName!=null && !"".equals(firstName)) {
                    userProperties.append(" firstname=\"").append(firstName).append("\"");
                }
                String lastName = getValue(values,"root_lastname");
                if(lastName!=null && !"".equals(lastName)) {
                    userProperties.append(" lastname=\"").append(lastName).append("\"");
                }
                String email = getValue(values,"root_email");
                if(email!=null && !"".equals(email)) {
                    userProperties.append(" email=\"").append(email).append("\"");
                }
                fileContent = StringUtils.replace(fileContent, "root_user_properties=\"\"", userProperties.toString());
                // we have finished replacing values, let's save the modified
                // file.
                FileOutputStream fileOutputStream = new FileOutputStream(jahiaConfigFile);
                OutputStreamWriter fileWriter = new OutputStreamWriter(fileOutputStream, "UTF-8");
                fileWriter.write(fileContent);
                fileWriter.close();
                fileOutputStream.close();

            } catch (java.io.FileNotFoundException fnfe) {
                logger.error("Error modifying repository config file " +
                             jahiaConfigFile.toString(), fnfe);
            } catch (java.io.IOException ioe) {
                logger.error("Error modifying repository config file " +
                             jahiaConfigFile.toString(), ioe);
            }

        }
    }

    private static String getValue(Map values, String key) {
        String replacement = (String) values.get(key);
        if (replacement == null) {
            return "";
        }
        replacement = replacement.replaceAll("&", "&amp;");
        return replacement;
    }

    public static String encryptPassword(String password) {
        if (password == null) {
            return null;
        }

        if (password.length() == 0) {
            return null;
        }

        String result = null;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            if (md != null) {
                md.reset();
                md.update(password.getBytes());
                result = new String(Base64.encodeBase64(md.digest()));
            }
            md = null;
        } catch (NoSuchAlgorithmException ex) {

            result = null;
        }

        return result;
    }
}
