package org.jahia.tools.bytecode;

import org.jahia.osgi.BundleUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DeprecationTrackerPlugin}.
 * <p>
 * This test verifies that the ByteBuddy plugin correctly transforms deprecated methods
 * and that the injected bytecode calls the tracker service with the correct metadata.
 * </p>
 * <p><b>Important:</b> Make sure the Maven {@code byte-buddy:transform-test goal} is executed prior to executing this test!</p>
 */
@RunWith(MockitoJUnitRunner.class)
public class DeprecationTrackerPluginTest {

    private DeprecationTrackerService mockService;
    private MockedStatic<BundleUtils> bundleUtilsMock;

    @Before
    public void setUp() {
        // Create a mock service
        mockService = mock(DeprecationTrackerService.class);

        // Mock BundleUtils to return our mock service
        bundleUtilsMock = Mockito.mockStatic(BundleUtils.class);
        bundleUtilsMock.when(() -> BundleUtils.getOsgiService(DeprecationTrackerService.class, null)).thenReturn(mockService);
    }

    @After
    public void tearDown() {
        // Close the static mock
        if (bundleUtilsMock != null) {
            bundleUtilsMock.close();
        }
    }

    @Test
    public void GIVEN_a_simple_deprecated_method_WHEN_called_THEN_should_be_tracked() {
        // When - Call the deprecated method
        // Note: This assumes TestClass has been transformed by the ByteBuddy plugin during compilation
        new TestClass().deprecatedMethod();

        // Then
        assertMethodCalledOnce("org.jahia.tools.bytecode.DeprecationTrackerPluginTest$TestClass.deprecatedMethod()", "8.3", false);
    }

    @Test
    public void GIVEN_a_method_with_parameters_WHEN_called_THEN_should_be_tracked() {
        // When
        new TestClass().deprecatedMethodWithParams("test", 42);

        // Then
        assertMethodCalledOnce(
                "org.jahia.tools.bytecode.DeprecationTrackerPluginTest$TestClass.deprecatedMethodWithParams(java.lang.String, int)", "8.2",
                false);
    }

    @Test
    public void GIVEN_method_marked_for_removal_WHEN_called_THEN_should_be_tracked() {
        // When
        new TestClass().methodMarkedForRemoval();

        // Then
        assertMethodCalledOnce("org.jahia.tools.bytecode.DeprecationTrackerPluginTest$TestClass.methodMarkedForRemoval()", "8.1", true);
    }

    @Test
    public void GIVEN_method_without_metadata_WHEN_called_THEN_should_be_tracked_with_defaults() {
        // When
        new TestClass().methodWithoutMetadata();

        // Then - since should be null when not specified
        assertMethodCalledOnce("org.jahia.tools.bytecode.DeprecationTrackerPluginTest$TestClass.methodWithoutMetadata()", null, false);
    }

    @Test
    public void GIVEN_static_deprecated_method_WHEN_called_THEN_should_be_tracked() {
        // When
        TestClass.staticDeprecatedMethod();

        // Then
        assertMethodCalledOnce("org.jahia.tools.bytecode.DeprecationTrackerPluginTest$TestClass.staticDeprecatedMethod()", "8.3", false);
    }

    @Test
    public void GIVEN_overloaded_methods_WHEN_called_THEN_should_have_unique_signatures() {
        // When - call all three overloaded methods
        TestClass instance = new TestClass();
        instance.overloadedMethod();
        instance.overloadedMethod("test");
        instance.overloadedMethod("test", 42);

        // Then - verify each was tracked with unique signature
        verify(mockService, times(3)).onMethodCall(any(), any(), anyBoolean());

        // Verify they have different signatures
        ArgumentCaptor<String> signatureCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockService, times(3)).onMethodCall(signatureCaptor.capture(), any(), anyBoolean());

        assertEquals(3, signatureCaptor.getAllValues().size());
        assertTrue("Should contain no-arg overload", signatureCaptor.getAllValues()
                .contains("org.jahia.tools.bytecode.DeprecationTrackerPluginTest$TestClass.overloadedMethod()"));
        assertTrue("Should contain single-arg overload", signatureCaptor.getAllValues()
                .contains("org.jahia.tools.bytecode.DeprecationTrackerPluginTest$TestClass.overloadedMethod(java.lang.String)"));
        assertTrue("Should contain two-arg overload", signatureCaptor.getAllValues()
                .contains("org.jahia.tools.bytecode.DeprecationTrackerPluginTest$TestClass.overloadedMethod(java.lang.String, int)"));
    }

    @Test
    public void GIVEN_method_called_multiple_times_WHEN_tracked_THEN_each_call_should_be_tracked() {
        // When - call the same method multiple times
        TestClass instance = new TestClass();
        instance.deprecatedMethod();
        instance.deprecatedMethod();
        instance.deprecatedMethod();

        // Then - should be tracked exactly 3 times with the same parameters each time
        verify(mockService, times(3)).onMethodCall(eq("org.jahia.tools.bytecode.DeprecationTrackerPluginTest$TestClass.deprecatedMethod()"),
                eq("8.3"), eq(false));
    }

    private void assertMethodCalledOnce(String expectedSignature, String expectedSince, boolean expectedForRemoval) {
        // First - Verify the service was called exactly once
        // Note: Use nullable() for since parameter as it can be null when @Deprecated has no 'since' attribute
        verify(mockService, times(1)).onMethodCall(anyString(), nullable(String.class), anyBoolean());

        // Then - Verify the service was called with the correct parameters extracted from @Deprecated annotation
        ArgumentCaptor<String> signatureCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> sinceCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Boolean> forRemovalCaptor = ArgumentCaptor.forClass(Boolean.class);

        verify(mockService).onMethodCall(signatureCaptor.capture(), sinceCaptor.capture(), forRemovalCaptor.capture());

        // Verify captured values match the @Deprecated annotation
        assertEquals("Method signature should match", expectedSignature, signatureCaptor.getValue());
        assertEquals("Since parameter should match annotation", expectedSince, sinceCaptor.getValue());
        assertEquals("forRemoval parameter should match annotation", expectedForRemoval, forRemovalCaptor.getValue());
    }

    /**
     * Test class with deprecated methods for testing ByteBuddy transformation.
     */
    public static class TestClass {

        @Deprecated(since = "8.3")
        public void deprecatedMethod() {
            // Simple deprecated method for testing
        }

        @Deprecated(since = "8.2")
        public void deprecatedMethodWithParams(String param1, int param2) {
            // Deprecated method with parameters for testing
        }

        @Deprecated(since = "8.1", forRemoval = true)
        public void methodMarkedForRemoval() {
            // Method marked for removal in future version
        }

        @Deprecated
        public void methodWithoutMetadata() {
            // Method with @Deprecated but no attributes
        }

        @Deprecated(since = "8.3")
        public static void staticDeprecatedMethod() {
            // Static deprecated method for testing
        }

        @Deprecated(since = "8.2")
        public void overloadedMethod() {
            // First overloaded method - no parameters
        }

        @Deprecated(since = "8.2")
        public void overloadedMethod(String param) {
            // Second overloaded method - one parameter
        }

        @Deprecated(since = "8.2")
        public void overloadedMethod(String param1, int param2) {
            // Third overloaded method - two parameters
        }
    }
}
