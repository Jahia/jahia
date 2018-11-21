/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
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
package org.jahia.osgi;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.services.SpringContextSingleton;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.ConstantException;
import org.springframework.core.Constants;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Convenient utilities for Jahia OSGi bundles.
 *
 * @author Sergiy Shyrkov
 */
public final class BundleUtils {

    private static final Constants BUNDLE_EVENTS = new Constants(BundleEvent.class);

    private static final Logger logger = LoggerFactory.getLogger(BundleUtils.class);

    private static Map<String, String[]> moduleForClass = new ConcurrentHashMap<String, String[]>();

    private static Map<Bundle, AbstractApplicationContext> contextToStart = new HashMap<Bundle, AbstractApplicationContext>();

    private static Map<String, Throwable> contextException = new HashMap<String, Throwable>();

    private static Map<String, Map<String, JahiaTemplatesPackage>> modules = new ConcurrentHashMap<String, Map<String, JahiaTemplatesPackage>>(
            64);

    /**
     * Returns a String representation for the given bundle event.
     *
     * @param eventType OSGi <code>BundleEvent</code> given as an int
     * @return String representation for the bundle event
     * @see org.eclipse.gemini.blueprint.util.OsgiStringUtils
     */
    public static String bundleEventToString(int eventType) {
        try {
            return BUNDLE_EVENTS.toCode(Integer.valueOf(eventType), "");
        } catch (ConstantException cex) {
            return "Unknown";
        }
    }

    /**
     * Creates an instance of the {@link BundleDelegatingClassLoader} baked by the provided bundle and having Jahia root Spring context's
     * class loader as a parent.
     *
     * @param bundle the bundle to create class loader for
     * @return an instance of the {@link BundleDelegatingClassLoader} baked by the provided bundle and having Jahia root Spring context's
     *         class loader as a parent
     */
    public static ClassLoader createBundleClassLoader(Bundle bundle) {
        return BundleDelegatingClassLoader.createBundleClassLoaderFor(bundle, SpringContextSingleton.getInstance()
                .getContext().getClassLoader());
    }

    /**
     * Finds the bundle by its symbolic name.
     *
     * @param symbolicName the bundle symbolic name
     * @param version the bundle version; or <code>null</code> to get the first matching bundle, found
     * @return the bundle for the specified symbolic name and version or <code>null</code> if the corresponding bundle is not present
     */
    public static Bundle getBundleBySymbolicName(String symbolicName, String version) {
        for (Bundle bundle : FrameworkService.getBundleContext().getBundles()) {
            String n = bundle.getSymbolicName();
            if (StringUtils.equals(n, symbolicName)) {
                if (version == null || StringUtils.equals(bundle.getVersion().toString(), version)) {
                    return bundle;
                }
            }
        }
        return null;
    }

    /**
     * Returns the bundle with the specified identifier.
     *
     * @param id The identifier of the bundle to retrieve.
     * @return A {@code Bundle} object or {@code null} if the identifier does
     *         not match any installed bundle.
     */
    public static Bundle getBundle(long id) {
        return FrameworkService.getBundleContext().getBundle(id);
    }

    /**
     * Finds bundle by its location.
     *
     * @param location the location of the bundle to be found
     * @return bundle for the specified location or <code>null</code> if there is no matching bundle installed
     */
    public static Bundle getBundle(String location) {
        return FrameworkService.getBundleContext().getBundle(location);
    }

    /**
     * Find the bundle that is represented by the specified module and version.
     *
     * @param moduleId the module Id
     * @param version the module version
     * @return the bundle for the specified module and version or <code>null</code> if the corresponding bundle is not present
     */
    public static Bundle getBundle(String moduleId, String version) {
        for (Bundle bundle : FrameworkService.getBundleContext().getBundles()) {
            String n = getModuleId(bundle);
            if (StringUtils.equals(n, moduleId)) {
                String v = getModuleVersion(bundle);
                if (StringUtils.equals(v, version)) {
                    return bundle;
                }
            }
        }
        return null;
    }

    /**
     * Returns the bundle display name containing module name (ID) and the version.
     *
     * @param bundle the bundle to get display name for
     * @return the bundle display name containing module name (ID) and the version
     */
    public static String getDisplayName(Bundle bundle) {
        return getModuleId(bundle) + " v" + getModuleVersion(bundle) + " [" + bundle.getBundleId() + "]";
    }

