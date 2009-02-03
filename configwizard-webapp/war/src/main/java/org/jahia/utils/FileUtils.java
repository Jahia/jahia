/*
 * Copyright 2002-2008 Jahia Ltd
 *
 * Licensed under the JAHIA COMMON DEVELOPMENT AND DISTRIBUTION LICENSE (JCDDL), 
 * Version 1.0 (the "License"), or (at your option) any later version; you may 
 * not use this file except in compliance with the License. You should have 
 * received a copy of the License along with this program; if not, you may obtain 
 * a copy of the License at 
 *
 *  http://www.jahia.org/license/
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 *///
//
//  FileUtils
//  EV      19.12.2000
//  MAP     24.01.2002  Files are stored into UTF-8 format.
//

package org.jahia.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.collections.iterators.EnumerationIterator;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;


public final class FileUtils {

    private static Logger logger = Logger.getLogger(FileUtils.class);

    private static FileUtils _fileUtils = null;

    private static Map<String, String> fileExtensionIcons;
    private static String[] fileExtensionIconsMapping;

    /***
     * getInstance
     * EV    19.12.2000
     * @deprecated use static methods of this class instead
     * @Deprecated use static methods of this class instead
     */
    public static FileUtils getInstance () {
        if (_fileUtils == null) {
            _fileUtils = new FileUtils();
        }
        return _fileUtils;
    }

    /***
     * EV    18.11.2000
     * MAP   24.01.2002 : What's about these limitations ?
     * FIXME     : each jahia site should have its own file directory
     * FIXME     : no more than 100 files per directory
     *
     */
    public static String composeBigTextFullPathName (String jahiaDiskPath, int jahiaID,
                                                     int pageID, int fieldID, int versionID,
                                                     int versionStatus, String languageCode) {
        return composeBigTextFullPathName(jahiaDiskPath,
                composeBigTextFileNamePart(jahiaID, pageID, fieldID, versionID, versionStatus, languageCode));
    }

    public static String composeBigTextFullPathName(String jahiaDiskPath, String bigTextFileNamePart) {
        return new StringBuilder(jahiaDiskPath.length()
                + bigTextFileNamePart.length() + 1).append(jahiaDiskPath)
                .append(File.separator).append(bigTextFileNamePart).toString();
    }

    public static String composeBigTextFileNamePart (int jahiaID, int pageID,
                                                     int fieldID, int versionID,
                                                     int versionStatus,
                                                     String languageCode) {
        StringBuilder fileName = new StringBuilder();
        fileName.append(Integer.toString(jahiaID));
        fileName.append("-");
        fileName.append(Integer.toString(pageID));
        fileName.append("-");
        fileName.append(Integer.toString(fieldID));
        fileName.append("-");
        fileName.append(languageCode);
        if (versionStatus > 1) {
            fileName.append("-s");
        } else if ( (versionID != 0) && (versionStatus <= 0)) {
            fileName.append("-" + versionID);
        }
        fileName.append(".jahia");
        return fileName.toString();
    }

    /***
     * fileExists
     * EV    18.11.2000
     * called by loadContents
     *
     */
    public static boolean fileExists (String fileName)
            throws Exception {
        try {
            File theFile = new File(fileName);
            return theFile.exists();
        } catch (SecurityException se) {
            String errorMsg = "Security error in readFile : " + se.getMessage();
            logger.error(errorMsg, se);
            throw new Exception("Cannot access to jahia files");
        }
    }

    public static final void copyInputStream(InputStream in, OutputStream out)
    throws IOException
    {
      byte[] buffer = new byte[1024];
      int len;

      while((len = in.read(buffer)) >= 0)
        out.write(buffer, 0, len);

      in.close();
      out.close();
    }

    
   public static Boolean unzipFile(String sourceFilePath, String destFilePath){

       Iterator entries;
       ZipFile zipFile;

       try {
             zipFile = new ZipFile(sourceFilePath);

             entries = new EnumerationIterator(zipFile.entries());
             while(entries.hasNext()) {
               ZipEntry entry = (ZipEntry)entries.next();

               if(entry.isDirectory()) {
                 // Assume directories are stored parents first then children.

                 
                 (new File(destFilePath+entry.getName())).mkdirs();
                 continue;
               }

               logger.debug("Extracting file to : " + destFilePath+entry.getName());
               copyInputStream(zipFile.getInputStream(entry),
                  new BufferedOutputStream(new FileOutputStream(destFilePath+entry.getName())));
             }

             zipFile.close();
           } catch (IOException ioe) {
             logger.error("Unhandled exception:"+ioe);
             return false;
           }
       return true;
           
   }



