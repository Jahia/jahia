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
//
//
//  JahiaArchiveFileHandler
//
//  NK      15.01.2001
//
//


package org.jahia.utils.zip;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaArchiveFileException;
import org.jahia.exceptions.JahiaException;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * A Wrapper to handle some manipulations on .jar, .war and .ear files
 *
 * @author Khue ng
 */
public class JahiaArchiveFileHandler {

    private static org.slf4j.Logger logger =
            org.slf4j.LoggerFactory.getLogger(JahiaArchiveFileHandler.class);

    private static final String CLASS_NAME = JahiaArchiveFileHandler.class.
            getName();

    /**
     * The full path to the file *
     */
    private String filePath;

    /**
     * The JarFile object *
     */
    private JarFile jarFile;

    private String basePath;

    /**
     * Constructor
     *
     * @param path, the full path to the file
     * @throws IOException
     */
    public JahiaArchiveFileHandler(String path)
            throws IOException {

        filePath = path;
        File f = new File(path);
        try {

            jarFile = new JarFile(f);

        } catch (IOException ioe) {
            logger.error("IOException occurred " + f.getAbsolutePath(), ioe);
            throw new IOException(CLASS_NAME + " IOException occurred ");
        } catch (java.lang.NullPointerException e) {
            logger.error("NullPointerException " + f.getAbsolutePath(), e);
            throw new IOException(CLASS_NAME +
                    " NullPointerException occurred ");
        }
    }

    public JahiaArchiveFileHandler(String path, String basePath) throws IOException {
        this(path);
        this.basePath = basePath;
    }

    /**
     * Decompresses the file in it's current location
     */
    public void unzip()
            throws JahiaException {

        try {

            File f = new File(filePath);
            String parentPath = f.getParent() + File.separator;

            try (FileInputStream fis = new FileInputStream(filePath);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    ZipInputStream zis = new ZipInputStream(bis);
                    ZipFile zf = new ZipFile(filePath)){
                ZipEntry ze = null;
                while ((ze = zis.getNextEntry()) != null) {
                    String zeName = ze.getName();
                    String path = parentPath + genPathFile(zeName);
                    File fo = new File(path);

                    if (ze.isDirectory()) {
                        fo.mkdirs();
                    } else {
                        copyStream(zis, new FileOutputStream(fo));
                    }
                    zis.closeEntry();
                }
            }
        } catch (IOException ioe) {
            throw new JahiaException("JahiaArchiveFileHandler",
                    "fail processing unzip",
                    JahiaException.SERVICE_ERROR,
                    JahiaException.ERROR_SEVERITY, ioe);
        }

    }

    /**
     * Decompress the file in a given folder
     *
     * @param path
     */
    public Map<String, String> unzip(String path)
            throws JahiaException {
        return unzip(path, true);
    }

    public Map<String, String> unzip(String path, PathFilter pathFilter) throws JahiaException {
        return unzip(path, true, pathFilter, null);
    }

    public Map<String, String> unzip(String path, boolean overwrite)
            throws JahiaException {
        return unzip(path, overwrite, PathFilter.ALL, null);
    }

