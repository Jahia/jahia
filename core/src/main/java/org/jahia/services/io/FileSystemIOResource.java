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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Objects;

/**
 * Resource backed by a file on the filesystem.
 */
public class FileSystemIOResource implements IOResource {

    private final File file;

    public FileSystemIOResource(File file) {
        this.file = Objects.requireNonNull(file, "File must not be null");
    }

    public FileSystemIOResource(String path) {
        this(new File(Objects.requireNonNull(path, "Path must not be null")));
    }

    @Override
    public URL getURL() throws IOException {
        return file.toURI().toURL();
    }

    @Override
    public URI getURI() {
        return file.toURI();
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new FileInputStream(file);
    }

    @Override
    public boolean exists() {
        return file.exists();
    }

    @Override
    public long lastModified() {
        return file.lastModified();
    }

    @Override
    public String getFilename() {
        return file.getName();
    }

    @Override
    public String getDescription() {
        return "file [" + file.getAbsolutePath() + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return file.equals(((FileSystemIOResource) o).file);
    }

    @Override
    public int hashCode() {
        return file.hashCode();
    }

    @Override
    public String toString() {
        return getDescription();
    }
}