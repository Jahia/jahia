/*
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2020 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.services.providers;

import com.google.common.collect.Sets;

import org.jahia.api.Constants;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.render.filter.cache.CacheUtils;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

/**
 * Test class for modules provider
 * User: david
 * Date: 11/26/12
 * Time: 10:23 AM
 */
public class ModulesProviderTest {

    private Node root;
    private JahiaTemplatesPackage dummyPackage;
    private JahiaTemplateManagerService templateManagerService;

    @Before
    public void setUp() throws Exception {
        JCRSessionFactory.getInstance().closeAllSessions();
        JCRSessionWrapper s = JCRSessionFactory.getInstance().getCurrentUserSession();

        // get default module
        templateManagerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
        final JCRNodeWrapper dummy1 = templateManagerService.checkoutModule(null, "scm:git:git@github.com:Jahia/test-module-dummy1.git", null, "dummy1", null, s);
        root = dummy1.getNode("sources/src/main/resources");
    }

    @After
    public void tearDown() throws Exception {
        JahiaTemplatesPackage pack = templateManagerService.getTemplatePackageById("dummy1");
        if (pack != null) {
            templateManagerService.undeployModule(pack);
        }
    }

    // Read by type
    @Test
    public void readTypes() throws Exception {

        // Read
        readType("jnt:cssFolder", "css");
        readType("jnt:cssFile", "css/dummy.css");
        readType("jnt:nodeTypeFolder", "jnt_testComponent1");
        readType("jnt:templateTypeFolder", "jnt_testComponent1/html");
        readType("jnt:viewFile", "jnt_testComponent1/html/testComponent1.jsp");
        readType("jnt:resourceBundleFolder", "resources");
        readType("jnt:definitionFile", "META-INF/definitions.cnd");

    }

    private void readType(String nodeType, String path) throws RepositoryException {
        String resolvedNodeType = root.getNode(path).getPrimaryNodeType().getName();
        assertThat(resolvedNodeType).isEqualTo(nodeType);

    }

    // Write by type
    @Test
    public void writeAndReadProperties() throws Exception {
        JCRSessionWrapper s = JCRSessionFactory.getInstance().getCurrentUserSession();
        //read properties
        Node viewNode = root.getNode("jnt_testComponent1/html/testComponent1.jsp");
        String sourceCode = viewNode.getProperty("sourceCode").getString();
        assertThat(sourceCode).isEqualToNormalizingNewlines("--------------\ntest component\n--------------\n");

        // write properties
        String testString = System.currentTimeMillis() + "\n";
        viewNode.setProperty("sourceCode", sourceCode + testString);
        viewNode.setProperty(CacheUtils.FRAGMNENT_PROPERTY_CACHE_PER_USER, true);
        viewNode.setProperty("cache.requestParameters", "dummyParam");
        s.save();

        // read properties
        s.logout();
        s = JCRSessionFactory.getInstance().getCurrentUserSession();
        dummyPackage = templateManagerService.getTemplatePackageById("dummy1");
        root = s.getNode("/modules/" + dummyPackage.getIdWithVersion() + "/sources/src/main/resources");
        viewNode = root.getNode("jnt_testComponent1/html/testComponent1.jsp");
        assertThat(viewNode.getProperty("sourceCode").getString()).endsWith(testString);
        assertThat(viewNode.getProperty(CacheUtils.FRAGMNENT_PROPERTY_CACHE_PER_USER).getBoolean()).isTrue();
        assertThat(viewNode.getProperty("cache.requestParameters").getString()).isEqualTo("dummyParam");

        viewNode.setProperty("sourceCode", sourceCode);
        s.save();
    }

