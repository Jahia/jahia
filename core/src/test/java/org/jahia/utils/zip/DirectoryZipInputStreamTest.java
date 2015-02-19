package org.jahia.utils.zip;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A unit test class for the DirectoryZipInputStream class
 */
public class DirectoryZipInputStreamTest {

    public static final int RANDOM_BUFFER_SIZE = 2048;

    public static File tempDirectory;
    public static File outputDirectory;
    public static byte[] randomBuffer = new byte[RANDOM_BUFFER_SIZE];
    public int directoriesCreated = 0;
    public int filesCreated = 0;
    public long bytesWritten = 0;
    public List<String> entryNames = new ArrayList<String>();

    @BeforeClass
    public static void oneTimeSetUp() {
        tempDirectory = FileUtils.getTempDirectory();
        outputDirectory = new File(tempDirectory, "dirzip-dest-" + System.currentTimeMillis());
        outputDirectory.mkdirs();

        Random random = new Random();
        random.nextBytes(randomBuffer);
    }

    @AfterClass
    public static void oneTimeTearDown() throws IOException {
        FileUtils.deleteDirectory(outputDirectory);
    }

    @Test
    public void testDirectoryZipInputStream() throws IOException {
        createDirectoryZip();

        File inputStreamFile = File.createTempFile("dirzip-input", null);
        FileInputStream fileInputStream = new FileInputStream(inputStreamFile);
        DirectoryZipInputStream directoryZipInputStream = new DirectoryZipInputStream(outputDirectory);

        ZipEntry zipEntry = null;
        int directoriesFound = 0;
        int filesRead = 0;
        long bytesRead = 0;
        List<String> entryNamesFound = new ArrayList<String>();
        while ((zipEntry = directoryZipInputStream.getNextEntry()) != null) {
            entryNamesFound.add(zipEntry.getName());
            if (zipEntry.isDirectory()) {
                directoriesFound++;
            } else {
                filesRead++;
                byte[] fileContents = IOUtils.toByteArray(directoryZipInputStream);
                bytesRead += fileContents.length;
            }
        }

        Assert.assertEquals("Bytes read does not match bytes written", bytesWritten, bytesRead);
        Assert.assertEquals("Number of files read does not match number of files created", filesCreated, filesRead);
        Assert.assertEquals("Directories found does not match number of directories created", directoriesCreated, directoriesFound);
        Assert.assertEquals("Number of entry names does not match !", entryNames.size(), entryNamesFound.size());
        Assert.assertTrue("Entry names do not match !", CollectionUtils.isEqualCollection(entryNames, entryNamesFound));

        fileInputStream.close();
        inputStreamFile.delete();
    }

    private void createDirectoryZip() throws IOException {
        File outputStreamFile = File.createTempFile("dirzip-output", null);
        FileOutputStream fileOutputStream = new FileOutputStream(outputStreamFile);

        DirectoryZipOutputStream directoryZipOutputStream = new DirectoryZipOutputStream(outputDirectory, fileOutputStream);

        createDirectoryContent(directoryZipOutputStream, "", 3, 3, 0, 4);

        directoryZipOutputStream.close();

        fileOutputStream.close();
        outputStreamFile.delete();

    }

    private void createDirectoryContent(ZipOutputStream zipOutputStream, String parentName, int nbFiles, int nbDirs, int currentDepth, int maxDepth) throws IOException {
        if (currentDepth > maxDepth) {
            return;
        }
        String entryName = null;
        for (int i=0; i < nbDirs; i++) {
            directoriesCreated++;
            entryName = parentName + "dir-" + Integer.toString(i) + "/";
            entryNames.add(entryName);
            zipOutputStream.putNextEntry(new ZipEntry(entryName));
            createDirectoryContent(zipOutputStream, entryName, nbFiles, nbDirs, currentDepth+1, maxDepth);
        }
        for (int i=0; i < nbFiles; i++) {
            filesCreated++;
            entryName = parentName + "file-" + Integer.toString(i);
            zipOutputStream.putNextEntry(new ZipEntry(entryName));
            entryNames.add(entryName);
            zipOutputStream.write(randomBuffer, 0, RANDOM_BUFFER_SIZE);
            bytesWritten += RANDOM_BUFFER_SIZE;
        }
    }

}
