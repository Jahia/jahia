package org.jahia.services.content.nodetypes;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.util.Files;
import org.jahia.services.content.JCRNodeWrapper;
import org.jahia.services.content.JCRSessionFactory;
import org.jahia.services.content.JCRSessionWrapper;
import org.jahia.services.content.JCRStoreService;
import org.jahia.test.framework.AbstractJUnitTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.version.Version;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2016 Jahia Solutions Group SA. All rights reserved.
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

public class NodeTypesChangesIT extends AbstractJUnitTest {


    private static Logger logger = org.slf4j.LoggerFactory.getLogger(NodeTypesChangesIT.class);
    JCRNodeWrapper testNode;
    List<Result> results = new ArrayList<>();
    final boolean[] expectedResults = {true,true,false,false,false,false,true,true,true,true,true,true,true,true,true,true,true,true,true,true,false,true,
            true,false,true,true,true,true,true,false,true,true,true,true,true,true,true,true,true,true,true,false,false,false,false,true,true,true,true,
            true,true,false,false,false,false,true,true,true,true,true,true,false,false,false,false};

    @Before
    public void setUp() throws Exception {

        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession("default", Locale.ENGLISH, null);
        testNode = session.getNode("/").addNode("nodeTypeChanges", "nt:unstructured");
    }

    /**
     * This test is related to the story https://jira.jahia.org/browse/BACKLOG-6892
     * it creates a nodetype, content from this type then do modification on the nodetype and check if the node is still accessible.
     * If any change is done to the test (add modification or operation), please update the expectedResults to reflect the changes.
     * @throws Exception
     */
    @Test
    public void doNodetypesModifications() throws Exception {

        
        JCRSessionWrapper session = JCRSessionFactory.getInstance().getCurrentSystemSession("default", Locale.ENGLISH, null);
        String versionName;
        versionName = init(session);
        removeProperties();
        checkOperation(session, testNode.getNode("test").getPath(), "remove property from nodetype", versionName);

        versionName = init(session);
        removeNodeType();
        checkOperation(session, testNode.getNode("test").getPath(), "remove nodetype from definition", versionName);

        versionName = init(session);
        switchToi18n();
        checkOperation(session, testNode.getNode("test").getPath(), "switch a property from non-i18n to i18n", versionName);

        versionName = init(session);
        switchFromI18n();
        checkOperation(session, testNode.getNode("test").getPath(), "switch a property from i18n to non-i18n", versionName);

        versionName = init(session);
        switchToMultipe();
        checkOperation(session, testNode.getNode("test").getPath(), "switch a property from single to multipe", versionName);

        versionName = init(session);
        switchFromMultiple();
        checkOperation(session, testNode.getNode("test").getPath(), "switch a property from multipe to single", versionName);

        versionName = init(session);
        addMandatoryConstraint();
        checkOperation(session, testNode.getNode("test").getPath(), "add mandatory contraint to a property", versionName);

        //validate results, un comment the generation of the results if the test change.
        //String s = "";
        for (int i = 0; i < results.size(); i++) {
            Result result = results.get(i);
            logger.info("compare {} {}: {} with {}", new String[]{result.modification, result.operation, Boolean.toString(result.result),
                    Boolean.toString(expectedResults[i])});
            // s += results.get(i).result + ",";
            Assert.assertEquals(result.result, expectedResults[i]);
        }
        //logger.info(s);

        // show table
        String mod = null;
        for (Result r : results) {
            if (mod == null || !mod.equals(r.modification)) {
                // display title
                logger.info("\n=== {} ===", r.modification);

            }
            String detail = StringUtils.isEmpty(r.detail) ? "" : "(" + StringUtils.substringAfterLast(r.detail, ".") + ")";
            logger.info("{}: {} {}", new String[]{r.operation, Boolean.toString(r.result), detail});
            mod = r.modification;
        }


    }

