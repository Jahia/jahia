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
package org.jahia.services.importexport;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: May 26, 2008
 * Time: 10:59:42 AM
 * To change this template use File | Settings | File Templates.
 */
public class FilesDateImportHandler extends DefaultHandler {
    
    private static final transient Logger logger = Logger
            .getLogger(FilesDateImportHandler.class);
    
    private Map<String, Date> fileToDate = new HashMap<String, Date>();
    private DateFormat df = new SimpleDateFormat(ImportExportService.DATE_FORMAT);

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        String value = attributes.getValue("jahia:lastModification");
        if (value != null) {
            try {
                fileToDate.put(attributes.getValue("jahia:path"), df.parse(value));
            } catch (ParseException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public Map<String, Date> getFileToDate() {
        return fileToDate;
    }
}
