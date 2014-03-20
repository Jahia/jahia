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

    private static ExtractionService instance;

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(ExtractionService.class);

    public static ExtractionService getInstance() {
        if (instance == null) {
            instance = new ExtractionService();
        }
        return instance;
    }

    protected static void convertPropertyToBinary(JCRNodeWrapper fileNode, BinaryImpl contentValue,
            JCRSessionWrapper session) throws RepositoryException {
        if (session.hasPendingChanges()) {
            session.save();
        }
        JCRNodeWrapper contentNodeCopy = null;
        JCRObservationManager.setEventListenersAvailableDuringPublishOnly(Boolean.TRUE);
        try {
            JCRNodeWrapper contentNode = fileNode.getNode(Constants.JCR_CONTENT);
            contentNode.copy(fileNode, Constants.JCR_CONTENT + "-temp", true);
            contentNodeCopy = fileNode.getNode(Constants.JCR_CONTENT + "-temp");
            contentNode.remove();
            session.move(contentNodeCopy.getPath(), fileNode.getPath() + "/" + Constants.JCR_CONTENT);
            session.save();
        } finally {
            JCRObservationManager.setEventListenersAvailableDuringPublishOnly(null);
        }

        JCRNodeWrapper contentNode = fileNode.getNode(Constants.JCR_CONTENT);
        contentNode.setProperty(Constants.EXTRACTED_TEXT, contentValue);
        contentNode.setProperty(Constants.EXTRACTION_DATE, new GregorianCalendar());
        session.save();
    }

    private JCRTemplate jcrTemplate;

    private TextExtractionService textExtractionService;

    private Map<String, String[]> mapping = new HashMap<String, String[]>();

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

        if (!textExtractionService.isEnabled()) {
            return;
        }
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
                        } catch (ConstraintViolationException cve) {
                            if (n.hasProperty(Constants.EXTRACTED_TEXT)) {
                                convertPropertyToBinary(file, contentValue, session);
                            }
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
