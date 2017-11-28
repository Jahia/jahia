package org.jahia.services.modulemanager.impl;

import java.util.Collection;

import org.codehaus.plexus.util.ExceptionUtils;
import org.jahia.services.modulemanager.ModuleManagementException;
import org.jahia.settings.readonlymode.ReadOnlyModeException;
import org.springframework.core.io.Resource;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class ModuleManagerImplTest {

    private static ModuleManagerImpl moduleManager;

    @BeforeClass
    public static void oneTimeSetup() {
        moduleManager = new ModuleManagerImpl();
    }

    @Test
    public void moduleInstallShouldFailInReadOnlyMode() {

        verifyFailureInReadOnlyMode(new Runnable() {

            @Override
            public void run() {
                moduleManager.install((Collection<Resource>) null, null, false);
            }
        });
    }

    @Test
    public void moduleUninstallShouldFailInReadOnlyMode() {

        verifyFailureInReadOnlyMode(new Runnable() {

            @Override
            public void run() {
                moduleManager.uninstall("bundleKey", null);
            }
        });
    }

    @Test
    public void moduleStartShouldFailInReadOnlyMode() {

        verifyFailureInReadOnlyMode(new Runnable() {

            @Override
            public void run() {
                moduleManager.start("bundleKey", null);
            }
        });
    }

    @Test
    public void moduleStopShouldFailInReadOnlyMode() {

        verifyFailureInReadOnlyMode(new Runnable() {

            @Override
            public void run() {
                moduleManager.stop("bundleKey", null);
            }
        });
    }

    private void verifyFailureInReadOnlyMode(Runnable action) {
        moduleManager.switchReadOnlyMode(true);
        try {
            action.run();
            Assert.fail("The action should have failed due to read only mode");
        } catch (ModuleManagementException e) {
            if (!(ExceptionUtils.getRootCause(e) instanceof ReadOnlyModeException)) {
                Assert.fail("The action should have failed due to read only mode");
            }
        } finally {
            moduleManager.switchReadOnlyMode(false);
        }
    }
}