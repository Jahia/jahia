/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2012 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.ajax.gwt.helper;

import org.apache.commons.io.IOUtils;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.service.content.ExistingFileException;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.image.Image;
import org.jahia.services.image.JahiaImageService;
import org.jahia.utils.i18n.JahiaResourceBundle;
import org.slf4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Locale;

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

    public void crop(String path, String target, int top, int left, int width, int height, boolean forceReplace, JCRSessionWrapper session, Locale uiLocale) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = session.getNode(path);
            if (contentManager
                    .checkExistence(node.getPath().replace(node.getName(), target), session, uiLocale) &&
                    !forceReplace) {
                throw new ExistingFileException("The file " + target + " already exists.");
            }
            Image image = imageService.getImage(node);
            File f = File.createTempFile("image", null);
            imageService.cropImage(image, f, top, left, width, height);

            InputStream fis = new BufferedInputStream(new FileInputStream(f));
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

    public void resizeImage(String path, String target, int width, int height, boolean forceReplace, JCRSessionWrapper session, Locale uiLocale) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = session.getNode(path);
            if (contentManager
                    .checkExistence(node.getPath().replace(node.getName(), target), session, uiLocale) &&
                    !forceReplace) {
                throw new ExistingFileException(MessageFormat.format(JahiaResourceBundle.getJahiaInternalResource("file.already.exists", uiLocale), target));
            }
            Image image = imageService.getImage(node);
            File f = File.createTempFile("image", null);
            imageService.resizeImage(image, f, width, height);

            InputStream fis = new BufferedInputStream(new FileInputStream(f));
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

    public void rotateImage(String path, String target, boolean clockwise, boolean forceReplace, JCRSessionWrapper session, Locale uiLocale) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = session.getNode(path);
            if (contentManager
                    .checkExistence(node.getPath().replace(node.getName(), target), session, uiLocale) &&
                    !forceReplace) {
                throw new ExistingFileException(MessageFormat.format(JahiaResourceBundle.getJahiaInternalResource("file.already.exists", uiLocale), target));
            }
            Image image = imageService.getImage(node);
            File f = File.createTempFile("image", null);
            imageService.rotateImage(image, f, clockwise);

            InputStream fis = new BufferedInputStream(new FileInputStream(f));
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
