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

import org.jahia.api.io.IOResource;
import org.jahia.osgi.BundleResource;
import org.jahia.services.io.*;
import org.osgi.framework.Bundle;
import org.springframework.core.io.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adapts a Spring Resource to a Jahia Resource.
 * Internal use only — not exported via OSGi.
 * @deprecated this adapter will be removed with Spring Resource, use jahia Resource instead
 */
@Deprecated(since = "8.2.4.0", forRemoval = true)
public class SpringResourceAdapter implements IOResource {

    private final Resource delegate;

    /**
     * Private constructor to create an adapter for the given Spring Resource.
     * Private to enforce usage of the static factory method {@link #fromSpring(Resource)}.
     * @param delegate the Spring Resource to wrap
     */
    private SpringResourceAdapter(Resource delegate) {
        this.delegate = delegate;
    }

    @Override
    public URL getURL() throws IOException {
        return delegate.getURL();
    }

    @Override
    public URI getURI() throws IOException {
        return delegate.getURI();
    }

    @Override
    public File getFile() throws IOException {
        return delegate.getFile();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return delegate.getInputStream();
    }

    @Override
    public boolean exists() {
        return delegate.exists();
    }

    @Override
    public long lastModified() throws IOException {
        return delegate.lastModified();
    }

    @Override
    public String getFilename() {
        return delegate.getFilename();
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public String toString() {
        return "SpringAdapter{" + delegate.getDescription() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof SpringResourceAdapter) {
            return delegate.equals(((SpringResourceAdapter) o).delegate);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * Converts a Spring Resource to a Jahia IOResource.
     *
     * @param resource the Spring Resource to convert, may be null
     * @return the corresponding Jahia IOResource, or null if the input is null
     * @throws UncheckedIOException if an I/O error occurs during conversion
     */
    public static IOResource fromSpring(Resource resource) {
        if (resource == null) {
            return null;
        }
        if (resource instanceof IOResource) {
            return (IOResource) resource;
        }
        if (resource instanceof FileSystemResource) {
            return new FileSystemIOResource(((FileSystemResource) resource).getFile());
        }
        if (resource instanceof ByteArrayResource) {
            return new ByteArrayIOResource(((ByteArrayResource) resource).getByteArray(), resource.getDescription());
        }
        if (resource instanceof InputStreamResource) {
            try {
                return new InputStreamIOResource(resource.getInputStream(), resource.getDescription());
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to convert Spring Resource to Jahia IOResource: " + resource.getDescription(), e);
            }
        }
        try {
            return new UrlIOResource(resource.getURL());
        } catch (IOException e) {
            // Fallback: wrap with adapter
            return new SpringResourceAdapter(resource);
        }
    }

    /**
     * Converts a collection of Spring Resources to a list of Jahia IOResources.
     *
     * @param resources the collection of Spring Resources to convert, may be null
     * @return a list of corresponding Jahia IOResources, or null if the input is null
     * @throws UncheckedIOException if an I/O error occurs during conversion
     */
    public static List<IOResource> fromSpring(Collection<? extends Resource> resources) {
        return resources == null ? null : resources.stream()
                .map(SpringResourceAdapter::fromSpring)
                .collect(Collectors.toList());
    }

    /**
     * Converts an array of Spring Resources to an array of Jahia IOResources.
     *
     * @param resources the array of Spring Resources to convert, may be null
     * @return an array of corresponding Jahia IOResources, or null if the input is null
     * @throws UncheckedIOException if an I/O error occurs during conversion
     */
   public static IOResource[] fromSpring(Resource[] resources) {
        return resources == null ? null : Arrays.stream(resources)
                .map(SpringResourceAdapter::fromSpring)
                .toArray(IOResource[]::new);
    }

    /**
     * Converts a Jahia IOResource to a Spring Resource.
     *
     * @param resource the Jahia IOResource to convert, may be null
     * @return the corresponding Spring Resource, or null if the input is null
     * @throws UncheckedIOException if an I/O error occurs during conversion
     */
    @SuppressWarnings("java:S5738") // Assume usage of deprecated for backward compatibility
    public static Resource toSpring(IOResource resource) {
        if (resource == null) {
            return null;
        }
        if (resource instanceof SpringResourceAdapter) {
            return ((SpringResourceAdapter) resource).getDelegate();
        }
        if (resource instanceof FileSystemIOResource) {
            File file = ((FileSystemIOResource) resource).getFile();
            return new FileSystemResource(file);
        }
        if (resource instanceof ByteArrayIOResource) {
            return new ByteArrayResource(((ByteArrayIOResource) resource).getByteArray(), resource.getDescription());
        }
        try {
            if (resource instanceof BundleIOResource) {
                Bundle bundle = ((BundleIOResource) resource).getBundle();
                return new BundleResource(resource.getURL(), bundle);
            }
            if (resource instanceof InputStreamIOResource) {
                return new InputStreamResource(
                        resource.getInputStream(), resource.getDescription());
            }
            return new UrlResource(resource.getURL());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to convert Jahia IOResource to Spring Resource: " + resource.getDescription(), e);
        }
    }

    /**
     * Converts a collection of Jahia IOResources to a list of Spring Resources.
     *
     * @param resources the collection of Jahia IOResources to convert, may be null
     * @return a list of corresponding Spring Resources, or null if the input is null
     * @throws UncheckedIOException if an I/O error occurs during conversion
     */
    public static List<Resource> toSpring(Collection<? extends IOResource> resources) {
        return resources == null ? null : resources.stream()
                .map(SpringResourceAdapter::toSpring)
                .collect(Collectors.toList());
    }

    /**
     * Returns the underlying Spring Resource that is wrapped by this adapter.
     *
     * @return the wrapped Spring Resource
     */
    public Resource getDelegate() {
        return delegate;
    }
}
