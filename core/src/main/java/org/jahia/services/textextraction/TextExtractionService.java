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

package org.jahia.services.textextraction;

import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.IOUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.CompositeParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.WriteOutContentHandler;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

/**
 * Jahia metadata and text extraction service that uses Apache Tika for parsing
 * documents.
 * 
 * @author Sergiy Shyrkov
 */
public class TextExtractionService implements InitializingBean {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(TextExtractionService.class);

    private static CompositeParser configureParser(Resource config, boolean autoDetectType) {
        CompositeParser parser = null;
        InputStream stream = null;
        try {
            stream = config.getInputStream();
            if (autoDetectType) {
                parser = new AutoDetectParser(new TikaConfig(stream));
            } else {
                TikaConfig cfg = new TikaConfig(stream);
                parser = (CompositeParser) cfg.getParser();
            }
        } catch (Exception e) {
            logger.error("Error initializing text extraction service. Service will be disabled. Cause: "
                    + e.getMessage(), e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return parser;
    }

    /**
     * Performs the content extraction and fills in related metadata for the
     * specified document stream. <br>
     * The given document stream is consumed but not closed by this method. The
     * responsibility to close the stream remains on the caller.
     * 
     * 
     * @param parser the parser to be used
     * @param stream the document stream to be parsed
     * @param metadata the metadata containing parser specific information, like
     *            content type, encoding etc.
     * @param characterLimit the maximum number of characters to extract or -1
     *            to extract full document content
     * @return the text representation of the extracted content or
     *         <code>null</code> if the service is disabled
     * @throws IOException in case of the read/write errors
     * @throws SAXException in case of parsing errors
     * @throws TikaException in case of parsing errors
     */
    private static String doParse(CompositeParser parser, InputStream stream, Metadata metadata, int characterLimit)
            throws IOException, SAXException, TikaException {
        long startTime = System.currentTimeMillis();
        if (logger.isDebugEnabled()) {
            logger.debug("Start text extraction using metadata: " + metadata);
        }
        WriteOutContentHandler handler = new WriteOutContentHandler(characterLimit);
        try {
            parser.parse(stream, new BodyContentHandler(handler), metadata, new ParseContext());
        } catch (SAXException e) {
            if (handler.isWriteLimitReached(e)) {
                if (characterLimit > 0) {
                    logger.info("Document content length exceeded the configured limit. Extracted first "
                            + characterLimit + " characters.");
                }
            } else {
                throw e;
            }
        }
        String extractedText = handler.toString();
        if (logger.isDebugEnabled()) {
            logger.debug("Text extraction finished in " + (System.currentTimeMillis() - startTime) + " ms. Extracted "
                    + extractedText.length() + " characters.");
            logger.debug("Extracted metadata: " + metadata);
            if (logger.isTraceEnabled()) {
                logger.trace("Extracted text:\n" + extractedText);
            }
        }
        return extractedText;
    }

    private boolean autoDetectType = true;

    private Resource config;

    private Resource configMetadata;

    private boolean enabled = true;

    private int maxExtractedCharacters = 100000;

    private CompositeParser parser;

    private CompositeParser parserMetadata;

    public void afterPropertiesSet() throws Exception {
        if (!enabled) {
            logger.info("Text extraction service is disabled");
            return;
        }

        logger.info("Starting the text extraction service...");

        if (!config.exists() || !configMetadata.exists()) {
            logger.error("Text extraction configuration cannot be found. Disabling the service.");
            enabled = false;
            return;
        }

        parser = configureParser(config, autoDetectType);
        if (enabled && (parser == null || parser.getParsers().isEmpty())) {
            logger.error("No parsers have been found for text extraction service in the configuration '"
                    + config.getDescription() + "'. Disabling service.");
            enabled = false;
        }
        if (!enabled) {
            parser = null;
        }

        if (enabled) {
            logger.info("Initialized text extraction parser using " + config);
            if (!config.equals(configMetadata)) {
                parserMetadata = configureParser(configMetadata, autoDetectType);
                logger.info("Initialized metadata extraction parser using " + configMetadata);
            } else {
                // use same parser
                parserMetadata = parser;
                logger.info("Using same parser for metadata");
            }
        }
    }

    /**
     * Performs a check if the provided content can be handled by currently
     * configured parsers. <br>
     * The given document stream is consumed but not closed by this method. The
     * responsibility to close the stream remains on the caller.
     * 
     * 
     * @param stream the document stream to be parsed; can be null, in this case
     *            the contentType only will be used to detect appropriate parser
     * @param metadata the metadata containing parser specific information, like
     *            content type, encoding etc.
     * @return <code>true</code> if there is a parser that can handle provided
     *         content
     * @throws IOException in case of the read/write errors
     */
    public boolean canHandle(InputStream stream, Metadata metadata) throws IOException {
        if (!isEnabled()) {
            return false;
        }
        MediaType contentMediaType = null;
        if (parser instanceof AutoDetectParser) {
        	contentMediaType = ((AutoDetectParser) parser).getDetector().detect(stream, metadata);
        }
        if (contentMediaType == null) {
        	String contentType = metadata.get(Metadata.CONTENT_TYPE);
        	contentMediaType = contentType != null ? new MediaType(StringUtils.substringBefore(contentType, "/"), StringUtils.substringAfter(contentType, "/")) : null;
        }

        return contentMediaType != null ? parser.getParsers().containsKey(contentMediaType) : false;
    }

    /**
     * Performs the metadata extraction for the specified document stream. <br>
     * The given document stream is consumed but not closed by this method. The
     * responsibility to close the stream remains on the caller.
     * 
     * 
     * @param stream the document stream to be parsed
     * @param metadata the metadata containing parser specific information, like
     *            content type, encoding etc.
     * @throws IOException in case of the read/write errors
     * @throws SAXException in case of parsing errors
     * @throws TikaException in case of parsing errors
     */
    public void extractMetadata(InputStream stream, Metadata metadata) throws IOException, SAXException, TikaException {
        if (!enabled) {
            if (logger.isDebugEnabled()) {
                logger.debug("Text extraction service is disabled. Skipping metadata extraction.");
            }
        }
        // TODO check if for HTML file parsing the approach with setting character limit to 0
        // actually works.
        // Otherwise use DefaultHandler as suggested in http://tika.markmail.org/message/hecedrucfge4evo2
        doParse(parserMetadata, stream, metadata, 0);
    }

    /**
     * Returns <code>true</code> if the text extraction service is activated.
     * 
     * @return <code>true</code> if the text extraction service is activated
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Performs the content extraction and fills in related metadata for the
     * specified document stream. <br>
     * The given document stream is consumed but not closed by this method. The
     * responsibility to close the stream remains on the caller.
     * 
     * 
     * @param stream the document stream to be parsed
     * @param metadata the metadata containing parser specific information, like
     *            content type, encoding etc.
     * @return the text representation of the extracted content or
     *         <code>null</code> if the service is disabled
     * @throws IOException in case of the read/write errors
     * @throws SAXException in case of parsing errors
     * @throws TikaException in case of parsing errors
     */
    public String parse(InputStream stream, Metadata metadata) throws IOException, SAXException, TikaException {
        return parse(stream, metadata, maxExtractedCharacters);
    }

    /**
     * Performs the content extraction and fills in related metadata for the
     * specified document stream. <br>
     * The given document stream is consumed but not closed by this method. The
     * responsibility to close the stream remains on the caller.
     * 
     * 
     * @param stream the document stream to be parsed
     * @param metadata the metadata containing parser specific information, like
     *            content type, encoding etc.
     * @param characterLimit the maximum number of characters to extract or -1
     *            to extract full document content
     * @return the text representation of the extracted content or
     *         <code>null</code> if the service is disabled
     * @throws IOException in case of the read/write errors
     * @throws SAXException in case of parsing errors
     * @throws TikaException in case of parsing errors
     */
    public String parse(InputStream stream, Metadata metadata, int characterLimit) throws IOException, SAXException,
            TikaException {
        if (!enabled) {
            if (logger.isDebugEnabled()) {
                logger.debug("Text extraction service is disabled. Returning null.");
            }
            return null;
        }
        return doParse(parser, stream, metadata, characterLimit);
    }

    /**
     * Performs the content extraction for the specified document stream. <br>
     * The given document stream is consumed but not closed by this method. The
     * responsibility to close the stream remains on the caller.
     * 
     * 
     * @param stream the document stream to be parsed
     * @param contentType the content type of the provided document
     * @return the text representation of the extracted content or
     *         <code>null</code> if the service is disabled
     * @throws IOException in case of the read/write errors
     * @throws SAXException in case of parsing errors
     * @throws TikaException in case of parsing errors
     */
    public String parse(InputStream stream, String contentType) throws IOException, SAXException, TikaException {
        Metadata metadata = new Metadata();
        metadata.set(Metadata.CONTENT_TYPE, contentType);
        return parse(stream, metadata);
    }

    /**
     * @param autoDetectType the autoDetectType to set
     */
    public void setAutoDetectType(boolean autoDetectType) {
        this.autoDetectType = autoDetectType;
    }

    /**
     * Provides the Tika configuration resource.
     * 
     * @param config the Tika configuration resource
     */
    public void setConfig(Resource config) {
        this.config = config;
    }

    /**
     * @param configMetadata the configMetadata to set
     */
    public void setConfigMetadata(Resource configMetadata) {
        this.configMetadata = configMetadata;
    }

    /**
     * Set this flag to <code>true</code> to enable the text extraction service.
     * 
     * @param enabled the flag to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @param maxExtractedCharacters the maxExtractedCharacters to set
     */
    public void setMaxExtractedCharacters(int maxExtractedCharacters) {
        this.maxExtractedCharacters = maxExtractedCharacters;
    }
}
