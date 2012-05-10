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

package org.jahia.services.image;

import org.jahia.services.content.JCRNodeWrapper;

import javax.jcr.RepositoryException;
import java.io.File;
import java.io.IOException;

/**
 * The main interface for an image generation service implementation. This interface
 * provides access to various image manipulation operations such as image resizing,
 * cropping and rotating.
 */
public interface JahiaImageService {

    /**
     * The resize type allows to control how the original image will be fitted
     * in the destination size
     */
    public enum ResizeType {
        ADJUST_SIZE, SCALE_TO_FILL, ASPECT_FILL, ASPECT_FIT;
    }

    /**
     * Retrieve the image stored in a JCR node
     * @param node the JCR node that contains the image data.
     * @return an Image object that usually contains metadata for the other service
     * methods to be able to manipulate the image.
     * @throws IOException
     * @throws RepositoryException
     */
    public Image getImage(JCRNodeWrapper node) throws IOException, RepositoryException;

    /**
     * Creates an image thumbnail and stores it in a specified file
     * @param iw the image for which to generate a thumbnail, this image is loaded through the
     * getImage method
     * @param outputFile the file in which to store the generated thumbnail
     * @param size the size in pixels of the generated thumbnail. Usually this will mean the desired
     * width or height of the image, except if the square option is specified.
     * @param square if false, a ResizeType.ADJUST_SIZE resize will be performed, otherwise if
     * square is true, a ResizeType.ASPECT_FILL will be performed. For more information about these
     * resize types, see the resize method.
     * @return true if the operation succeeded, false otherwise
     * @throws IOException
     */
    public boolean createThumb(Image iw, File outputFile, int size, boolean square) throws IOException;

    /**
     * Retrieves the height in pixels of the specified image
     * @param i the image for which to retrieve the height
     * @return the height of the image in pixels
     * @throws IOException
     */
    public int getHeight(Image i) throws IOException;

    /**
     * Retrieves the width in pixels of the specified image
     * @param i the image
     * @return the width in pixels of the image
     * @throws IOException
     */
    public int getWidth(Image i) throws IOException;

    /**
     * Crops an image to a specified file using the specified coordinates
     * Note : due to a bug, the X and Y are reversed, be careful about that !
     * @param i the image to crop
     * @param outputFile the destination file in which to store the cropped image. The file type
     * of the original image is conserved
     * @param top the top Y coordinate at which to start the crop
     * @param left the left X coordinate at which to start the crop
     * @param width the width of the crop region
     * @param height the height of the crop region.
     * @return true if the cropping succeeded, false otherwise
     * @throws IOException
     */
    public boolean cropImage(Image i, File outputFile, int top, int left, int width, int height) throws IOException;

    /**
     * Resize an image using an ADJUST_SIZE resize type. See the other resizeImage method for more
     * details on the resize types
     * @param i the image to resize
     * @param outputFile the output file where to output the resized image
     * @param width the desired image width
     * @param height the desired image height
     * @return true if the resize succeeded, false otherwise
     * @throws IOException
     */
    public boolean resizeImage(Image i, File outputFile, int width, int height) throws IOException;

    /**
     * Rotate an image clockwise or counter clockwise
     * @param i the image to rotate
     * @param outputFile the file in which to store the rotated image, the original image type
     * is conserved
     * @param clockwise if the true, the image is rotated clockwise, otherwise it is rotated
     * counter clockwise
     * @return true if the resize succeeded, false otherwise
     * @throws IOException
     */
    public boolean rotateImage(Image i, File outputFile, boolean clockwise) throws IOException;

    /**
     * Resize an image using different types of resize algorithms. Here is a detailed explanation of
     * the different types of resize available :
     * - ResizeType.SCALE_TO_FILL : this is the simplest algorithm. It will simply ignore the original
     * image ratio and scale the image both horizontally and vertically to match the specified width
     * and height. This will usually result in a deformed image and is usually not recommended.
     * - ResizeType.ADJUST_SIZE : in this algorithm the width and height are more considered as
     * desired sizes, but they will be modified to fit the aspect ratio of the original image. Note that
     * the width and height will never be exceeded, so the resulting image will always be smaller than
     * the specified one.
     * - ResizeType.ASPECT_FILL : in this case the resulting image will have the exact specified size,
     * and the image will be resized and cropped (using the center gravity crop) to fill the desired
     * dimension. This will result in some of the image to be removed, but is usually acceptable for
     * profile or icons.
     * - ResizeType.ASPECT_FIT : in this last algorithm the desired image will have the exact specified
     * size and the original image ratio is conserved, but borders will be added to the image to make
     * it match the desired size.
     * @param i the image to resize
     * @param outputFile the file in which to output the resized image
     * @param width the width of the resized image
     * @param height the height of the resized image
     * @param resizeType the type of resize algorithm to use. Uses a ResizeType enum also available
     * in this interface
     * @return true if the resize succeeded, false otherwise.
     * @throws IOException
     */
    public boolean resizeImage(Image i, File outputFile, int width, int height, ResizeType resizeType) throws IOException;

}
