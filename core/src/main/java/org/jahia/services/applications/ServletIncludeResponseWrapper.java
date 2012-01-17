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

//
//                                   ____.
//                       __/\ ______|    |__/\.     _______
//            __   .____|    |       \   |    +----+       \
//    _______|  /--|    |    |    -   \  _    |    :    -   \_________
//   \\______: :---|    :    :           |    :    |         \________>
//           |__\---\_____________:______:    :____|____:_____\
//                                      /_____|
//
//                 . . . i n   j a h i a   w e   t r u s t . . .
//

package org.jahia.services.applications;

import org.slf4j.Logger;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;


/**
 * This response wrapper allows the use of a StringPrinterWriter instead of the
 * regular output stream-based ones. This allows Jahia to get a copy of the output
 * to enable further processing before sending it back to the browser.
 * Another important feature of this wrapper is to modify the behaviour of the
 * encodeURL function call to enable the routing of action URLs to the correct
 * application.
 * Note : the implementation of the getWriter and getOutputStream could be
 * re-written by using a combination like this one PrintWriter(StringWriter(StringBuffer))
 * but this would probably involve a small performance hit (to be tested !). It
 * would have the advantage of probably removing some code that is a little
 * unnecessary. (for implementation see org.jahia.bin.JahiaErrorDisplay where
 * this has been used but performance is not an issue there).
 *
 * @author Serge Huber
 */
public class ServletIncludeResponseWrapper extends HttpServletResponseWrapper {

    private static Logger logger = org.slf4j.LoggerFactory.getLogger (ServletIncludeResponseWrapper.class);

    private PrintWriter strPrintWriter = null;
    private StringServletOutputStream strOutputStream = null;
    private String contentType;
    private boolean outputStreamCalled = false;
    private String redirectLocation = null;
    private boolean isPassThrough = false;
    private String encoding = "ISO-8859-1";
    private String forceEncoding = null;

    /**
     * Simple constructor without support for URL redirecting.
     */
    public ServletIncludeResponseWrapper (HttpServletResponse httpServletResponse,
                                          boolean passThrough, String forcedEncoding) {
        super (httpServletResponse);
        if (logger.isDebugEnabled()) {
            logger.debug ("Initializing using normal mode");
        }
        this.isPassThrough = passThrough;
        this.forceEncoding = forcedEncoding;
        if (forcedEncoding != null) {
            encoding = forcedEncoding;            
            this.contentType = "text/html; charset=" + forcedEncoding;
        }
    }

    public PrintWriter getWriter () {
        if (logger.isDebugEnabled()) {
            logger.debug ("Using a print writer for output");
        }
        checkStreams ();
        if (outputStreamCalled && logger.isDebugEnabled()) {
            logger.debug (
                    "Servlet compliance warning, OutputStream has already been called, the response output will be reset !");
        }
        return strPrintWriter;
    }

