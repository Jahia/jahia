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

/**
 * Created by IntelliJ IDEA.
 * User: islam
 * Date: 24 juin 2008
 * Time: 11:51:55
 * To change this template use File | Settings | File Templates.
 */


    import java.io.BufferedReader;
    import java.io.File;
    import java.io.FileInputStream;
    import java.io.FileOutputStream;
    import java.io.FileReader;
    import java.io.FileWriter;
    import java.io.IOException;
import java.util.Enumeration;
    import java.util.Iterator;
import java.util.List;
    import java.util.Properties;
    import java.util.ArrayList;
import java.util.regex.PatternSyntaxException;

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
                  System.out.println(" problem in the loading the properties :"+ioe);
            } catch (SecurityException se) {
                     System.out.println(" problem in the loading the properties :"+se);
            } finally {
                try {
                    //inputStream.close();
                    //inputStream = null;
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
            List      bufferList             =  new ArrayList();
            String      lineReaded               =  null;

            File        propertiesFileObject     =  new File( propertiesFilePath );
            File        propertiesFileFolder     =  propertiesFileObject.getParentFile();

            // check if the destination folder exists and create it if needed...
            if(!propertiesFileFolder.exists()) {
                propertiesFileFolder.mkdirs();
                propertiesFileFolder = null;
            }

            // try to create a file object via the propertiesFilePath...
            File propertiesFileObjectBase = null;
            try {
                propertiesFileObjectBase    =  new File( this.propertiesFilePath );

            } catch (NullPointerException npe) {
                baseObjectExists = false;
            } finally {
                propertiesFileObjectBase = null;
            }

            try
            {
                if(baseObjectExists)
                {
                    BufferedReader buffered                  =  new BufferedReader( new FileReader( this.propertiesFilePath ) );
                    int            position                  =  0;

                    // compose all properties List, used to find the new properties...
                    List         allProperties             =  new ArrayList();
                    Enumeration    allPropertiesEnumeration  =  properties.propertyNames();
                    while(allPropertiesEnumeration.hasMoreElements()) {
                        allProperties.add( (String) allPropertiesEnumeration.nextElement());
                    }

                    // parse the file...
                    while((lineReaded = buffered.readLine()) != null)
                    {
                        try {
                            lineReaded = lineReaded.replaceAll("\\t","    "); //now supports Tab characters in lines
                            if(!lineReaded.trim().equals("") && !lineReaded.trim().substring(0,1).equals("#"))
                            {
                                boolean     propertyFound =  false;
                                int         countThisLine =  0;
                                Iterator propertyNames =  allProperties.iterator();

                                while(propertyNames.hasNext() && !propertyFound)
                                {
                                    String  propertyName  =  (String) propertyNames.next();
                                    String  propvalue =  properties.getProperty( propertyName );
                                    //System.out.println("getting property: "+propertyName+" with value "+propvalue);
                                    if(lineReaded.indexOf(propertyName + " ") == 0) {
                                        position =  lineReaded.indexOf("=");
                                        if(position >= 0)
                                        {
                                            propertyFound  = true;

                                            StringBuffer thisLineBuffer =  new StringBuffer();
                                            thisLineBuffer.append( lineReaded.substring(0,position+1) );
                                            thisLineBuffer.append( "   " );
                                            thisLineBuffer.append(  propvalue  );
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


}
