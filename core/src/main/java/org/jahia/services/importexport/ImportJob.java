/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2023 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
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
import org.apache.tika.io.IOUtils;
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

    public static final String TARGET = "target";
    public static final String CONTENT_TYPE = "contentType";
    public static final String PUBLISH_ALL_AT_END = "publishAllAtEnd";
    public static final String URI = "uri";
    public static final String DESTINATION_PARENT_PATH = "destParentPath";
    public static final String FILENAME = "filename";
    public static final String DELETE_FILE = "delete";
    public static final String ORIGINATING_JAHIA_RELEASE = "originatingJahiaRelease";

    public static final String COPY_TO_JCR = "copyToJCR";
    public static final String REPLACE_CONTENT = "replaceContent";

    @Override
    public void executeJahiaJob(JobExecutionContext jobExecutionContext) throws Exception {

        JobDetail jobDetail = jobExecutionContext.getJobDetail();
        JobDataMap jobDataMap = jobDetail.getJobDataMap();

        JahiaSite site = ServicesRegistry.getInstance().getJahiaSitesService().getSiteByKey((String) jobDataMap.get(JOB_SITEKEY));

        String uri = (String) jobDataMap.get(URI);
        JCRSessionWrapper session = ServicesRegistry.getInstance().getJCRStoreService().getSessionFactory().getCurrentUserSession();
        JCRFileNode f = (JCRFileNode) session.getNode(uri);
        String destinationParentPath = (String) jobDataMap.get(DESTINATION_PARENT_PATH);
        if (destinationParentPath == null) {
            // we are in the case of a site import
            if (f != null) {
                File file = JCRContentUtils.downloadFileContent(f, File.createTempFile("import", ".zip"));
                try {
                    if (file != null) {
                        ServicesRegistry.getInstance().getImportExportService()
                                .importSiteZip(new FileSystemResource(file), site, jobDataMap);
                    }
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
                    importContent(destinationParentPath, jobDataMap.getBoolean(REPLACE_CONTENT), file, f.getFileContent().getContentType());
                } else {
                    importContent(destinationParentPath, false, file, f.getFileContent().getContentType());
                }
                FileUtils.deleteQuietly(file);
                f.remove();
                session.save();
            }
        }
    }

    private static void importContent(String parentPath, boolean replaceContent, File file, String contentType) throws IOException, RepositoryException, JahiaException {
        ImportExportService importExport = ServicesRegistry.getInstance().getImportExportService();
        switch (contentType) {
            case "application/zip":
                importExport.importZip(parentPath, new FileSystemResource(file), DocumentViewImportHandler.ROOT_BEHAVIOUR_REPLACE);
                break;
            case "application/xml":
            case "text/xml":
                InputStream is = new FileInputStream(file);
                try {
                    importExport.importXML(parentPath, is, DocumentViewImportHandler.ROOT_BEHAVIOUR_REPLACE);
                } finally {
                    IOUtils.closeQuietly(is);
                }
                break;
        }
    }

}
