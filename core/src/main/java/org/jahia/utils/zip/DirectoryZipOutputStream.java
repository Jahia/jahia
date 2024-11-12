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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * An alternative ZipOutputStream implementation that outputs to a directory instead of a ZIP file. This is usually
 * a better idea if large amounts of data will be exported. Although it requires an output stream for compatibility
 * with the ZipOutputStream implementation, it does not use it.
 */
public class DirectoryZipOutputStream extends ZipOutputStream {

    File destination;
    File currentEntry;
    FileOutputStream currentEntryOutputStream;
    ZipEntry currentZipEntry;

    /**
     * Creates a new ZIP output stream.
     *
     * @param out the actual output stream
     */
    public DirectoryZipOutputStream(File destination, OutputStream out) {
        super(out);
        this.destination = destination;
    }

    @Override
    public void putNextEntry(ZipEntry e) throws IOException {
        if (currentEntryOutputStream != null) {
            closeEntry();
        }
        String canonicalDestinationDirPath = destination.getCanonicalPath();
        currentEntry = new File(destination, e.getName());
        String canonicalDestinationPath = currentEntry.getCanonicalPath();

        if (!canonicalDestinationPath.startsWith(canonicalDestinationDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target directory");
        }

        if (!currentEntry.getParentFile().exists()) {
            currentEntry.getParentFile().mkdirs();
        }
        currentZipEntry = e;
        if (e.getName().endsWith("/")) {
            currentEntry.mkdir();
        } else {
            currentEntryOutputStream = new FileOutputStream(currentEntry);
        }
    }

    @Override
    public void closeEntry() throws IOException {
        if (currentZipEntry.getTime() > 0) {
            currentEntry.setLastModified(currentZipEntry.getTime());
        }
        currentEntryOutputStream.close();
        currentEntryOutputStream = null;
    }

    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        currentEntryOutputStream.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        currentEntryOutputStream.flush();
    }

    @Override
    public void write(int b) throws IOException {
        currentEntryOutputStream.write(b);
    }

    @Override
    public void finish() throws IOException {
        if (currentEntryOutputStream != null) {
            currentEntryOutputStream.close();
        }
    }

    @Override
    public void close() throws IOException {
        if (currentEntryOutputStream != null) {
            currentEntryOutputStream.close();
        }
    }

    @Override
    public void setComment(String comment) {
    }

    @Override
    protected void deflate() throws IOException {
    }

    @Override
    public void write(byte[] b) throws IOException {
        currentEntryOutputStream.write(b);
    }

    @Override
    public void setMethod(int method) {
    }

    @Override
    public void setLevel(int level) {
    }
}
