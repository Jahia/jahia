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

import org.apache.log4j.Logger;
import org.apache.poi.hpsf.*;
import org.apache.poi.poifs.eventfilesystem.POIFSReader;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderEvent;
import org.apache.poi.poifs.eventfilesystem.POIFSReaderListener;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 3 août 2007
 * Time: 15:15:56
 * To change this template use File | Settings | File Templates.
 */
public class OfficeExtractor implements Extractor {
    private static Logger logger = Logger.getLogger(OfficeExtractor.class);

    public Map<String, Object> extract(InputStream content) throws Exception {
        OfficePropertiesListener listener = new OfficePropertiesListener();
        try {
            POIFSReader r = new POIFSReader();
            r.registerListener(listener);
            r.read(content);
        } catch (Exception e) {
            throw new Exception("Exception while extracting properties in OfficeExtractor: " + e);
        }
        return listener.getProperties();
    }

    class OfficePropertiesListener implements POIFSReaderListener {

        private Map<String, Object> extractedProperties = new HashMap<String, Object>();

        public Map<String, Object> getProperties() {
            return extractedProperties;
        }

        public void processPOIFSReaderEvent(POIFSReaderEvent event) {
            PropertySet ps;
            try {
                ps = PropertySetFactory.create(event.getStream());
            } catch (NoPropertySetStreamException ex) {
                return;
            } catch (Exception ex) {
                throw new RuntimeException("Property set stream \"" + event.getPath() + event.getName() + "\": " + ex);
            }

            String prefix = "";

            if (ps.isDocumentSummaryInformation()) {
                prefix = "dsi";
            }

            if (ps.isSummaryInformation()) {
                prefix = "si";
            }

            List<?> sections = ps.getSections();

            for (Iterator<?> i = sections.iterator(); i.hasNext();) {
                Section sec = (Section) i.next();

                Property[] properties = sec.getProperties();
                for (int j = 0; j < properties.length; j++) {
                    Property property = properties[j];
                    String wellKnownPID = sec.getPIDString(property.getID());
                    Object o = property.getValue();
                    if (!wellKnownPID.equals("[undefined]")) {
                        extractedProperties.put(wellKnownPID.substring(4).toLowerCase(), o);
                        logger.info("Well known property : " + wellKnownPID.substring(4).toLowerCase() + " = "+ o);
                    } else if (sec.getProperty(0) != null) {
                        Map<Object, Object> dict = (Map<Object, Object>)sec.getProperty(0);
                        String s = (String) dict.get(new Long(property.getID()));
                        if (o instanceof String) {
                            try {
                                // fix poi string decoding
                                o = new String(o.toString().getBytes(),"UTF-8");
                            } catch (UnsupportedEncodingException e) {
                            }
                        }
                        extractedProperties.put(s, o);
                        logger.info("Property found in dictionary : " + s + " = "+ o);
                    } else {
                        extractedProperties.put(prefix+sec.getFormatID().toString()+property.getID(), o);
                        logger.info("Unknown property : " + prefix+sec.getFormatID().toString()+property.getID() + " = "+ o);
                    }
                }
            }
        }
    }

}
