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

import java.util.StringTokenizer;

import javax.jcr.RepositoryException;
import javax.transaction.Status;

import org.apache.log4j.Logger;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.usermanager.JahiaGroup;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.webdav.JahiaWebdavBaseService;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 6 juil. 2005
 * Time: 17:31:05
 * To change this template use File | Settings | File Templates.
 */
public class FilesAclImportHandler extends DefaultHandler {
    private static Logger logger = Logger.getLogger(FilesAclImportHandler.class);

    private ProcessingContext jParams;

    public FilesAclImportHandler(ProcessingContext jParams) {
        this.jParams = jParams;
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if ("file".equals(localName) && ImportExportBaseService.JAHIA_URI.equals(uri)) {
            String path = attributes.getValue(ImportExportBaseService.JAHIA_URI, "path");
            String acl = attributes.getValue(ImportExportBaseService.JAHIA_URI, "fileacl");
            JCRNodeWrapper f = JahiaWebdavBaseService.getInstance ().getDAVFileAccess (path, jParams.getUser());
            if (f.isValid()) {
                try {
                    if (acl != null && acl.length() > 0) {
                        StringTokenizer st = new StringTokenizer(acl, "|");
                        while (st.hasMoreElements()) {
                            String s = st.nextToken();
                            int beginIndex = s.lastIndexOf(":");

                            String principal = s.substring(0, beginIndex);
                            String userName = principal.substring(2);
                            String value = null;

                                if (principal.charAt(0) == 'u') {
                                    JahiaUser user = ServicesRegistry
                                            .getInstance()
                                            .getJahiaSiteUserManagerService()
                                            .getMember(jParams.getSiteID(),
                                                    userName);
                                    if (user != null) {
                                        value = "/users/" + user.getUsername();
                                    }
                                } else {
                                    JahiaGroup group = ServicesRegistry
                                            .getInstance()
                                            .getJahiaGroupManagerService()
                                            .lookupGroup(jParams.getSiteID(),
                                                    userName);
                                    if (group != null) {
                                        value = "+/groups/"
                                                + group.getGroupname()
                                                + "/members";
                                    }
                                }

                            if (value != null) {
                                f.changePermissions(value, s
                                        .substring(beginIndex + 1));
                            }
                        }
                    }
                    for (int i = 0; i < attributes.getLength(); i++) {
                        String attUri = attributes.getURI(i);
                        String attName = attributes.getLocalName(i);
                        if (!ImportExportBaseService.JAHIA_URI.equals(attUri)
                                || (!"path".equals(attName) && !"fileacl".equals(attName) && !"lastModification".equals(attName))) {
                            String attValue = attributes.getValue(i);
                            try {
                                f.setProperty(attUri, attName, attValue);
                            } catch (RepositoryException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    try {
                        f.save();
                    } catch (RepositoryException e) {
                        logger.error("error",e);                        
                    }
                } finally {
                    if (f.getTransactionStatus() == Status.STATUS_ACTIVE) {
                        try {
                            f.refresh(false);
                        } catch (RepositoryException e) {
                            logger.error("error",e);

                        }
                    }
                }                    
            }
        }
    }
}
