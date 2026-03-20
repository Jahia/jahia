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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for the {@link UrlIOResource}.
 */
@SuppressWarnings("java:S1874")
public class UrlIOResourceTest {

    private File tempFile;
    private File tempDir;

    @Before
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("iotest").toFile();
        tempFile = new File(tempDir, "test-file.txt");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write("Test content from URL".getBytes());
        }
    }

    @After
    public void tearDown() {
        if (tempFile != null && tempFile.exists()) {
            tempFile.delete();
            // Ignore deletion result in cleanup
        }
        if (tempDir != null && tempDir.exists()) {
            tempDir.delete();
            // Ignore deletion result in cleanup
        }
    }

    @Test
    public void testGetURL() throws IOException {
        URL url = tempFile.toURI().toURL();
        UrlIOResource resource = new UrlIOResource(url);
        assertEquals("URL should match", url, resource.getURL());
    }

    @Test
    public void testGetURI() throws IOException {
        URL url = tempFile.toURI().toURL();
        UrlIOResource resource = new UrlIOResource(url);
        URI uri = resource.getURI();
        assertNotNull("URI should not be null", uri);
    }

    @Test
    public void testGetFile() throws IOException {
        URL url = tempFile.toURI().toURL();
        UrlIOResource resource = new UrlIOResource(url);
        File file = resource.getFile();
        assertNotNull("File should not be null", file);
        assertTrue("File should exist", file.exists());
    }

    @Test
    public void testGetInputStream() throws IOException {
        URL url = tempFile.toURI().toURL();
        UrlIOResource resource = new UrlIOResource(url);
        try (InputStream is = resource.getInputStream()) {
            assertNotNull("InputStream should not be null", is);
            byte[] buffer = new byte[21];
            int bytesRead = is.read(buffer);
            assertEquals("Should read correct number of bytes", 21, bytesRead);
            assertEquals("Content should match", "Test content from URL", new String(buffer));
        }
    }

    @Test
    public void testExists() throws IOException {
        URL url = tempFile.toURI().toURL();
        UrlIOResource resource = new UrlIOResource(url);
        assertTrue("Resource should exist", resource.exists());
    }

    @Test
    public void testExistsNonExistent() throws IOException {
        File nonExistent = new File(tempDir, "non-existent.txt");
        URL url = nonExistent.toURI().toURL();
        UrlIOResource resource = new UrlIOResource(url);
        assertFalse("Resource should not exist", resource.exists());
    }

    @Test
    public void testLastModified() throws IOException {
        URL url = tempFile.toURI().toURL();
        UrlIOResource resource = new UrlIOResource(url);
        long lastModified = resource.lastModified();
        assertTrue("Last modified should be greater than 0", lastModified > 0);
    }

    @Test
    public void testGetFilename() throws IOException {
        URL url = tempFile.toURI().toURL();
        UrlIOResource resource = new UrlIOResource(url);
        assertEquals("Filename should match", "test-file.txt", resource.getFilename());
    }

    @Test
    public void testGetDescription() throws IOException {
        URL url = tempFile.toURI().toURL();
        UrlIOResource resource = new UrlIOResource(url);
        String description = resource.getDescription();
        assertNotNull("Description should not be null", description);
        assertTrue("Description should start with 'URL ['", description.startsWith("URL ["));
        assertTrue("Description should contain URL", description.contains(url.toString()));
    }

    @Test
    public void testToString() throws IOException {
        URL url = tempFile.toURI().toURL();
        UrlIOResource resource = new UrlIOResource(url);
        assertEquals("toString should return description", resource.getDescription(), resource.toString());
    }

    @Test
    public void testEquals() throws IOException {
        URL url = tempFile.toURI().toURL();
        UrlIOResource resource1 = new UrlIOResource(url);
        UrlIOResource resource2 = new UrlIOResource(url);
        assertEquals("Resources with same URL should be equal", resource1, resource2);
    }

    @Test
    public void testEqualsSameInstance() throws IOException {
        URL url = tempFile.toURI().toURL();
        UrlIOResource resource = new UrlIOResource(url);
        assertEquals("Resource should equal itself", resource, resource);
    }

    @Test
    public void testHashCode() throws IOException {
        URL url = tempFile.toURI().toURL();
        UrlIOResource resource1 = new UrlIOResource(url);
        UrlIOResource resource2 = new UrlIOResource(url);
        assertEquals("HashCodes should match", resource1.hashCode(), resource2.hashCode());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullURL() {
        new UrlIOResource((URL) null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullPath() throws MalformedURLException {
        new UrlIOResource((String) null);
    }

    @Test
    public void testConstructorWithPath() throws MalformedURLException {
        String urlString = tempFile.toURI().toString();
        UrlIOResource resource = new UrlIOResource(urlString);
        assertNotNull("Resource should not be null", resource);
        assertEquals("URL should match", urlString, resource.getURL().toString());
    }

    @Test
    public void testGetInputStreamMultipleTimes() throws IOException {
        URL url = tempFile.toURI().toURL();
        UrlIOResource resource = new UrlIOResource(url);

        // Should be able to open stream multiple times
        try (InputStream is1 = resource.getInputStream()) {
            assertNotNull("First stream should not be null", is1);
        }

        try (InputStream is2 = resource.getInputStream()) {
            assertNotNull("Second stream should not be null", is2);
        }
    }

    @Test
    public void testHttpUrl() throws MalformedURLException {
        URL url = new URL("http://www.jahia.com");
        UrlIOResource resource = new UrlIOResource(url);
        assertEquals("URL should match", url, resource.getURL());
        assertNotNull("Description should not be null", resource.getDescription());
    }

    @Test
    public void testJarUrl() throws MalformedURLException {
        // Test with a typical JAR URL format
        URL url = new URL("jar:file:/path/to/file.jar!/entry/path.txt");
        UrlIOResource resource = new UrlIOResource(url);
        assertEquals("URL should match", url, resource.getURL());
        assertEquals("Filename should be extracted", "path.txt", resource.getFilename());
    }
}
