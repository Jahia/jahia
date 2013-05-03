/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.test.services.content;

import com.google.common.collect.Sets;
import org.jahia.api.Constants;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRPropertyWrapper;
import org.jahia.services.content.JCRPublicationService;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.sites.JahiaSite;
import org.jahia.test.TestHelper;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.jcr.Node;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * Test with multilingual and langauge independent references
 *
 * @author toto and bpapez
 */
public class ReferencesTest {

    private final static String TESTSITE_NAME = "jcrReferencesTest";
    private final static String SITECONTENT_ROOT_NODE = "/sites/" + TESTSITE_NAME;

    @BeforeClass
    public static void oneTimeSetUp() throws Exception {
        JahiaSite site = TestHelper.createSite(TESTSITE_NAME, Sets.newHashSet(Locale.ENGLISH.toString(), Locale.FRENCH.toString()), null, false);
        Assert.assertNotNull(site);
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentUserSession();
        session.save();
    }
    
    @AfterClass
    public static void oneTimeTearDown() throws Exception {
        TestHelper.deleteSite(TESTSITE_NAME);
    }
    
    @Before
    public void setUp() {

    }

    @After
    public void tearDown() {

    }      

    /*
    [test:externalReference] > nt:base, jmix:droppableContent
    - test:simpleNode (reference)
    - test:multipleI18NNode (reference) multiple i18n
     
    [test:externalWeakReference] > nt:base, jmix:droppableContent
    - test:simpleNode (weakreference)
    - test:multipleNode (weakreference) multiple
    - test:multipleI18NNode (weakreference) multiple i18n
    */    
    
    @org.junit.Test
    public void testI18NReferences() throws Exception {
        testReferences("refTest", "test:externalReference");        
    }    
    
    @org.junit.Test
    public void testI18NWeakreferences() throws Exception {
        testReferences("weakrefTest", "test:externalWeakReference");
    }

    private void testReferences(String testRootNodeName, String nodeType) throws Exception {
        JCRPublicationService jcrService = ServicesRegistry.getInstance()
                .getJCRPublicationService();

        JCRSessionWrapper englishEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.ENGLISH);
        JCRNodeWrapper stageNode = englishEditSession.getNode(SITECONTENT_ROOT_NODE+"/home").addNode(testRootNodeName, "jnt:contentList");

        JCRNodeWrapper textNode1 = stageNode.addNode("text1", "jnt:text");
        JCRNodeWrapper textNode2 = stageNode.addNode("text2", "jnt:text");
        JCRNodeWrapper textNode3 = stageNode.addNode("text3", "jnt:text");
        englishEditSession.save();

        JCRNodeWrapper ref = stageNode.addNode("reference", nodeType);
        ref.setProperty("test:simpleNode",textNode1);
        ref.setProperty("test:multipleI18NNode",new Value[] { englishEditSession.getValueFactory().createValue(textNode2) }) ;
        englishEditSession.save();

        JCRSessionWrapper frenchEditSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE, Locale.FRENCH);
        JCRNodeWrapper frStageNode = frenchEditSession.getNode(SITECONTENT_ROOT_NODE + "/home/" + testRootNodeName);
        JCRNodeWrapper frRef = frStageNode.getNode("reference");

        frRef.setProperty("test:multipleI18NNode",new Value[] { frenchEditSession.getValueFactory().createValue(textNode3) }) ;
        frenchEditSession.save();

        checkReference(textNode1, ref, "test:simpleNode", 1);
        checkReference(textNode2, ref, "test:multipleI18NNode", 1);
        checkNoReference(textNode3, "test:multipleI18NNode");
        
        textNode1 = frStageNode.getNode("text1");
        textNode2 = frStageNode.getNode("text2");
        textNode3 = frStageNode.getNode("text3");        
        
        checkReference(textNode1, ref, "test:simpleNode", 1);
        checkNoReference(textNode2, "test:multipleI18NNode");
        checkReference(textNode3, ref, "test:multipleI18NNode", 1);

        // now lets use a non-i18n session, so the reference checks must be on the translation nodes 
        JCRSessionWrapper noni18nSession = jcrService.getSessionFactory().getCurrentUserSession(Constants.EDIT_WORKSPACE);
        JCRNodeWrapper noni18nStageNode = noni18nSession.getNode(SITECONTENT_ROOT_NODE+"/home/" + testRootNodeName);
        
        textNode1 = noni18nStageNode.getNode("text1");
        textNode2 = noni18nStageNode.getNode("text2");
        textNode3 = noni18nStageNode.getNode("text3");
        
        checkReference(textNode1, ref, "test:simpleNode", 1);
        checkReference(textNode2, ref.getI18N(Locale.ENGLISH), "test:multipleI18NNode", 1);
        checkReference(textNode3, ref.getI18N(Locale.FRENCH), "test:multipleI18NNode", 1);        
    }    
    
    private void checkReference(JCRNodeWrapper node, Node ref, String name, int expectedSize) throws RepositoryException {
        PropertyIterator pi = node.getWeakReferences();
        assertEquals("Unexpected number of references", expectedSize, pi.getSize());
        JCRPropertyWrapper prop = (JCRPropertyWrapper) pi.nextProperty();
        assertEquals("Invalid property reference",name,prop.getName());
        assertEquals("Invalid property reference",ref.getPath(),prop.getParent().getPath());
        
        pi = node.getWeakReferences(name);        
        assertEquals("Unexpected number of references", expectedSize, pi.getSize());
        prop = (JCRPropertyWrapper) pi.nextProperty();        
        assertEquals("Invalid property reference",ref.getPath(),prop.getParent().getPath());        
    }

    private void checkNoReference(JCRNodeWrapper node, String name) throws RepositoryException {
        PropertyIterator pi = node.getWeakReferences();
        assertEquals("Unexpected number of references", 0, pi.getSize());
        
        pi = node.getWeakReferences(name);        
        assertEquals("Unexpected number of references", 0, pi.getSize());
    }
}
