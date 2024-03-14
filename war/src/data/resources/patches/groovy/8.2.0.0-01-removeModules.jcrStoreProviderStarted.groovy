import org.jahia.services.modulemanager.persistence.jcr.BundleInfoJcrHelper

import java.util.stream.Collectors

BundleInfoJcrHelper.storePersistentStates(
        BundleInfoJcrHelper.getPersistentStates()
                .stream()
                .filter { bpi ->
                    !(bpi.getSymbolicName() in [
                            "content-editor",
                            "jahia-category-manager",
                            "clustering-tools",
                            "sdl-generator-tools"
                    ])
                }
                .collect(Collectors.toList())
);
