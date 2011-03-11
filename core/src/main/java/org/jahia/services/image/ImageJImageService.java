package org.jahia.services.image;

import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ImageProcessor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.tools.imageprocess.ImageProcess;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 3/11/11
 * Time: 11:33
 * To change this template use File | Settings | File Templates.
 */
public class ImageJImageService  implements JahiaImageService {
    private static ImageJImageService instance;

    protected ImageJImageService() {

    }

    public void init() {
    }

    public static synchronized ImageJImageService getInstance() {
        if (instance == null) {
            instance = new ImageJImageService();
        }
        return instance;
    }



    public Image getImage(JCRNodeWrapper node) throws IOException, RepositoryException {
        File tmp = null;
        FileOutputStream os = null;
        try {
            tmp = File.createTempFile("image", null);
            Node contentNode = node.getNode(Constants.JCR_CONTENT);
            os = new FileOutputStream(tmp);
            InputStream is = contentNode.getProperty(Constants.JCR_DATA).getBinary().getStream();
            try {
                IOUtils.copy(is, os);
            } finally {
                IOUtils.closeQuietly(os);
                IOUtils.closeQuietly(is);
            }
            Opener op = new Opener();
            ImagePlus ip = op.openImage(tmp.getPath());
            return new ImageJImage(node.getPath(), ip, op.getFileType(tmp.getPath()));
        } finally {
            IOUtils.closeQuietly(os);
            FileUtils.deleteQuietly(tmp);
        }
    }


    /**
     * Creates a JPEG thumbnail from inputFile and saves it to disk in
     * outputFile. scaleWidth is the width to scale the image to
     */
    public boolean createThumb(Image iw, File outputFile, int size, boolean square) throws IOException {
        ImagePlus ip = ((ImageJImage)iw).getImagePlus();
        // Load the input image.
        if (ip == null) {
            return false;
        }
        ip = new ImagePlus(ip.getTitle(), ip.getImageStack());
        ImageProcessor processor = ip.getProcessor();
        if(square) {
            processor = processor.resize(size, size);
        } else if (ip.getWidth() > ip.getHeight()) {
            processor = processor.resize(size, ip.getHeight()*size/ip.getWidth());
        } else {
            processor = processor.resize(ip.getWidth()*size/ip.getHeight(), size);
        }
        ip.setProcessor(null,processor);
        int type = ((ImageJImage) iw).getImageType();

        return ImageProcess.save(type, ip, outputFile);
    }

    public int getHeight(Image i) {
        ImagePlus ip = ((ImageJImage)i).getImagePlus();
        if (ip != null) {
            return ip.getHeight();
        }
        return -1;
    }

    public int getWidth(Image i) {
        ImagePlus ip = ((ImageJImage)i).getImagePlus();
        if (ip != null) {
            return ip.getWidth();
        }
        return -1;
    }

    public boolean cropImage(Image i, File outputFile, int top, int left, int width, int height) {
        ImagePlus ip = ((ImageJImage)i).getImagePlus();
        ImageProcessor processor = ip.getProcessor();

        processor.setRoi(left, top, width, height);
        processor = processor.crop();
        ip.setProcessor(null, processor);

        return ImageProcess.save(((ImageJImage) i).getImageType(), ip, outputFile);
    }

    public boolean resizeImage(Image i, File outputFile, int width, int height) {
        ImagePlus ip = ((ImageJImage)i).getImagePlus();
        ImageProcessor processor = ip.getProcessor();
        processor = processor.resize(width, height);
        ip.setProcessor(null, processor);

        return ImageProcess.save(((ImageJImage) i).getImageType(), ip, outputFile);
    }

    public boolean rotateImage(Image i, File outputFile, boolean clockwise) {
        ImagePlus ip = ((ImageJImage)i).getImagePlus();
        ImageProcessor processor = ip.getProcessor();
        if (clockwise) {
            processor = processor.rotateRight();
        } else {
            processor = processor.rotateLeft();
        }
        ip.setProcessor(null, processor);

        return ImageProcess.save(((ImageJImage) i).getImageType(), ip, outputFile);
    }

}
