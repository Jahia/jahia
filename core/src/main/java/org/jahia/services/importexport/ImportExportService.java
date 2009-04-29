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

    void exportAll(OutputStream out, Map params, ProcessingContext jParams) throws JahiaException, SAXException, IOException;

    void exportSites(OutputStream outputStream, Map params, ProcessingContext processingContext, List sites) throws JahiaException, IOException, SAXException;

    void exportSite(JahiaSite jahiaSite, OutputStream out, ProcessingContext processingContext, Map params) throws JahiaException, SAXException, IOException;

    Document exportDocument(ContentObject object, String languageCode, ProcessingContext jParams, Map params) throws JahiaException, SAXException;

    void exportFile(ContentObject object, String languageCode, OutputStream out, ProcessingContext jParams, Map params) throws JahiaException, SAXException, IOException;

    void exportZip(ContentObject object, Set languageCodes, OutputStream out, ProcessingContext jParams, Map params) throws JahiaException, SAXException, IOException;

    void export(ContentObject object, String languageCodes, ContentHandler h, Set files, ProcessingContext jParams, Map params) throws JahiaException, SAXException;

    void exportCategories(OutputStream out, ProcessingContext jParams) throws JahiaException, SAXException, IOException;

    void exportVersions(OutputStream out, ProcessingContext jParams) throws JahiaException, SAXException, IOException;

    // Imports

    ContentObject importFile(ContentObject parent, ProcessingContext jParams, InputStream inputStream, boolean setUuid, List<ImportAction> actions, ExtendedImportResult result) throws IOException;

    ContentObject importFile(ContentObject parent, ProcessingContext jParams, File file, boolean setUuid, List<ImportAction> actions, ExtendedImportResult result) throws IOException;

    ContentObject importDocument(ContentObject parent, String lang, ProcessingContext jParams, InputStream inputStream, boolean updateOnly, boolean setUuid, List<ImportAction> actions, ExtendedImportResult result, Map<String, String> uuidMapping, Map<String, String> pathMapping, Map<String, Map<String, String>> typeMapping, Map<String, String> tplMapping, Map<String, String> importedMapping);

    void importCategories(ProcessingContext jParams, InputStream is);

    // Copy

    ContentObject copy(ContentObject source, ContentObject parentDest, ProcessingContext jParams, EntryLoadRequest loadRequest, String link, List<ImportAction> actions, ExtendedImportResult result);

    ContentObject copy(ContentObject source, ContentObject parentDest, Set languages, ProcessingContext jParams, EntryLoadRequest loadRequest, String link, List<ImportAction> actions, ExtendedImportResult result);

    boolean isCompatible(JahiaContainerDefinition dest, JahiaContainerDefinition source);

    boolean isCompatible(JahiaContainerDefinition dest, ContentContainer source, ProcessingContext context);

    boolean isPicker(ContentObject object) throws JahiaException;

    void getFilesForField(ContentObject object, ProcessingContext jParams, String language, EntryLoadRequest loadRequest, Set files) throws JahiaException;

    void ensureFile(String path, InputStream inputStream, String type, ProcessingContext jParams, JahiaSite destSite, Map<String,String> pathMapping);

    WebdavResource exportToSite(JahiaSite site, String targetName, Date exportTime, String username, String password, JahiaUser member, String sitename, boolean withMetadata, boolean withWorkflow, boolean withAcl, boolean publishAtEnd) throws IOException, JahiaException, SAXException;

    public void startProductionJob(JahiaSite site, ProcessingContext jParams) throws ParseException;

    public String getUuid(ContentObject object) throws JahiaException;

}
