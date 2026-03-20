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
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.Objects;
import java.util.jar.JarFile;

/**
 * Resource backed by a URL.
 */
public class UrlIOResource implements IOResource {

    private final URL url;
    private volatile String cleanedUrl;

    public UrlIOResource(URL url) {
        this.url = Objects.requireNonNull(url, "URL must not be null");
    }

    public UrlIOResource(String path) throws MalformedURLException {
        this(URI.create(Objects.requireNonNull(path, "Path must not be null")).toURL());
    }

    @Override
    public URL getURL() {
        return url;
    }

    public URI getURI() throws IOException {
        try {
            return ResourceUtils.toURI(url);
        } catch (URISyntaxException ex) {
            throw new IOException("Invalid URI [" + url + "]", ex);
        }
    }

    @Override
    public File getFile() throws IOException {
        return ResourceUtils.getFile(url, this.getDescription());
    }

    public InputStream getInputStream() throws IOException {
        URLConnection con = this.url.openConnection();
        con.setUseCaches(false);

        try {
            return con.getInputStream();
        } catch (IOException ex) {
            if (con instanceof HttpURLConnection) {
                ((HttpURLConnection)con).disconnect();
            }
            throw ex;
        }
    }

    @Override
    @SuppressWarnings("java:S3776")
    public boolean exists() {
        try {
            if (ResourceUtils.isFileURL(url)) {
                // Proceed with file system resolution
                return getFile().exists();
            }
            else {
                // Try a URL connection content-length header
                URLConnection con = url.openConnection();
                con.setUseCaches(false);

                HttpURLConnection httpCon = con instanceof HttpURLConnection ? (HttpURLConnection) con : null;
                if (httpCon != null) {
                    httpCon.setRequestMethod("HEAD");
                    int code = httpCon.getResponseCode();
                    if (code == HttpURLConnection.HTTP_OK) {
                        return true;
                    }
                    else if (code == HttpURLConnection.HTTP_NOT_FOUND) {
                        httpCon.disconnect();
                        return false;
                    }
                }

                JarURLConnection jarCon = con instanceof JarURLConnection ? (JarURLConnection) con : null;
                if (jarCon != null) {
                    // For JarURLConnection, do not check content-length but rather the
                    // existence of the entry (or the jar root in case of no entryName).
                    // getJarFile() called for enforced presence check of the jar file,
                    // throwing a NoSuchFileException otherwise (turned to false below).
                    try (JarFile jarFile = jarCon.getJarFile()) {
                        return (jarCon.getEntryName() == null || jarCon.getJarEntry() != null);
                    }
                } else if (con.getContentLengthLong() > 0) {
                    return true;
                }

                if (httpCon != null) {
                    // No HTTP OK status, and no content-length header: give up
                    httpCon.disconnect();
                    return false;
                } else {
                    // Fall back to stream existence: can we open the stream?
                    getInputStream().close();
                    return true;
                }
            }
        }  catch (IOException ex) {
            return false;
        }
    }

    @Override
    public long lastModified() throws IOException {
        boolean fileCheck = false;
        if (ResourceUtils.isFileURL(url) || ResourceUtils.isJarURL(url)) {
            // Proceed with file system resolution
            fileCheck = true;
            try {
                File fileToCheck = getFileForLastModifiedCheck();
                long lastModified = fileToCheck.lastModified();
                if (lastModified > 0L || fileToCheck.exists()) {
                    return lastModified;
                }
            } catch (FileNotFoundException ex) {
                // Defensively fall back to URL connection check instead
            }
        }

        // Try a URL connection last-modified header
        URLConnection con = url.openConnection();
        con.setUseCaches(false);
        if (con instanceof HttpURLConnection) {
            ((HttpURLConnection) con).setRequestMethod("HEAD");
        }

        try {
            long lastModified = con.getLastModified();
            if (lastModified == 0 && fileCheck && con.getContentLengthLong() <= 0) {
                throw new FileNotFoundException(getDescription() + " cannot be resolved in the file system for checking its last-modified timestamp");
            }
            return lastModified;
        } finally {
            if (con instanceof HttpURLConnection) {
                ((HttpURLConnection) con).disconnect();
            }
        }
    }

    @Override
    public String getFilename() {
        return (new File(this.url.getFile())).getName();
    }

    @Override
    public String getDescription() {
        return "URL [" + url + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        return getCleanedUrl().equals(((UrlIOResource) o).getCleanedUrl());
    }

    @Override
    public int hashCode() {
        return getCleanedUrl().hashCode();
    }

    @Override
    public String toString() {
        return getDescription();
    }

    /**
     * This implementation determines the underlying File
     * (or jar file, in case of a resource in a jar/zip).
     */
    private File getFileForLastModifiedCheck() throws IOException {
        if (ResourceUtils.isJarURL(url)) {
            URL actualUrl = ResourceUtils.extractJarFileURL(url);
            return ResourceUtils.getFile(actualUrl, "Jar URL");
        } else {
            return this.getFile();
        }
    }

    /**
     * Lazily determine a cleaned URL for the given original URL.
     */
    private String getCleanedUrl() {
        String result = this.cleanedUrl;
        if (result != null) {
            return result;
        }
        result = StringUtils.cleanPath(this.url.toString());
        this.cleanedUrl = result;
        return result;
    }
}