    private String init(JCRSessionWrapper session) throws IOException, ParseException, RepositoryException {
        // create definition file

        File file = Files.newTemporaryFile();
        String definition = "<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:test] > nt:base, mix:versionable\n" +
                " - test (string)\n" +
                " - test_multiple (string) multiple\n" +
                " - test_i18n (string) i18n\n";
        // namespace
        FileUtils.writeStringToFile(file, definition);

        // register nodetype
        NodeTypeRegistry.getInstance().addDefinitionsFile(file, "testModule");
        JCRStoreService.getInstance().deployDefinitions("testModule", null, -1);

        // create node from nodetype
        JCRNodeWrapper n = testNode.addNode("test", "test:test");
        n.setProperty("test", "test");
        n.setProperty("test_multiple",new String[]{"test", "test", "test"});
        n.setProperty("test_i18n", "test");
        session.save();
        // create a version
        Version v = session.getWorkspace().getVersionManager().checkin(n.getPath());
        session.getWorkspace().getVersionManager().checkout(n.getPath());
        n.setProperty("test", "new value");
        session.save();
        v = session.getWorkspace().getVersionManager().checkin(n.getPath());
        String versionName = v.getName();
        session.getWorkspace().getVersionManager().checkout(n.getPath());
        
        // restore origin value
        n.setProperty("test", "test");
        session.save();

        // cleanup
        FileUtils.deleteQuietly(file);
        return versionName;
    }


    private void removeProperties() throws IOException, ParseException, RepositoryException {
        File file = Files.newTemporaryFile();
        String definition = "<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:test] > nt:base, mix:versionable\n";
        FileUtils.writeStringToFile(file, definition);

        // register nodetype
        NodeTypeRegistry.getInstance().addDefinitionsFile(file, "testModule");
        JCRStoreService.getInstance().deployDefinitions("testModule", null, -1);

        // cleanup
        FileUtils.deleteQuietly(file);
    }

    private void removeNodeType() throws IOException, ParseException, RepositoryException {
        File file = Files.newTemporaryFile();
        String definition = "<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n";
        FileUtils.writeStringToFile(file, definition);

        // register nodetype
        NodeTypeRegistry.getInstance().addDefinitionsFile(file, "testModule");
        JCRStoreService.getInstance().deployDefinitions("testModule", null, -1);
        JCRStoreService.getInstance().reloadNodeTypeRegistry();

        // cleanup
        FileUtils.deleteQuietly(file);
    }

    private void switchToi18n() throws IOException, ParseException, RepositoryException {
        File file = Files.newTemporaryFile();
        String definition = "<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:test] > nt:base, mix:versionable\n" +
                " - test (string) i18n\n" +
                " - test_multiple (string) multiple\n" +
                " - test_i18n (string) i18n\n";
        // namespace
        FileUtils.writeStringToFile(file, definition);

        // register nodetype
        NodeTypeRegistry.getInstance().addDefinitionsFile(file, "testModule");
        JCRStoreService.getInstance().deployDefinitions("testModule", null, -1);

        // cleanup
        FileUtils.deleteQuietly(file);
    }

    private void switchToMultipe() throws IOException, ParseException, RepositoryException {
        File file = Files.newTemporaryFile();
        String definition = "<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:test] > nt:base, mix:versionable\n" +
                " - test (string) multiple\n" +
                " - test_multiple (string) multiple\n" +
                " - test_i18n (string) i18n\n";
        // namespace
        FileUtils.writeStringToFile(file, definition);

        // register nodetype
        NodeTypeRegistry.getInstance().addDefinitionsFile(file, "testModule");
        JCRStoreService.getInstance().deployDefinitions("testModule", null, -1);

        // cleanup
        FileUtils.deleteQuietly(file);
    }

    private void switchFromMultiple() throws IOException, ParseException, RepositoryException {
        File file = Files.newTemporaryFile();
        String definition = "<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:test] > nt:base, mix:versionable\n" +
                " - test (string) multiple\n" +
                " - test_multiple (string)\n" +
                " - test_i18n (string) i18n\n";
        // namespace
        FileUtils.writeStringToFile(file, definition);

        // register nodetype
        NodeTypeRegistry.getInstance().addDefinitionsFile(file, "testModule");
        JCRStoreService.getInstance().deployDefinitions("testModule", null, -1);

        // cleanup
        FileUtils.deleteQuietly(file);
    }

    private void switchFromI18n() throws IOException, ParseException, RepositoryException {
        File file = Files.newTemporaryFile();
        String definition = "<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:test] > nt:base, mix:versionable\n" +
                " - test (string)\n" +
                " - test_multiple (string) multiple\n" +
                " - test_i18n (string)\n";
        // namespace
        FileUtils.writeStringToFile(file, definition);

        // register nodetype
        NodeTypeRegistry.getInstance().addDefinitionsFile(file, "testModule");
        JCRStoreService.getInstance().deployDefinitions("testModule", null, -1);

        // cleanup
        FileUtils.deleteQuietly(file);
    }

