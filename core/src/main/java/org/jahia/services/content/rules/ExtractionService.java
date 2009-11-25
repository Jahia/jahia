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
package org.jahia.services.content.rules;

import org.drools.spi.KnowledgeHelper;
import org.drools.WorkingMemory;
import org.jahia.api.Constants;
import org.jahia.params.ProcessingContext;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRFileContent;
import org.jahia.services.content.textextraction.TextExtractionListener;
import org.jahia.services.content.textextraction.TextExtractorJob;
import org.jahia.services.scheduler.BackgroundJob;
import org.jahia.services.scheduler.SchedulerService;
import org.jahia.services.usermanager.JahiaUser;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.apache.commons.io.IOUtils;
import org.apache.jackrabbit.core.query.lucene.JackrabbitTextExtractor;
import org.apache.jackrabbit.extractor.TextExtractor;
import org.apache.log4j.Logger;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Jan 9, 2009
 * Time: 6:33:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExtractionService {
    private final static String TEXTEXTRACT_TYPE = "jnt:textExtract";
    private final static String TEXTEXTRACTS_TYPE = "jnt:textExtracts";
    private final static String TEXTEXTRACTIONS_BASE_NODE_NAME = "textextracts";
    
    private static Logger logger = Logger.getLogger(Service.class);
    private static ExtractionService instance;

    private Map<String, Extractor> extractors;
    
    private String textFilterClasses = null;

    private static TextExtractor extractor = null;
    private JCRTemplate jcrTemplate;
    public static synchronized ExtractionService getInstance() {
        if (instance == null) {
            instance = new ExtractionService();
        }
        return instance;
    }

    public ExtractionService() {
    }

    /**
     * Get file property extractors configured via Spring
     * @return map with content-type key and as value the extractor
     */
    public Map<String, Extractor> getExtractors() {
        return extractors;
    }

    /**
     * Set file property extractors configured via Spring
     * @param extractors map with content-type key and as value the extractor
     */
    public void setExtractors(Map<String, Extractor> extractors) {
        this.extractors = extractors;
    }

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    /**
     * Extract properties from document
     * @param node file node in JCR
     * @param drools context of the rule engine
     * @throws Exception
     */
    public void extractProperties(NodeWrapper node, KnowledgeHelper drools) throws Exception{
        if (!node.getNode().hasNode(Constants.JCR_CONTENT)) {
            return;
        }
        Node contentNode = node.getNode().getNode(Constants.JCR_CONTENT);
        Extractor extractor = (Extractor) extractors.get(node.getMimeType());
        if (extractor != null) {
            try {
                Map<String, Object> m = extractor.extract(contentNode.getProperty(Constants.JCR_DATA).getStream());
                if (m != null) {
                    WorkingMemory memory = drools.getWorkingMemory();
                    for (Map.Entry<String, Object> entry : m.entrySet()) {
                        if (entry.getKey() != null) {
                            String s = entry.getKey().replace("-","_");
                            memory.insert(new ExtractedVariable(node,s,entry.getValue()));
                        }
                    }
                }
            } catch (Exception e) {
                logger.debug("Error when extracting properties from "+node.getName(),e);
            }
        }
    }

    /**
     * Ask extractor to return a list of extractable content types
     * @return list with extractable content types
     */
    public List<String> getContentTypes() {
        return Arrays.asList(getExtractor().getContentTypes());
    }

    /**
     * Synchronously extract text from document and store it as a property of either the file 
     * node itself or the node referred to by the extractionNodePath parameter.
     * The node must have the following properties, which will be set:
     * j:extractedText, j:lastExtractionDate and for the case of extractionNodePath being passed
     * also j:originalUuid is required. 
     * @param provider the JCR store provider
     * @param sourcePath the path to the file node
     * @param extractionNodePath the optional path to the node receiving the extracted text
     * @param context the Jahia processing context
     * @throws IOException
     */
    public void extractText(final JCRStoreProvider provider, final String sourcePath, final String extractionNodePath,
            final ProcessingContext context) throws IOException {
        try {
            jcrTemplate.doExecuteWithSystemSession(new JCRCallback<Object>() {
                public Object doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JCRNodeWrapper file = session.getNode(sourcePath);
                JCRFileContent fileContent = file.getFileContent();
                Node n = file.getRealNode().getNode(Constants.JCR_CONTENT);

                String type = fileContent.getContentType();

                // jcr:encoding is not mandatory
                String encoding = fileContent.getEncoding();

                InputStream stream = fileContent.downloadFile();
                try {
                    final Reader reader = getExtractor().extractText(stream, type, encoding);
                    try {
                        if (extractionNodePath != null && extractionNodePath.length() > 0) {
                            n = session.getNode(extractionNodePath);
                            n.setProperty(Constants.ORIGINAL_UUID, file.getStorageName());
                        }
                        n.setProperty(Constants.EXTRACTED_TEXT, new InputStream() {
                            byte[] temp;

                            int i = 0;

                            public int read() throws IOException {
                                if (temp == null || i >= temp.length) {
                                    char cb[] = new char[1];
                                    if (reader.read(cb, 0, 1) == -1) {
                                        return -1;
                                    }
                                    temp = new String(cb).getBytes("UTF-8");
                                    i = 0;
                                }
                                if (temp[0] >= 0 && temp[0] <= 31) {
                                    i++;
                                    return 32; // if char is 31 or less (0
                                    // generates an error) it is
                                    // replace with space (32)
                                }
                                return temp[i++];
                            }
                        });
                        n.setProperty(Constants.EXTRACTION_DATE, new GregorianCalendar());
                        n.save();
                        session.save();
                    } finally {
                        reader.close();
                    }
                } catch (Exception e) {
                    logger.debug("Cannot extract content", e);
                } finally {
                    IOUtils.closeQuietly(stream);
                }
                return null;
                }
            });
        } catch (RepositoryException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Cannot extract content: " + e.getMessage(), e);
            } else {
                logger.warn("Cannot extract content: " + e.getMessage());
            }
        }
    }

    /**
     * Get all active text filter classes (separated by comma) set via Spring
     * @return active text filter classes (separated by comma)
     */
    public String getTextFilterClasses() {
        return textFilterClasses;
    }

    /**
     * Set all active text filter classes (separated by comma) set via Spring
     * @param textFilterClasses active text filter classes (separated by comma)
     */
    public void setTextFilterClasses(String textFilterClasses) {
        this.textFilterClasses = textFilterClasses;
    }

    /**
     * Return the text extractor service singleton
     * @return text extractor service singleton
     */
    public TextExtractor getExtractor() {
        if (extractor == null) {
            extractor = new JackrabbitTextExtractor(getTextFilterClasses());
        }
        return extractor;
    }

    /**
     * Get the already extracted text from the node. For files within Jahia's Jackrabbit
     * content repository, text extractions are done via event listeners and stored within 
     * the file node. For files accessed via mounted external repositories (via United Content 
     * Bus) the text extractions are stored in a separate sub-folder of the JCR and if the file
     * is accessed for the first time, the node to receive the extraction is created and the 
     * extraction is triggered asynchronously via Quartz. The Quartz job will extract the text 
     * and store it as property in the created node and also trigger a re-indexation of all 
     * Jahia content referring to that file.
     * Notice that this method will return null for texts, which have not been extracted yet.  
     * @param file file node in JCR
     * @param jParams Jahia processing context
     * @return extracted text from document or null if not extracted yet
     */
    public String getExtractedText(final JCRNodeWrapper file, ProcessingContext jParams) {

        if (isExtractedByEventListener(file)) {
            return file.getFileContent().getExtractedText();
        } else {
            String extractedText = null;
            if (getContentTypes().contains(file.getFileContent().getContentType())) {
                final boolean[] triggerExtraction = new boolean[]{false};
                final JCRNodeWrapper[] n = new JCRNodeWrapper[]{null};
                try {
                    extractedText = jcrTemplate.doExecuteWithSystemSession(new JCRCallback<String>() {
                        public String doInJCR(JCRSessionWrapper session) throws RepositoryException {
                            JCRNodeWrapper textExtractionNode = findTextExtractionNode(session);
                            try {
                                n[0] = (JCRNodeWrapper) textExtractionNode.getNode(file.getPath());
                            } catch (PathNotFoundException ex) {
                            }
                            if (n[0] == null) {
                                for (String pathElement : file.getPath().split("/")) {
                                    if (pathElement.length() > 0) {
                                        if (!textExtractionNode.hasNode(pathElement)) {
                                            textExtractionNode = textExtractionNode.addNode(pathElement,
                                                                                            !pathElement.equals(
                                                                                                    file.getName()) ? TEXTEXTRACTS_TYPE : TEXTEXTRACT_TYPE);
                                            triggerExtraction[0] = true;
                                        } else {
                                            textExtractionNode = (JCRNodeWrapper) textExtractionNode.getNode(
                                                    pathElement);
                                        }
                                    }
                                }
                                n[0] = textExtractionNode;
                            }
                            if (triggerExtraction[0]) {
                                session.save();
                            }
                            if (!triggerExtraction[0] && n[0].hasProperty(Constants.EXTRACTION_DATE)) {
                                Date lastModified = file.getLastModifiedAsDate();
                                Date extractionDate = new Date(n[0].getProperty(
                                        Constants.EXTRACTION_DATE).getDate().getTimeInMillis());
                                if (extractionDate.before(lastModified)) {
                                    triggerExtraction[0] = true;
                                }
                            }
                            if (!triggerExtraction[0]) {
                                return n[0].getProperty(Constants.EXTRACTED_TEXT).getString();
                            }
                            return "";
                        }
                    });
                } catch (RepositoryException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Cannot extract content: " + e.getMessage(), e);
                    } else {
                        logger.warn("Cannot extract content: " + e.getMessage());
                    }
                }
                try {
                    if (triggerExtraction[0]) {
                        JahiaUser member = jParams.getUser();

                        jParams = new ProcessingContext(org.jahia.settings.SettingsBean.getInstance(),
                                                        System.currentTimeMillis(), null, member, null);
                        jParams.setCurrentLocale(Locale.getDefault());

                        JobDetail jobDetail = BackgroundJob.createJahiaJob("Text extraction for " + file.getName(),
                                                                           TextExtractorJob.class, jParams);

                        SchedulerService schedulerServ = ServicesRegistry.getInstance().getSchedulerService();
                        JobDataMap jobDataMap;
                        jobDataMap = jobDetail.getJobDataMap();
                        jobDataMap.put(TextExtractorJob.PROVIDER, file.getProvider().getMountPoint());
                        jobDataMap.put(TextExtractorJob.PATH, file.getPath());
                        jobDataMap.put(TextExtractorJob.NAME, file.getName());
                        jobDataMap.put(TextExtractorJob.EXTRACTNODE_PATH, n[0].getPath());
                        jobDataMap.put(BackgroundJob.JOB_TYPE, TextExtractorJob.EXTRACTION_TYPE);
                        schedulerServ.scheduleJobNow(jobDetail);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            return extractedText;
        }
    }

    private boolean isExtractedByEventListener(JCRNodeWrapper file) {
//        boolean listenerFound = false;
        return file.getFileContent().getExtractedText() != null;
//        if (file.getProvider().getListeners() != null) {
//            for (List<DefaultEventListener> listeners : file.getProvider().getListeners().values()) {
//                for (DefaultEventListener eventListener : listeners) {
//                    if (eventListener instanceof TextExtractionListener) {
//                        listenerFound = true;
//                        break;
//                    }
//                }
//                if (listenerFound) {
//                    break;
//                }
//            }
//        }
//        return listenerFound;
    }

    private JCRNodeWrapper findTextExtractionNode(JCRSessionWrapper session) {
        JCRNodeWrapper node = null;
        try {
            JCRNodeWrapper rootNode = (JCRNodeWrapper) session.getRootNode();
            try {
                node = (JCRNodeWrapper) rootNode.getNode(TEXTEXTRACTIONS_BASE_NODE_NAME);
            } catch (PathNotFoundException e) {
                node = rootNode.addNode(TEXTEXTRACTIONS_BASE_NODE_NAME, TEXTEXTRACTS_TYPE);
            }
        } catch (RepositoryException e) {
            logger.error(e.toString(), e);
        }
        return node;
    }
}
