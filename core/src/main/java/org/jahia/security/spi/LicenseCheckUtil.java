package org.jahia.security.spi;

import org.jahia.osgi.BundleUtils;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class LicenseCheckUtil {

    private static volatile boolean initialized = false;
    private static AtomicReference<LicenseCheckerService> instance = new AtomicReference<>();

    private LicenseCheckUtil() {
    }

    /**
     * @see LicenseCheckerService#checkFeature(String)
     */
    public static boolean isAllowed(String featureId) {
        LicenseCheckerService service = getInstance();
        return service != null && service.checkFeature(featureId);
    }

    /**
     * @see LicenseCheckerService#isLimitReached(String, String)
     */
    public static boolean isLimitReached(String componentName, String limitName) {
        LicenseCheckerService service = getInstance();
        return service != null && service.isLimitReached(componentName, limitName);
    }

    /**
     * @see LicenseCheckerService#isLoggedInUsersLimitReached()
     */
    public static boolean isLoggedInUsersLimitReached() {
        LicenseCheckerService service = getInstance();
        return (service != null) && service.isLoggedInUsersLimitReached();
    }

    /**
     * @see LicenseCheckerService#getSiteLimit()
     */
    public static Optional<Long> getSiteLimit() {
        LicenseCheckerService service = getInstance();
        if (service != null) {
            return service.getSiteLimit();
        }
        return Optional.empty();
    }


    private static LicenseCheckerService getInstance() {
        if (instance.get() == null && !initialized) {
            instance.compareAndSet(null, BundleUtils.getOsgiService(LicenseCheckerService.class, null));
            initialized = true;
        }
        return instance.get();
    }
}
