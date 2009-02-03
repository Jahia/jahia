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

// $Id$
//
//  JahiaTools
//
//	26.02.2001  NK  added in Jahia.
//  27.03.2001  AK  changes in updatepropvalue().
//  25.07.2001  SB  added isValidURL()
//

package org.jahia.utils;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileCleaningTracker;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.DeferredFileOutputStream;
import org.apache.commons.jexl.Expression;
import org.apache.commons.jexl.ExpressionFactory;
import org.apache.commons.jexl.JexlContext;
import org.apache.commons.jexl.JexlHelper;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.log4j.Logger;
import org.jahia.bin.Jahia;
import org.jahia.params.ProcessingContext;
import org.jahia.params.valves.TokenAuthValveImpl;
import org.jahia.resourcebundle.ResourceBundleMarker;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.settings.SettingsBean;
import org.jahia.utils.keygenerator.JahiaKeyGen;

import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.URL;
import java.util.*;


/**
 * @author Jerome Tamiotti
 *         <p/>
 *         <p/>
 *         Class Tools:
 *         <p/>
 *         # Debugging tools
 *         # Date tools
 *         # String tools
 *         # Sql tools
 *         # Files tools
 *         # ComparisonImpl tools
 *         # General Purpose tools
 *         # Client Browser tools
 */
public class JahiaTools {

    // authorized chars
    private static final char[] AUTHORIZED_CHARS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_0123456789.-".toCharArray();

    // used to convert time expression like 1w 2d 3h 5m to millisecond.
    private static Map<String, Long> milliSecondsMultiplier;

    private static Logger logger = Logger.getLogger(JahiaTools.class);
    private static HttpClient httpClient;

    private static List<String> structureHTMLTags = new ArrayList<String>();
    private static FileCleaningTracker fileCleaningTracker = new FileCleaningTracker();

    /**************************************************************************
     * Debugging Tools
     *
     *
     *************************************************************************/

    static {
        milliSecondsMultiplier = new HashMap<String, Long>();
        milliSecondsMultiplier.put("w", new Long(604800000));
        milliSecondsMultiplier.put("d", new Long(86400000));
        milliSecondsMultiplier.put("h", new Long(3600000));
        milliSecondsMultiplier.put("m", new Long(60000));
        milliSecondsMultiplier.put("s", new Long(1000));

        structureHTMLTags.add("html");
        structureHTMLTags.add("body");
        structureHTMLTags.add("div");
        structureHTMLTags.add("span");
        structureHTMLTags.add("p");
        structureHTMLTags.add("table");
        structureHTMLTags.add("tbody");
        structureHTMLTags.add("tr");
        structureHTMLTags.add("td");
        structureHTMLTags.add("ul");
        structureHTMLTags.add("ol");
        structureHTMLTags.add("a");

    }


    //-------------------------------------------------------------------------
    /**
     * Method toConsole: print a debug message into server console
     *
     * @param localisation the current class name followed by the method name
     * @param msg          the msg to print
     */
    static public void toConsole(String localisation, String msg) {
        System.out.println(">> " + localisation + "(): " + msg);
    }

    //-------------------------------------------------------------------------
    /**
     * Method toConsole: print a debug message into server console
     *
     * @param msg the msg to print
     */
    static public void toConsole(String msg) {
        System.out.println(">> " + msg);
    }

    //-------------------------------------------------------------------------
    /**
     * Method getCurrentDateInMs: return the date of today as a long value
     *
     * @return the long value of today
     */
    static public long getCurrentDateInMs() {
        return System.currentTimeMillis();
    }


    //-------------------------------------------------------------------------
    /**
     * Method getDateInMs: return a long value for the time represented by the
     * given year, month and day
     *
     * @param year
     * @param month (1 to 12)
     * @param day
     * @return the long value of time
     */
    static public long getDateInMs(int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        // class calendar use monthes from 0 to 11, so decrement
        c.set(year, month - 1, day);
        Date d = c.getTime();
        return d.getTime();
    }


