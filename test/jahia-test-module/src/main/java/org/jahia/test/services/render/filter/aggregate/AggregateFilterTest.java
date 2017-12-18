/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2018 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.render.filter.aggregate;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.channels.Channel;
import org.jahia.services.channels.ChannelService;
import org.jahia.services.content.*;
import org.jahia.services.content.decorator.JCRReferenceNode;
import org.jahia.services.content.decorator.JCRSiteNode;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.RenderService;
import org.jahia.services.render.Resource;
import org.jahia.services.render.filter.AbstractFilter;
import org.jahia.services.render.filter.AggregateFilter;
import org.jahia.services.render.filter.RenderChain;
import org.jahia.services.render.filter.RenderFilter;
import org.jahia.services.render.filter.cache.CacheKeyGenerator;
import org.jahia.services.render.filter.cache.ModuleCacheProvider;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.templates.TemplatePackageRegistry;
import org.jahia.test.JahiaAdminUser;
import org.jahia.test.JahiaTestCase;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

import static org.junit.Assert.*;

/**
 * AggregateFilterTest unit test, bring unit tests for testing the new Aggregation implementation introduce in 7.2
 */
public class AggregateFilterTest extends JahiaTestCase {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(AggregateFilterTest.class);

    private static final String ESI_TAG_START = "<jahia_esi:include src=\"";
    private static final String ESI_TAG_END = "\"></jahia_esi:include>";
    public final static String TESTSITE_NAME = "test";
    private static List<RenderFilter> renderServiceFilters;
    private static List<RenderFilter> templatePackageRegistryFilters;
    private JCRSessionWrapper session;

    private OutFilter outFilter = new OutFilter();
    private int testNodesLvls = 3;
    private int testNodesChilds = 3;

    @Before
    public void setUp() throws Exception {
        JahiaSite site = TestHelper.createSite("test");

        session = JCRSessionFactory.getInstance().getCurrentUserSession("default", Locale.ENGLISH);
        session.getNode("/sites/" + site.getSiteKey());

        JCRNodeWrapper shared = session.getNode("/sites/" + site.getSiteKey() + "/home");
        if (shared.hasNode("testPage")) {
            shared.getNode("testPage").remove();
        }
        JCRNodeWrapper page = shared.addNode("testPage", "jnt:page");
        page.setProperty("jcr:title", "English test page");
        page.setProperty("j:templateName", "simple");
        createTestNodes(page, testNodesLvls, testNodesChilds);
        session.save();

        // store filters for testing unitary the AggregateFilter only
        RenderService renderService = RenderService.getInstance();
        TemplatePackageRegistry templatePackageRegistry = ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageRegistry();

        // test filters
        List<RenderFilter> filters = new LinkedList<>();
        filters.add(new BaseAttributesFilter());
        AggregateFilter aggregateFilter = (AggregateFilter) SpringContextSingleton.getInstance().getContext().getBean("org.jahia.services.render.filter.AggregateFilter");
        aggregateFilter.setDisabled(false);
        filters.add(aggregateFilter);
        filters.add(outFilter);

        // overide RenderService filters
        Field field = RenderService.class.getDeclaredField("filters");
        field.setAccessible(true);
        renderServiceFilters = new LinkedList<>((Collection<? extends RenderFilter>) field.get(renderService));
        field.set(renderService, filters);
        // overide TemplateManagerService filters
        templatePackageRegistryFilters = new LinkedList<>(templatePackageRegistry.getRenderFilters());
        templatePackageRegistry.getRenderFilters().clear();
    }

    @After
    public void tearDown() throws Exception {
        TestHelper.deleteSite("test");
        session.save();
        JCRSessionFactory.getInstance().closeAllSessions();

        // restore filters
        Field field = RenderService.class.getDeclaredField("filters");
        field.setAccessible(true);
        field.set(RenderService.getInstance(), new LinkedList<>(renderServiceFilters));
        // restore TemplateManagerService filters
        ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageRegistry().getRenderFilters().clear();
        ServicesRegistry.getInstance().getJahiaTemplateManagerService().getTemplatePackageRegistry().getRenderFilters().addAll(new LinkedList<>(templatePackageRegistryFilters));
    }

