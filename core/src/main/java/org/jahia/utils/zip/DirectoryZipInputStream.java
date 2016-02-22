/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * An alternative ZipInputStream implementation that reads from a directory instead of a ZIP file
 */
public class DirectoryZipInputStream extends ZipInputStream {

    File sourceDirectory;
    List<File> sourceDirectoryEntries;
    Iterator<File> sourceDirectoryEntriesIterator;
    File currentEntry;
    FileInputStream currentEntryInputStream;
    ZipEntry currentZipEntry;

    /**
     * Creates a new ZIP input stream.
     *
     * @param sourceDirectory the root directory of the fake zip input stream
     */
    public DirectoryZipInputStream(File sourceDirectory) {
        super(new ByteArrayInputStream(new byte[0]));
        this.sourceDirectory = sourceDirectory;
        this.sourceDirectoryEntries = new ArrayList<File>(FileUtils.listFilesAndDirs(sourceDirectory, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE));
        if (sourceDirectoryEntries.get(0).equals(sourceDirectory)) {
            sourceDirectoryEntries.remove(0);
        }
        this.sourceDirectoryEntriesIterator = sourceDirectoryEntries.iterator();
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
        currentEntry = sourceDirectoryEntriesIterator.next();
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
