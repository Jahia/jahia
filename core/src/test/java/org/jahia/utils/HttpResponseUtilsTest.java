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
package org.jahia.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link HttpResponseUtils}.
 *
 * <p>These tests cover logic challenging to verify through end-to-end Cypress tests: RFC edge cases, q-value parsing,
 * weak/strong ETag semantics, If-Range fallback, suffix ranges, stream helpers, etc.</p>
 */
@RunWith(MockitoJUnitRunner.class)
public class HttpResponseUtilsTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockResponse;

    // Arbitrary stable timestamp used throughout the tests (milliseconds)
    private static final long LAST_MODIFIED = 1_700_000_000_000L;

    // =========================================================================
    // buildStrongEtag
    // =========================================================================

    @Test
    public void buildStrongEtag_normalInput_returnsQuotedValue() {
        String etag = HttpResponseUtils.buildStrongEtag("abc123", LAST_MODIFIED);
        assertEquals("\"abc123-" + LAST_MODIFIED + "\"", etag);
    }

    @Test
    public void buildStrongEtag_nullStableId_usesUnknownFallback() {
        String etag = HttpResponseUtils.buildStrongEtag(null, LAST_MODIFIED);
        assertTrue("should start with double-quote", etag.startsWith("\""));
        assertTrue("should contain 'unknown'", etag.contains("unknown"));
    }

    @Test
    public void buildStrongEtag_stableIdContainsQuotes_stripsQuotes() {
        // A stableId with embedded quotes must not produce a malformed ETag
        String etag = HttpResponseUtils.buildStrongEtag("ab\"cd", LAST_MODIFIED);
        assertFalse("embedded quotes must be stripped", etag.contains("ab\"cd"));
        assertTrue("result must still be properly quoted", etag.startsWith("\"") && etag.endsWith("\""));
    }

    // =========================================================================
    // buildVariantEtag
    // =========================================================================

    @Test
    public void buildVariantEtag_fromStrongBase_appendsSuffix() {
        String base = "\"abc-" + LAST_MODIFIED + "\"";
        assertEquals("\"abc-" + LAST_MODIFIED + "-g\"", HttpResponseUtils.buildVariantEtag(base, "g"));
        assertEquals("\"abc-" + LAST_MODIFIED + "-a\"", HttpResponseUtils.buildVariantEtag(base, "a"));
    }

    @Test
    public void buildVariantEtag_fromWeakBase_stripsWeakPrefixAndProducesStrongEtag() {
        // A W/ weak ETag used as input must produce a plain strong ETag (no W/ in output)
        String weak = "W/\"abc-" + LAST_MODIFIED + "\"";
        String variant = HttpResponseUtils.buildVariantEtag(weak, "g");
        assertFalse("variant must not be weak", variant.startsWith("W/"));
        assertTrue("variant must be quoted", variant.startsWith("\"") && variant.endsWith("\""));
        assertTrue("variant must contain suffix", variant.endsWith("-g\""));
    }

    // =========================================================================
    // isCompressibleContentType
    // =========================================================================

    @Test
    public void isCompressibleContentType_nullContentType_returnsFalse() {
        assertFalse(HttpResponseUtils.isCompressibleContentType(null));
    }

    @Test
    public void isCompressibleContentType_compressibleTypes_returnsTrue() {
        // text/*
        assertTrue(HttpResponseUtils.isCompressibleContentType("text/html"));
        assertTrue(HttpResponseUtils.isCompressibleContentType("text/css"));
        assertTrue(HttpResponseUtils.isCompressibleContentType("text/javascript"));
        assertTrue(HttpResponseUtils.isCompressibleContentType("text/plain"));
        assertTrue(HttpResponseUtils.isCompressibleContentType("text/csv"));
        // application/json variants
        assertTrue(HttpResponseUtils.isCompressibleContentType("application/json"));
        assertTrue(HttpResponseUtils.isCompressibleContentType("application/ld+json"));
        assertTrue(HttpResponseUtils.isCompressibleContentType("application/manifest+json"));
        assertTrue(HttpResponseUtils.isCompressibleContentType("application/geo+json"));
        assertTrue(HttpResponseUtils.isCompressibleContentType("application/graphql"));
        // JavaScript
        assertTrue(HttpResponseUtils.isCompressibleContentType("application/javascript"));
        // XML variants
        assertTrue(HttpResponseUtils.isCompressibleContentType("application/xml"));
        assertTrue(HttpResponseUtils.isCompressibleContentType("application/xhtml+xml"));
        assertTrue(HttpResponseUtils.isCompressibleContentType("application/rss+xml"));
        assertTrue(HttpResponseUtils.isCompressibleContentType("application/atom+xml"));
        // SVG
        assertTrue(HttpResponseUtils.isCompressibleContentType("image/svg+xml"));
    }

    @Test
    public void isCompressibleContentType_binaryTypes_returnsFalse() {
        // Already-compressed image formats
        assertFalse(HttpResponseUtils.isCompressibleContentType("image/png"));
        assertFalse(HttpResponseUtils.isCompressibleContentType("image/jpeg"));
        assertFalse(HttpResponseUtils.isCompressibleContentType("image/webp"));
        assertFalse(HttpResponseUtils.isCompressibleContentType("image/avif"));
        // Generic binary
        assertFalse(HttpResponseUtils.isCompressibleContentType("application/octet-stream"));
        assertFalse(HttpResponseUtils.isCompressibleContentType("application/pdf"));
        // Archives — already compressed
        assertFalse(HttpResponseUtils.isCompressibleContentType("application/zip"));
        assertFalse(HttpResponseUtils.isCompressibleContentType("application/gzip"));
        // Fonts — woff/woff2 are already compressed internally
        assertFalse(HttpResponseUtils.isCompressibleContentType("font/woff"));
        assertFalse(HttpResponseUtils.isCompressibleContentType("font/woff2"));
        // WebAssembly — binary, compressed at build time
        assertFalse(HttpResponseUtils.isCompressibleContentType("application/wasm"));
    }

    @Test
    public void isCompressibleContentType_caseInsensitive() {
        assertTrue(HttpResponseUtils.isCompressibleContentType("TEXT/HTML"));
        assertTrue(HttpResponseUtils.isCompressibleContentType("Application/JSON"));
    }

    @Test
    public void isCompressibleContentType_typeWithCharsetParameter_returnsTrue() {
        // The method compares on the full string starting with "text/"; params follow a ";"
        // The current implementation uses startsWith so "text/css; charset=UTF-8" is covered
        assertTrue(HttpResponseUtils.isCompressibleContentType("text/css; charset=UTF-8"));
    }

    // =========================================================================
    // acceptsEncoding
    // =========================================================================

    @Test
    public void acceptsEncoding_nullHeader_returnsFalse() {
        assertFalse(HttpResponseUtils.acceptsEncoding(null, "gzip"));
    }

    @Test
    public void acceptsEncoding_nullEncoding_returnsFalse() {
        assertFalse(HttpResponseUtils.acceptsEncoding("gzip", null));
    }

    @Test
    public void acceptsEncoding_exactMatch_returnsTrue() {
        assertTrue(HttpResponseUtils.acceptsEncoding("gzip", "gzip"));
    }

    @Test
    public void acceptsEncoding_caseInsensitiveMatch_returnsTrue() {
        assertTrue(HttpResponseUtils.acceptsEncoding("GZIP", "gzip"));
        assertTrue(HttpResponseUtils.acceptsEncoding("gzip", "GZIP"));
    }

    @Test
    public void acceptsEncoding_wildcardWithNoExplicitRejection_returnsTrue() {
        assertTrue(HttpResponseUtils.acceptsEncoding("*", "gzip"));
        assertTrue(HttpResponseUtils.acceptsEncoding("deflate, *", "gzip"));
    }

    @Test
    public void acceptsEncoding_explicitQZero_returnsFalse() {
        assertFalse(HttpResponseUtils.acceptsEncoding("gzip;q=0", "gzip"));
    }

    @Test
    public void acceptsEncoding_explicitQZeroWithSpaces_returnsFalse() {
        assertFalse(HttpResponseUtils.acceptsEncoding("gzip ; q=0", "gzip"));
    }

    @Test
    public void acceptsEncoding_explicitRejectionBeatsWildcard() {
        // "gzip;q=0, *" — gzip is explicitly rejected even though wildcard allows everything else
        assertFalse(HttpResponseUtils.acceptsEncoding("gzip;q=0, *", "gzip"));
    }

    @Test
    public void acceptsEncoding_wildcardQZero_returnsFalse() {
        assertFalse(HttpResponseUtils.acceptsEncoding("*;q=0", "gzip"));
    }

    @Test
    public void acceptsEncoding_multipleTokensIncludingTarget_returnsTrue() {
        assertTrue(HttpResponseUtils.acceptsEncoding("deflate, gzip, br", "gzip"));
    }

    @Test
    public void acceptsEncoding_targetNotInList_returnsFalse() {
        assertFalse(HttpResponseUtils.acceptsEncoding("deflate, br", "gzip"));
    }

    @Test
    public void acceptsEncoding_malformedQValue_treatedAsZero() {
        assertFalse(HttpResponseUtils.acceptsEncoding("gzip;q=abc", "gzip"));
    }

    // =========================================================================
    // isIfModifiedSinceFresh
    // =========================================================================

    @Test
    public void isIfModifiedSinceFresh_headerAbsent_returnsFalse() {
        assertFalse(HttpResponseUtils.isIfModifiedSinceFresh(-1, LAST_MODIFIED));
    }

    @Test
    public void isIfModifiedSinceFresh_unknownLastModified_returnsFalse() {
        assertFalse(HttpResponseUtils.isIfModifiedSinceFresh(LAST_MODIFIED, 0));
    }

    @Test
    public void isIfModifiedSinceFresh_resourceNotModified_returnsTrue() {
        // Header timestamp >= resource timestamp (both at second precision) → still fresh
        long secondPrecision = (LAST_MODIFIED / 1000) * 1000;
        assertTrue(HttpResponseUtils.isIfModifiedSinceFresh(secondPrecision, LAST_MODIFIED));
    }

    @Test
    public void isIfModifiedSinceFresh_resourceModifiedAfterHeader_returnsFalse() {
        long headerTime = LAST_MODIFIED - 5_000; // header is 5 s in the past
        assertFalse(HttpResponseUtils.isIfModifiedSinceFresh(headerTime, LAST_MODIFIED));
    }

    @Test
    public void isIfModifiedSinceFresh_subSecondDifferenceIgnored() {
        // lastModified has sub-second precision; header is at the same second → still fresh
        long baseSecond = (LAST_MODIFIED / 1000) * 1000;
        long lastModifiedWithMillis = baseSecond + 500; // 500 ms within the same second
        assertTrue(HttpResponseUtils.isIfModifiedSinceFresh(baseSecond, lastModifiedWithMillis));
    }

    // =========================================================================
    // matchesIfNoneMatch
    // =========================================================================

    @Test
    public void matchesIfNoneMatch_nullHeader_returnsFalse() {
        assertFalse(HttpResponseUtils.matchesIfNoneMatch(null, "\"abc\""));
    }

    @Test
    public void matchesIfNoneMatch_nullEtag_returnsFalse() {
        assertFalse(HttpResponseUtils.matchesIfNoneMatch("\"abc\"", null));
    }

    @Test
    public void matchesIfNoneMatch_wildcard_returnsTrue() {
        assertTrue(HttpResponseUtils.matchesIfNoneMatch("*", "\"abc\""));
    }

    @Test
    public void matchesIfNoneMatch_exactStrongMatch_returnsTrue() {
        assertTrue(HttpResponseUtils.matchesIfNoneMatch("\"abc\"", "\"abc\""));
    }

    @Test
    public void matchesIfNoneMatch_weakVsStrong_returnsTrue() {
        // If-None-Match uses weak comparison: W/"abc" matches "abc"
        assertTrue(HttpResponseUtils.matchesIfNoneMatch("W/\"abc\"", "\"abc\""));
        assertTrue(HttpResponseUtils.matchesIfNoneMatch("\"abc\"", "W/\"abc\""));
    }

    @Test
    public void matchesIfNoneMatch_multipleTagsOneMatches_returnsTrue() {
        assertTrue(HttpResponseUtils.matchesIfNoneMatch("\"xyz\", \"abc\", \"def\"", "\"abc\""));
    }

    @Test
    public void matchesIfNoneMatch_noMatch_returnsFalse() {
        assertFalse(HttpResponseUtils.matchesIfNoneMatch("\"xyz\"", "\"abc\""));
    }

    // =========================================================================
    // matchesIfMatch
    // =========================================================================

    @Test
    public void matchesIfMatch_nullHeader_returnsFalse() {
        assertFalse(HttpResponseUtils.matchesIfMatch(null, "\"abc\""));
    }

    @Test
    public void matchesIfMatch_wildcard_returnsTrue() {
        assertTrue(HttpResponseUtils.matchesIfMatch("*", "\"abc\""));
    }

    @Test
    public void matchesIfMatch_exactStrongMatch_returnsTrue() {
        assertTrue(HttpResponseUtils.matchesIfMatch("\"abc\"", "\"abc\""));
    }

    @Test
    public void matchesIfMatch_weakVsStrong_returnsFalse() {
        // If-Match uses strong comparison: W/"abc" must NOT match "abc"
        assertFalse(HttpResponseUtils.matchesIfMatch("W/\"abc\"", "\"abc\""));
        assertFalse(HttpResponseUtils.matchesIfMatch("\"abc\"", "W/\"abc\""));
    }

    @Test
    public void matchesIfMatch_noMatch_returnsFalse() {
        assertFalse(HttpResponseUtils.matchesIfMatch("\"xyz\"", "\"abc\""));
    }

    // =========================================================================
    // isNotModified (integration of If-None-Match + If-Modified-Since priority)
    // =========================================================================

    @Test
    public void isNotModified_ifNoneMatchPresent_ignoresIfModifiedSince() {
        String etag = "\"abc\"";
        when(mockRequest.getHeader("If-None-Match")).thenReturn(etag);
        // If-None-Match matches → 304, even if If-Modified-Since would say "modified"
        assertTrue(HttpResponseUtils.isNotModified(mockRequest, etag, LAST_MODIFIED));
        // If-Modified-Since must never be consulted when If-None-Match is present
        verify(mockRequest, never()).getDateHeader("If-Modified-Since");
    }

    @Test
    public void isNotModified_ifNoneMatchMismatch_returns200EvenIfDateFresh() {
        when(mockRequest.getHeader("If-None-Match")).thenReturn("\"other\"");
        // ETag mismatch → must return false (serve 200), regardless of date
        assertFalse(HttpResponseUtils.isNotModified(mockRequest, "\"abc\"", LAST_MODIFIED));
    }

    @Test
    public void isNotModified_noIfNoneMatch_fallsBackToIfModifiedSince() {
        when(mockRequest.getHeader("If-None-Match")).thenReturn(null);
        long secondPrecision = (LAST_MODIFIED / 1000) * 1000;
        when(mockRequest.getDateHeader("If-Modified-Since")).thenReturn(secondPrecision);
        assertTrue(HttpResponseUtils.isNotModified(mockRequest, "\"abc\"", LAST_MODIFIED));
    }

    @Test
    public void isNotModified_noHeadersAtAll_returnsFalse() {
        when(mockRequest.getHeader("If-None-Match")).thenReturn(null);
        when(mockRequest.getDateHeader("If-Modified-Since")).thenReturn(-1L);
        assertFalse(HttpResponseUtils.isNotModified(mockRequest, "\"abc\"", LAST_MODIFIED));
    }

    // =========================================================================
    // appendVaryHeader
    // =========================================================================

    @Test
    public void appendVaryHeader_emptyHeader_setsValue() {
        when(mockResponse.getHeader("Vary")).thenReturn(null);
        HttpResponseUtils.appendVaryHeader(mockResponse, "Accept-Encoding");
        verify(mockResponse).setHeader("Vary", "Accept-Encoding");
    }

    @Test
    public void appendVaryHeader_valueAlreadyPresent_doesNotDuplicate() {
        when(mockResponse.getHeader("Vary")).thenReturn("Accept-Encoding");
        HttpResponseUtils.appendVaryHeader(mockResponse, "Accept-Encoding");
        verify(mockResponse, never()).setHeader(eq("Vary"), anyString());
    }

    @Test
    public void appendVaryHeader_valueAlreadyPresentCaseInsensitive_doesNotDuplicate() {
        when(mockResponse.getHeader("Vary")).thenReturn("accept-encoding");
        HttpResponseUtils.appendVaryHeader(mockResponse, "Accept-Encoding");
        verify(mockResponse, never()).setHeader(eq("Vary"), anyString());
    }

    @Test
    public void appendVaryHeader_differentValuePresent_appends() {
        when(mockResponse.getHeader("Vary")).thenReturn("Accept-Language");
        HttpResponseUtils.appendVaryHeader(mockResponse, "Accept-Encoding");
        verify(mockResponse).setHeader("Vary", "Accept-Language, Accept-Encoding");
    }

    @Test
    public void appendVaryHeader_nullValue_isNoOp() {
        HttpResponseUtils.appendVaryHeader(mockResponse, null);
        verify(mockResponse, never()).setHeader(anyString(), anyString());
    }

    @Test
    public void appendVaryHeader_emptyValue_isNoOp() {
        HttpResponseUtils.appendVaryHeader(mockResponse, "");
        verify(mockResponse, never()).setHeader(anyString(), anyString());
    }

    // =========================================================================
    // setContentLength
    // =========================================================================

    @Test
    public void setContentLength_smallValue_usesIntMethod() {
        HttpResponseUtils.setContentLength(mockResponse, 1024L);
        verify(mockResponse).setContentLength(1024);
        verify(mockResponse, never()).setHeader(eq("Content-Length"), anyString());
    }

    @Test
    public void setContentLength_largeValue_usesStringHeader() {
        long big = (long) Integer.MAX_VALUE + 1;
        HttpResponseUtils.setContentLength(mockResponse, big);
        verify(mockResponse).setHeader("Content-Length", Long.toString(big));
        verify(mockResponse, never()).setContentLength(anyInt());
    }

    @Test
    public void setContentLength_zeroValue_usesIntMethod() {
        HttpResponseUtils.setContentLength(mockResponse, 0L);
        verify(mockResponse).setContentLength(0);
    }

    @Test
    public void setContentLength_negativeValue_isNoOp() {
        HttpResponseUtils.setContentLength(mockResponse, -1L);
        verify(mockResponse, never()).setContentLength(anyInt());
        verify(mockResponse, never()).setHeader(eq("Content-Length"), anyString());
    }

    // =========================================================================
    // parseRange
    // =========================================================================

    private static final long CONTENT_LENGTH = 1000L;
    private static final String IDENTITY_ETAG = "\"abc-" + LAST_MODIFIED + "-a\"";

    @Before
    public void resetMocks() {
        // Provide safe defaults so tests that don't care about If-Range don't need to set it up
        lenient().when(mockRequest.getHeader("If-Range")).thenReturn(null);
    }

    @Test
    public void parseRange_noRangeHeader_returnsNull() throws IOException {
        when(mockRequest.getHeader("Range")).thenReturn(null);
        assertNull(HttpResponseUtils.parseRange(mockRequest, mockResponse, CONTENT_LENGTH, IDENTITY_ETAG, LAST_MODIFIED));
    }

    @Test
    public void parseRange_contentLengthZero_returnsNull() throws IOException {
        when(mockRequest.getHeader("Range")).thenReturn("bytes=0-99");
        assertNull(HttpResponseUtils.parseRange(mockRequest, mockResponse, 0, IDENTITY_ETAG, LAST_MODIFIED));
    }

    @Test
    public void parseRange_validMidRange_returnsCorrectByteRange() throws IOException {
        when(mockRequest.getHeader("Range")).thenReturn("bytes=100-199");
        HttpResponseUtils.ByteRange range = HttpResponseUtils.parseRange(mockRequest, mockResponse, CONTENT_LENGTH, IDENTITY_ETAG,
                LAST_MODIFIED);
        assertNotNull(range);
        assertNotSame(HttpResponseUtils.UNSATISFIABLE_RANGE, range);
        assertEquals(100L, range.start);
        assertEquals(199L, range.end);
        assertEquals(CONTENT_LENGTH, range.total);
        assertEquals(100L, range.length());
    }

    @Test
    public void parseRange_endBeyondFileSize_clampsToLastByte() throws IOException {
        when(mockRequest.getHeader("Range")).thenReturn("bytes=900-9999");
        HttpResponseUtils.ByteRange range = HttpResponseUtils.parseRange(mockRequest, mockResponse, CONTENT_LENGTH, IDENTITY_ETAG,
                LAST_MODIFIED);
        assertNotNull(range);
        assertEquals(900L, range.start);
        assertEquals(999L, range.end); // clamped to contentLength - 1
    }

    @Test
    public void parseRange_openEndedRange_readsToEndOfFile() throws IOException {
        when(mockRequest.getHeader("Range")).thenReturn("bytes=500-");
        HttpResponseUtils.ByteRange range = HttpResponseUtils.parseRange(mockRequest, mockResponse, CONTENT_LENGTH, IDENTITY_ETAG,
                LAST_MODIFIED);
        assertNotNull(range);
        assertEquals(500L, range.start);
        assertEquals(999L, range.end);
    }

    @Test
    public void parseRange_suffixRange_returnsLastNBytes() throws IOException {
        when(mockRequest.getHeader("Range")).thenReturn("bytes=-100");
        HttpResponseUtils.ByteRange range = HttpResponseUtils.parseRange(mockRequest, mockResponse, CONTENT_LENGTH, IDENTITY_ETAG,
                LAST_MODIFIED);
        assertNotNull(range);
        assertEquals(900L, range.start);
        assertEquals(999L, range.end);
        assertEquals(100L, range.length());
    }

    @Test
    public void parseRange_suffixRangeLargerThanFile_clampsToStart() throws IOException {
        when(mockRequest.getHeader("Range")).thenReturn("bytes=-9999");
        HttpResponseUtils.ByteRange range = HttpResponseUtils.parseRange(mockRequest, mockResponse, CONTENT_LENGTH, IDENTITY_ETAG,
                LAST_MODIFIED);
        assertNotNull(range);
        assertEquals(0L, range.start);
        assertEquals(999L, range.end);
    }

    @Test
    public void parseRange_multiRangeRequest_returns416() throws IOException {
        when(mockRequest.getHeader("Range")).thenReturn("bytes=0-10,20-30");
        HttpResponseUtils.ByteRange range = HttpResponseUtils.parseRange(mockRequest, mockResponse, CONTENT_LENGTH, IDENTITY_ETAG,
                LAST_MODIFIED);
        assertSame(HttpResponseUtils.UNSATISFIABLE_RANGE, range);
        verify(mockResponse).sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
    }

    @Test
    public void parseRange_nonBytesUnit_returns416() throws IOException {
        when(mockRequest.getHeader("Range")).thenReturn("items=0-10");
        HttpResponseUtils.ByteRange range = HttpResponseUtils.parseRange(mockRequest, mockResponse, CONTENT_LENGTH, IDENTITY_ETAG,
                LAST_MODIFIED);
        assertSame(HttpResponseUtils.UNSATISFIABLE_RANGE, range);
        verify(mockResponse).sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
    }

    @Test
    public void parseRange_startBeyondEndOfFile_returns416() throws IOException {
        when(mockRequest.getHeader("Range")).thenReturn("bytes=2000-2999");
        HttpResponseUtils.ByteRange range = HttpResponseUtils.parseRange(mockRequest, mockResponse, CONTENT_LENGTH, IDENTITY_ETAG,
                LAST_MODIFIED);
        assertSame(HttpResponseUtils.UNSATISFIABLE_RANGE, range);
        verify(mockResponse).sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
    }

    @Test
    public void parseRange_startAfterEnd_returns416() throws IOException {
        when(mockRequest.getHeader("Range")).thenReturn("bytes=200-100");
        HttpResponseUtils.ByteRange range = HttpResponseUtils.parseRange(mockRequest, mockResponse, CONTENT_LENGTH, IDENTITY_ETAG,
                LAST_MODIFIED);
        assertSame(HttpResponseUtils.UNSATISFIABLE_RANGE, range);
        verify(mockResponse).sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
    }

    @Test
    public void parseRange_noSeparatorInSpec_returns416() throws IOException {
        // "bytes=100" passes the "bytes=" prefix and no-comma checks, but the spec "100"
        // contains no '-' → separator < 0 → must return 416
        when(mockRequest.getHeader("Range")).thenReturn("bytes=100");
        HttpResponseUtils.ByteRange range = HttpResponseUtils.parseRange(mockRequest, mockResponse, CONTENT_LENGTH, IDENTITY_ETAG,
                LAST_MODIFIED);
        assertSame(HttpResponseUtils.UNSATISFIABLE_RANGE, range);
        verify(mockResponse).sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
    }

    @Test
    public void parseRange_nonNumericBoundary_returns416() throws IOException {
        // "bytes=abc-def" passes the "bytes=" prefix and no-comma checks but triggers
        // NumberFormatException when Long.parseLong() tries to parse "abc" → must return 416
        when(mockRequest.getHeader("Range")).thenReturn("bytes=abc-def");
        HttpResponseUtils.ByteRange range = HttpResponseUtils.parseRange(mockRequest, mockResponse, CONTENT_LENGTH, IDENTITY_ETAG,
                LAST_MODIFIED);
        assertSame(HttpResponseUtils.UNSATISFIABLE_RANGE, range);
        verify(mockResponse).sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
    }

    @Test
    public void parseRange_ifRangeEtagMatches_servesRange() throws IOException {
        when(mockRequest.getHeader("Range")).thenReturn("bytes=0-99");
        when(mockRequest.getHeader("If-Range")).thenReturn(IDENTITY_ETAG);
        HttpResponseUtils.ByteRange range = HttpResponseUtils.parseRange(mockRequest, mockResponse, CONTENT_LENGTH, IDENTITY_ETAG,
                LAST_MODIFIED);
        assertNotNull(range);
        assertNotSame(HttpResponseUtils.UNSATISFIABLE_RANGE, range);
    }

    @Test
    public void parseRange_ifRangeEtagMismatch_fallsBackToFullResponse() throws IOException {
        // If-Range ETag doesn't match and isn't a valid date → serve full resource (return null)
        when(mockRequest.getHeader("Range")).thenReturn("bytes=0-99");
        when(mockRequest.getHeader("If-Range")).thenReturn("\"stale-etag\"");
        when(mockRequest.getDateHeader("If-Range")).thenThrow(new IllegalArgumentException("not a date"));
        HttpResponseUtils.ByteRange range = HttpResponseUtils.parseRange(mockRequest, mockResponse, CONTENT_LENGTH, IDENTITY_ETAG,
                LAST_MODIFIED);
        assertNull(range);
    }

    @Test
    public void parseRange_ifRangeDateFresh_servesRange() throws IOException {
        // If-Range date is >= lastModified → the cached copy is still valid → serve range
        when(mockRequest.getHeader("Range")).thenReturn("bytes=0-99");
        when(mockRequest.getHeader("If-Range")).thenReturn("not-matching-etag");
        long freshDate = LAST_MODIFIED + 5_000;
        when(mockRequest.getDateHeader("If-Range")).thenReturn(freshDate);
        HttpResponseUtils.ByteRange range = HttpResponseUtils.parseRange(mockRequest, mockResponse, CONTENT_LENGTH, IDENTITY_ETAG,
                LAST_MODIFIED);
        assertNotNull(range);
        assertNotSame(HttpResponseUtils.UNSATISFIABLE_RANGE, range);
    }

    @Test
    public void parseRange_ifRangeDateStale_fallsBackToFullResponse() throws IOException {
        // If-Range date is older than lastModified by more than 1 second → serve full resource
        when(mockRequest.getHeader("Range")).thenReturn("bytes=0-99");
        when(mockRequest.getHeader("If-Range")).thenReturn("not-matching-etag");
        long staleDate = LAST_MODIFIED - 5_000;
        when(mockRequest.getDateHeader("If-Range")).thenReturn(staleDate);
        assertNull(HttpResponseUtils.parseRange(mockRequest, mockResponse, CONTENT_LENGTH, IDENTITY_ETAG, LAST_MODIFIED));
    }

    // =========================================================================
    // skipFully
    // =========================================================================

    @Test
    public void skipFully_skipZeroBytes_doesNothing() throws IOException {
        InputStream is = spy(new ByteArrayInputStream(new byte[] { 1, 2, 3 }));
        HttpResponseUtils.skipFully(is, 0);
        verify(is, never()).read();
    }

    @Test
    public void skipFully_normalSkip_positionsStreamCorrectly() throws IOException {
        byte[] data = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 };
        InputStream is = new ByteArrayInputStream(data);
        HttpResponseUtils.skipFully(is, 5);
        assertEquals("next byte after skip should be 5", 5, is.read());
    }

    @Test(expected = IOException.class)
    public void skipFully_unexpectedEndOfStream_throwsIOException() throws IOException {
        // Only 3 bytes available, trying to skip 10
        InputStream is = new ByteArrayInputStream(new byte[] { 1, 2, 3 });
        HttpResponseUtils.skipFully(is, 10);
    }

    // =========================================================================
    // copy
    // =========================================================================

    @Test
    public void copy_copiesAllBytes() throws IOException {
        String content = "Hello, world!";
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        HttpResponseUtils.copy(is, os);
        assertEquals(content, os.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void copy_emptyStream_writesNothing() throws IOException {
        InputStream is = new ByteArrayInputStream(new byte[0]);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        HttpResponseUtils.copy(is, os);
        assertEquals(0, os.size());
    }

    @Test
    public void copy_largePayload_copiesAllBytes() throws IOException {
        // Larger than the 1024-byte internal buffer to exercise the loop
        byte[] data = new byte[4096];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }
        InputStream is = new ByteArrayInputStream(data);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        HttpResponseUtils.copy(is, os);
        assertArrayEquals(data, os.toByteArray());
    }
}