    /** main resoure rendering have to initialize the Aggregation, init the key stack and the moduleMap parameters
     *
     * @throws Exception
     */
    @Test
    public void testMainResource() throws Exception {
        ModuleCacheProvider moduleCacheProvider = (ModuleCacheProvider) SpringContextSingleton.getInstance().getContext().getBean("ModuleCacheProvider");
        CacheKeyGenerator generator = moduleCacheProvider.getKeyGenerator();
        AggregateFilter aggregateFilter = (AggregateFilter) SpringContextSingleton.getInstance().getContext().getBean("org.jahia.services.render.filter.AggregateFilter");

        // init mainResource and render context
        Resource mainResource = new Resource(session.getNode("/sites/" + TESTSITE_NAME + "/home/testPage"), "html", null, Resource.CONFIGURATION_PAGE);
        RenderContext context = mockRenderContext(mainResource, mainResource.getNode().getResolveSite());

        // module map init
        Map<String, Object> moduleMap = new HashMap<>();
        context.getRequest().setAttribute("moduleMap", moduleMap);

        // test prepare on main resource
        String result = aggregateFilter.prepare(context, mainResource, null);
        String key = generator.generate(mainResource, context, generator.getAttributesForKey(context, mainResource));
        String finalKey = generator.replacePlaceholdersInCacheKey(context, key);
        Stack<Resource> stack = (Stack<Resource>) context.getRequest().getAttribute(AggregateFilter.RESOURCES_STACK);
        moduleMap = (Map<String, Object>) context.getRequest().getAttribute("moduleMap");

        assertNull(result);
        assertTrue(moduleMap.size() == 4);
        assertTrue(!((Boolean) moduleMap.get(AggregateFilter.AGGREGATING)));
        assertEquals(key, moduleMap.get(AggregateFilter.RENDERING_KEY));
        assertEquals(finalKey, moduleMap.get(AggregateFilter.RENDERING_FINAL_KEY));
        assertTrue(stack != null && stack.size() == 0);

        // test execute
        result = aggregateFilter.execute("MR render", context, mainResource, null);
        stack = (Stack<Resource>) context.getRequest().getAttribute(AggregateFilter.RESOURCES_STACK);
        moduleMap = (Map<String, Object>) context.getRequest().getAttribute("moduleMap");

        assertTrue(result != null && result.equals("MR render"));
        assertTrue(moduleMap.size() == 4);
        assertTrue(!((Boolean) moduleMap.get(AggregateFilter.AGGREGATING)));
        assertEquals(key, moduleMap.get(AggregateFilter.RENDERING_KEY));
        assertEquals(finalKey, moduleMap.get(AggregateFilter.RENDERING_FINAL_KEY));
        assertTrue(stack.size() == 0);

        // test finalize
        aggregateFilter.finalize(context, mainResource, null);
        // stack should be flushed
        stack = (Stack<Resource>) context.getRequest().getAttribute(AggregateFilter.RESOURCES_STACK);
        assertTrue(stack == null);
    }

