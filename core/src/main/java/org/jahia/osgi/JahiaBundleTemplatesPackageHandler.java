/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */
package org.jahia.osgi;

import java.io.File;
import java.util.Arrays;
import java.util.Enumeration;

import org.apache.commons.lang.StringUtils;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.templates.ModuleVersion;
import org.osgi.framework.Bundle;

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

        pkg.setAutoDeployOnSite(StringUtils.defaultString(getHeader(bundle,"Jahia-Deploy-On-Site")));
        pkg.setName(StringUtils.defaultString(getHeader(bundle, "Implementation-Title", "Bundle-Name"),
                bundle.getSymbolicName()));

        pkg.setVersion(new ModuleVersion(StringUtils.defaultIfBlank(getHeader(bundle, "Implementation-Version"), bundle
                .getVersion().toString())));

        pkg.setRootFolder(StringUtils.defaultString(getHeader(bundle, "Jahia-Root-Folder"), bundle.getSymbolicName()));
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

        //Check if sources are downloadable for this package
        Boolean isSourcesDownloadable = Boolean.TRUE;
        String downloadSourcesHeader = getHeader(bundle, "Jahia-Download-Sources-Available");
        if(downloadSourcesHeader!=null) {
            isSourcesDownloadable = Boolean.valueOf(downloadSourcesHeader);
        }
        pkg.setSourcesDownloadable(isSourcesDownloadable);


        if (bundle.getEntry("/") != null) {
            pkg.setFilePath(bundle.getEntry("/").getPath());
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

        pkg.setClassLoader(BundleUtils.createBundleClassLoader(bundle));

        pkg.setForgeUrl(getHeader(bundle, "Jahia-Forge"));

        return pkg;
    }

    private static void detectResourceBundle(Bundle bundle, JahiaTemplatesPackage pkg) {
        String resourceBundle = getHeader(bundle, "Jahia-Resource-Bundle");
        if (StringUtils.isNotBlank(resourceBundle)) {
            pkg.setResourceBundleName(resourceBundle.trim());
            return;
        }

        String rbName = pkg.getRootFolder();
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
        boolean found = false;
        // check if there is a resource bundle file in the resources folder
        if (bundle.getEntry("/resources/" + resourceBundleName + ".properties") != null) {
            found = true;
        } else {
            Enumeration<?> entries = bundle.findEntries("/resources", resourceBundleName + "_*.properties", false);
            found = entries != null && entries.hasMoreElements();
        }

        return found;
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
