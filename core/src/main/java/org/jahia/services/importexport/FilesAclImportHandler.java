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
package org.jahia.services.importexport;

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

import javax.jcr.RepositoryException;
import java.util.StringTokenizer;

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
            JCRNodeWrapper f = JahiaWebdavBaseService.getInstance().getDAVFileAccess(path, jParams.getUser());
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
                logger.error("error", e);
            }
        }
    }
}
