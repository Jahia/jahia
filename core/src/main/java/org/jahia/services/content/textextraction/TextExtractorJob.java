/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */

package org.jahia.services.content.textextraction;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.core.query.lucene.JackrabbitTextExtractor;
import org.apache.jackrabbit.extractor.TextExtractor;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.services.scheduler.BackgroundJob;
import org.quartz.JobExecutionContext;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 29 janv. 2008
 * Time: 13:58:32
 * To change this template use File | Settings | File Templates.
 */
public class TextExtractorJob extends BackgroundJob {
    private final static Logger logger = Logger.getLogger(TextExtractorJob.class);
    public static final String EXTRACTION_TYPE = "textextraction";
    public static final String PATH = "path";
    public static final String NAME = "name";
    public static final String PROVIDER = "provider";

    private static TextExtractor extractor = new JackrabbitTextExtractor(
                        "org.apache.jackrabbit.extractor.MsWordTextExtractor," +
                                "org.apache.jackrabbit.extractor.MsExcelTextExtractor," +
                                "org.apache.jackrabbit.extractor.MsPowerPointTextExtractor," +
                                "org.apache.jackrabbit.extractor.PdfTextExtractor," +
                                "org.apache.jackrabbit.extractor.OpenOfficeTextExtractor," +
                                "org.apache.jackrabbit.extractor.RTFTextExtractor," +
                                "org.apache.jackrabbit.extractor.HTMLTextExtractor," +
                                "org.apache.jackrabbit.extractor.XMLTextExtractor,"+
                                "org.apache.jackrabbit.extractor.PlainTextExtractor"
                );

    public static List<String> getContentTypes() {
        return Arrays.asList(extractor.getContentTypes());
    }

    public void executeJahiaJob(JobExecutionContext jobExecutionContext, ProcessingContext processingContext) throws Exception {
        String providerPath = (String) jobExecutionContext.getJobDetail().getJobDataMap().get("provider");

        String path = (String) jobExecutionContext.getJobDetail().getJobDataMap().get("path");

        JCRStoreProvider provider = (JCRStoreProvider) ServicesRegistry.getInstance().getJCRStoreService().getMountPoints().get(providerPath);

        try {
            Session s = provider.getSystemSession();
            try {
                Property p = (Property) s.getItem(path);
                Node n = p.getParent();


                if (n.hasProperty(Constants.JCR_MIMETYPE)) {
                    String type = n.getProperty(Constants.JCR_MIMETYPE).getString();

                    // jcr:encoding is not mandatory
                    String encoding = null;
                    if (n.hasProperty(Constants.JCR_ENCODING)) {
                        encoding = n.getProperty(Constants.JCR_ENCODING).getString();
                    }

                    InputStream stream = p.getStream();
                    try {
                        final Reader reader = extractor.extractText(stream, type, encoding);
                        try {
                            n.setProperty(Constants.EXTRACTED_TEXT, new InputStream() {
                                byte[] temp;
                                int i=0;
                                public int read() throws IOException {
                                    if (temp == null || i>=temp.length) {
                                        char cb[] = new char[1];
                                        if (reader.read(cb, 0, 1) == -1) {
                                            return -1;
                                        }
                                        temp = new String(cb).getBytes("UTF-8");
                                        i = 0;
                                    }
                                    return temp[i++];
                                }
                            });
                            n.save();
                        } finally {
                            reader.close();
                        }
                    } catch (Exception e) {
                        logger.debug("Cannot extract content",e);
                    } finally {
                        IOUtils.closeQuietly(stream);
                    }
                }

            } finally {
                s.logout();
            }
        } catch (RepositoryException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Cannot extract content: " + e.getMessage(), e);
            } else {
                logger.warn("Cannot extract content: " + e.getMessage());
            }
        }


    }
}