    /**
     * Returns the module instance that corresponds to the provided OSGi bundle. If the instance is not present yet, creates it and stores
     * internally.
     *
     * @param bundle the corresponding OSGi bundle
     * @return the module instance that corresponds to the provided OSGi bundle
     */
    public static JahiaTemplatesPackage getModule(Bundle bundle) {

        if (bundle.getState() == Bundle.UNINSTALLED) {
            // This should not happen: log detailed info if it does.
            if (logger.isWarnEnabled()) {
                logger.warn("Uninstalled bundle passed; name: {}, version: {}, stack trace: {}",
                        new Object[] { bundle.getSymbolicName(), bundle.getVersion(),
                                Arrays.toString(Thread.currentThread().getStackTrace()) });
            }
        }

        JahiaTemplatesPackage pkg = null;

        String moduleId = getModuleId(bundle);
        String version = getModuleVersion(bundle);
        String groupId = getModuleGroupId(bundle);

        Map<String, JahiaTemplatesPackage> moduleVersions = modules.get(moduleId);
        if (moduleVersions == null) {
            moduleVersions = new ConcurrentHashMap<String, JahiaTemplatesPackage>(1);
            modules.put(moduleId, moduleVersions);
        } else {
            if (!moduleVersions.isEmpty()) {
                JahiaTemplatesPackage firstVersionTemplatePackage = moduleVersions.values().iterator().next();
                if (((firstVersionTemplatePackage.getGroupId() != null) && (!firstVersionTemplatePackage.getGroupId().equals(groupId))) ||
                        ((firstVersionTemplatePackage.getGroupId() == null) && (groupId != null))) {
                    logger.error("A different Jahia Module with the Id " + bundle.getSymbolicName() + " already exists");
                    return null;
                }
            }

            pkg = moduleVersions.get(version);
        }

        if (pkg == null) {
            logger.info("Building module instance for bundle {} v{}", moduleId, version);
            pkg = JahiaBundleTemplatesPackageHandler.build(bundle);
            if (pkg != null) {
                moduleVersions.put(version, pkg);
            } else {
                logger.warn(
                        "Bundle {} seems to be not a valid Jahia module. Cannot build JahiaTemplatesPackage instance for it",
                        bundle.getSymbolicName());
                logger.info("The following manifest headers were found in the bundle: \n{}", bundle.getHeaders());
                throw new IllegalArgumentException("Bundle " + bundle.getSymbolicName()
                        + " is not a valid Jahia module");
            }
        }

        return pkg;
    }

    /**
     * Returns the module name read from the provided bundle.
     *
     * @param bundle the bundle to read module name from
     * @return the module name read from the provided bundle
     */
    public static String getModuleId(Bundle bundle) {
        final String symbolicName = bundle.getSymbolicName();
        if (symbolicName != null) {
            return symbolicName;
        } else {
            throw new NullPointerException("Check your bundle's MANIFEST: missing required Bundle-SymbolicName for bundle " + bundle);
        }
    }

    /**
     * Returns a version of the module read from the provided bundle.
     *
     * @param bundle the bundle to read the module version from
     * @return a version of the module read from the provided bundle
     */
    public static String getModuleVersion(Bundle bundle) {
        final String version = bundle.getVersion().toString();
        if (version == null) {
            throw new NullPointerException("Check your bundle's MANIFEST: missing required Bundle-Version for bundle " + bundle);
        }
        return StringUtils.defaultIfEmpty(bundle.getHeaders().get("Implementation-Version"), version);
    }

    /**
     * Returns the groupId of the module read from the provided bundle.
     *
     * @param bundle the bundle to read the module groupId from
     * @return groupId of the module read from the provided bundle
     */
    public static String getModuleGroupId(Bundle bundle) {
        return bundle.getHeaders().get("Jahia-GroupId");
    }

    /**
     * Returns <code>true</code> if the provided bundle represents Jahia-related bundle (either a module or a service).
     *
     * @param bundle the OSGi bundle to check
     * @return <code>true</code> if the provided bundle represents Jahia-related bundle (either a module or a service)
     */
    public static boolean isJahiaBundle(Bundle bundle) {
        return isJahiaModuleBundle(bundle)
                || StringUtils.defaultString((String) bundle.getHeaders().get("Bundle-Category")).toLowerCase()
                .contains("jahia");
    }

