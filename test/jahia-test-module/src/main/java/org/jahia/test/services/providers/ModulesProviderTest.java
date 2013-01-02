package org.jahia.test.services.providers;

import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.jahia.data.templates.JahiaTemplatesPackage;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.templates.JahiaTemplateManagerService;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Test class for modules provider
 * User: david
 * Date: 11/26/12
 * Time: 10:23 AM
 */
public class ModulesProviderTest {
    private static transient Logger logger = Logger.getLogger(ModulesProviderTest.class);
    
    private Node root;
    private JahiaTemplatesPackage dummyPackage;
    // Read by type
    @Test
    public void readTypes() {
        try {
            Session s = JCRSessionFactory.getInstance().getCurrentUserSession();

            // get default module
            JahiaTemplateManagerService templateManagerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
            dummyPackage = templateManagerService.getTemplatePackageByFileName("dummy1");
            root = templateManagerService.checkoutModule(null, dummyPackage.getScmURI(), null, dummyPackage.getName(), (JCRSessionWrapper) s).getNode("sources");

            // Read
            readType("jnt:cssFolder","css");
            readType("jnt:cssFile","css/dummy.css");
            readType("jnt:nodeTypeFolder","jnt_testComponent1");
            readType("jnt:templateTypeFolder","jnt_testComponent1/html");
            readType("jnt:viewFile", "jnt_testComponent1/html/testComponent1.jsp");
            readType("jnt:resourceBundleFolder", "resources");
            readType("jnt:definitionFile", "META-INF/definitions.cnd");

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail(e.getMessage());
        }

    }

    private void readType(String nodeType, String path) throws RepositoryException {
        String resolvedNodeType = root.getNode(path).getPrimaryNodeType().getName();
        assertEquals(nodeType + " is expected on path " + path + " but " + resolvedNodeType + " has been found", resolvedNodeType, nodeType);

    }

    // Write by type
    @Test
    public void writeAndReadProperties() {
        Session s = null;
        try {
            s = JCRSessionFactory.getInstance().getCurrentUserSession();

            // get default module
            JahiaTemplateManagerService templateManagerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
            dummyPackage = templateManagerService.getTemplatePackageByFileName("dummy1");
            root = templateManagerService.checkoutModule(null, dummyPackage.getScmURI(), null, dummyPackage.getName(), (JCRSessionWrapper) s).getNode("sources");

            //read properties
            Node viewNode = root.getNode("jnt_testComponent1/html/testComponent1.jsp");
            String sourceCode = viewNode.getProperty("sourceCode").getString();
            assertTrue("testComponent1 source not match", sourceCode.equals("--------------\ntest component\n--------------\n"));

            // write properties
            String testString = System.currentTimeMillis() + "\n";
            viewNode.setProperty("sourceCode", sourceCode + testString);
            viewNode.setProperty("cache.perUser", true);
            viewNode.setProperty("cache.requestParameters","dummyParam");
            s.save();

            // read properties
            s.logout();
            s = JCRSessionFactory.getInstance().getCurrentUserSession();
            dummyPackage = templateManagerService.getTemplatePackageByFileName("dummy1");
            root = s.getNode("/modules/" + dummyPackage.getRootFolderWithVersion() + "/sources");
            viewNode = root.getNode("jnt_testComponent1/html/testComponent1.jsp");
            assertTrue("testComponent1 source not match", viewNode.getProperty("sourceCode").getString().endsWith(testString));
            assertTrue("cache.perUser not set to true",viewNode.getProperty("cache.perUser").getBoolean());
            assertTrue("cache.requestParameters not set to dummyParam",viewNode.getProperty("cache.requestParameters").getString().equals("dummyParam"));

            viewNode.setProperty("sourceCode", sourceCode);
            s.save();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail(e.getMessage());
        }

    }

