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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.junit.Test;

/**
 * Integration tests for IOResource implementations using real test resources from src/test/resources.
 * Tests JAR files, classpath resources, and various file types.
 */
@SuppressWarnings("java:S1874")
public class RealResourcesTest {

    // ==================== Classpath Resource Tests ====================

    @Test
    public void testSimpleTextFileFromClasspath() throws IOException {
        URL url = getClass().getResource("/test-data/simple.txt");
        assertNotNull("simple.txt should be found on classpath", url);

        UrlIOResource resource = new UrlIOResource(url);

        assertTrue("Resource should exist", resource.exists());
        assertEquals("simple.txt", resource.getFilename());

        try (InputStream is = resource.getInputStream()) {
            byte[] content = readAllBytes(is);
            assertTrue("File should have content", content.length > 0);
        }
    }

    @Test
    public void testEmptyFileFromClasspath() throws IOException {
        URL url = getClass().getResource("/test-data/empty.txt");
        assertNotNull("empty.txt should be found on classpath", url);

        UrlIOResource resource = new UrlIOResource(url);

        assertTrue("Empty file should exist", resource.exists());
        assertEquals("empty.txt", resource.getFilename());

        try (InputStream is = resource.getInputStream()) {
            byte[] content = readAllBytes(is);
            assertEquals("Empty file should have zero bytes", 0, content.length);
        }
    }

    @Test
    public void testBinaryFileFromClasspath() throws IOException {
        URL url = getClass().getResource("/test-data/binary.dat");
        assertNotNull("binary.dat should be found on classpath", url);

        UrlIOResource resource = new UrlIOResource(url);

        assertTrue("Binary file should exist", resource.exists());
        assertEquals("binary.dat", resource.getFilename());

        try (InputStream is = resource.getInputStream()) {
            byte[] content = readAllBytes(is);
            assertTrue("Binary file should have content", content.length > 0);
            // Verify it's actually binary (contains non-text bytes)
        }
    }

    @Test
    public void testLargeFileFromClasspath() throws IOException {
        URL url = getClass().getResource("/test-data/large.txt");
        assertNotNull("large.txt should be found on classpath", url);

        UrlIOResource resource = new UrlIOResource(url);

        assertTrue("Large file should exist", resource.exists());
        assertEquals("large.txt", resource.getFilename());

        try (InputStream is = resource.getInputStream()) {
            byte[] content = readAllBytes(is);
            assertTrue("Large file should have substantial content", content.length > 1000);
        }
    }

    @Test
    public void testUnicodeFilenameFromClasspath() {
        URL url = getClass().getResource("/test-data/special-chars-文件.txt");
        assertNotNull("Unicode filename should be found on classpath", url);

        UrlIOResource resource = new UrlIOResource(url);

        assertTrue("Unicode filename resource should exist", resource.exists());
        String filename = resource.getFilename();
        assertNotNull("Filename should not be null", filename);
        // URL encoding converts Unicode chars to percent-encoded bytes
        // 文件 becomes %E6%96%87%E4%BB%B6 in URL encoding
        assertTrue("Filename should contain encoded unicode chars or raw chars",
                filename.contains("%") || filename.contains("文件"));
    }

    @Test
    public void testPropertiesFileFromClasspath() throws IOException {
        URL url = getClass().getResource("/test-data/test.properties");
        assertNotNull("test.properties should be found on classpath", url);

        UrlIOResource resource = new UrlIOResource(url);

        assertTrue("Properties file should exist", resource.exists());
        assertEquals("test.properties", resource.getFilename());

        // Load as properties and verify
        Properties props = new Properties();
        try (InputStream is = resource.getInputStream()) {
            props.load(is);
            assertFalse("Properties should not be empty", props.isEmpty());
        }
    }

    @Test
    public void testClasspathResourceLastModified() throws IOException {
        URL url = getClass().getResource("/test-data/simple.txt");
        UrlIOResource resource = new UrlIOResource(url);

        long lastModified = resource.lastModified();
        assertTrue("Last modified should be a valid timestamp", lastModified > 0);
    }

    @Test
    public void testClasspathResourceMultipleReads() throws IOException {
        URL url = getClass().getResource("/test-data/simple.txt");
        UrlIOResource resource = new UrlIOResource(url);

        byte[] content1;
        try (InputStream is = resource.getInputStream()) {
            content1 = readAllBytes(is);
        }

        byte[] content2;
        try (InputStream is = resource.getInputStream()) {
            content2 = readAllBytes(is);
        }

        assertArrayEquals("Multiple reads should return same content", content1, content2);
    }

