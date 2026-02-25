/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2026 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.bundles.core.services;

import org.codehaus.plexus.util.StringUtils;
import org.jahia.api.settings.SettingsBean;
import org.jahia.tools.bytecode.DeprecationTrackerService;
import org.jahia.utils.LimiterExecutor;
import org.osgi.service.component.annotations.*;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Implementation of the {@link DeprecationTrackerService} that instruments and logs deprecated method calls.
 * <p>
 * This service intercepts calls to deprecated methods and logs them with configurable intervals to avoid
 * log pollution. It supports excluding specific methods via regex patterns to filter out methods to exclude from logging.
 * </p>
 * <p>
 * The service is configured via OSGi configuration with PID: {@code org.jahia.bundles.core.services.deprecation}
 * </p>
 *
 */
@Component(service = DeprecationTrackerService.class, immediate = true, configurationPid = "org.jahia.bundles.core.services.deprecation", configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = DeprecationTrackerServiceImpl.Config.class)
public class DeprecationTrackerServiceImpl implements DeprecationTrackerService {
    private static final Logger logger = LoggerFactory.getLogger(DeprecationTrackerServiceImpl.class);
    static final String KEY_PREFIX = DeprecationTrackerServiceImpl.class.getName() + ".";

    private Config config;
    private transient Pattern excludedMethodsPattern;
    private SettingsBean settingsBean;

    @Activate
    public void activate(Config config) {
        applyConfiguration(config);
        logger.info("Deprecation Service activated");
    }

    @Modified
    public void modified(Config config) {
        applyConfiguration(config);
        logger.info("Deprecation Service configuration modified");
    }

    @Reference
    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }

    private void applyConfiguration(Config config) {
        Pattern compiledExcludedMethodsPattern = compileExcludedMethodsPattern(config);
        if (config.loggingIntervalInSeconds() <= 0) {
            throw new IllegalArgumentException("Invalid logging interval, must be positive: " + config.loggingIntervalInSeconds());
        }
        // once validated, update the local fields
        this.config = config;
        this.excludedMethodsPattern = compiledExcludedMethodsPattern;
    }

    /**
     * Compiles the excluded methods regex patterns from configuration into a single {@link Pattern}.
     * <p>
     * Multiple regex patterns are joined with the OR operator (|) to create a single compiled pattern
     * for efficient matching. If no patterns are provided, a pattern that never matches is returned.
     * </p>
     *
     * @param config the service configuration containing the regex patterns
     * @return a compiled {@link Pattern} that matches any of the configured exclusion patterns
     */
    private static Pattern compileExcludedMethodsPattern(Config config) {
        String[] patterns = config.excludedMethodsRegexes();
        if (patterns == null || patterns.length == 0) {
            // Return a pattern that never matches anything
            return Pattern.compile("(?!)");
        }
        return Pattern.compile(String.join("|", patterns));
    }

    /**
     * Tracks a deprecated method call by logging it with rate limiting.
     * <p>
     * This method is called whenever a deprecated method is invoked. It performs the following:
     * <ul>
     *     <li>Receives pre-computed method signature and deprecation metadata from build time</li>
     *     <li>Checks if the method matches any exclusion patterns and skips logging if excluded</li>
     *     <li>Logs the deprecated method call at WARN level (rate-limited by configured interval)</li>
     *     <li>Logs additional debug information including deprecation metadata and stack trace</li>
     * </ul>
     * </p>
     *
     * @param methodSignature the fully qualified signature of the deprecated method
     * @param deprecatedSince the value of the 'since' attribute from the @Deprecated annotation
     * @param forRemoval      true if the deprecated element is marked for removal
     */
    @Override
    public void onMethodCall(String methodSignature, String deprecatedSince, boolean forRemoval) {
        // Check if the method should be excluded
        if (isExcluded(methodSignature)) {
            logger.debug("Skipping excluded deprecated method: {}", methodSignature);
            return;
        }

        // check the operating mode
        if (config.developmentModeOnly() && !settingsBean.isDevelopmentMode()) {
            logger.debug("Skipping deprecated method: {} (development mode only)", methodSignature);
            return;
        }

        // log only once per interval
        long intervalInMs = config.loggingIntervalInSeconds() * 1000L;
        String key = KEY_PREFIX + methodSignature;
        LimiterExecutor.executeOncePerInterval(key, intervalInMs,
                () -> logDeprecatedMethodCall(methodSignature, deprecatedSince, forRemoval));
    }

    private boolean isExcluded(String methodSignature) {
        return excludedMethodsPattern.matcher(methodSignature).matches();
    }

    private static void logDeprecatedMethodCall(String methodSignature, String deprecatedSince, boolean forRemoval) {
        // Build warning message based on available metadata
        StringBuilder warnMsg = new StringBuilder("Deprecated method called: ");
        warnMsg.append(methodSignature);
        if (StringUtils.isNotBlank(deprecatedSince)) {
            warnMsg.append(" (deprecated since ").append(deprecatedSince).append(")");
        }
        if (forRemoval) {
            warnMsg.append(" [MARKED FOR REMOVAL]");
        }
        warnMsg.append(". Enable debugging to get the full stacktrace.");
        logger.warn(warnMsg.toString());
        logger.debug("Stack trace:", new Throwable()); // Log the stack trace
    }

    @Override
    public void onFeatureUsage(String featureName, String deprecatedSince, boolean markedForRemoval, String details) {

        // log only once per interval
        long intervalInMs = config.loggingIntervalInSeconds() * 1000L;
        String key = KEY_PREFIX + featureName;
        LimiterExecutor.executeOncePerInterval(key, intervalInMs,
                () -> logDeprecatedFeatureUsage(featureName, deprecatedSince, markedForRemoval, details));

    }

    private static void logDeprecatedFeatureUsage(String featureName, String deprecatedSince, boolean forRemoval, String details) {
        // Build warning message based on available metadata
        StringBuilder warnMsg = new StringBuilder("Deprecated feature used: ");
        warnMsg.append(featureName);
        if (StringUtils.isNotBlank(deprecatedSince)) {
            warnMsg.append(" (deprecated since ").append(deprecatedSince).append(")");
        }
        if (forRemoval) {
            warnMsg.append(" [MARKED FOR REMOVAL]");
        }
        warnMsg.append(".");
        logger.warn(warnMsg.toString());
        if (StringUtils.isNotBlank(details)) {
            logger.warn("Details: {}", details);
        }
    }

    @ObjectClassDefinition(name = "%configName", description = "%configDesc", localization = "OSGI-INF/l10n/deprecation/config")
    public @interface Config {

        int DEFAULT_LOGGING_INTERVAL = 24 * 60 * 60; // 1 day

        @AttributeDefinition(name = "%loggingIntervalInSeconds", description = "%loggingIntervalInSecondsDesc", min = "1") int loggingIntervalInSeconds() default DEFAULT_LOGGING_INTERVAL;

        @AttributeDefinition(name = "%excludedMethodsRegexes", description = "%excludedMethodsRegexesDesc") String[] excludedMethodsRegexes() default {
                "org\\.jahia\\.bin\\.Render\\.xssFilter.*" };

        @AttributeDefinition(name = "%developmentModeOnly", description = "%developmentModeOnlyDesc") boolean developmentModeOnly() default false;
    }
}
