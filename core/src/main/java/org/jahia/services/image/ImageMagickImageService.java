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
package org.jahia.services.image;

import org.im4java.process.ProcessStarter;

/**
 * An image service implementation that uses the external ImageMagick command line tool to
 * perform high-quality image manipulation for a wide variety of image formats.
 * @deprecated since 8.2.4.0 - Use {@link org.jahia.services.image.JahiaImageService} OSGi service instead (org.jahia.bundles.imageservice)
 */
@Deprecated(since = "8.2.4.0", forRemoval = true)
public class ImageMagickImageService extends AbstractImageService {

    private static final ImageMagickImageService instance = new ImageMagickImageService();

    private String imageMagickPath;

    protected ImageMagickImageService() {
        super();
    }

    public void init() {
        // Not required as done by the OSGi service, keep in case it is required before Karaf is started
        ProcessStarter.setGlobalSearchPath(imageMagickPath);
    }

    public static ImageMagickImageService getInstance() {
        return instance;
    }

    public void setImageMagickPath(String imageMagickPath) {
        this.imageMagickPath = imageMagickPath;
    }

}
