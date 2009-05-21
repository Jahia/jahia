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
// $Id$
//
//  DatabaseScripts
//
//  30.03.2001  AK  added in jahia.
//  01.04.2001  AK  change the package.
//

package org.jahia.admin.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jahia.utils.PathResolver;



/**
 * desc:  This class is used by the installation and the administration
 * to get all informations required from database scripts, like msaccess.script
 * or hypersonic.script, from the jahia database script path (a jahiafiles
 * subfolder).
 *
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 *
 * @author Alexandre Kraft
 * @version 1.0
 */
public class DatabaseScripts {

    private static org.apache.log4j.Logger logger =
            org.apache.log4j.Logger.getLogger(DatabaseScripts.class);

    /**
     * Default constructor.
     * @author  Alexandre Kraft
     */
    public DatabaseScripts()
    {
        // do nothing :o)
    } // end constructor



    /**
     * Get an Iterator containing all the database scripts in File objects
     * from the jahia database scripts path.
     * @author  Alexandre Kraft
     *
     * @return  Iterator containing all scripts in File objects.
     */
    public Iterator<File> getDatabaseScriptsFileObjects(String jahiaDatabaseScriptsPath)
    throws IOException
    {
        File   scriptsFolderFileObject =  new File( jahiaDatabaseScriptsPath );
        File[] scriptsListFileArray    =  scriptsFolderFileObject.listFiles();
        List<File> scriptsListList       =  new ArrayList<File>();

        for(int i=0; i<scriptsListFileArray.length; i++) {
            if(!scriptsListFileArray[i].isDirectory()) {
                if (scriptsListFileArray[i].getName().endsWith(".script")) {
                    scriptsListList.add( scriptsListFileArray[i] );
                }
            }
        }

        return scriptsListList.iterator();
    } // getDatabaseScriptsFileObjects



    /**
     * Get a List containing all database scripts informations in four-entry
     * HashMap's. These Map contains the script name, script username,
     * script password and script driver for each database.
     * @author  Alexandre Kraft
     *
     * @param   fileObjects   Iterator containing all scripts File objects.
     * @return  List containing all scripts infos in HashMap.
     */
    public List<Map<String, String>> getDatabaseScriptsInfos( Iterator<File> fileObjects, PathResolver pathResolver )
    throws IOException
    {
        List<Map<String, String>> scriptsInfosList  = new ArrayList<Map<String, String>>();

        while(fileObjects.hasNext())
        {
            File            scriptFileObject  = fileObjects.next();
            FileInputStream scriptInputStream = new FileInputStream(scriptFileObject.getPath());
            Properties      scriptProperties  = new Properties();

            scriptProperties.load( scriptInputStream );

            Enumeration<?> scriptPropertiesEnum  = scriptProperties.propertyNames();
            Map<String, String>     scriptPropertiesHash  = new HashMap<String, String>();

            while(scriptPropertiesEnum.hasMoreElements())
            {
                String scriptPropertyName  = (String) scriptPropertiesEnum.nextElement();
                try {
                    if(scriptPropertyName.startsWith("jahia.")) {
                        String scriptPropertyValue = scriptProperties.getProperty(scriptPropertyName);
                        if ("jahia.database.url".equals(scriptPropertyName)) {
                            if (pathResolver != null) {
                                // let's test if the URL contains a context clause
                                int contextMarker = scriptPropertyValue.indexOf(
                                    "$context/");
                                if (contextMarker != -1) {
                                    // note : here below we DO leave the first slash in, this is not a bug.
                                    String rightOfContext = scriptPropertyValue.
                                        substring(contextMarker +
                                                  "$context".length());
                                    String leftOfContext = scriptPropertyValue.
                                        substring(0, contextMarker);
                                    scriptPropertyValue = leftOfContext +
                                        pathResolver.resolvePath(rightOfContext);
                                }
                            }
                        }
                        scriptPropertiesHash.put(scriptPropertyName, scriptPropertyValue);
                    }
                } catch (IndexOutOfBoundsException ioobe) {
                    // this property-name length is smaller than 6 characters. do nothing.
                }
            }
            scriptPropertiesHash.put("jahia.database.script", scriptFileObject.getName());

            // now let's see if we can load the database driver class. If
            // we can we add the database settings to the list, otherwise we
            // don't.
            String driverName = scriptPropertiesHash.get("jahia.database.driver");
            if (driverName != null) {
                boolean loadedSuccessfully = false;
                try {
                    Class.forName(driverName);
                    loadedSuccessfully = true;
                } catch (ClassNotFoundException cfne) {
                    logger.debug("Database driver class " + driverName + " not found. Ignoring database script entry.");
                }

                if (loadedSuccessfully) {
                    logger.debug("Successfully loaded database setting from " + scriptFileObject);
                    scriptsInfosList.add(scriptPropertiesHash);
                }
            } else {
                logger.debug("No driver found for database script " + scriptFileObject + ", ignoring database entry.");
            }
        }

        return scriptsInfosList;
    }


