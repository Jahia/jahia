/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
package org.jahia.bundles.core.services.images;

import org.jahia.api.settings.SettingsBean;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.image.Image;
import org.jahia.services.image.JahiaImageService;
import org.jahia.utils.DeprecationUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Delegator component that selects and delegates to the appropriate image service implementation
 * based on configuration. This is the single OSGi service registered as JahiaImageService.
 *
 * <p>It dynamically instantiates one of three internal implementations based on the jahia.properties configuration:</p>
 * <ul>
 *   <li><b>ImageJAndJava2DImageService</b> (default) - Combined ImageJ + Java2D</li>
 *   <li><b>ImageMagickImageService</b> - External ImageMagick CLI</li>
 *   <li><b>Java2DImageService</b> - Pure Java2D</li>
 * </ul>
 */
@Component(service = JahiaImageService.class, immediate = true)
public class ImageServiceDelegator implements JahiaImageService {

    private static final Logger logger = LoggerFactory.getLogger(ImageServiceDelegator.class);

    private static final String SERVICE_IMAGEMAGICK6 = "ImageMagickImage6Service";
    private static final String SERVICE_IMAGEMAGICK = "ImageMagickImageService";
    private static final String IMAGE_JIMAGE_SERVICE = "ImageJImageService";
    private static final String SERVICE_IMAGEJ_JAVA2D = "ImageJAndJava2DImageService";

    private JahiaImageService delegateService;
    private String currentServiceType;

    private SettingsBean settingsBean;

    @Activate
    protected void activate() {
        logger.debug("Image service delegator activated");
        selectAndInitializeService(settingsBean.getPropertyValue("imageService"));
    }

    @Deactivate
    protected void deactivate() {
        delegateService = null;
        currentServiceType = null;
        logger.debug("Image service delegator deactivated");
    }

    /**
     * Selects and initializes the appropriate service implementation based on configuration.
     *
     * @param serviceType The service configuration
     */
    private synchronized void selectAndInitializeService(String serviceType) {

        // Default to ImageJAndJava2D if not specified or empty
        if (serviceType == null || serviceType.trim().isEmpty()) {
            serviceType = SERVICE_IMAGEJ_JAVA2D;
        }

        logger.info("Selecting image service implementation: {}", serviceType);

        try {
            delegateService = createServiceInstance(serviceType);
            currentServiceType = serviceType;
            logger.info("Image service successfully switched to: {}", serviceType);
            if (!SERVICE_IMAGEMAGICK.equals(serviceType)) {
                DeprecationUtils.onDeprecatedFeatureUsage("Image Service", "8.2.4.0", true,
                        String.format("Configured '%s' service is deprecated and will be removed in a future version. " +
                                "Please update jahia.properties to configure Image magick as image service and restart Jahia. " +
                                "ImageMagick 7+ is required.", serviceType));
            }

        } catch (Exception e) {
            logger.error("Failed to initialize image service {}: {}", serviceType, e.getMessage());

            // Fallback to ImageJAndJava2D if not already trying that
            if (!SERVICE_IMAGEJ_JAVA2D.equals(serviceType)) {
                logger.warn("Falling back to default ImageJAndJava2D service");
                try {
                    delegateService = new ImageJAndJava2DImageServiceImpl();
                    currentServiceType = SERVICE_IMAGEJ_JAVA2D;
                    logger.info("Fallback to ImageJAndJava2D service successful");
                } catch (Exception fallbackEx) {
                    logger.error("Fallback to ImageJAndJava2D also failed: {}", fallbackEx.getMessage());
                    delegateService = null;
                    currentServiceType = null;
                }
            } else {
                delegateService = null;
                currentServiceType = null;
            }
        }
    }

    /**
     * Creates an instance of the specified service implementation.
     *
     * @param serviceType The service type to create
     * @return A new instance of the service implementation
     * @throws IllegalArgumentException if the service type is unknown
     */
    private JahiaImageService createServiceInstance(String serviceType) {
        switch (serviceType) {
            case SERVICE_IMAGEMAGICK:
            case SERVICE_IMAGEMAGICK6:
                return new ImageMagickImageServiceImpl();
            case IMAGE_JIMAGE_SERVICE:
                return new Java2DImageServiceImpl();
            case SERVICE_IMAGEJ_JAVA2D:
                return new ImageJAndJava2DImageServiceImpl();
            default:
                throw new IllegalArgumentException("Unknown image service type: " + serviceType +
                        ". Valid values: " + SERVICE_IMAGEMAGICK + ", " + IMAGE_JIMAGE_SERVICE + ", " + SERVICE_IMAGEJ_JAVA2D);
        }
    }

    /**
     * Gets the current delegate service, throwing an exception if none is available.
     *
     * @return The active delegate service
     * @throws IOException if no service is initialized
     */
    private JahiaImageService getDelegate() throws IOException {
        if (delegateService == null) {
            throw new IOException("No image service is currently initialized. Check configuration and logs.");
        }
        return delegateService;
    }

    // ========================================================================
    // Delegated JahiaImageService Methods
    // ========================================================================

    @Override
    public Image getImage(JCRNodeWrapper node) throws IOException, RepositoryException {
        return getDelegate().getImage(node);
    }

    @Override
    public boolean createThumb(Image iw, File outputFile, int size, boolean square) throws IOException {
        return getDelegate().createThumb(iw, outputFile, size, square);
    }

    @Override
    public boolean cropImage(Image i, File outputFile, int top, int left, int width, int height) throws IOException {
        return getDelegate().cropImage(i, outputFile, top, left, width, height);
    }

    @Override
    public boolean rotateImage(Image i, File outputFile, boolean clockwise) throws IOException {
        return getDelegate().rotateImage(i, outputFile, clockwise);
    }

    @Override
    public boolean rotateImage(Image i, File outputFile, double angle) throws IOException {
        return getDelegate().rotateImage(i, outputFile, angle);
    }

    @Override
    public boolean resizeImage(Image i, File outputFile, int width, int height) throws IOException {
        return getDelegate().resizeImage(i, outputFile, width, height);
    }

    @Override
    public boolean resizeImage(Image i, File outputFile, int width, int height, ResizeType resizeType) throws IOException {
        return getDelegate().resizeImage(i, outputFile, width, height, resizeType);
    }

    @Override
    public BufferedImage resizeImage(BufferedImage image, int width, int height, ResizeType resizeType) throws IOException {
        return getDelegate().resizeImage(image, width, height, resizeType);
    }

    @Override
    public int getHeight(Image i) throws IOException {
        return getDelegate().getHeight(i);
    }

    @Override
    public int getWidth(Image i) throws IOException {
        return getDelegate().getWidth(i);
    }

    @Reference
    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }
}
