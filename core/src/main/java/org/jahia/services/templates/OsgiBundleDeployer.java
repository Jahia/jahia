package org.jahia.services.templates;

import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.osgi.http.bridge.ProvisionActivator;
import org.jahia.services.content.JCRSessionWrapper;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import java.io.File;

public class OsgiBundleDeployer extends TemplatePackageDeployer {

    private static Logger logger = LoggerFactory.getLogger(OsgiBundleDeployer.class);

    @Override
    public void startWatchdog() {
        //
    }

    @Override
    public void stopWatchdog() {
        //
    }

    @Override
    public void scanNow() {
        //
    }

    @Override
    public void deployAndRegisterTemplatePackages() {
        //
    }

    @Override
    public JahiaTemplatesPackage getPackage(File templateDir) {
        return super.getPackage(templateDir);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public JahiaTemplatesPackage deployModule(File warFile, JCRSessionWrapper session) throws RepositoryException {
        try {
            String location = warFile.toURI().toString();
            if (warFile.getName().toLowerCase().endsWith(".war")) {
                location = "jahiawar:" + location;
            }
            Bundle bundle = ProvisionActivator.getInstance().getBundleContext().installBundle(location);
            bundle.update();
            bundle.start();
            String moduleName = (String) bundle.getHeaders().get("root-folder");
            if (moduleName == null) {
                moduleName = bundle.getSymbolicName();
            }
            String version = (String) bundle.getHeaders().get("Implementation-Version");
            if (version == null) {
                version = bundle.getVersion().toString();
            }
            return service.getTemplatePackageRegistry().lookupByFileNameAndVersion(moduleName, new ModuleVersion(version));
        } catch (BundleException e) {
            logger.error("Cannot deploy module",e);
        }

        return null;
    }

    @Override
    public void undeployModule(JahiaTemplatesPackage pack, JCRSessionWrapper session, boolean keepWarFile) throws RepositoryException {
        Bundle[] bundles = ProvisionActivator.getInstance().getBundleContext().getBundles();
        for (Bundle bundle : bundles) {
            if (bundle.getHeaders().get("root-folder") != null) {
                String moduleName = bundle.getHeaders().get("root-folder").toString();
                if (moduleName == null) {
                    moduleName = bundle.getSymbolicName();
                }
                String version = (String) bundle.getHeaders().get("Implementation-Version");
                if (version == null) {
                    version = bundle.getVersion().toString();
                }
                if (moduleName.equals(pack.getRootFolder()) && version.equals(pack.getVersion().toString())) {
                    try {
                        bundle.uninstall();
                        return;
                    } catch (BundleException e) {
                        logger.error("Cannot undeploy module", e);
                    }
                }
            }
        }
        //
    }

}
