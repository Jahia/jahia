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
