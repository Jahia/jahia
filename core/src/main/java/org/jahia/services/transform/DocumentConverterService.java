/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2010 Jahia Solutions Group SA. All rights reserved.
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

package org.jahia.services.transform;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.artofsolving.jodconverter.StandardConversionTask;
import org.artofsolving.jodconverter.document.DefaultDocumentFormatRegistry;
import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.document.DocumentFormatRegistry;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeException;
import org.artofsolving.jodconverter.office.OfficeManager;
import org.artofsolving.jodconverter.office.OfficeTask;

/**
 * Document transformation service that uses OpenOffice for file conversion.
 * 
 * @author Sergiy Shyrkov
 */
public class DocumentConverterService {

    protected static final Map<String, Object> DEF_PROPS = new HashMap<String, Object>(2);

    private static Logger logger = Logger.getLogger(DocumentConverterService.class);

    static {
        DEF_PROPS.put("Hidden", true);
        DEF_PROPS.put("ReadOnly", true);
    }

    private Map<String, ?> defaultLoadProperties = DEF_PROPS;
    private boolean enabled;

    private DocumentFormatRegistry formatRegistry = new DefaultDocumentFormatRegistry();

    private OfficeManager officeManager;

    /**
     * Converts the provided input file into output, considering provided
     * document formats.
     * 
     * @param inputFile the source file
     * @param inputFormat description of the source file
     * @param outputFile the output file descriptor to store converted content
     *            into
     * @param outputFormat description of the output file
     */
    public void convert(File inputFile, DocumentFormat inputFormat, File outputFile, DocumentFormat outputFormat) {
        officeManager.execute(getConversionTask(inputFile, inputFormat, outputFile, outputFormat));
    }

    /**
     * Converts the provided input file into output automatically detecting file
     * mime-type by is extension.
     * 
     * @param inputFile the source file
     * @param outputFile the output file descriptor to store converted content
     *            into
     */
    public void convert(File inputFile, File outputFile) {
        convert(inputFile, null, outputFile, null);
    }

    /**
     * Converts the provided input stream into output, considering provided
     * mime-types.
     * 
     * @param inputStream the source stream
     * @param inputMimeType the source MIME type
     * @param outputStream the destination stream
     * @param outputMimeType the output MIME type
     */
    public void convert(InputStream inputStream, String inputMimeType, OutputStream outputStream, String outputMimeType) {
        // TODO implement me
    }

    /**
     * Converts the provided input file into output, considering provided
     * document formats.
     * 
     * @param inputFile the source file
     * @param inputFormat description of the source file
     * @param outputFile the output file descriptor to store converted content
     *            into
     * @param outputFormat description of the output file
     */
    protected OfficeTask getConversionTask(File inputFile, DocumentFormat inputFormat, File outputFile,
            DocumentFormat outputFormat) {
        StandardConversionTask officeTask = new StandardConversionTask(inputFile, outputFile,
                outputFormat != null ? outputFormat : getFormat(outputFile));
        officeTask.setDefaultLoadProperties(defaultLoadProperties);
        officeTask.setInputFormat(inputFormat != null ? inputFormat : getFormat(inputFile));
        return officeTask;
    }

    /**
     * Returns the {@link DocumentFormat} based of the extension of the
     * specified file.
     * 
     * @param file the file to detect format for
     * @return the {@link DocumentFormat} based of the extension of the
     *         specified file
     */
    protected DocumentFormat getFormat(File file) {
        return formatRegistry.getFormatByExtension(FilenameUtils.getExtension(file.getName()));
    }

    /**
     * @return the enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param defaultLoadProperties the defaultLoadProperties to set
     */
    public void setDefaultLoadProperties(Map<String, ?> defaultLoadProperties) {
        this.defaultLoadProperties = defaultLoadProperties;
    }

    /**
     * @param enabled the enabled to set
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Initializes an instance of the document format registry.
     * 
     * @param formatRegistry an instance of the document format registry
     */
    public void setFormatRegistry(DocumentFormatRegistry formatRegistry) {
        this.formatRegistry = formatRegistry;
    }

    /**
     * Injects an instance of the office Manager
     * 
     * @param officeManager the officeManager to set
     */
    public void setOfficeManager(OfficeManager officeManager) {
        this.officeManager = officeManager;
    }

    /**
     * Initializes the service and starts office manager.
     * 
     * @throws OfficeException in case of an initialization error
     */
    public void start() throws OfficeException {
        if (!isEnabled()) {
            return;
        }
        try {
            if (officeManager == null) {
                officeManager = new DefaultOfficeManagerConfiguration().setOfficeHome(
                        "c:\\Program Files (x86)\\OpenOffice.org 3").buildOfficeManager();
                // throw new
                // OfficeException("OfficeManager instance is not initialized");
            }

            logger.info("Starting OpenOffice manager...");

            officeManager.start();

            // convert(new File("c:\\Downloads\\Manfrotto-20070629.doc"),
            // new File("c:\\Downloads\\Manfrotto-20070629.pdf"));

            logger.info("...OpenOffice manager started.");
        } catch (Exception e) {
            logger.error("Error starting document converter service. Cause: " + e.getMessage(), e);
        }
    }

    /**
     * Shuts down the office manager.
     * 
     * @throws OfficeException in case of a shutdown error
     */
    public void stop() throws OfficeException {
        if (isEnabled() && officeManager != null) {
            try {
                officeManager.stop();
            } catch (Exception e) {
                logger.warn("Error stopping OfficeManager. Cause: " + e.getMessage(), e);
            }
        }
    }
}