    private void addMandatoryConstraint() throws IOException, ParseException, RepositoryException {
        File file = Files.newTemporaryFile();
        String definition = "<test = 'http://www.apache.org/jackrabbit/test'>\n" +
                "<nt = 'http://www.jcp.org/jcr/nt/1.0'>\n" +
                "[test:test] > nt:base, mix:versionable\n" +
                " - test (string)\n" +
                " - test_multiple (string) multiple\n" +
                " - test_i18n (string) i18n\n" +
                " - test1 (string) mandatory\n";
        // namespace
        FileUtils.writeStringToFile(file, definition);

        // register nodetype
        NodeTypeRegistry.getInstance().addDefinitionsFile(file, "testModule");
        JCRStoreService.getInstance().deployDefinitions("testModule", null, -1);

        // cleanup
        FileUtils.deleteQuietly(file);
    }

    private void checkOperation(final JCRSessionWrapper session, final  String nodePath, final String modification, final String versionName) {
        String operation;
        // read node
        doOperation("read node", modification, nodePath, new CallBack() {
            @Override
            public void execute() throws Exception {
                JCRNodeWrapper nread = session.getNode(nodePath);
            }
        });

        // copy node
        doOperation("copy node", modification, nodePath, new CallBack() {
            @Override
            public void execute() throws Exception {
                JCRNodeWrapper nread = session.getNode(nodePath);
                nread.copy(testNode, "test-copy", false);
            }
        });

        // read property
        doOperation("read properties", modification, nodePath, new CallBack() {
            @Override
            public void execute() throws Exception {
                JCRNodeWrapper nread = session.getNode(nodePath);
                String s = nread.getProperty("test").getValue().getString();

                results.add(new Result("read property value", modification, "test".equals(s)));
                boolean b = true;

                for (Value v  : nread.getProperty("test_multiple").getValues()) {
                    b &= "test".equals(v.getString());
                }

                results.add(new Result("read multiple property value", modification, b));
                s = nread.getProperty("test_i18n").getValue().getString();
                results.add(new Result("read i18n property value", modification, "test".equals(s)));
            }
        });

        // edit property
        doOperation("edit property", modification, nodePath, new CallBack() {
            @Override
            public void execute() throws Exception {
                JCRNodeWrapper nread = session.getNode(nodePath);
                nread.setProperty("test", "new text");
                session.save();
                // restore previous value
                nread.setProperty("test", "test");
                session.save();
            }
        });

        // remove property
        doOperation("remove property", modification, nodePath, new CallBack() {
            @Override
            public void execute() throws Exception {
                JCRNodeWrapper nread = session.getNode(nodePath);
                nread.getProperty("test").remove();
                session.save();
            }
        });

        //restore version
        doOperation("restore version", modification, nodePath, new CallBack() {
            @Override
            public void execute() throws Exception {
                session.save();
                session.getWorkspace().getVersionManager().checkout(nodePath);
                session.getWorkspace().getVersionManager().restore(nodePath,versionName,true);
                JCRNodeWrapper nread = session.getNode(nodePath);
                String s = nread.getProperty("test").getValue().getString();
                results.add(new Result("read restored property value", modification, "new value".equals(s)));
            }
        });

         // remove node
        doOperation("remove node", modification, nodePath, new CallBack() {
            @Override
            public void execute() throws Exception {
                JCRNodeWrapper nread = session.getNode(nodePath);
                nread.remove();
                session.save();
            }
        });
    }

    private void doOperation(String operation, String modification, String nodePath, CallBack callBack) {
        try {
            callBack.execute();
            results.add(new Result(operation, modification, true));
        } catch (Exception e) {
            results.add(new Result(operation, modification, false, e.getClass().getName()));
            logger.info("unable to perform {} after {}", operation, modification);
        }
    }

    private class Result {
        String modification, operation, detail;
        boolean result;

        public Result(String operation, String modification, boolean result) {
            this.modification = modification;
            this.operation = operation;
            this.result = result;
        }

        public Result(String operation, String modification, boolean result, String detail) {
            this.modification = modification;
            this.operation = operation;
            this.result = result;
            this.detail = detail;
        }
    }

    private interface CallBack {
        void execute() throws Exception;

    }
}
