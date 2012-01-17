/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.importexport;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.tika.io.IOUtils;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.content.server.GWTFileManagerUploadServlet;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.sites.JahiaSite;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;

import java.io.File;
import java.io.InputStream;

/**
 * Background job for performing an import of the JCR content.
 * Date: 25 oct. 2005 - 16:34:07
 *
 * @author toto
 * @version $Id$
 */
public class ImportJob extends BackgroundJob {

    private final static Logger logger = LoggerFactory.getLogger(ImportJob.class);

    public static final String TARGET = "target";
    public static final String CONTENT_TYPE = "contentType";
    public static final String PUBLISH_ALL_AT_END = "publishAllAtEnd";
    public static final String URI = "uri";
    public static final String FILE_KEY = "fileKey";
    public static final String DESTINATION_PARENT_PATH = "destParentPath";
    public static final String FILENAME = "filename";
    public static final String DELETE_FILE = "delete";
    public static final String ORIGINATING_JAHIA_RELEASE = "originatingJahiaRelease";

    public static final String COPY_TO_JCR = "copyToJCR";
    public static final String REPLACE_CONTENT = "replaceContent";

    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {
        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();

        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey((String) jobDataMap.get(JOB_SITEKEY));

        String uri = (String) jobDataMap.get(URI);
        if (uri != null) {
            // we are in the case of a site import
            JCRSessionWrapper session = ServicesRegistry.getInstance().getJCRStoreService().getSessionFactory().getCurrentUserSession();
            JCRNodeWrapper f = session.getNode(uri);

            if (f != null) {
                File file = JCRContentUtils.downloadFileContent(f, File.createTempFile("import", ".zip"));
                try {
                    ServicesRegistry.getInstance().getImportExportService().importSiteZip(file, site, jobDataMap);
                    f.remove();
                    session.save();
                } finally {
                    file.delete();
                }
            }
        } else {
            // we are in the case of a regular content import.
            String destinationParentPath = (String) jobDataMap.get(DESTINATION_PARENT_PATH);
            String fileKey = (String) jobDataMap.get(FILE_KEY);
            if(jobDataMap.containsKey(REPLACE_CONTENT)) {
                importContent(destinationParentPath, fileKey, jobDataMap.getBoolean(REPLACE_CONTENT));
            } else {
                importContent(destinationParentPath, fileKey);
            }
        }
    }

    public static void importContent(String parentPath, String fileKey) throws Exception {
        importContent(parentPath, fileKey, false);
    }

    public static void importContent(String parentPath, String fileKey, boolean replaceContent) throws Exception {
        ImportExportService importExport = ServicesRegistry.getInstance().getImportExportService();
        GWTFileManagerUploadServlet.Item item = GWTFileManagerUploadServlet.getItem(fileKey);
        String contentType = ImportExportBaseService.detectImportContentType(item);
        try {
            if(replaceContent) {
                parentPath = StringUtils.substringBeforeLast(parentPath,"/");
            }
            if ("application/zip".equals(contentType)) {
                try {
                    importExport.importZip(parentPath, item.getFile(),
                            replaceContent ? DocumentViewImportHandler.ROOT_BEHAVIOUR_REPLACE : DocumentViewImportHandler.ROOT_BEHAVIOUR_REPLACE);
                } finally {
                    item.dispose();
                }
            } else if ("application/xml".equals(contentType) || "text/xml".equals(contentType)) {
                InputStream is = item.getStream();
                try {
                    importExport.importXML(parentPath, is,
                            replaceContent ? DocumentViewImportHandler.ROOT_BEHAVIOUR_REPLACE : DocumentViewImportHandler.ROOT_BEHAVIOUR_REPLACE);
                } finally {
                    IOUtils.closeQuietly(is);
                    item.dispose();
                }
            } else {
                item.dispose();
            }
        } catch (Exception e) {
            logger.error("Error when importing", e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

}