    @Test
    public void readNodeTypeDefinition() throws Exception {
        Node nodeType = root.getNode("META-INF/definitions.cnd/jnt:testComponent2");
        assertThat(nodeType.getPrimaryNodeType().getName()).isEqualTo("jnt:primaryNodeType");
        assertThat(nodeType.getProperty("j:supertype").getString()).isEqualTo("jnt:content");
        Set<String> testValues = new HashSet<String>();
        for (Value value : nodeType.getProperty("j:mixins").getValues()) {
            testValues.add(value.getString());
        }
        assertThat(testValues).isEqualTo(Sets.newHashSet("jmix:editorialContent", "mix:title", "jmix:tagged", "jmix:structuredContent"));
        assertThat(nodeType.getProperty("j:hasOrderableChildNodes").getBoolean()).isTrue();
        assertThat(nodeType.getProperty("j:isQueryable").getBoolean()).isTrue();
        assertThat(nodeType.getProperty("j:isAbstract").getBoolean()).isFalse();

        Node propertyDefinition = nodeType.getNode("property1");
        assertThat(propertyDefinition.getPrimaryNodeType().getName()).isEqualTo("jnt:propertyDefinition");
        assertThat(propertyDefinition.getProperty("j:requiredType").getString()).isEqualTo("String");
        assertThat(propertyDefinition.getProperty("j:selectorType").getString()).isEqualTo("RichText");
        assertThat(propertyDefinition.getProperty("j:isInternationalized").getBoolean()).isTrue();
        assertThat(propertyDefinition.getProperty("j:mandatory").getBoolean()).isTrue();
        assertThat(propertyDefinition.getProperty("j:isFullTextSearchable").getBoolean()).isFalse();
        propertyDefinition = nodeType.getNode("property2");
        assertThat(propertyDefinition.getProperty("j:requiredType").getString()).isEqualTo("WeakReference");
        assertThat(propertyDefinition.getProperty("j:selectorType").getString()).isEqualTo("Picker");
        assertThat(propertyDefinition.getProperty("j:selectorOptions").getValues()[0].getString()).isEqualTo("type='image'");
        assertThat(propertyDefinition.getProperty("j:protected").getBoolean()).isTrue();
        propertyDefinition = nodeType.getNode("property3");
        assertThat(propertyDefinition.getProperty("j:requiredType").getString()).isEqualTo("String");
        assertThat(propertyDefinition.getProperty("j:selectorType").getString()).isEqualTo("Choicelist");
        assertThat(propertyDefinition.getProperty("j:selectorOptions").getValues()[0].getString()).isEqualTo("resourceBundle");
        assertThat(propertyDefinition.getProperty("j:multiple").getBoolean()).isTrue();
        assertThat(propertyDefinition.getProperty("j:index").getString()).isEqualTo("no");
        testValues = new HashSet<>();
        for (Value value : propertyDefinition.getProperty("j:defaultValues").getValues()) {
            testValues.add(value.getString());
        }
        assertThat(testValues).isEqualTo(Sets.newHashSet("one", "three"));
        testValues = new HashSet<>();
        for (Value value : propertyDefinition.getProperty("j:valueConstraints").getValues()) {
            testValues.add(value.getString());
        }
        assertThat(testValues).isEqualTo(Sets.newHashSet("one", "two", "three"));
        propertyDefinition = nodeType.getNode("property4");
        assertThat(propertyDefinition.getProperty("j:requiredType").getString()).isEqualTo("Long");
        assertThat(propertyDefinition.getProperty("j:defaultValues").getValues()[0].getString()).isEqualTo("2");
        assertThat(propertyDefinition.getProperty("j:autoCreated").getBoolean()).isTrue();
        assertThat(propertyDefinition.getProperty("j:isHidden").getBoolean()).isTrue();
        assertThat(propertyDefinition.getProperty("j:isFullTextSearchable").getBoolean()).isTrue();
        assertThat(propertyDefinition.getProperty("j:index").getString()).isEqualTo("tokenized");
        assertThat(propertyDefinition.getProperty("j:onParentVersion").getString()).isEqualTo("VERSION");
        assertThat(propertyDefinition.getProperty("j:onConflictAction").getString()).isEqualTo("latest");
        assertThat(propertyDefinition.getProperty("j:scoreboost").getDouble()).isEqualTo(1.0);
        assertThat(propertyDefinition.getProperty("j:isQueryOrderable").getBoolean()).isTrue();
        assertThat(propertyDefinition.getProperty("j:availableQueryOperators").getValues()[0].getString()).isEqualTo("=");
        propertyDefinition = nodeType.getNode("property5");
        assertThat(propertyDefinition.getProperty("j:autoCreated").getBoolean()).isFalse();
        assertThat(propertyDefinition.getProperty("j:isHidden").getBoolean()).isFalse();
        assertThat(propertyDefinition.getProperty("j:isFacetable").getBoolean()).isTrue();
        assertThat(propertyDefinition.getProperty("j:isHierarchical").getBoolean()).isTrue();
        assertThat(propertyDefinition.getProperty("j:onParentVersion").getString()).isEqualTo("COPY");
        assertThat(propertyDefinition.getProperty("j:onConflictAction").getString()).isEqualTo("oldest");
        assertThat(propertyDefinition.getProperty("j:scoreboost").getDouble()).isEqualTo(2.0);
        assertThat(propertyDefinition.getProperty("j:isQueryOrderable").getBoolean()).isFalse();
        testValues = new HashSet<>();
        for (Value value : propertyDefinition.getProperty("j:availableQueryOperators").getValues()) {
            testValues.add(value.getString());
        }
        assertThat(testValues).isEqualTo(Sets.newHashSet("=", "<>", "<", "<=", ">", ">=", "LIKE"));
        assertThat(propertyDefinition.getProperty("j:analyzer").getString()).isEqualTo("keyword");

        Node childNodeDefinition = nodeType.getNode("child1");
        assertThat(childNodeDefinition.getPrimaryNodeType().getName()).isEqualTo("jnt:childNodeDefinition");
        assertThat(childNodeDefinition.getProperty("j:requiredPrimaryTypes").getValues()[0].getString()).isEqualTo("jnt:testComponent1");
        assertThat(childNodeDefinition.getProperty("j:defaultPrimaryType").getString()).isEqualTo("jnt:testComponent1");
        assertThat(childNodeDefinition.getProperty("j:autoCreated").getBoolean()).isTrue();
        assertThat(childNodeDefinition.getProperty("j:mandatory").getBoolean()).isFalse();
        assertThat(childNodeDefinition.getProperty("j:protected").getBoolean()).isFalse();
        assertThat(childNodeDefinition.getProperty("j:onParentVersion").getString()).isEqualTo("VERSION");
        childNodeDefinition = nodeType.getNode("child2");
        assertThat(childNodeDefinition.getProperty("j:autoCreated").getBoolean()).isFalse();
        assertThat(childNodeDefinition.getProperty("j:mandatory").getBoolean()).isTrue();
        assertThat(childNodeDefinition.getProperty("j:protected").getBoolean()).isTrue();
        assertThat(childNodeDefinition.getProperty("j:onParentVersion").getString()).isEqualTo("COPY");
        childNodeDefinition = nodeType.getNode("__node__jnt@@testComponent2");
        assertThat(childNodeDefinition.getPrimaryNodeType().getName()).isEqualTo("jnt:unstructuredChildNodeDefinition");
        assertThat(childNodeDefinition.getProperty("j:requiredPrimaryTypes").getValues()[0].getString()).isEqualTo("jnt:testComponent2");

        nodeType = root.getNode("META-INF/definitions.cnd/jnt:testComponent3");
        assertThat(nodeType.getPrimaryNodeType().getName()).isEqualTo("jnt:primaryNodeType");
        assertThat(nodeType.getProperty("j:supertype").getString()).isEqualTo("jnt:content");
        assertThat(nodeType.getProperty("j:mixins").getValues()[0].getString()).isEqualTo("mix:title");
        assertThat(nodeType.getProperty("j:hasOrderableChildNodes").getBoolean()).isFalse();
        assertThat(nodeType.getProperty("j:isQueryable").getBoolean()).isFalse();
        assertThat(nodeType.getProperty("j:isAbstract").getBoolean()).isTrue();
        assertThat(nodeType.getProperty("j:itemsType").getString()).isEqualTo("metadata");

        nodeType = root.getNode("META-INF/definitions.cnd/jmix:testMixin1");
        assertThat(nodeType.getPrimaryNodeType().getName()).isEqualTo("jnt:mixinNodeType");
        assertThat(nodeType.getProperty("j:mixins").getValues()[0].getString()).isEqualTo("jmix:templateMixin");
        testValues = new HashSet<>();
        for (Value value : nodeType.getProperty("j:mixinExtends").getValues()) {
            testValues.add(value.getString());
        }
        assertThat(testValues).isEqualTo(Sets.newHashSet(Constants.JAHIAMIX_LIST, "jnt:area"));
        assertThat(nodeType.getProperty("j:itemsType").getString()).isEqualTo("layout");
    }

