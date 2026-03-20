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
package org.jahia.services.io.adapter;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jahia.api.io.IOResource;
import org.jahia.osgi.BundleResource;
import org.jahia.services.io.BundleIOResource;
import org.jahia.services.io.ByteArrayIOResource;
import org.jahia.services.io.FileSystemIOResource;
import org.jahia.services.io.InputStreamIOResource;
import org.jahia.services.io.UrlIOResource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

/**
 * Unit test for the {@link SpringResourceAdapter}.
 */
public class SpringResourceAdapterTest {

    private File tempFile;
    private File tempDir;

    @Before
    public void setUp() throws IOException {
        tempDir = Files.createTempDirectory("adapter-test").toFile();
        tempFile = new File(tempDir, "test-file.txt");
        try (FileOutputStream fos = new FileOutputStream(tempFile)) {
            fos.write("Test content for adapter".getBytes());
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

    // ==================== Adapter Delegation Tests ====================

    @Test
    public void testAdapterGetURL() throws IOException {
        Resource springResource = new FileSystemResource(tempFile);
        IOResource adapter = SpringResourceAdapter.fromSpring(springResource);

        // FileSystemResource gets converted to FileSystemIOResource, not wrapped in adapter
        assertTrue("Should be converted to FileSystemIOResource", adapter instanceof FileSystemIOResource);
        URL url = adapter.getURL();
        assertNotNull("URL should not be null", url);
        assertEquals("URL should match", tempFile.toURI().toURL(), url);
    }

    @Test
    public void testAdapterGetURI() throws IOException {
        Resource springResource = new FileSystemResource(tempFile);
        IOResource adapter = SpringResourceAdapter.fromSpring(springResource);

        URI uri = adapter.getURI();
        assertNotNull("URI should not be null", uri);
    }

    @Test
    public void testAdapterGetFile() throws IOException {
        Resource springResource = new FileSystemResource(tempFile);
        IOResource adapter = SpringResourceAdapter.fromSpring(springResource);

        File file = adapter.getFile();
        assertNotNull("File should not be null", file);
        assertEquals("File should match", tempFile, file);
    }

    @Test
    public void testAdapterGetInputStream() throws IOException {
        Resource springResource = new FileSystemResource(tempFile);
        IOResource adapter = SpringResourceAdapter.fromSpring(springResource);

        try (InputStream is = adapter.getInputStream()) {
            assertNotNull("InputStream should not be null", is);
            byte[] buffer = new byte[24];
            int bytesRead = is.read(buffer);
            assertEquals("Should read correct bytes", 24, bytesRead);
            assertEquals("Content should match", "Test content for adapter", new String(buffer));
        }
    }

    @Test
    public void testAdapterExists() {
        Resource springResource = new FileSystemResource(tempFile);
        IOResource adapter = SpringResourceAdapter.fromSpring(springResource);

        assertTrue("Resource should exist", adapter.exists());
    }

    @Test
    public void testAdapterLastModified() throws IOException {
        Resource springResource = new FileSystemResource(tempFile);
        IOResource adapter = SpringResourceAdapter.fromSpring(springResource);

        long lastModified = adapter.lastModified();
        assertTrue("Last modified should be greater than 0", lastModified > 0);
    }

    @Test
    public void testAdapterGetFilename() {
        Resource springResource = new FileSystemResource(tempFile);
        IOResource adapter = SpringResourceAdapter.fromSpring(springResource);

        assertEquals("Filename should match", "test-file.txt", adapter.getFilename());
    }

    @Test
    public void testAdapterGetDescription() {
        Resource springResource = new FileSystemResource(tempFile);
        IOResource adapter = SpringResourceAdapter.fromSpring(springResource);

        String description = adapter.getDescription();
        assertNotNull("Description should not be null", description);
    }

    @Test
    public void testAdapterToString() {
        Resource springResource = new FileSystemResource(tempFile);
        IOResource adapter = SpringResourceAdapter.fromSpring(springResource);

        String toString = adapter.toString();
        assertNotNull("toString should not be null", toString);
        // FileSystemIOResource toString contains file path, not SpringAdapter
        assertTrue("toString should contain file info", toString.contains("file"));
    }

    @Test
    public void testAdapterEquals() {
        Resource springResource1 = new FileSystemResource(tempFile);
        Resource springResource2 = new FileSystemResource(tempFile);

        IOResource adapter1 = SpringResourceAdapter.fromSpring(springResource1);
        IOResource adapter2 = SpringResourceAdapter.fromSpring(springResource2);

        assertEquals("Adapters with same delegate should be equal", adapter1, adapter2);
    }

    @Test
    public void testAdapterEqualsSameInstance() {
        Resource springResource = new FileSystemResource(tempFile);
        IOResource adapter = SpringResourceAdapter.fromSpring(springResource);

        assertEquals("Adapter should equal itself", adapter, adapter);
    }

    @Test
    public void testAdapterHashCode() {
        Resource springResource1 = new FileSystemResource(tempFile);
        Resource springResource2 = new FileSystemResource(tempFile);

        IOResource adapter1 = SpringResourceAdapter.fromSpring(springResource1);
        IOResource adapter2 = SpringResourceAdapter.fromSpring(springResource2);

        assertEquals("HashCodes should match", adapter1.hashCode(), adapter2.hashCode());
    }

    @Test
    public void testAdapterGetDelegate() throws IOException {
        // Use a resource type that gets wrapped in SpringResourceAdapter (not converted)
        // UrlResource might fail getURL(), so it falls back to wrapping
        Resource customResource = mock(Resource.class);
        when(customResource.getURL()).thenThrow(new IOException("URL not available"));
        when(customResource.getDescription()).thenReturn("Custom resource");

        IOResource adapter = SpringResourceAdapter.fromSpring(customResource);

        assertTrue("Should be SpringResourceAdapter", adapter instanceof SpringResourceAdapter);
        Resource delegate = ((SpringResourceAdapter) adapter).getDelegate();
        assertEquals("Delegate should match", customResource, delegate);
    }

    // ==================== fromSpring() Conversion Tests ====================

    @Test
    public void testFromSpringNull() {
        IOResource result = SpringResourceAdapter.fromSpring((Resource) null);
        assertNull("Null resource should return null", result);
    }

    @Test
    public void testFromSpringIOResource() {
        // Create a mock Resource that is also an IOResource
        // This tests the case where a Resource implementation also implements IOResource
        IOResource mockIOResource = mock(IOResource.class, withSettings().extraInterfaces(Resource.class));

        IOResource result = SpringResourceAdapter.fromSpring((Resource) mockIOResource);

        assertEquals("Should return same instance", mockIOResource, result);
    }

    @Test
    public void testFromSpringFileSystemResource() throws IOException {
        FileSystemResource springResource = new FileSystemResource(tempFile);
        IOResource result = SpringResourceAdapter.fromSpring(springResource);

        assertTrue("Should convert to FileSystemIOResource", result instanceof FileSystemIOResource);
        assertEquals("File should match", tempFile, result.getFile());
    }

    @Test
    public void testFromSpringByteArrayResource() {
        byte[] testData = "Test data".getBytes();
        ByteArrayResource springResource = new ByteArrayResource(testData, "Test description");
        IOResource result = SpringResourceAdapter.fromSpring(springResource);

        assertTrue("Should convert to ByteArrayIOResource", result instanceof ByteArrayIOResource);
        assertArrayEquals("Byte array should match", testData, ((ByteArrayIOResource) result).getByteArray());
        assertEquals("Description should match", "Test description", result.getDescription());
    }

    @Test
    public void testFromSpringInputStreamResource() {
        byte[] testData = "Test stream data".getBytes();
        InputStreamResource springResource = new InputStreamResource(new ByteArrayInputStream(testData), "Stream desc");
        IOResource result = SpringResourceAdapter.fromSpring(springResource);

        assertTrue("Should convert to InputStreamIOResource", result instanceof InputStreamIOResource);
        assertEquals("Description should match", "Stream desc", result.getDescription());
    }

    @Test
    public void testFromSpringInputStreamResourceIOException() throws IOException {
        InputStreamResource springResource = mock(InputStreamResource.class);
        when(springResource.getInputStream()).thenThrow(new IOException("Test exception"));
        when(springResource.getDescription()).thenReturn("Failed stream");

        try {
            SpringResourceAdapter.fromSpring(springResource);
            fail("Should throw UncheckedIOException");
        } catch (UncheckedIOException e) {
            assertTrue("Exception message should contain description",
                    e.getMessage().contains("Failed stream"));
        }
    }

    @Test
    public void testFromSpringUrlResource() throws IOException {
        UrlResource springResource = new UrlResource(tempFile.toURI().toURL());
        IOResource result = SpringResourceAdapter.fromSpring(springResource);

        assertTrue("Should convert to UrlIOResource", result instanceof UrlIOResource);
    }

    @Test
    public void testFromSpringGenericResourceFallback() throws IOException {
        Resource customResource = mock(Resource.class);
        when(customResource.getURL()).thenThrow(new IOException("URL not available"));
        when(customResource.getDescription()).thenReturn("Custom resource");
        when(customResource.exists()).thenReturn(true);

        IOResource result = SpringResourceAdapter.fromSpring(customResource);

        assertTrue("Should fallback to SpringResourceAdapter", result instanceof SpringResourceAdapter);
        assertTrue("Should exist", result.exists());
    }

    @Test
    public void testFromSpringCollection() {
        List<Resource> springResources = new ArrayList<>();
        springResources.add(new FileSystemResource(tempFile));
        springResources.add(new ByteArrayResource("test".getBytes()));

        List<IOResource> result = SpringResourceAdapter.fromSpring(springResources);

        assertNotNull("Result should not be null", result);
        assertEquals("Size should match", 2, result.size());
        assertTrue("First should be FileSystemIOResource", result.get(0) instanceof FileSystemIOResource);
        assertTrue("Second should be ByteArrayIOResource", result.get(1) instanceof ByteArrayIOResource);
    }

    @Test
    public void testFromSpringCollectionNull() {
        List<IOResource> result = SpringResourceAdapter.fromSpring((List<Resource>) null);
        assertNull("Null collection should return null", result);
    }

    @Test
    public void testFromSpringArray() {
        Resource[] springResources = new Resource[] {
            new FileSystemResource(tempFile),
            new ByteArrayResource("test".getBytes())
        };

        IOResource[] result = SpringResourceAdapter.fromSpring(springResources);

        assertNotNull("Result should not be null", result);
        assertEquals("Length should match", 2, result.length);
        assertTrue("First should be FileSystemIOResource", result[0] instanceof FileSystemIOResource);
        assertTrue("Second should be ByteArrayIOResource", result[1] instanceof ByteArrayIOResource);
    }

    @Test
    public void testFromSpringArrayNull() {
        IOResource[] result = SpringResourceAdapter.fromSpring((Resource[]) null);
        assertNull("Null array should return null", result);
    }

    // ==================== toSpring() Conversion Tests ====================

    @Test
    public void testToSpringNull() {
        Resource result = SpringResourceAdapter.toSpring((IOResource) null);
        assertNull("Null resource should return null", result);
    }

    @Test
    public void testToSpringAdapter() {
        Resource original = new FileSystemResource(tempFile);
        IOResource adapter = SpringResourceAdapter.fromSpring(original);

        Resource result = SpringResourceAdapter.toSpring(adapter);

        assertEquals("Should unwrap to original delegate", original, result);
    }

    @Test
    public void testToSpringFileSystemIOResource() {
        FileSystemIOResource jahiaResource = new FileSystemIOResource(tempFile);
        Resource result = SpringResourceAdapter.toSpring(jahiaResource);

        assertTrue("Should convert to FileSystemResource", result instanceof FileSystemResource);
        assertEquals("File should match", tempFile, ((FileSystemResource) result).getFile());
    }

    @Test
    public void testToSpringByteArrayIOResource() {
        byte[] testData = "Test data".getBytes();
        ByteArrayIOResource jahiaResource = new ByteArrayIOResource(testData, "Test desc");
        Resource result = SpringResourceAdapter.toSpring(jahiaResource);

        assertTrue("Should convert to ByteArrayResource", result instanceof ByteArrayResource);
        assertArrayEquals("Byte array should match", testData, ((ByteArrayResource) result).getByteArray());
    }

    @Test
    @SuppressWarnings("java:S5738")
    public void testToSpringBundleIOResource() throws Exception {
        Bundle bundle = mock(Bundle.class);
        when(bundle.getSymbolicName()).thenReturn("test.bundle");
        when(bundle.getLastModified()).thenReturn(12345L);

        URL bundleUrl = tempFile.toURI().toURL();
        BundleIOResource jahiaResource = new BundleIOResource(bundleUrl, bundle);

        Resource result = SpringResourceAdapter.toSpring(jahiaResource);

        assertTrue("Should convert to BundleResource", result instanceof BundleResource);
    }

    @Test
    public void testToSpringInputStreamIOResource() {
        byte[] testData = "Stream data".getBytes();
        InputStreamIOResource jahiaResource = new InputStreamIOResource(
                new ByteArrayInputStream(testData), "Stream desc");

        Resource result = SpringResourceAdapter.toSpring(jahiaResource);

        assertTrue("Should convert to InputStreamResource", result instanceof InputStreamResource);
    }

    @Test
    public void testToSpringUrlIOResource() throws Exception {
        UrlIOResource jahiaResource = new UrlIOResource(tempFile.toURI().toURL());
        Resource result = SpringResourceAdapter.toSpring(jahiaResource);

        assertTrue("Should convert to UrlResource", result instanceof UrlResource);
    }

    @Test
    public void testToSpringIOExceptionHandling() throws IOException {
        IOResource failingResource = mock(IOResource.class);
        when(failingResource.getURL()).thenThrow(new IOException("Test failure"));
        when(failingResource.getDescription()).thenReturn("Failing resource");

        try {
            SpringResourceAdapter.toSpring(failingResource);
            fail("Should throw UncheckedIOException");
        } catch (UncheckedIOException e) {
            assertTrue("Exception message should contain description",
                    e.getMessage().contains("Failing resource"));
        }
    }

    @Test
    public void testToSpringCollection() {
        List<IOResource> jahiaResources = new ArrayList<>();
        jahiaResources.add(new FileSystemIOResource(tempFile));
        jahiaResources.add(new ByteArrayIOResource("test".getBytes()));

        List<Resource> result = SpringResourceAdapter.toSpring(jahiaResources);

        assertNotNull("Result should not be null", result);
        assertEquals("Size should match", 2, result.size());
        assertTrue("First should be FileSystemResource", result.get(0) instanceof FileSystemResource);
        assertTrue("Second should be ByteArrayResource", result.get(1) instanceof ByteArrayResource);
    }

    @Test
    public void testToSpringCollectionNull() {
        List<Resource> result = SpringResourceAdapter.toSpring((List<IOResource>) null);
        assertNull("Null collection should return null", result);
    }

    // ==================== Round-trip Conversion Tests ====================

    @Test
    public void testRoundTripFileSystemResource() {
        FileSystemResource original = new FileSystemResource(tempFile);
        IOResource converted = SpringResourceAdapter.fromSpring(original);
        Resource roundTrip = SpringResourceAdapter.toSpring(converted);

        assertTrue("Should be FileSystemResource", roundTrip instanceof FileSystemResource);
        assertEquals("File should match", tempFile, ((FileSystemResource) roundTrip).getFile());
    }

    @Test
    public void testRoundTripByteArrayResource() {
        byte[] testData = "Round trip test".getBytes();
        ByteArrayResource original = new ByteArrayResource(testData, "Round trip");
        IOResource converted = SpringResourceAdapter.fromSpring(original);
        Resource roundTrip = SpringResourceAdapter.toSpring(converted);

        assertTrue("Should be ByteArrayResource", roundTrip instanceof ByteArrayResource);
        assertArrayEquals("Byte array should match", testData, ((ByteArrayResource) roundTrip).getByteArray());
    }

    @Test
    public void testRoundTripFileSystemIOResource() throws IOException {
        FileSystemIOResource original = new FileSystemIOResource(tempFile);
        Resource converted = SpringResourceAdapter.toSpring(original);
        IOResource roundTrip = SpringResourceAdapter.fromSpring(converted);

        assertTrue("Should be FileSystemIOResource", roundTrip instanceof FileSystemIOResource);
        assertEquals("File should match", tempFile, roundTrip.getFile());
    }

    @Test
    public void testRoundTripByteArrayIOResource() {
        byte[] testData = "Round trip test".getBytes();
        ByteArrayIOResource original = new ByteArrayIOResource(testData, "Round trip");
        Resource converted = SpringResourceAdapter.toSpring(original);
        IOResource roundTrip = SpringResourceAdapter.fromSpring(converted);

        assertTrue("Should be ByteArrayIOResource", roundTrip instanceof ByteArrayIOResource);
        assertArrayEquals("Byte array should match", testData, ((ByteArrayIOResource) roundTrip).getByteArray());
    }

    // ==================== Edge Cases and Special Scenarios ====================

    @Test
    public void testAdapterEqualsDifferentType() {
        Resource springResource = new FileSystemResource(tempFile);
        IOResource adapter = SpringResourceAdapter.fromSpring(springResource);

        assertNotEquals("Adapter should not equal different type", adapter, "string");
    }

    @Test
    public void testAdapterEqualsNull() {
        Resource springResource = new FileSystemResource(tempFile);
        IOResource adapter = SpringResourceAdapter.fromSpring(springResource);

        assertNotEquals("Adapter should not equal null", adapter, null);
    }

    @Test
    public void testFromSpringEmptyCollection() {
        List<Resource> emptyList = new ArrayList<>();
        List<IOResource> result = SpringResourceAdapter.fromSpring(emptyList);

        assertNotNull("Result should not be null", result);
        assertEquals("Should be empty", 0, result.size());
    }

    @Test
    public void testFromSpringEmptyArray() {
        Resource[] emptyArray = new Resource[0];
        IOResource[] result = SpringResourceAdapter.fromSpring(emptyArray);

        assertNotNull("Result should not be null", result);
        assertEquals("Should be empty", 0, result.length);
    }

    @Test
    public void testToSpringEmptyCollection() {
        List<IOResource> emptyList = new ArrayList<>();
        List<Resource> result = SpringResourceAdapter.toSpring(emptyList);

        assertNotNull("Result should not be null", result);
        assertEquals("Should be empty", 0, result.size());
    }

    @Test
    public void testMultipleConversionsPreserveType() {
        FileSystemResource original = new FileSystemResource(tempFile);

        // Spring -> Jahia -> Spring
        IOResource step1 = SpringResourceAdapter.fromSpring(original);
        Resource step2 = SpringResourceAdapter.toSpring(step1);

        assertTrue("Final result should be FileSystemResource", step2 instanceof FileSystemResource);
        assertEquals("File should be preserved", tempFile, ((FileSystemResource) step2).getFile());
    }

    @Test
    public void testCollectionWithMixedTypes() throws MalformedURLException {
        List<Resource> mixed = Arrays.asList(
            new FileSystemResource(tempFile),
            new ByteArrayResource("test".getBytes()),
            new UrlResource(tempFile.toURI().toURL())
        );

        List<IOResource> converted = SpringResourceAdapter.fromSpring(mixed);

        assertEquals("Size should match", 3, converted.size());
        assertTrue("First is FileSystemIOResource", converted.get(0) instanceof FileSystemIOResource);
        assertTrue("Second is ByteArrayIOResource", converted.get(1) instanceof ByteArrayIOResource);
        assertTrue("Third is UrlIOResource", converted.get(2) instanceof UrlIOResource);
    }
}
