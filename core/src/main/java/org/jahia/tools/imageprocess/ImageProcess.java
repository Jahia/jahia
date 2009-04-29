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
//
//
//                                   ____.
//                       __/\ ______|    |__/\.     _______
//            __   .____|    |       \   |    +----+       \
//    _______|  /--|    |    |    -   \  _    |    :    -   \_________
//   \\______: :---|    :    :           |    :    |         \________>
//           |__\---\_____________:______:    :____|____:_____\
//                                      /_____|
//
//              . . . i n   j a h i a   w e   t r u s t . . .
//
//  04.10.2005  POL  First release

package org.jahia.tools.imageprocess;

import ij.ImagePlus;
import ij.WindowManager;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.jahia.utils.FileUtils;

public class ImageProcess {
    
    private static final transient Logger logger = Logger
            .getLogger(ImageProcess.class);

    /**
     * Creates a JPEG thumbnail from inputFile and saves it to disk in
     * outputFile. scaleWidth is the width to scale the image to
     */
    public boolean createThumb(InputStream istream, File outputFile, int size) throws IOException {

        File parDir = outputFile.getParentFile();
        if (!parDir.exists())
            parDir.mkdir(); // create directory for thumbnails

        // Load the input image.

        File tmp = File.createTempFile("image","tmp");
        FileUtils.copyStream(istream, new FileOutputStream(tmp));
        Opener op = new Opener();
        ImagePlus ip = op.openImage(tmp.getPath());
        if (ip == null ) {
            return false;
        }
        int type = op.getFileType(tmp.getPath());
        tmp.delete();
        ImageProcessor processor = ip.getProcessor();
        if (ip.getWidth() > ip.getHeight()) {
            processor = processor.resize(size, ip.getHeight()*size/ip.getWidth());
        } else {
            processor = processor.resize(ip.getWidth()*size/ip.getHeight(), size);
        }
        ip.setProcessor(null,processor);

        return save(type,ip,outputFile);
    }

    public static boolean save(int type, ImagePlus ip, File outputFile) {
        switch (type) {
            case Opener.TIFF:
                return new FileSaver(ip).saveAsTiff(outputFile.getPath());
            case Opener.GIF:
                return new FileSaver(ip).saveAsGif(outputFile.getPath());
            case Opener.JPEG:
                return new FileSaver(ip).saveAsJpeg(outputFile.getPath());
            case Opener.TEXT:
                return new FileSaver(ip).saveAsText(outputFile.getPath());
            case Opener.LUT:
                return new FileSaver(ip).saveAsLut(outputFile.getPath());
            case Opener.ZIP:
                return new FileSaver(ip).saveAsZip(outputFile.getPath());
            case Opener.BMP:
                return new FileSaver(ip).saveAsBmp(outputFile.getPath());
            case Opener.PNG:
                ImagePlus tempImage = WindowManager.getTempCurrentImage();
                WindowManager.setTempCurrentImage(ip);
                PlugIn p =null;
                try {
                    p = (PlugIn) Class.forName("ij.plugin.PNG_Writer").newInstance();
                } catch (InstantiationException e) {
                    logger.error(e.getMessage(), e);
                } catch (IllegalAccessException e) {
                    logger.error(e.getMessage(), e);
                } catch (ClassNotFoundException e) {
                    logger.error(e.getMessage(), e);
                }
                p.run(outputFile.getPath());
                WindowManager.setTempCurrentImage(tempImage);
                return true;
            case Opener.PGM:
                return new FileSaver(ip).saveAsPgm(outputFile.getPath());
        }
        return false;
    }
}
