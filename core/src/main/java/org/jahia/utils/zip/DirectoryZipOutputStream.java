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
        currentEntry = new File(destination, e.getName());
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
