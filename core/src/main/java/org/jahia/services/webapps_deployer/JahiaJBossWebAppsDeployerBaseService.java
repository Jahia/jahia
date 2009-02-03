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

//
//
//  JahiaJBossWebAppsDeployerBaseService
//
//
//


package org.jahia.services.webapps_deployer;


import org.jahia.data.applications.ApplicationBean;
import org.jahia.exceptions.JahiaException;
import org.jahia.utils.JahiaTools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This Service Register new Application Definition,
 * deploy webApps packaged in a .war or .ear file.
 *
 * @author thomas draier
 * @version 1.0
 */
public class JahiaJBossWebAppsDeployerBaseService
        extends JahiaTomcatWebAppsDeployerBaseService {

    private static JahiaJBossWebAppsDeployerBaseService m_Instance = null;

    /**
     * Use this method to get an instance of this class
     */
    public static synchronized JahiaTomcatWebAppsDeployerBaseService getInstance () {

        if (m_Instance == null) {
            m_Instance = new JahiaJBossWebAppsDeployerBaseService ();
        }
        return m_Instance;
    }



    //-------------------------------------------------------------------------
    /**
     * Deploy a single .war web component file
     *
     * @param webContext , the web context
     * @param filePath   , the full path to the war file
     */
    protected boolean deployWarFile(String webContext, String filePath)
            throws JahiaException {
        List webApps = new ArrayList();
        StringBuffer webContextDiskPath = new StringBuffer (m_ServerHomeDiskPath);
        webContextDiskPath.append (File.separator);
        webContextDiskPath.append ("deploy");
        webContextDiskPath.append (File.separator);
        webContextDiskPath.append (webContext);
        webContextDiskPath.append (".war");

        //StringBuffer webContextBuff = new StringBuffer(site.getSiteKey());
        StringBuffer webContextBuff = new StringBuffer (webContext);

        webApps = handleWarFile (webContextDiskPath.toString (), filePath, true);

        // Activate the Web App in Tomcat
        // activateWebApp(webContextBuff.toString(),webContextDiskPath.toString());

        // register Web Apps in Jahia
        File f = new File (filePath);
        registerWebApps (webContextBuff.toString (), f.getName (), webApps);

        // move the war to the context
        File warFile = new File (filePath);
        warFile.delete ();

        /*
        webContextDiskPath.append(File.separator);
        webContextDiskPath.append(warFile.getName());

        File newPos = new File(webContextDiskPath.toString()+File.separator+m_WEB_INF);
        if ( !warFile.renameTo(newPos) ){
            warFile.delete();
        }
        */

        return true;

    }


    //-------------------------------------------------------------------------
    /**
     * Undeploy a web application. Delete the web component from disk.
     *
     * @param (app) the application bean object
     *
     * @return (boolean) true if successfull
     */

    public boolean undeploy (ApplicationBean app) throws JahiaException {
        if (app != null) {

            // try to delete physically the directory on disk
            StringBuffer webContextDiskPath = new StringBuffer (m_ServerHomeDiskPath);
            webContextDiskPath.append (File.separator);
            webContextDiskPath.append ("deploy");
            webContextDiskPath.append (File.separator);
            webContextDiskPath.append (app.getContext ());
            webContextDiskPath.append (".war");
            JahiaTools.deleteFile (new File (webContextDiskPath.toString ()));

            return true;
        }

        return false;
    }


    protected boolean activateWebApp (
            String context,
            String webAppDiskPath
            ) throws JahiaException {
        return true;
    }

    protected boolean undeployWebApp (String context
                                      ) throws JahiaException {
        return true;
    }

    protected boolean addManagerUser (String docPath) {
        return true;
    }

    public boolean canDeploy () {
        return true;
    }
}
