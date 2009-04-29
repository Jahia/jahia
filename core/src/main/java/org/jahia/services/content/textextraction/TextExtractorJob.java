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
package org.jahia.services.content.textextraction;

import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.core.query.lucene.JackrabbitTextExtractor;
import org.apache.jackrabbit.extractor.TextExtractor;
import org.apache.log4j.Logger;
import org.jahia.api.Constants;
import org.jahia.hibernate.manager.JahiaFieldXRefManager;
import org.jahia.hibernate.manager.SpringContextSingleton;
import org.jahia.hibernate.model.JahiaFieldXRef;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRStoreProvider;
import org.jahia.services.fields.ContentField;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.search.JahiaSearchService;
import org.jahia.services.search.indexingscheduler.RuleEvaluationContext;
import org.quartz.JobExecutionContext;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 29 janv. 2008
 * Time: 13:58:32
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
                            n.setProperty(Constants.EXTRACTION_DATE, new GregorianCalendar());
                            n.save();
                        } finally {
                            reader.close();
                        }
                        triggerJahiaFileReferenceReindexation(n.getParent(), provider, processingContext);
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
    
    private void triggerJahiaFileReferenceReindexation(Node n,
            JCRStoreProvider provider, ProcessingContext processingContext)
            throws Exception {
        JahiaSearchService searchService = ServicesRegistry.getInstance()
                .getJahiaSearchService();
        JahiaFieldXRefManager fieldXRefManager = (JahiaFieldXRefManager) SpringContextSingleton
                .getInstance().getContext().getBean(
                        JahiaFieldXRefManager.class.getName());
        Collection<JahiaFieldXRef> c = fieldXRefManager
                .getReferencesForTarget(JahiaFieldXRefManager.FILE
                        + provider.getKey() + ":" + n.getUUID());
        for (JahiaFieldXRef xref : c) {
            try {
                ContentField contentObject = ContentField.getField(xref
                        .getComp_id().getFieldId());
                RuleEvaluationContext ctx = new RuleEvaluationContext(
                        contentObject.getObjectKey(), contentObject,
                        processingContext, processingContext.getUser());
                searchService.indexContentObject(contentObject,
                        processingContext.getUser(), ctx);
            } catch (Exception e) {
                logger.warn("Error when starting re-indexation. Field "
                        + xref.getComp_id().getFieldId()
                        + " was not re-indexed.", e);
            }
        }
    }
}
