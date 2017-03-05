/**
 * ==========================================================================================
 * =                   JAHIA'S DUAL LICENSING - IMPORTANT INFORMATION                       =
 * ==========================================================================================
 *
 *                                 http://www.jahia.com
 *
 *     Copyright (C) 2002-2017 Jahia Solutions Group SA. All rights reserved.
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
package org.jahia.test.framework;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;
import org.jahia.exceptions.JahiaInitializationException;
import org.jahia.exceptions.JahiaRuntimeException;
import org.jahia.registries.ServicesRegistry;
import org.jahia.services.content.*;
import org.jahia.services.content.nodetypes.NodeTypeRegistry;
import org.jahia.services.content.rules.RulesListener;
import org.jahia.services.importexport.DocumentViewImportHandler;
import org.jahia.services.usermanager.JahiaUser;
import org.jahia.services.usermanager.JahiaUserManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.RepositoryException;
import java.io.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class DeployResourcesTestExecutionListener extends
        AbstractTestExecutionListener {
    private static Logger LOGGER = LoggerFactory
            .getLogger(DeployResourcesTestExecutionListener.class);

    private static final String TEST_SYSTEM_ID = "currentmodule";
    private static final String MAIN_META_INF = "./src/main/resources/META-INF";
    private static final String TEST_META_INF = "./src/test/resources/META-INF";

    @Override
    public void prepareTestInstance(TestContext testContext) throws Exception {
        addCndFiles(MAIN_META_INF, TEST_SYSTEM_ID);
        addCndFiles(TEST_META_INF, TEST_SYSTEM_ID);
        if (NodeTypeRegistry.getInstance().getFiles(TEST_SYSTEM_ID) != null) {
            JCRStoreService.getInstance().deployDefinitions(TEST_SYSTEM_ID, null, -1);
        }

        addRuleFiles(MAIN_META_INF);
        addRuleFiles(TEST_META_INF);

        importXmlFiles(MAIN_META_INF);
        importXmlFiles(TEST_META_INF);
        
        startSchedulers();
    }
    
    private void startSchedulers() {
        try {
            // start schedulers
            ServicesRegistry.getInstance().getSchedulerService().startSchedulers();
        } catch (JahiaInitializationException e) {
            LOGGER.error(e.getMessage(), e);
            throw new JahiaRuntimeException(e);
        }
    }
    
    private void addCndFiles(String resourcePath, String systemId)
            throws Exception {
        for (File cndFile : listFiles(resourcePath, "*.cnd")) {
            NodeTypeRegistry.getInstance().addDefinitionsFile(cndFile,
                    systemId);
        }
    }

    private void addRuleFiles(String resourcePath) throws Exception {
        for (File dslFile : listFiles(resourcePath, "*.dsl")) {
            for (RulesListener listener : RulesListener.getInstances()) {
                listener.addRulesDescriptor(dslFile);
            }
        }

        for (File drlFile : listFiles(resourcePath, "*.drl")) {
            for (RulesListener listener : RulesListener.getInstances()) {
                List<String> filesAccepted = listener.getFilesAccepted();
                if (filesAccepted.contains(StringUtils.substringAfterLast(
                        drlFile.getPath(), "/"))) {
                    listener.addRules(drlFile);
                }
            }
        }
    }

    private void importXmlFiles(String resourcePath) throws Exception {
        for (final File importFile : listFiles(resourcePath, "*import*.xml")) {
            final String targetPath = "/"
                    + StringUtils.substringAfter(
                            StringUtils.substringBeforeLast(
                                    importFile.getPath(), "."), "import-")
                            .replace('-', '/');

            JahiaUser user = JCRSessionFactory.getInstance().getCurrentUser() != null ? JCRSessionFactory
                    .getInstance().getCurrentUser() : JahiaUserManagerService
                    .getInstance().lookupRootUser().getJahiaUser();
            JCRTemplate.getInstance().doExecuteWithSystemSessionAsUser(user,
                    null, null, new JCRCallback<Boolean>() {
                        public Boolean doInJCR(JCRSessionWrapper session)
                                throws RepositoryException {
                            InputStream is = null;
                            try {
                                is = new FileInputStream(importFile);
                                session.importXML(
                                        targetPath,
                                        is,
                                        ImportUUIDBehavior.IMPORT_UUID_CREATE_NEW,
                                        DocumentViewImportHandler.ROOT_BEHAVIOUR_IGNORE);
                            } catch (IOException e) {
                                LOGGER.error("Cannot create file input stream",
                                        e);
                            } finally {
                                IOUtils.closeQuietly(is);
                            }

                            return null;
                        }
                    });
        }
    }

    private Collection<File> listFiles(String resourcePath, String extension) {
        File dir = new File(resourcePath);
        FileFilter fileFilter = new WildcardFileFilter(extension);
        File[] files = dir.listFiles(fileFilter);
        return files == null ? Collections.<File>emptyList() : Arrays.<File>asList(files); 
    }
}
