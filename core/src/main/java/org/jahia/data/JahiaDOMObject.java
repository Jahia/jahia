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
//  JahiaDOMObject
//
//  NK      25.07.2001
//

package org.jahia.data;

import java.io.FileOutputStream;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jahia.exceptions.JahiaException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * A DOM Object typically contains a DOM Document
 *
 * @author Khue Nguyen
 * @version 1.0
 */
public abstract class JahiaDOMObject {


    protected Document xmlDoc = null;


    //--------------------------------------------------------------------------
    /**
     * save the document to a file
     *
     * @param String destFileName
     */
    public void save (String destFileName) throws JahiaException{
        saveFile(destFileName);
    }

    //--------------------------------------------------------------------------
    /**
     * return a reference to the DOM Document
     *
     * @return Document the DOM Document
     */
    public Document getDocument () {
        return xmlDoc;
    }

    //--------------------------------------------------------------------------
    /**
     * return a DOM as string
     *
     * @return Document the DOM Document
     */
    public String toString () {
        Element el = (Element)xmlDoc.getDocumentElement();
        if (  el != null ){
            return el.toString();
        }
        return null;
    }

    //--------------------------------------------------------------------------
    private void saveFile(String destinationFileName)
    throws JahiaException {

        try {

            xmlDoc.normalize(); // cleanup DOM tree a little

            TransformerFactory tfactory = TransformerFactory.newInstance();

            // This creates a transformer that does a simple identity transform,
            // and thus can be used for all intents and purposes as a serializer.
            Transformer serializer = tfactory.newTransformer();

            serializer.setOutputProperty(OutputKeys.METHOD, "xml");
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            FileOutputStream fileStream = new FileOutputStream(destinationFileName);
            serializer.transform(new DOMSource(xmlDoc),
                             new StreamResult(fileStream));

        } catch ( Exception e ){
            throw new JahiaException(  "XMLPortlets",
                                        "Exception " + e.getMessage(),
                                        JahiaException.ERROR_SEVERITY,
                                        JahiaException.SERVICE_ERROR, e);
        }

    }

}
