package org.jahia.services.image;

import ij.ImagePlus;

/**
* Created by IntelliJ IDEA.
* User: toto
* Date: 3/11/11
* Time: 14:41
* To change this template use File | Settings | File Templates.
*/
public class ImageJImage implements Image {
    private String path;

    private ImagePlus ip;
    private int imageType;

    public ImageJImage(String path, ImagePlus ip, int imageType) {
        this.path = path;
        this.imageType = imageType;
        this.ip = ip;
    }

    public String getPath() {
        return path;
    }

    public ImagePlus getImagePlus() {
        return ip;
    }

    public int getImageType() {
        return imageType;
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        ip.close();
    }
}
