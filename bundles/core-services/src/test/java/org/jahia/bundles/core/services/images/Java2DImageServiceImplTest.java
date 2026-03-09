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

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.jahia.api.Constants;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.image.BufferImage;
import org.jahia.services.image.Image;
import org.jahia.services.image.JahiaImageService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;

import javax.imageio.ImageIO;
import javax.jcr.Binary;
import javax.jcr.RepositoryException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnitParamsRunner.class)
public class Java2DImageServiceImplTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private Java2DImageServiceImpl service;

    @Before
    public void setUp() {
        service = new Java2DImageServiceImpl();
    }

    @Test
    public void GIVEN_invalid_image_data_WHEN_getImage_THEN_IOException() throws Exception {
        // GIVEN:
        JCRNodeWrapper node = mock(JCRNodeWrapper.class);
        JCRNodeWrapper contentNode = mock(JCRNodeWrapper.class);
        JCRPropertyWrapper dataProperty = mock(JCRPropertyWrapper.class);
        JCRPropertyWrapper mimeTypeProperty = mock(JCRPropertyWrapper.class);
        Binary binary = mock(Binary.class);

        when(node.getNode(Constants.JCR_CONTENT)).thenReturn(contentNode);
        when(node.getPath()).thenReturn("/test/image");
        when(contentNode.getProperty(Constants.JCR_DATA)).thenReturn(dataProperty);
        when(contentNode.getProperty(Constants.JCR_MIMETYPE)).thenReturn(mimeTypeProperty);
        when(mimeTypeProperty.getString()).thenReturn("image/png");
        when(dataProperty.getBinary()).thenReturn(binary);
        when(binary.getStream()).thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));

        // WHEN:
        ThrowingRunnable runnable = () -> service.getImage(node);

        // THEN: ImageIO.read returns null for invalid data
        IOException exception = assertThrows(IOException.class, runnable);
        assertTrue(exception.getMessage().contains("Unable to load image"));
    }

    @Test
    public void GIVEN_initialized_service_WHEN_getImage_with_valid_node_THEN_returns_BufferImage() throws Exception {
        // GIVEN:
        JCRNodeWrapper node = createMockNodeWithImage(200, 100, "image/png", "test.png");

        // WHEN:
        Image result = service.getImage(node);

        // THEN:
        assertNotNull(result);
        assertTrue(result instanceof BufferImage);
        assertEquals("/test/image.png", result.getPath());
    }

    @Test
    public void GIVEN_initialized_service_WHEN_getImage_with_invalid_data_THEN_IOException() throws Exception {
        // GIVEN:
        JCRNodeWrapper node = mock(JCRNodeWrapper.class);
        JCRNodeWrapper contentNode = mock(JCRNodeWrapper.class);
        JCRPropertyWrapper dataProperty = mock(JCRPropertyWrapper.class);
        JCRPropertyWrapper mimeProperty = mock(JCRPropertyWrapper.class);
        Binary binary = mock(Binary.class);

        when(node.getNode(Constants.JCR_CONTENT)).thenReturn(contentNode);
        when(node.getPath()).thenReturn("/test/invalid.png");
        when(contentNode.getProperty(Constants.JCR_DATA)).thenReturn(dataProperty);
        when(contentNode.getProperty(Constants.JCR_MIMETYPE)).thenReturn(mimeProperty);
        when(dataProperty.getBinary()).thenReturn(binary);
        when(mimeProperty.getString()).thenReturn("image/png");
        when(binary.getStream()).thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));

        // WHEN:
        ThrowingRunnable runnable = () -> service.getImage(node);

        // THEN:
        IOException exception = assertThrows(IOException.class, runnable);
        assertTrue(exception.getMessage().contains("Unable to load image"));
    }

    @Test
    public void GIVEN_BufferImage_WHEN_getWidth_THEN_returns_correct_width() throws Exception {
        // GIVEN:
        BufferedImage bufferedImage = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);
        BufferImage image = new BufferImage("/test/path", bufferedImage, "image/png");

        // WHEN:
        int width = service.getWidth(image);

        // THEN:
        assertEquals(300, width);
    }

    @Test
    public void GIVEN_BufferImage_WHEN_getHeight_THEN_returns_correct_height() throws Exception {
        // GIVEN:
        BufferedImage bufferedImage = new BufferedImage(300, 200, BufferedImage.TYPE_INT_RGB);
        BufferImage image = new BufferImage("/test/path", bufferedImage, "image/png");

        // WHEN:
        int height = service.getHeight(image);

        // THEN:
        assertEquals(200, height);
    }

    @Test
    public void GIVEN_BufferedImage_WHEN_getWidth_THEN_returns_correct_width() throws Exception {
        // GIVEN:
        BufferedImage bufferedImage = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB);
        BufferImage image = new BufferImage("/test/path", bufferedImage, "image/png");

        // WHEN:
        int width = service.getWidth(image);

        // THEN:
        assertEquals(400, width);
    }

    @Test
    public void GIVEN_unsupported_image_type_WHEN_getWidth_THEN_IOException() throws Exception {
        // GIVEN:
        Image unsupportedImage = mock(Image.class);

        // WHEN:
        ThrowingRunnable runnable = () -> service.getWidth(unsupportedImage);

        // THEN:
        IOException exception = assertThrows(IOException.class, runnable);
        assertTrue(exception.getMessage().contains("Unsupported image type for Java2D service"));
    }

    @Test
    public void GIVEN_unsupported_image_type_WHEN_cropImage_THEN_IOException() throws Exception {
        // GIVEN:
        Image unsupportedImage = mock(Image.class);
        File outputFile = tempFolder.newFile();

        // WHEN:
        ThrowingRunnable runnable = () -> service.cropImage(unsupportedImage, outputFile, 0, 0, 100, 100);

        // THEN:
        IOException exception = assertThrows(IOException.class, runnable);
        assertTrue(exception.getMessage().contains("Unsupported image type for Java2D service"));
    }

    @Test
    public void GIVEN_BufferImage_WHEN_cropImage_THEN_creates_cropped_file() throws Exception {
        // GIVEN:
        BufferedImage bufferedImage = createTestImage(400, 300, Color.BLUE);
        BufferImage image = new BufferImage("/test/path", bufferedImage, "image/png");
        File outputFile = tempFolder.newFile("cropped.png");

        // WHEN:
        boolean result = service.cropImage(image, outputFile, 50, 50, 100, 100);

        // THEN:
        assertTrue(result);
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        BufferedImage croppedImage = ImageIO.read(outputFile);
        assertNotNull(croppedImage);
        assertEquals(100, croppedImage.getWidth());
        assertEquals(100, croppedImage.getHeight());
    }

    @Test
    public void GIVEN_BufferImage_WHEN_rotateImage_THEN_creates_rotated_file() throws Exception {
        // GIVEN:
        BufferedImage bufferedImage = createTestImage(200, 100, Color.RED);
        BufferImage image = new BufferImage("/test/path", bufferedImage, "image/png");
        File outputFile = tempFolder.newFile("rotated.png");

        // WHEN:
        boolean result = service.rotateImage(image, outputFile, 90.0);

        // THEN:
        assertTrue(result);
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        BufferedImage rotatedImage = ImageIO.read(outputFile);
        assertNotNull(rotatedImage);
    }

    @Test
    public void GIVEN_unsupported_image_type_WHEN_rotateImage_THEN_IOException() throws Exception {
        // GIVEN:
        Image unsupportedImage = mock(Image.class);
        File outputFile = tempFolder.newFile();

        // WHEN:
        ThrowingRunnable runnable = () -> service.rotateImage(unsupportedImage, outputFile, 90.0);

        // THEN:
        IOException exception = assertThrows(IOException.class, runnable);
        assertTrue(exception.getMessage().contains("Unsupported image type for Java2D service"));
    }

    @Test
    @Parameters({
            "ADJUST_SIZE",
            "SCALE_TO_FILL",
            "ASPECT_FILL",
            "ASPECT_FIT"
    })
    public void GIVEN_BufferImage_WHEN_resizeImage_with_ResizeType_THEN_creates_resized_file(String resizeTypeStr) throws Exception {
        // GIVEN:
        BufferedImage bufferedImage = createTestImage(400, 300, Color.GREEN);
        BufferImage image = new BufferImage("/test/path", bufferedImage, "image/png");
        File outputFile = tempFolder.newFile("resized_" + resizeTypeStr + ".png");
        JahiaImageService.ResizeType resizeType = JahiaImageService.ResizeType.valueOf(resizeTypeStr);

        // WHEN:
        boolean result = service.resizeImage(image, outputFile, 100, 100, resizeType);

        // THEN:
        assertTrue(result);
        assertTrue(outputFile.exists());
        assertTrue(outputFile.length() > 0);

        BufferedImage resizedImage = ImageIO.read(outputFile);
        assertNotNull(resizedImage);
    }

    @Test
    public void GIVEN_BufferedImage_WHEN_resizeImage_BufferedImage_THEN_returns_resized_BufferedImage() throws Exception {
        // GIVEN:
        BufferedImage originalImage = createTestImage(400, 300, Color.YELLOW);

        // WHEN:
        BufferedImage result = service.resizeImage(originalImage, 200, 150, JahiaImageService.ResizeType.SCALE_TO_FILL);

        // THEN:
        assertNotNull(result);
        assertEquals(200, result.getWidth());
        assertEquals(150, result.getHeight());
    }

    @Test
    public void GIVEN_unsupported_image_type_WHEN_resizeImage_THEN_IOException() throws Exception {
        // GIVEN:
        Image unsupportedImage = mock(Image.class);
        File outputFile = tempFolder.newFile();

        // WHEN:
        ThrowingRunnable runnable = () -> service.resizeImage(unsupportedImage, outputFile, 100, 100, JahiaImageService.ResizeType.ADJUST_SIZE);

        // THEN:
        IOException exception = assertThrows(IOException.class, runnable);
        assertTrue(exception.getMessage().contains("Unsupported image type for Java2D service"));
    }

    @Test
    public void GIVEN_BufferImage_WHEN_createThumb_square_THEN_creates_square_thumbnail() throws Exception {
        // GIVEN:
        BufferedImage bufferedImage = createTestImage(400, 300, Color.CYAN);
        BufferImage image = new BufferImage("/test/path", bufferedImage, "image/png");
        File outputFile = tempFolder.newFile("thumb_square.png");

        // WHEN:
        boolean result = service.createThumb(image, outputFile, 100, true);

        // THEN:
        assertTrue(result);
        assertTrue(outputFile.exists());

        BufferedImage thumbImage = ImageIO.read(outputFile);
        assertNotNull(thumbImage);
        assertEquals(100, thumbImage.getWidth());
        assertEquals(100, thumbImage.getHeight());
    }

    @Test
    public void GIVEN_BufferImage_WHEN_createThumb_not_square_THEN_creates_proportional_thumbnail() throws Exception {
        // GIVEN:
        BufferedImage bufferedImage = createTestImage(400, 300, Color.MAGENTA);
        BufferImage image = new BufferImage("/test/path", bufferedImage, "image/png");
        File outputFile = tempFolder.newFile("thumb_proportional.png");

        // WHEN:
        boolean result = service.createThumb(image, outputFile, 100, false);

        // THEN:
        assertTrue(result);
        assertTrue(outputFile.exists());

        BufferedImage thumbImage = ImageIO.read(outputFile);
        assertNotNull(thumbImage);
        assertTrue(thumbImage.getWidth() <= 100);
        assertTrue(thumbImage.getHeight() <= 100);
    }

    // Helper methods
    private BufferedImage createTestImage(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return image;
    }

    private JCRNodeWrapper createMockNodeWithImage(int width, int height, String mimeType, String fileName) throws RepositoryException, IOException {
        BufferedImage testImage = createTestImage(width, height, Color.WHITE);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(testImage, "png", baos);
        byte[] imageBytes = baos.toByteArray();

        JCRNodeWrapper node = mock(JCRNodeWrapper.class);
        JCRNodeWrapper contentNode = mock(JCRNodeWrapper.class);
        JCRPropertyWrapper dataProperty = mock(JCRPropertyWrapper.class);
        JCRPropertyWrapper mimeProperty = mock(JCRPropertyWrapper.class);
        Binary binary = mock(Binary.class);

        when(node.getPath()).thenReturn("/test/image.png");
        when(node.getName()).thenReturn(fileName);
        when(node.getNode(Constants.JCR_CONTENT)).thenReturn(contentNode);
        when(contentNode.getProperty(Constants.JCR_DATA)).thenReturn(dataProperty);
        when(contentNode.getProperty(Constants.JCR_MIMETYPE)).thenReturn(mimeProperty);
        when(dataProperty.getBinary()).thenReturn(binary);
        when(mimeProperty.getString()).thenReturn(mimeType);
        when(binary.getStream()).thenReturn(new ByteArrayInputStream(imageBytes));

        return node;
    }
}
