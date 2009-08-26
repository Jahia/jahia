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

import org.jahia.settings.SettingsBean;
import org.jahia.utils.keygenerator.JahiaKeyGen;



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
    public static boolean ensureValidity()
    {
        boolean  pwdChanged   =  false;

        try {
                // get tomcat-users.xml path...
                StringBuffer tomXMLPath = new StringBuffer( SettingsBean.getInstance().getServerHomeDiskPath() );
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

        } catch (Exception e) {
        }
    return pwdChanged;
    } // end ensureValidity


}