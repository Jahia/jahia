/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.templates.ModuleVersion;
import org.jahia.settings.SettingsBean;
import org.osgi.framework.Bundle;

import java.io.File;
import java.net.URL;
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

        String srcFolder = getHeader(bundle, "Jahia-Source-Folders");
        if (srcFolder != null) {
            File sources = new File(srcFolder);
            if (sources.exists()) {
                ServicesRegistry.getInstance().getJahiaTemplateManagerService().setSourcesFolderInPackage(pkg, sources);
            }
        }

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
        if (StringUtils.isNotBlank(depends)) {
            String[] dependencies = StringUtils.split(depends, ",");
            for (String dependency : dependencies) {
                pkg.setDepends(dependency.trim());
            }
        }

        pkg.setProvider(StringUtils.defaultString(getHeader(bundle, "Implementation-Vendor"),
                "Jahia Solutions Group SA"));

        pkg.setForgeUrl(getHeader(bundle, "Jahia-Private-App-Store"));

        pkg.setGroupId(getHeader(bundle, "Jahia-GroupId"));

        return pkg;
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
