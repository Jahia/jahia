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

import java.util.Collection;
import java.util.Map;

import org.jahia.osgi.BundleState;
import org.jahia.osgi.BundleUtils;
import org.jahia.services.modulemanager.BundleBucketInfo;
import org.jahia.services.modulemanager.BundleInfo;
import org.jahia.services.modulemanager.Constants;
import org.jahia.services.modulemanager.InvalidTargetException;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.services.modulemanager.ModuleNotFoundException;
import org.jahia.services.modulemanager.spi.BundleService;
import org.jahia.settings.SettingsBean;

/**
 * Delegate class that is dispatching the calls to an appropriate service implementation of the {@link BundleService}.
 *
 * @author Sergiy Shyrkov
 */
public class BundleServiceDelegate implements BundleService {

    private static final String CLUSTERED_SERVICE_FILTER = "(" + Constants.BUNDLE_SERVICE_PROPERTY_CLUSTERED + "=true)";

    private BundleService defaultBundleService;
    private SettingsBean settingsBean;

    @Override
    public void install(String uri, String target, boolean start) throws ModuleManagementException, InvalidTargetException {
        lookupService().install(uri, target, start);
    }

    @Override
    public void start(BundleInfo bundleInfo, String target) throws ModuleManagementException, ModuleNotFoundException, InvalidTargetException {
        lookupService().start(bundleInfo, target);
    }

    @Override
    public void stop(BundleInfo bundleInfo, String target) throws ModuleManagementException, ModuleNotFoundException, InvalidTargetException {
        lookupService().stop(bundleInfo, target);
    }

    @Override
    public void uninstall(BundleInfo bundleInfo, String target) throws ModuleManagementException, ModuleNotFoundException, InvalidTargetException {
        lookupService().uninstall(bundleInfo, target);
    }

    @Override
    public void refresh(BundleInfo bundleInfo, String target) throws ModuleManagementException, ModuleNotFoundException, InvalidTargetException {
        lookupService().refresh(bundleInfo, target);
    }

    @Override
    public void refresh(Collection<BundleInfo> bundleInfos, String target) throws ModuleManagementException, ModuleNotFoundException, InvalidTargetException {
        lookupService().refresh(bundleInfos, target);
    }

    @Override
    public Map<String, BundleInformation> getInfo(BundleInfo bundleInfo, String target) throws ModuleManagementException, InvalidTargetException {
        return lookupService().getInfo(bundleInfo, target);
    }

    @Override
    public Map<String, Map<String, BundleInformation>> getInfos(Collection<BundleInfo> bundleInfos, String target) throws ModuleManagementException, InvalidTargetException {
        return lookupService().getInfos(bundleInfos, target);
    }

    @Override
    public BundleState getLocalState(BundleInfo bundleInfo)	throws ModuleManagementException, ModuleNotFoundException {
        return lookupService().getLocalState(bundleInfo);
    }

    @Override
    public BundleInformation getLocalInfo(BundleInfo bundleInfo) throws ModuleManagementException, ModuleNotFoundException {
        return lookupService().getLocalInfo(bundleInfo);
    }

    @Override
    public Map<String, BundleInformation> getLocalInfos(BundleBucketInfo bundleBucketInfo) throws ModuleManagementException {
        return lookupService().getLocalInfos(bundleBucketInfo);
    }

    @Override
    public Map<String, BundleInformation> getAllLocalInfos() throws ModuleManagementException {
        return lookupService().getAllLocalInfos();
    }

    private BundleService lookupService() {
        BundleService service = settingsBean.isClusterActivated()
                ? BundleUtils.getOsgiService(BundleService.class, CLUSTERED_SERVICE_FILTER) : null;
        return service != null ? service : defaultBundleService;
    }

    public void setDefaultBundleService(BundleService defaultBundleService) {
        this.defaultBundleService = defaultBundleService;
    }

    public void setSettingsBean(SettingsBean settingsBean) {
        this.settingsBean = settingsBean;
    }
}
