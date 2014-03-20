/**
 * ==========================================================================================
 * =                        DIGITAL FACTORY v7.0 - Community Distribution                   =
 * ==========================================================================================
 *
 *     Rooted in Open Source CMS, Jahia's Digital Industrialization paradigm is about
 *     streamlining Enterprise digital projects across channels to truly control
 *     time-to-market and TCO, project after project.
 *     Putting an end to "the Tunnel effect", the Jahia Studio enables IT and
 *     marketing teams to collaboratively and iteratively build cutting-edge
 *     online business solutions.
 *     These, in turn, are securely and easily deployed as modules and apps,
 *     reusable across any digital projects, thanks to the Jahia Private App Store Software.
 *     Each solution provided by Jahia stems from this overarching vision:
 *     Digital Factory, Workspace Factory, Portal Factory and eCommerce Factory.
 *     Founded in 2002 and headquartered in Geneva, Switzerland,
 *     Jahia Solutions Group has its North American headquarters in Washington DC,
 *     with offices in Chicago, Toronto and throughout Europe.
 *     Jahia counts hundreds of global brands and governmental organizations
 *     among its loyal customers, in more than 20 countries across the globe.
 *
 *     For more information, please visit http://www.jahia.com
 *
 * JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION
 * ============================================
 *
 *     Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     "This program is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation; either version 2
 *     of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 *     As a special exception to the terms and conditions of version 2.0 of
 *     the GPL (or any later version), you may redistribute this Program in connection
 *     with Free/Libre and Open Source Software ("FLOSS") applications as described
 *     in Jahia's FLOSS exception. You should have received a copy of the text
 *     describing the FLOSS exception, and it is also available here:
 *     http://www.jahia.com/license"
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ==========================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.importexport;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.io.IOUtils;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.content.server.GWTFileManagerUploadServlet;
import org.jahia.exceptions.JahiaException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.decorator.JCRFileNode;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.sites.JahiaSite;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

import javax.jcr.RepositoryException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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

        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey((String) jobDataMap.get(
                JOB_SITEKEY));

        String uri = (String) jobDataMap.get(URI);
        if (uri != null) {
            JCRSessionWrapper session = ServicesRegistry.getInstance().getJCRStoreService().getSessionFactory().getCurrentUserSession();
            JCRFileNode f = (JCRFileNode) session.getNode(uri);
            String destinationParentPath = (String) jobDataMap.get(DESTINATION_PARENT_PATH);
            if (destinationParentPath == null) {
                // we are in the case of a site import
                if (f != null) {
                    File file = JCRContentUtils.downloadFileContent(f, File.createTempFile("import", ".zip"));
                    try {
                        ServicesRegistry.getInstance().getImportExportService().importSiteZip(
                                file == null ? null : new FileSystemResource(file), site, jobDataMap);
                        f.remove();
                        session.save();
                    } finally {
                        if (file != null) {
                            file.delete();
                        }
                    }
                }
            } else {
                // we are in the case of a regular content import.
                if (f != null) {
                    String filename = (String) jobDataMap.get(FILENAME);
                    File file = JCRContentUtils.downloadFileContent(f, File.createTempFile("import",
                            "." + FilenameUtils.getExtension(filename)));
                    if (jobDataMap.containsKey(REPLACE_CONTENT)) {
                        importContent(destinationParentPath, jobDataMap.getBoolean(REPLACE_CONTENT), file,
                                f.getFileContent().getContentType());
                    } else {
                        importContent(destinationParentPath, false, file, f.getFileContent().getContentType());
                    }
                    FileUtils.deleteQuietly(file);
                    f.remove();
                    session.save();
                }
            }
        } else {
            // we are in the case of a regular content import.
            String destinationParentPath = (String) jobDataMap.get(DESTINATION_PARENT_PATH);
            String fileKey = (String) jobDataMap.get(FILE_KEY);
            if (jobDataMap.containsKey(REPLACE_CONTENT)) {
                importContent(destinationParentPath, fileKey, jobDataMap.getBoolean(REPLACE_CONTENT));
            } else {
                importContent(destinationParentPath, fileKey, false);
            }
        }
    }

    public static void importContent(String parentPath, String fileKey, boolean replaceContent) throws Exception {
        GWTFileManagerUploadServlet.Item item = GWTFileManagerUploadServlet.getItem(fileKey);
        String contentType = ImportExportBaseService.detectImportContentType(item);
        try {
            if (replaceContent) {
                parentPath = StringUtils.substringBeforeLast(parentPath, "/");
            }
            importContent(parentPath, replaceContent, item.getFile(), contentType);
        } catch (Exception e) {
            logger.error("Error when importing", e);
            throw new GWTJahiaServiceException(e.getMessage());
        } finally {
            item.dispose();
        }
    }

    private static void importContent(String parentPath, boolean replaceContent, File file, String contentType)
            throws IOException, RepositoryException, JahiaException {
        ImportExportService importExport = ServicesRegistry.getInstance().getImportExportService();
        if ("application/zip".equals(contentType)) {
            importExport.importZip(parentPath, new FileSystemResource(file),
                    replaceContent ? DocumentViewImportHandler.ROOT_BEHAVIOUR_REPLACE : DocumentViewImportHandler.ROOT_BEHAVIOUR_REPLACE);

        } else if ("application/xml".equals(contentType) || "text/xml".equals(contentType)) {
            InputStream is = new FileInputStream(file);
            try {
                importExport.importXML(parentPath, is,
                        replaceContent ? DocumentViewImportHandler.ROOT_BEHAVIOUR_REPLACE : DocumentViewImportHandler.ROOT_BEHAVIOUR_REPLACE);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
    }

}