    public Map<String, String> unzip(String path, boolean overwrite, PathFilter pathFilter, String pathPrefix)
            throws JahiaException {

        Map<String, String> unzippedFiles = new TreeMap<>();
        Map<File, Long> timestamps = new HashMap<>();

        pathFilter = pathFilter != null ? pathFilter : PathFilter.ALL;

        try (FileInputStream fis = new FileInputStream(filePath);
                BufferedInputStream bis = new BufferedInputStream(fis);
                ZipInputStream zis = new ZipInputStream(bis);
                ZipFile zf = new ZipFile(filePath)) {
            ZipEntry ze = null;
            while ((ze = zis.getNextEntry()) != null) {

                String zeName = ze.getName();

                String entryFilePath = genPathFile(zeName);

                String destPath = path + File.separator + entryFilePath;

                File fo = new File(destPath);

                if (pathFilter.accept(pathPrefix != null ? pathPrefix + "/" + zeName : zeName)) {
                    String loggedPath = fo.getAbsolutePath();
                    if (basePath != null) {
                        loggedPath = StringUtils.substringAfter(loggedPath, basePath);
                    }
                    unzippedFiles.put(entryFilePath, loggedPath);
                    long lastModified = ze.getTime();
                    if (lastModified > 0) {
                        timestamps.put(fo, lastModified);
                    }
                    if (ze.isDirectory()) {
                        fo.mkdirs();
                    } else if (overwrite || !fo.exists()) {
                        File parent = new File(fo.getParent());
                        parent.mkdirs();
                        FileOutputStream fos = new FileOutputStream(fo);
                        copyStream(zis, fos);
                    }
                }
                zis.closeEntry();
            }
        } catch (IOException ioe) {
            throw new JahiaException(CLASS_NAME, "faile processing unzip", JahiaException.SERVICE_ERROR, JahiaException.ERROR_SEVERITY,
                    ioe);

        }

        // preserve last modified time stamps
        for (Map.Entry<File, Long> tst : timestamps.entrySet()) {
            try {
                if (!tst.getKey().setLastModified(tst.getValue())) {
                    logger.warn("Unable to set last mofified date for file {}.", tst.getKey());
                }
            } catch (Exception e) {
                logger.warn("Unable to set last mofified date for file {}. Cause: {}", tst.getKey(), e.getMessage());
            }
        }

        return unzippedFiles;

    }

    /**
     * Extract an entry of file type in the jar file
     * Return a File Object reference to the uncompressed file
     *
     * @param entryName, the entry name
     * @return (File) fo, a File Handler to the file ( It's a temporary file )
     */
    public File extractFile(String entryName)
            throws IOException, JahiaArchiveFileException {

        File tmpFile = null;

        // Create a temporary file and write the content of the file in it
        ZipEntry entry = jarFile.getEntry(entryName);

        if ((entry != null) && !entry.isDirectory()) {

            InputStream ins = jarFile.getInputStream(entry);

            if (ins != null) {
                tmpFile = File.createTempFile("jahia_temp", ".jar", null);

                if (tmpFile == null || !tmpFile.canWrite()) {
                    throw new IOException(
                            "extractFile error creating temporary file");
                }
                FileOutputStream outs = new FileOutputStream(tmpFile);
                try {
                    copyStream(ins, outs);
                    outs.flush();
                } finally {
                    outs.close();
                }
            }
        } else {
            throw new JahiaArchiveFileException(entryName + " is null or a directory ", JahiaException.ENTRY_NOT_FOUND);
        }

        return tmpFile;

    }

    /**
     * Extracts the content of the specifdied archive entry as a text.
     *
     * @param entryName the archive entry name
     * @return the content of the specifdied archive entry as a text
     */
    public String extractContent(String entryName) throws IOException,
            JahiaArchiveFileException {

        if (!entryExists(entryName)) {
            throw new JahiaArchiveFileException(entryName + " is not found", JahiaException.ENTRY_NOT_FOUND);
        }
        ZipEntry entry = jarFile.getEntry(entryName);

        StringWriter out = new StringWriter();
        IOUtils.copy(jarFile.getInputStream(entry), out);

        return out.getBuffer().toString();
    }

