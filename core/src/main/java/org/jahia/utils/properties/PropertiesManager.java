/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
// $Id$
//
//  PropertiesManager
//
//  27.03.2001  AK  added in jahia.
//  28.03.2001  AK  mammouth fix into method storeProperties.
//  12.08.2001  NK  Close input stream after reading from property file, otherwhise the file cannot be deleted.
//

package org.jahia.utils.properties;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * desc:  This class provides to you a *super interface* for properties.
 * It allows you to create a new properties file, set properties, remove
 * properties, get properties, get properties from an existing properties
 * object or from a flat file, get a complete properties object, and you can
 * store the new properties object where you want on the filesystem. The store
 * method keep your base properties file design (not like the store() method
 * from java properties object)!
 *
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 *
 * @author Alexandre Kraft
 * @version 1.0
 */
public class PropertiesManager
{
    private static final Logger logger = LoggerFactory.getLogger(PropertiesManager.class);

    public Properties properties = new Properties();
    public String propertiesFilePath;



    /**
	 * Default constructor.
	 * @author Alexandre Kraft
	 */
    public PropertiesManager()
    {
        // do nothing :o)
    } // end constructor



    /**
	 * Default constructor.
	 * @author Alexandre Kraft
	 *
	 * @param   propertiesFilePath  The filesystem path from the file is loaded.
	 */
    public PropertiesManager( String propertiesFilePath )
    {
        this.propertiesFilePath =  propertiesFilePath;
        this.loadProperties();
    } // end constructor



    /**
	 * Default constructor.
	 * @author Alexandre Kraft
	 *
	 * @param   properties  The properties object used to define base properties.
	 */
    public PropertiesManager( Properties properties )
    {
        this.properties =  properties;
    } // end constructor