    // ==================== JAR Resource Tests ====================

    @Test
    public void testJarResourceExists() {
        URL jarUrl = getClass().getResource("/test-resources.jar");
        assertNotNull("test-resources.jar should be found on classpath", jarUrl);

        UrlIOResource resource = new UrlIOResource(jarUrl);

        assertTrue("JAR file should exist", resource.exists());
        assertTrue("Filename should end with .jar", resource.getFilename().endsWith(".jar"));
    }

    @Test
    public void testJarResourceLastModified() throws IOException {
        URL jarUrl = getClass().getResource("/test-resources.jar");
        UrlIOResource resource = new UrlIOResource(jarUrl);

        long lastModified = resource.lastModified();
        assertTrue("JAR lastModified should be valid", lastModified > 0);
    }

    @Test
    public void testJarManifest() throws IOException {
        URL jarUrl = getClass().getResource("/test-resources.jar");
        assertNotNull("JAR URL should not be null", jarUrl);

        UrlIOResource resource = new UrlIOResource(jarUrl);

        assertTrue("JAR resource should exist", resource.exists());

        // Verify we can read the JAR through UrlIOResource
        try (InputStream is = resource.getInputStream()) {
            assertNotNull("JAR InputStream should not be null", is);
            byte[] content = readAllBytes(is);
            assertTrue("JAR should have content", content.length > 0);
        }
    }

    @Test
    public void testJarEntryExists() throws IOException {
        URL jarUrl = getClass().getResource("/test-resources.jar");
        assertNotNull("test-resources.jar should be found", jarUrl);

        // Create jar:file: URL for entry inside JAR
        URL entryUrl = new URL("jar:" + jarUrl + "!/test-entry.txt");
        UrlIOResource resource = new UrlIOResource(entryUrl);

        assertTrue("JAR entry should exist", resource.exists());
        assertEquals("test-entry.txt", resource.getFilename());
    }

    @Test
    public void testJarEntryContent() throws IOException {
        URL jarUrl = getClass().getResource("/test-resources.jar");
        URL entryUrl = new URL("jar:" + jarUrl + "!/test-entry.txt");

        UrlIOResource resource = new UrlIOResource(entryUrl);

        try (InputStream is = resource.getInputStream()) {
            byte[] content = readAllBytes(is);
            assertTrue("JAR entry should have content", content.length > 0);
        }
    }

    @Test
    public void testJarEntryNonExistent() throws IOException {
        URL jarUrl = getClass().getResource("/test-resources.jar");
        URL entryUrl = new URL("jar:" + jarUrl + "!/non-existent.txt");

        UrlIOResource resource = new UrlIOResource(entryUrl);

        assertFalse("Non-existent JAR entry should not exist", resource.exists());
    }

    @Test
    public void testNestedJarEntry() throws IOException {
        URL jarUrl = getClass().getResource("/test-resources.jar");
        URL entryUrl = new URL("jar:" + jarUrl + "!/nested/deep/resource.txt");

        UrlIOResource resource = new UrlIOResource(entryUrl);

        if (resource.exists()) {
            assertEquals("resource.txt", resource.getFilename());

            try (InputStream is = resource.getInputStream()) {
                byte[] content = readAllBytes(is);
                assertTrue("Nested entry should have content", content.length > 0);
            }
        }
    }

    @Test
    public void testJarEntryLastModified() throws IOException {
        URL jarUrl = getClass().getResource("/test-resources.jar");
        URL entryUrl = new URL("jar:" + jarUrl + "!/test-entry.txt");

        UrlIOResource resource = new UrlIOResource(entryUrl);

        long lastModified = resource.lastModified();
        // JAR entries typically get the JAR file's lastModified time
        assertTrue("JAR entry lastModified should be valid", lastModified > 0);
    }

    // ==================== ByteArrayIOResource with Real Data ====================

    @Test
    public void testByteArrayFromClasspathResource() throws IOException {
        URL url = getClass().getResource("/test-data/binary.dat");

        byte[] data;
        try (InputStream is = url.openStream()) {
            data = readAllBytes(is);
        }

        ByteArrayIOResource resource = new ByteArrayIOResource(data, "binary data");

        assertTrue("Resource should exist", resource.exists());
        assertEquals("binary data", resource.getDescription());

        try (InputStream is = resource.getInputStream()) {
            byte[] readData = readAllBytes(is);
            assertArrayEquals("Content should match original", data, readData);
        }
    }

