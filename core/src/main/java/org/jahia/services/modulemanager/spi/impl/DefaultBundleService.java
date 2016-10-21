/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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

import org.jahia.osgi.BundleLifecycleUtils;
import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.modulemanager.BundleInfo;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.services.modulemanager.ModuleNotFoundException;
import org.jahia.services.modulemanager.spi.BundleService;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.startlevel.BundleStartLevel;

import java.util.Collections;

/**
 * The default implementation of the {@link BundleService} which is using direct bundle operations (BundleContext.installBundle(),
 * bundle.start()/stop()/uninstall()). The implementation is used in a standalone DX instance or in case DX clustering is not activated (
 * <code>cluster.activated=false</code>).
 *
 * @author Sergiy Shyrkov
 */
public class DefaultBundleService implements BundleService {

    private Bundle getBundleEnsureExists(BundleInfo bundleInfo) throws ModuleNotFoundException {
        Bundle bundle = BundleUtils.getBundleBySymbolicName(bundleInfo.getSymbolicName(), bundleInfo.getVersion());
        if (bundle == null) {
            throw new ModuleNotFoundException(bundleInfo.getKey());
        }
        return bundle;
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
                bundle.update();
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
        Bundle bundle = getBundleEnsureExists(bundleInfo);
        BundleLifecycleUtils.refreshBundles(Collections.singleton(bundle), false, false);
    }
}
