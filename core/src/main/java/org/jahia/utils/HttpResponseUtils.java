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
package org.jahia.utils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Locale;

/**
 * Utility class for HTTP response validation and content-serving concerns shared across
 * Jahia's static-resource servlets ({@code ResourceServlet}, {@code FileServlet},
 * {@code StaticFileServlet}).
 *
 * <p>Covers four areas:</p>
 * <ul>
 *   <li><b>ETag building</b> — strong ETags and encoding-variant ETags ({@code -a} / {@code -g}).</li>
 *   <li><b>Conditional-request evaluation</b> — {@code If-None-Match}, {@code If-Modified-Since},
 *       {@code If-Match}, {@code If-Unmodified-Since} per RFC 7232.</li>
 *   <li><b>Byte-range parsing</b> — single {@code Range} / {@code If-Range} handling per RFC 7233.</li>
 *   <li><b>Content-serving helpers</b> — gzip capability detection, {@code Vary} header management,
 *       {@code Content-Length} writing, stream copy, and reliable stream skipping.</li>
 * </ul>
 *
 * <p>All methods are static. This class cannot be instantiated.</p>
 */
public final class HttpResponseUtils {

    private static final String VARY = "Vary";
    private static final int COPY_BUFFER_SIZE = 1024;

    private HttpResponseUtils() {
        // utility class
    }

    // -------------------------------------------------------------------------
    // ETag building
    // -------------------------------------------------------------------------

    /**
     * Builds a strong ETag value in the form {@code "<stableId>-<lastModified>"}.
     *
     * <p>Any double-quote characters inside {@code stableId} are stripped to avoid producing a
     * malformed ETag. If {@code stableId} is {@code null} the literal string {@code unknown} is
     * used instead.</p>
     *
     * @param stableId     a stable, content-identifying string (e.g. a JCR node UUID or an MD5 hex)
     * @param lastModified last-modified timestamp in milliseconds
     * @return a properly double-quoted strong ETag, e.g. {@code "abc123-1710000000000"}
     */
    public static String buildStrongEtag(String stableId, long lastModified) {
        String cleanStableId = stableId == null ? "unknown" : stableId.replace("\"", "");
        return "\"" + cleanStableId + "-" + lastModified + "\"";
    }

    /**
     * Derives an encoding-variant ETag from an existing base ETag by appending a suffix.
     *
     * <p>The opaque tag value is extracted from the base ETag (stripping any {@code W/} weak
     * prefix and surrounding quotes) before the suffix is appended. This ensures the result is
     * always a fresh strong ETag regardless of whether the input was weak or strong.</p>
     *
     * <p>Convention used across Jahia's servlets:</p>
     * <ul>
     *   <li>{@code "a"} → identity (uncompressed) variant</li>
     *   <li>{@code "g"} → gzip variant</li>
     * </ul>
     *
     * @param baseEtag      the base ETag (may be weak or strong, with or without quotes)
     * @param variantSuffix the suffix identifying the encoding variant, e.g. {@code "a"} or {@code "g"}
     * @return a properly double-quoted strong ETag, e.g. {@code "abc123-1710000000000-g"}
     */
    public static String buildVariantEtag(String baseEtag, String variantSuffix) {
        String opaque = opaqueTagValue(baseEtag);
        return "\"" + opaque + "-" + variantSuffix + "\"";
    }

    // -------------------------------------------------------------------------
    // Content-type / encoding negotiation
    // -------------------------------------------------------------------------

