package org.jahia.services.content.interceptor;

import org.jahia.params.ParamBean;
import org.jahia.services.sites.JahiaSite;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.test.TestHelper;
import org.jahia.bin.Jahia;
import org.jahia.data.JahiaData;

import javax.jcr.RepositoryException;
import javax.jcr.NodeIterator;
import javax.jcr.nodetype.ConstraintViolationException;
import java.util.Locale;

import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: toto
 * Date: Nov 30, 2009
 * Time: 5:30:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class URLInterceptorTest extends TestCase {
    private ParamBean paramBean;
    private JahiaSite site;
    private JCRSessionWrapper session;
    private JCRSessionWrapper localizedSession;
    private JCRNodeWrapper node;

    @Override
    protected void setUp() throws Exception {
        site = TestHelper.createSite("test");

        paramBean = (ParamBean) Jahia.getThreadParamBean();

        paramBean.getSession(true).setAttribute(ParamBean.SESSION_SITE, site);

        JahiaData jData = new JahiaData(paramBean, false);
        paramBean.setAttribute(JahiaData.JAHIA_DATA, jData);

        session = JCRSessionFactory.getInstance().getCurrentUserSession();
        localizedSession = JCRSessionFactory.getInstance().getCurrentUserSession(null, Locale.ENGLISH);

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

    @Override
    protected void tearDown() throws Exception {
        TestHelper.deleteSite("test");
        node.remove();
        session.save();
    }

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

        valideEncoding("<img src=\"" + Jahia.getContextPath() + "/files/shared/refContent\">",
                "<img src=\"##doc-context##/##ref:link1##\">", 1);


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
        validateEncodeAndDecode("<img src=\"" + Jahia.getContextPath() + "/files/shared/refContent\">");
    }

    private void validateEncodeAndDecode(String value) throws RepositoryException {
        JCRNodeWrapper n = localizedSession.getNode("/shared/testContent");
        n.setProperty("body", value);
        assertEquals("Not the same value after get",value, n.getProperty("body").getString());
    }





}
