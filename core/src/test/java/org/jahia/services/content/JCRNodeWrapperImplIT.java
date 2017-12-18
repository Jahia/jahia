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
