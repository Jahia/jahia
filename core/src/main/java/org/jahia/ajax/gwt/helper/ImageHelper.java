package org.jahia.ajax.gwt.helper;

import ij.ImagePlus;
import ij.io.Opener;
import ij.process.ImageProcessor;
import org.apache.commons.io.IOUtils;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.service.content.ExistingFileException;
import org.jahia.services.content.JCRContentUtils;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.image.Image;
import org.jahia.services.image.JahiaImageService;
import org.jahia.tools.imageprocess.ImageProcess;
import org.slf4j.Logger;

import java.io.File;
import java.io.FileInputStream;

public class ImageHelper {
    private static final transient Logger logger = org.slf4j.LoggerFactory.getLogger(ImageHelper.class);

    private ContentManagerHelper contentManager;
    private JahiaImageService imageService;

    public void setContentManager(ContentManagerHelper contentManager) {
        this.contentManager = contentManager;
    }

    public void setImageService(JahiaImageService imageService) {
        this.imageService = imageService;
    }

    public void crop(String path, String target, int top, int left, int width, int height, boolean forceReplace, JCRSessionWrapper session) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = session.getNode(path);
            if (contentManager
                    .checkExistence(node.getPath().replace(node.getName(), target), session) &&
                    !forceReplace) {
                throw new ExistingFileException("The file " + target + " already exists.");
            }
            Image image = imageService.getImage(node);
            File f = File.createTempFile("image", null);
            imageService.cropImage(image, f, top, left, width, height);

            FileInputStream fis = new FileInputStream(f);
            try {
                node.getParent().uploadFile(target, fis, node.getFileContent().getContentType());
                session.save();
            } finally {
                IOUtils.closeQuietly(fis);
                f.delete();
            }
        } catch (ExistingFileException e) {
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void resizeImage(String path, String target, int width, int height, boolean forceReplace, JCRSessionWrapper session) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = session.getNode(path);
            if (contentManager
                    .checkExistence(node.getPath().replace(node.getName(), target), session) &&
                    !forceReplace) {
                throw new ExistingFileException("The file " + target + " already exists.");
            }
            Image image = imageService.getImage(node);
            File f = File.createTempFile("image", null);
            imageService.resizeImage(image, f, width, height);

            FileInputStream fis = new FileInputStream(f);
            try {
                node.getParent().uploadFile(target, fis, node.getFileContent().getContentType());
                session.save();
            } finally {
                IOUtils.closeQuietly(fis);
                f.delete();
            }
        } catch (ExistingFileException e) {
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }

    public void rotateImage(String path, String target, boolean clockwise, boolean forceReplace, JCRSessionWrapper session) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = session.getNode(path);
            if (contentManager
                    .checkExistence(node.getPath().replace(node.getName(), target), session) &&
                    !forceReplace) {
                throw new ExistingFileException("The file " + target + " already exists.");
            }
            Image image = imageService.getImage(node);
            File f = File.createTempFile("image", null);
            imageService.rotateImage(image, f, true);

            FileInputStream fis = new FileInputStream(f);
            try {
                node.getParent().uploadFile(target, fis, node.getFileContent().getContentType());
                session.save();
            } finally {
                IOUtils.closeQuietly(fis);
                f.delete();
            }
        } catch (ExistingFileException e) {
            throw e;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new GWTJahiaServiceException(e.getMessage());
        }
    }


}
