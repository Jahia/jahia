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

import org.jahia.api.Constants;
import org.jahia.api.settings.SettingsBean;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.image.Image;
import org.jahia.services.image.JahiaImageService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.imageio.ImageIO;
import javax.jcr.Binary;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Integration test demonstrating the complete workflow of the Image Service architecture.
 * This test verifies the delegator pattern, service switching, and end-to-end image processing.
 */
public class ImageServiceIntegrationTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private ImageServiceDelegator delegator;
    private SettingsBean settingsBean;

    @Before
    public void setUp() {
        delegator = new ImageServiceDelegator();
        settingsBean = mock(SettingsBean.class);
        delegator.setSettingsBean(settingsBean);
    }

    @Test
    public void GIVEN_complete_workflow_WHEN_processing_image_THEN_success() throws Exception {
        // GIVEN: Service starts with default configuration
        when(settingsBean.getPropertyValue("imageService")).thenReturn(null);
        delegator.activate();

        // Verify default service is selected
        assertEquals("ImageJAndJava2DImageService", getCurrentServiceType());

        // Create a test image
        JCRNodeWrapper node = createMockNodeWithImage(400, 300);

        // WHEN: Load and process image
        Image image = delegator.getImage(node);

        // THEN: Image operations work correctly
        assertNotNull(image);
        assertEquals(400, delegator.getWidth(image));
        assertEquals(300, delegator.getHeight(image));

        // Test resize operation
        File resizedFile = tempFolder.newFile("resized.png");
        boolean resizeResult = delegator.resizeImage(image, resizedFile, 200, 150,
            JahiaImageService.ResizeType.SCALE_TO_FILL);
        assertTrue(resizeResult);
        assertTrue(resizedFile.exists());

        // Verify resized dimensions
        BufferedImage resizedImage = ImageIO.read(resizedFile);
        assertEquals(200, resizedImage.getWidth());
        assertEquals(150, resizedImage.getHeight());

        // Test crop operation
        File croppedFile = tempFolder.newFile("cropped.png");
        boolean cropResult = delegator.cropImage(image, croppedFile, 50, 50, 100, 100);
        assertTrue(cropResult);
        assertTrue(croppedFile.exists());

        // Test rotate operation
        File rotatedFile = tempFolder.newFile("rotated.png");
        boolean rotateResult = delegator.rotateImage(image, rotatedFile, true);
        assertTrue(rotateResult);
        assertTrue(rotatedFile.exists());

        // Test thumbnail creation
        File thumbFile = tempFolder.newFile("thumb.png");
        boolean thumbResult = delegator.createThumb(image, thumbFile, 100, true);
        assertTrue(thumbResult);
        assertTrue(thumbFile.exists());

        BufferedImage thumbImage = ImageIO.read(thumbFile);
        assertEquals(100, thumbImage.getWidth());
        assertEquals(100, thumbImage.getHeight());
    }

    @Test
    public void GIVEN_invalid_config_WHEN_activate_THEN_fallback_works_and_operations_succeed() throws Exception {
        // GIVEN: Invalid configuration
        when(settingsBean.getPropertyValue("imageService")).thenReturn("NonExistentService");

        // WHEN: Activate
        delegator.activate();

        // THEN: Falls back to default and works
        assertEquals("ImageJAndJava2DImageService", getCurrentServiceType());

        // Operations work correctly
        JCRNodeWrapper node = createMockNodeWithImage(200, 200);
        Image image = delegator.getImage(node);
        assertNotNull(image);
        assertEquals(200, delegator.getWidth(image));
        assertEquals(200, delegator.getHeight(image));
    }

    @Test
    public void GIVEN_multiple_resize_types_WHEN_resizing_THEN_all_work_correctly() throws Exception {
        // GIVEN:
        when(settingsBean.getPropertyValue("imageService")).thenReturn("Java2DImageService");
        delegator.activate();

        JCRNodeWrapper node = createMockNodeWithImage(400, 300);
        Image image = delegator.getImage(node);

        // WHEN & THEN: Test all resize types
        JahiaImageService.ResizeType[] resizeTypes = {
            JahiaImageService.ResizeType.ADJUST_SIZE,
            JahiaImageService.ResizeType.SCALE_TO_FILL,
            JahiaImageService.ResizeType.ASPECT_FILL,
            JahiaImageService.ResizeType.ASPECT_FIT
        };

        for (JahiaImageService.ResizeType resizeType : resizeTypes) {
            File outputFile = tempFolder.newFile("output_" + resizeType + ".png");
            boolean result = delegator.resizeImage(image, outputFile, 200, 150, resizeType);
            assertTrue("Resize with " + resizeType + " should succeed", result);
            assertTrue(outputFile.exists());
            assertTrue(outputFile.length() > 0);
        }
    }

    @Test
    public void GIVEN_BufferedImage_processing_WHEN_using_Java2D_service_THEN_works_directly() throws Exception {
        // GIVEN:
        when(settingsBean.getPropertyValue("imageService")).thenReturn("Java2DImageService");
        delegator.activate();

        BufferedImage sourceImage = createTestImage(300, 200, Color.BLUE);

        // WHEN:
        BufferedImage resized = delegator.resizeImage(sourceImage, 150, 100,
            JahiaImageService.ResizeType.SCALE_TO_FILL);

        // THEN:
        assertNotNull(resized);
        assertEquals(150, resized.getWidth());
        assertEquals(100, resized.getHeight());
    }

    // Helper methods
    private String getCurrentServiceType() throws Exception {
        Field field = ImageServiceDelegator.class.getDeclaredField("currentServiceType");
        field.setAccessible(true);
        return (String) field.get(delegator);
    }

    private BufferedImage createTestImage(int width, int height, Color color) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(color);
        g.fillRect(0, 0, width, height);
        g.dispose();
        return image;
    }

    private JCRNodeWrapper createMockNodeWithImage(int width, int height) throws Exception {
        BufferedImage testImage = createTestImage(width, height, Color.WHITE);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        String format = "image/png".substring("image/png".indexOf('/') + 1);
        ImageIO.write(testImage, format, baos);
        byte[] imageBytes = baos.toByteArray();

        JCRNodeWrapper node = mock(JCRNodeWrapper.class);
        JCRNodeWrapper contentNode = mock(JCRNodeWrapper.class);
        JCRPropertyWrapper dataProperty = mock(JCRPropertyWrapper.class);
        JCRPropertyWrapper mimeProperty = mock(JCRPropertyWrapper.class);
        Binary binary = mock(Binary.class);

        when(node.getPath()).thenReturn("/test/image." + format);
        when(node.getName()).thenReturn("image." + format);
        when(node.getNode(Constants.JCR_CONTENT)).thenReturn(contentNode);
        when(contentNode.getProperty(Constants.JCR_DATA)).thenReturn(dataProperty);
        when(contentNode.getProperty(Constants.JCR_MIMETYPE)).thenReturn(mimeProperty);
        when(dataProperty.getBinary()).thenReturn(binary);
        when(mimeProperty.getString()).thenReturn("image/png");
        when(binary.getStream()).thenAnswer(invocation -> new ByteArrayInputStream(imageBytes));

        return node;
    }
}
