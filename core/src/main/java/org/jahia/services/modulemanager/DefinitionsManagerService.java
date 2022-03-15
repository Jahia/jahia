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
 *     Copyright (C) 2002-2022 Jahia Solutions Group. All rights reserved.
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
package org.jahia.services.modulemanager;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.osgi.BundleResource;
import org.jahia.services.content.JCRStoreService;
import org.jahia.services.content.nodetypes.ExtendedNodeType;
import org.jahia.services.content.nodetypes.JahiaCndReader;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.nodetypes.ParseException;
import org.jahia.services.modulemanager.impl.DefinitionsBundleChecker;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.templates.ModuleVersion;
import org.jahia.settings.SettingsBean;
import org.ops4j.pax.swissbox.extender.BundleURLScanner;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * OSGI service for CND definitions
 */
public class DefinitionsManagerService {

    private static final Logger logger = LoggerFactory.getLogger(DefinitionsManagerService.class);
    private static final BundleURLScanner CND_SCANNER = new BundleURLScanner("META-INF", "*.cnd", false);

    private JahiaTemplateManagerService templateManagerService;
    private JCRStoreService jcrStoreService;
    public static final String SKIP_VALIDATION_PROP_KEY = "moduleManager.skipModuleDefinitionValidation";

    public enum CND_STATUS {OK, MODIFIED}


    public CND_STATUS checkDefinition(String moduleId) throws IOException, RepositoryException {
        JahiaTemplatesPackage module = templateManagerService.getTemplatePackageRegistry().lookupById(moduleId);
        if (module == null) {
            throw new RepositoryException(moduleId + " module not found");
        }
        Bundle bundle = module.getBundle();
        List<URL> urls = CND_SCANNER.scan(bundle);
        for (URL url : urls) {
            JahiaCndReader parser = parse(url, bundle);
            for (ExtendedNodeType newNt : parser.getNodeTypesList()) {
                CND_STATUS status = checkNodeTypeDefinition(newNt);
                if (status != CND_STATUS.OK) {
                    return status;
                }
            }
        }
        return CND_STATUS.OK;
    }

    private JahiaCndReader parse(URL url, Bundle bundle) throws IOException {
        Resource resource = new BundleResource(url, bundle);
        String systemId = bundle.getSymbolicName();
        try (Reader resourceReader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            JahiaCndReader r = new JahiaCndReader(resourceReader, resource.toString(), systemId, NodeTypeRegistry.getInstance());
            r.setDoCheckConsistency(false);
            try {
                r.parse();
            } catch (ParseException e) {
                logger.error("Error", e);
            }
            return r;
        }
    }

    private CND_STATUS checkNodeTypeDefinition(ExtendedNodeType newNt) throws NoSuchNodeTypeException {
        NodeTypeRegistry registry = NodeTypeRegistry.getInstance();
        if (registry.hasNodeType(newNt.getName())) {
            // node type already exist, do check diffs
            ExtendedNodeType regNt = registry.getNodeType(newNt.getName());
            if (!regNt.getSystemId().equals(newNt.getSystemId())) {
                logger.debug("Definition {} was already deployed in {}", newNt.getName(), regNt.getSystemId());
                return CND_STATUS.MODIFIED;
            }
            DefinitionsBundleChecker.NodeTypeDefDiff diff = DefinitionsBundleChecker.NodeTypeDefDiff.create(regNt, newNt);
            if (diff.isModified()) {
                logger.debug("CND changes detected: {}", diff);
                return CND_STATUS.MODIFIED;
            }
        }
        return CND_STATUS.OK;
    }

    public boolean skipDefinitionValidation() {
        return SettingsBean.getInstance().getBoolean(SKIP_VALIDATION_PROP_KEY, false);
    }

    /**
     * @param moduleId
     * @return true if current active version of moduleId corresponds to latest registered CND definition for that given module,
     * or if module has no registered CND definitions, false otherwise.
     * Note that modification (build time) is taken into account when comparing versions and must be the same to return true.
     * @throws RepositoryException if moduleId is not found
     */
    public boolean isLatest(String moduleId) throws RepositoryException {
        JahiaTemplatesPackage module = templateManagerService
                .getTemplatePackageRegistry()
                .lookupById(moduleId);
        if (module == null) {
            throw new RepositoryException(moduleId + " module not found");
        }

        String systemId = module.getBundle().getSymbolicName();
        String latestVersion = jcrStoreService.getDefinitionVersion(systemId);
        if (latestVersion == null) {
            // module has no registered definition version; return as true
            return true;
        }
        // compare versions first before looking at last modified; last modified requires bundle scan (I/O access)
        ModuleVersion latestDeployedVersion = new ModuleVersion(latestVersion);
        if (module.getVersion().compareTo(latestDeployedVersion) != 0) {
            return false;
        }

        long lastModified = getDefLastModified(module);
        return lastModified == Long.parseLong(jcrStoreService.getDefinitionLastModified(systemId));
    }

    private long getDefLastModified(JahiaTemplatesPackage module) {
        long lastModified = 0;

        Bundle bundle = module.getBundle();
        List<URL> urls = CND_SCANNER.scan(bundle);
        for (URL url : urls) {
            BundleResource bundleResource = new BundleResource(url, bundle);
            try {
                long l = bundleResource.lastModified();
                if (l > lastModified) {
                    lastModified = l;
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return lastModified;
    }

    public void setTemplateManagerService(JahiaTemplateManagerService templateManagerService) {
        this.templateManagerService = templateManagerService;
    }

    public void setJcrStoreService(JCRStoreService jcrStoreService) {
        this.jcrStoreService = jcrStoreService;
    }

}
