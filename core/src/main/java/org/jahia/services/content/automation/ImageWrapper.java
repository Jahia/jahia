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
package org.jahia.services.content.automation;

import ij.ImagePlus;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Oct 21, 2008
 * Time: 5:14:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class ImageWrapper {
    private NodeWrapper parentNode;

    private ImagePlus ip;
    private int imageType;

    public ImageWrapper(NodeWrapper parentNode, ImagePlus ip,int imageType) {
        this.parentNode = parentNode;
        this.imageType = imageType;
        this.ip = ip;
    }

    public NodeWrapper getParentNode() {
        return parentNode;
    }

    public ImagePlus getImagePlus() {
        return ip;
    }

    public int getImageType() {
        return imageType;
    }
}
