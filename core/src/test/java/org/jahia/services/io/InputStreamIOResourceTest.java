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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for the {@link InputStreamIOResource}.
 */
public class InputStreamIOResourceTest {

    @Test
    public void testGetInputStream() throws IOException {
        byte[] testData = "Test content".getBytes();
        InputStream sourceStream = new ByteArrayInputStream(testData);
        InputStreamIOResource resource = new InputStreamIOResource(sourceStream);

        InputStream resultStream = resource.getInputStream();
        assertNotNull("InputStream should not be null", resultStream);
        assertEquals("Should be the same stream", sourceStream, resultStream);
    }

    @Test
    public void testGetInputStreamOnlyOnce() throws IOException {
        byte[] testData = "Test content".getBytes();
        InputStream sourceStream = new ByteArrayInputStream(testData);
        InputStreamIOResource resource = new InputStreamIOResource(sourceStream);

        // First call should succeed
        resource.getInputStream();

        // Second call should fail
        try {
            resource.getInputStream();
            fail("Should throw IOException when reading stream twice");
        } catch (IOException e) {
            assertTrue("Exception message should mention already read",
                    e.getMessage().contains("already been read"));
        }
    }

    @Test
    public void testExists() {
        InputStream sourceStream = new ByteArrayInputStream(new byte[10]);
        InputStreamIOResource resource = new InputStreamIOResource(sourceStream);
        assertTrue("Resource should always exist", resource.exists());
    }

    @Test
    public void testGetURL() {
        InputStream sourceStream = new ByteArrayInputStream(new byte[10]);
        InputStreamIOResource resource = new InputStreamIOResource(sourceStream);
        try {
            resource.getURL();
            fail("Should throw IOException");
        } catch (IOException e) {
            assertTrue("Exception message should mention URL", e.getMessage().contains("URL"));
        }
    }

    @Test
    public void testGetURI() {
        InputStream sourceStream = new ByteArrayInputStream(new byte[10]);
        InputStreamIOResource resource = new InputStreamIOResource(sourceStream);
        try {
            resource.getURI();
            fail("Should throw IOException");
        } catch (IOException e) {
            assertTrue("Exception message should mention URI", e.getMessage().contains("URI"));
        }
    }

    @Test
    public void testGetFile() {
        InputStream sourceStream = new ByteArrayInputStream(new byte[10]);
        InputStreamIOResource resource = new InputStreamIOResource(sourceStream);
        try {
            resource.getFile();
            fail("Should throw IOException");
        } catch (IOException e) {
            assertTrue("Exception message should mention File", e.getMessage().contains("File"));
        }
    }

    @Test
    public void testLastModified() {
        InputStream sourceStream = new ByteArrayInputStream(new byte[10]);
        InputStreamIOResource resource = new InputStreamIOResource(sourceStream);
        assertEquals("Last modified should be 0", 0L, resource.lastModified());
    }

    @Test
    public void testGetFilename() {
        InputStream sourceStream = new ByteArrayInputStream(new byte[10]);
        InputStreamIOResource resource = new InputStreamIOResource(sourceStream);
        assertNull("Filename should be null", resource.getFilename());
    }

    @Test
    public void testGetDescription() {
        InputStream sourceStream = new ByteArrayInputStream(new byte[10]);
        InputStreamIOResource resource = new InputStreamIOResource(sourceStream);
        assertEquals("Default description", "InputStream resource", resource.getDescription());
    }

    @Test
    public void testGetDescriptionCustom() {
        InputStream sourceStream = new ByteArrayInputStream(new byte[10]);
        InputStreamIOResource resource = new InputStreamIOResource(sourceStream, "Custom InputStream");
        assertEquals("Custom description", "Custom InputStream", resource.getDescription());
    }

    @Test
    public void testToString() {
        InputStream sourceStream = new ByteArrayInputStream(new byte[10]);
        InputStreamIOResource resource = new InputStreamIOResource(sourceStream, "Test InputStream");
        assertEquals("toString should return description", "Test InputStream", resource.toString());
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullInputStream() {
        new InputStreamIOResource(null);
    }

    @Test
    public void testConstructorNullDescription() {
        InputStream sourceStream = new ByteArrayInputStream(new byte[10]);
        InputStreamIOResource resource = new InputStreamIOResource(sourceStream, null);
        assertEquals("Default description should be used", "InputStream resource", resource.getDescription());
    }

    @Test
    public void testReadContent() throws IOException {
        byte[] testData = "Hello World!".getBytes();
        InputStream sourceStream = new ByteArrayInputStream(testData);
        InputStreamIOResource resource = new InputStreamIOResource(sourceStream);

        try (InputStream is = resource.getInputStream()) {
            byte[] buffer = new byte[testData.length];
            int bytesRead = is.read(buffer);
            assertEquals("Should read correct number of bytes", testData.length, bytesRead);
        }
    }
}
