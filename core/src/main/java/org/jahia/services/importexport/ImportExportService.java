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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.webdav.lib.WebdavResource;
import org.jahia.content.ContentObject;
import org.jahia.data.containers.JahiaContainerDefinition;
import org.jahia.exceptions.JahiaException;
import org.jahia.params.ProcessingContext;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.version.EntryLoadRequest;
import org.jahia.services.containers.ContentContainer;
import org.jahia.services.content.JCRNodeWrapper;
import org.w3c.dom.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 13 d�c. 2004
 * Time: 12:21:48
 * To change this template use File | Settings | File Templates.
 */
public interface ImportExportService {
    String JCR_URI ="http://www.jcp.org/jcr/1.0";
    String NT_URI="http://www.jcp.org/jcr/nt/1.0";
    String PT_URI="http://www.jcp.org/jcr/pt/1.0";
    String SV_URI="http://www.jcp.org/jcr/sv/1.0";
    String JAHIA_URI="http://www.jahia.org/";
    String JAHIAGED_URI="GED:";
    String NS_URI = "http://www.w3.org/2000/xmlns/";
    String DAV_URI = "DAV:";
    String PRODUCTION_TARGET_LIST_PROPERTY = "prod_target_list";
    String PRODUCTION_CRON_PROPERTY = "prod_cron_";
    String PRODUCTION_USERNAME_PROPERTY = "prod_username_";
    String PRODUCTION_PASSWORD_PROPERTY = "prod_password_";
    String PRODUCTION_PROFILE_PROPERTY = "prod_profile_";
    String PRODUCTION_SITE_NAME_PROPERTY = "prod_sitename_";
    String PRODUCTION_ALIAS_PROPERTY = "prod_alias_";
    String PRODUCTION_METADATA_PROPERTY = "prod_metadata_";
    String PRODUCTION_WORKFLOW_PROPERTY = "prod_workflow_";
    String PRODUCTION_ACL_PROPERTY = "prod_acl_";
    String PRODUCTION_AUTO_PUBLISH_PROPERTY = "prod_autopublish_";
    String PRODUCTION_TARGET = "prod_target_site_";

    String REMOVED_STATUS = "removed";
    String UNCHANGED_STATUS = "unchanged";
    String ADDED_STATUS = "added";
    String UPDATED_STATUS = "updated";

    String FROM = "from";
    String TO = "to";
    String LOCK_KEY = "lock";
    String INCLUDE_TEMPLATES = "templates";
    String INCLUDE_SITE_INFOS = "siteinfos";
    String INCLUDE_FILES = "files";
    String INCLUDE_ALL_FILES = "allfiles";
    String INCLUDE_DEFINITIONS = "definitions";
    String LINK = "link";
    String VIEW_CONTENT = "content";
    String VIEW_VERSION = "version";
    String VIEW_METADATA = "metadata";
    String VIEW_JAHIALINKS = "links";
    String VIEW_PICKERS = "pickers";
    String VIEW_ACL = "acl";
    String VIEW_WORKFLOW = "wf";
    String VIEW_PID = "pid";
    String INCLUDED = "included";
    String FILES_DATE = "filesdate";

    String EXPORT_FORMAT = "format";
    String LEGACY_EXPORTER = "legacy";
    String SYSTEM_EXPORTER = "sys";
    String DOCUMENT_EXPORTER = "doc";

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    // Export

    void exportAll(OutputStream out, Map<String, Object> params, ProcessingContext jParams) throws JahiaException, SAXException, IOException;

    void exportSites(OutputStream outputStream, Map<String, Object> params, ProcessingContext processingContext, List<JahiaSite> sites) throws JahiaException, IOException, SAXException;

    void exportSite(JahiaSite jahiaSite, OutputStream out, ProcessingContext processingContext, Map<String, Object> params) throws JahiaException, SAXException, IOException;

    Document exportDocument(ContentObject object, String languageCode, ProcessingContext jParams, Map<String, Object> params) throws JahiaException, SAXException;

    void exportFile(ContentObject object, String languageCode, OutputStream out, ProcessingContext jParams, Map<String, Object> params) throws JahiaException, SAXException, IOException;

    void exportZip(ContentObject object, Set<String> languageCodes, OutputStream out, ProcessingContext jParams, Map<String, Object> params) throws JahiaException, SAXException, IOException;

    void export(ContentObject object, String languageCodes, ContentHandler h, Set<JCRNodeWrapper> files, ProcessingContext jParams, Map<String, Object> params) throws JahiaException, SAXException;

    void exportVersions(OutputStream out, ProcessingContext jParams) throws JahiaException, SAXException, IOException;

    // Imports

    ContentObject importFile(ContentObject parent, ProcessingContext jParams, InputStream inputStream, boolean setUuid, List<ImportAction> actions, ExtendedImportResult result) throws IOException;

    ContentObject importFile(ContentObject parent, ProcessingContext jParams, File file, boolean setUuid, List<ImportAction> actions, ExtendedImportResult result) throws IOException;

    ContentObject importDocument(ContentObject parent, String lang, ProcessingContext jParams, InputStream inputStream, boolean updateOnly, boolean setUuid, List<ImportAction> actions, ExtendedImportResult result, Map<String, String> uuidMapping, Map<String, String> pathMapping, Map<String, Map<String, String>> typeMapping, Map<String, String> tplMapping, Map<String, String> importedMapping);

    void importCategories(ProcessingContext jParams, InputStream is);

    // Copy

    ContentObject copy(ContentObject source, ContentObject parentDest, ProcessingContext jParams, EntryLoadRequest loadRequest, String link, List<ImportAction> actions, ExtendedImportResult result);

    ContentObject copy(ContentObject source, ContentObject parentDest, Set<String> languages, ProcessingContext jParams, EntryLoadRequest loadRequest, String link, List<ImportAction> actions, ExtendedImportResult result);

    boolean isCompatible(JahiaContainerDefinition dest, JahiaContainerDefinition source);

    boolean isCompatible(JahiaContainerDefinition dest, ContentContainer source, ProcessingContext context);

    boolean isPicker(ContentObject object) throws JahiaException;

    void getFilesForField(ContentObject object, ProcessingContext jParams, String language, EntryLoadRequest loadRequest, Set<JCRNodeWrapper> files) throws JahiaException;

    void ensureFile(String path, InputStream inputStream, String type, ProcessingContext jParams, JahiaSite destSite, Map<String,String> pathMapping);

    WebdavResource exportToSite(JahiaSite site, String targetName, Date exportTime, String username, String password, JahiaUser member, String sitename, boolean withMetadata, boolean withWorkflow, boolean withAcl, boolean publishAtEnd) throws IOException, JahiaException, SAXException;

    public void startProductionJob(JahiaSite site, ProcessingContext jParams) throws ParseException;

    public String getUuid(ContentObject object) throws JahiaException;

}
