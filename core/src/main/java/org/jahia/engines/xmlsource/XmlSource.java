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

package org.jahia.engines.xmlsource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.jahia.data.JahiaData;
import org.jahia.data.containers.JahiaContainer;
import org.jahia.data.containers.JahiaContainerList;
import org.jahia.data.fields.JahiaField;
import org.jahia.data.fields.LoadFlags;
import org.jahia.engines.*;
import org.jahia.engines.validation.EngineValidationHelper;
import org.jahia.exceptions.JahiaException;
import org.jahia.exceptions.JahiaForbiddenAccessException;
import org.jahia.params.ProcessingContext;
import org.jahia.params.ParamBean;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.containers.JahiaContainersService;
import org.jahia.services.fields.ContentField;
import org.jahia.services.pages.ContentPage;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.utils.xml.XMLSerializationOptions;
import org.jahia.utils.xml.XmlWriter;


public class XmlSource implements JahiaEngine {

    private static final org.apache.log4j.Logger logger
            = org.apache.log4j.Logger.getLogger (XmlSource.class);

    public static final String ENGINE_NAME = "xmlsource";

    public XmlSource () {
    }

    public boolean authoriseRender (ProcessingContext jParams) {
        logger.debug (
                "authorizeRender=" +
                Boolean.valueOf((jParams.getOperationMode () == ProcessingContext.EDIT)).toString ());
        return (jParams.getOperationMode () == ProcessingContext.EDIT);
    }

    public String renderLink (ProcessingContext jParams, Object theObj)
            throws JahiaException {
        JahiaField theField = (JahiaField) theObj;
        String params = "?mode=displaywindow&fid=" + theField.getID ();
        return jParams.composeEngineUrl (ENGINE_NAME, params);
    }


    public boolean needsJahiaData (ProcessingContext jParams) {
        return false;
    }


    public EngineValidationHelper handleActions (ProcessingContext jParams, JahiaData jData)
            throws JahiaException {

        JahiaUser curUser = jParams.getUser ();

        if (curUser == null) {
            throw new JahiaForbiddenAccessException ();
        }

        if (!curUser.isAdminMember (jParams.getSiteID ())) {
            // for the moment only site admins have access to this engine
            throw new JahiaForbiddenAccessException ();
        }


        String mode = jParams.getParameter ("mode");
        String ipAddr = jParams.getRemoteAddr ();
        String xsltFileName = jParams.getParameter ("xslt");

        Writer out;
        StringWriter strWriter = null;

        try {

            if (xsltFileName == null) {
                out = ((ParamBean)jParams).getResponse ().getWriter ();
            } else {
                strWriter = new StringWriter ();
                out = strWriter;
            }

            if (mode != null) {
                if ("field".equals (mode)) {
                    logger.debug (ipAddr + " is accessing field " + jParams.getFieldID ());
                    handleFieldData (jParams, out);
                } else if ("container".equals (mode)) {
                    logger.debug (
                            ipAddr + " is accessing container " + jParams.getContainerID ());
                    handleContainerData (jParams, out);
                } else if ("containerlist".equals (mode)) {
                    logger.debug (
                            ipAddr + " is accessing container list " +
                            jParams.getContainerListID ());
                    handleContainerListData (jParams, out);
                } else if ("page".equals (mode)) {
                    logger.debug (ipAddr + " is accessing page " + jParams.getPageID ());
                    handlePageData (jParams, out);
                } else if ("site".equals (mode)) {
                    logger.debug (ipAddr + " is accessing site " + jParams.getPageID ());
                    handleSiteData (jParams, out);
                }
            }

            if (xsltFileName != null) {
                out = ((ParamBean)jParams).getResponse ().getWriter ();
                try {
                    transform (jParams, strWriter.toString (), xsltFileName, out);
                } catch (Exception t) {
                    logger.debug (
                            "Error while transforming XML output using XSLT file " +
                            xsltFileName,
                            t);
                }
            }
        } catch (IOException ioe) {
            logger.debug (
                    "IOException while trying to output xml with params : mode=" + mode +
                    " xsltFileName=" +
                    xsltFileName,
                    ioe);
        }
        return null;
    }

    /**
     * Retrieve the engine name.
     *
     * @return the engine name.
     */
    public final String getName () {
        return ENGINE_NAME;
    }

