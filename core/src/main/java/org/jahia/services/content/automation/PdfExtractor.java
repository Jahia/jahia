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
