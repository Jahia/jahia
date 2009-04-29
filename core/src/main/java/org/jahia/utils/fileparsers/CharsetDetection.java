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
 package org.jahia.utils.fileparsers;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.mozilla.intl.chardet.nsPSMDetector;

/**
 * <p>Title: Char set detection , based on jcharset lib</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 *
 * @author Khue Nguyen
 * @version 1.0
 */
public class CharsetDetection implements nsICharsetDetectionObserver {

    private String charSet = null;

    public CharsetDetection(){

    }

    /**
     * Returns the charset. You must call charsetDetection(...) first
     *
     * @return String
     */
    public String getCharset(){
        return this.charSet;
    }

    /**
     * Returns true if only ascii
     *
     * @param ins InputStream
     * @throws IOException
     * @return int 1 = only ascii, 0 = not ascii, -1 = unknown
     */
    public int charsetDetection(InputStream ins) throws IOException {
        return charsetDetection(nsPSMDetector.ALL , ins);
    }

    /**
     * Returns true if only ascii
     *
     * @param url URL
     * @throws IOException
     * @return int 1 = only ascii, 0 = not ascii, -1 = unknown
     */
    public int charsetDetection(URL url) throws IOException {

        if ( url == null ){
            return -1;
        }
        return charsetDetection(nsPSMDetector.ALL , url.openStream());
    }

    /**
     * Returns true if only ascii
     *
     * @param lang int
     * @param url URL
     * @throws IOException
     * @return int 1 = only ascii, 0 = not ascii, -1 = unknown
     */
    public int charsetDetection(int lang, URL url) throws IOException {

        if ( url == null ){
            return -1;
        }
        return charsetDetection(lang, url.openStream());
    }

    /**
     * Returns true if only ascii
     *
     * @param lang int
     * @param ins InputStream
     * @throws IOException
     * @return int 1 = only ascii, 0 = not ascii, -1 = unknown
     */
    public int charsetDetection(int lang, InputStream ins) throws IOException {

        if ( ins == null ){
            return -1;
        }
        nsDetector det = new nsDetector(lang);
        det.Init(this);
        BufferedInputStream imp = new BufferedInputStream(ins);

        byte[] buf = new byte[1024];
        int len;
        boolean done = false;
        boolean isAscii = true;

        while ( (len = imp.read(buf, 0, buf.length)) != -1) {

            // Check if the stream is only ascii.
            if (isAscii)
                isAscii = det.isAscii(buf, len);

            // DoIt if non-ascii and not done yet.
            if (!isAscii && !done)
                done = det.DoIt(buf, len, true);
        }
        det.DataEnd();

        String[]charSets = det.getProbableCharsets();
        /*
        for ( int i=0; i<charSets.length ; i++ ){
            logger.debug(
                "Charset detection notification , PROBABLE CHARSET FOUND = "
                + charSets[i]);
        }*/
        if ( charSets.length>0 ){
            this.charSet = charSets[0]; // get the first
        }

        return (isAscii?1:0);
    }

    /**
     * nsICharsetDetectionObserver charset detection implementation
     *
     * @param charset String
     */
    public void Notify(String charset)
    {
        // We can't rely on this, we should prefer probableCharsets use.

        /*
        this.charSet = charset;
        logger.debug("Charset detection notification , CHARSET FOUND = "
                     + charset);
        */
    }

}