    public void handleFieldData (ProcessingContext jParams, Writer out)
            throws JahiaException {
        int fieldID = jParams.getFieldID ();
        logger.debug ("Sending XML for field " + fieldID);
        ContentField theField = ContentField.getField (fieldID);
        try {
            ((ParamBean)jParams).getResponse ().setContentType ("text/xml");
            out.write ("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
            XmlWriter xmlWriter = new XmlWriter (out);
            xmlWriter.writeEntity ("jahia");
            xmlWriter.enablePrettyPrint (true);
            if (theField != null) {
                theField.serializeToXML (xmlWriter, new XMLSerializationOptions (), jParams);
            } else {
                xmlWriter.writeEntityWithText ("error", "Field " + fieldID + " not found");
            }
            xmlWriter.endEntity ();
            xmlWriter.close ();
            out.flush ();

        } catch (IOException ioe) {
            logger.debug ("Error while serializing field to XML ", ioe);
        }
    }


    public void handleContainerData (ProcessingContext jParams, Writer out)
            throws JahiaException {
        int containerID = jParams.getContainerID ();
        logger.debug ("Sending XML for container " + containerID);
        JahiaContainersService ctnSrv = ServicesRegistry.getInstance ()
                .getJahiaContainersService ();
        JahiaContainer theContainer = ctnSrv.loadContainer (containerID, LoadFlags.ALL,
                jParams, EntryLoadRequest.CURRENT);
        try {
            ((ParamBean)jParams).getResponse ().setContentType ("text/xml");
            out.write ("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
            XmlWriter xmlWriter = new XmlWriter (out);
            xmlWriter.writeEntity ("jahia");
            xmlWriter.enablePrettyPrint (true);
            if (theContainer != null) {
                ctnSrv.serializeContainerToXML (xmlWriter, new XMLSerializationOptions (),
                        containerID, jParams);
            } else {
                xmlWriter.writeEntityWithText ("error",
                        "Container " + containerID + " not found");
            }
            xmlWriter.endEntity ();
            xmlWriter.close ();
            out.flush ();

        } catch (IOException ioe) {
            logger.debug ("Error while serializing container to XML ", ioe);
        }
    }

    public void handleContainerListData (ProcessingContext jParams, Writer out)
            throws JahiaException {
        int containerListID = jParams.getContainerListID ();
        logger.debug ("Sending XML for container list " + containerListID);
        JahiaContainersService ctnSrv = ServicesRegistry.getInstance ()
                .getJahiaContainersService ();
        JahiaContainerList theContainerList = ctnSrv.loadContainerList (containerListID,
                LoadFlags.ALL, jParams);
        try {
            ((ParamBean)jParams).getResponse ().setContentType ("text/xml");
            out.write ("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
            XmlWriter xmlWriter = new XmlWriter (out);
            xmlWriter.writeEntity ("jahia");
            xmlWriter.enablePrettyPrint (true);
            if (theContainerList != null) {
                ctnSrv.serializeContainerListToXML (xmlWriter, new XMLSerializationOptions (),
                        containerListID, jParams);
            } else {
                xmlWriter.writeEntityWithText ("error",
                        "Container list " + containerListID + " not found");
            }
            xmlWriter.endEntity ();
            xmlWriter.close ();
            out.flush ();

        } catch (IOException ioe) {
            logger.debug ("Error while serializing container list to XML ", ioe);
        }
    }


    public void handlePageData (ProcessingContext jParams, Writer out)
            throws JahiaException {
        int pageID = jParams.getPageID ();
        logger.debug ("Sending XML for page " + pageID);
        ContentPage contentPage = ServicesRegistry.getInstance ().getJahiaPageService ()
                .lookupContentPage (pageID, true);
        try {
            ((ParamBean)jParams).getResponse ().setContentType ("text/xml");
            out.write ("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
            XmlWriter xmlWriter = new XmlWriter (out);
            xmlWriter.writeEntity ("jahia");
            xmlWriter.enablePrettyPrint (true);
            if (contentPage != null) {
                contentPage.serializeToXML (xmlWriter, new XMLSerializationOptions (), jParams);
            } else {
                xmlWriter.writeEntityWithText ("error", "Page " + pageID + " not found");
            }
            xmlWriter.endEntity ();
            xmlWriter.close ();
            out.flush ();

        } catch (IOException ioe) {
            logger.debug ("Error while serializing field to XML ", ioe);
        }
    }


    public void handleSiteData (ProcessingContext jParams, Writer out)
            throws JahiaException {

        JahiaSite site = jParams.getSite ();
        logger.debug ("Sending XML for site " + jParams.getSiteID ());
        try {
            ((ParamBean)jParams).getResponse ().setContentType ("text/xml");
            out.write ("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
            XmlWriter xmlWriter = new XmlWriter (out);
            xmlWriter.enablePrettyPrint (true);
            xmlWriter.writeEntity ("jahia");

            site.serializeToXML (xmlWriter, new XMLSerializationOptions (), jParams);
            xmlWriter.endEntity ();
            xmlWriter.close ();
            out.flush ();

        } catch (IOException ioe) {
            logger.debug ("Error while serializing field to XML ", ioe);
        }
    }

    public void transform (ProcessingContext jParams, String xmlInputSource, String xsltFileName,
                           Writer out)
            throws TransformerConfigurationException, TransformerException,
            FileNotFoundException, IOException {

        logger.debug (
                "Using XSLT transformation file " + xsltFileName +
                " to transform XML output...");

        String xslFilePath = jParams.settings().getPathResolver().resolvePath (
                "/WEB-INF/views/xslt/" + xsltFileName);
        if (xslFilePath == null) {
            logger.debug (
                    "Error while trying to resolve file name for XSLT file : " +
                    "/WEB-INF/views/xslt/" +
                    xsltFileName +
                    " to disk path using ServletContext.getRealPath() method call.");
            out.write ("Couldn't find XSLT file " + xsltFileName);
            return;
        }

        File xslFile = new File (xslFilePath);
        if (!xslFile.exists ()) {
            logger.debug ("Coulnd't find XSLT file " + xslFile.toString ());
            out.write ("Couldn't find XSLT file " + xslFile.toString ());
            return;
        }

        // 1. Instantiate a TransformerFactory.
        javax.xml.transform.TransformerFactory tFactory =
                javax.xml.transform.TransformerFactory.newInstance ();

        // 2. Use the TransformerFactory to process the stylesheet Source and
        //    generate a Transformer.
        javax.xml.transform.Transformer transformer = tFactory.newTransformer
                (new javax.xml.transform.stream.StreamSource (xslFile));

        // 3. Use the Transformer to transform an XML Source and send the
        //    output to a Result object.
        transformer.transform (
            new javax.xml.transform.stream.StreamSource (new StringReader (xmlInputSource)),
            new javax.xml.transform.stream.StreamResult (out));

    }

}
