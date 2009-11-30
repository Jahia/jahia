package org.jahia.services.render.filter;

import org.jahia.params.ParamBean;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.render.RenderContext;
import org.jahia.services.render.Resource;
import org.jahia.services.render.RenderService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaAdminUser;
import org.jahia.bin.Jahia;
import org.jahia.data.JahiaData;
import org.jahia.test.TestHelper;

import java.io.IOException;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import javax.jcr.RepositoryException;

/**
 * Unit test for the {@link WrapperFilter} 
 * User: toto
 * Date: Nov 26, 2009
 * Time: 12:57:51 PM
 */
public class WrapperFilterTest extends TestCase {
    private ParamBean paramBean;
    private JahiaSite site;
    private JCRSessionWrapper session;
    private JCRNodeWrapper node;

    @Override
    protected void setUp() throws Exception {
        site = TestHelper.createSite("test");

        paramBean = (ParamBean) Jahia.getThreadParamBean();

        paramBean.getSession(true).setAttribute(ParamBean.SESSION_SITE, site);

        JahiaData jData = new JahiaData(paramBean, false);
        paramBean.setAttribute(JahiaData.JAHIA_DATA, jData);

        session = JCRSessionFactory.getInstance().getCurrentUserSession();

        JCRNodeWrapper shared = session.getNode("/content/shared");

        if (!shared.isCheckedOut()) {
            shared.checkout();
        }

        if (shared.hasNode("testContent")) {
            shared.getNode("testContent").remove();
        }
        node = shared.addNode("testContent", "jnt:mainContent");

        session.save();
    }

    @Override
    protected void tearDown() throws Exception {
        TestHelper.deleteSite("test");
        node.remove();
        session.save();
    }

    public void testFullpageWrapper() throws Exception {

        JahiaUser admin = JahiaAdminUser.getAdminUser(0);

        RenderContext context = new RenderContext(paramBean.getRequest(), paramBean.getResponse(), admin);
        context.setSite(site);
        Resource resource = new Resource(node, "html", null,null);
        context.setMainResource(resource);

        resource.pushWrapper("wrapper.fullpage");

        RenderChain chain = new RenderChain();
        AttributesFilter attributesFilter = new AttributesFilter();
        attributesFilter.setRenderService(RenderService.getInstance());
        chain.addFilter(attributesFilter);

        WrapperFilter filter = new WrapperFilter();
        filter.setRenderService(RenderService.getInstance());
        chain.addFilter(filter);

        chain.addFilter(new AbstractFilter() {
            public String execute(RenderContext renderContext, Resource resource, RenderChain chain) throws IOException, RepositoryException {
                return "test";
            }
        });
        String result = chain.doFilter(context,resource);

        assertTrue("Cannot find <body> tag", Pattern.compile("body").matcher(result).find());
        assertTrue("Cannot find test content", Pattern.compile("test").matcher(result).find());
    }

}

