/**
 * Jahia Enterprise Edition v6
 *
 * Copyright (C) 2002-2009 Jahia Solutions Group. All rights reserved.
 *
 * Jahia delivers the first Open Source Web Content Integration Software by combining Enterprise Web Content Management
 * with Document Management and Portal features.
 *
 * The Jahia Enterprise Edition is delivered ON AN "AS IS" BASIS, WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR
 * IMPLIED.
 *
 * Jahia Enterprise Edition must be used in accordance with the terms contained in a separate license agreement between
 * you and Jahia (Jahia Sustainable Enterprise License - JSEL).
 *
 * If you are unsure which license is appropriate for your use, please contact the sales department at sales@jahia.com.
 */
package org.jahia.services.applications;

import javax.servlet.ServletOutputStream;

import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * A simple ServletOutputStream based class that writes to a String and
 * simultaneously to an output stream if configured.
 * Note : none of this is supposed to be optimised in anyway, it was done a
 * quick and dirty wayusing mostly already available functions calls to make
 * this reliable.
 *
 * @author : Serge Huber
 * @todo Test this code with Orion, WebLogic, etc... This might be very
 * container-sensitive code.
 */
public class StringServletOutputStream extends ServletOutputStream {

    private ByteArrayOutputStream byteArray = new ByteArrayOutputStream(4096);
    private ServletOutputStream existingStream;
    private String encoding;

    public StringServletOutputStream (String encoding) throws UnsupportedEncodingException {
        super ();
        this.encoding = encoding;
    }

    public StringServletOutputStream (ServletOutputStream existingStream, String encoding)
            throws UnsupportedEncodingException {
        this(encoding);
        this.existingStream = existingStream;
    }

   public void write (int c)
            throws IOException {
        byteArray.write (c);
        if (existingStream != null) {
            existingStream.write (c);
        }
    }

    public String getBuffer () throws UnsupportedEncodingException {
        return byteArray.toString (encoding);
    }

    public void flush ()
            throws IOException {
        if (existingStream != null) {
            existingStream.flush ();
        }
    }

    public void close () {
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        byteArray.write(b, off, len);
        if (existingStream != null) {
            existingStream.write(b, off, len);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        byteArray.write(b);
        if (existingStream != null) {
            existingStream.write(b);
        }
    }
}