    public static String readFile(String fileName) throws Exception {

        String content = null;

        BufferedReader in;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(
                    fileName), "UTF-8"));
        } catch (IOException e) {
            in = null;
            logger.debug("Cannot read files in UTF-8 format trying ASCII", e);
        }
        try {
            if (null == in) {
                in = new BufferedReader(new InputStreamReader(
                        new FileInputStream(fileName)));
            }
            content = readerToString(in);

            IOUtils.closeQuietly(in);
        } catch (IOException e) {
            logger.error("Cannot read requested file '" + fileName + "'", e);
            throw new Exception("Cannot access to Jahia files");
        }
        return content;
    }


    public static String readFile (String fileName, String langCode)
            throws Exception {
        return readFile(fileName);
    }


    public static void writeFile (String fileName, String fileContent)
            throws Exception {
        try {
            BufferedWriter out = new BufferedWriter(
                    new OutputStreamWriter(
                            new FileOutputStream(fileName), "UTF-8"));
            out.write(fileContent);
            out.close();
        } catch (IOException ie) {
            String errorMsg = "Error in writeFile : " + fileName +
                    "\nIOException : " + ie.getMessage();
            logger.error( errorMsg, ie);
            throw new Exception("Cannot access to jahia files");
        }
    }


    public static void writeFile (String fileName, String fileContent, String langCode)
            throws Exception {
        writeFile(fileName, fileContent);
    }

    /***
     * deleteFile
     * EV    07.02.2001
     *
     */
    public static boolean deleteFile (String fileName) {
        File theFile = new File(fileName);
        return theFile.delete();
    }

    /**
     * renames a file
     * @param oldFileName name of the old file
     * @param newFileName destination name
     * @return true if it worked
     */
    public static boolean renameFile (String oldFileName, String newFileName) {
        File oldFile = new File(oldFileName);
        File newFile = new File(newFileName);
        return oldFile.renameTo(newFile);
    }

    // called by contentbigtextfield
    /**
     * copy a file
     * @param oldFileName name of the old file
     * @param newFileName destination name
     * @return true if it worked
     */
    public static boolean copyFile (String oldFileName, String newFileName) {
        File oldFile = new File(oldFileName);
        File newFile = new File(newFileName);
        if (!oldFile.exists()) {
            logger.error("Cannot copy file: source file doesn't exist");
            return false;
        }
        if (oldFile.getAbsolutePath().equals(newFile.getAbsolutePath())) {
            return true;
        }
        try {
            copyStream(new FileInputStream(oldFile),
                    new FileOutputStream(newFile));
        } catch (IOException ioe) {
            return false;
        }
        return true;
    }
    // called by contentbigtextfield
    /**
     * copy a file
     * @param oldFile name of the old file
     * @param newFile destination name
     * @return true if it worked
     */
    public static boolean copyFile (File oldFile, File newFile) {

        if (!oldFile.exists()) {
            logger.error("Cannot copy file: source file doesn't exist");
            return false;
        }
        if (oldFile.getAbsolutePath().equals(newFile.getAbsolutePath())) {
            return true;
        }
        try {
            copyStream(new FileInputStream(oldFile),
                    new FileOutputStream(newFile));
        } catch (IOException ioe) {
            return false;
        }
        return true;
    }
    /***
     * copyStream
     * EV    30.11.2000
     * called by download
     *
     */
    public static void copyStream (InputStream ins, OutputStream outs)
            throws IOException {
        int writeBufferSize = 4096;
        byte[] writeBuffer = new byte[writeBufferSize];

        BufferedInputStream bis = new BufferedInputStream(ins, writeBufferSize);
        BufferedOutputStream bos = new BufferedOutputStream(outs,
                writeBufferSize);
        int bufread;
        while ( (bufread = bis.read(writeBuffer)) != -1) {
            bos.write(writeBuffer, 0, bufread);
        }
        bos.flush();
        bos.close();
        bis.close();
    }

    /**
     * Return the content of a Reader as String
     * @param reader
     * @return
     */
    public static String readerToString(Reader reader) throws IOException{
        if ( reader == null ){
            return null;
        }
        StringBuilder buff = new StringBuilder(512);
        char[] stringToRead = new char[4096];
        int count = -1;
        while ( (count = reader.read(stringToRead, 0, 4096)) != -1) {
            buff.append(stringToRead, 0, count);
        }
        return buff.toString();
    }

    /**
     * Try to read a file in UTF-8 then GBK and finally ASCII, null if none
     * of them success
     *
     * @param inputStream
     * @return
     */
    public static String getFileCharset(InputStream inputStream){

        try {
            new BufferedReader(
                    new InputStreamReader(
                            inputStream, "UTF-8"));
            // success
            return "UTF-8";
        } catch (IOException ie) {
        }
        try {
            new BufferedReader(
                    new InputStreamReader(
                            inputStream, "GBK"));
            // success
            return "GBK";
        } catch (IOException ie) {
        }
        try {
            new BufferedReader(
                    new InputStreamReader(
                            inputStream, "ASCII"));
            // success
            return "ASCII";
        } catch (IOException ie) {
        }
        return null;
    }

    // Copies all files under srcDir to dstDir.
    // If dstDir does not exist, it will be created.
    public static void copyDirectory(File srcDir, File dstDir) throws IOException {
        if (srcDir.isDirectory()) {
            if (!dstDir.exists()) {
                dstDir.mkdir();
            }

            String[] children = srcDir.list();
            for (int i=0; i<children.length; i++) {
                copyDirectory(new File(srcDir, children[i]),
                        new File(dstDir, children[i]));
            }
        } else {
            // This method is implemented in e1071 Copying a File
            copyFile(srcDir, dstDir);
        }
    }



    /***
     * constructor
     * EV    19.12.2000
     *
     */
    private FileUtils () {
        logger.debug("Starting fileUtils");
    }

   static  public boolean deleteDirectory(File path) {
        if( path.exists() ) {
            File[] files = path.listFiles();
            for(int i=0; i<files.length; i++) {
                if(files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                }
                else {
                    files[i].delete();
                }
            }
        }
        return( path.delete() );
    }


}