    /**
     * Module alone should not be catch by the AggregateFilter, to be able to start an aggregation the render chain
     * it's mandatory that the renderchain start by Resource in configuration "page"
     * @throws Exception
     */
    @Test
    public void testModuleAlone() throws Exception {
        AggregateFilter aggregateFilter = (AggregateFilter) SpringContextSingleton.getInstance().getContext().getBean("org.jahia.services.render.filter.AggregateFilter");

        // init mainResource and render context
        Resource mainResource = new Resource(session.getNode("/sites/" + TESTSITE_NAME + "/home/testPage"), "html", null, Resource.CONFIGURATION_MODULE);
        RenderContext context = mockRenderContext(mainResource, mainResource.getNode().getResolveSite());

        // module map init
        Map<String, Object> moduleMap = new HashMap<>();
        context.getRequest().setAttribute("moduleMap", moduleMap);

        // test prepare
        String result = aggregateFilter.prepare(context, mainResource, null);
        Stack<Resource> stack = (Stack<Resource>) context.getRequest().getAttribute(AggregateFilter.RESOURCES_STACK);
        moduleMap = (Map<String, Object>) context.getRequest().getAttribute("moduleMap");

        assertNull(result);
        assertNull(stack);
        assertTrue(moduleMap.size() == 0);

        // test execute
        result = aggregateFilter.execute("Module render", context, mainResource, null);
        stack = (Stack<Resource>) context.getRequest().getAttribute(AggregateFilter.RESOURCES_STACK);
        moduleMap = (Map<String, Object>) context.getRequest().getAttribute("moduleMap");

        assertNull(stack);
        assertTrue(result != null && result.equals("Module render"));
        assertTrue(moduleMap.size() == 0);

        // test finalize
        aggregateFilter.finalize(context, mainResource, null);
        // stack should be flushed
        stack = (Stack<Resource>) context.getRequest().getAttribute(AggregateFilter.RESOURCES_STACK);
        moduleMap = (Map<String, Object>) context.getRequest().getAttribute("moduleMap");
        assertTrue(stack == null);
        assertTrue(moduleMap.size() == 0);
    }

    /**
     * Sub fragment rendering should return esi:include tag
     * @throws Exception
     */
    @Test
    public void testSubFragmentOfMainResource() throws Exception {
        ModuleCacheProvider moduleCacheProvider = (ModuleCacheProvider) SpringContextSingleton.getInstance().getContext().getBean("ModuleCacheProvider");
        CacheKeyGenerator generator = moduleCacheProvider.getKeyGenerator();
        AggregateFilter aggregateFilter = (AggregateFilter) SpringContextSingleton.getInstance().getContext().getBean("org.jahia.services.render.filter.AggregateFilter");

        // init mainResource and render context
        Resource mainResource = new Resource(session.getNode("/sites/" + TESTSITE_NAME + "/home/testPage"), "html", null, Resource.CONFIGURATION_PAGE);
        Resource fragmentResource = new Resource(session.getNode("/sites/" + TESTSITE_NAME + "/home/testPage/fragment"), "html", null, Resource.CONFIGURATION_MODULE);
        RenderContext context = mockRenderContext(mainResource, mainResource.getNode().getResolveSite());

        // module map init
        String key = generator.generate(mainResource, context, generator.getAttributesForKey(context, mainResource));
        String finalKey = generator.replacePlaceholdersInCacheKey(context, key);
        Map<String, Object> moduleMap = new HashMap<>();
        moduleMap.put(AggregateFilter.RENDERING_KEY, key);
        moduleMap.put(AggregateFilter.RENDERING_FINAL_KEY, finalKey);
        moduleMap.put(AggregateFilter.AGGREGATING, false);
        context.getRequest().setAttribute("moduleMap", moduleMap);

        // init key stack
        context.getRequest().setAttribute(AggregateFilter.RESOURCES_STACK, new Stack<Resource>());

        // calculate fragment key
        String fragmentKey = generator.generate(fragmentResource, context, generator.getAttributesForKey(context, fragmentResource));
        String fragmentFinalKey = generator.replacePlaceholdersInCacheKey(context, fragmentKey);

        // prepare test, should return an esi tag include
        String result = aggregateFilter.prepare(context, fragmentResource, null);
        Stack<Resource> stack = (Stack<Resource>) context.getRequest().getAttribute(AggregateFilter.RESOURCES_STACK);
        moduleMap = (Map<String, Object>) context.getRequest().getAttribute("moduleMap");
        String expectedResult = ESI_TAG_START + fragmentKey + ESI_TAG_END;

        assertTrue(result.equals(expectedResult));
        assertTrue(moduleMap.size() == 3);
        assertTrue(!((Boolean) moduleMap.get(AggregateFilter.AGGREGATING)));
        assertEquals(key, moduleMap.get(AggregateFilter.RENDERING_KEY));
        assertEquals(finalKey, moduleMap.get(AggregateFilter.RENDERING_FINAL_KEY));
        assertTrue(stack != null && stack.size() == 0);

        // execute test
        result = aggregateFilter.execute(result, context, fragmentResource, null);
        stack = (Stack<Resource>) context.getRequest().getAttribute(AggregateFilter.RESOURCES_STACK);
        moduleMap = (Map<String, Object>) context.getRequest().getAttribute("moduleMap");

        assertTrue(result.equals(expectedResult));
        assertTrue(moduleMap.size() == 3);
        assertTrue(!((Boolean) moduleMap.get(AggregateFilter.AGGREGATING)));
        assertEquals(key, moduleMap.get(AggregateFilter.RENDERING_KEY));
        assertEquals(finalKey, moduleMap.get(AggregateFilter.RENDERING_FINAL_KEY));
        assertTrue(stack != null && stack.size() == 0);

        // finalize test
        aggregateFilter.finalize(context, fragmentResource, null);
        stack = (Stack<Resource>) context.getRequest().getAttribute(AggregateFilter.RESOURCES_STACK);
        moduleMap = (Map<String, Object>) context.getRequest().getAttribute("moduleMap");

        assertTrue(moduleMap.size() == 3);
        assertTrue(!((Boolean) moduleMap.get(AggregateFilter.AGGREGATING)));
        assertEquals(key, moduleMap.get(AggregateFilter.RENDERING_KEY));
        assertEquals(finalKey, moduleMap.get(AggregateFilter.RENDERING_FINAL_KEY));
        assertTrue(stack != null && stack.size() == 0);
    }

