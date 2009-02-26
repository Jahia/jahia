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

package org.jahia.services.content.automation;

import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.pdfbox.cos.COSDictionary;
import org.pdfbox.cos.COSName;
import org.pdfbox.cos.COSString;
import org.pdfbox.exceptions.InvalidPasswordException;
import org.pdfbox.pdmodel.PDDocument;
import org.pdfbox.pdmodel.PDDocumentInformation;
import org.pdfbox.util.DateConverter;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 9 janv. 2008
 * Time: 15:22:04
 * To change this template use File | Settings | File Templates.
 */
public class PdfExtractor implements Extractor {
    private static org.apache.log4j.Logger logger =
        org.apache.log4j.Logger.getLogger(PdfExtractor.class);

    public Map<String, Object> extract(InputStream content) throws Exception {
        Map<String, Object> m = new HashMap<String, Object>();

        PDDocument pdfDocument = PDDocument.load(content);
        try {
            if( pdfDocument.isEncrypted() ) {
                //Just try using the default password and move on
                pdfDocument.decrypt( "" );
            }
        } catch (InvalidPasswordException e) {
            return m;
        }

        PDDocumentInformation info = pdfDocument.getDocumentInformation();
        COSDictionary dict = info.getDictionary();
        Iterator<?> iterator = dict.keyList().iterator();
        String val = null;
        while ( iterator.hasNext() ){
            try {
                COSName key = (COSName)iterator.next();
                logger.debug("Found Pdf property : key=" + key.getName());

                COSString value = (COSString)info.getDictionary().getDictionaryObject( key );
                if( value != null ) {
                    String s = value.getString();
                    Calendar c = DateConverter.toCalendar(s); // changes made for pdfbox 0.7.3
                    if (c == null) {
                        m.put(key.getName(), s);
                    } else {
                        m.put(key.getName(), c);                        
                    }
                }
                logger.debug("Found Pdf property : value=" + val);
            } catch (Exception t) {
                logger.debug("Error handling pdf properties", t);
            }
        }
        pdfDocument.close();
        return   m;
    }

}
