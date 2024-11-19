/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2024 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.content.rules;

import org.apache.commons.codec.Charsets;
import org.apache.commons.lang.StringUtils;
import org.apache.jackrabbit.value.BinaryImpl;
import org.apache.tika.io.IOUtils;
import org.apache.tika.metadata.Metadata;
import org.drools.core.WorkingMemory;
import org.drools.core.spi.KnowledgeHelper;
import org.jahia.api.Constants;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRFileContent;
import org.jahia.services.textextraction.TextExtractionService;
import org.slf4j.Logger;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.nodetype.ConstraintViolationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Jahia text extraction service that uses Apache Tika parsers to extract content from documents.
 * User: toto
 * Date: Jan 9, 2009
 * Time: 6:33:45 PM
 */
public class ExtractionService {

    private static volatile ExtractionService instance;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ExtractionService.class);

    private JCRTemplate jcrTemplate;
    private TextExtractionService textExtractionService;
    private Map<String, String[]> mapping = new HashMap<String, String[]>();

    public static ExtractionService getInstance() {
        if (instance == null) {
            synchronized (ExtractionService.class) {
                if (instance == null) {
                    instance = new ExtractionService();
                }
            }
        }
        return instance;
    }

    /**
     * Performs a check if the provided node can be handled by currently
     * configured parsers.
     *
     * @param node the node to be checked
     * @return <code>true</code> if there is a parser that can handle provided
     *         node's content
     * @throws IOException in case of the read/write errors
     */
    public boolean canHandle(JCRNodeWrapper node) throws IOException {
        if (!textExtractionService.isEnabled()) {
            return false;
        }
        String mimeType = null;
        try {
            mimeType = node.getProperty(Constants.JCR_MIMETYPE).getString();
        } catch (PathNotFoundException e) {
            // ignore
        } catch (RepositoryException e) {
            logger.warn("Unable to get mime type for the node " + node.getPath() + ". Cause: " + e.getMessage(), e);
        }
        // no mime type detected -> skip it
        if (mimeType == null) {
            return false;
        }
        Metadata metadata = new Metadata();
        metadata.set(Metadata.CONTENT_TYPE, mimeType);

        // TODO check if it makes sense to also provide the stream

        return textExtractionService.canHandle(null, metadata);
    }

    /**
     * Extract properties from document
     *
     * @param node   file node in JCR
     * @param drools context of the rule engine
     * @throws Exception
     */
    public void extractProperties(AddedNodeFact node, KnowledgeHelper drools) throws Exception {
        if (!textExtractionService.isEnabled()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Text extraction service is disabled. Skip extracting properties for node " + node.getPath());
            }
            return;
        }

        AddedNodeFact contentNode = node.getContent();
        if (contentNode == null) {
            return;
        }

        String mimeType = null;
        try {
            mimeType = contentNode.getNode().getProperty(Constants.JCR_MIMETYPE).getString();
        } catch (Exception e) {
            logger.warn("Unable to detect mime type for node " + node.getPath(), e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Extracting properties for document " + node.getPath() + " with mime type '" + mimeType + "'");
        }
        Metadata metadata = new Metadata();
        if (mimeType != null) {
            metadata.set(Metadata.CONTENT_TYPE, mimeType);
        }

        InputStream stream = null;
        try {
            stream = contentNode.getNode().getProperty(Constants.JCR_DATA).getBinary().getStream();
            textExtractionService.extractMetadata(stream, metadata);
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.warn("Error extracting metadata for node " + node.getPath(), e);
            } else {
                logger.warn("Error extracting metadata for node " + node.getPath() + ". Cause: "
                        + e.getMessage()
                        + (e.getCause() != null ? " Original cause: " + e.getCause().getMessage() : ""));
            }
        } finally {
            IOUtils.closeQuietly(stream);
        }

        if (metadata.size() > 0) {
            try {
                WorkingMemory memory = drools.getWorkingMemory();
                for (String key : metadata.names()) {
                    if (!Metadata.CONTENT_TYPE.equals(key)) {
                        // TODO handle multivalue metadata properties
                        String value = metadata.get(key);
                        if (StringUtils.isNotBlank(value)) {
                            String[] mappedTo = mapping.get(key);
                            memory.insert(new ExtractedVariable(node.getPath(), key, value, mappedTo != null ? mappedTo[0] : null, mappedTo != null ? mappedTo[1] : null));
                        } else {
                            logger.debug("Ignoring blank value for metadata " + key);
                        }
                    }
                }
            } catch (Exception e) {
                logger.warn("Error extracting metadata for node " + node.getPath(), e);
            }
        }
    }

    /**
     * Synchronously extract text from document and store it as a property of either the file
     * node itself or the node referred to by the extractionNodePath parameter.
     * The node must have the following properties, which will be set:
     * j:extractedText, j:lastExtractionDate and for the case of extractionNodePath being passed
     * also j:originalUuid is required.
     *
     * @param provider           the JCR store provider
     * @param sourcePath         the path to the file node
     * @param extractionNodePath the optional path to the node receiving the extracted text
     * @throws IOException
     */
    public void extractText(final JCRStoreProvider provider, final String sourcePath, final String extractionNodePath) throws IOException {
        extractText(provider, sourcePath, extractionNodePath, Constants.EDIT_WORKSPACE);
    }

    /**
     * Synchronously extract text from document and store it as a property of either the file
     * node itself or the node referred to by the extractionNodePath parameter.
     * The node must have the following properties, which will be set:
     * j:extractedText, j:lastExtractionDate and for the case of extractionNodePath being passed
     * also j:originalUuid is required.
     *
     * @param provider           the JCR store provider
     * @param sourcePath         the path to the file node
     * @param extractionNodePath the optional path to the node receiving the extracted text
     * @param workspace          the workspace, where document was saved (default or live)
     * @throws IOException
     */
    public boolean extractText(final JCRStoreProvider provider, final String sourcePath, final String extractionNodePath, final String workspace) throws IOException {
        boolean textExtracted = false;
        if (!textExtractionService.isEnabled()) {
            return textExtracted;
        }
        try {
            textExtracted = jcrTemplate.doExecuteWithSystemSessionAsUser(null, workspace, null, new JCRCallback<Boolean>() {
                public Boolean doInJCR(JCRSessionWrapper session) throws RepositoryException {
                    boolean success = false;
                    JCRNodeWrapper file = session.getNode(sourcePath);
                    JCRFileContent fileContent = file.getFileContent();
                    Node n = file.getRealNode().getNode(Constants.JCR_CONTENT);

                    String type = fileContent.getContentType();

                    // jcr:encoding is not mandatory
                    String encoding = fileContent.getEncoding();

                    InputStream stream = fileContent.downloadFile();
                    try {
                        Metadata metadata = new Metadata();
                        metadata.set(Metadata.CONTENT_TYPE, type);
                        if (encoding != null) {
                            metadata.set(Metadata.CONTENT_ENCODING, encoding);
                        }
                        String content = textExtractionService.parse(stream, metadata);
                        if (extractionNodePath != null && extractionNodePath.length() > 0) {
                            n = session.getNode(extractionNodePath);
                            session.checkout(n);
                            try {
                                n.setProperty(Constants.ORIGINAL_UUID, file.getIdentifier());
                            } catch (UnsupportedRepositoryOperationException e) {
                                // ignore
                            }
                        } else {
                            session.checkout(n);
                        }
                        BinaryImpl contentValue = new BinaryImpl(content.getBytes(Charsets.UTF_8));
                        try {
                            n.setProperty(Constants.EXTRACTED_TEXT, contentValue);
                            n.setProperty(Constants.EXTRACTION_DATE, new GregorianCalendar());
                            session.save();
                            success = true;
                        } catch (ConstraintViolationException cve) {
                            logger.error("Extracted text property is not of the right type for node " + sourcePath + " please run patch associated for this conversion.", cve);
                        } finally {
                            contentValue.dispose();
                        }
                    } catch (Exception e) {
                        if (logger.isDebugEnabled()) {
                            logger.warn("Cannot extract content for node " + sourcePath, e);
                        } else {
                            logger.warn("Cannot extract content for node " + sourcePath + ". Cause: "
                                    + e.getMessage()
                                    + (e.getCause() != null ? " Original cause: " + e.getCause().getMessage() : ""));
                        }
                    } finally {
                        IOUtils.closeQuietly(stream);
                    }
                    return success;
                }
            });
        } catch (RepositoryException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Cannot extract content: " + e, e);
            } else {
                logger.warn("Cannot extract content: " + e);
            }
        }
        return textExtracted;
    }

    /**
     * Returns <code>true</code> if the text extraction service is activated.
     *
     * @return <code>true</code> if the text extraction service is activated
     */
    public boolean isEnabled() {
        return textExtractionService.isEnabled();
    }

    public void setJcrTemplate(JCRTemplate jcrTemplate) {
        this.jcrTemplate = jcrTemplate;
    }

    /**
     * @param textExtractionService the textExtractionService to set
     */
    public void setTextExtractionService(TextExtractionService textExtractionService) {
        this.textExtractionService = textExtractionService;
    }

    /**
     * @param mapping the mapping to set
     */
    public void setMapping(Map<String, String> mapping) {
        this.mapping = new HashMap<String, String[]>(mapping.size());
        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            this.mapping.put(entry.getKey(), StringUtils.split(entry.getValue(), '.'));
        }
    }
}
