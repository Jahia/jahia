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
package org.jahia.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;

import org.jahia.data.constants.JahiaConstants;




public class ServletContainerUtils
{

    public static Map infos = null;


    //-------------------------------------------------------------------------
    /**
     * Default constructor.
     */
    public ServletContainerUtils ()
    {
        // nothing to do...
    }


   /**
     * Get server specific informations, like it's name (Tomcat, etc.)
     * and it's server home disk path. This method returns the result in a
     * Map with these keys: <code>type</code> and <code>home</code>.
     *
     * @author  Khue N'Guyen
     * @author  Alexandre Kraft
     */
    public static Map getServerInformations( ServletConfig config )
    {
        ServletContext context = config.getServletContext();

        if( infos != null ) {
            return infos;
        } else {

            // initialize the result hashmap...
            infos = new HashMap();
            infos.put("info", context.getServerInfo() );

            //JahiaConsole.println("getServerInformations","serverinfo = " + context.getServerInfo() );

            // try to get the server type
            if ( context.getServerInfo().toLowerCase().indexOf("tomcat")>=0 )
            {
                infos.put("type",JahiaConstants.SERVER_TOMCAT);

                //JahiaConsole.println("getServerInformations", "servertype = " +infos.get("type") );

                // try to get the server home disk path
                String contextRealPath = context.getRealPath("/" + config.getServletName());
                String jahiaRealPath = JahiaTools.replacePattern(contextRealPath,config.getServletName(),"");
                jahiaRealPath = JahiaTools.replacePattern(jahiaRealPath,File.separator+"."+File.separator,File.separator);

                File jahiaDir = new File(jahiaRealPath);					// ../tomcat/webapps/jahia
                //JahiaConsole.println("getServerInformations", "jahia dir name = " + jahiaDir.getAbsolutePath() );

                String webAppPath = jahiaDir.getParent();
                File webAppDir = new File(webAppPath);
                //JahiaConsole.println("getServerInformations", "webapp dir = " + webAppDir.getAbsolutePath() );

                String tomcatPath = webAppDir.getParent();
                File tomcatDir = new File(tomcatPath);

                //JahiaConsole.println("getServerInformations", "tomcat dir name = " + tomcatDir.getName() );

                /*
                File tomcatParentDir = tomcatDir.getParentFile();
                File[] files = tomcatParentDir.listFiles();
                for ( int i=0 ; i<files.length ; i++ ){
                    if ( files[i].isDirectory() && (files[i].getName().indexOf(JahiaConstants.SERVER_TOMCAT) != -1) ){
                        JahiaConsole.println("getServerInformations",
                        "Files[i] = " + files[i].getAbsolutePath() );
                        tomcatDir = files[i];
                        break;
                    }
                }
                */

                //JahiaConsole.println("getServerInformations", "tomcat real path = " + tomcatDir.getAbsolutePath() );

                String serverPath = tomcatDir.getAbsolutePath();

                if ( !serverPath.endsWith(File.separator) ){
                    serverPath+=File.separator;
                }

                infos.put("home", serverPath);

            } else if ( context.getServerInfo().toLowerCase().indexOf("weblogic")>=0 ) {
                infos.put("type",JahiaConstants.SERVER_WEBLOGIC);
                // try to get the server home disk path
                String contextRealPath = context.getRealPath("." + File.separator);
                int pos = contextRealPath.toLowerCase().lastIndexOf("server");
                if ( pos>=0 ){
                    int pos2 = contextRealPath.toLowerCase().indexOf(File.separator,pos);
                    if ( pos2 >=0 ){
                        infos.put("home", contextRealPath.substring(0, pos2) + File.separator);
                    }
                }
            } else {
                infos.put("type", "unknown");
                infos.put("home", "unknown");
            }

            try {
                Context ctx = new InitialContext();
                if (((String)ctx.getEnvironment().get("java.naming.factory.url.pkgs")).indexOf("org.jboss") > -1) {
                    infos.put("type",JahiaConstants.SERVER_JBOSS);
                    infos.put("info", infos.get("info") + " (JBoss integration)" );

                    // try to get the server home disk path
                    String contextRealPath = context.getRealPath("/" + config.getServletName());
                    String jahiaRealPath = JahiaTools.replacePattern(contextRealPath,config.getServletName(),"");
                    jahiaRealPath = JahiaTools.replacePattern(jahiaRealPath,File.separator+"."+File.separator,File.separator);

                    File jahiaDir = new File(jahiaRealPath);					// ../tomcat/webapps/jahia
                    //JahiaConsole.println("getServerInformations", "jboss dir name = " + jahiaDir.getAbsolutePath() );

                    String webAppPath = jahiaDir.getParent();
                    File webAppDir = new File(webAppPath);
                    //.println("getServerInformations", "webapp dir = " + webAppDir.getAbsolutePath() );
                   //
                    String tomcatPath = webAppDir.getParent();
                    File tomcatDir = new File(tomcatPath);

                    String serverPath = tomcatDir.getAbsolutePath();

                    if ( !serverPath.endsWith(File.separator) ){
                        serverPath+=File.separator;
                    }

                    infos.put("home", serverPath);

                }
            } catch (Exception e) {
            }

            return infos;
        }
    } // end getServerInformations



}