    /**
     * Simple aggregation of a fragment that contain a subfragment, the renderchain only contain the 3 filters from set up.
     * keys stack should be manage for this fragment
     * sub fragment will start new render chains in his own render
     * @throws Exception
     */
    @Test
    public void testSimpleAggregation() throws Exception {
        ModuleCacheProvider moduleCacheProvider = (ModuleCacheProvider) SpringContextSingleton.getInstance().getContext().getBean("ModuleCacheProvider");
        CacheKeyGenerator generator = moduleCacheProvider.getKeyGenerator();
        AggregateFilter aggregateFilter = (AggregateFilter) SpringContextSingleton.getInstance().getContext().getBean("org.jahia.services.render.filter.AggregateFilter");

        // init mainResource and render context
        Resource mainResource = new Resource(session.getNode("/sites/" + TESTSITE_NAME + "/home/testPage"), "html", null, Resource.CONFIGURATION_PAGE);
        Resource fragmentResource = new Resource(session.getNode("/sites/" + TESTSITE_NAME + "/home/testPage/fragment"), "html", null, Resource.CONFIGURATION_MODULE);
        Resource subFragmentResource = new Resource(session.getNode("/sites/" + TESTSITE_NAME + "/home/testPage/fragment/fragment"), "html", null, Resource.CONFIGURATION_MODULE);
        RenderContext context = mockRenderContext(mainResource, mainResource.getNode().getResolveSite());

        String fragmentKey = generator.generate(fragmentResource, context, generator.getAttributesForKey(context, fragmentResource));
        String fragmentFinalKey = generator.replacePlaceholdersInCacheKey(context, fragmentKey);
        String subFragmentKey = generator.generate(subFragmentResource, context, generator.getAttributesForKey(context, subFragmentResource));

        // set aggreting key in fragment resource
        fragmentResource.getModuleParams().put(AggregateFilter.AGGREGATING_KEY, fragmentKey);

        // init key stack
        context.getRequest().setAttribute(AggregateFilter.RESOURCES_STACK, new Stack<Resource>());

        // module map init
        Map<String, Object> moduleMap = new HashMap<>();
        context.getRequest().setAttribute("moduleMap", moduleMap);

        // test prepare
        String result = aggregateFilter.prepare(context, fragmentResource, null);
        Stack<Resource> stack = (Stack<Resource>) context.getRequest().getAttribute(AggregateFilter.RESOURCES_STACK);
        moduleMap = (Map<String, Object>) context.getRequest().getAttribute("moduleMap");

        // result should be null to continue render chain
        assertNull(result);
        // moduleMap should be fill with keys
        assertTrue(moduleMap.size() == 4);
        assertTrue(((Boolean) moduleMap.get(AggregateFilter.AGGREGATING)));
        assertEquals(fragmentKey, moduleMap.get(AggregateFilter.RENDERING_KEY));
        assertEquals(fragmentFinalKey, moduleMap.get(AggregateFilter.RENDERING_FINAL_KEY));
        // key stack should now contain fragment final key
        assertTrue(stack != null && stack.size() == 1);
        assertTrue(stack.get(0).equals(fragmentResource));
        // aggregating key should be removed
        assertTrue(!fragmentResource.getModuleParams().containsKey(AggregateFilter.AGGREGATING_KEY));

        // create fake content for fragment that contain already an esi:include tag
        String fragmentRender = "Render for fragment: " + fragmentResource.getNode().getPath() + "\n";
        String subfragmentRender = ESI_TAG_START + subFragmentKey + ESI_TAG_END;

        // test execute
        result = aggregateFilter.execute(fragmentRender + subfragmentRender, context, fragmentResource, null);
        moduleMap = (Map<String, Object>) context.getRequest().getAttribute("moduleMap");
        stack = (Stack<Resource>) context.getRequest().getAttribute(AggregateFilter.RESOURCES_STACK);

        // moduleMap should be fill with keys
        assertTrue(moduleMap.size() == 4);
        // key stack still contain only fragment final key
        assertTrue(stack != null && stack.size() == 1);
        assertTrue(stack.get(0).equals(fragmentResource));
        // test the result str
        assertTrue(result.substring(fragmentRender.length()).equals(getTestNodesPaths(subFragmentResource.getNode(), new StringBuilder())));

        // test finalize
        aggregateFilter.finalize(context, fragmentResource, null);
        moduleMap = (Map<String, Object>) context.getRequest().getAttribute("moduleMap");
        stack = (Stack<Resource>) context.getRequest().getAttribute(AggregateFilter.RESOURCES_STACK);

        // moduleMap should be fill with keys from the fragment resource only
        assertTrue(moduleMap.size() == 4);
        assertTrue(((Boolean) moduleMap.get(AggregateFilter.AGGREGATING)));
        assertEquals(fragmentKey, moduleMap.get(AggregateFilter.RENDERING_KEY));
        assertEquals(fragmentFinalKey, moduleMap.get(AggregateFilter.RENDERING_FINAL_KEY));
        // key stack should be empty
        assertTrue(stack != null && stack.size() == 0);
    }

