package org.jahia.bundles.url.jahiawar.internal;

import org.jahia.bundles.url.jahiawar.ServiceConstants;
import org.ops4j.lang.NullArgumentException;
import org.ops4j.util.property.PropertyResolver;
import org.ops4j.util.property.PropertyStore;

import java.util.ArrayList;
import java.util.List;

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
    public List<String> getImportedPackages() {
        if( !contains( ServiceConstants.PROPERTY_IMPORTED_PACKAGED ) )
        {
            String importedPackagePropValue = propertyResolver.get(ServiceConstants.PROPERTY_IMPORTED_PACKAGED);
            if (importedPackagePropValue != null) {
                String[] importPackagesArray = importedPackagePropValue.split(",");
                List<String> importedPackages = new ArrayList<String>();
                for (String importedPackage : importPackagesArray) {
                    importedPackages.add(importedPackage.trim());
                }
                return set(ServiceConstants.PROPERTY_IMPORTED_PACKAGED, importedPackages);
            } else {
                return set(ServiceConstants.PROPERTY_IMPORTED_PACKAGED, new ArrayList());
            }

        }
        return get( ServiceConstants.PROPERTY_IMPORTED_PACKAGED );
    }

}