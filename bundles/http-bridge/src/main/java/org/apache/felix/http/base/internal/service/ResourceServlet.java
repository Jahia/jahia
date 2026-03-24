/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
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
package org.apache.felix.http.base.internal.service;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.felix.http.base.internal.context.ServletContextImpl;
import org.jahia.utils.DeprecationUtils;
import org.jahia.utils.HttpResponseUtils;
import org.osgi.framework.Bundle;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.zip.GZIPOutputStream;

/**
 * Forked version of org.apache.felix.http.base
 */
public final class ResourceServlet extends HttpServlet {
    private final String path;
    private boolean gzipEnabled;

    public ResourceServlet(String path) {
        this.path = path;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        gzipEnabled = Boolean.parseBoolean(StringUtils.defaultString(config.getInitParameter("enable-gzip"), "true"));
        if (!gzipEnabled) {
            DeprecationUtils.onDeprecatedFeatureUsage("ResourceServlet#enable-gzip", "8.2.4.0", true,
                    "The init parameter 'enable-gzip' is no longer supported for ResourceServlet");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String target = req.getPathInfo();
        if (target == null) {
            target = "";
        }

        if (!target.startsWith("/")) {
            target += "/" + target;
        }

        String resName = this.path + target;
        URL url = getServletContext().getResource(resName);

        if (url == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND);
        } else {
            handle(req, res, url, resName);
        }
    }

    private void handle(HttpServletRequest req, HttpServletResponse res, URL url, String resName) throws IOException {
        String contentType = getServletContext().getMimeType(resName);
        boolean compressible = HttpResponseUtils.isCompressibleContentType(contentType);
        boolean rangeRequested = req.getHeader("Range") != null;
        boolean acceptsGzip = gzipEnabled
                && compressible
                && !rangeRequested
                && HttpResponseUtils.acceptsEncoding(req.getHeader("Accept-Encoding"), "gzip");

        long lastModified = getLastModified(url);
        String baseEtag = HttpResponseUtils.buildStrongEtag(DigestUtils.md5Hex(url.toString()), lastModified);
        String identityEtag = compressible ? HttpResponseUtils.buildVariantEtag(baseEtag, "a") : baseEtag;
        String responseEtag = compressible
                ? HttpResponseUtils.buildVariantEtag(baseEtag, acceptsGzip ? "g" : "a")
                : baseEtag;

        HttpResponseUtils.applyValidatorHeaders(res, responseEtag, lastModified);
        res.setHeader("Accept-Ranges", "bytes");
        if (compressible) {
            HttpResponseUtils.appendVaryHeader(res, "Accept-Encoding");
        }

        if (HttpResponseUtils.isNotModified(req, responseEtag, lastModified)) {
            res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        if (contentType != null) {
            res.setContentType(contentType);
        }

        long contentLength = getContentLength(url);
        HttpResponseUtils.ByteRange range = HttpResponseUtils.parseRange(req, res, contentLength, identityEtag, lastModified);
        if (range == HttpResponseUtils.UNSATISFIABLE_RANGE) {
            return;
        }

        if (range != null) {
            res.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
            res.setHeader("Content-Range", "bytes " + range.start + "-" + range.end + "/" + range.total);
            HttpResponseUtils.setContentLength(res, range.length());
            copyResourceRange(url, res, range);
            return;
        }

        copyResource(url, res, acceptsGzip, contentLength);
    }

    private long getLastModified(URL url) {
        long lastModified = 0;
        if ("bundle".equals(url.getProtocol())) {
            Long lastModifiedLong = getLastModifiedFromBundleHeaders();
            if (lastModifiedLong != null)
                return lastModifiedLong;
        }
        try {
            URLConnection conn = url.openConnection();
            lastModified = conn.getLastModified();
        } catch (Exception e) { // Do nothing
        }

        if (lastModified == 0) {
            String filepath = url.getPath();
            if (filepath != null) {
                File f = new File(filepath);
                if (f.exists()) {
                    lastModified = f.lastModified();
                }
            }
        }

        return lastModified;
    }

    private Long getLastModifiedFromBundleHeaders() {
        ServletContext servletContext = getServletContext();
        if (servletContext instanceof ServletContextImpl) {
            Bundle bundle = ((ServletContextImpl) servletContext).getBundle();
            String lastModifiedString = bundle.getHeaders().get("Bnd-LastModified");
            if (lastModifiedString != null) {
                try {
                    return Long.parseLong(lastModifiedString);
                } catch (NumberFormatException e) {
                    // Ignore header
                }
            }
        }
        return null;
    }

    private long getContentLength(URL url) {
        try {
            return url.openConnection().getContentLengthLong();
        } catch (IOException e) {
            return -1;
        }
    }

    private void copyResource(URL url, HttpServletResponse res, boolean gzip, long contentLength) throws IOException {
        URLConnection connection = url.openConnection();

        try (InputStream is = connection.getInputStream(); OutputStream os = res.getOutputStream()) {
            if (gzip) {
                res.setHeader("Content-Encoding", "gzip");
                try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(os, 1024)) {
                    HttpResponseUtils.copy(is, gzipOutputStream);
                }
                return;
            }

            HttpResponseUtils.setContentLength(res, contentLength);
            HttpResponseUtils.copy(is, os);
        }
    }

    private void copyResourceRange(URL url, HttpServletResponse res, HttpResponseUtils.ByteRange range) throws IOException {
        try (InputStream is = url.openConnection().getInputStream(); OutputStream os = res.getOutputStream()) {
            HttpResponseUtils.skipFully(is, range.start);

            long remaining = range.length();
            byte[] buf = new byte[1024];
            while (remaining > 0) {
                int toRead = (int) Math.min(buf.length, remaining);
                int read = is.read(buf, 0, toRead);
                if (read < 0) {
                    break;
                }
                os.write(buf, 0, read);
                remaining -= read;
            }
        }
    }
}