    @Test
    public void testInfiteLoopRendering() throws Exception {
        try {
            outFilter.infiniteLoopRender = true;

            JCRNodeWrapper rootNode = session.getNode("/sites/" + TESTSITE_NAME + "/home/testPage");
            Resource resource = new Resource(rootNode, "html", null, Resource.CONFIGURATION_PAGE);
            RenderContext context = mockRenderContext(resource, resource.getNode().getResolveSite());

            String result = RenderService.getInstance().render(resource, context);
            // key stack should be flushed
            assertTrue(context.getRequest().getAttribute(AggregateFilter.RESOURCES_STACK) == null);
            // test that the render is good with subfragments
            assertTrue(!result.equals(getTestNodesPaths(rootNode, new StringBuilder())));
            // assert that result contain error message about recursion detected
            assertTrue(result.contains("Loop detected while rendering resource /sites/test/home/testPage/fragment/fragment.default.html. Please check your content structure and references."));
        } finally {
            outFilter.infiniteLoopRender = false;
        }
    }

    /**
     * Test that parent referenced content are well detected while rendering
     * @throws Exception
     */
    @Test
    public void testReferenceLoopRendering() throws Exception {
        try {
            JCRNodeWrapper rootNode = session.getNode("/sites/" + TESTSITE_NAME + "/home/testPage");
            // create reference
            JCRNodeWrapper refNode = rootNode.addNode("reference", "jnt:nodeLink");
            refNode.setProperty("j:node", rootNode);
            session.save();
            Resource resource = new Resource(rootNode, "html", null, Resource.CONFIGURATION_PAGE);
            RenderContext context = mockRenderContext(resource, resource.getNode().getResolveSite());

            String result = RenderService.getInstance().render(resource, context);
            // key stack should be flushed
            assertTrue(context.getRequest().getAttribute(AggregateFilter.RESOURCES_STACK) == null);
            // test that the render is good with subfragments
            assertTrue(!result.equals(getTestNodesPaths(rootNode, new StringBuilder())));
            // assert that result contain error message about recursion detected
            assertTrue(result.contains("oop detected while rendering resource /sites/test/home/testPage/reference@/testPage/reference.default.html. Please check your content structure and references."));
        } finally {
            session.getNode("/sites/" + TESTSITE_NAME + "/home/testPage/reference").remove();
            session.save();
        }
    }

