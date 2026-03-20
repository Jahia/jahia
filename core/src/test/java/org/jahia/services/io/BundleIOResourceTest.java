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
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Test;
import org.osgi.framework.Bundle;

/**
 * Unit test for the {@link BundleIOResource}.
 */
@SuppressWarnings("java:S1874")
public class BundleIOResourceTest {

    @Test
    public void testGetBundle() throws Exception {
        Bundle bundle = createMockBundle("test.bundle", 12345L);
        URL url = createMockURL("bundle://test/entry.txt");

        BundleIOResource resource = new BundleIOResource(url, bundle);
        assertEquals("Bundle should match", bundle, resource.getBundle());
    }

    @Test
    public void testGetURL() throws Exception {
        Bundle bundle = createMockBundle("test.bundle", 12345L);
        URL url = createMockURL("bundle://test/entry.txt");

        BundleIOResource resource = new BundleIOResource(url, bundle);
        assertEquals("URL should match", url, resource.getURL());
    }

    @Test
    public void testLastModifiedFromBundleHeader() throws Exception {
        long expectedTime = 1234567890L;
        Bundle bundle = createMockBundle("test.bundle", 99999L);
        Dictionary<String, String> headers = new Hashtable<>();
        headers.put("Bnd-LastModified", String.valueOf(expectedTime));
        when(bundle.getHeaders()).thenReturn(headers);

        URL url = createMockURL("bundle://test/entry.txt");
        BundleIOResource resource = new BundleIOResource(url, bundle);

        assertEquals("Should use Bnd-LastModified header", expectedTime, resource.lastModified());
    }

    @Test
    public void testLastModifiedFromBundleTimestamp() throws Exception {
        long expectedTime = 1234567890L;
        Bundle bundle = createMockBundle("test.bundle", expectedTime);
        Dictionary<String, String> headers = new Hashtable<>();
        when(bundle.getHeaders()).thenReturn(headers);

        URL url = createMockURL("bundle://test/entry.txt");
        BundleIOResource resource = new BundleIOResource(url, bundle);

        assertEquals("Should use bundle lastModified", expectedTime, resource.lastModified());
    }

    @Test
    public void testLastModifiedWithInvalidHeader() throws Exception {
        long expectedTime = 1234567890L;
        Bundle bundle = createMockBundle("test.bundle", expectedTime);
        Dictionary<String, String> headers = new Hashtable<>();
        headers.put("Bnd-LastModified", "invalid-number");
        when(bundle.getHeaders()).thenReturn(headers);

        URL url = createMockURL("bundle://test/entry.txt");
        BundleIOResource resource = new BundleIOResource(url, bundle);

        assertEquals("Should fall back to bundle lastModified", expectedTime, resource.lastModified());
    }

    @Test
    public void testExists() throws Exception {
        Bundle bundle = createMockBundle("test.bundle", 12345L);
        URL url = createTestURLWithContent("bundle://test/entry.txt", "test content");

        BundleIOResource resource = new BundleIOResource(url, bundle);
        assertTrue("Resource should exist", resource.exists());
    }

    @Test
    public void testExistsNonExistent() throws Exception {
        Bundle bundle = createMockBundle("test.bundle", 12345L);
        URL url = createNonExistentURL("bundle://test/missing.txt");

        BundleIOResource resource = new BundleIOResource(url, bundle);
        assertFalse("Resource should not exist", resource.exists());
    }

    @Test
    public void testGetDescription() throws Exception {
        Bundle bundle = createMockBundle("test.bundle", 12345L);
        URL url = createMockURL("bundle://test/entry.txt");

        BundleIOResource resource = new BundleIOResource(url, bundle);
        String description = resource.getDescription();

        assertNotNull("Description should not be null", description);
        assertTrue("Description should contain bundle name", description.contains("test.bundle"));
        assertTrue("Description should contain URL", description.contains(url.toString()));
    }

