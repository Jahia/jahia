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

package org.jahia.bundles.url.jahiawar.internal;

import org.jahia.bundles.url.jahiawar.ServiceConstants;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.util.property.PropertyResolver;
import org.ops4j.util.property.PropertyStore;

import java.util.*;

/**
 * Service configuration implementation
 */
public class ConfigurationImpl
    extends PropertyStore
    implements Configuration
{

    /**
     * Property resolver. Cannot be null.
     */
    private final PropertyResolver propertyResolver;

    /**
     * Creates a new service configuration.
     *
     * @param propertyResolver propertyResolver used to resolve properties; mandatory
     */
    public ConfigurationImpl( final PropertyResolver propertyResolver )
    {
        NullArgumentException.validateNotNull(propertyResolver, "Property resolver");
        this.propertyResolver = propertyResolver;
    }

    /**
     * @see Configuration#getCertificateCheck()
     */
    public Boolean getCertificateCheck()
    {
        if( !contains( ServiceConstants.PROPERTY_CERTIFICATE_CHECK ) )
        {
            return set( ServiceConstants.PROPERTY_CERTIFICATE_CHECK,
                        Boolean.valueOf(propertyResolver.get(ServiceConstants.PROPERTY_CERTIFICATE_CHECK))
            );
        }
        return get( ServiceConstants.PROPERTY_CERTIFICATE_CHECK );
    }

    @Override
    public Map<String,Set<String>> getImportedPackages() {
        if( !contains( ServiceConstants.PROPERTY_IMPORTED_PACKAGES) )
        {
            String importedPackagePropValue = propertyResolver.get(ServiceConstants.PROPERTY_IMPORTED_PACKAGES);
            if (importedPackagePropValue != null) {
                Map<String,Set<String>> importedPackages = getBundlePackageMappings(importedPackagePropValue);
                return set(ServiceConstants.PROPERTY_IMPORTED_PACKAGES, importedPackages);
            } else {
                return set(ServiceConstants.PROPERTY_IMPORTED_PACKAGES, Collections.<String,Set<String>>emptyMap());
            }
        }
        return get( ServiceConstants.PROPERTY_IMPORTED_PACKAGES);
    }

    @Override
    public Map<String,Set<String>> getExcludedImportPackages() {
        if( !contains( ServiceConstants.PROPERTY_EXCLUDED_IMPORT_PACKAGES) )
        {
            String excludedImportPackagesPropValue = propertyResolver.get(ServiceConstants.PROPERTY_EXCLUDED_IMPORT_PACKAGES);
            if (excludedImportPackagesPropValue != null) {
                Map<String,Set<String>> excludedImportPackages = getBundlePackageMappings(excludedImportPackagesPropValue);
                return set(ServiceConstants.PROPERTY_EXCLUDED_IMPORT_PACKAGES, excludedImportPackages);
            } else {
                return set(ServiceConstants.PROPERTY_EXCLUDED_IMPORT_PACKAGES, Collections.<String,Set<String>>emptyMap());
            }
        }
        return get( ServiceConstants.PROPERTY_EXCLUDED_IMPORT_PACKAGES);
    }

    @Override
    public Map<String, Set<String>> getExcludedExportPackages() {
        if( !contains( ServiceConstants.PROPERTY_EXCLUDED_EXPORT_PACKAGES) )
        {
            String excludedExportPackagesPropValue = propertyResolver.get(ServiceConstants.PROPERTY_EXCLUDED_EXPORT_PACKAGES);
            if (excludedExportPackagesPropValue != null) {
                Map<String,Set<String>> excludedExportPackages = getBundlePackageMappings(excludedExportPackagesPropValue);
                return set(ServiceConstants.PROPERTY_EXCLUDED_EXPORT_PACKAGES, excludedExportPackages);
            } else {
                return set(ServiceConstants.PROPERTY_EXCLUDED_EXPORT_PACKAGES, Collections.<String,Set<String>>emptyMap());
            }
        }
        return get( ServiceConstants.PROPERTY_EXCLUDED_EXPORT_PACKAGES);
    }

    private Map<String,Set<String>> getBundlePackageMappings(String propertyValue) {
        Map<String,Set<String>> excludedImportPackages = new TreeMap<String,Set<String>>();
        String[] excludedImportPackageArray = propertyValue.split(",");
        for (String excludedImportPackage : excludedImportPackageArray) {
            String[] excludedImportPackageMapping = excludedImportPackage.split("=");
            String bundleName = "*";
            String bundleExcludedImportPackage = "";
            if (excludedImportPackageMapping.length == 2) {
                bundleName = excludedImportPackageMapping[0].trim();
                bundleExcludedImportPackage = excludedImportPackageMapping[1].trim();
            } else if (excludedImportPackageMapping.length == 1) {
                bundleExcludedImportPackage = excludedImportPackageMapping[0].trim();
                if (bundleExcludedImportPackage.length() == 0) {
                    // no value for the package name, we skip this entry.
                    continue;
                }
            } else {
                // no valid mapping, skip
                continue;
            }
            Set<String> bundleExcludedImportPackages = excludedImportPackages.get(bundleName);
            if (bundleExcludedImportPackages == null) {
                bundleExcludedImportPackages = new TreeSet<String>();
            }
            bundleExcludedImportPackages.add(bundleExcludedImportPackage);
            excludedImportPackages.put(bundleName, bundleExcludedImportPackages);
        }
        return excludedImportPackages;
    }

}