    /**
     * Extract an entry in a gived folder. If this entry is a directory,
     * all its contents are extracted too.
     *
     * @param entryName, the name of an entry in the jar
     * @param destPath,  the path to the destination folder
     */
    public void extractEntry(String entryName,
                             String destPath)
            throws JahiaException {

        try {

            ZipEntry entry = jarFile.getEntry(entryName);

            if (entry == null) {
                StringBuilder strBuf = new StringBuilder(1024);
                strBuf.append(" extractEntry(), cannot find entry ");
                strBuf.append(entryName);
                strBuf.append(" in the jar file ");

                throw new JahiaException(CLASS_NAME, strBuf.toString(),
                        JahiaException.SERVICE_ERROR,
                        JahiaException.ERROR_SEVERITY);

            }

            File destDir = new File(destPath);
            if (!destDir.isDirectory() || !destDir.canWrite()) {
                throw new JahiaException(CLASS_NAME,
                        " cannot access to the destination dir ",
                        JahiaException.SERVICE_ERROR,
                        JahiaException.ERROR_SEVERITY);
            }

            try (FileInputStream fis = new FileInputStream(filePath);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    ZipInputStream zis = new ZipInputStream(bis);
                    ZipFile zf = new ZipFile(filePath);) {
                ZipEntry ze = null;
                while ((ze = zis.getNextEntry()) != null && !ze.getName().equalsIgnoreCase(entryName)) {
                    // loop until the requested entry
                    zis.closeEntry();
                }

                if (ze != null) {
                    if (ze.isDirectory()) {

                        while (ze != null) {
                            String zeName = ze.getName();
                            String path = destPath + File.separator + genPathFile(zeName);
                            File fo = new File(path);
                            if (ze.isDirectory()) {
                                fo.mkdirs();
                            } else {

                                FileOutputStream outs = new FileOutputStream(fo);
                                copyStream(zis, outs);
                            }
                            zis.closeEntry();
                            ze = zis.getNextEntry();

                        }
                    } else {
                        String zeName = ze.getName();
                        String path = destPath + File.separator + genPathFile(zeName);

                        File fo = new File(path);
                        FileOutputStream outs = new FileOutputStream(fo);
                        copyStream(zis, outs);
                    }
                }
            }
        } catch (IOException ioe) {
            throw new JahiaException(CLASS_NAME, "faile processing unzip",
                    JahiaException.SERVICE_ERROR,
                    JahiaException.ERROR_SEVERITY, ioe);
        }

    }

    /**
     * Return an entry in the jar file of the gived name or null if not found
     *
     * @param entryName the entry name
     * @return (ZipEntry) the entry
     */
    public ZipEntry getEntry(String entryName) {

        return jarFile.getEntry(entryName);

    }

    /**
     * Check if an entry is a directory or not
     *
     * @param (String) entryName the entry name
     * @return (boolean) true if the entry exists and is a a directory
     */
    public boolean isDirectory(String entryName) {
        return ((jarFile.getEntry(entryName) != null) &&
                jarFile.getEntry(entryName).isDirectory());
    }

    /**
     * Check if an entry exist or not
     *
     * @param (String) entryName the entry name
     * @return (boolean) true if exist
     */
    public boolean entryExists(String entryName) {
        return (jarFile.getEntry(entryName) != null);
    }

    /**
     * Close the Zip file. Important to close the JarFile object
     * to be able to delete it from disk.
     */
    public void closeArchiveFile() {

        try {
            jarFile.close();
        } catch (IOException e) {
            logger.error("cannot close jar file", e);
            // cannot close file
        }

    }

    /**
     * Generates a file path for a given entry name
     * Parses "/" char and replaces them with File.separator char
     */
    protected String genPathFile(String entryName) {

        StringBuilder sb = new StringBuilder(entryName.length());
        for (int i = 0; i < entryName.length(); i++) {
            if (entryName.charAt(i) == '/') {
                sb.append(File.separator);
            } else {
                sb.append(entryName.charAt(i));
            }
        }

        return (sb.toString());

    }

    /**
     * Copy an InputStream to an OutPutStream
     *
     * @param ins  An InputStream.
     * @param outs An OutputStream.
     * @throws IOException.
     */
    protected void copyStream(InputStream ins,
                              OutputStream outs)
            throws IOException {
        int bufferSize = 1024;
        byte[] writeBuffer = new byte[bufferSize];

        BufferedOutputStream bos =
                new BufferedOutputStream(outs, bufferSize);
        int bufferRead;
        while ((bufferRead = ins.read(writeBuffer)) != -1)
            bos.write(writeBuffer, 0, bufferRead);
        bos.flush();
        bos.close();
        outs.flush();
        outs.close();
    }

    public String getPath() {
        return jarFile.getName();
    }
} // End Class JahiaArchiveFileHandler
