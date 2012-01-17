/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
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
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.utils.zip;

import org.apache.commons.lang.ArrayUtils;

import java.io.*;
import java.util.zip.CRC32;
import java.util.zip.Inflater;
import java.util.zip.ZipException;

/**
 * This class implements an input stream filter for reading files in the
 * ZIP file format. Includes support for both compressed and uncompressed
 * entries.
 *
 * @author	David Connelly
 * @version	1.33, 02/07/03
 */
public
class ZipInputStream extends InflaterInputStream implements ZipConstants {
    private ZipEntry entry;
    private CRC32 crc = new CRC32();
    private long remaining;
    private byte[] tmpbuf = new byte[512];

    private static final int STORED = ZipEntry.STORED;
    private static final int DEFLATED = ZipEntry.DEFLATED;

    private boolean closed = false;
    // this flag is set to true after EOF has reached for
    // one entry
    private boolean entryEOF = false;

    /**
     * Check to make sure that this stream has not been closed
     */
    private void ensureOpen() throws IOException {
	if (closed) {
	    throw new IOException("Stream closed");
        }
    }

    /**
     * Creates a new ZIP input stream.
     * @param in the actual input stream
     */
    public ZipInputStream(InputStream in) {
	super(new PushbackInputStream(in, 512), new Inflater(true), 512);
        usesDefaultInflater = true;
        if(in == null) {
            throw new NullPointerException("in is null");
        }
    }

    /**
     * Reads the next ZIP file entry and positions stream at the beginning
     * of the entry data.
     * @return the ZipEntry just read
     * @exception ZipException if a ZIP file error has occurred
     * @exception IOException if an I/O error has occurred
     */
    public ZipEntry getNextEntry() throws IOException {
        ensureOpen();
	if (entry != null) {
	    closeEntry();
	}
	crc.reset();
	inf.reset();
	if ((entry = readLOC()) == null) {
	    return null;
	}
	if (entry.method == STORED) {
	    remaining = entry.size;
	}
        entryEOF = false;
	return entry;
    }

    /**
     * Closes the current ZIP entry and positions the stream for reading the
     * next entry.
     * @exception java.util.zip.ZipException if a ZIP file error has occurred
     * @exception IOException if an I/O error has occurred
     */
    public void closeEntry() throws IOException {
        ensureOpen();
	while (read(tmpbuf, 0, tmpbuf.length) != -1) ;
        entryEOF = true;
    }

    /**
     * Returns 0 after EOF has reached for the current entry data,
     * otherwise always return 1.
     * <p>
     * Programs should not count on this method to return the actual number
     * of bytes that could be read without blocking.
     *
     * @return     1 before EOF and 0 after EOF has reached for current entry.
     * @exception  IOException  if an I/O error occurs.
     *
     */
    public int available() throws IOException {
        ensureOpen();
        if (entryEOF) {
            return 0;
        } else {
            return 1;
        }
    }

    /**
     * Reads from the current ZIP entry into an array of bytes. Blocks until
     * some input is available.
     * @param b the buffer into which the data is read
     * @param off the start offset of the data
     * @param len the maximum number of bytes read
     * @return the actual number of bytes read, or -1 if the end of the
     *         entry is reached
     * @exception java.util.zip.ZipException if a ZIP file error has occurred
     * @exception IOException if an I/O error has occurred
     */
    public int read(byte[] b, int off, int len) throws IOException {
        ensureOpen();
        if (off < 0 || len < 0 || off > b.length - len) {
	    throw new IndexOutOfBoundsException();
	} else if (len == 0) {
	    return 0;
	}

	if (entry == null) {
	    return -1;
	}
	switch (entry.method) {
	case DEFLATED:
	    len = super.read(b, off, len);
	    if (len == -1) {
		readEnd(entry);
                entryEOF = true;
		entry = null;
	    } else {
		crc.update(b, off, len);
	    }
	    return len;
	case STORED:
	    if (remaining <= 0) {
                entryEOF = true;
		entry = null;
		return -1;
	    }
	    if (len > remaining) {
		len = (int)remaining;
	    }
	    len = in.read(b, off, len);
	    if (len == -1) {
		throw new ZipException("unexpected EOF");
	    }
	    crc.update(b, off, len);
	    remaining -= len;
	    return len;
	default:
	    throw new InternalError("invalid compression method");
	}
    }

    /**
     * Skips specified number of bytes in the current ZIP entry.
     * @param n the number of bytes to skip
     * @return the actual number of bytes skipped
     * @exception java.util.zip.ZipException if a ZIP file error has occurred
     * @exception IOException if an I/O error has occurred
     * @exception IllegalArgumentException if n < 0
     */
    public long skip(long n) throws IOException {
        if (n < 0) {
            throw new IllegalArgumentException("negative skip length");
        }
        ensureOpen();
	int max = (int)Math.min(n, Integer.MAX_VALUE);
	int total = 0;
	while (total < max) {
	    int len = max - total;
	    if (len > tmpbuf.length) {
		len = tmpbuf.length;
	    }
	    len = read(tmpbuf, 0, len);
	    if (len == -1) {
                entryEOF = true;
		break;
	    }
	    total += len;
	}
	return total;
    }

    /**
     * Closes the ZIP input stream.
     * @exception IOException if an I/O error has occurred
     */
    public void close() throws IOException {
        if (!closed) {
	    super.close();
            closed = true;
        }
    }

