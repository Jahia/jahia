package org.jahia.bin;

import java.util.Set;

/**
 * Core representation of the edit configuration
 */
public interface EditConfiguration {

    String getName();

    Set<String> getBypassModeForTypes();

    String getNodeCheckPermission();

    String getRequiredPermission();

    String getDefaultUrlMapping();

    String getDefaultLocation();

    Set<String> getSkipMainModuleTypesDomParsing();

    Set<String> getEditableTypes();

    Set<String> getNonEditableTypes();

    Set<String> getVisibleTypes();

    Set<String> getNonVisibleTypes();

    boolean isForceHeaders();

}
