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
//
//
//  JahiaXmlDocument
//
//  NK      29.01.2001
//
//

package org.jahia.data.xml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jahia.exceptions.JahiaException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 * An abstract class to handle Xml documents
 *
 * @author Khue ng
 * @version 1.0
 */
public abstract class JahiaXmlDocument {

    /** The xml Document **/
    //protected XmlDocument m_XMLDocument;
    protected Document m_XMLDocument;

    /** The Full Path to the xml file **/
    protected String m_DocPath;

    /**
     * Handle xml document using default parser behavior
     *
     * @param (String) path, the full path to a xml file
     */
    public JahiaXmlDocument (String docPath)
        throws JahiaException {
        m_DocPath = docPath;
        //File doc = new File(m_DocPath);

        try {
            loadFile(m_DocPath);
        } catch (Exception t) {
            throw new JahiaException("JahiaXmlDocument",
                                     "Exception while loading to the file" +
                                     m_DocPath + t.getMessage(),
                                     JahiaException.ERROR_SEVERITY,
                                     JahiaException.SERVICE_ERROR, t);
        }
    }

    /**
     * Handle xml document using a gived parser
     *
     * @param (String) path, the full path to a xml file
     * @param (Parser) parser, the parser to use
     */
    public JahiaXmlDocument (String docPath,
                             org.xml.sax.helpers.ParserAdapter parser)
        //com.sun.xml.parser.Parser parser)
        throws JahiaException {

        m_DocPath = docPath;
        //File doc = new File(m_DocPath);

        try {
            loadFile(m_DocPath);
        } catch (Exception t) {
            throw new JahiaException("JahiaXmlDocument",
                                     "Exception while loading to the file" +
                                     m_DocPath + t.getMessage(),
                                     JahiaException.ERROR_SEVERITY,
                                     JahiaException.SERVICE_ERROR, t);
        }
    }

    /**
     * To be override method. Codes to extract data from xml document.
     */
    public abstract void extractDocumentData ()
        throws JahiaException;

    /**
     * Write Document to the xml text file
     *
     */
    public void write ()
        throws JahiaException {

        try {
            saveFile(m_DocPath);
        } catch (Exception e) {
            throw new JahiaException("JahiaXmlDocument",
                                     "Exception while writing to the file: '" +
                                     m_DocPath + "'. Cause: " + e.getMessage(),
                                     JahiaException.ERROR_SEVERITY,
                                     JahiaException.SERVICE_ERROR, e);
        }

    }

    private void loadFile (String sourceFileName)
        throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();

        DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
        FileInputStream sourceStream = new FileInputStream(sourceFileName);
        m_XMLDocument = docBuilder.parse(sourceStream);
        m_XMLDocument.normalize(); // clean up DOM tree a little

        //JahiaConsole.println("JahiaXmlDocument.loadFile",sourceFileName + " loaded successfully");
    }

    private void saveFile (String destinationFileName)
        throws TransformerConfigurationException, FileNotFoundException,
        TransformerException {

        m_XMLDocument.normalize(); // cleanup DOM tree a little

        TransformerFactory tfactory = TransformerFactory.newInstance();

        // This creates a transformer that does a simple identity transform,
        // and thus can be used for all intents and purposes as a serializer.
        Transformer serializer = tfactory.newTransformer();

        serializer.setOutputProperty(OutputKeys.METHOD, "xml");
        serializer.setOutputProperty(OutputKeys.INDENT, "yes");
        FileOutputStream fileStream = new FileOutputStream(destinationFileName);

        serializer.transform(new DOMSource(m_XMLDocument),
                             new StreamResult(fileStream));

        try {
            fileStream.flush();
            fileStream.close();
            fileStream = null;
        } catch (IOException ioe) {
        }
    }

} // end JahiaXmlDocument