    /**
	 * Load a complete properties file in memory by its filename.
	 * @author Alexandre Kraft
	 */
    private void loadProperties() {
        properties = new Properties();
        File file = new File(propertiesFilePath);
        if (!file.exists()) {
            return;
        }
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            properties.load(inputStream);
        } catch (IOException ioe) {
            logger.error("IOException on loadProperties().", ioe);
        } catch (SecurityException se) {
            logger.error("SecurityException on file [" + propertiesFilePath + "]", se);
        }
    } // end loadProperties

    /**
	 * Get a property value by its name.
	 * @author Alexandre Kraft
     *
	 * @param   propertyName    The property name to get its value.
	 * @return  Returns a String containing the value of the property name.
	 */
    public String getProperty( String propertyName )
    {
        return properties.getProperty( propertyName );
    } // end getProperty



    /**
	 * Set a property value by its name.
	 * @author Alexandre Kraft
     *
	 * @param   propertyName    The property name to set.
	 * @param   propvalue   The property value to set.
	 */
    public void setProperty( String propertyName,
                             String propvalue )
    {
        properties.setProperty( propertyName, propvalue );
    } // end setProperty



    /**
	 * Remove a property by its name.
	 * @author Alexandre Kraft
     *
	 * @param   propertyName    The property name to remove.
	 */
    public void removeProperty( String propertyName )
    {
        properties.remove( propertyName );
    } // end removeProperty



	/**
	 * Store new properties and values in the properties file.
	 * The file writed is the same, using the same file path, as the file loaded before.
	 * @author Alexandre Kraft
	 */
    public void storeProperties()
    {
        try {
            storeProperties( this.propertiesFilePath );
        } catch (NullPointerException npe) {
            logger.error( "NullPointerException on storeProperties().", npe);
        }
    } // end storeProperties



    /**
	 * Store new properties and values in the properties file.
	 * If the file where you want to write doesn't exists, the file is created.
	 * @author  Alexandre Kraft
	 * @author  Khue N'Guyen
	 *
	 * @param   propertiesFilePath  The filesystem path where the file is saved.
	 */
    public void storeProperties(String propertiesFilePath) {
        File propertiesFileObject = new File(propertiesFilePath);
        File propertiesFileFolder = propertiesFileObject.getParentFile();

        // check if the destination folder exists and create it if needed...
        if (!propertiesFileFolder.exists()) {
            propertiesFileFolder.mkdirs();
            propertiesFileFolder = null;
        }

        try {
            if (new File(this.propertiesFilePath).exists()) {
                List<String> bufferList = new ArrayList<>();
                String lineReaded = null;

                try (BufferedReader buffered = new BufferedReader(new FileReader(this.propertiesFilePath))) {
                    int position = 0;

                    // compose all properties List, used to find the new properties...
                    List<String> allProperties = (List<String>) Collections.list(properties.propertyNames());

                    // parse the file...
                    while ((lineReaded = buffered.readLine()) != null) {
                        try {
                            lineReaded = lineReaded.replaceAll("\\t", "    "); // now supports Tab characters in lines
                            if (!lineReaded.trim().equals("") && !lineReaded.trim().substring(0, 1).equals("#")) {
                                boolean propertyFound = false;
                                Iterator<String> propertyNames = allProperties.iterator();

                                while (propertyNames.hasNext() && !propertyFound) {
                                    String propertyName = propertyNames.next();
                                    String propvalue = properties.getProperty(propertyName);

                                    if (lineReaded.indexOf(propertyName + " ") == 0) {
                                        position = lineReaded.indexOf('=');
                                        if (position >= 0) {
                                            propertyFound = true;

                                            StringBuilder thisLineBuffer = new StringBuilder();
                                            thisLineBuffer.append(lineReaded.substring(0, position + 1));
                                            thisLineBuffer.append("   ");
                                            thisLineBuffer.append(string2Property(propvalue));
                                            bufferList.add(thisLineBuffer.toString());

                                            // remove this line from allProperties to affine the search and to find new properties...
                                            allProperties.remove(propertyName);
                                        }
                                    }
                                }
                            } else {
                                // this is a special line only for layout, like a comment or a blank line...
                                bufferList.add(lineReaded.trim());
                            }
                        } catch (IndexOutOfBoundsException ioobe) {
                        } catch (IllegalArgumentException ex) {
                            logger.error(ex.getMessage(), ex);
                        }
                    }

                    // add not found properties at the end of the file (and the jahia.properties layout is keeping)...
                    Iterator<String> restantPropertyNames = allProperties.iterator();
                    while (restantPropertyNames.hasNext()) {
                        String restantPropertyName = restantPropertyNames.next();
                        StringBuilder specialLineBuffer = new StringBuilder();
                        specialLineBuffer.append(restantPropertyName);
                        for (int i = 0; i < 55 - restantPropertyName.length(); i++) {
                            specialLineBuffer.append(" ");
                        }
                        specialLineBuffer.append("=   ");
                        specialLineBuffer.append(properties.getProperty(restantPropertyName));
                        bufferList.add(specialLineBuffer.toString());
                    }
                }

                // write the file...
                writeTheFile(propertiesFilePath, bufferList);
            } else {
                try (FileOutputStream outputStream = new FileOutputStream(propertiesFileObject)) {
                    properties.store(outputStream, "This file has been written by Jahia.");
                }
            }
        } catch (IOException ioe) {
        }
    } // end storeProperties



    /**
	 * Write the file composed by the storeProperties() method, using
	 * @author Alexandre Kraft
	 *
	 * @param   propertiesFilePath  The filesystem path where the file is saved.
	 * @param   bufferList        List containing all the string lines of the new file.
	 */
    private void writeTheFile(String propertiesFilePath, List<String> bufferList) {

        try (FileWriter fileWriter = new FileWriter(new File(propertiesFilePath))) {
            StringBuilder outputBuffer = new StringBuilder();

            for (int i = 0; i < bufferList.size(); i++) {
                outputBuffer.append(bufferList.get(i));
                outputBuffer.append("\n");
            }

            fileWriter.write(outputBuffer.toString());
        } catch (IOException ioe) {
        }
    } // end writeTheFile



    /**
	 * Get the properties object for the instance of this class.
	 * @author Alexandre Kraft
	 *
	 * @return  The properties object for the instance of this class.
	 */
    public Properties getPropertiesObject()
    {
        return properties;
    } // end getPropertiesObject

    /**
     * Convert a standard string to a property-compatible string.
     *
     * @param originalValue The string that you want to convert.
     * @return The string converted.
     * @author Alexandre Kraft
     */
    public static String string2Property(String originalValue) {
        StringBuilder convertedValue = new StringBuilder();
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

}
