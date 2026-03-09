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
import org.jahia.api.settings.SettingsBean;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.image.Image;
import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(JUnitParamsRunner.class)
public class ImageServiceDelegatorTest {

    private ImageServiceDelegator delegator;
    private SettingsBean settingsBean;

    @Before
    public void setUp() {
        delegator = new ImageServiceDelegator();
        settingsBean = mock(SettingsBean.class);
        delegator.setSettingsBean(settingsBean);
    }

    @Test
    public void GIVEN_default_config_WHEN_activate_THEN_ImageJAndJava2DService_selected() throws Exception {
        // GIVEN:
        when(settingsBean.getPropertyValue("imageService")).thenReturn(null);

        // WHEN:
        delegator.activate();

        // THEN:
        String currentServiceType = getDelegatorStringField();
        assertEquals("ImageJAndJava2DImageService", currentServiceType);
        assertNotNull(getDelegatorField("delegateService"));
    }

    @Test
    @Parameters({
            "ImageJAndJava2DImageService",
            "Java2DImageService"
    })
    public void GIVEN_valid_config_WHEN_activate_THEN_service_initialized(String serviceType) throws Exception {
        // GIVEN:
        when(settingsBean.getPropertyValue("imageService")).thenReturn(serviceType);

        // WHEN:
        delegator.activate();

        // THEN:
        String currentServiceType = getDelegatorStringField();
        assertEquals(serviceType, currentServiceType);
        assertNotNull(getDelegatorField("delegateService"));
    }

    @Test
    public void GIVEN_empty_config_WHEN_activate_THEN_default_service_selected() throws Exception {
        // GIVEN:
        when(settingsBean.getPropertyValue("imageService")).thenReturn("");

        // WHEN:
        delegator.activate();

        // THEN:
        String currentServiceType = getDelegatorStringField();
        assertEquals("ImageJAndJava2DImageService", currentServiceType);
    }

    @Test
    public void GIVEN_invalid_config_WHEN_activate_THEN_fallback_to_default() throws Exception {
        // GIVEN:
        when(settingsBean.getPropertyValue("imageService")).thenReturn("InvalidService");

        // WHEN:
        delegator.activate();

        // THEN: should fallback to ImageJAndJava2DImageService
        String currentServiceType = getDelegatorStringField();
        assertEquals("ImageJAndJava2DImageService", currentServiceType);
    }

    @Test
    public void GIVEN_service_not_initialized_WHEN_calling_getImage_THEN_IOException() throws Exception {
        // GIVEN:
        JCRNodeWrapper node = mock(JCRNodeWrapper.class);
        setDelegatorField();

        // WHEN:
        ThrowingRunnable runnable = () -> delegator.getImage(node);

        // THEN:
        IOException exception = assertThrows(IOException.class, runnable);
        assertTrue(exception.getMessage().contains("No image service is currently initialized"));
    }

    @Test
    public void GIVEN_service_initialized_WHEN_deactivate_THEN_service_cleared() throws Exception {
        // GIVEN:
        when(settingsBean.getPropertyValue("imageService")).thenReturn("Java2DImageService");
        delegator.activate();
        assertNotNull(getDelegatorField("delegateService"));

        // WHEN:
        delegator.deactivate();

        // THEN:
        assertNull(getDelegatorField("delegateService"));
        assertNull(getDelegatorField("currentServiceType"));
    }

    @Test
    public void GIVEN_service_not_initialized_WHEN_calling_createThumb_THEN_IOException() throws Exception {
        // GIVEN:
        Image image = mock(Image.class);
        File outputFile = mock(File.class);
        setDelegatorField();

        // WHEN:
        ThrowingRunnable runnable = () -> delegator.createThumb(image, outputFile, 100, false);

        // THEN:
        IOException exception = assertThrows(IOException.class, runnable);
        assertTrue(exception.getMessage().contains("No image service is currently initialized"));
    }

    @Test
    public void GIVEN_service_not_initialized_WHEN_calling_cropImage_THEN_IOException() throws Exception {
        // GIVEN:
        Image image = mock(Image.class);
        File outputFile = mock(File.class);
        setDelegatorField();

        // WHEN:
        ThrowingRunnable runnable = () -> delegator.cropImage(image, outputFile, 0, 0, 100, 100);

        // THEN:
        IOException exception = assertThrows(IOException.class, runnable);
        assertTrue(exception.getMessage().contains("No image service is currently initialized"));
    }

    @Test
    public void GIVEN_service_not_initialized_WHEN_calling_rotateImage_THEN_IOException() throws Exception {
        // GIVEN:
        Image image = mock(Image.class);
        File outputFile = mock(File.class);
        setDelegatorField();

        // WHEN:
        ThrowingRunnable runnable = () -> delegator.rotateImage(image, outputFile, true);

        // THEN:
        IOException exception = assertThrows(IOException.class, runnable);
        assertTrue(exception.getMessage().contains("No image service is currently initialized"));
    }

    @Test
    public void GIVEN_service_not_initialized_WHEN_calling_resizeImage_THEN_IOException() throws Exception {
        // GIVEN:
        Image image = mock(Image.class);
        File outputFile = mock(File.class);
        setDelegatorField();

        // WHEN:
        ThrowingRunnable runnable = () -> delegator.resizeImage(image, outputFile, 100, 100);

        // THEN:
        IOException exception = assertThrows(IOException.class, runnable);
        assertTrue(exception.getMessage().contains("No image service is currently initialized"));
    }

    @Test
    public void GIVEN_service_not_initialized_WHEN_calling_getWidth_THEN_IOException() throws Exception {
        // GIVEN:
        Image image = mock(Image.class);
        setDelegatorField();

        // WHEN:
        ThrowingRunnable runnable = () -> delegator.getWidth(image);

        // THEN:
        IOException exception = assertThrows(IOException.class, runnable);
        assertTrue(exception.getMessage().contains("No image service is currently initialized"));
    }

    @Test
    public void GIVEN_service_not_initialized_WHEN_calling_getHeight_THEN_IOException() throws Exception {
        // GIVEN:
        Image image = mock(Image.class);
        setDelegatorField();

        // WHEN:
        ThrowingRunnable runnable = () -> delegator.getHeight(image);

        // THEN:
        IOException exception = assertThrows(IOException.class, runnable);
        assertTrue(exception.getMessage().contains("No image service is currently initialized"));
    }

    // Helper methods to access private fields via reflection for testing
    private Object getDelegatorField(String fieldName) throws Exception {
        Field field = ImageServiceDelegator.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(delegator);
    }

    private String getDelegatorStringField() throws Exception {
        Object value = getDelegatorField("currentServiceType");
        return value != null ? value.toString() : null;
    }

    private void setDelegatorField() throws Exception {
        Field field = ImageServiceDelegator.class.getDeclaredField("delegateService");
        field.setAccessible(true);
        field.set(delegator, null);
    }
}