    /**
     * Returns <code>true</code> if the provided bundle represents Jahia module.
     *
     * @param bundle the OSGi bundle to check
     * @return <code>true</code> if the provided bundle represents Jahia module
     */
    public static boolean isJahiaModuleBundle(Bundle bundle) {
        return bundle.getHeaders().get("Jahia-Module-Type") != null;
    }

    /**
     * Performs lookup of the specified class in classloaders of all modules, which are in state Resolved or Active.
     *
     * @param className the fully qualified class name to look up
     * @return the requested class
     * @throws ClassNotFoundException in case the corresponding class cannot be found
     */
    public static Class<?> loadModuleClass(String className) throws ClassNotFoundException {
        return loadModuleClass(className, Bundle.RESOLVED);
    }

    /**
     * Performs lookup of the specified class in classloaders of all modules, which are at least in the requested state.
     *
     * @param className the fully qualified class name to look up
     * @param requiredModuleStatus the minimal required state of the bundle to be considered for class lookup
     * @return the requested class
     * @throws ClassNotFoundException in case the corresponding class cannot be found
     */
    public static Class<?> loadModuleClass(String className, int requiredModuleStatus) throws ClassNotFoundException {
        Class<?> clazz = null;
        String[] moduleKey = moduleForClass.get(className); // [moduleId, moduleVersion]
        if (moduleKey != null) {
            ClassLoader cl = null;
            Map<String, JahiaTemplatesPackage> versions = modules.get(moduleKey[0]);
            if (versions != null) {
                JahiaTemplatesPackage pkg = versions.get(moduleKey[1]);
                if (pkg != null && pkg.getBundle().getState() >= requiredModuleStatus) {
                    cl = pkg.getClassLoader();
                }
            }
            if (cl == null) {
                moduleForClass.remove(className);
            } else {
                return cl.loadClass(className);
            }
        }

        for (Map<String, JahiaTemplatesPackage> moduleVersions : modules.values()) {
            for (JahiaTemplatesPackage pkg : moduleVersions.values()) {
                ClassLoader pkgClassLoader = pkg.getClassLoader();
                if (pkgClassLoader != null) {
                    try {
                        clazz = pkgClassLoader.loadClass(className);
                    } catch (IllegalStateException e) {
                        // An un-installed bundle was likely cached in the modules. This should never happen, but if it does, log to have more info
                        // and continue searching the class in other modules.
                        String message = "Error loading class via module's class loader; group: " + pkg.getGroupId() + ", ID/version: " + pkg.getIdWithVersion();
                        logger.warn(message, e);
                        continue;
                    } catch (ClassNotFoundException e) {
                        // Continue searching the class in other modules.
                        continue;
                    }
                    Bundle bundle = getSourceBundleForClass(clazz);
                    if (bundle != null && bundle.equals(pkg.getBundle()) && bundle.getState() >= requiredModuleStatus) {
                        moduleForClass.put(className, new String[] { pkg.getId(), pkg.getVersion().toString() });
                        return clazz;
                    }
                }
            }
        }

        throw new ClassNotFoundException("Unable to find class '" + className + "' in the class loaders of modules");
    }

