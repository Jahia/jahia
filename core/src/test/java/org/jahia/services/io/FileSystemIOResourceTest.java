/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2026 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2026 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.services.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for the {@link FileSystemIOResource}.
 */
public class FileSystemIOResourceTest {

    private File tempFile;
    private File tempDir;

    @Before
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("iotest").toFile();
        tempFile = new File(tempDir, "test-file.txt");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write("Test content".getBytes());
        }
    }

    @After
    public void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
        }
        if (tempDir != null && tempDir.exists()) {
            tempDir.delete();
        }
    }

    @Test
    public void testGetFile() {
        FileSystemIOResource resource = new FileSystemIOResource(tempFile);
        assertEquals("File should match", tempFile, resource.getFile());
    }

    @Test
    public void testGetURL() throws IOException {
        FileSystemIOResource resource = new FileSystemIOResource(tempFile);
        URL url = resource.getURL();
        assertNotNull("URL should not be null", url);
        assertEquals("URL should match file URI", tempFile.toURI().toURL(), url);
    }

    @Test
    public void testGetURI() {
        FileSystemIOResource resource = new FileSystemIOResource(tempFile);
        URI uri = resource.getURI();
        assertNotNull("URI should not be null", uri);
        assertEquals("URI should match", tempFile.toURI(), uri);
    }

    @Test
    public void testGetInputStream() throws IOException {
        FileSystemIOResource resource = new FileSystemIOResource(tempFile);
        try (InputStream is = resource.getInputStream()) {
            assertNotNull("InputStream should not be null", is);
            byte[] buffer = new byte[12];
            int bytesRead = is.read(buffer);
            assertEquals("Should read correct number of bytes", 12, bytesRead);
            assertEquals("Content should match", "Test content", new String(buffer));
        }
    }

    @Test
    public void testExists() {
        FileSystemIOResource resource = new FileSystemIOResource(tempFile);
        assertTrue("File should exist", resource.exists());
    }

    @Test
    public void testExistsNonExistent() {
        File nonExistent = new File(tempDir, "non-existent.txt");
        FileSystemIOResource resource = new FileSystemIOResource(nonExistent);
        assertFalse("File should not exist", resource.exists());
    }

    @Test
    public void testLastModified() {
        FileSystemIOResource resource = new FileSystemIOResource(tempFile);
        long lastModified = resource.lastModified();
        assertTrue("Last modified should be greater than 0", lastModified > 0);
        assertEquals("Last modified should match file", tempFile.lastModified(), lastModified);
    }

    @Test
    public void testGetFilename() {
        FileSystemIOResource resource = new FileSystemIOResource(tempFile);
        assertEquals("Filename should match", "test-file.txt", resource.getFilename());
    }

    @Test
    public void testGetDescription() {
        FileSystemIOResource resource = new FileSystemIOResource(tempFile);
        String description = resource.getDescription();
        assertNotNull("Description should not be null", description);
        assertTrue("Description should contain file path",
                description.contains(tempFile.getAbsolutePath()));
        assertTrue("Description should start with 'file ['", description.startsWith("file ["));
    }

    @Test
    public void testToString() {
        FileSystemIOResource resource = new FileSystemIOResource(tempFile);
        assertEquals("toString should return description", resource.getDescription(), resource.toString());
    }

    @Test
    public void testEquals() {
        FileSystemIOResource resource1 = new FileSystemIOResource(tempFile);
        FileSystemIOResource resource2 = new FileSystemIOResource(tempFile);
        assertEquals("Resources with same file should be equal", resource1, resource2);
    }

    @Test
    public void testEqualsSameInstance() {
        FileSystemIOResource resource = new FileSystemIOResource(tempFile);
        assertEquals("Resource should equal itself", resource, resource);
    }

    @Test
    public void testNotEquals() throws IOException {
        File otherFile = new File(tempDir, "other-file.txt");
        boolean created = otherFile.createNewFile();
        assertTrue("File should be created", created);

        FileSystemIOResource resource1 = new FileSystemIOResource(tempFile);
        FileSystemIOResource resource2 = new FileSystemIOResource(otherFile);

        assertNotEquals("Resources with different files should not be equal", resource1, resource2);

        otherFile.delete();
        // Ignore deletion result
    }

    @Test
    public void testHashCode() {
        FileSystemIOResource resource1 = new FileSystemIOResource(tempFile);
        FileSystemIOResource resource2 = new FileSystemIOResource(tempFile);
        assertEquals("HashCodes should match", resource1.hashCode(), resource2.hashCode());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullFile() {
        new FileSystemIOResource((File) null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullPath() {
        new FileSystemIOResource((String) null);
    }

    @Test
    public void testConstructorWithPath() {
        FileSystemIOResource resource = new FileSystemIOResource(tempFile.getAbsolutePath());
        assertEquals("File should match", tempFile, resource.getFile());
    }

    @Test
    public void testGetInputStreamMultipleTimes() throws IOException {
        FileSystemIOResource resource = new FileSystemIOResource(tempFile);

        // Should be able to open stream multiple times
        try (InputStream is1 = resource.getInputStream()) {
            assertNotNull("First stream should not be null", is1);
        }

        try (InputStream is2 = resource.getInputStream()) {
            assertNotNull("Second stream should not be null", is2);
        }
    }
}
