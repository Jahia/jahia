package org.jahia.services.image;

import org.apache.commons.io.IOUtils;
import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import org.im4java.core.Info;
import org.im4java.core.InfoException;
import org.im4java.process.ProcessStarter;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 3/11/11
 * Time: 11:33
 * To change this template use File | Settings | File Templates.
 */
public class ImageMagickImageService implements JahiaImageService {

    private static ImageMagickImageService instance;

    private String imageMagickPath;

    protected ImageMagickImageService() {

    }

    public void init() {
        ProcessStarter.setGlobalSearchPath(imageMagickPath);
    }

    public static synchronized ImageMagickImageService getInstance() {
        if (instance == null) {
            instance = new ImageMagickImageService();
        }
        return instance;
    }

    public void setImageMagickPath(String imageMagickPath) {
        this.imageMagickPath = imageMagickPath;
    }

    public Image getImage(JCRNodeWrapper node) throws IOException, RepositoryException {
        File tmp = File.createTempFile("image", "tmp");
        Node contentNode = node.getNode(Constants.JCR_CONTENT);
        IOUtils.copy(contentNode.getProperty(Constants.JCR_DATA).getStream(), new FileOutputStream(tmp));
        return new ImageMagickImage(tmp, node.getPath());
    }

    private File getFile(Image i) {
        return ((ImageMagickImage) i).getFile();
    }

    public boolean createThumb(Image iw, File outputFile, int size, boolean square) throws IOException {
        try {
            // create command
            ConvertCmd cmd = new ConvertCmd();

            // create the operation, add images and operators/options
            IMOperation op = new IMOperation();
            op.addImage(getFile(iw).getPath());

            if(square) {
                op.resize(size,size,"^");
                op.gravity("center");
                op.crop(size,size,0,0);
            } else {
                op.resize(size,size);
            }

            op.addImage(outputFile.getPath());

            cmd.run(op);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public int getHeight(Image i) throws IOException {
        try {
            Info imageInfo = new Info(getFile(i).getPath());
            return Integer.parseInt(imageInfo.getProperty("Geometry").split("[x+]")[1]);
        } catch (InfoException e) {
            throw new IOException(e);
        }
    }

    public int getWidth(Image i) throws IOException {
        try {
            Info imageInfo = new Info(getFile(i).getPath());
            return Integer.parseInt(imageInfo.getProperty("Geometry").split("[x+]")[0]);
        } catch (InfoException e) {
            throw new IOException(e);
        }
    }

    public boolean cropImage(Image i, File outputFile, int top, int left, int width, int height) throws IOException {
        try {
            // create command
            ConvertCmd cmd = new ConvertCmd();

            // create the operation, add images and operators/options
            IMOperation op = new IMOperation();
            op.addImage(getFile(i).getPath());
            op.crop(width, height, left, top);
            op.addImage(outputFile.getPath());

            cmd.run(op);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean resizeImage(Image i, File outputFile, int width, int height) throws IOException {
        try {
            // create command
            ConvertCmd cmd = new ConvertCmd();

            // create the operation, add images and operators/options
            IMOperation op = new IMOperation();
            op.addImage(getFile(i).getPath());
            op.resize(width, height);
            op.addImage(outputFile.getPath());

            cmd.run(op);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean rotateImage(Image i, File outputFile, boolean clockwise) throws IOException {
        try {
            // create command
            ConvertCmd cmd = new ConvertCmd();

            // create the operation, add images and operators/options
            IMOperation op = new IMOperation();
            op.addImage(getFile(i).getPath());
            op.rotate(clockwise ? 90. : -90.);
            op.addImage(outputFile.getPath());

            cmd.run(op);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