    /**
     * Test a full aggregation of all the fragments nodes
     *
     * @throws Exception
     */
    @Test
    public void testFullAggregation() throws Exception{
        JCRNodeWrapper rootNode = session.getNode("/sites/" + TESTSITE_NAME + "/home/testPage");
        Resource resource = new Resource(rootNode, "html", null, Resource.CONFIGURATION_PAGE);
        RenderContext context = mockRenderContext(resource, resource.getNode().getResolveSite());

        String result = RenderService.getInstance().render(resource, context);
        // key stack should be flushed
        assertTrue(context.getRequest().getAttribute(AggregateFilter.RESOURCES_STACK) == null);
        // test that the render is good with subfragments
        assertTrue(result.equals(getTestNodesPaths(rootNode, new StringBuilder())));
    }

    private void createTestNodes(JCRNodeWrapper rootNode, int lvl, int nbofChilds) throws Exception {
        if (lvl > 0) {
            lvl --;
            for (int i = 0; i < nbofChilds; i++) {
                JCRNodeWrapper fragment = rootNode.addNode(JCRContentUtils.findAvailableNodeName(rootNode, "fragment"), "jnt:contentList");
                createTestNodes(fragment, lvl, nbofChilds);
            }
        }
    }

    private String getTestNodesPaths(JCRNodeWrapper rootNode, StringBuilder result) throws Exception {
        result.append("\nRender for fragment: ").append(rootNode.getPath());
        JCRNodeIteratorWrapper iteratorWrapper = rootNode.getNodes();
        if(iteratorWrapper.getSize() > 0) {
            JCRNodeWrapper subNode;
            while (iteratorWrapper.hasNext()) {
                subNode = (JCRNodeWrapper) iteratorWrapper.nextNode();
                getTestNodesPaths(subNode, result);
            }
        }
        return result.toString();
    }

