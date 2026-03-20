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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

/**
 * Unit test for the {@link ByteArrayIOResource}.
 */
public class ByteArrayIOResourceTest {

    @Test
    public void testGetInputStream() throws IOException {
        byte[] testData = "Test content".getBytes();
        ByteArrayIOResource resource = new ByteArrayIOResource(testData);

        try (InputStream is = resource.getInputStream()) {
            byte[] result = new byte[testData.length];
            int bytesRead = is.read(result);
            assertEquals("Should read all bytes", testData.length, bytesRead);
            assertArrayEquals("Content should match", testData, result);
        }
    }

    @Test
    public void testGetInputStreamMultipleTimes() throws IOException {
        byte[] testData = "Test content".getBytes();
        ByteArrayIOResource resource = new ByteArrayIOResource(testData);

        // Should be able to read multiple times
        try (InputStream is1 = resource.getInputStream()) {
            assertNotNull("First stream should not be null", is1);
        }

        try (InputStream is2 = resource.getInputStream()) {
            assertNotNull("Second stream should not be null", is2);
        }
    }

    @Test
    public void testGetByteArray() {
        byte[] testData = "Test content".getBytes();
        ByteArrayIOResource resource = new ByteArrayIOResource(testData);

        byte[] result = resource.getByteArray();
        assertArrayEquals("Byte array should match", testData, result);
    }

    @Test
    public void testExists() {
        ByteArrayIOResource resource = new ByteArrayIOResource(new byte[0]);
        assertTrue("Resource should always exist", resource.exists());
    }

    @Test
    public void testLastModified() {
        ByteArrayIOResource resource = new ByteArrayIOResource(new byte[10]);
        assertEquals("Last modified should be 0", 0L, resource.lastModified());
    }

    @Test
    public void testGetURL() {
        ByteArrayIOResource resource = new ByteArrayIOResource(new byte[10]);
        try {
            resource.getURL();
            fail("Should throw IOException");
        } catch (IOException e) {
            assertTrue("Exception message should mention URL", e.getMessage().contains("URL"));
        }
    }

    @Test
    public void testGetURI() {
        ByteArrayIOResource resource = new ByteArrayIOResource(new byte[10]);
        try {
            resource.getURI();
            fail("Should throw IOException");
        } catch (IOException e) {
            assertTrue("Exception message should mention URI", e.getMessage().contains("URI"));
        }
    }

    @Test
    public void testGetFile() {
        ByteArrayIOResource resource = new ByteArrayIOResource(new byte[10]);
        try {
            resource.getFile();
            fail("Should throw IOException");
        } catch (IOException e) {
            assertTrue("Exception message should mention File", e.getMessage().contains("File"));
        }
    }

    @Test
    public void testGetFilename() {
        ByteArrayIOResource resource = new ByteArrayIOResource(new byte[10]);
        assertNull("Filename should be null", resource.getFilename());
    }

    @Test
    public void testGetDescription() {
        ByteArrayIOResource resource = new ByteArrayIOResource(new byte[10]);
        assertEquals("Default description", "Byte array resource", resource.getDescription());
    }

    @Test
    public void testGetDescriptionCustom() {
        ByteArrayIOResource resource = new ByteArrayIOResource(new byte[10], "Custom description");
        assertEquals("Should have custom description", "Custom description", resource.getDescription());
    }

    @Test
    public void testToString() {
        ByteArrayIOResource resource = new ByteArrayIOResource(new byte[10], "Test resource");
        assertEquals("toString should return description", "Test resource", resource.toString());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullBytes() {
        new ByteArrayIOResource(null);
    }

    @Test
    public void testConstructorNullDescription() {
        ByteArrayIOResource resource = new ByteArrayIOResource(new byte[10], null);
        assertEquals("Default description should be used", "Byte array resource", resource.getDescription());
    }

    @Test
    public void testEmptyByteArray() throws IOException {
        ByteArrayIOResource resource = new ByteArrayIOResource(new byte[0]);
        try (InputStream is = resource.getInputStream()) {
            assertEquals("Empty byte array should return -1", -1, is.read());
        }
    }
}