    @Test
    public void writeNodeTypeDefinition() throws Exception {
        JCRSessionWrapper s = JCRSessionFactory.getInstance().getCurrentUserSession();
        String definitionsPath = "META-INF/definitions.cnd";
        Node definitions = root.getNode(definitionsPath);
        Node nodeType = definitions.addNode("jnt:testComponent4", "jnt:primaryNodeType");
        nodeType.setProperty("j:supertype", "jnt:content");
        String[] values = {"jmix:tagged", "jmix:structuredContent"};
        nodeType.setProperty("j:mixins", values);
        nodeType.setProperty("j:hasOrderableChildNodes", true);
        nodeType.setProperty("j:isAbstract", false);
        nodeType.setProperty("j:isQueryable", false);
        Node propertyDefinition = nodeType.addNode("property1", "jnt:propertyDefinition");
        propertyDefinition.setProperty("j:requiredType", "String");
        propertyDefinition.setProperty("j:mandatory", true);
        propertyDefinition = nodeType.addNode("property2", "jnt:propertyDefinition");
        propertyDefinition.setProperty("j:requiredType", "Long");
        propertyDefinition.setProperty("j:protected", true);
        s.save();
        s.logout();
        s = JCRSessionFactory.getInstance().getCurrentUserSession();
        dummyPackage = templateManagerService.getTemplatePackageById("dummy1");
        String cndPath = dummyPackage.getSourcesFolder().getAbsolutePath() + "/src/main/resources/META-INF/definitions.cnd";
        try (BufferedReader input = new BufferedReader(new FileReader(cndPath))){
            String line = null;
            int n = 1;
            while ((line = input.readLine()) != null) {
                if (n == 31) {
                    assertThat(line).isEqualTo("[jnt:testComponent4] > jnt:content, jmix:tagged, jmix:structuredContent orderable noquery");
                } else if (n == 32) {
                    assertThat(line).isEqualTo(" - property1 (string) mandatory");
                } else if (n == 33) {
                    assertThat(line).isEqualTo(" - property2 (long) protected");
                }
                n++;
            }
        }
        root = s.getNode("/modules/" + dummyPackage.getIdWithVersion() + "/sources/src/main/resources");
        nodeType = root.getNode("META-INF/definitions.cnd/jnt:testComponent4");
        assertThat(nodeType.getPrimaryNodeType().getName()).isEqualTo("jnt:primaryNodeType");
        assertThat(nodeType.getProperty("j:supertype").getString()).isEqualTo("jnt:content");
        Set<String> testValues = new HashSet<>();
        for (Value value : nodeType.getProperty("j:mixins").getValues()) {
            testValues.add(value.getString());
        }
        assertThat(testValues).isEqualTo(Sets.newHashSet("jmix:tagged", "jmix:structuredContent"));
        assertThat(nodeType.getProperty("j:hasOrderableChildNodes").getBoolean()).isTrue();
        assertThat(nodeType.getProperty("j:isAbstract").getBoolean()).isFalse();
        assertThat(nodeType.getProperty("j:isQueryable").getBoolean()).isFalse();

        propertyDefinition = nodeType.getNode("property1");
        assertThat(propertyDefinition.getPrimaryNodeType().getName()).isEqualTo("jnt:propertyDefinition");
        assertThat(propertyDefinition.getProperty("j:requiredType").getString()).isEqualTo("String");
        assertThat(propertyDefinition.getProperty("j:mandatory").getBoolean()).isTrue();
        propertyDefinition = nodeType.getNode("property2");
        assertThat(propertyDefinition.getProperty("j:requiredType").getString()).isEqualTo("Long");
        assertThat(propertyDefinition.getProperty("j:protected").getBoolean()).isTrue();

        nodeType.orderBefore("property2", "property1");
        s.save();
        s.logout();
        try (BufferedReader input = new BufferedReader(new FileReader(cndPath))){
            String line = null;
            int n = 1;
            while ((line = input.readLine()) != null) {
                if (n == 32) {
                    assertThat(line).isEqualTo(" - property2 (long) protected");
                } else if (n == 33) {
                    assertThat(line).isEqualTo(" - property1 (string) mandatory");
                }
                n++;
            }
        }

        /**
         * move
         */

        s = JCRSessionFactory.getInstance().getCurrentUserSession();

        s.move("/modules/" + dummyPackage.getIdWithVersion() + "/sources/src/main/resources/META-INF/definitions.cnd/jnt:testComponent4",
                "/modules/" + dummyPackage.getIdWithVersion() + "/sources/src/main/resources/META-INF/definitions.cnd/jnt:testRenamedComponent");
        s.save();

        try (BufferedReader input = new BufferedReader(new FileReader(cndPath))){
            String line = null;
            int n = 1;
            while ((line = input.readLine()) != null) {
                if (n == 31) {
                    assertThat(line).isEqualTo("[jnt:testRenamedComponent] > jnt:content, jmix:tagged, jmix:structuredContent orderable noquery");
                }
                n++;
            }
        }
        root = s.getNode("/modules/" + dummyPackage.getIdWithVersion() + "/sources/src/main/resources");
        assertThat(root.hasNode("META-INF/definitions.cnd/jnt:testRenamedComponent")).isTrue();
        s.save();
        s.logout();

        /**
         * ordering
         */

        s = JCRSessionFactory.getInstance().getCurrentUserSession();
        root = s.getNode("/modules/" + dummyPackage.getIdWithVersion() + "/sources/src/main/resources/META-INF/definitions.cnd/jnt:testRenamedComponent");
        root.orderBefore("property2", "property1");
        s.save();
        s.logout();
        try (BufferedReader input = new BufferedReader(new FileReader(cndPath))) {
            String line = null;
            int n = 1;
            while ((line = input.readLine()) != null) {
                if (n == 32) {
                    assertThat(line).isEqualTo(" - property2 (long) protected");
                } else if (n == 33) {
                    assertThat(line).isEqualTo(" - property1 (string) mandatory");
                }
                n++;
            }
        }


        /**
         * cleanup
         */

        s = JCRSessionFactory.getInstance().getCurrentUserSession();
        root = s.getNode("/modules/" + dummyPackage.getIdWithVersion() + "/sources/src/main/resources");
        nodeType = root.getNode("META-INF/definitions.cnd/jnt:testRenamedComponent");
        nodeType.remove();
        s.save();
        s.logout();
        s = JCRSessionFactory.getInstance().getCurrentUserSession();
        dummyPackage = templateManagerService.getTemplatePackageById("dummy1");
        root = s.getNode("/modules/" + dummyPackage.getIdWithVersion() + "/sources/src/main/resources");
        assertThat(root.hasNode("META-INF/definitions.cnd/jnt:testRenamedComponent")).isFalse();
    }

}