    /**
     * Retrieves SQL statement for schema creation, by way of the database
     * dependent configuration file.
     * @param fileObject File the database configuration file
     * @throws IOException thrown if there was an error opening or parsing
     * the files
     * @return Iterator an Iterator of String objects containing the
     * schema creation SQL statements.
     */
    public List<String> getSchemaSQL( File fileObject )
        throws IOException {
        FileInputStream scriptInputStream = new FileInputStream(fileObject.getPath());
        Properties      scriptProperties  = new Properties();
        scriptProperties.load( scriptInputStream );


        String scriptLocation = scriptProperties.getProperty("jahia.database.schemascriptdir");
        File parentFile = fileObject.getParentFile();
        File schemaDir = new File(parentFile, scriptLocation);

        List<String> result = getSQLStatementsInDir(schemaDir, ".sql");

        return result;
    }

    /**
     * Retrieves SQL statement for schema creation, by way of the database
     * dependent configuration file.
     * @param fileObject File the database configuration file
     * @throws IOException thrown if there was an error opening or parsing
     * the files
     * @return Iterator an Iterator of String objects containing the
     * schema creation SQL statements.
     */
    public List<String> getPopulationSQL( File fileObject )
        throws IOException {
        FileInputStream scriptInputStream = new FileInputStream(fileObject.getPath());
        Properties      scriptProperties  = new Properties();
        scriptProperties.load( scriptInputStream );

        String scriptLocation = scriptProperties.getProperty("jahia.database.popuplationscriptdir");
        File parentFile = fileObject.getParentFile();
        File schemaDir = new File(parentFile, scriptLocation);

        List<String> result = getSQLStatementsInDir(schemaDir, ".sql");

        return result;
    }

