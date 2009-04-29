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
//
//
//  JahiaWebAppsWarHandler
//
//  NK      13.01.2001
//
//

package org.jahia.data.webapps;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.jahia.exceptions.JahiaException;
import org.jahia.utils.zip.JahiaArchiveFileHandler;

/**
 * This class is responsible for loading data from a WebApp War File
 * The Information are loaded from the WEB-INF\web.xml file :
 *
 * -----------------------------------------------------------------
 * IMPORTANT !!!!! Must call the method closeArchiveFile()
 * to be able to delete the file later !!!
 * -----------------------------------------------------------------
 *
 * @author Khue ng
 * @version 1.0
 *
 */
public class JahiaWebAppsWarHandler {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(JahiaWebAppsWarHandler.class);

    /** The web.xml file **/
    private static final String WEB_XML_FILE = "WEB-INF/web.xml";
    private static final String PORTLET_XML_FILE = "WEB-INF/portlet.xml";

    /** The Full path to the War File **/
    private String m_FilePath;

    /** The Archive File **/
    private JahiaArchiveFileHandler m_ArchFile;

    /** The Jahia Web App War Package **/
    private JahiaWebAppsWarPackage m_WebAppsPackage;

    /** The Web_App_Xml to store data extracted from web.xml file **/
    private Web_App_Xml m_WebXmlDoc;

    /**
     * Constructor is initialized with the war File
     *
     */
    public JahiaWebAppsWarHandler (String filePath)
        throws JahiaException {

        m_FilePath = filePath;
        File f = new File(filePath);
        try {

            m_ArchFile = new JahiaArchiveFileHandler(f.getAbsolutePath());

        } catch (IOException e) {

            String errMsg = "Failed creating an Archive File Handler ";
            logger.error(errMsg + "\n" + e.toString(), e);
            throw new JahiaException("JahiaWebAppsWarHandler", errMsg,
                                     JahiaException.SERVICE_ERROR,
                                     JahiaException.ERROR_SEVERITY, e);

        }

        try {
            buildWebAppsWarPackage();
        } catch (JahiaException je) {

            if (m_ArchFile != null) {
                m_ArchFile.closeArchiveFile();
            }

            logger.error("error building the WebApssWarPackage" + je.toString(),
                         je);
            throw new JahiaException("JahiaWebAppsWarPackage",
                                     "error building the WebApssWarPackage",
                                     JahiaException.SERVICE_ERROR,
                                     JahiaException.ERROR_SEVERITY, je);

        }

    }

    /**
     * Extract data from th web.xml file and build the JahiaWebAppsWarPackage
     *
     */
    protected void buildWebAppsWarPackage ()
        throws JahiaException {

        String type = "servlet";
        // extract data from the web.xml file
        try {
            File tmpFile = m_ArchFile.extractFile(WEB_XML_FILE);
            m_WebXmlDoc = new Web_App_Xml(tmpFile.getAbsolutePath());
            m_WebXmlDoc.extractDocumentData();
            tmpFile.deleteOnExit();
            tmpFile.delete();

            if (m_ArchFile.entryExists(PORTLET_XML_FILE)) {
                type = "portlet";
            }
            if("servlet".equals(type) && ! m_ArchFile.entryExists("WEB-INF/jahia.xml")) {
                throw new JahiaException("JahiaWebAppsWarPackage",
                                         "Cannot find file WEB-INF/jahia.xml in war archive",
                                         JahiaException.SERVICE_ERROR,
                                         JahiaException.ERROR_SEVERITY);
            }
        } catch (IOException ioe) {

            String errMsg = "Failed extracting web.xml file data ";
            logger.error(errMsg + "\n" + ioe.toString(), ioe);
            throw new JahiaException("JahiaWebAppsWarPackage", errMsg,
                                     JahiaException.SERVICE_ERROR,
                                     JahiaException.ERROR_SEVERITY, ioe);

        }

        // Actually the Context Root for the web application is the war filename without the .war extension
        String contextRoot = "/"+removeFileExtension( (new File(m_FilePath)).
                                                 getName(), ".war");

        // build the list of the Web Apps Definition
        m_WebAppsPackage = new JahiaWebAppsWarPackage(contextRoot);

        List servlets = m_WebXmlDoc.getServlets();
        int size = servlets.size();

        Servlet_Element servlet = null;

        String webAppName = m_WebXmlDoc.getDisplayName();
        if (webAppName == null || webAppName.length() <= 0) {
            webAppName = removeFileExtension( (new File(m_FilePath)).getName(),
                                             ".war");
        }

        JahiaWebAppDef webAppDef = new JahiaWebAppDef(webAppName,
            contextRoot,
            type
            );

        webAppDef.addRoles(m_WebXmlDoc.getRoles());

        // set description
        String desc = m_WebXmlDoc.getdesc();
        if(desc!=null){
            webAppDef.setdesc(m_WebXmlDoc.getdesc());
        }

        for (int i = 0; i < size; i++) {

            servlet = (Servlet_Element) servlets.get(i);

            webAppDef.addServlet(servlet);

        }

        m_WebAppsPackage.addWebAppDef(webAppDef);

    }

    /**
     * Returns the WebApps Package Object
     *
     * @return (JahiaWebAppsPackage) the Jahia WebApps Package Object
     */
    public JahiaWebAppsWarPackage getWebAppsPackage () {

        return m_WebAppsPackage;

    }

    /**
     * Unzip the contents of the jar file in it's current folder
     *
     */
    public void unzip ()
        throws JahiaException {

        // Unzip the file
        m_ArchFile.unzip();

    }

    /**
     * Unzip the contents of the jar file in a gived folder
     *
     * @param (String) path , the path where to extract file
     */
    public void unzip (String path)
        throws JahiaException {

        // Unzip the file
        m_ArchFile.unzip(path);

    }

    /**
     * Unzip an entry in a gived folder
     *
     * @param (String) entryName , the name of the entry
     * @param (String) path , the path where to extract file
     */
    public void extractEntry (String entryName,
                              String path)
        throws JahiaException {

        // Unzip the entry
        m_ArchFile.extractEntry(entryName, path);

    }

    /**
     * Close the Jar file
     *
     */
    public void closeArchiveFile () {

        if (m_ArchFile != null) {
            m_ArchFile.closeArchiveFile();
        }
    }

    /**
     * Return the file name of the war file without the .war extension
     *
     * @param (String) filename , the complete file name with extension
     * @param (String) ext , the extension to remove
     * @return(String) the filename without a gived extension
     */
    protected String removeFileExtension (String filename, String ext) {

        String name = filename.toLowerCase(); // work on a copy
        if (name.endsWith(ext.toLowerCase())) {
            return (filename.substring(0, name.lastIndexOf(ext.toLowerCase())));
        }
        return filename;
    }

} // End Class JahiaWebAppsWarHandler
