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

//
//
//  JahiaArchiveFileHandler
//
//  NK      15.01.2001
//
//


package org.jahia.utils.zip;

import org.apache.commons.io.IOUtils;
import org.jahia.exceptions.JahiaArchiveFileException;
import org.jahia.exceptions.JahiaException;

import java.io.*;
import java.util.*;
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

    /** The full path to the file **/
    private String m_FilePath;

    /** The JarFile object **/
    private JarFile m_JarFile;

    /**
     * Constructor
     *
     * @param path, the full path to the file
     * @exception IOException
     */
    public JahiaArchiveFileHandler (String path)
        throws IOException {

        m_FilePath = path;
        File f = new File(path);
        try {

            m_JarFile = new JarFile(f);

        } catch (IOException ioe) {
            logger.error("IOException occurred " + f.getAbsolutePath(), ioe);
            throw new IOException(CLASS_NAME + " IOException occurred ");
        } catch (java.lang.NullPointerException e) {
            logger.error("NullPointerException " + f.getAbsolutePath(), e);
            throw new IOException(CLASS_NAME +
                                  " NullPointerException occurred ");
        }

        if (m_JarFile == null) {

            throw new IOException(CLASS_NAME + " source file is null");
        }

    }

    /**
     * Decompresses the file in it's current location
     *
     */
    public void unzip ()
        throws JahiaException {

        try {

            File f = new File(m_FilePath);

            //JahiaConsole.println(CLASS_NAME + ".upzip"," Start Decompressing " + f.getName() );

            String parentPath = f.getParent() + File.separator;
            String path = null;

            FileInputStream fis = new FileInputStream(m_FilePath);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ZipInputStream zis = new ZipInputStream(bis);
            ZipFile zf = new ZipFile(m_FilePath);
            ZipEntry ze = null;
            String zeName = null;

            try {

                while ( (ze = zis.getNextEntry()) != null) {
                    zeName = ze.getName();
                    path = parentPath + genPathFile(zeName);
                    File fo = new File(path);

                    if (ze.isDirectory()) {
                        fo.mkdirs();
                    } else {
                        copyStream(zis, new FileOutputStream(fo));
                    }
                    zis.closeEntry();
                }
            } finally {

                // Important !!!
                zf.close();
                fis.close();
                zis.close();
                bis.close();
            }

            //JahiaConsole.println(CLASS_NAME+".unzip"," Decompressing " + f.getName() + " done ! ");

        } catch (IOException ioe) {

            logger.error(" fail unzipping " + ioe.getMessage(), ioe);

            throw new JahiaException("JahiaArchiveFileHandler",
                                     "faile processing unzip",
                                     JahiaException.SERVICE_ERROR,
                                     JahiaException.ERROR_SEVERITY, ioe);

        }

    }

    /**
     * Decompress the file in a given folder
     *
     * @param path
     */
    public Map<String,String> unzip (String path)
        throws JahiaException {
        return unzip(path, true);
    }

    public Map<String,String> unzip(String path, PathFilter pathFilter) throws JahiaException {
        return unzip(path, true, pathFilter, null);
    }

    public Map<String,String> unzip (String path, boolean overwrite)
    throws JahiaException {
        return unzip(path, overwrite, PathFilter.ALL, null);
    }
    
    public Map<String,String> unzip (String path, boolean overwrite, PathFilter pathFilter, String pathPrefix)
        throws JahiaException {

        Map<String,String> unzippedFiles = new TreeMap<String,String>();
        Map<File,Long> timestamps = new HashMap<File,Long>();

        pathFilter = pathFilter != null ? pathFilter : PathFilter.ALL;

        try {

            String destPath = null;

            FileInputStream fis = new FileInputStream(m_FilePath);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ZipInputStream zis = new ZipInputStream(bis);
            ZipFile zf = new ZipFile(m_FilePath);
            ZipEntry ze = null;
            String zeName = null;

            try {

                while ( (ze = zis.getNextEntry()) != null) {

                    zeName = ze.getName();

                    String filePath = genPathFile(zeName);

                    destPath = path + File.separator + filePath;

                    File fo = new File(destPath);

                    if (pathFilter.accept(pathPrefix != null ? pathPrefix + "/"
                            + zeName : zeName)) {
                        unzippedFiles.put(filePath, fo.getAbsolutePath());
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
            } finally {

                // Important !!!
                zf.close();
                fis.close();
                zis.close();
                bis.close();
            }

        } catch (IOException ioe) {

            logger.error(" fail unzipping " + ioe.getMessage(), ioe);

            throw new JahiaException(CLASS_NAME, "faile processing unzip",
                                     JahiaException.SERVICE_ERROR,
                                     JahiaException.ERROR_SEVERITY, ioe);

        }
        
        // preserve last modified time stamps
        for (Map.Entry<File, Long> tst : timestamps.entrySet()) {
            try {
                tst.getKey().setLastModified(tst.getValue());
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
    public File extractFile (String entryName)
        throws IOException, JahiaArchiveFileException {

        File tmpFile = null;

        // Create a temporary file and write the content of the file in it
        ZipEntry entry = m_JarFile.getEntry(entryName);

        if ( (entry != null) && !entry.isDirectory()) {

            InputStream ins = m_JarFile.getInputStream(entry);

            if (ins != null) {
                tmpFile = File.createTempFile("jahia_temp", ".jar", null);

                if (tmpFile == null || !tmpFile.canWrite()) {
                    throw new IOException(
                        "extractFile error creating temporary file");
                }
                FileOutputStream outs = new FileOutputStream(tmpFile);
                copyStream(ins, outs);
                outs.flush();
                outs.close();
            }
        } else {
            logger.error(entryName + " is null or a directory ");
            throw new JahiaArchiveFileException(JahiaException.ENTRY_NOT_FOUND);
        }

        return tmpFile;

    }

    /**
     * Extracts the content of the specifdied archive entry as a text.
     * 
     * @param entryName
     *            the archive entry name
     * @return the content of the specifdied archive entry as a text
     */
    public String extractContent(String entryName) throws IOException,
            JahiaArchiveFileException {

        if (!entryExists(entryName))
            throw new JahiaArchiveFileException(JahiaException.ENTRY_NOT_FOUND);

        ZipEntry entry = m_JarFile.getEntry(entryName);

        StringWriter out = new StringWriter();
        IOUtils.copy(m_JarFile.getInputStream(entry), out);

        return out.getBuffer().toString();
    }

    /**
     * Extract an entry in a gived folder. If this entry is a directory,
     * all its contents are extracted too.
     *
     * @param entryName, the name of an entry in the jar
     * @param destPath, the path to the destination folder
     */
    public void extractEntry (String entryName,
                              String destPath)
        throws JahiaException {

        try {

            ZipEntry entry = m_JarFile.getEntry(entryName);

            if (entry == null) {
                StringBuffer strBuf = new StringBuffer(1024);
                strBuf.append(" extractEntry(), cannot find entry ");
                strBuf.append(entryName);
                strBuf.append(" in the jar file ");

                throw new JahiaException(CLASS_NAME, strBuf.toString(),
                                         JahiaException.SERVICE_ERROR,
                                         JahiaException.ERROR_SEVERITY);

            }

            File destDir = new File(destPath);
            if (destDir == null || !destDir.isDirectory() || !destDir.canWrite()) {

                logger.error(" cannot access to the destination dir " +
                             destPath);

                throw new JahiaException(CLASS_NAME,
                    " cannot access to the destination dir ",
                                         JahiaException.SERVICE_ERROR,
                                         JahiaException.ERROR_SEVERITY);
            }

            String path = null;

            FileInputStream fis = new FileInputStream(m_FilePath);
            BufferedInputStream bis = new BufferedInputStream(fis);
            ZipInputStream zis = new ZipInputStream(bis);
            ZipFile zf = new ZipFile(m_FilePath);
            ZipEntry ze = null;
            String zeName = null;

            while ( (ze = zis.getNextEntry()) != null &&
                          !ze.getName().equalsIgnoreCase(entryName)) {
                // loop until the requested entry
                zis.closeEntry();
            }

            try {

                if (ze.isDirectory()) {

                    while (ze != null) {
                        zeName = ze.getName();
                        path = destPath + File.separator + genPathFile(zeName);
                        File fo = new File(path);
                        if (ze.isDirectory()) {
                            fo.mkdirs();
                        } else {

                            FileOutputStream outs = new FileOutputStream(fo);
                            copyStream(zis, outs);
                            //outs.flush();
                            //outs.close();
                        }
                        zis.closeEntry();
                        ze = zis.getNextEntry();

                    }
                } else {

                    zeName = ze.getName();
                    path = destPath + File.separator + genPathFile(zeName);

                    File fo = new File(path);
                    FileOutputStream outs = new FileOutputStream(fo);
                    copyStream(zis, outs);
                    //outs.flush();
                    //outs.close();
                }

            } finally {

                // Important !!!
                zf.close();
                fis.close();
                zis.close();
                bis.close();
            }

        } catch (IOException ioe) {

            logger.error(" fail unzipping " + ioe.getMessage(), ioe);

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
    public ZipEntry getEntry (String entryName) {

        return m_JarFile.getEntry(entryName);

    }

    /**
     * Check if an entry is a directory or not
     *
     * @param (String) entryName the entry name
     * @return (boolean) true if the entry exists and is a a directory
     */
    public boolean isDirectory (String entryName) {
        return ( (m_JarFile.getEntry(entryName) != null) &&
                m_JarFile.getEntry(entryName).isDirectory());
    }

    /**
     * Check if an entry exist or not
     *
     * @param (String) entryName the entry name
     * @return (boolean) true if exist
     */
    public boolean entryExists (String entryName) {
        return (m_JarFile.getEntry(entryName) != null);
    }

    /**
     * Close the Zip file. Important to close the JarFile object
     * to be able to delete it from disk.
     *
     */
    public void closeArchiveFile () {

        try {
            m_JarFile.close();
        } catch (IOException e) {
            logger.error("cannot close jar file", e);
            // cannot close file
        }

    }

    /**
     * Generates a file path for a gived entry name
     * Parses "/" char and replaces them with File.separator char
     *
     */
    protected String genPathFile (String entryName) {

        StringBuffer sb = new StringBuffer(entryName.length());
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
     * @param ins An InputStream.
     * @param outs An OutputStream.
     * @exception IOException.
     */
    protected void copyStream (InputStream ins,
                               OutputStream outs)
        throws IOException {
        int bufferSize = 1024;
        byte[] writeBuffer = new byte[bufferSize];

        BufferedOutputStream bos =
            new BufferedOutputStream(outs, bufferSize);
        int bufferRead;
        while ( (bufferRead = ins.read(writeBuffer)) != -1)
            bos.write(writeBuffer, 0, bufferRead);
        bos.flush();
        bos.close();
        outs.flush();
        outs.close();
    }

    public String getPath() {
    	return m_JarFile.getName();
    }
} // End Class JahiaArchiveFileHandler
