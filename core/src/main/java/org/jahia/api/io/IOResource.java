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
package org.jahia.api.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URI;

/**
 * A lightweight resource abstraction interface for accessing resources in a uniform way.
 * <p>
 * This interface provides a simple abstraction for various resource types (file system resources,
 * classpath resources, URL resources, etc.) similar to Spring's Resource interface.
 * </p>
 *
 * @see java.io.File
 * @see java.net.URL
 * @see java.net.URI
 */
public interface IOResource {

    /**
     * Returns a URL handle for this resource.
     *
     * @return the URL handle for this resource
     * @throws IOException if the resource cannot be resolved as URL
     */
    URL getURL() throws IOException;

    /**
     * Returns a URI handle for this resource.
     *
     * @return the URI handle for this resource
     * @throws IOException if the resource cannot be resolved as URI
     */
    URI getURI() throws IOException;

    /**
     * Returns a File handle for this resource.
     *
     * @return the File handle for this resource
     * @throws IOException if the resource cannot be resolved as absolute file path,
     *                     i.e., if the resource is not available in a file system
     */
    File getFile() throws IOException;

    /**
     * Returns an InputStream for reading the resource content.
     *
     * @return an InputStream for the resource content
     * @throws IOException if the stream could not be opened
     */
    InputStream getInputStream() throws IOException;

    /**
     * Checks whether this resource actually exists in physical form.
     *
     * @return {@code true} if the resource exists, {@code false} otherwise
     */
    boolean exists();

    /**
     * Determines the last-modified timestamp for this resource.
     *
     * @return the last-modified timestamp in milliseconds since epoch (Unix time)
     * @throws IOException if the resource cannot be resolved or the timestamp cannot be determined
     */
    long lastModified() throws IOException;

    /**
     * Returns the filename of this resource.
     * <p>
     * This is typically the last segment of the path. For example, "myfile.txt"
     * would be the filename for a file system resource.
     * </p>
     *
     * @return the filename, or {@code null} if this resource doesn't have a filename
     */
    String getFilename();

    /**
     * Returns a human-readable description of this resource, useful for logging and diagnostics.
     *
     * @return a description of this resource (never {@code null})
     */
    String getDescription();
}