    @Test
    public void readNodeTypeDefinition() {
        Session s = null;
        try {
            s = JCRSessionFactory.getInstance().getCurrentUserSession();

            JahiaTemplateManagerService templateManagerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
            dummyPackage = templateManagerService.getTemplatePackageByFileName("dummy1");
            root = templateManagerService.checkoutModule(null, dummyPackage.getScmURI(), null, dummyPackage.getName(), (JCRSessionWrapper) s).getNode("sources");

            Node nodeType = root.getNode("META-INF/definitions.cnd/jnt:testComponent2");
            assertEquals("jnt:primaryNodeType", nodeType.getPrimaryNodeType().getName());
            assertEquals("jnt:content", nodeType.getProperty("j:supertype").getString());
            Set<String> testValues = new HashSet<String>();
            for (Value value : nodeType.getProperty("j:mixins").getValues()) {
                testValues.add(value.getString());
            }
            assertTrue(Sets.newHashSet("jmix:editorialContent", "mix:title", "jmix:tagged", "jmix:structuredContent").equals(testValues));
            assertTrue(nodeType.getProperty("j:hasOrderableChildNodes").getBoolean());
            assertTrue(nodeType.getProperty("j:isQueryable").getBoolean());
            assertFalse(nodeType.getProperty("j:isAbstract").getBoolean());

            Node propertyDefinition = nodeType.getNode("property1");
            assertEquals("jnt:propertyDefinition", propertyDefinition.getPrimaryNodeType().getName());
            assertEquals("String", propertyDefinition.getProperty("j:requiredType").getString());
            assertEquals("RichText", propertyDefinition.getProperty("j:selectorType").getString());
            assertTrue(propertyDefinition.getProperty("j:isInternationalized").getBoolean());
            assertTrue(propertyDefinition.getProperty("j:mandatory").getBoolean());
            assertFalse(propertyDefinition.getProperty("j:isFullTextSearchable").getBoolean());
            propertyDefinition = nodeType.getNode("property2");
            assertEquals("WeakReference", propertyDefinition.getProperty("j:requiredType").getString());
            assertEquals("Picker", propertyDefinition.getProperty("j:selectorType").getString());
            assertEquals("type='image'", propertyDefinition.getProperty("j:selectorOptions").getValues()[0].getString());
            assertTrue(propertyDefinition.getProperty("j:protected").getBoolean());
            propertyDefinition = nodeType.getNode("property3");
            assertEquals("String", propertyDefinition.getProperty("j:requiredType").getString());
            assertEquals("Choicelist", propertyDefinition.getProperty("j:selectorType").getString());
            assertEquals("resourceBundle", propertyDefinition.getProperty("j:selectorOptions").getValues()[0].getString());
            assertTrue(propertyDefinition.getProperty("j:multiple").getBoolean());
            assertEquals("no", propertyDefinition.getProperty("j:index").getString());
            testValues = new HashSet<String>();
            for (Value value : propertyDefinition.getProperty("j:defaultValues").getValues()) {
                testValues.add(value.getString());
            }
            assertTrue(Sets.newHashSet("one", "three").equals(testValues));
            testValues = new HashSet<String>();
            for (Value value : propertyDefinition.getProperty("j:valueConstraints").getValues()) {
                testValues.add(value.getString());
            }
            assertTrue(Sets.newHashSet("one", "two", "three").equals(testValues));
            propertyDefinition = nodeType.getNode("property4");
            assertEquals("Long", propertyDefinition.getProperty("j:requiredType").getString());
            assertEquals("2", propertyDefinition.getProperty("j:defaultValues").getValues()[0].getString());
            assertTrue(propertyDefinition.getProperty("j:autoCreated").getBoolean());
            assertTrue(propertyDefinition.getProperty("j:isHidden").getBoolean());
            assertTrue(propertyDefinition.getProperty("j:isFullTextSearchable").getBoolean());
            assertEquals("tokenized", propertyDefinition.getProperty("j:index").getString());
            assertEquals("VERSION", propertyDefinition.getProperty("j:onParentVersion").getString());
            assertEquals("latest", propertyDefinition.getProperty("j:onConflictAction").getString());
            assertEquals(1.0, propertyDefinition.getProperty("j:scoreboost").getDouble(), 0);
            assertTrue(propertyDefinition.getProperty("j:isQueryOrderable").getBoolean());
            assertEquals("=", propertyDefinition.getProperty("j:availableQueryOperators").getValues()[0].getString());
            propertyDefinition = nodeType.getNode("property5");
            assertFalse(propertyDefinition.getProperty("j:autoCreated").getBoolean());
            assertFalse(propertyDefinition.getProperty("j:isHidden").getBoolean());
            assertTrue(propertyDefinition.getProperty("j:isFacetable").getBoolean());
            assertTrue(propertyDefinition.getProperty("j:isHierarchical").getBoolean());
            assertEquals("COPY", propertyDefinition.getProperty("j:onParentVersion").getString());
            assertEquals("oldest", propertyDefinition.getProperty("j:onConflictAction").getString());
            assertEquals(2.0, propertyDefinition.getProperty("j:scoreboost").getDouble(), 0);
            assertFalse(propertyDefinition.getProperty("j:isQueryOrderable").getBoolean());
            testValues = new HashSet<String>();
            for (Value value : propertyDefinition.getProperty("j:availableQueryOperators").getValues()) {
                testValues.add(value.getString());
            }
            assertTrue(Sets.newHashSet("=", "<>", "<", "<=", ">", ">=", "LIKE").equals(testValues));
            assertEquals("keyword", propertyDefinition.getProperty("j:analyzer").getString());

            Node childNodeDefinition = nodeType.getNode("child1");
            assertEquals("jnt:childNodeDefinition", childNodeDefinition.getPrimaryNodeType().getName());
            assertEquals("jnt:testComponent1", childNodeDefinition.getProperty("j:requiredPrimaryTypes").getValues()[0].getString());
            assertEquals("jnt:testComponent1", childNodeDefinition.getProperty("j:defaultPrimaryType").getString());
            assertTrue(childNodeDefinition.getProperty("j:autoCreated").getBoolean());
            assertFalse(childNodeDefinition.getProperty("j:mandatory").getBoolean());
            assertFalse(childNodeDefinition.getProperty("j:protected").getBoolean());
            assertEquals("VERSION", childNodeDefinition.getProperty("j:onParentVersion").getString());
            childNodeDefinition = nodeType.getNode("child2");
            assertFalse(childNodeDefinition.getProperty("j:autoCreated").getBoolean());
            assertTrue(childNodeDefinition.getProperty("j:mandatory").getBoolean());
            assertTrue(childNodeDefinition.getProperty("j:protected").getBoolean());
            assertEquals("COPY", childNodeDefinition.getProperty("j:onParentVersion").getString());
            childNodeDefinition = nodeType.getNode("__node__jnt:testComponent2");
            assertEquals("jnt:unstructuredChildNodeDefinition", childNodeDefinition.getPrimaryNodeType().getName());
            assertEquals("jnt:testComponent2", childNodeDefinition.getProperty("j:requiredPrimaryTypes").getValues()[0].getString());

            nodeType = root.getNode("META-INF/definitions.cnd/jnt:testComponent3");
            assertEquals("jnt:primaryNodeType", nodeType.getPrimaryNodeType().getName());
            assertEquals("jnt:content", nodeType.getProperty("j:supertype").getString());
            assertEquals("mix:title", nodeType.getProperty("j:mixins").getValues()[0].getString());
            assertFalse(nodeType.getProperty("j:hasOrderableChildNodes").getBoolean());
            assertFalse(nodeType.getProperty("j:isQueryable").getBoolean());
            assertTrue(nodeType.getProperty("j:isAbstract").getBoolean());
            assertEquals("metadata", nodeType.getProperty("j:itemsType").getString());

            nodeType = root.getNode("META-INF/definitions.cnd/jmix:testMixin1");
            assertEquals("jnt:mixinNodeType", nodeType.getPrimaryNodeType().getName());
            assertEquals("jmix:templateMixin", nodeType.getProperty("j:mixins").getValues()[0].getString());
            testValues = new HashSet<String>();
            for (Value value : nodeType.getProperty("j:mixinExtends").getValues()) {
                testValues.add(value.getString());
            }
            assertTrue(Sets.newHashSet("jmix:list", "jnt:area").equals(testValues));
            assertEquals("layout", nodeType.getProperty("j:itemsType").getString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail(e.getMessage());
        }
    }

    @Test
    public void writeNodeTypeDefinition() {
        Session s = null;
        try {
            s = JCRSessionFactory.getInstance().getCurrentUserSession();

            JahiaTemplateManagerService templateManagerService = ServicesRegistry.getInstance().getJahiaTemplateManagerService();
            dummyPackage = templateManagerService.getTemplatePackageByFileName("dummy1");
            root = templateManagerService.checkoutModule(null, dummyPackage.getScmURI(), null, dummyPackage.getName(), (JCRSessionWrapper) s).getNode("sources");
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
            dummyPackage = templateManagerService.getTemplatePackageByFileName("dummy1");
            String cndPath = dummyPackage.getSourcesFolder().getAbsolutePath() + "/src/main/webapp/META-INF/definitions.cnd";
            BufferedReader input =  new BufferedReader(new FileReader(cndPath));
            try {
                String line = null;
                int n = 1;
                while ((line = input.readLine()) != null){
                    if (n == 30) {
                        assertEquals("[jnt:testComponent4] > jnt:content, jmix:tagged, jmix:structuredContent orderable noquery", line);
                    } else if (n == 31) {
                        assertEquals(" - property1 (string) mandatory", line);
                    } else if (n == 32) {
                        assertEquals(" - property2 (long) protected", line);
                    }
                    n++;
                }
            } finally {
                input.close();
            }
            root = s.getNode("/modules/" + dummyPackage.getRootFolderWithVersion() + "/sources");
            nodeType = root.getNode("META-INF/definitions.cnd/jnt:testComponent4");
            assertEquals("jnt:primaryNodeType", nodeType.getPrimaryNodeType().getName());
            assertEquals("jnt:content", nodeType.getProperty("j:supertype").getString());
            Set<String> testValues = new HashSet<String>();
            for (Value value : nodeType.getProperty("j:mixins").getValues()) {
                testValues.add(value.getString());
            }
            assertTrue(Sets.newHashSet("jmix:tagged", "jmix:structuredContent").equals(testValues));
            assertTrue(nodeType.getProperty("j:hasOrderableChildNodes").getBoolean());
            assertFalse(nodeType.getProperty("j:isAbstract").getBoolean());
            assertFalse(nodeType.getProperty("j:isQueryable").getBoolean());

            propertyDefinition = nodeType.getNode("property1");
            assertEquals("jnt:propertyDefinition", propertyDefinition.getPrimaryNodeType().getName());
            assertEquals("String", propertyDefinition.getProperty("j:requiredType").getString());
            assertTrue(propertyDefinition.getProperty("j:mandatory").getBoolean());
            propertyDefinition = nodeType.getNode("property2");
            assertEquals("Long", propertyDefinition.getProperty("j:requiredType").getString());
            assertTrue(propertyDefinition.getProperty("j:protected").getBoolean());

            nodeType.orderBefore("property2", "property1");
            s.save();
            s.logout();
            input =  new BufferedReader(new FileReader(cndPath));
            try {
                String line = null;
                int n = 1;
                while ((line = input.readLine()) != null){
                    if (n == 31) {
                        assertEquals(" - property2 (long) protected", line);
                    } else if (n == 32) {
                        assertEquals(" - property1 (string) mandatory", line);
                    }
                    n++;
                }
            } finally {
                input.close();
            }

            s.move("/modules/" + dummyPackage.getRootFolderWithVersion() + "/sources/META-INF/definitions.cnd/jnt:testComponent4",
                   "/modules/" + dummyPackage.getRootFolderWithVersion() + "/sources/META-INF/definitions.cnd/jnt:testRenamedComponent");
            s.save();
            s.logout();
            input =  new BufferedReader(new FileReader(cndPath));
            try {
                String line = null;
                int n = 1;
                while ((line = input.readLine()) != null){
                    if (n == 30) {
                        assertEquals("[jnt:testRenamedComponent] > jnt:content, jmix:tagged, jmix:structuredContent orderable noquery", line);
                    }
                    n++;
                }
            } finally {
                input.close();
            }
            root = s.getNode("/modules/" + dummyPackage.getRootFolderWithVersion() + "/sources");
            assertTrue(root.hasNode("META-INF/definitions.cnd/jnt:testRenamedComponent"));

            s = JCRSessionFactory.getInstance().getCurrentUserSession();
            root = s.getNode("/modules/" + dummyPackage.getRootFolderWithVersion() + "/sources");
            nodeType = root.getNode("META-INF/definitions.cnd/jnt:testRenamedComponent");
            nodeType.remove();
            s.save();
            s.logout();
            s = JCRSessionFactory.getInstance().getCurrentUserSession();
            dummyPackage = templateManagerService.getTemplatePackageByFileName("dummy1");
            root = s.getNode("/modules/" + dummyPackage.getRootFolderWithVersion() + "/sources");
            assertFalse(root.hasNode("META-INF/definitions.cnd/jnt:testRenamedComponent"));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            fail(e.getMessage());
        }

    }

}
