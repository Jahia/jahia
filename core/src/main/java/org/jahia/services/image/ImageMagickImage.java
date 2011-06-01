package org.jahia.services.image;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: 3/11/11
 * Time: 14:49
 * To change this template use File | Settings | File Templates.
 */
public class ImageMagickImage implements Image {
    private File file;
    private String path;

    public ImageMagickImage(File file, String path) {
        this.file = file;
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public File getFile() {
        return file;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        file.delete();
    }
}
