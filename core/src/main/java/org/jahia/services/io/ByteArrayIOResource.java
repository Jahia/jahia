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

import org.jahia.api.io.IOResource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

/**
 * Resource backed by a byte array.
 */
public class ByteArrayIOResource implements IOResource {

    private final byte[] bytes;
    private final String description;

    public ByteArrayIOResource(byte[] bytes) {
        this(bytes, "Byte array resource");
    }

    public ByteArrayIOResource(byte[] bytes, String description) {
        this.bytes = Objects.requireNonNull(bytes, "Byte array must not be null");
        this.description = description != null ? description : "Byte array resource";
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(bytes);
    }

    public byte[] getByteArray() {
        return bytes;
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public long lastModified() {
        return 0;
    }

    @Override
    public URL getURL() throws IOException {
        throw new IOException("URL not available for " + getDescription());
    }

    @Override
    public URI getURI() throws IOException {
        throw new IOException("URI not available for " + getDescription());
    }

    @Override
    public File getFile() throws IOException {
        throw new IOException("File not available for " + getDescription());
    }

    @Override
    public String getFilename() {
        return null;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return getDescription();
    }
}