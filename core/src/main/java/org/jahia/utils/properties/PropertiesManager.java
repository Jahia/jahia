/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
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
import java.util.regex.PatternSyntaxException;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;


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
    private static final transient Logger logger =
            org.slf4j.LoggerFactory.getLogger (PropertiesManager.class);

    public Properties properties =  new Properties();
    public String     propertiesFilePath;



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
    private void loadProperties()
    {
        properties  = new Properties();
        File file =  new File(propertiesFilePath);
        if (!file.exists()) {
            return;
        } 
    	InputStream inputStream = null;
        try
        {
            inputStream =  new BufferedInputStream(new FileInputStream( file ));
            properties.load( inputStream );

        } catch (IOException ioe) {
            logger.error( "IOException on loadProperties().", ioe );
        } catch (SecurityException se) {
            logger.error( "SecurityException on file ["+propertiesFilePath+"]", se);
        } finally {
            IOUtils.closeQuietly(inputStream);
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
    public void storeProperties( String propertiesFilePath )
    throws NullPointerException
    {
        File        propertiesFileObject     =  new File( propertiesFilePath );
        File        propertiesFileFolder     =  propertiesFileObject.getParentFile();

        // check if the destination folder exists and create it if needed...
        if(!propertiesFileFolder.exists()) {
            propertiesFileFolder.mkdirs();
            propertiesFileFolder = null;
        }

        try
        {
            if(new File(this.propertiesFilePath).exists())
            {
                List      bufferList             =  new ArrayList();
                String      lineReaded               =  null;

                BufferedReader buffered                  =  new BufferedReader( new FileReader( this.propertiesFilePath ) );
                int            position                  =  0;

                // compose all properties List, used to find the new properties...
                List         allProperties             =  new ArrayList();
                Iterator    allPropertiesEnumeration  =  new EnumerationIterator(properties.propertyNames());
                while(allPropertiesEnumeration.hasNext()) {
                    allProperties.add( (String) allPropertiesEnumeration.next() );
                }

                // parse the file...
                while((lineReaded = buffered.readLine()) != null)
                {
                    try {
                        lineReaded = lineReaded.replaceAll("\\t","    "); //now supports Tab characters in lines
                        if(!lineReaded.trim().equals("") && !lineReaded.trim().substring(0,1).equals("#"))
                        {
                            boolean     propertyFound =  false;
                            Iterator propertyNames =  allProperties.iterator();

                            while(propertyNames.hasNext() && !propertyFound)
                            {
                                String  propertyName  =  (String) propertyNames.next();
                                String  propvalue =  properties.getProperty( propertyName );

                                if(lineReaded.indexOf(propertyName + " ") == 0) {
                                    position =  lineReaded.indexOf("=");
                                    if(position >= 0)
                                    {
                                        propertyFound  = true;

                                        StringBuffer thisLineBuffer =  new StringBuffer();
                                        thisLineBuffer.append( lineReaded.substring(0,position+1) );
                                        thisLineBuffer.append( "   " );
                                        thisLineBuffer.append( string2Property( propvalue ) );
                                        bufferList.add( thisLineBuffer.toString() );

                                        // remove this line from allProperties to affine the search and to find new properties...
                                        allProperties.remove( propertyName );
                                    }
                                }
                            }
                        }
                        else
                        {
                            // this is a special line only for layout, like a comment or a blank line...
                            bufferList.add( lineReaded.trim() );
                        }
                    } catch (IndexOutOfBoundsException ioobe) {
                    } catch (PatternSyntaxException ex1) {
                        logger.error(ex1.getMessage(), ex1);
                    } catch (IllegalArgumentException ex2) {
                        logger.error(ex2.getMessage(), ex2);
                    }
                }

                // add not found properties at the end of the file (and the jahia.properties layout is keeping)...
                Iterator restantPropertyNames =  allProperties.iterator();
                while(restantPropertyNames.hasNext())
                {
                    String restantPropertyName = (String) restantPropertyNames.next();
                    StringBuffer specialLineBuffer =  new StringBuffer();
                    specialLineBuffer.append( restantPropertyName );
                    for(int i=0; i<55-restantPropertyName.length(); i++) {
                        specialLineBuffer.append( " " );
                    }
                    specialLineBuffer.append( "=   " );
                    specialLineBuffer.append( properties.getProperty( restantPropertyName ) );
                    bufferList.add( specialLineBuffer.toString() );
                }

                // close the buffered filereader...
                buffered.close();

                // write the file...
                writeTheFile( propertiesFilePath, bufferList );
            }
            else
            {
                FileOutputStream outputStream =  new FileOutputStream( propertiesFileObject );
                properties.store( outputStream, "This file has been written by Jahia." );
                outputStream.close();
            }
        } catch (java.io.IOException ioe) {
        }
    } // end storeProperties



    /**
	 * Write the file composed by the storeProperties() method, using
	 * @author Alexandre Kraft
	 *
	 * @param   propertiesFilePath  The filesystem path where the file is saved.
	 * @param   bufferList        List containing all the string lines of the new file.
	 */
    private void writeTheFile( String propertiesFilePath,
                               List bufferList )
    {

        File         thisFile     =  null;
        FileWriter   fileWriter   =  null;
        StringBuffer outputBuffer =  null;

        try
        {
            thisFile     =  new File( propertiesFilePath );
            fileWriter   =  new FileWriter( thisFile );
            outputBuffer =  new StringBuffer();

            for(int i=0; i < bufferList.size(); i++) {
                outputBuffer.append((String) bufferList.get(i));
                outputBuffer.append("\n");
            }

            fileWriter.write( outputBuffer.toString() );
        } catch (java.io.IOException ioe) {
    	} finally {
            try {
            	fileWriter.close();
            }catch ( java.io.IOException ioe2 ){
			}
			fileWriter = null;
			thisFile = null;
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

}