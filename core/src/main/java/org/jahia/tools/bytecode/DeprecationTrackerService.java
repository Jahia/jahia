package org.jahia.tools.bytecode;

/**
 * Tracker service for recording and monitoring deprecated method calls and feature usage in Jahia Core.
 * <p>
 * This service provides two main tracking mechanisms:
 * </p>
 * <ul>
 *   <li><strong>Automatic method tracking:</strong> Invoked automatically by the {@link DeprecationTrackerPlugin}
 *       bytecode instrumentation layer whenever a method annotated with {@link Deprecated} is called at runtime.</li>
 *   <li><strong>Manual feature tracking:</strong> Explicitly called to track usage of deprecated features,
 *       patterns, or behaviors that are not specific methods.</li>
 * </ul>
 * <p>
 * The tracking behavior can be configured via the OSGi configuration file:
 * {@code org.jahia.bundles.core.services.deprecation.cfg}
 * </p>
 * <p>
 * Configuration options include:
 * </p>
 * <ul>
 *   <li>{@code loggingIntervalInSeconds} - Minimum interval between consecutive logs for the same method/feature (default: 86400 seconds / 1 day)</li>
 *   <li>{@code excludedMethodsRegexes} - Regular expressions to exclude specific methods from tracking</li>
 *   <li>{@code developmentModeOnly} - When enabled, tracking only occurs when {@code operatingMode=development} in jahia.properties</li>
 * </ul>
 *
 * @see DeprecationTrackerPlugin
 */
public interface DeprecationTrackerService {

    /**
     * Records a call to a deprecated method.
     * <p>
     * This callback is invoked automatically by instrumented bytecode whenever a method
     * marked with {@code @Deprecated} is called. The metadata parameters are pre-computed
     * at build time and embedded as constants in the bytecode for optimal runtime performance.
     * </p>
     * <p>
     * The actual logging behavior is controlled by the service configuration. Methods may be
     * excluded from tracking via regex patterns, and logging is rate-limited to avoid excessive
     * log output from frequently called deprecated methods.
     * </p>
     *
     * @param methodSignature  the fully qualified signature of the deprecated method in the format:
     *                         {@code com.example.ClassName.methodName(param.Type, ...)}
     * @param deprecatedSince  the version when the method was deprecated ({@link Deprecated#since()}),
     *                         or an empty string if not specified
     * @param markedForRemoval {@code true} if the method is subject to removal in a future version
     *                         ({@link Deprecated#forRemoval()}), {@code false} otherwise
     */
    void onMethodCall(String methodSignature, String deprecatedSince, boolean markedForRemoval);

    /**
     * Records usage of a deprecated feature.
     * <p>
     * This method should be called explicitly to track usages of deprecated features that are not
     * specific methods, such as:
     * </p>
     * <ul>
     *   <li>Configuration options or properties</li>
     *   <li>API patterns or usage conventions</li>
     *   <li>Runtime behaviors or operational modes</li>
     *   <li>Data formats or protocols</li>
     * </ul>
     * <p>
     * Unlike {@link #onMethodCall(String, String, boolean)}, this method is not invoked automatically
     * and must be called explicitly in code where the deprecated feature is detected or used.
     * </p>
     *
     * @param featureName      a descriptive name identifying the deprecated feature
     *                         (e.g., "Legacy XML Configuration", "Old Authentication Flow")
     * @param deprecatedSince  the version when the feature was deprecated, or an empty string if not specified
     * @param markedForRemoval {@code true} if the feature is subject to removal in a future version,
     *                         {@code false} otherwise
     * @param details          additional contextual information about the feature usage, such as:
     *                         code location, configuration settings, or migration guidance;
     *                         may be {@code null} if no additional details are available
     */
    void onFeatureUsage(String featureName, String deprecatedSince, boolean markedForRemoval, String details);
}
