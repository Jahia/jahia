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
//
//
//  FileUtils
//  EV      19.12.2000
//  MAP     24.01.2002  Files are stored into UTF-8 format.
//

package org.jahia.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.Map;

import org.apache.commons.collections.FastHashMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jahia.exceptions.JahiaException;
import org.jahia.hibernate.manager.SpringContextSingleton;

public final class FileUtils {

    private static Logger logger = Logger.getLogger(FileUtils.class);

    private static FileUtils _fileUtils = null;
    
    private static Map<String, String> fileExtensionIcons;
    private static String[] fileExtensionIconsMapping;
    
    /***
     * getInstance
     * EV    19.12.2000
     * @deprecated use static methods of this class instead
     * @Deprecated use static methods of this class instead
     */
    public static FileUtils getInstance () {
        if (_fileUtils == null) {
            _fileUtils = new FileUtils();
        }
        return _fileUtils;
    }

    /***
     * EV    18.11.2000
     * MAP   24.01.2002 : What's about these limitations ?
     * FIXME     : each jahia site should have its own file directory
     * FIXME     : no more than 100 files per directory
     *
     */
    public static String composeBigTextFullPathName (String jahiaDiskPath, int jahiaID,
                                        int pageID, int fieldID, int versionID,
                                        int versionStatus, String languageCode) {
        return composeBigTextFullPathName(jahiaDiskPath,
                                          composeBigTextFileNamePart(jahiaID, pageID, fieldID, versionID, versionStatus, languageCode));
    }

    public static String composeBigTextFullPathName(String jahiaDiskPath, String bigTextFileNamePart) {
        return new StringBuilder(jahiaDiskPath.length()
                + bigTextFileNamePart.length() + 1).append(jahiaDiskPath)
                .append(File.separator).append(bigTextFileNamePart).toString();
    }

    public static String composeBigTextFileNamePart (int jahiaID, int pageID,
                                              int fieldID, int versionID,
                                              int versionStatus,
                                              String languageCode) {
        StringBuilder fileName = new StringBuilder();
        fileName.append(Integer.toString(jahiaID));
        fileName.append("-");
        fileName.append(Integer.toString(pageID));
        fileName.append("-");
        fileName.append(Integer.toString(fieldID));
        fileName.append("-");
        fileName.append(languageCode);
        if (versionStatus > 1) {
            fileName.append("-s");
        } else if ( (versionID != 0) && (versionStatus <= 0)) {
            fileName.append("-" + versionID);
        }
        fileName.append(".jahia");
        return fileName.toString();
    }

    /***
     * fileExists
     * EV    18.11.2000
     * called by loadContents
     *
     */
    public static boolean fileExists (String fileName)
        throws JahiaException {
        try {
            File theFile = new File(fileName);
            return theFile.exists();
        } catch (SecurityException se) {
            String errorMsg = "Security error in readFile : " + se.getMessage();
            logger.error(errorMsg, se);
            throw new JahiaException("Cannot access to jahia files",
                                     errorMsg, JahiaException.FILE_ERROR,
                                     JahiaException.CRITICAL_SEVERITY, se);
        }
    }

