/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.servlets;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This class is based on code from Tomcat's DefaultServlet, available at :
 *
 * http://svn.apache.org/repos/asf/tomcat/tc6.0.x/trunk/java/org/apache/catalina/servlets/DefaultServlet.java
 */
public class RangeUtils {

    /**
     * The input buffer size to use when serving resources.
     */
    private static int inputBufferSize = 2048;
    /**
     * The output buffer size to use when serving resources.
     */
    private static int output = 2048;

    /**
     * Full range marker.
     */
    public static final List<Range> FULL = new ArrayList<Range>();

    /**
     * MIME multipart separation string
     */
    public static final String MIME_SEPARATION = "CATALINA_MIME_BOUNDARY";

    /**
     * Parse the range header.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     * @return Vector of ranges
     */
    public static List<Range> parseRange(HttpServletRequest request,
                                HttpServletResponse response,
                                String eTag, long lastModified, long fileLength)
        throws IOException {

        // Checking If-Range
        String headerValue = request.getHeader("If-Range");

        if (headerValue != null) {

            long headerValueTime = (-1L);
            try {
                headerValueTime = request.getDateHeader("If-Range");
            } catch (IllegalArgumentException e) {
                ;
            }

            if (headerValueTime == (-1L)) {

                // If the ETag the client gave does not match the entity
                // etag, then the entire entity is returned.
                if (!eTag.equals(headerValue.trim()))
                    return FULL;

            } else {

                // If the timestamp of the entity the client got is older than
                // the last modification date of the entity, the entire entity
                // is returned.
                if (lastModified > (headerValueTime + 1000))
                    return FULL;

            }

        }

        if (fileLength == 0)
            return null;

        // Retrieving the range header (if any is specified
        String rangeHeader = request.getHeader("Range");

        if (rangeHeader == null)
            return null;
        // bytes is the only range unit supported (and I don't see the point
        // of adding new ones).
        if (!rangeHeader.startsWith("bytes")) {
            response.addHeader("Content-Range", "bytes */" + fileLength);
            response.sendError
                (HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
            return null;
        }

        rangeHeader = rangeHeader.substring(6);

        // Vector which will contain all the ranges which are successfully
        // parsed.
        ArrayList<Range> result = new ArrayList<Range>();
        StringTokenizer commaTokenizer = new StringTokenizer(rangeHeader, ",");

        // Parsing the range list
        while (commaTokenizer.hasMoreTokens()) {
            String rangeDefinition = commaTokenizer.nextToken().trim();

            Range currentRange = new Range();
            currentRange.length = fileLength;

            int dashPos = rangeDefinition.indexOf('-');

            if (dashPos == -1) {
                response.addHeader("Content-Range", "bytes */" + fileLength);
                response.sendError
                    (HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return null;
            }

            if (dashPos == 0) {

                try {
                    long offset = Long.parseLong(rangeDefinition);
                    currentRange.start = fileLength + offset;
                    currentRange.end = fileLength - 1;
                } catch (NumberFormatException e) {
                    response.addHeader("Content-Range",
                                       "bytes */" + fileLength);
                    response.sendError
                        (HttpServletResponse
                         .SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return null;
                }

            } else {

                try {
                    currentRange.start = Long.parseLong
                        (rangeDefinition.substring(0, dashPos));
                    if (dashPos < rangeDefinition.length() - 1)
                        currentRange.end = Long.parseLong
                            (rangeDefinition.substring
                             (dashPos + 1, rangeDefinition.length()));
                    else
                        currentRange.end = fileLength - 1;
                } catch (NumberFormatException e) {
                    response.addHeader("Content-Range",
                                       "bytes */" + fileLength);
                    response.sendError
                        (HttpServletResponse
                         .SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                    return null;
                }

            }

            if (!currentRange.validate()) {
                response.addHeader("Content-Range", "bytes */" + fileLength);
                response.sendError
                    (HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                return null;
            }

            result.add(currentRange);
        }

        return result;
    }

/**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param ostream The output stream to write to
     * @param range Range the client wanted to retrieve
     * @exception IOException if an input/output error occurs
     */
    public static void copy(InputStream resourceInputStream, ServletOutputStream ostream,
                      Range range)
        throws IOException {

        IOException exception = null;

        InputStream istream =
            new BufferedInputStream(resourceInputStream, inputBufferSize);
        exception = copyRange(istream, ostream, range.start, range.end);

        // Clean up the input stream
        istream.close();

        // Rethrow any exception that has occurred
        if (exception != null)
            throw exception;

    }

    /**
         * Copy the contents of the specified input stream to the specified
         * output stream, and ensure that both streams are closed before returning
         * (even in the face of an exception).
         *
         * @param ostream The output stream to write to
         * @param ranges Enumeration of the ranges the client wanted to retrieve
         * @param contentType Content type of the resource
         * @exception IOException if an input/output error occurs
         */
        public static void copy(InputStream resourceInputStream, ServletOutputStream ostream,
                          Iterator<Range> ranges, String contentType)
            throws IOException {

            IOException exception = null;

            while ( (exception == null) && (ranges.hasNext()) ) {

                InputStream istream =
                    new BufferedInputStream(resourceInputStream, inputBufferSize);

                Range currentRange = (Range) ranges.next();

                // Writing MIME header.
                ostream.println();
                ostream.println("--" + MIME_SEPARATION);
                if (contentType != null)
                    ostream.println("Content-Type: " + contentType);
                ostream.println("Content-Range: bytes " + currentRange.start
                               + "-" + currentRange.end + "/"
                               + currentRange.length);
                ostream.println();

                // Printing content
                exception = copyRange(istream, ostream, currentRange.start,
                                      currentRange.end);

                istream.close();

            }

            ostream.println();
            ostream.print("--" + MIME_SEPARATION + "--");

            // Rethrow any exception that has occurred
            if (exception != null)
                throw exception;

        }

    /**
     * Copy the contents of the specified input stream to the specified
     * output stream, and ensure that both streams are closed before returning
     * (even in the face of an exception).
     *
     * @param istream The input stream to read from
     * @param ostream The output stream to write to
     * @param start Start of the range which will be copied
     * @param end End of the range which will be copied
     * @return Exception which occurred during processing
     */
    public static IOException copyRange(InputStream istream,
                                  ServletOutputStream ostream,
                                  long start, long end) {

        try {
            istream.skip(start);
        } catch (IOException e) {
            return e;
        }

        IOException exception = null;
        long bytesToRead = end - start + 1;

        byte buffer[] = new byte[inputBufferSize];
        int len = buffer.length;
        while ( (bytesToRead > 0) && (len >= buffer.length)) {
            try {
                len = istream.read(buffer);
                if (bytesToRead >= len) {
                    ostream.write(buffer, 0, len);
                    bytesToRead -= len;
                } else {
                    ostream.write(buffer, 0, (int) bytesToRead);
                    bytesToRead = 0;
                }
            } catch (IOException e) {
                exception = e;
                len = -1;
            }
            if (len < buffer.length)
                break;
        }

        return exception;

    }

    public static int getInputBufferSize() {
        return inputBufferSize;
    }

    public static void setInputBufferSize(int inputBufferSize) {
        RangeUtils.inputBufferSize = inputBufferSize;
    }

    public static int getOutput() {
        return output;
    }

    public static void setOutput(int output) {
        RangeUtils.output = output;
    }

    public static class Range {

           public long start;
           public long end;
           public long length;

           /**
            * Validate range.
            */
           public boolean validate() {
               if (end >= length)
                   end = length - 1;
               return ( (start >= 0) && (end >= 0) && (start <= end)
                        && (length > 0) );
           }

           public void recycle() {
               start = 0;
               end = 0;
               length = 0;
           }

       }
}
