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
