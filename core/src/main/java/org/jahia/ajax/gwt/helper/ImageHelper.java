/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.ajax.gwt.helper;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jahia.ajax.gwt.client.service.GWTJahiaServiceException;
import org.jahia.ajax.gwt.client.service.content.ExistingFileException;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.image.Image;
import org.jahia.services.image.JahiaImageService;
import org.jahia.services.image.JahiaImageService.ResizeType;
import org.jahia.utils.i18n.Messages;
import org.slf4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
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
            String fileExtension = FilenameUtils.getExtension(node.getName());
            if ((fileExtension != null) && (!"".equals(fileExtension))) {
                fileExtension += "." + fileExtension;
            } else {
                fileExtension = null;
            }
            File f = File.createTempFile("image", fileExtension);
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
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.crop.image", uiLocale, e.getLocalizedMessage()));
        }
    }

    public void resizeImage(String path, String target, int width, int height, boolean forceReplace, JCRSessionWrapper session, Locale uiLocale) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = session.getNode(path);
            if (contentManager
                    .checkExistence(node.getPath().replace(node.getName(), target), session, uiLocale) &&
                    !forceReplace) {
                throw new ExistingFileException(Messages.getInternalWithArguments("file.already.exists", uiLocale, target));
            }
            Image image = imageService.getImage(node);
            String fileExtension = FilenameUtils.getExtension(node.getName());
            if ((fileExtension != null) && (!"".equals(fileExtension))) {
                fileExtension += "." + fileExtension;
            } else {
                fileExtension = null;
            }
            File f = File.createTempFile("image", fileExtension);
            imageService.resizeImage(image, f, width, height, ResizeType.SCALE_TO_FILL);

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
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.resize.image", uiLocale, e.getLocalizedMessage()));
        }
    }

    public void rotateImage(String path, String target, boolean clockwise, boolean forceReplace, JCRSessionWrapper session, Locale uiLocale) throws GWTJahiaServiceException {
        try {
            JCRNodeWrapper node = session.getNode(path);
            if (contentManager
                    .checkExistence(node.getPath().replace(node.getName(), target), session, uiLocale) &&
                    !forceReplace) {
                throw new ExistingFileException(Messages.getInternalWithArguments("file.already.exists", uiLocale, target));
            }
            Image image = imageService.getImage(node);
            String fileExtension = FilenameUtils.getExtension(node.getName());
            if ((fileExtension != null) && (!"".equals(fileExtension))) {
                fileExtension += "." + fileExtension;
            } else {
                fileExtension = null;
            }
            File f = File.createTempFile("image", fileExtension);
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
            throw new GWTJahiaServiceException(Messages.getInternalWithArguments("label.gwt.error.cannot.rotate.image", uiLocale, e.getLocalizedMessage()));
        }
    }


}
