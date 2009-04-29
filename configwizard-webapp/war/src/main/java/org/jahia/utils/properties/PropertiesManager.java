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
package org.jahia.utils.properties;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.regex.PatternSyntaxException;

import org.jahia.utils.JahiaTools;



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
    	FileInputStream inputStream = null;
        try
        {
            
            inputStream =  new FileInputStream( propertiesFilePath );

            properties  = new Properties();
            properties.load( inputStream );

        } catch (IOException ioe) {

        } catch (SecurityException se) {
            
        } finally {
        	try {
	        	inputStream.close();
    	    	inputStream = null;
    	    } catch (Throwable t){
    	    	t.printStackTrace();
    	    }
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
        boolean     baseObjectExists         =  true;
        List<String>      bufferList             =  new ArrayList<String>();
        String      lineReaded               =  null;

        File        propertiesFileObject     =  new File( propertiesFilePath );
        File        propertiesFileFolder     =  propertiesFileObject.getParentFile();

        // check if the destination folder exists and create it if needed...
        if(!propertiesFileFolder.exists()) {
            propertiesFileFolder.mkdirs();
            propertiesFileFolder = null;
        }

        // try to create a file object via the propertiesFilePath...
        try {
            new File(this.propertiesFilePath);
        } catch (NullPointerException npe) {
            baseObjectExists = false;
        }

        try
        {
            if(baseObjectExists)
            {
                BufferedReader buffered                  =  new BufferedReader( new FileReader( this.propertiesFilePath ) );
                int            position                  =  0;

                // compose all properties List, used to find the new properties...
                List<String>         allProperties             =  new ArrayList<String>();
                Enumeration<?>    allPropertiesEnumeration  =  properties.propertyNames();
                while(allPropertiesEnumeration.hasMoreElements()) {
                    allProperties.add( (String) allPropertiesEnumeration.nextElement() );
                }

                // parse the file...
                while((lineReaded = buffered.readLine()) != null)
                {
                    try {
                        lineReaded = lineReaded.replaceAll("\\t","    "); //now supports Tab characters in lines
                        if(!lineReaded.trim().equals("") && !lineReaded.trim().substring(0,1).equals("#"))
                        {
                            boolean     propertyFound =  false;
                            Iterator<String> propertyNames =  allProperties.iterator();

                            while(propertyNames.hasNext() && !propertyFound)
                            {
                                String  propertyName  =  propertyNames.next();
                                String  propvalue =  properties.getProperty( propertyName );

                                if(lineReaded.indexOf(propertyName + " ") == 0) {
                                    position =  lineReaded.indexOf("=");
                                    if(position >= 0)
                                    {
                                        propertyFound  = true;

                                        StringBuffer thisLineBuffer =  new StringBuffer();
                                        thisLineBuffer.append( lineReaded.substring(0,position+1) );
                                        thisLineBuffer.append( "   " );
                                        thisLineBuffer.append( JahiaTools.string2Property( propvalue ) );
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
                        ex1.printStackTrace();
                    } catch (IllegalArgumentException ex2) {
                        ex2.printStackTrace();
                    }
                }

                // add not found properties at the end of the file (and the jahia.properties layout is keeping)...
                Iterator<String> restantPropertyNames =  allProperties.iterator();
                while(restantPropertyNames.hasNext())
                {
                    String restantPropertyName = restantPropertyNames.next();
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
                               List<String> bufferList )
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
                outputBuffer.append(bufferList.get(i));
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


}