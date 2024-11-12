/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/Apache2 OR 2/JSEL
 *
 *     1/ Apache2
 *     ==================================================================================
 *
 *     Copyright (C) 2002-2025 Jahia Solutions Group SA. All rights reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
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
    public void install(String uri, String target, boolean start, int startLevel) throws ModuleManagementException, InvalidTargetException {
        lookupService().install(uri, target, start, startLevel);
    }

    @Override
    public void resolve(BundleInfo bundleInfo, String target) throws ModuleManagementException, ModuleNotFoundException, InvalidTargetException {
        lookupService().resolve(bundleInfo, target);
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
    public void update(BundleInfo bundleInfo, String target) throws ModuleManagementException, ModuleNotFoundException, InvalidTargetException {
        lookupService().update(bundleInfo, target);
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
    public Map<String, Map<String, BundleInformation>> getInfos(BundleBucketInfo bundleBucketInfo, String target) throws ModuleManagementException, InvalidTargetException {
        return lookupService().getInfos(bundleBucketInfo, target);
    }

    @Override
    public Map<String, Map<String, BundleInformation>> getAllInfos(String target) throws ModuleManagementException, InvalidTargetException {
        return lookupService().getAllInfos(target);
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