    /**
     * Retrieves all the statement in a directory for files with a
     * specific extension (usually ".sql")
     * @param sqlDir File the directory in which to search for SQL files.
     * @param extension String extension for files, in lowercase. May be null
     * in which case all the files will be used.
     * @throws IOException
     * @return ArrayList
     */
    public List<String> getSQLStatementsInDir (File sqlDir, final String extension)
        throws IOException {
        List<String> result = new ArrayList<String>();
        File[] schemaFiles = sqlDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir,
                                  String name) {
                if (extension != null) {
                    return name.toLowerCase().endsWith(extension);
                } else {
                    return true;
                }
            }
        });
        if (schemaFiles == null) {
            return result;
        }
        List<File> indexFiles = new ArrayList<File>();
        for (int i=0; i < schemaFiles.length; i++) {
            File sqlFile = schemaFiles[i];
            if(sqlFile.getName().endsWith("index.sql")) {
                indexFiles.add(sqlFile);
            } else {
                List<String> curFileSQL = getScriptFileStatements(sqlFile);
                result.addAll(curFileSQL);
            }
        }
        for (int i = 0; i < indexFiles.size(); i++) {
            File indexFile = indexFiles.get(i);
            List<String> curFileSQL = getScriptFileStatements(indexFile);
            result.addAll(curFileSQL);
        }
        return result;
    }

    /**
     * Get a Iterator containing all lines of the sql runtime from a
     * database script. This database script is getted in parameter like
     * a File object. The method use the BufferedReader object on a
     * FileReader object instanciate on the script file name.
     * @author  Alexandre Kraft
     *
     * @param   fileObject   File object of the database script file.
     * @return  Iterator containing all lines of the database script.
     */
    public List<String> getScriptFileStatements( File fileObject )
    throws IOException
    {
        List<String> scriptsRuntimeList  = new ArrayList<String>();

        BufferedReader  buffered     = new BufferedReader( new FileReader(fileObject.getPath()) );
        String          buffer       = "";

        StringBuffer curSQLStatement = new StringBuffer();
        while((buffer = buffered.readLine()) != null)
        {

            // let's check for comments.
            int commentPos = buffer.indexOf("#");
            if ((commentPos != -1) && (!isInQuotes(buffer, commentPos))) {
                buffer = buffer.substring(0, commentPos);
            }
            commentPos = buffer.indexOf("//");
            if ((commentPos != -1) && (!isInQuotes(buffer, commentPos))) {
                buffer = buffer.substring(0, commentPos);
            }
            commentPos = buffer.indexOf("/*");
            if ((commentPos != -1) && (!isInQuotes(buffer, commentPos))) {
                buffer = buffer.substring(0, commentPos);
            }
            commentPos = buffer.indexOf("REM ");
            if ((commentPos != -1) && (!isInQuotes(buffer, commentPos))) {
                buffer = buffer.substring(0, commentPos);
            }
            commentPos = buffer.indexOf("--");
            if ((commentPos != -1) && (!isInQuotes(buffer, commentPos))) {
                buffer = buffer.substring(0, commentPos);
            }

            // is the line after comment removal ?
            if (buffer.trim().length() == 0) {
                continue;
            }

            int curPos = 0;
            int separatorPos = findNextSeparator(buffer, ";", curPos);
            while (separatorPos != -1) {
                // found seperator char in the script file, finish constructing
                curSQLStatement.append(buffer.substring(curPos, separatorPos));
                String sqlStatement = curSQLStatement.toString().trim();
                if (!"".equals(sqlStatement)) {
                    logger.debug("Found statement [" + sqlStatement +
                                 "]");
                    scriptsRuntimeList.add(sqlStatement);
                }
                curSQLStatement = new StringBuffer();
                curPos = separatorPos + 1;
                separatorPos = findNextSeparator(buffer, ";", curPos);
            }
            curSQLStatement.append(buffer.substring(curPos));
            curSQLStatement.append('\n');

        }
        String sqlStatement = curSQLStatement.toString().trim();
        if (!"".equals(sqlStatement)) {
            logger.debug("Found statement [" + sqlStatement + "]");
            scriptsRuntimeList.add(sqlStatement);
        }
        buffered.close();

        return scriptsRuntimeList;
    } // getDatabaseScriptsRuntime

    private int findNextSeparator(String sqlStatement, String separator, int curPos) {
        int nextPos = sqlStatement.indexOf(separator, curPos);
        while ((nextPos != -1) && isInQuotes(sqlStatement, nextPos)) {
            curPos = nextPos + 1;
            nextPos = sqlStatement.indexOf(separator, curPos);
        }
        return nextPos;
    }

    private boolean isInQuotes(String sqlStatement, int pos) {
        if (pos < 0) {
            return false;
        }
        String beforeStr = sqlStatement.substring(0, pos);
        int quoteCount = 0;
        int curPos = 0;
        int quotePos = beforeStr.indexOf("'");
        while (quotePos != -1) {
            quoteCount++;
            curPos = quotePos +1;
            quotePos = beforeStr.indexOf("'", curPos);
        }
        if (quoteCount % 2 == 0) {
            return false;
        } else {
            return true;
        }
    }

} // end DatabaseScripts
