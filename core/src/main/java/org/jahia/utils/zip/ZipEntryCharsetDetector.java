/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.utils.zip;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.services.content.decorator.JCRFileContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Charset detector for the ZIP file entries.
 *
 * @author Sergiy Shyrkov
 */
public final class ZipEntryCharsetDetector {

    private static volatile List<Charset> charsetTryChain;

    public static final String ZIP_ENTRY_ALTERNATIVE_ENCODING = "jahia.zipEntry.alternativeEncoding";
    private static Logger logger = LoggerFactory.getLogger(ZipEntryCharsetDetector.class);

    private static boolean canRead(File file, Charset charset) throws IOException {
        boolean canRead = true;
        ZipFile zip = null;
        try {
            zip = charset != null ? new ZipFile(file, charset) : new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            try {
                while (entries.hasMoreElements()) {
                    entries.nextElement();
                }
            } catch (IllegalArgumentException e) {
                canRead = false;
            }
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }

        return canRead;
    }

    private static boolean canRead(InputStream is, Charset charset) throws IOException {
        File f = File.createTempFile(UUID.randomUUID() + ".zip", "");
        FileUtils.copyInputStreamToFile(is, f);

        try {
            return canRead(f, charset);
        } finally {
            FileUtils.deleteQuietly(f);
        }
    }

    /**
     * Returns the detected character set, which is suitable to read the entries of the provided ZIP file properly. The set of charsets to
     * try can be specified by the system property {@link #ZIP_ENTRY_ALTERNATIVE_ENCODING} and also by the corresponding jahia.properties
     * entry. If the method fails to detect the charset it returns <code>null</code>.
     *
     * @param zipFile
     *            the ZIP file to read entries from
     * @return the detected character set, which is suitable to read the entries of the provided ZIP file properly or <code>null</code> if
     *         we fail to detect the charset
     */
    public static Charset detect(File zipFile) {
        try {
            for (Charset c : getCharsetTryChain()) {
                if (canRead(zipFile, c)) {
                    return c;
                }
            }
        } catch (IOException e) {
            logger.warn("Error checking charset for the file " + zipFile, e);
        }

        logger.warn("Unable to find a charset to read the entries of a provided ZIP file: {}", zipFile);

        return null;
    }

    /**
     * Returns the detected character set, which is suitable to read the entries of the provided ZIP file properly. The set of charsets to
     * try can be specified by the system property {@link #ZIP_ENTRY_ALTERNATIVE_ENCODING} and also by the corresponding jahia.properties
     * entry. If the method fails to detect the charset it returns <code>null</code>.
     *
     * @param zipFileResettableInputStream
     *            a ZIP file stream that has proper support for {@link InputStream#reset()} method, e.g. is a {@link ByteArrayInputStream}
     * @return the detected character set, which is suitable to read the entries of the provided ZIP file properly or <code>null</code> if
     *         we fail to detect the charset
     */
    public static Charset detect(InputStream resettableInputStream) {
        try {
            for (Charset c : getCharsetTryChain()) {
                try {
                    if (canRead(resettableInputStream, c)) {
                        return c;
                    }
                } finally {
                    resettableInputStream.reset();
                }
            }
        } catch (IOException e) {
            logger.warn("Error checking charset for the input stream", e);
        }

        logger.warn("Unable to find a charset to read the entries of a provided ZIP file stream");

        return null;
    }

    /**
     * Returns the detected character set, which is suitable to read the entries of the provided ZIP file properly. The set of charsets to
     * try can be specified by the system property {@link #ZIP_ENTRY_ALTERNATIVE_ENCODING} and also by the corresponding jahia.properties
     * entry. If the method fails to detect the charset it returns <code>null</code>.
     *
     * @param zipFile
     *            the ZIP file to read entries from
     * @return the detected character set, which is suitable to read the entries of the provided ZIP file properly or <code>null</code> if
     *         we fail to detect the charset
     */
    public static Charset detect(JCRFileContent zipFileNode) {
        try {
            for (Charset c : getCharsetTryChain()) {
                InputStream is = null;
                try {
                    is = zipFileNode.downloadFile();
                    if (canRead(is, c)) {
                        return c;
                    }
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }
        } catch (IOException e) {
            logger.warn("Error checking charset for the file node", e);
        }

        return null;
    }

    /**
     * Returns the detected character set, which is suitable to read the entries of the provided ZIP resource properly. The set of charsets
     * to try can be specified by the system property {@link #ZIP_ENTRY_ALTERNATIVE_ENCODING} and also by the corresponding jahia.properties
     * entry. If the method fails to detect the charset it returns <code>null</code>.
     *
     * @param resourceUrl
     *            the URL of the resource to open a stream from
     * @return the detected character set, which is suitable to read the entries of the provided ZIP resource properly or <code>null</code>
     *         if we fail to detect the charset
     */
    public static Charset detect(URL resourceUrl) {
        try {
            for (Charset c : getCharsetTryChain()) {
                InputStream is = null;
                try {
                    is = resourceUrl.openStream();
                    if (canRead(is, c)) {
                        return c;
                    }
                } finally {
                    IOUtils.closeQuietly(is);
                    ;
                }
            }
        } catch (IOException e) {
            logger.warn("Error checking charset for the input stream", e);
        }

        logger.warn("Unable to find a charset to read the entries of a provided ZIP resource {}", resourceUrl);

        return null;
    }

    private static List<Charset> getCharsetTryChain() {
        if (charsetTryChain == null) {
            synchronized (ZipEntryCharsetDetector.class) {
                if (charsetTryChain == null) {
                    String encoding = System.getProperty(ZIP_ENTRY_ALTERNATIVE_ENCODING, "Cp437");
                    List<Charset> charsets = new LinkedList<>();
                    for (String c : StringUtils.split(encoding, " ,")) {
                        try {
                            charsets.add(Charset.forName(c));
                        } catch (UnsupportedCharsetException e) {
                            logger.warn(e.getMessage(), e);
                        }
                    }
                    if (!charsets.contains(StandardCharsets.UTF_8)) {
                        // first we try with UTF-8
                        charsets.add(0, StandardCharsets.UTF_8);
                    }
                    charsetTryChain = charsets;
                }
            }
        }
        return charsetTryChain;
    }
}
