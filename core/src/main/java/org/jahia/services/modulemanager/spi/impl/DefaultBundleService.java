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
package org.jahia.services.modulemanager.spi.impl;

import org.jahia.data.templates.ModuleState;
import org.jahia.osgi.BundleLifecycleUtils;
import org.jahia.osgi.BundleState;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.modulemanager.BundleInfo;
import org.jahia.services.modulemanager.InvalidTargetException;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.services.modulemanager.ModuleNotFoundException;
import org.jahia.services.modulemanager.spi.BundleService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.startlevel.BundleStartLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * The default implementation of the {@link BundleService} which is using direct bundle operations (BundleContext.installBundle(),
 * bundle.start()/stop()/uninstall()). The implementation is used in a standalone DX instance or in case DX clustering is not activated (
 * <code>cluster.activated=false</code>).
 * <p>
 * getInfo/getInfos methods of this implementation return a map containing a single entry whose key is an empty string, and value is local
 * information about the bundle/bundles. These methods do not support the target parameter, because it only makes sense in a cluster;
 * InvalidTargetException will be thrown in case there is a non-null value passed.
 *
 * @author Sergiy Shyrkov
 */
public class DefaultBundleService implements BundleService {

    private JahiaTemplateManagerService templateManagerService;
    private static final Logger logger = LoggerFactory.getLogger(DefaultBundleService.class);

    private static Bundle getBundleEnsureExists(BundleInfo bundleInfo) throws ModuleNotFoundException {
        Bundle bundle = BundleUtils.getBundleBySymbolicName(bundleInfo.getSymbolicName(), bundleInfo.getVersion());
        if (bundle == null) {
            throw new ModuleNotFoundException(bundleInfo.getKey());
        }
        return bundle;
    }

    /**
     * Injects an instance of the template manager service.
     *
     * @param templateManagerService an instance of the template manager service
     */
    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    @Override
    public void install(String uri, String target, boolean start) throws ModuleManagementException {
        try {
            BundleContext bundleContext = FrameworkService.getBundleContext();
            Bundle bundle = bundleContext.getBundle(uri);
            if (bundle == null || bundle.getState() == Bundle.UNINSTALLED) {
                bundle = bundleContext.installBundle(uri);
                bundle.adapt(BundleStartLevel.class).setStartLevel(SettingsBean.getInstance().getModuleStartLevel());
            } else {
                BundleLifecycleUtils.updateBundle(bundle);
            }
            if (start) {
                bundle.start();
            }
            BundleLifecycleUtils.startBundlesPendingDependencies();
        } catch (BundleException e) {
            throw new ModuleManagementException(e);
        }
    }

    protected void refreshUninstalledBundle(Bundle bundle) {
        if (Bundle.UNINSTALLED == bundle.getState()) {
             BundleLifecycleUtils.refreshBundle(bundle);
        }
    }

    @Override
    public void start(BundleInfo bundleInfo, String target) throws ModuleNotFoundException {
        try {
            getBundleEnsureExists(bundleInfo).start();
            BundleLifecycleUtils.startBundlesPendingDependencies();
        } catch (BundleException e) {
            throw new ModuleManagementException(e);
        }
    }

    @Override
    public void stop(BundleInfo bundleInfo, String target) throws ModuleNotFoundException {
        try {
            getBundleEnsureExists(bundleInfo).stop();
            BundleLifecycleUtils.startBundlesPendingDependencies();
        } catch (BundleException e) {
            throw new ModuleManagementException(e);
        }
    }

    @Override
    public void uninstall(BundleInfo bundleInfo, String target) throws ModuleNotFoundException {
        try {
            Bundle bundle = getBundleEnsureExists(bundleInfo);
            bundle.uninstall();
            refreshUninstalledBundle(bundle);
        } catch (BundleException e) {
            throw new ModuleManagementException(e);
        }
    }

    @Override
    public void refresh(BundleInfo bundleInfo, String target) throws ModuleManagementException {
        refresh(Collections.singleton(bundleInfo), target);
    }

    @Override
    public void refresh(Collection<BundleInfo> bundleInfos, String target) throws ModuleManagementException {
        ArrayList<Bundle> bundles = new ArrayList<>(bundleInfos.size());
        for (BundleInfo bundleInfo : bundleInfos) {
            bundles.add(getBundleEnsureExists(bundleInfo));
        }
        BundleLifecycleUtils.refreshBundles(bundles, false, false);
    }

