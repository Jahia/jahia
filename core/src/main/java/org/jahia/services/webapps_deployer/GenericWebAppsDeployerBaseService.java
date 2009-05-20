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
 package org.jahia.services.webapps_deployer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.jahia.data.applications.ApplicationBean;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaInitializationException;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <p>Title: Generic web app deployment service for unsupported web applications
 * servers.</p>
 * <p>Description: This class was created to offer some basic support for
 * application servers that are not (yet) recognized by Jahia.</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 *
 * @author Serge Huber
 * @version 1.0
 */

public class GenericWebAppsDeployerBaseService extends JahiaWebAppsDeployerService {

    static private GenericWebAppsDeployerBaseService m_Instance = null;
    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(GenericWebAppsDeployerBaseService.class);
    /**
     * Use this method to get an instance of this class
     */
    public static synchronized GenericWebAppsDeployerBaseService getInstance () {

        if (m_Instance == null) {
            m_Instance = new GenericWebAppsDeployerBaseService ();
        }
        return m_Instance;
    }

    //-------------------------------------------------------------------------
    /**
     * Initialize with Tomcat configuration and disk paths
     *
     */
    public void start()
            throws JahiaInitializationException {
        super.start();
    } // end init

    public void stop() {}

    public boolean canDeploy () {
        return false;
    }

    public boolean deploy(String context, String filePath)
            throws org.jahia.exceptions.JahiaException {
        if(isPortletWarFile(filePath)) {
            deployPortletWarFile(context, filePath);
        }
        return false;
    }
    private boolean isPortletWarFile (String filePath) {
        // now let's see it the webapp is a JSR-168 portlet file.
        File portletXMLFile = new File (filePath + File.separator + "WEB-INF" + File.separator + "portlet.xml");
        return portletXMLFile.exists();
    }

    /**
     * Deploy a single .war web component file
     *
     * @param webContext the web context
     * @param filePath   the full path to the war file
     */
    protected void deployPortletWarFile(String webContext, String filePath)
            throws JahiaException {
        File dir = new File(filePath);
        if (dir.isDirectory()) {
            /** todo fill in PLUTO deployment specific work */
        }
    }
    public boolean undeploy (ApplicationBean app) throws org.jahia.exceptions.JahiaException {
        return false;
    }

    public boolean deploy(List files) {
        return false;
    }


    protected Document parseXml(InputStream source) throws Exception
    {
        // Parse using the local dtds instead of remote dtds. This
        // allows to deploy the application offline
        SAXBuilder saxBuilder = new SAXBuilder();
        saxBuilder.setEntityResolver(new EntityResolver()
        {
            public InputSource resolveEntity(java.lang.String publicId, java.lang.String systemId) throws SAXException,
                                                                                                          java.io.IOException
            {
                return org.jahia.settings.SettingsBean.getInstance().getDtdEntityResolver().resolveEntity(publicId, systemId);
            }
        });
        return saxBuilder.build(source);
    }

    protected void writeFile(Document source, OutputStream jos) throws IOException
    {
        if (source != null)
        {
            XMLOutputter xmlOutputter = new XMLOutputter(Format.getPrettyFormat());
            try
            {
                xmlOutputter.output(source, jos);
            }
            finally
            {
                jos.close();
            }
        }
    }
}