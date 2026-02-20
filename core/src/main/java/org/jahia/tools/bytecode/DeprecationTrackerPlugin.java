package org.jahia.tools.bytecode;

import net.bytebuddy.build.Plugin;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodCall;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.commons.lang3.StringUtils;
import org.jahia.osgi.BundleUtils;

import java.lang.reflect.Method;

/**
 * ByteBuddy plugin that instruments deprecated methods to track their usage.
 * <p>
 * This transformer extracts deprecation metadata (method signature, 'since' attribute,
 * and 'forRemoval' flag) at build time and injects calls to {@link #onDeprecatedMethodCall}
 * with the pre-computed metadata.
 * </p>
 */
public class DeprecationTrackerPlugin implements Plugin {

    @Override
    public DynamicType.Builder<?> apply(DynamicType.Builder<?> builder, TypeDescription typeDescription,
            ClassFileLocator classFileLocator) {

        DynamicType.Builder<?> result = builder;

        // Process each deprecated method individually to extract metadata at build time
        for (MethodDescription method : typeDescription.getDeclaredMethods().filter(ElementMatchers.isAnnotatedWith(Deprecated.class))) {

            // Extract deprecation metadata at build time (single pass)
            String methodSignature = buildMethodSignature(method);
            DeprecationMetadata metadata = extractDeprecationMetadata(method);

            // Inject call to our static helper method with build-time constants
            result = result.method(ElementMatchers.is(method)).intercept(
                    MethodCall.invoke(getTrackDeprecationMethod()).with(methodSignature).with(metadata.since).with(metadata.forRemoval)
                            .andThen(SuperMethodCall.INSTANCE));
        }

        return result;
    }

    /**
     * Static helper method that gets injected into deprecated methods.
     * This does the OSGi service lookup and delegates to the {@link DeprecationTrackerService}.
     *
     * @param methodSignature the fully qualified method signature
     * @param deprecatedSince the 'since' value from @Deprecated annotation
     * @param forRemoval      whether the method is marked for removal
     */
    public static void onDeprecatedMethodCall(String methodSignature, String deprecatedSince, boolean forRemoval) {
        DeprecationTrackerService service = BundleUtils.getOsgiService(DeprecationTrackerService.class, null);
        if (service != null) {
            service.onMethodCall(methodSignature, deprecatedSince, forRemoval);
        }
    }

    /**
     * Gets the trackDeprecation helper method for ByteBuddy to invoke.
     */
    private static Method getTrackDeprecationMethod() {
        try {
            return DeprecationTrackerPlugin.class.getMethod("onDeprecatedMethodCall", String.class, String.class, boolean.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Failed to find trackDeprecation method", e);
        }
    }

    @Override
    public boolean matches(TypeDescription target) {
        return !target.getDeclaredMethods().filter(ElementMatchers.isAnnotatedWith(Deprecated.class)).isEmpty();
    }

    @Override
    public void close() {
        // No resources to close
    }

    /**
     * Builds a unique method signature string for a given method at build time.
     * <p>
     * The signature format is: {@code fully.qualified.ClassName.methodName(param1.Type, param2.Type)}
     * </p>
     */
    private static String buildMethodSignature(MethodDescription method) {
        StringBuilder signature = new StringBuilder();
        signature.append(method.getDeclaringType().getTypeName()).append(".").append(method.getName()).append("(");

        int paramCount = method.getParameters().size();
        for (int i = 0; i < paramCount; i++) {
            if (i > 0) {
                signature.append(", ");
            }
            signature.append(method.getParameters().get(i).getType().getTypeName());
        }
        signature.append(")");

        return signature.toString();
    }

    /**
     * Extracts both 'since' and 'forRemoval' attributes from the @Deprecated annotation
     * at build time in a single pass.
     * <p>
     * This is more efficient than extracting each attribute separately, as it only needs
     * to retrieve and load the annotation once.
     * </p>
     *
     * @param method the method to extract deprecation metadata from
     * @return a DeprecationMetadata object containing both attributes
     */
    private static DeprecationMetadata extractDeprecationMetadata(MethodDescription method) {
        AnnotationDescription deprecated = method.getDeclaredAnnotations().filter(ElementMatchers.annotationType(Deprecated.class))
                .getOnly();
        if (deprecated == null) {
            // Default values if annotation not found or cannot be loaded
            return new DeprecationMetadata(null, false);
        }
        Deprecated deprecatedAnnotation = deprecated.prepare(Deprecated.class).load();
        String since = StringUtils.trimToNull(deprecatedAnnotation.since());
        boolean forRemoval = deprecatedAnnotation.forRemoval();
        return new DeprecationMetadata(since, forRemoval);

    }

    /**
     * Holds deprecation metadata extracted from the @Deprecated annotation.
     */
    private static class DeprecationMetadata {
        final String since;
        final boolean forRemoval;

        DeprecationMetadata(String since, boolean forRemoval) {
            this.since = since;
            this.forRemoval = forRemoval;
        }
    }
}