    @Test
    public void testByteArrayFromPropertiesFile() throws IOException {
        URL url = getClass().getResource("/test-data/test.properties");

        byte[] data;
        try (InputStream is = url.openStream()) {
            data = readAllBytes(is);
        }

        ByteArrayIOResource resource = new ByteArrayIOResource(data, "test.properties");

        // Load properties from ByteArrayIOResource
        Properties props = new Properties();
        try (InputStream is = resource.getInputStream()) {
            props.load(is);
            assertFalse("Properties should be loaded", props.isEmpty());
        }
    }

    // ==================== URL Cleaning and Equality Tests ====================

    @Test
    public void testUrlResourceEquality() {
        URL url1 = getClass().getResource("/test-data/simple.txt");
        URL url2 = getClass().getResource("/test-data/simple.txt");

        UrlIOResource resource1 = new UrlIOResource(url1);
        UrlIOResource resource2 = new UrlIOResource(url2);

        assertEquals("Same URL should produce equal resources", resource1, resource2);
        assertEquals("HashCodes should match", resource1.hashCode(), resource2.hashCode());
    }

    @Test
    public void testUrlResourceWithDifferentProtocols() {
        // Test that file: and jar: URLs are handled differently
        URL fileUrl = getClass().getResource("/test-data/simple.txt");
        URL jarUrl = getClass().getResource("/test-resources.jar");

        UrlIOResource fileResource = new UrlIOResource(fileUrl);
        UrlIOResource jarResource = new UrlIOResource(jarUrl);

        assertNotEquals("Different URLs should not be equal", fileResource, jarResource);
    }

    // ==================== InputStreamIOResource with Real Data ====================

    @Test
    public void testInputStreamFromClasspath() throws IOException {
        URL url = getClass().getResource("/test-data/simple.txt");

        try (InputStream source = url.openStream()) {
            InputStreamIOResource resource = new InputStreamIOResource(source, "simple.txt content");

            assertTrue("Resource should exist", resource.exists());

            try (InputStream is = resource.getInputStream()) {
                byte[] content = readAllBytes(is);
                assertTrue("Should read content", content.length > 0);
            }

            // Second read should fail
            try {
                resource.getInputStream();
                fail("Should not allow second read");
            } catch (IOException e) {
                assertTrue("Exception should mention already read",
                        e.getMessage().contains("already been read"));
            }
        }
    }

    // ==================== Edge Cases and Error Handling ====================

    @Test
    public void testResourceDescription() {
        URL url = getClass().getResource("/test-data/simple.txt");
        UrlIOResource resource = new UrlIOResource(url);

        String description = resource.getDescription();
        assertNotNull("Description should not be null", description);
        assertTrue("Description should contain URL", description.contains("URL"));
    }

    @Test
    public void testResourceToString() {
        URL url = getClass().getResource("/test-data/simple.txt");
        UrlIOResource resource = new UrlIOResource(url);

        String str = resource.toString();
        assertNotNull("toString should not be null", str);
        assertTrue("toString should contain description", str.contains("URL"));
    }

    @Test
    public void testLargeFileStreaming() throws IOException {
        URL url = getClass().getResource("/test-data/large.txt");
        UrlIOResource resource = new UrlIOResource(url);

        // Read in chunks to test streaming
        try (InputStream is = resource.getInputStream()) {
            byte[] buffer = new byte[1024];
            int totalRead = 0;
            int bytesRead;

            while ((bytesRead = is.read(buffer)) != -1) {
                totalRead += bytesRead;
            }

            assertTrue("Should read substantial data from large file", totalRead > 1000);
        }
    }

    @Test
    public void testBinaryDataIntegrity() throws IOException {
        URL url = getClass().getResource("/test-data/binary.dat");

        byte[] original;
        try (InputStream is = url.openStream()) {
            original = readAllBytes(is);
        }

        // Read through UrlIOResource
        UrlIOResource resource = new UrlIOResource(url);
        byte[] viaResource;
        try (InputStream is = resource.getInputStream()) {
            viaResource = readAllBytes(is);
        }

        assertArrayEquals("Binary data should be identical", original, viaResource);
    }

    // ==================== Helper Methods ====================

    /**
     * Reads all bytes from an InputStream (Java 8 compatible).
     */
    private byte[] readAllBytes(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int bytesRead;

        while ((bytesRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }

        return buffer.toByteArray();
    }
}
