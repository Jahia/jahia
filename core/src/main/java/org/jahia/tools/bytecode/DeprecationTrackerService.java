package org.jahia.tools.bytecode;

/**
 * Tracker service for recording and monitoring deprecated method calls in Jahia Core.
 * <p>
 * This service is invoked automatically by the {@link DeprecationTrackerPlugin} bytecode
 * instrumentation layer whenever a method annotated with {@link Deprecated} is called at runtime.
 * </p>
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
     *
     * @param methodSignature  the fully qualified signature of the deprecated method in the format:
     *                         {@code com.example.ClassName.methodName(param.Type, ...)}
     * @param deprecatedSince  the version when the method was deprecated ({@link Deprecated#since()}) or {@code null} if not specified
     * @param markedForRemoval {@code true} if the method is subject to removal in a future version ({@link Deprecated#forRemoval()}), {@code false} otherwise
     */
    void onMethodCall(String methodSignature, String deprecatedSince, boolean markedForRemoval);

}
