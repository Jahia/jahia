/*
 * ==========================================================================================
 * =                            JAHIA'S ENTERPRISE DISTRIBUTION                             =
 * ==========================================================================================
 *
 *                                  http://www.jahia.com
 *
 * JAHIA'S ENTERPRISE DISTRIBUTIONS LICENSING - IMPORTANT INFORMATION
 * ==========================================================================================
 *
 *     Copyright (C) 2002-2019 Jahia Solutions Group. All rights reserved.
 *
 *     This file is part of a Jahia's Enterprise Distribution.
 *
 *     Jahia's Enterprise Distributions must be used in accordance with the terms
 *     contained in the Jahia Solutions Group Terms &amp; Conditions as well as
 *     the Jahia Sustainable Enterprise License (JSEL).
 *
 *     For questions regarding licensing, support, production usage...
 *     please contact our team at sales@jahia.com or go to http://www.jahia.com/license.
 *
 * ==========================================================================================
 */
package org.jahia.bundles.modules;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.jahia.services.modulemanager.BundlePersistentInfo;
import org.jahia.services.modulemanager.ModuleManager;

import java.util.Collection;

/**
 * Short description of the class
 *
 * @author yousria
 */

@Command(
        scope = "bundle",
        name = "apply-bundle-states",
        description = "Output DX modules with their persistent state and version, and save those information in the JCR"
)
@Service
public class ApplyBundlesPersistentState implements Action {

    @Reference
    private ModuleManager moduleManager;

    @Override
    public Object execute() throws Exception {
        moduleManager.applyBundlesPersistentStates();
        return null;
    }

    public void setModuleManager(ModuleManager moduleManager) {
        this.moduleManager = moduleManager;
    }
}
