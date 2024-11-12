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
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * An alternative ZipInputStream implementation that reads from a directory instead of a ZIP file
 */
public class DirectoryZipInputStream extends ZipInputStream {

    File sourceDirectory;
    Iterator<String> sourceDirectoryEntriesIterator;
    File currentEntry;
    FileInputStream currentEntryInputStream;
    ZipEntry currentZipEntry;
    Stream<Path> directoryStream;

    /**
     * Creates a new ZIP input stream.
     *
     * @param sourceDirectory the root directory of the fake zip input stream
     */
    public DirectoryZipInputStream(File sourceDirectory) {
        super(new ByteArrayInputStream(new byte[0]));
        this.sourceDirectory = sourceDirectory;
        try {
            directoryStream = Files.walk(sourceDirectory.toPath());
            this.sourceDirectoryEntriesIterator = directoryStream.skip(1).map(Path::toString).collect(Collectors.toList()).iterator();
        } catch (IOException e) {
            //This should never happen as existence of directory has been validated before in our code, but in case we call it from new places, safer to throw exception
            throw new RuntimeException(e);
        }
    }

    public File getSourceDirectory() {
        return sourceDirectory;
    }

    @Override
    public ZipEntry getNextEntry() throws IOException {
        if (currentEntryInputStream != null) {
            closeEntry();
        }
        if (!sourceDirectoryEntriesIterator.hasNext()) {
            return null;
        }
        currentEntry = new File(sourceDirectoryEntriesIterator.next());
        String currentEntryName = currentEntry.getPath();
        if (currentEntryName.startsWith(sourceDirectory.getPath()+File.separator)) {
            currentEntryName = currentEntryName.substring(sourceDirectory.getPath().length()+1);
        }
        if (currentEntry.isDirectory() && !currentEntryName.endsWith("/")) {
            currentZipEntry = new ZipEntry(currentEntryName + "/");
        } else {
            currentZipEntry = new ZipEntry(currentEntryName);
        }
        currentZipEntry.setSize(currentEntry.length());
        currentZipEntry.setTime(currentEntry.lastModified());
        if (!currentEntry.isDirectory()) {
            currentEntryInputStream = new FileInputStream(currentEntry);
        }
        return currentZipEntry;
    }

    @Override
    public void closeEntry() throws IOException {
        if (currentEntryInputStream != null) {
            currentEntryInputStream.close();
            currentEntryInputStream = null;
        }
    }

    @Override
    public int available() throws IOException {
        return currentEntryInputStream.available();
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return currentEntryInputStream.read(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return currentEntryInputStream.skip(n);
    }

    @Override
    public void close() throws IOException {
        if (currentEntryInputStream != null) {
            currentEntryInputStream.close();
        }
        if (directoryStream != null) {
            directoryStream.close();
        }
    }

    @Override
    protected ZipEntry createZipEntry(String name) {
        return new ZipEntry(name);
    }

    @Override
    public int read() throws IOException {
        return currentEntryInputStream.read();
    }

    @Override
    protected void fill() throws IOException {
    }

    @Override
    public int read(byte[] b) throws IOException {
        return currentEntryInputStream.read(b);
    }

    @Override
    public synchronized void mark(int readlimit) {
        currentEntryInputStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        currentEntryInputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return currentEntryInputStream.markSupported();
    }
}
