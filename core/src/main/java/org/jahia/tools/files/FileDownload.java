/**
 * This file is part of Jahia: An integrated WCM, DMS and Portal Solution
 * Copyright (C) 2002-2009 Jahia Solutions Group SA. All rights reserved.
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
 * Commercial and Supported Versions of the program
 * Alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms contained in a separate written agreement
 * between you and Jahia Solutions Group SA. If you are unsure which license is appropriate
 * for your use, please contact the sales department at sales@jahia.com.
 */
 package org.jahia.tools.files;


/*
 * FileDownload
 * Copyright (c) 2000 Jahia Ltd  All rights reserved.
 *
 */


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class FileDownload.
 * Class to handle filedownload
 *
 * @author Khue ng
 * @version 1.0
 */
public class FileDownload {


    private HttpServletResponse m_Res;

    private String m_FileStorageFullPath = "";
    private String m_FileContentType;

    private int m_BufferSize = 65536;
    private byte[] m_WriteBuffer = new byte[m_BufferSize];

    /**
     * Constructor
     *
     * @param req the HttpServletRequest object
     * @param res the HttpServletResponse object
     * @param fileRealName the real name of the file
     * @param fileStorageFullPath the storage name of the file
     * @param fileContentType the Content Type of the file
     */
     public FileDownload(
                                HttpServletRequest req,
                                HttpServletResponse res,
                                String fileRealName,
                                String fileStorageFullPath,
                                String fileContentType){

        m_Res = res;
        m_FileStorageFullPath = fileStorageFullPath;
        if ( fileContentType != null )
            m_FileContentType = fileContentType;

     }


    /**
     * Method writeFileContentToResponse<br>
     * Write file content to the client
     *
     * @exception IOException
     */
    public void writeFileContentToResponse() throws IOException {

        toConsole("FileDownload, fullRealpath is " + m_FileStorageFullPath);
        File theFile = new File(m_FileStorageFullPath);
        if ( theFile.exists() && theFile.isFile() && theFile.canRead()	){

            // Write Header
            // m_Res.setHeader("Content-Disposition","attachment; filename=\"" + m_FileRealName + "\""); apparently confuses IE more that helps him !
            m_Res.setContentLength((int)theFile.length());
            m_Res.setContentType(m_FileContentType);
            //m_Res.setHeader("content-type",m_FileContentType);

            //m_Res.setDateHeader("Date", new Date().getTime());
            //m_Res.setDateHeader("Expires", new Date().getTime());
            //m_Res.setIntHeader("Age",0);
            //m_Res.setIntHeader("Retry-After",60);
            //m_Res.setHeader("Pragma","no-cache");
            //m_Res.setHeader("Connection","Keep-Alive");

            try {
                copyStream(new FileInputStream(m_FileStorageFullPath), m_Res.getOutputStream());
            } catch (IOException ioe) {
                toConsole("FileDownload :: Error writing file to response" + ioe.getMessage());
                throw new IOException("FileDownload::writeFileContentToResponse error while writing to client");
            }

            /*
            if ( m_Res.isCommitted() ){
                m_Res.flushBuffer();
            }
            */
        } else {
            toConsole("FileDownload :: checkFile, file denoted by " + m_FileStorageFullPath + " doesn't exist or cannot be read");
            throw new IOException("FileDownload::writeFileContentToResponse error while trying to open file");
        }

    }


    /**
    * Method copyStream<br>
    *
    * @param ins An InputStream.
    * @param outs An OutputStream.
    * @exception IOException.
    */
   private void copyStream(InputStream ins,
                            OutputStream outs)
       throws IOException
   {
       BufferedInputStream bis =
           new BufferedInputStream(ins, m_BufferSize);
       BufferedOutputStream bos =
           new BufferedOutputStream(outs, m_BufferSize);
       int bufread;
        while((bufread = bis.read(m_WriteBuffer)) != -1)
           bos.write(m_WriteBuffer,0,bufread);
       bos.flush(); bos.close();
       bis.close();
       //outs.close();
   }


   /**
     * Method toConsole
     * For debugging purpose
    *
    * @param msg any String message to write to the console
    */
    public void toConsole(String msg) {
        if (false){
            //System.out.println(msg);
        }
    }


}