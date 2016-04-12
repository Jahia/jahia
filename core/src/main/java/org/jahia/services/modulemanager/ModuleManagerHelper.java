/*
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
package org.jahia.services.modulemanager;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jahia.bin.Jahia;
import org.jahia.commons.Version;
import org.jahia.security.license.LicenseCheckerService;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.ModuleVersion;
import org.jahia.services.templates.TemplatePackageRegistry;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;

/**
 * @author bdjiba
 *
 */
public class ModuleManagerHelper {

    /**
     * Validate the given jahia package.
     * Below attributes are checked:
     * <ul>
     *    <li>Jahia-Package-Name: a jahia package should contains this manifest attribute key and should not be empty</li>
     *    <li>Jahia-Package-License: a valid package license feature</li>
     *    <li>Jahia-Required-Version: {@link #isValidJahiaVersion(Attributes, MessageContext)} is used </li>
     * </ul>
     * @param bundleManifest the package bundle manifest file
     * @param context the message context
     * @param originalFilename the original file name
     * @return true when the validation is fine otherwise return false.
     * @throws IOException
     */
    public static boolean isValidJahiaPackageFile(Manifest bundleManifest, MessageContext context, String originalFilename) throws IOException {
        if (bundleManifest == null) {
            return false;
        }
        // Assume that it is valid and try to invalidate
        // and pass over all validation and return final validation result
        boolean isValid = true;
        Attributes manifestAttributes = bundleManifest.getMainAttributes();
        if (manifestAttributes.containsKey("Jahia-Package-Name")) {
            // a package
            if (StringUtils.isBlank(manifestAttributes.getValue("Jahia-Package-Name"))) {
                if (context != null) {
                    context.addMessage(new MessageBuilder().source("moduleFile")
                            .code("serverSettings.manageModules.install.package.name.error").error()
                            .build());
                }
                isValid = false;
            }
            String licenseFeature = manifestAttributes.getValue("Jahia-Package-License");
            if (licenseFeature != null && !LicenseCheckerService.Stub.isAllowed(licenseFeature)) {
                if (context != null) {
                    context.addMessage(new MessageBuilder().source("moduleFile")
                            .code("serverSettings.manageModules.install.package.missing.license")
                            .args(new String[]{originalFilename, licenseFeature})
                            .error() // FIXME: Should be consider as an error
                            .build());
                }
                isValid = false;
            }
        }

        return isValid && isValidJahiaVersion(bundleManifest.getMainAttributes(), context);
    }

    /**
     * Check if the bundle manifest contains a correct Jahia required version
     * and if it is compatible to the current running plateform.
     * The target manifest attribute is Jahia-Required-Version
     * If it return false the message context is used to specify the reason
     * @param manifestAttributes the manifest main attributes
     * @param context the message context.
     * @return true if the Jahia version
     */
    public static boolean isValidJahiaVersion(Attributes manifestAttributes, MessageContext context) {
        boolean isValidValidated = true;
        String jahiaRequiredVersion = manifestAttributes.getValue("Jahia-Required-Version");
        if (StringUtils.isEmpty(jahiaRequiredVersion)) {
            context.addMessage(new MessageBuilder().source("moduleFile")
                    .code("serverSettings.manageModules.install.required.version.missing.error").error().build());
            isValidValidated = false;
        }
        if (new Version(jahiaRequiredVersion).compareTo(new Version(Jahia.VERSION)) > 0) {
            context.addMessage(new MessageBuilder().source("moduleFile")
                    .code("serverSettings.manageModules.install.required.version.error")
                    .args(new String[]{jahiaRequiredVersion, Jahia.VERSION}).error().build());
            isValidValidated = false;
        }
        return isValidValidated;
    }

    /**
     * Check is the given bundle is a package, a mega-jar
     * @param bundleManifest the target bundle manifest
     * @return true is it is jar package otherwise return false
     * @throws IOException
     */
    public static boolean isPackageModule(Manifest bundleManifest) throws IOException {
        return StringUtils.isNotBlank(bundleManifest.getMainAttributes().getValue("Jahia-Package-Name"));
    }