    private HttpSession mockHttpSession() {
        return (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class[] { HttpSession.class },
                new InvocationHandler() {
                    Map<String, Object> attributes = new HashMap<String, Object>();

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.getName().equals("setAttribute")) {
                            attributes.put((String) args[0], args[1]);
                        } if (method.getName().equals("getAttribute")) {
                            return attributes.get(args[0]);
                        }
                        return null;
                    }
                });
    }

    private HttpServletRequest mockNewServletRequest() {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class[] { HttpServletRequest.class },
                new InvocationHandler() {
                    Map<String, Object> attributes = new HashMap<String, Object>();
                    HttpSession session = mockHttpSession();

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.getName().equals("setAttribute")) {
                            attributes.put((String) args[0], args[1]);
                        } else if (method.getName().equals("getAttribute")) {
                            return attributes.get(args[0]);
                        } else if (method.getName().equals("getParameterMap")) {
                            return new HashMap<String, String[]>();
                        } else if (method.getName().equals("removeAttribute")) {
                            attributes.remove(args[0]);
                        } else if (method.getName().equals("getSession")) {
                            return session;
                        } else if (method.getName().equals("getMethod")) {
                            return "GET";
                        }
                        return null;
                    }
                });
    }

    private HttpServletResponse mockNewServletResponse() {
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class[]{HttpServletResponse.class},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        return null;
                    }
                });
    }

    private RenderContext mockRenderContext(Resource mr, JCRSiteNode site) throws Exception {
        RenderContext context = new RenderContext(mockNewServletRequest(), mockNewServletResponse(), JahiaAdminUser.getAdminUser(null));
        context.setSite(site);
        context.setServletPath("/render");
        ChannelService channelService = (ChannelService) SpringContextSingleton.getInstance().getContext().getBean("ChannelService");
        context.setChannel(channelService.getChannel(Channel.GENERIC_CHANNEL));
        context.setWorkspace(session.getWorkspace().getName());
        context.setMainResource(mr);
        return context;
    }

    public static class BaseAttributesFilter extends AbstractFilter {
        @Override
        public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
            chain.pushAttribute(renderContext.getRequest(), "moduleMap", new HashMap());
            return null;
        }

        @Override
        public String getDescription() {
            return "Base attribute filter, use to instantiate new moduleMap";
        }

        @Override
        public float getPriority() {
            return 15;
        }
    }

    public static class OutFilter extends AbstractFilter {
        public boolean infiniteLoopRender = false;

        @Override
        public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
            JCRNodeIteratorWrapper iteratorWrapper = resource.getNode().getNodes();
            String out = "\nRender for fragment: " + resource.getNode().getPath();
            JCRNodeWrapper subNode;
            if(iteratorWrapper.getSize() > 0) {

                if (infiniteLoopRender && resource.getNode().getPath().equals("/sites/" + TESTSITE_NAME + "/home/testPage/fragment/fragment")) {
                    Resource sameResource = new Resource(resource.getNode(), "html", null, Resource.CONFIGURATION_MODULE);
                    out += RenderService.getInstance().render(sameResource, renderContext);
                }

                while (iteratorWrapper.hasNext()) {
                    subNode = (JCRNodeWrapper) iteratorWrapper.nextNode();

                    // start new render chain for a sub fragment that need to be part of the parent html
                    Resource subResource = new Resource(subNode, "html", null, Resource.CONFIGURATION_MODULE);

                    String renderedSubModule = RenderService.getInstance().render(subResource, renderContext);
                    out += renderedSubModule;
                }
            } else if (resource.getNode() instanceof JCRReferenceNode) {
                JCRNodeWrapper referencedNode = (JCRNodeWrapper) ((JCRReferenceNode) resource.getNode()).getNode();
                subNode = resource.getNode().getSession().getNode(resource.getNode().getPath() + "@/" + referencedNode.getName());

                // start new render chain for the referenced node
                Resource subResource = new Resource(subNode, "html", null, Resource.CONFIGURATION_MODULE);

                String renderedSubModule = RenderService.getInstance().render(subResource, renderContext);
                out += renderedSubModule;
            }
            return out;
        }

        @Override
        public String getDescription() {
            return "out filter";
        }

        @Override
        public float getPriority() {
            return 17;
        }
    }
}
