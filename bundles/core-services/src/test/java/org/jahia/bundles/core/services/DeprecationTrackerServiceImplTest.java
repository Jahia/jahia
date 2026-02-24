package org.jahia.bundles.core.services;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.jahia.api.settings.SettingsBean;
import org.jahia.utils.LimiterExecutor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.lang.reflect.Field;
import java.util.Map;

import static org.jahia.bundles.core.services.DeprecationTrackerServiceImpl.Config.DEFAULT_LOGGING_INTERVAL;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link DeprecationTrackerServiceImpl}.
 */
@RunWith(JUnitParamsRunner.class)
public class DeprecationTrackerServiceImplTest {

    private DeprecationTrackerServiceImpl deprecationService;
    private DeprecationTrackerServiceImpl.Config config;
    private Map<String, Long> limiterExecutorMap;
    private Field limiterExecutorField;

    @Mock
    private SettingsBean settingsBean;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        deprecationService = new DeprecationTrackerServiceImpl();
        deprecationService.setSettingsBean(settingsBean);

        // Create a config with default settings
        config = configBuilder().build();

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
        DeprecationTrackerServiceImpl.Config customConfig = configBuilder().excludedMethodsRegexes(".*TestClass\\.excludedMethod.*")
                .build();
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
        DeprecationTrackerServiceImpl.Config multiPatternConfig = configBuilder().excludedMethodsRegexes(".*excludedMethod.*",
                ".*internalApi.*").build();
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
        DeprecationTrackerServiceImpl.Config rateLimitConfig = configBuilder().loggingIntervalInSeconds(1).build();
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
        DeprecationTrackerServiceImpl.Config initialConfig = configBuilder().excludedMethodsRegexes().build();
        deprecationService.activate(initialConfig);
        String signature = "org.example.Service.excludedMethod()";
        String key = DeprecationTrackerServiceImpl.KEY_PREFIX + signature;

        // When - method is not excluded initially
        deprecationService.onMethodCall(signature, "", false);
        assertTrue("Method should be tracked with initial config", limiterExecutorMap.containsKey(key));

        // Clear the map for next test
        limiterExecutorMap.clear();

        // When - update config to exclude this method
        DeprecationTrackerServiceImpl.Config newConfig = configBuilder().excludedMethodsRegexes(".*excludedMethod.*").build();
        deprecationService.modified(newConfig);
        deprecationService.onMethodCall(signature, "", false);

