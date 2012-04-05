package org.jahia.services.content;

import com.google.common.collect.Sets;
import junit.framework.TestCase;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.jahia.utils.LanguageCodeConverters;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;

import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.Locale;

public class ReferencesTest extends TestCase {
    private static Logger logger = org.slf4j.LoggerFactory.getLogger(MultiLanguageTest.class);
    private JahiaSite site;
    private final static String TESTSITE_NAME = "jcrReferencesTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;

    @Before
    public void setUp() throws Exception {
        site = TestHelper.createSite(TESTSITE_NAME, Sets.newHashSet(Locale.ENGLISH.toString(), Locale.FRENCH.toString()), null, false);
        Assert.assertNotNull(site);
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        session.save();
    }

    @org.junit.Test
    public void testFallBackLanguage() throws Exception {
        JCRPublicationService jcrService = ServicesRegistry.getInstance()
                .getJCRPublicationService();

        JCRSessionWrapper englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        JCRNodeWrapper stageNode = englishEditSession.getNode(SITECONTENT_ROOT_NODE+"/home");

/*
[test:externalReference] > nt:base, jmix:droppableContent
- test:simpleNode (reference)
- test:multipleI18NNode (reference) multiple i18n
 */

        JCRNodeWrapper textNode1 = stageNode.addNode("text1", "jnt:text");
        JCRNodeWrapper textNode2 = stageNode.addNode("text2", "jnt:text");
        JCRNodeWrapper textNode3 = stageNode.addNode("text3", "jnt:text");
        englishEditSession.save();

        JCRNodeWrapper ref = stageNode.addNode("reference", "test:externalReference");
        ref.setProperty("test:simpleNode",textNode1);
        ref.setProperty("test:multipleI18NNode",new Value[] { englishEditSession.getValueFactory().createValue(textNode2) }) ;
        englishEditSession.save();

        JCRSessionWrapper frenchEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.FRENCH);
        JCRNodeWrapper frStageNode = frenchEditSession.getNode(SITECONTENT_ROOT_NODE + "/home");
        JCRNodeWrapper frRef = frStageNode.getNode("reference");

        frRef.setProperty("test:multipleI18NNode",new Value[] { frenchEditSession.getValueFactory().createValue(textNode3) }) ;
        frenchEditSession.save();

        checkReference(textNode1, ref, "test:simpleNode");
        checkReference(textNode2, ref, "test:multipleI18NNode");
        checkNoReference(textNode3);

        textNode1 = stageNode.getNode("text1");
        textNode2 = stageNode.getNode("text2");
        textNode3 = stageNode.getNode("text3");

        checkReference(textNode1, frRef, "test:simpleNode");
        checkNoReference(textNode2);
        checkReference(textNode3, frRef, "test:multipleI18NNode");
    }

    private void checkReference(JCRNodeWrapper node, JCRNodeWrapper ref, String name) throws RepositoryException {
        PropertyIterator pi = node.getReferences();
        assertEquals("Unexpected number of references", 1, pi.getSize());
        JCRPropertyWrapper prop = (JCRPropertyWrapper) pi.nextProperty();
        assertEquals("Invalid property reference",name,prop.getName());
        assertEquals("Invalid property reference",ref.getPath(),prop.getParent().getPath());
    }

    private void checkNoReference(JCRNodeWrapper node) throws RepositoryException {
        PropertyIterator pi = node.getReferences();
        assertEquals("Unexpected number of references", 0, pi.getSize());
    }

    @After
    public void tearDown() throws Exception {
        TestHelper.deleteSite(TESTSITE_NAME);
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        session.save();
        session.logout();
    }



}
