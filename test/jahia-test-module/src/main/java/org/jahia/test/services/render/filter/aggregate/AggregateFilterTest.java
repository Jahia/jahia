package org.jahia.test.services.render.filter.aggregate;

import org.jahia.registries.ServicesRegistry;
import org.jahia.services.SpringContextSingleton;
import org.jahia.services.channels.Channel;
import org.jahia.services.channels.ChannelService;
import org.jahia.services.content.*;
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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

import static org.junit.Assert.*;

/**
 * Created by jkevan on 30/05/2016.
 */
public class AggregateFilterTest extends JahiaTestCase {
    private transient static Logger logger = org.slf4j.LoggerFactory.getLogger(AggregateFilterTest.class);

    public final static String TESTSITE_NAME = "test";
    private static List<RenderFilter> renderServiceFilters;
    private static List<RenderFilter> templatePackageRegistryFilters;
    private JCRSessionWrapper session;

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
        filters.add((RenderFilter) SpringContextSingleton.getInstance().getContext().getBean("org.jahia.services.render.filter.AggregateFilter"));
        filters.add(new OutFilter());

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

    @Test
    public void testPrepareOnMainResource() throws Exception {
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
        Map<String, Object> updatedModuleMap = (Map<String, Object>) context.getRequest().getAttribute("moduleMap");
        String key = generator.generate(mainResource, context, generator.getAttributesForKey(context, mainResource));
        String finalKey = generator.replacePlaceholdersInCacheKey(context, key);
        Stack<String> stack = (Stack<String>) context.getRequest().getAttribute(AggregateFilter.FRAGMENT_KEYS_STACK);

        assertNull(result);
        assertTrue(updatedModuleMap.size() == 4);
        assertTrue(!((Boolean) updatedModuleMap.get(AggregateFilter.AGGREGATING)));
        assertEquals(key, updatedModuleMap.get(AggregateFilter.RENDERING_KEY));
        assertEquals(finalKey, updatedModuleMap.get(AggregateFilter.RENDERING_FINAL_KEY));
        assertTrue(stack != null && stack.size() == 0);
    }

    @Test
    public void testExecuteOnMainResource() throws Exception {
        ModuleCacheProvider moduleCacheProvider = (ModuleCacheProvider) SpringContextSingleton.getInstance().getContext().getBean("ModuleCacheProvider");
        CacheKeyGenerator generator = moduleCacheProvider.getKeyGenerator();
        AggregateFilter aggregateFilter = (AggregateFilter) SpringContextSingleton.getInstance().getContext().getBean("org.jahia.services.render.filter.AggregateFilter");

        // init mainResource and render context
        Resource mainResource = new Resource(session.getNode("/sites/" + TESTSITE_NAME + "/home/testPage"), "html", null, Resource.CONFIGURATION_PAGE);
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
        context.getRequest().setAttribute(AggregateFilter.FRAGMENT_KEYS_STACK, new Stack<String>());

        // test execute on main resource
        String result = aggregateFilter.execute("MR render", context, mainResource, null);
        Map<String, Object> updatedModuleMap = (Map<String, Object>) context.getRequest().getAttribute("moduleMap");
        Stack<String> stack = (Stack<String>) context.getRequest().getAttribute(AggregateFilter.FRAGMENT_KEYS_STACK);

        assertTrue(result != null && result.equals("MR render"));
        assertTrue(updatedModuleMap.size() == 3);
        assertTrue(!((Boolean) updatedModuleMap.get(AggregateFilter.AGGREGATING)));
        assertEquals(key, updatedModuleMap.get(AggregateFilter.RENDERING_KEY));
        assertEquals(finalKey, updatedModuleMap.get(AggregateFilter.RENDERING_FINAL_KEY));
        assertTrue(stack != null && stack.size() == 0);
    }

    // TODO: test that a module render out of a page rendering do nothing
    // TODO: test prepare on sub fragments and esi:tag
    // TODO: test execute on sub fragments and esi:tag
    // TODO: test finalize
    // TODO: test simple aggregation
    // TODO: test key stack and fragments including them selfs

    @Test
    public void testFullAggregation() throws Exception{
        JCRNodeWrapper rootNode = session.getNode("/sites/" + TESTSITE_NAME + "/home/testPage");
        Resource resource = new Resource(rootNode, "html", null, Resource.CONFIGURATION_PAGE);
        RenderContext context = mockRenderContext(resource, resource.getNode().getResolveSite());

        Map<String, Object> moduleMap = new HashMap<>();
        context.getRequest().setAttribute("moduleMap", moduleMap);

        String result = RenderService.getInstance().render(resource, context);
        // key stack should be flushed
        assertTrue(context.getRequest().getAttribute(AggregateFilter.FRAGMENT_KEYS_STACK) == null);
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

    private HttpServletRequest mockNewServletRequest() {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class[] { HttpServletRequest.class },
                new InvocationHandler() {
                    Map<String, Object> attributes = new HashMap<String, Object>();

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (method.getName().equals("setAttribute")) {
                            attributes.put((String) args[0], args[1]);
                        } if (method.getName().equals("getAttribute")) {
                            return attributes.get(args[0]);
                        } if (method.getName().equals("getParameterMap")) {
                            return new HashMap<String, String[]>();
                        } if (method.getName().equals("removeAttribute")) {
                            attributes.remove(args[0]);
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

    public static class OutFilter extends AbstractFilter {

        @Override
        public String prepare(RenderContext renderContext, Resource resource, RenderChain chain) throws Exception {
            JCRNodeIteratorWrapper iteratorWrapper = resource.getNode().getNodes();
            String out = "\nRender for fragment: " + resource.getNode().getPath();
            if(iteratorWrapper.getSize() > 0) {
                JCRNodeWrapper subNode;
                while (iteratorWrapper.hasNext()) {
                    subNode = (JCRNodeWrapper) iteratorWrapper.nextNode();
                    Resource subResource = new Resource(subNode, "html", null, Resource.CONFIGURATION_MODULE);
                    String renderedSubModule = RenderService.getInstance().render(subResource, renderContext);
                    out += renderedSubModule;
                }
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