    /**
     * Returns {@code true} if the given MIME type is worth compressing with gzip.
     *
     * <p>The following types are considered compressible:</p>
     * <ul>
     *   <li>{@code text/*} — all text subtypes (HTML, CSS, JS, plain, CSV, …)</li>
     *   <li>{@code application/json} and JSON variants ({@code ld+json}, {@code manifest+json},
     *       {@code geo+json}, {@code graphql})</li>
     *   <li>{@code application/javascript}</li>
     *   <li>{@code application/xml} and XML variants ({@code xhtml+xml}, {@code rss+xml},
     *       {@code atom+xml})</li>
     *   <li>{@code image/svg+xml} — SVG is XML text and compresses extremely well (60–80%)</li>
     * </ul>
     *
     * <p>Binary formats are intentionally excluded: already-compressed images
     * ({@code image/jpeg}, {@code image/png}, {@code image/webp}, {@code image/avif}),
     * fonts ({@code font/woff}, {@code font/woff2}), archives ({@code application/zip},
     * {@code application/gzip}), and WebAssembly ({@code application/wasm}) gain nothing
     * from gzip and waste CPU.</p>
     *
     * @param contentType the MIME type to test, optionally with parameters (e.g. {@code text/css; charset=UTF-8});
     *                    {@code null} returns {@code false}
     * @return {@code true} if the content type is in the compressible list
     */
    public static boolean isCompressibleContentType(String contentType) {
        if (contentType == null) {
            return false;
        }

        String normalized = contentType.toLowerCase(Locale.ROOT);
        return normalized.startsWith("text/")
                // JSON variants
                || normalized.equals("application/json")
                || normalized.equals("application/ld+json")       // JSON-LD structured data
                || normalized.equals("application/manifest+json") // Web app manifest
                || normalized.equals("application/geo+json")      // GeoJSON
                || normalized.equals("application/graphql")       // GraphQL bodies
                // JavaScript
                || normalized.equals("application/javascript")
                // XML variants
                || normalized.equals("application/xml")
                || normalized.equals("application/xhtml+xml")
                || normalized.equals("application/rss+xml")       // RSS feeds
                || normalized.equals("application/atom+xml")      // Atom feeds
                // SVG: XML text that compresses extremely well (60-80% reduction)
                || normalized.equals("image/svg+xml");
    }

    /**
     * Returns {@code true} if the given {@code Accept-Encoding} header value indicates that the
     * specified encoding is acceptable to the client.
     *
     * <p>Implements the quality-value ({@code q=}) negotiation defined in RFC 7231 §5.3.4:
     * an encoding is accepted when it (or the {@code *} wildcard) appears with {@code q > 0},
     * and rejected when it appears with {@code q=0}. An explicit rejection always wins over a
     * wildcard acceptance.</p>
     *
     * @param acceptEncodingHeader the value of the {@code Accept-Encoding} request header;
     *                             {@code null} returns {@code false}
     * @param encoding             the encoding token to test, e.g. {@code "gzip"}; {@code null} returns {@code false}
     * @return {@code true} if the client will accept the given encoding
     */
    public static boolean acceptsEncoding(String acceptEncodingHeader, String encoding) {
        if (acceptEncodingHeader == null || encoding == null) {
            return false;
        }

        String expected = encoding.toLowerCase(Locale.ROOT);
        boolean wildcardAllowed = false;
        boolean encodingExplicitlyRejected = false;

        for (String token : acceptEncodingHeader.split(",")) {
            // Each token is of the form "encoding" or "encoding;q=<value>"
            String[] parts = token.trim().split(";");
            if (parts.length == 0) {
                continue;
            }

            String candidate = parts[0].trim().toLowerCase(Locale.ROOT);
            if (candidate.isEmpty()) {
                continue;
            }

            // Default q-value is 1.0 when the parameter is absent (RFC 7231 §5.3.1)
            double qValue = 1.0d;
            for (int i = 1; i < parts.length; i++) {
                String parameter = parts[i].trim().toLowerCase(Locale.ROOT);
                if (parameter.startsWith("q=")) {
                    try {
                        qValue = Double.parseDouble(parameter.substring(2).trim());
                    } catch (NumberFormatException e) {
                        // Malformed q-value: treat as q=0 to be safe
                        qValue = 0.0d;
                    }
                }
            }

            if (candidate.equals(expected)) {
                if (qValue <= 0.0d) {
                    // Explicit rejection takes precedence over any wildcard match found later
                    encodingExplicitlyRejected = true;
                } else {
                    return true;
                }
            } else if (candidate.equals("*")) {
                wildcardAllowed = qValue > 0.0d;
            }
        }

        // Accept via wildcard only if the encoding was never explicitly rejected
        return wildcardAllowed && !encodingExplicitlyRejected;
    }

    // -------------------------------------------------------------------------
    // Conditional-request evaluation (RFC 7232)
    // -------------------------------------------------------------------------