    //-------------------------------------------------------------------------
    /**
     * Method getDayFromMs: from a date in ms from 1970, January 1st, return the day
     *
     * @param time
     * @return the day value
     */
    static public int getDayFromMs(long time) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(time));
        return c.get(Calendar.DAY_OF_MONTH);
    }


    //-------------------------------------------------------------------------
    /**
     * Method getMonthFromMs: from a date in ms from 1970, January 1st, return the month
     *
     * @param time
     * @return the month value
     */
    static public int getMonthFromMs(long time) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(time));
        return c.get(Calendar.MONTH);
    }


    //-------------------------------------------------------------------------
    /**
     * Method getYearFromMs: from a date in ms from 1970, January 1st, return the year
     *
     * @param time
     * @return the year value
     */
    static public int getYearFromMs(long time) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date(time));
        return c.get(Calendar.YEAR);
    }


    //-------------------------------------------------------------------------
    /**
     * Format a epoch time (string) to human readable date.
     *
     * @author AK
     */
    public static String formatDateFromEpoch(String epochString) {

        // get a human-readable data format
        long longTime = Long.parseLong(epochString);
        java.util.Date normalDate = new java.util.Date(longTime);

        return java.text.DateFormat.getDateTimeInstance(3, 3).format(normalDate);
    }

    /**************************************************************************
     * String Tools
     *
     *
     *************************************************************************/

    //-------------------------------------------------------------------------
    /**
     * Method replacePattern :  replace a pattern in a text with another one
     *
     * @param str      the text to alter
     * @param oldToken the token to replace
     * @param newToken the new text
     * @return the altered text
     */
    static public String replacePattern(String str, String oldToken, String newToken) {
        if (str == null) {
            return str;
        }
        StringBuffer result = new StringBuffer(str.length() + 100);
        int i = str.indexOf(oldToken);
        int startOfIndex = 0;
        while (i != -1) {
            result.append(str.substring(startOfIndex, i));
            result.append(newToken);
            startOfIndex = i + oldToken.length();
            i = str.indexOf(oldToken, startOfIndex);
        }
        result.append(str.substring(startOfIndex, str.length()));
        return result.toString();
    }

    //-------------------------------------------------------------------------
    /**
     * Method replacePattern :  replace a pattern in a text with another one
     * ignore oldToken case.
     *
     * @param str      the text to alter
     * @param oldToken the token to replace
     * @param newToken the new text
     * @return the altered text
     */
    static public String replacePatternIgnoreCase(String str, String oldToken, String newToken) {
        if (str == null || oldToken == null || newToken == null) {
            return str;
        }
        String oldTokenLowerCase = oldToken.toLowerCase();
        StringBuffer result = new StringBuffer(str.length() + 100);
        String strLower = str.toLowerCase();

        int i = strLower.indexOf(oldTokenLowerCase);
        int startOfIndex = 0;
        while (i != -1) {
            result.append(str.substring(startOfIndex, i));
            result.append(newToken);
            startOfIndex = i + oldToken.length();
            i = strLower.indexOf(oldTokenLowerCase, startOfIndex);
        }
        result.append(str.substring(startOfIndex, str.length()));
        return result.toString();
    }

    // if invert == 1 -> inverse oldTocken with newTocken
    static public String replacePattern(String str, String newToken, String oldToken, int invert) {
        if (invert == 0) {
            return replacePattern(str, newToken, oldToken);
        } else {
            // inverse arguments
            return replacePattern(str, oldToken, newToken);
        }
    }

    //-------------------------------------------------------------------------
    /**
     * Method getTokens :  	return an arrays of String tokens
     *
     * @param str the string to parse
     * @param sep the separator
     * @return an array of string values
     * @author NK
     */
    static public String[] getTokens(String str, String sep) {
        if (str == null) {
            return new String[]{};
        }

        StringTokenizer st = new StringTokenizer(str, sep);
        String[] result = new String[st.countTokens()];
        int count = 0;
        while (st.hasMoreTokens()) {
            result[count] = st.nextToken();
            count++;
        }

        return result;
    }

    //-------------------------------------------------------------------------
    /**
     * Method getTokens :  	return a list of String tokens
     *
     * @param str the string to parse
     * @param sep the separator
     * @return a list of String tokens
     * @author NK
     */
    static public List<String> getTokensList(String str, String sep) {
        String[] tokens = getTokens(str, sep);
        if (str == null) {
            return null;
        }
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < tokens.length; i++) {
            result.add(tokens[i].trim());
        }

        return result;
    }

    //-------------------------------------------------------------------------
    /**
     * @param str the string to parse
     * @param sep the separator
     * @return a list of Integer from String tokens
     * @throws NumberFormatException
     */
    static public List<Integer> getIntegerList(String str, String sep)
            throws NumberFormatException {
        String[] tokens = getTokens(str, sep);
        if (str == null) {
            return null;
        }
        List<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < tokens.length; i++) {
            result.add(new Integer(Integer.parseInt(tokens[i].trim())));
        }
        return result;
    }

    //-------------------------------------------------------------------------
    /**
     * Convert a String starting with the word "$context" into a real filesystem
     * path. This method is principally used by JahiaPrivateSettings and to
     * convert jahia.properties settings.
     *
     * @param convert      The string to convert.
     * @param pathResolver The path resolver used to get the real path.
     * @author Alexandre Kraft
     */
    public static String convertContexted(String convert,
                                          PathResolver pathResolver) {
        if (convert.startsWith("$context/")) {
            convert = pathResolver.resolvePath(convert.substring(8, convert.length()));
        }
        return convert;
    } // end convertContexted

    /**
     * Convert a string starting with the word "$webContext" into a real
     * filesystem path.
     *
     * @param convert the string to convert
     * @return converted string
     */
    public static String convertWebContexted(String convert) {
        return convert.startsWith("$webContext/") ? Jahia.getContextPath()
                + convert.substring("$webContext".length(), convert.length())
                : convert;
    }

    //-------------------------------------------------------------------------
    /**
     * Convert a standard string to a property-compatible string.
     *
     * @param originalValue The string that you want to convert.
     * @return The string converted.
     * @author Alexandre Kraft
     */
    public static String string2Property(String originalValue) {
        StringBuffer convertedValue = new StringBuffer();
        for (int i = 0; i < originalValue.length(); i++) {
            if (originalValue.substring(i, i + 1).equals(":")) {
                convertedValue.append("\\:");
            } else if (originalValue.substring(i, i + 1).equals("\\")) {
                convertedValue.append("\\\\");
            } else {
                convertedValue.append(originalValue.substring(i, i + 1));
            }
        }
        return convertedValue.toString();
    } // end string2Property


    //-------------------------------------------------------------------------
    /**
     * Get each line from a string or a file and set it to an enumeration.
     *
     * @param decompose The string that you want to convert if <code>isFile</code>
     *                  is <code>false</code>. Otherwise, it's the path to
     *                  the file you want to read.
     * @return Iterator containing all lines of the string passed in parameter.
     * @author Alexandre Kraft
     * @boolean isFile      <code>true</code> is the source is a File or
     * <code>false</code> if the source is a String.
     */
    public static Iterator<String> string2Enumeration(String decompose, boolean isFile)
            throws IOException {
        List<String> stringLines = new ArrayList<String>();
        String buffer = "";
        BufferedReader buffered;

        if (isFile) {
            buffered = new BufferedReader(new FileReader(decompose));
        } else {
            buffered = new BufferedReader(new StringReader(decompose));
        }

        while ((buffer = buffered.readLine()) != null) {
            if (buffer.trim().length() > 0) {
                stringLines.add(buffer);
            }
        }
        buffered.close();

        return stringLines.iterator();
    } // end string2Enumeration


    //-------------------------------------------------------------------------
    /**
     * Check if the String passed in parameter is Alpha valid.
     *
     * @author FH
     */
    public static boolean isAlphaValid(String name) {
        if (name == null) {
            return false;
        }
        if (name.length() == 0) {
            return false;
        }

        char[] chars = AUTHORIZED_CHARS;
        char[] nameBuffer = name.toCharArray();

        boolean badCharFound = false;
        int i = 0;
        while ((i < nameBuffer.length) && (!badCharFound)) {
            int j = 0;
            boolean ok = false;
            while ((j < chars.length) && (!ok)) {
                if (chars[j] == nameBuffer[i]) {
                    ok = true;
                }
                j++;
            }
            badCharFound = (!ok);
            if (badCharFound) {
                JahiaConsole.println("JahiaTools.isAlphaValid", "--/ Bad character found in [" + name + "] at position " + Integer.toString(i));
            }
            i++;
        }
        return (!badCharFound);
    } // end isAlphaValid


    //-------------------------------------------------------------------------
    /**
     * Write a string in a file.
     *
     * @param fileName File name.
     * @param output   String output.
     * @author AK
     */
    public static void writeStringInFile(String fileName,
                                         String output) {
        // try to write the file...
        try {
            File fileObject = new File(fileName);
            FileWriter fileWriter = new FileWriter(fileObject);

            fileWriter.write(output);
            fileWriter.close();
        } catch (IOException ioe) {
        }
    } // end writeStringInFile

    /**************************************************************************
     * Files Tools
     *
     *
     **************************************************************************/


    //-------------------------------------------------------------------------
    /**
     * Copy files from String origin to String destination.
     *
     * @author AK
     */
    public static void copyFolderContent(String origin,
                                         String destination)
            throws IOException {
        File originFolder = new File(origin);
        File destinationFolder = new File(destination);

        // create the destination folder if necessary...
        if (!destinationFolder.exists()) {
            destinationFolder.mkdirs();
        }

        // copy recursive...
        if (originFolder.isDirectory()) {
            File[] filesInThisDirectory = originFolder.listFiles();
            StringBuffer destinationFile = null;
            for (int i = 0; i < filesInThisDirectory.length; i++) {
                String originFile = filesInThisDirectory[i].getPath();
                String originFileName = filesInThisDirectory[i].getName();
                destinationFile = new StringBuffer(destination);
                destinationFile.append(File.separator);
                destinationFile.append(originFileName);
                if (filesInThisDirectory[i].isFile()) {
                    FileInputStream fileInput = new FileInputStream(originFile);
                    FileOutputStream fileOutput = new FileOutputStream(destinationFile.toString());
                    copyStream(fileInput, fileOutput);
                } else {
                    copyFolderContent(originFile, destinationFile.toString());
                }
            }
        }
    } // end copyFiles


    //-------------------------------------------------------------------------
    /**
     * Copy an InputStream to an OutputStream
     *
     * @author AK
     */
    public static void copyStream(InputStream inputStream,
                                  OutputStream outputStream)
            throws IOException {
        int bufferRead;
        int bufferSize = 65536;
        byte[] writeBuffer = new byte[bufferSize];

        BufferedInputStream bufInStream = new BufferedInputStream(inputStream, bufferSize);
        BufferedOutputStream bufOutStream = new BufferedOutputStream(outputStream, bufferSize);
        while ((bufferRead = bufInStream.read(writeBuffer)) != -1)

            bufOutStream.write(writeBuffer, 0, bufferRead);
        bufOutStream.flush();
        bufOutStream.close();

        inputStream.close();

        outputStream.flush();
        outputStream.close();
    } // end copyStream


    //-------------------------------------------------------------------------
    public static boolean checkFileExists(String fileName) {
        try {
            File fileObject = new File(fileName);
            return fileObject.exists();
        } catch (NullPointerException npe) {
            return false;
        }
    } // end checkFileExists


    //-------------------------------------------------------------------------
    /**
     * check if a file or directory exists on disk. The check is case sensitive
     *
     * @param path the absolute path
     * @return boolean true if comparison is success
     * @author NK
     */
    static public boolean checkFileNameCaseSensitive(String path) {

        File tmpFile = new File(path);
        if (tmpFile != null && (tmpFile.isFile() || tmpFile.isDirectory())) {
            String name = tmpFile.getName();
            if (tmpFile.getParentFile() != null) {
                File[] files = tmpFile.getParentFile().listFiles();
                int nbFiles = files.length;
                for (int i = 0; i < nbFiles; i++) {
                    if (files[i].getName().equals(name)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }


    //-------------------------------------------------------------------------
    /**
     * delete a file or directory (and all its content)
     *
     * @param f the abstract file object
     * @author NK
     */
    public static boolean deleteFile(File f) {
        return deleteFile(f, false);

    }


    //-------------------------------------------------------------------------
    /**
     * delete a file or directory ( and all its contains )
     *
     * @param f           the abstract file object
     * @param contentOnly contentOnly id true, delete only the folder content
     * @author NK
     */
    public static boolean deleteFile(File f, boolean contentOnly) {

        if (f == null) {
            return false;
        }

        if (f.isDirectory()) {

            File[] files = f.listFiles();

            for (int i = 0; i < files.length; i++) {
                if (files[i].isFile()) {
                    files[i].delete();
                } else {
                    deleteFile(files[i], false);
                }
            }
            if (!contentOnly) {
                return f.delete();
            }
        }
        return true;

    }


    //-------------------------------------------------------------------------
    /**
     * Return the file name but without the extension
     *
     * @param filename , the complete file name with extension
     * @param ext      , the extension to remove
     * @author NK
     * @return(String) the filename without a gived extension
     */
    public static String removeFileExtension(String filename, String ext) {

        String name = filename.toLowerCase();
        if (name.endsWith(ext.toLowerCase())) {
            return (filename.substring(0, name.lastIndexOf(ext.toLowerCase())));
        }
        return filename;
    }


    //-------------------------------------------------------------------------
    /**
     * Method getUniqueDirName
     * Return a random and unique String that can be used
     * as directory name
     *
     * @return a unique directory name
     */
    public static String getUniqueDirName() {

        return (JahiaKeyGen.getKey(10));
    }

    /**************************************************************************
     * Sql Tools
     *
     *
     *************************************************************************/

    //-------------------------------------------------------------------------
    /**
     * Method Quote
     * Double the quotes in order to be SQL compliant
     *
     * @param input String to filter
     * @return a filtered string
     */
    static public String quote(String input) {

        if (input != null) {
            StringBuffer sb = new StringBuffer(input);
            for (int i = 0; i < sb.length(); i++) {
                char c = sb.charAt(i);
                if (c == '\'') {
                    sb.insert(i++, '\'');
                }
            }
            return sb.toString();
        }
        return input;
    }

    //-------------------------------------------------------------------------
    /**
     * Method escape double quote " with \"
     *
     * @param input String
     * @return a filtered string
     */
    static public String escapeString(String input) {
        if (input != null) {
            StringBuffer sb = new StringBuffer(input);
            for (int i = 0; i < sb.length(); i++) {
                char c = sb.charAt(i);
                if (c == '"') {
                    sb.insert(i++, '\"');
                }
            }
            return sb.toString();
        }
        return input;
    }

    /**************************************************************************
     * General purposes Tools
     *
     *
     **************************************************************************/


    //-------------------------------------------------------------------------
    /**
     * Check if a string value is in an array of string
     *
     * @param aValue a string value
     * @param values an array of String
     * @return true if value found in array
     * @author NK
     */
    static public boolean inValues(String aValue, String[] values) {
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                if (aValue.equals(values[i])) {
                    return true;
                }
            }
        }
        return false;
    }


    //-------------------------------------------------------------------------
    /**
     * Return a substitute String value if the source is null otherwise
     * return the source value
     *
     * @param data     the data
     * @param newValue the subsitute value
     * @return String
     * @author NK
     */
    static public String replaceNullString(String data, String newValue) {
        if (data != null) {
            return data;
        }
        return newValue;
    }


    //-------------------------------------------------------------------------
    /**
     * Return a substitute Integer object if the source is null otherwise
     * return the source integer object
     *
     * @param data
     * @param newValue
     * @return String
     * @author NK
     */
    static public Integer replaceNullInteger(Integer data, Integer newValue) {
        if (data != null) {
            return data;
        }
        return newValue;
    }


    //-------------------------------------------------------------------------
    /**
     * Guarantee a String not to be null, giving it an empty value if needed.
     *
     * @param inputString A <code>String</code> value, that may be null
     * @return a <code>String</code> value, guaranteed not to be null.
     * @author Mikha�l Janson
     */
    public static String nnString(String inputString) {
        String outputString = (inputString != null) ? inputString : "";
        return outputString;
    } // end nnString


    //-------------------------------------------------------------------------
    /**
     * Simple <code>int</code> to <code>boolean</code> converter
     *
     * @param value an integer value
     * @return <code>false</code> if <code>0</code>, <code>1</code> if not <code>0</code>
     */
    static public boolean int2boolean(int value) {
        return value != 0;
    }


    //-------------------------------------------------------------------------
    /**
     * Simple <code>boolean</code> to <code>int</code> converter
     *
     * @param value a <code>boolean</code> value
     * @return <code>0</code> if <code>false</code>, <code>1</code> if <code>true</code>
     */
    static public int boolean2int(boolean value) {
        return value ? 1 : 0;
    }


    //-------------------------------------------------------------------------
    /**
     * Method inverseList : inverse the elements contained in a List
     *
     * @param myList the List to inverse
     * @return the inversed List
     */
    static public List<?> inverseList(List<?> myList) {
        Collections.reverse(myList);
        return myList;
    }


    //-------------------------------------------------------------------------
    /**
     * Update a property's value in a properties file.
     *
     * @param propertyName the property name
     * @param propvalue    the property value
     * @param path         the full filesystem path to the properties file
     * @author Khue N'Guyen
     * @author Alexandre Kraft
     */
    public static void updatepropvalue(String propertyName,
                                       String propvalue,
                                       String path) {
        List<String> bufferList = new ArrayList<String>();
        String lineReaded = null;
        int position = 0;
        boolean lineFound = false;

        try {
            // parse the file...
            BufferedReader buffered = new BufferedReader(new FileReader(path));
            while ((lineReaded = buffered.readLine()) != null) {
                if (lineReaded.indexOf(propertyName) >= 0) {
                    position = lineReaded.lastIndexOf("=");
                    if (position >= 0) {
                        bufferList.add(lineReaded.substring(0, position + 1) + "   " + propvalue);
                        lineFound = true;
                    }
                } else {
                    bufferList.add(lineReaded);
                }
            }
            buffered.close();

            // add property if it don't exists before...
            if (!lineFound) {
                bufferList.add(propertyName + " =   " + propvalue);
            }

            // rewrite the file...
            File thisFile = new File(path);
            FileWriter fileWriter = new FileWriter(thisFile);
            StringBuffer outputBuffer = new StringBuffer();

            for (int i = 0; i < bufferList.size(); i++) {
                outputBuffer.append(bufferList.get(i));
            }

            fileWriter.write(outputBuffer.toString());
            fileWriter.close();
        } catch (java.io.IOException ioe) {
        }
    } // end updatepropvalue


    /**
     * ***********************************************************************
     * Client Browser Check Tools
     * <p/>
     * ************************************************************************
     */

    //-------------------------------------------------------------------------
    public static boolean isMSIExplorer(HttpServletRequest req) {
        return (req.getHeader("user-agent") != null && req.getHeader("user-agent").indexOf("MSIE") != -1);
    }

    //-------------------------------------------------------------------------
    public static boolean isLynx(HttpServletRequest req) {
        return (req.getHeader("user-agent") != null && req.getHeader("user-agent").indexOf("Lynx") != -1);
    }

    /**************************************************************************
     * Servlet Request Parameters handling
     *
     **************************************************************************/

    //-------------------------------------------------------------------------
    /**
     * return a parameter of String type if not null or return the subsitute value
     *
     * @param processingContext the request object
     * @param paramName         the param name
     * @param nullVal           the subsitute value to return if the parameter is null
     * @return String the parameter value
     * @author NK
     */
    public static String getStrParameter(ProcessingContext processingContext, String paramName, String nullVal) {

        String val = (String) processingContext.getParameter(paramName);
        if (val == null) {
            return nullVal;
        }
        return val;
    }

    /**
     * return a parameter of String type if not null or return the subsitute value
     *
     * @param request   the request object
     * @param paramName the param name
     * @param nullVal   the subsitute value to return if the parameter is null
     * @return String the parameter value
     * @author NK
     */
    public static String getStrParameter(ServletRequest request, String paramName, String nullVal) {
        String val = request.getParameter(paramName);
        if (val == null) {
            if (nullVal == null) return "";
            return nullVal;
        }
        return val;
    }


    //-------------------------------------------------------------------------
    /**
     * return a parameter of Integer type if not null or return the subsitute value
     *
     * @param processingContext the request object
     * @param paramName         the param name
     * @param nullVal           the subsitute value to return if the parameter is null
     * @author NK
     */
    public static Integer getIntParameter(ProcessingContext processingContext, String paramName, Integer nullVal) {

        String strVal = (String) processingContext.getParameter(paramName);
        Integer val = null;
        if (strVal == null) {
            return nullVal;
        } else {
            try {
                val = new Integer(strVal);
            } catch (Exception t) {
                val = nullVal;
            }
        }
        return val;
    }


    //-------------------------------------------------------------------------
    /**
     * Remove context attribute they names start with the string passed in
     * parameter. In details, this method select which attribute he wants to
     * remove and compose a temporary List with it. Next step is to delete
     * all attributes contains in this List, via an enumeration. The reason
     * of this is that you can't remove a context attribute while you use the
     * getAttributeNames method to get the list of context attribute names.
     *
     * @param context     The context where do you want to remove attributes
     * @param startString The string for select the attributes by they names.
     * @author Alexandre Kraft
     */
    public static void removeContextAttributes(ServletContext context,
                                               String startString) {
        Enumeration<?> contextAttributeNames = context.getAttributeNames();
        List<String> attributesToRemove = new ArrayList<String>();
        String attributeName;

        while (contextAttributeNames.hasMoreElements()) {
            attributeName = (String) contextAttributeNames.nextElement();
            if (attributeName.length() >= 36) {
                if ((attributeName.substring(0, 36).equals(startString)) && (attributeName.indexOf("accessGranted") == -1)) {
                    attributesToRemove.add(attributeName);
                }
            }
        }

        Iterator<String> attributesListToRemove = attributesToRemove.iterator();
        while (attributesListToRemove.hasNext()) {
            attributeName = attributesListToRemove.next();
            context.removeAttribute(attributeName);
        }
    } // end removeContextAttributes

    /**************************************************************************
     * HTML Tools
     *
     *
     *************************************************************************/

    //-------------------------------------------------------------------------
    /**
     * Return text with HTML entities (text2html)
     *
     * @param str Input String
     * @return str    HtmlEntities output
     * @author POL
     * @version 1.0   POL 14/06/2001
     * @version 1.1   MAP 23.10.2001
     */
    /*public static String text2html(String str) {
        return TextHtml.text2html(str); // Changed by MAP
    }*/

    //-------------------------------------------------------------------------
    /**
     * Return text without HTML entities (html2text)
     *
     * @param str Input String
     * @return str    Text output
     * @author POL
     * @version 1.0   POL 18/06/2001
     * @version 1.1   MAP 23.10.2001
     */
    public static String html2text(String str) {
        //str = replacePattern(str,"X-AND-X","&amp;");
        str = replacePattern(str, "&amp;", "&");
        return TextHtml.html2text(str); // Changed by MAP
    }

    //-------------------------------------------------------------------------
    /**
     * text2XMLEntityRef
     * Parse a text and replace XML specific characters to their XML Entity Reference value.
     *
     * @param str    Input String
     * @param invert int (0/1) 0 : text to XML Entity Reference / 1 : XML Entity Reference to text
     * @return str    Text output
     * @author Khue Nguyen
     */
    public static String text2XMLEntityRef(String str, int invert) {
        if (str == null || str.trim().equals("")) {
            return "";
        }
        if (invert == 0) {  // Change by MAP
            str = TextHtml.text2html(str);
        } else {
            str = TextHtml.html2text(str);
        }
        str = replacePattern(str, "&", "&amp;", invert);
        str = replacePattern(str, "<", "&lt;", invert);
        str = replacePattern(str, ">", "&gt;", invert);
        str = replacePattern(str, "\"", "&quot;", invert);
        str = replacePattern(str, "'", "&apos;", invert);
        return str;
    }

    public static String text2XML(String str) {
        if (str == null || str.trim().equals("")) {
            return "";
        }
        str = str.replaceAll( "&", "&amp;");
        str = str.replaceAll("<", "&lt;");
        str = str.replaceAll(">", "&gt;");
        str = str.replaceAll("\"", "&quot;");
        str = str.replaceAll("'", "&apos;");
        return str;
    }


    //-------------------------------------------------------------------------
    /**
     * Tests whether an URL is valid or not.
     *
     * @param URLString the String representation of the URL to test
     * @return <code>true</code> if the URL could be accessed; <code>false</code>
     *         otherwise
     */
    public static boolean isValidURL(String URLString) {
        try {
            URL testURL = new URL(URLString);
            testURL.openConnection();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Converts whitespace characters no longer supported by
     * JRE 1.3.1_01a or later to character codes that Editize
     * will understand.
     */
    public static String javaSpecialChars(String text) {
        if (text == null) {
            return text;
        }
        text = replacePattern(text, "\n", "\\n");
        text = replacePattern(text, "\r", "\\r");
        text = replacePattern(text, "\t", "\\t");
        text = replacePattern(text, "\\", "\\\\");
        return text;
    }

    public static String htmlSpecialChars(String text) {
        if (text == null) {
            return text;
        }
        text = replacePattern(text, "&", "&amp;");
        text = replacePattern(text, "<", "&lt;");
        text = replacePattern(text, ">", "&gt;");
        text = replacePattern(text, "\"", "&quot;");
        /*
        text = replacePattern(text,"&","&#60;");
        text = replacePattern(text,"<","&#34;");
        text = replacePattern(text,">","&#38;");
        text = replacePattern(text,"\"","&#62;");
        */

        return text;

    }

    /**
     * This is a powerful expression evaluator that allows to evaluate a String
     * that has the following format :
     * "text${jexlExpr1}text${jexlExpr2}"
     * So the string passed may contain multiple JEXL expressions, which can
     * themselves be quite complex.
     *
     * @param expr        String a String containing one or multiple JEXL expressions
     * @param contextVars Map a map containing String keyed variables to be
     *                    used as the environment for the JEXL expressions.
     * @return String the resulting string from all the expression evaluations
     * @throws Exception if an error happened during parsing or expression
     *                   evaluation.
     */
    public static String evaluateExpressions(String expr, Map<String, Object> contextVars)
            throws Exception {

        final String START_EXPR_MARKER = "${";
        final String END_EXPR_MARKER = "}";

        StringBuffer result = new StringBuffer();

        int startExprMarkerPos = expr.indexOf(START_EXPR_MARKER);
        int endExprMarkerPos = -1;
        int curParsingPos = 0;
        while (startExprMarkerPos != -1) {

            result.append(expr.substring(curParsingPos, startExprMarkerPos));

            curParsingPos = startExprMarkerPos + START_EXPR_MARKER.length();

            endExprMarkerPos = expr.indexOf(END_EXPR_MARKER, curParsingPos);
            if (endExprMarkerPos == -1) {
                throw new Exception(
                        "Parsing exception, missing end-of-expression marker " +
                                END_EXPR_MARKER + " for expression at column " +
                                curParsingPos);
            }
            String curExpr = expr.substring(curParsingPos, endExprMarkerPos);

            Expression e = ExpressionFactory.createExpression(curExpr);

            JexlContext jc = JexlHelper.createContext();
            jc.getVars().putAll(contextVars);

            Object o = e.evaluate(jc);
            String s = null;
            if (o instanceof String) {
                s = (String) o;
            } else {
                s = o.toString();
            }

            result.append(s);
            curParsingPos = endExprMarkerPos + END_EXPR_MARKER.length();

            startExprMarkerPos = expr.indexOf(START_EXPR_MARKER, curParsingPos);
        }
        result.append(expr.substring(curParsingPos));

        return result.toString();
    }

    /**
     * Returns an array of String value as a String of token separated by the separator.
     *
     * @param strs
     * @param separator
     * @return
     */
    public static String getStringArrayToString(String[] strs,
                                                String separator) {
        if (strs == null) {
            return null;
        }
        if (strs.length == 0) {
            return "";
        }
        StringBuffer buff = new StringBuffer();
        for (int i = 0; i < strs.length; i++) {
            buff.append(strs[i]);
            if (i < strs.length - 1) {
                buff.append(separator);
            }
        }
        return buff.toString();
    }

    public static InputStream makeJahiaRequest(URL url, JahiaUser user, String username, String password, int redirectContinue) throws IOException {
        GetMethod method = null;
        if (httpClient == null) {
            MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
            connectionManager.getParams().setMaxTotalConnections(40);
            connectionManager.getParams().setDefaultMaxConnectionsPerHost(10);
            httpClient = new HttpClient(connectionManager);
            httpClient.getParams().setConnectionManagerTimeout(SettingsBean.getInstance().getConnectionTimeoutForProductionJob());
            //Set to COMPATIBILITY for it to work in as many cases as possible
            httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        }
        for (; redirectContinue > 0; redirectContinue--) {
            // Create an instance of HttpClient.

            // Create a method instance.
            method = new GetMethod(url.toString());
            method.setFollowRedirects(false);
            if (username != null && password != null) {
                method.addRequestHeader("Authorization", "BASIC " + org.apache.axis.encoding.Base64.encode((username + ":" + password).getBytes("ISO-8859-1")));
            } else {
                method.addRequestHeader(TokenAuthValveImpl.addToken(user));
            }
            // Execute the method.
            int result = httpClient.executeMethod(method);
            logger.debug("Response status code for [" + url + "] is : " + result);

            //this is just in case we get a different host redirection or an error:
            if (result == HttpStatus.SC_MOVED_TEMPORARILY) {
                String redirectLocation;
                Header locationHeader = method.getResponseHeader("location");
                if (locationHeader != null) {
                    redirectLocation = locationHeader.getValue();
                    url = new URL(redirectLocation);
                    method.releaseConnection();
                    logger.info("Redirection to a different host [" + url + "]");
                } else {
                    method.releaseConnection();
                    throw new IOException("No HTTP location Header in redirect send by request to [" + url + "]. HTTP status code [" + result + "]");
                }
            } else if (result != HttpStatus.SC_OK) {
                method.releaseConnection();
                throw new HttpException("Unsupported HTTP status code ["+result+" i.e. "+method.getStatusText()+"] for request to ["+url +"].");
            } else {
                DeferredFileOutputStream dfos =
                        new DeferredFileOutputStream(1024 * 1024 * 10, File.createTempFile("temp", "httpclient"));
                InputStream inputStream = new BufferedInputStream(new FileInputStream(dfos.getFile()));
                fileCleaningTracker.track(dfos.getFile(), inputStream);
                IOUtils.copy(method.getResponseBodyAsStream(), dfos);
                method.releaseConnection();
                if (dfos.isInMemory()) {
                    inputStream.close();
                    return new ByteArrayInputStream(dfos.getData());
                }
                return inputStream;
            }
        }

        throw new IOException("Too many redirects for request to [" + url + "].");
    }

    public static String getConditionalInClauseFromBitSet(BitSet ids,
                                                          String columnName, String valueOperator, String suffix) {
        StringBuffer buff = new StringBuffer(1000);
        if (ids.cardinality() <= org.jahia.settings.SettingsBean.getInstance()
                .getDBMaxElementsForInClause()) {
            if (valueOperator.indexOf("!=") != -1
                    || valueOperator.indexOf("<>") != -1) {
                buff.append(columnName).append(" NOT IN (");
            } else if (valueOperator.indexOf("=") != -1) {
                buff.append(columnName).append(" IN (");
            }

            for (int i = ids.nextSetBit(0); i >= 0; i = ids.nextSetBit(i + 1)) {
                buff.append(String.valueOf(i));
                if (ids.nextSetBit(i + 1) >= 0) {
                    buff.append(",");
                }
            }
            buff.append(") ").append(suffix);
        }
        return buff.toString();
    }


    /**
     * The format of this is ' *w *d *h *m *s ' (representing weeks, days, hours and minutes - where * can be any number)
     * Examples: 4d, 5h 30m, 60m and 3w.
     * Note: Your current conversion rates are 1w = 7d and 1d = 24h
     *
     * @param val
     * @return -1 on errors
     */
    public static Long getTimeAsLong(String val) {
        return getTimeAsLong(val, null);
    }

    /**
     * The format of this is ' *w *d *h *m *s ' (representing weeks, days, hours and minutes - where * can be any number)
     * Examples: 4d, 5h 30m, 60m and 3w.
     * Note: Your current conversion rates are 1w = 7d and 1d = 24h
     *
     * @param val
     * @param defaultValue
     * @return -1 on errors
     */
    public static Long getTimeAsLong(String val, String defaultValue) {
        Long result = null;
        try {
            if (NumberUtils.isNumber(val + "0")) {
                result = NumberUtils.createLong(val);
                return result;
            }
            String[] tokens = getTokens(val, " ");
            if (tokens == null || tokens.length == 0) {
                if (defaultValue == null) {
                    return null;
                }
                return getTimeAsLong(defaultValue);
            }
            long millis = 0;
            String token = null;
            Long multiplier = null;
            for (int i = 0; i < tokens.length; i++) {
                token = tokens[i].trim().toLowerCase();
                multiplier = null;
                if (token.endsWith("w")) {
                    multiplier = milliSecondsMultiplier.get("w");
                } else if (token.endsWith("d")) {
                    multiplier = milliSecondsMultiplier.get("d");
                } else if (token.endsWith("h")) {
                    multiplier = milliSecondsMultiplier.get("h");
                } else if (token.endsWith("m")) {
                    multiplier = milliSecondsMultiplier.get("m");
                } else if (token.endsWith("s")) {
                    multiplier = milliSecondsMultiplier.get("s");
                }
                if (multiplier != null) {
                    millis += multiplier.longValue() * Integer.parseInt(token.substring(0, token.length() - 1));
                }
            }
            result = new Long(millis);
        } catch (Exception t) {
            logger.debug("Error converting time to millis", t);
            if (defaultValue != null) {
                return getTimeAsLong(defaultValue);
            }
        }
        return result;
    }

    public static String getMultiValuesMultiple(String resBundleID, String values[]) {
        return getMultiValues(resBundleID, values, "jahia_multivalue_multiple");
    }

    public static String getMultiValuesSingle(String resBundleID, String values[]) {
        return getMultiValues(resBundleID, values, "jahia_multivalue_single");
    }

    public static String getMultiValuesMultiple(String resBundleID, Collection<String> values) {
        return getMultiValues(resBundleID, values.toArray(new String[]{}),
                "jahia_multivalue_multiple");
    }

    public static String getMultiValuesSingle(String resBundleID, Collection<String> values) {
        return getMultiValues(resBundleID, values.toArray(new String[]{}),
                "jahia_multivalue_single");
    }

    private static String getMultiValues(String resBundleID, String values[], String type) {
        final StringBuffer sb = new StringBuffer();
        sb.append("<");
        sb.append(type);
        sb.append("[");
        final List<String> al = new ArrayList<String>();
        for (int i = 0; i < values.length; i++) {
            al.add(values[i]);
        }
        Collections.sort(al);
        for (int i = 0; i < al.size(); i++) {
            if (i > 0) {
                sb.append(":");
            }
            if (resBundleID == null || resBundleID.length() == 0) {
                sb.append(al.get(i));
            } else {
                sb.append(ResourceBundleMarker.drawMarker(resBundleID, al.get(i), al.get(i)));
            }

        }
        sb.append("]>");
        if (al.size() >= 0) {
            if (resBundleID == null || resBundleID.length() == 0) {
                sb.append(al.get(0));
            } else {
                sb.append(ResourceBundleMarker.drawMarker(resBundleID, al.get(0), al.get(0)));
            }
        }
        return sb.toString();
    } // getMultivalues

    /**
     * Remove all html tags
     *
     * @param str Input string (html code).
     * @return Output string
     */
    public static String removeTags(final String str) {
        if (str == null) {
            return "";
        }
        final StringBuffer result = new StringBuffer(str.length());
        int startIndex = 0;
        int i = str.indexOf("<");
        while (i != -1) {
            result.append(str.substring(startIndex, i));
            i = str.indexOf(">", i);
            if (i != -1) {
                startIndex = i + 1;
            }
            i = str.indexOf("<", startIndex);
        }
        return result.append(str.substring(startIndex, str.length())).toString();
    }

     /**
     * This method ensures that the output String has only
     * valid XML unicode characters as specified by the
     * XML 1.0 standard. For reference, please see
     * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
     * standard</a>. This method will return an empty
     * String if the input is null or empty.
     *
     * @param in The String whose non-valid characters we want to remove.
     * @return The in String, stripped of non-valid characters.
     */
    public static String stripNonValidXMLCharacters(String in) {
        StringBuffer out = new StringBuffer(); // Used to hold the output.
        char current; // Used to reference the current character.

        if (in == null || ("".equals(in))) return ""; // vacancy test.
        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if ((current == 0x9) ||
                (current == 0xA) ||
                (current == 0xD) ||
                ((current >= 0x20) && (current <= 0xD7FF)) ||
                ((current >= 0xE000) && (current <= 0xFFFD)) ||
                ((current >= 0x10000) && (current <= 0x10FFFF)))
                out.append(current);
        }
        return out.toString();
    }

    /**
     * Returns true if html fragment is not closed correctly
     *
     * @param text
     * @return
     */
    public static boolean unClosedTag(String text){
        return  unClosedTag(text,true);
    }

    /**
     * Returns true if html fragment is not closed correctly
     *
     * @param text
     * @param firstIteration
     * @return
     */
    private static boolean unClosedTag(String text, boolean firstIteration){
        if (firstIteration){
            text = removeAllHTMLComments(text);
            text = removeAllEmptyBodyTags(text.toLowerCase());
        }
        int pos = text.indexOf("</");
        int pos2 = 0;
        if (pos != -1){
            pos2 = text.indexOf(">",pos);
            if (pos2==-1){
                return true;
            }
            String tagName = text.substring(pos+2,pos2);
            if (!structureHTMLTags.contains(tagName)){
                // ignore this tag
                return unClosedTag(text.substring(0,pos)+text.substring(pos2+1),false);
            }
            int pos3 = text.substring(0,pos).lastIndexOf("<");
            if (pos3 == -1){
                // mising open tag
                return true;
            }
            int pos4 = text.indexOf(">",pos3);
            if (pos4>pos || pos4 == -1){
                // wrong
                return true;
            }
            String openTag = text.substring(pos3+1,pos4);
            if (openTag.indexOf(" ") != -1){
                openTag = openTag.substring(0,openTag.indexOf(" "));
            }
            if (openTag.equals(tagName)){
                return unClosedTag(text.substring(0,pos3)+text.substring(pos2+1),false);
            } else if (!structureHTMLTags.contains(openTag)){
                return unClosedTag(text.substring(0,pos3)+text.substring(pos),false);
            }
        }
        pos = text.indexOf("<");
        if (pos != -1){
            pos2 = text.indexOf(">");
            if (pos2 == -1 || pos2<pos){
                return true;
            }
            String tagName = text.substring(pos+1,pos2);
            if (tagName.indexOf(" ") != -1){
                tagName = tagName.substring(0,tagName.indexOf(" "));
            }
            if (!structureHTMLTags.contains(tagName)){
                // ignore this tag
                return unClosedTag(text.substring(pos2+1),false);
            } else  {
                // find an unclosed tag
                return true;
            }
        }
        return false;
    }

    private static String removeAllEmptyBodyTags(String text){
        int pos = text.indexOf("/>");
        if (pos == -1){
            return text;
        }
        int pos2 = text.substring(0,pos).lastIndexOf("<");
        if (pos2==-1){
            return text;
        }
        return removeAllEmptyBodyTags(text.substring(0,pos2) + text.substring(pos+2));
    }

    private static String removeAllHTMLComments(String text){
        StringBuffer buffer = new StringBuffer();
        int startPos = 0;
        int pos = 0;
        int pos2 = 0;
        while (startPos<text.length()){
            pos = text.indexOf("<!--",startPos);
            if (pos != -1){
                pos2 = text.indexOf("-->",pos);
                if (pos2 != -1){
                    buffer.append(text.substring(startPos,pos));
                    text = text.substring(pos2+3);
                    startPos = 0;
                    continue;
                }
            }
            buffer.append(text.substring(startPos));
            break;
        }
        return buffer.toString();
    }

}