    public ServletOutputStream getOutputStream () throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug ("Using an output stream for output");
        }
        checkStreams ();
        this.outputStreamCalled = true;
        return strOutputStream;
    }

    public String getStringBuffer () throws IOException {
        return getStringBuffer (true);
    }

    public String getStringBuffer (boolean checkWriterError) throws IOException {
        try {
            if (outputStreamCalled) {
                // logger.debug("buffer=[" + strOutputStream.getBuffer(encoding) + "]");
                return strOutputStream.getBuffer ();
            } else {
                // logger.debug("buffer=[" + strOutputStream.getBuffer(encoding) + "]");
                if (strOutputStream == null) {
                    return null;
                } else {
                    if (checkWriterError && strPrintWriter.checkError ()) {
                        throw new IOException ("An error has occured while writing to output");
                    }
                    return strOutputStream.getBuffer ();
                }
            }
        } catch (UnsupportedEncodingException uee) {
            logger.debug ("Error in encoding [" + encoding + " ], returning empty buffer", uee);
            return "";
        }
    }

    public String getRedirectLocation () {
        return redirectLocation;
    }

    public String getCharacterEncoding () {
        return encoding;
    }

    public void flushBuffer () {
        if (org.jahia.settings.SettingsBean.getInstance().isWrapperBufferFlushingActivated()) {
            if (strPrintWriter != null) {
                strPrintWriter.flush();
            }
            if (strOutputStream != null) {
                try {
                    strOutputStream.flush();
                } catch (IOException ioe) {
                    logger.debug("Detailed error while flushing output stream", ioe);
                    logger.error("Error while flushing output stream : " + ioe.getMessage() + ". For stack trace, please activate debug mode");
                }
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug ("flushBuffer()");
        }
    }

    public void resetBuffer () {
        if (logger.isDebugEnabled()) {
            logger.debug ("resetBuffer()");
        }
    }

    public void finishResponse () {
        if (logger.isDebugEnabled()) {
            logger.debug ("finishResponse()");
        }
    }

    public boolean isCommitted () {
        if (logger.isDebugEnabled()) {
            logger.debug ("isCommitted()");
        }
        return false;
    }

    private static String[][] STATIC_EXTS = { null, // 0
            null, // 1
            { "js" }, // 2
            { "css", "gif", "ico", "jpg", "jpe", "htm", "png", "xml" }, // 3
            { "jpeg", "html" } // 4
    };

    private String internalEncodeURL(String url) {
        if (url != null && url.length() != 0) {
            if (url.indexOf("/webdav/") == -1 || url.indexOf('?') == -1) {
                int point = url.lastIndexOf('.'); // in case URL is absolute
                int extLen = url.length() - point - 1;
                if (extLen > 0 && extLen < STATIC_EXTS.length) {
                    String ext = url.substring(point + 1);
                    String[] exts = STATIC_EXTS[extLen];
                    if (exts != null) {
                        for (int j = 0; j < exts.length; j++)
                            if (ext.equalsIgnoreCase(exts[j]))
                                return url; // skip encoding
                    }
                }
            }
            return super.encodeURL(url);
        } else
            return url;
    }

    /**
     * The purpose of this function is to transform relative URL for a Jahia aggregation.
     * The issue here is that Jahia uses it's own parameters, and aggregates applications that
     * also use their own parameters. Therefore Jahia must encode the URL of the application so
     * that it is able to dispatch the call to the correct app and not misinterpret it for one
     * of it's own parameters.
     * <pre>
     * Example:
     *   Let's say the application generates a URL like the following one :
     *       /servlet/Test?arg1=2&arg3=4
     *   Jahia must encode it into something like this :
     *       ?context=appid&appargs=%2Fservlet%2FTest%3Farg1%3D2%26arg3%3D
     * </pre>
     */
    public String encodeURL (String url) {

        if (url == null) {
            return null;
        }

        String servletIncludeURL;

            if (url.indexOf (";jsessionid=") != -1) {
                if (logger.isDebugEnabled()) {
                    logger.debug (
                        "jsessionid already in URL, ignoring call and returning unmodified URL... ");
                }
                servletIncludeURL = url;
            } else {
                servletIncludeURL = internalEncodeURL(url); // let's add Java
            }
        if (!servletIncludeURL.startsWith("http")) {
            servletIncludeURL = super.encodeURL(servletIncludeURL);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("encodeURL return "+servletIncludeURL);
        }
        return servletIncludeURL;
    }

    public String encodeUrl (String URL) {
        return encodeURL (URL);
    }

    public void sendRedirect (String location) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug ("location=" + location + "");
        }
        if (redirectLocation != null) {
            if (logger.isDebugEnabled()) {
                logger.debug ("Multiple calls to sendRedirect, keeping only the first one.");
            }
        } else {
            if (location.endsWith ("/")) {
                redirectLocation = location.substring (0, location.length () - 1);
            } else {
                redirectLocation = location;
            }
        }
        if (isPassThrough) {
            if (logger.isDebugEnabled()) {
                logger.debug (
                    "Pass-through active, sending redirect to wrapped response directly...");
            }
            super.sendRedirect (location);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug ("redirectLocation=" + redirectLocation + "");
            }
        }
    }


    public void setContentType (java.lang.String type) {
        if (logger.isDebugEnabled()) {
            logger.debug ("Content-type set to [" + type + "]");
        }
        if (contentType != null && logger.isDebugEnabled()) {
            if (!this.contentType.equals (type)) {
                logger.debug ("Warning, content type has already been set to [" +
                        contentType + "]. Trying to set now to [" + type +
                        "]");
            }
        }
        if (forceEncoding != null) {
            if (logger.isDebugEnabled()) {
                logger.debug ("Enforcing charset=[" + forceEncoding + "]");
            }
            int separatorPos = type.indexOf (";");
            if (separatorPos > 0) {
                type = type.substring (0, separatorPos);
            }
            type += ";charset=" + forceEncoding;
        }
        this.contentType = type;
        int charsetPos = type.toLowerCase ().indexOf ("charset=");
        if (charsetPos > 0) {
            encoding = type.toUpperCase ().substring (charsetPos + "charset=".length ()).trim ();
        }
        if (isPassThrough) {
            super.setContentType (this.contentType);
            // if un-commented, serveResources of JSR286 api will not work due to the fact that getOutputStream() is called.
            /*try {
                ServletOutputStream outputStream = super.getResponse ().
                        getOutputStream ();
                if (outputStream.getClass ().getName ().equals (
                        "weblogic.servlet.internal.ServletOutputStreamImpl")) {
                    Object o = outputStream.getClass ().getMethod ("getOutput",
                            new Class[]{}).invoke (outputStream, new Object[]{});
                    o.getClass ().getMethod ("changeToCharset",
                            new Class[]{String.class,
                                        Class.
                            forName ("weblogic.servlet.internal.CharsetMap")}).
                            invoke (o, new Object[]{null, null});
                }
            } catch (Exception e) {
            } */
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug (
                    "Not setting parent response object because this wrapper is not in pass-through mode");
            }
        }
    }

    /**
     * Returns the stored content type.
     *
     * @return a string containing the set contained type, or null if none
     *         was ever set.
     */
    public String getContentType () {
        return this.contentType;
    }

    public void addCookie (Cookie cookie) {
        if (logger.isDebugEnabled()) {
            logger.debug ("Adding cookie name=" + cookie.getName ());
        }
        super.addCookie (cookie);
    }

    private void checkStreams () {
        if ((strOutputStream == null) && (strPrintWriter == null)) {
            try {
                if (isPassThrough) {
                    if (logger.isDebugEnabled()) {
                        logger.debug (
                            "Creating output streams for response wrapper using pass-through servlet output stream...");
                    }
                    strOutputStream =
                            new StringServletOutputStream (
                                    super.getResponse ().getOutputStream (), encoding);
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug (
                            "Creating output streams for response wrapper using only stringbuffers and no pass-through...");
                    }
                    strOutputStream = new StringServletOutputStream (encoding);
                }
                OutputStreamWriter streamWriter;
                if (encoding != null) {
                    if (logger.isDebugEnabled()) {
                        logger.debug ("Using PrintWriter with encoding : " + encoding);
                    }
                    streamWriter = new OutputStreamWriter (strOutputStream, encoding);
                } else {
                    streamWriter = new OutputStreamWriter (strOutputStream);
                }
                strPrintWriter = new PrintWriter (streamWriter, true);
            } catch (Exception e) {
                logger.debug ("Error creating PrintWriter object !", e);
            }
        }
    }


}
