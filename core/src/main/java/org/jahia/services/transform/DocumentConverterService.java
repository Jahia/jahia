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
package org.jahia.services.transform;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.core.document.DocumentFormat;
import org.jodconverter.core.document.DocumentFormatRegistry;
import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.jodconverter.remote.RemoteConverter;
import org.jodconverter.remote.office.RemoteOfficeManager;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Document transformation service that uses OpenOffice for file conversion.
 *
 * @author Fabrice Cantegrel
 * @author Sergiy Shyrkov
 */
public class DocumentConverterService implements ApplicationContextAware {

    protected static final Map<String, Object> DEF_PROPS = new HashMap<>();

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(DocumentConverterService.class);

    static {
        DEF_PROPS.put("Hidden", true);
        DEF_PROPS.put("ReadOnly", true);
    }

    private Map<String, Object> defaultLoadProperties = DEF_PROPS;
    private boolean enabled;
    private DocumentFormatRegistry formatRegistry = DefaultDocumentFormatRegistry.getInstance();

    private OfficeManager officeManager;

    private String officeManagerBeanName;

    private ApplicationContext applicationContext;

    /**
     * Converts the provided input file into output, considering provided
     * document formats.
     *
     * @param inputFile the source file
     * @param inputFormat description of the source file
     * @param outputFile the output file descriptor to store converted content
     *            into
     * @param outputFormat description of the output file
     * @throws OfficeException in case of a conversion error
     */
    public void convert(File inputFile, DocumentFormat inputFormat, File outputFile, DocumentFormat outputFormat) throws OfficeException {
        if (!isEnabled()) {
            return;
        }
        long startTime = System.currentTimeMillis();

        try {
            getDocumentConverter().convert(inputFile).as(inputFormat).to(outputFile).as(outputFormat).execute();
        } catch (OfficeException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage(), e);
            }
            throw e;
        } finally {
            if (logger.isDebugEnabled()) {
                logger.debug(
                        "Conversion from {} format to {} took {} ms",
                        new String[] {
                                inputFormat != null ? (inputFormat.getName() + " ("
                                        + inputFormat.getMediaType() + ")") : "",
                                outputFormat != null ? (outputFormat.getName() + " ("
                                        + outputFormat.getMediaType() + ")") : "",
                                String.valueOf(System.currentTimeMillis() - startTime) });
            }
        }
    }

    /**
     * Converts the provided input file into output automatically detecting file
     * mime-type by is extension.
     *
     * @param inputFile the source file
     * @param outputFile the output file descriptor to store converted content
     *            into
     * @throws OfficeException in case of a conversion error
     */
    public void convert(File inputFile, File outputFile) throws OfficeException {
        convert(inputFile, null, outputFile, null);
    }

    /**
     * Converts the provided input stream into output, considering provided
     * mime-types.
     *
     * @param inputStream the source stream
     * @param inputMimeType the input MIME type
     * @param outputStream the destination stream
     * @param outputMimeType the output MIME type
     * @throws OfficeException in case of a conversion error
     */
    public void convert(InputStream inputStream, String inputMimeType, OutputStream outputStream, String outputMimeType) throws OfficeException {

        if (!isEnabled()) {
            return;
        }
        File inputFile = null;
        File outputFile = null;
        try {

            inputFile = getFile(inputStream);
            // The outputFile required by the service
            outputFile = createTempFile();

            // convert inputFile to outputFile
            convert(inputFile, getFormatByMimeType(inputMimeType), outputFile, getFormatByMimeType(outputMimeType));

            // write the outputFileContent into outputStream
            writeToOutputStream(outputStream, outputFile);

        } catch (IOException ioe) {
            logger.warn("A problem occurred during the transformation", ioe);
        } finally {
            FileUtils.deleteQuietly(inputFile);
            FileUtils.deleteQuietly(outputFile);
        }
    }

     /**
     * Converts the provided file, considering provided mime-types.
     *
     * @param inputFile the source File
     * @param inputFileMimeType the source file content type
     * @param outputMimeType the output MIME type
     *
     * @return A File, which is inputFile converted into a mimeType defined by outputMimeType
     * @throws OfficeException in case of a conversion error
     */
    public File convert(File inputFile, String inputFileMimeType, String outputMimeType) throws IOException, OfficeException {

        if (!isEnabled()) {
            return null;
        }

        File outputFile = createTempFile();

        // convert inputFile to outputFile
        convert(inputFile, getFormatByMimeType(inputFileMimeType), outputFile,
                getFormatByMimeType(outputMimeType));

        return outputFile;
    }

    protected File createTempFile() throws IOException {
        // todo: use fileCleaningTracker
        return File.createTempFile("doc-converter", null);
    }

    public String getMimeType(String extension) {
        DocumentFormat df = formatRegistry.getFormatByExtension(extension);
        if (df == null) {
            return null;
        }
        return df.getMediaType();
    }

    public String getExtension(String mimeType) {
        DocumentFormat df = formatRegistry.getFormatByMediaType(mimeType);
        if (df == null) {
            return null;
        }
        return df.getExtension();
    }

    /**
     *
     * @param os  The outputStream to write into
     * @param file The file to read from
     */
    protected void writeToOutputStream(OutputStream os, File file) {

        InputStream is = null;
        try {
            is = new BufferedInputStream(new FileInputStream(file));
            IOUtils.copy(is, os);
        } catch (IOException ioe) {
            logger.warn("File " + file.getName() + " can't be written into outputStream", ioe);
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    @NotNull
    private DocumentConverter getDocumentConverter() {
        if (officeManager instanceof RemoteOfficeManager) {
            return RemoteConverter.make(officeManager);
        } else {
            return LocalConverter.make(officeManager);
        }
    }

    /**
     * Create a {@link File} from an {@link InputStream}
     * @param is The inputStream to read from
     * @return a {@link File} or null if given parameter is null.
     */
    private File getFile(InputStream is) {
        if (is == null) {
            return null;
        }

        File file = null;
        OutputStream os = null;

        try {
            file = createTempFile();
            os = new FileOutputStream(file);
            IOUtils.copy(is, os);
        } catch (IOException ioe) {
            logger.warn("inputStream from file " + (file != null?file.getName():null) + " can't be converted into file", ioe);
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
        }
        return file;
    }

    /**
     * Returns the {@link DocumentFormat} based of the extension of the
     * specified file.
     *
     * @param file the file to detect format for
     * @return the {@link DocumentFormat} based of the extension of the
     *         specified file
     */
    public DocumentFormat getFormat(File file) {
        return formatRegistry.getFormatByExtension(FilenameUtils.getExtension(file.getName()));
    }

    /**
     * Returns the {@link DocumentFormat} based on the mimeType of the
     * specified file.
     *
     * @param mimeType the mimeType to detect format for
     * @return the {@link DocumentFormat} based on a mimeType
     */
    public DocumentFormat getFormatByMimeType(String mimeType) {
        return formatRegistry.getFormatByMediaType(mimeType);
    }

    /**
     * Returns the {@link DocumentFormat} based on the extension extracted from the file name of the
     * specified file.
     *
     * @param fileName the file name to detect format for
     * @return the {@link DocumentFormat} based on a mimeType
     */
    public DocumentFormat getFormatByFileName(String fileName) {
        return formatRegistry.getFormatByExtension(FilenameUtils.getExtension(fileName));
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
    public void setDefaultLoadProperties(Map<String, Object> defaultLoadProperties) {
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
     * Initializes the service and starts office manager.
     *
     * @throws OfficeException in case of an initialization error
     */
    public void start() throws OfficeException {
        if (!isEnabled()) {
            return;
        }

        try {
            officeManager = (OfficeManager) applicationContext.getBean(officeManagerBeanName);
        } catch (Exception e) {
            logger.error("OfficeManager factory exception. Cause: " + e.getMessage(), e);
        }

        if (officeManager == null) {
            logger.warn("OfficeManager instance is not initialized correctly. Disabling service.");
            setEnabled(false);
            return;
        }

        logger.info("Starting OpenOffice manager...");

        try {
            officeManager.start();

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
                logger.info("Stopping OfficeManager...");
                officeManager.stop();
                logger.info("...OfficeManager successfully stopped.");
            } catch (Exception e) {
                logger.warn("Error stopping OfficeManager. Cause: " + e.getMessage(), e);
            }
        }
    }

    /**
     * @param officeManagerBeanName the officeManagerBeanName to set
     */
    public void setOfficeManagerBeanName(String officeManagerBeanName) {
        this.officeManagerBeanName = officeManagerBeanName != null ? officeManagerBeanName.trim() : null;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