    /**
     * Check if a module with the same ID exists
     * @param bundleSymbolicName the bundle symbolic name
     * @param bundleJahiaGrpID the bundle jahia group id
     * @param context the message context
     * @param templateManagerService the Jahia template manager service
     * @return true if no module with the same id exist otherwise return false.
     * @throws IOException
     */
    public static boolean isDifferentModuleWithSameIdExists(String bundleSymbolicName, String bundleJahiaGrpID, MessageContext context, JahiaTemplateManagerService templateManagerService) throws IOException {
        boolean isSameIdExists = false;
        if (templateManagerService.differentModuleWithSameIdExists(bundleSymbolicName, bundleJahiaGrpID)) {
            if (context != null) {
                context.addMessage(new MessageBuilder().source("moduleFile")
                        .code("serverSettings.manageModules.install.moduleWithSameIdExists")
                        .arg(bundleSymbolicName)
                        .error()
                        .build());
            }
            isSameIdExists = true;
        }
        return isSameIdExists;
    }

    /**
     * Check if a module with the given symbolic name and the version exists.<br />
     * This method is called when there the forceUpdate flag is set to false during installation
     * @param templatePackageRegistry the package registry
     * @param symbolicName the module symbolic name
     * @param version the module version
     * @param context the spring message context
     * @return true if the module exists otherwise return false.
     */
    public static boolean isModuleExists(TemplatePackageRegistry templatePackageRegistry, String symbolicName, String version, MessageContext context) {
        boolean isModuleExists = false;
        Set<ModuleVersion> aPackage = templatePackageRegistry.getAvailableVersionsForModule(symbolicName);
        ModuleVersion moduleVersion = new ModuleVersion(version);
        if (!moduleVersion.isSnapshot() && aPackage.contains(moduleVersion)) {
            if (context != null) {
                context.addMessage(new MessageBuilder().source("moduleExists")
                        .code("serverSettings.manageModules.install.moduleExists")
                        .args(new String[]{symbolicName, version})
                        .build());
            }
            isModuleExists = true;
        }
        return isModuleExists;
    }

    public static Manifest getJarFileManifest(File file) {
        Manifest manifest = null;
        JarInputStream jarStream = null;
        try {
            jarStream = new JarInputStream(new FileInputStream(file));
            manifest = jarStream.getManifest();
        } catch (IOException e) {
            // TODO: log error
        } finally {
            if (jarStream != null) {
                IOUtils.closeQuietly(jarStream);
            }
        }
        return manifest;
    }

    /**
     * Get the given manifest symbolic name.
     * It looks for the Bundle-SymbolicName attribute and if missing root-folder attributes is used
     * @param manifest the manifest
     * @return the manifest symbolic name
     */
    public static String getManifestSymbolicName(Manifest manifest) {
        if (manifest == null) {
            return null;
        }
        String symbolicName = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
        if (symbolicName == null) {
            symbolicName = manifest.getMainAttributes().getValue("root-folder");
        }
        return symbolicName;
    }

    /**
     * Gets the Implementation-Version attribute value from the manifest.
     * It returns null if the manifest is null
     * @param manifest the bundle manifest
     * @return the version or null if missing
     */
    public static String getManifestVersion(Manifest manifest) {
        if (manifest != null) {
            return manifest.getMainAttributes().getValue("Implementation-Version");
        }
        return null;
    }

    /**
     * Gets the manifest Jahia-GroupId attribute.
     * It returns null if the manifest is null
     * @param manifest the target manifest
     * @return the Jahia group id attribute value or null if empty
     */
    public static String getManifestGroupId(Manifest manifest) {
        if (manifest != null) {
            return manifest.getMainAttributes().getValue("Jahia-GroupId");
        }
        return null;
    }

}