    @Override
    public Map<String, BundleInformation> getInfo(BundleInfo bundleInfo, String target) throws ModuleManagementException, InvalidTargetException {

        if (target != null) {
            throw new InvalidTargetException(target);
        }

        BundleInformation info;
        try {
            info = getLocalInfo(bundleInfo);
        } catch (final Exception e) {

            logger.error("Error retrieving bundle info", e);

            // Return a special implementation of the BundleInformation to indicate failure.
            info = new BundleInformation() {

                @Override
                public org.jahia.osgi.BundleState getOsgiState() throws ModuleManagementException {
                    throw new ModuleManagementException(e.getMessage(), e);
                }
            };
        }
        return Collections.singletonMap("", info);
    }

    @Override
    public Map<String, Map<String, BundleInformation>> getInfos(Collection<BundleInfo> bundleInfos, String target) throws ModuleManagementException, InvalidTargetException {

        if (target != null) {
            throw new InvalidTargetException(target);
        }

        Map<String, BundleInformation> result;
        try {
            result = new LinkedHashMap<String, BundleInformation>();
            for (BundleInfo bundleInfo : new LinkedHashSet<BundleInfo>(bundleInfos)) {
                BundleInformation info = getLocalInfo(bundleInfo);
                result.put(bundleInfo.getKey(), info);
            }
        } catch (final Exception e) {

            logger.error("Error retrieving bundle info", e);

            // Return a special implementation of the Map to indicate failure.
            result = new FailingMap<String, BundleInformation>(new ExceptionProvider() {

                @Override
                public RuntimeException get() {
                    return new ModuleManagementException(e.getMessage(), e);
                }
            });
        }

        return Collections.singletonMap("", result);
    }

    @Override
    public BundleState getLocalState(BundleInfo bundleInfo) throws ModuleNotFoundException {
        Bundle bundle = getBundleEnsureExists(bundleInfo);
        return BundleState.fromInt(bundle.getState());
    }

    @Override
    public BundleInformation getLocalInfo(BundleInfo bundleInfo) throws ModuleNotFoundException {

        Bundle bundle = getBundleEnsureExists(bundleInfo);
        final BundleState osgiState = BundleState.fromInt(bundle.getState());

        if (!BundleUtils.isJahiaModuleBundle(bundle)) {

            return new BundleService.BundleInformation() {

                @Override
                public BundleState getOsgiState() {
                    return osgiState;
                }
            };
        }

        final ModuleState moduleState = templateManagerService.getModuleStates().get(bundle);

        return new BundleService.ModuleInformation() {

            @Override
            public BundleState getOsgiState() {
                return osgiState;
            }

            @Override
            public ModuleState.State getModuleState() {
                return (moduleState == null ? null : moduleState.getState());
            }
        };
    }

    protected interface ExceptionProvider {

        RuntimeException get();
    }

    /**
     * A special implementation of the Map whose any method simply throws an exception provided by the associated ExceptionProvider.
     */
    protected static class FailingMap<K, V> implements Map<K, V> {

        private ExceptionProvider exceptionProvider;

        /**
         * Create an instance of the map.
         * @param exceptionProvider Exception provider to obtain exceptions to throw from
         */
        public FailingMap(ExceptionProvider exceptionProvider) {
            this.exceptionProvider = exceptionProvider;
        }

        @Override
        public void clear() {
            throw exceptionProvider.get();
        }

        @Override
        public boolean containsKey(Object arg0) {
            throw exceptionProvider.get();
        }

        @Override
        public boolean containsValue(Object arg0) {
            throw exceptionProvider.get();
        }

        @Override
        public Set<java.util.Map.Entry<K, V>> entrySet() {
            throw exceptionProvider.get();
        }

        @Override
        public V get(Object arg0) {
            throw exceptionProvider.get();
        }

        @Override
        public boolean isEmpty() {
            throw exceptionProvider.get();
        }

        @Override
        public Set<K> keySet() {
            throw exceptionProvider.get();
        }

        @Override
        public V put(K arg0, V arg1) {
            throw exceptionProvider.get();
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> arg0) {
            throw exceptionProvider.get();
        }

        @Override
        public V remove(Object arg0) {
            throw exceptionProvider.get();
        }

        @Override
        public int size() {
            throw exceptionProvider.get();
        }

        @Override
        public Collection<V> values() {
            throw exceptionProvider.get();
        }
    }
}