    /**
     * Read a text file in UTF-8 format.
         * Avalaible from Jahia Edition 3. The old Jahia ASCII file are also readable
     * but are saved in UTF-8 format.
     *
     * EV    18.11.2000
     * MAP   31.01.2002 Try the UTF-8 format before the the ASCII format
     *
     * @param String fileName : the absolute path file name.
     * @return the file content as String
     */
    public static String readFile(String fileName) throws JahiaException {

        String content = null;

        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(
                    fileName), "UTF-8"));
        } catch (IOException e) {
            in = null;
            logger.debug("Cannot read files in UTF-8 format trying ASCII", e);
        }
        try {
            if (null == in) {
                in = new BufferedReader(new InputStreamReader(
                        new FileInputStream(fileName)));
            }
            content = readerToString(in);
            
            IOUtils.closeQuietly(in);
        } catch (IOException e) {
            logger.error("Cannot read requested file '" + fileName + "'", e);
            throw new JahiaException("Cannot access to Jahia files",
                    "Error in readFile : " + fileName,
                    JahiaException.FILE_ERROR, JahiaException.ERROR_SEVERITY, e);
        }
        return content;
    }

    /**
     * Read a text file in GBK or other format.
     *
     * Liu Gang    4.5.2003
     *
     * @param String fileName : the absolute path file name.
     * @param String langCode.
     * @return the file content as String
     */
    public static String readFile (String fileName, String langCode)
        throws JahiaException {
        return readFile(fileName);
    }

    /**
     * Write a String content to a UTF-8 file format.
     *
     * EV    18.11.2000
     * MAP   24.01.2002 Write into UTF-8 file format
     *
     * @param String fileName : the absolute path file name.
     * @param String fileContent : the String file content
     */
    public static void writeFile (String fileName, String fileContent)
        throws JahiaException {
        try {
            BufferedWriter out = new BufferedWriter(
                new OutputStreamWriter(
                new FileOutputStream(fileName), "UTF-8"));
            out.write(fileContent);
            out.close();
        } catch (IOException ie) {
            String errorMsg = "Error in writeFile : " + fileName +
                              "\nIOException : " + ie.getMessage();
            logger.error( errorMsg, ie);
            throw new JahiaException("Cannot access to jahia files",
                                     errorMsg, JahiaException.FILE_ERROR,
                                     JahiaException.CRITICAL_SEVERITY, ie);
        }
    }

    /**
     * Write a String content to a GBK or other file format.
     *
     * Liu Gang    4.5.2003
     *
     * @param String fileName : the absolute path file name.
     * @param String langCode.
     * @param String fileContent : the String file content
     */
    public static void writeFile (String fileName, String fileContent, String langCode)
        throws JahiaException {
        writeFile(fileName, fileContent);
    }

    /***
     * deleteFile
     * EV    07.02.2001
     *
     */
    public static boolean deleteFile (String fileName) {
        File theFile = new File(fileName);
        return theFile.delete();
    }

    /**
     * renames a file
     * @param oldFileName name of the old file
     * @param newFileName destination name
     * @return true if it worked
     */
    public static boolean renameFile (String oldFileName, String newFileName) {
        File oldFile = new File(oldFileName);
        File newFile = new File(newFileName);
        return oldFile.renameTo(newFile);
    }

    // called by contentbigtextfield
    /**
     * copy a file
     * @param oldFileName name of the old file
     * @param newFileName destination name
     * @return true if it worked
     */
    public static boolean copyFile (String oldFileName, String newFileName) {
        File oldFile = new File(oldFileName);
        File newFile = new File(newFileName);
        if (!oldFile.exists()) {
            logger.error("Cannot copy file: source file doesn't exist");
            return false;
        }
        if (oldFile.getAbsolutePath().equals(newFile.getAbsolutePath())) {
            return true;
        }
        try {
            copyStream(new FileInputStream(oldFile),
                       new FileOutputStream(newFile));
        } catch (IOException ioe) {
            return false;
        }
        return true;
    }

    /***
     * copyStream
     * EV    30.11.2000
     * called by download
     *
     */
    public static void copyStream (InputStream ins, OutputStream outs)
        throws IOException {
        int writeBufferSize = 4096;
        byte[] writeBuffer = new byte[writeBufferSize];

        BufferedInputStream bis = new BufferedInputStream(ins, writeBufferSize);
        BufferedOutputStream bos = new BufferedOutputStream(outs,
            writeBufferSize);
        int bufread;
        while ( (bufread = bis.read(writeBuffer)) != -1) {
            bos.write(writeBuffer, 0, bufread);
        }
        bos.flush();
        bos.close();
        bis.close();
    }

    /**
     * Return the content of a Reader as String
     * @param reader
     * @return
     */
    public static String readerToString(Reader reader) throws IOException{
        if ( reader == null ){
            return null;
        }
        StringBuilder buff = new StringBuilder(512);
        char[] stringToRead = new char[4096];
        int count = -1;
        while ( (count = reader.read(stringToRead, 0, 4096)) != -1) {
            buff.append(stringToRead, 0, count);
        }
        return buff.toString();
    }

    /**
     * Try to read a file in UTF-8 then GBK and finally ASCII, null if none
     * of them success
     *
     * @param inputStream
     * @return
     */
    public static String getFileCharset(InputStream inputStream){

        try {
            new BufferedReader(
                new InputStreamReader(
                inputStream, "UTF-8"));
            // success
            return "UTF-8";
        } catch (IOException ie) {
        }
        try {
            new BufferedReader(
                new InputStreamReader(
                inputStream, "GBK"));
            // success
            return "GBK";
        } catch (IOException ie) {
        }
        try {
            new BufferedReader(
                new InputStreamReader(
                inputStream, "ASCII"));
            // success
            return "ASCII";
        } catch (IOException ie) {
        }
        return null;
    }
    
    private static Map<String, String> getFileExtensionIcons() {
        if (fileExtensionIcons == null) {
            synchronized (FileUtils.class) {
                if (fileExtensionIcons == null) {
                    SpringContextSingleton ctxHolder = SpringContextSingleton
                            .getInstance();
                    if (ctxHolder.isInitialized()) {
                        Map<String, String> icons = (Map) ctxHolder
                                .getContext().getBean("fileExtensionIcons");
                        FastHashMap mappings = new FastHashMap(icons);
                        mappings.setFast(true);
                        String[] jsMappings = new String[2];
                        jsMappings[0] = new StringBuilder(512).append("\"")
                                .append(
                                        StringUtils.join(mappings.keySet()
                                                .iterator(), "\", \"")).append(
                                        "\"").toString();
                        jsMappings[1] = new StringBuilder(512).append("\"")
                                .append(
                                        StringUtils.join(mappings.values()
                                                .iterator(), "\", \"")).append(
                                        "\"").toString();
                        fileExtensionIconsMapping = jsMappings;
                        fileExtensionIcons = mappings;
                    }
                }
            }
        }

        return fileExtensionIcons;
    }

    public static String getFileIcon(String fileName) {
        String ext = "unknown";
        if (StringUtils.isNotEmpty(fileName)) {
            int index = FilenameUtils.indexOfExtension(fileName);
            if (index != -1) {
                ext = fileName.substring(index + 1);
            } else {
                ext = fileName;
            }
            ext = ext.toLowerCase();
        }
        Map<String, String> mappings = getFileExtensionIcons();
        if (mappings == null) {
            return "file";
        }

        String icon = mappings.get(ext);

        return icon != null ? icon : mappings.get("unknown");
    }

    public static String[] getFileExtensionIconsMapping() {
        if (null == fileExtensionIconsMapping) {
            // initialize it
            getFileExtensionIcons();
        }
        return fileExtensionIconsMapping;
    }
    
    /***
     * constructor
     * EV    19.12.2000
     *
     */
    private FileUtils () {
        logger.debug("Starting fileUtils");
    }
}