        // Then - should now be excluded
        assertFalse("Method should be excluded from tracking after config update", limiterExecutorMap.containsKey(key));
    }

    @Test(expected = java.util.regex.PatternSyntaxException.class)
    public void GIVEN_invalid_regex_pattern_WHEN_activate_THEN_should_throw_exception() {
        // Given - config with invalid regex pattern
        DeprecationTrackerServiceImpl.Config invalidRegexConfig = configBuilder().excludedMethodsRegexes("[invalid(regex").build();

        // When - activate with invalid pattern
        deprecationService.activate(invalidRegexConfig);

        // Then - PatternSyntaxException should be thrown
    }

    @Test(expected = java.util.regex.PatternSyntaxException.class)
    public void GIVEN_invalid_regex_pattern_WHEN_modified_THEN_should_throw_exception() {
        // Given
        deprecationService.activate(config);
        DeprecationTrackerServiceImpl.Config invalidConfig = configBuilder().excludedMethodsRegexes("[invalid(regex").build();

        // When
        deprecationService.modified(invalidConfig);

        // Then - PatternSyntaxException should be thrown
    }

    // ==================== Development Mode Tests ====================

    @Test
    @Parameters({ "true", "false" })
    public void GIVEN_an_environment_in_development_mode_WHEN_developmentModeOnly_is_configured_THEN_should_always_track(
            boolean developmentModeOnly) {
        // Given
        DeprecationTrackerServiceImpl.Config devModeConfig = configBuilder().developmentModeOnly(developmentModeOnly).build();
        deprecationService.activate(devModeConfig);

        // Mock: system is in development mode
        when(settingsBean.isDevelopmentMode()).thenReturn(true);

        String signature = "org.example.Service.deprecatedMethod()";
        String key = DeprecationTrackerServiceImpl.KEY_PREFIX + signature;

        // When
        deprecationService.onMethodCall(signature, "8.3", false);

        // Then - method should be tracked
        assertTrue("Method should be tracked when in development mode", limiterExecutorMap.containsKey(key));
    }

    @Test
    public void GIVEN_developmentModeOnly_enabled_WHEN_not_in_development_mode_THEN_should_not_track() {
        // Given - config with developmentModeOnly enabled
        DeprecationTrackerServiceImpl.Config devModeConfig = configBuilder().developmentModeOnly(true).build();
        deprecationService.activate(devModeConfig);

        // Mock: system is NOT in development mode (production)
        when(settingsBean.isDevelopmentMode()).thenReturn(false);

        String signature = "org.example.Service.deprecatedMethod()";
        String key = DeprecationTrackerServiceImpl.KEY_PREFIX + signature;

        // When
        deprecationService.onMethodCall(signature, "8.3", false);

        // Then - method should NOT be tracked
        assertFalse("Method should not be tracked when not in development mode", limiterExecutorMap.containsKey(key));
    }

    @Test
    public void GIVEN_developmentModeOnly_disabled_WHEN_not_in_development_mode_THEN_should_track() {
        // Given - config with developmentModeOnly disabled (default)
        DeprecationTrackerServiceImpl.Config devModeConfig = configBuilder().developmentModeOnly(false).build();
        deprecationService.activate(devModeConfig);

        // Note: No need to mock settingsBean.isDevelopmentMode() when developmentModeOnly is false,
        // because the condition short-circuits and never checks the development mode

        String signature = "org.example.Service.deprecatedMethod()";
        String key = DeprecationTrackerServiceImpl.KEY_PREFIX + signature;

        // When
        deprecationService.onMethodCall(signature, "8.3", false);

        // Then - method should be tracked (works in all modes)
        assertTrue("Method should be tracked regardless of mode when developmentModeOnly is false", limiterExecutorMap.containsKey(key));
    }

    @Test
    public void GIVEN_developmentModeOnly_enabled_and_excluded_pattern_WHEN_method_called_THEN_exclusion_takes_precedence() {
        // Given - config with both developmentModeOnly and exclusion pattern
        DeprecationTrackerServiceImpl.Config combinedConfig = configBuilder().developmentModeOnly(true)
                .excludedMethodsRegexes(".*excludedMethod.*").build();
        deprecationService.activate(combinedConfig);

        // Mock: system is in development mode
        when(settingsBean.isDevelopmentMode()).thenReturn(true);

        String excludedSignature = "org.example.Service.excludedMethod()";
        String normalSignature = "org.example.Service.normalMethod()";
        String excludedKey = DeprecationTrackerServiceImpl.KEY_PREFIX + excludedSignature;
        String normalKey = DeprecationTrackerServiceImpl.KEY_PREFIX + normalSignature;

        // When
        deprecationService.onMethodCall(excludedSignature, "8.3", false);
        deprecationService.onMethodCall(normalSignature, "8.3", false);

        // Then
        assertFalse("Excluded method should not be tracked even in development mode", limiterExecutorMap.containsKey(excludedKey));
        assertTrue("Normal method should be tracked in development mode", limiterExecutorMap.containsKey(normalKey));
    }

    // ==================== Helper Methods ====================

    /**
     * Creates a config builder for flexible configuration creation.
     *
     * @return a new ConfigBuilder instance with default values
     */
    private static ConfigBuilder configBuilder() {
        return new ConfigBuilder();
    }

    /**
     * Builder pattern for creating Config instances with optional parameters.
     */
    private static class ConfigBuilder {
        private String[] excludedMethodsRegexes = new String[] { "org\\.jahia\\.bin\\.Render\\.xssFilter.*" };
        private int loggingIntervalInSeconds = DEFAULT_LOGGING_INTERVAL;
        private boolean developmentModeOnly = false;

        public ConfigBuilder excludedMethodsRegexes(String... regexes) {
            this.excludedMethodsRegexes = regexes;
            return this;
        }

        public ConfigBuilder loggingIntervalInSeconds(int seconds) {
            this.loggingIntervalInSeconds = seconds;
            return this;
        }

        public ConfigBuilder developmentModeOnly(boolean developmentModeOnly) {
            this.developmentModeOnly = developmentModeOnly;
            return this;
        }

        public DeprecationTrackerServiceImpl.Config build() {
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
                public boolean developmentModeOnly() {
                    return developmentModeOnly;
                }

                @Override
                public int loggingIntervalInSeconds() {
                    return loggingIntervalInSeconds;
                }
            };
        }
    }
}
