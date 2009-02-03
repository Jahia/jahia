/**
 * 
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Limited. All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have recieved a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license"
 * 
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Limited. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
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