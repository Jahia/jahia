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

import java.util.Collections;

import org.jahia.osgi.BundleUtils;
import org.jahia.osgi.FrameworkService;
import org.jahia.services.modulemanager.BundleInfo;
import org.jahia.services.modulemanager.spi.BundleService;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.osgi.framework.startlevel.BundleStartLevel;

/**
 * The default implementation of the {@link BundleService} which is using direct bundle operations (BundleContext.installBundle(),
 * bundle.start()/stop()/uninstall()). The implementation is used in a standalone DX instance or in case DX clustering is not activated (
 * <code>cluster.activated=false</code>).
 *
 * @author Sergiy Shyrkov
 */
public class DefaultBundleService implements BundleService {

    private Bundle getBundleEnsureExists(BundleInfo bundleInfo) throws BundleException {
        Bundle bundle = BundleUtils.getBundleBySymbolicName(bundleInfo.getSymbolicName(), bundleInfo.getVersion());
        if (bundle == null) {
            throw new BundleException("Unable to find bundle for symbolic name " + bundleInfo.getSymbolicName()
                    + " and version " + bundleInfo.getVersion(), BundleException.RESOLVE_ERROR);
        }
        return bundle;
    }

    @Override
    public void install(String uri, String target, boolean start) throws BundleException {
        Bundle installedBundle = FrameworkService.getBundleContext().installBundle(uri);
        installedBundle.adapt(BundleStartLevel.class).setStartLevel(SettingsBean.getInstance().getModuleStartLevel());
        if (start) {
            installedBundle.start();
        } else {
            // force bundle resolution
            BundleUtils.resolveBundles(Collections.singleton(installedBundle));
        }
    }

    @Override
    public void start(BundleInfo bundleInfo, String target) throws BundleException {
        getBundleEnsureExists(bundleInfo).start();
    }

    @Override
    public void stop(BundleInfo bundleInfo, String target) throws BundleException {
        getBundleEnsureExists(bundleInfo).stop();
    }

    @Override
    public void uninstall(BundleInfo bundleInfo, String target) throws BundleException {
        getBundleEnsureExists(bundleInfo).uninstall();
    }
}
