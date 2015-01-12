package org.jahia.utils.zip;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A unit test for the DirectoryZipOutputStream class
 */
public class DirectoryZipOutputStreamTest {

    public static final int RANDOM_BUFFER_SIZE = 2048;

    public static File tempDirectory;
    public static File outputDirectory;
    public static byte[] randomBuffer = new byte[RANDOM_BUFFER_SIZE];

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
    public void testDirectoryZipOutputStream() throws IOException {
        File outputStreamFile = File.createTempFile("dirzip-output", null);
        FileOutputStream fileOutputStream = new FileOutputStream(outputStreamFile);

        DirectoryZipOutputStream directoryZipOutputStream = new DirectoryZipOutputStream(outputDirectory, fileOutputStream);

        createDirectoryContent(directoryZipOutputStream, "", 3, 3, 0, 4);

        directoryZipOutputStream.close();

        fileOutputStream.close();

        Assert.assertTrue("No write to the output stream should happen !", outputStreamFile.length() == 0);

        outputStreamFile.delete();
    }

    private void createDirectoryContent(ZipOutputStream zipOutputStream, String parentName, int nbFiles, int nbDirs, int currentDepth, int maxDepth) throws IOException {
        if (currentDepth > maxDepth) {
            return;
        }
        for (int i=0; i < nbFiles; i++) {
            zipOutputStream.putNextEntry(new ZipEntry(parentName + "file-" + Integer.toString(i)));
            zipOutputStream.write(randomBuffer, 0, RANDOM_BUFFER_SIZE);
        }
        for (int i=0; i < nbDirs; i++) {
            zipOutputStream.putNextEntry(new ZipEntry(parentName + "dir-" + Integer.toString(i) + "/"));
            createDirectoryContent(zipOutputStream, parentName + "dir-" + Integer.toString(i) + "/", nbFiles, nbDirs, currentDepth+1, maxDepth);
        }
    }

}
