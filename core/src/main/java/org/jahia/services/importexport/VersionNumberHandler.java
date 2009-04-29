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

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jahia.services.version.EntryLoadRequest;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 9 janv. 2006
 * Time: 16:43:51
 * To change this template use File | Settings | File Templates.
 */
public class VersionNumberHandler extends DefaultHandler {
    private Map<String, EntryLoadRequest> uuidToVersions = new HashMap<String, EntryLoadRequest>();
    private List<Locale> locales;

    public VersionNumberHandler(List<Locale> locales) {
        this.locales = locales;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        String value = attributes.getValue("jahia:lastImportedVersion");
        if (value != null) {
            uuidToVersions.put(attributes.getValue("jahia:originalUuid"), new EntryLoadRequest(EntryLoadRequest.VERSIONED_WORKFLOW_STATE, Integer.parseInt(value), locales));
        }
    }

    public Map<String, EntryLoadRequest> getUuidToVersions() {
        return uuidToVersions;
    }
}
