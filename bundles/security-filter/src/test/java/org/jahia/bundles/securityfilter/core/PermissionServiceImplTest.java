package org.jahia.bundles.securityfilter.core;

import org.jahia.api.settings.SettingsBean;
import org.jahia.bundles.securityfilter.legacy.PermissionsConfig;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Dictionary;
import java.util.Hashtable;

import static org.jahia.bundles.securityfilter.core.PermissionServiceImpl.AUTHORIZATION_DEFAULT_FILE;
import static org.jahia.bundles.securityfilter.core.PermissionServiceImpl.MANAGED_FILE_HEADER;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link PermissionServiceImpl#updated(Dictionary)}.
 *
 * <p>These tests verify:
 * <ul>
 *   <li>When {@code security.profile} is absent (commented out), the {@code default} profile is loaded.</li>
 *   <li>When {@code security.profile=off}, the authorization file is emptied with an explanatory header.</li>
 *   <li>When a valid built-in profile is specified, its YAML content is deployed with the managed-file header.</li>
 *   <li>When an unknown profile is specified, the service falls back to the {@code default} profile.</li>
 * </ul>
 */
public class PermissionServiceImplTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    private BundleContext bundleContext;

    @Mock
    private Bundle bundle;

    @Mock
    private AuthorizationConfig authorizationConfig;

    @Mock
    private PermissionsConfig permissionsConfig;

    @Mock
    private SettingsBean settingsBean;

    private PermissionServiceImpl service;
    private File authorizationDefaultFile;
    private String defaultProfileContent;

    @Before
    public void setUp() throws IOException, java.net.URISyntaxException {
        MockitoAnnotations.openMocks(this);

        // Build karaf/etc directory structure inside the temp folder
        File etcDir = temporaryFolder.newFolder("karaf", "etc");

        // Wire mocks
        when(bundleContext.getBundle()).thenReturn(bundle);
        when(settingsBean.getJahiaVarDiskPath()).thenReturn(temporaryFolder.getRoot().getAbsolutePath());

        // Point built-in profiles to the real classpath resources of the security-filter bundle
        URL defaultProfileUrl = getClass().getResource("/META-INF/configuration-profiles/profile-default.yml");
        assertNotNull(defaultProfileUrl);
        URL openProfileUrl = getClass().getResource("/META-INF/configuration-profiles/profile-open.yml");
        assertNotNull(openProfileUrl);
        URL compatProfileUrl = getClass().getResource("/META-INF/configuration-profiles/profile-compat.yml");
        assertNotNull(compatProfileUrl);

        // Default: any unknown resource returns null; specific known profiles return their URL
        // (Mockito evaluates stubs in LIFO order, so later stubs for specific strings take priority)
        when(bundle.getResource(anyString())).thenReturn(null);
        when(bundle.getResource("META-INF/configuration-profiles/profile-default.yml")).thenReturn(defaultProfileUrl);
        when(bundle.getResource("META-INF/configuration-profiles/profile-open.yml")).thenReturn(openProfileUrl);
        when(bundle.getResource("META-INF/configuration-profiles/profile-compat.yml")).thenReturn(compatProfileUrl);

        // Load the raw content of the default profile for full-content comparison in assertions
        defaultProfileContent = Files.readString(Paths.get(defaultProfileUrl.toURI()));

        // Build service under test
        service = new PermissionServiceImpl();
        service.activate(bundleContext);
        service.setAuthorizationConfig(authorizationConfig);
        service.setPermissionsConfig(permissionsConfig);
        service.setSettingsBean(settingsBean);

        authorizationDefaultFile = new File(etcDir, AUTHORIZATION_DEFAULT_FILE);
    }

    /**
     * When {@code security.profile} is not present (e.g. commented out in the .cfg file),
     * the service must fall back to the {@code default} profile and write the authorization file.
     */
    @Test
    public void GIVEN_noSecurityProfileProperty_WHEN_updated_THEN_defaultProfileIsDeployed() throws Exception {
        service.updated(new Hashtable<>());

        assertTrue("Authorization file should have been created", authorizationDefaultFile.exists());
        String content = readAuthorizationFile();
        assertTrue("File should start with the managed-file header", content.startsWith(MANAGED_FILE_HEADER));
        assertDefaultProfileContent(content, true);
    }

    /**
     * When {@code security.profile=default} is set explicitly, the default profile YAML
     * must be deployed with the managed-file header prepended.
     */
    @Test
    public void GIVEN_securityProfileDefault_WHEN_updated_THEN_defaultProfileIsDeployedWithHeader() throws Exception {
        service.updated(createProperties("default"));

        assertTrue("Authorization file should have been created", authorizationDefaultFile.exists());
        String content = readAuthorizationFile();
        assertTrue("File should start with the managed-file header", content.startsWith(MANAGED_FILE_HEADER));
        assertDefaultProfileContent(content, true);
    }

    /**
     * When {@code security.profile=open} is set, the open profile YAML
     * must be deployed with the managed-file header prepended.
     */
    @Test
    public void GIVEN_securityProfileOpen_WHEN_updated_THEN_openProfileIsDeployedWithHeader() throws Exception {
        service.updated(createProperties("open"));

        assertTrue("Authorization file should have been created", authorizationDefaultFile.exists());
        String content = readAuthorizationFile();
        assertTrue("File should start with the managed-file header", content.startsWith(MANAGED_FILE_HEADER));
        assertDefaultProfileContent(content, false);
    }

    /**
     * When {@code security.profile=off} is set, the authorization file must be written
     * with only the managed-file header and a comment explaining that no profile is active.
     * The file must not contain any actual authorization rules.
     */
    @Test
    public void GIVEN_securityProfileOff_WHEN_updated_THEN_authorizationFileIsEmptiedWithExplanatoryComment() throws Exception {
        // Pre-populate the file so we can confirm it gets emptied
        Files.write(authorizationDefaultFile.toPath(), "previous-content: true\n".getBytes());

        service.updated(createProperties("off"));

        assertTrue("Authorization file should still exist after profile=off", authorizationDefaultFile.exists());
        String content = readAuthorizationFile();
        assertTrue("File should start with the managed-file header", content.startsWith(MANAGED_FILE_HEADER));
        assertTrue("File should contain the off-profile comment", content.contains("security.profile=off"));
        assertTrue("File should contain an empty YAML mapping so the file remains parseable", content.contains("{}"));
        assertFalse("File must not contain previous authorization rules", content.contains("previous-content"));
        assertDefaultProfileContent(content, false);
    }

    /**
     * When an unknown/invalid profile name is given, the service must fall back to
     * the {@code default} profile and log an error (no exception thrown to the caller).
     */
    @Test
    public void GIVEN_unknownSecurityProfile_WHEN_updated_THEN_fallsBackToDefaultProfile() throws Exception {
        service.updated(createProperties("nonexistent-profile"));

        assertTrue("Authorization file should have been created with fallback profile", authorizationDefaultFile.exists());
        String content = readAuthorizationFile();
        assertTrue("File should start with the managed-file header", content.startsWith(MANAGED_FILE_HEADER));
        assertDefaultProfileContent(content, true);
    }

    /**
     * Calling {@code updated(null)} must not throw an exception and must deploy the default profile.
     */
    @Test
    public void GIVEN_nullProperties_WHEN_updated_THEN_defaultProfileIsDeployed() throws Exception {
        service.updated(null);

        assertTrue("Authorization file should have been created", authorizationDefaultFile.exists());
        String content = readAuthorizationFile();
        assertTrue("File should start with the managed-file header", content.startsWith(MANAGED_FILE_HEADER));
        assertDefaultProfileContent(content, true);
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private Dictionary<String, Object> createProperties(String profile) {
        Dictionary<String, Object> dictionary = new Hashtable<>();
        dictionary.put("security.profile", profile);
        return dictionary;
    }

    private String readAuthorizationFile() throws IOException {
        return new String(Files.readAllBytes(authorizationDefaultFile.toPath()));
    }

    /**
     * Asserts whether the authorization file contains (or does not contain) the full content
     * of the default built-in security profile ({@code profile-default.yml}).
     *
     * @param content         the file content to inspect
     * @param expectedPresent {@code true} if the default profile rules are expected to be present,
     *                        {@code false} if they must be absent
     */
    private void assertDefaultProfileContent(String content, boolean expectedPresent) {

        if (expectedPresent) {
            assertTrue("File should contain the full content of profile-default.yml", content.contains(defaultProfileContent));
        } else {
            assertFalse("File must NOT contain the content of profile-default.yml", content.contains(defaultProfileContent));
        }
    }

}