    /**
     * Determines whether the resource should be served as {@code 304 Not Modified} by evaluating
     * the conditional request headers in priority order as specified by RFC 7232 §6:
     *
     * <ol>
     *   <li>If {@code If-None-Match} is present, it takes sole precedence; the result is based
     *       on a weak ETag comparison only.</li>
     *   <li>Otherwise, {@code If-Modified-Since} is consulted.</li>
     * </ol>
     *
     * @param request      the HTTP request carrying the conditional headers
     * @param currentEtag  the ETag of the resource that would be returned in a {@code 200} response
     * @param lastModified the last-modified timestamp of the resource in milliseconds
     * @return {@code true} if the client's cached copy is still valid and a {@code 304} should be sent
     */
    public static boolean isNotModified(HttpServletRequest request, String currentEtag, long lastModified) {
        String ifNoneMatch = request.getHeader("If-None-Match");
        if (ifNoneMatch != null) {
            // If-None-Match is present: ignore If-Modified-Since entirely (RFC 7232 §6 step 2)
            return matchesIfNoneMatch(ifNoneMatch, currentEtag);
        }

        return isIfModifiedSinceFresh(request.getDateHeader("If-Modified-Since"), lastModified);
    }

    /**
     * Returns {@code true} if the given {@code If-None-Match} header value matches the current
     * ETag using a <em>weak</em> comparison (RFC 7232 §2.3 and §3.2).
     *
     * <p>The wildcard {@code *} always matches any non-null ETag.</p>
     *
     * @param ifNoneMatchHeader the value of the {@code If-None-Match} request header;
     *                          {@code null} returns {@code false}
     * @param currentEtag       the ETag of the current representation; {@code null} returns {@code false}
     * @return {@code true} if there is a weak match, indicating a {@code 304} can be sent
     */
    public static boolean matchesIfNoneMatch(String ifNoneMatchHeader, String currentEtag) {
        if (ifNoneMatchHeader == null || currentEtag == null) {
            return false;
        }

        for (String candidate : ifNoneMatchHeader.split(",")) {
            String value = candidate.trim();
            if ("*".equals(value) || weakEquals(value, currentEtag)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns {@code true} if the given {@code If-Match} header value matches the current ETag
     * using a <em>strong</em> comparison (RFC 7232 §2.3 and §3.1).
     *
     * <p>The wildcard {@code *} always matches any non-null ETag. Weak ETags (prefixed with
     * {@code W/}) never satisfy a strong comparison.</p>
     *
     * @param ifMatchHeader the value of the {@code If-Match} request header;
     *                      {@code null} returns {@code false}
     * @param currentEtag   the ETag of the current representation; {@code null} returns {@code false}
     * @return {@code true} if there is a strong match, meaning the precondition is satisfied
     */
    public static boolean matchesIfMatch(String ifMatchHeader, String currentEtag) {
        if (ifMatchHeader == null || currentEtag == null) {
            return false;
        }

        for (String candidate : ifMatchHeader.split(",")) {
            String value = candidate.trim();
            if ("*".equals(value) || strongEquals(value, currentEtag)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns {@code true} if the resource has <em>not</em> been modified since the given
     * {@code If-Modified-Since} date, meaning a {@code 304} can be sent.
     *
     * <p>Both timestamps are truncated to second precision before comparison, as HTTP dates
     * (RFC 7231 §7.1.1.1) carry no sub-second information. The method returns {@code false}
     * when either value is absent/unknown ({@code ifModifiedSince == -1} or
     * {@code lastModified <= 0}).</p>
     *
     * @param ifModifiedSince the value of the {@code If-Modified-Since} date header in milliseconds,
     *                        or {@code -1} if the header is absent
     * @param lastModified    the last-modified timestamp of the resource in milliseconds, or {@code 0} if unknown
     * @return {@code true} if the resource has not changed since the client's cached copy
     */
    public static boolean isIfModifiedSinceFresh(long ifModifiedSince, long lastModified) {
        // Truncate to second precision to match HTTP date resolution
        return ifModifiedSince > -1 && lastModified > 0 && (lastModified / 1000 * 1000) <= ifModifiedSince;
    }

    // -------------------------------------------------------------------------
    // Response header helpers
    // -------------------------------------------------------------------------

    /**
     * Sets the {@code ETag} and {@code Last-Modified} validator headers on the response.
     *
     * <p>Each header is only written when its value is meaningful: {@code eTag} must be non-null,
     * and {@code lastModified} must be greater than zero.</p>
     *
     * @param response     the HTTP response to write headers to
     * @param eTag         the ETag value (already quoted), or {@code null} to skip
     * @param lastModified the last-modified timestamp in milliseconds, or {@code 0} to skip
     */
    public static void applyValidatorHeaders(HttpServletResponse response, String eTag, long lastModified) {
        if (eTag != null) {
            response.setHeader("ETag", eTag);
        }
        if (lastModified > 0) {
            response.setDateHeader("Last-Modified", lastModified);
        }
    }

    /**
     * Adds a value to the {@code Vary} response header without producing duplicates.
     *
     * <p>If the header does not yet exist it is created with {@code varyValue} as its sole entry.
     * If the header already contains {@code varyValue} (case-insensitive) no change is made.
     * Otherwise {@code varyValue} is appended to the existing list.</p>
     *
     * <p>This method is typically called with {@code "Accept-Encoding"} to inform caches and CDNs
     * that the response varies by the client's encoding capability.</p>
     *
     * @param response  the HTTP response to write the header to
     * @param varyValue the field name to add to {@code Vary}, e.g. {@code "Accept-Encoding"};
     *                  {@code null} or empty string is silently ignored
     */
    public static void appendVaryHeader(HttpServletResponse response, String varyValue) {
        if (varyValue == null || varyValue.isEmpty()) {
            return;
        }

        String currentVary = response.getHeader(VARY);
        if (currentVary == null || currentVary.isEmpty()) {
            response.setHeader(VARY, varyValue);
            return;
        }

        // Skip writing if already present (case-insensitive)
        for (String candidate : currentVary.split(",")) {
            if (candidate.trim().equalsIgnoreCase(varyValue)) {
                return;
            }
        }

        response.setHeader(VARY, currentVary + ", " + varyValue);
    }

    /**
     * Sets the {@code Content-Length} response header.
     *
     * <p>Values that fit in a 32-bit signed integer are written via
     * {@link HttpServletResponse#setContentLength(int)}; larger values are written as a raw
     * string header to avoid integer overflow. Non-positive values are ignored.</p>
     *
     * @param response the HTTP response to write to
     * @param length   the content length in bytes; {@code <= 0} is a no-op
     */
    public static void setContentLength(HttpServletResponse response, long length) {
        if (length >= 0 && length <= Integer.MAX_VALUE) {
            response.setContentLength((int) length);
        } else if (length > Integer.MAX_VALUE) {
            response.setHeader("Content-Length", Long.toString(length));
        }
    }

    // -------------------------------------------------------------------------
    // Byte-range support (RFC 7233)
    // -------------------------------------------------------------------------

    /**
     * Immutable value object representing a single inclusive byte range within a resource,
     * as defined in RFC 7233.
     *
     * <p>All three fields are public and final; use {@link #length()} to compute the number of
     * bytes in the range instead of deriving it manually.</p>
     */
    public static final class ByteRange {
        /**
         * Zero-based index of the first byte to include.
         */
        public final long start;
        /**
         * Zero-based index of the last byte to include (inclusive).
         */
        public final long end;
        /**
         * Total length of the complete resource (used to build the {@code Content-Range} header).
         */
        public final long total;

        /**
         * @param start first byte index (0-based, inclusive)
         * @param end   last byte index (0-based, inclusive)
         * @param total total number of bytes in the full resource
         */
        public ByteRange(long start, long end, long total) {
            this.start = start;
            this.end = end;
            this.total = total;
        }

        /**
         * Returns the number of bytes covered by this range ({@code end - start + 1}).
         *
         * @return number of bytes in this range
         */
        public long length() {
            return end - start + 1;
        }
    }

    /**
     * Sentinel value returned by {@link #parseRange} to signal that the requested range could
     * not be satisfied and that a {@code 416 Range Not Satisfiable} response has already been
     * committed to the client.
     *
     * <p>Callers must check for this sentinel using reference equality ({@code ==}) and must
     * not write any further response body when it is returned.</p>
     */
    public static final ByteRange UNSATISFIABLE_RANGE = new ByteRange(0, -1, 0);

    /**
     * Parses and validates a single-range {@code Range} request header according to RFC 7233,
     * taking the {@code If-Range} precondition into account.
     *
     * <p>Return value semantics:</p>
     * <ul>
     *   <li>{@code null} — no {@code Range} header present, or the range request was invalidated
     *       by {@code If-Range}; the caller should serve the full resource.</li>
     *   <li>{@link #UNSATISFIABLE_RANGE} — the range could not be satisfied; a
     *       {@code 416 Range Not Satisfiable} response has already been sent; the caller must
     *       not write any body.</li>
     *   <li>any other {@link ByteRange} — a valid, clamped byte range ready to be served as
     *       {@code 206 Partial Content}.</li>
     * </ul>
     *
     * <p>Multi-range requests ({@code Range: bytes=0-10,20-30}) are intentionally not supported;
     * they are rejected with {@code 416}.</p>
     *
     * @param req           the HTTP request
     * @param res           the HTTP response (used to send {@code 416} errors)
     * @param contentLength total length of the resource in bytes; must be {@code > 0}
     * @param identityEtag  the ETag of the uncompressed (identity) representation, used for
     *                      {@code If-Range} ETag matching — must never be the gzip variant
     * @param lastModified  last-modified timestamp of the resource in milliseconds, used for
     *                      {@code If-Range} date matching
     * @return a {@link ByteRange}, {@link #UNSATISFIABLE_RANGE}, or {@code null} — see above
     * @throws IOException if sending the {@code 416} error response fails
     */
    public static ByteRange parseRange(HttpServletRequest req, HttpServletResponse res, long contentLength, String identityEtag,
            long lastModified) throws IOException {
        String rangeHeader = req.getHeader("Range");
        if (rangeHeader == null || contentLength <= 0) {
            return null;
        }

        // Reject multi-range requests: only a single "bytes=<start>-<end>" form is supported
        if (!rangeHeader.startsWith("bytes=") || rangeHeader.indexOf(',') != -1) {
            res.setHeader("Content-Range", "bytes */" + contentLength);
            res.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return UNSATISFIABLE_RANGE;
        }

        // Evaluate the If-Range precondition (RFC 7233 §3.2):
        // If-Range may carry either an ETag or an HTTP date.
        // If the precondition fails the server must ignore the Range header and serve the full resource.
        String ifRange = req.getHeader("If-Range");
        if (ifRange != null && !ifRange.trim().equals(identityEtag)) {
            try {
                long ifRangeTime = req.getDateHeader("If-Range");
                if (ifRangeTime != -1 && ifRangeTime + 1000 < lastModified) {
                    // The stored copy is older than the current resource → serve full response
                    return null;
                }
            } catch (IllegalArgumentException e) {
                // If-Range value is not a valid HTTP date and did not match the ETag → serve full response
                return null;
            }
        }

        // Parse the single range spec: "bytes=<startSpec>-<endSpec>"
        String spec = rangeHeader.substring("bytes=".length()).trim();
        int separator = spec.indexOf('-');
        if (separator < 0) {
            res.setHeader("Content-Range", "bytes */" + contentLength);
            res.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return UNSATISFIABLE_RANGE;
        }

        String startSpec = spec.substring(0, separator).trim();
        String endSpec = spec.substring(separator + 1).trim();

        long start;
        long end;

        try {
            if (startSpec.isEmpty()) {
                // Suffix range: "bytes=-<N>" means the last N bytes (RFC 7233 §2.1)
                long suffixLength = Long.parseLong(endSpec);
                if (suffixLength <= 0) {
                    throw new NumberFormatException("Invalid suffix range");
                }
                start = Math.max(0, contentLength - suffixLength);
                end = contentLength - 1;
            } else {
                start = Long.parseLong(startSpec);
                // Open-ended range: "bytes=<N>-" means from N to the last byte
                end = endSpec.isEmpty() ? (contentLength - 1) : Long.parseLong(endSpec);
            }
        } catch (NumberFormatException e) {
            res.setHeader("Content-Range", "bytes */" + contentLength);
            res.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return UNSATISFIABLE_RANGE;
        }

        if (start < 0 || start >= contentLength || end < start) {
            res.setHeader("Content-Range", "bytes */" + contentLength);
            res.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return UNSATISFIABLE_RANGE;
        }

        // Clamp end to the last available byte (handles requests beyond EOF gracefully)
        end = Math.min(end, contentLength - 1);
        return new ByteRange(start, end, contentLength);
    }

    // -------------------------------------------------------------------------
    // Stream helpers
    // -------------------------------------------------------------------------

    /**
     * Skips exactly {@code bytesToSkip} bytes from the given input stream.
     *
     * <p>{@link InputStream#skip(long)} is not guaranteed to skip the requested number of bytes
     * in one call (it may return a smaller value without reaching EOF). This method loops and,
     * if {@code skip()} returns zero or a negative value, falls back to reading one byte at a
     * time to guarantee progress and to detect unexpected end-of-stream.</p>
     *
     * @param is          the input stream to skip bytes in
     * @param bytesToSkip number of bytes to skip; must be {@code >= 0}
     * @throws IOException if the end of the stream is reached before the skip is complete,
     *                     or if an I/O error occurs
     */
    public static void skipFully(InputStream is, long bytesToSkip) throws IOException {
        long remaining = bytesToSkip;
        while (remaining > 0) {
            long skipped = is.skip(remaining);
            if (skipped <= 0) {
                // skip() made no progress; fall back to a single-byte read to advance the stream
                if (is.read() == -1) {
                    throw new IOException("Unexpected end of stream while skipping to range start");
                }
                skipped = 1;
            }
            remaining -= skipped;
        }
    }

    /**
     * Copies all bytes from {@code is} to {@code os} using an internal buffer.
     *
     * <p>This method closes neither stream; the caller is responsible for closing them.</p>
     *
     * @param is the source stream
     * @param os the destination stream
     * @throws IOException if an I/O error occurs during reading or writing
     */
    public static void copy(InputStream is, OutputStream os) throws IOException {
        byte[] buf = new byte[COPY_BUFFER_SIZE];
        int n;
        while ((n = is.read(buf, 0, buf.length)) >= 0) {
            os.write(buf, 0, n);
        }
    }

    // -------------------------------------------------------------------------
    // Private ETag comparison helpers
    // -------------------------------------------------------------------------

    /**
     * Weak ETag equality: both tags are equal after stripping weak prefix and outer quotes.
     */
    private static boolean weakEquals(String left, String right) {
        return opaqueTagValue(left).equals(opaqueTagValue(right));
    }

    /**
     * Strong ETag equality: neither tag may be weak, and the opaque values must be equal.
     */
    private static boolean strongEquals(String left, String right) {
        return isNotWeak(left) && isNotWeak(right) && weakEquals(left, right);
    }

    /**
     * Returns {@code true} if the tag value carries the {@code W/} weak indicator.
     */
    private static boolean isNotWeak(String tagValue) {
        return tagValue == null || !tagValue.trim().startsWith("W/");
    }

    /**
     * Extracts the bare opaque string from an ETag value by stripping the optional
     * {@code W/} weak prefix and surrounding double-quote characters.
     *
     * <p>Examples:</p>
     * <ul>
     *   <li>{@code "abc"} → {@code abc}</li>
     *   <li>{@code W/"abc"} → {@code abc}</li>
     *   <li>{@code abc} → {@code abc} (no-op if already unquoted)</li>
     * </ul>
     */
    private static String opaqueTagValue(String tagValue) {
        if (tagValue == null) {
            return "";
        }

        String normalized = tagValue.trim();

        // Strip the W/ weak prefix if present
        if (normalized.startsWith("W/")) {
            normalized = normalized.substring(2).trim();
        }

        // Strip surrounding double quotes
        if (normalized.startsWith("\"") && normalized.endsWith("\"") && normalized.length() > 1) {
            normalized = normalized.substring(1, normalized.length() - 1);
        }

        return normalized;
    }
}
