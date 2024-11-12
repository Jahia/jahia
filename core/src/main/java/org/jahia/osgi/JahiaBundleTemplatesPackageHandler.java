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
package org.jahia.osgi;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.modulemanager.Constants;
import org.jahia.services.modulemanager.models.JahiaDepends;
import org.jahia.services.modulemanager.util.ModuleUtils;
import org.jahia.services.templates.ModuleVersion;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.Bundle;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.Enumeration;

/**
 * Utility class for creating {@link JahiaTemplatesPackage} from a provided bundle and populating data from the manifest headers.
 *
 * @author Sergiy Shyrkov
 */
class JahiaBundleTemplatesPackageHandler {
    static JahiaTemplatesPackage build(Bundle bundle) {
        if (bundle == null) {
            throw new IllegalArgumentException("Provided bundle is null");
        }

        String moduleType = getHeader(bundle, "Jahia-Module-Type");

        if (StringUtils.isEmpty(moduleType)) {
            // not a valid Jahia module package
            return null;
        }

        final JahiaTemplatesPackage pkg = new JahiaTemplatesPackage(bundle);

        pkg.setModuleType(moduleType);

        pkg.setModulePriority(Integer.parseInt(StringUtils.defaultString(getHeader(bundle,"Jahia-Module-Priority"), "0")));
        pkg.setEditModeBlocked(Boolean.parseBoolean(StringUtils.defaultString(getHeader(bundle, "Jahia-Block-Edit-Mode"), "false")));
        pkg.setAutoDeployOnSite(StringUtils.defaultString(getHeader(bundle, "Jahia-Deploy-On-Site")));
        pkg.setName(StringUtils.defaultString(getHeader(bundle, "Implementation-Title", "Bundle-Name"),
                bundle.getSymbolicName()));

        pkg.setVersion(new ModuleVersion(StringUtils.defaultIfBlank(getHeader(bundle, "Implementation-Version"), bundle
                .getVersion().toString())));

        pkg.setId(bundle.getSymbolicName());
        pkg.setDescription(getHeader(bundle, "Bundle-Description"));
        detectResourceBundle(bundle, pkg);

        setSourcesFolderInPackageIfPossible(bundle, pkg);

        if (pkg.getScmURI() == null) {
            String scmUri = getHeader(bundle, "Jahia-Source-Control-Connection");
            pkg.setScmURI(scmUri);
        }

        if (pkg.getScmTag() == null) {
            String scmTag = getHeader(bundle, "Jahia-Source-Control-Tag");
            pkg.setScmTag(scmTag);
        }

        //Check if sources are downloadable for this package
        boolean isSourcesDownloadable = SettingsBean.getInstance().isMavenExecutableSet();
        if (isSourcesDownloadable) {
            String downloadSourcesHeader = getHeader(bundle, "Jahia-Download-Sources-Available");
            if (downloadSourcesHeader != null
                    && ("false".equalsIgnoreCase(downloadSourcesHeader) || "no".equalsIgnoreCase(downloadSourcesHeader))) {
                isSourcesDownloadable = false;
            }
        }
        pkg.setSourcesDownloadable(isSourcesDownloadable);

        URL rootEntry = bundle.getEntry("/");
        if (rootEntry != null) {
            pkg.setFilePath(rootEntry.getPath());
        }

        String depends = getHeader(bundle, "Jahia-Depends");
        setPackageDepends(pkg, depends);

        pkg.setProvider(StringUtils.defaultString(getHeader(bundle, "Implementation-Vendor"),
                "Jahia Solutions Group SA"));

        pkg.setForgeUrl(getHeader(bundle, "Jahia-Private-App-Store"));

        pkg.setGroupId(getHeader(bundle, "Jahia-GroupId"));

        return pkg;
    }

    private static void setSourcesFolderInPackageIfPossible(Bundle bundle, JahiaTemplatesPackage pkg) {
        String srcFolder = getHeader(bundle, "Jahia-Source-Folders");
        if (srcFolder != null) {
            File sources = new File(srcFolder);
            if (sources.exists()) {
                ServicesRegistry.getInstance().getJahiaTemplateManagerService().setSourcesFolderInPackage(pkg, sources);
            } else {
                File containerSources = new File(SettingsBean.getInstance().getModulesSourcesDiskPath() + File.separator + sources.getName());
                if (Files.exists(containerSources.toPath())) {
                    ServicesRegistry.getInstance().getJahiaTemplateManagerService().setSourcesFolderInPackage(pkg, containerSources);
                }
            }
        }
    }

    private static void setPackageDepends(JahiaTemplatesPackage pkg, String depends) {
        if (StringUtils.isNotBlank(depends)) {
            String[] dependencies = ModuleUtils.toDependsArray(depends);
            for (String dependency : dependencies) {
                JahiaDepends dep = new JahiaDepends(dependency);
                if (!dep.isOptional()) {
                    pkg.setDepends(dep.getModuleName());
                }
                pkg.setVersionDepends(dep);
            }
        }
    }

    private static void detectResourceBundle(Bundle bundle, JahiaTemplatesPackage pkg) {
        String resourceBundle = getHeader(bundle, "Jahia-Resource-Bundle");
        if (StringUtils.isNotBlank(resourceBundle)) {
            pkg.setResourceBundleName(resourceBundle.trim());
            return;
        }

        String rbName = pkg.getId();
        if (hasResourceBundle(bundle, rbName)) {
            pkg.setResourceBundleName("resources." + rbName);
            return;
        }

        rbName = StringUtils.replace(pkg.getName(), " ", "");
        if (hasResourceBundle(bundle, rbName)) {
            pkg.setResourceBundleName("resources." + rbName);
            return;
        }

        rbName = StringUtils.replace(pkg.getName(), " ", "_");
        if (hasResourceBundle(bundle, rbName)) {
            pkg.setResourceBundleName("resources." + rbName);
        }
    }

    private static boolean hasResourceBundle(Bundle bundle, String resourceBundleName) {
        // check if there is a resource bundle file in the resources folder
        Enumeration<String> paths = bundle.getEntryPaths("/resources/");
        while (paths != null && paths.hasMoreElements()) {
            String path = paths.nextElement();
            if (StringUtils.startsWith(path, "resources/" +resourceBundleName) && StringUtils.endsWith(path, ".properties")) {
                return true;
            }
        }
        return false;
    }

    private static String getHeader(Bundle bundle, String... headerNamesToLookup) {
        String val = null;
        for (String headerName : headerNamesToLookup) {
            val = (String) bundle.getHeaders().get(headerName);
            if (val != null) {
                break;
            }
        }

        return val;
    }

}
