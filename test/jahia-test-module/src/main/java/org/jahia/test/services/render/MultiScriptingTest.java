/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
 *
 *     THIS FILE IS AVAILABLE UNDER TWO DIFFERENT LICENSES:
 *     1/GPL OR 2/JSEL
 *
 *     1/ GPL
 *     ==================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE GPL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *     2/ JSEL - Commercial and Supported Versions of the program
 *     ===================================================================================
 *
 *     IF YOU DECIDE TO CHOOSE THE JSEL LICENSE, YOU MUST COMPLY WITH THE FOLLOWING TERMS:
 *
 *     Alternatively, commercial and supported versions of the program - also known as
 *     Enterprise Distributions - must be used in accordance with the terms and conditions
 *     contained in a separate written agreement between you and Jahia Solutions Group SA.
 *
 *     If you are unsure which license is appropriate for your use,
 *     please contact the sales department at sales@jahia.com.
 */
package org.jahia.test.services.render;

import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.render.TemplateNotFoundException;
import org.jahia.services.render.scripting.Script;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.test.JahiaAdminUser;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.ModuleTestHelper;
import org.jahia.test.TestHelper;
import org.jahia.utils.ScriptEngineUtils;
import org.junit.*;

import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NoSuchNodeTypeException;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.Assert.*;

/**
 * @author Christophe Laprun
 */
public class MultiScriptingTest extends JahiaTestCase {
    private static final String TESTSITE_NAME = "multiscriptingtest";
    private static final JahiaTemplateManagerService managerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
    private static final String JNT_THYMELEAF_NODE = "jnt:thymeleafNode";
    private static final String SCRIPTLANGUAGES_THYMELEAF = "scriptlanguages-thymeleaf";
    private static final String SCRIPTLANGUAGES_THYMELEAF_EXAMPLES = "scriptlanguages-thymeleaf-examples";
    private static JCRNodeWrapper page;
    private static JCRNodeWrapper home;
    private static final Map<String, String> moduleToVersion = new HashMap<>(7);

    @BeforeClass
    public static void oneTimeSetup() throws Exception {
        // deploy templates-web
        deployAndWait(TestHelper.WEB_TEMPLATES);

        final JahiaSite site = TestHelper.createSite(TESTSITE_NAME);

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        JCRNodeWrapper siteNode = session.getNode("/sites/" + site.getSiteKey());

        home = siteNode.getNode("home");
        recreateAndPublishTestPage(session);
    }

