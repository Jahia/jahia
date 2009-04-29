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