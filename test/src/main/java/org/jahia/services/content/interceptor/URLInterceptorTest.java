package org.jahia.services.content.interceptor;

import static junit.framework.Assert.*;

import org.jahia.params.ParamBean;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.test.TestHelper;
import org.jahia.bin.Jahia;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jcr.RepositoryException;
import javax.jcr.NodeIterator;
import javax.jcr.nodetype.ConstraintViolationException;
import java.util.Locale;

/**
 * Test case for the {@link URLInterceptor}.
 * User: toto
 * Date: Nov 30, 2009
 * Time: 5:30:59 PM
 */
public class URLInterceptorTest {
    private static ParamBean paramBean;
    private static JahiaSite site;
    private static JCRSessionWrapper session;
    private static JCRSessionWrapper localizedSession;
    private static JCRNodeWrapper node;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        site = TestHelper.createSite("test");

        paramBean = (ParamBean) Jahia.getThreadParamBean();

        paramBean.getSession(true).setAttribute(ParamBean.SESSION_SITE, site);

        /*
        JahiaData jData = new JahiaData(paramBean, false);
        paramBean.setAttribute(JahiaData.JAHIA_DATA, jData);
        */
        
        session = JCRSessionFactory.getInstance().getCurrentUserSession();
        localizedSession = JCRSessionFactory.getInstance().getCurrentUserSession(null, Locale.ENGLISH);
    }

    @Before
    public void setUp() throws RepositoryException {

        JCRNodeWrapper shared = session.getNode("/shared");
        if (shared.hasNode("testContent")) {
            shared.getNode("testContent").remove();
        }
        if (shared.hasNode("refContent")) {
            shared.getNode("refContent").remove();
        }
        if (!shared.isCheckedOut()) {
            shared.checkout();
        }

        node = shared.addNode("testContent", "jnt:mainContent");
        node = shared.addNode("refContent", "jnt:mainContent");

        session.save();
    }
    
    @After
    public void tearDown() throws Exception {
        node.remove();
        session.save();
    }
    
    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        TestHelper.deleteSite("test");
        session.save();
    }

    @Test
    public void testReferenceEncoding() throws Exception {
        valideEncoding("<a href=\"" + Jahia.getContextPath() + "/cms/render/default/en/shared/refContent.html\">test</a>",
                "<a href=\"##cms-context##/render/default/en/##ref:link1##.html\">test</a>", 1);
        valideEncoding("<a href=\"" + Jahia.getContextPath() + "/cms/edit/default/en/shared/refContent.html\">test</a>",
                "<a href=\"##cms-context##/edit/default/en/##ref:link1##.html\">test</a>", 1);
        valideEncoding("<a href=\"" + Jahia.getContextPath() + "/cms/render/live/fr/shared/refContent.html\">test</a>",
                "<a href=\"##cms-context##/render/live/fr/##ref:link1##.html\">test</a>", 1);
        valideEncoding("<a href=\"" + Jahia.getContextPath() + "/cms/render/live/{lang}/shared/refContent.html\">test</a>",
                "<a href=\"##cms-context##/render/live/{lang}/##ref:link1##.html\">test</a>", 1);
        valideEncoding("<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/en/shared/refContent.html\">test</a>",
                "<a href=\"##cms-context##/{mode}/en/##ref:link1##.html\">test</a>", 1);
        valideEncoding("<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/{lang}/shared/refContent.html\">test</a>",
                "<a href=\"##cms-context##/{mode}/{lang}/##ref:link1##.html\">test</a>", 1);
        valideEncoding("<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/{lang}/shared/refContent.html\">test</a>" +
                "<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/{lang}/shared/refContent.html\">test</a>",
                "<a href=\"##cms-context##/{mode}/{lang}/##ref:link1##.html\">test</a>"+
                        "<a href=\"##cms-context##/{mode}/{lang}/##ref:link1##.html\">test</a>", 1);
        valideEncoding("<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/{lang}/shared/refContent.html\">test</a>" +
                "<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/{lang}/shared/testContent.html\">test</a>",
                "<a href=\"##cms-context##/{mode}/{lang}/##ref:link1##.html\">test</a>" +
                        "<a href=\"##cms-context##/{mode}/{lang}/##ref:link2##.html\">test</a>", 2);

        valideEncoding("<img src=\"" + Jahia.getContextPath() + "/files/default/shared/refContent\">",
                "<img src=\"##doc-context##/default/##ref:link1##\">", 1);


    }

    private void valideEncoding(String value, String expectedValue, int expectedRefSize) throws RepositoryException {
        JCRNodeWrapper n = localizedSession.getNode("/shared/testContent");
        n.setProperty("body", value);
        localizedSession.save();

        JCRNodeWrapper nodeCheck = session.getNode("/shared/testContent/j:translation");
        assertEquals("Invalid encoded value", expectedValue, nodeCheck.getProperty("body_en").getString());
        NodeIterator ni = nodeCheck.getParent().getNodes("j:referenceInField");
        assertEquals("Invalid number of references", expectedRefSize,ni.getSize());
    }

    @Test
    public void testBadReferenceEncoding() throws Exception {
        JCRNodeWrapper n = localizedSession.getNode("/shared/testContent");
        try {
            String value = "<img src=\"" + Jahia.getContextPath() + "/files/shared/noNode\">";
            n.setProperty("body", value);
            fail("Did not throw exception " +value);
        } catch (ConstraintViolationException e) {
        }

        try {
            String value = "<a href=\"" + Jahia.getContextPath() + "/cms/render/live/{lang}/shared/noNode.html\">test</a>";
            n.setProperty("body", value);
            fail("Did not throw exception " +value);
        } catch (ConstraintViolationException e) {
        }
    }

    @Test
    public void testEncodeAndDecode() throws Exception {
        validateEncodeAndDecode("<a href=\"" + Jahia.getContextPath() + "/cms/render/default/en/shared/refContent.html\">test</a>");
        validateEncodeAndDecode("<a href=\"" + Jahia.getContextPath() + "/cms/edit/default/en/shared/refContent.html\">test</a>");
        validateEncodeAndDecode("<a href=\"" + Jahia.getContextPath() + "/cms/render/live/fr/shared/refContent.html\">test</a>");
        validateEncodeAndDecode("<a href=\"" + Jahia.getContextPath() + "/cms/render/live/{lang}/shared/refContent.html\">test</a>");
        validateEncodeAndDecode("<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/en/shared/refContent.html\">test</a>");
        validateEncodeAndDecode("<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/{lang}/shared/refContent.html\">test</a>");
        validateEncodeAndDecode("<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/{lang}/shared/refContent.html\">test</a>" +
                "<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/{lang}/shared/refContent.html\">test</a>");
        validateEncodeAndDecode("<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/{lang}/shared/refContent.html\">test</a>" +
                "<a href=\"" + Jahia.getContextPath() + "/cms/{mode}/{lang}/shared/testContent.html\">test</a>");
        validateEncodeAndDecode("<img src=\"" + Jahia.getContextPath() + "/files/default/shared/refContent\">");
    }

    private void validateEncodeAndDecode(String value) throws RepositoryException {
        JCRNodeWrapper n = localizedSession.getNode("/shared/testContent");
        n.setProperty("body", value);
        assertEquals("Not the same value after get",value, n.getProperty("body").getString());
    }





}