    private static void recreateAndPublishTestPage(JCRSessionWrapper session) throws RepositoryException {
        final String test = "test";
        if (home.hasNode(test)) {
            home.getNode(test).remove();
        }
        page = home.addNode(test, "jnt:page");
        page.setProperty("jcr:title", test);
        page.setProperty("j:templateName", "simple");
        session.save();

        publish(home.getIdentifier());
    }

    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        TestHelper.deleteSite(TESTSITE_NAME);
        undeployAndWait(TestHelper.WEB_TEMPLATES);
    }

    @Before
    public void setUp() throws RepositoryException {
        recreateAndPublishTestPage(JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH));
    }

    @After
    public void tearDown() throws RepositoryException {
        undeployAndWait(SCRIPTLANGUAGES_THYMELEAF);
        undeployAndWait(SCRIPTLANGUAGES_THYMELEAF_EXAMPLES);
    }

    @Test
    public void deployingNewScriptFactoryModuleShouldMakeItAvailable() throws RepositoryException {
        deployAndWait(SCRIPTLANGUAGES_THYMELEAF);

        try {
            final ScriptEngine thymeleaf = ScriptEngineUtils.getInstance().getEngineByName("thymeleaf");
            assertNotNull(thymeleaf);
            assertEquals("thymeleaf", thymeleaf.getFactory().getEngineName().toLowerCase());
        } catch (ScriptException e) {
            fail(e.getLocalizedMessage());
        }

        undeployAndWait(SCRIPTLANGUAGES_THYMELEAF);
    }

    @Test
    public void deployingViewsWithoutFactoryShouldNotWork() throws TemplateNotFoundException, RepositoryException {
        // deploy factory and views and publish page with new view
        final ModuleAndPath moduleAndPath = getModuleAndPath();
        final JahiaTemplatesPackage jahiaTemplatesPackage = moduleAndPath.module;

        // now undeploy factory, view should not be resolved anymore
        managerService.undeployModule(jahiaTemplatesPackage);

        // view should be now be coming from default
        final Script script = resolveScript(moduleAndPath.path);
        assertEquals("default", script.getView().getModule().getId());
        assertEquals("base.jsp", script.getView().getDisplayName());
    }

    @Test
    public void deployingShouldMakeNewViewsAvailable() throws RepositoryException, TemplateNotFoundException {
        getModuleAndPath();
    }

    private ModuleAndPath getModuleAndPath() throws RepositoryException, TemplateNotFoundException {
        JahiaTemplatesPackage factoryModule = deployAndWait(SCRIPTLANGUAGES_THYMELEAF);
        JahiaTemplatesPackage viewModule = deployAndWait(SCRIPTLANGUAGES_THYMELEAF_EXAMPLES);

        try {
            NodeTypeRegistry.getInstance().getNodeType(JNT_THYMELEAF_NODE);
        } catch (NoSuchNodeTypeException e) {
            fail("Definition not registered");
        }
        assertTrue("Module view is not correctly registered", managerService.getModulesWithViewsForComponent(JNT_THYMELEAF_NODE).contains(viewModule));

        // add thymeleaf node
        JCRSessionWrapper editSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        JCRNodeWrapper thymeleafNode = page.addNode("thymeleafNode", JNT_THYMELEAF_NODE);
        thymeleafNode.setProperty("jcr:title", "thymeleafNode");
        editSession.save();

        // and publish the page
        final String parentIdentifier = page.getIdentifier();
        publish(parentIdentifier);

        final String path = thymeleafNode.getPath();
        final Script script = resolveScript(path);

        // check that the view comes from the example module
        assertEquals(SCRIPTLANGUAGES_THYMELEAF_EXAMPLES, script.getView().getModule().getId());

        return new ModuleAndPath(factoryModule, path);
    }

    private Script resolveScript(String path) throws RepositoryException, TemplateNotFoundException {
        JCRSessionWrapper liveSession = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.LIVE_WORKSPACE, Locale.ENGLISH);
        liveSession.refresh(false);
        final JahiaUser user = liveSession.getUser();
        JCRNodeWrapper node = liveSession.getNode(path);

        RenderContext context = new RenderContext(getRequest(), getResponse(), user);
        context.setSite(node.getResolveSite());
        context.setServletPath("/render");

        Resource resource = new Resource(node, "html", null, Resource.CONFIGURATION_PAGE);
        context.setMainResource(resource);
        context.setWorkspace(liveSession.getWorkspace().getName());

        return RenderService.getInstance().resolveScript(resource, context);
    }

    private static void publish(String nodeId) throws RepositoryException {
        JCRPublicationService.getInstance().publishByMainId(nodeId, Constants.EDIT_WORKSPACE,
                Constants.LIVE_WORKSPACE, new LinkedHashSet<>(Collections.singletonList(Locale.ENGLISH.toString())),
                true, Collections.<String>emptyList());

        waitFor(500);
    }

    private static JahiaTemplatesPackage deployAndWait(String moduleName) throws RepositoryException {
        return deployAndWait(moduleName, null);
    }

    private static JahiaTemplatesPackage deployAndWait(final String moduleName, final String version) throws RepositoryException {
        final JahiaTemplatesPackage module = JCRTemplate.getInstance().doExecuteWithSystemSession(new JCRCallback<JahiaTemplatesPackage>() {
            @Override
            public JahiaTemplatesPackage doInJCR(JCRSessionWrapper session) throws RepositoryException {
                JahiaTemplatesPackage module = null;
                try {
                    File moduleFile = ModuleTestHelper.getModuleFromMaven("org.jahia.modules", moduleName, version);
                    module = managerService.deployModule(moduleFile, session);
                    moduleToVersion.put(moduleName, module.getVersion().toString());
                } catch (IOException e) {
                    fail(e.toString());
                }

                return module;
            }
        });

        assertNotNull(module);

        waitFor(500);

        return module;
    }

    private static void undeployAndWait(String moduleName) throws RepositoryException {
        managerService.undeployModule(moduleName, moduleToVersion.get(moduleName));

        waitFor(500);
    }

    private static void waitFor(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            fail(e.getLocalizedMessage());
        }
    }


    private static class ModuleAndPath {
        private final JahiaTemplatesPackage module;
        private final String path;

        public ModuleAndPath(JahiaTemplatesPackage module, String path) {
            this.module = module;
            this.path = path;
        }
    }
}
