package org.jahia.bundles.core.services;

import org.jahia.utils.LimiterExecutor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Map;

import static org.jahia.bundles.core.services.DeprecationTrackerServiceImpl.Config.DEFAULT_LOGGING_INTERVAL;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link DeprecationTrackerServiceImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeprecationTrackerServiceImplTest {

    private DeprecationTrackerServiceImpl deprecationService;
    private DeprecationTrackerServiceImpl.Config config;
    private Map<String, Long> limiterExecutorMap;
    private Field limiterExecutorField;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        deprecationService = new DeprecationTrackerServiceImpl();

        // Create a config with default settings
        config = createConfig(new String[] { "org\\.jahia\\.bin\\.Render\\.xssFilter.*" });

        // Access LimiterExecutor's internal map using reflection for testing
        limiterExecutorField = LimiterExecutor.class.getDeclaredField("lastExecuteOncePerInterval");
        limiterExecutorField.setAccessible(true);
        limiterExecutorMap = (Map<String, Long>) limiterExecutorField.get(null);
        limiterExecutorMap.clear(); // Clear before each test
    }

    @After
    public void tearDown() {
        // Clear the map to ensure test isolation
        if (limiterExecutorMap != null) {
            limiterExecutorMap.clear();
        }

        // Reset field accessibility to maintain proper encapsulation
        if (limiterExecutorField != null) {
            limiterExecutorField.setAccessible(false);
        }
    }

    @Test
    public void GIVEN_excluded_method_WHEN_called_THEN_should_not_be_tracked() {
        // Given - config with custom exclusion pattern
        DeprecationTrackerServiceImpl.Config customConfig = createConfig(new String[] { ".*TestClass\\.excludedMethod.*" });
        deprecationService.activate(customConfig);
        String excludedSignature = "org.example.TestClass.excludedMethod()";
        String excludedKey = DeprecationTrackerServiceImpl.KEY_PREFIX + excludedSignature;

        // When
        deprecationService.onMethodCall(excludedSignature, "", false);

        // Then - should NOT be in the limiter map because it was excluded
        assertFalse("Excluded method should not trigger LimiterExecutor", limiterExecutorMap.containsKey(excludedKey));
    }

    @Test
    public void GIVEN_multiple_exclusion_patterns_WHEN_methods_called_THEN_should_filter_correctly() {
        // Given
        DeprecationTrackerServiceImpl.Config multiPatternConfig = createConfig(new String[] { ".*excludedMethod.*", ".*internalApi.*" });
        deprecationService.activate(multiPatternConfig);

        String excludedSignature = "org.example.Service.excludedMethod()";
        String normalSignature = "org.example.Service.normalMethod()";
        String excludedKey = DeprecationTrackerServiceImpl.KEY_PREFIX + excludedSignature;
        String normalKey = DeprecationTrackerServiceImpl.KEY_PREFIX + normalSignature;

        // When
        deprecationService.onMethodCall(excludedSignature, "", false);
        deprecationService.onMethodCall(normalSignature, "8.3", false);

        // Then
        assertFalse("Excluded method should not be tracked", limiterExecutorMap.containsKey(excludedKey));
        assertTrue("Normal method should be tracked", limiterExecutorMap.containsKey(normalKey));
    }

    @Test
    public void GIVEN_rate_limiting_config_WHEN_method_called_multiple_times_THEN_should_respect_interval() throws InterruptedException {
        // Given - config with 1 second interval
        DeprecationTrackerServiceImpl.Config rateLimitConfig = createConfig(new String[] {}, 1);
        deprecationService.activate(rateLimitConfig);
        String signature = "org.example.Service.deprecatedMethod()";
        String key = DeprecationTrackerServiceImpl.KEY_PREFIX + signature;

        // When - call the method twice quickly
        deprecationService.onMethodCall(signature, "8.3", false);
        Long firstTimestamp = limiterExecutorMap.get(key);

        deprecationService.onMethodCall(signature, "8.3", false);
        Long secondTimestamp = limiterExecutorMap.get(key);

        // Then - timestamps should be the same (rate limited)
        assertEquals("Timestamp should not change within interval", firstTimestamp, secondTimestamp);

        // When - wait for interval to pass and call again
        Thread.sleep(1100); // Wait slightly more than 1 second
        deprecationService.onMethodCall(signature, "8.3", false);
        Long thirdTimestamp = limiterExecutorMap.get(key);

        // Then - timestamp should be updated
        assertTrue("Timestamp should update after interval", thirdTimestamp > secondTimestamp);
    }

    // ==================== Configuration Tests ====================

    @Test
    public void GIVEN_config_update_WHEN_exclusion_pattern_changed_THEN_should_apply_new_pattern() {
        // Given - initial config with no exclusion
        DeprecationTrackerServiceImpl.Config initialConfig = createConfig(new String[] {});
        deprecationService.activate(initialConfig);
        String signature = "org.example.Service.excludedMethod()";
        String key = DeprecationTrackerServiceImpl.KEY_PREFIX + signature;

        // When - method is not excluded initially
        deprecationService.onMethodCall(signature, "", false);
        assertTrue("Method should be tracked with initial config", limiterExecutorMap.containsKey(key));

        // Clear the map for next test
        limiterExecutorMap.clear();

        // When - update config to exclude this method
        DeprecationTrackerServiceImpl.Config newConfig = createConfig(new String[] { ".*excludedMethod.*" });
        deprecationService.modified(newConfig);
        deprecationService.onMethodCall(signature, "", false);

        // Then - should now be excluded
        assertFalse("Method should be excluded from tracking after config update", limiterExecutorMap.containsKey(key));
    }

    @Test(expected = java.util.regex.PatternSyntaxException.class)
    public void GIVEN_invalid_regex_pattern_WHEN_activate_THEN_should_throw_exception() {
        // Given - config with invalid regex pattern
        DeprecationTrackerServiceImpl.Config invalidRegexConfig = createConfig(new String[] { "[invalid(regex" });

        // When - activate with invalid pattern
        deprecationService.activate(invalidRegexConfig);

        // Then - PatternSyntaxException should be thrown
    }

    @Test(expected = java.util.regex.PatternSyntaxException.class)
    public void GIVEN_invalid_regex_pattern_WHEN_modified_THEN_should_throw_exception() {
        // Given
        deprecationService.activate(config);
        DeprecationTrackerServiceImpl.Config invalidConfig = createConfig(new String[] { "[invalid(regex" });

        // When
        deprecationService.modified(invalidConfig);

        // Then - PatternSyntaxException should be thrown
    }

    // ==================== Helper Methods ====================

    /**
     * Helper to verify LimiterExecutor was called for a given method signature.
     */
    private void assertLimiterExecutorHasBeenCalled(String methodSignature) {
        String key = DeprecationTrackerServiceImpl.KEY_PREFIX + methodSignature;
        assertTrue("LimiterExecutor should have been called with " + methodSignature, limiterExecutorMap.containsKey(key));
        assertNotNull("Timestamp should be recorded", limiterExecutorMap.get(key));
    }

    /**
     * Creates a config with the specified excluded method regexes and default logging interval.
     *
     * @param excludedMethodsRegexes the regex patterns to exclude
     * @return a new Config instance
     */
    private static DeprecationTrackerServiceImpl.Config createConfig(String[] excludedMethodsRegexes) {
        return createConfig(excludedMethodsRegexes, DEFAULT_LOGGING_INTERVAL);
    }

    /**
     * Creates a config with the specified excluded method regexes and logging interval.
     *
     * @param excludedMethodsRegexes   the regex patterns to exclude
     * @param loggingIntervalInSeconds the interval between log messages in seconds
     * @return a new Config instance
     */
    private static DeprecationTrackerServiceImpl.Config createConfig(String[] excludedMethodsRegexes, long loggingIntervalInSeconds) {
        return new DeprecationTrackerServiceImpl.Config() {
            @Override
            public Class<? extends java.lang.annotation.Annotation> annotationType() {
                return DeprecationTrackerServiceImpl.Config.class;
            }

            @Override
            public String[] excludedMethodsRegexes() {
                return excludedMethodsRegexes;
            }

            @Override
            public int loggingIntervalInSeconds() {
                return (int) loggingIntervalInSeconds;
            }
        };
    }
}
