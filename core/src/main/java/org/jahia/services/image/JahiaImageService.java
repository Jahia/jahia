package org.jahia.services.image;

import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.RepositoryException;
import java.io.File;
import java.io.IOException;

public interface JahiaImageService {

    public Image getImage(JCRNodeWrapper node) throws IOException, RepositoryException;

    public boolean createThumb(Image iw, File outputFile, int size, boolean square) throws IOException;

    public int getHeight(Image i) throws IOException;

    public int getWidth(Image i) throws IOException;

    public boolean cropImage(Image i, File outputFile, int top, int left, int width, int height) throws IOException;

    public boolean resizeImage(Image i, File outputFile, int width, int height) throws IOException;

    public boolean rotateImage(Image i, File outputFile, boolean clockwise) throws IOException;



}
