package org.jahia.services.content;

import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.jahia.api.Constants;
import org.jahia.test.framework.AbstractJUnitTest;
import org.jahia.test.utils.TestHelper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class JCRNodeWrapperImplIT extends AbstractJUnitTest {

    private static final String SITE_NAME = JCRNodeWrapperImplIT.class.getSimpleName();
    private static final Locale LOCALE = Locale.ENGLISH;

    private static String siteKey;

    private JCRNodeWrapper siteNode;

    @Override
    public void beforeClassSetup() throws Exception {
        super.beforeClassSetup();
        siteKey = TestHelper.createSite(SITE_NAME).getSiteKey();
    }

    @Override
    public void afterClassSetup() throws Exception {
        super.afterClassSetup();
        TestHelper.deleteSite(SITE_NAME);
    }

    @Before
    public void setUp() throws RepositoryException {
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession(Constants.EDIT_WORKSPACE, LOCALE);
        siteNode = session.getNode("/sites/" + siteKey);
    }

    @After
    public void tearDown() {
        JCRSessionFactory.getInstance().closeAllSessions();
    }

    @Test
    public void testRemoveMixin_mixinTranslationPropertiesShouldBeRemoved() throws RepositoryException {
        addMixinAndProperties("test:i18nMixin", "namedProperty", "unnamedProperty");
        siteNode.removeMixin("test:i18nMixin");
        Node translationNode = getTranslationNode();
        Assert.assertFalse(translationNode.hasProperty("namedProperty"));
        Assert.assertFalse(translationNode.hasProperty("unnamedProperty"));
    }

    @Test
    public void testRemoveMixin_mixinInheritedTranslationPropertiesShouldBeRemoved() throws RepositoryException {
        addMixinAndProperties("test:i18nMixinSubtype", "namedProperty", "unnamedProperty");
        siteNode.removeMixin("test:i18nMixinSubtype");
        Node translationNode = getTranslationNode();
        Assert.assertFalse(translationNode.hasProperty("namedProperty"));
        Assert.assertFalse(translationNode.hasProperty("unnamedProperty"));
    }

    @Test
    public void testRemoveMixin_mixinDuplicateTranslationPropertiesShouldBePreserved() throws RepositoryException {
        addMixinAndProperties("test:i18nMixin", "namedProperty", "unnamedProperty");
        addMixinAndProperties("test:i18nMixinSubtypeDuplicateProperties", "namedProperty", "unnamedProperty");
        siteNode.removeMixin("test:i18nMixin");
        Node translationNode = getTranslationNode();
        Assert.assertTrue(translationNode.hasProperty("namedProperty"));
        Assert.assertTrue(translationNode.hasProperty("unnamedProperty"));
    }

    @Test
    public void testRemoveMixin_translationOwnPropertiesShouldBePreserved() throws RepositoryException {
        addMixinAndProperties("test:i18nMixin", "namedProperty", "unnamedProperty");
        siteNode.removeMixin("test:i18nMixin");
        Node translationNode = getTranslationNode();
        Assert.assertTrue(translationNode.hasProperty(Constants.JCR_PRIMARYTYPE));
        Assert.assertTrue(translationNode.hasProperty(Constants.JCR_ISCHECKEDOUT));
    }

    @Test
    public void testRemoveMixin_translationAmbiguousPropertyShouldBePreserved() throws RepositoryException {
        addMixinAndProperties("test:i18nMixinAmbiguousProperty", Constants.JCR_LANGUAGE);
        siteNode.removeMixin("test:i18nMixinAmbiguousProperty");
        Assert.assertTrue(getTranslationNode().hasProperty(Constants.JCR_LANGUAGE));
    }

    private void addMixinAndProperties(String mixinName, String... propertyNames) throws RepositoryException {
        siteNode.addMixin(mixinName);
        for (String propertyName : propertyNames) {
            siteNode.setProperty(propertyName, "property-value");
            assert getTranslationNode().hasProperty(propertyName);
        }
    }

    private Node getTranslationNode() throws RepositoryException {
        return siteNode.getRealNode().getNode(JCRNodeWrapperImpl.getTranslationNodeName(LOCALE));
    }
}
