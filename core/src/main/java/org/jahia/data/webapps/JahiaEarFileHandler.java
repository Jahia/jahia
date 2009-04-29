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
//  JahiaEarFileHandler
//
//  NK      13.01.2001
//
//

package org.jahia.data.webapps;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jahia.exceptions.JahiaException;
import org.jahia.utils.zip.JahiaArchiveFileHandler;

/**
 * This class is responsible for loading data from a .Ear File
 * The Information are loaded from the META-INF/application.xml file :
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
public class JahiaEarFileHandler {

    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(JahiaEarFileHandler.class);

    /** The web.xml file **/
    private static final String APPLICATION_XML_FILE =
        "META-INF/application.xml";

    /** The Archive File **/
    private JahiaArchiveFileHandler m_ArchFile;

    /** The List of Web Compomnents Definition **/
    private List<Web_Component> m_WebComponents = new ArrayList<Web_Component>();

    /** The Application_Xml to store data extracted from application.xml file **/
    private Application_Xml m_AppXmlDoc;

    /**
     * Constructor is initialized with the Ear File full path
     *
     */
    public JahiaEarFileHandler (String filePath)
        throws JahiaException {

        File f = new File(filePath);
        try {

            m_ArchFile = new JahiaArchiveFileHandler(f.getAbsolutePath());

        } catch (IOException e) {

            String errMsg = "Failed creating an Archive File Handler ";
            logger.error(errMsg + "\n" + e.toString(), e);
            throw new JahiaException("JahiaEarFileHandler", errMsg,
                                     JahiaException.SERVICE_ERROR,
                                     JahiaException.ERROR_SEVERITY, e);

        }

        try {
            buildWebComponents();
        } catch (JahiaException je) {

            if (m_ArchFile != null) {
                m_ArchFile.closeArchiveFile();
            }

            logger.error("error building the WebApssWarPackage" + je.toString(),
                         je);
            throw new JahiaException("JahiaEarFileHandler",
                "error building the JahiaWebAppsWar Package",
                                     JahiaException.SERVICE_ERROR,
                                     JahiaException.ERROR_SEVERITY, je);

        }

    }

    /**
     * Extract data from the application.xml file and build the list
     * of WebComponents Definition
     *
     */
    protected void buildWebComponents ()
        throws JahiaException {

        // extract data from the application.xml file
        try {
            File tmpFile = m_ArchFile.extractFile(APPLICATION_XML_FILE);

            //System.out.println(" tmpxmlfile is " + tmpFile.getAbsolutePath() );

            m_AppXmlDoc = new Application_Xml(tmpFile.getAbsolutePath());
            m_AppXmlDoc.extractDocumentData();
            m_WebComponents = m_AppXmlDoc.getWebComponents();
            tmpFile.deleteOnExit();
            tmpFile.delete();
        } catch (IOException ioe) {

            String errMsg = "Failed extracting application.xml file data ";
            logger.error(errMsg + "\n" + ioe.toString(), ioe);
            throw new JahiaException("JahiaEarFileHandler", errMsg,
                                     JahiaException.SERVICE_ERROR,
                                     JahiaException.ERROR_SEVERITY, ioe);

        }

    }

    /**
     * Returns the WebComponents list
     *
     * @return (List) the list of Web Component Definitions
     */
    public List<Web_Component> getWebComponents () {

        return m_WebComponents;

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

} // End Class JahiaEarFileHandler
