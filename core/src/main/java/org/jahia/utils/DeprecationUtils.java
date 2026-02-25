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
package org.jahia.utils;

import org.jahia.osgi.BundleUtils;
import org.jahia.tools.bytecode.DeprecationTrackerService;

/**
 * Utility class for tracking deprecated methods and features in Jahia.
 * <p>
 * This class provides static helper methods that are automatically injected into deprecated code
 * via bytecode instrumentation. It acts as a bridge between instrumented code and the OSGi-based
 * {@link DeprecationTrackerService}, enabling centralized logging and monitoring of deprecated API usage.
 * </p>
 * <p>
 * The tracking behavior can be configured through the OSGi configuration file:
 * {@code org.jahia.bundles.core.services.deprecation.cfg}
 * </p>
 *
 * @see DeprecationTrackerService
 */
public class DeprecationUtils {
    /**
     * Tracks the invocation of a deprecated method.
     * <p>
     * This static method is automatically injected into deprecated methods during bytecode instrumentation.
     * It looks up the {@link DeprecationTrackerService} from the OSGi service registry and delegates
     * the tracking to it. If the service is unavailable, the call is silently ignored.
     * </p>
     *
     * @param methodSignature the fully qualified method signature including class name, method name,
     *                        and parameter types (e.g., "com.example.Service.myMethod(String, int)")
     * @param deprecatedSince the version since which the method has been deprecated, as specified in
     *                        the {@code @Deprecated} annotation's {@code since} attribute; may be empty
     * @param forRemoval      {@code true} if the method is marked for removal in a future version,
     *                        as indicated by the {@code @Deprecated} annotation's {@code forRemoval} attribute
     */
    public static void onDeprecatedMethodCall(String methodSignature, String deprecatedSince, boolean forRemoval) {
        DeprecationTrackerService service = BundleUtils.getOsgiService(DeprecationTrackerService.class, null);
        if (service != null) {
            service.onMethodCall(methodSignature, deprecatedSince, forRemoval);
        }
    }

    /**
     * Tracks the usage of a deprecated feature.
     * <p>
     * This method should be called explicitly when a deprecated feature (not a specific method) is used,
     * such as configuration options, API patterns, or runtime behaviors that are being phased out.
     * It provides more context than method-level tracking by allowing additional details to be specified.
     * </p>
     *
     * @param featureName     a descriptive name for the deprecated feature (e.g., "Legacy XML Configuration",
     *                        "Old Authentication Flow")
     * @param deprecatedSince the version since which the feature has been deprecated
     * @param forRemoval      {@code true} if the feature is marked for removal in a future version
     * @param details         additional contextual information about the feature usage, such as the code location,
     *                        configuration settings, or migration guidance; may be {@code null}
     */
    public static void onDeprecatedFeatureUsage(String featureName, String deprecatedSince, boolean forRemoval, String details) {
        DeprecationTrackerService service = BundleUtils.getOsgiService(DeprecationTrackerService.class, null);
        if (service != null) {
            service.onFeatureUsage(featureName, deprecatedSince, forRemoval, details);
        }
    }
}
