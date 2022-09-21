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