    private static Bundle getSourceBundleForClass(Class<?> clazz) {
        Bundle bundle = null;
        try {
            bundle = (Bundle) MethodUtils.invokeExactMethod(clazz.getClassLoader(), "getBundle", null, null);
        } catch (IllegalAccessException e) {
            logger.error(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            // not a bundle class loader
        }
        return bundle;
    }

    /**
     * Removes the module instance that corresponds to the provided OSGi bundle from internal registry.
     *
     * @param bundle the corresponding OSGi bundle
     */
    public static void unregisterModule(Bundle bundle) {

        String moduleId = getModuleId(bundle);
        String version = getModuleVersion(bundle);
        String groupId = getModuleGroupId(bundle);

        Map<String, JahiaTemplatesPackage> moduleVersions = modules.get(moduleId);
        if (moduleVersions != null) {
            if (!moduleVersions.isEmpty()) {
                JahiaTemplatesPackage firstVersionTemplatePackage = moduleVersions.values().iterator().next();
                if (((firstVersionTemplatePackage.getGroupId() != null) && (!firstVersionTemplatePackage.getGroupId().equals(groupId))) ||
                        ((firstVersionTemplatePackage.getGroupId() == null) && (groupId != null))) {
                    logger.warn("A different Jahia Module with the Id " + bundle.getSymbolicName() + " already exists");
                    return;
                }
            }

            JahiaTemplatesPackage pkg = moduleVersions.remove(version);
            if (moduleVersions.isEmpty()) {
                modules.remove(moduleId);
            }
            if (pkg != null) {
                pkg.setClassLoader(null);
            } else {
                logger.warn("Version {} of module {} does not exist", version, moduleId);
            }
        }
    }

    public static AbstractApplicationContext getContextToStartForModule(Bundle bundle) {
        return contextToStart.get(bundle);
    }

    public static void setContextToStartForModule(Bundle bundle, AbstractApplicationContext context) {
        contextToStart.put(bundle, context);
    }

    public static Throwable getContextStartException(String key) {
        return contextException.get(key);
    }

    public static void setContextStartException(String key, Throwable exception) {
        contextException.put(key, exception);
    }

    /**
     * Looks up an OSGi service for the specified class and filter. If multiple services are found matching the criteria, returns the best
     * one, based on the service ranking. If no matching service has been found, returns <code>null</code>.
     *
     * @param serviceClass the class of the service to be looked up
     * @param filter the filter expression or <code>null</code> for all services of the specified type
     * @return an OSGi service instance matching the specified criteria or <code>null</code> if no match was found
     * @throws IllegalArgumentException in case the filter expression is syntactically invalid
     */
    public static <S> S getOsgiService(Class<S> serviceClass, String filter) {
        S serviceInstance = null;
        BundleContext bundleContext = FrameworkService.getBundleContext();
        Collection<ServiceReference<S>> serviceReferences;
        try {
            serviceReferences = bundleContext.getServiceReferences(serviceClass, filter);
        } catch (InvalidSyntaxException e) {
            throw new JahiaRuntimeException(e);
        }
        if (serviceReferences != null && !serviceReferences.isEmpty()) {
            ServiceReference<S> bestServiceReferefnce;
            if (serviceReferences.size() > 1) {
                List<ServiceReference<S>> matchingServices = new ArrayList<>(serviceReferences);
                // sort references by ranking (ascending)
                Collections.sort(matchingServices);
                // get the service with the highest ranking
                bestServiceReferefnce = matchingServices.get(matchingServices.size() - 1);
            } else {
                bestServiceReferefnce = serviceReferences.iterator().next();
            }
            // obtain the service
            serviceInstance = bundleContext.getService(bestServiceReferefnce);
        }

        return serviceInstance;
    }

    /**
     * Looks up an OSGi service for the specified class name and filter. If multiple services are found matching the criteria, returns the best
     * one, based on the service ranking. If no matching service has been found, returns <code>null</code>.
     *
     * @param serviceClassName the class name of the service to be looked up
     * @param filter the filter expression or <code>null</code> for all services of the specified type
     * @return an OSGi service instance matching the specified criteria or <code>null</code> if no match was found
     * @throws IllegalArgumentException in case the filter expression is syntactically invalid
     */
    public static Object getOsgiService(String serviceClassName, String filter) {
        Object serviceInstance = null;
        BundleContext bundleContext = FrameworkService.getBundleContext();
        Collection<ServiceReference<?>> serviceReferences;
        try {
            ServiceReference<?>[] refs = bundleContext.getServiceReferences(serviceClassName, filter);
            serviceReferences = refs != null && refs.length > 0 ? Arrays.asList(refs) : null;
        } catch (InvalidSyntaxException e) {
            throw new JahiaRuntimeException(e);
        }
        if (serviceReferences != null && !serviceReferences.isEmpty()) {
            ServiceReference<?> bestServiceReferefnce;
            if (serviceReferences.size() > 1) {
                List<ServiceReference<?>> matchingServices = new ArrayList<>(serviceReferences);
                // sort references by ranking (ascending)
                Collections.sort(matchingServices);
                // get the service with the highest ranking
                bestServiceReferefnce = matchingServices.get(matchingServices.size() - 1);
            } else {
                bestServiceReferefnce = serviceReferences.iterator().next();
            }
            // obtain the service
            serviceInstance = bundleContext.getService(bestServiceReferefnce);
        }

        return serviceInstance;
    }

    /**
     * Checks if the provided bundle is a fragment bundle.
     *
     * @param bundle the bundle to be checked
     * @return <code>true</code> in case the supplied bundle is a fragment bundle; <code>false</code> otherwise
     */
    public static boolean isFragment(Bundle bundle) {
        return StringUtils.isNotBlank((String) bundle.getHeaders().get(org.osgi.framework.Constants.FRAGMENT_HOST));
    }

}