    @Test
    public void testEquals() throws Exception {
        Bundle bundle = createMockBundle("test.bundle", 12345L);
        URL url1 = createMockURL("bundle://test/entry.txt");
        URL url2 = createMockURL("bundle://test/entry.txt");

        BundleIOResource resource1 = new BundleIOResource(url1, bundle);
        BundleIOResource resource2 = new BundleIOResource(url2, bundle);

        assertEquals("Resources with same bundle and path should be equal", resource1, resource2);
    }

    @Test
    public void testEqualsSameInstance() throws Exception {
        Bundle bundle = createMockBundle("test.bundle", 12345L);
        URL url = createMockURL("bundle://test/entry.txt");

        BundleIOResource resource = new BundleIOResource(url, bundle);
        assertEquals("Resource should equal itself", resource, resource);
    }

    @Test
    public void testNotEqualsDifferentBundle() throws Exception {
        Bundle bundle1 = createMockBundle("test.bundle1", 12345L);
        Bundle bundle2 = createMockBundle("test.bundle2", 12345L);
        URL url1 = createMockURL("bundle://test/entry.txt");
        URL url2 = createMockURL("bundle://test/entry.txt");

        BundleIOResource resource1 = new BundleIOResource(url1, bundle1);
        BundleIOResource resource2 = new BundleIOResource(url2, bundle2);

        assertNotEquals("Resources with different bundles should not be equal", resource1, resource2);
    }

    @Test
    public void testNotEqualsDifferentPath() throws Exception {
        Bundle bundle = createMockBundle("test.bundle", 12345L);
        URL url1 = createMockURL("bundle://test/entry1.txt");
        URL url2 = createMockURL("bundle://test/entry2.txt");

        BundleIOResource resource1 = new BundleIOResource(url1, bundle);
        BundleIOResource resource2 = new BundleIOResource(url2, bundle);

        assertNotEquals("Resources with different paths should not be equal", resource1, resource2);
    }

    @Test
    public void testHashCode() throws Exception {
        Bundle bundle = createMockBundle("test.bundle", 12345L);
        URL url1 = createMockURL("bundle://test/entry.txt");
        URL url2 = createMockURL("bundle://test/entry.txt");

        BundleIOResource resource1 = new BundleIOResource(url1, bundle);
        BundleIOResource resource2 = new BundleIOResource(url2, bundle);

        assertEquals("HashCodes should match for equal resources", resource1.hashCode(), resource2.hashCode());
    }

    @Test
    public void testHashCodeDifferentBundle() throws Exception {
        Bundle bundle1 = createMockBundle("test.bundle1", 12345L);
        Bundle bundle2 = createMockBundle("test.bundle2", 12345L);
        URL url1 = createMockURL("bundle://test/entry.txt");
        URL url2 = createMockURL("bundle://test/entry.txt");

        BundleIOResource resource1 = new BundleIOResource(url1, bundle1);
        BundleIOResource resource2 = new BundleIOResource(url2, bundle2);

        assertNotEquals("HashCodes should differ for different bundles", resource1.hashCode(), resource2.hashCode());
    }

    // Helper methods

    private Bundle createMockBundle(String symbolicName, long lastModified) {
        Bundle bundle = mock(Bundle.class);
        when(bundle.getSymbolicName()).thenReturn(symbolicName);
        when(bundle.getLastModified()).thenReturn(lastModified);
        return bundle;
    }

    private URL createMockURL(String spec) throws Exception {
        return new URL(null, spec, new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                URLConnection conn = mock(URLConnection.class);
                when(conn.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
                return conn;
            }
        });
    }

    private URL createTestURLWithContent(String spec, String content) throws Exception {
        return new URL(null, spec, new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                URLConnection conn = mock(URLConnection.class);
                when(conn.getInputStream()).thenReturn(new ByteArrayInputStream(content.getBytes()));
                return conn;
            }
        });
    }

    private URL createNonExistentURL(String spec) throws Exception {
        return new URL(null, spec, new URLStreamHandler() {
            @Override
            protected URLConnection openConnection(URL u) throws IOException {
                URLConnection conn = mock(URLConnection.class);
                when(conn.getInputStream()).thenThrow(new IOException("Resource not found"));
                return conn;
            }
        });
    }
}
