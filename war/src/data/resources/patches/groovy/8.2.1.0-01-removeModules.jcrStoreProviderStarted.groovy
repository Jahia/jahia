import org.jahia.services.modulemanager.persistence.jcr.BundleInfoJcrHelper

import java.util.stream.Collectors

BundleInfoJcrHelper.storePersistentStates(
        BundleInfoJcrHelper.getPersistentStates()
                .stream()
                .filter { bpi ->
                    !(bpi.getSymbolicName() in [
                            "npm-modules-engine"
                    ])
                }
                .collect(Collectors.toList())
);
