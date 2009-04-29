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
// $Id$
//
//  TomcatUsersModifier
//
//  17.05.2001  AK  added in jahia.
//

package org.jahia.utils.modifier;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.jahia.utils.keygenerator.JahiaKeyGen;
import org.jahia.utils.properties.PropertiesManager;



/**
 * Copyright:    Copyright (c) 2002
 * Company:      Jahia Ltd
 *
 * @author Alexandre Kraft
 * @version 1.0
 */
public class TomcatUsersModifier
{



    /**
	 * Default constructor.
	 * @author Alexandre Kraft
	 */
    public TomcatUsersModifier()
    {
        // do nothing :o)
    } // end constructor



    /**
	 * Load a complete properties file in memory by its filename.
	 * @author Alexandre Kraft
	 */
    public static boolean ensureValidity( String pathJahiaProperties )
    {
        boolean  pwdChanged   =  false;

        try {
            PropertiesManager pm           =  new PropertiesManager( pathJahiaProperties );

            if(pm.getProperty("server").indexOf("Tomcat")>-1)
            {
                // get tomcat-users.xml path...
                StringBuffer tomXMLPath = new StringBuffer( pm.getProperty("serverHomeDiskPath") );
                tomXMLPath.append( "conf" );
                tomXMLPath.append( File.separator );
                tomXMLPath.append( "tomcat-users.xml" );

                // calculate new password...
                String newPwd =  JahiaKeyGen.getKey( 10 );

                // open tomcat-users.xml file...
                FileReader     fileReader     =  new FileReader( tomXMLPath.toString() );
                BufferedReader bufferedReader =  new BufferedReader( fileReader );
                String         lineReaded     =  null;
                List         newLines       =  new ArrayList();

                // parse the file...
                boolean jahiaIsFound =  false;
                while((lineReaded = bufferedReader.readLine()) != null)
                {
                    if(lineReaded.indexOf("Jahia")>-1) {
                        jahiaIsFound = true;
                        int pos =  lineReaded.indexOf("password");
                        if(pos>-1) {
                            int start =  pos+10;
                            int stop  =  lineReaded.indexOf("\"", start);
                            if(lineReaded.substring(start,stop).equals("Jahia") || lineReaded.substring(start,stop).equals("")) {
                                pwdChanged = true;
                                newLines.add( lineReaded.substring(0,start) + newPwd + lineReaded.substring(stop,lineReaded.length()));
                            } else {
                                newLines.add( lineReaded );
                            }
                        }
                    } else {
                        // add line to List. no changes...
                        newLines.add( lineReaded );
                    }
                }

                // add a new line if user jahia isn't found...
                if(!jahiaIsFound) {
                    fileReader     =  new FileReader( tomXMLPath.toString() );
                    bufferedReader =  new BufferedReader( fileReader );
                    newLines       =  new ArrayList();
                    while((lineReaded = bufferedReader.readLine()) != null)
                    {
                        newLines.add( lineReaded );
                        if(lineReaded.indexOf("<tomcat-users>")>-1) {
                            newLines.add( "<user name=\"Jahia\" password=\""+newPwd+"\" roles=\"manager\"></user>" );
                            pwdChanged = true;
                        }
                    }
                }

                // write the file...
                FileWriter   fileWriter   =  new FileWriter( tomXMLPath.toString() );
                StringBuffer outputBuffer =  new StringBuffer();
                for(int i=0; i < newLines.size(); i++) {
                    outputBuffer.append((String) newLines.get(i));
                    outputBuffer.append("\n");
                }
                fileWriter.write( outputBuffer.toString() );

                // close streams...
                bufferedReader.close();
                fileReader.close();
                fileWriter.close();

                // performance issue, force garbage collector...
                newPwd         =  null;
                lineReaded     =  null;
                outputBuffer   =  null;
                newLines       =  null;
                bufferedReader =  null;
                fileWriter     =  null;
                fileReader     =  null;
                tomXMLPath     =  null;
            }
            pm = null;

        } catch (Exception e) {
        }
    return pwdChanged;
    } // end ensureValidity


}