    private byte[] b = new byte[256];

    /*
     * Reads local file (LOC) header for next entry.
     */
    private ZipEntry readLOC() throws IOException {
	try {
	    readFully(tmpbuf, 0, LOCHDR);
	} catch (EOFException e) {
	    return null;
	}
	if (get32(tmpbuf, 0) != LOCSIG) {
	    return null;
	}
	// get the entry name and create the ZipEntry first
	int len = get16(tmpbuf, LOCNAM);
	if (len == 0) {
	    throw new ZipException("missing entry name");
	}
        int blen = b.length;
        if (len > blen) {
            do
                blen = blen * 2;
            while (len > blen);
            b = new byte[blen];
        }
	readFully(b, 0, len);
	ZipEntry e = createZipEntry(getUTF8String(b, 0, len));
	// now get the remaining fields for the entry
	e.version = get16(tmpbuf, LOCVER);
	e.flag = get16(tmpbuf, LOCFLG);
	if ((e.flag & 1) == 1) {
	    throw new ZipException("encrypted ZIP entry not supported");
	}
	e.method = get16(tmpbuf, LOCHOW);
	e.time = get32(tmpbuf, LOCTIM);
	if ((e.flag & 8) == 8) {
	    /* EXT descriptor present */
	    if (e.method != DEFLATED) {
		throw new ZipException(
			"only DEFLATED entries can have EXT descriptor");
	    }
	} else {
	    e.crc = get32(tmpbuf, LOCCRC);
	    e.csize = get32(tmpbuf, LOCSIZ);
	    e.size = get32(tmpbuf, LOCLEN);
	}
	len = get16(tmpbuf, LOCEXT);
	if (len > 0) {
	    byte[] bb = new byte[len];
	    readFully(bb, 0, len);
	    e.extra = bb;
	}
	return e;
    }

    private boolean utfFailed = false;
    private String encoding = "Cp437";

    /*
     * Fetches a UTF8-encoded String from the specified byte array.
     */
    private String getUTF8String(byte[] b, int off, int len) {
        try {
            if (!utfFailed) {
                String r = new String(b,off,len,"UTF-8");
                byte[] b2 = r.getBytes("UTF-8");
                if (ArrayUtils.isEquals(b2, ArrayUtils.subarray(b,0,b2.length))) {
                    return r;
                } else {
                    utfFailed = true;
                }
            }
            return new String(b,off,len,encoding);
        } catch (UnsupportedEncodingException e) {
            return new String(b,off,len);
        }
    }

    /**
     * Creates a new <code>ZipEntry</code> object for the specified
     * entry name.
     *
     * @param name the ZIP file entry name
     * @return the ZipEntry just created
     */
    protected ZipEntry createZipEntry(String name) {
	return new ZipEntry(name);
    }

    /*
     * Reads end of deflated entry as well as EXT descriptor if present.
     */
    private void readEnd(ZipEntry e) throws IOException {
	int n = inf.getRemaining();
	if (n > 0) {
	    ((PushbackInputStream)in).unread(buf, len - n, n);
	}
	if ((e.flag & 8) == 8) {
	    /* EXT descriptor present */
	    readFully(tmpbuf, 0, EXTHDR);
	    long sig = get32(tmpbuf, 0);
            if (sig != EXTSIG) { // no EXTSIG present
                e.crc = sig;
                e.csize = get32(tmpbuf, EXTSIZ - EXTCRC);
                e.size = get32(tmpbuf, EXTLEN - EXTCRC);
                ((PushbackInputStream)in).unread(
                                           tmpbuf, EXTHDR - EXTCRC - 1, EXTCRC);
            } else {
                e.crc = get32(tmpbuf, EXTCRC);
                e.csize = get32(tmpbuf, EXTSIZ);
                e.size = get32(tmpbuf, EXTLEN);
            }
	}
	if (e.size != inf.getTotalOut()) {
	    throw new ZipException(
		"invalid entry size (expected " + e.size + " but got " +
		inf.getTotalOut() + " bytes)");
	}
	if (e.csize != inf.getTotalIn()) {
	    throw new ZipException(
		"invalid entry compressed size (expected " + e.csize +
		" but got " + inf.getTotalIn() + " bytes)");
	}
	if (e.crc != crc.getValue()) {
	    throw new ZipException(
		"invalid entry CRC (expected 0x" + Long.toHexString(e.crc) +
		" but got 0x" + Long.toHexString(crc.getValue()) + ")");
	}
    }

    /*
     * Reads bytes, blocking until all bytes are read.
     */
    private void readFully(byte[] b, int off, int len) throws IOException {
	while (len > 0) {
	    int n = in.read(b, off, len);
	    if (n == -1) {
		throw new EOFException();
	    }
	    off += n;
	    len -= n;
	}
    }

    /*
     * Fetches unsigned 16-bit value from byte array at specified offset.
     * The bytes are assumed to be in Intel (little-endian) byte order.
     */
    private static final int get16(byte b[], int off) {
	return (b[off] & 0xff) | ((b[off+1] & 0xff) << 8);
    }

    /*
     * Fetches unsigned 32-bit value from byte array at specified offset.
     * The bytes are assumed to be in Intel (little-endian) byte order.
     */
    private static final long get32(byte b[], int off) {
	return get16(b, off) | ((long)get16(b, off+2) << 16);
    }
}
