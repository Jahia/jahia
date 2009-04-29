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
 package org.jahia.bin;

import org.jahia.exceptions.JahiaBadRequestException;
import org.jahia.services.deamons.filewatcher.FileListSync;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 25 avr. 2006
 * Time: 10:36:54
 * To change this template use File | Settings | File Templates.
 */
public class GetFile extends HttpServlet {
    private static transient Logger logger = Logger.getLogger(GetFile.class);

    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        String key = httpServletRequest.getParameter("key");
        if (FileListSync.getInstance().getKey().equals(key)) {
            String filename = httpServletRequest.getParameter("filename");
            if (!filename.startsWith("/")) {
                filename = "/" + filename;
            }
            logger.info("Try to upload file : "+filename);
            InputStream is = getServletContext().getResourceAsStream(filename);
            try {
                OutputStream os = httpServletResponse.getOutputStream();
                byte[] buff = new byte[2048];
                int i;
                while ( (i=is.read(buff))>0) {
                    os.write(buff,0,i);
                }
            } finally {
                if(is!=null) is.close();
            }
        } else {
            logger.error("Synchronization error, incompatiable keys - check your sync config");
            throw new JahiaBadRequestException("Synchronization error, incompatiable keys - check your sync config");
        }
    }
}