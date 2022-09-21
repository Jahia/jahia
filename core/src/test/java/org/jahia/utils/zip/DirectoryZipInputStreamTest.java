/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2022 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
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

import static org.assertj.core.api.Assertions.assertThat;

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
            entryNamesFound.add(zipEntry.getName().replace('\\', '/'));
            if (zipEntry.isDirectory()) {
                directoriesFound++;
            } else {
                filesRead++;
                byte[] fileContents = IOUtils.toByteArray(directoryZipInputStream);
                bytesRead += fileContents.length;
            }
        }

        assertThat(entryNamesFound).hasSameElementsAs(entryNames);
        assertThat(filesRead).withFailMessage("Number of files read does not match number of files created").isEqualTo(filesCreated);
        assertThat(directoriesFound).withFailMessage("Directories found does not match number of directories created")
                .isEqualTo(directoriesCreated);
        assertThat(entryNamesFound.size()).withFailMessage("Number of entry names does not match !").isEqualTo(entryNames.size());
        assertThat(bytesRead).withFailMessage("Bytes read does not match bytes written").isEqualTo(bytesWritten);

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
